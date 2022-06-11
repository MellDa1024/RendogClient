package com.lambda.client.setting

import com.lambda.client.LambdaMod
import com.lambda.client.gui.rgui.Component
import com.lambda.client.module.modules.client.Configurations
import com.lambda.client.setting.configs.AbstractConfig
import com.lambda.client.setting.settings.AbstractSetting
import java.io.File

internal object GuiConfig : AbstractConfig<Component>(
    "gui",
    "${LambdaMod.DIRECTORY}config/gui"
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