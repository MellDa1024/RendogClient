package com.lambda.mixin.render;

import com.lambda.client.event.LambdaEventBus;
import com.lambda.client.event.events.RenderOverlayEvent;
import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EntityRenderer.class, priority = Integer.MAX_VALUE)
public class MixinEntityRenderer {

    @Inject(method = "updateCameraAndRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiIngame;renderGameOverlay(F)V", shift = At.Shift.AFTER))
    public void updateCameraAndRender(float partialTicks, long nanoTime, CallbackInfo ci) {
        LambdaEventBus.INSTANCE.post(new RenderOverlayEvent());
    }
}
