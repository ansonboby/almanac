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
- Room for persistence, DataStore for preferences, Coil for images, Navigation Compose for navigation, Hilt for DI. (Photo capture uses the system camera app via `ActivityResultContracts.TakePicture` + a gallery `GetContent` picker — no in-app CameraX.)
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
- **Exception (added post Stitch Round 1):** Habit completion marks use `CircleShape` on purpose — a small filled dot/ring reads as a stamp impression, not a generic checkbox. This is the one place a shape other than the 0dp sharp corner is allowed; don't extend that exception to other controls.

## Stitch Round 1 reconciliation notes

After a Stitch-generated design pass (per-screen references in `/home/anson/Downloads/stitch_almanac_design_system/`), these reconciliations were made against `field_ledger/DESIGN.md`:

- **Typography:** we kept **Fraunces** for display, not Newsreader. Fraunces is in the PRD/DESIGN.md token table; Newsreader was a Stitch suggestion that diverged. Keep Fraunces.
- **No avatar in TopAppBar.** Stitch's Today mockup shows a circular avatar; the PRD never specified one and it adds a decorative motif competing with the date-stamp. Drop it.
- **Settings layout:** Stitch generated a settings screen with a different structure than what we built (vertical scroll with Appearance / Data / Colophon sections). Use our structure; the Stitch screen is a reference for spacing/typography only.
- **Month mood-tinting:** Stitch's month grid shows mood-tinted cells. Our implementation uses brass borders + a subtle mood wash. Acceptable — keep ours, but ensure the mood wash stays subtle (not a full fill).
- **Open product question:** Stitch's direction leans toward an "observation" log (notes about the world) while the PRD frames entries as personal mood/life logging. Keep the personal-journal voice for now; flag this as an open product decision, not a code change.
- **Copy voice:** keep the established tone — "Stamp into Ledger", `№ 442` counters, italic day tags, colophon line. Don't flatten to generic Material copy.

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
- **When investigating a bug reported from a real device or emulator run**, first rule out a stale install (`adb uninstall <package>` then a fresh install of the current build) before deep-diving into source — a Phase 3 "crash" briefly looked real but was actually an old APK still on the device. Confirm the reproduction is against current code before spending time on root-causing.
- **The Photo entry flow is the one path that has never been confirmed end-to-end** (pick → `FileStorage.importUri` → Room save → Coil load of the private-storage path → "pressed specimen" render on Today). Automated UI drivers (uiautomator) can't reliably operate the DocumentsUI `GetContent` picker, so the trustworthy close is a real-device tap-through: fresh-install, tap Photo, take/pick a real image, Stamp into Ledger, and confirm it renders mat-bordered + slightly rotated (not full-bleed/broken/blank). Don't claim the photo flow works off "shares the verified save path" alone — that only rules out the one already-found bug class.

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
- **Known compile gotcha (found in Phase 1):** `Modifier.semantics { }` mis-resolves in this project's Compose BOM inside `ThemeToggleChip` and cascades into a misleading "non-composable" error. Don't re-add it there — accessibility on that component is handled via `clickable`'s `onClickLabel` instead. `EntryRow`'s use of `semantics` is fine and can be used as the working reference if `semantics` is needed elsewhere.
- **Hilt + Room DAO pattern (established Phase 2):** Hilt's DI graph only provides `AlmanacDatabase` directly. Repositories take the `Database` in their constructor and call `.entryDao()` / `.habitDao()` etc. internally — don't add `@Provides` functions for individual DAOs, that path caused resolution failures for newly-added DAOs in Phase 2.
- **Custom charts (established Phase 2):** draw shapes/lines/bars in a plain `Canvas`, then overlay any text (axis labels, etc.) as normal Compose `Text` composables positioned with `Box` + `Alignment`, not `nativeCanvas.drawText`. Keep using this pattern for any future chart work (Phase 3/4 insights additions).
- **`kotlin.incremental=false` is intentional, not temporary** — a Phase 2 build hit Kotlin incremental-cache corruption (`LazyStorage` crash) after an aborted build. Leave incremental compilation off through the rest of active agent-driven development; the corruption risk (an aborted build recurring) costs more than the slower clean builds do at this project's size. Only reconsider re-enabling it later if build times become a genuine bottleneck, and do it as its own isolated, revertible commit if so.
- **Hilt `@ApplicationContext` (established Phase 3):** any `@Inject` constructor that needs a `Context` must take it typed as `@ApplicationContext Context` (or `@ActivityContext`). Hilt only binds the *qualified* Context types, not a bare `Context` — a bare `Context` param fails Dagger resolution at `hiltJavaCompileDebug`. Applies to `LocationRepository`, `LedgerExport`, and `ReminderScheduler`.
- **`androidx.security.crypto` is pinned to 1.0.0 (stable), not 1.1.0-alpha** — so use `MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)` + `EncryptedFile.Builder(file, context, name, FileEncryptionScheme.AES256_GCM_HKDF_4KB).setKeysetAlias(alias).build()`. The `MasterKey` class and `FileEncryptionProperties` only exist in the alpha line and are NOT in 1.0.0.
- **Room DB swap must restart the process (established Phase 3):** restoring a backup overwrites the Room `.db`/`-wal`/`-shm` files on disk. Because Room holds the connection open, the restored data won't be visible until the process restarts. `LedgerExport.restartApp()` swaps the files then calls `Runtime.getRuntime().exit(0)` after starting a fresh `MainActivity` task. Don't try to "reopen" the database in-process — kill and relaunch.
- **`FusedLocationProviderClient` is not provided by Hilt** — construct it lazily via `LocationServices.getFusedLocationProviderClient(context)` inside `LocationRepository` (which gets the qualified `@ApplicationContext`). Don't add an `@Inject constructor(client: FusedLocationProviderClient)` param; there's no `@Provides` for it and it will fail Dagger resolution.
- **`Geocoder.getFromLocation(...)` is deprecated** on newer API levels but still functional on minSdk 26; wrap in try/catch and return null on failure so geotagging degrades gracefully when offline or on devices without a geocoder.
- **On-device crash (found in Phase 3 field testing):** saving an entry with geotagging crashed with "Not in application's main thread." Root cause was `Tasks.await()` (the blocking Google Play Services variant) running off the main thread without being on a coroutine dispatcher — fixed by using the suspend `kotlinx.coroutines.tasks.await()` extension instead, with the whole save path wrapped in `withContext(Dispatchers.IO)`. Any future code that calls into a `com.google.android.gms.tasks.Task` (Play Services APIs generally) must use the coroutine `.await()` extension, never the blocking one. Entry saving is also now wrapped in try/catch with an on-brand error state — never let a raw exception string render in the UI again.
- **CameraX is RETIRED (as of Phase 3.5):** the in-app CameraX capture (`CameraCaptureView`, `ProcessCameraProvider`, `ImageCapture`) was ripped out. NewEntry now opens a system-camera-app via `ActivityResultContracts.TakePicture` (writes to an app-private `FileProvider` URI from `FileStorage.newCaptureFile()`/`uriForFile()`) or a gallery picker via `GetContent`. Do NOT reintroduce `androidx.camera.*` here. (The earlier "CameraX must stay ON the main thread" gotcha is now historical only — keep it as a lesson, but the code path no longer exists.)
- **Main-thread rule — know which calls move OFF main vs stay ON it:** Play Services `Task`s (`FusedLocationProviderClient`, etc.) must move **OFF** the main thread (coroutine `.await()` inside `withContext(Dispatchers.IO)`). Any future code that calls into a `com.google.android.gms.tasks.Task` (Play Services APIs generally) must use the coroutine `.await()` extension, never the blocking `Tasks.await()`. (The CameraX half of the original rule is gone with the CameraX removal above.)
