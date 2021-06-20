#!/usr/bin/awk -f

function printArray(ary, _str) {
    _str = ""
    for(i = 0; i < length(ary); ++i)
        _str = _str ary[i] " "
    return _str
}

function swap(ary, a, b, _tmp) {
    _tmp = ary[a]
    ary[a] = ary[b]
    ary[b] = _tmp
}

function permutation(ary, start, end, accum, _i, _j, _len) {
    if(start == end) {
        _len = length(accum)
        for(_j in ary)
            accum[_len][_j] = ary[_j]
        return;
    }
    
    for(_i = start; _i <= end; ++_i) {
        swap(ary, start, _i);
        permutation(ary, start + 1, end, accum);
        swap(ary, start, _i);
    }
}

BEGIN {
    FS= ",";
    phases[0] = 0; phases[1] = 1; phases[2] = 2; phases[3] = 3; phases[4] = 4;
    delete phasePermutations; delete data; delete copy;
    permutation(phases, 0, 4, phasePermutations);
}

function makeCopy() { for(i in data) copy[i] = data[i] }

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

function runProgram(input, _ptr, _ins, _output, _tmp1, _tmp2, _inputPtr) {
    makeCopy()
    _ptr = 0
    _inputPtr = 0
    while(1) {
        decode_ins(copy, _ptr, _ins)
        if(_ins["op"] == 99) break
        else if(_ins["op"] == 1) {
            copy[_ins["p3"]] = _ins["v1"] + _ins["v2"]
            _ptr += 4
        }
        else if(_ins["op"] == 2) {
            copy[_ins["p3"]] = _ins["v1"] * _ins["v2"]
            _ptr += 4
        }
        else if(_ins["op"] == 3) {
            copy[_ins["p1"]] = input[_inputPtr++]
            _ptr += 2
        }
        else if(_ins["op"] == 4) {
            _output = _ins["v1"]
            _ptr += 2
            if(_output != 0 && copy[_ptr] != 99) {
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
                copy[_ins["p3"]] = 1
            else
                copy[_ins["p3"]] = 0
            _ptr += 4
        }
        else if(_ins["op"] == 8) {
            if(_ins["v1"] == _ins["v2"])
                copy[_ins["p3"]] = 1
            else
                copy[_ins["p3"]] = 0
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
    print "1: " maxThrusterOutput()
}
