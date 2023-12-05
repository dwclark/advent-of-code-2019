import static Aoc.*
import groovy.transform.Immutable

@Immutable class Point {
    int row, col

    public List<Point> neighbors() {
	return [new Point(row + 1, col), new Point(row - 1, col),
		new Point(row, col + 1), new Point(row, col -1)]
    }
}

class Maze {
    static final Set KEYS = ('a'..'z').toSet()
    static final Set DOORS = ('A'..'Z').toSet()
    static final String START = '@'
    static final String WALL = '#'
    static final String FREE = '.'
    
    final Map<String,Point> mazeKeys = [:]
    final Map<String,Point> mazeDoors = [:]
    final Map<Point,String> graph = [:]
    final List<Point> starts

    private boolean shouldAdd(String s) {
	(s in KEYS) || (s in DOORS) || (s == START) || (s == FREE)
    }
    
    Maze(List<String> lines) {
	def tmpKeys = [:], tmpDoors = [:], tmpGraph = [:], tmpStarts = []
	lines.eachWithIndex { String line, row ->
	    line.eachWithIndex { String s, col ->
		Point p = new Point(row, col)
		if(shouldAdd(s)) tmpGraph[p] = s
		if(s in KEYS) tmpKeys[s] = p
		else if(s in DOORS) tmpDoors[s] = p
		else if(s == START) tmpStarts += p } }
	mazeKeys = tmpKeys.asImmutable()
	mazeDoors = tmpDoors.asImmutable()
	graph = tmpGraph.asImmutable()
	starts = tmpStarts.asImmutable()
    }

    private String addKey(final String prev, final String latest) {
	return ((prev + latest) as List).sort().join()
    }
    
    int solve() {
	BestCost bc = new BestCost(Heap.min())
	bc.add([starts, ""], 0)
	List current = null
	while((current = bc.next()) != null) {
	    int cost = bc.cost(current)
	    List<Point> points = current[0]
	    String keys = current[1]

	    for(Point at : points) {
		String content = graph[at]
		if(content in KEYS && !keys.contains(content)) {
		    keys = addKey(keys, content)
		    
		    if(keys.size() == mazeKeys.size()) {
			return cost;
		    }
		}
	    }

	    points.eachWithIndex { Point at, int index ->
		at.neighbors().each { n ->
		    String nextContent = graph[n]
		    if(nextContent == FREE || nextContent == START || nextContent in KEYS ||
		       (nextContent in DOORS && keys.contains(nextContent.toLowerCase()))) {
			List copy = new ArrayList(points)
			copy[index] = n
			bc.add([copy, keys], cost + 1)
		    } } }
	}
    }
}

//assert new Maze(lines("data/18a")).solve() == 8
//assert new Maze(lines("data/18b")).solve() == 86
//assert new Maze(lines("data/18c")).solve() == 132
//assert new Maze(lines("data/18d")).solve() == 136
//assert new Maze(lines("data/18e")).solve() == 81
printAssert("Part 1:", new Maze(lines("data/18")).solve(), 5068)

//assert new Maze(lines("data/18pb_a")).solve() == 24
//assert new Maze(lines("data/18pb_b")).solve() == 32
//assert new Maze(lines("data/18pb_c")).solve() == 72
//println new Maze(lines("data/18pb")).solve()
