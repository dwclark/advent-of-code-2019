import static Aoc.*
import groovy.transform.Immutable

enum Tile {
    EMPTY('.'), WALL('|'), BLOCK('B'), PADDLE('_'), BALL('*')

    private Tile(String d) { display = d }
    
    static Tile from(int val) {
        if(val == 0) return EMPTY
        else if(val == 1) return WALL
        else if(val == 2) return BLOCK
        else if(val == 3) return PADDLE
        else if(val == 4) return BALL
        else throw new IllegalArgumentException()
    }
    
    final String display
}

@Immutable
class Position {
    int left, top
}

void drawGrid(Map grid) {
    def lastPos = new Position(-1,-1)
    grid.each { k, v ->
        if(k.top != lastPos.top) println()
        print v.display
        lastPos = k
    }

    println()
}

int part1() {
    def grid = [:]
    def ioBus = IoBus.separateChannels()
    def intCode = Intcode.from(new File("data/13"), ioBus)

    def controlThread = Thread.start {
        try {
            while(true) {
                int xpos = ioBus.writeChannel.take()
                if(xpos == -1)
                    break
                
                int ypos = ioBus.writeChannel.take()
                Tile tile = Tile.from((int) ioBus.writeChannel.take())
                grid[new Position(xpos, ypos)] = tile
            }
        }
        catch(InterruptedException ie) {}
    }
    
    def robotThread = Thread.start {
        intCode.call()
        controlThread.interrupt()
    }

    robotThread.join()
    controlThread.join()
    return grid.values().count { t -> t == Tile.BLOCK }
}

int part2(boolean show = false) {
    def grid = [:]
    def ioBus = IoBus.separateChannels()
    def intCode = Intcode.from(new File("data/13"), ioBus).store(0, 2L)
    def scanner = new Scanner(System.in)
    def paddleAt = new Position(0,0), ballAt = new Position(0,0)
    int score;

    def updateState = { initializing ->
        while(true) {
            int ins1 = ioBus.writeChannel.take()
            int ins2 = ioBus.writeChannel.take()
            int ins3 = ioBus.writeChannel.take()
            if(ins1 == -1 && ins2 == 0) {
                score = ins3
                if(initializing)
                    break
            }
            else {
                Tile tile = Tile.from(ins3)
                Position pos = new Position(ins1, ins2)
                if(tile == Tile.BALL) ballAt = pos
                if(tile == Tile.PADDLE) paddleAt = pos
                grid[pos] = tile

                if(!initializing && tile == Tile.BALL)
                    break
            }
        }

        if(show) {
            println "score: $score"
            drawGrid(grid)
        }
    }

    def takeAction = {
        if(paddleAt.left < ballAt.left) ioBus.readChannel.put(1L)
        else if(paddleAt.left > ballAt.left) ioBus.readChannel.put(-1L)
        else ioBus.readChannel.put(0L)
    }

    def controlThread = Thread.start {
        try {
            updateState(true)
            while(true) {
                takeAction()
                updateState()
            }
        }
        catch(InterruptedException ie) { }
    }

    def robotThread = Thread.start {
        intCode.call()
        controlThread.interrupt()
    }
        
    robotThread.join()
    controlThread.join()
    return score
}

printAssert("Part 1:", part1(), 326, "Part 2:", part2(), 15988)

