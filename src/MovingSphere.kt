data class MovingSphere(val centerStart: Vec3, val centerEnd: Vec3, val startAt: Double, val endAt: Double, val radius: Double, val material: Material) : Hitable {
    override fun hit(ray: Ray, min_t: Double, max_t: Double): Hit? {
        val sphere = sphereAtTime(ray.time)
        return sphere.hit(ray, min_t, max_t)
    }

    override fun boundingBox(t0: Double, t1: Double): Aabb? {
        val left = sphereAtTime(t0)
        val right = sphereAtTime(t1)
        return left.boundingBox(t0, t1)?.union(right.boundingBox(t0, t1)!!)
    }

    private fun sphereAtTime(t: Double): Sphere {
        val speed = (centerEnd - centerStart) / (endAt - startAt)
        val center = centerStart + speed * t
        return Sphere(center, radius, material)
    }
}
