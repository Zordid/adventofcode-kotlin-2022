# Advent Of Code 2022
Solutions in [Kotlin][kotlin], the most **fun** languge on the planet!

My solutions to the ingenious Advent Of Code 2022[^aoc] by Eric Wastl.

This is my ~~fifth~~ sixth year of Advent of Code coding in a row - my body gets trained again to get up at 5:45 in the morning for almost a month... the addiction is real! This year even more coders are saving Christmas using Kotlin - thanks to JetBrains' support and their cool content! Thx, [Sebi][sebi]

If you are into programming, logic, maybe also a little into competition, this one is for you as well!

### Overview of the puzzles
| Day | Title                   | Runtime | Remarks                                                 |
|----:|-------------------------|--------:|---------------------------------------------------------|
| [1] | Calorie Counting        | few ns? | Chunks of calories, delimited by blank lines. Split it! |
| [2] | Rock Paper Scissors     | few ns? | Win against the elves by understanding a cheat sheet    |
| [3] | Rucksack Reorganization | few ns? | A neat thing about sets and intersections               |
| [4] | Camp Cleanup            | few ns? | Play around with ranges and operators                   |
| [5] | Supply Stacks           | few ns? | The famous tower of Hanoi with stacks of crates         |

## My logbook of 2022

### [Day 5][5]: Supply Stacks
Luckily my first intuition worked out that building a quick parser helps to speed up my processing. Others in my leaderboard tried to copy&paste it manually and made some mistakes with that manual labor, so I finally got back to position 1.

Initially I went right to using ArrayDeque as the data structure for the stacks, but later reverted that to immutable Lists - if you don't require the speed from optimized data structures, maybe you should not use them in the first place? ;-)

### [Day 4][4]: Camp Cleanup
This was a nightmare to parse because I got my fingers tangled - not a good start at all. But the problem, once you got the data in the right format, is pretty easy. It's all about ranges and overlapping and containing. To my surprise, the Kotlin standard library does not define operator functions for that purpose. You can check whether an int is within a range using the operator function `contains` by just saying `n in range`, but you cannot ask whether another range is contained in a range.

So, operator overload to the rescue!

    private operator fun IntRange.contains(other: IntRange) =
        first >= other.first && last <= other.last

Because this is checking for one range contained in another, you have to reverse the question by flipping around the intervals and combining the results with or

    first in second || second in first

For part two, an infix function `overlaps` can be defined to check for partial overlapping

    private infix fun IntRange.overlaps(other: IntRange) =
        first <= other.last && last >= other.first

This time, one check is enough, because the operation is commutative: `first overlaps second` - Done.

### [Day 3][3]: Rucksack Reorganization
Lost my position 1 on our private leaderboard... *sigh*
It took me a little too long to read the text and properly understanding it. My first iteration had some require statements to safe me from doing stupid stuff, like not splitting in right the middle of the rucksack's items list. Nice finding for the priority calculation, though. Instead of some "magic" numbers when dealing with codepoint arithmetic, you can easily write an easy to comprehend priorities list in Kotlin like this:

    val priorites: List<Char> = ('a'..'z') + ('A'..'Z')

Sweet. Of course this lacks a bit of performance, but might be more understandable than if's and `c - 'A' + 27`!

Addition: thanks to a colleague, my splitting of the rucksack items in half looks nicer now:

    val (itemsIn1: Set<Char>, itemsIn2: Set<Char>) = rucksack.chunked(rucksack.length / 2) { it.toSet() }

### [Day 2][2]: Rock Paper Scissors
I do not like to type `this[0]` and `this[1]` in the morning... use destructuring more often when dealing with input data helps! To avoid `if else` or `when` hell, map Rock, Paper, Scissors onto the values 0, 1 and 2 - winning means being exactly 1 "above" your opponents value - wrapping around at the value 2. One of the important aspects today is to use `mod` function instead of `%` operator, which is the remainder and not floor division and helps you to correctly "go down" one step.

Addition: an interesting way to destructure a String without using split is to convert it to a List which is giving you the inherent destructuring tools. Here, only the first and third characters were of interest:

    val (first, _, third) = s.toList()

### [Day 1][1]: Calorie Counting
Done for today - the fight is real to get up as early as 5:45 again for almost one month! But, it worked. At least I did not mess this one up. ;-)

[^aoc]:
    [Advent of Code][aoc] – An annual event of Christmas-oriented programming challenges started December 2015. 
    Every year since then, beginning on the first day of December, a programming puzzle is published every day for twenty-five days.
    You can solve the puzzle and provide an answer using the language of your choice.

[aoc]: https://adventofcode.com
[kotlin]: https://www.kotlinlang.org/
[sebi]: https://github.com/SebastianAigner
[1]: src/main/kotlin/Day01.kt
[2]: src/main/kotlin/Day02.kt
[3]: src/main/kotlin/Day03.kt
[4]: src/main/kotlin/Day04.kt
[5]: src/main/kotlin/Day05.kt