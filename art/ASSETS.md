# Original assets

Four vapor images were generated specifically for this mod with OpenAI's image generation tool on 2026-10-05. They are not downloaded Minecraft, shader-pack, stock, or third-party mod textures. The art direction was neutral pale condensation, genuinely transparent background, airy irregular edges, curled fine wisps, subtle density variation, and no flames, symbols, text, scenery, or square borders.

The four independent variants are a compact exhalation puff, thin curled filaments, a more diffuse puff, and a curling filament plume. Full-resolution originals are in `source/wisp_0.png` through `source/wisp_3.png`. Production sprites are 128×128 RGBA images in `common/src/main/resources/assets/breath_fog/textures/particle`, resized with Lanczos while preserving generated alpha. The contact sheet is for inspection, not a texture atlas.

`texture-validation.json` records dimensions, alpha ranges, partial-alpha pixel counts, and maximum alpha along all four borders. Every production sprite has a fully transparent border. The build checks those invariants in the packaged JAR as well.

The 400×400 mod icon was supplied by the owner as `Kopie van Template(1).png`. Its original PNG bytes are bundled unchanged for Fabric, Forge and NeoForge menus where supported. `icon-provenance.json` records its hash and dimensions. The previous abstract icon vector is retained in `historical/original-abstract-icon.svg` as historical source.

Earlier material retains its CC0 dedication to the extent that rights can be dedicated. The supplied icon and other new original material owned by Derk Ottersberg follow the current policy in `LICENSE.txt`; prior and third-party rights are preserved in `LICENSES/README.md`. No exclusive copyright claim is made for AI-generated imagery. Minecraft, Fabric, Iris, Sodium, Complementary, and BSL remain the property of their respective authors. Third-party game or shader files used in local QA are not redistributed.

The 26.3 pixelated style adds four original code-authored 16×16 RGBA sprites. Reproduce them with Python 3 using `art/generate_pixel_sprites.py` (standard library only). Broad flat opacity bands and stepped silhouettes are authored directly; these are independent assets, not resized vanilla textures.
