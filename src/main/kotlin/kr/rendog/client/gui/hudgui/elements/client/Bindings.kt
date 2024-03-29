package kr.rendog.client.gui.hudgui.elements.client

import kr.rendog.client.commons.interfaces.DisplayEnum
import kr.rendog.client.event.SafeClientEvent
import kr.rendog.client.gui.hudgui.LabelHud
import kr.rendog.client.module.AbstractModule
import kr.rendog.client.module.ModuleManager
import kr.rendog.client.util.color.ColorHolder
import kr.rendog.client.util.graphics.font.HAlign
import kr.rendog.client.util.threads.safeAsyncListener
import net.minecraftforge.fml.common.gameevent.TickEvent

internal object Bindings : LabelHud(
    name = "Bindings",
    category = Category.CLIENT,
    description = "Display current module keybindings"
) {

    private val sortingMode by setting("Sorting Mode", SortingMode.LENGTH)
    private val ignoreClientBindings by setting("Ignore Client Category", true,
        description = "Ignore bindings for client specific bindings like the ClickGUI")

    @Suppress("UNUSED")
    private enum class SortingMode(
        override val displayName: String,
        val comparator: Comparator<AbstractModule>
    ) : DisplayEnum {
        LENGTH("Length", compareByDescending { it.name.length }),
        ALPHABET("Alphabet", compareBy { it.name }),
        CATEGORY("Category", compareBy { it.category.ordinal })
    }

    private var modulesWithBindings: List<AbstractModule> = emptyList()

    init {
        dockingH = HAlign.RIGHT

        safeAsyncListener<TickEvent.ClientTickEvent> { event ->
            if (event.phase != TickEvent.Phase.END) return@safeAsyncListener

            // this isn't terribly efficient, consider creating events for editing bindings and module toggle state
            modulesWithBindings = ModuleManager.modules
                .sortedWith(sortingMode.comparator)
                .filter { if (ignoreClientBindings) it.category != kr.rendog.client.module.Category.CLIENT else true }
                .filterNot { it.bind.value.isEmpty }
        }
    }

    override fun SafeClientEvent.updateText() {
        modulesWithBindings.forEach {
            displayText.add(it.name, if (it.isEnabled) ColorHolder(0, 255, 0) else primaryColor)
            displayText.addLine(it.bind.toString(), secondaryColor)
        }
    }
}