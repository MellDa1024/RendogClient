package kr.rendog.client.gui.hudgui.elements.combat

import kr.rendog.client.event.SafeClientEvent
import kr.rendog.client.gui.hudgui.LabelHud
import kr.rendog.client.manager.managers.LootDataManager
import kr.rendog.client.util.Bind
import kr.rendog.client.util.color.ColorHolder
import kr.rendog.client.util.graphics.font.HAlign
import kr.rendog.client.util.text.Color.deColorize
import kr.rendog.client.util.text.MessageSendHelper
import kr.rendog.client.util.threads.safeListener
import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.init.SoundEvents
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.gameevent.InputEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.lwjgl.input.Keyboard
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

internal object FarmingStat : LabelHud(
    name = "FarmingStat",
    category = Category.COMBAT,
    description = "Show how much did you spend in Farming, Make sure you enabled loot alert.",
) {

    private val showTime by setting("Show Farming Time", true)
    private var block by setting("Block Loot Alert", false, description = "Block loot alert in client-side.")
    private val timeColor by setting("Time Color", ColorHolder(87, 116, 255, 255))
    private val mobColor by setting("Mob Color", ColorHolder(255, 87, 87, 255))
    private val countColor by setting("Count Color", ColorHolder(255, 255, 255, 255))
    private val bracketColor by setting("Bracket Color", ColorHolder(190, 190, 190, 255))
    private var reset by setting("Reset Stat", false, consumer = { _, _ ->
        reset()
        false
    })
    private val resetBind by setting("Reset Bind", Bind())

    private val lootPattern = Pattern.compile("^ {3}\\[ RD ] {3}\\[ (.*) ] 전리품을 획득하셨습니다\\.")

    private val playerStat = ConcurrentHashMap<String, Int>()

    private var startTime = 0L

    private var firstOpen = true

    init {
        relativePosX = 0.0f
        relativePosY = 0.3f
        dockingH = HAlign.RIGHT

        safeListener<InputEvent.KeyInputEvent> {
            if (resetBind.isEmpty) return@safeListener
            val eventKey = Keyboard.getEventKey()
            if (eventKey == Keyboard.KEY_NONE || Keyboard.isKeyDown(Keyboard.KEY_F3)) return@safeListener
            if (resetBind.isDown(eventKey)) reset()
        }

        safeListener<TickEvent.ClientTickEvent> {
            if (firstOpen) {
                startTime = System.currentTimeMillis()
                playerStat.clear()
                firstOpen = false
            }
        }

        safeListener<ClientChatReceivedEvent> {
            if (!it.message.unformattedText.deColorize().startsWith("   [ RD ]")) return@safeListener
            val patternedMessage = lootPattern.matcher(it.message.unformattedText.deColorize())
            if (!patternedMessage.find()) return@safeListener
            val lootName = patternedMessage.group(1)
            LootDataManager.getMobNameByLoot(lootName)?.let { mob ->
                playerStat.computeIfAbsent(mob) { 0 }
                playerStat.computeIfPresent(mob) { _, value ->
                    value + 1
                }
            }
            if (block) it.isCanceled = true
        }
    }

    fun reset() {
        firstOpen = true
        MessageSendHelper.sendWarningMessage("[${name}] FarmingData Cleared.")
        mc.soundHandler.playSound(PositionedSoundRecord.getRecord(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f))
    }

    override fun SafeClientEvent.updateText() {
        if (showTime) displayText.addLine("You Farmed for ${timeBuilder()} !", timeColor)
        if (playerStat.isEmpty()) {
            displayText.addLine("Hunted Mobs : None")
        } else {
            displayText.addLine("Hunted Mobs : ")
            for (mob in playerStat) {
                displayText.add(mob.key, mobColor)
                displayText.add(":", bracketColor)
                displayText.addLine("${mob.value}", countColor)
            }
        }
    }

    private fun timeBuilder(): String {
        val timePassed = System.currentTimeMillis() - startTime
        return String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(timePassed),
            TimeUnit.MILLISECONDS.toMinutes(timePassed) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timePassed)),
            TimeUnit.MILLISECONDS.toSeconds(timePassed) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timePassed)))
    }
}