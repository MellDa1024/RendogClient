package kr.rendog.client.manager.managers

import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import kr.rendog.client.RendogMod
import kr.rendog.client.commons.utils.ConnectionUtils
import kr.rendog.client.manager.Manager
import java.util.concurrent.ConcurrentHashMap

object LootDataManager : Manager {

    private val gson = GsonBuilder().setPrettyPrinting().create()

    private var loot = ConcurrentHashMap<String, String>()
    private lateinit var lootData: LootDataList
    private var enabled = false
    private const val url = "https://raw.githubusercontent.com/MellDa1024/RendogDataBase/main/LootData.json"

    fun getMobNameByLoot(lootName: String): String? {
        return loot[lootName]
    }

    fun loadLootData(): Boolean {
        try {
            val rawJson = ConnectionUtils.requestRawJsonFrom(url)
            lootData = gson.fromJson(rawJson, object : TypeToken<LootDataList>() {}.type)
            loot.clear()
            lootData.lootList.forEach {
                loot[it.loot] = it.mob
            }
            enabled = lootData.enabled
            RendogMod.LOG.info("LootData loaded.")
            return true
        } catch (e: Exception) {
            RendogMod.LOG.error("Failed loading LootData : ", e)
            return false
        }
    }

    data class LootDataList(
        @SerializedName("Enabled")
        var enabled: Boolean = true,
        @SerializedName("LootList")
        val lootList: LinkedHashSet<LootData>
    )

    data class LootData(
        @SerializedName("Loot")
        val loot : String,
        @SerializedName("Mob")
        val mob : String
    )
}