# Almanac — UI Fix & Polish Pass (Phase 3.5)

Scope (agreed): fix the UI and the listed bugs only.
DEFERRED (per user): timezone day-tracking, sleep logging, notifications, home/lock widgets.
Camera: REPLACE in-app CameraX capture with a system camera app OR gallery chooser (user confirmed RIP OUT on 2026-07-16).
NEW (user): habit color-picker UX (was not in this plan; added during reconciliation).

## Bugs (fix)
1. **Onboarding flashes then jumps to main** — `MainActivity` uses `initialValue = false`,
   so on first real emit (true) it swaps Onboarding→NavHost in one frame. Fix: hold a
   `ready` flag, only decide the gate after the flow has emitted, and keep Onboarding shown
   until then. (`MainActivity.kt`)
2. **Settings shows two theme toggles** — top-bar `ThemeToggleChip` + the segmented Ink/Parchment
   control in the Appearance section. Both edit the same pref. Fix: remove the top-bar chip
   from Settings (keep the in-screen segmented control as the canonical one).
3. **New Habit sheet renders half-visible** — `ModalBottomSheet` content likely clipped; the
   sheet needs `windowInsets = WindowInsets(0,0,0,0)` and the content uses
   `.navigationBarsPadding()`. Fix the sheet insets + ensure full content scrolls.
4. **FINIS OPUS / flourish copy reads as nonsense** — remove `finis_opus` string usages from
   Insights, Habits, Settings. Keep the `colophon` line (it's fine); drop the brass "FINIS OPUS".
5. **Text entry non-functional button (pre-fix era)** — verify `LedgerTextField` + Stamp button
   actually work in current build (the type chips: Text sets type, but the text field only shows
   when `state.type == TEXT || (type==null && text not blank)`). The "Text" chip currently sets
   type then the field appears; confirm tap-through. Improve: when Photo chip selected, text field
   should still be available for caption (already is). Verify and keep.

## Design reversals (real use overturned)
6. **Per-screen theme toggle on every screen** — KILL `ThemeToggleChip` from Today, Month,
   Insights, NewEntry, EntryDetail top bars. Keep ONLY in Settings (segmented control) and
   Onboarding (optional). Replace the now-free top-bar action slot with something useful:
   - Today: keep filter bar; no chip.
   - Month: keep nav chevrons; no chip.
   - Insights/NewEntry/EntryDetail: no chip.
7. **Quiet "+" affordances too invisible** — give `AddEntryAffordance` (Today) and
   `AddHabitAffordance` (Habits) real visual weight: a bordered row / outlined button with
   moss border + filled-on-press, not a ghost glyph. Short of a filled FAB, but clearly tappable.
8. **Flourish copy** — covered in #4.

## Polish gaps
9. **Today / home date box not readable** — the home screen has no big date box; the complaint is
   the Today top-bar date stack (`displaySmall` "Today" + body date). Improve hierarchy: keep the
   eyebrow, make the date line a proper `StampType`/readable treatment, ensure contrast on both
   themes. Optionally add a tasteful large date treatment. Keep it quiet per AGENTS (boldness on
   the date stamp).
10. **Month view "too ugly"** — revamp `DayCell`: better spacing, clearer selected/empty states,
    mood wash kept subtle, fix the per-cell layout so numbers don't crowd. Keep brass borders +
    subtle mood wash (per Stitch reconciliation). Improve the weekday header + grid gutters.
11. **Insights page looks bad / "stupid text"** — the complaint is likely the redundant/ugly
    empty states + the FINIS OPUS. Improve: tighter section spacing, give charts a bordered card
    frame, clean empty states, remove FINIS OPUS. Keep charts (they're fine functionally).
12. **Export "looks shit"** — PDF export (`LedgerExport`/SettingsViewModel.exportPdf). Improve the
    PDF layout: proper title, date, entries as readable rows, not a raw dump. (Verify current
    export code first; this is Phase 3 code.)
13. **General UI scaling** — ensure `Modifier.fillMaxWidth` + `Arrangement.spacedBy` + dp padding
    everywhere; check no fixed widths that overflow on small screens. Audit the main screens.
14. **"Where are my animations"** — verify the stamp bounce (NewEntry), the Month cell bleed, the
    DayCell animation actually run. They're implemented; the concern is they may be near-invisible.
    Make them perceptible (slightly longer/larger) or confirm. Keep within tasteful bounds.

## Camera (new, simple)
15. **Camera: menu → system camera app OR gallery** — remove the in-app CameraX `CameraCaptureView`
    and the camera-permission primer flow. NewEntry "Photo" chip opens a small menu (AlertDialog or
    a chooser): "Camera" launches `ActivityResultContracts.TakePicture` (system camera app, saves to
    a FileProvider/File uri) and "Gallery" launches `GetContent`. This avoids the in-app CameraX
    complexity entirely. Update AGENTS.md to drop the CameraX main-thread rule (no longer used) or
    note it's retired. Keep geotag + caption flow.

## Verification
- `./gradlew lintDebug` and `./gradlew testDebugUnitTest` must pass.
- Fresh `./gradlew assembleDebug`; recommend user installs and taps through: onboarding, Today add,
  Habits add (full sheet), Month, Insights, Settings (single theme control), NewEntry Photo menu
  (camera + gallery), export PDF.

## Order
A. MainActivity onboarding fix (#1)
B. Remove per-screen theme chips (#2, #6) + Settings dedupe
C. Camera simplification (#15)
D. Habit sheet fix (#3) + affordance weight (#7)
E. Flourish copy removal (#4)
F. Polish: Today (#9), Month (#10), Insights (#11), scaling (#13), animations (#14)
G. Export PDF improvement (#12)
H. Build + lint + unit tests; update AGENTS.md camera note.
