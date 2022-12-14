package io.github.lucaargolo.accuratemaps.utils

import io.github.lucaargolo.accuratemaps.utils.AccurateMapColor.Companion.getAccurate
import it.unimi.dsi.fastutil.longs.Long2IntArrayMap
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.MapColor
import net.minecraft.nbt.*
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.RegistryKey
import net.minecraft.world.PersistentState
import net.minecraft.world.biome.Biome
import net.minecraft.world.biome.BiomeKeys

class AccurateMapState(startLists: Boolean = true): PersistentState() {

    val biomesPalette = mutableListOf<RegistryKey<Biome>>().also {
        if(startLists) it.add(BiomeKeys.FOREST)
    }
    val statesPalette = mutableListOf<BlockState>().also {
        if(startLists) it.add(Blocks.AIR.defaultState)
    }

    var biomes = IntArray(16384)
    var states = IntArray(16384)
    var positions = LongArray(16384)

    fun setBlock(x: Int, z: Int, color: MapColor) {
        val accurateColor = color as? AccurateMapColor ?: color.getAccurate(BiomeKeys.FOREST, Blocks.AIR.defaultState, BlockPos.ORIGIN)
        val blockBiome = accurateColor.blockBiome
        var biomePaletteId = biomesPalette.indexOf(blockBiome)
        if(biomePaletteId == -1) {
            biomePaletteId = biomesPalette.size
            biomesPalette.add(blockBiome)
        }
        val blockState = accurateColor.blockState
        var statePaletteId = statesPalette.indexOf(blockState)
        if(statePaletteId == -1) {
            statePaletteId = statesPalette.size
            statesPalette.add(blockState)
        }
        biomes[x + z * 128] = biomePaletteId
        states[x + z * 128] = statePaletteId
        positions[x + z * 128] = accurateColor.blockPos.asLong()
        markDirty()
    }

    fun writeClientNbt(nbt: NbtCompound): NbtCompound {
        nbt.put("biomesPalette", NbtList().also { list ->
            biomesPalette.forEach { biomeKey ->
                list.add(NbtString.of(biomeKey.value.toString()))
            }
        })
        nbt.put("statesPalette", NbtIntArray(
            statesPalette.map { state ->
                Block.getRawIdFromState(state)
            }.toIntArray()
        ))
        nbt.put("biomes", NbtIntArray(biomes))
        nbt.put("states", NbtIntArray(states))
        nbt.put("positions", NbtLongArray(positions))
        return nbt
    }

    fun readClientNbt(nbt: NbtCompound) {
        (nbt.get("biomesPalette") as? NbtList)?.forEachIndexed { index, element ->
            biomesPalette.add(index, RegistryKey.of(RegistryKeys.BIOME, Identifier(element.asString())))
        }
        nbt.getIntArray("statesPalette").forEachIndexed { index, id ->
            statesPalette.add(index, Block.getStateFromRawId(id))
        }
        nbt.getIntArray("biomes").also { array ->
            if(array.size == 16384) {
                biomes = array
            }
        }
        nbt.getIntArray("states").also { array ->
            if(array.size == 16384) {
                states = array
            }
        }
        nbt.getLongArray("positions").also { array ->
            if(array.size == 16384) {
                positions = array
            }
        }
    }

    override fun writeNbt(nbt: NbtCompound): NbtCompound {
        nbt.put("biomesPalette", NbtList().also { list ->
            biomesPalette.forEach { biomeKey ->
                list.add(NbtString.of(biomeKey.value.toString()))
            }
        })
        nbt.put("statesPalette", NbtList().also { list ->
            statesPalette.forEach { state ->
                val encodedBlockState = BlockState.CODEC.encode(state, NbtOps.INSTANCE, NbtOps.INSTANCE.empty()).getOrThrow(true) {}
                list.add(encodedBlockState)
            }
        })
        nbt.put("biomes", NbtIntArray(biomes))
        nbt.put("states", NbtIntArray(states))
        nbt.put("positions", NbtLongArray(positions))
        return nbt
    }

    companion object {

        fun createFromNbt(nbt: NbtCompound): AccurateMapState {
            return AccurateMapState(false).also {
                (nbt.get("biomesPalette") as? NbtList)?.forEachIndexed { index, element ->
                    it.biomesPalette.add(index, RegistryKey.of(RegistryKeys.BIOME, Identifier(element.asString())))
                }
                (nbt.get("statesPalette") as? NbtList)?.forEachIndexed { index, element ->
                    it.statesPalette.add(index, BlockState.CODEC.decode(NbtOps.INSTANCE, element).getOrThrow(true) {}.first)
                }
                nbt.getIntArray("biomes").also { array ->
                    if(array.size == 16384) {
                        it.biomes = array
                    }
                }
                nbt.getIntArray("states").also { array ->
                    if(array.size == 16384) {
                        it.states = array
                    }
                }
                nbt.getLongArray("positions").also { array ->
                    if(array.size == 16384) {
                        it.positions = array
                    }
                }
            }
        }

    }


}