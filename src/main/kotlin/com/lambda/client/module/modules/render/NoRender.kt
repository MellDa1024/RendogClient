package com.lambda.client.module.modules.render

import com.lambda.client.event.events.PacketEvent
import com.lambda.client.event.listener.listener
import com.lambda.client.module.Category
import com.lambda.client.module.Module
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.monster.EntityMob
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.EntityEquipmentSlot
import net.minecraft.network.play.server.SPacketParticles
import net.minecraft.world.EnumSkyBlock

object NoRender : Module(
    name = "NoRender",
    category = Category.RENDER,
    description = "Ignore entity spawn packets"
) {
    private val page by setting("Page", Page.OTHER)

    //Armor
    private val armorPlayer by setting("Players", false, { page == Page.ARMOR })
    private val armorStands by setting("Armour Stands", false, { page == Page.ARMOR })
    private val armorMobs by setting("Mobs", false, { page == Page.ARMOR })
    private val helmet by setting("Helmet", true, { page == Page.ARMOR })
    private val chestplate by setting("Chestplate", true, { page == Page.ARMOR })
    private val leggings by setting("Leggings", true, { page == Page.ARMOR })
    private val boots by setting("Boots", true, { page == Page.ARMOR })

    // Others
    private val particles = setting("Particles", false, { page == Page.OTHER })
    private val allLightingUpdates by setting("All Lighting Updates", false, { page == Page.OTHER })
    private val skylight by setting("SkyLight Updates", false, { page == Page.OTHER && !allLightingUpdates })

    private enum class Page {
        ARMOR, OTHER
    }

    var entityList = HashSet<Class<out Any>>(); private set

    init {
        listener<PacketEvent.Receive> {
            if (particles.value && it.packet is SPacketParticles) {
                it.cancel()
                return@listener
            }
        }
    }

    fun handleLighting(lightType: EnumSkyBlock): Boolean {
        return isEnabled && (skylight && lightType == EnumSkyBlock.SKY || allLightingUpdates)
    }

    @JvmStatic
    fun shouldHide(slotIn: EntityEquipmentSlot, entity: EntityLivingBase): Boolean {
        return when (entity) {
            is EntityPlayer -> armorPlayer && shouldHidePiece(slotIn)
            is EntityArmorStand -> armorStands && shouldHidePiece(slotIn)
            is EntityMob -> armorMobs && shouldHidePiece(slotIn)
            else -> false
        }
    }

    private fun shouldHidePiece(slotIn: EntityEquipmentSlot): Boolean {
        return helmet && slotIn == EntityEquipmentSlot.HEAD
            || chestplate && slotIn == EntityEquipmentSlot.CHEST
            || leggings && slotIn == EntityEquipmentSlot.LEGS
            || boots && slotIn == EntityEquipmentSlot.FEET
    }
}