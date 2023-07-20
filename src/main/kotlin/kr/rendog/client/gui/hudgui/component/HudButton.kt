package kr.rendog.client.gui.hudgui.component

import kr.rendog.client.gui.hudgui.AbstractHudElement
import kr.rendog.client.gui.hudgui.RendogHudGui
import kr.rendog.client.gui.rgui.component.BooleanSlider
import kr.rendog.client.util.math.Vec2f

class HudButton(val hudElement: AbstractHudElement) : BooleanSlider(hudElement.name, 0.0, hudElement.description) {
    init {
        if (hudElement.visible) value = 1.0
    }

    override fun onTick() {
        super.onTick()
        value = if (hudElement.visible) 1.0 else 0.0
    }

    override fun onClick(mousePos: Vec2f, buttonId: Int) {
        super.onClick(mousePos, buttonId)
        if (buttonId == 0) hudElement.visible = !hudElement.visible
    }

    override fun onRelease(mousePos: Vec2f, buttonId: Int) {
        super.onRelease(mousePos, buttonId)
        if (buttonId == 1) RendogHudGui.displaySettingWindow(hudElement)
    }
}