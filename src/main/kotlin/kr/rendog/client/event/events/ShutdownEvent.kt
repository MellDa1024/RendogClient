package kr.rendog.client.event.events

import kr.rendog.client.event.Event
import kr.rendog.client.event.RendogEventBus
import kr.rendog.client.event.SingletonEvent

object ShutdownEvent : Event, SingletonEvent(RendogEventBus)