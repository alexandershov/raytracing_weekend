import kotlin.math.log
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

data class ConstantMedium(val boundary: Hitable, val density: Double, val texture: Texture) : Hitable {
    val phaseFunction = Isotropic(texture)

    override fun hit(ray: Ray, min_t: Double, max_t: Double): Hit? {
        var global = boundary.hit(ray, Double.MIN_VALUE, Double.MAX_VALUE) ?: return null
        // TODO: do we need 0.0001
        var local = boundary.hit(ray, min_t + 0.0001, max_t) ?: return null
        global = global.copy(t = max(min_t, global.t))

        local = local.copy(t = min(max_t, local.t))
        if (global.t >= local.t) {
            return null
        }
        global = global.copy(t = max(0.0, global.t))
        val distanceInsideBoundary = (local.t - global.t) * ray.direction.length()
        val hitDistance = (-1.0 / density) * log(Random.nextDouble(), 10.0)
        if (hitDistance < distanceInsideBoundary) {
            val t = global.t + hitDistance / (ray.direction).length()
            val point = ray.pointAtParameter(t)
            return Hit(t, point, Vec3(1.0, 0.0, 0.0), 0.0, 0.0, phaseFunction)
        }
        return null
    }

    override fun boundingBox(t0: Double, t1: Double): Aabb? {
        return boundary.boundingBox(t0, t1)
    }
}


data class Isotropic(val albedo: Texture) : Material {
    override fun scatter(incident: Ray, hit: Hit): Scatter? {
        val scattered = Ray(hit.point, randomInUnitSphere())
        return Scatter(scattered, albedo.value(hit.u, hit.v, hit.point))
    }

    override fun emitted(u: Double, v: Double, p: Vec3): Vec3 {
        return Vec3(0.0, 0.0, 0.0)
    }

}
