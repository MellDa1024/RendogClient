package com.rendog.client.manager.managers

import com.rendog.client.event.listener.events.PacketEvent
import com.rendog.client.manager.Manager
import com.rendog.client.mixin.extension.currentPlayerItem
import com.rendog.client.module.AbstractModule
import com.rendog.client.util.TickTimer
import com.rendog.client.util.TimeoutFlag
import com.rendog.client.util.items.HotbarSlot
import com.rendog.client.util.threads.runSafe
import com.rendog.client.commons.extension.firstEntryOrNull
import com.rendog.client.commons.extension.firstKeyOrNull
import com.rendog.client.commons.extension.firstValue
import com.rendog.client.commons.extension.synchronized
import com.rendog.client.event.listener.listener
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.CPacketHeldItemChange
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.util.*

object HotbarManager : Manager {
    // <Module, <Slot, <Reset Time>
    private val spoofingModule = TreeMap<AbstractModule, TimeoutFlag<Int>>(compareByDescending { it.modulePriority }).synchronized()
    private val timer = TickTimer()

    var serverSideHotbar = 0; private set
    var swapTime = 0L; private set

    val EntityPlayerSP.serverSideItem: ItemStack
        get() = inventory.mainInventory[serverSideHotbar]

    init {
        listener<PacketEvent.Send>(-69420) {
            if (it.packet is CPacketHeldItemChange && spoofingModule.isNotEmpty() && it.packet.slotId != serverSideHotbar) {
                it.cancel()
            }
        }

        listener<PacketEvent.PostSend>(-420) {
            if (it.cancelled || it.packet !is CPacketHeldItemChange) return@listener

            if (it.packet.slotId != serverSideHotbar) {
                serverSideHotbar = it.packet.slotId
                swapTime = System.currentTimeMillis()
            }
        }

        listener<TickEvent.ClientTickEvent> {
            if (it.phase != TickEvent.Phase.START || !timer.tick(250)) return@listener

            val prevFirstKey = spoofingModule.firstKeyOrNull()
            trimMap()
            val newEntry = spoofingModule.firstEntryOrNull()

            if (spoofingModule.isEmpty()) {
                resetHotbarPacket()
            } else if (prevFirstKey != null && newEntry != null && prevFirstKey != newEntry.key) {
                sendHotbarPacket(newEntry.value.value)
            }
        }
    }


    fun AbstractModule.spoofHotbar(slot: HotbarSlot, timeout: Long = 250L) {
        spoofHotbar(slot.hotbarSlot, timeout)
    }

    fun AbstractModule.spoofHotbar(slot: Int, timeout: Long = 250L) {
        if (slot in 0..8) {
            spoofingModule[this] = TimeoutFlag.relative(slot, timeout)
            if (spoofingModule.firstKeyOrNull() == this) {
                sendHotbarPacket(slot)
            }
        }
    }

    fun AbstractModule.resetHotbar() {
        val prevFirstKey = spoofingModule.firstKeyOrNull()

        spoofingModule.remove(this)

        if (spoofingModule.isEmpty()) {
            resetHotbarPacket()
        } else if (prevFirstKey != null && prevFirstKey == this) {
            spoofingModule.firstEntryOrNull()?.let { (_, flag) ->
                sendHotbarPacket(flag.value)
            }
        }
    }

    private fun trimMap() {
        while (spoofingModule.isNotEmpty() && spoofingModule.firstValue().timeout()) {
            spoofingModule.pollFirstEntry()
        }
    }

    private fun sendHotbarPacket(slot: Int) {
        if (serverSideHotbar != slot) {
            runSafe {
                serverSideHotbar = slot
                swapTime = System.currentTimeMillis()
                mc.connection?.sendPacket(CPacketHeldItemChange(slot))
            }
        }
    }

    private fun resetHotbarPacket() {
        runSafe {
            sendHotbarPacket(playerController.currentPlayerItem)
        }
    }
}