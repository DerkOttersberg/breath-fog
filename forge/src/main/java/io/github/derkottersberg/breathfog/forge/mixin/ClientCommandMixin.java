package io.github.derkottersberg.breathfog.forge.mixin;
import io.github.derkottersberg.breathfog.client.BreathFogCommands;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ClientPacketListener.class)
public abstract class ClientCommandMixin {
    @Inject(method="sendCommand", at=@At("HEAD"), cancellable=true)
    private void breathFog$localCommand(String command, CallbackInfo ci) {
        if (BreathFogCommands.handleIfOwned(command)) ci.cancel();
    }
}
