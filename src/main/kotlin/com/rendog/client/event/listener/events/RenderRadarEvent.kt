package com.rendog.client.event.listener.events

import com.rendog.client.event.Event
import com.rendog.client.util.graphics.VertexHelper

class RenderRadarEvent(
    val vertexHelper: VertexHelper,
    val radius: Float,
    val scale: Float
) : Event