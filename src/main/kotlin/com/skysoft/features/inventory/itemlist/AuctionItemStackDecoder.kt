package com.skysoft.features.inventory.itemlist

import com.mojang.datafixers.DataFixer
import com.mojang.serialization.Dynamic
import com.mojang.serialization.DynamicOps
import com.skysoft.features.inventory.StorageRuntime
import com.skysoft.features.inventory.registryOps
import java.io.ByteArrayInputStream
import java.util.Base64
import net.minecraft.SharedConstants
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtAccounter
import net.minecraft.nbt.NbtIo
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.Tag
import net.minecraft.util.datafix.DataFixers
import net.minecraft.util.datafix.fixes.References
import net.minecraft.world.item.ItemStack

internal fun decodeAuctionItemStack(encoded: String, ops: DynamicOps<Tag> = registryOps()): ItemStack {
    val tag = migrateAuctionItemTag(encoded)
    return ItemStack.CODEC.parse(ops, tag)
        .resultOrPartial { error -> throw IllegalArgumentException("Failed to decode auction item: $error") }
        .orElseThrow { IllegalArgumentException("Failed to decode auction item") }
}

internal fun migrateAuctionItemTag(
    encoded: String,
    dataFixer: DataFixer = DataFixers.getDataFixer(),
    currentDataVersion: Int = SharedConstants.getCurrentVersion().dataVersion().version(),
): CompoundTag {
    require(encoded.length <= MAXIMUM_ENCODED_ITEM_LENGTH) { "Auction item data is too large" }
    val bytes = Base64.getDecoder().decode(encoded)
    val root = NbtIo.readCompressed(
        ByteArrayInputStream(bytes),
        NbtAccounter.create(StorageRuntime.MAX_ITEM_NBT_BYTES),
    )
    val item = root.getList("i").orElseThrow { IllegalArgumentException("Auction item list is missing") }
        .getCompound(0).orElseThrow { IllegalArgumentException("Auction item is missing") }
    val currentComponents = item.getCompound("components").orElse(null)
    val migrated = dataFixer.update(
        References.ITEM_STACK,
        Dynamic(NbtOps.INSTANCE, item),
        LEGACY_ITEM_DATA_VERSION,
        currentDataVersion,
    ).value as? CompoundTag ?: throw IllegalArgumentException("Auction item migration returned invalid data")
    if (currentComponents != null) {
        val components = migrated.getCompoundOrEmpty("components").copy().apply { merge(currentComponents) }
        migrated.put("components", components)
    }
    migrated.getCompoundOrEmpty("components")
        .getCompound("minecraft:custom_data")
        .orElse(null)
        ?.getCompound("ExtraAttributes")
        ?.orElse(null)
        ?.let { extraAttributes ->
            val customData = migrated.getCompoundOrEmpty("components")
                .getCompoundOrEmpty("minecraft:custom_data")
                .copy()
                .apply {
                    remove("ExtraAttributes")
                    merge(extraAttributes)
                }
            migrated.getCompoundOrEmpty("components").put("minecraft:custom_data", customData)
        }
    return migrated
}

private const val LEGACY_ITEM_DATA_VERSION = -1
private const val MAXIMUM_ENCODED_ITEM_LENGTH = 2_000_000
