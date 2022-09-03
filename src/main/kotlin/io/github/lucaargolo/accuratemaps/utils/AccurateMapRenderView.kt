package io.github.lucaargolo.accuratemaps.utils

import it.unimi.dsi.fastutil.longs.Long2IntArrayMap
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.entity.BlockEntity
import net.minecraft.fluid.FluidState
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.registry.Registry
import net.minecraft.world.BlockRenderView
import net.minecraft.world.World
import net.minecraft.world.biome.BiomeKeys
import net.minecraft.world.chunk.ChunkProvider
import net.minecraft.world.chunk.light.LightingProvider
import net.minecraft.world.level.ColorResolver

class AccurateMapRenderView(private val accurateMapState: AccurateMapState, val world: World): BlockRenderView {

    private val lightningProvider = LightingProvider(AccurateMapRenderViewChunkProvider(), true, true)
    private val colorCache = mutableMapOf<BlockPos, Int>()

    override fun getHeight() = world.height

    override fun getBottomY() = world.bottomY

    override fun getBlockEntity(pos: BlockPos?): BlockEntity? = null

    override fun getBlockState(pos: BlockPos): BlockState {
        val index = accurateMapState.positions.indexOf(pos.asLong())
        return if(index == -1) Blocks.AIR.defaultState else accurateMapState.statesPalette[accurateMapState.states[index]]
    }

    override fun getFluidState(pos: BlockPos): FluidState = getBlockState(pos).fluidState

    override fun getBrightness(direction: Direction?, shaded: Boolean) = 1f

    override fun getLightingProvider() = lightningProvider

    override fun getColor(pos: BlockPos, colorResolver: ColorResolver): Int {
        return colorCache.getOrPut(pos) {
            val index = accurateMapState.positions.indexOf(pos.asLong())
            val biome = world.registryManager.get(Registry.BIOME_KEY).get(if (index == -1) BiomeKeys.FOREST else accurateMapState.biomesPalette[accurateMapState.biomes[index]])
            colorResolver.getColor(biome, pos.x + 0.5, pos.z + 0.5)
        }
    }

    private inner class AccurateMapRenderViewChunkProvider: ChunkProvider {
        override fun getChunk(chunkX: Int, chunkZ: Int) = this@AccurateMapRenderView
        override fun getWorld() = this@AccurateMapRenderView
    }

}