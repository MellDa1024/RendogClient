package kr.rendog.client.gui.clickgui.component

import kr.rendog.client.gui.clickgui.RendogClickGui
import kr.rendog.client.gui.rgui.component.BooleanSlider
import kr.rendog.client.module.AbstractModule
import kr.rendog.client.util.math.Vec2f

class ModuleButton(val module: AbstractModule) : BooleanSlider(module.name, 0.0, module.description) {
    init {
        if (module.isEnabled) value = 1.0
    }

    override fun onTick() {
        super.onTick()
        value = if (module.isEnabled) 1.0 else 0.0
    }

    override fun onClick(mousePos: Vec2f, buttonId: Int) {
        super.onClick(mousePos, buttonId)
        if (buttonId == 0) module.toggle()
    }

    override fun onRelease(mousePos: Vec2f, buttonId: Int) {
        super.onRelease(mousePos, buttonId)
        if (buttonId == 1) RendogClickGui.displaySettingWindow(module)
    }
}