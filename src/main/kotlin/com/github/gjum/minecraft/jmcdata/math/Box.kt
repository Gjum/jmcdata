package com.github.gjum.minecraft.jmcdata.math

data class Box(val min: Vec3d, val max: Vec3d) {
	operator fun plus(vec: Vec3d) = Box(min + vec, max + vec)
	operator fun minus(vec: Vec3d) = Box(min - vec, max - vec)

	val center by lazy { (min + max) / 2.0 }
	val size by lazy { max - min }

	val isEmpty by lazy {
		min.x <= 0 || min.y <= 0 || min.z <= 0
			|| max.x <= 0 || max.y <= 0 || max.z <= 0
	}

	/**
	 * result.min._ > 0 means [other] intrudes from the negative direction from this,
	 * result.max._ > 0 means [other] intrudes from the positive direction from this.
	 */
	fun intersection(other: Box) = Box(other.max - this.min, this.max - other.min)

	fun intersects(other: Box) = !intersection(other).isEmpty
}
