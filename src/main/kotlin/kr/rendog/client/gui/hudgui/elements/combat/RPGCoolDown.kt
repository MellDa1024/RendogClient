package kr.rendog.client.gui.hudgui.elements.combat

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kr.rendog.client.event.SafeClientEvent
import kr.rendog.client.event.events.PacketEvent
import kr.rendog.client.gui.hudgui.HudElement
import kr.rendog.client.manager.managers.WeaponCoolManager
import kr.rendog.client.module.modules.client.GuiColors
import kr.rendog.client.util.graphics.GlStateUtils
import kr.rendog.client.util.graphics.RenderUtils2D
import kr.rendog.client.util.graphics.VertexHelper
import kr.rendog.client.util.graphics.font.HAlign
import kr.rendog.client.util.math.Vec2d
import kr.rendog.client.util.rendog.CoolDownType
import kr.rendog.client.util.rendog.PlayerWeaponCD
import kr.rendog.client.util.text.Color.deColorize
import kr.rendog.client.util.text.MessageSendHelper
import kr.rendog.client.util.threads.defaultScope
import kr.rendog.client.util.threads.runSafe
import kr.rendog.client.util.threads.safeListener
import net.minecraft.client.audio.PositionedSoundRecord
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

    private val cdr by setting("Cooldown Reduction", 0.0, 0.0..100.0, 1.0, unit = "%")
    private val horizontal by setting("Horizontal", true)
    private val background by setting("BackGround", true)
    private val alpha by setting("Alpha", 150, 0..255, 1, { background })
    private val extension by setting("Extension", 0, 0..3, 1, unit = " line")
    private val chatDelay by setting("Chat-Detection Delay", 150, 0..500, 10, { method != Method.DataBase }, description = "How many ms to client wait for detect next leftclick information, it the value is too small, it could cause overwriting other weapon's cooldown.")
    private val colorCode by setting("Colored CoolDown", true)
    private val disableRenderingItem by setting("Disable Rendering Item", false, description = "Disables rendering item.")
    private var information by setting("Method Info", false, description = "Shows Each Methods Difference in chat.", consumer = { _, _ ->
        mc.soundHandler.playSound(PositionedSoundRecord.getRecord(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f))
        MessageSendHelper.sendWarningMessage("§f§l[ §r§b§lRPGCoolDown Method Information §r§f§l]")
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
    private var sentInformation = false
    private var initialized = false
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
            if (!sentInformation) {
                sentInformation = true
                defaultScope.launch {
                    delay(1500)
                    MessageSendHelper.sendRawChatMessage(" ")
                    MessageSendHelper.sendWarningMessage("렌독서버 공식 카페에 있는 '멜다의 렌독 런처' 카테고리에서 쿨타임 데이터 추가 및 수정 요청을 받고 있습니다.")
                    MessageSendHelper.sendWarningMessage("많은 관심 부탁드립니다.")
                    MessageSendHelper.sendRawChatMessage(" ")
                }
            }
            if (!initialized) {
                invItemCD.clear()
                runSafe { updateWeaponInv() }
                initialized = true
            }
        }

        safeListener<TickEvent.ClientTickEvent> {
            if (lastSlot != player.inventory.currentItem) lastSwapTime = currentTimeMillis()
            lastSlot = player.inventory.currentItem
        }

        safeListener<ClientChatReceivedEvent> { //moonlight
            if (moonlightName == "") return@safeListener
            if (it.message.unformattedText.trim() == "문라이트가 영혼을 방출합니다!") {
                updateWeaponCoolDown(moonlightName, CoolDownType.RIGHT, WeaponCoolManager.getCD(moonlightName, CoolDownType.RIGHT), false)
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
                updateWeaponCoolDown(rightClickChat, CoolDownType.RIGHT, patternedMessage.group(1).toDouble(), true)
            } else if (patternedMessage2.find()) {
                val value = patternedMessage2.group(2).toDouble() + patternedMessage2.group(1).toDouble() * 60
                updateWeaponCoolDown(rightClickChat, CoolDownType.RIGHT, value, true)
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
                updateWeaponCoolDown(leftClickChat, CoolDownType.LEFT, patternedMessage.group(1).toDouble(), true)
            } else if (patternedMessage2.find()) {
                val value = patternedMessage2.group(2).toDouble() + patternedMessage2.group(1).toDouble() * 60
                updateWeaponCoolDown(leftClickChat, CoolDownType.LEFT, value, true)
            }

            leftClickChat = ""
        }

        safeListener<PacketEvent.Send> { event -> //rightClick
            if (event.packet !is CPacketPlayerTryUseItem) return@safeListener
            if (!initialized) return@safeListener

            val item = player.inventory.getCurrentItem()

            if (!invItemCD.containsKey(item.displayName)) item.addWeaponCDData()

            if (method != Method.DataBase) chatDetectionUpdate(CoolDownType.RIGHT, player.inventory.getCurrentItem().displayName)
            if (method == Method.Chat) return@safeListener

            if (checkVillageAndValidation(item)) {
                if (item.displayName.contains("문라이트") && !item.displayName.contains("초월")) {
                    moonlightName = item.displayName
                } else if ((invItemCD[item.displayName]!!.rightCD - currentTimeMillis()) <= 0) {
                    updateWeaponCoolDown(item.displayName, CoolDownType.RIGHT, WeaponCoolManager.getCD(item.displayName, CoolDownType.RIGHT), false)
                }
            }
        }

        safeListener<PacketEvent.Send> { event -> //leftClick
            if (event.packet !is CPacketAnimation) return@safeListener
            if (!initialized) return@safeListener

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
                    updateWeaponCoolDown(item.displayName, CoolDownType.LEFT, WeaponCoolManager.getCD(item.displayName, CoolDownType.LEFT), false)
                }
            }
        }
    }

    private fun updateWeaponCoolDown(weaponName: String, coolDownType: CoolDownType, value: Double, isValueFromChat: Boolean) {
        val finalCoolDown = if (isValueFromChat || cdr == 0.0) value
        else value * (1.0 - cdr / 100.0)
            when (coolDownType) {
            CoolDownType.RIGHT -> invItemCD[weaponName]?.rightCD = currentTimeMillis() + (1000 * finalCoolDown).toLong()
            CoolDownType.LEFT -> invItemCD[weaponName]?.leftCD = currentTimeMillis() + (1000 * finalCoolDown).toLong()
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
            ((player.world.spawnPoint == BlockPos(278, 11, -134)) && WeaponCoolManager.isAbleInVillage(item.displayName)))
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
        if (invItemCD.isNotEmpty() && initialized) {
            if (extension != 0) for (i in (4 - extension) * 9 until 36) drawCoolDown(i)
            for (i in 0..8) drawCoolDown(i)
            GlStateManager.popMatrix()
        }
    }

    private fun drawCoolDown(slot: Int) {
        runSafe {
            val item = player.inventory.getStackInSlot(slot)
            if (invItemCD.containsKey(item.displayName)) {
                val rightCoold = ((invItemCD[item.displayName]!!.rightCD - currentTimeMillis()).toDouble() / 100.0).roundToInt() / 10.0
                val leftCoold = ((invItemCD[item.displayName]!!.leftCD - currentTimeMillis()).toDouble() / 100.0).roundToInt() / 10.0

                val rightCoolText = if (rightCoold <= 0) ""
                else if (rightCoold > 60) {
                    if (colorCode) "§c${convert2Min(rightCoold)}"
                    else convert2Min(rightCoold)
                } else {
                    if (colorCode) "§c$rightCoold"
                    else "$rightCoold"
                }
                val leftCoolText = if (leftCoold <= 0) ""
                else if (leftCoold > 60) {
                    if (colorCode) "§e${convert2Min(leftCoold)}"
                    else convert2Min(leftCoold)
                } else {
                    if (colorCode) "§e$leftCoold"
                    else "$leftCoold"
                }

                drawCoolDownItem(item, 2, 2, rightCoolText, leftCoolText)
            } else {
                drawCoolDownItem(item, 2, 2, "", "")
            }
            if (horizontal) GlStateManager.translate(20.0f, 0.0f, 0.0f)
            else GlStateManager.translate(0.0f, 20.0f, 0.0f)
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

    private fun drawCoolDownItem(itemStack: ItemStack, x: Int, y: Int, rightCool: String, leftCool: String) {
        val fr = mc.fontRenderer
        GlStateUtils.blend(true)
        GlStateUtils.depth(true)
        RenderHelper.enableGUIStandardItemLighting()

        mc.renderItem.zLevel = 0.0f
        if (!disableRenderingItem) mc.renderItem.renderItemAndEffectIntoGUI(itemStack, x, y)

        GlStateManager.disableLighting()
        GlStateManager.disableDepth()
        GlStateManager.disableBlend()

        fr.drawStringWithShadow(leftCool, x.toFloat(), (y - 2).toFloat(), 16777215)
        fr.drawStringWithShadow(rightCool, (x + 17 - fr.getStringWidth(rightCool)).toFloat(), (y + 9).toFloat(), 16777215)

        GlStateManager.enableLighting()
        GlStateManager.enableDepth()
        GlStateManager.enableBlend()

        RenderHelper.disableStandardItemLighting()
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)

        GlStateUtils.depth(false)
        GlStateUtils.texture2d(true)
    }
}