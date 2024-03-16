package kr.rendog.client.manager.managers

import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import kr.rendog.client.RendogMod
import kr.rendog.client.commons.utils.ConnectionUtils
import kr.rendog.client.manager.Manager
import kr.rendog.client.module.modules.client.CommandConfig
import kr.rendog.client.util.ConfigUtils
import kr.rendog.client.util.FolderUtils
import kr.rendog.client.util.rendog.CoolDownType
import kr.rendog.client.util.rendog.WeaponCDData
import kr.rendog.client.util.text.Color.deColorize
import kr.rendog.client.util.text.MessageSendHelper
import java.io.File
import java.util.concurrent.ConcurrentHashMap

object WeaponCoolManager : Manager {

    private val gson = GsonBuilder().setPrettyPrinting().create()

    private var coolDown = ConcurrentHashMap<String, WeaponCDData>()
    private lateinit var coolDownData: WeaponDataList
    private var enabled = false
    private val availableVersion = mutableListOf("b7", "b8")

    private val file = File(FolderUtils.rendogFolder + "WeaponDataV2.json")
    private const val url = "https://raw.githubusercontent.com/MellDa1024/RendogDataBase/main/WeaponDataV2.json"

    fun inDatabase(item: String): Boolean {
        return if (enabled) {
            coolDown.containsKey(item.deColorize().trim())
        } else {
            MessageSendHelper.sendErrorMessage("Failed to load CoolDown data. type ${CommandConfig.prefix}weaponcool reload to reload data.")
            false
        }
    }

    fun getCD(item: String, cdType: CoolDownType): Double {
        return if (enabled) {
            when (cdType) {
                CoolDownType.RIGHT -> coolDown[item.deColorize()]?.rightCD ?: 0.0
                CoolDownType.LEFT -> coolDown[item.deColorize()]?.leftCD ?: 0.0
            }
        } else {
            MessageSendHelper.sendErrorMessage("Failed to load CoolDown data. type ${CommandConfig.prefix}weaponcool reload to reload data.")
            0.0
        }
    }

    fun isAbleInVillage(item: String): Boolean {
        return coolDown[item.deColorize()]?.inVillage ?: false
    }

    fun loadCoolDownDataFromFile(): Boolean {
        ConfigUtils.fixEmptyJson(file)
        return try {
            coolDownData = file.bufferedReader().use {
                gson.fromJson(it.readText(), object : TypeToken<WeaponDataList>() {}.type)
            }
            loadCoolDownData(coolDownData)
        } catch (e: Exception) {
            RendogMod.LOG.error("Failed loading CoolDownData : ", e)
            false
        }
    }

    fun loadCoolDownDataFromGithub(): Boolean {
        return try {
            val rawJson = ConnectionUtils.requestRawJsonFrom(url)
            coolDownData = gson.fromJson(rawJson, object : TypeToken<WeaponDataList>() {}.type)
            loadCoolDownData(coolDownData)
        } catch (e: Exception) {
            RendogMod.LOG.error("Failed loading CoolDownData : ", e)
            false
        }
    }

    private fun loadCoolDownData(coolDownData: WeaponDataList): Boolean {
        coolDown.clear()
        if (!availableVersion.contains(coolDownData.version)) {
            RendogMod.LOG.error("CoolDownData needs RendogClient version ${WeaponCoolManager.coolDownData.version} or higher, but your version is in ${RendogMod.VERSION}, The RendogClient Needs Update.")
            RendogMod.LOG.error("Update your RendogClient to new version.")
            return false
        } else {
            coolDownData.weaponList.forEach { weaponData ->
                if (weaponData.maxLevel > 1) {
                    for (lvl in 1 until weaponData.maxLevel + 1) {
                        val modifiedWeaponName =
                            if (weaponData.maxLevel != lvl) weaponData.weaponName + " [ +$lvl ]"
                            else weaponData.weaponName + " [ MAX ]"

                        if (!weaponData.changeByLevel) register(modifiedWeaponName, weaponData, 0)
                        else register(modifiedWeaponName, weaponData, lvl - 1)
                    }
                } else {
                    val modifiedWeaponName = weaponData.weaponName + when {
                        weaponData.maxLevel == -1 -> " [ SPECIAL ]"
                        weaponData.weaponName.contains("< 초월 >") -> " [ MAX ] (거래 불가)"
                        weaponData.maxLevel == 1 -> ""
                        else -> throw Exception("Cannot Recognize the Weapon, WeaponName : ${weaponData.weaponName}")
                    }
                    register(modifiedWeaponName, weaponData, 0)
                }
            }
        }
        enabled = coolDownData.enabled
        RendogMod.LOG.info("CoolDown Data loaded.")
        return true
    }

    private fun register(weaponName: String, weaponData: WeaponData, cdIndex: Int) {
        coolDown[weaponName] = WeaponCDData(weaponData.leftCD[cdIndex], weaponData.rightCD[cdIndex], weaponData.inVillage)
        //RendogMod.LOG.info("$weaponName registered. Data : ${coolDown[weaponName]}")
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
        val weaponName: String,
        @SerializedName("maxLevel")
        val maxLevel: Int,
        @SerializedName("changeByLevel")
        val changeByLevel: Boolean,
        @SerializedName("LeftCoolDown")
        val leftCD: Array<Double>,
        @SerializedName("RightCoolDown")
        val rightCD: Array<Double>,
        @SerializedName("InVillage")
        val inVillage: Boolean
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