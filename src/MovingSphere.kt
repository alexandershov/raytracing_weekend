data class MovingSphere(val centerStart: Vec3, val centerEnd: Vec3, val startAt: Double, val endAt: Double, val radius: Double, val material: Material) : Hitable {
    override fun hit(ray: Ray, min_t: Double, max_t: Double): Hit? {
        val speed = (centerEnd - centerStart) / (endAt - startAt)
        val center = centerStart + speed * ray.time
        val sphere = Sphere(center, radius, material)
        return sphere.hit(ray, min_t, max_t)
    }
}
