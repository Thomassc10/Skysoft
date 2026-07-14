package com.skysoft.utils

import com.skysoft.utils.TextUtilities.cleanSkyBlockText
import net.minecraft.world.entity.Entity

object EntityUtilities {
    fun Entity.cleanName(): String = name.cleanSkyBlockText()
}
