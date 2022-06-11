package com.rendog.client.manager.managers

import com.rendog.client.event.listener.events.RunGameLoopEvent
import com.rendog.client.manager.Manager
import com.rendog.client.mixin.extension.tickLength
import com.rendog.client.mixin.extension.timer
import com.rendog.client.module.AbstractModule
import com.rendog.client.util.TickTimer
import com.rendog.client.util.TimeUnit
import com.rendog.client.commons.extension.synchronized
import com.rendog.client.event.listener.listener
import java.util.*

object TimerManager : Manager {
    private val timer = TickTimer(TimeUnit.TICKS)
    private val modifications = TreeMap<AbstractModule, Pair<Float, Long>>(compareByDescending { it.modulePriority }).synchronized() // <Module, <Tick length, Added Time>>

    private var modified = false

    var tickLength = 50.0f; private set

    init {
        listener<RunGameLoopEvent.Start> {
            if (timer.tick(5L)) {
                val removeTime = System.currentTimeMillis() - 250L
                modifications.values.removeIf { it.second < removeTime }
            }

            if (mc.player != null && modifications.isNotEmpty()) {
                modifications.firstEntry()?.let {
                    mc.timer.tickLength = it.value.first
                }
                modified = true
            } else if (modified) {
                reset()
            }

            tickLength = mc.timer.tickLength
        }
    }

    fun AbstractModule.resetTimer() {
        modifications.remove(this)
    }

    fun AbstractModule.modifyTimer(tickLength: Float) {
        if (mc.player != null) {
            modifications[this] = tickLength to System.currentTimeMillis()
        }
    }

    private fun reset() {
        mc.timer.tickLength = 50.0f
        modified = false
    }
}