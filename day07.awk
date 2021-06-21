#!/usr/bin/awk -f

@include "./aoc_utils.awk"

BEGIN {
    FS= ",";
    AMP_IDS["a"] = "a"; AMP_IDS["b"] = "b"; AMP_IDS["c"] = "c"; AMP_IDS["d"] = "d"; AMP_IDS["e"] = "e";
    P1_PHASES[0] = 0; P1_PHASES[1] = 1; P1_PHASES[2] = 2; P1_PHASES[3] = 3; P1_PHASES[4] = 4;
    P2_PHASES[0] = 5; P2_PHASES[1] = 6; P2_PHASES[2] = 7; P2_PHASES[3] = 8; P2_PHASES[4] = 9;
    delete P1_PERMUTATIONS;
    delete P2_PERMUTATIONS;
    delete DATA;
    delete amplifiers;
    delete ioBus;
    permutation(P1_PHASES, 0, 4, P1_PERMUTATIONS);
    permutation(P2_PHASES, 0, 4, P2_PERMUTATIONS);
}

function resetIoBus(phases) {
    delete ioBus
    ioBus["e", "out"][0] = phases[0]
    ioBus["e", "out"][1] = 0
    ioBus["e", "read"] = 0
    ioBus["e", "from"] = "d"
    ioBus["a", "out"][0] = phases[1]
    ioBus["a", "read"] = 0
    ioBus["a", "from"] = "e"
    ioBus["b", "out"][0] = phases[2]
    ioBus["b", "read"] = 0
    ioBus["b", "from"] = "a"
    ioBus["c", "out"][0] = phases[3]
    ioBus["c", "read"] = 0
    ioBus["c", "from"] = "b"
    ioBus["d", "out"][0] = phases[4]
    ioBus["d", "read"] = 0
    ioBus["d", "from"] = "c"
}

function output_to_string(id, _str, _i) {
    _str = id ": (" ioBus[id, "read"] ") [ "
    for(_i = 0; _i < length(ioBus[id, "out"]); ++_i)
        _str = _str ioBus[id, "out"][_i] " "
    return _str "]"
}

function io_bus_to_string(_str) {
    return (output_to_string("a") "\n" output_to_string("b") "\n" output_to_string("c") "\n" \
            output_to_string("d") "\n" output_to_string("e"))
}

function toBus(source, datum, _at) {
    _at = length(ioBus[source, "out"]);
    ioBus[source, "out"][_at] = datum;
}

function fromBus(requester, _from,  _at, _ret) {
    _from = ioBus[requester, "from"];
    _at = ioBus[_from, "read"];
    if(_at >= length(ioBus[_from, "out"])) {
        print "trying to read beyond limit from: " _from " requester: " requester;
        exit(0);
    }
    
    _ret = ioBus[_from, "out"][_at];
    ++ioBus[_from, "read"];
    return _ret;
}

function resetAmplifiers(_id, _i) {
    delete amplifiers
    for(_id in AMP_IDS) {
        amplifiers[_id]["id"] = _id
        amplifiers[_id]["ptr"] = 0
        for(_i in DATA) {
            amplifiers[_id][_i] = DATA[_i]
        }
    }
}

function decode_ins(code, ptr, ins, _str, _tmp) {
    delete ins
    _str = "00000" code[ptr]
    split(_str, _tmp, "")
    ins["m1"] = strtonum(substr(_str, length(_str) - 2, 1))
    ins["m2"] = strtonum(substr(_str, length(_str) - 3, 1))
    ins["m3"] = strtonum(substr(_str, length(_str) - 4, 1))
    ins["op"] = strtonum(substr(_str, length(_str) - 1, 2))
    
    if(ins["op"] != 99) {
        ins["p1"] = code[ptr+1]
        ins["v1"] = (ins["m1"] == 1) ? ins["p1"] : code[ins["p1"]]
    }

    if(ins["op"] == 1 || ins["op"] == 2 || (ins["op"] >= 5 && ins["op"] <= 8)) {
        ins["p2"] = code[ptr+2]
        ins["v2"] = (ins["m2"] == 1) ? ins["p2"] : code[ins["p2"]]
        ins["p3"] = code[ptr+3]
    }
}

function runProgram(amp, _ptr, _ins) {
    while(1) {
        decode_ins(amp, amp["ptr"], _ins)
        if(_ins["op"] == 99) {
            return 99
        }
        else if(_ins["op"] == 1) {
            amp[_ins["p3"]] = _ins["v1"] + _ins["v2"]
            amp["ptr"] += 4
        }
        else if(_ins["op"] == 2) {
            amp[_ins["p3"]] = _ins["v1"] * _ins["v2"]
            amp["ptr"] += 4
        }
        else if(_ins["op"] == 3) {
            amp[_ins["p1"]] = fromBus(amp["id"])
            amp["ptr"] += 2
        }
        else if(_ins["op"] == 4) {
            toBus(amp["id"], _ins["v1"])
            amp["ptr"] += 2
            return 4
        }
        else if(_ins["op"] == 5) {
            if(_ins["v1"] != 0)
                amp["ptr"] = _ins["v2"]
            else
                amp["ptr"] += 3
        }
        else if(_ins["op"] == 6) {
            if(_ins["v1"] == 0)
                amp["ptr"] = _ins["v2"]
            else
                amp["ptr"] += 3
        }
        else if(_ins["op"] == 7) {
            if (_ins["v1"] < _ins["v2"])
                amp[_ins["p3"]] = 1
            else
                amp[_ins["p3"]] = 0
            amp["ptr"] += 4
        }
        else if(_ins["op"] == 8) {
            if(_ins["v1"] == _ins["v2"])
                amp[_ins["p3"]] = 1
            else
                amp[_ins["p3"]] = 0
            amp["ptr"] += 4
        }
        else {
            print "bad op: " _ins["op"]
            return
        }
    }
}

function part1_cycle(phase, _input) {
    resetAmplifiers()
    resetIoBus(phase)
    runProgram(amplifiers["a"])
    runProgram(amplifiers["b"])
    runProgram(amplifiers["c"])
    runProgram(amplifiers["d"])
    runProgram(amplifiers["e"])
    return fromBus("a");
}

function part1(_max, _tmp, _i) {
    _max = -1
    for(_i in P1_PERMUTATIONS) {
        _tmp = part1_cycle(P1_PERMUTATIONS[_i])
        _max = (_max < _tmp) ? _tmp : _max
    }
    
    return _max
}

function part2_cycle(phase, _ret) {
    resetAmplifiers()
    resetIoBus(phase)
    _ret = -1
    while(_ret != 99) {
        runProgram(amplifiers["a"])
        runProgram(amplifiers["b"])
        runProgram(amplifiers["c"])
        runProgram(amplifiers["d"])
        _ret = runProgram(amplifiers["e"])
    }
    
    return fromBus("a")
}

function part2(_max, _tmp, _i) {
    _max = -1
    for(_i in P2_PERMUTATIONS) {
        _tmp = part2_cycle(P2_PERMUTATIONS[_i])
        _max = (_max < _tmp) ? _tmp : _max
    }
    
    return _max
}

{ for(i = 1; i <= NF; ++i) DATA[i-1] = $(i) }

END {
    print "1: " part1() " 2: " part2()
}
