package com.rendog.client.manager

import com.rendog.client.RendogMod
import com.rendog.client.event.RendogEventBus
import com.rendog.client.util.StopTimer
import com.rendog.client.commons.utils.ClassUtils
import com.rendog.client.commons.utils.ClassUtils.instance
import kotlinx.coroutines.Deferred

internal object ManagerLoader : com.rendog.client.AsyncLoader<List<Class<out Manager>>> {
    override var deferred: Deferred<List<Class<out Manager>>>? = null

    override fun preLoad0(): List<Class<out Manager>> {
        val stopTimer = StopTimer()

        val list = ClassUtils.findClasses<Manager>("com.rendog.client.manager.managers")

        val time = stopTimer.stop()

        RendogMod.LOG.info("${list.size} managers found, took ${time}ms")
        return list
    }

    override fun load0(input: List<Class<out Manager>>) {
        val stopTimer = StopTimer()

        for (clazz in input) {
            RendogEventBus.subscribe(clazz.instance)
        }

        val time = stopTimer.stop()
        RendogMod.LOG.info("${input.size} managers loaded, took ${time}ms")
    }
}