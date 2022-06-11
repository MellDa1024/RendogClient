package com.rendog.client.util.text

import com.rendog.client.RendogMod
import com.rendog.client.command.CommandManager
import com.rendog.client.manager.managers.MessageManager
import com.rendog.client.module.AbstractModule
import com.rendog.client.util.TaskState
import com.rendog.client.util.Wrapper
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TextComponentBase
import net.minecraft.util.text.TextComponentString
import java.util.regex.Pattern

object MessageSendHelper {
    private val mc = Wrapper.minecraft

    fun sendChatMessage(message: ITextComponent) {
        val component = TextComponentString("")
        component.appendSibling(ChatMessage(coloredName('9')))
        component.appendSibling(message)
        sendRawChatMessage(component)
    }

    fun sendChatMessage(message: String) {
        sendRawChatMessage(coloredName('9') + message)
    }

    fun sendWarningMessage(message: String) {
        sendRawChatMessage(coloredName('6') + message)
    }

    fun sendErrorMessage(message: String) {
        sendRawChatMessage(coloredName('4') + message)
    }

    fun sendLambdaCommand(command: String) {
        CommandManager.runCommand(command.removePrefix(CommandManager.prefix))
    }

    fun sendRawChatMessage(message: String?) {
        if (message == null) return
        mc.player?.sendMessage(ChatMessage(message))
    }

    fun sendRawChatMessage(message: ITextComponent?) {
        if (message == null) return
        mc.ingameGUI.chatGUI.printChatMessage(message)
    }

    fun Any.sendServerMessage(message: String?): TaskState {
        if (message.isNullOrBlank()) return TaskState(true)
        val priority = if (this is AbstractModule) modulePriority else 0
        return MessageManager.addMessageToQueue(message, this, priority)
    }

    class ChatMessage internal constructor(text: String) : TextComponentBase() {
        val text: String
        override fun getUnformattedComponentText(): String {
            return text
        }

        override fun createCopy(): ITextComponent {
            return ChatMessage(text)
        }

        init {
            val p = Pattern.compile("&[0123456789abcdefrlonmk]")
            val m = p.matcher(text)
            val sb = StringBuffer()
            while (m.find()) {
                val replacement = "\u00A7" + m.group().substring(1)
                m.appendReplacement(sb, replacement)
            }
            m.appendTail(sb)
            this.text = sb.toString()
        }
    }

    private fun coloredName(colorCode: Char) = "&7[&$colorCode" + RendogMod.RENDOG + "&7] &r"

}