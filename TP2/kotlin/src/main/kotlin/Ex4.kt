import kotlin.math.sqrt

data class Vec2(val x: Double, val y: Double) : Comparable<Vec2> {

    operator fun plus(other: Vec2): Vec2 = Vec2(x + other.x, y + other.y)
    operator fun minus(other: Vec2): Vec2 = Vec2(x - other.x, y - other.y)
    operator fun times(scalar: Double): Vec2 = Vec2(x * scalar, y * scalar)
    operator fun unaryMinus(): Vec2 = Vec2(-x, -y)

    override fun compareTo(other: Vec2): Int {
        return this.magnitude().compareTo(other.magnitude())
    }

    operator fun get(index: Int): Double {
        return when (index) {
            0 -> x
            1 -> y
            else -> throw IndexOutOfBoundsException("Index $index is out of bounds for Vec2")
        }
    }

    fun magnitude(): Double = sqrt(x * x + y * y)

    fun dot(other: Vec2): Double = (x * other.x) + (y * other.y)

    fun normalized(): Vec2 {
        val mag = magnitude()
        if (mag == 0.0) throw IllegalStateException("Cannot normalize a zero vector")
        return Vec2(x / mag, y / mag)
    }
}

operator fun Double.times(vec: Vec2): Vec2 = vec * this

fun main() {
    val a = Vec2(3.0, 4.0)
    val b = Vec2(1.0, 2.0)

    println("a = $a")
    println("b = $b")
    println("a + b = ${a + b}")
    println("a - b = ${a - b}")
    println("a * 2.0 = ${a * 2.0}")

    // Challenge: Left-hand multiplication
    println("2.0 * a = ${2.0 * a}")

    println("-a = ${-a}")
    println("|a| = ${a.magnitude()}")
    println("a dot b = ${a.dot(b)}")
    println("norm(a) = ${a.normalized()}")

    println("a[0] = ${a[0]}")
    println("a[1] = ${a[1]}")

    println("a > b = ${a > b}")
    println("a < b = ${a < b}")

    // Challenge: Destructuring
    val (xCoord, yCoord) = a
    println("Destructured a: x=$xCoord, y=$yCoord")

    val vectors = listOf(Vec2(1.0, 0.0), Vec2(3.0, 4.0), Vec2(0.0, 2.0))
    println("Longest = ${vectors.maxOrNull()}")
    println("Shortest = ${vectors.minOrNull()}")
}