package kr.rendog.client.manager.managers

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kr.rendog.client.RendogMod
import kr.rendog.client.event.SafeClientEvent
import kr.rendog.client.manager.Manager
import kr.rendog.client.util.threads.defaultScope
import kr.rendog.client.util.threads.safeListener
import net.minecraft.network.play.server.SPacketDisconnect
import net.minecraft.util.text.TextComponentString
import net.minecraftforge.fml.common.gameevent.TickEvent

object RendogSVCheckManager : Manager {
    private val rendogSVIP = arrayOf("rendog.kr", "global.rendog.kr", "private-scn-sev99.scn.pw:1799")
    private var logFired = false

    init {
        safeListener<TickEvent.ClientTickEvent> {
            if (it.phase != TickEvent.Phase.END) return@safeListener
            if (logFired) return@safeListener
            if (mc.isSingleplayer) logDelay("RendogClient is only available in RendogServer.")
            val ip = mc.currentServerData?.serverIP ?: return@safeListener
            if (ip.lowercase() !in rendogSVIP) {
                logDelay("RendogClient is only available in RendogServer.")
            }
        }

    }
    private fun SafeClientEvent.logDelay(msg : String) {
        defaultScope.launch {
            logFired = true
            RendogMod.LOG.info("Player is not in RendogServer! Will disconnect in 1 second, it may crash in SinglePlayer...")
            delay(1000L)
            connection.handleDisconnect(SPacketDisconnect(TextComponentString(msg)))
            logFired = false
        }
    }
}