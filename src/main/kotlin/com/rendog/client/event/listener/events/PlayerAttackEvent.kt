package com.rendog.client.event.listener.events

import com.rendog.client.event.Cancellable
import com.rendog.client.event.Event
import net.minecraft.entity.Entity

class PlayerAttackEvent(val entity: Entity) : Event, Cancellable()