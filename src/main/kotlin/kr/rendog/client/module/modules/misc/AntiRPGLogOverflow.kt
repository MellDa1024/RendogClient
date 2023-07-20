package kr.rendog.client.module.modules.misc

import kr.rendog.client.event.events.PacketEvent
import kr.rendog.client.module.Module
import kr.rendog.client.module.Category
import kr.rendog.client.util.threads.safeListener
import net.minecraft.network.play.server.SPacketSetPassengers

/**
 * This is a module. First set properties then settings then add listener.
 * **/
internal object AntiRPGLogOverflow : Module(
    name = "AntiLogOverflow",
    category = Category.MISC,
    description = "Cancels unknown packet to stop log file to overflow",
) {
    init {
        safeListener<PacketEvent.Receive>(1557) { event ->
            if (event.packet is SPacketSetPassengers) {
                val clonedList = ArrayList(mc.world.loadedEntityList)
                var isavailable = false
                for (entity in clonedList) {
                    if (entity.entityId == event.packet.entityId) {
                        isavailable = true
                        break
                    }
                }
                if (!isavailable) event.cancel()
            }
        }
    }
}