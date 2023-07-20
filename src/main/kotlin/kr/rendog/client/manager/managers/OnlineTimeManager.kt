package kr.rendog.client.manager.managers

import kr.rendog.client.event.events.ConnectionEvent
import kr.rendog.client.event.listener.listener
import kr.rendog.client.manager.Manager
import kotlin.time.Duration
import kotlin.time.TimeSource

object OnlineTimeManager: Manager {

    private var connectTime = TimeSource.Monotonic.markNow()

    init {
        listener<ConnectionEvent.Connect> {
            connectTime = TimeSource.Monotonic.markNow()
        }
    }

    fun getOnlineTime(): Duration {
        return connectTime.elapsedNow()
    }
}