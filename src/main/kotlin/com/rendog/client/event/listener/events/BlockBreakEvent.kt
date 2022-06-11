package com.rendog.client.event.listener.events

import com.rendog.client.event.Event
import net.minecraft.util.math.BlockPos

class BlockBreakEvent(val breakerID: Int, val position: BlockPos, val progress: Int) : Event