package com.rendog.client.module.modules.client

import com.rendog.client.module.Category
import com.rendog.client.module.Module

object ChatSetting : Module(
    name = "ChatSetting",
    category = Category.CLIENT,
    description = "Configures chat message manager",
    showOnArray = false,
    alwaysEnabled = true
) {
    val delay by setting("Message Speed Limit", 0.5f, 0.1f..20.0f, 0.1f, description = "Delay between each message in seconds")
    val maxMessageQueueSize by setting("Max Message Queue Size", 50, 10..200, 5)
}