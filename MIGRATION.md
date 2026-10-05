# Upgrading Breath Fog

Install the JAR built for the exact Minecraft version and loader, replacing the previous Breath Fog JAR. Keep one Breath Fog runtime JAR in the profile. Fabric also needs the matching Fabric API; Mod Menu is optional. Forge and NeoForge provide their own Mods config entry.

The mod ID remains `breath_fog`, and settings remain in `config/breath_fog.json`. Existing settings keep their values. Configs without `pixelated` default to soft vapor. Enable **Pixelated Minecraft style** on the Appearance page and select **Save** to persist it; **Cancel** discards the draft.

Malformed configs are preserved while defaults are used. Saving recovered settings first copies the original bytes to a unique `breath_fog-invalid-*.json.bak` file. A failed write reports an error and leaves the settings screen open.

Breath Fog stores no world data, registers no world content, and sends no packets. The viewer alone installs it. World/version migration remains Minecraft's own responsibility; use separate profiles for different Minecraft versions.

See [README.md](README.md) for the version branches and [TESTING.txt](TESTING.txt) for the exact accepted artifacts and limitations.
