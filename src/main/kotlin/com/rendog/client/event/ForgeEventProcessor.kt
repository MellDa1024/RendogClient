package com.rendog.client.event

import com.rendog.client.command.CommandManager
import com.rendog.client.event.listener.events.ConnectionEvent
import com.rendog.client.event.listener.events.RenderWorldEvent
import com.rendog.client.event.listener.events.ResolutionUpdateEvent
import com.rendog.client.gui.mc.RendogGuiChat
import com.rendog.client.module.ModuleManager
import com.rendog.client.util.Wrapper
import com.rendog.client.util.graphics.RendogTessellator
import com.rendog.client.util.graphics.ProjectionUtils
import com.rendog.client.util.text.MessageDetection
import net.minecraftforge.client.event.*
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.event.world.ChunkEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.InputEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent
import org.lwjgl.input.Keyboard

internal object ForgeEventProcessor {
    private val mc = Wrapper.minecraft
    private var prevWidth = mc.displayWidth
    private var prevHeight = mc.displayHeight

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase == TickEvent.Phase.START) {
            mc.profiler.startSection("kbTickPre")
        } else {
            mc.profiler.startSection("kbTickPost")
        }

        RendogEventBus.postProfiler(event)

        if (event.phase == TickEvent.Phase.END && (prevWidth != mc.displayWidth || prevHeight != mc.displayHeight)) {
            prevWidth = mc.displayWidth
            prevHeight = mc.displayHeight
            RendogEventBus.post(ResolutionUpdateEvent(mc.displayWidth, mc.displayHeight))
        }

        mc.profiler.endSection()
    }

    @SubscribeEvent
    @Suppress("UNUSED_PARAMETER")
    fun onWorldRender(event: RenderWorldLastEvent) {
        ProjectionUtils.updateMatrix()
        RendogTessellator.prepareGL()
        RendogEventBus.post(RenderWorldEvent())
        RendogTessellator.releaseGL()
    }

    @SubscribeEvent
    fun onRenderPre(event: RenderGameOverlayEvent.Pre) {
        RendogEventBus.post(event)
    }

    @SubscribeEvent
    fun onRender(event: RenderGameOverlayEvent.Post) {
        RendogEventBus.post(event)
    }

    @SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
    fun onKeyInput(event: InputEvent.KeyInputEvent) {
        if (!Keyboard.getEventKeyState()) return

        if (!mc.gameSettings.keyBindSneak.isKeyDown) {
            val prefix = CommandManager.prefix
            val typedChar = Keyboard.getEventCharacter().toString()
            if (prefix.length == 1 && typedChar.equals(CommandManager.prefix, true)) {
                mc.displayGuiScreen(RendogGuiChat(CommandManager.prefix))
            }
        }

        RendogEventBus.post(event)
        ModuleManager.onBind(Keyboard.getEventKey())
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onChatSent(event: ClientChatEvent) {
        if (MessageDetection.Command.RENDOG detect event.message) {
            CommandManager.runCommand(event.message.removePrefix(CommandManager.prefix))
            event.isCanceled = true
        }
    }

    @SubscribeEvent
    fun onEventMouse(event: InputEvent.MouseInputEvent) {
        RendogEventBus.post(event)
    }

    @SubscribeEvent
    fun onChunkLoaded(event: ChunkEvent.Unload) {
        RendogEventBus.post(event)
    }

    @SubscribeEvent
    fun onInputUpdate(event: InputUpdateEvent) {
        RendogEventBus.post(event)
    }

    @SubscribeEvent
    fun onLivingEntityUseItemEventTick(event: LivingEntityUseItemEvent.Tick) {
        RendogEventBus.post(event)
    }

    @SubscribeEvent
    fun onLeftClickBlock(event: PlayerInteractEvent.LeftClickBlock) {
        RendogEventBus.post(event)
    }

    @SubscribeEvent
    fun onRenderBlockOverlay(event: RenderBlockOverlayEvent) {
        RendogEventBus.post(event)
    }

    @SubscribeEvent
    fun onClientChat(event: ClientChatReceivedEvent) {
        RendogEventBus.post(event)
    }

    @SubscribeEvent
    @Suppress("UNUSED_PARAMETER")
    fun onServerDisconnect(event: FMLNetworkEvent.ServerDisconnectionFromClientEvent) {
        RendogEventBus.post(ConnectionEvent.Disconnect())
    }

    @SubscribeEvent
    @Suppress("UNUSED_PARAMETER")
    fun onClientDisconnect(event: FMLNetworkEvent.ClientDisconnectionFromServerEvent) {
        RendogEventBus.post(ConnectionEvent.Disconnect())
    }

    @SubscribeEvent
    @Suppress("UNUSED_PARAMETER")
    fun onClientConnect(event: FMLNetworkEvent.ClientConnectedToServerEvent) {
        RendogEventBus.post(ConnectionEvent.Connect())
    }

    @SubscribeEvent
    fun onRenderFogColors(event: EntityViewRenderEvent.FogColors) {
        RendogEventBus.post(event)
    }
}
