import groovy.transform.Immutable

enum Status {
    WALL(0L), MOVED(1L), OXYGEN(2L);
    private Status(long val) { this.val = val }
    final long val;

    static Status from(long val) {
        if(val == WALL.val) return WALL
        else if(val == MOVED.val) return MOVED
        else if(val == OXYGEN.val) return OXYGEN
        else throw new IllegalArgumentException()
    }
}

enum Direction {
    NORTH(1L), SOUTH(2L), WEST(3L), EAST(4L);
    private Direction(long val) { this.val = val }
    final long val

    public Direction getUndo() {
        if(this == NORTH) return SOUTH
        else if(this == SOUTH) return NORTH
        else if(this == EAST) return WEST
        else return EAST
    }
}

@Immutable class Position {
    int x, y

    @Override String toString() { "($x,$y)" }

    Position move(Direction d) {
        if(d == Direction.NORTH) return new Position(x, y+1)
        else if(d == Direction.SOUTH) return new Position(x, y-1)
        else if(d == Direction.WEST) return new Position(x-1, y)
        else return new Position(x+1, y)
    }

    Direction move(Position newPosition) {
        if(newPosition.y > y) return Direction.NORTH
        else if(newPosition.y < y) return Direction.SOUTH
        else if(newPosition.x > x) return Direction.EAST
        else return Direction.WEST
    }
}

class DiscoveringDfsWalk extends Graph.DfsWalk<Position> {
    Position oxygen
    IoBus ioBus
    Graph graph

    void discover(final Position current, final Direction d) {
        ioBus.readChannel.put(d.val)
        Status response = Status.from(ioBus.writeChannel.take())
        if(response == Status.WALL) return

        if(response == Status.MOVED || response == Status.OXYGEN) {
            Position p = current.move(d)
            graph.addNode(p)
            graph.addEdge(current, p)
            if(response == Status.OXYGEN) oxygen = p
            ioBus.readChannel.put(d.undo.val)
            Status undoResponse = Status.from(ioBus.writeChannel.take())
            assert undoResponse != Status.WALL
        }
    }
    
    @Override
    Graph.DfsStep<Position> startVisit(final Position current, final Position predecessor) {
        //println "starting visit at $current"
        if(predecessor != null) {
            //need to actually move to what graph thinks is current
            Direction d = predecessor.move(current)
            ioBus.readChannel.put(d.val)
            Status response = Status.from(ioBus.writeChannel.take())
            assert response != Status.WALL
        }
        
        discover(current, Direction.NORTH)
        discover(current, Direction.SOUTH)
        discover(current, Direction.EAST)
        discover(current, Direction.WEST)
        return super.startVisit(current, predecessor)
    }

    @Override
    void finish(final Graph.DfsStep<Position> step) {
        if(step.predecessor != null) {
            Direction toMove = step.vertex.move(step.predecessor)
            ioBus.readChannel.put(toMove.val)
            Status response = Status.from(ioBus.writeChannel.take())
            assert response != Status.WALL
        }

        super.finish(step)
    }
}

def discover() {
    def origin = new Position(0,0)
    def ioBus = IoBus.separateChannels()
    def intCode = Intcode.from(new File("data/15"), ioBus)
    Graph<Position> graph = new Graph<>([origin])
    DiscoveringDfsWalk walk = new DiscoveringDfsWalk(graph: graph, ioBus: ioBus)
    
    def robotThread = Thread.start {
        try {
            intCode.call()
        }
        catch(InterruptedException e) {}
    }

    def controlThread = Thread.start {
        graph.depthFirstSearch(walk, origin)
        robotThread.interrupt()
    }

    controlThread.join()
    robotThread.join()

    return [ origin: origin, graph: graph, oxygen: walk.oxygen ]
}

def out = discover()
def sp = out.graph.dijkstra(out.origin)
def o2sp = out.graph.dijkstra(out.oxygen)
def longestShortest = out.graph.nodes.keySet().collect { n -> o2sp.pathTo(n).size() - 1 }.max()
println "1: ${sp.pathTo(out.oxygen).size() - 1}, 2: ${longestShortest}"
