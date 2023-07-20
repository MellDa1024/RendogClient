package kr.rendog.client.module.modules.client

import kr.rendog.client.event.events.ShutdownEvent
import kr.rendog.client.event.listener.listener
import kr.rendog.client.gui.hudgui.RendogHudGui
import kr.rendog.client.module.Category
import kr.rendog.client.module.Module

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
