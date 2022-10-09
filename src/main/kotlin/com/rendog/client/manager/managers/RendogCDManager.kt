package com.rendog.client.manager.managers

import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import com.rendog.client.RendogMod
import com.rendog.client.commons.utils.ConnectionUtils
import com.rendog.client.manager.Manager
import com.rendog.client.module.modules.client.CommandConfig
import com.rendog.client.util.text.MessageSendHelper
import com.rendog.client.util.text.RemoveColorCode.removeColorCode
import kotlin.collections.LinkedHashSet

object RendogCDManager : Manager {

    private val gson = GsonBuilder().setPrettyPrinting().create()

    private var coolDown = mutableMapOf("" to Pair(0.0,0.0))
    private lateinit var coolDownData: WeaponDataList
    private val ableInVillage = mutableListOf("")
    private var enabled = false
    private val availableVersion = mutableListOf("b4, b5, b6")

    private const val url = "https://raw.githubusercontent.com/MellDa1024/RendogDataBase/main/WeaponDataV2.json"

    fun inDatabase(item: String) : Boolean {
        return if (enabled) {
            coolDown.containsKey(item.removeColorCode().trim())
        } else {
            MessageSendHelper.sendErrorMessage("Failed to load CoolDown data. type ${CommandConfig.prefix}rendogcd reload to reload data.")
            false
        }
    }

    fun getCD(item : String, rightClick : Boolean = true): Double {
        if (enabled) {
            return if (rightClick) {
                if (coolDown.containsKey(item.removeColorCode())) {
                    coolDown[item.removeColorCode()]!!.second
                } else {
                    0.0
                }
            } else {
                if (coolDown.containsKey(item.removeColorCode())) {
                    coolDown[item.removeColorCode()]!!.first
                } else {
                    0.0
                }
            }
        } else {
            MessageSendHelper.sendErrorMessage("Failed to load CoolDown data. type ${CommandConfig.prefix}rendogcd reload to reload data.")
            return 0.0
        }
    }

    fun isAbleInVillage(item : String): Boolean {
        return item.removeColorCode() in ableInVillage
    }

    fun loadCoolDownData(): Boolean {
        try {
            val rawJson = ConnectionUtils.requestRawJsonFrom(url)
            coolDownData = gson.fromJson(rawJson, object : TypeToken<WeaponDataList>() {}.type)
            coolDown.clear()
            ableInVillage.clear()
            if (availableVersion.contains(coolDownData.version)) {
                RendogMod.LOG.error("CoolDownData needs RendogClient version ${coolDownData.version} or higher, but your version is in ${RendogMod.VERSION}, The RendogClient Needs Update.")
                RendogMod.LOG.error("Update your RendogClient to new version.")
                return false
            }
            else {
                for (i in coolDownData.weaponList) {
                    if (i.maxLevel == -1) {
                        val modifiedWeaponName = i.weaponName + " [ SPECIAL ]"
                        coolDown[modifiedWeaponName] = Pair(i.leftCD[0], i.rightCD[0])
                        if (i.inVillage) {
                            ableInVillage.add(modifiedWeaponName)
                        }
                    }
                    else if (i.weaponName.contains("< 초월 >")) {
                        val modifiedWeaponName = i.weaponName + " [ MAX ]"
                        coolDown[modifiedWeaponName] = Pair(i.leftCD[0], i.rightCD[0])
                        if (i.inVillage) {
                            ableInVillage.add(modifiedWeaponName)
                        }
                    }
                    else if (i.maxLevel == 1) {
                        coolDown[i.weaponName] = Pair(i.leftCD[0], i.rightCD[0])
                        if (i.inVillage) {
                            ableInVillage.add(i.weaponName)
                        }
                    }
                    else {
                        var modifiedWeaponName: String
                        for (j in 0 until i.maxLevel) {
                            modifiedWeaponName = if (i.maxLevel-1 != j) {
                                i.weaponName + " [ +${j+1} ]"
                            } else {
                                i.weaponName + " [ MAX ]"
                            }
                            if (!i.changeByLevel) {
                                coolDown[modifiedWeaponName] = Pair(i.leftCD[0], i.rightCD[0])
                            } else {
                                coolDown[modifiedWeaponName] = Pair(i.leftCD[j], i.rightCD[j])
                            }
                            if (i.inVillage) {
                                ableInVillage.add(modifiedWeaponName)
                            }
                        }
                    }
                }
            }
            enabled = coolDownData.enabled
            RendogMod.LOG.info("CoolDown Data loaded.")
            return true
        } catch (e: Exception) {
            RendogMod.LOG.error("Failed loading CoolDownData : ", e)
            return false
        }
    }

    data class WeaponDataList(
        @SerializedName("Enabled")
        var enabled: Boolean = true,
        @SerializedName("VersionRequires")
        val version: String,
        @SerializedName("WeaponList")
        val weaponList: LinkedHashSet<WeaponData>
    )

    data class WeaponData(
        @SerializedName("WeaponName")
        val weaponName : String,
        @SerializedName("maxLevel")
        val maxLevel : Int,
        @SerializedName("changeByLevel")
        val changeByLevel : Boolean,
        @SerializedName("LeftCoolDown")
        val leftCD : Array<Double>,
        @SerializedName("RightCoolDown")
        val rightCD : Array<Double>,
        @SerializedName("InVillage")
        val inVillage : Boolean
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as WeaponData

            if (weaponName != other.weaponName) return false
            if (maxLevel != other.maxLevel) return false
            if (changeByLevel != other.changeByLevel) return false
            if (!leftCD.contentEquals(other.leftCD)) return false
            if (!rightCD.contentEquals(other.rightCD)) return false
            if (inVillage != other.inVillage) return false

            return true
        }

        override fun hashCode(): Int {
            var result = weaponName.hashCode()
            result = 31 * result + maxLevel
            result = 31 * result + changeByLevel.hashCode()
            result = 31 * result + leftCD.contentHashCode()
            result = 31 * result + rightCD.contentHashCode()
            result = 31 * result + inVillage.hashCode()
            return result
        }
    }
}