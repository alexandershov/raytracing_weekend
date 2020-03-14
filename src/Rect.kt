// plane == 0|1|2 == x|y|z
data class Rect(val material: Material, val min: Vec3, val max: Vec3, val plane: Int): Hitable {
    override fun hit(ray: Ray, min_t: Double, max_t: Double): Hit? {
        val t = min[plane] - ray.origin[plane] / ray.direction[plane]
        val p = ray.pointAtParameter(t)
        if ((p.x in min.x..max.x) and (p.y in min.y..max.y) and (t in min_t..max_t)) {
            return Hit(t, p, normal(), (p.x - min.x) / (max.x - min.x), (p.y - min.y) / (max.y - min.y), material)
        }
        return null
    }

    override fun boundingBox(t0: Double, t1: Double): Aabb? {
        return Aabb(
            Vec3(min.x, min.y, min.z - 0.001),
            Vec3(max.x, max.y, max.z + 0.001)
        )
    }

    private fun normal(): Vec3 {
        val result = Vec3(0.0, 0.0, 0.0)
        result[plane] = 1.0
        return result
    }
}
