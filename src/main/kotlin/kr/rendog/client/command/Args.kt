package kr.rendog.client.command

import kr.rendog.client.capeapi.PlayerProfile
import kr.rendog.client.command.args.AbstractArg
import kr.rendog.client.command.args.AutoComplete
import kr.rendog.client.command.args.DynamicPrefixMatch
import kr.rendog.client.command.args.StaticPrefixMatch
import kr.rendog.client.gui.GuiManager
import kr.rendog.client.gui.hudgui.AbstractHudElement
import kr.rendog.client.manager.managers.UUIDManager
import kr.rendog.client.module.AbstractModule
import kr.rendog.client.module.ModuleManager
import kr.rendog.client.util.*
import kr.rendog.client.util.threads.runSafeR
import kotlinx.coroutines.Dispatchers
import net.minecraft.block.Block
import net.minecraft.entity.EntityList
import net.minecraft.item.Item
import net.minecraft.util.math.BlockPos
import java.io.File

class ModuleArg(
    override val name: String
) : AbstractArg<AbstractModule>(), AutoComplete by DynamicPrefixMatch(::allAlias) {

    override suspend fun convertToType(string: String?): AbstractModule? {
        return ModuleManager.getModuleOrNull(string)
    }

    private companion object {
        val allAlias by CachedValue(5L, TimeUnit.SECONDS) {
            ModuleManager.modules.asSequence()
                .flatMap { sequenceOf(it.name, *it.alias) }
                .sorted()
                .toList()
        }
    }

}

class HudElementArg(
    override val name: String
) : AbstractArg<AbstractHudElement>(), AutoComplete by DynamicPrefixMatch(::allAlias) {
    override suspend fun convertToType(string: String?): AbstractHudElement? {
        return GuiManager.getHudElementOrNull(string)
    }

    private companion object {
        val allAlias by CachedValue(5L, TimeUnit.SECONDS) {
            GuiManager.hudElements.asSequence()
                .flatMap { sequenceOf(it.name, *it.alias) }
                .sorted()
                .toList()
        }
    }
}

class BlockPosArg(
    override val name: String
) : AbstractArg<BlockPos>(), AutoComplete by DynamicPrefixMatch(::playerPosString) {

    override suspend fun convertToType(string: String?): BlockPos? {
        if (string == null) return null

        val splitInts = string.split(',').mapNotNull { it.toIntOrNull() }
        if (splitInts.size != 3) return null

        return BlockPos(splitInts[0], splitInts[1], splitInts[2])
    }

    private companion object {
        val playerPosString: List<String>?
            get() = Wrapper.player?.position?.let { listOf("${it.x},${it.y},${it.z}") }
    }

}

class BlockArg(
    override val name: String
) : AbstractArg<Block>(), AutoComplete by StaticPrefixMatch(allBlockNames) {

    override suspend fun convertToType(string: String?): Block? {
        if (string == null) return null
        return Block.getBlockFromName(string)
    }

    private companion object {
        val allBlockNames = ArrayList<String>().apply {
            Block.REGISTRY.keys.forEach {
                add(it.toString())
                add(it.path)
            }
            sort()
        }
    }
}

class EntityArg(
    override val name: String
) : AbstractArg<String>(), AutoComplete by StaticPrefixMatch(allEntityNames) {
    override suspend fun convertToType(string: String?): String? {
        if (string == null) return null
        // checks if a valid entity class is registered with this name
        return if (EntityList.getClassFromName(string) != null) string else null
    }

    private companion object {
        val allEntityNames = EntityList.getEntityNameList().map { it.path }
    }
}


class SchematicArg(
    override val name: String
) : AbstractArg<File>(), AutoComplete by DynamicPrefixMatch(::schematicFiles) {

    override suspend fun convertToType(string: String?): File? {
        if (string == null) return null

        val nameWithoutExt = string.removeSuffix(".schematic")
        val file = File("schematics").listFiles()?.filter {
            it.exists() && it.isFile && it.name.equals("$nameWithoutExt.schematic", true)
        } // this stupid find and search is required because ext4 is case sensitive (Linux)

        return file?.firstOrNull()
    }

    private companion object {
        val schematicFolder = File("schematics")

        val schematicFiles by AsyncCachedValue(5L, TimeUnit.SECONDS, Dispatchers.IO) {
            schematicFolder.listFiles()?.map { it.name } ?: emptyList<String>()
        }
    }
}

class ItemArg(
    override val name: String
) : AbstractArg<Item>(), AutoComplete by StaticPrefixMatch(allItemNames) {

    override suspend fun convertToType(string: String?): Item? {
        if (string == null) return null
        return Item.getByNameOrId(string)
    }

    private companion object {
        val allItemNames = ArrayList<String>().run {
            Item.REGISTRY.keys.forEach {
                add(it.toString())
                add(it.path)
            }
            sorted()
        }
    }

}

class PlayerArg(
    override val name: String
) : AbstractArg<PlayerProfile>(), AutoComplete by DynamicPrefixMatch(::playerInfoMap) {

    override suspend fun checkType(string: String?): Boolean {
        return !string.isNullOrBlank()
    }

    override suspend fun convertToType(string: String?): PlayerProfile? {
        return UUIDManager.getByString(string)
    }

    private companion object {
        val playerInfoMap by CachedValue(3L, TimeUnit.SECONDS) {
            runSafeR {
                connection.playerInfoMap.asSequence()
                    .map { it.gameProfile.name }
                    .sorted()
                    .toList()
            }
        }
    }

}