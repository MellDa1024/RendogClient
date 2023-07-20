package kr.rendog.client.module.modules.misc

import com.jagrosh.discordipc.IPCClient
import com.jagrosh.discordipc.entities.RichPresence
import com.jagrosh.discordipc.entities.pipe.PipeStatus
import com.jagrosh.discordipc.exceptions.NoDiscordClientException
import kr.rendog.client.RendogMod
import kr.rendog.client.module.Category
import kr.rendog.client.module.Module
import kr.rendog.client.util.InfoCalculator.speed
import kr.rendog.client.util.combat.HealthUtils.getRendogCurrentHealth
import kr.rendog.client.util.text.Color.deColorize
import kr.rendog.client.util.text.Color.removeUntradeableMark
import kr.rendog.client.util.text.MessageSendHelper
import kr.rendog.client.util.threads.BackgroundJob
import kr.rendog.client.util.threads.BackgroundScope
import kr.rendog.client.util.threads.runSafeR
import net.minecraft.client.Minecraft
import java.time.OffsetDateTime
import kotlin.math.roundToInt

object DiscordRPC : Module(
    name = "DiscordRPC",
    description = "Discord Rich Presence",
    category = Category.MISC
) {
    private val line2 by setting("Setting", LineInfo.USERNAME) // details right
    private val noIgn by setting("No Ign Text", false, { line2 == LineInfo.USERNAME })
    private val noHolding by setting("No Holding Text", false, { line2 == LineInfo.HELD_ITEM })
    private val noUntradeable by setting("Remove Untradeable", false, { line2 == LineInfo.HELD_ITEM })

    private enum class LineInfo {
        VERSION, USERNAME, HELD_ITEM, HEALTH, SPEED, FPS
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
                    .setDetails("Using RendogClient")
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
            LineInfo.HELD_ITEM -> {
                val itemName = mc.player?.heldItemMainhand?.displayName
                if (itemName?.contains("§") == true) {
                    try {
                        val itemName2 = if (noUntradeable) itemName.deColorize().removeUntradeableMark().trim()
                        else itemName.deColorize().trim()
                        if (noHolding) itemName2
                        else "Holding $itemName2"
                    }
                    catch (e : Exception) { "" }
                }
                else "Holding Nothing"
            }
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
