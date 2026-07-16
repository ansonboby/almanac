# Almanac — UI Redesign Spec (Phase 3.5 Redo)

> Status: DRAFT for your review. Approach = write-a-spec-then-build (your choice).
> Scope decision (recommended, override if you disagree): **reskin within the Field Ledger concept.**
> Rationale: every complaint in your list is an *execution* problem — invisible affordances,
> redundant controls, ugly Month grid, unreadable date, half sheet, nonsense flourish copy,
> bad export — NOT a problem with the date-stamp / moss-brass-parchment / Fraunces+Inter+Mono
> identity. That identity is the app's actual selling point (PRD §1). Rebooting the concept
> throws away the one thing that works. So we keep the signature, fix the execution.

---

## 0. Design principles for this redo

1. **The date stamp stays the hero.** Every screen spends its boldness on it, nowhere else.
2. **Controls must be findable.** No more ghost "+" glyphs. Primary actions get a real,
   bordered, clearly-tappable treatment (outlined button, not a floating FAB).
3. **One theme control, one place.** Theme lives only in Settings (segmented Ink/Parchment).
   Removed from every top bar. Onboarding keeps a quiet toggle.
4. **No archaic flourish copy.** "FINIS OPUS" and similar removed. Keep the small `colophon`
   line — it's tasteful, not confusing.
5. **Calm, legible hierarchy.** Readable date on home, proper section frames on Insights,
   a real Month grid you'd want to look at.
6. **Motion stays minimal and perceptible** (PRD §3.4): stamp press + month ink-bleed only,
   slightly more visible than today, respect reduce-motion.

---

## 1. Navigation & chrome

- **Bottom bar:** unchanged (Today / Month / Habits / Insights / Settings) — it's fine.
- **Top bars:** every screen drops `ThemeToggleChip`. The action slot is freed.
  - Today: no action (filter bar sits below title).
  - Month: month ‹ › chevrons only.
  - Habits: a real **"+ New habit"** outlined button in the top-bar action slot (replaces the
    hidden ghost affordance at the bottom — see §4).
  - Insights / NewEntry / EntryDetail: no action chip.
- **Settings:** the single canonical theme control (segmented Ink/Parchment). Remove the
  duplicate top-bar chip.

---

## 2. Today (home)

Problems: date box "not readable", quiet add affordance.

New layout:
- Top bar title block: eyebrow (`ARCHIVAL LOG — VOL. NN`) in mono brass → big **"Today"**
  in Fraunces displaySmall → the full date in a *readable* treatment: Fraunces medium,
  `onBackground`, not the faint bodySmall it is now. Ensure strong contrast on both themes.
- Below: `EntryFilterBar` (unchanged).
- Empty state: centered message + a real **outlined "New entry" button** (moss border,
  tappable height ~48dp, label in mono/italic) — replaces the ghost "+ New entry" row.
- List: `EntryRow` items unchanged in structure, but the bottom **add affordance becomes the
  same outlined button**, not a ghost glyph.
- A persistent **capture shortcut**: a fixed outlined "＋ Stamp an entry" button anchored at
  the bottom of the list (not a FAB) so it's always reachable.

---

## 3. Month (revamp — "too ugly")

Keep: 7-col grid, brass border per stamped day, subtle mood wash.
Fix layout:
- Weekday header row: mono metadata, muted, spaced to match grid gutters.
- `DayCell`: increase cell padding, give the stamp breathing room; the day *number* moves
  below the stamp but with more separation and clearer color (onSurface when has entry,
  faint onSurfaceVariant otherwise). Selected/today cell gets a moss underline or ring.
- Grid gutters: consistent 10–12dp, cells square-ish via `aspectRatio(1f)`.
- Keep the ink-bleed tap animation, make it a touch more visible (radius + duration up ~30%).
- Tap → Entry Detail for that day (unchanged).

---

## 4. Habits

Problems: new-habit sheet half-visible, invisible add affordance, color picker "no purpose".

- **Sheet fix:** `ModalBottomSheet` gets `windowInsets = WindowInsets(0,0,0,0)` and content
  uses `.navigationBarsPadding()` + `fillMaxWidth` so it's fully visible and scrolls.
- **Add affordance:** move to a real **"+ New habit"** outlined button in the top-bar action
  slot. Remove the bottom ghost row (or keep a secondary one, but the primary is now visible).
- **Color ("Ink") picker purpose:** currently it only tints the completion dot/streak — which
  is genuinely useful but invisible. Fix: show a live *preview chip* (a habit row mock) that
  uses the selected tint, so the user sees what the color does *before* saving. Also relabel
  "Ink" → "Mark color" and add a one-line hint: "Tints this habit's stamp."
- Keep streak/done logic and the `CircleShape` completion mark (AGENTS exception stands).

---

## 5. Insights

Problems: "looks bad / stupid text", empty states, FINIS OPUS.

- Remove FINIS OPUS line. Keep the `colophon` at the very bottom, small.
- Wrap each chart (Mood trend, Frequency) in a **bordered card frame** (moss hairline border,
  parchment/ink surface, 0dp corners) with the section label inside the top of the card —
  gives structure instead of floating text.
- Empty states: keep but tighten copy; center them in the card with clear "No readings yet".
- Habit consistency rows: unchanged logic; give them the same card framing for consistency.
- Keep charts as-is (functionally fine); just frame them.

---

## 6. New Entry

Problems: camera flow buggy/confusing; per-screen theme chip.

- Remove top-bar `ThemeToggleChip`.
- **Photo action = simple menu.** The "Photo" type chip opens a small `AlertDialog` chooser:
  - **"Camera"** → `ActivityResultContracts.TakePicture` (system camera app) writing to a
    FileProvider/app-private uri, then back to the caption view.
  - **"Gallery"** → `ActivityResultContracts.GetContent("image/*")`.
  - **"Cancel"**.
  This removes the in-app CameraX `CameraCaptureView` entirely (simpler, matches your ask:
  "menu opens gallery or the camera app, that's it").
- Keep Text / Mood chips. Keep caption + geotag + Stamp button. Keep the stamp-press animation
  (make it perceptible).
- AGENTS.md: retire the CameraX main-thread rule (no longer used) and note CameraX is removed.

---

## 7. Entry Detail

- Remove top-bar `ThemeToggleChip` (already only has Back/Edit/Delete — confirm; chip not here).
- Keep LargeDateStamp hero, photo mat, mood, text. Structure already reasonable; minor spacing
  polish only.

---

## 8. Settings

- Remove the duplicate top-bar `ThemeToggleChip`; keep the segmented Ink/Parchment control as
  the single theme switch.
- Everything else (privacy, reminders, data mgmt, export) unchanged structurally; tighten
  spacing to match the new card framing language.

---

## 9. Onboarding

- Keep a quiet theme toggle (it's the one place a prefs-new user might want it). Fix the
  **flash bug**: in `MainActivity`, don't render the gate decision until the
  `onboardingComplete` flow has emitted at least once (hold a `ready` flag with
  `initialValue = false` → only swap after first real emission). This stops the flash-then-skip.

---

## 10. Export (PDF) — "looks shit"

- Improve `LedgerExport.exportPdf` layout: title ("Field Report"), generated date (mono),
  then each entry as a readable block: № archival no., date stamp text, type, text/tags,
  mood, location — separated by hairline rules. Avoid a raw unstyled dump. (Verify current
  export code; this is Phase 3 code.) Keep it monochrome/ledger-styled.

---

## 11. General scaling / legibility audit

- Audit all screens for: `fillMaxWidth` + `spacedBy` + dp padding (no magic fixed widths that
  overflow on ~360dp); touch targets ≥ 48dp for primary actions; contrast of metadata text on
  both themes. Fix anything that fails.

---

## 12. What this redo does NOT change

- Data model, Room, Repository, ViewModels, Hilt graph, navigation routes, permissions model.
- The Field Ledger palette, Fraunces/Inter/Mono pairing, the date-stamp signature.
- Habit `CircleShape` completion-mark exception (AGENTS).
- Deferred scope (your call): timezone day-tracking, sleep logging, notifications, widgets.

---

## 13. Build & verify

1. `./gradlew assembleDebug` — fresh APK.
2. `./gradlew lintDebug` and `./gradlew testDebugUnitTest` — must pass.
3. Manual tap-through (recommend you install): onboarding (no flash), Today add, Habits add
   (full visible sheet + color preview), Month (looks good?), Insights (framed, no FINIS OPUS),
   Settings (one theme control), NewEntry Photo menu (camera + gallery), export PDF.

---

## Open questions for you

- Q1: Agree with **reskin within Field Ledger** (recommended), or do you want a bolder reboot?
- Q2: Capture shortcut on Today — outlined bottom button (recommended) vs a real FAB?
- Q3: Anything here you'd cut or add before I build?
