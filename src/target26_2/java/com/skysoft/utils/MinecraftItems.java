package com.skysoft.utils;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

public final class MinecraftItems {
    private MinecraftItems() {
    }

    public static List<Item> stainedGlassPanes() {
        return Blocks.STAINED_GLASS_PANE.asList().stream()
            .map(Block::asItem)
            .toList();
    }

    public static Item grayDye() {
        return Items.DYE.gray();
    }
}
