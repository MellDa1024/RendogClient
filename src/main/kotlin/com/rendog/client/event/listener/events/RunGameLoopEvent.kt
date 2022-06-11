package com.rendog.client.event.listener.events

import com.rendog.client.event.Event
import com.rendog.client.event.ProfilerEvent

sealed class RunGameLoopEvent(override val profilerName: String) : Event, ProfilerEvent {
    class Start : RunGameLoopEvent("start")
    class Tick : RunGameLoopEvent("tick")
    class Render : RunGameLoopEvent("render")
    class End : RunGameLoopEvent("end")
}