package kr.rendog.client.manager.managers

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kr.rendog.client.RendogMod
import kr.rendog.client.command.CommandManager
import kr.rendog.client.event.listener.listener
import kr.rendog.client.manager.Manager
import kr.rendog.client.util.ConfigUtils
import kr.rendog.client.util.FolderUtils
import kr.rendog.client.util.text.MessageSendHelper
import net.minecraftforge.fml.common.gameevent.InputEvent
import org.lwjgl.input.Keyboard
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.*

object MacroManager : Manager {
    private var macroMap = TreeMap<Int, ArrayList<String>>()
    val isEmpty get() = macroMap.isEmpty()
    val macros: Map<Int, List<String>> get() = macroMap

    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val type = object : TypeToken<TreeMap<Int, List<String>>>() {}.type
    private val file get() = File(FolderUtils.rendogFolder + "macros.json")

    init {
        listener<InputEvent.KeyInputEvent> {
            sendMacro(Keyboard.getEventKey())
        }
    }

    fun loadMacros(): Boolean {
        ConfigUtils.fixEmptyJson(file)

        return try {
            FileReader(file).buffered().use {
                macroMap = gson.fromJson(it, type)
            }
            RendogMod.LOG.info("Macro loaded")
            true
        } catch (e: Exception) {
            RendogMod.LOG.warn("Failed loading macro", e)
            false
        }
    }

    fun saveMacros(): Boolean {
        return try {
            FileWriter(file, false).buffered().use {
                gson.toJson(macroMap, it)
            }
            RendogMod.LOG.info("Macro saved")
            true
        } catch (e: Exception) {
            RendogMod.LOG.warn("Failed saving macro", e)
            false
        }
    }

    /**
     * Sends the message or command, depending on which one it is
     * @param keyCode int keycode of the key the was pressed
     */
    private fun sendMacro(keyCode: Int) {
        val macros = getMacros(keyCode) ?: return
        for (macro in macros) {
            if (macro.startsWith(CommandManager.prefix)) { // this is done instead of just sending a chat packet so it doesn't add to the chat history
                MessageSendHelper.sendRendogCommand(macro) // ie, the false here
            } else {
                MessageManager.sendMessageDirect(macro)
            }
        }
    }

    private fun getMacros(keycode: Int) = macroMap[keycode]

    fun addMacroToKey(keycode: Int, macro: String) {
        macroMap.getOrPut(keycode, ::ArrayList).add(macro)
    }

    fun removeMacro(keycode: Int) {
        macroMap.remove(keycode)
    }

}