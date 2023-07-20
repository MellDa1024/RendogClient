package kr.rendog.client.manager.managers

import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import kr.rendog.client.RendogMod
import kr.rendog.client.capeapi.PlayerProfile
import kr.rendog.client.commons.extension.synchronized
import kr.rendog.client.commons.utils.ConnectionUtils
import kr.rendog.client.manager.Manager

object GuideManager : Manager {
    private val gson = GsonBuilder().setPrettyPrinting().create()

    private val url = "https://raw.githubusercontent.com/MellDa1024/RendogDataBase/main/guides.json"
    private var guideFile = GuideFile()
    val guides = HashMap<String, PlayerProfile>().synchronized()

    val empty get() = guides.isEmpty()
    var enabled = guideFile.enabled
        set(value) {
            field = value
            guideFile.enabled = value
        }

    fun isGuide(name: String) = guideFile.enabled && guides.contains(name.lowercase())

    fun loadGuides(): Boolean {
        return try {
            val rawJson = ConnectionUtils.requestRawJsonFrom(url)
            guideFile = gson.fromJson(rawJson, object : TypeToken<GuideFile>() {}.type)
            guides.clear()
            guides.putAll(guideFile.guides.associateBy { it.name.lowercase() })
            RendogMod.LOG.info("Guide loaded")
            true
        } catch (e: Exception) {
            RendogMod.LOG.warn("Failed loading guides", e)
            false
        }
    }

    data class GuideFile(
        @SerializedName("Enabled")
        var enabled: Boolean = true,

        @SerializedName("Guides")
        val guides: MutableSet<PlayerProfile> = LinkedHashSet<PlayerProfile>().synchronized()
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is GuideFile) return false

            if (enabled != other.enabled) return false
            if (guides != other.guides) return false

            return true
        }

        override fun hashCode(): Int {
            var result = enabled.hashCode()
            result = 31 * result + guides.hashCode()
            return result
        }
    }
}