package com.rendog.client.module.modules.render

import com.rendog.client.module.Category
import com.rendog.client.module.Module
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.EntityEquipmentSlot

object NoArmor : Module(
    name = "NoArmor",
    category = Category.RENDER,
    description = "Cancel to render armor"
) {
    private val chestplate by setting("Chestplate", true)
    private val leggings by setting("Leggings", true)
    private val boots by setting("Boots", true)

    var entityList = HashSet<Class<out Any>>(); private set

    @JvmStatic
    fun shouldHide(slotIn: EntityEquipmentSlot, entity: EntityLivingBase): Boolean {
        return when (entity) {
            is EntityPlayer -> shouldHidePiece(slotIn)
            else -> false
        }
    }

    private fun shouldHidePiece(slotIn: EntityEquipmentSlot): Boolean {
        return chestplate && slotIn == EntityEquipmentSlot.CHEST
            || leggings && slotIn == EntityEquipmentSlot.LEGS
            || boots && slotIn == EntityEquipmentSlot.FEET
    }
}