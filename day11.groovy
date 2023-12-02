import static Aoc.*
import groovy.transform.Immutable

enum Color {
    BLACK(0), WHITE(1);
    Color(int val) { this.val = val }
    static Color from(int input) { return input == 0 ? BLACK : WHITE }
    final int val;
}

enum Direction {
    N, W, S, E

    Direction turn(int input) {
        if(this == N) return input == 0 ? W : E;
        else if(this == W) return input == 0 ? S : N;
        else if(this == S) return input == 0 ? E : W;
        else return input == 0 ? N : S;
    }
}

@Immutable
class Point {
    int x, y
    
    Point move(Direction direction) {
        if(direction == Direction.N) return new Point(x, y+1)
        else if(direction == Direction.W) return new Point(x-1, y)
        else if(direction == Direction.S) return new Point(x, y-1)
        else return new Point(x+1, y)
    }
}

Color panelColor(Map<Point,Color> grid, Point location) {
    return grid.containsKey(location) ? grid[location] : Color.BLACK
}

Map<Point,Color> colorHull(Color initial) {
    def ioBus = IoBus.separateChannels()
    def intCode = Intcode.from(new File("data/11"), ioBus)
    Direction direction = Direction.N
    Point location = new Point(0,0)
    Map<Point,Color> grid = [:]
    
    def robotThread = Thread.start {
        intCode.call()
        ioBus.writeChannel.put(-1)
    }
    
    def controlThread = Thread.start {
        ioBus.readChannel.put(initial.val)
        while(true) {
            int paintIt = ioBus.writeChannel.take()
            if(paintIt == -1)
                break
            
            int newDirection = ioBus.writeChannel.take()
            grid[location] = Color.from(paintIt)
            direction = direction.turn(newDirection)
            location = location.move(direction)
            ioBus.readChannel.put(panelColor(grid, location).val)
        }
    }
    
    robotThread.join()
    controlThread.join()

    return grid
}

printAssert("Part 1:", colorHull(Color.BLACK).size(), 1909)

Map<Point,Color> p2Hull = colorHull(Color.WHITE)
int left = p2Hull.keySet().min { p1, p2 -> p1.x <=> p2.x }.x
int right = p2Hull.keySet().max { p1, p2 -> p1.x <=> p2.x }.x
int top = p2Hull.keySet().max { p1, p2 -> p1.y <=> p2.y }.y
int bottom = p2Hull.keySet().min { p1, p2 -> p1.y <=> p2.y }.y

for(int v = top; v >= bottom; --v) {
    for(int h = left; h <= right; ++h) {
        Color c = panelColor(p2Hull, new Point(h, v))
        if(c == Color.BLACK) print "."
        else print "#"
    }

    println()
}
