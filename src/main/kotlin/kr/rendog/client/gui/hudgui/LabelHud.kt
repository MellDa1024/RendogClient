package kr.rendog.client.gui.hudgui

import kr.rendog.client.gui.rgui.Component
import kr.rendog.client.setting.GuiConfig
import kr.rendog.client.setting.settings.SettingRegister

internal abstract class LabelHud(
    name: String,
    alias: Array<String> = emptyArray(),
    category: Category,
    description: String,
    alwaysListening: Boolean = false,
    enabledByDefault: Boolean = false,
    separator: String = " ",
) : AbstractLabelHud(name, alias, category, description, alwaysListening, enabledByDefault, GuiConfig, separator),
    SettingRegister<Component> by GuiConfig