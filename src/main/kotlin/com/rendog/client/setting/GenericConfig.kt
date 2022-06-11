package com.rendog.client.setting

import com.rendog.client.RendogMod
import com.rendog.client.setting.configs.NameableConfig

internal object GenericConfig : NameableConfig<GenericConfigClass>(
    "generic",
    "${RendogMod.DIRECTORY}config/"
)