data class Hit(
    val t: Double, val point: Vec3, val normal: Vec3, val u: Double, val v: Double,
    val material: Material
)

interface Hitable {
    fun hit(ray: Ray, min_t: Double, max_t: Double): Hit?
    fun boundingBox(t0: Double, t1: Double): Aabb?
}
