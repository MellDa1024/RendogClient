package com.rendog.client.gui.hudgui.window

import com.rendog.client.gui.hudgui.AbstractHudElement
import com.rendog.client.gui.rgui.windows.SettingWindow
import com.rendog.client.setting.settings.AbstractSetting

class HudSettingWindow(
    hudElement: AbstractHudElement,
    posX: Float,
    posY: Float
) : SettingWindow<AbstractHudElement>(hudElement.name, hudElement, posX, posY, SettingGroup.NONE) {

    override fun getSettingList(): List<AbstractSetting<*>> {
        return element.settingList
    }

}