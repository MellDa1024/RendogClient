package kr.rendog.client.gui.hudgui.elements.player

import kr.rendog.client.commons.interfaces.DisplayEnum
import kr.rendog.client.event.SafeClientEvent
import kr.rendog.client.gui.hudgui.LabelHud
import kr.rendog.client.util.math.Direction

internal object Direction : LabelHud(
    name = "Direction",
    category = Category.PLAYER,
    description = "Displays the direction you are facing"
) {
    private val directionDisplayMode by setting("Direction Format", DirectionFormat.XZ)

    enum class DirectionFormat(override val displayName: String): DisplayEnum {
        NAME("Name"), XZ("XZ"), BOTH("Both")
    }

    override fun SafeClientEvent.updateText() {
        val entity = mc.renderViewEntity ?: player
        val direction = Direction.fromEntity(entity)
        if (directionDisplayMode == DirectionFormat.NAME || directionDisplayMode == DirectionFormat.BOTH)
            displayText.add(direction.displayName, secondaryColor)
        if (directionDisplayMode == DirectionFormat.XZ || directionDisplayMode == DirectionFormat.BOTH)
            displayText.add("(${direction.displayNameXY})", primaryColor)
    }

}