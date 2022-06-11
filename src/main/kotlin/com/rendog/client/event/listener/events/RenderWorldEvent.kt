package com.rendog.client.event.listener.events

import com.rendog.client.event.Event
import com.rendog.client.event.ProfilerEvent
import com.rendog.client.mixin.extension.renderPosX
import com.rendog.client.mixin.extension.renderPosY
import com.rendog.client.mixin.extension.renderPosZ
import com.rendog.client.util.Wrapper
import com.rendog.client.util.graphics.RendogTessellator

class RenderWorldEvent : Event, ProfilerEvent {
    override val profilerName: String = "kbRender3D"

    init {
        RendogTessellator.buffer.setTranslation(
            -Wrapper.minecraft.renderManager.renderPosX,
            -Wrapper.minecraft.renderManager.renderPosY,
            -Wrapper.minecraft.renderManager.renderPosZ
        )
    }
}