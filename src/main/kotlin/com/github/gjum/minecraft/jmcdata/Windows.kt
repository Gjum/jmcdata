package com.github.gjum.minecraft.jmcdata

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

sealed class WindowOpenMethod {
	data class WithBlock(val blockId: Int) : WindowOpenMethod()
	data class WithEntity(val entityId: Int) : WindowOpenMethod()
}

data class WindowInfo(
	val id: String,
	val name: String,
	val slots: Map<String, IntRange>,
	val properties: List<String>,
	val openedWith: List<WindowOpenMethod>
) {

	override fun toString() = "WindowInfo{$name}"
}

class WindowInfoStorage(windowsJson: JsonArray) {
	private val windowInfosById = mutableMapOf<String, WindowInfo>()

	operator fun get(windowId: String): WindowInfo? {
		return windowInfosById[windowId]
	}

	init {
		for (windowJson in windowsJson) {
			val o = windowJson.asJsonObject
			val id = o.get("id").asString
			try {
				val window = WindowInfo(
					id = id,
					name = o.get("name").asString,
					slots = o.getOrNull("slots")?.asJsonArray?.map {
						val s = it.asJsonObject
						val start = s.get("index").asInt
						val size = s.getOrNull("size")?.asInt ?: 1
						Pair(s.get("name").asString,
							start until start + size)
					}?.toMap() ?: emptyMap(),
					properties = o.getOrNull("properties")?.asJsonArray?.map { it.asString }?.toList() ?: emptyList(),
					openedWith = o.getOrNull("openedWith")?.asJsonArray?.mapNotNull {
						when (it.asJsonObject.get("type").asString) {
							"block" -> WindowOpenMethod.WithBlock(it.asJsonObject.get("id").asInt)
							"entity" -> WindowOpenMethod.WithEntity(it.asJsonObject.get("id").asInt)
							else -> {
								System.err.println("Ignoring unknown open method '${it.asJsonObject.get("type")}'")
								null
							}
						}
					}?.toList() ?: emptyList()
				)
				windowInfosById[window.id] = window
			} catch (e: Throwable) {
				System.err.println("while reading window '$id' $o")
				throw e
			}
		}
	}
}

private fun JsonObject.getOrNull(key: String): JsonElement? {
	return get(key)?.let { if (it.isJsonNull) null else it }
}
