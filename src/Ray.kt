data class Ray(val origin: Vec3, val direction: Vec3) {
    fun pointAtParameter(t: Double): Vec3 {
        return origin + direction * t
    }
}
