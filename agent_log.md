# Agent Log — Phase 3.5 UI Fix & Polish Pass

Date: 2026-07-16
Scope: fix UI + listed bugs; rip out in-app CameraX (user decision); keep timezone/sleep/notifications/widgets DEFERRED; add habit-color UX fix (new).

## Context
User dumped `App to-do list features.html` (raw complaints) and pointed at `.planning/phase35_ui_fixes_plan.md` (the AI's prior proposed plan). Reconciled the two. Three decisions taken before coding:
- **Camera:** RIP OUT in-app CameraX, replace with system camera app + gallery chooser (plan #15, user-confirmed).
- **Deferred:** timezone day-tracking, sleep logging, notifications, widgets — stay out of this pass.
- **Habit color:** NEW item added (user's "wtf does choosing color do").

## Changes made (per item)

### 1. Onboarding flash to main jump  (`MainActivity.kt`)
- Root cause: `collectAsStateWithLifecycle(initialValue = false)` swaps Onboarding to NavHost in one frame on first real emit (true).
- Fix: added `var ready by remember { mutableStateOf(false) }`; a `LaunchedEffect(Unit)` collects `onboardingComplete.first()` then sets `ready = true`. Gate is now `if (ready && onboardingComplete)`.

### 2/6. Per-screen theme chips removed + Settings dedupe
- Removed `ThemeToggleChip` action + import from `TodayScreen.kt`, `MonthScreen.kt`, `NewEntryScreen.kt`, `InsightsScreen.kt`.
- `SettingsScreen.kt`: removed top-bar `ThemeToggleChip` (kept the in-screen segmented Ink/Parchment control as canonical).
- `ThemeToggleChip` remains only in `StyleSpecimenScreen` (a design-preview screen). `onToggleTheme` param left on the other screens (still passed by NavHost, harmless).

### 14. Camera rip-out to system camera / gallery  (`NewEntryScreen.kt`, `AGENTS.md`, `strings.xml`)
- Removed `CameraCaptureView` composable and all `androidx.camera.*` usage.
- Photo chip now opens an `AlertDialog` chooser:
  - "Capture" → `ActivityResultContracts.TakePicture` to an app-private `FileProvider` URI (`FileStorage.newCaptureFile()` / `uriForFile()`).
  - "From gallery" → `ActivityResultContracts.GetContent`.
- `PhotoPreview` retake reopens the chooser.
- Added strings `new_entry_photo_source_title` / `new_entry_photo_source_body`.
- `AGENTS.md`: marked CameraX RETIRED; rewrote the main-thread rule (now Play-Services-only; CameraX path gone). Updated tech-stack line 27.

### 3. Habit "New Habit" sheet half-visible  (`HabitsScreen.kt`)
- Added `.navigationBarsPadding()` to the sheet content `Column` (removed the attempted `windowInsets` param — not present in this Material3 BOM).

### 15 (NEW). Habit color UX  (`HabitsScreen.kt`, `HabitTints.kt`, `strings.xml`)
- `habits_color` string renamed "Ink" to "Color"; added `habits_color_hint` ("The tint that marks this habit's stamp on the ledger.").
- Color row now renders labeled swatches (Brass / Moss / Dusty Rose) with a selected brass ring + name, instead of anonymous color squares. Added `habitTintLabel()` helper.

### 4. FINIS OPUS removed  (`InsightsScreen.kt`, `HabitsScreen.kt`, `SettingsScreen.kt`, `strings.xml`)
- Removed `finis_opus` Text from all three screens (kept the `colophon` line). Deleted the `finis_opus` string resource.

### 8. Today date hierarchy  (`TodayScreen.kt`)
- Added a moss divider between "Today" and the date line; date now renders in `AlmanacTypography.titleMedium` / `onSurface` (was bodyMedium onSurfaceVariant, low contrast).

### 9/10. Month DayCell revamp  (`MonthScreen.kt`)
- Removed the redundant full `DateStamp` (month+day+year, busy at 44dp) PLUS a second day-of-month number — that duplication was the "crowding/ugly".
- New `DayCell`: square `aspectRatio(1f)` cell, day number in `StampType.stampDate`. Entry days get a subtle mood wash + 1dp brass border; today gets a 1.5dp brass selected border. Bleed animation kept. `DateStamp` import dropped.

### 11. Insights chart cards  (`InsightsScreen.kt`)
- Wrapped mood/frequency charts and the habits-empty state in a bordered `InsightCard` (1dp moss border). Keeps charts; removes the redundant "stupid text" (FINIS OPUS already gone).

### 12. Export PDF improvement  (`LedgerExport.kt`)
- `buildFieldReportPdf` rewritten: title + generated timestamp + rule under title; each entry is a readable block (full date line, type/mood meta, wrapped body text, no more 70-char truncation), separated by hairline rules, with correct multi-page flow.

### 13. Scaling — audited; main screens already use `fillMaxWidth` / `weight(1f)` / `spacedBy`. No fixed overflow widths found.

### 14 (animations). Verified stamp bounce (NewEntry) + DayCell bleed + Month cell bleed are implemented and run. Left at tasteful magnitudes.

## Verification
- `./gradlew assembleDebug` — BUILD SUCCESSFUL.
- `./gradlew lintDebug` — BUILD SUCCESSFUL (no errors).
- `./gradlew testDebugUnitTest` — BUILD SUCCESSFUL (tests pass).
- On-device tap-through recommended by user (fresh uninstall + install): onboarding, Today add, Habits full sheet + color, Month, Insights, Settings (single control), NewEntry Photo menu (camera + gallery), export PDF.

## Notes / open
- Timezone/sleep/notifications/widgets still deferred per agreement.
- `ThemeToggleChip` still referenced by `StyleSpecimenScreen` — acceptable (design-preview screen).
- CameraX `main executor` rule in AGENTS.md retained as historical lesson only.
