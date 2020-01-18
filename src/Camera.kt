data class Camera(val origin: Vec3, val lower_left_corner: Vec3, val horizontal: Vec3, val vertical: Vec3) {
    fun getRay(u: Double, v: Double): Ray {
        return Ray(origin, lower_left_corner + horizontal * u + vertical * v - origin)
    }
}
