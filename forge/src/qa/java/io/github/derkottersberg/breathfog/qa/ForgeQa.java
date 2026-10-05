package io.github.derkottersberg.breathfog.qa;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.event.TickEvent;
import net.minecraft.client.Minecraft;
@Mod("breath_fog_qa")
public final class ForgeQa {
    public ForgeQa() {
        if (FMLEnvironment.dist.isClient()) {
            var qa = new QaClient(); qa.initialize();
            QaClient.nativeConfigFactory=parent -> {
                var info=net.minecraftforge.fml.ModList.get().getModContainerById("breath_fog").orElseThrow().getModInfo();
                return net.minecraftforge.client.ConfigScreenHandler.getScreenFactoryFor(info).orElseThrow().apply(Minecraft.getInstance(),parent);
            };
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener((TickEvent.ClientTickEvent event) -> { if (event.phase==TickEvent.Phase.END) qa.tick(Minecraft.getInstance()); });
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener((TickEvent.RenderTickEvent event) -> { if (event.phase==TickEvent.Phase.END) qa.observeRender(Minecraft.getInstance()); });
        }
    }
}
