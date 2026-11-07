/*
 * Headless benchmark: run the optimiser to convergence and report how much lap
 * time it takes off the centreline baseline.
 *
 *   node tools/bench.js [maxIterations]
 */
const Racing = require('../racing.js');

const maxIterations = Number(process.argv[2] || 600);

const track = Racing.createTrack();
const optimiser = Racing.createOptimizer(track, { maxIterations });

const baseline = optimiser.baselineResult;
console.log('track length      : ' + track.length.toFixed(0) + ' m');
console.log('centreline lap    : ' + baseline.lapTime.toFixed(3) + ' s' +
  '  (off track ' + baseline.offTrackTime.toFixed(2) + ' s)');
console.log('');

const started = Date.now();
let snap = optimiser.snapshot();

while (!snap.done) {
  snap = optimiser.step();
  if (snap.iteration === 1 || snap.iteration % 25 === 0) {
    console.log(
      'iter ' + String(snap.iteration).padStart(3) +
      '  current ' + snap.currentLapTime.toFixed(3) + ' s' +
      '  best ' + snap.bestLapTime.toFixed(3) + ' s' +
      '  delta ' + (snap.bestLapTime - baseline.lapTime).toFixed(3) + ' s'
    );
  }
}

const elapsed = Date.now() - started;
const gain = baseline.lapTime - snap.bestLapTime;

console.log('');
console.log('iterations        : ' + snap.iteration + (snap.done ? ' (converged)' : ''));
console.log('solve time        : ' + elapsed + ' ms');
console.log('best lap          : ' + snap.bestLapTime.toFixed(3) + ' s');
console.log('best off track    : ' + snap.best.offTrackTime.toFixed(2) + ' s');
console.log('improvement       : ' + gain.toFixed(3) + ' s (' +
  ((gain / baseline.lapTime) * 100).toFixed(2) + '%)');

let failed = false;
if (!Number.isFinite(snap.bestLapTime)) {
  console.error('FAIL: optimiser produced no valid lap');
  failed = true;
}
if (gain <= 0) {
  console.error('FAIL: optimiser did not beat the baseline');
  failed = true;
}
if (snap.best.offTrackTime > 0.5) {
  console.error('FAIL: best line leaves the track for ' + snap.best.offTrackTime.toFixed(2) + ' s');
  failed = true;
}
if (failed) process.exit(1);

console.log('PASS: optimiser beat the baseline and stayed on track');
