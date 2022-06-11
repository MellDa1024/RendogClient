package com.rendog.client.event.listener.events

import com.rendog.client.event.Event
import com.rendog.client.module.AbstractModule

class ModuleToggleEvent internal constructor(val module: AbstractModule) : Event