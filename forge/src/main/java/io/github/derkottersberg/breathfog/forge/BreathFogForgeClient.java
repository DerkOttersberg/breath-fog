package io.github.derkottersberg.breathfog.forge;

import io.github.derkottersberg.breathfog.client.BreathFogClient;
import io.github.derkottersberg.breathfog.client.BreathFogConfigScreen;
import io.github.derkottersberg.breathfog.client.BreathFogCommands;
import io.github.derkottersberg.breathfog.platform.ClientPlatformServices;
import java.nio.file.Path;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

final class BreathFogForgeClient {
    static void initialize(FMLJavaModLoadingContext context) {
        var controller = BreathFogClient.instance();
        controller.initialize(new Services());
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener((TickEvent.ClientTickEvent event) -> { if (event.phase == TickEvent.Phase.END) controller.tick(Minecraft.getInstance()); });
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut event) -> controller.clear());
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener((RegisterClientCommandsEvent event) -> BreathFogCommands.register(event.getDispatcher()));
        net.minecraftforge.fml.ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
            () -> new ConfigScreenHandler.ConfigScreenFactory((client, parent) -> new BreathFogConfigScreen(parent)));
    }
    private static final class Services implements ClientPlatformServices {
        public String loaderName() { return "Forge"; }
        public Path configDirectory() { return FMLPaths.CONFIGDIR.get(); }
    }
}
