package com.lambda.client.command.commands

import com.lambda.client.command.ClientCommand
import com.lambda.client.module.modules.client.Configurations
import com.lambda.client.util.ConfigUtils
import com.lambda.client.util.TickTimer
import com.lambda.client.util.TimeUnit
import com.lambda.client.util.text.MessageSendHelper
import com.lambda.client.util.text.formatValue
import com.lambda.client.util.threads.defaultScope
import com.lambda.client.command.execute.IExecuteEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object ConfigCommand : ClientCommand(
    name = "config",
    alias = arrayOf("cfg"),
    description = "Change config saving path or manually save and reload your config"
) {
    private val confirmTimer = TickTimer(TimeUnit.SECONDS)
    private var lastArgs = emptyArray<String>()

    init {
        literal("all") {
            literal("reload") {
                execute("Reload all configs") {
                    defaultScope.launch(Dispatchers.IO) {
                        val loaded = ConfigUtils.loadAll()
                        if (loaded) MessageSendHelper.sendChatMessage("All configurations reloaded!")
                        else MessageSendHelper.sendErrorMessage("Failed to load config!")
                    }
                }
            }

            literal("save") {
                execute("Save all configs") {
                    defaultScope.launch(Dispatchers.IO) {
                        val saved = ConfigUtils.saveAll()
                        if (saved) MessageSendHelper.sendChatMessage("All configurations saved!")
                        else MessageSendHelper.sendErrorMessage("Failed to load config!")
                    }
                }
            }
        }

        enum<Configurations.ConfigType>("config type") { configTypeArg ->
            literal("reload") {
                execute("Reload a config") {
                    configTypeArg.value.reload()
                }
            }

            literal("save") {
                execute("Save a config") {
                    configTypeArg.value.save()
                }
            }

            literal("set") {
                string("name") { nameArg ->
                    execute("Change preset") {
                        configTypeArg.value.setPreset(nameArg.value)
                    }
                }
            }

            literal("copy", "ctrl+c", "ctrtc") {
                string("name") { nameArg ->
                    execute("Copy current preset to specific preset") {
                        val name = nameArg.value
                        if (!confirm()) return@execute

                        configTypeArg.value.copyPreset(name)
                    }
                }
            }

            literal("delete", "del", "remove") {
                string("name") { nameArg ->
                    execute("Delete specific preset") {
                        val name = nameArg.value
                        if (!confirm()) return@execute

                        configTypeArg.value.deletePreset(name)
                    }
                }
            }

            literal("list") {
                execute("List all available presets") {
                    configTypeArg.value.printAllPresets()
                }
            }

            execute("Print current preset name") {
                configTypeArg.value.printCurrentPreset()
            }
        }
    }

    private fun IExecuteEvent.confirm(): Boolean {
        return if (!args.contentEquals(lastArgs) || confirmTimer.tick(8L, false)) {
            MessageSendHelper.sendWarningMessage("This can't be undone, run " +
                "${formatValue("${prefix}${args.joinToString(" ")}")} to confirm!")

            confirmTimer.reset()
            lastArgs = args
            false
        } else {
            lastArgs = emptyArray()
            true
        }
    }
}
