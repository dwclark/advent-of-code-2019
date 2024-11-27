import static Aoc.*
import groovy.transform.Immutable
import static java.lang.Character.isLetter
import static java.util.Map.entry

@Immutable
class Point {
    int row, col

    Point getLeft() { new Point(row, col-1) }
    Point getRight() { new Point(row, col+1) }
    Point getAbove() { new Point(row-1, col) }
    Point getBelow() { new Point(row+1, col) }

    String toString() { "(${row},${col})" }
}

enum Kind { OUTER, INNER, NONE }

@Immutable
class Label {
    String val
    Kind kind

    boolean same(Label rhs) { return val == rhs }
    static final Label NONE = new Label('none', Kind.NONE)

    Label getOpposite() {
	if(kind == Kind.OUTER)
	    return new Label(val, Kind.INNER)
	else if(kind == Kind.INNER)
	    return new Label(val, Kind.OUTER)
	else
	    return NONE
    }

    String toString() { "(${val},${kind})" }
}

class Maze {
    List<String> lines
    Map<Point,Label> paths
    Map<Label,Point> labels
    
    Point start
    Point finish
    
    Maze(String path) {
	lines = new File(path).readLines()
	paths = populatePaths(populateAll())
	labels = populateLabels(paths)
	start = paths.findResult { p, label -> return label.val == 'AA' ? p : null }
	finish = paths.findResult { p, label -> return label.val == 'ZZ' ? p : null }
    }

    Map<Point,String> populateAll() {
	Map<Point,String> all = [:]
	lines.eachWithIndex { String line, int row ->
	    line.eachWithIndex { String ch, int col ->
		all[new Point(row, col)] = ch } }
	return all
    }

    Label findLabel(Map<Point,String> all, Point first, Point second) {
	char f = all[first] as char
	char s = all[second] as char

	if(isLetter(f) && isLetter(s)) {
	    String val = "${f}${s}"
	    if(first.row < 2 || first.row > (lines.size() - 3) ||
	       first.col < 2 || first.col > (lines[0].size() - 3))
		return new Label(val, Kind.OUTER)
	    else
		return new Label(val, Kind.INNER)
	}
	else
	    return null
    }
    
    Map<Point,Label> populatePaths(Map<Point,String> all) {
	Map<Point,Label> paths = [:]
	all.each { Point p, String s ->
	    if(!isPath(s)) return

	    paths[p] = null
	    
	    def addLabel = { Label found ->
		if(found) paths[p] = found
	    }
	    
	    addLabel(findLabel(all, p.left.left, p.left))
	    addLabel(findLabel(all, p.above.above, p.above))
	    addLabel(findLabel(all, p.right, p.right.right))
	    addLabel(findLabel(all, p.below, p.below.below))

	    if(!paths[p]) paths[p] = Label.NONE
	}

	return paths
    }

    Map<Label,Point> populateLabels(Map<Point,Label> paths) {
	Map<Label,Point> ret = [:]
	paths.each { Point p, Label label ->
	    if(label != Label.NONE)
		ret[label] = p
	}

	return ret
    }
    
    boolean isPath(String c) { return c == '.' }
    String toString() { lines.join('\n') }

    Point warpTo(Point from) {
	paths.findResult { Point to, Label label -> (to != from && label.same(paths[from])) ? to : null }
    }
    
    List nextVisits(Point p, Set visited, int soFar) {
	List visits = []
	['left','right','above','below'].each { f ->
	    Point possible = p[f]
	    if(paths.containsKey(possible) && !visited.contains(possible))
		visits.add(entry(possible, soFar+1))
	}

	if(paths[p]) {
	    Point toWarp = labels[paths[p].opposite]
	    if(toWarp && !visited.contains(toWarp))
		visits.add(entry(toWarp, soFar+1))
	}

	return visits
    }
    
    int part1() {
	Set visited = new HashSet()
	List working = new LinkedList()
	def current
	working.add(entry(start, 0))

	while(working) {
	    current = working.pop()
	    if(current.key == finish)
		break
	    
	    visited.add current.key
	    working.addAll nextVisits(current.key, visited, current.value)
	}

	return current.value
    }
}

def m = new Maze('data/20')
println m.part1()
