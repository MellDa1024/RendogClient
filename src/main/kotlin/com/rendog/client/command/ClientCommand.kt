package com.rendog.client.command

import com.rendog.client.capeapi.PlayerProfile
import com.rendog.client.command.CommandBuilder
import com.rendog.client.command.args.AbstractArg
import com.rendog.client.command.utils.BuilderBlock
import com.rendog.client.command.utils.ExecuteBlock
import com.rendog.client.event.ClientExecuteEvent
import com.rendog.client.event.SafeExecuteEvent
import com.rendog.client.gui.hudgui.AbstractHudElement
import com.rendog.client.module.AbstractModule
import com.rendog.client.module.modules.client.CommandConfig
import com.rendog.client.util.Wrapper
import com.rendog.client.util.threads.defaultScope
import com.rendog.client.util.threads.toSafe
import kotlinx.coroutines.launch
import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.util.math.BlockPos
import java.io.File

abstract class ClientCommand(
    name: String,
    alias: Array<out String> = emptyArray(),
    description: String = "No description",
) : CommandBuilder<ClientExecuteEvent>(name, alias, description) {

    val prefixName get() = "$prefix$name"

    @CommandBuilder
    protected inline fun AbstractArg<*>.module(
        name: String,
        block: BuilderBlock<AbstractModule>
    ) {
        arg(ModuleArg(name), block)
    }

    @CommandBuilder
    protected inline fun AbstractArg<*>.hudElement(
        name: String,
        block: BuilderBlock<AbstractHudElement>
    ) {
        arg(HudElementArg(name), block)
    }

    @CommandBuilder
    protected inline fun AbstractArg<*>.block(
        name: String,
        block: BuilderBlock<Block>
    ) {
        arg(BlockArg(name), block)
    }

    @CommandBuilder
    protected inline fun AbstractArg<*>.item(
        name: String,
        block: BuilderBlock<Item>
    ) {
        arg(ItemArg(name), block)
    }

    @CommandBuilder
    protected inline fun AbstractArg<*>.player(
        name: String,
        block: BuilderBlock<PlayerProfile>
    ) {
        arg(PlayerArg(name), block)
    }

    @CommandBuilder
    protected inline fun AbstractArg<*>.blockPos(
        name: String,
        block: BuilderBlock<BlockPos>
    ) {
        arg(BlockPosArg(name), block)
    }

    @CommandBuilder
    protected inline fun AbstractArg<*>.schematic(
        name: String,
        file: BuilderBlock<File>
    ) {
        arg(SchematicArg(name), file)
    }

    @CommandBuilder
    protected fun AbstractArg<*>.executeAsync(
        description: String = "No description",
        block: ExecuteBlock<ClientExecuteEvent>
    ) {
        val asyncExecuteBlock: ExecuteBlock<ClientExecuteEvent> = {
            defaultScope.launch { block() }
        }
        this.execute(description, block = asyncExecuteBlock)
    }

    @CommandBuilder
    protected fun AbstractArg<*>.executeSafe(
        description: String = "No description",
        block: ExecuteBlock<SafeExecuteEvent>
    ) {
        val safeExecuteBlock: ExecuteBlock<ClientExecuteEvent> = {
            toSafe()?.block()
        }
        this.execute(description, block = safeExecuteBlock)
    }

    protected companion object {
        val mc = Wrapper.minecraft
        val prefix: String get() = CommandConfig.prefix
    }

}