package io.github.lucaargolo.accuratemaps.utils

import io.github.lucaargolo.accuratemaps.mixin.MapColorInvoker
import net.minecraft.block.BlockState
import net.minecraft.block.MapColor
import net.minecraft.util.math.BlockPos
import net.minecraft.util.registry.RegistryKey
import net.minecraft.world.biome.Biome

class AccurateMapColor : MapColor {

    val blockBiome: RegistryKey<Biome>
    val blockState: BlockState
    val blockPos: BlockPos

    private constructor(originalColor: MapColor, blockBiome: RegistryKey<Biome>, blockState: BlockState, blockPos: BlockPos): super(originalColor.id, originalColor.color) {
        this.blockBiome = blockBiome
        this.blockState = blockState
        this.blockPos = blockPos
    }

    constructor(color: Int, blockBiome: RegistryKey<Biome>, blockState: BlockState, blockPos: BlockPos): super(63, color) {
        this.blockBiome = blockBiome
        this.blockState = blockState
        this.blockPos = blockPos
    }

    companion object {
        fun MapColor.getAccurate(blockBiome: RegistryKey<Biome>, blockState: BlockState, blockPos: BlockPos): AccurateMapColor {
            val backup = MapColorInvoker.getColors()[this.id]
            val accurate = AccurateMapColor(this, blockBiome, blockState, blockPos)
            MapColorInvoker.getColors()[this.id] = backup
            return accurate
        }
    }

}