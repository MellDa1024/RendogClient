package kr.rendog.client.gui.hudgui.elements.combat

import kr.rendog.client.event.SafeClientEvent
import kr.rendog.client.gui.hudgui.LabelHud
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

    private val lootMap = mapOf(
        "목장돼지의 고기" to "목장돼지",
        "슬라임볼" to "슬라임",
        "신성한 묘목" to "숲의 수호자",
        "라마의 가죽" to "초원라마",
        "스켈레톤의 잔해" to "스켈레톤 검사",
        "광부의 가족 사진" to "광산좀비",
        "하이에나 가죽" to "하이에나",
        "설인의 눈물" to "설인",
        "얼어붙은 눈 결정" to "스트레이",
        "먹다 남은 연어" to "아이스베어",
        "황금 조각" to "해적 선원",
        "골렘의 조각" to "사원 골렘",
        "좀벌레의 눈" to "좀벌레",
        "거미의 눈" to "동굴 거미",
        "구아노" to "흡혈 박쥐",
        "이름 모를 자의 영혼" to "네임리스", //Typo Problem
        "이름 없는 자의 영혼" to "네임리스",
        "심해의 돌" to "물의 수호자",
        "수정석" to "수정괴물",
        "수정 벌레의 등껍질" to "수정벌레",
        "용암괴수의 잔해" to "용암괴수",
        "마그마 리치의 구슬" to "마그마 리치",
        "불의 꽃" to "불의 요정",
        "어둠의 핵" to "타락한 망령",
        "암흑물질" to "프랜틱 스켈레톤",
        "괴생명체의 살점" to "괴생명체",
        "고대잔해" to "스켈레톤 궁수 | 주술사",
        "괴상한 호박" to "공허 악령"
    )

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
            lootMap[patternedMessage.group(1)]?.let { lootItem ->
                playerStat[lootItem] = playerStat[lootItem]?.plus(1) ?: 1
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