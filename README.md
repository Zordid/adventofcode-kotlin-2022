# Advent Of Code 2022
Solutions in [Kotlin](https://www.kotlinlang.org/), the most fun language on the planet!

My solutions to the ingenious [Advent Of Code 2022](https://adventofcode.com/) by Eric Wastl.

This is my ~~fifth~~ sixth year of Advent of Code coding in a row - my body gets trained again to get up at 5:45 in the morning for almost a month... the addiction is real! This year even more coders are saving Christmas using Kotlin - thanks to JetBrains' support and their cool content! Thx, [Sebi](https://github.com/SebastianAigner)

If you are into programming, logic, maybe also a little into competition, this one is for you as well!

### Overview of the puzzles
| Day | Title               | Runtime | Remarks                                                 |
|----:|---------------------|--------:|---------------------------------------------------------|
|   1 | Calorie Counting    | few ns? | Chunks of calories, delimited by blank lines. Split it! |
|   2 | Rock Paper Scissors | few ns? | Win against the elves by understanding a cheat sheet    |

## My logbook of 2022

### Day 2: Rock Paper Scissors
I do not like to type `this[0]` and `this[1]` in the morning... use destructuring more often when dealing with input data helps! To avoid `if else` or `when` hell, map Rock, Paper, Scissors onto the values 0, 1 and 2 - winning means being exactly 1 "above" your opponents value - wrapping around at the value 2. One of the important aspects today is to use `mod` function instead of `%` operator, which is the remainder and not floor division and helps you to correctly "go down" one step.

### Day 1: Calorie Counting
Done for today - the fight is real to get up as early as 5:45 again for almost one month! But, it worked. At least I did not mess this one up. ;-)