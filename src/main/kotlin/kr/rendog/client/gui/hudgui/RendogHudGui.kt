package kr.rendog.client.gui.hudgui

import kr.rendog.client.event.events.RenderOverlayEvent
import kr.rendog.client.event.listener.listener
import kr.rendog.client.gui.AbstractRendogGui
import kr.rendog.client.gui.hudgui.component.HudButton
import kr.rendog.client.gui.hudgui.window.HudSettingWindow
import kr.rendog.client.gui.rgui.Component
import kr.rendog.client.gui.rgui.windows.ListWindow
import kr.rendog.client.module.modules.client.ClickGUI
import kr.rendog.client.module.modules.client.Hud
import kr.rendog.client.module.modules.client.HudEditor
import kr.rendog.client.util.graphics.GlStateUtils
import kr.rendog.client.util.graphics.VertexHelper
import kr.rendog.client.util.math.Vec2f
import kr.rendog.client.util.threads.safeListener
import net.minecraftforge.fml.common.gameevent.InputEvent
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11.*
import java.util.*

object RendogHudGui : AbstractRendogGui<HudSettingWindow, AbstractHudElement>() {

    override val alwaysTicking = true
    private val hudWindows = EnumMap<AbstractHudElement.Category, ListWindow>(AbstractHudElement.Category::class.java)

    init {
        var posX = 0.0f

        AbstractHudElement.Category.values().forEach { category ->
            val window = ListWindow(category.displayName, posX, 0.0f, 90.0f, 300.0f, Component.SettingGroup.HUD_GUI)
            windowList.add(window)
            hudWindows[category] = window

            posX += 90.0f
        }

        listener<InputEvent.KeyInputEvent> {
            val eventKey = Keyboard.getEventKey()

            if (eventKey == Keyboard.KEY_NONE || Keyboard.isKeyDown(Keyboard.KEY_F3)) return@listener

            windowList
                .filterIsInstance<AbstractHudElement>()
                .filter { it.bind.isDown(eventKey) }
                .forEach { it.visible = !it.visible }
        }
    }

    override fun updateWindowOrder() {
        val cacheList = windowList.sortedBy { it.lastActiveTime + if (it is AbstractHudElement) 1000000 else 0 }
        windowList.clear()
        windowList.addAll(cacheList)
    }

    internal fun register(hudElement: AbstractHudElement) {
        val button = HudButton(hudElement)
        hudWindows[hudElement.category]?.add(button)
        windowList.add(hudElement)
    }

    internal fun unregister(hudElement: AbstractHudElement) {
        hudWindows[hudElement.category]?.children?.removeIf { it is HudButton && it.hudElement == hudElement }
        windowList.remove(hudElement)
    }

    override fun onGuiClosed() {
        super.onGuiClosed()
        setHudButtonVisibility { true }
    }

    override fun newSettingWindow(element: AbstractHudElement, mousePos: Vec2f): HudSettingWindow {
        return HudSettingWindow(element, mousePos.x, mousePos.y)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (keyCode == Keyboard.KEY_ESCAPE ||
            (keyCode == ClickGUI.bind.value.key ||
                keyCode == HudEditor.bind.value.key)
            && !searching && settingWindow?.listeningChild == null) {
            HudEditor.disable()
        } else {
            super.keyTyped(typedChar, keyCode)

            val string = typedString.replace(" ", "")

            if (string.isNotEmpty()) {
                setHudButtonVisibility { hudButton ->
                    hudButton.hudElement.componentName.contains(string, true)
                        || hudButton.hudElement.alias.any { it.contains(string, true) }
                }
            } else {
                setHudButtonVisibility { true }
            }
        }
    }

    private fun setHudButtonVisibility(function: (HudButton) -> Boolean) {
        windowList.filterIsInstance<ListWindow>().forEach { window ->
            window.children.filterIsInstance<HudButton>().forEach { button ->
                button.visible = function(button)
            }
        }
    }

    init {
        safeListener<RenderOverlayEvent>(0) {
            if (Hud.isDisabled || mc.currentScreen is RendogHudGui) return@safeListener

            val vertexHelper = VertexHelper(GlStateUtils.useVbo())
            GlStateUtils.rescaleRendog()

            windowList
                .filterIsInstance<AbstractHudElement>()
                .filter { it.visible }
                .forEach { window ->
                    renderHudElement(vertexHelper, window)
                }

            GlStateUtils.rescaleMc()
            GlStateUtils.depth(true)
        }
    }

    private fun renderHudElement(vertexHelper: VertexHelper, window: AbstractHudElement) {
        window.updatePrevPos()
        window.updatePrevSize()
        glPushMatrix()
        glTranslatef(window.renderPosX, window.renderPosY, 0.0f)

        if (Hud.hudFrame) window.renderFrame(vertexHelper)

        glScalef(window.scale, window.scale, window.scale)
        window.renderHud(vertexHelper)

        glPopMatrix()
    }

}