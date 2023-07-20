package kr.rendog.client.setting

import kr.rendog.client.gui.rgui.Component
import kr.rendog.client.module.modules.client.Configurations
import kr.rendog.client.setting.configs.AbstractConfig
import kr.rendog.client.setting.settings.AbstractSetting
import kr.rendog.client.util.FolderUtils
import java.io.File

internal object GuiConfig : AbstractConfig<Component>(
    "gui",
    "${FolderUtils.rendogFolder}config/gui"
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