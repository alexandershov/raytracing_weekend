data class HitableList(val items: List<Hitable>): Hitable {
    override fun hit(ray: Ray, min_t: Double, max_t: Double): Hit? {
        val hits = items.mapNotNull { it.hit(ray, min_t, max_t) }
        for (hit in hits.sortedBy { it.t }) {
            if (hit.t in min_t..max_t) {
                return hit
            }
        }
        return null
    }

    override fun boundingBox(t0: Double, t1: Double): Aabb? {
        if (items.isEmpty()) {
            return null
        }
        val boxes = items.map { it.boundingBox(t0, t1) }
        if (boxes.any { box -> box == null }) {
            return null
        }
        return boxes.filterNotNull().reduce { acc, box -> acc.union(box)}
    }
}
