package com.rendog.client.gui.hudgui.elements.misc

import com.rendog.client.event.SafeClientEvent
import com.rendog.client.gui.hudgui.LabelHud
import com.rendog.client.util.graphics.font.HAlign

internal object MemoryUsage : LabelHud(
    name = "MemoryUsage",
    category = Category.MISC,
    description = "Display the used, allocated and max memory"
) {

    init {
        dockingH = HAlign.RIGHT
    }

    private val showAllocated = setting("Show Allocated", false)
    private val showMax = setting("Show Max", false)

    override fun SafeClientEvent.updateText() {
        val memory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576L
        displayText.add(memory.toString(), primaryColor)
        if (showAllocated.value) {
            val allocatedMemory = Runtime.getRuntime().totalMemory() / 1048576L
            displayText.add(allocatedMemory.toString(), primaryColor)
        }
        if (showMax.value) {
            val maxMemory = Runtime.getRuntime().maxMemory() / 1048576L
            displayText.add(maxMemory.toString(), primaryColor)
        }
        displayText.add("MB", secondaryColor)
    }

}