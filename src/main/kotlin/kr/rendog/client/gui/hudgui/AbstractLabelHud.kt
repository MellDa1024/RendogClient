package kr.rendog.client.gui.hudgui

import kr.rendog.client.commons.interfaces.Nameable
import kr.rendog.client.event.SafeClientEvent
import kr.rendog.client.module.modules.client.Hud
import kr.rendog.client.module.modules.client.HudEditor
import kr.rendog.client.setting.configs.AbstractConfig
import kr.rendog.client.util.graphics.VertexHelper
import kr.rendog.client.util.graphics.font.TextComponent
import kr.rendog.client.util.math.Vec2d
import kr.rendog.client.util.threads.safeAsyncListener
import net.minecraftforge.fml.common.gameevent.TickEvent

abstract class AbstractLabelHud(
    name: String,
    alias: Array<String>,
    category: Category,
    description: String,
    alwaysListening: Boolean,
    enabledByDefault: Boolean,
    config: AbstractConfig<out Nameable>,
    separator: String = " ",
) : AbstractHudElement(name, alias, category, description, alwaysListening, enabledByDefault, config) {

    override val hudWidth: Float get() = displayText.getWidth() + 2.0f
    override val hudHeight: Float get() = displayText.getHeight(2)
    protected val displayText = TextComponent(separator)

    init {
        safeAsyncListener<TickEvent.ClientTickEvent> {
            if (it.phase != TickEvent.Phase.END) return@safeAsyncListener
            displayText.clear()
            updateText()

            if (displayText.isEmpty() && HudEditor.isEnabled) displayText.addLine(name, primaryColor)
        }
    }

    abstract fun SafeClientEvent.updateText()

    override fun renderHud(vertexHelper: VertexHelper) {
        super.renderHud(vertexHelper)

        val textPosX = width * dockingH.multiplier / scale - dockingH.offset
        val textPosY = height * dockingV.multiplier / scale

        displayText.draw(
            Vec2d(textPosX.toDouble(), textPosY.toDouble()),
            horizontalAlign = dockingH,
            verticalAlign = dockingV,
            drawShadow = Hud.textShadow
        )
    }

}