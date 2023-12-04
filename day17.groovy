import static Aoc.*
import groovy.transform.Immutable

enum Facing {
    N, S, E, W;

    public List turnLeft() {
        if(this == N) return [ W, Position.&west ]
        else if(this == W) return [ S, Position.&south ]
        else if(this == S) return [ E, Position.&east ]
        else return [ N, Position.&north ]
    }

    public List turnRight() {
        if(this == N) return [ E, Position.&east ]
        else if(this == E) return [ S, Position.&south ]
        else if(this == S) return [ W, Position.&west ]
        else return [ N, Position.&north ]
    }
}

@Immutable
class Position {
    int right, down
    static Position east(Position p) { return new Position(p.right+1, p.down) }
    static Position west(Position p) { return new Position(p.right-1, p.down) }
    static Position north(Position p) { return new Position(p.right, p.down-1) }
    static Position south(Position p) { return new Position(p.right, p.down+1) }
}

enum Element {
    SCAFFOLD('#'), EMPTY('.'), UP('^');
    private Element(String s) { val = s as char }
    private char val
    private static final Map map = Element.values().inject([:]) { m, e -> m << new MapEntry(e.val, e) }
    static Element from(char c) { map[c] }
}

def ioBus = IoBus.separateChannels()
def intCode = Intcode.from(new File("data/17"), ioBus).call()
def chars = ioBus.writeChannel.collect { (char) it }
int down = 0, right = 0
def grid = chars.inject([:]) { m, c ->
    if(c == '\n') {
        down++; right = 0; m;
    }
    else {
        m << new MapEntry(new Position(right++, down), Element.from(c))
    }
}

def sumAlignment = grid.keySet().sum { p ->
    def e = grid[p]
    if(e == Element.SCAFFOLD && grid[Position.west(p)] == Element.SCAFFOLD &&
       grid[Position.east(p)] == Element.SCAFFOLD && grid[Position.north(p)] == Element.SCAFFOLD &&
       grid[Position.south(p)] == Element.SCAFFOLD) return p.right * p.down
    else return 0
}

List<String> singleStream(Map<Position, Element> grid) {
    List<Character> ret = []
    Position position = grid.find { k,v -> v != Element.SCAFFOLD && v != Element.EMPTY }.key
    Facing facing = Facing.N;
    def method = null
    
    while(true) {
        def letter;
        if(grid[facing.turnLeft()[1].call(position)] == Element.SCAFFOLD) {
            (facing, method) = facing.turnLeft()
            letter = 'L'
        }
        else if(grid[facing.turnRight()[1].call(position)] == Element.SCAFFOLD) {
            (facing, method) = facing.turnRight()
            letter = 'R'
        }
        else {
            return ret
        }
        
        Integer steps = 0
        while(grid[method(position)] == Element.SCAFFOLD) {
            position = method(position)
            ++steps
        }
        
        ret << "$letter$steps"
    }
}

List stm = singleStream(grid)
//println "All paths: " + stm.join(':')
//println "All pairs: " + (stm as Set)

//There's probably some way to do this programmatially using DFS search or longest substrings, but
//it seemed easier to solve it by hand so I did, see grid.txt for solution
def mainFunction = "A,B,A,C,C,A,B,C,B,B\n"  
def A = "L,8,R,10,L,8,R,8\n"
def B = "L,12,R,8,R,8\n"
def C = "L,8,R,6,R,6,R,10,L,8\n"
def feed = "n\n"
def program = mainFunction + A + B + C + feed
def longAscii = program.toCharArray().collect { it as Long }

def ioBus2 = IoBus.separateChannels()
longAscii.each { ioBus2.readChannel.put(it) }
def intCode2 = Intcode.from(new File("data/17"), ioBus2)
intCode2.store(0L, 2L)
intCode2.call()

printAssert("Part 1:", sumAlignment, 6680, "Part 2:", ioBus2.lastWrite(), 1103905)

