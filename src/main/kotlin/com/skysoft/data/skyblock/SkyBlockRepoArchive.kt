package com.skysoft.data.skyblock

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.skysoft.utils.TextUtilities.removeColor
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.util.Locale
import java.util.zip.ZipInputStream

internal object SkyBlockRepoArchive {
    fun apply(snapshot: SkyBlockDataSnapshot, archive: ByteArray): SkyBlockDataSnapshot {
        val updates = parse(archive)
        val knownUpdates = updates.filterKeys { snapshot.itemInfo.containsKey(ItemListEntryKey(ItemListEntryKind.SKYBLOCK, it)) }
        require(knownUpdates.size >= MINIMUM_MATCHED_ITEMS) {
            "SkyblockRepo archive matches only ${knownUpdates.size} bundled items"
        }
        val info = snapshot.itemInfo.toMutableMap()
        knownUpdates.forEach { (id, update) ->
            val key = ItemListEntryKey(ItemListEntryKind.SKYBLOCK, id)
            val existing = info.getValue(key)
            info[key] = existing.copy(
                displayName = update.name.ifBlank { existing.displayName },
                category = update.category ?: existing.category,
                rarity = update.rarity ?: existing.rarity,
                lore = update.lore.ifEmpty { existing.lore },
                flags = update.flags.ifEmpty { existing.flags },
            )
        }
        val entries = snapshot.entries.map { entry ->
            val updated = info[entry.key] ?: return@map entry
            if (entry.key.kind != ItemListEntryKind.SKYBLOCK) return@map entry
            entry.copy(
                displayName = updated.displayName,
                searchableText = searchableText(updated),
            )
        }
        return snapshot.copy(
            entries = entries,
            entriesByKey = entries.associateBy(ItemListEntry::key),
            itemInfo = info,
        )
    }

    private fun parse(archive: ByteArray): Map<String, ItemUpdate> {
        require(archive.size in MINIMUM_ARCHIVE_SIZE..MAXIMUM_ARCHIVE_SIZE) {
            "SkyblockRepo archive has an invalid size"
        }
        val items = mutableMapOf<String, ItemUpdate>()
        var manifest: JsonObject? = null
        var entryCount = 0
        var expandedBytes = 0L
        ZipInputStream(ByteArrayInputStream(archive)).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                entryCount++
                require(entryCount <= MAXIMUM_ARCHIVE_ENTRIES) { "SkyblockRepo archive has too many entries" }
                validatePath(entry.name)
                if (entry.isDirectory) continue
                val bytes = readEntry(zip)
                expandedBytes += bytes.size
                require(expandedBytes <= MAXIMUM_EXPANDED_SIZE) { "SkyblockRepo archive expands beyond its size limit" }
                when {
                    entry.name.endsWith("/manifest.json") -> manifest = parseObject(bytes)
                    entry.name.contains("/items/") && entry.name.endsWith(".json") -> {
                        val update = parseItem(parseObject(bytes))
                        require(items.put(update.id, update) == null) { "SkyblockRepo archive repeats item ${update.id}" }
                    }
                }
            }
        }
        validateManifest(requireNotNull(manifest) { "SkyblockRepo archive has no manifest" })
        require(items.size in MINIMUM_ITEM_COUNT..MAXIMUM_ITEM_COUNT) {
            "SkyblockRepo archive contains ${items.size} items"
        }
        return items
    }

    private fun parseItem(json: JsonObject): ItemUpdate {
        val id = json.text("internalId")
        require(id.matches(itemIdPattern)) { "SkyblockRepo item has an invalid ID" }
        val name = json.text("name")
        require(name.isNotBlank() && name.length <= MAXIMUM_NAME_LENGTH) { "SkyblockRepo item $id has an invalid name" }
        val lore = json.text("lore").split('\n').take(MAXIMUM_LORE_LINES).map(::minecraftColors)
        val data = json.objectValue("data")
        val flags = json.objectValue("flags")?.entrySet()?.mapNotNullTo(mutableSetOf()) { (key, value) ->
            key.takeIf { value.isJsonPrimitive && value.asJsonPrimitive.isBoolean && value.asBoolean }
        }.orEmpty()
        return ItemUpdate(
            id = id,
            name = minecraftColors(name).removeColor(),
            category = json.text("category").takeIf(String::isNotBlank),
            rarity = data?.text("tier")?.takeIf(String::isNotBlank),
            lore = lore,
            flags = flags,
        )
    }

    private fun validateManifest(manifest: JsonObject) {
        require(manifest.get("version")?.asInt == SUPPORTED_MANIFEST_VERSION) {
            "SkyblockRepo manifest version is unsupported"
        }
        val paths = manifest.objectValue("paths") ?: error("SkyblockRepo manifest has no paths")
        require(paths.text("items") == "items") { "SkyblockRepo manifest has an invalid item path" }
    }

    private fun validatePath(path: String) {
        require(path.isNotBlank() && !path.startsWith('/') && '\\' !in path) { "SkyblockRepo archive has an invalid path" }
        require(path.split('/').none { it == ".." }) { "SkyblockRepo archive path escapes its root" }
    }

    private fun readEntry(zip: ZipInputStream): ByteArray {
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(READ_BUFFER_SIZE)
        while (true) {
            val read = zip.read(buffer)
            if (read < 0) break
            output.write(buffer, 0, read)
            require(output.size() <= MAXIMUM_ENTRY_SIZE) { "SkyblockRepo archive entry is too large" }
        }
        return output.toByteArray()
    }

    private fun parseObject(bytes: ByteArray): JsonObject =
        JsonParser.parseString(bytes.toString(StandardCharsets.UTF_8)).asJsonObject

    private fun searchableText(info: SkyBlockItemInfo): String = buildString {
        append(info.displayName).append(' ').append(info.key.id).append(' ')
        info.lore.forEach { append(it.removeColor()).append(' ') }
    }.lowercase(Locale.ROOT)

    private fun minecraftColors(text: String): String = colorCodePattern.replace(text) { match -> "§${match.groupValues[1]}" }

    private fun JsonObject.text(name: String): String = get(name)?.takeUnless { it.isJsonNull }?.asString.orEmpty()
    private fun JsonObject.objectValue(name: String): JsonObject? = get(name)?.takeIf { it.isJsonObject }?.asJsonObject

    private data class ItemUpdate(
        val id: String,
        val name: String,
        val category: String?,
        val rarity: String?,
        val lore: List<String>,
        val flags: Set<String>,
    )

    private const val SUPPORTED_MANIFEST_VERSION = 1
    private const val MINIMUM_ARCHIVE_SIZE = 1_000_000
    private const val MAXIMUM_ARCHIVE_SIZE = 24_000_000
    private const val MAXIMUM_EXPANDED_SIZE = 128_000_000L
    private const val MAXIMUM_ARCHIVE_ENTRIES = 20_000
    private const val MAXIMUM_ENTRY_SIZE = 1_000_000
    private const val MINIMUM_ITEM_COUNT = 5_000
    private const val MAXIMUM_ITEM_COUNT = 12_000
    private const val MINIMUM_MATCHED_ITEMS = 5_000
    private const val MAXIMUM_NAME_LENGTH = 256
    private const val MAXIMUM_LORE_LINES = 256
    private const val READ_BUFFER_SIZE = 8_192
    private val colorCodePattern = Regex("&([0-9a-fk-or])", RegexOption.IGNORE_CASE)
    private val itemIdPattern = Regex("[A-Za-z0-9_:;.\\-]+")
}
