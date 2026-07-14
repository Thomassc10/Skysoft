package com.skysoft.utils

import net.minecraft.client.Minecraft
import net.minecraft.world.entity.Entity

object LocationUtilities {
    fun Entity.distanceToPlayer(): Double {
        val player = Minecraft.getInstance().player ?: return Double.MAX_VALUE
        return distanceTo(player).toDouble()
    }
}
