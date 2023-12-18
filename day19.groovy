import static Aoc.*
import groovy.transform.Immutable

class Finder {
    def ioBus
    def intCode
    
    Finder() {
	ioBus = IoBus.separateChannels()
	intCode = Intcode.from(new File("data/19"), ioBus)
    }

    Object call(Integer x, Integer y) {
	intCode.reset()
	ioBus.readChannel.put(x)
	ioBus.readChannel.put(y)
	intCode.call()
	return ioBus.writeChannel.take()
    }
}

class SquareFinder {
    Finder f = new Finder()
    int row, guessCol
    final int squareSize

    SquareFinder(int squareSize, int rowHint = 3, int colHint = 4) {
	this.squareSize = squareSize
	this.row = rowHint
	this.guessCol = colHint
    }

    List find() {
	while(true) {
	    int col = guessCol
	    while(f(row, col) == 0) {
		++col
	    }
	    println "trying row ${row}, col ${col}"
	    
	    if(columnMatch(col) &&
	       rowMatch(row - (squareSize - 1), col) &&
	       rowMatch(row, col)) {
		return [ row - (squareSize - 1), col ]
	    }
	    else {
		++row
		++guessCol
	    }
	}
    }

    boolean columnMatch(int col) {
	for(int i = 0; i < squareSize; ++i) {
	    if(f(row - i, col) == 0) {
		return false
	    }
	}

	return true
    }

    boolean rowMatch(int row, int col) {
	for(int i = 0; i < squareSize; ++i) {
	    if(f(row, col + i) == 0) {
		return false
	    }
	}

	return true
    }
}

def part1() {
    Finder f = new Finder()
    return (0..49).sum { x -> (0..49).sum { y -> f(x, y) } }
}

def part2() {
    //added hints to make future runs go faster, originally
    //this started searching at the default hints
    SquareFinder finder = new SquareFinder(100, 900, 900)
    def (row, col) = finder.find()
    return (row * 10000) + col
}

printAssert("Part 1:", part1(), 211,
	    "Part 2:", part2(), 8071006)
