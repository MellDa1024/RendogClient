package kr.rendog.client.util.combat

import kr.rendog.client.event.SafeClientEvent
import kr.rendog.client.util.text.Color.deColorize
import net.minecraft.entity.player.EntityPlayer
import java.util.regex.Pattern

object HealthUtils {
    fun SafeClientEvent.getRendogMaxHealth(uniqueId : Int) : Int?{
        val entity = mc.world.getEntityByID(uniqueId)
        return if (entity is EntityPlayer) getRendogMaxHealth(entity)
        else null
    }

    fun getRendogMaxHealth(player : EntityPlayer) : Int{
        val hpPattern = Pattern.compile("^\" {3}\\[ 체력 ] :: ([0-9]+) 체력\"")
        var maxHealth = 20
        for (armor in player.armorInventoryList.drop(0)) {
            val nbtTagList = armor.tagCompound?.getCompoundTag("display")?.getTagList("Lore", 8) ?: continue
            for (tag in 0 until nbtTagList.tagCount()) {
                val lore = nbtTagList.get(tag).toString().deColorize()
                val patternedLore = hpPattern.matcher(lore)
                if (patternedLore.find()) maxHealth = maxHealth.plus(patternedLore.group(1).toIntOrNull() ?: 0)
            }
        }
        return maxHealth
    }

    fun SafeClientEvent.getRendogCurrentHealth(uniqueId: Int) : Float? {
        val entity = mc.world.getEntityByID(uniqueId)
        return if (entity is EntityPlayer) getRendogCurrentHealth(entity)
        else null
    }

    fun getRendogCurrentHealth(player: EntityPlayer) : Float{
        val maxHealth = getRendogMaxHealth(player)
        return if (player.health == 20f) maxHealth.toFloat()
        else maxHealth / 20 * player.health
    }
}