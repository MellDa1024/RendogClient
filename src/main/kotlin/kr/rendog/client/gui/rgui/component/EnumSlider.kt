package kr.rendog.client.gui.rgui.component

import kr.rendog.client.commons.extension.readableName
import kr.rendog.client.module.modules.client.ClickGUI
import kr.rendog.client.module.modules.client.CustomFont
import kr.rendog.client.module.modules.client.GuiColors
import kr.rendog.client.setting.settings.impl.primitive.EnumSetting
import kr.rendog.client.util.graphics.VertexHelper
import kr.rendog.client.util.graphics.font.FontRenderAdapter
import kr.rendog.client.util.math.Vec2f
import kotlin.math.floor

class EnumSlider(val setting: EnumSetting<*>) : Slider(setting.name, 0.0, setting.description, setting.visibility) {
    private val enumValues = setting.enumValues

    override val isBold
        get() = setting.isModified && ClickGUI.showModifiedInBold

    override fun onRelease(mousePos: Vec2f, buttonId: Int) {
        super.onRelease(mousePos, buttonId)
        if (prevState != MouseState.DRAG) setting.nextValue()
    }

    override fun onDrag(mousePos: Vec2f, clickPos: Vec2f, buttonId: Int) {
        super.onDrag(mousePos, clickPos, buttonId)
        updateValue(mousePos)
    }

    private fun updateValue(mousePos: Vec2f) {
        value = (mousePos.x / width).toDouble()
        setting.setValue(enumValues[roundInput(value)].name)
    }

    private fun roundInput(valueIn: Double) = floor(valueIn * enumValues.size).toInt().coerceIn(0, enumValues.size - 1)

    override fun onRender(vertexHelper: VertexHelper, absolutePos: Vec2f) {
        if (mouseState != MouseState.DRAG) {
            val settingValue = setting.value.ordinal
            if (roundInput(value) != settingValue) {
                value = (settingValue + settingValue / (enumValues.size - 1.0)) / enumValues.size.toDouble()
            }
        }
        val valueText = setting.value.readableName()
        protectedWidth = FontRenderAdapter.getStringWidth(valueText, 0.75f).toDouble()

        super.onRender(vertexHelper, absolutePos)
        val posX = (renderWidth - protectedWidth - 2.0f).toFloat()
        val posY = renderHeight - 2.0f - FontRenderAdapter.getFontHeight(0.75f)
        FontRenderAdapter.drawString(valueText, posX, posY, color = GuiColors.text, drawShadow = CustomFont.shadow, scale = 0.75f)
    }
}