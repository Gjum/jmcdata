package com.github.gjum.minecraft.jmcdata

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import java.io.InputStreamReader

class MinecraftData(mcVersion: String) {
	val items = ItemInfoStorage(
		loadJson(mcVersion, "items", JsonArray::class.java),
		*getBlockItemsInfoSource(mcVersion))

	val blocks = BlockInfoStorage(
		loadJson(mcVersion, "blocks", JsonArray::class.java),
		loadJson(mcVersion, "blockCollisionShapes", JsonObject::class.java),
		items)

	val windows = WindowInfoStorage(loadJson(mcVersion, "windows", JsonArray::class.java))
}

/**
 * For versions before The Flattening, `blocks.json` has to be loaded as additional items.
 * @return empty array after The Flattening, singleton array of `blocks.json` for older versions
 */
private fun getBlockItemsInfoSource(mcVersion: String): Array<JsonArray> {
	val dataPaths = getDataPaths(mcVersion, "pc")
	val blocksAreItems = dataPaths["blocksAreItems"]?.asBoolean ?: false
	if (!blocksAreItems) return emptyArray()
	return arrayOf(loadJson(mcVersion, "blocks", JsonArray::class.java))
}

private fun <T> loadJson(mcVersion: String, fileName: String, type: Class<T>): T {
	val path = getDataFilePath(mcVersion, fileName)
	val jsonStream = MinecraftData::class.java.getResourceAsStream(path)
		?: error("Could not find $fileName data for Minecraft version $mcVersion")
	return Gson().fromJson(InputStreamReader(jsonStream), type)
}

private fun getDataFilePath(mcVersion: String, fileName: String): String {
	val dataPaths = getDataPaths(mcVersion, "pc")
	val subDir = dataPaths.get(fileName)?.asString
		?: error("Unknown data file `$fileName` for version `$mcVersion`")
	val dir = subDir.substringAfter("pc/")
	return "/mcdata/$dir/$fileName.json"
}

private fun getDataPaths(mcVersion: String, platform: String = "pc"): JsonObject {
	val jsonStream = MinecraftData::class.java.getResourceAsStream("/mcdata/dataPaths.json")
	val dataPaths = Gson().fromJson(InputStreamReader(jsonStream), JsonObject::class.java)
	val platformObj = dataPaths.get(platform)?.asJsonObject
		?: error("Unknown platform `$platform`")
	return platformObj.get(mcVersion)?.asJsonObject
		?: error("Unknown Minecraft version `$mcVersion`")
}

private val String.baseVersion get() = split('.').take(2).joinToString(".")
