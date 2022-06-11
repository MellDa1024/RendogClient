package com.rendog.client.setting

import com.rendog.client.RendogMod
import com.rendog.client.gui.rgui.Component
import com.rendog.client.module.modules.client.Configurations
import com.rendog.client.setting.configs.AbstractConfig
import com.rendog.client.setting.settings.AbstractSetting
import java.io.File

internal object GuiConfig : AbstractConfig<Component>(
    "gui",
    "${RendogMod.DIRECTORY}config/gui"
) {
    override val file: File get() = File("$filePath/${Configurations.guiPreset}.json")
    override val backup get() = File("$filePath/${Configurations.guiPreset}.bak")

    override fun addSettingToConfig(owner: Component, setting: AbstractSetting<*>) {
        val groupName = owner.settingGroup.groupName
        if (groupName.isNotEmpty()) {
            getGroupOrPut(groupName).getGroupOrPut(owner.name).addSetting(setting)
        }
    }

    override fun getSettings(owner: Component): List<AbstractSetting<*>> {
        val groupName = owner.settingGroup.groupName
        if (groupName.isNotEmpty()) {
            return getGroupOrPut(groupName).getGroupOrPut(owner.name).getSettings()
        } else {
            return emptyList()
        }
    }
}