import kotlin.math.PI
import kotlin.math.tan

class Camera(vfov: Double, aspect: Double) {
    private var lowerLeftCorner: Vec3
    private var horizontal: Vec3
    private var vertical: Vec3
    private var origin: Vec3

    init {
        val theta = PI * vfov / 180.0
        val halfHeight = tan(theta / 2)
        val halfWidth = halfHeight * aspect
        lowerLeftCorner = Vec3(-halfWidth, -halfHeight, -1.0)
        horizontal = Vec3(halfWidth * 2.0, 0.0, 0.0)
        vertical = Vec3(0.0, halfHeight * 2.0, 0.0)
        origin = Vec3(0.0, 0.0, 0.0)
    }
    fun getRay(u: Double, v: Double): Ray {
        return Ray(origin, lowerLeftCorner + horizontal * u + vertical * v - origin)
    }
}
