package com.rendog.client.gui.hudgui.elements.combat

import com.rendog.client.event.SafeClientEvent
import com.rendog.client.event.listener.events.PacketEvent
import com.rendog.client.gui.hudgui.HudElement
import com.rendog.client.manager.managers.RendogCDManager
import com.rendog.client.module.modules.client.GuiColors
import com.rendog.client.util.graphics.GlStateUtils
import com.rendog.client.util.graphics.RenderUtils2D
import com.rendog.client.util.graphics.VertexHelper
import com.rendog.client.util.graphics.font.HAlign
import com.rendog.client.util.math.Vec2d
import com.rendog.client.util.rendog.CoolDownType
import com.rendog.client.util.rendog.PlayerWeaponCD
import com.rendog.client.util.text.Color.deColorize
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
import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Pattern
import kotlin.math.roundToInt

internal object RPGCoolDown : HudElement(
    name = "RPGCoolDown",
    category = Category.COMBAT,
    description = "Show Weapon's Cooldown",
) {
    private val method by setting("Method", Method.Both, description = "What kind of method to manage cooldown.\nClick Method Info for more information.")

    private val horizontal by setting("Horizontal", true)
    private val background by setting("BackGround", true)
    private val alpha by setting("Alpha", 150, 0..255, 1, { background })
    private val extension by setting("Extension", 0, 0..3, 1, unit = " line")
    private val chatDelay by setting("Chat-Detection Delay", 150, 0..500, 10, { method != Method.DataBase }, description = "How many ms to client wait for detect next leftclick information, it the value is too small, it could cause overwriting other weapon's cooldown.")
    private val colorCode by setting("Colored CoolDown", true)
    private var information by setting("Method Info", false, description = "Shows Each Methods Difference in chat.", consumer = { _, _ ->
        mc.soundHandler.playSound(PositionedSoundRecord.getRecord(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f))
        MessageSendHelper.sendWarningMessage("§f§l[ §r§b§lRPGTimer Method Information §r§f§l]")
        MessageSendHelper.sendWarningMessage("§6§lDataBase :§r brings CoolDown data from plugin's Database.\nThe item which isn't in a database will be ignored.\nAlso, it will have a little cooldown desync with server.")
        MessageSendHelper.sendWarningMessage("§6§lChat :§r brings CoolDown data from RendogServer's Chat,\nAble to display any item's cooldown,\nbut should turn on the setting §o'Show Skill Cooldown'§r\nand click your item twice.")
        MessageSendHelper.sendWarningMessage("§6§lBoth :§r Use Both Method at the Same time.")
        false
    })

    private enum class Method {
        DataBase, Chat, Both
    }

    private var lastSwapTime = currentTimeMillis()
    private var invItemCD = ConcurrentHashMap<String, PlayerWeaponCD>() //left, right
    private var firstOpen = true
    private var rightClickChat = ""
    private var leftClickChat = ""
    private var moonlightName = ""
    private var lastSlot = 0
    private val cdPattern = Pattern.compile("^ {3}\\[ RD ] {3}재사용 대기시간이 ([0-9.]*)초 남았습니다.")
    private val cdMinPattern = Pattern.compile("^ {3}\\[ RD ] {3}재사용 대기시간이 ([0-9]*)분 ([0-9.]*)초 남았습니다.")

    init {
        relativePosX = 0.0f
        relativePosY = 0.0f
        dockingH = HAlign.CENTER
    }

    init {
        safeListener<TickEvent.ClientTickEvent> {
            if (firstOpen) {
                invItemCD.clear()
                runSafe { updateWeaponInv() }
                firstOpen = false
            }
        }

        safeListener<TickEvent.ClientTickEvent> {
            if (lastSlot != player.inventory.currentItem) lastSwapTime = currentTimeMillis()
            lastSlot = player.inventory.currentItem
        }

        safeListener<ClientChatReceivedEvent> { //moonlight
            if (moonlightName == "") return@safeListener
            if (it.message.unformattedText.trim() == "문라이트가 영혼을 방출합니다!") {
                updateWeaponCoolDown(moonlightName, CoolDownType.RIGHT, RendogCDManager.getCD(moonlightName, CoolDownType.RIGHT))
                moonlightName = ""
            }
        }

        safeListener<ClientChatReceivedEvent> { //rightClick
            if (rightClickChat == "") return@safeListener
            if (!it.message.formattedText.contains("재사용 대기시간이 §r§c§l")) {
                rightClickChat = ""
                return@safeListener
            }

            val patternedMessage = cdPattern.matcher(it.message.unformattedText.deColorize())
            val patternedMessage2 = cdMinPattern.matcher(it.message.unformattedText.deColorize())

            if (patternedMessage.find()) {
                updateWeaponCoolDown(rightClickChat, CoolDownType.RIGHT, patternedMessage.group(1).toDouble())
            } else if (patternedMessage2.find()) {
                val value = patternedMessage2.group(2).toDouble() + patternedMessage2.group(1).toDouble() * 60
                updateWeaponCoolDown(rightClickChat, CoolDownType.RIGHT, value)
            }

            rightClickChat = ""
        }

        safeListener<ClientChatReceivedEvent> { //leftClick
            if (leftClickChat == "") return@safeListener
            if (!it.message.formattedText.contains("재사용 대기시간이 §r§e§l")) {
                leftClickChat = ""
                return@safeListener
            }
            if (currentTimeMillis() - lastSwapTime <= chatDelay.toLong()) {
                leftClickChat = ""
                return@safeListener
            }

            val patternedMessage = cdPattern.matcher(it.message.unformattedText.deColorize())
            val patternedMessage2 = cdMinPattern.matcher(it.message.unformattedText.deColorize())

            if (patternedMessage.find()) {
                updateWeaponCoolDown(leftClickChat, CoolDownType.LEFT, patternedMessage.group(1).toDouble())
            } else if (patternedMessage2.find()) {
                val value = patternedMessage2.group(2).toDouble() + patternedMessage2.group(1).toDouble() * 60
                updateWeaponCoolDown(leftClickChat, CoolDownType.LEFT, value)
            }

            leftClickChat = ""
        }

        safeListener<PacketEvent.Send> { event -> //rightClick
            if (event.packet !is CPacketPlayerTryUseItem) return@safeListener
            if (firstOpen) return@safeListener

            val item = player.inventory.getCurrentItem()

            if (!invItemCD.containsKey(item.displayName)) item.addWeaponCDData()

            if (method != Method.DataBase) chatDetectionUpdate(CoolDownType.RIGHT, player.inventory.getCurrentItem().displayName)
            if (method == Method.Chat) return@safeListener

            if (checkVillageAndValidation(item)) {
                if (item.displayName.contains("문라이트") && !item.displayName.contains("초월")) {
                    moonlightName = item.displayName
                } else if ((invItemCD[item.displayName]!!.rightCD - currentTimeMillis()) <= 0) {
                    updateWeaponCoolDown(item.displayName, CoolDownType.RIGHT, RendogCDManager.getCD(item.displayName, CoolDownType.RIGHT))
                }
            }
        }

        safeListener<PacketEvent.Send> { event -> //leftClick
            if (event.packet !is CPacketAnimation) return@safeListener
            if (firstOpen) return@safeListener

            if (event.packet.hand != EnumHand.MAIN_HAND) return@safeListener
            val item = player.inventory.getCurrentItem()

            if (!invItemCD.containsKey(item.displayName)) item.addWeaponCDData()

            if (method != Method.DataBase) {
                if (currentTimeMillis() - lastSwapTime >= chatDelay.toLong()) {
                    chatDetectionUpdate(CoolDownType.LEFT, player.inventory.getCurrentItem().displayName)
                } else chatDetectionUpdate(CoolDownType.LEFT, "")
            }
            if (method == Method.Chat) return@safeListener

            if (checkVillageAndValidation(item)) {
                if ((invItemCD[player.inventory.getCurrentItem().displayName]!!.leftCD - currentTimeMillis()) <= 0) {
                    updateWeaponCoolDown(item.displayName, CoolDownType.LEFT, RendogCDManager.getCD(item.displayName, CoolDownType.LEFT))
                }
            }
        }
    }

    private fun updateWeaponCoolDown(weaponName: String, coolDownType: CoolDownType, value: Double) {
        when (coolDownType) {
            CoolDownType.RIGHT -> invItemCD[weaponName]?.rightCD = currentTimeMillis() + (1000 * value).toLong()
            CoolDownType.LEFT -> invItemCD[weaponName]?.leftCD = currentTimeMillis() + (1000 * value).toLong()
        }
    }

    private fun SafeClientEvent.updateWeaponInv() {
        for (i in 0..36) {
            val item = player.inventory.getStackInSlot(i)
            if (!invItemCD.containsKey(item.displayName)) {
                item.addWeaponCDData()
            }
        }
    }

    private fun ItemStack.addWeaponCDData() {
        invItemCD[this.displayName] = PlayerWeaponCD(currentTimeMillis(), currentTimeMillis())
    }

    private fun SafeClientEvent.checkVillageAndValidation(item: ItemStack): Boolean {
        return ((player.world.spawnPoint != BlockPos(278, 11, -134)) ||
            ((player.world.spawnPoint == BlockPos(278, 11, -134)) && RendogCDManager.isAbleInVillage(item.displayName)))
    }

    private fun chatDetectionUpdate(coolDownType: CoolDownType, itemName: String) {
        when (coolDownType) {
            CoolDownType.RIGHT -> rightClickChat = itemName
            CoolDownType.LEFT -> leftClickChat = itemName
        }
    }

    private fun convert2Min(time: Double): String {
        val minute = (time / 60).toInt()
        val second = (time.roundToInt() % 60)
        return if (second < 10) "$minute:0$second"
        else "$minute:$second"
    }


    override val hudWidth: Float
        get() = if (horizontal) 180.0f
        else (20.0f * (extension + 1))

    override val hudHeight: Float
        get() = if (horizontal) (20.0f * (extension + 1))
        else 180.0f

    override fun renderHud(vertexHelper: VertexHelper) {
        if (background) drawFrame(vertexHelper)
        GlStateManager.pushMatrix()
        if (invItemCD.isNotEmpty() && !firstOpen) {
            if (extension != 0) for (i in (4 - extension) * 9 until 36) drawItem(i)
            for (i in 0..8) drawItem(i)
            GlStateManager.popMatrix()
        }
    }

    private fun drawItem(slot: Int) {
        runSafe {
            val item = player.inventory.getStackInSlot(slot)
            if (invItemCD.containsKey(item.displayName)) {
                val rightcoold = ((invItemCD[item.displayName]!!.rightCD - currentTimeMillis()).toDouble() / 100.0).roundToInt() / 10.0
                if (rightcoold <= 0) drawItem(item, 2, 2, "")
                else if (rightcoold > 60) {
                    if (colorCode) drawItem(item, 2, 2, "§c${convert2Min(rightcoold)}")
                    else drawItem(item, 2, 2, convert2Min(rightcoold))
                } else {
                    if (colorCode) drawItem(item, 2, 2, "§c$rightcoold")
                    else drawItem(item, 2, 2, "$rightcoold")
                }
                GlStateManager.translate(0.0f, -11.1f, 0.0f)
                val leftcoold = ((invItemCD[item.displayName]!!.leftCD - currentTimeMillis()).toDouble() / 100.0).roundToInt() / 10.0
                if (leftcoold <= 0) drawItem(item, 2, 2, "", true)
                else if (leftcoold > 60) {
                    if (colorCode) drawItem(item, 2, 2, "§e${convert2Min(leftcoold)}}", true)
                    else drawItem(item, 2, 2, convert2Min(leftcoold), true)
                } else {
                    if (colorCode) drawItem(item, 2, 2, "§e$leftcoold", true)
                    else drawItem(item, 2, 2, "$leftcoold", true)
                }
                GlStateManager.translate(0.0f, 11.1f, 0.0f)
                if (horizontal) GlStateManager.translate(20.0f, 0.0f, 0.0f)
                else GlStateManager.translate(0.0f, 20.0f, 0.0f)
            } else {
                drawItem(item, 2, 2, "")
                if (horizontal) GlStateManager.translate(20.0f, 0.0f, 0.0f)
                else GlStateManager.translate(0.0f, 20.0f, 0.0f)
            }
            if ((slot + 1) % 9 == 0) {
                if (horizontal) GlStateManager.translate(-180.0f, 20.0f, 0.0f)
                else GlStateManager.translate(20.0f, -180.0f, 0.0f)
            }
        }
    }

    private fun drawFrame(vertexHelper: VertexHelper) {
        if (horizontal) RenderUtils2D.drawRectFilled(vertexHelper, posEnd = Vec2d(180.0, 20.0 * (extension + 1)), color = GuiColors.backGround.apply { a = alpha })
        else RenderUtils2D.drawRectFilled(vertexHelper, posEnd = Vec2d(20.0 * (extension + 1), 180.0), color = GuiColors.backGround.apply { a = alpha })
        if (horizontal) RenderUtils2D.drawRectOutline(vertexHelper, posEnd = Vec2d(180.0, 20.0 * (extension + 1)), lineWidth = 2.5F, color = GuiColors.outline.apply { a = alpha })
        else RenderUtils2D.drawRectOutline(vertexHelper, posEnd = Vec2d(20.0 * (extension + 1), 180.0), lineWidth = 2.5F, color = GuiColors.outline.apply { a = alpha })
    }

    private fun drawItem(itemStack: ItemStack, x: Int, y: Int, text: String? = null, invisibleItem: Boolean = false) {
        GlStateUtils.blend(true)
        GlStateUtils.depth(true)
        RenderHelper.enableGUIStandardItemLighting()

        mc.renderItem.zLevel = 0.0f
        if (!invisibleItem) mc.renderItem.renderItemAndEffectIntoGUI(itemStack, x, y)
        if (invisibleItem) renderItemOverlayIntoGUI(mc.fontRenderer, itemStack, x, y, text, false)
        else renderItemOverlayIntoGUI(mc.fontRenderer, itemStack, x, y, text)

        mc.renderItem.zLevel = 0.0f

        RenderHelper.disableStandardItemLighting()
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)

        GlStateUtils.depth(false)
        GlStateUtils.texture2d(true)
    }

    private fun renderItemOverlayIntoGUI(fr: FontRenderer, stack: ItemStack, xPosition: Int, yPosition: Int, text: String?, rightAlign: Boolean = true) {
        if (!stack.isEmpty) {
            if (stack.count != 1 || text != null) {
                val s = text ?: stack.count.toString()
                GlStateManager.disableLighting()
                GlStateManager.disableDepth()
                GlStateManager.disableBlend()
                val modifiedXPosition = if (rightAlign) {
                    (xPosition + 17 - fr.getStringWidth(s)).toFloat()
                } else {
                    (xPosition).toFloat()
                }
                fr.drawStringWithShadow(s, modifiedXPosition, (yPosition + 9).toFloat(), 16777215)
                GlStateManager.enableLighting()
                GlStateManager.enableDepth()
                GlStateManager.enableBlend()
            }
        }
    }
}