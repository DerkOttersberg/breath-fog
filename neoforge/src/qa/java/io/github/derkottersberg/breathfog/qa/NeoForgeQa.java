package io.github.derkottersberg.breathfog.qa;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.minecraft.client.Minecraft;
@Mod("breath_fog_qa")
public final class NeoForgeQa {
    public NeoForgeQa() {
        if (!FMLEnvironment.dist.isClient()) return;
        var qa = new QaClient(); qa.initialize(net.neoforged.fml.loading.FMLPaths.GAMEDIR.get());
        QaClient.nativeConfigFactory=parent -> {
            var container=net.neoforged.fml.ModList.get().getModContainerById("breath_fog").orElseThrow();
            return container.getCustomExtension(net.neoforged.neoforge.client.gui.IConfigScreenFactory.class).orElseThrow().createScreen(container,parent);
        };
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> qa.tick(Minecraft.getInstance()));
    }
}
