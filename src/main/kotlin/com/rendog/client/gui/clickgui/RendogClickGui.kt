package com.rendog.client.gui.clickgui

import com.rendog.client.gui.AbstractRendogGui
import com.rendog.client.gui.clickgui.component.*
import com.rendog.client.gui.clickgui.window.ModuleSettingWindow
import com.rendog.client.gui.rgui.Component
import com.rendog.client.gui.rgui.windows.ListWindow
import com.rendog.client.module.AbstractModule
import com.rendog.client.module.ModuleManager
import com.rendog.client.module.modules.client.ClickGUI
import com.rendog.client.util.math.Vec2f
import net.minecraft.util.text.TextFormatting
import org.lwjgl.input.Keyboard

object RendogClickGui : AbstractRendogGui<ModuleSettingWindow, AbstractModule>() {

    private val windows = ArrayList<ListWindow>()
    private var moduleCount = ModuleManager.modules.size

    init {
        val allButtons = ModuleManager.modules
            .groupBy { it.category.displayName }
            .mapValues { (_, modules) -> modules.map { ModuleButton(it) } }

        var posX = 0.0f
        var posY = 0.0f
        val screenWidth = mc.displayWidth / ClickGUI.getScaleFactorFloat()

        /* Modules */
        for ((category, buttons) in allButtons) {
            val window = ListWindow(category, posX, posY, 90.0f, 300.0f, Component.SettingGroup.CLICK_GUI)

            window.addAll(buttons.customSort())
            windows.add(window)
            posX += 90.0f

            if (posX > screenWidth) {
                posX = 0.0f
                posY += 100.0f
            }
        }


        windowList.addAll(windows)
    }

    override fun onDisplayed() {
        reorderModules()

        super.onDisplayed()
    }

    override fun onGuiClosed() {
        super.onGuiClosed()
        setModuleButtonVisibility { true }
    }

    override fun newSettingWindow(element: AbstractModule, mousePos: Vec2f): ModuleSettingWindow {
        return ModuleSettingWindow(element, mousePos.x, mousePos.y)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (keyCode == Keyboard.KEY_ESCAPE || keyCode == ClickGUI.bind.value.key && !searching && settingWindow?.listeningChild == null) {
            ClickGUI.disable()
        } else {
            super.keyTyped(typedChar, keyCode)

            val string = typedString.replace(" ", "")

            if (string.isNotEmpty()) {
                setModuleButtonVisibility { moduleButton ->
                    moduleButton.module.name.contains(string, true)
                        || moduleButton.module.alias.any { it.contains(string, true) }
                }
            } else {
                setModuleButtonVisibility { true }
            }
        }
    }

    private fun setModuleButtonVisibility(function: (ModuleButton) -> Boolean) {
        windowList.filterIsInstance<ListWindow>().forEach { window ->
            window.children.filterIsInstance<ModuleButton>().forEach {
                it.visible = function(it)
            }
        }
    }

    fun reorderModules() {
        moduleCount = ModuleManager.modules.size
        val allButtons = ModuleManager.modules
            .groupBy { it.category.displayName }
            .mapValues { (_, modules) -> modules.map { ModuleButton(it) } }

        windows.forEach { window ->
            window.clear()
            allButtons[window.name]?.let {
                window.addAll(it.customSort())
            }
        }

        setModuleButtonVisibility { moduleButton ->
            moduleButton.module.name.contains(typedString, true)
                || moduleButton.module.alias.any { it.contains(typedString, true) }
        }
    }

    fun printInfo(name: String, version: String) =
        "${TextFormatting.GREEN}$name${TextFormatting.RESET} ${TextFormatting.GRAY}v$version${TextFormatting.RESET}"

    private fun List<ModuleButton>.customSort(): List<ModuleButton> {
        return when (ClickGUI.sortBy.value) {
            ClickGUI.SortByOptions.CUSTOM -> this.sortedByDescending { it.module.priorityForGui.value }
            ClickGUI.SortByOptions.FREQUENCY -> this.sortedByDescending { it.module.clicks.value }
            else -> this.sortedBy { it.name }
        }
    }
}