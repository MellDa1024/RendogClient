package kr.rendog.client.manager

import kr.rendog.client.util.Wrapper

interface Manager {
    val mc get() = Wrapper.minecraft
}