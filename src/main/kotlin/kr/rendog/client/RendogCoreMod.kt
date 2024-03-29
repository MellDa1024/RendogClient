package kr.rendog.client

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin
import org.apache.logging.log4j.LogManager
import org.spongepowered.asm.launch.MixinBootstrap
import org.spongepowered.asm.mixin.MixinEnvironment
import org.spongepowered.asm.mixin.Mixins

@IFMLLoadingPlugin.Name("RendogCoreMod")
@IFMLLoadingPlugin.MCVersion("1.12.2")
class RendogCoreMod : IFMLLoadingPlugin {
    override fun getASMTransformerClass(): Array<String> {
        return emptyArray()
    }

    override fun getModContainerClass(): String? {
        return null
    }

    override fun getSetupClass(): String? {
        return null
    }

    override fun injectData(data: Map<String, Any>) {}

    override fun getAccessTransformerClass(): String? {
        return null
    }

    init {
        val logger = LogManager.getLogger("Rendog")

        MixinBootstrap.init()
        Mixins.addConfigurations("mixins.rendog.json")

        MixinEnvironment.getDefaultEnvironment().obfuscationContext = "searge"
        logger.info("Rendog mixins initialised.")
    }
}