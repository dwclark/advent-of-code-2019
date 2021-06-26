import groovy.transform.Field

@Field def orbits = [:];
@Field def reversed = [:]
@Field def visited = [] as Set

new File("data/06").splitEachLine(/\)/) { list -> orbits[list[1]] = list[0]; reversed[list[0]] = reversed.get(list[0], []) + [list[1]]  }
                                         
int countHops(String key) { key == "COM" ? 0 : 1 + countHops(orbits[key]) }

int countOrbits() { orbits.keySet().sum { countHops(it) } }

int backwardHops(String key, int soFar) {
    if(visited.contains(key)) return 0

    visited.add(key)
    if(key == orbits["SAN"]) return soFar

    if(!reversed.containsKey(key)) return 0

    for(body in reversed[key]) {
        hops = backwardHops(body, soFar+1)
        if(hops != 0) return hops
    }

    return 0
}

int forwardHops(String key, int soFar) {
    if(key == orbits["SAN"]) return soFar

    int hops = backwardHops(key, 0)
    if(hops != 0) return hops + soFar
    else return forwardHops(orbits[key], soFar + 1)
}

println "1: ${countOrbits()} 2: ${forwardHops(orbits['YOU'], 0)}"

                   
