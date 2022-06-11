package com.lambda.client

import com.lambda.client.event.ForgeEventProcessor
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
    modid = LambdaMod.ID,
    name = LambdaMod.NAME,
    version = LambdaMod.VERSION,
    dependencies = LambdaMod.DEPENDENCIES
)
class LambdaMod {

    companion object {
        const val NAME = "RendogClient"
        const val ID = "RendogClient"
        const val DIRECTORY = "RendogClient/"

        const val VERSION = "b1"

        const val DEPENDENCIES = "required-after:forge@[14.23.5.2860,);"

        const val GITHUB_API = "https://api.github.com/"
        private const val MAIN_ORG = "MellDa1024"
        private const val REPO_NAME = "RendogClient"
        const val RELEASES_API = "${GITHUB_API}repos/${MAIN_ORG}/${REPO_NAME}/releases"
        const val DOWNLOAD_LINK = "https://github.com/${MAIN_ORG}/${REPO_NAME}/releases"

        const val RENDOG = "ℜ"

        val LOG: Logger = LogManager.getLogger(NAME)

        var ready: Boolean = false; private set
    }

    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent) {
        val directory = File(DIRECTORY)
        if (!directory.exists()) directory.mkdir()

        LoaderWrapper.preLoadAll()
    }

    @Mod.EventHandler
    fun init(event: FMLInitializationEvent) {
        LOG.info("Initializing $NAME $VERSION")

        LoaderWrapper.loadAll()

        MinecraftForge.EVENT_BUS.register(ForgeEventProcessor)

        ConfigUtils.moveAllLegacyConfigs()
        ConfigUtils.loadAll()

        BackgroundScope.start()

        WebUtils.updateCheck()

        KamiCheck.runCheck()

        LOG.info("$NAME initialized!")
    }

    @Mod.EventHandler
    fun postInit(event: FMLPostInitializationEvent) {
        ready = true
    }
}
