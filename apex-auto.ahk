#Requires AutoHotkey v2.0
#SingleInstance Force
SendMode "Input"
SetWorkingDir A_ScriptDir

; =============================================================================
; VLOX AUTO - plays a timed sequence of key presses, so a whole lap can be
; choreographed as a list of phases and looped.
;
; HOW TO USE
;   1. Install AutoHotkey v2 from https://www.autohotkey.com
;   2. Double-click this file.
;   3. Join the track in Roblox and click the game window so it is focused.
;   4. Press F7 to start. F7 again stops it and releases every key. F9 quits.
;
; Each phase below is: hold some keys for a number of milliseconds, then
; release them. Optional fields:
;   repeat : how many times to do this phase (default 1)
;   gap    : milliseconds to wait between repeats (default 0)
;   label  : shown in the on-screen status while the phase runs
;
; Keys held together are listed together, e.g. ["w", "a"] is throttle + steer.
; A phase with an empty key list (keys: []) just waits.
; =============================================================================

LAP := [
    ; --- main straight ---------------------------------------------------
    { keys: ["w"],            ms: 2600, label: "full throttle" },
    { keys: ["w"],            ms: 900,  repeat: 3, gap: 0, label: "straight" },

    ; --- turn 1: brake, then turn in while on the throttle ---------------
    { keys: ["s"],            ms: 500,  label: "braking" },
    { keys: ["w", "a"],       ms: 900,  label: "turn in" },
    { keys: ["w"],            ms: 700,  label: "exit" },

    ; --- castle section: short bursts of steering -------------------------
    { keys: ["w", "d"],       ms: 600,  label: "right hander" },
    { keys: ["w"],            ms: 400,  label: "straighten" },
    { keys: ["s"],            ms: 350,  label: "brake" },
    { keys: ["w", "a"],       ms: 1100, label: "long left" },

    ; --- a repeated tap, e.g. a shift or a look-around every second -------
    { keys: ["e"],            ms: 60,   repeat: 5, gap: 940, label: "cycle action" },

    ; --- final section back onto the straight ----------------------------
    { keys: ["w"],            ms: 1500, label: "power down" },
    { keys: ["s"],            ms: 600,  label: "brake for the last corner" },
    { keys: ["w", "d"],       ms: 1000, label: "last corner" },
    { keys: ["w"],            ms: 1200, label: "onto the straight" }
]

LAPS            := 0        ; how many laps to play; 0 = loop forever
BETWEEN_LAPS_MS := 3000     ; pause between laps
START_DELAY_MS  := 3000     ; countdown before the first phase
ONLY_WHEN_ROBLOX := true    ; never send keys to any other window
ROBLOX_EXE      := "ahk_exe RobloxPlayerBeta.exe"
TIP_X           := 20       ; where the status tooltip appears
TIP_Y           := 20

; -----------------------------------------------------------------------------
; Hotkeys
; -----------------------------------------------------------------------------
F7::ToggleLap()
F9::ExitApp()

; -----------------------------------------------------------------------------
; State
; -----------------------------------------------------------------------------
global Running := false
global SessionActive := false
global Held := []

; -----------------------------------------------------------------------------
; Start / stop
; -----------------------------------------------------------------------------
ToggleLap() {
    global Running

    if Running {
        Running := false
        ReleaseHeld()
        ShowTip("stopped")
        SetTimer(HideTip, -1500)
        return
    }

    if (SessionActive) {
        ShowTip("still stopping - try again in a moment")
        SetTimer(HideTip, -2500)
        return
    }

    if (ONLY_WHEN_ROBLOX && !WinActive(ROBLOX_EXE)) {
        ShowTip("Roblox is not the active window")
        SetTimer(HideTip, -2500)
        return
    }

    Running := true
    ShowTip("starting in " Round(START_DELAY_MS / 1000) "s")
    SetTimer(RunLaps, -START_DELAY_MS)
}

StopForFocusLoss() {
    global Running
    Running := false
    ReleaseHeld()
    ShowTip("stopped - Roblox lost focus")
    SetTimer(HideTip, -2500)
}

; -----------------------------------------------------------------------------
; The lap runner
; -----------------------------------------------------------------------------
RunLaps(*) {
    global Running, SessionActive

    SessionActive := true
    lap := 0
    while (Running && (LAPS = 0 || lap < LAPS)) {
        lap++

        for index, phase in LAP {
            if (!Running)
                break

            if (ONLY_WHEN_ROBLOX && !WinActive(ROBLOX_EXE)) {
                StopForFocusLoss()
                return
            }

            label := phase.HasOwnProp("label") ? phase.label : "phase " index
            ShowTip("lap " lap " - " label)
            if (!RunPhase(phase))
                break
        }

        if (Running && BETWEEN_LAPS_MS)
            Sleep BETWEEN_LAPS_MS
    }

    ReleaseHeld()
    SessionActive := false

    if (Running) {
        Running := false
        ShowTip("all laps complete")
        SetTimer(HideTip, -2500)
    }
}

RunPhase(phase) {
    global Running

    count := phase.HasOwnProp("repeat") ? phase.repeat : 1
    gap := phase.HasOwnProp("gap") ? phase.gap : 0

    index := 0
    while (index < count) {
        index++
        if (!Running)
            return false

        HoldKeys(phase.keys)
        Sleep phase.ms
        ReleaseHeld()

        if (gap && index < count)
            Sleep gap
    }

    return Running
}

; -----------------------------------------------------------------------------
; Key handling
; -----------------------------------------------------------------------------
HoldKeys(keys) {
    global Held
    for key in keys {
        Send "{" key " down}"
        Held.Push(key)
    }
}

ReleaseHeld() {
    global Held
    for key in Held
        Send "{" key " up}"
    Held := []
}

; -----------------------------------------------------------------------------
; Status tooltip
; -----------------------------------------------------------------------------
ShowTip(text) {
    ToolTip("VLOX AUTO: " text, TIP_X, TIP_Y)
}

HideTip(*) {
    ToolTip()
}
