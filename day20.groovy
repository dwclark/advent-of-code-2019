import static Aoc.*
import groovy.transform.Immutable
import static java.lang.Character.isLetter

enum Kind {
    OUTER, INNER, MID, START, FINISH, NULL
    boolean isWarp() { return this.is(OUTER) || this.is(INNER) }
}

class Point {
    final int row
    final int col
    final String label
    final Kind kind

    Point(int row, int col) {
	this(row, col, null, null)
    }
    
    Point(int row, int col, String label, Kind kind) {
	this.row = row
	this.col = col
	this.label = label

	if('AA' == label)
	    this.kind = Kind.START
	else if('ZZ' == label)
	    this.kind = Kind.FINISH
	else
	    this.kind = kind
    }
    
    @Override
    boolean equals(Object rhs) {
	if(!(rhs instanceof Point))
	    return false

	Point other = (Point) rhs
	return row == other.row && col == other.col
    }

    @Override
    int hashCode() { return row + 31 * col }

    @Override
    String toString() { "(${row},${col},${label},${kind})" }

    List<Point> getPossibleNeighbors() {
	[ new Point(row,col-1), new Point(row,col+1), new Point(row-1,col), new Point(row+1,col) ]
    }
    
    List<Point> visitable(Map<Point,Point> maze, Set<Point> visited) {
	possibleNeighbors.inject([]) { List<Point> list, Point possible ->
	    if(maze.containsKey(possible) && !visited.contains(possible))
		return list << maze[possible]
	    else
		return list
	}
    }

    Point warpable(Map<Point,Point> maze, Set<Point> visited) {
	if(!kind.warp)
	    return null

	Point possible = maze.keySet().find { Point p -> p.label == label && p != this }
	return (possible && !visited.contains(possible)) ? possible : null
    }
}

class Level {
    final Set<Point> visited
    final Point via

    Level(Point via) { this(via, new HashSet()) }

    Level(Point via, Set visited) {
	this.via = via
	this.visited = visited
    }
    
    Level copy() { new Level(via, new HashSet(visited)) }
}

static Map<Point,Point> parseMaze(List<String> lines) {
    Map<Point,Point> tmp = [:]
    
    lines.eachWithIndex { String line, int row ->
	line.eachWithIndex { String ch, int col ->
	    if(ch == '.') {
		String label = ''
		Kind kind = Kind.MID
		if(isLetter(line[col-1] as char)) {
		    label = line[col-2] + line[col-1]
		    kind = (col-2 == 0) ? Kind.OUTER : Kind.INNER
		}
		else if(isLetter(line[col+1] as char)) {
		    label = line[col+1] + line[col+2]
		    kind = (col+3 == line.length()) ? Kind.OUTER : Kind.INNER
		}
		else if(isLetter(lines[row-1][col] as char)) {
		    label = lines[row-2][col] + lines[row-1][col]
		    kind = (row-2 == 0) ? Kind.OUTER : Kind.INNER
		}
		else if(isLetter(lines[row+1][col] as char)) {
		    label = lines[row+1][col] + lines[row+2][col]
		    kind = (row+3 == lines.size()) ? Kind.OUTER : Kind.INNER
		}

		final Point p = new Point(row, col, label, kind)
		tmp[p] = p
	    }
	}
    }

    return Map.copyOf(tmp)
}

static testParse() {
    def maze = parseMaze(new File('data/20').readLines())
    assert maze[new Point(63,2)].kind == Kind.START
    assert maze[new Point(112,59)].kind == Kind.FINISH

    def test = { p, label, kind ->
	Point found = maze[p]
	assert found.label == label && found.kind == kind && found.kind.warp
    }
    
    test(new Point(2,59), 'RN', Kind.OUTER)
    test(new Point(28,57), 'BI', Kind.INNER)
    test(new Point(43,2), 'DY', Kind.OUTER)
    test(new Point(45,28), 'KS', Kind.INNER)
    test(new Point(112,61), 'OW', Kind.OUTER)
    test(new Point(86,67), 'ZU', Kind.INNER)
    test(new Point(69,110), 'ND', Kind.OUTER)
    test(new Point(51,84), 'ZV', Kind.INNER)
}

testParse()

static part1(String path) {
    Map<Point,Point> maze = parseMaze(new File(path).readLines())
    Point start = maze.keySet().find { it.kind == Kind.START }
    Point finish = maze.keySet().find { it.kind == Kind.FINISH }

    Set<Point> visited = new HashSet()
    List working = new LinkedList()
    working.add([start, 0])
    Point point
    int soFar
    
    while(working) {
	(point, soFar) = working.pop()
	if(point == finish)
	    return soFar
	
	visited.add point
	
	point.visitable(maze, visited).each { Point next ->
	    working.add([next, soFar+1])
	}

	Point warp = point.warpable(maze, visited)
	if(warp)
	    working.add([warp, soFar+1])
    }
}


static part2(String path) {
    Map<Point,Point> maze = parseMaze(new File(path).readLines())
    Map<Point,Point> bottom = Map.copyOf(maze.findAll { k, v -> k.kind != Kind.OUTER })
    Map<Point,Point> other = Map.copyOf(maze.findAll { k, v -> !(k.kind in [Kind.START, Kind.FINISH]) })
    Point start = maze.keySet().find { it.kind == Kind.START }
    Point finish = maze.keySet().find { it.kind == Kind.FINISH }
    
    List working = new LinkedList() //next, soFar, recursion, List<Level>
    working.add([start, 0, 0, [new Level(null)]])
    Point point
    int soFar
    int recursion
    List<Level> levelStack
    
    println "max recursive depth should be: ${maze.keySet().findAll { it.kind == Kind.INNER }.size()}"
    
    while(working) {
	(point, soFar, recursion, levelStack) = working.pop()
	if(point == finish)
	    return soFar
	
	Level currentLevel = levelStack[recursion]
	currentLevel.visited.add point
	Map<Point,Point> currentMaze = (recursion > 0) ? other : bottom
	
	point.visitable(currentMaze, currentLevel.visited).each { Point next ->
	    working.add([next, soFar+1, recursion, levelStack])
	}
	
	if(point.kind == Kind.INNER && !(0..recursion).any { idx -> levelStack[idx].via == point }) {
	    final int nextRecursion = recursion + 1
	    final boolean fresh = nextRecursion == levelStack.size()
	    final Level nextLevel = fresh ? new Level(point) : levelStack[nextRecursion]
	    final Point warp = point.warpable(other, nextLevel.visited)
	    if(warp) {
		if(fresh) {
		    List<Set> nextStack = levelStack.collect { it.copy() } + nextLevel
		    working.add([warp, soFar+1, nextRecursion, nextStack])
		}
		else {
		    working.add([warp, soFar+1, nextRecursion, levelStack])
		}
	    }
	}
	else if(point.kind == Kind.OUTER) {
	    final int prevRecursion = recursion - 1
	    final Level prevLevel = levelStack[prevRecursion]
	    final Map<Point,Point> prevMaze = (recursion > 0) ? other : bottom
	    final Point warp = point.warpable(prevMaze, prevLevel.visited)
	    if(warp)
		working.add([warp, soFar+1, prevRecursion, levelStack])
	}
    }
}

println part2('data/20c')
