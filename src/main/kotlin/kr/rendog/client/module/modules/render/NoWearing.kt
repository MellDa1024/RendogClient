package kr.rendog.client.module.modules.render

import kr.rendog.client.event.Phase
import kr.rendog.client.event.events.RenderEntityEvent
import kr.rendog.client.event.listener.listener
import kr.rendog.client.module.Category
import kr.rendog.client.module.Module
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.EntityEquipmentSlot

object NoWearing : Module(
    name = "NoWearing",
    category = Category.RENDER,
    description = "Cancel to render armor and cosmetics instantly"
) {

    private val cosmetic by setting("Cosmetics", false)
    private val chestplate by setting("Chestplate", true)
    private val leggings by setting("Leggings", true)
    private val boots by setting("Boots", true)

    init {
        listener<RenderEntityEvent.All> {
            if (it.phase != Phase.PRE) return@listener
            if (!cosmetic) return@listener
            if (it.entity !is EntityArmorStand) return@listener
            if (it.entity.isCosmetic()) it.cancel()
        }
    }

    @JvmStatic
    fun shouldHide(slotIn: EntityEquipmentSlot, entity: EntityLivingBase): Boolean {
        return when (entity) {
            is EntityPlayer -> shouldHidePiece(slotIn)
            else -> false
        }
    }


    /*
    * Difference with RendogCosmetic and Holographic(or a Citizen)
    * X = NBT Tag doesn't exist
    * +-------------------+-------------------------------+----------------+
    * |                   | RendogCosmetic                | Holographic    |
    * +-------------------+-------------------------------+----------------+
    * | Small             | 0b                            | 1b             |
    * +-------------------+-------------------------------+----------------+
    * | OnGround          | 1b                            | 0b             |
    * +-------------------+-------------------------------+----------------+
    * | Rotation          | *Depends on Player's Rotation | [0f, 0f]       |
    * +-------------------+-------------------------------+----------------+
    * | CustomName        | X                             | O              |
    * +-------------------+-------------------------------+----------------+
    * | ArmorItems        | O                             | {}, {}, {}, {} |
    * +-------------------+-------------------------------+----------------+
    * | NoBasePlate       | 0b                            | 1b             |
    * +-------------------+-------------------------------+----------------+
    * | CustomNameVisible | X                             | 1b             |
    * +-------------------+-------------------------------+----------------+
    */
    private fun EntityArmorStand.isCosmetic(): Boolean {
        return !this.isSmall &&
                this.onGround &&
                !this.hasCustomName() &&
                !this.hasNoBasePlate() &&
                !this.alwaysRenderNameTag

    }

    private fun shouldHidePiece(slotIn: EntityEquipmentSlot): Boolean {
        return cosmetic && slotIn == EntityEquipmentSlot.HEAD
            || chestplate && slotIn == EntityEquipmentSlot.CHEST
            || leggings && slotIn == EntityEquipmentSlot.LEGS
            || boots && slotIn == EntityEquipmentSlot.FEET
    }
}