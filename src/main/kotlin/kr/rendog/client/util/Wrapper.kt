package kr.rendog.client.util

import kr.rendog.client.RendogMod
import kr.rendog.client.event.events.ShutdownEvent
import kr.rendog.client.util.ConfigUtils.saveAll
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.multiplayer.WorldClient

object Wrapper {
    @JvmStatic
    val minecraft: Minecraft
        get() = Minecraft.getMinecraft()

    @JvmStatic
    val player: EntityPlayerSP?
        get() = minecraft.player

    @JvmStatic
    val world: WorldClient?
        get() = minecraft.world

    @JvmStatic
    fun saveAndShutdown() {
        if (!RendogMod.ready) return

        ShutdownEvent.post()
        saveAll()
    }
}