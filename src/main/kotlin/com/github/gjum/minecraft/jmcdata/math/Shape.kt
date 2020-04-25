package com.github.gjum.minecraft.jmcdata.math

data class Shape(val boxes: Collection<Box>) {
	val outerBox: Box? by lazy {
		boxes.singleOrNull()
			?: if (boxes.isEmpty()) null else Box(
				Vec3d(
					boxes.map { it.min.x }.min()!!,
					boxes.map { it.min.y }.min()!!,
					boxes.map { it.min.z }.min()!!),
				Vec3d(
					boxes.map { it.max.x }.max()!!,
					boxes.map { it.max.y }.max()!!,
					boxes.map { it.max.z }.max()!!)
			)
	}

	companion object {
		val EMPTY = Shape(emptyList())
		val SOLID = Shape(listOf(Box(Vec3d.origin, Vec3d.unit)))
	}
}
