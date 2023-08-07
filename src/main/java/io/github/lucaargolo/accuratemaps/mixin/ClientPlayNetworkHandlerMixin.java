package io.github.lucaargolo.accuratemaps.mixin;

import io.github.lucaargolo.accuratemaps.client.AccurateMapsClient;
import io.github.lucaargolo.accuratemaps.client.AccurateMapsClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.MapUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {

    @Inject(at = @At("HEAD"), method = "onMapUpdate")
    public void onMethodUpdate(MapUpdateS2CPacket packet, CallbackInfo ci) {
        AccurateMapsClient.INSTANCE.onMapUpdate(packet);
    }


}
