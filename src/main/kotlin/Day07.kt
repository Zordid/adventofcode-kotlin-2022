class Day07 : Day(7, 2022, "No Space Left On Device") {

    val p = input

    val directories = buildMap<List<String>, Long> {
        var pwd = listOf("")
        for (o in p) {
            when {
                o == "$ cd /" -> pwd = listOf("")
                o == "$ cd .." -> pwd = pwd.dropLast(1)
                o.startsWith("$ cd ") -> {
                    pwd = pwd + o.substringAfter("$ cd ")
                }
                o.extractAllLongs().isNotEmpty() -> {
                    pwd.indices.forEach {
                        val sd = pwd.take(it + 1)
                        put(sd, getOrDefault(sd, 0L) + o.extractAllLongs().first())
                    }
                }

                o == "$ ls" -> {}
                o.startsWith("dir") -> {}
                else -> error(o)
            }
        }
    }

    override fun part1() = directories.filterValues { it <= 100000 }.values.sum()

    override fun part2(): Long {
        val total = 70_000_000L
        val needed = 30_000_000L
        val available = total - directories[listOf("")]!!
        val requires = needed - available

        return directories.filter { it.value >= requires }.minBy { it.value }.value
    }

}

fun main() {
    solve<Day07> {
        """
            ${'$'} cd /
            ${'$'} ls
            dir a
            14848514 b.txt
            8504156 c.dat
            dir d
            ${'$'} cd a
            ${'$'} ls
            dir e
            29116 f
            2557 g
            62596 h.lst
            ${'$'} cd e
            ${'$'} ls
            584 i
            ${'$'} cd ..
            ${'$'} cd ..
            ${'$'} cd d
            ${'$'} ls
            4060174 j
            8033020 d.log
            5626152 d.ext
            7214296 k
        """.trimIndent()(95437, 24933642)
    }
}