package io.github.lucaargolo.accuratemaps.mixin;

import com.google.common.collect.Multiset;
import io.github.lucaargolo.accuratemaps.utils.AccurateMapState;
import io.github.lucaargolo.accuratemaps.utils.AccurateMapState;
import net.minecraft.block.MapColor;
import net.minecraft.entity.Entity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(FilledMapItem.class)
public abstract class FilledMapItemMixin {

    @Unique
    AccurateMapState currentState;

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/item/FilledMapItem;updateColors(Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;Lnet/minecraft/item/map/MapState;)V"), method = "inventoryTick")
    public void captureAccurateMapState(ItemStack stack, World world, Entity entity, int slot, boolean selected, CallbackInfo ci) {
        Integer id = FilledMapItem.getMapId(stack);
        if(id != null && world instanceof ServerWorld serverWorld) {
            currentState = serverWorld.getServer().getOverworld().getPersistentStateManager().getOrCreate(AccurateMapState.Companion::createFromNbt, AccurateMapState::new, "accurate_map_"+id);
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/item/map/MapState;putColor(IIB)Z"), method = "updateColors", locals = LocalCapture.CAPTURE_FAILHARD)
    public void updateAccurateMapState(World world, Entity entity, MapState state, CallbackInfo ci, int i, int j, int k, int l, int m, int n, MapState.PlayerUpdateTracker playerUpdateTracker, BlockPos.Mutable pos1, BlockPos.Mutable pos2, boolean bl, int o, double d, int p, int r, boolean bl2, int s, int t, Multiset<MapColor> multiset, WorldChunk worldChunk, int u, double e, MapColor mapColor, MapColor.Brightness brightness) {
        currentState.setBlock(o, p, mapColor);
    }




}
