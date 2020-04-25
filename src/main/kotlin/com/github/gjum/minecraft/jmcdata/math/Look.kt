package com.github.gjum.minecraft.jmcdata.math

import kotlin.math.*

/**
 * [yaw] and [pitch] are Euler angles measured in radians.
 * yaw = pitch = 0 is looking straight south.
 * yaw = -pi/2 is looking straight east.
 * pitch = -pi/2 is looking straight up.
 */
data class Look(val yaw: Radians, val pitch: Radians) {
	override fun toString() = "(${yawDegrees.roundToInt() % 360}°${yaw.compass8()}, ${pitchDegrees.roundToInt() % 360}°)"

	operator fun plus(other: Look) = other.let { Look(yaw + it.yaw, pitch + it.pitch) }
	operator fun unaryMinus() = Look(-yaw, -pitch)
	operator fun minus(other: Look) = this + (-other)

	fun turnToVec3(delta: Vec3d): Look {
		val groundDistance = delta.run { sqrt(x * x + z * z) }
		val pitchNew = -atan2(delta.y, groundDistance)
		val yawNew = if (-PI / 2 < pitchNew && pitchNew < PI / 2) {
			PI - atan2(-delta.x, -delta.z)
		} else yaw // keep current yaw and look straight up/down
		return Look(yawNew, pitchNew)
	}

	val asVec3d by lazy {
		Vec3d(
			-cos(pitch) * sin(yaw),
			-sin(pitch),
			cos(pitch) * cos(yaw))
	}

	val yawDegrees: Degrees by lazy { yaw.degFromRad() }
	val pitchDegrees: Degrees by lazy { pitch.degFromRad() }

	companion object {
		val origin = Look(0.0, 0.0)

		/**
		 * Use [turnToVec3] if possible, it uses the previous yaw when looking straight up/down.
		 */
		fun fromVec3(delta: Vec3d) = origin.turnToVec3(delta)

		fun fromDegrees(yaw: Float, pitch: Float): Look {
			return Look(yaw.toDouble().radFromDeg(), pitch.toDouble().radFromDeg())
		}
	}
}
