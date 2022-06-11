package com.rendog.client.gui.rgui.windows

import com.rendog.client.module.modules.client.ClickGUI
import com.rendog.client.module.modules.client.GuiColors
import com.rendog.client.setting.GuiConfig
import com.rendog.client.setting.configs.AbstractConfig
import com.rendog.client.util.graphics.RenderUtils2D
import com.rendog.client.util.graphics.VertexHelper
import com.rendog.client.util.math.Vec2d
import com.rendog.client.util.math.Vec2f
import com.rendog.client.commons.interfaces.Nameable

/**
 * Window with rectangle rendering
 */
open class BasicWindow(
    name: String,
    posX: Float,
    posY: Float,
    width: Float,
    height: Float,
    settingGroup: SettingGroup,
    config: AbstractConfig<out Nameable> = GuiConfig
) : CleanWindow(name, posX, posY, width, height, settingGroup, config) {

    override fun onRender(vertexHelper: VertexHelper, absolutePos: Vec2f) {
        super.onRender(vertexHelper, absolutePos)
        RenderUtils2D.drawRoundedRectFilled(
            vertexHelper,
            Vec2d(0.0, 0.0),
            Vec2f(renderWidth, renderHeight).toVec2d(),
            ClickGUI.radius,
            color = GuiColors.backGround
        )
        if (ClickGUI.windowOutline) {
            RenderUtils2D.drawRoundedRectOutline(
                vertexHelper,
                Vec2d(0.0, 0.0),
                Vec2f(renderWidth, renderHeight).toVec2d(),
                ClickGUI.radius,
                lineWidth = ClickGUI.outlineWidth,
                color = GuiColors.outline
            )
        }
    }

}