data class DiffuseLight(val texture: Texture) : Material {
    override fun scatter(incident: Ray, hit: Hit): Scatter? {
        return null
    }

    override fun emitted(u: Double, v: Double, p: Vec3): Vec3 {
        return texture.value(u, v, p)
    }
}
