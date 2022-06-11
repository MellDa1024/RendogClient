package com.rendog.client.module.modules.client

import com.rendog.client.RendogMod
import com.rendog.client.event.listener.events.ModuleToggleEvent
import com.rendog.client.module.Category
import com.rendog.client.module.Module
import com.rendog.client.util.TickTimer
import com.rendog.client.util.text.MessageSendHelper
import com.rendog.client.util.text.format
import com.rendog.client.event.listener.listener
import net.minecraft.util.text.TextFormatting
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.lwjgl.opengl.Display

object CommandConfig : Module(
    name = "CommandConfig",
    category = Category.CLIENT,
    description = "Configures client chat related stuff",
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