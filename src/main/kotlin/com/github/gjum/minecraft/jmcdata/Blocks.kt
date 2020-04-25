package com.github.gjum.minecraft.jmcdata

import com.github.gjum.minecraft.jmcdata.math.Box
import com.github.gjum.minecraft.jmcdata.math.Cardinal
import com.github.gjum.minecraft.jmcdata.math.Shape
import com.github.gjum.minecraft.jmcdata.math.Vec3d
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.util.Collections

data class BlockVariant(
	/**
	 * Absolute block variant id for 1.13+,
	 * `id * 16 + meta` before 1.13
	 */
	val variantNr: Int,
	val block: BlockInfo,
	val collisionShape: Shape,
	val displayName: String
) {
	val stateName = lazy {
		val prefix = block.idName + "["
		block.properties.joinToString(", ", prefix, "]") { prop ->
			val propValue = prop.getString(variantNr - block.variants[0].variantNr)
			"${prop.name}=$propValue"
		}
	}

	override fun toString() = "BlockVariantInfo{$stateName ${block.idNr}:$variantNr}"
}

data class BlockInfo(
	val idNr: Int,
	val idName: String,
	val item: ItemInfo?,
	val displayName: String,
	val transparent: Boolean,
	val filterLight: Int,
	val emitLight: Int,
	val diggable: Boolean,
	val hardness: Float,
	val droppedItems: Collection<ItemInfo>,
	val properties: List<BlockProperty>,
	var variants: List<BlockVariant>
) {
	override fun toString() = "BlockInfo{$idName nr=$idNr}"
}

enum class BlockPropertyType { BOOL, INT, DIRECTION, ENUM }

data class BlockProperty(
	val name: String,
	val type: BlockPropertyType,
	val numValues: Int,
	val values: List<String>?,
	internal val factor: Int
) {
	private val BlockVariant.stateNr get() = variantNr - block.variants[0].variantNr

	fun getInt(variant: BlockVariant) = getInt(variant.stateNr)
	fun getInt(stateNr: Int): Int {
		return (stateNr / factor) % numValues
	}

	fun getString(variant: BlockVariant) = getString(variant.stateNr)
	fun getString(stateNr: Int): String {
		val value = getInt(stateNr)
		return when (type) {
			BlockPropertyType.BOOL -> if (value == 0) "FALSE" else "TRUE"
			BlockPropertyType.INT -> value.toString()
			BlockPropertyType.DIRECTION -> values!![value]
			BlockPropertyType.ENUM -> values!![value]
		}
	}

	fun getBool(variant: BlockVariant) = getBool(variant.stateNr)
	fun getBool(stateNr: Int): Boolean {
		if (type != BlockPropertyType.BOOL) error(
			"Tried getting block property of type $type as BOOL")
		return getInt(stateNr) != 0
	}

	fun getDirection(variant: BlockVariant) = getDirection(variant.stateNr)
	fun getDirection(stateNr: Int): Cardinal {
		if (type != BlockPropertyType.DIRECTION) error(
			"Tried getting block property of type $type as DIRECTION")
		return Cardinal.valueOf(getString(stateNr))
	}
}

// XXX private
fun preFlatteningVariantId(blockId: Int, meta: Int) = blockId * 16 + meta

class BlockInfoStorage(blocksJson: JsonArray, collisionShapesJson: JsonObject, itemInfos: ItemInfoStorage) {
	private val blockVariantInfos = mutableMapOf<Int, BlockVariant>()
	private val blockInfosByIdName = mutableMapOf<String, BlockInfo>()

	operator fun get(blockVariantId: Int): BlockVariant? {
		return blockVariantInfos[blockVariantId]
	}

	operator fun get(idName: String): BlockInfo? {
		return blockInfosByIdName[idName]
	}

	init {
		val collisionShapes = getShapes(collisionShapesJson)

		for (blockJson in blocksJson) {
			val o = blockJson.asJsonObject
			val idName = o.get("name").asString
			try {
				val properties = getProperties(o)

				val block = BlockInfo(
					idNr = o.get("id").asInt,
					idName = idName,
					item = itemInfos[idName],
					displayName = o.get("displayName").asString,
					transparent = o.get("transparent").asBoolean,
					filterLight = o.getOrNull("filterLight")?.asInt ?: 0,
					emitLight = o.getOrNull("emitLight")?.asInt ?: 0,
					diggable = o.get("diggable").asBoolean,
					hardness = o.getOrNull("hardness")?.asFloat ?: Float.POSITIVE_INFINITY,
					droppedItems = emptyList(), // TODO o.get("drops").asJsonArray.map { itemInfos[it.asJsonObject.get("drop").asInt]!! },
					properties = properties ?: emptyList(),
					variants = emptyList() // filled below
				)

				val collisionShapesByVariantOffset = collisionShapes[block.idName]
					?: run {
						System.err.println("Failed to load collision shape for block '$idName', assuming solid")
						ShapePerBlockVariant.SOLID
					}

				val minVariantNr = o["minStateId"]?.asInt
					?: preFlatteningVariantId(block.idNr, 0)
				val namedVariants = if (o["minStateId"] == null) {
					o["variations"]?.asJsonArray?.map {
						val meta = it.asJsonObject["metadata"].asInt
						val stateIndex = preFlatteningVariantId(block.idNr, meta)
						val displayName = it.asJsonObject["displayName"].asString
						Pair(stateIndex, displayName)
					} ?: Collections.singletonList(minVariantNr to block.displayName)
				} else {
					val maxVariantNr = o["maxStateId"]?.asInt ?: minVariantNr
					if (minVariantNr == maxVariantNr) {
						listOf(minVariantNr to block.displayName)
					} else {
						(minVariantNr..maxVariantNr).map { variantNr ->
							val stateStr = properties.joinToString(", ", "[", "]") {
								val value = it.getString(variantNr - minVariantNr)
								"${it.name}=$value"
							}
							variantNr to block.displayName + stateStr
						}
					}
				}
				block.variants = namedVariants.map { (variantNr, displayName) ->
					BlockVariant(
						variantNr = variantNr,
						block = block,
						collisionShape = collisionShapesByVariantOffset[variantNr - minVariantNr],
						displayName = displayName
					).also {
						blockVariantInfos[it.variantNr] = it
					}
				}

				blockInfosByIdName[block.idName] = block
				block.item?.block = block
			} catch (e: Throwable) {
				System.err.println("while reading block '$idName' $o")
				throw e
			}
		}
	}
}

private fun getProperties(o: JsonObject): List<BlockProperty> {
	val states = o["states"] ?: return emptyList()
	return states.asJsonArray.map { it.asJsonObject }.let { props ->
		var factor = 1
		props.map { p ->
			BlockProperty(
				name = p["name"].asString,
				type = BlockPropertyType.valueOf(p["type"].asString.toUpperCase()),
				numValues = p["num_values"].asInt,
				values = p["values"]?.asJsonArray?.map(JsonElement::getAsString),
				factor = factor
			).apply { factor *= numValues }
		}
	}
}

private fun getShapes(shapesJson: JsonObject): Map<String, ShapePerBlockVariant> {
	val shapes = mutableMapOf<Int, Shape>()
	val blockShapes = mutableMapOf<String, ShapePerBlockVariant>()
	for ((key, value) in shapesJson.getAsJsonObject("shapes").entrySet()) {
		val boxesJson = value.asJsonArray
		val boxes = mutableListOf<Box>()
		for (element in boxesJson) {
			val a = element.asJsonArray
			var i = 0
			boxes.add(Box(Vec3d(
				a[i++].asDouble,
				a[i++].asDouble,
				a[i++].asDouble), Vec3d(
				a[i++].asDouble,
				a[i++].asDouble,
				a[i++].asDouble)))
		}
		shapes[key.toInt()] = Shape(boxes)
	}
	for ((key, value) in shapesJson.getAsJsonObject("blocks").entrySet()) {
		blockShapes[key] = when {
			value == null || value.isJsonNull -> ShapePerBlockVariant.EMPTY
			value.isJsonPrimitive -> {
				val shape = shapes[value.asInt]
					?: error("undefined shape id $value in block $key")
				ShapePerBlockVariant.Same(shape)
			}
			else -> {
				val shapeIds = value.asJsonArray
				ShapePerBlockVariant.OneEach(shapeIds.map {
					shapes[it.asInt]
				}.toTypedArray())
			}
		}
	}

	return blockShapes
}

private sealed class ShapePerBlockVariant {
	companion object {
		val EMPTY = Same(Shape.EMPTY)
		val SOLID = Same(Shape.SOLID)
	}

	abstract operator fun get(blockVariantId: Int): Shape

	internal class Same(private val shape: Shape) : ShapePerBlockVariant() {
		override operator fun get(blockVariantId: Int): Shape {
			return shape
		}
	}

	internal class OneEach(private val shapes: Array<Shape?>) : ShapePerBlockVariant() {
		override operator fun get(blockVariantId: Int): Shape {
			require(blockVariantId >= 0) { "block variant id '$blockVariantId' < 0" }
			require(blockVariantId < shapes.size) {
				("block variant id $blockVariantId out of bounds for length ${shapes.size}")
			}
			return shapes[blockVariantId] ?: Shape.EMPTY
		}
	}
}

private fun JsonObject.getOrNull(key: String): JsonElement? {
	return get(key)?.let { if (it.isJsonNull) null else it }
}
