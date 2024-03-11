package kr.rendog.client.module.modules.chat

import kr.rendog.mixin.gui.MixinGuiChat
import kr.rendog.client.gui.mc.KoreanGuiChat
import kr.rendog.client.gui.mc.KoreanGuiTextField
import kr.rendog.client.module.Category
import kr.rendog.client.module.Module

object KoreanChat : Module(
    name = "KoreanChat",
    description = "KoreanChat settings",
    category = Category.CHAT,
    alwaysEnabled = true,
    showOnArray = false
) {
    var language by setting("Language", Language.KOREAN, description = "Language to use, you can use left ctrl key to switch.")
    var ctrlComboBypass by setting("CtrlComboBypass", true, description =  "Force to stop switching korean/english when player uses a ctrl combokey.")
    val debugging by setting("Debugging", false, description = "Logs something. I do not recommend to enable this without any reason.")

    enum class Language {
        KOREAN, ENGLISH;

        fun switch() {
            if (this == KOREAN) language = ENGLISH
            if (this == ENGLISH) language = KOREAN
        }
    }
    /**
     * @see KoreanGuiChat
     * @see KoreanGuiTextField
     * @see MixinGuiChat
     */
}
