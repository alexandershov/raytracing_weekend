import kotlin.math.sqrt

data class Sphere(val center: Vec3, val radius: Double, val material: Material) : Hitable {
    override fun hit(ray: Ray, min_t: Double, max_t: Double): Hit? {
        val diff = ray.origin - center
        val a = ray.direction.dot(ray.direction)
        val b = 2.0 * ray.direction.dot(diff)
        val c = diff.dot(diff) - radius * radius
        val d = b * b - 4.0 * a * c
        if (d > 0.0) {
            for (sign in listOf(-1.0, 1.0)) {
                val t = (-b + sign * sqrt(d)) / (2 * a)
                if (t in min_t..max_t) {
                    val point = ray.pointAtParameter(t)
                    val normal = (point - center) / radius
                    return Hit(t, point, normal, material)
                }
            }
        }
        return null
    }
}
