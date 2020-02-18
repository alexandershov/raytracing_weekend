data class BVHNode(val left: Hitable, val right: Hitable, val box: Aabb) : Hitable {
    override fun hit(ray: Ray, min_t: Double, max_t: Double): Hit? {
        val myHit = box.hit(ray, min_t, max_t)
        if (myHit) {
            val leftHit = left.hit(ray, min_t, max_t)
            val rightHit = right.hit(ray, min_t, max_t)
            if (leftHit != null && rightHit != null) {
                if (leftHit.t < rightHit.t) {
                    return leftHit
                }
                return rightHit
            } else if (leftHit != null) {
                return leftHit
            } else if (rightHit != null) {
                return rightHit
            } else {
                return null
            }
        }
        return null
    }

    override fun boundingBox(t0: Double, t1: Double): Aabb? {
        val leftBox = left.boundingBox(t0, t1)
        val rightBox = right.boundingBox(t0, t1)
        if (leftBox == null || rightBox == null) {
            return null
        }
        return leftBox.union(rightBox)
    }

}
