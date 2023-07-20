package kr.rendog.mixin.render;

import kr.rendog.client.event.RendogEventBus;
import kr.rendog.client.event.Phase;
import kr.rendog.client.event.events.RenderEntityEvent;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RenderManager.class, priority = 114514)
public class MixinRenderManager {
    @Inject(method = "renderEntity", at = @At("HEAD"), cancellable = true)
    public void renderEntityPre(Entity entity, double x, double y, double z, float yaw, float partialTicks, boolean debug, CallbackInfo ci) {
        if (entity == null || !RenderEntityEvent.getRenderingEntities()) return;

        RenderEntityEvent eventAll = new RenderEntityEvent.All(entity, Phase.PRE);
        RendogEventBus.INSTANCE.post(eventAll);
        if (eventAll.getCancelled()) ci.cancel();

        if (!(entity instanceof EntityLivingBase)) {
            RenderEntityEvent eventModel = new RenderEntityEvent.Model(entity, Phase.PRE);
            RendogEventBus.INSTANCE.post(eventModel);
        }
    }

    @Inject(method = "renderEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/Render;doRender(Lnet/minecraft/entity/Entity;DDDFF)V", shift = At.Shift.AFTER))
    public void renderEntityPeri(Entity entity, double x, double y, double z, float yaw, float partialTicks, boolean debug, CallbackInfo ci) {
        if (entity == null || !RenderEntityEvent.getRenderingEntities()) return;

        RenderEntityEvent event = new RenderEntityEvent.All(entity, Phase.PERI);
        RendogEventBus.INSTANCE.post(event);

        if (!(entity instanceof EntityLivingBase)) {
            RenderEntityEvent eventModel = new RenderEntityEvent.Model(entity, Phase.POST);
            RendogEventBus.INSTANCE.post(eventModel);
        }
    }

    @Inject(method = "renderEntity", at = @At("RETURN"))
    public void renderEntityPost(Entity entity, double x, double y, double z, float yaw, float partialTicks, boolean debug, CallbackInfo ci) {
        if (entity == null || !RenderEntityEvent.getRenderingEntities()) return;

        RenderEntityEvent event = new RenderEntityEvent.All(entity, Phase.POST);
        RendogEventBus.INSTANCE.post(event);
    }
}
