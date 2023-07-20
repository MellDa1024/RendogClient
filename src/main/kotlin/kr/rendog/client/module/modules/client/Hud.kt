package kr.rendog.client.module.modules.client

import kr.rendog.client.module.Category
import kr.rendog.client.module.Module
import kr.rendog.client.util.color.ColorHolder

object Hud : Module(
    name = "Hud",
    description = "Toggles Hud displaying and settings",
    category = Category.CLIENT,
    showOnArray = false,
    enabledByDefault = true
) {
    val hudFrame by setting("Hud Frame", false)
    val primaryColor by setting("Primary Color", ColorHolder(255, 255, 255), false)
    val secondaryColor by setting("Secondary Color", ColorHolder(46, 169, 255), false)
    val textShadow by setting("Text Shadow", true)
    val chatSnap by setting("Chat Snap", true)
    val collisionSnapping by setting("Collision Snapping", true)
}