#!/usr/bin/awk -f

BEGIN { FS="-" }

function same_adjacent(ary) {
    for(i = 1; i < 6; ++i)
        if(ary[i] == ary[i+1])
            return 1
    return 0
}

function ascending(ary, _f, _s) {
    for(i = 1; i < 6; ++i)
        if(ary[i] > ary[i+1])
            return 0
    return 1
}

function has_a_2_group(ary, _groups) {
    for(i in ary) _groups[ary[i]]++

    for(i in _groups)
        if(_groups[i] == 2)
            return 1
    return 0
}

{
    
    for(num = $1; num <= $2; ++num) {
        split(num, ary, "")
        if(same_adjacent(ary) && ascending(ary))
            p1match[p1++] = num
    }

    for(idx in p1match) {
        split(p1match[idx], ary, "")
        if(has_a_2_group(ary))
            p2match++
    }
}

END { print "1: " length(p1match) " 2: " p2match }
    
