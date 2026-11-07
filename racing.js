/*
 * VLOX RACING — dependency-free circuit, car physics and a lap-time optimiser.
 *
 * Runs in the browser (window.Racing) and in Node (module.exports), so the
 * optimiser can be measured headlessly.
 *
 * How the lap time comes down:
 *   - the centreline of the circuit is the starting line (the slow reference);
 *   - an optimiser repeatedly relaxes the line toward its minimum-curvature
 *     shape, clamped to the width of the track, which straightens corners and
 *     cuts apexes without leaving the circuit;
 *   - every candidate line is driven by the same pure-pursuit driver with a
 *     curvature-based speed profile, and the lap is timed in a physics loop.
 *
 * Because the car and the driver are identical for every candidate, a lower
 * lap time means a genuinely better line, not a faster car.
 */
(function (root, factory) {
  if (typeof module === 'object' && module.exports) {
    module.exports = factory();
  } else {
    root.Racing = factory();
  }
})(typeof globalThis !== 'undefined' ? globalThis : this, function () {
  'use strict';

  // ---------------------------------------------------------------------------
  // Car and track constants (metres, seconds, m/s)
  // ---------------------------------------------------------------------------
  const CAR = {
    length: 4.6,
    width: 2.0,
    wheelBase: 2.9,
    maxSpeed: 88,
    maxAccel: 11.5,
    maxBrake: 26,
    maxLatAccel: 17
  };

  const TRACK = {
    width: 14
  };

  // A Baku-inspired street circuit: a long main straight, a castle section of
  // tight 90 degree corners, then sweeping exits back onto the straight.
  const CONTROL = [
    { x: 100, y: 620 },
    { x: 700, y: 620 },
    { x: 860, y: 592 },
    { x: 940, y: 480 },
    { x: 930, y: 350 },
    { x: 850, y: 285 },
    { x: 760, y: 300 },
    { x: 715, y: 240 },
    { x: 740, y: 165 },
    { x: 690, y: 100 },
    { x: 560, y: 95 },
    { x: 520, y: 160 },
    { x: 440, y: 175 },
    { x: 395, y: 250 },
    { x: 330, y: 265 },
    { x: 285, y: 340 },
    { x: 210, y: 370 },
    { x: 150, y: 440 },
    { x: 95, y: 520 }
  ];

  // Start/finish sits part-way down the main straight, not in a corner.
  const START_ANCHOR = { x: 330, y: 620 };

  // ---------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------
  const clamp = (v, lo, hi) => (v < lo ? lo : v > hi ? hi : v);
  const lerp = (a, b, t) => a + (b - a) * t;
  const lerpPoint = (a, b, t) => ({ x: lerp(a.x, b.x, t), y: lerp(a.y, b.y, t) });
  const dist = (a, b) => Math.hypot(b.x - a.x, b.y - a.y);

  function wrapAngle(a) {
    while (a > Math.PI) a -= Math.PI * 2;
    while (a < -Math.PI) a += Math.PI * 2;
    return a;
  }

  // ---------------------------------------------------------------------------
  // Closed centripetal Catmull-Rom spline (no cusps, no self-intersection)
  // ---------------------------------------------------------------------------
  function catmullRom(p0, p1, p2, p3, t) {
    const eps = 1e-4;
    const d1 = Math.max(Math.pow(dist(p0, p1), 0.5), eps);
    const d2 = Math.max(Math.pow(dist(p1, p2), 0.5), eps);
    const d3 = Math.max(Math.pow(dist(p2, p3), 0.5), eps);
    const t0 = 0;
    const t1 = t0 + d1;
    const t2 = t1 + d2;
    const t3 = t2 + d3;
    const u = t1 + (t2 - t1) * t;

    const a1 = lerpPoint(p0, p1, (u - t0) / (t1 - t0));
    const a2 = lerpPoint(p1, p2, (u - t1) / (t2 - t1));
    const a3 = lerpPoint(p2, p3, (u - t2) / (t3 - t2));
    const b1 = lerpPoint(a1, a2, (u - t0) / (t2 - t0));
    const b2 = lerpPoint(a2, a3, (u - t1) / (t3 - t1));
    return lerpPoint(b1, b2, (u - t1) / (t2 - t1));
  }

  function sampleClosedSpline(points, perSegment) {
    const n = points.length;
    const out = [];
    for (let i = 0; i < n; i++) {
      const p0 = points[(i - 1 + n) % n];
      const p1 = points[i];
      const p2 = points[(i + 1) % n];
      const p3 = points[(i + 2) % n];
      for (let j = 0; j < perSegment; j++) {
        out.push(catmullRom(p0, p1, p2, p3, j / perSegment));
      }
    }
    return out;
  }

  function resampleClosed(points, count) {
    const n = points.length;
    const segments = [];
    let total = 0;
    for (let i = 0; i < n; i++) {
      const a = points[i];
      const b = points[(i + 1) % n];
      const len = dist(a, b);
      segments.push({ a, b, len, start: total });
      total += len;
    }

    const out = [];
    let si = 0;
    for (let k = 0; k < count; k++) {
      const target = (k / count) * total;
      while (si < segments.length - 1 && segments[si].start + segments[si].len < target) si++;
      const s = segments[si];
      const f = s.len > 1e-9 ? (target - s.start) / s.len : 0;
      out.push(lerpPoint(s.a, s.b, f));
    }
    return { points: out, length: total };
  }

  // ---------------------------------------------------------------------------
  // Geometry of a closed loop: tangents, normals, spacing, curvature, length
  // ---------------------------------------------------------------------------
  function describeLoop(points) {
    const n = points.length;
    const tangents = new Array(n);
    const normals = new Array(n);
    const ds = new Float64Array(n);
    const curvature = new Float64Array(n);
    let length = 0;

    for (let i = 0; i < n; i++) {
      const prev = points[(i - 1 + n) % n];
      const next = points[(i + 1) % n];
      let tx = next.x - prev.x;
      let ty = next.y - prev.y;
      const len = Math.hypot(tx, ty) || 1;
      tx /= len;
      ty /= len;
      tangents[i] = { x: tx, y: ty };
      normals[i] = { x: -ty, y: tx };

      const step = dist(points[i], next);
      ds[i] = step;
      length += step;
    }

    // Menger curvature: k = 2 * area / (a * b * c) for the triangle i-1, i, i+1
    for (let i = 0; i < n; i++) {
      const a = points[(i - 1 + n) % n];
      const b = points[i];
      const c = points[(i + 1) % n];
      const ab = dist(a, b);
      const bc = dist(b, c);
      const ca = dist(c, a);
      const doubleArea = Math.abs((b.x - a.x) * (c.y - a.y) - (c.x - a.x) * (b.y - a.y));
      const denom = ab * bc * ca;
      curvature[i] = denom > 1e-9 ? (2 * doubleArea) / denom : 0;
    }

    return { points, tangents, normals, ds, curvature, length, count: n };
  }

  // Speed limit per point from curvature, then a backward braking pass so the
  // profile never asks for a speed the car cannot slow down to in time.
  function speedProfile(loop) {
    const n = loop.count;
    const v = new Float64Array(n);

    for (let i = 0; i < n; i++) {
      const k = loop.curvature[i];
      v[i] = k > 1e-6 ? Math.min(CAR.maxSpeed, Math.sqrt(CAR.maxLatAccel / k)) : CAR.maxSpeed;
    }

    for (let pass = 0; pass < 2; pass++) {
      for (let i = n - 1; i >= 0; i--) {
        const j = (i + 1) % n;
        const cap = Math.sqrt(v[j] * v[j] + 2 * CAR.maxBrake * loop.ds[i]);
        if (cap < v[i]) v[i] = cap;
      }
    }

    return v;
  }

  function lineFromPoints(points) {
    const loop = describeLoop(points);
    loop.speeds = speedProfile(loop);
    return loop;
  }

  // ---------------------------------------------------------------------------
  // Track
  // ---------------------------------------------------------------------------
  function createTrack(opts) {
    opts = opts || {};
    const samples = opts.samples || 480;
    const perSegment = opts.perSegment || 14;
    const control = opts.control || CONTROL;
    const width = opts.width || TRACK.width;

    const spline = sampleClosedSpline(control, perSegment);
    const resampled = resampleClosed(spline, samples);

    // Rotate so index 0 is the start/finish point on the main straight.
    const anchor = opts.startAt || START_ANCHOR;
    let startIdx = 0;
    let bestD = Infinity;
    for (let i = 0; i < resampled.points.length; i++) {
      const d = dist(resampled.points[i], anchor);
      if (d < bestD) {
        bestD = d;
        startIdx = i;
      }
    }
    const rotated = resampled.points
      .slice(startIdx)
      .concat(resampled.points.slice(0, startIdx));

    const track = describeLoop(rotated);
    track.length = resampled.length;
    track.width = width;
    track.halfWidth = width / 2;
    track.control = control;
    track.startAnchor = anchor;
    return track;
  }

  // How far a line may wander from the centreline and still keep the car on
  // the road.
  function maxOffset(track) {
    return Math.max(0, track.halfWidth - CAR.width * 0.55);
  }

  function clonePoints(points) {
    return points.map((p) => ({ x: p.x, y: p.y }));
  }

  // ---------------------------------------------------------------------------
  // Index helpers
  // ---------------------------------------------------------------------------
  function nearestIndex(points, guess, x, y, windowSize) {
    const n = points.length;
    let best = guess;
    let bestD = Infinity;
    for (let k = -windowSize; k <= windowSize; k++) {
      const i = (guess + k + n * 2) % n;
      const dx = points[i].x - x;
      const dy = points[i].y - y;
      const d = dx * dx + dy * dy;
      if (d < bestD) {
        bestD = d;
        best = i;
      }
    }
    return best;
  }

  function pointAt(loop, index, ahead) {
    const n = loop.count;
    let i = index;
    let remain = ahead;
    let guard = 0;
    while (remain > loop.ds[i] && guard++ < n) {
      remain -= loop.ds[i];
      i = (i + 1) % n;
    }
    const a = loop.points[i];
    const b = loop.points[(i + 1) % n];
    const f = loop.ds[i] > 1e-9 ? remain / loop.ds[i] : 0;
    return { x: lerp(a.x, b.x, f), y: lerp(a.y, b.y, f), index: i };
  }

  // ---------------------------------------------------------------------------
  // The driver: pure pursuit for steering, curvature profile for speed
  // ---------------------------------------------------------------------------
  function createSim(track, line) {
    const n = track.count;
    const state = {
      x: line.points[0].x,
      y: line.points[0].y,
      heading: Math.atan2(line.tangents[0].y, line.tangents[0].x),
      speed: 0,
      time: 0,
      index: 0,
      progress: 0,
      offTrackTime: 0,
      maxSpeed: 0,
      lapTime: null,
      finished: false
    };

    function step(dt) {
      if (state.finished) return state;

      const idx = nearestIndex(line.points, state.index, state.x, state.y, 8);
      let delta = idx - state.index;
      if (delta > n / 2) delta -= n;
      if (delta < -n / 2) delta += n;
      state.progress += delta * (line.length / n);
      state.index = idx;

      const c = track.points[idx];
      const nrm = track.normals[idx];
      const lateral = (state.x - c.x) * nrm.x + (state.y - c.y) * nrm.y;
      const onTrack = Math.abs(lateral) <= track.halfWidth - CAR.width * 0.2;
      if (!onTrack) state.offTrackTime += dt;

      if (state.progress >= line.length * 0.998) {
        state.finished = true;
        state.lapTime = state.time;
        return state;
      }

      const v = state.speed;
      const lookahead = clamp(7 + 0.8 * v, 9, 45);
      const target = pointAt(line, idx, lookahead);
      const alpha = wrapAngle(Math.atan2(target.y - state.y, target.x - state.x) - state.heading);
      let steer = Math.atan2(2 * CAR.wheelBase * Math.sin(alpha), lookahead);

      const latLimit = onTrack ? CAR.maxLatAccel : CAR.maxLatAccel * 0.5;
      const steerLimit = Math.atan((latLimit * CAR.wheelBase) / Math.max(v * v, 1));
      steer = clamp(steer, -steerLimit, steerLimit);

      // Slowest speed anywhere inside the braking horizon.
      const horizon = Math.max(14, v * 0.75);
      let targetSpeed = CAR.maxSpeed;
      for (let s = 0; s <= 6; s++) {
        const probe = pointAt(line, idx, (horizon * s) / 6);
        if (line.speeds[probe.index] < targetSpeed) targetSpeed = line.speeds[probe.index];
      }

      let accel;
      if (targetSpeed > v) {
        accel = CAR.maxAccel * Math.min(1, (targetSpeed - v) / 1.5);
      } else {
        accel = -CAR.maxBrake * Math.min(1, (v - targetSpeed) / 2);
      }
      if (!onTrack) accel -= 7;

      state.speed = clamp(v + accel * dt, 0, CAR.maxSpeed);
      state.heading += ((state.speed / CAR.wheelBase) * Math.tan(steer)) * dt;
      state.x += Math.cos(state.heading) * state.speed * dt;
      state.y += Math.sin(state.heading) * state.speed * dt;
      state.time += dt;
      if (state.speed > state.maxSpeed) state.maxSpeed = state.speed;

      return state;
    }

    return { state, step };
  }

  function simulate(track, line, opts) {
    opts = opts || {};
    const dt = opts.dt || 1 / 60;
    const maxTime = opts.maxTime || 240;
    const sim = createSim(track, line);

    while (sim.state.time < maxTime && !sim.state.finished) sim.step(dt);

    return {
      lapTime: sim.state.finished ? sim.state.lapTime : Infinity,
      finished: sim.state.finished,
      offTrackTime: sim.state.offTrackTime,
      topSpeed: sim.state.maxSpeed,
      time: sim.state.time
    };
  }

  // ---------------------------------------------------------------------------
  // Candidate line -> scored result
  // ---------------------------------------------------------------------------
  function scoreLine(track, points, dt) {
    const line = lineFromPoints(points);
    const result = simulate(track, line, { dt: dt });
    return {
      line,
      points,
      lapTime: result.lapTime,
      offTrackTime: result.offTrackTime,
      topSpeed: result.topSpeed,
      finished: result.finished,
      fitness: (result.finished ? result.lapTime : 500) + result.offTrackTime * 4
    };
  }

  // One relaxation sweep: each point drifts toward the midpoint of its
  // neighbours (which reduces curvature) but is clamped to the track width, so
  // the line straightens corners and cuts apexes without leaving the road.
  function relaxOnce(track, points, alpha) {
    const n = points.length;
    const lim = maxOffset(track);
    const next = new Array(n);

    for (let i = 0; i < n; i++) {
      const prev = points[(i - 1 + n) % n];
      const cur = points[i];
      const nxt = points[(i + 1) % n];

      const midX = (prev.x + nxt.x) / 2;
      const midY = (prev.y + nxt.y) / 2;
      const px = cur.x + (midX - cur.x) * alpha;
      const py = cur.y + (midY - cur.y) * alpha;

      const c = track.points[i];
      const nr = track.normals[i];
      const lateral = clamp((px - c.x) * nr.x + (py - c.y) * nr.y, -lim, lim);
      next[i] = { x: c.x + nr.x * lateral, y: c.y + nr.y * lateral };
    }
    return next;
  }

  // ---------------------------------------------------------------------------
  // Optimiser
  // ---------------------------------------------------------------------------
  function createOptimizer(track, options) {
    const cfg = Object.assign(
      {
        alpha: 0.08,       // relaxation step
        maxIterations: 600,
        patience: 70,      // stop after this many sweeps without improvement
        dt: 1 / 90
      },
      options
    );

    const baselineLine = lineFromPoints(clonePoints(track.points));
    const baselineResult = scoreLine(track, clonePoints(track.points), cfg.dt);

    let points = clonePoints(track.points);
    let current = { line: baselineLine, lapTime: baselineResult.lapTime, fitness: baselineResult.fitness };
    let best = {
      line: baselineLine,
      points: clonePoints(track.points),
      lapTime: baselineResult.lapTime,
      offTrackTime: baselineResult.offTrackTime,
      topSpeed: baselineResult.topSpeed,
      fitness: baselineResult.fitness,
      iteration: 0
    };

    let iteration = 0;
    let stagnant = 0;
    let done = false;

    function step() {
      if (done) return snapshot();

      points = relaxOnce(track, points, cfg.alpha);
      const scored = scoreLine(track, points, cfg.dt);
      iteration++;

      current = {
        line: scored.line,
        lapTime: scored.lapTime,
        offTrackTime: scored.offTrackTime,
        fitness: scored.fitness
      };

      if (scored.fitness < best.fitness - 1e-4) {
        best = {
          line: scored.line,
          points: clonePoints(points),
          lapTime: scored.lapTime,
          offTrackTime: scored.offTrackTime,
          topSpeed: scored.topSpeed,
          fitness: scored.fitness,
          iteration
        };
        stagnant = 0;
      } else {
        stagnant++;
      }

      if (stagnant >= cfg.patience || iteration >= cfg.maxIterations) done = true;
      return snapshot();
    }

    function snapshot() {
      return {
        iteration,
        done,
        currentLine: current.line,
        currentLapTime: current.lapTime,
        currentOffTrack: current.offTrackTime,
        best: best,
        bestLapTime: best.lapTime,
        bestPoints: best.points,
        baselineLapTime: baselineResult.lapTime,
        gain: baselineResult.lapTime - best.lapTime
      };
    }

    return { step, snapshot, baselineLine, baselineResult, config: cfg };
  }

  return {
    CAR,
    TRACK,
    CONTROL,
    START_ANCHOR,
    createTrack,
    lineFromPoints,
    maxOffset,
    clonePoints,
    relaxOnce,
    scoreLine,
    createSim,
    simulate,
    createOptimizer,
    describeLoop,
    speedProfile
  };
});
