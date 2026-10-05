# Porting foundation

The starting reference was [Seamless Crafting, branch 26.2](https://github.com/DerkOttersberg/seamless-crafting/tree/26.2), inspected at commit `74ec7fb8ea0fc055b95a4297ea7bd9a056702550`. Its [porting guide](https://github.com/DerkOttersberg/seamless-crafting/blob/26.2/PORTING.md), shared common module, explicit platform services, catalog, isolation checks, and packaged-loader checks are the foundation reused here. This is a standalone mod with its own identity and client-only design.

## What transfers

- `common/core`: plain Java breathing, cold exposure, camera attenuation, analytic curl/wakes, and emission budgets. No Minecraft or loader imports; deterministic tests run without a game.
- `common/config`: settings validation, recovery, and atomic persistence. No loader imports. Gson is supplied by Minecraft at runtime.
- `common/client`: Minecraft-specific particle simulation and submission, emitter orchestration, and settings UI. Shared by loaders within this Minecraft version; this layer still needs adaptation across Minecraft versions.
- `common/platform/ClientPlatformServices`: the loader explicitly supplies its name and configuration path to the shared bootstrap. No reflection or service discovery.
- `fabric`: initialization, tick/disconnect hooks, resource-reload registration, client commands, and optional Mod Menu integration. All Fabric imports stay here.
- `gradle/libs.versions.toml`: Minecraft, Java, build plugins, loader, API, and test dependency pins in one place.

Architectury supplies build tooling. There is no Architectury API mod dependency and no runtime transformation of common code. Fabric's JAR bundles common output directly. The compile-only Loader annotation dependency in common supports annotations on Loom's merged Minecraft classes; no loader APIs are imported or shipped by common.

## Add another loader within 26.2

1. Add a thin loader project, dependency catalog entries, loader metadata, and its build configuration. Do not add its imports to common.
2. Implement `ClientPlatformServices` and explicitly call `BreathFogClient.initialize` once.
3. Wire client end-tick and disconnect events. Invoke `resourcesReloaded` after resource reload; shared code reacquires sprites on the next client tick.
4. Register loader-native local config/preview commands and optional settings-menu integration.
5. Bundle common classes/resources and the CC0 license. Keep metadata client-only. Preserve namespaced additive atlas sources and direct particle instances: do not add a synchronized particle registry.
6. Add isolation rules, that loader's packaged-JAR assertions, and a CI job. Run genuine game, server, unmodded LAN guest, and shader checks; compilation does not establish compatibility.

## Add another Minecraft version

Use a version branch such as `1.21.11` or the desired actual release. Keep the stable mod ID `breath_fog`, package `io.github.derkottersberg.breathfog`, config name `breath_fog.json`, and asset namespace. Update pins once in the catalog and version constraints/resource format in metadata. Reuse the pure core and its tests.

Adapt the shared Minecraft layer deliberately. Check particle construction, render-state extraction and layer names, camera vectors/position, atlas lookup, biome access, pose/eye position, collision, GUI extraction, and resource reload APIs. Check loader APIs and Mod Menu separately. Minecraft 26.2 `AtlasManager.getAtlasOrThrow` accepts **`AtlasIds.PARTICLES`**, not the texture location `TextureAtlas.LOCATION_PARTICLES`; confusing these compiled successfully but crashed on the first cold-biome emission during testing.

Run `clean check build`. `verifyCommonIsolation` blocks loader imports in common and Minecraft imports in core. `verifyLoaderJar` checks client identity, version, common code, asset dimensions and transparent borders, license, additive atlas content, and absence of QA code, embedded dependencies, renderer mixins, foreign loader metadata, and core shaders.

Start with one loader that actually builds and runs. Empty loader projects would imply unsupported targets. The repository deliberately ships only Fabric 26.2 until additional ports are implemented and tested.
