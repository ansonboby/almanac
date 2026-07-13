# AGENTS.md — Almanac (Android)

This file is read automatically by OpenCode at the start of every session in this repo. Full context lives in `PRD.md` — read it before doing any planning or scaffolding work. This file is the condensed, load-bearing ruleset; keep it updated whenever a convention below changes.

## Project

Native Android app, "Almanac" (placeholder name/package — do not invent a different concept or feature set than what's in `PRD.md`). A local-first daily life logger: photo, text, mood, and location entries in one unified timeline, with a distinctive "Field Ledger" visual identity that is a primary selling point of the app, not an afterthought.

## Repository Setup

This project should live at **github.com/ansonboby/almanac** (adjust the repo name below if a different one is preferred — ask before assuming otherwise).

As the very first action in Phase 0, before any Gradle scaffolding:

1. `git init` in the project root.
2. Create the remote repo under the `ansonboby` account:
   - Preferred: `gh repo create ansonboby/almanac --public --source=. --remote=origin` (requires `gh auth login` already done locally — check with `gh auth status` first, and if it's not authenticated, stop and ask rather than guessing at credentials).
   - Fallback if `gh` isn't installed/authenticated: use the **playwright** MCP to drive a logged-in browser to github.com/new, create a repo named `almanac` under the `ansonboby` account, then add it as the `origin` remote locally and push.
3. Add a `.gitignore` for Android/Gradle/IDE files, commit `PRD.md` + `AGENTS.md` + the initial scaffold, and push.
4. Default to a **public** repo unless told otherwise — this only exposes code, never the user's actual journal data (which is local-only per PRD §7).

## Tech Stack

- Kotlin only, no Java.
- Jetpack Compose + Material 3 as the component base — but themed with the custom palette/typography in `PRD.md` §3. Never leave default Material You dynamic color in place.
- MVVM + Repository pattern. `ViewModel` exposes `StateFlow` of UI state; `Repository` is the only thing that talks to `Room`/`DataStore`.
- Room for persistence, DataStore for preferences, Coil for images, CameraX for capture, Navigation Compose for navigation, Hilt for DI.
- Single-Activity architecture.
- Min SDK 26.

## Architecture

```
app/
 ui/            Composables, screen-level and component-level, organized by feature (today/, month/, entry/, habits/, insights/, settings/)
 ui/theme/       Color.kt, Type.kt, Shape.kt, Theme.kt — the Field Ledger design system tokens
 viewmodel/      One ViewModel per screen/feature
 data/
   local/        Room entities, DAOs, database class
   repository/   Repository classes, the only layer ViewModels talk to
   datastore/    Preferences
 di/             Hilt modules
```

Keep Composables pure/stateless where possible; hoist state to ViewModels. Don't let Composables query Room directly.

## Code Style

- Standard Kotlin style (ktlint defaults). 4-space indent.
- Composable function names: `PascalCase`, noun-based (`TodayScreen`, `EntryStampBadge`), not verb-based.
- Prefer `sealed interface` for UI state and one-off events over booleans-and-nulls scattered across a data class.
- No hardcoded strings/colors in Composables — pull from `strings.xml` and the theme tokens in `ui/theme/`.
- Every custom-drawn glyph/icon (stamps, mood weather-glyphs) needs a `contentDescription` — these are informational, see PRD §7.

## Design System Rules (read PRD.md §3 in full before touching UI)

- Palette, type pairing (Fraunces / Inter / IBM Plex Mono), and the date-stamp signature element are specified in `PRD.md` and are not up for reinterpretation without asking first.
- Mono type (IBM Plex Mono) is reserved for stamped metadata only (dates, coordinates, counters) — don't use it as a general UI font.
- The date-stamp is the one signature flourish. Don't add competing decorative motifs elsewhere; keep surrounding UI quiet per the "spend boldness in one place" principle in PRD §3.

## MCP Servers Available

Three MCP servers are connected in this OpenCode environment — use them deliberately, not by default on every turn:

- **context7** — pulls current, version-accurate documentation for libraries (Compose, Room, Hilt, CameraX, Navigation Compose, etc.). Use it before implementing against any API you're not 100% certain of, instead of relying on possibly-outdated training data — Android/Compose APIs shift often enough that this matters.
- **stitch** — Google's AI UI design tool. Use it to generate/iterate visual mockups of a screen (Today timeline, Month stamp-grid, New Entry flow) against the Field Ledger direction in `PRD.md` §3 before writing the Compose implementation, especially for any screen whose layout isn't already nailed down. Treat its output as a visual reference to translate into the app's actual theme tokens, not as a final asset to import directly.
- **playwright** — browser automation. Its main job here is the GitHub-repo-creation fallback above; otherwise reach for it only if a task genuinely needs driving a real browser (e.g. checking a web-based reference).

## Build Order

Work through `PRD.md` §4/§8 phase by phase — do not attempt to scaffold every phase at once:

1. **Phase 0:** Repository setup (above) first, then Gradle project setup, package structure above, Hilt/Room/Navigation wired up empty, the `ui/theme/` design system implemented and visually verifiable (a simple screen showing the palette/type scale is a good sanity check before building real screens — consider using **stitch** to explore the look before committing to a Compose implementation).
2. **Phase 1:** Entry data model + DAO, Repository, Today timeline, Month grid, New Entry flow (photo/text/mood), Entry detail/edit/delete. This is the MVP — stop and check in before moving on.
3. **Phase 2+:** Habits, Insights, location, reminders, export — only after Phase 1 is working end-to-end.

When starting a new phase, propose a short plan (Plan mode) before writing code, especially for anything touching the data model or the theme system.

## Testing

- ViewModel and Repository logic should have JUnit + Turbine tests for Flow-emitting functions.
- Don't write UI snapshot tests unless asked — prioritize logic coverage first.

## Commands

```
./gradlew assembleDebug        # build the debug APK
./gradlew testDebugUnitTest    # run JVM unit tests
./gradlew lintDebug            # Android lint
```
ktlint is not wired yet — add it in a later phase and update this block.

### Build environment (this machine)

The toolchain was installed under `mise` / a local Android SDK; `./gradlew` needs
`JAVA_HOME` (JDK 17) on PATH and the SDK location (already in `local.properties`,
which is git-ignored):

```
export JAVA_HOME=/home/anson/.local/share/mise/installs/java/temurin-17.0.19+10
export ANDROID_HOME=/home/anson/Android/sdk
```

- **Build stack (locked in Phase 0):** AGP 8.13.2, Gradle 8.14.3 (wrapper),
  Kotlin 2.2.21, compileSdk/targetSdk 36, minSdk 26, JDK 17.
- We deliberately did **not** adopt AGP 9 / SDK 37 / the 2026 AndroidX libs: AGP 9's
  new built-in-Kotlin plugin model is too new/under-documented for a stable scaffold.
  Library versions are pinned in `gradle/libs.versions.toml` to a coherent SDK-36 set
  (core-ktx 1.17, lifecycle 2.9.4, Compose BOM 2025.12.01, Hilt 2.57.2, Room 2.8.4).
  Revisit when AGP 9 tooling matures.

## Gotchas

- Location and any networking permission should only be requested in the phase that actually needs them (Phase 3+) — Phase 1 should request zero network-adjacent permissions. This is a stated privacy/marketing point in the PRD; don't add permissions early "just in case."
- Photo URIs: use scoped storage / app-private storage, not `MediaStore` shared storage, since these are private journal photos.
- Keep `PRD.md` and this file in sync — if a design or architecture decision changes, update both.
