package com.rendog.client.util

import com.rendog.client.RendogMod
import java.net.URL

object KamiCheck {
    var isKami: Boolean = false
    var didDisplayWarning: Boolean = false
    fun runCheck() {
        val kamiCheckList: List<URL> = this.javaClass.classLoader.getResources("org/kamiblue/client/KamiMod.class").toList()
        if (kamiCheckList.isNotEmpty()) {
            RendogMod.LOG.error("KAMI Blue detected!")
            isKami = true
        }
    }
}