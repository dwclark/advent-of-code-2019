import groovy.transform.ToString
import java.awt.Point
import java.awt.geom.Line2D

@ToString
class Segment {
    int by
    Point start, end

    private Point populateEnd(String dir) {
        if(dir == "R") return new Point(start.@x + by, start.@y)
        else if(dir == "L") return new Point(start.@x - by, start.@y)
        else if(dir == "U") return new Point(start.@x, start.@y + by)
        else if(dir == "D") return new Point(start.@x, start.@y - by)
    }
    
    Segment(Point start, String str) {
        this.by = str.substring(1).toInteger()
        this.start = start
        this.end = populateEnd(str.substring(0, 1));
    }

    boolean isHorizontal() { start.@y == end.@y }
    boolean isVertical() { start.@x == end.@x }
    
    Point intersection(Segment o) {
        if(new Line2D.Double(start, end).intersectsLine(new Line2D.Double(o.start, o.end))) {
            Segment h = horizontal ? this : o
            Segment v = vertical ? this : o
            return new Point(v.start.@x, h.start.@y)
        }
        else return null
    }
}

class Wire {
    Wire(String str) {
        str.split(',').each { val -> add(val) }
    }
    
    List<Segment> segments = []
    Point getPrev() { return segments.empty ? new Point(0,0) : segments[-1].end }
    void add(String str) { segments << new Segment(prev, str) }

    int distance(int upTo, Point p) {
        int ret = 0
        for(int i = 0; i < upTo; ++i) ret += segments[i].by
        Segment last = segments[upTo]
        if(last.horizontal) ret += Math.abs(p.@x - last.start.@x)
        else ret += Math.abs(p.@y - last.start.@y)
        return ret
    }
}

def manhattan = { Point p -> return Math.abs(p.@x) + Math.abs(p.@y) }
List<String> lines = new File("data/03").readLines();
Wire w1 = new Wire(lines[0])
Wire w2 = new Wire(lines[1])
int manhattanLeast = Integer.MAX_VALUE
int signalDistance = Integer.MAX_VALUE

for(int outer = 1; outer < w2.segments.size(); ++outer) {
    Segment sOuter = w2.segments[outer]
    for(int inner = 1; inner < w1.segments.size(); ++inner) {
        Segment sInner = w1.segments[inner]
        Point p = sOuter.intersection(sInner);
        if(p) {
            manhattanLeast = Math.min(manhattan(p), manhattanLeast)
            signalDistance = Math.min(signalDistance, w2.distance(outer, p) + w1.distance(inner, p))
        }
    }
}

println "1: ${manhattanLeast} 2: ${signalDistance}"
