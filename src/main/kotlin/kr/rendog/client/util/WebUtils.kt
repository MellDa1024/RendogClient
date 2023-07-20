package kr.rendog.client.util

import com.google.gson.JsonParser
import kr.rendog.client.RendogMod
import kr.rendog.client.commons.utils.ConnectionUtils
import kr.rendog.client.util.threads.mainScope
import kotlinx.coroutines.launch
import org.apache.maven.artifact.versioning.DefaultArtifactVersion
import java.awt.Desktop
import java.io.BufferedReader
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.URI
import java.net.URL
import java.nio.channels.Channels

object WebUtils {
    var isLatestVersion = true
    var latestVersion: String? = null

    fun updateCheck() {
        mainScope.launch {
            try {
                RendogMod.LOG.info("Attempting RendogClient update check...")

                val rawJson = ConnectionUtils.requestRawJsonFrom(RendogMod.RELEASES_API) {
                    throw it
                }

                rawJson?.let { json ->
                    val jsonTree = JsonParser().parse(json).asJsonArray
                    latestVersion = jsonTree[0]?.asJsonObject?.get("tag_name")?.asString

                    latestVersion?.let {
                        val remoteVersion = DefaultArtifactVersion(it)
                        val localVersion = DefaultArtifactVersion(RendogMod.VERSION)
                        when {
                            remoteVersion == localVersion -> {
                                RendogMod.LOG.info("Your RendogClient (" + RendogMod.VERSION + ") is up-to-date with the latest stable release.")
                            }
                            remoteVersion > localVersion -> {
                                isLatestVersion = false
                                RendogMod.LOG.warn("Your RendogClient is outdated.\nCurrent: ${RendogMod.VERSION}\nLatest: $latestVersion")
                            }
                            remoteVersion < localVersion -> {
                                RendogMod.LOG.info("Your RendogClient (" + RendogMod.VERSION + ") is ahead of time.")
                            }
                        }
                    }
                }
            } catch (e: IOException) {
                RendogMod.LOG.error("An exception was thrown during the update check.", e)
            }
        }
    }

    fun openWebLink(url: String) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI(url))
            } else {
                val exitCode = Runtime.getRuntime().exec(arrayOf("xdg-open", url)).waitFor()
                if (exitCode != 0) {
                    RendogMod.LOG.error("Couldn't open link, xdg-open returned: $exitCode")
                }
            }
        } catch (e: IOException) {
            RendogMod.LOG.error("Couldn't open link: $url")
        }
    }

    fun getUrlContents(url: String): String {
        val content = StringBuilder()

        ConnectionUtils.runConnection(url, block = { connection ->
            val bufferedReader = BufferedReader(InputStreamReader(connection.inputStream))
            bufferedReader.forEachLine { content.append("$it\n") }
        }, catch = {
            it.printStackTrace()
        })

        return content.toString()
    }

    @Throws(IOException::class)
    fun downloadUsingNIO(url: String, file: String) {
        Channels.newChannel(URL(url).openStream()).use { channel ->
            FileOutputStream(file).use {
                it.channel.transferFrom(channel, 0, Long.MAX_VALUE)
            }
        }
    }
}