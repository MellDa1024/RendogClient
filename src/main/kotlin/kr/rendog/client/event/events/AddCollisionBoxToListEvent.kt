package kr.rendog.client.event.events

import kr.rendog.client.event.Event
import net.minecraft.util.math.AxisAlignedBB

/**
 * @author Doogie13
 * @since 06/10/2022
 */
class AddCollisionBoxToListEvent(val collisionBoxList : MutableList<AxisAlignedBB>) : Event