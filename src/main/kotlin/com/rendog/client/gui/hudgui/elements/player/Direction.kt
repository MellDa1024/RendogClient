package com.rendog.client.gui.hudgui.elements.player

import com.rendog.client.event.SafeClientEvent
import com.rendog.client.gui.hudgui.LabelHud
import com.rendog.client.util.graphics.font.HAlign
import com.rendog.client.util.math.Direction

internal object Direction : LabelHud(
    name = "Direction",
    category = Category.PLAYER,
    description = "Direction of player facing to"
) {

    init {
        dockingH = HAlign.RIGHT
    }

    override fun SafeClientEvent.updateText() {
        val entity = mc.renderViewEntity ?: player
        val direction = Direction.fromEntity(entity)
        displayText.add(direction.displayName, secondaryColor)
        displayText.add("(${direction.displayNameXY})", primaryColor)
    }

}