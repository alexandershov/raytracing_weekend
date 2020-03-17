import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class RotateY(val hitable: Hitable, val angle: Double) : Hitable {
    override fun hit(ray: Ray, min_t: Double, max_t: Double): Hit? {
        val rotatedRay = rotate(ray, -angle)
        val hit = hitable.hit(rotatedRay, min_t, max_t) ?: return null
        return hit.copy(point = rotate(hit.point, angle), normal = rotate(hit.normal, angle))
    }

    override fun boundingBox(t0: Double, t1: Double): Aabb? {
        val box = hitable.boundingBox(t0, t1) ?: return null
        val result = Aabb(
            Vec3(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE),
            Vec3(Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE)
        )
        for (i in 0..1) {
            for (j in 0..1) {
                for (k in 0..1) {
                    val x = i * box.max.x + (1 - i) * box.min.x
                    val y = j * box.max.y + (1 - j) * box.min.y
                    val z = k * box.max.z + (1 - k) * box.min.z
                    val p = rotate(Vec3(x, y, z), angle)
                    for (c in 0..2) {
                        if (p[c] < result.min[c]) {
                            result.min[c] = p[c]
                        }
                        if (p[c] > result.max[c]) {
                            result.max[c] = p[c]
                        }
                    }
                }
            }
        }
        return result
    }

    private fun rotate(p: Vec3, angle: Double): Vec3 {
        val r = sqrt(p.dot(p) - p.y * p.y)
        val alpha = atan2(p.z, p.x)
        return Vec3(r * cos(angle + alpha), p.y, r * sin(angle + alpha))
    }

    private fun rotate(ray: Ray, angle: Double): Ray {
        return Ray(rotate(ray.origin, angle), rotate(ray.direction, angle))
    }

}
