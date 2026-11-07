#Requires AutoHotkey v2.0
#SingleInstance Force
SendMode "Input"
SetWorkingDir A_ScriptDir

; =============================================================================
; VLOX AUTO - repeated key actions for F1 Apex Racing (Baku)
;
; HOW TO USE
;   1. Install AutoHotkey v2 from https://www.autohotkey.com
;   2. Double-click this file.
;   3. Join the track in Roblox, click the game window so it is focused.
;   4. Press F7 to start. Press F7 again to stop. F9 quits the script.
;
; Everything below in CFG is yours to change. Save the file and reload the
; script (right-click the tray icon -> Reload) after editing.
; =============================================================================

CFG := {
    ; Keys sent once per cycle, in order. Examples:
    ;   ["w"]              -> press W over and over (gas / throttle)
    ;   ["w", "a"]         -> W then A, repeating
    ;   ["space"]          -> press Space repeatedly
    ;   ["w", "d", "w", "a"] -> weave left/right
    Keys          : ["w"],

    HoldMs        : 60,      ; how long each key stays down (milliseconds)
    KeyGapMs      : 40,      ; pause between keys inside one cycle (ms)
    CycleGapMs    : 840,     ; pause after a full cycle before repeating (ms)
    StartDelayMs  : 2000,    ; countdown before the first press, so you can
                             ; get your hands on the keyboard (ms)

    OnlyWhenRoblox: true,    ; safety: only send keys while Roblox is focused
    RobloxExe     : "ahk_exe RobloxPlayerBeta.exe"
}

TOGGLE_KEY := "F7"   ; change to a key Roblox itself does not use
QUIT_KEY   := "F9"

; -----------------------------------------------------------------------------
; Hotkeys
; -----------------------------------------------------------------------------
F7::ToggleAuto()
F9::ExitApp()

; -----------------------------------------------------------------------------
; Logic
; -----------------------------------------------------------------------------
global Running := false

ToggleAuto() {
    global Running, CFG

    if Running {
        Running := false
        SetTimer(RunCycle, 0)
        ToolTip("VLOX AUTO: stopped")
        SetTimer(ClearTip, -1500)
        return
    }

    if (CFG.OnlyWhenRoblox && !WinActive(CFG.RobloxExe)) {
        ToolTip("VLOX AUTO: Roblox is not the active window")
        SetTimer(ClearTip, -2500)
        return
    }

    Running := true
    ToolTip("VLOX AUTO: starting in " Round(CFG.StartDelayMs / 1000) "s")
    SetTimer(ClearTip, -CFG.StartDelayMs)
    SetTimer(RunCycle, -CFG.StartDelayMs)
}

RunCycle() {
    global Running, CFG

    if (!Running)
        return

    if (CFG.OnlyWhenRoblox && !WinActive(CFG.RobloxExe)) {
        Running := false
        SetTimer(RunCycle, 0)
        ToolTip("VLOX AUTO: stopped (Roblox lost focus)")
        SetTimer(ClearTip, -2500)
        return
    }

    for key in CFG.Keys {
        if (!Running)
            break
        Send "{" key " down}"
        Sleep CFG.HoldMs
        Send "{" key " up}"
        Sleep CFG.KeyGapMs
    }

    if (Running) {
        Sleep CFG.CycleGapMs
        if (Running)
            SetTimer(RunCycle, -1)
    }
}

ClearTip(*) {
    ToolTip()
}
