import java.io.File
import kotlin.random.Random.Default.nextDouble


fun color(ray: Ray, world: Hitable): Vec3 {
    val unitDirection = ray.direction.makeUnitVector()
    val hit = world.hit(ray, 0.0, Double.MAX_VALUE)
    if (hit != null) {
        return (hit.normal + Vec3(1.0, 1.0, 1.0)) * 0.5
    }
    val t = 0.5 * (unitDirection.y + 1)
    return (1.0 - t) * Vec3(1.0, 1.0, 1.0) + t * Vec3(0.5, 0.7, 1.0)
}

fun makeCamera(): Camera {
    val lowerLeft = Vec3(-2.0, -1.0, -1.0)
    val horizontal = Vec3(4.0, 0.0, 0.0)
    val vertical = Vec3(0.0, 2.0, 0.0)
    val origin = Vec3(0.0, 0.0, 0.0)
    return Camera(origin, lowerLeft, horizontal, vertical)
}

fun main() {
    File("scene.ppm").printWriter().use { out ->
        val nx = 200
        val ny = 100
        out.print("P3\n$nx $ny\n255\n")
        val camera = makeCamera()
        val world = makeWorld()
        val antiAliasing = 100
        for (j in ny - 1 downTo 0) {
            for (i in 0 until nx) {
                var col = Vec3(0.0, 0.0, 0.0)
                for (n in 0 until antiAliasing) {
                    val u = (i + nextDouble()) / nx
                    val v = (j + nextDouble()) / ny
                    val ray = camera.getRay(u, v)
                    col += color(ray, world)
                }
                col /= antiAliasing.toDouble()
                val r = (col.r() * 255.99).toInt()
                val g = (col.g() * 255.99).toInt()
                val b = (col.b() * 255.99).toInt()
                out.print("$r $g $b\n")
            }
        }
    }
}

private fun makeWorld(): Hitable {
    val smallSphere = Sphere(Vec3(0.0, 0.0, -1.0), 0.5)
    val largeSphere = Sphere(Vec3(0.0, -100.5, -1.0), 100.0)
    return HitableList(listOf(smallSphere, largeSphere))
}

private operator fun Double.times(vec: Vec3): Vec3 {
    return vec * this
}
