package com.github.gjum.minecraft.jmcdata.math

import kotlin.math.sqrt

data class Vec3i(val x: Int, val y: Int, val z: Int) {
	override fun toString() = "[$x $y $z]"

	operator fun times(scalar: Int) = Vec3i(x * scalar, y * scalar, z * scalar)
	operator fun times(scalar: Double) = Vec3d(x * scalar, y * scalar, z * scalar)
	operator fun div(scalar: Double) = Vec3d(x / scalar, y / scalar, z / scalar)
	operator fun plus(other: Vec3i) = other.let { Vec3i(x + it.x, y + it.y, z + it.z) }
	operator fun unaryMinus() = Vec3i(-x, -y, -z)
	operator fun minus(other: Vec3i) = other.let { Vec3i(x - it.x, y - it.y, z - it.z) }

	val asVec3d get() = this * 1.0
	fun normed() = Vec3i(x, y, z) / length()
	fun length() = sqrt(lengthSquared().toDouble())
	fun lengthSquared() = x * x + y * y + z * z

	fun getAxis(axis: Axis): Int = when (axis) {
		Axis.X -> x
		Axis.Y -> y
		Axis.Z -> z
	}

	fun withAxis(axis: Axis, value: Int): Vec3i = when (axis) {
		Axis.X -> Vec3i(value, y, z)
		Axis.Y -> Vec3i(x, value, z)
		Axis.Z -> Vec3i(x, y, value)
	}

	companion object {
		val origin = Vec3i(0, 0, 0)
		val unit = Vec3i(1, 1, 1)
	}
}
