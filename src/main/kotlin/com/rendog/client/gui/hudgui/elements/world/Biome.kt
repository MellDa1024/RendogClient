package com.rendog.client.gui.hudgui.elements.world

import com.rendog.client.event.SafeClientEvent
import com.rendog.client.gui.hudgui.LabelHud
import com.rendog.client.util.graphics.font.HAlign

internal object Biome : LabelHud(
    name = "Biome",
    category = Category.WORLD,
    description = "Display the current biome you are in"
) {

    init {
        dockingH = HAlign.RIGHT
    }

    override fun SafeClientEvent.updateText() {
        val biome = world.getBiome(player.position).biomeName ?: "Unknown"

        displayText.add(biome, primaryColor)
        displayText.add("Biome", secondaryColor)
    }

}