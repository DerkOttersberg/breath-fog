# Private client QA

For default/icon-only updates after a full baseline acceptance, an owned staging tree may contain `focus-default-icon-update` to run the focused update suite. Its PASS record explicitly names that scope. It checks fresh/default and restart settings, native config access, Save/Cancel, validation, resize, both styles/cameras, reload, server connection and cleanup. Use the full suite for rendering, timing, lifecycle or platform behavior changes; retain the previous full acceptance separately.

Shared instrumentation lives in `common/src/qa`; loader bootstraps and metadata live in each loader's `src/qa`. Build the separate helpers with `-Pqa :fabric:remapQaHelperJar :forge:remapQaHelperJar`. Runtime JAR verification rejects QA classes. Neither helpers nor development worlds are release artifacts.

`.github/scripts/playtest_isolated.py` takes a Linux staging tree, loader, copied production-client template, isolated-display wrapper, and fresh output path. An optional sixth argument supplies matching sibling runtime JARs. `BREATH_FOG_QA_WORLD` selects an owned copied world; `BREATH_FOG_QA_SERVER` selects a loopback vanilla test server; `BREATH_FOG_QA_SKIP_MODMENU=1` omits the optional Fabric integration for an absence profile; `BREATH_FOG_QA_CONFIG` supplies a previously saved config to check a fresh process.

Use only a private, nonzero Xvfb display with WSLg/Wayland removed, a single-client lock, bounded CPU/heap/time, and low priority. An owned copy may wait on the same global lock before its bounded client runtime. The wrapper at `C:/Users/derko/Desktop/minecraft/tools/Run-IsolatedMinecraftClient.sh` supplies these constraints. Never use desktop input or launch Windows Minecraft for this workflow. Options and early-window settings belong only to disposable profiles.

The helper drives Minecraft's own controls and screenshot API. It exercises settings buttons, native registered config factories, local commands, both styles, cameras, sprint/sneak/riding, sleeping/death/underwater/invisibility/spectator suppression, resource reload, missing-sprite pack recovery, glass collision, crowded-player budgets, warm-biome preview, respawn, server connection, and cleanup. Captures require actual owned particles when specified. Synthetic actors test selection/motion budgets; they are not actual networked guests.

`run_vanilla_server.py` launches a fresh, unmodded, loopback-only offline development server using an already accepted QA EULA. Stop the owned server through its stdin.

Keep logs, request/status JSON, screenshots, installed-JAR hashes, and PASS markers. Preserve failed attempts. See `TESTING.txt` for the actual tested driver/backend and limitations. Historical 26.2/26.3 evidence is distinct from this branch's acceptance. The older PowerShell controls are retained for historical use; the current acceptance driver is the isolated Python workflow.

If a private wrapper queues on the shared lock, `BREATH_FOG_QA_STARTUP_SECONDS` can extend startup allowance to include that wait. Keep the client runtime timeout and CPU/heap limits intact. Companion NeoForge mods may require a NeoForge server even though the standalone Breath Fog client accepts a vanilla server; choose each fixture according to its installed mods.

QA resource packs include matching metadata so Forge startup has no helper-induced warning screen. The helper writes status atomically; the driver retains complete snapshots rather than treating an incomplete read as zero particles. The extended natural-cadence trace reads actual own-clock phase transitions for 30 seconds without preview.
