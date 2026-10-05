package io.github.derkottersberg.breathfog.qa;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
public final class FabricQa implements ClientModInitializer {
    public void onInitializeClient() {
        var qa = new QaClient(); qa.initialize();
        QaClient.nativeConfigFactory=parent -> {
            try {
                Class<?> menu=Class.forName("com.terraformersmc.modmenu.ModMenu");
                return (net.minecraft.client.gui.screens.Screen)menu.getMethod("getConfigScreen",String.class,net.minecraft.client.gui.screens.Screen.class).invoke(null,"breath_fog",parent);
            } catch (ReflectiveOperationException error) { throw new IllegalStateException(error); }
        };
        ClientTickEvents.END_CLIENT_TICK.register(qa::tick);
    }
}
