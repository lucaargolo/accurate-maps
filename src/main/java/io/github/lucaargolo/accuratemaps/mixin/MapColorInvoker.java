package io.github.lucaargolo.accuratemaps.mixin;

import net.minecraft.block.MapColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MapColor.class)
public interface MapColorInvoker {

    @Accessor("COLORS")
    static MapColor[] getColors() {
        throw new AssertionError();
    }

    @Invoker
    static MapColor invokeGetUnchecked(int id) {
        throw new AssertionError();
    }

    @Mixin(MapColor.Brightness.class)
    interface BrightnessInvoker {

        @Invoker
        static MapColor.Brightness invokeGet(int id) {
            throw new AssertionError();
        }

    }

}
