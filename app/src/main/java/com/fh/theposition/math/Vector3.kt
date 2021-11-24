package com.fh.theposition.math

import kotlin.math.sqrt

data class Vector3(val x: Float, val y: Float, val z: Float) {
    fun cross(vector3: Vector3): Vector3 {
        return Vector3(
            this.y * vector3.z - this.z * vector3.y,
            this.z * vector3.x - this.x * vector3.z,
            this.x * vector3.y - this.y * vector3.x
        )
    }

    operator fun minus(other: Vector3): Vector3 {
        return Vector3(
            x - other.x,
            y - other.y,
            z - other.z
        )
    }

    operator fun plus(other: Vector3): Vector3 {
        return Vector3(
            x + other.x,
            y + other.y,
            z + other.z
        )
    }

    operator fun times(factor: Float): Vector3 {
        return Vector3(
            x * factor,
            y * factor,
            z * factor
        )
    }

    fun dot(other: Vector3): Float {
        return x * other.x + y * other.y + z * other.z
    }

    fun magnitude(): Float {
        return sqrt(x * x + y * y + z * z)
    }

    fun normalize(): Vector3 {
        val mag = magnitude()
        return Vector3(
            x / mag,
            y / mag,
            z / mag
        )
    }

    companion object {
        val zero = Vector3(0f, 0f, 0f)
    }
}
