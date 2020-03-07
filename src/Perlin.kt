import kotlin.random.Random

class Perlin {
    private val permX = perlinGeneratePerm()
    private val permY = perlinGeneratePerm()
    private val permZ = perlinGeneratePerm()
    private val ranDouble = perlinGenerate()

    fun noise(p: Vec3): Double {
        val i = (p.x * 4.0).toInt() and 255
        val j = (p.y * 4.0).toInt() and 255
        val k = (p.z * 4.0).toInt() and 255
        return ranDouble[permX[i] xor permY[j] xor permZ[k]]
    }
}

class NoiseTexture : Texture {
    private val perlin = Perlin()
    override fun value(u: Double, v: Double, p: Vec3): Vec3 {
        return Vec3(1.0, 1.0, 1.0) * perlin.noise(p)
    }
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
