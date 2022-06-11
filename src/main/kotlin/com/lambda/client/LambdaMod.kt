package com.lambda.client

import com.lambda.client.event.ForgeEventProcessor
import com.lambda.client.gui.clickgui.LambdaClickGui
import com.lambda.client.util.ConfigUtils
import com.lambda.client.util.KamiCheck
import com.lambda.client.util.WebUtils
import com.lambda.client.util.threads.BackgroundScope
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File

@Suppress("UNUSED_PARAMETER")
@Mod(
    modid = com.lambda.client.LambdaMod.Companion.ID,
    name = com.lambda.client.LambdaMod.Companion.NAME,
    version = com.lambda.client.LambdaMod.Companion.VERSION,
    dependencies = com.lambda.client.LambdaMod.Companion.DEPENDENCIES
)
class LambdaMod {

    companion object {
        const val NAME = "Lambda"
        const val ID = "lambda"
        const val DIRECTORY = "lambda/"

        const val VERSION = "3.1"

        const val APP_ID = 835368493150502923 // DiscordIPC
        const val DEPENDENCIES = "required-after:forge@[14.23.5.2860,);"

        const val GITHUB_API = "https://api.github.com/"
        private const val MAIN_ORG = "lambda-client"
        const val PLUGIN_ORG = "lambda-plugins"
        private const val REPO_NAME = "lambda"
        const val CAPES_JSON = "https://raw.githubusercontent.com/${com.lambda.client.LambdaMod.Companion.MAIN_ORG}/cape-api/capes/capes.json"
        const val RELEASES_API = "${com.lambda.client.LambdaMod.Companion.GITHUB_API}repos/${com.lambda.client.LambdaMod.Companion.MAIN_ORG}/${com.lambda.client.LambdaMod.Companion.REPO_NAME}/releases"
        const val DOWNLOAD_LINK = "https://github.com/${com.lambda.client.LambdaMod.Companion.MAIN_ORG}/${com.lambda.client.LambdaMod.Companion.REPO_NAME}/releases"
        const val GITHUB_LINK = "https://github.com/${com.lambda.client.LambdaMod.Companion.MAIN_ORG}/"
        const val DISCORD_INVITE = "https://discord.gg/QjfBxJzE5x"

        const val LAMBDA = "λ"

        val LOG: Logger = LogManager.getLogger(com.lambda.client.LambdaMod.Companion.NAME)

        var ready: Boolean = false; private set
    }

    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent) {
        val directory = File(com.lambda.client.LambdaMod.Companion.DIRECTORY)
        if (!directory.exists()) directory.mkdir()

        com.lambda.client.LoaderWrapper.preLoadAll()
    }

    @Mod.EventHandler
    fun init(event: FMLInitializationEvent) {
        com.lambda.client.LambdaMod.Companion.LOG.info("Initializing ${com.lambda.client.LambdaMod.Companion.NAME} ${com.lambda.client.LambdaMod.Companion.VERSION}")

        com.lambda.client.LoaderWrapper.loadAll()

        MinecraftForge.EVENT_BUS.register(ForgeEventProcessor)

        ConfigUtils.moveAllLegacyConfigs()
        ConfigUtils.loadAll()

        BackgroundScope.start()

        WebUtils.updateCheck()

        KamiCheck.runCheck()

        com.lambda.client.LambdaMod.Companion.LOG.info("${com.lambda.client.LambdaMod.Companion.NAME} initialized!")
    }

    @Mod.EventHandler
    fun postInit(event: FMLPostInitializationEvent) {
        com.lambda.client.LambdaMod.Companion.ready = true
    }
}
