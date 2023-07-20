package kr.rendog.client.module

import kr.rendog.client.commons.interfaces.DisplayEnum

enum class Category(override val displayName: String) : DisplayEnum {
    CHAT("Chat"),
    CLIENT("Client"),
    MISC("Misc"),
    RENDER("Render");

    override fun toString() = displayName
}