package com.rendog.client.setting.configs

import com.rendog.client.setting.settings.AbstractSetting
import com.rendog.client.commons.interfaces.Nameable

open class NameableConfig<T : Nameable>(
    name: String,
    filePath: String
) : AbstractConfig<T>(name, filePath) {

    override fun addSettingToConfig(owner: T, setting: AbstractSetting<*>) {
        getGroupOrPut(owner.name).addSetting(setting)
    }

    override fun getSettings(owner: T) = getGroup(owner.name)?.getSettings() ?: emptyList()
}
