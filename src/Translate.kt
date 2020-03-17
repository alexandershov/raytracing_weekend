data class Translate(val hitable: Hitable, val offset: Vec3): Hitable {
    override fun hit(ray: Ray, min_t: Double, max_t: Double): Hit? {
        val translatedRay = Ray(ray.origin - offset, ray.direction)
        val hit = hitable.hit(translatedRay, min_t, max_t) ?: return null
        return hit.copy(point=hit.point + offset)
    }

    override fun boundingBox(t0: Double, t1: Double): Aabb? {
        val result = hitable.boundingBox(t0, t1) ?: return null
        return Aabb(result.min + offset, result.max + offset)
    }
}
