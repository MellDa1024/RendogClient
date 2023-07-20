package kr.rendog.client.event.events

import kr.rendog.client.event.Cancellable
import kr.rendog.client.event.Event
import kr.rendog.client.event.ICancellable
import kr.rendog.client.event.ProfilerEvent

class PlayerTravelEvent : Event, ICancellable by Cancellable(), ProfilerEvent {
    override val profilerName: String = "kbPlayerTravel"
}