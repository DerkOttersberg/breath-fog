package io.github.derkottersberg.breathfog.forge;

import io.github.derkottersberg.breathfog.BreathFog;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(BreathFog.ID)
public final class BreathFogForge {
    public BreathFogForge(FMLJavaModLoadingContext context) {
        if (FMLEnvironment.dist.isClient()) BreathFogForgeClient.initialize(context);
    }
}

