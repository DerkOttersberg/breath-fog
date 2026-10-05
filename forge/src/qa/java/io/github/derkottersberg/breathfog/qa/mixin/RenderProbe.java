package io.github.derkottersberg.breathfog.qa.mixin;
import io.github.derkottersberg.breathfog.qa.QaClient;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Minecraft.class)
public abstract class RenderProbe {
    @Inject(method="renderFrame", at=@At("TAIL"))
    private void sample(boolean renderLevel, CallbackInfo ci) {
        if (QaClient.active!=null) {
            QaClient.active.observeRender((Minecraft)(Object)this);
            QaClient.active.tickFromRender((Minecraft)(Object)this);
        }
    }
}
