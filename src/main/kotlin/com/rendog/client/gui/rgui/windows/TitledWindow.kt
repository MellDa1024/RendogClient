package com.rendog.client.gui.rgui.windows

import com.rendog.client.module.modules.client.CustomFont
import com.rendog.client.module.modules.client.GuiColors
import com.rendog.client.util.graphics.VertexHelper
import com.rendog.client.util.graphics.font.FontRenderAdapter
import com.rendog.client.util.math.Vec2f

/**
 * Window with rectangle and title rendering
 */
open class TitledWindow(
    name: String,
    posX: Float,
    posY: Float,
    width: Float,
    height: Float,
    settingGroup: SettingGroup
) : BasicWindow(name, posX, posY, width, height, settingGroup) {
    override val draggableHeight: Float get() = FontRenderAdapter.getFontHeight() + 5.0f

    override val minimizable get() = true

    override fun onRender(vertexHelper: VertexHelper, absolutePos: Vec2f) {
        super.onRender(vertexHelper, absolutePos)
        FontRenderAdapter.drawString(componentName, (width - FontRenderAdapter.getStringWidth(componentName)) / 2, 3.0f, color = GuiColors.text, drawShadow = CustomFont.shadow)
    }
}