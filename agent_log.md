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

## 15. Onboarding CameraPrimer removed (2026-07-16)
- The "Open the ledger" button had set `cameraPrimer = true`, surfacing a `CameraPrimer` composable that requested `Manifest.permission.CAMERA` via `rememberLauncherForActivityResult`. Since in-app CameraX was retired (system camera via `TakePicture` now handles access), the primer was obsolete.
- `OnboardingScreen.kt`: removed `CameraPrimer` composable, `cameraPrimer` state, `cameraLauncher`, and unused imports (`Manifest`, `rememberLauncherForActivityResult`, `ActivityResultContracts`). "Open the ledger" and "Not now" now both call `onFinish()` directly.
- `strings.xml`: removed orphaned `onboarding_camera_title`, `onboarding_camera_body`, `new_entry_grant`, `new_entry_deny`, `new_entry_camera_permission_title`, `new_entry_camera_permission_body`.
- **On-device re-verified** (fresh uninstall + install, `almanac_pixel35` headless): onboarding now lands straight on Today (no camera primer); NewEntry Photo chooser opens "Add a photo" (Capture / From gallery); From gallery launches system `PhotoPickerGetContentActivity`; back returns to NewEntry with no crash; Month grid, Habits sheet + labeled color swatches, Insights bordered cards, Settings single appearance control all render; no FINIS OPUS anywhere.
- `assembleDebug` + `lintDebug` + `testDebugUnitTest` all BUILD SUCCESSFUL.

## 16. Permission-request fixes + NavCrash fix + VM Flow refactor (2026-07-17)
Root cause of the user's real-device crash ("app crashed, didn't ask for permission"):
- Only `ACCESS_FINE_LOCATION` was requested at runtime. `CAMERA` (NewEntry Capture) and `POST_NOTIFICATIONS` (Settings reminder) were never requested, so tapping Capture did nothing / silently failed and the reminder silently no-op'd.
- `AlmanacNavHost.kt` used a **nullable Int navArgument** for `day` (`NavType.IntType` + `nullable=true`), which throws `IllegalArgumentException: integer does not allow nullable values` at NavHost build → launch-time crash/ANR. Fixed by using a non-nullable arg with `defaultValue = -1` (sentinel) and parsing `-1 -> null`.

### Permission fixes
- `NewEntryScreen.kt`: added `cameraPermissionLauncher` (`RequestPermission` for `CAMERA`) + `requestCamera()`; CAPTURE chooser button requests CAMERA before opening system camera. Dismiss the chooser dialog first, then fire the request via a `LaunchedEffect(pendingCameraRequest)` flag to avoid the AlertDialog-dismiss/launcher race. Added `new_entry_camera_denied` toast string.
- `SettingsScreen.kt`: added `notificationPermissionLauncher` (`POST_NOTIFICATIONS`); reminder `SwitchRow` requests `POST_NOTIFICATIONS` on API 33+ before enabling. Added `settings_reminder_permission_denied` string + `Toast`/`Manifest` imports.
- `ACCESS_FINE_LOCATION` request on geotag toggle unchanged (already correct).

### Audit fixes (code-level)
- **P1** Month→day now opens `Today?day=N` (was wrongly routing to `EntryDetail`). `Destination.Today.create(day)`, `TodayViewModel.setDay()`, `TodayScreen(day)` + `LaunchedEffect`. Verified on-device: tapping day 15 opens "July 15, 2026" Today, not "Entry not found".
- **P2** `TodayViewModel`/`MonthViewModel` refactored to a single `flatMapLatest` chain over a `(day/center, query, filter)` StateFlow, `launchIn(viewModelScope)`. `setQuery/setFilter/setDay/goToMonth` now only update StateFlow (no stacked collectors / leak).
- **P3** `HabitsViewModel.save()` preserves `createdAt` + `archived` when editing (loads existing via new `HabitRepository.loadHabit(id)`); previously reset `createdAt` to now on every edit (broke streaks).
- **P5** `HabitsScreen` streak `Text` made non-clickable (was calling `onArchive` on tap — tapping the streak archived the habit). Archive kept via explicit menu.

### On-device verification (fresh uninstall + install, almanac_pixel35 headless)
- No launch crash/ANR (the nullable-NavArg crash is gone).
- Onboarding → Today → Month tap day 15 → Today for that day ✓.
- NewEntry → Photo → CAPTURE → CAMERA permission dialog ("While using the app"/"Only this time"/"Don't allow") → grant → system camera → shutter → Done → photo returns to NewEntry ✓. Deny → returns to chooser, no crash ✓.
- Settings → End-of-day reminder toggle → POST_NOTIFICATIONS dialog → Allow → granted, no crash ✓.
- Habits/Insights/Month/Settings all render.

### Build gates
- `assembleDebug` + `lintDebug` + `testDebugUnitTest` — all BUILD SUCCESSFUL.
- NOTE: the helper `tap.sh` mis-reports tap coordinates (tapped 540,1191 instead of real 790,1367 for the CAPTURE button); use `tapnode.sh` (exact-bounds tap) for future on-device work.

## 17. Nav-bar clipping fixes (2026-07-17)
User's screenshots showed three screens with content cut off by the system navigation bar:
- **Onboarding** ("Not now" flush against nav bar).
- **NewEntry** ("Capture"/"From gallery" clipped).
- **Habits "New habit" sheet** (Color row + swatches clipped).

Root cause: scrollable/full-screen Columns did not apply navigation-bar window insets, so their last elements rendered under the 3-button/gesture nav bar (device is 1080x2400, nav bar occupies y 2337-2400).

Fixes:
- `OnboardingScreen.kt`: root `Column` now `.navigationBarsPadding()` (added import).
- `NewEntryScreen.kt`: scroll `Column` now `.navigationBarsPadding().imePadding()` (added imports) so Capture/From gallery + keyboard clear the bar.
- `HabitsScreen.kt`: sheet scroll `Column` now `.navigationBarsPadding().imePadding()` + a trailing `Spacer(72.dp)` so the Save button and Color swatches clear the bar (added `imePadding` + `height` imports).

### On-device verification (fresh uninstall + install, almanac_pixel35)
- Onboarding "Not now" now at y 2159-2285 (was flush at ~2400); ~52dp clearance above nav bar top 2337 ✓.
- NewEntry photo mode: bottom content ends at STAMP INTO LEDGER y~1065, well clear; no app node extends under nav bar ✓.
- Habits "New habit" sheet: scrolled to bottom, Save ends at y~2066, Color/Dusty Rose at y~1900 — clear of nav bar 2337 ✓.
- `assembleDebug` + `lintDebug` + `testDebugUnitTest` all BUILD SUCCESSFUL.

## 18. Final bug fixes + project archived (2026-07-17)
Final fixes before archiving:
- `MonthScreen.kt`: `leadingBlanks` corrected from `dayOfWeek.value % 7` to `dayOfWeek.value - 1` (Mon=0 blanks). Month grid alignment bug.
- `HabitRepository.kt`: `computeStreak()` loop now bounded by the habit's `createdAt` day instead of scanning back to minDay() (year 2000) every call.
- `LedgerExport.kt`: `unzipToLedger()` hardened against Zip Slip (strips path separators, rejects "..", verifies resolved file stays inside photosDir).
- `NewEntryScreen.kt`: content editor now driven by `state.type` tab (Photo/Text/Mood) instead of `photoUri != null` first — fixes Mood tab doing nothing after a photo was taken.
- All gates green. User decided not to use the app; repo archived on GitHub, app uninstalled from device.
