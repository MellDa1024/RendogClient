package kr.rendog.client.event.events

import kr.rendog.client.event.Cancellable
import kr.rendog.client.event.Event
import net.minecraft.entity.Entity

class PlayerAttackEvent(val entity: Entity) : Event, Cancellable()