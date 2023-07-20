package kr.rendog.mixin.player;

import kr.rendog.client.event.RendogEventBus;
import kr.rendog.client.event.events.PlayerAttackEvent;
import kr.rendog.client.event.events.WindowClickEvent;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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

    @Inject(method = "windowClick", at = @At("HEAD"), cancellable = true)
    public void onWindowClick(int windowId, int slotId, int mouseButton, ClickType type, EntityPlayer player, CallbackInfoReturnable<ItemStack> cir) {
        WindowClickEvent event = new WindowClickEvent(windowId, slotId, mouseButton, type);
        RendogEventBus.INSTANCE.post(event);
        if (event.getCancelled()) {
            cir.cancel();
        }
    }
}
