# Almanac — Product Requirements Document

*A daily life logger for native Android. Working title: "Almanac" (rename freely — the codebase uses `almanac` as a placeholder package/app name throughout).*

---

## 1. Vision

Almanac is a private, local-first logbook for your day: photos, journal entries, mood, habits, and where you were — all captured in one unified daily record. Most life-logging apps look like spreadsheets wearing a coat of paint. Almanac's whole pitch is that **the UI is the product** — it should feel like keeping a beautiful field journal, not filling out a form. If the interface doesn't make someone want to open it every night before bed, the app has failed at its one job.

**One-liner:** *A field journal for your life — log a day the way an explorer logs a discovery.*

### Goals
- One app, one timeline: photos, text, mood, habits, and location all live as entries in the same daily record — no separate disconnected modules.
- The design is distinctive enough to be a reason people choose this app over a generic journaling app.
- 100% offline, local-first, no account required. Your data never leaves your device unless you explicitly export it.
- Fast capture: logging a moment should take under 10 seconds.

### Non-Goals (v1)
- No social features, sharing, or multi-user sync.
- No cloud backend. (Optional local encrypted export is in scope; cloud sync is a post-v1 stretch goal.)
- No cross-platform (iOS/web) — Android native only, per the brief.

---

## 2. Platform & Tech Stack

| Layer | Choice | Notes |
|---|---|---|
| Language | Kotlin | 100% Kotlin, no Java |
| UI toolkit | Jetpack Compose (Material 3) | Custom theme overrides — see §4. Do not ship default Material You colors. |
| Architecture | MVVM + Repository pattern | `ViewModel` → `Repository` → `Room` / `DataStore` |
| Async | Kotlin Coroutines + Flow | `StateFlow` for UI state |
| Local database | Room (SQLite) | Single source of truth, offline-first |
| Preferences | Jetpack DataStore | Theme, reminder settings, onboarding state |
| Image loading | Coil | Compose-native, handles local URIs efficiently |
| Camera | CameraX (in-app capture) with fallback to system camera intent | In-app gives control over aspect ratio/UI polish |
| Charts (mood/habit trends) | Custom Canvas draws in Compose, or Vico if a library is preferred | Avoid a generic-looking off-the-shelf chart look — restyle to match the design system |
| Location | FusedLocationProviderClient + Android Geocoder | Opt-in only, see §7 Privacy |
| Navigation | Navigation Compose | Single-Activity architecture |
| Dependency injection | Hilt | Standard for this scale of app |
| Min SDK | 26 (Android 8.0) | Target/compile SDK: latest stable |
| Testing | JUnit + Turbine (Flow testing) + Compose UI test | ViewModel and Repository layers should have real test coverage |

---

## 3. Design System — "Field Ledger"

This is the section that matters most to the brief: **the UI has to sell the app.** The direction below is deliberately not a generic Material Design app, and not the three looks that AI-generated UIs tend to default to (warm cream + terracotta serif; near-black + one acid accent; broadsheet newspaper columns). Instead it's grounded in the actual subject: a naturalist's field journal — the kind of notebook used to log specimens, weather, and coordinates, adapted for logging a life.

**The signature element:** every entry gets a hand-inked **date stamp** — a small rubber-stamp-style mark (monospace type, slightly imperfect rotation, brass ink) in the corner, as if a librarian stamped it in. The Month view is literally a sheet of these stamps. The Entry detail view is one stamp, zoomed in. This one device ties the whole app together and should not be diluted elsewhere — keep the rest of the UI quiet around it.

### 3.1 Color palette

| Token | Hex | Use |
|---|---|---|
| `ink` | `#22261F` | Primary dark background — a deep bottle-green charcoal, not pure black |
| `parchment` | `#E9E4D3` | Card / surface color on dark, and primary background in light mode |
| `moss` | `#6B7A5A` | Primary accent — active states, habit streaks, primary buttons |
| `brass` | `#B8934A` | Secondary accent — the date-stamp ink, tags, location pins |
| `dusty-rose` | `#C08B7A` | Mood/warmth accent, used sparingly (e.g. a low-mood indicator) — never paired alone the way a template uses a single accent; it always sits alongside moss or brass |
| `ink-text` / `parchment-text` | `#1A1C16` / `#F5F2E8` | Body text, chosen per-surface for contrast |

Dark ("Ink") is the default/hero theme — the app is most often opened in the evening. Light ("Parchment") is a full alternate theme, not an afterthought — swap surface/background pairing, keep moss/brass/rose as accents in both.

### 3.2 Typography

- **Display / dates / headers — Fraunces** (soft optical size, weights 400–600). Used with restraint: section headers, the day's date, month names. This is the one place the type gets to have personality.
- **Body / UI text — Inter or IBM Plex Sans.** Everything the user reads at length (journal entries, settings, buttons) needs to stay highly legible — no personality tax on readability.
- **Metadata / stamps / timestamps / coordinates — IBM Plex Mono.** Reserved specifically for "stamped" data: the date stamp, GPS coordinates, mood score, habit counters. Using mono only for this category is a structural signal, not decoration — it tells the user "this is a logged fact" the moment they see it.

### 3.3 Layout concept

- Daily timeline: a single vertical ledger, each entry as a line-item with a thin `moss` hairline rule down the left margin (evoking a notebook's margin line), not a floating card grid.
- Photos: presented like a pressed specimen or tucked photograph — a `parchment` mat border, a slight ±1–3° rotation per photo (deterministic per-entry, not random on every recompose), subtle drop shadow. Never full-bleed edge-to-edge like a generic gallery app.
- Month view: a grid of small stamps — one per day, brass-inked if it has an entry, empty/faint if not, tinted by that day's mood color when present.
- Mood input: not a slider — a small set of hand-drawn-style glyphs (weather-report inspired: clear / cloudy / storm, etc., matched to the almanac concept) rather than a generic 1–5 emoji row.

### 3.4 Motion

- New entry "stamps in": scale 105% → 100% with a small rotational settle (~150ms), mimicking a stamp being pressed down.
- Month-grid stamps ripple/ink-bleed very briefly on tap.
- Respect system "reduce motion" — all of the above degrade to a plain fade when enabled.
- No animation for the sake of it: the two moments above are the entire motion budget. Everything else is a plain, fast fade/slide.

### 3.5 Explicit non-goals for the design
- Do not use default Material 3 dynamic color (Material You). Ship the fixed palette above in both themes.
- Do not use stock Material icons uncustomized for the stamp/mood glyphs — those are the signature elements and deserve custom-drawn Compose Canvas or vector assets.
- Avoid rounded "friendly SaaS app" cards with heavy elevation/shadow everywhere — the ledger-line layout replaces most card chrome.

---

## 4. Feature Set

### Phase 1 — MVP
- **Unified daily entry ("Moments")** — one timeline per day that can hold a mix of photo, text, mood, and tag content, not siloed screens per type.
- **Text journal entries** — plain text with optional light markdown (bold/italic), per-entry tags.
- **Photo entries** — capture via camera or pick from gallery, optional one-line caption, stored in app-private storage.
- **Mood check-in** — one mood glyph per day (weather-report style, §3.3), optional short note.
- **Today timeline + Month grid view** (the stamp-sheet).
- **Entry detail / edit / delete.**
- **Onboarding** — permissions requested contextually (camera/gallery only up front; location deferred until the user turns on check-ins).

### Phase 2 — Habits & Insights
- **Habit tracking** — user-defined habits, daily checklist, streak counting.
- **Insights screen** — mood trend over time, habit completion rate, entry frequency, restyled charts matching the design system (§2 chart guidance).
- **Search & filter** — by date range, tag, mood, or habit.

### Phase 3 — Location & Utility
- **Location check-ins** — opt-in, reverse-geocoded to a place name, shown as a `brass` coordinate stamp rather than a raw map by default (tap to expand a map).
- **Reminders** — a local notification nudging an end-of-day entry (no server, `WorkManager`-based).
- **Backup / export** — export all data + photos to a single encrypted local archive the user can move manually (e.g. to their own cloud drive); import to restore.
- **Home-screen widget** — shows today's stamp / quick-capture shortcut.

### Phase 4 — Stretch
- "Year in pixels" full-year mosaic view.
- Optional end-to-end-encrypted cloud sync (explicitly opt-in, separate from the local-first default).
- Theming: user-selectable accent swaps within the Field Ledger system (e.g. an "indigo ledger" variant) without breaking the design language.

---

## 5. Data Model (Room entities, simplified)

```
Entry
 - id: Long (PK)
 - epochDayLocal: Int          // which calendar day this belongs to, in local time
 - createdAt: Instant
 - type: EntryType              // PHOTO, TEXT, MOOD, CHECK_IN — an entry can carry more than one; see note
 - textContent: String?
 - photoUri: String?
 - moodScore: Int?              // e.g. -2..+2, mapped to a weather glyph
 - locationName: String?
 - lat: Double?, lng: Double?
 - tagIds: List<Long>           // cross-ref table EntryTag

Habit
 - id: Long (PK)
 - name: String
 - iconKey: String
 - colorTag: String
 - createdAt: Instant
 - archived: Boolean

HabitLog
 - id: Long (PK)
 - habitId: Long (FK)
 - epochDayLocal: Int
 - completed: Boolean

Tag
 - id: Long (PK)
 - label: String

EntryTag (cross-ref)
 - entryId: Long (FK)
 - tagId: Long (FK)
```

Note: rather than one rigid row per type, Phase 1 can model a day as a small ordered list of `Entry` rows sharing the same `epochDayLocal`, where each row is one piece of content (one photo, one text block, one mood check-in). The Today/Month views group by `epochDayLocal`. This keeps the schema simple while still supporting "a day can have three photos and a paragraph."

---

## 6. Screens & Navigation

```
Onboarding (first launch only)
 └─ permission primers (contextual, not all up front)

Bottom nav / primary destinations:
 1. Today        — current day's ledger line, quick-capture entry point
 2. Month        — the stamp-sheet grid, tap a day → Day Detail
 3. Insights      — mood/habit charts (Phase 2+)
 4. Settings      — theme, reminders, export/import, privacy toggles

Day Detail        — full ledger for one day, add/edit/delete entries
New Entry flow    — type selector (photo / text / mood / tag) → capture → confirm
Habit management  — list, add/edit/archive habits (Phase 2+)
```

---

## 7. Non-Functional Requirements

- **Privacy is a feature, not a checkbox.** Default state: fully offline, no network permission requested at all in Phase 1 (location and any future sync are separately-gated permissions added only in the phase that needs them). This is a real differentiator to say out loud in the app's own copy ("Your data stays on your phone").
- **Performance:** 60fps scroll on the Month grid and Today timeline even with hundreds of entries; paginate/virtualize queries via Room's `Paging3` integration if entry counts grow large.
- **Accessibility:** support dynamic text sizing, content descriptions on all custom-drawn glyphs/stamps (they are informational, not decorative, and must not rely on color alone to convey mood), and honor "reduce motion."
- **Resilience:** no data loss on process death mid-capture (draft entries persisted immediately, not only on explicit save).

---

## 8. Roadmap Summary

| Phase | Focus | Ships when |
|---|---|---|
| 0 | Project scaffold, theme system, navigation shell | Before any feature work |
| 1 | Photo + text + mood entries, Today + Month views, core aesthetic | MVP — usable daily driver |
| 2 | Habits, streaks, Insights charts, search/filter | |
| 3 | Location check-ins, reminders, backup/export, widget | |
| 4 | Year-in-pixels, theming variants, optional cloud sync | Stretch |

---

## 9. Glossary

- **Entry** — one logged piece of content (a photo, a note, a mood check-in) tied to a day.
- **Stamp** — the visual signature element: a date-marked, brass-inked glyph representing a day or entry.
- **Ledger line** — the vertical timeline layout for a single day's entries.
