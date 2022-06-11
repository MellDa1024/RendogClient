package com.rendog.client.gui.hudgui.elements.world

import com.rendog.client.event.SafeClientEvent
import com.rendog.client.gui.hudgui.LabelHud
import com.rendog.client.util.graphics.font.HAlign
import com.rendog.client.util.graphics.font.TextComponent
import com.rendog.client.util.math.VectorUtils.times
import net.minecraft.util.math.Vec3d

internal object Coordinates : LabelHud(
    name = "Coordinates",
    category = Category.WORLD,
    description = "Display the current coordinate"
) {

    init {
        dockingH = HAlign.RIGHT
    }

    private val showX by setting("Show X", true)
    private val showY by setting("Show Y", true)
    private val showZ by setting("Show Z", true)

    private val decimalPlaces by setting("Decimal Places", 1, 0..4, 1)
    private val thousandsSeparator by setting("Thousands Separator", false)

    override fun SafeClientEvent.updateText() {
        val entity = mc.renderViewEntity ?: player

        displayText.add("XYZ", secondaryColor)
        displayText.addLine(getFormattedCoords(entity.positionVector))
    }

    private fun getFormattedCoords(pos: Vec3d): TextComponent.TextElement {
        val x = roundOrInt(pos.x)
        val y = roundOrInt(pos.y)
        val z = roundOrInt(pos.z)
        return StringBuilder().run {
            if (showX) append(x)
            if (showY) appendWithComma(y)
            if (showZ) appendWithComma(z)
            TextComponent.TextElement(toString(), primaryColor)
        }
    }

    private fun roundOrInt(input: Double): String {
        val separatorFormat = if (thousandsSeparator) "," else ""

        return "%$separatorFormat.${decimalPlaces}f".format(input)
    }

    private fun StringBuilder.appendWithComma(string: String) = append(if (length > 0) ", $string" else string)

}