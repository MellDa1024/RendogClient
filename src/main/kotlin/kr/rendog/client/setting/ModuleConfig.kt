package kr.rendog.client.setting

import kr.rendog.client.module.AbstractModule
import kr.rendog.client.module.modules.client.Configurations
import kr.rendog.client.setting.configs.NameableConfig
import kr.rendog.client.util.FolderUtils
import java.io.File

internal object ModuleConfig : NameableConfig<AbstractModule>(
    "modules",
    "${FolderUtils.rendogFolder}config/modules",
) {
    override val file: File get() = File("$filePath/${Configurations.modulePreset}.json")
    override val backup get() = File("$filePath/${Configurations.modulePreset}.bak")
}