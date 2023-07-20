package kr.rendog.client.gui.hudgui

import kr.rendog.client.commons.interfaces.Alias
import kr.rendog.client.commons.interfaces.DisplayEnum
import kr.rendog.client.commons.interfaces.Nameable
import kr.rendog.client.event.RendogEventBus
import kr.rendog.client.gui.GuiManager
import kr.rendog.client.gui.rgui.windows.BasicWindow
import kr.rendog.client.module.modules.client.GuiColors
import kr.rendog.client.module.modules.client.Hud
import kr.rendog.client.setting.GuiConfig
import kr.rendog.client.setting.GuiConfig.setting
import kr.rendog.client.setting.configs.AbstractConfig
import kr.rendog.client.util.Bind
import kr.rendog.client.util.graphics.RenderUtils2D
import kr.rendog.client.util.graphics.VertexHelper
import kr.rendog.client.util.graphics.font.FontRenderAdapter
import kr.rendog.client.util.math.Vec2d
import kr.rendog.client.util.math.Vec2f
import kr.rendog.client.util.text.MessageSendHelper
import kr.rendog.client.util.threads.safeListener
import net.minecraft.client.gui.GuiChat
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.lwjgl.opengl.GL11.glScalef

abstract class AbstractHudElement(
    name: String,
    final override val alias: Array<String>,
    val category: Category,
    val description: String,
    val alwaysListening: Boolean,
    enabledByDefault: Boolean,
    config: AbstractConfig<out Nameable>
) : BasicWindow(name, 20.0f, 20.0f, 100.0f, 50.0f, SettingGroup.HUD_GUI, config), Alias {

    val bind by setting("Bind", Bind())
    val scale by setting("Scale", 1.0f, 0.1f..4.0f, 0.05f)
    val default = setting("Default", false)
    private val overridePrimaryColor by setting("Override Primary Color", false)
    private val overridePrimaryColorValue by setting("Override Primary Color Value", Hud.primaryColor, visibility = { overridePrimaryColor })
    private val overrideSecondaryColor by setting("Override Secondary Color", false)
    private val overrideSecondaryColorValue by setting("Override Secondary Color Value", Hud.secondaryColor, visibility = { overrideSecondaryColor })

    val primaryColor get() = if (overridePrimaryColor) overridePrimaryColorValue else Hud.primaryColor
    val secondaryColor get() = if (overrideSecondaryColor) overrideSecondaryColorValue else Hud.secondaryColor
    override val resizable = false

    final override val minWidth: Float get() = FontRenderAdapter.getFontHeight() * scale * 2.0f
    final override val minHeight: Float get() = FontRenderAdapter.getFontHeight() * scale

    final override val maxWidth: Float get() = hudWidth * scale
    final override val maxHeight: Float get() = hudHeight * scale

    open val hudWidth: Float get() = 20f
    open val hudHeight: Float get() = 10f

    val settingList get() = GuiConfig.getSettings(this)
    private var chatSnapping = false
    private val snappedElements = mutableListOf<AbstractHudElement>()
    private val chatSnapY = 15f

    init {
        safeListener<TickEvent.ClientTickEvent> { event ->
            if (event.phase != TickEvent.Phase.END || !visible) return@safeListener
            width = maxWidth
            height = maxHeight

            if (!Hud.chatSnap) return@safeListener

            val currentScreen = mc.currentScreen
            if (currentScreen is GuiChat && !chatSnapping) {
                val screenH = currentScreen.height
                if (posY >= screenH - height - 3 && yShift == 0.0f) {
                    val prevPosYSnap = posY
                    yShift = -chatSnapY
                    snappedElements.clear()
                    GuiManager.getHudElementOrNull(componentName)?.let { snappedElements.add(it) }
                    chatSnapCheck(componentName, prevPosYSnap, posX, posX + width)
                    chatSnapping = true
                }
            } else if (currentScreen !is GuiChat && chatSnapping) {
                yShift = 0.0f
                snappedElements.forEach {
                    it.yShift = 0.0f
                }
                snappedElements.clear()
                chatSnapping = false
            }
        }
    }

    private fun chatSnapCheck(thisElement: String, prevSnapY: Float, prevSnapXMin: Float, prevSnapXMax: Float) {
        for (element in GuiManager.hudElements) {
            if (!snappedElements.contains(element)
                && element.componentName != thisElement
                && element.visible
                && element.posY + element.height >= prevSnapY - 3
                && element.posX >= prevSnapXMin
                && element.posX <= prevSnapXMax) {
                snappedElements.add(element)
                chatSnapCheck(element.componentName, element.posY, element.posX, element.posX + element.width)
                element.yShift = -chatSnapY
            }
        }
    }

    override fun onReposition() {
        super.onReposition()
        if (Hud.collisionSnapping) {
            for (element in GuiManager.hudElements) {
                if (element.componentName != componentName && element.visible && element.posY + element.height >= posY && element.posY <= posY + height && element.posX + element.width >= posX && element.posX <= posX + width) {
                    if (posY + height / 2 <= element.posY + element.height / 2) {
                        posY = element.posY - height
                    } else {
                        posY = element.posY + element.height
                    }
                }
            }
        }
    }

    override fun onGuiInit() {
        super.onGuiInit()
        if (alwaysListening || visible) RendogEventBus.subscribe(this)
    }

    override fun onClosed() {
        super.onClosed()
        if (alwaysListening || visible) RendogEventBus.subscribe(this)
    }

    final override fun onTick() {
        super.onTick()
    }

    final override fun onRender(vertexHelper: VertexHelper, absolutePos: Vec2f) {
        super.onRender(vertexHelper, absolutePos)
        renderFrame(vertexHelper)
        glScalef(scale, scale, scale)
        renderHud(vertexHelper)
    }

    open fun renderHud(vertexHelper: VertexHelper) {}

    open fun renderFrame(vertexHelper: VertexHelper) {
        RenderUtils2D.drawRectFilled(vertexHelper, Vec2d(0.0, 0.0), Vec2f(renderWidth, renderHeight).toVec2d(), GuiColors.backGround)
        RenderUtils2D.drawRectOutline(vertexHelper, Vec2d(0.0, 0.0), Vec2f(renderWidth, renderHeight).toVec2d(), 1.5f, GuiColors.outline)
    }

    init {
        visibleSetting.valueListeners.add { _, it ->
            if (it) {
                RendogEventBus.subscribe(this)
                lastActiveTime = System.currentTimeMillis()
            } else if (!alwaysListening) {
                RendogEventBus.unsubscribe(this)
            }
        }

        default.valueListeners.add { _, it ->
            if (it) {
                settingList.filter { it != visibleSetting && it != default }.forEach {
                    it.resetValue()
                    updatePreDrag(null)
                }
                default.value = false
                MessageSendHelper.sendChatMessage("$name Set to defaults!")
            }
        }

        if (!enabledByDefault) visible = false
    }

    enum class Category(override val displayName: String) : DisplayEnum {
        CLIENT("Client"),
        COMBAT("Combat"),
        PLAYER("Player"),
        WORLD("World"),
        MISC("Misc")
    }

}