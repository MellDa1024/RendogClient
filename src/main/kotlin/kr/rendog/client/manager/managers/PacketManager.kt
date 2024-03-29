package kr.rendog.client.manager.managers

import kr.rendog.client.event.events.ConnectionEvent
import kr.rendog.client.event.events.PacketEvent
import kr.rendog.client.event.listener.listener
import kr.rendog.client.manager.Manager
import kr.rendog.client.util.threads.safeListener
import net.minecraft.network.Packet
import net.minecraft.network.play.server.SPacketPlayerPosLook
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.util.concurrent.ConcurrentLinkedDeque

object PacketManager : Manager {
    private const val maxAge = 1000L

    val recentReceived = ConcurrentLinkedDeque<Pair<Packet<*>,Long>>()
    var totalReceived = 0

    val recentSent = ConcurrentLinkedDeque<Pair<Packet<*>,Long>>()
    var totalSent = 0

    var lastTeleportId = -1

    init {
        listener<PacketEvent.PostSend> {
            recentSent.add(Pair(it.packet, System.currentTimeMillis()))
            totalSent++
        }

        listener<PacketEvent.PostReceive> {
            recentReceived.add(Pair(it.packet, System.currentTimeMillis()))
            totalReceived++

            when (it.packet) {
                is SPacketPlayerPosLook -> {
                    lastTeleportId = it.packet.teleportId
                }
            }
        }

        safeListener<ConnectionEvent> {
            lastTeleportId = -1
        }

        safeListener<TickEvent.ClientTickEvent> {
            val currentTime = System.currentTimeMillis()

            recentReceived.removeIf { currentTime - it.second > maxAge }
            recentSent.removeIf { currentTime - it.second > maxAge }
        }
    }
}