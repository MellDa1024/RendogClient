package com.rendog.client.module.modules.misc

import com.jagrosh.discordipc.IPCClient
import com.jagrosh.discordipc.entities.RichPresence
import com.jagrosh.discordipc.entities.pipe.PipeStatus
import com.jagrosh.discordipc.exceptions.NoDiscordClientException
import com.rendog.client.RendogMod
import com.rendog.client.module.Category
import com.rendog.client.module.Module
import com.rendog.client.util.InfoCalculator.speed
import com.rendog.client.util.combat.HealthUtils.getRendogCurrentHealth
import com.rendog.client.util.text.MessageSendHelper
import com.rendog.client.util.threads.BackgroundJob
import com.rendog.client.util.threads.BackgroundScope
import com.rendog.client.util.threads.runSafeR
import net.minecraft.client.Minecraft
import java.time.OffsetDateTime
import kotlin.math.roundToInt

object DiscordRPC : Module(
    name = "DiscordRPC",
    description = "Discord Rich Presence",
    category = Category.MISC
) {
    private val line2 by setting("Setting", LineInfo.USERNAME) // details right
    private val noIgn by setting("No Ign Mark", false, { line2 == LineInfo.USERNAME })

    private enum class LineInfo {
        VERSION, USERNAME, HEALTH, SPEED, FPS
    }

    // Not using "by lazy" to be able to catch failure in onEnable
    private lateinit var ipc: IPCClient
    private var initialised = false
    private val rpcBuilder = RichPresence.Builder()
        .setLargeImage("default", "Rendog.KR")
        .setSmallImage("minecraft_logo", "https://github.com/MellDa1024/RendogClient/")
    private val job = BackgroundJob("Discord RPC", 5000L) { updateRPC() }

    init {
        onEnable {
            if (!initialised) {
                try {
                    ipc = IPCClient(RendogMod.APP_ID)
                    initialised = true
                } catch (e: UnsatisfiedLinkError) {
                    error("Failed to initialise DiscordRPC due to missing native library", e)
                    disable()
                    return@onEnable
                }
            }
            start()
        }

        onDisable {
            end()
        }
    }

    private fun start() {
        RendogMod.LOG.info("Starting Discord RPC")
        try {
            ipc.connect()
            rpcBuilder.setStartTimestamp(OffsetDateTime.now())
            val richPresence = rpcBuilder.build()
            ipc.sendRichPresence(richPresence)
            BackgroundScope.launchLooping(job)

            RendogMod.LOG.info("Discord RPC initialised successfully")
        } catch (e: NoDiscordClientException) {
            error("No discord client found for RPC, stopping")
            disable()
        }
    }

    private fun end() {
        RendogMod.LOG.info("Shutting down Discord RPC...")
        BackgroundScope.cancel(job)
        if (initialised && ipc.status == PipeStatus.CONNECTED) {
            ipc.close()
        }
    }

    private fun updateRPC() {
        when (ipc.status) {
            PipeStatus.CONNECTED -> {
                val richPresence = rpcBuilder
                    .setDetails("Playing RendogServer")
                    .setState(getLine(line2))
                    .build()
                ipc.sendRichPresence(richPresence)
            }

            PipeStatus.UNINITIALIZED -> {
                tryConnect()
            }

            PipeStatus.DISCONNECTED -> {
                tryConnect()
            }

            else -> {
                // Why is this necessary now kotlin? WHY
            }
        }
    }

    private fun tryConnect() {
        try {
            ipc.connect()
        } catch (e: NoDiscordClientException) {
            // Add something here if you want to spam the log i guess
        }
    }

    private fun getLine(line: LineInfo): String {
        return when (line) {
            LineInfo.VERSION -> {
                "Client Version : ${RendogMod.VERSION}"
            }
            LineInfo.USERNAME -> {
                if (noIgn) "${mc.session.username}"
                else "IGN : ${mc.session.username}"
            }
            LineInfo.HEALTH -> {
                if (mc.player != null) "${getRendogCurrentHealth(mc.player).roundToInt()} HP"
                else "No HP"
            }
            LineInfo.SPEED -> {
                runSafeR {
                    "Speed : ${"%.1f".format(speed())} m/s"
                } ?: "No Speed"
            }
           /* LineInfo.HELD_ITEM -> {
                if (mc.player?.heldItemMainhand?.displayName?.contains("§") == true) {
                    try{ "Using ${mc.player?.heldItemMainhand?.displayName?.removeColorCode()}" }
                    catch (e : Exception) {
                        ""
                    }
                }
                else "Using Nothing"
            }*/ //Not now, troubleshooting unicode problem
            LineInfo.FPS -> {
                "${Minecraft.getDebugFPS()} FPS"
            }
        }
    }
/*
    private fun getSeparator(line: Int): String {
        return if (line == 0) {
            if (line1Right == LineInfo.NONE) " " else " | "
        } else {
            if (line2Left == LineInfo.NONE || line2Right == LineInfo.NONE) " " else " | "
        }
    }*/

    // Change to Throwable? if more logging is ever needed
    private fun error(message: String, error: UnsatisfiedLinkError? = null) {
        MessageSendHelper.sendErrorMessage(message)
        RendogMod.LOG.error(message, error)
    }
}
