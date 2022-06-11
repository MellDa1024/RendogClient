package com.rendog.client.command.commands

import com.rendog.client.command.ClientCommand
import com.rendog.client.manager.managers.RendogCDManager
import com.rendog.client.module.modules.client.CommandConfig
import com.rendog.client.util.text.MessageSendHelper
import com.rendog.client.util.text.formatValue

object RendogCDCommand : ClientCommand(
    name = "rendogcd",
    description = "Command for RendogCooldown Database"
) {
    init {
        literal("reload") {
            execute("reloads RendogServer's Weapon Cooldown data") {
                if (RendogCDManager.loadCoolDownData()) {
                    MessageSendHelper.sendChatMessage("Weapon's Cooldown data has loaded successfully.")
                } else {
                    MessageSendHelper.sendErrorMessage("Failed to load Cooldown data, see Minecraft's log for more information.")
                }
            }
        }
    }
}