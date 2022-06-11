package com.rendog.client.event.listener.events

import com.rendog.client.event.Event
import com.rendog.client.event.ProfilerEvent

class RenderOverlayEvent : Event, ProfilerEvent {
    override val profilerName: String = "kbRender2D"
}