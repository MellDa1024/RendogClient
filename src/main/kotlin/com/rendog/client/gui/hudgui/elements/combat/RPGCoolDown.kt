package com.rendog.client.gui.hudgui.elements.combat

import com.rendog.client.gui.hudgui.HudElement
import com.rendog.client.event.listener.events.PacketEvent
import com.rendog.client.manager.managers.RendogCDManager
import com.rendog.client.module.modules.client.GuiColors
import com.rendog.client.util.graphics.GlStateUtils
import com.rendog.client.util.graphics.RenderUtils2D
import com.rendog.client.util.graphics.VertexHelper
import com.rendog.client.util.math.Vec2d
import com.rendog.client.util.text.MessageSendHelper
import com.rendog.client.util.threads.runSafe
import com.rendog.client.util.threads.safeListener
import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.init.SoundEvents
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.CPacketAnimation
import net.minecraft.network.play.client.CPacketPlayerTryUseItem
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.lang.System.currentTimeMillis
import java.util.regex.Pattern
import kotlin.math.roundToInt

internal object RPGCoolDown : HudElement(
    name = "RPGCoolDown",
    category = Category.COMBAT,
    description = "Show Weapon's Cooldown"
) {
    private val method by setting("Method", Method.DataBase, description = "What kind of method to manage cooldown.\nClick Method Info for more information.")

    private val horizontal by setting("Horizontal", true)
    private val background by setting("BackGround", true)
    private val alpha by setting("Alpha", 150, 0..255, 1, { background })
    private val colorcode by setting("Colored Cooldown", true)
    private var information by setting("Method Info", true, description = "Shows Each Methods Difference in chat.")

    private enum class Method{
        DataBase, Chat, Both
    }

    data class VarPair(var first: Long, var second: Long)

    private var itemcd = mutableMapOf<String, VarPair>() //left, right
    private var firstopen = true
    private var rightclickchat = ""
    private var leftclickchat = ""
    private val cdpattern = Pattern.compile(" {3}\\[ RD ] {3}재사용 대기시간이 ([0-9.]*)초 남았습니다.")

    init {
        safeListener<TickEvent.ClientTickEvent> {
            if (firstopen) {
                itemcd.clear()
                runSafe {
                    for (i in 0..36) {
                        val item = player.inventory.getStackInSlot(i)
                        if (!itemcd.containsKey(item.displayName)) {
                            itemcd[item.displayName] = VarPair(currentTimeMillis(), currentTimeMillis())
                        }
                    }
                }
                firstopen = false
            }

            if (information) {
                information = false
                mc.soundHandler.playSound(PositionedSoundRecord.getRecord(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f))
                MessageSendHelper.sendWarningMessage("§f§l[ §r§b§lRPGTimer Method Information §r§f§l]")
                MessageSendHelper.sendWarningMessage("§6§lDataBase :§r brings CoolDown data from plugin's Database.\nThe item which isn't in a database will be ignored.\nAlso, it will have a little cooldown desync with server.")
                MessageSendHelper.sendWarningMessage("§6§lChat :§r brings CoolDown data from RendogServer's Chat,\nAble to display any item's cooldown,\nbut should turn on the setting §o'Show Skill Cooldown'§r\nand click your item twice.")
                MessageSendHelper.sendWarningMessage("§6§lBoth :§r Use Both Method at the Same time.")
            }
        }

        safeListener<ClientChatReceivedEvent> { //rightclick
            if (rightclickchat == "") return@safeListener
            if (!it.message.formattedText.contains("재사용 대기시간이 §r§c§l")) {
                rightclickchat = ""
                return@safeListener
            }
            val patternedmassage = cdpattern.matcher(RendogCDManager.removecolorcode(it.message.unformattedText))
            if(patternedmassage.find()) {
                patternedmassage.group(1).toFloat()
                itemcd[player.inventory.getCurrentItem().displayName]!!.second = (currentTimeMillis() + (1000 * patternedmassage.group(1).toFloat()).toLong())
                rightclickchat = ""
            }
        }

        safeListener<ClientChatReceivedEvent> { //leftclick
            if (leftclickchat == "") return@safeListener
            if (!it.message.formattedText.contains("재사용 대기시간이 §r§e§l")) {
                leftclickchat = ""
                return@safeListener
            }
            val patternedmassage = cdpattern.matcher(RendogCDManager.removecolorcode(it.message.unformattedText))
            if(patternedmassage.find()) {
                patternedmassage.group(1).toFloat()
                itemcd[player.inventory.getCurrentItem().displayName]!!.first = (currentTimeMillis() + (1000 * patternedmassage.group(1).toFloat()).toLong())
                leftclickchat = ""
            }
        }

        safeListener<PacketEvent.Send> { event -> //rightclick
            if (event.packet !is CPacketPlayerTryUseItem) return@safeListener
            if (firstopen) return@safeListener
            if (!itemcd.containsKey(player.inventory.getCurrentItem().displayName)) {
                for (i in 0..36) {
                    val item = player.inventory.getStackInSlot(i)
                    if (!itemcd.containsKey(item.displayName)) {
                        itemcd[item.displayName] = VarPair(currentTimeMillis(), currentTimeMillis())
                    }
                }
            }
            if (method == Method.Chat) chatdetectupdate(true, player.inventory.getCurrentItem().displayName)
            else {
                if (method == Method.Both) chatdetectupdate(true, player.inventory.getCurrentItem().displayName)
                if ((player.world.spawnPoint != BlockPos(278,11, -134)) || ((player.world.spawnPoint == BlockPos(278,11, -134)) && RendogCDManager.isableinvillage(player.inventory.getCurrentItem().displayName))) { //village
                    if ((itemcd[player.inventory.getCurrentItem().displayName]!!.second - currentTimeMillis()) <= 0) {
                        itemcd[player.inventory.getCurrentItem().displayName]!!.second = (currentTimeMillis() + 1000 * RendogCDManager.getCD(player.inventory.getCurrentItem().displayName))
                    }
                }

            }
        }

        safeListener<PacketEvent.Send> { event -> //leftclick
            if (event.packet !is CPacketAnimation) return@safeListener
            if (firstopen) return@safeListener
            if (event.packet.hand != EnumHand.MAIN_HAND) return@safeListener
            if (!itemcd.containsKey(player.inventory.getCurrentItem().displayName)) {
                for (i in 0..36) {
                    val item = player.inventory.getStackInSlot(i)
                    if (!itemcd.containsKey(item.displayName)) {
                        itemcd[item.displayName] = VarPair(currentTimeMillis(), currentTimeMillis())
                    }
                }
            }
            if (method == Method.Chat) chatdetectupdate(false, player.inventory.getCurrentItem().displayName)
            else {
                if (method == Method.Both) chatdetectupdate(false, player.inventory.getCurrentItem().displayName)
                if ((player.world.spawnPoint != BlockPos(278,11, -134)) || ((player.world.spawnPoint == BlockPos(278,11, -134)) && RendogCDManager.isableinvillage(player.inventory.getCurrentItem().displayName))) { //village
                    if ((itemcd[player.inventory.getCurrentItem().displayName]!!.first - currentTimeMillis()) <= 0) {
                        itemcd[player.inventory.getCurrentItem().displayName]!!.first = (currentTimeMillis() + 1000 * RendogCDManager.getCD(player.inventory.getCurrentItem().displayName, false))
                    }
                }

            }
        }
    }

    override val hudWidth: Float
        get() = if (horizontal) 180.0f
        else 20.0f

    override val hudHeight: Float
        get() = if (horizontal) 20.0f
        else 180.0f

    override fun renderHud(vertexHelper: VertexHelper) {
         if (background) drawFrame(vertexHelper)
         GlStateManager.pushMatrix()
         if (itemcd.isNotEmpty() && !firstopen) {
             for (i in 0..8) {
                 runSafe {
                     val item = player.inventory.getStackInSlot(i)
                     if (itemcd.containsKey(item.displayName)) {
                         val rightcoold = ((itemcd[item.displayName]!!.second - currentTimeMillis()).toDouble()/100.0).roundToInt()/10.0
                         if (rightcoold <=0) drawItem(item, 2, 2, "")
                         else {
                             if (colorcode) drawItem(item, 2, 2, "§c$rightcoold")
                             else drawItem(item, 2, 2, "$rightcoold")
                         }
                         GlStateManager.translate(0.0f, -11.1f, 0.0f)
                         val leftcoold = ((itemcd[item.displayName]!!.first - currentTimeMillis()).toDouble()/100.0).roundToInt()/10.0
                         if (leftcoold <=0) drawItem(item, 2, 2, "", true)
                         else {
                             if (colorcode) drawItem(item, 2, 2, "§e$leftcoold", true)
                             else drawItem(item, 2, 2, "$leftcoold", true)
                         }
                         GlStateManager.translate(0.0f, 11.1f, 0.0f)
                         if (horizontal) GlStateManager.translate(20.0f, 0.0f, 0.0f)
                         else GlStateManager.translate(0.0f, 20.0f, 0.0f)
                     }
                     else {
                         drawItem(item, 2, 2, "")
                         if (horizontal) GlStateManager.translate(20.0f, 0.0f, 0.0f)
                         else GlStateManager.translate(0.0f, 20.0f, 0.0f)
                     }
                 }
             }
             GlStateManager.popMatrix()
         }
    }

    private fun drawFrame(vertexHelper: VertexHelper) {
        if (horizontal) RenderUtils2D.drawRectFilled(vertexHelper, posEnd = Vec2d(180.0, 20.0), color = GuiColors.backGround.apply { a = alpha })
        else RenderUtils2D.drawRectFilled(vertexHelper, posEnd = Vec2d(20.0, 180.0), color = GuiColors.backGround.apply { a = alpha })
        if (horizontal) RenderUtils2D.drawRectOutline(vertexHelper, posEnd = Vec2d(180.0, 20.0), lineWidth = 2.5F, color = GuiColors.outline.apply { a = alpha })
        else  RenderUtils2D.drawRectOutline(vertexHelper, posEnd = Vec2d(20.0, 180.0), lineWidth = 2.5F, color = GuiColors.outline.apply { a = alpha })
    }

    private fun chatdetectupdate(isrightclick : Boolean, itemname : String) {
        /* istrue = rightclick, !istrue = leftclick */
        if (isrightclick) {
            rightclickchat = itemname
            leftclickchat = ""
        } else {
            rightclickchat = ""
            leftclickchat = itemname
        }
    }

    private fun drawItem(itemStack: ItemStack, x: Int, y: Int, text: String? = null, invisibleitem: Boolean = false) {
        GlStateUtils.blend(true)
        GlStateUtils.depth(true)
        RenderHelper.enableGUIStandardItemLighting()

        mc.renderItem.zLevel = 0.0f
        if (!invisibleitem) mc.renderItem.renderItemAndEffectIntoGUI(itemStack, x, y)
        if (invisibleitem) renderItemOverlayIntoGUI(mc.fontRenderer, itemStack, x, y, text, false)
        else renderItemOverlayIntoGUI(mc.fontRenderer, itemStack, x, y, text)

        mc.renderItem.zLevel = 0.0f

        RenderHelper.disableStandardItemLighting()
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)

        GlStateUtils.depth(false)
        GlStateUtils.texture2d(true)
    }


    private fun renderItemOverlayIntoGUI(fr: FontRenderer, stack: ItemStack, xPosition: Int, yPosition: Int, text: String?, rightalign: Boolean = true) {
        if (!stack.isEmpty) {
            if (stack.count != 1 || text != null) {
                val s = text ?: stack.count.toString()
                GlStateManager.disableLighting()
                GlStateManager.disableDepth()
                GlStateManager.disableBlend()
                val modifiedxPosition = if (rightalign) {
                    (xPosition + 17 - fr.getStringWidth(s)).toFloat()
                } else {
                    (xPosition).toFloat()
                }
                fr.drawStringWithShadow(s, modifiedxPosition, (yPosition + 9).toFloat(), 16777215)
                GlStateManager.enableLighting()
                GlStateManager.enableDepth()
                GlStateManager.enableBlend()
            }
        }
    }
}