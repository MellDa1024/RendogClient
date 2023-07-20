package kr.rendog.client.util.text

object Color {
    fun String.deColorize(): String {
        val colorCode = arrayOf("§0","§1","§2","§3","§4","§5","§6","§7","§8","§9","§a","§b","§c","§d","§e","§f","§k","§l","§m","§n","§o","§r")
        var temp = this
        for (i in colorCode) {
            temp = temp.replace(i,"")
        }
        return temp
    }

    fun String.removeUntradeableMark(): String {
        return this.replace("(거래 불가)", "")
    }
}