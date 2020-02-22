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
    aperture: Double, focusDist: Double, val startAt: Double, val endAt: Double) {
    private var lowerLeftCorner: Vec3
    private var horizontal: Vec3
    private var vertical: Vec3
    private var origin: Vec3
    private var lensRadius: Double = aperture / 2.0
    private var u: Vec3
    private var w: Vec3
    private var v: Vec3

    init {
        val theta = PI * vfov / 180.0
        val halfHeight = tan(theta / 2) * focusDist
        val halfWidth = halfHeight * aspect
        origin = lookFrom
        w = (lookFrom - lookAt).makeUnitVector()
        u = vup.cross(w).makeUnitVector()
        v = w.cross(u)
        lowerLeftCorner = origin - u * (halfWidth) - v * (halfHeight) - w * focusDist
        horizontal = u * halfWidth * 2.0
        vertical = v * halfHeight * 2.0
    }

    fun getRay(s: Double, t: Double): Ray {
        val rd = randomInUnitDisc() * lensRadius
        val offset = u * rd.x + v * rd.y
        val time = startAt + Random.nextDouble() * (endAt - startAt)
        return Ray(origin + offset, lowerLeftCorner + horizontal * s + vertical * t - origin + (offset * -1.0), time)
    }
}
