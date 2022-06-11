package com.rendog.client.util

import com.rendog.client.RendogMod
import com.rendog.client.event.listener.events.ShutdownEvent
import com.rendog.client.util.ConfigUtils.saveAll
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