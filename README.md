# Advent of Code 2019

## Overview

This repo contains solutions to the first eight days of [Advent of Code 2019](https://adventofcode.com/2019) done in Awk. Each day is a self contained awk script that can be run directly, just give the day's input file as the first argument to the script. I gave up during day 9, which is included as my last attempt to get it working correctly.

## Why did I give up?

Put simply, Awk is not the right tool for this year. Why? It seemed like I spent a lot of time parsing inputs during the 2020 AoC. As I was learning Awk, and since Awk has such good functionality for input parsing, it seemed like Awk _might_ be a good fit for another AoC. All of the inputs for 2019 were extremely simple. Awk was still good at parsing these inputs, in fact on most days the input parsing is a single line of code, sometimes zero lines of code (for example, Day 6).

## Why does Awk fail here?

In a word: INTCODE. So many aspects of trying to code the INTCODE computer are simply not suited for Awk. Not just INTCODE itself, but also the way in which the requirements for INTCODE were dribbled out over time made implementing it in Awk very difficult. Here's why

### Language Challenges

* Automatic type conversion is great for simple scripts that fit on your screen. After that, it's really nice for the compiler/interpreter to give some information that you are trying to do something wrong the data. I had a few hard to diagnose bugs because I thought something was a string when it was really a number (and vice-versa)
* It's hard to use Awk's arrays correctly. Specifically, it's really easy to do something wrong with the array, have Awk think you are trying to use it as a scalar, and then get the dreaded `Attempt to use array in a scalar context` error.
* You can't assign arrays to anything other than keys in an array. If `b` is an array, then this: `a = b` is illegal no matter what.
* You can't allocate anything, no `malloc/new` for you! For small scripts this is great, you shouldn't be worrying about allocation anyway. In fact, I think this one issue may be the best guidance for when you should use Awk and when you should avoid it:

**If you have a problem and you can name everything you work with in the script (i.e. you can give everything you want to use a variable name), Awk is probably a safe choice. If not, avoid Awk. I mean everything. If you think to yourself, "I'll need a couple of those, but I want to treat them uniformly", then you can't name everything and you should avoid Awk**

### Data Structure/Algorithm Limitations

* For any algorithmic work you need lists and maps/hashtables. For small scripts you can fake the lack of lists with Awk's associative arrays and conventional indices. For large ones, you can't. Note, Perl wisely chose to have both.
* No function pointers, lambdas, or closures. This makes it very hard to abstract things.
* The previous problem would be manageable if there were some sort of classes/interfaces, but there isn't.
* You can attempt to overcome of the previous two with a creative use of associative arrays, but the lack of ability to allocate anything, being forced to name everything, and not being able to assign arrays to anything makes this error prone and tedious at best.
* Sorting sucks. I can overlook the previous problems because they can be explained away as not being needed for the kinds of problems Awk targets. Fair enough. But asort/asorti are horrible and error prone. There really needs to be an easy way to sort things in Awk, it is very much needed to solve the kinds of problems Awk targets.

### Hard to Evolve Awk Scripts

* Awk is made to solve a simple problem and then be done with it. It is not made for fulfilling nebulous requirements dribbled out arbitrarily over several days (INTCODE). If you don't know what problem you want to solve when you start out, maybe don't use Awk.
* Lack of abstraction and allocation (see previous) makes it hard to write generic test code to make sure you are not introducing bugs later on.
* It was also hard to make the INTCODE VM into a generic library given the fact that you can't allocate anything and you have to name everything. Because of this every INTCODE day ended up being an exercise in starting from scratch. You can fake all of this with _very_ creative use of associative arrays and a copious amount of copy/paste. Day 7 really showed the problem with not being able to allocate anything. Day 9 showed the problem with having to start over every time I looked at INTCODE.
