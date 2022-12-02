import com.github.ajalt.mordant.rendering.TextColors.*
import com.github.ajalt.mordant.terminal.Terminal
import org.reflections.Reflections
import java.io.File
import java.net.URL
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

@Suppress("unused")
const val FAST_MODE = false

val terminal = Terminal()

@ExperimentalTime
fun main() {
    verbose = false
    with(terminal) {
        println(red("\n~~~ Advent Of Code Runner ~~~\n"))
        val dayClasses = getAllDayClasses().sortedBy(::dayNumber)
        val totalDuration = dayClasses.map { it.execute() }.reduceOrNull(Duration::plus)
        println("\nTotal runtime: ${red("$totalDuration")}")
    }
}

private fun getAllDayClasses(): Set<Class<out Day>> =
    Reflections("").getSubTypesOf(Day::class.java)

@ExperimentalTime
private fun Class<out Day>.execute(): Duration {
    val day = constructors[0].newInstance() as Day
    print("${day.day}: ${day.title}".paddedTo(30, 30))
    val part1 = measureTimedValue { day.part1 }
    println("Part 1 [${part1.duration.toString().padStart(6)}]: ${part1.value}")
    print(" ".repeat(30))
    val part2 = measureTimedValue { day.part2 }
    println("Part 2 [${part2.duration.toString().padStart(6)}]: ${part2.value}")
    return part1.duration + part2.duration
}

private fun dayNumber(day: Class<out Day>) = day.simpleName.replace("Day", "").toInt()

/**
 * Dirty, but effective way to inject test data globally for one-time use only!
 * Will be reset after usage.
 */
var globalTestData: String? = null
    get() = field?.also {
        println("\n!!!! USING TEST DATA !!!!\n")
        field = null
    }

inline fun <reified T : Day> create(): T =
    T::class.constructors.first { it.parameters.isEmpty() }.call()

data class TestData(val input: String, val expectedPart1: Any?, val expectedPart2: Any?)

inline fun <reified T : Day> test(testData: TestData) = with(testData) {
    globalTestData = input
    with(create<T>()) {
        if (expectedPart1 != null) {
            println("Checking part 1... ")
            part1().let { result ->
                check(result.toString() == expectedPart1.toString()) {
                    "Part 1 result failed!\nExpected: $expectedPart1\nActual: $result"
                }
            }
        }
        if (expectedPart2 != null) {
            println("Checking part 2... ")
            part2().let { result ->
                check(result.toString() == expectedPart2.toString()) {
                    "Part 2 result failed!\nExpected: $expectedPart2\nActual: $result"
                }
            }
        }
    }
}

inline fun <reified T : Day> solve(
    testData: String? = null,
    expectedPart1: Any? = null,
    expectedPart2: Any? = null,
    onlyTest: Boolean = false,
) {
    if (testData != null && (expectedPart1 != null || expectedPart2 != null)) {
        test<T>(TestData(testData, expectedPart1, expectedPart2))
    }
    if (!onlyTest)
        create<T>().solve()
}

/**
 * Global flag to indicate verbosity or silence
 */
var verbose = true

@Suppress("unused", "MemberVisibilityCanBePrivate")
sealed class Day(val day: Int, private val year: Int = 2022, val title: String = "unknown") {

    init {
        require(day in 1..25) { "Wrong day $day" }
        require(year in 2015..2050) { "Wrong year $year" }
    }

    private val header: Unit by lazy { if (verbose) println("--- AoC $year, Day $day: $title ---\n") }

    private val rawInput: List<String> by lazy { globalTestData?.split("\n") ?: AoC.getPuzzleInput(day, year) }

    // all the different ways to get your input
    val input: List<String> by lazy { rawInput.show("Raw") }
    val inputAsGrid: List<List<Char>> by lazy { rawInput.map { it.toList() }.show("Grid") }
    val inputAsInts: List<Int> by lazy { rawInput.map { it.extractInt() }.show("Int") }
    val inputAsLongs: List<Long> by lazy { rawInput.map { it.extractLong() }.show("Long") }
    val inputAsString: String by lazy { rawInput.joinToString("\n").also { listOf(it).show("One string") } }

    var chunkDelimiter: (String) -> Boolean = String::isEmpty
    val inputChunks: List<List<String>> by lazy { chunkedInput(chunkDelimiter) }

    fun chunkedInput(delimiter: (String) -> Boolean = String::isEmpty): List<List<String>> {
        val result = mutableListOf<List<String>>()
        var currentSubList: MutableList<String>? = null
        for (line in rawInput) {
            if (delimiter(line)) {
                currentSubList = null
            } else {
                if (currentSubList == null) {
                    currentSubList = mutableListOf(line)
                    result += currentSubList
                } else
                    currentSubList.add(line)
            }
        }
        return result.show("Chunked into ${result.size} chunks of")
    }

    fun <T> mappedInput(lbd: (String) -> T): List<T> =
        rawInput.map(catchingMapper(lbd)).show("Mapped")

    fun <T> parsedInput(
        columnSeparator: Regex = Regex("\\s+"),
        predicate: (String) -> Boolean = { true },
        lbd: ParserContext.(String) -> T,
    ): List<T> =
        rawInput.filter(predicate).map(parsingMapper(columnSeparator, lbd)).show("Parsed")

    fun <T> matchedInput(regex: Regex, lbd: (List<String>) -> T): List<T> =
        rawInput.map(matchingMapper(regex, lbd)).show("Matched")

    val part1: Any? by lazy { part1() }
    val part2: Any? by lazy { part2() }

    open fun part1(): Any? = "not yet implemented"
    open fun part2(): Any? = "not yet implemented"

    fun solve() {
        header
        runWithTiming("1") { part1 }
        runWithTiming("2") { part2 }
    }

    fun <T> T.show(prompt: String = "", maxLines: Int = 10): T {
        if (!verbose) return this
        header
        if (this is List<*>)
            show(prompt, maxLines)
        else
            println("$prompt: $this")
        return this
    }

    private fun <T : Any?> List<T>.show(type: String, maxLines: Int = 10): List<T> {
        if (!verbose) return this
        header

        with(listOfNotNull(type.takeIf { it.isNotEmpty() }, "input data").joinToString(" ")) {
            println("==== $this ${"=".repeat(50 - length - 6)}")
        }
        val idxWidth = lastIndex.toString().length
        preview(maxLines) { idx, data ->
            val original = rawInput.getOrNull(idx)
            val s = when {
                rawInput.size != this.size -> "$data"
                original != "$data" -> "${original.paddedTo(40, 40)} => $data"
                else -> original
            }
            println("${idx.toString().padStart(idxWidth)}: ${s.paddedTo(0, 160)}")
        }
        println("=".repeat(50))
        return this
    }

    companion object {
        private fun <T> matchingMapper(regex: Regex, lbd: (List<String>) -> T): (String) -> T = { s ->
            regex.matchEntire(s)?.groupValues?.let {
                runCatching { lbd(it) }.getOrElse { ex(s, it) }
            } ?: error("Input line does not match regex: \"$s\"")
        }

        private fun <T> catchingMapper(lbd: (String) -> T): (String) -> T = { s ->
            runCatching { lbd(s) }.getOrElse { ex(s, it) }
        }

        private fun <T> parsingMapper(columnSeparator: Regex, lbd: ParserContext.(String) -> T): (String) -> T = { s ->
            runCatching {
                ParserContext(columnSeparator, s).lbd(s)
            }.getOrElse { ex(s, it) }
        }

        private fun ex(input: String, ex: Throwable): Nothing =
            error("Exception on input line\n\n\"$input\"\n\n$ex")

        private fun <T> List<T>.preview(maxLines: Int, f: (idx: Int, data: T) -> Unit) {
            if (size <= maxLines) {
                forEachIndexed(f)
            } else {
                val cut = (maxLines - 1) / 2
                (0 until maxLines - cut - 1).forEach { f(it, this[it]!!) }
                if (size > maxLines) println("...")
                (lastIndex - cut + 1..lastIndex).forEach { f(it, this[it]!!) }
            }
        }

    }

}

@OptIn(ExperimentalTime::class)
inline fun runWithTiming(part: String, f: () -> Any?) {
    val (result, duration) = measureTimedValue(f)
    with(terminal) { success("\nSolution $part: (took $duration)\n" + brightBlue("$result")) }
}

@Suppress("unused", "MemberVisibilityCanBePrivate")
class ParserContext(private val columnSeparator: Regex, private val line: String) {
    val cols: List<String> by lazy { line.split(columnSeparator) }
    val nonEmptyCols: List<String> by lazy { cols.filter { it.isNotEmpty() } }
    val nonBlankCols: List<String> by lazy { cols.filter { it.isNotBlank() } }
    val ints: List<Int> by lazy { line.extractAllIntegers() }
    val longs: List<Long> by lazy { line.extractAllLongs() }
}

private fun String.extractInt() = toIntOrNull() ?: sequenceContainedIntegers().first()
private fun String.extractLong() = toLongOrNull() ?: sequenceContainedLongs().first()

private val numberRegex = Regex("(-+)?\\d+")

fun String.sequenceContainedIntegers(): Sequence<Int> =
    numberRegex.findAll(this)
        .mapNotNull { m -> m.value.toIntOrNull() ?: warn("Number too large for Int: ${m.value}") }

fun String.sequenceContainedLongs(): Sequence<Long> =
    numberRegex.findAll(this)
        .mapNotNull { m -> m.value.toLongOrNull() ?: warn("Number too large for Long: ${m.value}") }

private fun <T> warn(msg: String): T? {
    with(terminal) { warning("WARNING: $msg") }
    return null
}

fun String.extractAllIntegers(): List<Int> = sequenceContainedIntegers().toList()
fun String.extractAllLongs(): List<Long> = sequenceContainedLongs().toList()

private fun Any?.paddedTo(minWidth: Int, maxWidth: Int) = with(this.toString()) {
    when {
        length > maxWidth -> this.substring(0, maxWidth - 3) + "..."
        length < minWidth -> this.padEnd(minWidth)
        else -> this
    }
}


object AoC {
    fun getPuzzleInput(day: Int, year: Int): List<String> {
        val cached = readInputFile(day, year)
        if (cached != null) return cached

        return runCatching {
            downloadInput(day, year).also {
                writeInputFile(day, year, it)
            }
        }.getOrElse { listOf("Unable to download $day/$year: $it") }
    }

    private fun downloadInput(day: Int, year: Int): List<String> {
        println("Downloading puzzle for $year, day $day...")
        val uri = "https://adventofcode.com/$year/day/$day/input"
        val cookies = mapOf("session" to getSessionCookie())

        val url = URL(uri)
        val connection = url.openConnection()
        connection.setRequestProperty(
            "Cookie", cookies.entries.joinToString(separator = "; ") { (k, v) -> "$k=$v" }
        )
        connection.connect()
        val result = arrayListOf<String>()
        connection.getInputStream().bufferedReader().useLines { result.addAll(it) }
        return result
    }

    fun download(uri: String): String {
        val url = URL(uri)
        val cookies = mapOf("session" to getSessionCookie())

        val connection = url.openConnection()
        connection.setRequestProperty(
            "Cookie", cookies.entries.joinToString(separator = "; ") { (k, v) -> "$k=$v" }
        )
        connection.connect()
        return connection.getInputStream().bufferedReader().readText()
    }

//    // not working yet
//    fun submitAnswer(day: Int, year: Int, level: Int, answer: Any?): List<String> {
//        println("Submitting answer for $year, day $day...")
//        val uri = "https://adventofcode.com/$year/day/$day/answer"
//        val cookies = mapOf("session" to getSessionCookie())
//
//        val payload = """{ "level": $level, "answer": \"$answer\" }"""
//
//        val url = URL(uri)
//        val result = mutableListOf<String>()
//        with(url.openConnection() as HttpURLConnection) {
//            requestMethod = "POST"
//            setRequestProperty(
//                "Cookie", cookies.entries.joinToString(separator = "; ") { (k, v) -> "$k=$v" }
//            )
//            setRequestProperty("Accept", "application/json")
//            doOutput = true
//            outputStream.bufferedWriter().use { it.write(payload) }
//            inputStream.bufferedReader().useLines { result.addAll(it) }
//        }
//        return result
//    }

    private fun getSessionCookie() =
        System.getenv("AOC_COOKIE")
            ?: object {}.javaClass.getResource("session-cookie")?.readText()
            ?: error("Cookie missing")

    private fun readInputFile(day: Int, year: Int): List<String>? {
        val file = File(fileNameFor(day, year))
        file.exists() || return null
        return file.bufferedReader().readLines()
    }

    private fun writeInputFile(day: Int, year: Int, puzzle: List<String>) {
        File(pathNameForYear(year)).mkdirs()
        File(fileNameFor(day, year)).writeText(puzzle.joinToString("\n"))
    }

    private fun pathNameForYear(year: Int) = "puzzles/$year"
    private fun fileNameFor(day: Int, year: Int) = "${pathNameForYear(year)}/day${"%02d".format(day)}.txt"

}

@Suppress("unused")
val pass = Unit