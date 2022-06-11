package com.rendog.client.module.modules.misc

import com.rendog.client.event.listener.listener
import com.rendog.client.manager.managers.GuideManager
import com.rendog.client.module.Category
import com.rendog.client.util.text.MessageSendHelper
import com.rendog.client.module.Module
import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.init.SoundEvents
import net.minecraftforge.client.event.ClientChatReceivedEvent
import java.util.regex.Pattern
import org.lwjgl.opengl.Display
import java.awt.*

/**
 * This is a module. First set properties then settings then add listener.
 * **/
internal object RendogNotification : Module(
    name = "RendogNotification",
    category = Category.MISC,
    description = "Rendog Notification, only for guide.",
) {
    private val page by setting("Page", Page.WHISPER)

    /* whisper settings */
    private val whisperenable by setting("Enable Whisper", true, { page == Page.WHISPER })
    private var whispertoast by setting("Whisper Toast Notification", true, { (page == Page.WHISPER) && whisperenable }, description = "Notifies with Toast Notification when minecraft is unfocused.")
    private val whispersound by setting("Whisper Sound Notification", true, { (page == Page.WHISPER) && whisperenable }, description = "Notifies with Sound when minecraft is focused.")
    private val whispermessage by setting("Whisper Message Notification", true, { (page == Page.WHISPER) && whisperenable }, description = "Notifies with Message when minecraft is focused.")

    /* trade settings */
    private val tradeenable by setting("Enable Trade", true, { page == Page.TRADE })
    private var tradetoast by setting("Trade Toast Notification", true, { (page == Page.TRADE) && tradeenable }, description = "Notifies with Toast Notification when minecraft is unfocused.")
    private val tradesound by setting("Trade Sound Notification", true, { (page == Page.TRADE) && tradeenable }, description = "Notifies with Sound when minecraft is focused.")
    private val trademessage by setting("Trade Message Notification", true, { (page == Page.TRADE) && tradeenable }, description = "Notifies with Message when minecraft is focused.")

    private enum class Page {
        WHISPER, TRADE
    }

    private lateinit var tray : SystemTray
    private lateinit var image : Image
    private lateinit var trayicon : TrayIcon
    private var istrayopen = false
    private var notsupported = false
    private val whisperpattern = Pattern.compile(" \\[ ([0-9a-zA-Z_]+) -> 나 ] .*")

    init{
        onEnable {
            try {
                if (!GuideManager.isGuide(mc.session.username)) disable()
                else if (!SystemTray.isSupported()) {
                    MessageSendHelper.sendErrorMessage("$chatName Toast System Not supported, Toast Notification will be disabled.")
                    whispertoast = false
                    tradetoast = false
                    notsupported = true
                } else {
                    tray = SystemTray.getSystemTray()
                    image = Toolkit.getDefaultToolkit().createImage(javaClass.getResourceAsStream("/assets/minecraft/rendog/rendog_icon.png").readBytes())
                    trayicon = TrayIcon(image, "Rendog Client Notification System")
                    trayicon.isImageAutoSize = true
                    tray.add(trayicon)
                    istrayopen = true
                }
            } catch (e: Exception) {
                MessageSendHelper.sendErrorMessage("$chatName Unknown Error Occurred, Toast Notification will be disabled.\nError : $e")
                whispertoast = false
                tradetoast = false
                notsupported = true
            }
        }
        onDisable {
            if (istrayopen) {
                try{
                    tray.remove(trayicon)
                    istrayopen = false
                } catch (e : Exception) {
                    MessageSendHelper.sendErrorMessage("$chatName Unknown Error occurred, Error : $e")
                }
            }
        }

        listener<ClientChatReceivedEvent> {
            if (notsupported) {
                whispertoast = false
                tradetoast = false
            }
            if (it.message.unformattedText.contains("-> 나 ]")) {
                val patternedmessage = whisperpattern.matcher(removecolorcode(it.message.unformattedText))
                if (patternedmessage.find()) {
                    if (!Display.isActive()) {
                        if (whispertoast && !notsupported) toastnotification("Whisper Detected.")
                    }
                    else {
                        if (whispersound) mc.soundHandler.playSound(PositionedSoundRecord.getRecord(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f))
                        if (whispermessage) MessageSendHelper.sendWarningMessage("$chatName §f§lWhisper Detected.")
                    }
                }
            }
            if (it.message.unformattedText.trim() == "┌───────────── [TRADE] ─────────────┐") {
                if (!Display.isActive()) {
                    if (tradetoast && !notsupported) toastnotification("Trade Request Detected.")
                }
                else {
                    if (tradesound) mc.soundHandler.playSound(PositionedSoundRecord.getRecord(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f))
                    if (trademessage) {
                        MessageSendHelper.sendWarningMessage("$chatName §f§lTrade request Detected.")
                    }
                }
            }
        }
    }
    private fun toastnotification(message : String) {
        trayicon.displayMessage("RendogClient", message, TrayIcon.MessageType.NONE)
    }
    private fun removecolorcode(message: String): String {
        val colorcode = arrayOf("§0","§1","§2","§3","§4","§5","§6","§7","§8","§9","§a","§b","§c","§d","§e","§f","§k","§l","§m","§n","§o","§r")
        var temp = message
        for (i in colorcode) {
            temp = temp.replace(i,"")
        }
        return temp
    }
}