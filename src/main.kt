import java.io.File
import kotlin.math.sqrt
import kotlin.random.Random.Default.nextDouble


data class Lambertian(val albedo: Vec3) : Material {
    override fun scatter(incident: Ray, hit: Hit): Scatter? {
        val center = hit.point + hit.normal
        val point = center + randomInUnitSphere()
        return Scatter(Ray(hit.point, point - hit.point), albedo)
    }
}

data class Metal(val albedo: Vec3, val f: Double): Material {
    override fun scatter(incident: Ray, hit: Hit): Scatter? {
        val reflection = Ray(hit.point, reflect(incident.direction, hit.normal) + f * randomInUnitSphere())
        if (reflection.direction.dot(hit.normal) <= 0) {
            return null
        }
        return Scatter(reflection, albedo)
    }
}

fun reflect(v: Vec3, normal: Vec3): Vec3 {
    return v - 2.0 * v.dot(normal) * normal
}

fun refract(v: Vec3, n: Vec3, ni_over_nt: Double): Vec3? {
    val uv = v.makeUnitVector()
    val dt = uv.dot(n)
    val discriminant = 1.0 - ni_over_nt * ni_over_nt * (1 - dt * dt)
    if (discriminant > 0) {
        return ni_over_nt * (uv - n * dt) - n * sqrt(discriminant)
    }
    return null
}


data class Dielectric(val ri: Double): Material {
    override fun scatter(incident: Ray, hit: Hit): Scatter? {
        val outward: Vec3
        val reflected = reflect(incident.direction, hit.normal)
        val niOverNt: Double
        val attenuation = Vec3(1.0, 1.0, 1.0)
        if (incident.direction.dot(hit.normal) > 0) {
            outward = -hit.normal
            niOverNt = ri
        } else {
            outward = hit.normal
            niOverNt = 1.0 / ri
        }
        val refraction = refract(incident.direction, outward, niOverNt)
        if (refraction != null) {
            return Scatter(Ray(hit.point, refraction), attenuation)
        }
        return null
}
}

fun color(ray: Ray, world: Hitable, depth: Int): Vec3 {
    val hit = world.hit(ray, 0.001, Double.MAX_VALUE)
    if (hit != null) {
        val scatter = hit.material.scatter(ray, hit)
        if (depth < 50 && scatter != null) {
            val col = color(scatter.ray, world, depth + 1)
            return scatter.attenuation * col
        }
        return Vec3(0.0, 0.0, 0.0)
    }
    val unitDirection = ray.direction.makeUnitVector()
    val t = 0.5 * (unitDirection.y + 1)
    return (1.0 - t) * Vec3(1.0, 1.0, 1.0) + t * Vec3(0.5, 0.7, 1.0)
}

fun randomInUnitSphere(): Vec3 {
    while (true) {
        val x = nextDouble() * 2 - 1
        val y = nextDouble() * 2 - 1
        val z = nextDouble() * 2 - 1
        if (x * x + y * y + z * z < 1) {
            return Vec3(x, y, z)
        }
    }
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
                    col += color(ray, world, 1)
                }
                col /= antiAliasing.toDouble()
                val r = (sqrt(col.r()) * 255.99).toInt()
                val g = (sqrt(col.g()) * 255.99).toInt()
                val b = (sqrt(col.b()) * 255.99).toInt()
                out.print("$r $g $b\n")
            }
        }
    }
}

private fun makeWorld(): Hitable {
    val smallSphere = Sphere(Vec3(0.0, 0.0, -1.0), 0.5, Lambertian(Vec3(0.1, 0.2, 0.5)))
    val largeSphere = Sphere(Vec3(0.0, -100.5, -1.0), 100.0, Lambertian(Vec3(0.8, 0.8, 0.0)))
    val firstMetal = Sphere(Vec3(1.0, 0.0, -1.0), 0.5, Metal(Vec3(0.8, 0.6, 0.2), 0.0))
    val secondMetal = Sphere(Vec3(-1.0, 0.0, -1.0), 0.5, Dielectric(1.5))
    return HitableList(listOf(smallSphere, largeSphere, firstMetal, secondMetal))
}

private operator fun Double.times(vec: Vec3): Vec3 {
    return vec * this
}
