package com.rendog.client.gui.hudgui.elements.misc

import com.rendog.client.event.SafeClientEvent
import com.rendog.client.gui.hudgui.LabelHud
import com.rendog.client.util.InfoCalculator
import com.rendog.client.util.graphics.font.HAlign

internal object Ping : LabelHud(
    name = "Ping",
    category = Category.MISC,
    description = "Delay between client and server"
) {

    init {
        dockingH = HAlign.RIGHT
    }

    override fun SafeClientEvent.updateText() {
        displayText.add(InfoCalculator.ping().toString(), primaryColor)
        displayText.add("ms", secondaryColor)
    }

}