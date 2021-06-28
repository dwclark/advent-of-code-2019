import groovy.transform.ToString
import groovy.transform.CompileStatic
import static java.lang.Math.abs

Position parsePosition(String line) {
    def re = /<x=(-*[0-9]+), y=(-*[0-9]+), z=(-*[0-9]+)>/
    def m = line =~ re
    return new Position(m[0][1].toInteger(), m[0][2].toInteger(), m[0][3].toInteger())
}

@CompileStatic
class Position {
    int x, y, z

    Position(int x, int y, int z) { this.x = x; this.y = y; this.z = z }
    
    void add(Velocity v) {
        x += v.x
        y += v.y
        z += v.z
    }
    
    int energy() { return abs(x) + abs(y) + abs(z) }

    Position copy() { return new Position(x, y, z) }
}

@CompileStatic
class Velocity {
    int x, y, z

    Velocity(int x, int y, int z) { this.x = x; this.y = y; this.z = z }

    int changeBy(int me, int them) {
        if(me < them) return 1
        else if(me > them) return -1
        else return 0
    }
    
    void add(Position me, Position other) {
        x += changeBy(me.x, other.x)
        y += changeBy(me.y, other.y)
        z += changeBy(me.z, other.z)
    }

    int energy() { abs(x) + abs(y) + abs(z) }

    Velocity copy() { new Velocity(x, y, z) }
}

@ToString @CompileStatic
class Moon {
    int id
    Position position
    Velocity velocity
    
    public Moon(int i, Position p, Velocity v) { id = i; position = p; velocity = v; }

    public void nextVelocities(List<Moon> moons) {
        for(int i = 0; i < moons.size(); ++i) {
            Moon m = moons.get(i);
            if(m.id != id) {
                velocity.add(position, m.position)
            }
        }
    }

    public void nextPosition() {
        position.add(velocity)
    }

    public Moon copy() {
        return new Moon(id, position.copy(), velocity.copy())
    }
    
    int energy() { position.energy() * velocity.energy() }
}

@CompileStatic
class DimensionStates {
    int[] xstates = new int[8]
    int[] ystates = new int[8]
    int[] zstates = new int[8]
    int[] test = new int[8]
    List<Moon> moons
    BigInteger xperiod = 0G, yperiod = 0G, zperiod = 0G

    boolean getPeriodSolved() { return xperiod != 0 && yperiod != 0 && zperiod != 0 }
    
    int[] fillX(int[] ary) {
        for(int i = 0; i < moons.size(); ++i) {
            ary[2 * i] = moons[i].position.x
            ary[2*i + 1] = moons[i].velocity.x
        }

        return ary
    }

    int[] fillY(int[] ary) {
        for(int i = 0; i < moons.size(); ++i) {
            ary[2 * i] = moons[i].position.y
            ary[2*i + 1] = moons[i].velocity.y
        }

        return ary
    }

    int[] fillZ(int[] ary) {
        for(int i = 0; i < moons.size(); ++i) {
            ary[2 * i] = moons[i].position.z
            ary[2*i + 1] = moons[i].velocity.z
        }

        return ary
    }
    
    DimensionStates(List<Moon> moons) {
        this.moons = moons
        fillX(xstates)
        fillY(ystates)
        fillZ(zstates)
    }

    BigInteger lcm() {
        BigInteger s1 = ((xperiod * yperiod) / xperiod.gcd(yperiod)).toBigInteger()
        return ((s1 * zperiod) / s1.gcd(zperiod)).toBigInteger()
    }
    
    BigInteger repeatsEvery() {
        long steps = 0
        while(!periodSolved) {
            for(Moon m in moons) m.nextVelocities(moons)
            for(Moon m in moons) m.nextPosition()
            ++steps
            
            if(xperiod == 0G && Arrays.equals(xstates, fillX(test)))
                xperiod = steps

            if(yperiod == 0G && Arrays.equals(ystates, fillY(test)))
                yperiod = steps

            if(zperiod == 0G && Arrays.equals(zstates, fillZ(test)))
                zperiod = steps
        }

        return lcm()
    }
}

int totalEnergy(List<Moon> moons) {
    moons.sum { m -> m.energy() }
}

int idx = 1
List<Moon> initial = new File("data/12").readLines().collect { new Moon(idx++, parsePosition(it), new Velocity(0,0,0)) }.asImmutable()
List<Moon> current = initial.collect { m -> m.copy() }

1000.times {
    for(Moon m in current)
        m.nextVelocities(current)

    for(Moon m in current)
        m.nextPosition()
}

def states = new DimensionStates(initial.collect { m -> m.copy() })
println "1: ${totalEnergy(current)}, 2: ${states.repeatsEvery()}"
