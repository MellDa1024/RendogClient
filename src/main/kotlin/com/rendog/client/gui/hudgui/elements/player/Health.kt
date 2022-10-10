package com.rendog.client.gui.hudgui.elements.player

import com.rendog.client.event.SafeClientEvent
import com.rendog.client.gui.hudgui.LabelHud
import com.rendog.client.util.combat.HealthUtils.getRendogCurrentHealth
import com.rendog.client.util.combat.HealthUtils.getRendogMaxHealth
import com.rendog.client.util.graphics.font.HAlign
import kotlin.math.roundToInt

internal object Health : LabelHud(
    name = "Health",
    category = Category.PLAYER,
    description = "Shows Health"
) {
    private val showMaxHealth by setting("Show Max Health", false)

    init {
        dockingH = HAlign.RIGHT
    }

    override fun SafeClientEvent.updateText() {
        displayText.add("Health :", secondaryColor)
        displayText.add(getRendogCurrentHealth(mc.player).roundToInt().toString(), primaryColor)
        if (showMaxHealth) {
            displayText.add("/", secondaryColor)
            displayText.add(getRendogMaxHealth(mc.player).toString(), primaryColor)
        }
    }
}