package kr.rendog.client.module.modules.client

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.rendog.client.RendogMod
import kr.rendog.client.commons.interfaces.DisplayEnum
import kr.rendog.client.event.events.ConnectionEvent
import kr.rendog.client.event.listener.listener
import kr.rendog.client.gui.AbstractRendogGui
import kr.rendog.client.module.AbstractModule
import kr.rendog.client.module.Category
import kr.rendog.client.setting.ConfigManager
import kr.rendog.client.setting.GenericConfig
import kr.rendog.client.setting.GuiConfig
import kr.rendog.client.setting.ModuleConfig
import kr.rendog.client.setting.configs.AbstractConfig
import kr.rendog.client.setting.configs.IConfig
import kr.rendog.client.setting.settings.impl.primitive.StringSetting
import kr.rendog.client.util.ConfigUtils
import kr.rendog.client.util.TickTimer
import kr.rendog.client.util.TimeUnit
import kr.rendog.client.util.text.MessageSendHelper
import kr.rendog.client.util.text.formatValue
import kr.rendog.client.util.threads.BackgroundScope
import kr.rendog.client.util.threads.defaultScope
import java.io.File
import java.io.IOException
import java.nio.file.Paths

internal object Configurations : AbstractModule(
    name = "Configurations",
    description = "Setting up configurations of the client",
    category = Category.CLIENT,
    alwaysEnabled = true,
    showOnArray = false,
    config = GenericConfig
) {
    private const val defaultPreset = "default"

    private val autoSaving by setting("Auto Saving", true)
    private val savingFeedBack by setting("Saving FeedBack", false, { autoSaving })
    private val savingInterval by setting("Interval", 10, 1..30, 1, { autoSaving }, description = "Frequency of auto saving in minutes")
    private val guiPresetSetting = setting("Gui Preset", defaultPreset)
    private val modulePresetSetting = setting("Module Preset", defaultPreset)

    val guiPreset by guiPresetSetting
    val modulePreset by modulePresetSetting

    private val timer = TickTimer(TimeUnit.MINUTES)
    private var connected = false

    init {
        BackgroundScope.launchLooping("Config Auto Saving", 60000L) {
            if (autoSaving && mc.currentScreen !is AbstractRendogGui<*, *> && timer.tick(savingInterval.toLong())) {
                if (savingFeedBack) MessageSendHelper.sendChatMessage("Auto saving settings...")
                else RendogMod.LOG.info("Auto saving settings...")
                ConfigUtils.saveAll()
            }
        }

        listener<ConnectionEvent.Connect> {
            connected = true
        }
    }

    private fun verifyPresetName(input: String): Boolean {
        val nameWithoutExtension = input.removeSuffix(".json")
        val nameWithExtension = "$nameWithoutExtension.json"

        return if (!ConfigUtils.isPathValid(nameWithExtension)) {
            MessageSendHelper.sendChatMessage("${formatValue(nameWithoutExtension)} is not a valid preset name")
            false
        } else {
            true
        }
    }

    private fun updatePreset(setting: StringSetting, input: String, config: IConfig) {
        if (!verifyPresetName(input)) return

        val nameWithoutExtension = input.removeSuffix(".json")
        val prev = setting.value

        try {
            ConfigManager.save(config)
            setting.value = nameWithoutExtension
            ConfigManager.save(GenericConfig)
            ConfigManager.load(config)

            MessageSendHelper.sendChatMessage("Preset set to ${formatValue(nameWithoutExtension)}!")
        } catch (e: IOException) {
            MessageSendHelper.sendChatMessage("Couldn't set preset: ${e.message}")
            RendogMod.LOG.warn("Couldn't set path!", e)

            setting.value = prev
            ConfigManager.save(GenericConfig)
        }
    }

    init {
        with({ prev: String, input: String ->
            if (verifyPresetName(input)) {
                input
            } else {
                if (verifyPresetName(prev)) {
                    prev
                } else {
                    defaultPreset
                }
            }
        }) {
            guiPresetSetting.consumers.add(this)
            modulePresetSetting.consumers.add(this)
        }
    }

    @Suppress("UNUSED")
    enum class ConfigType(
        override val displayName: String,
        override val config: AbstractConfig<out Any>,
        override val setting: StringSetting
    ) : DisplayEnum, IConfigType {
        GUI("GUI", GuiConfig, guiPresetSetting),
        MODULES("Modules", ModuleConfig, modulePresetSetting);

        override val serverPresets get() = getJsons(config.filePath) { it.name.startsWith("server-") }

        override val allPresets get() = getJsons(config.filePath) { true }

        private companion object {
            fun getJsons(path: String, filter: (File) -> Boolean): Set<String> {
                val dir = File(path)
                if (!dir.exists() || !dir.isDirectory) return emptySet()

                val files = dir.listFiles() ?: return emptySet()
                val jsonFiles = files.filter {
                    it.isFile && it.extension == "json" && it.length() > 8L && filter(it)
                }

                return LinkedHashSet<String>().apply {
                    jsonFiles.forEach {
                        add(it.nameWithoutExtension)
                    }
                }
            }
        }
    }

    interface IConfigType : DisplayEnum {
        val config: AbstractConfig<out Any>
        val setting: StringSetting
        val serverPresets: Set<String>
        val allPresets: Set<String>

        fun reload() {
            defaultScope.launch(Dispatchers.IO) {
                var loaded = ConfigManager.load(GenericConfig)
                loaded = ConfigManager.load(config) || loaded

                if (loaded) MessageSendHelper.sendChatMessage("${formatValue(config.name)} config reloaded!")
                else MessageSendHelper.sendErrorMessage("Failed to load ${formatValue(config.name)} config!")
            }
        }

        fun save() {
            defaultScope.launch(Dispatchers.IO) {
                var saved = ConfigManager.save(GenericConfig)
                saved = ConfigManager.save(config) || saved

                if (saved) MessageSendHelper.sendChatMessage("${formatValue(config.name)} config saved!")
                else MessageSendHelper.sendErrorMessage("Failed to load ${formatValue(config.name)} config!")
            }
        }

        fun setPreset(name: String) {
            defaultScope.launch(Dispatchers.IO) {
                updatePreset(setting, name, config)
            }
        }

        fun copyPreset(name: String) {
            defaultScope.launch(Dispatchers.IO) {
                if (name == setting.value) {
                    MessageSendHelper.sendErrorMessage("Destination preset name ${formatValue(name)} is same as current preset")
                }

                ConfigManager.save(config)

                try {
                    val fileFrom = File("${config.filePath}/${setting.value}.json")
                    val fileTo = File("${config.filePath}/${name}.json")

                    fileFrom.copyTo(fileTo, true)
                } catch (e: Exception) {
                    MessageSendHelper.sendErrorMessage("Failed to copy preset, ${e.message}")
                    RendogMod.LOG.error("Failed to copy preset", e)
                }
            }
        }

        fun deletePreset(name: String) {
            defaultScope.launch(Dispatchers.IO) {
                if (!allPresets.contains(name)) {
                    MessageSendHelper.sendChatMessage("${formatValue(name)} is not a valid preset for ${formatValue(displayName)} config")
                    return@launch
                }

                try {
                    val file = File("${config.filePath}/${name}.json")
                    val fileBak = File("${config.filePath}/${name}.bak")

                    file.delete()
                    fileBak.delete()

                    MessageSendHelper.sendChatMessage("Deleted preset $name for ${formatValue(displayName)} config")
                } catch (e: Exception) {
                    MessageSendHelper.sendErrorMessage("Failed to delete preset, ${e.message}")
                    RendogMod.LOG.error("Failed to delete preset", e)
                }
            }
        }

        fun printCurrentPreset() {
            val path = Paths.get("${config.filePath}/${setting.value}.json").toAbsolutePath()
            MessageSendHelper.sendChatMessage("Path to config: ${formatValue(path)}")
        }

        fun printAllPresets() {
            if (allPresets.isEmpty()) {
                MessageSendHelper.sendChatMessage("No preset for ${formatValue(displayName)} config!")
            } else {
                MessageSendHelper.sendChatMessage("List of presets: ${formatValue(allPresets.size)}")

                allPresets.forEach {
                    val path = Paths.get("${config.filePath}/${it}.json").toAbsolutePath()
                    MessageSendHelper.sendRawChatMessage(formatValue(path))
                }
            }
        }

        private fun convertIpToPresetName(ip: String) = "server-" +
            ip.replace('.', '_').replace(':', '_')

    }
}