#!/usr/bin/awk -f

BEGIN { FS = ","; delete data; delete copy; }

{ for(i = 1; i <= NF; ++i) data[i-1] = $(i) }

function makeCopy() { for(i in data) copy[i] = data[i] }

function runProgram(noun, verb) {
    makeCopy()
    copy[1] = noun; copy[2] = verb; idx = 0;
    while(1) {
        op = copy[idx]
        if(op == 99) break
        if(op == 1) copy[copy[idx+3]] = (copy[copy[idx+1]] + copy[copy[idx+2]]);
        if(op == 2) copy[copy[idx+3]] = (copy[copy[idx+1]] * copy[copy[idx+2]]);
        idx += 4
    }

    return copy[0]
}

END {
    for(noun = 0; noun <= 99; ++noun)
        for(verb = 0; verb <= 99; ++verb)
            if(runProgram(noun, verb) == 19690720) {
                print "1: " runProgram(12, 2) " 2: " (100 * noun + verb)
                exit 0
            }
}
