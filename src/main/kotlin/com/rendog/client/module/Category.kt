package com.rendog.client.module

import com.rendog.client.commons.interfaces.DisplayEnum

enum class Category(override val displayName: String) : DisplayEnum {
    COMBAT("Combat"),
    MISC("Misc"),
    MOVEMENT("Movement"),
    PLAYER("Player"),
    RENDER("Render"),
    CHAT("Chat"),
    CLIENT("Client");

    override fun toString() = displayName
}