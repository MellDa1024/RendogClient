package com.rendog.client.manager.managers

import com.rendog.client.manager.Manager
import com.rendog.client.util.text.MessageSendHelper
import com.rendog.client.util.threads.safeListener
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.util.*

object NotificationManager : Manager {
    private val pendingNotifications: Queue<String> = LinkedList()

    init {
        safeListener<TickEvent.ClientTickEvent> {
            while (pendingNotifications.isNotEmpty()) {
                MessageSendHelper.sendErrorMessage(pendingNotifications.poll())
            }
        }
    }

    fun registerNotification(message: String) {
        pendingNotifications.add(message)
    }
}