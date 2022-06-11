package com.rendog.client.event.listener.events

import com.rendog.client.event.Event

abstract class ConnectionEvent : Event {
    class Connect : ConnectionEvent()
    class Disconnect : ConnectionEvent()
}