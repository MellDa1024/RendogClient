package com.rendog.client.gui.hudgui.elements.misc

import com.rendog.client.event.SafeClientEvent
import com.rendog.client.gui.hudgui.LabelHud
import com.rendog.client.util.TimeUtils
import com.rendog.client.util.graphics.font.HAlign

internal object Time : LabelHud(
    name = "Time",
    category = Category.MISC,
    description = "System date and time"
) {

    init {
        dockingH = HAlign.RIGHT
    }

    private val showDate = setting("Show Date", true)
    private val showTime = setting("Show Time", true)
    private val dateFormat = setting("Date Format", TimeUtils.DateFormat.DDMMYY, { showDate.value })
    private val timeFormat = setting("Time Format", TimeUtils.TimeFormat.HHMM, { showTime.value })
    private val timeUnit = setting("Time Unit", TimeUtils.TimeUnit.H12, { showTime.value })

    override fun SafeClientEvent.updateText() {
        if (showDate.value) displayText.addLine(TimeUtils.getDate(dateFormat.value))
        if (showTime.value) displayText.addLine(TimeUtils.getTime(timeFormat.value, timeUnit.value))
    }

}