package com.rendog.client.module.modules.client

import com.rendog.client.module.Category
import com.rendog.client.module.Module
import com.rendog.client.util.color.ColorHolder

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
}