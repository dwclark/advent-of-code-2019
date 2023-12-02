import static Aoc.*

def file = new File("data/09")
printAssert("Part 1:", Intcode.from(file, new IoBus().write(1L)).call().bus.writes[0], 3839402290,
	    "Part 2:", Intcode.from(file, new IoBus().write(2L)).call().bus.writes[0], 35734)
