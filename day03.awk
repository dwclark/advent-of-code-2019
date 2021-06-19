#!/usr/bin/awk -f

function abs(v) { return (v < 0) ? -v : v }
function horiz(p1, p2) { return p1["y"] == p2["y"] }
function vert(p1, p2) { return p1["x"] == p2["x"] }
function manhattan(p) { return abs(p["x"]) + abs(p["y"]) }

function to_seg(p1, p2, seg) {
    seg[1]["x"] = p1["x"]
    seg[1]["y"] = p1["y"]
    seg[2]["x"] = p2["x"]
    seg[2]["y"] = p2["y"]
}

function assign_hv(p1, p2, p3, p4, h, v) {
    if(horiz(p1, p2) && vert(p3, p4)) {
        to_seg(p1, p2, h)
        to_seg(p3, p4, v)
        return 1
    }
    else if(horiz(p3, p4) && vert(p1, p2)){
        to_seg(p3, p4, h)
        to_seg(p1, p2, v)
        return 1
    }
    else return 0
}

function intersect(h, v) {
    return (((h[1]["y"] >= v[1]["y"] && h[1]["y"] <= v[2]["y"]) ||
             (h[1]["y"] <= v[1]["y"] && h[1]["y"] >= v[2]["y"])) &&
            
            ((v[1]["x"] >= h[1]["x"] && v[1]["x"] <= h[2]["x"]) ||
             (v[1]["x"] <= h[1]["x"] && v[1]["x"] >= h[2]["x"])))
}

function intersection(p1, p2, p3, p4, at, _h, _v) {
    if(!assign_hv(p1, p2, p3, p4, _h, _v)) return 0

    if(intersect(_h, _v)) {
        at["x"] = _v[1]["x"]
        at["y"] = _h[1]["y"]
        return 1
    }
    else return 0
}

function point_to_string(p, _s) {
    return "(" p["x"] "," p["y"] ")"
}

function wire_to_string(w, _s) {
    for(i in w)
        _s = _s i ": " point_to_string(w[i]) " "
    return _s
}

function steps(w, from, to, _total) {
    for(i = from; i <= to; ++i) {
        _total += w[i]["by"]
    }

    return _total
}

function add_to_wire(w, i, _letter, _by) {
    _letter = substr($(i), 1, 1)
    _by = strtonum(substr($(i), 2))
    w[i]["by"] = _by
    w[i]["dir"] = _letter
    
    if(_letter == "R") { w[i]["x"] = w[i-1]["x"] + _by; w[i]["y"] = w[i-1]["y"] }
    else if(_letter == "L") { w[i]["x"] = w[i-1]["x"] - _by; w[i]["y"] = w[i-1]["y"] }
    else if(_letter == "U") { w[i]["x"] = w[i-1]["x"]; w[i]["y"] = w[i-1]["y"] + _by }
    else if(_letter == "D") { w[i]["x"] = w[i-1]["x"]; w[i]["y"] = w[i-1]["y"] - _by }
    else { print "bad letter " _letter; exit(1) }
}

function dist(p1, p2, dir) {
    if(dir == "R") return p2["x"] - p1["x"];
    else if(dir == "L") return p1["x"] - p2["x"]
    else if(dir == "U") return p2["y"] - p1["y"]
    else return p1["y"] - p2["y"]
}

BEGIN { FS = ","; wire1[0]["x"] = 0; wire1[0]["y"] = 0; wire2[0]["x"] = 0; wire2[0]["y"] = 0 }

NR == 1 { for(i = 1; i <= NF; ++i) add_to_wire(wire1, i) }
NR == 2 { for(i = 1; i <= NF; ++i) add_to_wire(wire2, i) }

END {

    leastM = -1; leastS = -1;
    currM = -1; currS = -1;
    for(i2 = 2; i2 < length(wire2); ++i2) {
        for(i1 = 2; i1 < length(wire1); ++i1) {
            if(intersection(wire1[i1-1], wire1[i1], wire2[i2-1], wire2[i2], at)) {
                currM = manhattan(at);
                currS = steps(wire2, 1, i2-1) + dist(wire2[i2-1], at, wire2[i2]["dir"]) + steps(wire1, 1, i1-1) + dist(wire1[i1-1], at, wire1[i1]["dir"])
                if(leastM < 0 || currM < leastM) leastM = currM
                if(leastS < 0 || currS < leastS) leastS = currS
            }
        }
    }

    print "1: " leastM " 2: " leastS
}
