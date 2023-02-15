package com.rendog.client.command.commands

import com.rendog.client.command.ClientCommand
import com.rendog.client.manager.managers.RendogCDManager
import com.rendog.client.util.text.MessageSendHelper
import com.rendog.client.util.text.Color.deColorize
import com.rendog.client.util.threads.runSafe
import java.lang.System.currentTimeMillis
import kotlin.math.roundToInt

object RendogCDCommand : ClientCommand(
    name = "rendogcd",
    description = "Command for RendogCooldown Database"
) {
    var lastReloadTime = 0L
    init {
        literal("reload") {
            execute("reloads RendogServer's Weapon Cooldown data, Command's CoolDown : 10s") {
                if (lastReloadTime + 10 * 1000 <= currentTimeMillis()) {
                    lastReloadTime = currentTimeMillis()
                    if (RendogCDManager.loadCoolDownData()) {
                        MessageSendHelper.sendChatMessage("Weapon's Cooldown data has loaded successfully.")
                    } else {
                        MessageSendHelper.sendErrorMessage("Failed to load Cooldown data, see Minecraft's log for more information.")
                    }
                } else {
                    val cooldown = ((currentTimeMillis()- lastReloadTime).toDouble()/100.0).roundToInt()/10.0
                    MessageSendHelper.sendWarningMessage("Reload Command can be execute with 10 seconds delay. You have $cooldown sec left.")
                }
            }
        }
        literal("showInfo") {
            greedy("weapon") { weaponName ->
                execute("Shows Item's Database, weapon's name should be exact name of weapon.") {
                    if (RendogCDManager.inDatabase(weaponName.value)) {
                        val leftClick = RendogCDManager.getCD(weaponName.value.trim(), false)
                        val rightClick = RendogCDManager.getCD(weaponName.value.trim(), true)
                        MessageSendHelper.sendChatMessage("${weaponName.value.trim()} 's Data : ")
                        if (leftClick == 0.0) {
                            MessageSendHelper.sendChatMessage("LeftClick Cooldown : Unavailable")
                        } else {
                            MessageSendHelper.sendChatMessage("LeftClick Cooldown : $leftClick")
                        }
                        if (rightClick == 0.0) {
                            MessageSendHelper.sendChatMessage("RightClick Cooldown : Unavailable")
                        } else {
                            MessageSendHelper.sendChatMessage("RightClick Cooldown : $rightClick")
                        }
                        MessageSendHelper.sendChatMessage("Available in Village : ${RendogCDManager.isAbleInVillage(weaponName.value.trim())}")
                    } else {
                        MessageSendHelper.sendErrorMessage("There isn't any data of weapon which named ${weaponName.value}.")
                    }
                }
            }
        }
        literal("showHeldItemInfo") {
            execute("Shows held Item's Information.") {
                runSafe {
                    val weaponName = player.inventory.getCurrentItem().displayName.deColorize().trim()
                    if (RendogCDManager.inDatabase(weaponName)) {
                        val leftClick = RendogCDManager.getCD(weaponName, false)
                        val rightClick = RendogCDManager.getCD(weaponName, true)
                        MessageSendHelper.sendChatMessage("$weaponName 's Data : ")
                        if (leftClick == 0.0) {
                            MessageSendHelper.sendChatMessage("LeftClick Cooldown : Unavailable")
                        } else {
                            MessageSendHelper.sendChatMessage("LeftClick Cooldown : $leftClick")
                        }
                        if (rightClick == 0.0) {
                            MessageSendHelper.sendChatMessage("RightClick Cooldown : Unavailable")
                        } else {
                            MessageSendHelper.sendChatMessage("RightClick Cooldown : $rightClick")
                        }
                        MessageSendHelper.sendChatMessage("Available in Village : ${RendogCDManager.isAbleInVillage(weaponName)}")
                    } else {
                        MessageSendHelper.sendErrorMessage("There isn't any data of weapon which named ${weaponName}.")
                    }
                }
            }
        }
    }
}