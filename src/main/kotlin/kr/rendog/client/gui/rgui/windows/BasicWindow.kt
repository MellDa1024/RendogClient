package kr.rendog.client.gui.rgui.windows

import kr.rendog.client.commons.interfaces.Nameable
import kr.rendog.client.module.modules.client.ClickGUI
import kr.rendog.client.module.modules.client.GuiColors
import kr.rendog.client.setting.GuiConfig
import kr.rendog.client.setting.configs.AbstractConfig
import kr.rendog.client.util.graphics.RenderUtils2D
import kr.rendog.client.util.graphics.VertexHelper
import kr.rendog.client.util.math.Vec2d
import kr.rendog.client.util.math.Vec2f

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