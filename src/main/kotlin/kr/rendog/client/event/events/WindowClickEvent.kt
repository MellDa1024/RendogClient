package kr.rendog.client.event.events

import kr.rendog.client.event.Cancellable
import kr.rendog.client.event.Event
import kr.rendog.client.event.ICancellable
import net.minecraft.inventory.ClickType

class WindowClickEvent(val windowId: Int, val slotId: Int, val mouseButton: Int, val type: ClickType) : Event, ICancellable by Cancellable()