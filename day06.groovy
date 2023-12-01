import static Aoc.*

Map makeOrbits(List lines) {
    lines.inject([:]) { map, line ->
	(first, second) = line.split('\\)')
	map.get(first, []) << second
	map
    }
}

Map makeBackwards(Map orbits) {
    orbits.inject([:]) { map, k, list ->
	list.each { v -> map[v] = k }
	map
    }
}

Map makePaths(Map orbits, Map backwards) {
    def copy = orbits.collectEntries { k, list -> new MapEntry(k, new ArrayList(list)) }
    backwards.inject(copy) { m, k, v ->
	if(m[k]) m[k].add(v)
	else m[k] = [v]
	m
    }
}

int findPathLength(Map paths) {
    BestCost bc = new BestCost(Heap.min())
    bc.add('YOU', 0)
    String n = null
    while((n = bc.next()) != null) {
	if(n == 'SAN') {
	    return bc.cost(n) - 2
	}
	else {
	    paths[n].each { p -> bc.add(p, bc.cost(n) + 1) }
	}
    }
}

orbits = makeOrbits(lines("data/06"))
backwards = makeBackwards(orbits)
paths = makePaths(orbits, backwards)
int countHops(String key) { key == "COM" ? 0 : 1 + countHops(backwards[key]) }
int countOrbits() { backwards.keySet().sum { countHops(it) } }

printAssert("Part 1:", countOrbits(), 147807,
	    "Part 2:", findPathLength(paths), 229)
