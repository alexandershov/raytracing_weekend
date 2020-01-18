import java.io.File
import kotlin.math.sqrt


fun color(ray: Ray, world: Hitable): Vec3 {
    val unitDirection = ray.direction.makeUnitVector()
    val hit = world.hit(ray, 0.0, Double.MAX_VALUE)
    if (hit != null) {
        return (hit.normal + Vec3(1.0, 1.0, 1.0)) * 0.5
    }
    val t = 0.5 * (unitDirection.y + 1)
    return (1.0 - t) * Vec3(1.0, 1.0, 1.0) + t * Vec3(0.5, 0.7, 1.0)
}

fun main() {
    File("scene.ppm").printWriter().use { out ->
        val nx = 200
        val ny = 100
        out.print("P3\n$nx $ny\n255\n")
        val lowerLeft = Vec3(-2.0, -1.0, -1.0)
        val horizontal = Vec3(4.0, 0.0, 0.0)
        val vertical = Vec3(0.0, 2.0, 0.0)
        val origin = Vec3(0.0, 0.0, 0.0)
        val smallSphere = Sphere(Vec3(0.0, 0.0, -1.0), 0.5)
        val largeSphere = Sphere(Vec3(0.0, -100.5, -1.0), 100.0)
        val world = HitableList(listOf(smallSphere, largeSphere))
        for (j in ny - 1 downTo 0) {
            for (i in 0 until nx) {
                val u = i.toDouble() / nx
                val v = j.toDouble() / ny
                val ray = Ray(origin, lowerLeft + u * horizontal + v * vertical)
                val col = color(ray, world)
                val r = (col.r() * 255.99).toInt()
                val g = (col.g() * 255.99).toInt()
                val b = (col.b() * 255.99).toInt()
                out.print("$r $g $b\n")
            }
        }
    }
}

private operator fun Double.times(vec: Vec3): Vec3 {
    return vec * this
}
