package kr.rendog.client.gui.hudgui.elements.misc

import kr.rendog.client.event.SafeClientEvent
import kr.rendog.client.gui.hudgui.LabelHud
import kr.rendog.client.manager.managers.OnlineTimeManager
import kotlin.math.roundToInt
import kotlin.time.DurationUnit
import kotlin.time.toDuration

internal object OnlineTime: LabelHud(
    name = "OnlineTime",
    category = Category.MISC,
    description = "Displays how long you have been online"
) {
    override fun SafeClientEvent.updateText() {
        val onlineTime = OnlineTimeManager.getOnlineTime().toDouble(DurationUnit.SECONDS).roundToInt()
        displayText.add("Online:", secondaryColor)
        displayText.add(onlineTime.toDuration(DurationUnit.SECONDS).toString(), primaryColor)
    }
}