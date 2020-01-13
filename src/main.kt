import java.io.File

fun main() {
    File("scene.ppm").printWriter().use { out ->
        val nx = 200
        val ny = 100
        out.print("P3\n$nx $ny\n255\n")
        for (j in ny - 1 downTo 0) {
            for (i in 0 until nx) {
                val col = Vec3((i.toDouble() / (nx - 1)), (j.toDouble() / (ny - 1)), 0.2)
                val color = col * 255.0
                val r = color.r().toInt()
                val g = color.g().toInt()
                val b = color.b().toInt()
                out.print("$r $g $b\n")
            }
        }
    }
}

private operator fun Double.times(color: Vec3): Vec3 {
    return color * this
}
