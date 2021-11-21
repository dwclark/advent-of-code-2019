import groovy.transform.Immutable

class Position {
    final int right;
    final int down;
    final String type;

    Position(int right, int down, String type) {
        this.right = right;
        this.down = down;
        this.type = type;
    }

    @Override String toString() { "($right,$down,$type)" }
    @Override boolean equals(Object o) { right == o.right && down == o.down }
    @Override int hashCode() { 31 * right + down }

    Position left() { new Position(right-1, down) }
    Position up() { new Position(right, down-1) }
    
    boolean isDoor() { type =~ /[A-Z]/ }
    boolean isKey() { type =~ /[a-z]/ }
    boolean isHall(){ type == '.' }
    boolean isLocation() { type == '@' }
    boolean isWall() { type == '#' }
    boolean isValid() { door || key || hall || location || wall }
}

class Maze {
    Set<Position> unopened = new HashSet<>()
    Set<Position> unfound = new HashSet<>()
    Graph<Position> graph = new Graph<>()
    Position location;
    
    Maze(File file) {
        Map tmp = [:]
        file.eachLine(0) { line, down ->
            line.eachWithIndex { s, right ->
                Position p = new Position(right, down, s)
                if(p.door) unopened.add(p)
                if(p.key) unfound.add(p)
                if(p.location) location = p
                if(p.key || p.hall || p.location || p.door) {
                    tmp[p] = p
                    graph.addNode(p)
                    if(tmp[p.left()] != null) graph.addEdge(tmp[p.left()], p)
                    if(tmp[p.up()] != null) graph.addEdge(tmp[p.up()], p)
                }
            } }
    }
}

new Maze(new File("data/18"))
