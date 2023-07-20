package kr.rendog.client.event.events

import kr.rendog.client.event.Event
import kr.rendog.client.module.AbstractModule

class ModuleToggleEvent internal constructor(val module: AbstractModule) : Event