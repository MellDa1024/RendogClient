package com.rendog.mixin;

import com.rendog.client.event.RendogEventBus;
import com.rendog.client.event.listener.events.GuiEvent;
import com.rendog.client.event.listener.events.RunGameLoopEvent;
import com.rendog.client.gui.hudgui.elements.misc.FPS;
import com.rendog.client.util.Wrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.settings.GameSettings;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {

    @Shadow public WorldClient world;
    @Shadow public EntityPlayerSP player;
    @Shadow public GuiScreen currentScreen;
    @Shadow public GameSettings gameSettings;
    @Shadow public PlayerControllerMP playerController;
    @Shadow private int fpsCounter;
    private boolean handActive = false;
    private boolean isHittingBlock = false;

    @Shadow
    protected abstract void clickMouse();

    @ModifyVariable(method = "displayGuiScreen", at = @At("HEAD"), argsOnly = true)
    public GuiScreen editDisplayGuiScreen(GuiScreen guiScreenIn) {
        GuiEvent.Closed screenEvent = new GuiEvent.Closed(this.currentScreen);
        RendogEventBus.INSTANCE.post(screenEvent);
        GuiEvent.Displayed screenEvent1 = new GuiEvent.Displayed(guiScreenIn);
        RendogEventBus.INSTANCE.post(screenEvent1);
        return screenEvent1.getScreen();
    }

    @Inject(method = "runGameLoop", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Timer;updateTimer()V", shift = At.Shift.BEFORE))
    public void runGameLoopStart(CallbackInfo ci) {
        Wrapper.getMinecraft().profiler.startSection("lambda");
        RendogEventBus.INSTANCE.post(new RunGameLoopEvent.Start());
        Wrapper.getMinecraft().profiler.endSection();
    }

    @Inject(method = "runGameLoop", at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/Profiler;endSection()V", ordinal = 0, shift = At.Shift.BEFORE))
    public void runGameLoopTick(CallbackInfo ci) {
        Wrapper.getMinecraft().profiler.endStartSection("lambda");
        RendogEventBus.INSTANCE.post(new RunGameLoopEvent.Tick());
        Wrapper.getMinecraft().profiler.endStartSection("scheduledExecutables");
    }

    @Inject(method = "runGameLoop", at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/Profiler;startSection(Ljava/lang/String;)V", ordinal = 2, shift = At.Shift.BEFORE))
    public void runGameLoopRender(CallbackInfo ci) {
        Wrapper.getMinecraft().profiler.startSection("lambda");
        RendogEventBus.INSTANCE.post(new RunGameLoopEvent.Render());
        Wrapper.getMinecraft().profiler.endSection();
    }

    @Inject(method = "runGameLoop", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;isFramerateLimitBelowMax()Z", shift = At.Shift.BEFORE))
    public void runGameLoopEnd(CallbackInfo ci) {
        Wrapper.getMinecraft().profiler.startSection("lambda");
        RendogEventBus.INSTANCE.post(new RunGameLoopEvent.End());
        Wrapper.getMinecraft().profiler.endSection();
    }

    @Inject(method = "runGameLoop", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;debugFPS:I", opcode = Opcodes.PUTSTATIC))
    public void runGameLoopPutFieldDebugFPS(CallbackInfo ci) {
        FPS.updateFps(this.fpsCounter);
    }


    @Inject(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;displayCrashReport(Lnet/minecraft/crash/CrashReport;)V", shift = At.Shift.BEFORE))
    public void displayCrashReport(CallbackInfo info) {
        Wrapper.saveAndShutdown();
    }

    @Inject(method = "shutdown", at = @At("HEAD"))
    public void shutdown(CallbackInfo info) {
        Wrapper.saveAndShutdown();
    }

    @Inject(method = "setIngameFocus", at = @At("HEAD"), cancellable = true)
    public void setIngameFocus(CallbackInfo info) {
        if (currentScreen instanceof GuiContainer) {
            info.cancel();
        }
    }

}