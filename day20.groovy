import static Aoc.*
import static java.lang.Character.isLetter

enum Kind {
    OUTER, INNER, MID, START, FINISH, NULL
    boolean isWarp() { return this.is(OUTER) || this.is(INNER) }
}

class Point {
    final int row
    final int col
    final int level
    final String label
    final Kind kind

    Point(int row, int col) {
	this(row, col, 0, null, null)
    }

    Point(int row, int col, int level) {
	this(row, col, level, null, null)
    }
    
    Point(int row, int col, int level, String label, Kind kind) {
	this.row = row
	this.col = col
	this.level = level
	this.label = label

	if('AA' == label)
	    this.kind = Kind.START
	else if('ZZ' == label)
	    this.kind = Kind.FINISH
	else
	    this.kind = kind
    }

    Point changeLevel(int newLevel) {
	return new Point(row, col, newLevel, label, kind)
    }
    
    @Override
    boolean equals(Object rhs) {
	if(!(rhs instanceof Point))
	    return false

	Point other = (Point) rhs
	return row == other.row && col == other.col && level == other.level
    }

    @Override
    int hashCode() {
	final int prime = 31
	int hash = row
	hash = prime * hash + col
	hash = prime * hash + level
	return hash
    }

    @Override
    String toString() { "(${row},${col},${level},${label},${kind})" }

    List<Point> getPossibleNeighbors() {
	[new Point(row,col-1,level), new Point(row,col+1,level),
	 new Point(row-1,col,level), new Point(row+1,col,level) ]
    }
}

class Maze {
    final Map<Point,Point> points
    final Point start
    final Point finish
    
    Maze(Map<Point,Point> points) {
	this.points = points
	this.start = points.keySet().find { it.kind == Kind.START }
	this.finish = points.keySet().find { it.kind == Kind.FINISH }
    }
    
    Maze(List<String> lines) {
	this(parse(lines))
    }

    static Map<Point,Point> parse(List<String> lines) {
	def tmp = [:]
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
		    
		    final Point p = new Point(row, col, 0, label, kind)
		    tmp[p] = p
		}
	    }
	}

	return Map.copyOf(tmp)
    }

    Point getAt(Point p) { points[p] }

    Point warpFor(Point point) {
	points.keySet().find { Point p -> p.label == point.label && p != point }
    }
    
    List<Point> canVisit(Point point) {
	List<Point> ret = []
	
	if(point.kind.warp) {
	    ret << warpFor(point)
	}
	
	point.possibleNeighbors.inject(ret) { List<Point> list, Point possible ->
	    this[possible] ? list << this[possible] : list
	}
    }

    int solve() {
	Set<Point> visited = new HashSet()
	List working = new LinkedList() << [ start, 0 ]
	
	while(working) {
	    def (Point point, int soFar) = working.pop()
	    if(point == finish)
		return soFar
	    
	    visited.add point
	    
	    canVisit(point).each { Point next ->
		if(!(next in visited))
		    working << [next, soFar+1]
	    }
	}
    }
}

class Maze2 extends Maze {
    Maze2(List<String> lines) {
	super(levels(parse(lines)))
    }

    @Override
    Point warpFor(Point point) {
	if(point.kind == Kind.INNER) {
	    return points.keySet().find { Point p ->
		p.label == point.label && p.kind == Kind.OUTER && p.level == (point.level + 1)
	    }
	}
	else {
	    return points.keySet().find { Point p ->
		p.label == point.label && p.kind == Kind.INNER && p.level == (point.level - 1)
	    }
	}
    }

    static Map<Point,Point> keepIf(Map<Point,Point> map, int level, Closure closure) {
	map.inject([:]) { ret, ignore, point ->
	    Point newPoint = point.changeLevel(level)
	    closure(point) ? ret << new MapEntry(newPoint, newPoint) : ret
	}
    }
    
    static Map<Point,Point> levels(Map<Point,Point> map) {
	Map<Point,Point> accum = [:]
	//ground level
	accum += keepIf(map, 0) { point -> point.kind != Kind.OUTER }

	//next levels
	final Integer total = map.values().sum { p -> p.kind == Kind.INNER ? 1 : 0 }
	(1..<total).each { level ->
	    accum += keepIf(map, level) { p -> !(p.kind in [Kind.START, Kind.FINISH]) }
	}

	//final level
	accum += keepIf(map, total) { p -> !(p.kind in [Kind.START, Kind.FINISH, Kind.INNER]) }
	return Map.copyOf(accum)
    }
}

def lines = new File('data/20').readLines()

printAssert("Part 1:", new Maze(lines).solve(), 448,
	    "Part 2:", new Maze2(lines).solve(), 5678)
