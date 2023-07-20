package kr.rendog.client.setting

import kr.rendog.client.setting.configs.NameableConfig
import kr.rendog.client.util.FolderUtils

internal object GenericConfig : NameableConfig<GenericConfigClass>(
    "generic",
    "${FolderUtils.rendogFolder}config/"
)