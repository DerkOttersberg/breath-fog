# Breath Fog

Client-only breath vapor for **Minecraft Java 1.21.1**, supporting **Fabric, Forge, and NeoForge**. Cold air becomes visible for you and nearby players as layered exhalations. Choose soft vapor or a pixelated Minecraft style in the settings.

## Minecraft version branches

| Minecraft | Loaders | Source |
| --- | --- | --- |
| 26.3 | Fabric, Forge, NeoForge | [26.3](https://github.com/DerkOttersberg/breath-fog/tree/26.3) |
| 1.21.1 | Fabric, Forge, NeoForge | [1.21.1](https://github.com/DerkOttersberg/breath-fog/tree/1.21.1) |
| 1.20.1 | Fabric, Forge | [1.20.1](https://github.com/DerkOttersberg/breath-fog/tree/1.20.1) |
| 26.2 | Original Fabric version | [26.2](https://github.com/DerkOttersberg/breath-fog/tree/26.2) |

Each version branch contains every supported loader for that Minecraft version. Quilt is outside the maintained support matrix. Read the chosen branch's install instructions and acceptance report before building.

## Install

Use Java 21 and the JAR matching your loader:

| Loader | Tested version | Required extras |
| --- | --- | --- |
| Fabric | 0.19.5 | Fabric API 0.116.17+1.21.1 |
| Forge | 52.1.16 | None |
| NeoForge | 21.1.255 | None |

Install one `breath-fog-1.0.0+mc1.21.1-<loader>.jar` in your profile's `mods` folder. The older Fabric 26.2 source remains on branch `26.2`.

Only the viewer needs the mod. Servers and other players need no installation. It sends no packets, registers no particle types, changes no gameplay, and stores no world data. Breath timing is independently simulated by each viewer.

## Settings

Open `/breathfog config`, Mod Menu on Fabric (optional), or Forge/NeoForge's Mods config button. `/breathfog preview` starts a 20-second preview even in warm biomes.

- **Pixelated Minecraft style** uses original 16×16 sprites, steady orientation, and stepped growth. Disable it for the original soft, curling vapor.
- Visibility controls cover first person, both third-person views, and nearby players.
- Vapor intensity: 0.10–2.00. First-person strength: 0.10–1.50.
- Save applies your draft; Cancel discards it; Reset defaults changes the draft. Drafts survive pagination and resizing. Preview uses your saved settings.

Settings are local to your client, in `config/breath_fog.json`. Existing configs retain their values and default to soft vapor. Invalid configs remain intact during recovery; a subsequent Save first preserves their original bytes in a uniquely named `.json.bak` file. Writes are atomic when supported by the filesystem.

Actual 1.21.1 screenshots: [soft vapor](docs/images/1.21.1/soft-first.png), [pixelated vapor](docs/images/1.21.1/pixel-first.png), and [Appearance settings](docs/images/1.21.1/settings-pixel-toggle.png).

## Behavior

Cold means a biome base temperature below 0.5 or the conventional `c:is_cold` biome tag. Exposure blends over ten ticks; biome sampling adds up to ten ticks of detection latency. Indoors follows the same biome rule. Dead, sleeping, invisible, spectating, or submerged players do not emit. Preview respects these suppression and visibility rules.

Idle breaths start every 3.2–4.8 seconds, or 2.2–3.2 while sprinting. Seven ticks of emission form a plume. Wisps last 18–26 ticks, expand, rise, collide, and dissolve. At most 24 nearest emitters and 256 owned particles are tracked; distance and vanilla particle settings reduce emission.

Rendering uses vanilla's lit translucent particle path and depth testing. Original namespaced sprites and the icon are bundled; resource packs can replace the sprites. Style changes clear existing breath particles immediately. Reloads reacquire atlas sprites. The first-person view-cone fade keeps the crosshair clear. A direct rear view can naturally hide the early plume behind the head.

## Build and verification

With Java 25 running Gradle and Java 21 available as a toolchain: `./gradlew clean check build` (`gradlew.bat` on Windows). `build.ps1` can obtain a project-local, checksum-verified Java toolchain. Loader runtime and source JARs appear in each loader's `build/libs` folder. Architectury is build tooling; no Architectury API runtime dependency is required. This existing client visual mod remains independently installable and uses no SeamlessLib contracts.

`check` runs the portable unit tests, common-loader isolation, and all 3 packaged-JAR checks. See [TESTING.txt](TESTING.txt) for exact runtime evidence and limits. The separately packaged `-Pqa :fabric:remapQaHelperJar :forge:remapQaHelperJar :neoforge:remapQaHelperJar` instrumentation is never included in a runtime JAR. [.github/scripts/playtest_isolated.py](.github/scripts/playtest_isolated.py) drives copied production profiles through a private WSL Xvfb display.

See [MIGRATION.md](MIGRATION.md), [PORTING.md](PORTING.md), [ARCHITECTURE.md](ARCHITECTURE.md), and [art/ASSETS.md](art/ASSETS.md). Original code and assets are dedicated under CC0.

## License

**All Rights Reserved** for new original material owned by Derk Ottersberg.
See [LICENSE.txt](LICENSE.txt) and [licensing history](LICENSES/README.md) for prior-license and third-party exceptions.

Public source may be viewed and forked on GitHub. Issues and pull requests are welcome;
write access to this repository is reserved for the owner.
