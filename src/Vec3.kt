data class Vec3(val x: Double, val y: Double, val z: Double) {
    operator fun plus(other: Vec3): Vec3 {
        return Vec3(x + other.x, y + other.y, z + other.z)
    }
}
