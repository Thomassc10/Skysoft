package com.skysoft.data.skyblock

import com.skysoft.data.skyblock.SkyBlockItemId.skyBlockId
import net.minecraft.world.item.ItemStack

object SkyBlockMenuItem {
    const val SKYBLOCK_MENU_ID = "SKYBLOCK_MENU"

    fun ItemStack.isSkyBlockMenu(): Boolean = skyBlockId() == SKYBLOCK_MENU_ID
}
