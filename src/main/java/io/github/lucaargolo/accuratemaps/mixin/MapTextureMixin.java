package io.github.lucaargolo.accuratemaps.mixin;

import io.github.lucaargolo.accuratemaps.client.AccurateMapsClient;
import io.github.lucaargolo.accuratemaps.utils.AccurateMapState;
import io.github.lucaargolo.accuratemaps.utils.AccurateMapState;
import net.minecraft.client.render.MapRenderer;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.item.map.MapState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MapRenderer.MapTexture.class)
public class MapTextureMixin {

    @Shadow private MapState state;
    @Shadow @Final private NativeImageBackedTexture texture;
    private int id;

    @Inject(at = @At("TAIL"), method = "<init>")
    public void afterInit(MapRenderer mapRenderer, int id, MapState state, CallbackInfo ci) {
        this.id = id;
    }

    @Inject(at = @At("HEAD"), method = "updateTexture", cancellable = true)
    public void updateAccurateTexture(CallbackInfo ci) {
        AccurateMapState accurateMapState = AccurateMapsClient.INSTANCE.getAccurateMapStates().get(id);
        if(accurateMapState != null) {
            AccurateMapsClient.INSTANCE.updateAccurateTexture(id, accurateMapState, state, texture);
            ci.cancel();
        }
    }


}
