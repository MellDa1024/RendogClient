package com.lambda.client.module.modules.render

import com.lambda.client.event.Phase
import com.lambda.client.event.events.PacketEvent
import com.lambda.client.event.events.RenderEntityEvent
import com.lambda.client.module.Category
import com.lambda.client.module.Module
import com.lambda.client.util.threads.runSafe
import com.lambda.client.util.threads.safeListener
import com.lambda.client.event.listener.listener
import net.minecraft.block.BlockSnow
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.client.particle.Particle
import net.minecraft.client.particle.ParticleFirework
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.client.tutorial.TutorialSteps
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.effect.EntityLightningBolt
import net.minecraft.entity.item.*
import net.minecraft.entity.monster.EntityMob
import net.minecraft.entity.passive.IAnimals
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.init.MobEffects
import net.minecraft.inventory.EntityEquipmentSlot
import net.minecraft.network.play.server.*
import net.minecraft.tileentity.*
import net.minecraft.util.ResourceLocation
import net.minecraft.world.EnumSkyBlock
import net.minecraftforge.client.event.RenderBlockOverlayEvent
import net.minecraftforge.client.event.RenderBlockOverlayEvent.OverlayType
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.registries.GameData
import org.lwjgl.opengl.GL11.GL_QUADS

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