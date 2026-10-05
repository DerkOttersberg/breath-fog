# Porting

Branch `26.3` contains common code and Fabric, Forge, and NeoForge adapters for Minecraft 26.3 / Java 25. Branch `26.2` preserves the Fabric MVP. Keep one Minecraft version per branch with all supported loaders together.

`common/core` is portable Java; `common/config` uses Minecraft's Gson. `common/client` contains target-version Minecraft rendering and settings. Explicit `ClientPlatformServices` provides the loader name and config directory. Loader modules own initialization, tick/disconnect events, local commands, and native config access. Common imports no loader APIs. No runtime Architectury API, reflective adapter discovery, synchronized particle registry, networking, or sibling gameplay dependency is used.

Fabric bundles common output. Forge and NeoForge compile common sources/resources into their own JARs. Forge initializes client code only on the client distribution; NeoForge's entrypoint declares `Dist.CLIENT`. A server requires no installation.

For a new version, check real particle construction/extraction, camera and atlas APIs, UI/input methods, loader events, native config registration, resource format, and metadata constraints. Minecraft 26.3 uses `LocalPlayer.sendSystemMessage`, `Minecraft.resizeGui`, and input-aware Button callbacks. Atlas lookup uses `AtlasIds.PARTICLES`. The controller detects replaced atlases/sprites before emitting after reload; Fabric also supplies an apply-phase reload callback.

Run `clean check build`, inspect the exact packaged artifacts, and playtest every loader using its production installation. Shared visual tests need an actual world; server GameTests do not exercise this client-only renderer. QA source sets produce separate helper JARs; they never enter distributables. Keep private-display isolation and copied profiles/worlds.

Use [TESTING.txt](TESTING.txt) to record actual passes and limitations. A different version or loader needs compatible code plus new runtime evidence, not wider metadata ranges.

Forge 66.0.9's legacy tick and command events miss parts of the 26.3 in-world loop, as verified by the QA render probe. Forge-only lifecycle/command mixins bridge those paths. The simulation hook runs at the end of `Minecraft.tick`; a frame hook also handles menu requests and level changes. The shared controller advances once per client game time value, preventing double updates if both hooks or the native event fire. Simulation timing is therefore independent of how often frames render. These hooks do not replace shaders, particle rendering, or framebuffers.
