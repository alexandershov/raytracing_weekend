import kotlin.math.sin

interface Texture {
    fun value(u: Double, v: Double, p: Vec3): Vec3
}


data class ConstantTexture(val color: Vec3) : Texture {
    override fun value(u: Double, v: Double, p: Vec3): Vec3 {
        return color
    }
}


data class CheckeredTexture(val even: Texture, val odd: Texture) : Texture {
    override fun value(u: Double, v: Double, p: Vec3): Vec3 {
        val length = Math.PI / 10.0
        val mul = Math.PI / length
        val sign = sin(mul * p.x) * sin(mul * p.y)* sin(mul * p.z)
        if (sign > 0) {
            return even.value(u, v, p)
        }
        return odd.value(u, v, p)
    }
}
