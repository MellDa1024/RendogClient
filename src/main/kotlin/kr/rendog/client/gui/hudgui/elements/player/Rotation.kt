package kr.rendog.client.gui.hudgui.elements.player

import kr.rendog.client.commons.utils.MathUtils
import kr.rendog.client.event.SafeClientEvent
import kr.rendog.client.gui.hudgui.LabelHud
import kr.rendog.client.util.graphics.font.HAlign
import kr.rendog.client.util.math.RotationUtils

internal object Rotation : LabelHud(
    name = "Rotation",
    category = Category.PLAYER,
    description = "Player rotation"
) {
    private val yaw by setting("Yaw", true)
    private val pitch by setting("Pitch", true)

    override fun SafeClientEvent.updateText() {
        if (yaw) {
            val yawVal = MathUtils.round(RotationUtils.normalizeAngle(mc.player?.rotationYaw ?: 0.0f), 1)
            displayText.add("Yaw", secondaryColor)
            displayText.add(yawVal.toString(), primaryColor)
        }
        if (pitch) {
            val pitchVal = MathUtils.round(mc.player?.rotationPitch ?: 0.0f, 1)
            displayText.add("Pitch", secondaryColor)
            displayText.add(pitchVal.toString(), primaryColor)
        }
    }

}