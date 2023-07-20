package kr.rendog.client.gui

import kr.rendog.client.RendogMod
import kr.rendog.client.commons.collections.AliasSet
import kr.rendog.client.commons.utils.ClassUtils
import kr.rendog.client.commons.utils.ClassUtils.instance
import kr.rendog.client.event.RendogEventBus
import kr.rendog.client.gui.clickgui.RendogClickGui
import kr.rendog.client.gui.hudgui.AbstractHudElement
import kr.rendog.client.gui.hudgui.RendogHudGui
import kr.rendog.client.util.AsyncCachedValue
import kr.rendog.client.util.StopTimer
import kr.rendog.client.util.TimeUnit
import kotlinx.coroutines.Deferred
import java.lang.reflect.Modifier

internal object GuiManager : kr.rendog.client.AsyncLoader<List<Class<out AbstractHudElement>>> {
    override var deferred: Deferred<List<Class<out AbstractHudElement>>>? = null
    private val hudElementSet = AliasSet<AbstractHudElement>()

    val hudElements by AsyncCachedValue(5L, TimeUnit.SECONDS) {
        hudElementSet.distinct().sortedBy { it.name }
    }

    override fun preLoad0(): List<Class<out AbstractHudElement>> {
        val stopTimer = StopTimer()

        val list = ClassUtils.findClasses<AbstractHudElement>("kr.rendog.client.gui.hudgui.elements") {
            filter { Modifier.isFinal(it.modifiers) }
        }

        val time = stopTimer.stop()

        RendogMod.LOG.info("${list.size} hud elements found, took ${time}ms")
        return list
    }

    override fun load0(input: List<Class<out AbstractHudElement>>) {
        val stopTimer = StopTimer()

        for (clazz in input) {
            register(clazz.instance)
        }

        val time = stopTimer.stop()
        RendogMod.LOG.info("${input.size} hud elements loaded, took ${time}ms")

        RendogClickGui.onGuiClosed()
        RendogHudGui.onGuiClosed()

        RendogEventBus.subscribe(RendogClickGui)
        RendogEventBus.subscribe(RendogHudGui)
    }

    internal fun register(hudElement: AbstractHudElement) {
        hudElementSet.add(hudElement)
        RendogHudGui.register(hudElement)
    }

    internal fun unregister(hudElement: AbstractHudElement) {
        hudElementSet.remove(hudElement)
        RendogHudGui.unregister(hudElement)
    }

    fun getHudElementOrNull(name: String?) = name?.let { hudElementSet[it] }
}