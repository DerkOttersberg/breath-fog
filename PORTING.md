# Minecraft 1.21.1 port

This branch contains common code plus Fabric, Forge, and NeoForge in one project. Java 21 is the compile/game toolchain; Java 25 runs Gradle 9.5.1. Regular Architectury Loom 1.17.493 and official Mojang mappings remap Fabric to intermediary. Forge 1.20.1 uses SRG; Forge/NeoForge 1.21.1 use Mojang names in their packaged runtimes. `remapJar` is the installable artifact; the named development JAR is not a release.

Vanilla `TextureSheetParticle` / `PARTICLE_SHEET_TRANSLUCENT` render both styles, using camera-relative vertex rendering. `GuiGraphics` supplies the responsive settings UI. Native loader tick, logout, command and config-screen APIs feed the same common controller. The 26.3-specific Forge loop/command mixins are absent here.

Common never imports loaders; portable core never imports Minecraft. Existing config identity and CC0 licensing are preserved. No runtime Architectury or SeamlessLib dependency is introduced into this existing visual mod.

Run `./gradlew --no-build-cache clean check build` for unit, isolation and packaged-JAR checks. Development helpers are built separately with `-Pqa` and each loader's `remapQaHelperJar` task. They drive copied genuine production profiles through the private WSL display. See TESTING.txt for actual acceptance.

Version branches: [26.3](https://github.com/DerkOttersberg/breath-fog/tree/26.3), [1.21.1](https://github.com/DerkOttersberg/breath-fog/tree/1.21.1), [1.20.1](https://github.com/DerkOttersberg/breath-fog/tree/1.20.1), and historical [26.2](https://github.com/DerkOttersberg/breath-fog/tree/26.2). Quilt is outside the maintained matrix. No cross-version binary compatibility is implied.
