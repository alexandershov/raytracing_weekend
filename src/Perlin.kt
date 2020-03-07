import kotlin.math.absoluteValue
import kotlin.math.floor
import kotlin.random.Random

class Perlin {
    private val permX = perlinGeneratePerm()
    private val permY = perlinGeneratePerm()
    private val permZ = perlinGeneratePerm()
    private val ranVector = perlinGenerate()

    fun noise(p: Vec3): Double {
        val i = floor(p.x).toInt()
        val j = floor(p.y).toInt()
        val k = floor(p.z).toInt()
        val u = p.x - i
        val v = p.y - j
        val w = p.z - k
        val c = Array(2) { Array(2) { Array(2) { Vec3(0.0, 0.0, 0.0) } } }
        for (di in 0..1) {
            for (dj in 0..1) {
                for (dk in 0..1) {
                    val idx = (permX[(i + di) and 255]) xor (permY[(j + dj) and 255]) xor (permZ[(k + dk) and 255])
                    c[di][dj][dk] = ranVector[idx]
                }
            }
        }
        return trilinearInterp(c, u, v, w)
    }

    fun turb(p: Vec3): Double {
        var accum = 0.0
        var temp = p
        var weight = 1.0
        for (i in 0..6) {
            accum += weight * noise(temp)
            temp *= 2.0
            weight *= 0.5
        }
        return accum.absoluteValue
    }
}

private fun hermit(u: Double): Double {
    return u * u * (3 - 2 * u)
}

class NoiseTexture : Texture {
    private val perlin = Perlin()
    override fun value(u: Double, v: Double, p: Vec3): Vec3 {
        val scale = 4.0
        return Vec3(1.0, 1.0, 1.0) * perlin.turb(p * scale)
    }
}

private fun trilinearInterp(c: Array<Array<Array<Vec3>>>, u: Double, v: Double, w: Double): Double {
    val uu = hermit(u)
    val vv = hermit(v)
    val ww = hermit(w)
    var accum = 0.0
    for (i in 0..1) {
        for (j in 0..1) {
            for (k in 0..1) {
                val weightV = Vec3(u - i, v - j, w - k)
                accum += (i * uu + (1 - i) * (1 - uu)) * (j * vv + (1 - j) * (1 - vv)) * (k * ww + (1 - k) * (1 - ww)) * (c[i][j][k].dot(
                    weightV
                ))
            }
        }
    }
    return accum
}

fun permute(items: Array<Int>) {
    for (i in items.size - 1 downTo 1) {
        val j = Random.nextInt(0, i)
        val tmp = items[j]
        items[j] = items[i]
        items[i] = tmp
    }
}

fun perlinGenerate(): Array<Vec3> {
    return Array(256) { randomVector() }
}

fun randomVector(): Vec3 {
    return Vec3(
        -1.0 + Random.nextDouble() * 2.0,
        -1.0 + Random.nextDouble() * 2.0,
        -1.0 + Random.nextDouble() * 2.0
    ).makeUnitVector()
}

fun perlinGeneratePerm(): Array<Int> {
    val items = (0..255).toList().toTypedArray()
    permute(items)
    return items
}
