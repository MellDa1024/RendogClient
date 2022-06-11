package com.rendog.client.gui.clickgui.component

import com.rendog.client.gui.clickgui.RendogClickGui
import com.rendog.client.gui.rgui.component.BooleanSlider
import com.rendog.client.module.AbstractModule
import com.rendog.client.util.math.Vec2f

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