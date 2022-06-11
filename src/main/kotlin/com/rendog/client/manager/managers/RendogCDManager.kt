package com.rendog.client.manager.managers

import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import com.rendog.client.RendogMod
import com.rendog.client.commons.utils.ConnectionUtils
import com.rendog.client.manager.Manager
import com.rendog.client.module.modules.client.CommandConfig
import com.rendog.client.util.text.MessageSendHelper
import kotlin.collections.LinkedHashSet

object RendogCDManager : Manager {

    private val gson = GsonBuilder().setPrettyPrinting().create()

    private var cooldown = mutableMapOf("" to Pair(0,0))
    private lateinit var cooldowndata: WeaponDataList
    private val ableinvillage = mutableListOf("")
    private var enabled = false

    private val url = "https://raw.githubusercontent.com/MellDa1024/RendogDataBase/main/WeaponData.json"

    fun getCD(item : String, rightclick : Boolean = true): Int {
        if (enabled) {
            if (rightclick) {
                if (cooldown.containsKey(removecolorcode(item))) {
                    return cooldown[removecolorcode(item)]!!.second
                } else {
                    return 0
                }
            } else {
                if (cooldown.containsKey(removecolorcode(item))) {
                    return cooldown[removecolorcode(item)]!!.first
                } else {
                    return 0
                }
            }
        } else {
            MessageSendHelper.sendErrorMessage("Failed to load CoolDown data. type ${CommandConfig.prefix}rendogcd reload to reload data.")
            return 0
        }
    }

    fun isableinvillage(item : String): Boolean {
        return removecolorcode(item) in ableinvillage
    }

    fun removecolorcode(message: String): String {
        val colorcode = arrayOf("§0","§1","§2","§3","§4","§5","§6","§7","§8","§9","§a","§b","§c","§d","§e","§f","§k","§l","§m","§n","§o","§r")
        var temp = message
        for (i in colorcode) {
            temp = temp.replace(i,"")
        }
        return temp
    }

    fun loadCoolDownData(): Boolean {
        return try {
            val rawJson = ConnectionUtils.requestRawJsonFrom(url)
            cooldowndata = gson.fromJson(rawJson, object : TypeToken<WeaponDataList>() {}.type)
            cooldown.clear()
            ableinvillage.clear()
            for (i in cooldowndata.weaponlist) {
                cooldown[i.weaponname] = Pair(i.leftcd, i.rightcd)
                if (i.invillage) {
                    ableinvillage.add(i.weaponname)
                }
            }
            enabled = cooldowndata.enabled
            RendogMod.LOG.info("CoolDown Data loaded")
            true
        } catch (e: Exception) {
            RendogMod.LOG.warn("Failed loading CoolDownData : ", e)
            false
        }
    }

    data class WeaponDataList(
        @SerializedName("Enabled")
        var enabled: Boolean = true,

        @SerializedName("WeaponList")
        val weaponlist: LinkedHashSet<WeaponData>
    )

    data class WeaponData(
        @SerializedName("WeaponName")
        val weaponname : String,
        @SerializedName("RightCoolDown")
        val rightcd : Int,
        @SerializedName("LeftCoolDown")
        val leftcd : Int,
        @SerializedName("InVillage")
        val invillage : Boolean
    )
}