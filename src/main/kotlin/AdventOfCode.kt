@file:Suppress("unused")

import AoCWebInterface.Verdict
import Part.*
import com.github.ajalt.mordant.rendering.TextColors.*
import com.github.ajalt.mordant.rendering.TextStyles
import com.github.ajalt.mordant.terminal.Terminal
import org.reflections.Reflections
import java.awt.Toolkit
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.StringSelection
import java.awt.datatransfer.Transferable
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.reflect.KClass
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue
import kotlin.time.toJavaDuration

const val FAST_MODE = false

val aocTerminal = Terminal()

@ExperimentalTime
fun main() {
    verbose = false
    with(aocTerminal) {
        println(red("\n~~~ Advent Of Code Runner ~~~\n"))
        val dayClasses = getAllDayClasses().sortedBy(::dayNumber)
        val totalDuration = dayClasses.map { it.execute() }.reduceOrNull(Duration::plus)
        println("\nTotal runtime: ${red("$totalDuration")}")
    }
}

private fun getAllDayClasses(): Collection<Class<out Day>> =
    Reflections("").getSubTypesOf(Day::class.java).filter { it.simpleName.matches(Regex("Day\\d+")) }

@ExperimentalTime
private fun Class<out Day>.execute(): Duration {
    val day = constructors[0].newInstance() as Day
    print("${day.day}: ${day.title}".restrictWidth(30, 30))
    val part1 = measureTimedValue { day.part1 }
    println("Part 1 [${part1.duration}]: ${part1.value}")
    print(" ".repeat(30))
    val part2 = measureTimedValue { day.part2 }
    println("Part 2 [${part2.duration}]: ${part2.value}")
    return part1.duration + part2.duration
}

private fun dayNumber(day: Class<out Day>) = day.simpleName.replace("Day", "").toInt()

/**
 * Dirty, but effective way to inject test data globally for one-time use only!
 * Will be reset on first read.
 */
var globalTestData: String? = null
    get() = field?.also {
        println("\n!!!! USING TEST DATA !!!!\n")
        field = null
    }

var logEnabled = false
fun log(message: Terminal.() -> Any?) {
    if (logEnabled && verbose) alog(message)
}

fun alog(message: Terminal.() -> Any?) {
    if (verbose) println(aocTerminal.message().takeIf { it != Unit } ?: "")
}

fun <T : Day> create(dayClass: KClass<T>): T =
    dayClass.constructors.first { it.parameters.isEmpty() }.call()

data class TestData(val input: String, val expectedPart1: Any?, val expectedPart2: Any?) {

    fun <T : Day> passesTestsUsing(dayClass: KClass<T>): Boolean {
        (expectedPart1 != null || expectedPart2 != null) || return true
        globalTestData = input
        val day = create(dayClass)
        return listOfNotNull(
            Triple(1, { day.part1() }, "$expectedPart1").takeIf { expectedPart1 != null },
            Triple(2, { day.part2() }, "$expectedPart2").takeIf { expectedPart2 != null }
        ).all { (part, partFun, expectation) ->
            println(gray("Checking part $part against $expectation..."))
            val actual = partFun()
            val match = actual == Day.NotYetImplemented || "$actual" == expectation
            if (!match) {
                aocTerminal.danger("Checking of part $part failed")
                println("Expected: ${brightRed(expectation)}")
                println("  Actual: ${brightRed("$actual")}")
                println(yellow("Check demo ${TextStyles.bold("input")} and demo ${TextStyles.bold("expectation")}!"))
            }
            match
        }
    }

}

inline fun <reified T : Day> solve(offerSubmit: Boolean = false, test: SolveDsl<T>.() -> Unit = {}) {
    if (SolveDsl(T::class).apply(test).isEverythingOK())
        create(T::class).solve(offerSubmit)
}

class SolveDsl<T : Day>(private val dayClass: KClass<T>) {

    val tests = mutableListOf<TestData>()

    var ok = true
    operator fun String.invoke(part1: Any? = null, part2: Any? = null) {
        ok || return
        ok = TestData(this, part1, part2).passesTestsUsing(dayClass)
    }

    infix fun String.part1(expectedPart1: Any?): TestData =
        TestData(this, expectedPart1, null).also { tests += it }

    infix fun String.part2(expectedPart2: Any?) {
        TestData(this, null, expectedPart2).also { tests += it }
    }

    infix fun TestData.part2(expectedPart2: Any?) {
        tests.remove(this)
        copy(expectedPart2 = expectedPart2).also { tests += it }
    }

    fun isEverythingOK() =
        ok && tests.all { it.passesTestsUsing(dayClass) }
}

/**
 * Global flag to indicate verbosity or silence when loading puzzle input
 */
var verbose = true

@Suppress("MemberVisibilityCanBePrivate")
open class Day private constructor(
    val fqd: FQD,
    val title: String,
    private val terminal: Terminal,
) {
    constructor(day: Int, year: Int, title: String = "unknown", terminal: Terminal = aocTerminal) : this(
        FQD(day, Event(year)), title, terminal
    )

    val day = fqd.day
    val year = fqd.year

    var testInput = false

    private val header: Unit by lazy { if (verbose) println("--- AoC $year, Day $day: $title ---\n") }

    private val rawInput: List<String> by lazy {
        globalTestData?.also {
            logEnabled = true
            testInput = true
        }?.split("\n") ?:
        AoC.getPuzzleInput(day, year).also {
            logEnabled = false
        }
    }

    // all the different ways to get your input
    val input: List<String> by lazy { rawInput.show("Raw") }
    val inputAsGrid: List<List<Char>> by lazy { rawInput.map { it.toList() }.show("Grid") }
    val inputAsInts: List<Int> by lazy { rawInput.map { it.extractInt() }.show("Int") }
    val inputAsLongs: List<Long> by lazy { rawInput.map { it.extractLong() }.show("Long") }
    val inputAsString: String by lazy { rawInput.joinToString("\n").also { listOf(it).show("One string") } }

    var groupDelimiter: (String) -> Boolean = String::isEmpty
    val inputAsGroups: List<List<String>> by lazy { groupedInput(groupDelimiter) }

    fun groupedInput(delimiter: (String) -> Boolean): List<List<String>> {
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

    open fun part1(): Any? = NotYetImplemented

    open fun part2(): Any? = NotYetImplemented

    fun solve(offerSubmit: Boolean) {
        header
        runWithTiming("1") { part1 }
        runWithTiming("2") { part2 }
        if (offerSubmit) submit(part1, part2)
    }

    private fun submit(part1: Any?, part2: Any?) {
        println()
        if ("$part1" == "$part2") {
            aocTerminal.println(yellow("The two answers are identical. No submitting allowed."))
            return
        }
        listOfNotNull(part2.isPossibleAnswerOrNull(P2), part1.isPossibleAnswerOrNull(P1)).firstOrNull()
            ?.let { (part, answer) ->
                with(aocTerminal) {
                    val previouslySubmitted = AoC.previouslySubmitted(day, year, part)
                    if (answer in previouslySubmitted) {
                        println(brightMagenta("This answer to part $part has been previously submitted!"))
                        return
                    }
                    if (previouslySubmitted.isNotEmpty()) {
                        println(previouslySubmitted)
                    }
                    val extra = previouslySubmitted.waitSecondsOrNull?.let {
                        "wait $it seconds and then "
                    }.orEmpty()
                    val choice = prompt(
                        brightCyan("""Should I ${extra}submit "${brightBlue(answer)}" as answer to part $part?"""),
                        choices = listOf("y", "n"),
                        default = "n"
                    )
                    if (choice == "y") {
                        previouslySubmitted.waitUntilFree()
                        val verdict = AoC.submitAnswer(fqd, part, answer)
                        println(verdict)
                        AoC.appendSubmitLog(day, year, part, answer, verdict)
                    }
                }
            }
    }

    private fun Any?.isPossibleAnswerOrNull(part: Part): Pair<Part, String>? =
        (part to "$this").takeIf { (_, sAnswer) ->
            this !in listOf("null", 0, -1, NotYetImplemented) && sAnswer.length > 1 && "\n" !in sAnswer
        }

    fun <T> T.show(prompt: String = "", maxLines: Int = 10): T {
        verbose || return this

        header
        if (this is List<*>)
            show(prompt, maxLines)
        else
            println("$prompt: $this")
        return this
    }

    private fun <T : Any?> List<T>.show(type: String, maxLines: Int = 10): List<T> {
        verbose || return this

        header
        with(listOfNotNull(type.takeIf { it.isNotEmpty() }, "input data").joinToString(" ")) {
            println("==== $this ${"=".repeat(50 - length - 6)}")
        }
        val idxWidth = lastIndex.toString().length
        preview(maxLines) { idx, data ->
            val original = rawInput.getOrNull(idx)
            val s = when {
                rawInput.size != this.size -> "$data"
                original != "$data" -> "${original.restrictWidth(40, 40)} => $data"
                else -> original
            }
            println("${idx.toString().padStart(idxWidth)}: ${s.restrictWidth(0, 160)}")
        }
        println("=".repeat(50))
        return this
    }

    object NotYetImplemented {
        override fun toString() = "not yet implemented"
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
    with(aocTerminal) {
        success("\nSolution $part: (took $duration)\n" + brightBlue("$result"))
    }
}

@Suppress("MemberVisibilityCanBePrivate")
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
private val positiveNumberRegex = Regex("\\d+")

fun String.sequenceContainedIntegers(startIndex: Int = 0, includeNegativeNumbers: Boolean = true): Sequence<Int> =
    (if (includeNegativeNumbers) numberRegex else positiveNumberRegex).findAll(this, startIndex)
        .mapNotNull { m -> m.value.toIntOrNull() ?: warn("Number too large for Int: ${m.value}") }

fun String.sequenceContainedLongs(startIndex: Int = 0, includeNegativeNumbers: Boolean = true): Sequence<Long> =
    (if (includeNegativeNumbers) numberRegex else positiveNumberRegex).findAll(this, startIndex)
        .mapNotNull { m -> m.value.toLongOrNull() ?: warn("Number too large for Long: ${m.value}") }

fun String.extractAllIntegers(startIndex: Int = 0, includeNegativeNumbers: Boolean = true): List<Int> =
    sequenceContainedIntegers(startIndex, includeNegativeNumbers).toList()

fun String.extractAllLongs(startIndex: Int = 0, includeNegativeNumbers: Boolean = true): List<Long> =
    sequenceContainedLongs(startIndex, includeNegativeNumbers).toList()

@Suppress("UNCHECKED_CAST")
inline fun <reified T : Any> String.extractAllNumbers(
    startIndex: Int = 0,
    includeNegativeNumbers: Boolean = true,
    klass: KClass<T> = T::class,
): List<T> = when (klass) {
    Int::class -> extractAllIntegers(startIndex, includeNegativeNumbers)
    UInt::class -> extractAllIntegers(startIndex, false).map { it.toUInt() }
    Long::class -> extractAllLongs(startIndex, includeNegativeNumbers)
    ULong::class -> extractAllLongs(startIndex, false).map { it.toULong() }
    else -> error("Cannot extract numbers of type ${klass.simpleName}")
} as List<T>

private fun <T> warn(msg: String): T? {
    with(aocTerminal) { warning("WARNING: $msg") }
    return null
}

private fun Any?.restrictWidth(minWidth: Int, maxWidth: Int) = with("$this") {
    when {
        length > maxWidth -> substring(0, maxWidth - 3) + "..."
        length < minWidth -> padEnd(minWidth)
        else -> this
    }
}

object AoC {

    private val web = AoCWebInterface(getSessionCookie())

    fun sendToClipboard(a: Any?): Boolean {
        if (a in listOf(null, 0, -1, Day.NotYetImplemented)) return false
        return runCatching {
            val clipboard: Clipboard = Toolkit.getDefaultToolkit().systemClipboard
            val transferable: Transferable = StringSelection(a.toString())
            clipboard.setContents(transferable, null)
        }.isSuccess
    }

    fun getPuzzleInput(day: Int, year: Event): List<String> {
        val cached = readInputFileOrNull(day, year)
        if (!cached.isNullOrEmpty()) return cached

        return web.downloadInput(FQD(day, year)).onSuccess {
            writeInputFile(day, year, it)
        }.getOrElse {
            listOf("Unable to download $day/$year: $it")
        }
    }

    fun submitAnswer(fqd: FQD, part: Part, answer: String): Verdict =
        web.submitAnswer(fqd, part, answer)

    private val logFormat = DateTimeFormatter.ofPattern("HH:mm:ss")

    fun appendSubmitLog(day: Int, year: Event, part: Part, answer: String, verdict: Verdict) {
        val now = LocalDateTime.now()
        val nowText = logFormat.format(now)
        val id = idFor(day, year, part)
        val text =
            "$nowText - $id - submitted \"$answer\" - ${if (verdict is Verdict.Correct) "OK" else "FAIL with ${verdict::class.simpleName}"}"
        appendSubmitLog(year, text)
        appendSubmitLog(year, verdict.text)
        if (verdict is Verdict.WithWait) {
            val locked = now + verdict.wait.toJavaDuration()
            appendSubmitLog(year, "$nowText - $id - LOCKED until ${DateTimeFormatter.ISO_DATE_TIME.format(locked)}")
        }
    }

    class PreviousSubmitted(
        private val locked: LocalDateTime?,
        private val answers: List<String>,
        private val log: List<String>,
    ) {
        operator fun contains(answer: String) = answer in answers
        fun isNotEmpty() = answers.isNotEmpty()

        override fun toString() = (
                listOf(
                    brightMagenta("Previously submitted answers were:"),
                    "${log.size} attempts in total".takeIf { log.size > 3 }
                ) +
                        log.takeLast(3).map { it.highlight() } +
                        lockInfo()
                )
            .filterNotNull()
            .joinToString("\n", postfix = "\n ")

        private fun String.highlight() =
            split("\"", limit = 3).mapIndexed { index, s -> if (index == 1) brightMagenta(s) else s }.joinToString("")

        private fun lockInfo() = locked?.let {
            if (isStillLocked)
                brightRed("Locked until $it")
            else
                yellow("Had been locked, but is free again!")
        }

        private val isStillLocked get() = locked?.let { LocalDateTime.now() < it } == true

        val waitSecondsOrNull
            get() = locked?.let {
                val now = LocalDateTime.now()
                (it.toEpochSecond(ZoneOffset.UTC) - now.toEpochSecond(ZoneOffset.UTC))
            }.takeIf { (it ?: 0) > 0 }

        fun waitUntilFree() {
            isStillLocked || return
            with(aocTerminal) {
                do {
                    cursor.move { startOfLine();clearLine() }
                    print(brightRed("Waiting $waitSecondsOrNull more seconds..."))
                    Thread.sleep(500)
                } while (LocalDateTime.now() < locked!!)
                cursor.move { startOfLine(); clearLine() }
                println("Fire!")
            }
        }

    }

    fun previouslySubmitted(day: Int, year: Event, part: Part): PreviousSubmitted =
        readSubmitLog(year).filter { idFor(day, year, part) in it }.let { relevant ->
            val answers = relevant.filter { "submitted" in it }.mapNotNull { log ->
                log.split("\"").getOrNull(1)?.let { it to log }
            }
            val locked = if ("LOCKED" in relevant.lastOrNull().orEmpty()) {
                val lock = relevant.last().substringAfter("until ")
                LocalDateTime.from(DateTimeFormatter.ISO_DATE_TIME.parse(lock))
            } else null
            PreviousSubmitted(locked, answers.map { it.first }, answers.map { it.second })
        }

    private fun idFor(day: Int, year: Event, part: Part) =
        "$year day $day part $part"

    private fun getSessionCookie() =
        System.getenv("AOC_COOKIE")
            ?: object {}.javaClass.getResource("session-cookie")?.readText()?.lines()
                ?.firstOrNull { it.isNotBlank() }
            ?: warn("No session cookie in environment or file found, will not be able to talk to AoC server.")

    private fun readInputFileOrNull(day: Int, year: Event): List<String>? {
        val file = File(fileNameFor(day, year))
        file.exists() || return null
        return file.readLines()
    }

    private fun writeInputFile(day: Int, year: Event, puzzle: List<String>) {
        File(pathNameForYear(year)).mkdirs()
        File(fileNameFor(day, year)).writeText(puzzle.joinToString("\n"))
    }

    private fun readSubmitLog(year: Event): List<String> {
        val file = File(submitLogFor(year))
        file.exists() || return emptyList()
        return file.readLines()
    }

    private fun appendSubmitLog(year: Event, text: String) {
        File(submitLogFor(year)).appendText("\n$text")
    }

    private fun fileNameFor(day: Int, year: Event) = "${pathNameForYear(year)}/day${"%02d".format(day)}.txt"
    private fun submitLogFor(year: Event) = "${pathNameForYear(year)}/submit.log"
    private fun pathNameForYear(year: Event) = "puzzles/$year"

}

@JvmInline
value class Event(private val year: Int) {
    init {
        require(year in 2015..2050) { "Invalid year $year" }
    }

    override fun toString() = "$year"
}

enum class Part(private val level: Int) {
    P1(1), P2(2);

    override fun toString() = "$level"
}

data class FQD(val day: Int, val year: Event) {
    init {
        require(day in 1..25) { "Invalid day $day" }
    }

    override fun toString() = "$year day $day"
}

@Suppress("NOTHING_TO_INLINE")
inline fun useSystemProxies() {
    System.setProperty("java.net.useSystemProxies", "true")
}

class AoCWebInterface(private val sessionCookie: String?) {
    companion object {
        private const val BASE_URL = "https://adventofcode.com"
        private const val RIGHT = "That's the right answer"
        private const val FAIL = "That's not the right answer"
        private const val TOO_RECENT = "You gave an answer too recently"

        fun FQD.toUri() = "$BASE_URL/$year/day/$day"

        private fun String.urlEncode() = URLEncoder.encode(this, Charsets.UTF_8)
    }

    fun downloadInput(fqd: FQD): Result<List<String>> = runCatching {
        with(fqd) {
            println("Downloading puzzle for $fqd...")
            val url = URL("${fqd.toUri()}/input")
            val cookies = mapOf("session" to sessionCookie)

            with(url.openConnection()) {
                setRequestProperty(
                    "Cookie", cookies.entries.joinToString(separator = "; ") { (k, v) -> "$k=$v" }
                )
                connect()
                getInputStream().bufferedReader().readLines()
            }
        }
    }

    fun submitAnswer(fqd: FQD, part: Part, answer: String): Verdict = runCatching {
        with(fqd) {
            println("Submitting answer for $fqd...")
            val url = URL("${fqd.toUri()}/answer")
            val cookies = mapOf("session" to sessionCookie)
            val payload = "level=$part&answer=${answer.urlEncode()}"
            with(url.openConnection() as HttpURLConnection) {
                requestMethod = "POST"
                setRequestProperty(
                    "Cookie", cookies.entries.joinToString(separator = "; ") { (k, v) -> "$k=$v" }
                )
                setRequestProperty("content-type", "application/x-www-form-urlencoded")
                doOutput = true
                outputStream.bufferedWriter().use { it.write(payload) }
                inputStream.reader().readText()
            }
        }
    }.map { Verdict.of(it) }.getOrElse { Verdict.ofException(it) }

    sealed class Verdict private constructor(val text: String) {

        class Correct(response: String) : Verdict(response) {
            override fun toString() = brightGreen(text)
        }

        abstract class WithWait(response: String, val wait: Duration) : Verdict(response)

        class Incorrect(response: String, wait: Duration) : WithWait(response, wait) {
            override fun toString(): String = brightRed("Incorrect result, wait $wait")
        }

        class TooRecent(response: String, wait: Duration) : WithWait(response, wait) {
            override fun toString(): String = brightRed("Too recent submission, wait $wait")
        }

        class SomethingWrong(response: String) : Verdict(response) {
            override fun toString() = brightRed(text)
        }

        companion object {
            private fun String.pleaseWait() =
                lowercase().substringAfter("please wait ", "")
                    .substringBefore(" before trying", "").ifEmpty { null }

            private fun String.parseDuration(): Duration? {
                val (a, u) = split(" ", limit = 2)
                val amount = a.toIntOrNull() ?: when (a) {
                    "one" -> 1
                    "two" -> 2
                    else -> return null
                }
                return when (u.let { if (u.endsWith("s")) it.dropLast(1) else it }) {
                    "second" -> amount.seconds
                    "minute" -> amount.minutes
                    "hour" -> amount.hours
                    else -> null
                }
            }

            private fun String.parseMinSec(): Duration? {
                val (min, sec) =
                    (Regex(".* you have (\\d+)m (\\d+)s left to wait.*").matchEntire(lowercase())?.destructured
                        ?: return null)
                return min.toInt().minutes + sec.toInt().seconds
            }

            fun ofException(exception: Throwable): Verdict = SomethingWrong("Exception encountered: $exception")

            fun of(response: String): Verdict {
                val article = response.lines().filter { "<article>" in it }
                    .joinToString("") { it.replace(Regex("</?[^>]+(>|\$)"), "") }
                    .ifEmpty {
                        println(brightRed("WARNING: no <article> tag found in response!"))
                        "WARNING: no <article> tag found in response: $response"
                    }

                val pleaseWait = article.pleaseWait()?.parseDuration() ?: article.parseMinSec()

                return when {
                    FAIL in article && pleaseWait != null -> Incorrect(article, pleaseWait)
                    RIGHT in article && pleaseWait == null -> Correct(article)
                    TOO_RECENT in article && pleaseWait != null -> TooRecent(article, pleaseWait)
                    else -> SomethingWrong(article)
                }
            }
        }
    }

}