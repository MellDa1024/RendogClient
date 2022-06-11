package com.rendog.client.command.commands

import com.rendog.client.command.ClientCommand
import com.rendog.client.manager.managers.GuideManager
import com.rendog.client.util.text.MessageSendHelper

object GuideCommand : ClientCommand(
    name = "guide",
    alias = arrayOf("g"),
    description = "Add someone in guide list!"
) {

    private var confirmTime = 0L

    init {
        literal("is") {
            player("player") { playerArg ->
                execute("Check if player is a guide") {
                    isGuide(playerArg.value.name)
                }
            }
        }

        literal("list") {
            execute("Show guide list") {
                listGuides()
            }
        }

        execute("Show guide list") {
            listGuides()
        }
    }

    private fun isGuide(name: String) {
        val string = if (GuideManager.isGuide(name)) "$name is in guide list."
        else "$name isn't in the guide list."
        MessageSendHelper.sendChatMessage(string)
    }

    private fun listGuides() {
        if (GuideManager.empty) {
            MessageSendHelper.sendChatMessage("You currently don't have any guides added. run &7${prefix}guide add <name>&r to add one.")
        } else {
            val f = GuideManager.guides.values.joinToString(prefix = "\n    ", separator = "\n    ") { it.name } // nicely format the chat output
            MessageSendHelper.sendChatMessage("Guide list: $f")
        }
    }
}