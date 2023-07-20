package kr.rendog.client.gui.hudgui.elements.player

import kr.rendog.client.event.SafeClientEvent
import kr.rendog.client.event.events.ConnectionEvent
import kr.rendog.client.event.events.PacketEvent
import kr.rendog.client.gui.hudgui.HudElement
import kr.rendog.client.mixin.extension.windowID
import kr.rendog.client.util.color.ColorHolder
import kr.rendog.client.util.graphics.GlStateUtils
import kr.rendog.client.util.graphics.RenderUtils2D
import kr.rendog.client.util.graphics.VertexHelper
import kr.rendog.client.util.graphics.font.HAlign
import kr.rendog.client.util.items.storageSlots
import kr.rendog.client.util.math.Vec2d
import kr.rendog.client.util.threads.runSafe
import kr.rendog.client.util.threads.safeListener
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.init.Blocks
import net.minecraft.inventory.ContainerChest
import net.minecraft.inventory.InventoryBasic
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.CPacketCloseWindow
import net.minecraft.network.play.server.SPacketOpenWindow
import net.minecraft.util.ResourceLocation
import net.minecraft.util.text.TextComponentTranslation
import org.lwjgl.opengl.GL11.*

internal object InventoryViewer : HudElement(
    name = "InventoryViewer",
    category = Category.PLAYER,
    description = "Items in Inventory"
) {
    private val enderChest by setting("Inventory", SlotType.PLAYER)
    private val mcTexture by setting("Minecraft Texture", false)
    private val showIcon by setting("Show Icon", false, { !mcTexture })
    private val iconScale by setting("Icon Scale", 0.5f, 0.1f..1.0f, 0.1f, { !mcTexture && showIcon })
    private val background by setting("Background", true, { !mcTexture })
    private val backgroundColor by setting("Background Color", ColorHolder(0, 0, 0, 150), visibility = { !mcTexture && background })
    private val outline by setting("Outline", true, visibility = { !mcTexture })
    private val outlineColor by setting("Outline Color", ColorHolder(80, 115, 220, 75), visibility = { !mcTexture && outline })
    private val outlineThickness by setting("Outline Thickness", 1.0f, 0.5f..5.0f, 0.5f, { !mcTexture && outline })
    private val containerTexture = ResourceLocation("textures/gui/container/inventory.png")
    private val rendogIcon = ResourceLocation("rendog/rendog_icon.png")
    private var enderChestContents: MutableList<ItemStack> = MutableList(27) { ItemStack(Blocks.AIR) }

    override val hudWidth: Float = 162.0f
    override val hudHeight: Float = 54.0f

    private var openedEnderChest: Int = -1

    init {
        relativePosX = 0.0f
        relativePosY = 0.0f
        dockingH = HAlign.CENTER
    }

    override fun renderHud(vertexHelper: VertexHelper) {
        super.renderHud(vertexHelper)
        runSafe {
            drawFrame(vertexHelper)
            drawFrameTexture()
            drawItems()
        }
    }

    private fun drawFrame(vertexHelper: VertexHelper) {
        if (!mcTexture) {
            if (background) {
                RenderUtils2D.drawRectFilled(vertexHelper, posEnd = Vec2d(hudWidth.toDouble(), hudHeight.toDouble()), color = backgroundColor)
            }
            if (outline) {
                RenderUtils2D.drawRectOutline(vertexHelper, posEnd = Vec2d(hudWidth.toDouble(), hudHeight.toDouble()), lineWidth = outlineThickness, color = outlineColor)
            }
        }
    }

    private fun drawFrameTexture() {
        val tessellator = Tessellator.getInstance()
        val buffer = tessellator.buffer
        GlStateUtils.texture2d(true)

        if (mcTexture) {
            mc.renderEngine.bindTexture(containerTexture)
            buffer.begin(GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX)
            buffer.pos(0.0, 0.0, 0.0).tex(0.02734375, 0.32421875).endVertex() // (7 / 256), (83 / 256)
            buffer.pos(0.0, hudHeight.toDouble(), 0.0).tex(0.02734375, 0.53125).endVertex() // (7 / 256), (136 / 256)
            buffer.pos(hudWidth.toDouble(), 0.0, 0.0).tex(0.65625, 0.32421875).endVertex() // (168 / 256), (83 / 256)
            buffer.pos(hudWidth.toDouble(), hudHeight.toDouble(), 0.0).tex(0.65625, 0.53125).endVertex() // (168 / 256), (136 / 256)
            tessellator.draw()
        } else if (showIcon) {
            mc.renderEngine.bindTexture(rendogIcon)
            GlStateManager.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)

            val center = Vec2d(hudWidth / 2.0, hudHeight / 2.0)
            val halfWidth = iconScale * 50.0
            val halfHeight = iconScale * 50.0

            buffer.begin(GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX)
            buffer.pos(center.x - halfWidth, center.y - halfHeight, 0.0).tex(0.0, 0.0).endVertex()
            buffer.pos(center.x - halfWidth, center.y + halfHeight, 0.0).tex(0.0, 1.0).endVertex()
            buffer.pos(center.x + halfWidth, center.y - halfHeight, 0.0).tex(1.0, 0.0).endVertex()
            buffer.pos(center.x + halfWidth, center.y + halfHeight, 0.0).tex(1.0, 1.0).endVertex()
            tessellator.draw()

            GlStateManager.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
        }
    }

    init {
        safeListener<ConnectionEvent.Disconnect> {
            openedEnderChest = -1
        }

        safeListener<PacketEvent.Receive> {
            if (it.packet !is SPacketOpenWindow) return@safeListener
            if (it.packet.guiId != "minecraft:container") return@safeListener
            val title = it.packet.windowTitle
            if (title !is TextComponentTranslation) return@safeListener
            if (title.key != "container.enderchest") return@safeListener

            openedEnderChest = it.packet.windowId
        }

        safeListener<PacketEvent.PostSend> {
            if (it.packet !is CPacketCloseWindow) return@safeListener
            if (it.packet.windowID != openedEnderChest) return@safeListener

            checkEnderChest()
            openedEnderChest = -1
        }
    }

    private fun checkEnderChest() {
        val guiScreen = mc.currentScreen

        if (guiScreen !is GuiContainer) return

        val container = guiScreen.inventorySlots

        if (container is ContainerChest && container.lowerChestInventory is InventoryBasic) {
            if (container.windowId == openedEnderChest) {
                for (i in 0..26) enderChestContents[i] = container.inventory[i]
            }
        }
    }

    private fun SafeClientEvent.drawItems() {
        if (enderChest == SlotType.ENDER_CHEST) {
            for ((index, stack) in enderChestContents.withIndex()) {
                if (stack.isEmpty) continue
                val slotX = index % 9 * (hudWidth / 9.0) + 1
                val slotY = index / 9 * (hudWidth / 9.0) + 1
                RenderUtils2D.drawItem(stack, slotX.toInt(), slotY.toInt())
            }
        } else {
            for ((index, slot) in player.storageSlots.withIndex()) {
                val itemStack = slot.stack
                if (itemStack.isEmpty) continue

                val slotX = index % 9 * (hudWidth / 9.0) + 1
                val slotY = index / 9 * (hudWidth / 9.0) + 1

                RenderUtils2D.drawItem(itemStack, slotX.toInt(), slotY.toInt())
            }
        }
    }

    private enum class SlotType {
        PLAYER, ENDER_CHEST
    }
}