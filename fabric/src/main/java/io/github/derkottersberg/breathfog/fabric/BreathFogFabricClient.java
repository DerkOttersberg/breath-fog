package io.github.derkottersberg.breathfog.fabric;

import io.github.derkottersberg.breathfog.client.BreathFogClient;
import io.github.derkottersberg.breathfog.BreathFog;
import io.github.derkottersberg.breathfog.client.BreathFogConfigScreen;
import io.github.derkottersberg.breathfog.platform.ClientPlatformServices;
import java.nio.file.Path;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;

public final class BreathFogFabricClient implements ClientModInitializer {
    @Override public void onInitializeClient() {
        BreathFogClient controller=BreathFogClient.instance();
        controller.initialize(new FabricServices());
        ClientTickEvents.END_CLIENT_TICK.register(controller::tick);
        ClientPlayConnectionEvents.DISCONNECT.register((handler,client)->controller.clear());
        ClientCommandRegistrationCallback.EVENT.register((dispatcher,registryAccess)->dispatcher.register(
            ClientCommandManager.literal("breathfog")
                .then(ClientCommandManager.literal("config").executes(context->{
                    var client=context.getSource().getClient();
                    // Chat closes its screen after command execution; enqueue opening for afterward.
                    controller.requestSettingsScreen();
                    return 1;
                }))
                .then(ClientCommandManager.literal("preview").executes(context->{
                    controller.preview();
                    context.getSource().sendFeedback(Component.literal("Breath Fog preview: 20 seconds. Try both camera views."));
                    return 1;
                }))));
    }
    private static final class FabricServices implements ClientPlatformServices {
        @Override public String loaderName() { return "Fabric"; }
        @Override public Path configDirectory() { return FabricLoader.getInstance().getConfigDir(); }
    }
}
