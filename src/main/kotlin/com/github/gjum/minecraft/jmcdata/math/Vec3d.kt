package com.github.gjum.minecraft.jmcdata.math

import kotlin.math.roundToInt
import kotlin.math.sqrt

data class Vec3d(val x: Double, val y: Double, val z: Double) {
	override fun toString() = "[${x.round(2)} ${y.round(2)} ${z.round(2)}]"

	operator fun times(scalar: Double) = Vec3d(x * scalar, y * scalar, z * scalar)
	operator fun div(scalar: Double) = Vec3d(x / scalar, y / scalar, z / scalar)
	operator fun plus(other: Vec3d) = other.let { Vec3d(x + it.x, y + it.y, z + it.z) }
	operator fun unaryMinus() = Vec3d(-x, -y, -z)
	operator fun minus(other: Vec3d) = other.let { Vec3d(x - it.x, y - it.y, z - it.z) }

	fun floored() = Vec3i(x.floor, y.floor, z.floor)
	fun rounded() = Vec3i(x.roundToInt(), y.roundToInt(), z.roundToInt())
	fun normed() = Vec3d(x, y, z) / length()
	fun length() = sqrt(lengthSquared())
	fun lengthSquared() = x * x + y * y + z * z

	fun getAxis(axis: Axis): Double = when (axis) {
		Axis.X -> x
		Axis.Y -> y
		Axis.Z -> z
	}

	fun withAxis(axis: Axis, value: Double): Vec3d = when (axis) {
		Axis.X -> Vec3d(value, y, z)
		Axis.Y -> Vec3d(x, value, z)
		Axis.Z -> Vec3d(x, y, value)
	}

	fun anyNaN() = x.isNaN() || y.isNaN() || z.isNaN()

	companion object {
		val origin = Vec3d(0.0, 0.0, 0.0)
		val unit = Vec3d(1.0, 1.0, 1.0)
	}
}
