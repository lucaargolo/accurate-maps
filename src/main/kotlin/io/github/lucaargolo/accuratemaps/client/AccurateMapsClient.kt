package io.github.lucaargolo.accuratemaps.client

import com.mojang.blaze3d.systems.RenderSystem
import io.github.lucaargolo.accuratemaps.AccurateMaps
import io.github.lucaargolo.accuratemaps.mixin.MapColorInvoker
import io.github.lucaargolo.accuratemaps.utils.AccurateMapColor
import io.github.lucaargolo.accuratemaps.utils.AccurateMapRenderView
import io.github.lucaargolo.accuratemaps.utils.AccurateMapState
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.client.MinecraftClient
import net.minecraft.client.texture.NativeImageBackedTexture
import net.minecraft.item.map.MapState
import net.minecraft.network.packet.s2c.play.MapUpdateS2CPacket
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.random.Random
import net.minecraft.registry.Registries
import net.minecraft.world.biome.BiomeKeys
import org.lwjgl.opengl.GL11
import java.lang.Float.min
import java.nio.ByteBuffer
import kotlin.math.roundToInt

object AccurateMapsClient: ClientModInitializer {

    val accurateMapStates = linkedMapOf<Int, AccurateMapState>()

    private val renderViewsCache = linkedMapOf<Int, AccurateMapRenderView>()
    private val blockColorMap = linkedMapOf<BlockState, Int>()

    fun paintBlockColorMap(client: MinecraftClient) {
        val atlasId = client.bakedModelManager.blockModels.getModel(Blocks.STONE.defaultState).particleSprite.atlasId
        val atlas = client.getTextureManager().getTexture(atlasId)
        RenderSystem.bindTexture(atlas.glId)
        val width = intArrayOf(0)
        GL11.glGetTexLevelParameteriv(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH, width)
        val height = intArrayOf(0)
        GL11.glGetTexLevelParameteriv(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT, height)
        val pixels = ByteArray(width[0] * height[0] * 4)
        val buffer: ByteBuffer = ByteBuffer.allocateDirect(pixels.size)
        GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer)
        buffer.get(pixels)
        Registries.BLOCK.forEach { block ->
            block.stateManager.states.forEach { state ->
                val blockModel = client.bakedModelManager.blockModels.getModel(state)
                val blockSprite = blockModel.getQuads(state, Direction.UP, Random.create()).firstOrNull()?.sprite ?: blockModel.particleSprite
                var sumIndex = 0
                var sumR = 0
                var sumG = 0
                var sumB = 0
                val minU = (blockSprite.minU*width[0]).toInt()
                val maxU = (blockSprite.maxU*width[0]).toInt()
                val minV = (blockSprite.minV*height[0]).toInt()
                val maxV = (blockSprite.maxV*height[0]).toInt()
                (minU until maxU).forEach { pixelX ->
                    (minV until maxV).forEach { pixelY ->
                        val index = (pixelX + pixelY * width[0]) * 4
                        if(pixels[index + 3].toInt() and 0xFF > 0) {
                            sumIndex++
                            sumR += pixels[index].toInt() and 0xFF
                            sumG += pixels[index + 1].toInt() and 0xFF
                            sumB += pixels[index + 2].toInt() and 0xFF
                        }
                    }
                }
                if(sumIndex > 0) {
                    blockColorMap[state] = (sumB / sumIndex) + ((sumG / sumIndex) shl 8) + ((sumR / sumIndex) shl 16)
                }
            }
        }
        renderViewsCache.clear()
    }

    fun onMapUpdate(packet: MapUpdateS2CPacket) {
        if(ClientPlayNetworking.canSend(AccurateMaps.REQUEST_ACCURATE_MAP)) {
            val buf = PacketByteBufs.create()
            buf.writeInt(packet.id)
            ClientPlayNetworking.send(AccurateMaps.REQUEST_ACCURATE_MAP, buf)
        }
    }

    fun updateAccurateTexture(id: Int, accurateState: AccurateMapState, state: MapState, texture: NativeImageBackedTexture) {
        val client = MinecraftClient.getInstance()

        val world = renderViewsCache.getOrPut(id) {
            client.world?.let { AccurateMapRenderView(accurateState, it) } ?: return
        }.let { renderView ->
            if(renderView.world != client.world) {
                renderViewsCache[id] = client.world?.let { AccurateMapRenderView(accurateState, it) } ?: return
            }
            renderViewsCache[id] ?: return
        }

        for (y in 0..127) {
            for (x in 0..127) {
                val index = x + y * 128

                val color = state.colors[index].toInt() and 255
                val originalColor = MapColorInvoker.invokeGetUnchecked(color shr 2)
                val brightness = MapColorInvoker.BrightnessInvoker.invokeGet(color and 3)

                val blockBiome = accurateState.biomesPalette.getOrNull(accurateState.biomes[index]) ?: BiomeKeys.FOREST
                val blockState = accurateState.statesPalette.getOrNull(accurateState.states[index]) ?: Blocks.AIR.defaultState
                val blockPos = BlockPos.fromLong(accurateState.positions[index])

                if(!blockState.isAir) {
                    val tintColor =  ColorProviderRegistry.BLOCK.get(blockState.block)?.getColor(blockState, world, blockPos, 0) ?: 0xFFFFFF
                    val savedColor = blockColorMap.getOrDefault(blockState, 0)
                    val lightFactor = 1.2f
                    val red = min(255f, ((((tintColor shr 16 and 255) * (savedColor shr 16 and 255)) / 255f) * lightFactor)).roundToInt()
                    val green = min(255f, ((((tintColor shr 8 and 255) * (savedColor shr 8 and 255)) / 255f) * lightFactor)).roundToInt()
                    val blue = min(255f, ((((tintColor and 255) * (savedColor and 255)) / 255f) * lightFactor)).roundToInt()
                    val multipliedColor = (red shl 16) + (green shl 8) + blue
                    val accurateColor = AccurateMapColor(multipliedColor, blockBiome, blockState, blockPos)
                    texture.image?.setColor(x, y, accurateColor.getRenderColor(brightness))
                }else{
                    texture.image?.setColor(x, y, originalColor.getRenderColor(brightness))
                }
            }
        }

        texture.upload()
    }

    override fun onInitializeClient() {
        ClientPlayConnectionEvents.JOIN.register { _, _, client ->
            if(blockColorMap.isEmpty()) {
                paintBlockColorMap(client)
            }
            accurateMapStates.clear()
        }
        ClientPlayNetworking.registerGlobalReceiver(AccurateMaps.RECEIVE_ACCURATE_MAP) { client, _, buf, _ ->
            val id = buf.readInt()
            val nbt = buf.readNbt() ?: return@registerGlobalReceiver
            client.execute {
                accurateMapStates[id] = AccurateMapState(false).also {
                    it.readClientNbt(nbt)
                }
            }
        }
    }


}