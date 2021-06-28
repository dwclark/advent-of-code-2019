import groovy.transform.Immutable
import groovy.transform.ToString
import static java.lang.Math.abs

@Immutable
class Position {
    int x, y, z

    static Position from(String line) {
        def re = /<x=(-*[0-9]+), y=(-*[0-9]+), z=(-*[0-9]+)>/
        def m = line =~ re
        return new Position(m[0][1].toInteger(), m[0][2].toInteger(), m[0][3].toInteger())
    }
    
    Position plus(Velocity v) {
        return new Position(x + v.x, y + v.y, z + v.z)
    }

    public int energy() { abs(x) + abs(y) + abs(z) }
}

@Immutable
class Velocity {
    int x, y, z

    int changeBy(int me, int them) {
        if(me < them) return 1
        else if(me > them) return -1
        else return 0
    }

    Velocity add(Position me, List<Position> others) {
        int newX = x
        int newY = y
        int newZ = z

        for(Position pos in others) {
            newX += changeBy(me.x, pos.x)
            newY += changeBy(me.y, pos.y)
            newZ += changeBy(me.z, pos.z)
        }

        return new Velocity(newX, newY, newZ)
    }

    public int energy() { abs(x) + abs(y) + abs(z) }
}

@ToString
class Moon {
    final int id
    final Position position
    final Velocity velocity

    public Moon(int i, Position p, Velocity v) { id = i; position = p; velocity = v; }

    static Moon from(int id, String line) {
        return new Moon(id, Position.from(line), new Velocity(0,0,0))
    }

    Moon plus(List<Moon> moons) {
        Velocity newVelocity = velocity.add(position, moons.findAll { m -> m != this }.collect { it.position })
        return new Moon(id, position + newVelocity, newVelocity)
    }

    int energy() { position.energy() * velocity.energy() }

    @Override boolean equals(Object o) { return id == o.id }
    @Override int hashCode() { return id }
}

int totalEnergy(List<Moon> moons) {
    moons.sum { m -> m.energy() }
}

int idx = 1
List<Moon> initial = new File("data/12").readLines().collect { Moon.from(idx++, it) }.asImmutable()
List<Moon> current = new ArrayList(initial)

1000.times {
    current = current.collect { m -> m + current }
}

println "1: ${totalEnergy(current)}"


