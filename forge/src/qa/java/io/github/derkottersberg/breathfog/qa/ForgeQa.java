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
                var info=net.minecraftforge.fml.ModList.getModContainerById("breath_fog").orElseThrow().getModInfo();
                return net.minecraftforge.client.ConfigScreenHandler.getScreenFactoryFor(info).orElseThrow().apply(Minecraft.getInstance(),parent);
            };
            TickEvent.ClientTickEvent.Post.BUS.addListener(event -> qa.tick(Minecraft.getInstance()));
            TickEvent.RenderTickEvent.Post.BUS.addListener(event -> qa.observeRender(Minecraft.getInstance()));
        }
    }
}
