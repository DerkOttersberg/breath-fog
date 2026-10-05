package io.github.derkottersberg.breathfog.neoforge;

import io.github.derkottersberg.breathfog.client.BreathFogClient;
import io.github.derkottersberg.breathfog.client.BreathFogConfigScreen;
import io.github.derkottersberg.breathfog.client.BreathFogCommands;
import io.github.derkottersberg.breathfog.platform.ClientPlatformServices;
import java.nio.file.Path;
import net.minecraft.client.Minecraft;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

final class BreathFogNeoForgeClient {
    static void initialize(ModContainer container) {
        var controller = BreathFogClient.instance();
        controller.initialize(new Services());
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> controller.tick(Minecraft.getInstance()));
        NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut event) -> controller.clear());
        NeoForge.EVENT_BUS.addListener((RegisterClientCommandsEvent event) -> BreathFogCommands.register(event.getDispatcher()));
        container.registerExtensionPoint(IConfigScreenFactory.class,
            (modContainer, parent) -> new BreathFogConfigScreen(parent));
    }
    private static final class Services implements ClientPlatformServices {
        public String loaderName() { return "NeoForge"; }
        public Path configDirectory() { return FMLPaths.CONFIGDIR.get(); }
    }
}
