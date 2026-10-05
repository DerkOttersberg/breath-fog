package io.github.derkottersberg.breathfog.forge.mixin;

import io.github.derkottersberg.breathfog.client.BreathFogClient;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Forge 66's legacy tick events do not cover Minecraft's new in-world loop. */
@Mixin(Minecraft.class)
public abstract class ClientLoopMixin {
    @Inject(method="tick", at=@At("TAIL"))
    private void breathFog$afterGameTick(CallbackInfo ci) {
        BreathFogClient.instance().tick((Minecraft)(Object)this);
    }

    // Also handles menu requests and level changes between simulation ticks.
    @Inject(method="renderFrame", at=@At("TAIL"))
    private void breathFog$afterFrame(boolean renderLevel, CallbackInfo ci) {
        BreathFogClient.instance().tick((Minecraft)(Object)this);
    }
}
