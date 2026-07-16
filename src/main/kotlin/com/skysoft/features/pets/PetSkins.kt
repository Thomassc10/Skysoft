package com.skysoft.features.pets

import com.google.gson.JsonObject
import net.minecraft.nbt.CompoundTag

internal object PetSkins {
    fun animatedTexture(skinInternalName: String, variantIndex: Int?): String? =
        animated(skinInternalName, variantIndex)?.textures?.firstOrNull()

    fun animated(skinInternalName: String, variantIndex: Int?, displayIconTexture: String? = null): AnimatedSkinJson? {
        val animated = PetRepoCache.animatedSkullsJson ?: return null
        if (variantIndex != null && variantIndex >= 0) {
            val variant = animated.petSkinVariants[skinInternalName]?.getOrNull(variantIndex)
            if (variant != null) return animated.skins[variant]
        }
        displayIconTexture?.let { animationMatchingTexture(animated, skinInternalName, it) }?.let { return it }
        return animated.skins[skinInternalName]
    }

    private fun animationMatchingTexture(
        animated: SkysoftAnimatedSkullsRepoJson,
        skinInternalName: String,
        displayIconTexture: String,
    ): AnimatedSkinJson? {
        val texture = displayIconTexture.substringAfter(':')
        val cacheKey = "$skinInternalName:$texture"
        PetRepoCache.animatedSkinMatches[cacheKey]?.let { return it }
        if (!PetRepoCache.missingAnimatedSkinMatches.add(cacheKey)) return null
        val match = animated.skins.asSequence()
            .filter { (internalName) ->
                internalName == skinInternalName || internalName.startsWith("${skinInternalName}_")
            }
            .map { it.value }
            .firstOrNull { animation ->
                animation.textures.any { it.substringAfter(':') == texture }
            } ?: return null
        PetRepoCache.missingAnimatedSkinMatches.remove(cacheKey)
        PetRepoCache.animatedSkinMatches[cacheKey] = match
        return match
    }

    fun isSkinForPet(internalName: String, properName: String): Boolean {
        val skinName = internalName.removePrefix("PET_SKIN_")
        return skinNamePrefixes(properName).any { prefix ->
            skinName == prefix || skinName.startsWith("${prefix}_")
        }
    }

    fun variantIndex(extraData: CompoundTag): Int? =
        PetRepoCache.animatedSkullsJson?.petSkinNbtNames?.firstNotNullOfOrNull { key ->
            if (extraData.contains(key)) extraData.getInt(key).orElse(-1).takeIf { it >= 0 } else null
        }

    fun variantIndex(extraData: JsonObject?): Int? {
        val keys = PetRepoCache.animatedSkullsJson?.petSkinNbtNames ?: return null
        if (extraData == null) return null
        return keys.firstNotNullOfOrNull { key ->
            extraData.get(key)?.takeIf { it.isJsonPrimitive && it.asJsonPrimitive.isNumber }?.asInt
        }
    }

    private fun skinNamePrefixes(properName: String): List<String> =
        if (properName == "PHOENIX") listOf("PHOENIX", "PHEONIX") else listOf(properName)
}
