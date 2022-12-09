package utils

/**
 * An alias for looking at `List<List<T>>` as a [Grid].
 *
 * Important: Grids are always treated as densely filled. If a Grid has rows with fewer elements, use
 * [fixed] helper function to fix this issue.
 */
typealias Grid<T> = List<List<T>>
typealias MutableGrid<T> = MutableList<MutableList<T>>

fun <T> List<List<T>>.asGrid(): Grid<T> = this

val Grid<*>.width: Int get() = firstOrNull()?.size ?: 0
val Grid<*>.height: Int get() = size
val Grid<*>.area: Area get() = origin to lastPoint

/**
 * The last (bottom right) point in this [Grid] or `-1 to -1` for an empty Grid.
 */
val Grid<*>.lastPoint get() = width - 1 to height - 1

/**
 * Creates a new [Grid] with the specified [size], where each element is calculated by calling
 * the specified [init] function.
 */
inline fun <T> Grid(width: Int, height: Int, init: (Point) -> T): Grid<T> = MutableGrid(width, height, init)

/**
 * Creates a new [MutableGrid] with the specified [size], where each element is calculated by calling
 * the specified [init] function.
 */
inline fun <T> MutableGrid(width: Int, height: Int, init: (Point) -> T): MutableGrid<T> {
    require(width >= 0 && height >= 0) { "Given area $width x $height must not be negative" }
    return MutableList(height) { y ->
        MutableList(width) { x -> init(x to y) }
    }
}

fun <T> Grid(map: Map<Point, T>, default: T): Grid<T> = Grid(map) { default }

inline fun <T> Grid(width: Int, height: Int, map: Map<Point, T>, crossinline default: (Point) -> T): Grid<T> =
    Grid(width, height) { p -> map.getOrElse(p) { default(p) } }

inline fun <T> Grid(map: Map<Point, T>, crossinline default: (Point) -> T): Grid<T> {
    val (first, last) = map.keys.boundingArea() ?: return emptyList()
    require(first.x >= 0 && first.y >= 0) {
        "Given Map contains negative points. Maybe construct using Grid(width, height) { custom translation }"
    }
    val area = origin to last
    return Grid(area.width, area.height) { p ->
        map.getOrElse(p) { default(p) }
    }
}

/**
 * Returns a new [MutableGrid] filled with all elements of this Grid.
 */
fun <T> Grid<T>.toMutableGrid(): MutableGrid<T> = MutableList(size) { this[it].toMutableList() }

/**
 * Fixes missing elements in a [Grid] by filling in `null`.
 * @return a completely uniform n x m Grid
 */
fun <T> Grid<T>.fixed(): Grid<T?> = fixed(null)

/**
 * Fixes missing elements in a [Grid] by filling in [default].
 * @return a completely uniform n x m Grid
 */
fun <T> Grid<T>.fixed(default: T): Grid<T> {
    val (min, max) = asSequence().map { it.size }.minMaxOrNull() ?: return this
    if (min == max) return this
    return map { row ->
        row.takeIf { row.size == max } ?: List(max) { idx -> if (idx <= row.lastIndex) row[idx] else default }
    }
}

inline fun <T> Grid<T>.searchIndices(crossinline predicate: (T) -> Boolean): Sequence<Point> =
    area.allPoints().filter { predicate(this[it]) }

fun <T> Grid<T>.searchIndices(vararg elements: T): Sequence<Point> =
    searchIndices { it in elements }

fun Grid<*>.indices(): Sequence<Point> = sequence {
    for (y in this@indices.indices) {
        for (x in this@indices[y].indices)
            yield(x to y)
    }
}

inline fun <T> Grid<T>.forAreaIndexed(f: (p: Point, v: T) -> Unit) {
    for (y in this.indices)
        for (x in this[y].indices)
            f(x to y, this[y][x])
}

inline fun <T> Grid<T>.forArea(f: (p: Point) -> Unit) {
    for (y in this.indices)
        for (x in this[y].indices)
            f(x to y)
}

fun <T> Grid<T>.transposed() =
    List(height) { row -> List(height) { col -> this[col][row] } }

fun <T> Grid<T>.toMapGrid(vararg sparseElements: T): Map<Point, T> =
    toMapGrid { it in sparseElements }

inline fun <T> Grid<T>.toMapGrid(sparsePredicate: (T) -> Boolean): Map<Point, T> =
    buildMap { forAreaIndexed { p, v -> if (!sparsePredicate(v)) this[p] = v } }

fun <T, R> Grid<T>.mapValues(transform: (T) -> R): Grid<R> =
    map { it.map(transform) }

fun <T, R> Grid<T>.mapValuesIndexed(transform: (Point, T) -> R): Grid<R> =
    mapIndexed { y, r -> r.mapIndexed { x, v -> transform(x to y, v) } }

fun <T> Grid<T>.formatted(transform: (Point, T) -> String = { _, value -> "$value" }) =
    withIndex().joinToString(System.lineSeparator()) { (y, row) ->
        row.withIndex().joinToString("") { (x, value) -> transform(x to y, value) }
    }

operator fun <T> Grid<T>.get(p: Point): T =
    if (p.y in indices && p.x in first().indices) this[p.y][p.x]
    else notInGridError(p)

fun <T> Grid<T>.getOrNull(p: Point): T? =
    if (p.y in indices && p.x in first().indices) this[p.y][p.x]
    else null

inline fun <T> Grid<T>.getOrElse(p: Point, default: (Point) -> T): T =
    if (p.y in indices && p.x in first().indices) this[p.y][p.x]
    else default(p)

operator fun <T> MutableGrid<T>.set(p: Point, v: T) {
    if (p.y in indices && p.x in first().indices) this[p.y][p.x] = v
    else notInGridError(p)
}

operator fun List<String>.get(p: Point): Char =
    if (p.y in indices && p.x in first().indices) this[p.y][p.x]
    else notInListGridError(p)

fun List<String>.getOrNull(p: Point): Char? =
    if (p.y in indices && p.x in first().indices) this[p.y][p.x]
    else null

fun List<String>.getOrElse(p: Point, default: (Point) -> Char): Char =
    if (p.y in indices && p.x in first().indices) this[p.y][p.x]
    else default(p)

private fun Grid<*>.notInGridError(p: Point): Nothing =
    error("Point $p not in grid of dimensions $width x $height")

private fun List<String>.notInListGridError(p: Point): Nothing =
    error("Point $p not in grid of dimensions ${firstOrNull()?.let { it.length } ?: 0} x $size")
