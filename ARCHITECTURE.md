# Architecture and decisions

Fabric, Forge, and NeoForge pass services and client lifecycle callbacks to `BreathFogClient`. The controller reads existing players and biome holders, smooths cold exposure, advances independent breathing clocks, selects the nearest players, and emits direct `BreathParticle` instances through the vanilla particle engine. Particle ticks sample a small analytic field and nearby-player wakes; vanilla rendering interpolates their world positions and submits textured, lit quads with depth testing.

Each emitter is keyed by UUID, with a seeded initial breath phase, exposure state, cached biome target, previous eye position, and a per-exhalation particle budget. Players leaving the nearest-player set are evicted before replacements are created, keeping tracked emitters inside the cap even while players exchange places. Camera switches alter visibility and the origin of future breaths; old particles retain their world position and the same clock continues. Connection or level changes clear owned particles and emitters. Resource reload invalidates sprite handles and clears only this mod's particles.

The local player and seven nearby movers can contribute wake data; a total of eight movers is bounded independently of the 24-emitter cap. Teleports above two blocks per tick contribute no velocity. Forces are clamped, then combined with drag and slight buoyancy. This produces curling, splitting vapor without pairwise fluid simulation or a three-dimensional grid.

Particle collision compares the requested displacement with the actual displacement, including ceilings, before vanilla can zero velocity components. Collided wisps damp and fade. A lifetime envelope grows and dissolves the cloud. First-person rendering additionally uses distance attenuation, lower placement, a soft central viewing cone, and a separate user-controlled strength. The vanilla shader's 0.1 fragment-alpha cutoff limits the faintest tails, so density is tuned for this existing render path.

The saved pixelated option selects a separate set of four 16×16 sprites, holds billboard roll steady, and quantizes growth into eight steps. It shares the same emission, collision, lighting, suppression, and budget behavior. Changing style removes only owned particles before selecting the new atlas sprites.

## Multiplayer boundary

No server gameplay, packets, channels, custom particle types, world attachments, synchronized data, or entity mutations exist in production code. A viewer can render the breath of an unmodded player because standard player position, head rotation, sprint state, visibility, and the surrounding biome already reach the client. Timing is intentionally viewer-local. The mod cannot synchronize precisely identical breaths across observers without additional networking.

The `assets/minecraft/atlases/particles.json` resource contains eight additive namespaced single-sprite sources. It extends the particle atlas instead of replacing vanilla particles or installing an atlas filter. Missing sprites suspend emission with a log message until the next resource reload.

## Shader and performance boundary

The renderer uses `TextureSheetParticle` with `PARTICLE_SHEET_TRANSLUCENT` and vanilla lighting. Iris has its own integration of the particle feature renderer; Breath Fog leaves that integration in control. Background refraction and custom blend/framebuffer pipelines are deferred because they would expand the compatibility surface significantly. Pack-specific brightness and ordering are not forced by the mod.

Selection work is bounded to a 24-item nearest list while scanning the current world's player list. Simulation has a 256-particle cap, at most eight wake samples per particle, no particle-particle interaction, and reused three-component scratch arrays. The normal target is far below that cap: a nearby emitter uses 12 particles per breath, fewer at distance or with reduced vanilla particle settings.

## Primary research

The links below document the original 26.2 research. This 1.21.1 port uses its own catalog pins, inspected official Minecraft classes, and packaged production-client acceptance. Historical shader/hardware results do not establish acceptance for this branch.

- [Seamless Crafting version catalog](https://github.com/DerkOttersberg/seamless-crafting/blob/26.2/gradle/libs.versions.toml): Gradle 9.5.1, Java 25, Architectury plugin 3.5.169, Loom 1.17.491, Loader 0.19.3, Fabric API 0.159.0+26.2.
- [Fabric 26.2 update](https://fabricmc.net/2026/06/15/262.html) and [Fabric particle documentation](https://docs.fabricmc.net/develop/rendering/particles/creating-particles): current client APIs and vanilla rendering integration.
- [Bridson, Hourihan and Nordenstam, Curl-Noise for Procedural Fluid Flow](https://www.cs.ubc.ca/~rbridson/docs/bridson-siggraph2007-curlnoise.pdf): the principle of deriving a divergence-free field from a vector potential. This MVP uses its own small analytic sinusoidal potential, rather than claiming a full fluid simulation or copying a solver.
- [Iris 26.2 particle integration](https://github.com/IrisShaders/Iris/blob/26.2/common/src/main/java/net/irisshaders/iris/mixin/MixinParticleEngine.java): the standard particle submission path is integrated into Iris's rendering phases.
- Minecraft 26.2's official client JAR was inspected locally to verify current APIs, atlas IDs, and the existing particle shader's alpha cutoff. These details were then checked in live game runs.
