data class Hit(val t: Double, val point: Vec3, val normal: Vec3)

interface Hitable {
    fun hit(ray: Vec3, min_t: Double, max_t: Double): Hit?
}
