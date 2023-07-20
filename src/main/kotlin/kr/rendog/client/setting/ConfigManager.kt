package kr.rendog.client.setting

import kr.rendog.client.RendogMod
import kr.rendog.client.commons.collections.NameableSet
import kr.rendog.client.setting.configs.IConfig

internal object ConfigManager {
    private val configSet = NameableSet<IConfig>()

    init {
        register(GuiConfig)
        register(ModuleConfig)
    }

    fun loadAll(): Boolean {
        var success = load(GenericConfig) // Generic config must be loaded first

        configSet.forEach {
            success = load(it) || success
        }

        return success
    }

    fun load(config: IConfig): Boolean {
        return try {
            config.load()
            RendogMod.LOG.info("${config.name} config loaded")
            true
        } catch (e: Exception) {
            RendogMod.LOG.error("Failed to load ${config.name} config", e)
            false
        }
    }

    fun saveAll(): Boolean {
        var success = save(GenericConfig) // Generic config must be loaded first

        configSet.forEach {
            success = save(it) || success
        }

        return success
    }

    fun save(config: IConfig): Boolean {
        return try {
            config.save()
            RendogMod.LOG.info("${config.name} config saved")
            true
        } catch (e: Exception) {
            RendogMod.LOG.error("Failed to save ${config.name} config!", e)
            false
        }
    }

    fun register(config: IConfig) {
        configSet.add(config)
    }

    fun unregister(config: IConfig) {
        configSet.remove(config)
    }
}