package kr.rendog.client.gui.hudgui.elements.combat

import kr.rendog.client.event.SafeClientEvent
import kr.rendog.client.gui.hudgui.LabelHud
import kr.rendog.client.util.graphics.font.HAlign
import net.minecraft.init.MobEffects
import kotlin.math.round

internal object GodModeInfo : LabelHud(
    name = "GodModeInfo",
    category = Category.COMBAT,
    description = "Notifies when you are on the godmode.",
) {
    init {
        relativePosX = 0.0f
        relativePosY = 0.3f
        dockingH = HAlign.CENTER
    }

    override fun SafeClientEvent.updateText() {
        if (player.isPotionActive(MobEffects.LUCK)) {
            displayText.add(" §fYou are on the §6§lGodMode!")
            displayText.addLine("")
            player.getActivePotionEffect(MobEffects.LUCK)?.let {
                displayText.add("§fTime Remaining : §6§l${convert(it.duration)}§r§f Sec ")
            }
        }
    }
    private fun convert(duration : Int) : Double {
        return round(duration.toDouble()/2) /10 //tick
    }


    override val hudWidth = 110.0f

    override val hudHeight = 20.0f
}