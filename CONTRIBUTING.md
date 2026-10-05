# Contributing

Choose the branch matching the exact Minecraft version. Keep all its supported loaders in that branch; avoid permanent per-loader branches. Preserve `breath_fog` and the local config filename so upgrades retain settings. Code and original assets are CC0-1.0.

This historical branch preserves the 26.2 Fabric MVP; use a maintained version branch for the multi-loader implementation. Before a source change is accepted, run the branch's `clean check build` and inspect the runtime JARs. Java/toolchain and loader pins live in `gradle/libs.versions.toml`. Legacy branches use regular Loom and installable `remapJar` outputs; development and QA JARs are not release artifacts.

For rendering/settings changes, use separately packaged QA helpers and copied production profiles. Test both styles, config Save/Cancel/validation/restart, camera visibility, cold/warm biomes, resource reload, movement and suppression, respawn/disconnect, emitter/particle budgets, native settings access, and an unmodded loopback server. Keep client testing on the private WSL display. See the maintained branch's tools/QA.md and TESTING.txt for the version-specific commands and actual evidence.

Record final artifact hashes, scenario names, logs, screenshots, renderer and limitations. Keep failed attempts. Client-only rendering is verified in real clients rather than server gameplay GameTests. Publishing a binary release or changing repository visibility/default branch requires an explicit owner request.
