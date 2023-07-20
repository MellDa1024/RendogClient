package kr.rendog.client.manager

import kr.rendog.client.RendogMod
import kr.rendog.client.commons.utils.ClassUtils
import kr.rendog.client.commons.utils.ClassUtils.instance
import kr.rendog.client.event.RendogEventBus
import kr.rendog.client.util.StopTimer
import kotlinx.coroutines.Deferred

internal object ManagerLoader : kr.rendog.client.AsyncLoader<List<Class<out Manager>>> {
    override var deferred: Deferred<List<Class<out Manager>>>? = null

    override fun preLoad0(): List<Class<out Manager>> {
        val stopTimer = StopTimer()

        val list = ClassUtils.findClasses<Manager>("kr.rendog.client.manager.managers")

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