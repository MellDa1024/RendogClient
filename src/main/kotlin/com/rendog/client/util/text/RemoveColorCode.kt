package com.rendog.client.util.text

object RemoveColorCode {
    fun String.removeColorCode(): String {
        val colorCode = arrayOf("§0","§1","§2","§3","§4","§5","§6","§7","§8","§9","§a","§b","§c","§d","§e","§f","§k","§l","§m","§n","§o","§r")
        var temp = this
        for (i in colorCode) {
            temp = temp.replace(i,"")
        }
        return temp
    }
}