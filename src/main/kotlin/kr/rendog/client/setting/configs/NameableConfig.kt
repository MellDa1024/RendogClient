package kr.rendog.client.setting.configs

import kr.rendog.client.commons.interfaces.Nameable
import kr.rendog.client.setting.settings.AbstractSetting

open class NameableConfig<T : Nameable>(
    name: String,
    filePath: String
) : AbstractConfig<T>(name, filePath) {

    override fun addSettingToConfig(owner: T, setting: AbstractSetting<*>) {
        getGroupOrPut(owner.name).addSetting(setting)
    }

    override fun getSettings(owner: T) = getGroup(owner.name)?.getSettings() ?: emptyList()
}
