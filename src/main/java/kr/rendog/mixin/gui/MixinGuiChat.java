package kr.rendog.mixin.gui;

import kr.rendog.client.gui.mc.KoreanGuiChat;
import kr.rendog.client.gui.mc.RendogGuiChat;
import kr.rendog.client.util.Wrapper;
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

    @Inject(method = "initGui", at = @At("RETURN"))
    public void returnInitGui(CallbackInfo info) {
        GuiScreen currentScreen = Wrapper.getMinecraft().currentScreen;
        if (currentScreen instanceof GuiChat) {
            if (!(currentScreen instanceof KoreanGuiChat) && !(currentScreen instanceof RendogGuiChat)) {
                Wrapper.getMinecraft().displayGuiScreen(
                    new KoreanGuiChat(inputField.getText(), null, historyBuffer, sentHistoryCursor)
                );
            }
        }
    }
}
