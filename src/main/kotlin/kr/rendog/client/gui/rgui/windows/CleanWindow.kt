package kr.rendog.client.gui.rgui.windows

import kr.rendog.client.commons.interfaces.Nameable
import kr.rendog.client.gui.rgui.WindowComponent
import kr.rendog.client.setting.GuiConfig
import kr.rendog.client.setting.configs.AbstractConfig

/**
 * Window with no rendering
 */
open class CleanWindow(
    name: String,
    posX: Float,
    posY: Float,
    width: Float,
    height: Float,
    settingGroup: SettingGroup,
    config: AbstractConfig<out Nameable> = GuiConfig
) : WindowComponent(name, posX, posY, width, height, settingGroup, config)