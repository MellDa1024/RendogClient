package kr.rendog.client.command.commands

import kr.rendog.client.command.ClientCommand
import kr.rendog.client.manager.managers.GuideManager
import kr.rendog.client.util.text.MessageSendHelper

object GuideCommand : ClientCommand(
    name = "guide",
    alias = arrayOf("g"),
    description = "Settings for guide list"
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
            MessageSendHelper.sendWarningMessage("There isn't any guide on your guide list. maybe there are some problem with Guide Database.")
        } else {
            val f = GuideManager.guides.values.joinToString(prefix = "\n    ", separator = "\n    ") { it.name } // nicely format the chat output
            MessageSendHelper.sendChatMessage("Guide list: $f")
        }
    }
}