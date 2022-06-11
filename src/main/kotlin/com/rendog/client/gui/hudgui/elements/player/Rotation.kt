package com.rendog.client.gui.hudgui.elements.player

import com.rendog.client.event.SafeClientEvent
import com.rendog.client.gui.hudgui.LabelHud
import com.rendog.client.util.math.RotationUtils
import com.rendog.client.commons.utils.MathUtils
import com.rendog.client.util.graphics.font.HAlign

internal object Rotation : LabelHud(
    name = "Rotation",
    category = Category.PLAYER,
    description = "Player rotation"
) {

    init {
        dockingH = HAlign.RIGHT
    }

    override fun SafeClientEvent.updateText() {
        val yaw = MathUtils.round(RotationUtils.normalizeAngle(mc.player?.rotationYaw ?: 0.0f), 1)
        val pitch = MathUtils.round(mc.player?.rotationPitch ?: 0.0f, 1)

        displayText.add("Yaw", secondaryColor)
        displayText.add(yaw.toString(), primaryColor)
        displayText.add("Pitch", secondaryColor)
        displayText.add(pitch.toString(), primaryColor)
    }

}