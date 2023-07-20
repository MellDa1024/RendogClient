package kr.rendog.client.util

import kr.rendog.client.RendogMod
import kr.rendog.client.manager.managers.*
import kr.rendog.client.setting.ConfigManager
import java.io.File
import java.io.FileWriter
import java.io.IOException

object ConfigUtils {

    fun loadAll(): Boolean {
        var success = ConfigManager.loadAll()
        success = WaypointManager.loadWaypoints() && success // Waypoint
        success = GuideManager.loadGuides() && success
        success = RendogCDManager.loadCoolDownData() && success
        success = UUIDManager.load() && success // UUID Cache

        return success
    }

    fun saveAll(): Boolean {
        var success = ConfigManager.saveAll()
        success = WaypointManager.saveWaypoints() && success // Waypoint
        success = UUIDManager.save() && success // UUID Cache

        return success
    }

    fun isPathValid(path: String): Boolean {
        return try {
            File(path).canonicalPath
            true
        } catch (e: Throwable) {
            false
        }
    }

    fun fixEmptyJson(file: File, isArray: Boolean = false) {
        var empty = false

        if (!file.exists()) {
            file.createNewFile()
            empty = true
        } else if (file.length() <= 8) {
            val string = file.readText()
            empty = string.isBlank() || string.all {
                it == '[' || it == ']' || it == '{' || it == '}' || it == ' ' || it == '\n' || it == '\r'
            }
        }

        if (empty) {
            try {
                FileWriter(file, false).use {
                    it.write(if (isArray) "[]" else "{}")
                }
            } catch (exception: IOException) {
                RendogMod.LOG.warn("Failed fixing empty json", exception)
            }
        }
    }
}