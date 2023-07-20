package kr.rendog.client.module.modules.render

import kr.rendog.client.manager.managers.GuideManager
import kr.rendog.client.module.Category
import kr.rendog.client.module.Module
import kr.rendog.client.util.color.EnumTextColor
import kr.rendog.client.util.text.Color.deColorize
import kr.rendog.client.util.text.format
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraft.scoreboard.ScorePlayerTeam

object ExtraTab : Module(
    name = "ExtraTab",
    description = "Expands the player tab menu",
    category = Category.RENDER
) {
    private val tabSize by setting("Max Players", 265, 80..400, 5)
    val highlightGuide by setting("Highlight Guides", true)
    private val guideColor by setting("Guide Color", EnumTextColor.YELLOW, { highlightGuide })

    @JvmStatic
    fun getPlayerName(info: NetworkPlayerInfo): String {
        val name = info.displayName?.formattedText
            ?: ScorePlayerTeam.formatPlayerName(info.playerTeam, info.gameProfile.name)

        return if (GuideManager.isGuide(name.deColorize())) {
            guideColor format name.deColorize()
        } else {
            name
        }
    }

    @JvmStatic
    fun subList(list: List<NetworkPlayerInfo>, newList: List<NetworkPlayerInfo>): List<NetworkPlayerInfo> {
        return if (isDisabled) newList else list.subList(0, tabSize.coerceAtMost(list.size))
    }
}