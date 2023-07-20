package kr.rendog.client.module.modules.client

import kr.rendog.client.RendogMod
import kr.rendog.client.event.events.ModuleToggleEvent
import kr.rendog.client.event.listener.listener
import kr.rendog.client.module.Category
import kr.rendog.client.module.Module
import kr.rendog.client.util.TickTimer
import kr.rendog.client.util.text.MessageSendHelper
import kr.rendog.client.util.text.format
import net.minecraft.util.text.TextFormatting
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.lwjgl.opengl.Display

object CommandConfig : Module(
    name = "CommandConfig",
    description = "Configures client chat related stuff",
    category = Category.CLIENT,
    showOnArray = false,
    alwaysEnabled = true
) {
    var prefix by setting("Prefix", ";", { false })
    val toggleMessages by setting("Toggle Messages", false)
    private val customTitle = setting("Window Title", true)

    private val timer = TickTimer()
    private val prevTitle = Display.getTitle()
    private const val title = "${RendogMod.NAME} ${RendogMod.RENDOG} ${RendogMod.VERSION}"

    init {
        listener<ModuleToggleEvent> {
            if (!toggleMessages || it.module == ClickGUI) return@listener

            MessageSendHelper.sendChatMessage(it.module.name +
                if (it.module.isEnabled) TextFormatting.RED format " disabled"
                else TextFormatting.GREEN format " enabled"
            )
        }

        listener<TickEvent.ClientTickEvent> {
            if (timer.tick(10000L)) {
                if (customTitle.value) Display.setTitle("$title - ${mc.session.username}")
                else Display.setTitle(prevTitle)
            }
        }

        customTitle.listeners.add {
            timer.reset(-0xCAFEBABE)
        }
    }
}