package io.github.derkottersberg.breathfog.neoforge;

import io.github.derkottersberg.breathfog.BreathFog;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.api.distmarker.Dist;

@Mod(value = BreathFog.ID, dist = Dist.CLIENT)
public final class BreathFogNeoForge {
    public BreathFogNeoForge(ModContainer container) {
        BreathFogNeoForgeClient.initialize(container);
    }
}

