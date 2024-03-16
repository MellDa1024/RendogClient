package kr.rendog.client.command.commands

import kr.rendog.client.command.ClientCommand
import kr.rendog.client.manager.managers.WeaponCoolManager
import kr.rendog.client.util.rendog.CoolDownType
import kr.rendog.client.util.text.MessageSendHelper
import kr.rendog.client.util.text.Color.deColorize
import kr.rendog.client.util.threads.runSafe
import java.lang.System.currentTimeMillis
import kotlin.math.roundToInt

object WeaponCoolCommand : ClientCommand(
    name = "weaponcool",
    description = "Command for RendogCooldown Database"
) {
    private var lastReloadTime = 0L

    init {
        literal("reload") {
            execute("reloads RendogServer's Weapon Cooldown data, Command's CoolDown : 10s") {
                if (lastReloadTime + 10 * 1000 <= currentTimeMillis()) {
                    lastReloadTime = currentTimeMillis()
                    if (WeaponCoolManager.loadCoolDownDataFromGithub()) {
                        MessageSendHelper.sendChatMessage("Weapon's Cooldown data has loaded successfully.")
                    } else {
                        MessageSendHelper.sendErrorMessage("Failed to load Cooldown data, see Minecraft's log for more information.")
                    }
                } else {
                    val cooldown = ((currentTimeMillis() - lastReloadTime).toDouble() / 100.0).roundToInt() / 10.0
                    MessageSendHelper.sendWarningMessage("Reload Command can be execute with 10 seconds delay. You have $cooldown sec left.")
                }
            }
        }
        literal("overrideFromLocalFile") {
            execute("overrides RendogServer's Weapon Cooldown data from .minecraft/RendogClient/WeaponDataV2.json.") {
                if (WeaponCoolManager.loadCoolDownDataFromFile()) {
                    MessageSendHelper.sendChatMessage("Weapon's Cooldown data has loaded from local file successfully.")
                } else {
                    MessageSendHelper.sendErrorMessage("Failed to load Cooldown data, see Minecraft's log for more information.")
                }
            }
        }
        literal("showInfo") {
            greedy("weapon") { weaponName ->
                execute("Shows Item's Database, weapon's name should be exact name of weapon.") {
                    if (WeaponCoolManager.inDatabase(weaponName.value)) {
                        val leftClick = WeaponCoolManager.getCD(weaponName.value.trim(), CoolDownType.LEFT)
                        val rightClick = WeaponCoolManager.getCD(weaponName.value.trim(), CoolDownType.RIGHT)
                        MessageSendHelper.sendChatMessage("${weaponName.value.trim()} 's Data : ")
                        if (leftClick == 0.0) MessageSendHelper.sendChatMessage("LeftClick Cooldown : Unavailable")
                        else MessageSendHelper.sendChatMessage("LeftClick Cooldown : $leftClick")

                        if (rightClick == 0.0) MessageSendHelper.sendChatMessage("RightClick Cooldown : Unavailable")
                        else MessageSendHelper.sendChatMessage("RightClick Cooldown : $rightClick")

                        MessageSendHelper.sendChatMessage("Available in Village : ${WeaponCoolManager.isAbleInVillage(weaponName.value.trim())}")
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
                    if (WeaponCoolManager.inDatabase(weaponName)) {
                        val leftClick = WeaponCoolManager.getCD(weaponName, CoolDownType.LEFT)
                        val rightClick = WeaponCoolManager.getCD(weaponName, CoolDownType.RIGHT)
                        MessageSendHelper.sendChatMessage("$weaponName 's Data : ")
                        if (leftClick == 0.0) MessageSendHelper.sendChatMessage("LeftClick Cooldown : Unavailable")
                        else MessageSendHelper.sendChatMessage("LeftClick Cooldown : $leftClick")

                        if (rightClick == 0.0) MessageSendHelper.sendChatMessage("RightClick Cooldown : Unavailable")
                        else MessageSendHelper.sendChatMessage("RightClick Cooldown : $rightClick")

                        MessageSendHelper.sendChatMessage("Available in Village : ${WeaponCoolManager.isAbleInVillage(weaponName)}")
                    } else {
                        MessageSendHelper.sendErrorMessage("There isn't any data of weapon which named ${weaponName}.")
                    }
                }
            }
        }
    }
}