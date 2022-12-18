package utils.dim3d

import utils.range
import kotlin.math.absoluteValue
import kotlin.math.sign

typealias Point3D = Triple<Int, Int, Int>
typealias Cube = Pair<Point3D, Point3D>
typealias Matrix3D = List<Point3D>

val Point3D.x: Int get() = first
val Point3D.y: Int get() = second
val Point3D.z: Int get() = third

operator fun Point3D.unaryMinus() = Point3D(-x, -y, -z)

operator fun Point3D.plus(other: Point3D) =
    Point3D(x + other.x, y + other.y, z + other.z)

operator fun Point3D.minus(other: Point3D) =
    Point3D(x - other.x, y - other.y, z - other.z)

operator fun Point3D.times(n: Int) =
    Point3D(x * n, y * n, z * n)

operator fun Point3D.div(n: Int) =
    Point3D(x / n, y / n, z / n)

val Point3D.manhattanDistance: Int
    get() = x.absoluteValue + y.absoluteValue + z.absoluteValue

infix fun Point3D.manhattanDistanceTo(other: Point3D) =
    (x - other.x).absoluteValue + (y - other.y).absoluteValue + (z - other.z).absoluteValue

val Point3D.sign: Point3D get() = Triple(x.sign, y.sign, z.sign)

fun Point3D.toList() = listOf(x, y, z)
fun asPoint3D(l: List<Int>): Point3D {
    require(l.size == 3) { "List should exactly contain 3 values for x,y  and z, but has ${l.size} values!" }
    return Point3D(l[0], l[1], l[2])
}

fun Iterable<Point3D>.boundingCube(): Cube {
    val xr = map { it.x }.range()
    val yr = map { it.y }.range()
    val zr = map { it.z }.range()
    return Cube(Point3D(xr.first, yr.first, zr.first), Point3D(xr.last, yr.last, zr.last))
}

operator fun Cube.contains(p: Point3D) =
    p.x in first.x..second.x && p.y in first.y..second.y && p.z in first.z..second.z

val origin3D = Point3D(0, 0, 0)
val unitVecX = Point3D(1, 0, 0)
val unitVecY = Point3D(0, 1, 0)
val unitVecZ = Point3D(0, 0, 1)

fun Point3D.rotateX(times: Int) = rotXM[times.mod(4)] * this
fun Point3D.rotateY(times: Int) = rotYM[times.mod(4)] * this
fun Point3D.rotateZ(times: Int) = rotZM[times.mod(4)] * this

operator fun Matrix3D.times(p: Point3D): Point3D =
    Point3D(
        p.x * this[0].x + p.y * this[0].y + p.z * this[0].z,
        p.x * this[1].x + p.y * this[1].y + p.z * this[1].z,
        p.x * this[2].x + p.y * this[2].y + p.z * this[2].z,
    )

private const val COS_0 = 1
private const val COS_180 = -1
private const val COS_90 = 0
private const val COS_270 = 0
private const val SIN_0 = 0
private const val SIN_90 = 1
private const val SIN_180 = 0
private const val SIN_270 = -1

val identityMatrix3D: Matrix3D = listOf(
    Point3D(1, 0, 0),
    Point3D(0, 1, 0),
    Point3D(0, 0, 1),
)

val rotXM: List<Matrix3D> = listOf(
    identityMatrix3D,
    listOf(
        Point3D(1, 0, 0),
        Point3D(0, COS_90, -SIN_90),
        Point3D(0, SIN_90, COS_90),
    ),
    listOf(
        Point3D(1, 0, 0),
        Point3D(0, COS_180, -SIN_180),
        Point3D(0, SIN_180, COS_180),
    ),
    listOf(
        Point3D(1, 0, 0),
        Point3D(0, COS_270, -SIN_270),
        Point3D(0, SIN_270, COS_270),
    ),
)

val rotYM: List<Matrix3D> = listOf(
    identityMatrix3D,
    listOf(
        Point3D(COS_90, 0, SIN_90),
        Point3D(0, 1, 0),
        Point3D(-SIN_90, 0, COS_90),
    ),
    listOf(
        Point3D(COS_180, 0, SIN_180),
        Point3D(0, 1, 0),
        Point3D(-SIN_180, 0, COS_180),
    ),
    listOf(
        Point3D(COS_270, 0, SIN_270),
        Point3D(0, 1, 0),
        Point3D(-SIN_270, 0, COS_270),
    ),
)

val rotZM: List<Matrix3D> = listOf(
    identityMatrix3D,
    listOf(
        Point3D(COS_90, -SIN_90, 0),
        Point3D(SIN_90, COS_90, 0),
        Point3D(0, 0, 1),
    ),
    listOf(
        Point3D(COS_180, -SIN_180, 0),
        Point3D(SIN_180, COS_180, 0),
        Point3D(0, 0, 1),
    ),
    listOf(
        Point3D(COS_270, -SIN_270, 0),
        Point3D(SIN_270, COS_270, 0),
        Point3D(0, 0, 1),
    ),
)
