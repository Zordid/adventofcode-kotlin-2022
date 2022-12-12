# Advent Of Code 2022

Solutions in [Kotlin][kotlin], the most **fun** languge on the planet!

My solutions to the ingenious Advent Of Code 2022[^aoc] by Eric Wastl.

This is my ~~fifth~~ sixth year of Advent of Code coding in a row - my body gets trained again to get up at 5:45 in the
morning for almost a month... the addiction is real! This year even more coders are saving Christmas using Kotlin -
thanks to JetBrains' support and their cool content! Thx, [Sebi][sebi]

If you are into programming, logic, maybe also a little into competition, this one is for you as well!

### Overview of the puzzles

|  Day | Title                   | Vis? | Notes                                                                                             |
|-----:|-------------------------|------|---------------------------------------------------------------------------------------------------|
|  [1] | Calorie Counting        |      | Chunks of calories, delimited by blank lines. Split it!                                           |
|  [2] | Rock Paper Scissors     |      | Win against the elves by understanding a cheat sheet                                              |
|  [3] | Rucksack Reorganization |      | A neat thing about sets and intersections                                                         |
|  [4] | Camp Cleanup            |      | Play around with ranges and operators                                                             |
|  [5] | Supply Stacks           | YES  | The famous tower of Hanoi with stacks of crates                                                   |
|  [6] | Tuning Trouble          |      | String marker detection in a signal - too easy!                                                   |
|  [7] | No Space Left On Device |      | Let's free up some space on a disk drive.                                                         |
|  [8] | Treetop Tree House      | YES  | The first grid puzzle of 2022! Looking through a forest with trees of different heights.          |
|  [9] | Rope Bridge             | YES  | A variant of "Snake" with a rope of knots moving around.                                          |
| [10] | Cathode-Ray Tube        | YES  | The first CPU simulation in 2022, albeit a very rudimentary one controlling a screen.             |
| [11] | Monkey in the Middle    |      | Let some monkey's play around with items and get worried about it. Modulus Arithmetics!           |
| [12] | Hill Climbing Algorithm |      | Ok, now we're into graph search algorithms finding the easiest path to climb to an elevated spot. |

## My logbook of 2022

### [Day 12][12]: Hill Climbing Algorithm

This one is an easy one - if you have a bfs search in your toolset. Simply take the delivered heights of the area, let
your search engine find the shortest path possible obeying that steps cannot be greater than 1.

### [Day 11][11]: Monkey in the Middle

Today, I was featured on JetBrains' video series of Advent of Code in Kotlin - check it
out [here](https://youtu.be/1eBSyPe_9j0).

This puzzle starts out with parsing a description about a number of Monkeys and their specific behaviour, so the first
question here is: do I want to *model* this properly or hack my way through the puzzle? Somehow, I went modelling this
way, soon to find out the first question is: to be mutable or not to be mutable? ;-)

Went the mutable way first, but quickly regretted that decision as the Monkeys throw items at each other, so every
Monkey needs "access" to all the other ones. This quickly gets messy.

But, hey, this is AoC and at 6 in the morning, I got little other things to do - so I went for it.

Part 1 basically could be solved by properly simulating the game of the Monkeys.

But for part 2, I quickly new this is not going to be done without thinking about the problem. First problem was that
modelling the worry level as an `Int` in Kotlin leads to horrible change of data type to at least `Long`... but I also
fell into the trap to at least try to brute force this and use `BigInteger` for a literally arbitrarily large number of
a worry level.
No. Does not work. The numbers grow and the calculation speed decreases as the machine eats up memory more and more.

The solution to this one lies in understanding "the world of a Monkey". As its only decision made is based on a division
remainder, each and every Monkey's world can be restricted to
a [modular arithmetic](https://en.wikipedia.org/wiki/Modular_arithmetic) without the Monkey even noticing the
difference. But as there are several Monkeys interacting, the trick is to find the least common multiple of each
individual Monkey's own modulus and use that for modulus for all of their actions.

Applying `% modulus` on the new worry levels keeps the numbers nice and small and 10,000 iterations is not a problem at
all. So, the code from part 1 runs almost unaltered to before.

### [Day 10][10]: Cathode-Ray Tube

Looks like we're back in CPU simulation business! But wait: only two instructions? Do nothing and addx? Turns out, here
the trick is to simulate micro operations where one instruction takes up longer time to do its work than the other.
Internally this leads to clock cycles passing by while still working on a portion of a bigger operation.
Sadly, the instruction reading took way too long - it's kind of complicated when you read "at the start of cycle N",
then "during cycle" and "at the end". I over engineered my solution providing micro ops and turn my assembler code into
those first, having a machine that can run only the pre-processed micro ops.

In part 2, a screen comes back that we have already seen last year. Maybe we should write OCR to automatically detect
the characters displayed?

### [Day 9][9]: Rope Bridge

Good start today with some internally created problems of my setup... *sigh* I am not yet warmed up in 2022!
A few changes to my internal checking of demo data lead to a glitch before moving to part 2 - it somehow also checked my
not yet given expected result of part 2 - `null`... This forced me to quickly add an expectation for part 2 - and that
in turn made me quickly jump to the end, looking for the new expectation, 36.... but wait... I forgot to update the demo
data for part 2 and accidentally executed it on the demo input for part 1 which had way too few moves for the tail to
start moving a lot. :-/ Lost at least 10 minutes before I noticed that. Bummer!

But other than that: time for a visualization! Yeah!! ;-) I like these puzzles!

### [Day 8][8]: Treetop Tree House

How many times do I want to lament about some mistakes made...? ;-) This time, I started out wanting to solve it with
brute force and copy&paste code for 4 directions over and over again, using loops but also some fold commands. Turned
out like a horror story! I have ready-made tools for working with Grid-like puzzles but I was determined to do
everything with the standard library only so my convoluted loops grew over my head pretty quick as the predicates were
somehow not quite right. Having to twist and turn them in four different spots, all at once while obeying the needed -1
and +1 and `..` and `downTo` in the different directions... not good at all!

In the end - my lesson: do it one step at a time and build your tools from scratch if you need them rather than starting
with convoluted code...

What's needed here?

Firstly: a Grid, represented as a `List<List<T>>` and locations, conveniently done with `Pair<Int, Int>` in Kotlin. One
typealias and two operator funs are enough to keep cool from the start:

    typealias Point = Pair<Int, Int>
    operator fun Point.plus(other: Point): Point = first + other.first to second + other.second
    operator fun <T> List<List<T>>.get(p: Point) = this[p.second][p.first]

Armed with these, you can add the four directions so using the plus operator you can now "walk" into all directions!

    private val directions = listOf(-1 to 0, +1 to 0, 0 to -1, 0 to +1)

Sequences come in **very** handy using the sequence builders to allow imperative style code to be well hidden from the
outside - a perfect match:

    private fun Point.treesInDirection(dir: Point) = sequence {
        var next = this@treesInDirection + dir
        while (next.first in colIndices && next.second in rowIndices) {
            yield(next)
            next += dir
        }
    }

This function gives you a list of Points in a given direction that are within the region and starting one step next to
your origin! Perfect for applying the puzzle's different criteria like "is the tree here visible from the outside" which
just means "can you find *any* direction where all trees in that direction are smaller?"

    directions.any { d ->
        here.treesInDirection(d).all { tree-> heights[tree] < height }
    }

Part 2 added a little twist, I think. It's not *that* easy to code something like count *until* you hit a larger or
equally sized tree or if you don't count how many smaller trees there are... But - have a look at my code!

### [Day 7][7]: No Space Left On Device

Ok, again a heavy input parsing task! Give me a log of your terminal output and I will calculate the total bytes per
directory. Of course I suspected that there will be more difficult questions coming up so I made two Maps - one holding
each file, one for each directory.
The first approach to parse is simply iterating over the output, remembering the present working directory `pwd` and "
understanding" each possible output making sure that unknown lines will be reported with an error. That's one way to
quickly see that you missed one of the instructions in the puzzle text.
My first failure was that I indeed did not accumulate the files in deeper directories to *all* of the directories above.
So, I had to fix that. After that, it all went well.

Part 2 was a different story. Even though I was *immediately* sure it would be easy, I did the most stupid of *all*
mistakes. Because my setup allows me to safeguard me from wrong answers, I can add the expected answer to the test
input. So I did. Looking for what's wanted, my eyes stopped at "choose the smallest: d" and I added "d" as the correct
answer for the demo data...
My part 2 worked like a charm, the expected output matched and I submitted my smallest directory *name* that would do
the trick... and it did not work out...
AoC's answer was that *maybe* I am **not looking at the full data set**!

Starting to look for what's wrong, I went all directions:

- in which format should I submit a directories name? Just its name or its full path?
- should I add the first "/" at the beginning or not?
- maybe my algorithm is wrong after all?

It did not occur to me that I was not supposed to answer with the total size of that directory instead of its name...
for almost 15 minutes I turned around and around, trying to remember what I had submitted already, not to submit
something twice. ...until I *read* the task again.

Lessons learned: **RTFM**!

### [Day 6][6]: Tuning Trouble

I do not like puzzles that are way too easy. This one turns out to be a one liner and the adaptations from part 1 to
part 2 is nothing more than changing the only magic number. Why did I even get up that early? Well, to lose my
leaderboard position it is.
Got my fingers completely tangled when trying to copy & paste my second result - pasted total crap (code) into it and
hit Enter too fast. Got blocked for one minute, cursed, waited - and then entered a wrong result, because I did not take
that minute to check my code properly. Nightmares at 6 in the morning...
The solution to this puzzle in Kotlin - again a one-liner! Easy. How to find the first occurrence of
a sub string within a string that consists of n different characters? Just write that statement:

    val s = "bvwbjplbgvbhsrlpgdmjqwftvncz"
    val len = 4
    val pos = s.asIterable().windowed(size = len).indexOfFirst { it.toSet().size == len }

Note: the `asIterable()` is needed to make windowed() deliver `List<List<Char>>` (without creating a list instance
as `toList()` would do!

### [Day 5][5]: Supply Stacks

Luckily my first intuition worked out that building a quick parser helps to speed up my processing. Others in my
leaderboard tried to copy&paste it manually and made some mistakes with that manual labor, so I finally got back to
position 1.

Initially I went right to using ArrayDeque as the data structure for the stacks, but later reverted that to immutable
Lists - if you don't require the speed from optimized data structures, maybe you should not use them in the first
place? ;-)

### [Day 4][4]: Camp Cleanup

This was a nightmare to parse because I got my fingers tangled - not a good start at all. But the problem, once you got
the data in the right format, is pretty easy. It's all about ranges and overlapping and containing. To my surprise, the
Kotlin standard library does not define operator functions for that purpose. You can check whether an int is within a
range using the operator function `contains` by just saying `n in range`, but you cannot ask whether another range is
contained in a range.

So, operator overload to the rescue!

    private operator fun IntRange.contains(other: IntRange) =
        first >= other.first && last <= other.last

Because this is checking for one range contained in another, you have to reverse the question by flipping around the
intervals and combining the results with or

    first in second || second in first

For part two, an infix function `overlaps` can be defined to check for partial overlapping

    private infix fun IntRange.overlaps(other: IntRange) =
        first <= other.last && last >= other.first

This time, one check is enough, because the operation is commutative: `first overlaps second` - Done.

### [Day 3][3]: Rucksack Reorganization

Lost my position 1 on our private leaderboard... *sigh*
It took me a little too long to read the text and properly understanding it. My first iteration had some require
statements to safe me from doing stupid stuff, like not splitting in right the middle of the rucksack's items list. Nice
finding for the priority calculation, though. Instead of some "magic" numbers when dealing with codepoint arithmetic,
you can easily write an easy to comprehend priorities list in Kotlin like this:

    val priorites: List<Char> = ('a'..'z') + ('A'..'Z')

Sweet. Of course this lacks a bit of performance, but might be more understandable than if's and `c - 'A' + 27`!

Addition: thanks to a colleague, my splitting of the rucksack items in half looks nicer now:

    val (itemsIn1: Set<Char>, itemsIn2: Set<Char>) = rucksack.chunked(rucksack.length / 2) { it.toSet() }

### [Day 2][2]: Rock Paper Scissors

I do not like to type `this[0]` and `this[1]` in the morning... use destructuring more often when dealing with input
data helps! To avoid `if else` or `when` hell, map Rock, Paper, Scissors onto the values 0, 1 and 2 - winning means
being exactly 1 "above" your opponents value - wrapping around at the value 2. One of the important aspects today is to
use `mod` function instead of `%` operator, which is the remainder and not floor division and helps you to correctly "go
down" one step.

Addition: an interesting way to destructure a String without using split is to convert it to a List which is giving you
the inherent destructuring tools. Here, only the first and third characters were of interest:

    val (first, _, third) = s.toList()

### [Day 1][1]: Calorie Counting

Done for today - the fight is real to get up as early as 5:45 again for almost one month! But, it worked. At least I did
not mess this one up. ;-)

[^aoc]:
[Advent of Code][aoc] – An annual event of Christmas-oriented programming challenges started December 2015.
Every year since then, beginning on the first day of December, a programming puzzle is published every day for
twenty-five days.
You can solve the puzzle and provide an answer using the language of your choice.

[aoc]: https://adventofcode.com

[kotlin]: https://www.kotlinlang.org/

[sebi]: https://github.com/SebastianAigner

[1]: src/main/kotlin/Day01.kt

[2]: src/main/kotlin/Day02.kt

[3]: src/main/kotlin/Day03.kt

[4]: src/main/kotlin/Day04.kt

[5]: src/main/kotlin/Day05.kt

[6]: src/main/kotlin/Day06.kt

[7]: src/main/kotlin/Day07.kt

[8]: src/main/kotlin/Day08.kt

[9]: src/main/kotlin/Day09.kt

[10]: src/main/kotlin/Day10.kt

[11]: src/main/kotlin/Day11.kt

[12]: src/main/kotlin/Day12.kt