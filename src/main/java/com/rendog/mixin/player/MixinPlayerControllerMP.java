package com.rendog.mixin.player;

import com.rendog.client.event.RendogEventBus;
import com.rendog.client.event.listener.events.PlayerAttackEvent;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerControllerMP.class)
public class MixinPlayerControllerMP {

    @Inject(method = "attackEntity", at = @At("HEAD"), cancellable = true)
    public void attackEntity(EntityPlayer playerIn, Entity targetEntity, CallbackInfo ci) {
        if (targetEntity == null) return;
        PlayerAttackEvent event = new PlayerAttackEvent(targetEntity);
        RendogEventBus.INSTANCE.post(event);
        if (event.getCancelled()) {
            ci.cancel();
        }
    }
}
