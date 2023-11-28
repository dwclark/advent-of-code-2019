import static Aoc.*

def all = lines('data/01') { it.toInteger() }
def single(w) { w.intdiv(3) - 2 }
def cont(f) { return single(f) <= 0 ? 0 : single(f) + cont(single(f)) }

printAssert("Part 1:", all.sum { single(it) }, 3301059,
	    "Part 2:", all.sum { cont(it) }, 4948732)
