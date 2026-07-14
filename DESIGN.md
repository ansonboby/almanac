# DESIGN.md — Almanac: "Field Ledger"

Design system reference for the Almanac app. Import this into Stitch before generating any screen so every screen shares the same tokens, and paste it back in as context if a generation drifts from it.

## Product Context

**Almanac** is a private, local-first daily life logger for native Android: photos, journal entries, mood, habits, and location, all in one unified daily timeline. Target user: someone who wants to keep a daily record of their life and cares about the app feeling like a beautiful object, not a form to fill out. The design's whole job is to make someone want to open it every night before bed.

**Design personality:** a naturalist's field journal — the kind of notebook used to log specimens, weather, and coordinates — adapted for logging a life. Not a generic productivity app. Not a "friendly SaaS" journaling app with rounded cards and soft shadows everywhere.

## Color Tokens

| Token | Hex | Role |
|---|---|---|
| `ink` | `#22261F` | Primary background (dark/default theme) — a deep bottle-green charcoal, never pure black |
| `parchment` | `#E9E4D3` | Surface/card color on dark; primary background in the light theme variant |
| `moss` | `#6B7A5A` | Primary accent — buttons, active states, habit streaks, the margin hairline on the timeline |
| `brass` | `#B8934A` | Secondary accent — the date-stamp ink color, tags, location pins |
| `dusty-rose` | `#C08B7A` | Warmth/mood accent, used sparingly — always paired with moss or brass nearby, never as a lone accent |
| `ink-text` | `#1A1C16` | Body text on light/parchment surfaces |
| `parchment-text` | `#F5F2E8` | Body text on dark/ink surfaces |

Dark ("Ink") is the default, hero theme. Light ("Parchment") is a full alternate theme — same accent roles, swapped background/surface pairing.

## Typography

- **Display / headers / dates — Fraunces**, soft optical size, weights 400–600. Used sparingly: section headers, the day's date, month names. This is the one place type gets personality.
- **Body / UI text — Inter** (or IBM Plex Sans). Everything read at length — journal entries, settings, buttons. Stays highly legible, no personality tax.
- **Metadata / stamps / timestamps / coordinates — IBM Plex Mono.** Reserved *only* for stamped data: the date stamp, GPS coordinates, mood score, habit counters. This is a structural signal, not decoration — mono type means "this is a logged fact."

## The Signature Element

Every entry gets a **hand-inked date stamp**: a small rubber-stamp-style mark (IBM Plex Mono, brass ink, slightly imperfect rotation, as if physically stamped) in the corner of the entry. The Month view is a grid of these stamps — a "sheet of stamps," brass-inked where a day has an entry, faint/empty where it doesn't, tinted by that day's mood color when present. The Entry Detail view is essentially one stamp, zoomed in.

This is the one place to spend visual boldness. Everything else in the UI stays quiet around it — don't add competing decorative motifs.

## Layout Principles

- **Timeline, not cards.** The daily view is a single vertical ledger — each entry is a line-item with a thin `moss` hairline down the left margin (like a notebook's margin rule), not a floating card grid with heavy shadows.
- **Photos as pressed specimens.** A `parchment` mat border, a slight deterministic ±1–3° rotation per photo, subtle drop shadow. Never full-bleed edge-to-edge like a stock gallery app.
- **Mood as weather, not emoji.** Small hand-drawn-style glyphs in a weather-report register (clear / cloudy / storm, etc.) rather than a generic 1–5 emoji row.
- **Quiet chrome.** Minimal use of elevation/rounded-corner "SaaS card" styling. Structure comes from the ledger line and the stamp motif, not from boxes.

## Motion (for prototype mode / interaction notes)

- New entry "stamps in": a quick scale + small rotational settle, like a stamp being pressed down.
- Month-grid stamps do a brief ink-bleed ripple on tap.
- Everything else: plain, fast fade/slide. The two moments above are the entire motion budget — no animation for its own sake.

## Voice & Copy Patterns

Discovered in the first round of Stitch mockups and worth keeping consistently — reference these in future prompts and in real implementation strings:

- Actions speak in the ledger/field-journal voice, not generic app language: the primary save action reads **"Stamp into Ledger"**, not "Save" or "Submit."
- Entries carry a running archival number, e.g. **"№ 442"**, rather than no identifier at all.
- Tags, species-style labels, or classifications are set in italics (e.g. *Aquilegia formosa*), reinforcing the field-guide register.
- Section eyebrows use an archival-log framing, e.g. **"ARCHIVAL LOG VOL. IV"**, **"Vol. 24 — Obs. 842"**.
- A small closing flourish belongs at the bottom of longer screens (Insights, Settings) — e.g. **"FINIS OPUS"**, or a colophon block ("Authenticated Ledger — The Naturalist's Almanac — Est. MMXXIV — Crafted for intention & observation"). Don't overuse this — one per screen, at most.
- Privacy messaging is stated plainly and repeated where relevant, not just in onboarding: e.g. *"Your ledger remains on this device."*

## Do / Don't

- **Do** default to the dark "Ink" theme unless asked for the light variant.
- **Do** keep mono type confined to stamped metadata only.
- **Don't** use default Material You dynamic color or stock Material icons for the stamp/mood glyphs — those need custom treatment.
- **Don't** reach for a warm-cream-and-serif look, a near-black-with-one-neon-accent look, or a broadsheet-newspaper look — none of those are this design.
- **Don't** over-animate. Restraint is the point.
