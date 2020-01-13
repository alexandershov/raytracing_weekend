import java.io.File

fun main() {
    File("scene.ppm").printWriter().use { out ->
        val nx = 200
        val ny = 100
        out.print("P3\n$nx $ny\n255\n")
        for (j in ny - 1 downTo 0) {
            for (i in 0 until nx) {
                val r = ((i.toDouble() / (nx - 1)) * 255).toInt()
                val g = ((j.toDouble() / (ny - 1)) * 255).toInt()
                val b = (0.2 * 255).toInt()
                out.print("$r $g $b\n")
            }
        }
    }
}
