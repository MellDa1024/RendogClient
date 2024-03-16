package kr.rendog.client.command.commands

import kr.rendog.client.command.ClientCommand
import kr.rendog.client.manager.managers.LootDataManager
import kr.rendog.client.util.text.MessageSendHelper
import java.lang.System.currentTimeMillis
import kotlin.math.roundToInt

object LootCommand : ClientCommand(
    name = "loot",
    description = "Command for Loot Database"
) {
    private var lastReloadTime = 0L

    init {
        literal("reload") {
            execute("reloads RendogServer's Loot data, Command's CoolDown : 10s") {
                if (lastReloadTime + 10 * 1000 <= currentTimeMillis()) {
                    lastReloadTime = currentTimeMillis()
                    if (LootDataManager.loadLootDataFromGithub()) {
                        MessageSendHelper.sendChatMessage("Loot data has loaded successfully.")
                    } else {
                        MessageSendHelper.sendErrorMessage("Failed to load Loot data, see Minecraft's log for more information.")
                    }
                } else {
                    val cooldown = ((currentTimeMillis() - lastReloadTime).toDouble() / 100.0).roundToInt() / 10.0
                    MessageSendHelper.sendWarningMessage("Reload Command can be execute with 10 seconds delay. You have $cooldown sec left.")
                }
            }
        }
        literal("overrideFromLocalFile") {
            execute("overrides RendogServer's Loot data from .minecraft/RendogClient/LootData.json.") {
                if (LootDataManager.loadLootDataFromFile()) {
                    MessageSendHelper.sendChatMessage("Loot data has loaded from local file successfully.")
                } else {
                    MessageSendHelper.sendErrorMessage("Failed to load Loot data, see Minecraft's log for more information.")
                }
            }
        }
    }
}