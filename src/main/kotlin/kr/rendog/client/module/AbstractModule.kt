package kr.rendog.client.module

import kr.rendog.client.commons.interfaces.Alias
import kr.rendog.client.commons.interfaces.Nameable
import kr.rendog.client.event.RendogEventBus
import kr.rendog.client.event.events.ModuleToggleEvent
import kr.rendog.client.gui.clickgui.RendogClickGui
import kr.rendog.client.module.modules.client.ClickGUI
import kr.rendog.client.setting.configs.NameableConfig
import kr.rendog.client.setting.settings.AbstractSetting
import kr.rendog.client.setting.settings.SettingRegister
import kr.rendog.client.setting.settings.impl.number.IntegerSetting
import kr.rendog.client.setting.settings.impl.other.BindSetting
import kr.rendog.client.setting.settings.impl.primitive.BooleanSetting
import kr.rendog.client.setting.settings.impl.primitive.EnumSetting
import kr.rendog.client.util.Bind
import kr.rendog.client.util.text.MessageSendHelper
import kr.rendog.client.util.threads.safeListener
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse

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
    private val toggleMode = EnumSetting("Toggle Mode", ToggleMode.TOGGLE).also(::addSetting)
    private val enabled = BooleanSetting("Enabled", false, { false }).also(::addSetting)
    private val visible = BooleanSetting("Visible", showOnArray).also(::addSetting)
    private val default = BooleanSetting("Default", false, { settingList.isNotEmpty() }).also(::addSetting)
    val priorityForGui = IntegerSetting("Priority In GUI", 0, 0..1000, 50, { ClickGUI.sortBy.value == ClickGUI.SortByOptions.CUSTOM }, fineStep = 1).also(::addSetting)
    val clicks = IntegerSetting("Clicks", 0, 0..Int.MAX_VALUE, 1, { false }).also(::addSetting) // Not nice, however easiest way to save it.
    enum class ToggleMode {
        TOGGLE, HOLD
    }

    val fullSettingList get() = (config as NameableConfig<Nameable>).getSettings(this)
    val settingList: List<AbstractSetting<*>> get() = fullSettingList.filter { it != bind && it != enabled && it != visible && it != default && it != clicks }

    val isEnabled: Boolean get() = enabled.value || alwaysEnabled
    val isDisabled: Boolean get() = !isEnabled
    var isPaused = false
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
        isPaused = false
        if (enabled.value) clicks.value++
    }

    fun enable() {
        clicks.value++
        enabled.value = true
    }

    fun disable() {
        enabled.value = false
    }

    fun pause() {
        isPaused = true
        RendogEventBus.unsubscribe(this)
    }

    fun unpause() {
        isPaused = false
        RendogEventBus.subscribe(this)
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

        safeListener<ClientTickEvent> {
            if (it.phase != TickEvent.Phase.START || bind.value.isEmpty || toggleMode.value != ToggleMode.HOLD) return@safeListener
            bind.value.mouseKey?.let {
                if (!Mouse.isButtonDown(it-1)) toggle()
            } ?: run {
                if (!Keyboard.isKeyDown(bind.value.key)) toggle()
            }
        }
    }

    protected companion object {
        val mc: Minecraft = Minecraft.getMinecraft()
    }
}
