import static Aoc.*
import groovy.transform.Immutable

@Immutable class Point {
    int row, col

    public List<Point> neighbors() {
	return [new Point(row + 1, col), new Point(row - 1, col),
		new Point(row, col + 1), new Point(row, col -1)]
    }
}

@Immutable class Cost implements Comparable<Cost> {
    int distance
    String doors

    static Cost init() { return new Cost(0, "", "") }
    int compareTo(Cost rhs) { return distance <=> rhs.distance }
    Cost increment() { return new Cost(distance + 1, doors) }
    Cost increment(String door) { return new Cost(distance + 1, Maze.addKey(doors, door)) }
}

@Immutable class Path {
    String startId
    Point start
    String endId
    Point end
    String doors
    int distance

    boolean need(String keys) { !keys.contains(endId) }
    boolean canWalkTo(String keys) { doors.every { d -> keys.contains(d.toLowerCase()) } }
}

@Immutable class State {
    List<Point> points
    String keys

    State walk(int index, Point next, String key) {
	List<Point> tmp = new ArrayList<>(points);
	tmp[index] = next
	return new State(tmp, Maze.addKey(keys, key))
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
    final Map<Point,List<Path>> paths = [:]
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
	paths = allShortestPaths([starts, mazeKeys.values()].flatten())
    }
    
    public static String addKey(final String prev, final String latest) {
	return ((prev + latest) as List).sort().join()
    }
    
    Map<Point,List<Path>> allShortestPaths(List<Point> points) {
	points.inject([:]) { map, point -> map + [ (point): shortestPaths(point) ] }
    }
    
    List<Path> shortestPaths(final Point start) {
	List<Path> ret = []
	Set<Point> visited = new HashSet<>()
	Heap heap = Heap.min()
	visited.add(start)
	heap.insert(start, new Cost(0, ""))
	Map.Entry<Point,Cost> entry = null
	while((entry = heap.nextWithCost()) != null) {
	    Point p = entry.key
	    visited.add(p)
	    Cost cost = entry.value
	    String content = graph[p]
	    
	    if(content in KEYS && p != start) {
		ret.add(new Path(startId: graph[start], start: start,
				 endId: content, end: p,
				 doors: cost.doors, distance: cost.distance))
	    }
	    
	    p.neighbors().each { nextPoint ->
		if(!visited.contains(nextPoint) && graph[nextPoint] != null && graph[nextPoint] != WALL) {
		    String nextContent = graph[nextPoint]
		    if(nextContent in DOORS)
			heap.insert(nextPoint, cost.increment(nextContent))
		    else
			heap.insert(nextPoint, cost.increment())
		}
	    }
	}

	return ret
    }

    boolean solved(String keys) {
	mazeKeys.keySet().every { k -> keys.indexOf(k) != -1 }
    }
    
    int solve() {
	BestCost bc = new BestCost(Heap.min())
	State current = new State(points: starts, keys: "@")
	bc.add(current, 0)
	while((current = bc.next()) != null) {
	    int cost = bc.cost(current)
	    if(solved(current.keys)) {
		return cost
	    }

	    for(int i = 0; i < current.points.size(); ++i) {
		Point anchor = current.points[i]
		List<Path> neighbors = paths[anchor].findAll { it.need(current.keys) && it.canWalkTo(current.keys) }
		for(Path neighbor : neighbors) {
		    bc.add(current.walk(i, neighbor.end, neighbor.endId), cost + neighbor.distance)
		}
	    }
	}
    }
}

printAssert("Part 1:", new Maze(lines("data/18")).solve(), 5068,
	    "Part 2:", new Maze(lines("data/18pb")).solve(), 1966)
