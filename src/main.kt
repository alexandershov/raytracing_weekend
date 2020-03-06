import java.io.File
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random
import kotlin.random.Random.Default.nextDouble
import kotlin.reflect.jvm.internal.impl.util.Check


data class Lambertian(val texture: Texture) : Material {
    override fun scatter(incident: Ray, hit: Hit): Scatter? {
        val center = hit.point + hit.normal
        val point = center + randomInUnitSphere()
        return Scatter(
            Ray(hit.point, point - hit.point, incident.time),
            texture.value(0.0, 0.0, hit.point)
        )
    }
}

data class Metal(val albedo: Vec3, val f: Double) : Material {
    override fun scatter(incident: Ray, hit: Hit): Scatter? {
        val reflection =
            Ray(hit.point, reflect(incident.direction, hit.normal) + f * randomInUnitSphere(), incident.time)
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

fun schlick(cosine: Double, refIdx: Double): Double {
    val r0 = ((1 - refIdx) / (1 + refIdx)).pow(2)
    return r0 + (1 - r0) * (1 - cosine).pow(5)

}


data class Dielectric(val ri: Double) : Material {
    override fun scatter(incident: Ray, hit: Hit): Scatter? {
        val outward: Vec3
        val reflected = reflect(incident.direction, hit.normal)
        val niOverNt: Double
        val cosine: Double
        val attenuation = Vec3(1.0, 1.0, 1.0)
        if (incident.direction.dot(hit.normal) > 0) {
            outward = -hit.normal
            niOverNt = ri
            cosine = ri * incident.direction.dot(hit.normal) / incident.direction.length()
        } else {
            outward = hit.normal
            niOverNt = 1.0 / ri
            cosine = -incident.direction.dot(hit.normal) / incident.direction.length()
        }
        val refraction = refract(incident.direction, outward, niOverNt)
        if (refraction != null) {
            val reflectProb = schlick(cosine, ri)
            if (nextDouble() < reflectProb) {
                return Scatter(Ray(hit.point, reflected, incident.time), attenuation)
            }
            return Scatter(Ray(hit.point, refraction, incident.time), attenuation)
        } else {
            return Scatter(Ray(hit.point, reflected, incident.time), attenuation)
        }
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

fun makeCamera(nx: Int, ny: Int): Camera {
    val lookFrom = Vec3(3.0, 3.0, 2.0)
    val lookAt = Vec3(0.0, 0.0, -1.0)
    val focusDist = (lookAt - lookFrom).length()
    val aperture = 0.0
    return Camera(
        lookFrom, lookAt, Vec3(0.0, 1.0, 0.0), 20.0,
        nx.toDouble() / ny.toDouble(), aperture, focusDist, startAt = 0.0, endAt = 1.0
    )
}

fun main() {
    File("scene.ppm").printWriter().use { out ->
        val nx = 200
        val ny = 100
        out.print("P3\n$nx $ny\n255\n")
        val camera = makeCheckeredCamera(nx, ny)
        val world = makeBVHNode(makePerlinWorld(), camera.startAt, camera.endAt)
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

private fun makeCheckeredWorld(): List<Hitable> {
    val checker = CheckeredTexture(ConstantTexture(Vec3(0.2, 0.3, 0.1)), ConstantTexture(Vec3(0.9, 0.9, 0.9)))
    val bottom = Sphere(Vec3(0.0, -10.0, 0.0), 10.0, Lambertian(checker))
    val top = Sphere(Vec3(0.0, 10.0, 0.0), 10.0, Lambertian(checker))
    return listOf(bottom, top)
}

private fun makeCheckeredCamera(nx: Int, ny: Int): Camera {
    val lookFrom = Vec3(13.0, 2.0, 3.0)
    val lookAt = Vec3(0.0, 0.0, 0.0)
    val distToFocus = 10.0
    val aperture = 0.0
    return Camera(lookFrom, lookAt, Vec3(0.0, 1.0, 0.0), 20.0, nx.toDouble() / ny.toDouble(), aperture, distToFocus, 0.0, 1.0)
}

private fun makePerlinWorld(): List<Hitable> {
    val texture = NoiseTexture()
    val large = Sphere(Vec3(0.0, -1000.0, 0.0), 1000.0, Lambertian(texture))
    val small = Sphere(Vec3(0.0, 2.0, 0.0), 2.0, Lambertian(texture))
    return listOf(large, small)
}

private fun makeWorld(): List<Hitable> {
    val smallSphere = Sphere(Vec3(0.0, 0.0, -1.0), 0.5, Lambertian(ConstantTexture(Vec3(0.1, 0.2, 0.5))))
    val largeSphere = Sphere(Vec3(0.0, -100.5, -1.0), 100.0, Lambertian(ConstantTexture(Vec3(0.8, 0.8, 0.0))))
    val metal = MovingSphere(Vec3(1.0, 0.0, -1.0), Vec3(1.0, 0.2, -1.0), 0.0, 1.0, 0.5, Metal(Vec3(0.8, 0.6, 0.2), 0.0))
    val firstDielectric = Sphere(Vec3(-1.0, 0.0, -1.0), 0.5, Dielectric(1.5))
    val secondDielectric = Sphere(Vec3(-1.0, 0.0, -1.0), -0.45, Dielectric(1.5))
    return listOf(smallSphere, largeSphere, metal, firstDielectric, secondDielectric)
}

private fun makeBVHNode(items: List<Hitable>, t0: Double, t1: Double): BVHNode {
    assert(items.isNotEmpty())
    if (items.size == 1) {
        return BVHNode(items[0], items[0], items[0].boundingBox(t0, t1)!!)
    }
    val axis = Random.nextInt(3)
    val fns = listOf(::byX, ::byY, ::byZ)
    val sorted = items.sortedBy { item -> fns[axis](item.boundingBox(t0, t1)!!.min) }
    return BVHNode(
        makeBVHNode(sorted.subList(0, sorted.size / 2), t0, t1),
        makeBVHNode(sorted.subList(sorted.size / 2, sorted.size), t0, t1),
        HitableList(items).boundingBox(t0, t1)!!
    )
}


private fun byX(v: Vec3): Double {
    return v.x
}

private fun byY(v: Vec3): Double {
    return v.y
}

private fun byZ(v: Vec3): Double {
    return v.z
}

private fun makeRandomWorld(): List<Hitable> {
    val items: MutableList<Hitable> = mutableListOf()
    val checker = CheckeredTexture(ConstantTexture(Vec3(0.2, 0.3, 0.1)), ConstantTexture(Vec3(0.9, 0.9, 0.9)))
    val largeSphere = Sphere(Vec3(0.0, -1000.0, 0.0), 1000.0, Lambertian(checker))
    items.add(largeSphere)
    for (a in -11..10) {
        for (b in -11..10) {
            val chooseMat = nextDouble()
            val center = Vec3(a + 0.9 * nextDouble(), 0.2, b + 0.9 * nextDouble())
            if ((center - Vec3(4.0, 0.2, 0.0)).length() > 0.9) {
                when {
                    chooseMat < 0.8 -> {
                        items.add(
                            MovingSphere(
                                center,
                                center + Vec3(0.0, 0.5 * nextDouble(), 0.0),
                                0.0,
                                1.0,
                                0.2,
                                Lambertian(
                                    ConstantTexture(
                                        Vec3(
                                            nextDouble() * nextDouble(),
                                            nextDouble() * nextDouble(),
                                            nextDouble() * nextDouble()
                                        )
                                    )
                                )
                            )
                        )
                    }
                    chooseMat < 0.95 -> {
                        items.add(
                            Sphere(
                                center,
                                0.2,
                                Metal(
                                    Vec3(0.5 * (1 + nextDouble()), 0.5 * (1 + nextDouble()), 0.5 * (1 + nextDouble())),
                                    0.5 * nextDouble()
                                )
                            )
                        )
                    }
                    else -> {
                        items.add(Sphere(center, 0.2, Dielectric(1.5)))
                    }
                }
            }
        }
    }
    items.add(Sphere(Vec3(0.0, 1.0, 0.0), 1.0, Dielectric(1.5)))
    items.add(Sphere(Vec3(-4.0, 1.0, 0.0), 1.0, Lambertian(ConstantTexture(Vec3(0.4, 0.2, 0.1)))))
    items.add(Sphere(Vec3(4.0, 1.0, 0.0), 1.0, Metal(Vec3(0.7, 0.6, 0.5), 0.0)))
    return items
}

private operator fun Double.times(vec: Vec3): Vec3 {
    return vec * this
}
