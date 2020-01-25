data class Scatter(val ray: Ray, val attenuation: Vec3)

interface Material {
    fun scatter(incident: Ray, hit: Hit): Scatter?
}


