data class Ray(val origin: Vec3, val direction: Vec3, val time: Double = 0.0) {
    fun pointAtParameter(t: Double): Vec3 {
        return origin + direction * t
    }
}
