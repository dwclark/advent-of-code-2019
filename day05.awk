#!/usr/bin/awk -f

# NOTE: I believe the last exammple in part 2 (Here's a larger example) is incorrect.
# It correctly outputs the right numbers for the given inputs. However, the output
# is not following by a 99 instruction and so the error detection code signals an
# error and then returns. At that point the correct value is in the output.
# Fortunately, part 2 does return the correct code and exits correctly.

BEGIN { FS = ","; delete data; delete copy; }

function makeCopy() { for(i in data) copy[i] = data[i] }

function program_to_string(p, _str) {
    _str = ""
    for(i in p)
        _str = _str p[i] ","
    return _str
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

function memory_cells(code, start, end, _str) {
    _str = ""
    for(i = start; i <= end; ++i) {
        _str = _str i ": " code[i] " "
    }

    return _str
}

function ins_to_string(ins) {
    return "[ m1: " ins["m1"] ", m2: " ins["m2"] ", m3: " ins["m3"] " op: " ins["op"] \
        " p1: " ins["p1"] " p2: " ins["p2"] " p3: " ins["p3"] " v1: " ins["v1"] " v2: " ins["v2"] "]" 
}

function runProgram(input, _ptr, _ins, _output, _tmp1, _tmp2) {
    makeCopy()
    _ptr = 0
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
            copy[_ins["p1"]] = input
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

{ for(i = 1; i <= NF; ++i) data[i-1] = $(i) }

END {
    print "1: " runProgram(1) " 2: " runProgram(5)
}
