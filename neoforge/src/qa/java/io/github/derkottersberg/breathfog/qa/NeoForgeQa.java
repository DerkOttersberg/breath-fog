package io.github.derkottersberg.breathfog.qa;
import net.neoforged.fml.common.Mod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.minecraft.client.Minecraft;
@Mod(value="breath_fog_qa", dist=Dist.CLIENT)
public final class NeoForgeQa {
    public NeoForgeQa() {
        var qa = new QaClient(); qa.initialize(net.neoforged.fml.loading.FMLPaths.GAMEDIR.get());
        QaClient.nativeConfigFactory=parent -> {
            var container=net.neoforged.fml.ModList.get().getModContainerById("breath_fog").orElseThrow();
            return container.getCustomExtension(net.neoforged.neoforge.client.gui.IConfigScreenFactory.class).orElseThrow().createScreen(container,parent);
        };
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> qa.tick(Minecraft.getInstance()));
    }
}
