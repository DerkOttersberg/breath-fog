package io.github.derkottersberg.breathfog.neoforge;

import io.github.derkottersberg.breathfog.BreathFog;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(BreathFog.ID)
public final class BreathFogNeoForge {
    public BreathFogNeoForge(ModContainer container) {
        if (FMLEnvironment.dist.isClient()) BreathFogNeoForgeClient.initialize(container);
    }
}
