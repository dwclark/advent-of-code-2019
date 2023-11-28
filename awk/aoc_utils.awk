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

