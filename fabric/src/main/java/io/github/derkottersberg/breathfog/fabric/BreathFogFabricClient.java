package io.github.derkottersberg.breathfog.fabric;

import io.github.derkottersberg.breathfog.client.BreathFogClient;
import io.github.derkottersberg.breathfog.BreathFog;
import io.github.derkottersberg.breathfog.client.BreathFogConfigScreen;
import io.github.derkottersberg.breathfog.platform.ClientPlatformServices;
import java.nio.file.Path;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.reloader.SimpleReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;

public final class BreathFogFabricClient implements ClientModInitializer {
    @Override public void onInitializeClient() {
        BreathFogClient controller=BreathFogClient.instance();
        controller.initialize(new FabricServices());
        ClientTickEvents.END_CLIENT_TICK.register(controller::tick);
        ClientPlayConnectionEvents.DISCONNECT.register((handler,client)->controller.clear());
        ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloadListener(BreathFog.id("vapor_sprites"),new SimpleReloadListener<Boolean>() {
            @Override protected Boolean prepare(PreparableReloadListener.SharedState state) { return true; }
            @Override protected void apply(Boolean prepared, PreparableReloadListener.SharedState state) { controller.resourcesReloaded(); }
        });
        ClientCommandRegistrationCallback.EVENT.register((dispatcher,registryAccess)->dispatcher.register(
            ClientCommands.literal("breathfog")
                .then(ClientCommands.literal("config").executes(context->{
                    var client=context.getSource().getClient();
                    // Chat closes its screen after command execution; enqueue opening for afterward.
                    client.schedule(()->client.setScreenAndShow(new BreathFogConfigScreen(null)));
                    return 1;
                }))
                .then(ClientCommands.literal("preview").executes(context->{
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
