import kotlin.math.sqrt

data class Vec3(var x: Double, var y: Double, var z: Double) {
    operator fun plus(other: Vec3): Vec3 {
        return Vec3(x + other.x, y + other.y, z + other.z)
    }

    operator fun minus(other: Vec3): Vec3 {
        return Vec3(x - other.x, y - other.y, z - other.z)
    }

    operator fun times(other: Vec3): Vec3 {
        return Vec3(x * other.x, y * other.y, z * other.z)
    }

    operator fun div(other: Vec3): Vec3 {
        return Vec3(x / other.x, y / other.y, z / other.z)
    }

    operator fun times(t: Double): Vec3 {
        return Vec3(x * t, y * t, z * t)
    }

    operator fun div(t: Double): Vec3 {
        return Vec3(x / t, y / t, z / t)
    }

    operator fun get(i: Int): Double {
        return when (i) {
            0 -> x
            1 -> y
            2 -> z
            else -> throw IndexOutOfBoundsException()
        }
    }

    operator fun set(i: Int, value: Double) {
        when (i) {
            0 -> x = value
            1 -> y = value
            2 -> z = value
            else -> throw java.lang.IndexOutOfBoundsException()
        }
    }

    operator fun unaryMinus(): Vec3 {
        return Vec3(-x, -y, -z)
    }

    fun r(): Double {
        return x
    }

    fun g(): Double {
        return y
    }

    fun b(): Double {
        return z
    }

    fun dot(other: Vec3): Double {
        return x * other.x + y * other.y + z * other.z
    }

    fun cross(other: Vec3): Vec3 {
        return Vec3(y * other.z - z * other.y, z * other.x - x * other.z, x * other.y - y * other.x)
    }

    fun length(): Double {
        return sqrt(squaredLength())
    }

    private fun squaredLength(): Double {
        return this.dot(this)
    }

    fun makeUnitVector(): Vec3 {
        return this / this.length()
    }


}
