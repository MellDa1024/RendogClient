package kr.rendog.client.manager.managers

import kr.rendog.client.event.SafeClientEvent
import kr.rendog.client.manager.Manager
import kr.rendog.client.util.threads.safeListener
import net.minecraft.network.play.server.SPacketDisconnect
import net.minecraft.util.text.TextComponentString
import net.minecraftforge.fml.common.gameevent.TickEvent

object RendogSVCheckManager : Manager {
    private val rendogSvIP = arrayOf("rendog.kr", "global.rendog.kr", "private-scn-sev99.scn.pw:1799")

    init {
        safeListener<TickEvent.ClientTickEvent> {
            val ip = mc.currentServerData?.serverIP ?: return@safeListener
            if (ip.lowercase() !in rendogSvIP) {
                log("RendogClient is only available in RendogServer.")
            }
        }

    }
    private fun SafeClientEvent.log(msg : String) {
        connection.handleDisconnect(SPacketDisconnect(TextComponentString(msg)))
    }
}