package com.rendog.client.module.modules.client

import com.rendog.client.module.Category
import com.rendog.client.module.Module

object Tooltips : Module(
    name = "Tooltips",
    description = "Displays handy module descriptions in the GUI",
    category = Category.CLIENT,
    showOnArray = false,
    enabledByDefault = true
)
