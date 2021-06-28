import groovy.transform.ToString
import groovy.transform.CompileStatic
import static java.lang.Math.abs

int[] parsePosition(String line) {
    def re = /<x=(-*[0-9]+), y=(-*[0-9]+), z=(-*[0-9]+)>/
    def m = line =~ re
    return [ m[0][1].toInteger(), m[0][2].toInteger(), m[0][3].toInteger() ] as int[]
}

@ToString @CompileStatic
class Moon {
    static int X = 0, Y = 1, Z = 2
    int id
    int[] position
    int[] velocity
    
    public Moon(int i, int[] p, int[] v) { id = i; position = p; velocity = v; }
    
    int changeBy(int me, int them) {
        if(me < them) return 1
        else if(me > them) return -1
        else return 0
    }
    
    void add(int[] other) {
        velocity[X] += changeBy(position[X], other[X])
        velocity[Y] += changeBy(position[Y], other[Y])
        velocity[Z] += changeBy(position[Z], other[Z])
    }

    public void nextVelocities(List<Moon> moons) {
        for(int i = 0; i < moons.size(); ++i) {
            Moon m = moons.get(i);
            if(m.id != id) {
                add(m.position)
            }
        }
    }

    public void nextPosition() {
        for(int i = X; i <= Z; ++i) position[i] += velocity[i]
    }

    public Moon copy() {
        return new Moon(id, Arrays.copyOf(position, position.length), Arrays.copyOf(velocity, velocity.length))
    }

    int energy(int[] ary) { return abs(ary[X]) + abs(ary[Y]) + abs(ary[Z]) }
    
    int energy() { return energy(position) * energy(velocity) }

    static void runStep(List<Moon> moons) {
        for(Moon m in moons) m.nextVelocities(moons)
        for(Moon m in moons) m.nextPosition()
    }

    static int totalEnergy(List<Moon> moons) { (int) moons.sum { m -> m.energy() } }
}

@CompileStatic
class DimensionStates {
    static int X = 0, Y = 1, Z = 2
    int[] xstates = new int[8]
    int[] ystates = new int[8]
    int[] zstates = new int[8]
    int[] test = new int[8]
    List<Moon> moons
    BigInteger xperiod = 0G, yperiod = 0G, zperiod = 0G

    boolean getPeriodSolved() { return xperiod != 0 && yperiod != 0 && zperiod != 0 }
    
    int[] fill(int dimension, int[] ary) {
        for(int i = 0; i < moons.size(); ++i) {
            ary[2 * i] = moons[i].position[dimension]
            ary[2*i + 1] = moons[i].velocity[dimension]
        }

        return ary
    }
    
    DimensionStates(List<Moon> moons) {
        this.moons = moons
        fill(X, xstates)
        fill(Y, ystates)
        fill(Z, zstates)
    }

    BigInteger lcm() {
        BigInteger s1 = ((xperiod * yperiod) / xperiod.gcd(yperiod)).toBigInteger()
        return ((s1 * zperiod) / s1.gcd(zperiod)).toBigInteger()
    }
    
    BigInteger repeatsEvery() {
        long steps = 0
        while(!periodSolved) {
            Moon.runStep(moons)
            ++steps
            
            if(xperiod == 0G && Arrays.equals(xstates, fill(X, test)))
                xperiod = steps

            if(yperiod == 0G && Arrays.equals(ystates, fill(Y, test)))
                yperiod = steps

            if(zperiod == 0G && Arrays.equals(zstates, fill(Z, test)))
                zperiod = steps
        }

        return lcm()
    }
}



int idx = 1
List<Moon> initial = new File("data/12").readLines().collect { new Moon(idx++, parsePosition(it), [0,0,0] as int[]) }.asImmutable()
List<Moon> current = initial.collect { m -> m.copy() }

1000.times { Moon.runStep(current) }
def states = new DimensionStates(initial.collect { m -> m.copy() })
println "1: ${Moon.totalEnergy(current)}, 2: ${states.repeatsEvery()}"
