package com.rendog.client.event.listener.events

import com.rendog.client.event.Event
import com.rendog.client.event.RendogEventBus
import com.rendog.client.event.SingletonEvent

object ShutdownEvent : Event, SingletonEvent(RendogEventBus)