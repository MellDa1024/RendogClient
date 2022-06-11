package com.rendog.mixin.gui;

import com.rendog.client.command.CommandManager;
import com.rendog.client.gui.mc.RendogGuiChat;
import com.rendog.client.util.Wrapper;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiChat.class)
public abstract class MixinGuiChat extends GuiScreen {

    @Shadow protected GuiTextField inputField;
    @Shadow private String historyBuffer;
    @Shadow private int sentHistoryCursor;

    @Inject(method = "keyTyped(CI)V", at = @At("RETURN"))
    public void returnKeyTyped(char typedChar, int keyCode, CallbackInfo info) {
        GuiScreen currentScreen = Wrapper.getMinecraft().currentScreen;
        if (currentScreen instanceof GuiChat && !(currentScreen instanceof RendogGuiChat)
            && inputField.getText().startsWith(CommandManager.INSTANCE.getPrefix())) {
            Wrapper.getMinecraft().displayGuiScreen(new RendogGuiChat(inputField.getText(), historyBuffer, sentHistoryCursor));
        }
    }

}
