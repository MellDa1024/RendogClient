package kr.rendog.client.gui.hudgui.elements.player

import kr.rendog.client.event.SafeClientEvent
import kr.rendog.client.gui.hudgui.LabelHud
import kr.rendog.client.util.combat.HealthUtils.getRendogCurrentHealth
import kr.rendog.client.util.combat.HealthUtils.getRendogMaxHealth
import kotlin.math.roundToInt

internal object Health : LabelHud(
    name = "Health",
    category = Category.PLAYER,
    description = "Shows Health"
) {
    private val showMaxHealth by setting("Show Max Health", false)

    override fun SafeClientEvent.updateText() {
        displayText.add("Health :", secondaryColor)
        displayText.add(getRendogCurrentHealth(mc.player).roundToInt().toString(), primaryColor)
        if (showMaxHealth) {
            displayText.add("/", secondaryColor)
            displayText.add(getRendogMaxHealth(mc.player).toString(), primaryColor)
        }
    }
}