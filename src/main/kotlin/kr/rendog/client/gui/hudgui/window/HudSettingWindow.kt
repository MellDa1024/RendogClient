package kr.rendog.client.gui.hudgui.window

import kr.rendog.client.gui.hudgui.AbstractHudElement
import kr.rendog.client.gui.rgui.windows.SettingWindow
import kr.rendog.client.setting.settings.AbstractSetting

class HudSettingWindow(
    hudElement: AbstractHudElement,
    posX: Float,
    posY: Float
) : SettingWindow<AbstractHudElement>(hudElement.name, hudElement, posX, posY, SettingGroup.NONE) {

    override fun getSettingList(): List<AbstractSetting<*>> {
        return element.settingList
    }

}