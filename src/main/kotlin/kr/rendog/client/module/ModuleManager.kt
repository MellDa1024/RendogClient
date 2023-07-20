package kr.rendog.client.module

import kr.rendog.client.AsyncLoader
import kr.rendog.client.RendogMod
import kr.rendog.client.commons.collections.AliasSet
import kr.rendog.client.commons.utils.ClassUtils
import kr.rendog.client.commons.utils.ClassUtils.instance
import kr.rendog.client.event.RendogEventBus
import kr.rendog.client.util.AsyncCachedValue
import kr.rendog.client.util.StopTimer
import kr.rendog.client.util.TimeUnit
import kotlinx.coroutines.Deferred
import org.lwjgl.input.Keyboard
import java.lang.reflect.Modifier

object ModuleManager : AsyncLoader<List<Class<out AbstractModule>>> {
    override var deferred: Deferred<List<Class<out AbstractModule>>>? = null

    private val moduleSet = AliasSet<AbstractModule>()
    private val modulesDelegate = AsyncCachedValue(5L, TimeUnit.SECONDS) {
        moduleSet.distinct().sortedBy { it.name }
    }

    val modules by modulesDelegate

    override fun preLoad0(): List<Class<out AbstractModule>> {
        val stopTimer = StopTimer()

        val list = ClassUtils.findClasses<AbstractModule>("kr.rendog.client.module.modules") {
            filter { Modifier.isFinal(it.modifiers) }
        }

        val time = stopTimer.stop()

        RendogMod.LOG.info("${list.size} modules found, took ${time}ms")
        return list
    }

    override fun load0(input: List<Class<out AbstractModule>>) {
        val stopTimer = StopTimer()

        for (clazz in input) {
            register(clazz.instance)
        }

        val time = stopTimer.stop()
        RendogMod.LOG.info("${input.size} modules loaded, took ${time}ms")
    }

    internal fun register(module: AbstractModule) {
        moduleSet.add(module)
        if (module.enabledByDefault || module.alwaysEnabled) module.enable()
        if (module.alwaysListening) RendogEventBus.subscribe(module)

        modulesDelegate.update()
    }

    internal fun unregister(module: AbstractModule) {
        moduleSet.remove(module)
        RendogEventBus.unsubscribe(module)

        modulesDelegate.update()
    }

    internal fun onBind(eventKey: Int) {
        if (Keyboard.isKeyDown(Keyboard.KEY_F3)) return  // if key is the 'none' key (stuff like mod key in i3 might return 0)
        modules.filter {
            it.bind.value.isDown(eventKey)
        }.forEach { it.toggle() }
    }

    internal fun onMouseBind(eventMouse: Int) {
        modules.filter {
            it.bind.value.isMouseDown(eventMouse)
        }.forEach { it.toggle() }
    }

    fun getModuleOrNull(moduleName: String?) = moduleName?.let { moduleSet[it] }
}