package kr.rendog.client.event.events

import kr.rendog.client.event.Event
import kr.rendog.client.event.ProfilerEvent

class RenderOverlayEvent : Event, ProfilerEvent {
    override val profilerName: String = "kbRender2D"
}