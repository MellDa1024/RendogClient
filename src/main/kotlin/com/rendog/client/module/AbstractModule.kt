package com.rendog.client.module

import com.rendog.client.event.RendogEventBus
import com.rendog.client.event.listener.events.ModuleToggleEvent
import com.rendog.client.gui.clickgui.RendogClickGui
import com.rendog.client.module.modules.client.ClickGUI
import com.rendog.client.setting.configs.NameableConfig
import com.rendog.client.setting.settings.AbstractSetting
import com.rendog.client.setting.settings.SettingRegister
import com.rendog.client.setting.settings.impl.number.IntegerSetting
import com.rendog.client.setting.settings.impl.other.BindSetting
import com.rendog.client.setting.settings.impl.primitive.BooleanSetting
import com.rendog.client.util.Bind
import com.rendog.client.util.text.MessageSendHelper
import com.rendog.client.commons.interfaces.Alias
import com.rendog.client.commons.interfaces.Nameable
import net.minecraft.client.Minecraft

@Suppress("UNCHECKED_CAST")
abstract class AbstractModule(
    override val name: String,
    override val alias: Array<String> = emptyArray(),
    val category: Category,
    val description: String,
    val modulePriority: Int = -1,
    var alwaysListening: Boolean = false,
    val showOnArray: Boolean = true,
    val alwaysEnabled: Boolean = false,
    val enabledByDefault: Boolean = false,
    private val config: NameableConfig<out Nameable>
) : Nameable, Alias, SettingRegister<Nameable> by config as NameableConfig<Nameable> {

    val bind = BindSetting("Bind", Bind(), { !alwaysEnabled }).also(::addSetting)
    private val enabled = BooleanSetting("Enabled", false, { false }).also(::addSetting)
    private val visible = BooleanSetting("Visible", showOnArray).also(::addSetting)
    private val default = BooleanSetting("Default", false, { settingList.isNotEmpty() }).also(::addSetting)
    val priorityForGui = IntegerSetting("Priority In GUI", 0, 0..1000, 50, { ClickGUI.sortBy.value == ClickGUI.SortByOptions.CUSTOM }, fineStep = 1).also(::addSetting)
    val clicks = IntegerSetting("Clicks", 0, 0..Int.MAX_VALUE, 1, { false }).also(::addSetting) // Not nice, however easiest way to save it.

    val fullSettingList get() = (config as NameableConfig<Nameable>).getSettings(this)
    val settingList: List<AbstractSetting<*>> get() = fullSettingList.filter { it != bind && it != enabled && it != visible && it != default && it != clicks }

    val isEnabled: Boolean get() = enabled.value || alwaysEnabled
    val isDisabled: Boolean get() = !isEnabled
    val chatName: String get() = "[${name}]"
    val isVisible: Boolean get() = visible.value

    private fun addSetting(setting: AbstractSetting<*>) {
        (config as NameableConfig<Nameable>).addSettingToConfig(this, setting)
    }

    internal fun postInit() {
        enabled.value = enabledByDefault || alwaysEnabled
        if (alwaysListening) RendogEventBus.subscribe(this)
    }

    fun toggle() {
        enabled.value = !enabled.value
        if (enabled.value) clicks.value++
    }

    fun enable() {
        clicks.value++
        enabled.value = true
    }

    fun disable() {
        enabled.value = false
    }

    open fun isActive(): Boolean {
        return isEnabled || alwaysListening
    }

    open fun getHudInfo(): String {
        return ""
    }

    protected fun onEnable(block: (Boolean) -> Unit) {
        enabled.valueListeners.add { _, input ->
            if (input) {
                block(true)
            }
        }
    }

    protected fun onDisable(block: (Boolean) -> Unit) {
        enabled.valueListeners.add { _, input ->
            if (!input) {
                block(false)
            }
        }
    }

    protected fun onToggle(block: (Boolean) -> Unit) {
        enabled.valueListeners.add { _, input ->
            block(input)
        }
    }

    init {
        enabled.consumers.add { prev, input ->
            val enabled = alwaysEnabled || input

            if (prev != input && !alwaysEnabled) {
                RendogEventBus.post(ModuleToggleEvent(this))
            }

            if (enabled || alwaysListening) {
                RendogEventBus.subscribe(this)
            } else {
                RendogEventBus.unsubscribe(this)
            }

            enabled
        }

        default.valueListeners.add { _, it ->
            if (it) {
                settingList.forEach { it.resetValue() }
                default.value = false
                MessageSendHelper.sendChatMessage("$chatName Set to defaults!")
            }
        }

        priorityForGui.listeners.add { RendogClickGui.reorderModules() }

        // clicks is deliberately not re-organised when changed.
    }

    protected companion object {
        val mc: Minecraft = Minecraft.getMinecraft()
    }
}
