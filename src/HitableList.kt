data class HitableList(val items: List<Hitable>): Hitable {
    override fun hit(ray: Ray, min_t: Double, max_t: Double): Hit? {
        val hits = items.mapNotNull { it.hit(ray, min_t, max_t) }
        for (hit in hits.sortedBy { it.t }) {
            if (hit.t > min_t && hit.t < max_t) {
                return hit
            }
        }
        return null
    }
}
