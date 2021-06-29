import groovy.transform.Immutable

enum Tile {
    EMPTY, WALL, BLOCK, PADDLE, BALL

    static Tile from(int val) {
        if(val == 0) return EMPTY
        else if(val == 1) return WALL
        else if(val == 2) return BLOCK
        else if(val == 3) return PADDLE
        else if(val == 4) return BALL
        else throw new IllegalArgumentException()
    }
    
    final int val
}

@Immutable
class Position {
    int left, top
}

def grid = [:]
def ioBus = IoBus.separateChannels()
def intCode = Intcode.from(new File("data/13"), ioBus)

def robotThread = Thread.start {
    intCode.call()
    ioBus.writeChannel.put(-1)
}

def controlThread = Thread.start {
    while(true) {
        int xpos = ioBus.writeChannel.take()
        if(xpos == -1)
            break
        
        int ypos = ioBus.writeChannel.take()
        Tile tile = Tile.from((int) ioBus.writeChannel.take())
        grid[new Position(xpos, ypos)] = tile
    }
}

robotThread.join()
controlThread.join()
println "1: ${grid.values().count { t -> t == Tile.BLOCK }}"
