import kotlin.math.PI
import kotlin.math.tan
import kotlin.random.Random

fun randomInUnitDisc(): Vec3 {
    while (true) {
        val p = Vec3(Random.nextDouble(), Random.nextDouble(), 0.0) * 2.0 - Vec3(1.0, 1.0, 0.0)
        if (p.dot(p) < 1.0) {
            return p
        }
    }
}

class Camera(
    lookFrom: Vec3, lookAt: Vec3, vup: Vec3, vfov: Double, aspect: Double,
    aperture: Double, focusDist: Double) {
    private var lowerLeftCorner: Vec3
    private var horizontal: Vec3
    private var vertical: Vec3
    private var origin: Vec3
    private var lensRadius: Double = aperture / 2.0

    init {
        val theta = PI * vfov / 180.0
        val halfHeight = tan(theta / 2)
        val halfWidth = halfHeight * aspect
        origin = lookFrom
        val w = (lookFrom - lookAt).makeUnitVector()
        val u = vup.cross(w).makeUnitVector()
        val v = w.cross(u)
        lowerLeftCorner = origin - u * (halfWidth * focusDist) - v * (halfHeight * focusDist) - w * focusDist
        horizontal = u * halfWidth * 2.0 * focusDist
        vertical = v * halfHeight * 2.0 * focusDist
    }

    fun getRay(u: Double, v: Double): Ray {
        val rd = randomInUnitDisc() * lensRadius
        val offset = u * rd.x + v * rd.y
        return Ray(origin + offset, lowerLeftCorner + horizontal * u + vertical * v - origin + (-1 * offset))
    }
}
