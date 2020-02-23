interface Texture {
    fun value(u: Double, v: Double, p: Vec3): Vec3
}


data class ConstantTexture(val color: Vec3) : Texture {
    override fun value(u: Double, v: Double, p: Vec3): Vec3 {
        return color
    }
}
