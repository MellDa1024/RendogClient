package com.rendog.client.module.modules.render

import com.rendog.client.manager.managers.GuideManager
import com.rendog.client.module.Category
import com.rendog.client.module.Module
import com.rendog.client.util.color.EnumTextColor
import com.rendog.client.util.text.format
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraft.scoreboard.ScorePlayerTeam

object ExtraTab : Module(
    name = "ExtraTab",
    description = "Expands the player tab menu",
    category = Category.RENDER
) {
    private val tabSize by setting("Max Players", 265, 80..400, 5)
    val highlightGuide by setting("Highlight Guides", true)
    private val guidecolor by setting("Guide Color", EnumTextColor.YELLOW, { highlightGuide })

    @JvmStatic
    fun getPlayerName(info: NetworkPlayerInfo): String {
        val name = info.displayName?.formattedText
            ?: ScorePlayerTeam.formatPlayerName(info.playerTeam, info.gameProfile.name)

        return if (GuideManager.isGuide(removecolorcode(name))) {
            guidecolor format removecolorcode(name)
        } else {
            name
        }
    }

    fun removecolorcode(message: String): String {
        val colorcode = arrayOf("§0","§1","§2","§3","§4","§5","§6","§7","§8","§9","§a","§b","§c","§d","§e","§f","§k","§l","§m","§n","§o","§r")
        var temp = message
        for (i in colorcode) {
            temp = temp.replace(i,"")
        }
        return temp
    }

    @JvmStatic
    fun subList(list: List<NetworkPlayerInfo>, newList: List<NetworkPlayerInfo>): List<NetworkPlayerInfo> {
        return if (isDisabled) newList else list.subList(0, tabSize.coerceAtMost(list.size))
    }
}