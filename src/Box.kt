data class Box(val min: Vec3, val max: Vec3, val material: Material): Hitable {
    private val hitableList: HitableList = rectangles(min, max, material)

    override fun hit(ray: Ray, min_t: Double, max_t: Double): Hit? {
        return hitableList.hit(ray, min_t, max_t)
    }

    override fun boundingBox(t0: Double, t1: Double): Aabb? {
        return Aabb(min, max)
    }
}


private fun rectangles(min: Vec3, max: Vec3, material: Material): HitableList {
    val floor = Rect(material, Vec3(min.x, min.y, min.z), Vec3(max.x, min.y, max.z), 1)
    val ceiling = Rect(material, Vec3(min.x, max.y, min.z), Vec3(max.x, max.y, max.z), 1)
    val left = Rect(material, Vec3(min.x, min.y, min.z), Vec3(min.x, max.y, max.z), 0)
    val right = Rect(material, Vec3(max.x, min.y, min.z), Vec3(max.x, max.y, max.z), 0)
    val far = Rect(material, Vec3(min.x, min.y, max.z), Vec3(max.x, max.y, max.z), 2)
    val close = Rect(material, Vec3(min.x, min.y, min.z), Vec3(max.x, max.y, min.z), 2)
    return HitableList(listOf(floor, ceiling, left, right, far, close))
}
