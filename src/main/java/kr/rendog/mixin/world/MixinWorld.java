package kr.rendog.mixin.world;

import kr.rendog.client.module.modules.render.TimeWarp;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = World.class, priority = Integer.MAX_VALUE)
public class MixinWorld {
    @Inject(method = "getWorldTime", at = @At("HEAD"), cancellable = true)
    public void onGetWorldTime(CallbackInfoReturnable<Long> cir) {
        if (TimeWarp.INSTANCE.isEnabled())
            cir.setReturnValue(TimeWarp.INSTANCE.getUpdatedTime());
    }
}
