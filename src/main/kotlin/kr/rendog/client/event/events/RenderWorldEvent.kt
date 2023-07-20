package kr.rendog.client.event.events

import kr.rendog.client.event.Event
import kr.rendog.client.event.ProfilerEvent
import kr.rendog.client.mixin.extension.renderPosX
import kr.rendog.client.mixin.extension.renderPosY
import kr.rendog.client.mixin.extension.renderPosZ
import kr.rendog.client.util.Wrapper
import kr.rendog.client.util.graphics.RendogTessellator

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