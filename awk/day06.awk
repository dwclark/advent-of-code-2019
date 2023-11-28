#!/usr/bin/awk -f

BEGIN { FS = ")"; }

{ orbits[$2] = $1; reversed[$1][$2] = 1; LOOK_FOR=""; }

function count_hops(key) { return (key == "COM") ? 0 : 1 + count_hops(orbits[key]) }

function count_orbits(_num) {
    for(body in orbits) {
        _num += count_hops(body)
    }

    return _num
}

function backward_hops(key, soFar) {
    if(visited[key] == 1)
        return 0
    
    visited[key] = 1
    if(key == LOOK_FOR)
        return soFar

    if(!isarray(reversed[key]))
        return 0

    for(body in reversed[key]) {
        hops = backward_hops(body, soFar+1)
        if(hops != 0)
            return hops
    }

    return 0
}

function forward_hops(key, soFar) {
    if(key == LOOK_FOR)
        return soFar

    hops = backward_hops(key, 0)
    if(hops != 0)
        return hops + soFar
    else
        return forward_hops(orbits[key], soFar+1)
}
    
END { LOOK_FOR = orbits["SAN"]; print "1: " count_orbits() " 2: " forward_hops(orbits["YOU"], 0) }
