# Changelog

## 1.0.1+mc1.21.1

- Included the current owner license policy and preserved prior notices in runtime and source JARs; earlier granted rights remain unchanged.
- Make pixelated Minecraft-style breath the default for fresh configs and Reset defaults; preserve explicit saved soft-style choices.
- Bundle the owner-supplied 400×400 icon unchanged for loader menus.
- Verify fresh/default config persistence, explicit soft-style migration, and exact packaged icon bytes.

## 1.0.0+mc1.21.1

- Ported both vapor styles, responsive settings, local preview/config commands, reload cleanup, and bounded client simulation to Minecraft 1.21.1.
- Added packaged Fabric, Forge, and NeoForge adapters with native settings integration and production remapping.
- Kept settings labels and tooltips sharp in the 1.21.1 screen render pipeline.
- Preserved config compatibility and original CC0 code/assets.
- See TESTING.txt for version-specific actual acceptance.

# Changes

## 1.0.1+mc1.21.1

- Default new configs and Reset defaults to pixelated Minecraft-style breath while preserving saved soft-style choices.
- Bundle the owner-supplied 400x400 PNG icon unchanged for loader menus.
- Add config migration checks and exact packaged-icon validation.

## 1.0.0+mc26.3

- Ported the shared client implementation to Minecraft 26.3.
- Added real Forge and NeoForge builds alongside Fabric, with native Mods config screens and local config/preview commands.
- Added the saved Pixelated Minecraft style option, original 16×16 sprites, steady orientation, and stepped expansion.
- Clear owned particles when changing style and reacquire sprites after resource reload.
- Preserve invalid configuration bytes in a unique backup before saving recovered defaults.
- Wrap settings tooltips to fit smaller GUI sizes; retain drafts across pages and resizing.
- Verify all three loader JARs, resource contents, dependencies, Java bytecode, and exclusion of QA helpers.
- Added portable production-client QA helpers and a private WSL playtest driver.
- Bridged Forge 66's missing in-world tick and local-command hooks, with actual simulation ticks and duplicate-update prevention.
