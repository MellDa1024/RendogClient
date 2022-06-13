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

    private var cooldown = mutableMapOf("" to Pair(0.0,0.0))
    private lateinit var cooldowndata: WeaponDataList
    private val ableinvillage = mutableListOf("")
    private var enabled = false

    private const val url = "https://raw.githubusercontent.com/MellDa1024/RendogDataBase/main/WeaponDataV2.json"

    private val versioninfo = arrayOf("b1", "b2", "b3")

    fun indatabase(item : String) : Boolean {
        return if (enabled) {
            cooldown.containsKey(removecolorcode(item).trim())
        } else {
            MessageSendHelper.sendErrorMessage("Failed to load CoolDown data. type ${CommandConfig.prefix}rendogcd reload to reload data.")
            false
        }
    }

    fun getCD(item : String, rightclick : Boolean = true): Double {
        if (enabled) {
            return if (rightclick) {
                if (cooldown.containsKey(removecolorcode(item))) {
                    cooldown[removecolorcode(item)]!!.second
                } else {
                    0.0
                }
            } else {
                if (cooldown.containsKey(removecolorcode(item))) {
                    cooldown[removecolorcode(item)]!!.first
                } else {
                    0.0
                }
            }
        } else {
            MessageSendHelper.sendErrorMessage("Failed to load CoolDown data. type ${CommandConfig.prefix}rendogcd reload to reload data.")
            return 0.0
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
        try {
            val rawJson = ConnectionUtils.requestRawJsonFrom(url)
            cooldowndata = gson.fromJson(rawJson, object : TypeToken<WeaponDataList>() {}.type)
            cooldown.clear()
            ableinvillage.clear()
            if (!versioncheck(cooldowndata.version)) {
                RendogMod.LOG.warn("CoolDownData needs RendogClient version ${cooldowndata.version} or higher, but your version is in ${RendogMod.VERSION}, The RendogClient Needs Update.")
                RendogMod.LOG.warn("Update your RendogClient to new version.")
                return false
            }
            else {
                for (i in cooldowndata.weaponlist) {
                    if (i.maxlevel == -1) {
                        val modifiedweaponname = i.weaponname + " [ SPECIAL ]"
                        cooldown[modifiedweaponname] = Pair(i.leftcd[0], i.rightcd[0])
                        if (i.invillage) {
                            ableinvillage.add(modifiedweaponname)
                        }
                    }
                    else if (i.weaponname.contains("< 초월 >")) {
                        val modifiedweaponname = i.weaponname + " [ MAX ]"
                        cooldown[modifiedweaponname] = Pair(i.leftcd[0], i.rightcd[0])
                        if (i.invillage) {
                            ableinvillage.add(modifiedweaponname)
                        }
                    }
                    else if (i.maxlevel == 1) {
                        cooldown[i.weaponname] = Pair(i.leftcd[0], i.rightcd[0])
                        if (i.invillage) {
                            ableinvillage.add(i.weaponname)
                        }
                    }
                    else {
                        var modifiedweaponname: String
                        for (j in 0 until i.maxlevel) {
                            modifiedweaponname = if (i.maxlevel-1 != j) {
                                i.weaponname + " [ +${j+1} ]"
                            } else {
                                i.weaponname + " [ MAX ]"
                            }
                            if (!i.changebylevel) {
                                cooldown[modifiedweaponname] = Pair(i.leftcd[0], i.rightcd[0])
                            } else {
                                cooldown[modifiedweaponname] = Pair(i.leftcd[j], i.rightcd[j])
                            }
                            if (i.invillage) {
                                ableinvillage.add(modifiedweaponname)
                            }
                        }
                    }
                }
            }
            enabled = cooldowndata.enabled
            RendogMod.LOG.info("CoolDown Data loaded.")
            return true
        } catch (e: Exception) {
            RendogMod.LOG.warn("Failed loading CoolDownData : ", e)
            return false
        }
    }

    private fun versioncheck(compareversion :String) :Boolean {
        return versioninfo.indexOf(compareversion) <= versioninfo.indexOf(RendogMod.VERSION.replace("GuideOnly", ""))
    }


    data class WeaponDataList(
        @SerializedName("Enabled")
        var enabled: Boolean = true,
        @SerializedName("VersionRequires")
        val version: String,
        @SerializedName("WeaponList")
        val weaponlist: LinkedHashSet<WeaponData>
    )

    data class WeaponData(
        @SerializedName("WeaponName")
        val weaponname : String,
        @SerializedName("maxlevel")
        val maxlevel : Int,
        @SerializedName("changebylevel")
        val changebylevel : Boolean,
        @SerializedName("LeftCoolDown")
        val leftcd : Array<Double>,
        @SerializedName("RightCoolDown")
        val rightcd : Array<Double>,
        @SerializedName("InVillage")
        val invillage : Boolean
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as WeaponData

            if (weaponname != other.weaponname) return false
            if (maxlevel != other.maxlevel) return false
            if (changebylevel != other.changebylevel) return false
            if (!leftcd.contentEquals(other.leftcd)) return false
            if (!rightcd.contentEquals(other.rightcd)) return false
            if (invillage != other.invillage) return false

            return true
        }

        override fun hashCode(): Int {
            var result = weaponname.hashCode()
            result = 31 * result + maxlevel
            result = 31 * result + changebylevel.hashCode()
            result = 31 * result + leftcd.contentHashCode()
            result = 31 * result + rightcd.contentHashCode()
            result = 31 * result + invillage.hashCode()
            return result
        }
    }
}