import java.io.File
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random
import kotlin.random.Random.Default.nextDouble


data class Lambertian(val texture: Texture) : Material {
    override fun scatter(incident: Ray, hit: Hit): Scatter? {
        val center = hit.point + hit.normal
        val point = center + randomInUnitSphere()
        return Scatter(
            Ray(hit.point, point - hit.point, incident.time),
            texture.value(hit.u, hit.v, hit.point)
        )
    }

    override fun emitted(u: Double, v: Double, p: Vec3): Vec3 {
        return Vec3(0.0, 0.0, 0.0)
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

    override fun emitted(u: Double, v: Double, p: Vec3): Vec3 {
        return Vec3(0.0, 0.0, 0.0)
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

    override fun emitted(u: Double, v: Double, p: Vec3): Vec3 {
        return Vec3(0.0, 0.0, 0.0)
    }

}

fun color(ray: Ray, world: Hitable, depth: Int): Vec3 {
    val hit = world.hit(ray, 0.001, Double.MAX_VALUE)
    if (hit != null) {
        val scatter = hit.material.scatter(ray, hit)
        val emitted = hit.material.emitted(hit.u, hit.v, hit.point)
        if (depth < 50 && scatter != null) {
            val col = color(scatter.ray, world, depth + 1)
            return emitted + scatter.attenuation * col
        } else {
            return emitted
        }
    }
    return Vec3(0.0, 0.0, 0.0)
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
        val camera = makeCornellCamera(nx, ny)
        val world = makeBVHNode(makeCornellWorld(), camera.startAt, camera.endAt)
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
                val r = convertColor(col.r())
                val g = convertColor(col.g())
                val b = convertColor(col.b())
                out.print("$r $g $b\n")
            }
        }
    }
}

private fun convertColor(x: Double): Int {
    return (min(sqrt(x), 1.0) * 255.99).toInt()
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
    return Camera(
        lookFrom,
        lookAt,
        Vec3(0.0, 1.0, 0.0),
        35.0,
        nx.toDouble() / ny.toDouble(),
        aperture,
        distToFocus,
        0.0,
        1.0
    )
}


private fun makeCornellCamera(nx: Int, ny: Int): Camera {
    val lookFrom = Vec3(278.0, 278.0, -800.0)
    val lookAt = Vec3(278.0, 278.0, 0.0)
    val distToFocus = 10.0
    val aperture = 0.0
    return Camera(
        lookFrom,
        lookAt,
        Vec3(0.0, 1.0, 0.0),
        40.0,
        nx.toDouble() / ny.toDouble(),
        aperture,
        distToFocus,
        0.0,
        1.0
    )
}

private fun makeCornellWorld(): List<Hitable> {
    val red = Lambertian(ConstantTexture(Vec3(0.65, 0.05, 0.05)))
    val white = Lambertian(ConstantTexture(Vec3(0.73, 0.73, 0.73)))
    val green = Lambertian(ConstantTexture(Vec3(0.12, 0.45, 0.15)))
    val light = DiffuseLight(ConstantTexture(Vec3(15.0, 15.0, 15.0)))
    val greenWall = Rect(green, Vec3(555.0, 0.0, 0.0), Vec3(555.0, 555.0, 555.0), 0)
    val redWall = Rect(red, Vec3(0.0, 0.0, 0.0), Vec3(0.0, 555.0, 555.0), 0)
    val floor = Rect(white, Vec3(0.0, 0.0, 0.0), Vec3(555.0, 0.0, 555.0), 1)
    val ceiling = Rect(white, Vec3(0.0, 555.0, 0.0), Vec3(555.0, 555.0, 555.0), 1)
    val farWall = Rect(white, Vec3(0.0, 0.0, 555.0), Vec3(555.0, 555.0, 555.0), 2)
    val lamp = Rect(light, Vec3(213.0, 554.0, 227.0), Vec3(343.0, 554.0, 332.0), 1)
    val wideBox = Box(Vec3(130.0, 0.0, 65.0), Vec3(295.0, 165.0, 230.0), white)
    val tallBox = Box(Vec3(265.0, 0.0, 295.0), Vec3(430.0, 330.0, 460.0), white)
    return listOf(lamp, greenWall, redWall, floor, ceiling, farWall, Translate(tallBox, Vec3(0.0, 100.0, 0.0)), wideBox)
}


private fun makePerlinWorld(): List<Hitable> {
    val texture = NoiseTexture()
    val light = DiffuseLight(ConstantTexture(Vec3(4.0, 4.0, 4.0)))
    val image = ImageTexture("/Users/aershov/Downloads/earthmap.jpg")
    val large = Sphere(Vec3(0.0, -1000.0, 0.0), 1000.0, Lambertian(texture))
    val small = Sphere(Vec3(0.0, 2.0, 0.0), 2.0, Lambertian(texture))
    val smallLight = Sphere(Vec3(0.0, 10.0, 0.0), 1.0, light)
    val rect = Rect(light, Vec3(3.0, 1.0, 0.0), Vec3(5.0, 3.0, 0.0), 2)
    return listOf(large, small, smallLight, rect)
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
