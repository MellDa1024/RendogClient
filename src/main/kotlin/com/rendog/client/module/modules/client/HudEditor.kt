package com.rendog.client.module.modules.client

import com.rendog.client.event.listener.events.ShutdownEvent
import com.rendog.client.gui.hudgui.RendogHudGui
import com.rendog.client.module.Category
import com.rendog.client.module.Module
import com.rendog.client.event.listener.listener

object HudEditor : Module(
    name = "HudEditor",
    description = "Edits the Hud",
    category = Category.CLIENT,
    showOnArray = false
) {
    init {
        onEnable {
            if (mc.currentScreen !is RendogHudGui) {
                ClickGUI.disable()
                mc.displayGuiScreen(RendogHudGui)
                RendogHudGui.onDisplayed()
            }
        }

        onDisable {
            if (mc.currentScreen is RendogHudGui) {
                mc.displayGuiScreen(null)
            }
        }

        listener<ShutdownEvent> {
            disable()
        }
    }
}
