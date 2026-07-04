# JETT (Just Enough Tooltips) Changelog

---

## v1.5.0 — 2026-07-04

### 🔧 Improvements

- **Dynamic base value for merged modifiers.** `mergeModifiers` now reads `Attribute.getDefaultValue()` instead of hardcoding +1/+4 for attack damage and attack speed — compatible with mods that alter base empty-hand values.
- **Unified versioning.** All three branches (1.16.5, 1.20-1.20.4, 1.20.6) now share the same semantic version.

---

## v1.4.2 — 2026-06-06

### ✨ Features

- **Global tooltip.** New `[globalTooltip]` config section: `globalTooltip` + `globalTooltipBlacklist`.
- **Multi-item matching in custom tooltips.** Comma-separated item lists (e.g. `"minecraft:diamond,diamond_block=text"`).
- **Hide potion effect header.** `hidePotionEffectHeader` hides "When Applied:" on tipped arrows and potions (1.16.5 only).

### 🔧 Improvements

- **Config file restructured.** `[general]` section consolidated; all five sections with bilingual headers and color-code references.
- **Field-level Javadoc unified.** Consistent Chinese/English coverage across all config fields.
- **Wiki updated.** Full documentation for all features, 8-step execution order, FAQ.

---

## v1.4.0 — 2026-06-05

### ✨ Features

- **Global tooltip.** New `[globalTooltip]` config section. Set `globalTooltip` to a string to display it on **every item's tooltip** — perfect for server names, modpack watermarks, or usage hints. Supports `N:text` insert position and `globalTooltipBlacklist` to exclude specific items (e.g. `minecraft:air`).
- **Multi-item matching in custom tooltips.** `customTooltips` keys now support comma-separated item lists (e.g. `"minecraft:diamond,diamond_block,emerald=0:§b★ Gem ★"`). One config line can now target multiple items, drastically reducing boilerplate for grouped items.
- **Hide potion effect header.** New `hidePotionEffectHeader` config option (under `[slotHeaders]`) hides the "When Applied:" line on tipped arrows and potions.

### 🔧 Improvements

- **Config file restructured.** `[general]` section was previously split across two locations in the generated TOML — now properly consolidated into one. All five config sections (`general`, `globalTooltip`, `slotHeaders`, `customTooltips`, `regexRemove`) have clean separators, bilingual descriptions, and inline color-code reference tables.
- **Field-level Javadoc unified.** Every config field now has consistent Javadoc covering both Chinese and English usage.
- **Wiki updated.** Fully documented all new features in JETT-Wiki.md, including execution order (8-step pipeline) and FAQ.

---

## v1.2.1 — 2026-06-04

### 🐛 Bug Fixes

- **Fixed crash caused by invalid regex.** Configuring an invalid regex pattern (e.g. `"broken[regex"`) in `regexRemove` or `perItemRegexRemove` no longer throws `PatternSyntaxException`. Instead, a descriptive error is logged and the invalid pattern is skipped.

### ✨ Features

- **Debug logging system.** Set `debug = true` in `jett-common.toml` to see the full tooltip processing pipeline in the game log, including per-item step trace, modifier merge details, regex match results, and more. Zero performance cost when disabled.

### 🔧 Improvements

- Unified version number to `1.2.1` across all config files (`build.gradle`, `gradle.properties`, `mods.toml`, `JETT-Wiki.md`).
- Updated `pack.mcmeta` description from early dev name "Green Tooltip Hider" to the official name "Just Enough Tooltips".
- Fixed stale `mod_version` in `gradle.properties` (was stuck at 1.0.0).

---

## v1.2.0

- Initial public release.
- Seven feature modules: hide attribute modifiers, merge multi-modifiers, hide slot headers, item whitelist, custom tooltip text, hide item names, regex-based removal.
- Client-side only, zero server dependencies.
- Full user documentation (JETT-Wiki.md).
