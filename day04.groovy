import static Aoc.*

def input = (272091..815432)
def adj = { s -> (0..<(s.length()-1)).any { i -> s[i] == s[i+1] } }
def asc = { s -> (0..<(s.length()-1)).every { i -> s[i].toInteger() <= s[i+1].toInteger() } }
def group2 = { s -> s.toList().groupBy { it }.find { k,v -> v.size() == 2 } }
def part1 = { s -> adj(s) && asc(s) }
def part2 = { s -> part1(s) && group2(s) }

printAssert("Part 1:", input.count { n -> part1(n.toString()) }, 931,
	    "Part 2:", input.count { n -> part2(n.toString()) }, 609)
