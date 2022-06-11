package com.rendog.client.gui

import com.rendog.client.RendogMod
import com.rendog.client.event.RendogEventBus
import com.rendog.client.gui.clickgui.RendogClickGui
import com.rendog.client.gui.hudgui.AbstractHudElement
import com.rendog.client.gui.hudgui.RendogHudGui
import com.rendog.client.util.AsyncCachedValue
import com.rendog.client.util.StopTimer
import com.rendog.client.util.TimeUnit
import com.rendog.client.commons.collections.AliasSet
import com.rendog.client.commons.utils.ClassUtils
import com.rendog.client.commons.utils.ClassUtils.instance
import kotlinx.coroutines.Deferred
import java.lang.reflect.Modifier

internal object GuiManager : com.rendog.client.AsyncLoader<List<Class<out AbstractHudElement>>> {
    override var deferred: Deferred<List<Class<out AbstractHudElement>>>? = null
    private val hudElementSet = AliasSet<AbstractHudElement>()

    val hudElements by AsyncCachedValue(5L, TimeUnit.SECONDS) {
        hudElementSet.distinct().sortedBy { it.name }
    }

    override fun preLoad0(): List<Class<out AbstractHudElement>> {
        val stopTimer = StopTimer()

        val list = ClassUtils.findClasses<AbstractHudElement>("com.rendog.client.gui.hudgui.elements") {
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