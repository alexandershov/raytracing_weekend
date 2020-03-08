import kotlin.math.acos
import kotlin.math.atan2
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
                    val uv = getUv((point - center) / radius)
                    return Hit(t, point, normal, uv.first, uv.second, material)
                }
            }
        }
        return null
    }

    override fun boundingBox(t0: Double, t1: Double): Aabb? {
        return Aabb(center - Vec3(radius, radius, radius), center + Vec3(radius, radius, radius))
    }
}


private fun getUv(p: Vec3): Pair<Double, Double> {
    val phi = atan2(p.x, p.z) + Math.PI
    val theta = acos(p.y)
    return Pair(phi / (2 * Math.PI), theta / Math.PI)
}
