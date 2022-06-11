package com.rendog.mixin.render;

import com.rendog.client.event.RendogEventBus;
import com.rendog.client.event.Phase;
import com.rendog.client.event.listener.events.RenderEntityEvent;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RenderLivingBase.class, priority = 114514)
public class MixinRenderLivingBase<T extends EntityLivingBase> {
    @Inject(method = "renderModel", at = @At("HEAD"), cancellable = true)
    public void renderModelHead(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, CallbackInfo ci) {
        if (entity == null || !RenderEntityEvent.getRenderingEntities()) return;

        RenderEntityEvent eventModel = new RenderEntityEvent.Model(entity, Phase.PRE);
        RendogEventBus.INSTANCE.post(eventModel);
    }

    @Inject(method = "renderModel", at = @At("RETURN"), cancellable = true)
    public void renderEntityReturn(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, CallbackInfo ci) {
        if (entity == null || !RenderEntityEvent.getRenderingEntities()) return;

        RenderEntityEvent eventModel = new RenderEntityEvent.Model(entity, Phase.POST);
        RendogEventBus.INSTANCE.post(eventModel);
    }
}
