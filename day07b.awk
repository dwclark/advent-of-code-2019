#!/usr/bin/awk -f

@include "./aoc_utils.awk"

BEGIN {
    FS= ",";
    TMP_PHASES[0] = 5; TMP_PHASES[1] = 6; TMP_PHASES[2] = 7; TMP_PHASES[3] = 8; TMP_PHASES[4] = 9;
    delete PERMUTATIONS; delete DATA; delete amplifiers;
    permutation(TMP_PHASES, 0, 4, PERMUTATIONS);
}

function resetAmplifiers(phases, _ampIndex, _amp, _amps, _dataIndex) {
    split("abcde", _amps, "")
    for(_ampIndex = 1; _ampIndex <= length(_amps); ++_ampIndex) {
        _amp = _amps[_ampIndex]
        amplifiers[_amp]["output"][0] = phases[_ampIndex];
        amplifiers[_amp]["ouput_addr"] = 0
        amplifiers[_amp]["prev"] = (_amp == "a") ? "e" : _amps[_ampIndex]
        for(_dataIndex in DATA) {
            amplifiers[_amp][_dataIndex] = DATA[_dataIndex]
        }
    }

    amplifiers["e"]["output"][1] = 0
    amplifiers["e"]["output_addr"]++
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

function runProgram(amp, _ptr, _ins, _output, _tmp1, _tmp2) {
    _ptr = 0
    while(1) {
        decode_ins(amp, _ptr, _ins)
        if(_ins["op"] == 99) break
        else if(_ins["op"] == 1) {
            amp[_ins["p3"]] = _ins["v1"] + _ins["v2"]
            _ptr += 4
        }
        else if(_ins["op"] == 2) {
            amp[_ins["p3"]] = _ins["v1"] * _ins["v2"]
            _ptr += 4
        }
        else if(_ins["op"] == 3) {
            amp[_ins["p1"]] = #TODO put amp input here
            _ptr += 2
        }
        else if(_ins["op"] == 4) {
            _output = _ins["v1"]
            _ptr += 2
            if(_output != 0 && amp[_ptr] != 99) {
                print "error"
                return _output
            }
        }
        else if(_ins["op"] == 5) {
            if(_ins["v1"] != 0)
                _ptr = _ins["v2"]
            else
                _ptr += 3
        }
        else if(_ins["op"] == 6) {
            if(_ins["v1"] == 0)
                _ptr = _ins["v2"]
            else
                _ptr += 3
        }
        else if(_ins["op"] == 7) {
            if (_ins["v1"] < _ins["v2"])
                amp[_ins["p3"]] = 1
            else
                amp[_ins["p3"]] = 0
            _ptr += 4
        }
        else if(_ins["op"] == 8) {
            if(_ins["v1"] == _ins["v2"])
                amp[_ins["p3"]] = 1
            else
                amp[_ins["p3"]] = 0
            _ptr += 4
        }
        else {
            print "bad op: " _ins["op"]
            return _output
        }
    }

    return _output
}

function runPhase(phase, _input) {
    _input[0] = phase[0] #phase
    _input[1] = 0
    _input[1] = runProgram(_input)
    _input[0] = phase[1] #phase
    _input[1] = runProgram(_input)
    _input[0] = phase[2] #phase
    _input[1] = runProgram(_input)
    _input[0] = phase[3] #phase
    _input[1] = runProgram(_input)
    _input[0] = phase[4] #phase
    return runProgram(_input)
}

function maxThrusterOutput(_max, _tmp) {
    _max = -1
    for(phase in phasePermutations) {
        _tmp = runPhase(phasePermutations[phase])
        _max = (_max < _tmp) ? _tmp : _max
    }

    return _max
}

{ for(i = 1; i <= NF; ++i) data[i-1] = $(i) }

END {
    resetAmplifiers(PERMUTATIONS[0])
}
