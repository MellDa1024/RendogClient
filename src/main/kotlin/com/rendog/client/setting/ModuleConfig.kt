package com.rendog.client.setting

import com.rendog.client.RendogMod
import com.rendog.client.module.AbstractModule
import com.rendog.client.module.modules.client.Configurations
import com.rendog.client.setting.configs.NameableConfig
import java.io.File

internal object ModuleConfig : NameableConfig<AbstractModule>(
    "modules",
    "${RendogMod.DIRECTORY}config/modules",
) {
    override val file: File get() = File("$filePath/${Configurations.modulePreset}.json")
    override val backup get() = File("$filePath/${Configurations.modulePreset}.bak")
}