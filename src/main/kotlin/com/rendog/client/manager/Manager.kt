package com.rendog.client.manager

import com.rendog.client.util.Wrapper

interface Manager {
    val mc get() = Wrapper.minecraft
}