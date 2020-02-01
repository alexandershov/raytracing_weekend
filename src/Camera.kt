import kotlin.math.PI
import kotlin.math.tan

class Camera(lookFrom: Vec3, lookAt: Vec3, vup: Vec3, vfov: Double, aspect: Double) {
    private var lowerLeftCorner: Vec3
    private var horizontal: Vec3
    private var vertical: Vec3
    private var origin: Vec3

    init {
        val theta = PI * vfov / 180.0
        val halfHeight = tan(theta / 2)
        val halfWidth = halfHeight * aspect
        origin = lookFrom
        val w = (lookFrom - lookAt).makeUnitVector()
        val u = vup.cross(w).makeUnitVector()
        val v = w.cross(u)
        horizontal = u * halfWidth * 2.0
        vertical = v * halfHeight * 2.0
//        lowerLeftCorner = lookAt - horizontal / 2.0 - vertical / 2.0
        lowerLeftCorner = origin - horizontal / 2.0 - vertical / 2.0 - w
    }
    fun getRay(u: Double, v: Double): Ray {
        return Ray(origin, lowerLeftCorner + horizontal * u + vertical * v - origin)
    }
}
