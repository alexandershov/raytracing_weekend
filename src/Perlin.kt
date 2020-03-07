import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.random.Random

class Perlin {
    private val permX = perlinGeneratePerm()
    private val permY = perlinGeneratePerm()
    private val permZ = perlinGeneratePerm()
    private val ranDouble = perlinGenerate()

    fun noise(p: Vec3): Double {
        val i = floor(p.x).toInt()
        val j = floor(p.y).toInt()
        val k = floor(p.z).toInt()
        val u = hermit(p.x - i)
        val v = hermit(p.y - j)
        val w = hermit(p.z - k)
        val c = Array(2) { Array(2) { Array(2) { 0.0 } } }
        for (di in 0..1) {
            for (dj in 0..1) {
                for (dk in 0..1) {
                    val idx = (permX[(i + di) and 255]) xor (permY[(j + dj) and 255]) xor (permZ[(k + dk) and 255])
                    c[di][dj][dk] = ranDouble[idx]
                }
            }
        }
        return trilinearInterp(c, u, v, w)
    }
}

private fun hermit(u: Double): Double {
    return u * u * (3 - 2 * u)
}

class NoiseTexture : Texture {
    private val perlin = Perlin()
    override fun value(u: Double, v: Double, p: Vec3): Vec3 {
        return Vec3(1.0, 1.0, 1.0) * perlin.noise(p)
    }
}

private fun trilinearInterp(c: Array<Array<Array<Double>>>, u: Double, v: Double, w: Double): Double {
    var accum = 0.0
    for (i in 0..1) {
        for (j in 0..1) {
            for (k in 0..1) {
                accum += (i * u + (1 - i) * (1 - u)) * (j * v + (1 - j) * (1 - v)) * (k * w + (1 - k) * (1 - w)) * c[i][j][k]
            }
        }
    }
    return accum
}

fun permute(items: Array<Int>): Unit {
    for (i in items.size - 1 downTo 1) {
        val j = Random.nextInt(0, i)
        val tmp = items[j]
        items[j] = items[i]
        items[i] = tmp
    }
}

fun perlinGenerate(): Array<Double> {
    return Array(256) { Random.nextDouble() }
}

fun perlinGeneratePerm(): Array<Int> {
    val items = (0..255).toList().toTypedArray()
    permute(items)
    return items
}
