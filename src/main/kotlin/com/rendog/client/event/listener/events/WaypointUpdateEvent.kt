package com.rendog.client.event.listener.events

import com.rendog.client.event.Event
import com.rendog.client.manager.managers.WaypointManager.Waypoint

class WaypointUpdateEvent(val type: Type, val waypoint: Waypoint?) : Event {
    enum class Type {
        GET, ADD, REMOVE, CLEAR, RELOAD
    }
}