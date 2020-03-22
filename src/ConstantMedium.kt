import kotlin.math.log
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

data class ConstantMedium(val boundary: Hitable, val density: Double, val texture: Texture) : Hitable {
    val phaseFunction = Isotropic(texture)

    override fun hit(ray: Ray, min_t: Double, max_t: Double): Hit? {
        var input = boundary.hit(ray, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY) ?: return null
        var output = boundary.hit(ray, input.t + 0.0001, Double.POSITIVE_INFINITY) ?: return null
        input = input.copy(t = max(min_t, input.t))

        output = output.copy(t = min(max_t, output.t))
        if (input.t >= output.t) {
            return null
        }
        input = input.copy(t = max(0.0, input.t))
        val distanceInsideBoundary = (output.t - input.t) * ray.direction.length()
        val hitDistance = (-1.0 / density) * log(Random.nextDouble(), 2.71)
        if (hitDistance < distanceInsideBoundary) {
            val t = input.t + hitDistance / ray.direction.length()
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
