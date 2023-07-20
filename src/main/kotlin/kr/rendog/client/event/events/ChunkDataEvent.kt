package kr.rendog.client.event.events

import kr.rendog.client.event.Event
import net.minecraft.world.chunk.Chunk

/**
 * Event emitted when chunk data is read
 */
class ChunkDataEvent(val isFullChunk: Boolean, val chunk: Chunk): Event