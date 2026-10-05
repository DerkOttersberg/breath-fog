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
        TickEvent.ClientTickEvent.Post.BUS.addListener(event -> controller.tick(Minecraft.getInstance()));
        ClientPlayerNetworkEvent.LoggingOut.BUS.addListener(event -> controller.clear());
        RegisterClientCommandsEvent.BUS.addListener(event -> BreathFogCommands.register(event.getDispatcher()));
        context.getContainer().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
            () -> new ConfigScreenHandler.ConfigScreenFactory(BreathFogConfigScreen::new));
    }
    private static final class Services implements ClientPlatformServices {
        public String loaderName() { return "Forge"; }
        public Path configDirectory() { return FMLPaths.CONFIGDIR.get(); }
    }
}
