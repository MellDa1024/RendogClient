package com.rendog.client.gui.rgui.windows

import com.rendog.client.gui.rgui.WindowComponent
import com.rendog.client.setting.GuiConfig
import com.rendog.client.setting.configs.AbstractConfig
import com.rendog.client.commons.interfaces.Nameable

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