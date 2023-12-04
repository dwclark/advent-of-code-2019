import static Aoc.*
import groovy.transform.Immutable
import groovy.transform.ToString

enum Content {
    WALL(0L), FREE(1L), OXYGEN(2L);
    private Content(long val) { this.val = val }
    final long val;
    private static final Map<Long,Content> _from = [ (0L): WALL, (1L): FREE, (2L): OXYGEN ]
    static Content from(long val) { _from[val] }
}

enum Direction {
    NORTH(1L), SOUTH(2L), WEST(3L), EAST(4L);
    private Direction(long val) { this.val = val }
    final long val
    private static final Map<Direction,Direction> _back = [ (NORTH): SOUTH, (SOUTH): NORTH, (EAST): WEST, (WEST): EAST ]
    Direction getBack() { _back[this] }
    static final List<Direction> all = [ NORTH, SOUTH, EAST, WEST ].asImmutable()
}

@Immutable class Position {
    int x, y

    Position plus(Direction d) {
	switch(d) {
	    case Direction.NORTH: return new Position(x, y+1)
	    case Direction.SOUTH: return new Position(x, y-1)
	    case Direction.WEST: return new Position(x-1, y)
	    default: return new Position(x+1, y)
	}
    }
}

@ToString
class Tries {
    final Position position
    final Direction direction
    Content content
    final Set nextDirections

    Tries(Position position) {
	this.position = position
	this.direction = null
	this.content = Content.FREE
	this.nextDirections = new HashSet(Direction.all)
    }
    
    Tries(Position from, Direction direction) {
	this.position = from + direction
	this.direction = direction
	this.nextDirections = new HashSet(Direction.all)
	this.nextDirections.remove(direction.back)
    }

    Direction getBack() { direction ? direction.back : null }
    
    Tries next(Map<Position, Tries> graph) {
	def iter = nextDirections.iterator()
	while(iter.hasNext()) {
	    Direction dir = iter.next()
	    Tries tries = new Tries(position, dir)
	    iter.remove()
	    if(!graph.containsKey(tries.position)) {
		return tries
	    }
	}

	return null
    }
}

Map<Position,Content> robotDfs(IoBus bus) {
    Map<Position,Tries> tmp = [:]
    Position current = new Position(0, 0)
    tmp[current] = new Tries(current)

    while(true) {
	Tries currentTries = tmp[current]
	Tries nextTries = currentTries.next(tmp)
	if(nextTries == null && !currentTries.back) {
	    break;
	}
	else if(nextTries == null && currentTries.back) {
	    bus.readChannel.put(currentTries.back.val)
	    final Content back = Content.from(bus.writeChannel.take())
	    assert (back == Content.FREE || back == Content.OXYGEN)
	    current = current + currentTries.back
	}
	else {
	    bus.readChannel.put(nextTries.direction.val)
	    Content c = Content.from(bus.writeChannel.take())
	    if(c != Content.WALL) {
		nextTries.content = c
		tmp[nextTries.position] = nextTries
		current = nextTries.position
	    }
	}
    }

    tmp.inject([:]) { map, pos, tries -> map << new MapEntry(pos, tries.content) }
}

Map discover() {
    def ioBus = IoBus.separateChannels()
    def intCode = Intcode.from(new File("data/15"), ioBus)
    def graph = [:]
    
    def robotThread = Thread.start {
        try {
            intCode.call()
        }
        catch(InterruptedException e) {}
    }

    def controlThread = Thread.start {
	graph.putAll(robotDfs(ioBus))
        robotThread.interrupt()
    }

    controlThread.join()
    robotThread.join()

    return graph
}

List dijkstra(Map<Position,Content> graph) {
    BestCost bc = new BestCost(Heap.min())
    bc.add(new Position(0,0), 0)
    Position pos = null
    while((pos = bc.next()) != null) {
	int cost = bc.cost(pos)
	if(graph[pos] == Content.OXYGEN) {
	    return [pos, cost]
	}

	Direction.all.each { dir ->
	    if(graph.containsKey(pos + dir)) {
		bc.add(pos + dir, cost + 1)
	    }
	}
    }
}

int spread(Map<Position,Content> graph, Position start) {
    final Set<Position> oxygens = new HashSet<>()
    oxygens.add(start)
    int steps = 0
    while(oxygens.size() != graph.size()) {
	Set<Position> toAdd = new HashSet<>()
	for(Position oxygen : oxygens) {
	    Direction.all.each { dir ->
		Position toTry = oxygen + dir
		if(graph.containsKey(toTry) && !oxygens.contains(toTry)) {
		    toAdd.add(toTry)
		}
	    }
	}

	oxygens.addAll(toAdd)
	++steps
    }

    return steps
}

def graph = discover()
def (oxygen, cost) = dijkstra(graph)
printAssert("Part 1:", cost, 412, "Part 2:", spread(graph, oxygen), 418)

