// plane == 0|1|2 == x|y|z
data class Rect(val material: Material, val min: Vec3, val max: Vec3, val plane: Int): Hitable {
    override fun hit(ray: Ray, min_t: Double, max_t: Double): Hit? {
        val t = min[plane] - ray.origin[plane] / ray.direction[plane]
        val p = ray.pointAtParameter(t)
        val inside = listOf(0, 1, 2).filter { d -> d != plane}.all { d -> p[d] in min[d]..max[d] }
        if (inside and (t in min_t..max_t)) {
            return Hit(t, p, normal(), getU(p), getV(p), material)
        }
        return null
    }

    override fun boundingBox(t0: Double, t1: Double): Aabb? {
        val minBound = Vec3(min.x, min.y, min.z)
        val maxBound = Vec3(max.x, max.y, max.z)
        minBound[plane] -= 0.001
        maxBound[plane] += 0.001
        return Aabb(minBound, maxBound)
    }

    private fun normal(): Vec3 {
        val result = Vec3(0.0, 0.0, 0.0)
        result[plane] = 1.0
        return result
    }

    private fun getU(p: Vec3): Double {
        val map = mapOf(2 to 0, 1 to 0, 0 to 2)
        val d = map.getValue(plane)
        return (p[d] - min[d]) / (max[d] - min[d])
    }

    private fun getV(p: Vec3): Double {
        val map = mapOf(2 to 1, 1 to 2, 0 to 1)
        val d = map.getValue(plane)
        return (p[d] - min[d]) / (max[d] - min[d])
    }
}
