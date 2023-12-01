import static Aoc.*

List make(List lines) {
    def orbits = [:], backwards = [:], paths = [:]
    lines.each { line ->
	(first, second) = line.split('\\)')
	orbits.get(first, []) << second
	paths.get(first, []) << second
	paths.get(second, []) << first
	backwards[second] = first
    }

    [ orbits, backwards, paths ]
}

int findPathLength(Map paths) {
    BestCost bc = new BestCost(Heap.min())
    bc.add('YOU', 0)
    String n = null
    while((n = bc.next()) != null) {
	if(n == 'SAN') return bc.cost(n) - 2
	else paths[n].each { p -> bc.add(p, bc.cost(n) + 1) }
    }
}

(orbits, backwards, paths) = make(lines("data/06"))
int countHops(String key) { key == "COM" ? 0 : 1 + countHops(backwards[key]) }
int countOrbits() { backwards.keySet().sum { countHops(it) } }

printAssert("Part 1:", countOrbits(), 147807, "Part 2:", findPathLength(paths), 229)
