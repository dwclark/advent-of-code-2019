#!/usr/bin/env -S awk -f

# NOTE, does not work, stopping now

@include "./aoc_utils.awk"

BEGIN {
    FS= ", |,"; DEBUG = 1; MAX_EXECS = 100000000; DEBUG_PROC = 1

    #"declare" arrays used for data storage
    delete processors; delete ioBus; delete memoryAccesses;
}

function reset_io_bus(id, initial) {
    delete ioBus
    ioBus[id, "out"][0] = initial
    ioBus[id, "read"] = 0
    ioBus[id, "from"] = id
}

function dump_used_memory(m, _str, _ary, _i) {
    _str = "memory: ["
    asorti(memoryAccesses, _ary, "@ind_num_asc")
    for(_i in _ary) {
        _str =  _str " " _ary[_i] ": " m[_ary[_i]] ","
    }
    
    return _str "]"
}

function output_to_string(id, _str, _i) {
    _str = id ": (" ioBus[id, "read"] ") [ "
    for(_i = 0; _i < length(ioBus[id, "out"]); ++_i)
        _str = _str ioBus[id, "out"][_i] " "
    return _str "]"
}

function ins_to_string(ins) {
    return "[ m1: " ins["m1"] ", m2: " ins["m2"] ", m3: " ins["m3"] " op: " ins["op"] \
        " p1: " ins["p1"] " p2: " ins["p2"] " p3: " ins["p3"] "]" 
}

function from_memory(memory, addr) {
    memoryAccesses[addr] = 1
    return memory[addr]
}

function to_memory(memory, addr, val) {
    memoryAccesses[addr] = 1
    memory[addr] = val
}

function to_bus(source, datum, _at) {
    _at = length(ioBus[source, "out"]);
    ioBus[source, "out"][_at] = datum;
}

function from_bus(requester, _from,  _at, _ret) {
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

function reset_processors(id, _i) {
    delete processors
    processors[id]["id"] = id
    processors[id]["ptr"] = 0
    processors[id]["base"] = 0
    for(_i in DATA) {
        processors[id]["memory"][_i] = DATA[_i]
    }
}

function decode_ins(proc, ins, _str, _tmp, _ptr) {
    delete ins
    _ptr = proc["ptr"]
    _str = "00000" from_memory(proc["memory"], _ptr)
    split(_str, _tmp, "")
    ins["m1"] = strtonum(substr(_str, length(_str) - 2, 1))
    ins["m2"] = strtonum(substr(_str, length(_str) - 3, 1))
    ins["m3"] = strtonum(substr(_str, length(_str) - 4, 1))
    ins["op"] = strtonum(substr(_str, length(_str) - 1, 2))
    ins["p1"] = from_memory(proc["memory"], _ptr+1)
    ins["p2"] = from_memory(proc["memory"], _ptr+2)
    ins["p3"] = from_memory(proc["memory"], _ptr+3)

    if(DEBUG) print ins_to_string(ins)
}

function get_value(proc, ins, num, _m, _p) {
    _p = ins["p" num]
    _m = ins["m" num]
    print "_p: " _p " _m: " _m
    if(_m == 0) return from_memory(proc["memory"], _p)
    else if(_m == 1) return _p
    else if(_m == 2) {
        return from_memory(proc["memory"], _p + proc["base"])
    }
    else {
        print "Bad m value decoded: " _m
        exit(1)
    }
}

function set_value_advance_ptr(proc, ins, val, ptr_advance) {
    to_memory(proc["memory"], ins["p3"],  val)
    proc["ptr"] += ptr_advance;
}

function op_1(proc, ins, _v1, _v2) {
    _v1 = get_value(proc, ins, 1)
    _v2 = get_value(proc, ins, 2)
    set_value_advance_ptr(proc, ins, _v1 + _v2, 4)

    if(DEBUG) print "op1: " _v1 " + " _v2 " ("  (_v1 + _v2) ") -> " ins["p3"]
}

function op_2(proc, ins, _v1, _v2) {
    _v1 = get_value(proc, ins, 1)
    _v2 = get_value(proc, ins, 2)
    set_value_advance_ptr(proc, ins, _v1 * _v2, 4)

    if(DEBUG) print "op2: " _v1 " * " _v2 " ("  (_v1 * _v2) ") -> " ins["p3"]
}

function op_3(proc, ins, _input, _addr) {
    _addr = ins["p1"]
    if(ins["m1"] == 2) {
        _addr = _addr + proc["base"]
    }
    
    _input = from_bus(proc["id"])
    to_memory(proc["memory"], _addr,  _input)
    proc["ptr"] += 2

    if(DEBUG) print "op3: from input " _input " -> " _addr
}

function op_4(proc, ins, _val) {
    _val = get_value(proc, ins, 1)
    to_bus(proc["id"], _val)
    proc["ptr"] += 2

    if(DEBUG) print "op4: output " _val " -> " proc["id"]
}

function op_5(proc, ins, _v1, _v2) {
    _v1 = get_value(proc, ins, 1)
    _v2 = get_value(proc, ins, 2)
    if(_v1 != 0) {
        if(DEBUG) print "op5: " _v1 " != 0 proc ptr is now " _v2
        proc["ptr"] = _v2
    }
    else {
        proc["ptr"] += 3
        if(DEBUG) print "op5: " _v1 " == 0 proc ptr is now " proc["ptr"]
    }
}

function op_6(proc, ins, _v1, _v2) {
    _v1 = get_value(proc, ins, 1)
    _v2 = get_value(proc, ins, 2)
    if(_v1 == 0) {
        if(DEBUG) print "op6: " _v1 " == 0 proc ptr is now " _v2
        proc["ptr"] = _v2
    }
    else {
        proc["ptr"] += 3
        if(DEBUG) print "op6: " _v1 " != 0 proc ptr is now " proc["ptr"]
    }
}

function op_7(proc, ins, _v1, _v2) {
    _v1 = get_value(proc, ins, 1)
    _v2 = get_value(proc, ins, 2)
    if(_v1 < _v2) {
        set_value_advance_ptr(proc, ins, 1, 4)
        if(DEBUG) print "op7: " _v1 " < " _v2 " 1 -> " ins["p3"]
    }
    else {
        set_value_advance_ptr(proc, ins, 0, 4)
        if(DEBUG) print "op7: " _v1 " !< " _v2 " 0 -> " ins["p3"]
    }
}

function op_8(proc, ins, _v1, _v2) {
    _v1 = get_value(proc, ins, 1)
    _v2 = get_value(proc, ins, 2)
    if(_v1 == _v2) {
        set_value_advance_ptr(proc, ins, 1, 4)
        if(DEBUG) print "op8: " _v1 " == " _v2 " 1 -> " ins["p3"]
    }
    else {
        set_value_advance_ptr(proc, ins, 0, 4)
        if(DEBUG) print "op8: " _v1 " != " _v2 " 0 -> " ins["p3"]
    }
}

function op_9(proc, ins, _v1) {
    _v1 = get_value(proc, ins, 1)
    proc["base"] += _v1
    proc["ptr"] += 2

    if(DEBUG) print "op9: adding " _v1 " to base, base is now " proc["base"]
}

function run_program(proc, max_execs, _ins, _execs) {
    _execs = 0
    while(_execs < max_execs) {
        decode_ins(proc, _ins)
        if(DEBUG) {
            print "ptr: " proc["ptr"] " base: " proc["base"]
        }

        if(_ins["op"] == 99) {
            return 99
        }
        else if(_ins["op"] == 1) {
            op_1(proc, _ins)
        }
        else if(_ins["op"] == 2) {
            op_2(proc, _ins)
        }
        else if(_ins["op"] == 3) {
            op_3(proc, _ins)
        }
        else if(_ins["op"] == 4) {
            op_4(proc, _ins)
        }
        else if(_ins["op"] == 5) {
            op_5(proc, _ins);
        }
        else if(_ins["op"] == 6) {
            op_6(proc, _ins)
        }
        else if(_ins["op"] == 7) {
            op_7(proc, _ins)
        }
        else if(_ins["op"] == 8) {
            op_8(proc, _ins)
        }
        else if(_ins["op"] == 9) {
            op_9(proc, _ins)
        }
        else {
            print "bad op: " _ins["op"]
            return
        }

        ++_execs
        if(DEBUG_PROC) print dump_used_memory(proc["memory"])
        if(DEBUG) print ""
    }
}

function part1() {
    reset_processors("a")
    reset_io_bus("a", 1)
    run_program(processors["a"], 1000)
    return output_to_string("a")
}

{ for(i = 1; i <= NF; ++i) DATA[i-1] = $(i) }

END {
    print "1: " part1()
}
