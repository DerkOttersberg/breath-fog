# Changes

## 1.0.1+mc26.3

- Included the current owner license policy and preserved prior notices in runtime and source JARs; earlier granted rights remain unchanged.
- Default new configs and Reset defaults to pixelated Minecraft-style breath while preserving saved soft-style choices.
- Bundle the owner-supplied 400x400 PNG icon unchanged for loader menus.
- Add config migration checks and exact packaged-icon validation.
- Correct NeoForge's mod-list icon metadata to use `logoFile` and check that reference during packaging.

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
