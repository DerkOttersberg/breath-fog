# Original assets

Four vapor images were generated specifically for this mod with OpenAI's image generation tool on 2026-10-05. They are not downloaded Minecraft, shader-pack, stock, or third-party mod textures. The art direction was neutral pale condensation, genuinely transparent background, airy irregular edges, curled fine wisps, subtle density variation, and no flames, symbols, text, scenery, or square borders.

The four independent variants are a compact exhalation puff, thin curled filaments, a more diffuse puff, and a curling filament plume. Full-resolution originals are in `source/wisp_0.png` through `source/wisp_3.png`. Production sprites are 128×128 RGBA images in `common/src/main/resources/assets/breath_fog/textures/particle`, resized with Lanczos while preserving generated alpha. The contact sheet is for inspection, not a texture atlas.

`texture-validation.json` records dimensions, alpha ranges, partial-alpha pixel counts, and maximum alpha along all four borders. Every production sprite has a fully transparent border. The build checks those invariants in the packaged JAR as well.

The 256×256 mod icon is an original abstract exhalation motif. Editable vector source is `common/src/main/resources/assets/breath_fog/icon.svg`; the packaged PNG is used by loader menus. An icon regeneration dependency is not required to build or install the mod.

The project's CC0 dedication applies to the original source and assets to the extent that rights can be dedicated. No exclusive copyright claim is made for AI-generated imagery. Minecraft, Fabric, Iris, Sodium, Complementary, and BSL remain the property of their respective authors. Third-party game or shader files used in local QA are not redistributed.
