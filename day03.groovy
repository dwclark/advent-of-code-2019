import static Aoc.*
import static Math.*
import groovy.transform.Immutable

@Immutable
class Point {
    static Point ORIGIN = new Point(0,0)
    int x, y
    int manhattan(Point rhs) { abs(x - rhs.x) + abs(y - rhs.y) }
}

@Immutable
class Line {
    static Line ZERO = new Line(0,0,0,0)
    int x1, y1, x2, y2
    
    boolean getVertical() { x1 == x2 }
    boolean getHorizontal() { y1 == y2 }
    int getLength() { abs(x2-x1) + abs(y2-y1) }

    boolean intersects(Point p) {
	((horizontal && p.y == y1 && between(x1, p.x, x2)) ||
	 (vertical && p.x == x1 && between(y1, p.y, y2)))
    }

    boolean between(int v1, int test, int v2) {
	(min(v1,v2) <= test) && (test <= max(v1,v2))
    }
    
    Point intersection(Line rhs) {
	Line h = horizontal ? this : (rhs.horizontal ? rhs : null)
	Line v = vertical ? this : (rhs.vertical ? rhs : null)
	if(h && v && between(h.x1, v.x1, h.x2) && between(v.y1, h.y1, v.y2)) new Point(v.x1, h.y1)
	else null
    }
    
    Line plus(String s) {
	int mag = s[1..<s.length()].toInteger()
	String dir = s[0]
	int newX = x2 + (dir == 'R' ? mag : (dir == 'L' ? -mag : 0))
	int newY = y2 + (dir == 'U' ? mag : (dir == 'D' ? -mag : 0))
	new Line(x2, y2, newX, newY)
    }
}

int distance(List<Line> wire, Point p) {
    int soFar = 0
    for(line in wire) {
	if(line.intersects(p))
	    return soFar += new Point(line.x1, line.y1).manhattan(p)
	else
	    soFar += line.length
    }
}

List<Point> intersections(List<Line> wire1, List<Line> wire2) {
    [wire1,wire2].combinations().collect { lst -> lst[0].intersection(lst[1]) }.findAll { p -> p && p != Point.ORIGIN }
}

(wire1, wire2) = lines("data/03") { str -> str.split(',').inject([]) { ret, s -> ret << ((ret ? ret[-1] : Line.ZERO) + s) } }
points = intersections(wire1, wire2)
printAssert("Part 1:", points.collect { Point.ORIGIN.manhattan(it) }.min(), 209,
	    "Part 2:", points.collect { p -> distance(wire1, p) + distance(wire2, p) }.min(), 43258)
