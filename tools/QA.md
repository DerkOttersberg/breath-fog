# Development QA

The opt-in helper is in `fabric/src/qa` and is excluded from normal JARs. It uses Fabric's tick/render callbacks and Minecraft's own screenshot API; it does not install production hooks, UI automation, networking, or renderer mixins. `verifyLoaderJar` rejects leaked QA classes.

Create an isolated test save named `New World` inside `fabric/run-qa/saves`. Run Java 25 with `gradlew.bat -Pqa :fabric:runClient`. For shader testing, install your own compatible Iris/Sodium files in `fabric/run-qa/mods`, and shader packs in `fabric/run-qa/shaderpacks`. These third-party files are deliberately absent from the source bundle.

Use PowerShell 7 for the QA scripts. `tools/qa-control.ps1 -Json '{"preview":true,"camera":"FIRST_PERSON","shot":"preview","minimumParticles":10}'` writes a unique, atomic request to the QA client. Status is written to `fabric/run-qa/qa/status.json`. Screenshots use the completed main render target on the next tick, with a short delay after the requested particle count; capturing inside the level render callback would omit Iris's final composite.

Supported development requests include camera type (`FIRST_PERSON`, `THIRD_PERSON_BACK`, `THIRD_PERSON_FRONT`), FOV, yaw/pitch, enablement/intensity, preview, screenshot, config screen, close screen, resource reload, shader pack filename or `off`, synthetic remote-player count, synthetic movement strength, integrated-server command array, connect/disconnect, and client stop. A command with world-dependent actions must be issued after status says `world: true`.

Additional fixture controls (`drive`: `none`, `walk`, `sprint`, `sneak`, `swim`; `respawn`; `useBlock`: `[x,y,z]`; `wake`) and pose/collision status were added for the next acceptance pass. They compile but their runtime acceptance was paused when the user shut down the computer. They do not establish completed pose tests. The helper remains separate from production code.

Synthetic players are client-side development actors, not actual multiplayer clients. They are useful for deterministic visibility, wake, nearest-emitter, and budget stress scenes. They do not establish multiplayer interoperability. Use genuine vanilla server/client connections separately.

`tools/qa-assert.ps1 -Seconds 10 -ExpectBreath -ExpectedEmitters 24` verifies an active sampling interval, the hard emitter/particle limits and helper errors. For the crowd case, first request `{"actors":48,"motion":0.09,"reload":true}` and allow the reload to finish. `-ExpectZero -ExpectedEmitters 0` checks disable/disconnect cleanup. Stale status from a closed client is rejected, rather than counted as a successful test.

`tools/qa-benchmark.ps1 -Seconds 20` runs an off/on/off/on sequence with seven stationary synthetic remote actors plus the local player, keeping camera, FOV and shader state fixed. It disables VSync and sets the ordinary game's inactivity limiter to `MINIMIZED` for this isolated profile, so an unfocused visible window is not capped at 30 FPS. Verify `throttle: NONE` in status; do not minimize the window. It records render-event intervals, sample counts, median/p95 times and peak owned particle counts. Filesystem/status instrumentation is present in both states. Check the position remains unchanged. Warm up the world before measuring, and distinguish capped or shader-transition samples from valid pairs.

This helper deliberately has strong powers inside a disposable test world, including local server commands. Do not enable it in a world you want to preserve. It is unnecessary for installation or ordinary play.
