package kr.rendog.client.event.events

import kr.rendog.client.event.Event
import kr.rendog.client.util.graphics.VertexHelper

class RenderRadarEvent(
    val vertexHelper: VertexHelper,
    val radius: Float,
    val scale: Float,
    val chunkLines: Boolean
) : Event