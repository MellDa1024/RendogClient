package kr.rendog.client.gui.hudgui.elements.client

import kr.rendog.client.RendogMod
import kr.rendog.client.event.SafeClientEvent
import kr.rendog.client.gui.hudgui.LabelHud
import kr.rendog.client.util.graphics.VertexHelper
import org.lwjgl.opengl.GL11.glScalef

internal object WaterMark : LabelHud(
    name = "Watermark",
    category = Category.CLIENT,
    description = "RendogClient watermark",
    enabledByDefault = true
) {

    override val hudWidth: Float get() = (displayText.getWidth() + 2.0f) / scale
    override val hudHeight: Float get() = displayText.getHeight(2) / scale

    override fun SafeClientEvent.updateText() {
        displayText.add(RendogMod.NAME, primaryColor)
        displayText.add(RendogMod.VERSION, secondaryColor)
    }

    override fun renderHud(vertexHelper: VertexHelper) {
        val reversedScale = 1.0f / scale
        glScalef(reversedScale, reversedScale, reversedScale)
        super.renderHud(vertexHelper)
    }

    init {
        relativePosX = 0.0f
        relativePosY = 0.0f
    }
}
