package io.github.lucaargolo.accuratemaps.mixin;

import io.github.lucaargolo.accuratemaps.utils.AccurateMapColor;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.MapColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.AbstractBlockState.class)
public class AbstractBlockStateMixin {

    @SuppressWarnings("ConstantConditions")
    @Inject(at = @At("RETURN"), method = "getMapColor", cancellable = true)
    public void injectBlockInfo(BlockView world, BlockPos pos, CallbackInfoReturnable<MapColor> cir) {
        MapColor originalColor = cir.getReturnValue();
        if((Object) this instanceof BlockState blockState && world instanceof WorldView worldView) {
            worldView.getBiome(pos).getKey().ifPresent(blockBiome -> cir.setReturnValue(AccurateMapColor.Companion.getAccurate(originalColor, blockBiome, blockState, pos)));
        }
    }


}
