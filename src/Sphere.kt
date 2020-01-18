import kotlin.math.sqrt

data class Sphere(val center: Vec3, val radius: Double) : Hitable {
    override fun hit(ray: Ray, min_t: Double, max_t: Double): Hit? {
    val diff = ray.origin - center
    val a = ray.direction.dot(ray.direction)
    val b = 2.0 * ray.direction.dot(diff)
    val c = diff.dot(diff) - radius * radius
    val d = b * b - 4.0 * a * c
    if (d >= 0.0) {
        val t = (-b - sqrt(d)) / (2 * a)
        if (t in min_t..max_t) {
            val point = ray.pointAtParameter(t)
            val normal = (point - center).makeUnitVector()
            return Hit(t, point, normal)
        }
    }
    return null
    }
}