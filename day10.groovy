@Grab('com.google.guava:guava:30.1.1-jre')
import static com.google.common.math.IntMath.*
import groovy.transform.Immutable
import groovy.transform.Field

class Point {
    final int x;
    final int y;

    Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override public boolean equals(Object rhs) { x == rhs.x && y == rhs.y }
    @Override public int hashCode() { 31 * x + y }
    @Override public String toString() { "($x,$y)" }
}

enum Quadrant { I, II, III, IV }

class Polarish {
    final Point target;
    final double r;
    final double phi;

    static Quadrant computeQuadrant(int x, int y) {
        if(x > 0 && y > 0) return Quadrant.I
        else if(x < 0 && y > 0) return Quadrant.II
        else if(x < 0 && y < 0) return Quadrant.III
        else if(x > 0 && y < 0) return Quadrant.IV
        else throw new IllegalArgumentException("$x,$y are not in a quadrant")
    }
    
    static double computePhi(int x, int y) {
        if(x == 0 && y > 0) return 0d
        if(y == 0 && x > 0) return (double) Math.PI/2
        if(x == 0 && y < 0) return (double) Math.PI
        if(y == 0 && x < 0) return (double) (3/2) * Math.PI

        Quadrant quad = computeQuadrant(x, y)
        if(quad == Quadrant.I) return Math.atan((double) (x/y))
        else if(quad == Quadrant.II) return (2 * Math.PI) + Math.atan((double) x/y)
        else if(quad == Quadrant.III || quad == Quadrant.IV) return Math.PI + Math.atan((double) (x/y))
        else throw new IllegalArgumentException();
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

    @Override public boolean equals(Object rhs) { phi == rhs.phi }
    @Override public int hashCode() { Double.hashCode(phi) }
    @Override public String toString() { target.toString() }
}

@Field def lines = new File("data/10").readLines()
@Field def points = []
lines.eachWithIndex { line, y ->
    line.eachWithIndex { c, x ->
        if(c == '#') points << new Point(x, y) } }

List findMax() {
    Point p
    int soFar = -1
    
    points.each { outer ->
        def set = new HashSet()
        points.each { inner ->
            if(inner != outer) {
                set.add(new Polarish(outer, inner))
            } }
        
        if(set.size() > soFar) {
            soFar = set.size()
            p = outer
        } }
    
    return [p, soFar]
}

int find200(Point laserAt) {
    def map = points.inject(new TreeMap(Double.&compare)) { m, p ->
        if(p != laserAt) {
            Polarish s = new Polarish(laserAt, p)
            if(m[s.phi] == null || s.r < m[s.phi].r)
                m[s.phi] = s
        }

        return m
    }

    int i = 0
    for(entry in map) {
        ++i
        if(i == 200) return 100 * entry.value.target.x + entry.value.target.y
    }
}

def (Point p, int asteroids) = findMax()
println "1: ${asteroids}, 2: ${find200(p)}"
