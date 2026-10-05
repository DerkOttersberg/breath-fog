# Architecture and decisions

Fabric, Forge, and NeoForge pass services and client lifecycle callbacks to `BreathFogClient`. The controller reads existing players and biome holders, smooths cold exposure, advances independent breathing clocks, selects the nearest players, and emits direct `BreathParticle` instances through the vanilla particle engine. Particle ticks sample a small analytic field and nearby-player wakes; vanilla extraction interpolates their world positions and submits textured, lit quads with depth testing.

Each emitter is keyed by UUID, with a seeded initial breath phase, exposure state, cached biome target, previous eye position, and a per-exhalation particle budget. Players leaving the nearest-player set are evicted before replacements are created, keeping tracked emitters inside the cap even while players exchange places. Camera switches alter visibility and the origin of future breaths; old particles retain their world position and the same clock continues. Connection or level changes clear owned particles and emitters. Resource reload invalidates sprite handles and clears only this mod's particles.

The local player and seven nearby movers can contribute wake data; a total of eight movers is bounded independently of the 24-emitter cap. Teleports above two blocks per tick contribute no velocity. Forces are clamped, then combined with drag and slight buoyancy. This produces curling, splitting vapor without pairwise fluid simulation or a three-dimensional grid.

Particle collision compares the requested displacement with the actual displacement, including ceilings, before vanilla can zero velocity components. Collided wisps damp and fade. A lifetime envelope grows and dissolves the cloud. First-person rendering additionally uses distance attenuation, lower placement, a soft central viewing cone, and a separate user-controlled strength. The vanilla shader's 0.1 fragment-alpha cutoff limits the faintest tails, so density is tuned for this existing render path.

The saved pixelated option selects a separate set of four 16×16 sprites, holds billboard roll steady, and quantizes growth into eight steps. It shares the same emission, collision, lighting, suppression, and budget behavior. Changing style removes only owned particles before selecting the new atlas sprites.

## Multiplayer boundary

No server gameplay, packets, channels, custom particle types, world attachments, synchronized data, or entity mutations exist in production code. A viewer can render the breath of an unmodded player because standard player position, head rotation, sprint state, visibility, and the surrounding biome already reach the client. Timing is intentionally viewer-local. The mod cannot synchronize precisely identical breaths across observers without additional networking.

The `assets/minecraft/atlases/particles.json` resource contains eight additive namespaced single-sprite sources. It extends the particle atlas instead of replacing vanilla particles or installing an atlas filter. Missing sprites suspend emission with a log message until the next resource reload.

## Shader and performance boundary

The renderer uses `SingleQuadParticle.Layer.TRANSLUCENT` and vanilla lighting. Iris has its own integration of the particle feature renderer; Breath Fog leaves that integration in control. Background refraction and custom blend/framebuffer pipelines are deferred because they would expand the compatibility surface significantly. Pack-specific brightness and ordering are not forced by the mod.

Selection work is bounded to a 24-item nearest list while scanning the current world's player list. Simulation has a 256-particle cap, at most eight wake samples per particle, no particle-particle interaction, and reused three-component scratch arrays. The normal target is far below that cap: a nearby emitter uses 12 particles per breath, fewer at distance or with reduced vanilla particle settings.

## Primary research

The initial design research below belongs to the 26.2 foundation. The 26.3 port uses the current catalog pins, inspected official 26.3 classes, and fresh packaged-client acceptance on every supported loader. Historical shader and hardware results do not establish 26.3 acceptance.
