package io.github.lucaargolo.accuratemaps

import io.github.lucaargolo.accuratemaps.utils.AccurateMapState
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.Identifier

object AccurateMaps: ModInitializer {

    const val MOD_ID = "accuratemaps"
    val REQUEST_ACCURATE_MAP = Identifier(MOD_ID, "request_accurate_map")
    val RECEIVE_ACCURATE_MAP = Identifier(MOD_ID, "receive_accurate_map")

    override fun onInitialize() {
        ServerPlayNetworking.registerGlobalReceiver(REQUEST_ACCURATE_MAP) { server, player, _, outerBuf, _ ->
            val id = outerBuf.readInt()
            server.execute {
                val accurateMapState = player.server.overworld.persistentStateManager.getOrCreate(AccurateMapState::createFromNbt, ::AccurateMapState, "accurate_map_$id")
                val nbt = accurateMapState.writeClientNbt(NbtCompound())
                val innerBuf = PacketByteBufs.create()
                innerBuf.writeInt(id)
                innerBuf.writeNbt(nbt)
                ServerPlayNetworking.send(player, RECEIVE_ACCURATE_MAP, innerBuf)
            }
        }
    }

}