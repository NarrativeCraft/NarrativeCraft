## Context

This project is a Minecraft mod rewrite. The codebase already includes a working **cutscene system**, a **camera angle maker**, and a **script Ink integration**. You need to follow the **existing architectural patterns** of those systems to implement the new feature below.

---

## Feature to implement: Interaction Maker

Implement an **Interaction Maker** editor — a dev-mode GUI tool that lets the user create, edit, delete, and serialize/deserialize **interactions** that trigger a named Ink stitch when activated.

There are **two types** of interactions:

---

### Type 1 — Detection Zone

A rectangular detection area defined by **two corner points**.

**Dev mode rendering:**
- Rendered as **blue boundary lines** (top, bottom, left, right edges forming a square outline).
- The **zone name** is displayed centered inside the rectangle in a large font.

**Production mode:**
- No visual rendering at all — the zone is **invisible**.
- Only the **detection logic is active**: the stitch is triggered when the player enters the area.

---

### Type 2 — Interaction Point

A **white dot** rendered using the GUI sprite `"interaction_white_point"`.

**Dev mode rendering:**
- The point is **always visible**, regardless of distance or mouse position — ignoring all visibility criteria.
- A **small label** (point name) is displayed above the dot at all times.

**Production mode:**
- The point is **only rendered** when the configured visibility criteria are met:
  - Player is **within a defined distance** from the point.
  - And/or the mouse is **pointing within a radius** around the point (can be disabled).
- When becoming visible/invisible: **scale interpolation animation** (lerp fade-in/out).
- Triggers the Ink stitch on interaction.

---

### Editor UI / UX

Two modes accessible from the editor:

**Mode A — Zone editor**
- Add a new zone by typing a name → zone is created and selectable.
- Select a zone from the list → click two positions in the world to place the corners.
- List of existing zones: each entry has:
  - A **name button** → closes the list and **teleports the player** to the zone center.
  - An **edit button** and a **delete button**.

**Mode B — Point editor**
- Add a new point by typing a name → records the **current player position**.
- List of existing points: each entry has:
  - A **name button** → closes the list and **teleports the player** to the point.
  - An **edit button** and a **delete button**.

---

### Preview button (stub)

Both interaction types must include a **"Preview" button** in their edit UI.
- The button is **visible but non-functional** for now.
- Leave a `// TODO: execute stitch preview` comment at the call site.

---

### Persistence

- Serialize/deserialize all interaction data to/from **JSON**.
- Follow the same serialization pattern used by the cutscene and camera angle makers.

---

### Session management (top-left buttons)

Replicate the **exact same behavior** as the cutscene/camera angle makers:
- **✕ button** (exit): opens a confirmation dialog asking whether to save before leaving.
- **Save button** (next to ✕): saves the current session **without closing** the editor.

---

## Dev mode vs production mode — summary

| | Detection Zone | Interaction Point |
|---|---|---|
| **Dev** | Blue outline + centered name rendered | Always visible: dot + name label, ignoring all criteria |
| **Prod** | Invisible — detection logic only | Rendered only when visibility criteria are met, with scale lerp |

The dev/production mode flag should follow whatever pattern is already used elsewhere in the codebase. If none exists, introduce a clean, reusable constant or config entry.

---

## Code quality requirements

- Clean, readable, maintainable code — a human should be able to understand it at a glance.
- **Respect the existing project architecture** for structural consistency.
- **Reuse existing classes and utilities** wherever applicable — do not duplicate logic already present in the cutscene or camera angle systems.
- Identify and leverage any shared base classes, render helpers, GUI components, or input handlers already in the codebase.

---

Before writing any code, **explore the project structure** and point out:
1. Which existing classes/patterns you plan to reuse.
2. Where the new files will live.
3. Any shared abstraction (e.g. a base `MakerScreen` class) worth extracting if it doesn't already exist.
4. How the dev/production mode flag is currently handled (or how you plan to introduce it).