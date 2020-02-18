import kotlin.math.max
import kotlin.math.min

data class Aabb(val min: Vec3, val max: Vec3)  {
    fun hit(ray: Ray, tmin: Double, tmax: Double): Boolean {
        var curMin = tmin
        var curMax = tmax
        for (i in 0..2) {
            val t0 = (min[i] - ray.origin[i]) / ray.direction[i]
            val t1 = (max[i] - ray.origin[i]) / ray.direction[i]
            curMin = max(curMin, min(t0, t1))
            curMax = min(curMax, max(t0, t1))
            if (curMin >= curMax) {
                return false
            }
        }
        return curMin < curMax
    }
}
