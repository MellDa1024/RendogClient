package kr.rendog.client.gui.hudgui.elements.client

import kr.rendog.client.event.SafeClientEvent
import kr.rendog.client.gui.hudgui.LabelHud

internal object Username : LabelHud(
    name = "Username",
    category = Category.CLIENT,
    description = "Player username",
    enabledByDefault = true
) {

    private val prefix = setting("Prefix", "Welcome")
    private val suffix = setting("Suffix", "")

    override fun SafeClientEvent.updateText() {
        if (prefix.value != "") displayText.add(prefix.value, primaryColor)
        displayText.add(mc.session.username, secondaryColor)
        if (suffix.value != "") displayText.add(suffix.value, primaryColor)
    }
    init {
        relativePosX = 0.0f
        relativePosY = 10.0f
    }
}