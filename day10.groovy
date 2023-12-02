import groovy.transform.Immutable
import groovy.transform.EqualsAndHashCode
import static Aoc.*

@Immutable
class Point {
    int x, y
}

@EqualsAndHashCode(excludes=['target', 'r'])
class Polarish {
    final Point target
    final double r
    final double phi

    static double computePhi(int x, int y) {
        if(x == 0 && y > 0) return 0d
        if(y == 0 && x > 0) return (double) Math.PI/2
        if(x == 0 && y < 0) return (double) Math.PI
        if(y == 0 && x < 0) return (double) (3/2) * Math.PI

        if(x > 0 && y > 0) return Math.atan((double) (x/y))
        else if(x < 0 && y > 0) return (2 * Math.PI) + Math.atan((double) x/y)
        else if((x < 0 && y < 0) || (x > 0 && y < 0)) return Math.PI + Math.atan((double) (x/y))
        else throw new IllegalArgumentException()
    }

    public Polarish(Point origin, Point target) {
        this.target = target
        //re-center with origin at (0,0)
        int x = target.x - origin.x
        int y = -(target.y - origin.y)
        this.r = Math.sqrt((x*x) + (y*y))
        //not strictly polar, using phi to hopefully signal that
        //phi increases clockwise starting that the positive y-axis
        this.phi = computePhi(x, y)
    }
}

List findMax(List points) {
    int index = -1, soFar = -1
    for(int i = 0; i < points.size(); ++i) {
	def set = (points[0..<i] + points[(i+1)..<points.size()]).collect { p-> new Polarish(points[i], p) } as Set
	if(soFar < set.size()) {
	    soFar = set.size()
	    index = i
	}
    }
    
    return [points[index], soFar]
}

int find200(List points, Point laserAt) {
    points.remove(laserAt)
    List<Polarish> coords = points.collect { p -> new Polarish(laserAt, p) }
    List<Double> rotateBy = (coords.collect { it.phi } as SortedSet) as List
    List collected = []
    int i = 0
    while(collected.size() < 200) {
	Double rot = rotateBy[(i++) % rotateBy.size()] 
	List candidates = coords.findAll { p -> p.phi == rot }
	if(candidates) {
	    Polarish next = candidates.min { it.r }
	    collected.add(next)
	    points.remove(next)
	}
    }

    return 100 * collected[199].target.x + collected[199].target.y
}

points = []
lines("data/10").eachWithIndex { line, y ->
    line.eachWithIndex { c, x ->
        if(c == '#') points << new Point(x, y) } }

def (Point p, int asteroids) = findMax(points)
printAssert("Part 1:", asteroids, 227, "Part 2:", find200(points, p), 604)
