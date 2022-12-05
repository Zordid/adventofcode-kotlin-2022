import java.awt.Toolkit
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.StringSelection
import java.awt.datatransfer.Transferable

class Day05 : Day(5, 2022) {

    val p = chunkedInput().map { it }

    override fun part1(): Any? {
        val (l, i) = p
        val stacks = (0..8).map { ArrayDeque<Char>() }
        l.reversed().drop(1).forEach { l ->
            stacks.forEachIndexed { index, stack ->
                val c = l.getOrNull((index * 4) + 1) ?: ' '
                if (c != ' ')
                    stack.add(c)
            }
        }
        stacks.forEach { println(it) }

        i.forEach {
            val (q, from, to) = it.extractAllIntegers()
            repeat(q) {
                stacks[to-1].add(stacks[from-1].removeLast())
            }
        }

        return stacks.mapNotNull { it.lastOrNull() }.joinToString("")
    }

    override fun part2(): Any? {
        val (l, i) = p
        val stacks = (0..8).map { ArrayDeque<Char>() }
        l.reversed().drop(1).forEach { l ->
            stacks.forEachIndexed { index, stack ->
                val c = l.getOrNull((index * 4) + 1) ?: ' '
                if (c != ' ')
                    stack.add(c)
            }
        }
        stacks.forEach { println(it) }

        i.forEach {
            val (q, from, to) = it.extractAllIntegers()
            (0 until q).map { stacks[from-1].removeLast() }.reversed()
                .forEach {
                stacks[to-1].add(it)
            }
        }

        return stacks.mapNotNull { it.lastOrNull() }.joinToString("")
    }

}

fun main() {
    solve<Day05>(
        """
    [D]    
[N] [C]    
[Z] [M] [P]
 1   2   3 

move 1 from 2 to 1
move 3 from 1 to 3
move 2 from 2 to 1
move 1 from 1 to 2
    """.trimIndent(), "CMZ", "MCD"
    )


}

fun clip(a: Any?) {
    val clipboard: Clipboard = Toolkit.getDefaultToolkit().getSystemClipboard()
    val transferable: Transferable = StringSelection(a.toString())
    clipboard.setContents(transferable, null)
}
