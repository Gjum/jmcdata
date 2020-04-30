package com.github.gjum.minecraft.jmcdata.math

import kotlin.math.PI
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.roundToInt

val Double.floor: Int get() = floor(this).toInt()

fun Double.round(places: Int): Double {
	if (this.isNaN()) return Double.NaN
	val zeroes = 10.0.pow(places)
	return (this * zeroes).roundToInt() / zeroes
}

fun Int.euclideanDiv(denominator: Int): Int =
	if (this >= 0) this / denominator
	else (this + 1) / denominator - 1

fun Int.euclideanMod(denominator: Int): Int =
	if (this >= 0 || this % denominator == 0) this % denominator
	else denominator + (this % denominator)

fun Double.euclideanMod(denominator: Double): Double =
	if (this >= 0) this % denominator
	else denominator + ((this + 1) % denominator) - 1

typealias Radians = Double
typealias Degrees = Double

const val TAU = 2.0 * PI

fun Double.degFromRad(): Degrees = this * 360.0 / TAU
fun Double.radFromDeg(): Radians = this * TAU / 360.0
fun Float.degFromRad(): Degrees = this * 360.0 / TAU
fun Float.radFromDeg(): Radians = this * TAU / 360.0

private val compass8Names = arrayOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")

fun Radians.compass8(): String {
	val yawEights = this * 8 / (2 * PI)
	val index = 4 + yawEights.roundToInt()
	return compass8Names[(index + 8).euclideanMod(8)]
}

enum class Axis { X, Y, Z }

enum class Cardinal(val axis: Axis, val sign: Int) {
	WEST(Axis.X, -1),
	EAST(Axis.X, 1),
	DOWN(Axis.Y, -1),
	UP(Axis.Y, 1),
	NORTH(Axis.Z, -1),
	SOUTH(Axis.Z, 1);

	val asVec3i get() = Vec3i.origin.withAxis(axis, sign)
}
