package com.rendog.client.module

import com.rendog.client.commons.interfaces.DisplayEnum

enum class Category(override val displayName: String) : DisplayEnum {
    CHAT("Chat"),
    CLIENT("Client"),
    COMBAT("Combat"),
    MISC("Misc"),
    MOVEMENT("Movement"),
    PLAYER("Player"),
    RENDER("Render");

    override fun toString() = displayName
}