package kr.rendog.client.module.modules.render

import kr.rendog.client.module.Category
import kr.rendog.client.module.Module
import net.minecraft.client.entity.AbstractClientPlayer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumHand
import net.minecraft.util.EnumHandSide

object ItemModel : Module(
    name = "ItemModel",
    description = "Modify hand item rendering in first person",
    category = Category.RENDER,
    alias = arrayOf("ViewModel", "SmallShield", "LowerOffhand")
) {
    private val page by setting("Page", Page.POSITION)

    private val posX by setting("Pos X", 0.0f, -5.0f..5.0f, 0.025f, { page == Page.POSITION })
    private val posY by setting("Pos Y", 0.0f, -5.0f..5.0f, 0.025f, { page == Page.POSITION })
    private val posZ by setting("Pos Z", 0.0f, -5.0f..5.0f, 0.025f, { page == Page.POSITION })

    private val rotateX by setting("Rotate X", 0.0f, -180.0f..180.0f, 1.0f, { page == Page.ROTATION })
    private val rotateY by setting("Rotate Y", 0.0f, -180.0f..180.0f, 1.0f, { page == Page.ROTATION })
    private val rotateZ by setting("Rotate Z", 0.0f, -180.0f..180.0f, 1.0f, { page == Page.ROTATION })

    private val scale by setting("Scale", 1.0f, 0.1f..3.0f, 0.025f, { page == Page.SCALE })

    private val modifyHand by setting("Modify Hand", false)

    private enum class Page {
        POSITION, ROTATION, SCALE
    }

    @JvmStatic
    fun translate(stack: ItemStack, hand: EnumHand, player: AbstractClientPlayer) {
        if (isDisabled || !modifyHand && stack.isEmpty) return

        val enumHandSide = getEnumHandSide(player, hand)

        if (enumHandSide == EnumHandSide.RIGHT) {
            translate(posX, posY, posZ, -1.0f)
        }
    }

    private fun translate(x: Float, y: Float, z: Float, sideMultiplier: Float) {
        GlStateManager.translate(x * sideMultiplier, y, -z)
    }

    @JvmStatic
    fun rotateAndScale(stack: ItemStack, hand: EnumHand, player: AbstractClientPlayer) {
        if (isDisabled || !modifyHand && stack.isEmpty) return

        val enumHandSide = getEnumHandSide(player, hand)
        if (enumHandSide == EnumHandSide.RIGHT) {
              rotate(rotateX, rotateY, rotateZ, -1.0f)
              GlStateManager.scale(scale, scale, scale)
        }
    }

    private fun rotate(x: Float, y: Float, z: Float, sideMultiplier: Float) {
        GlStateManager.rotate(x, 1.0f, 0.0f, 0.0f)
        GlStateManager.rotate(y * sideMultiplier, 0.0f, 1.0f, 0.0f)
        GlStateManager.rotate(z * sideMultiplier, 0.0f, 0.0f, 1.0f)
    }

    private fun getEnumHandSide(player: AbstractClientPlayer, hand: EnumHand): EnumHandSide =
        if (hand == EnumHand.MAIN_HAND) player.primaryHand else player.primaryHand.opposite()
}