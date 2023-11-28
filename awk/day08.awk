#!/usr/bin/awk -f

BEGIN {
    FS = ""; TOTAL = 25 * 6; zeroes = 151; offset = 1; layers = 0; delete counts;
}

function showing_pixel(off, _i) {
    for(_i = off; _i < NF; _i = _i + TOTAL)
        if($(_i) == 1 || $(_i) == 0)
            return $(_i)
    return 2
}

function group_counts(off, _ret, _i) {
    delete counts;
    for(_i = 0; _i < TOTAL; ++_i)
        counts[$(_i+off)]++
}

END {
    layers = NF / TOTAL;
    for(i = 1; i <= NF; i = (i + layers)) {
        group_counts(i);
        if(counts[0] < zeroes) {
            zeroes = counts[0];
            ones_and_twos = counts[1] * counts[2]
        }
    }

    print "1: " ones_and_twos

    for(i = 1; i <= TOTAL; ++i) {
        printf("%s", showing_pixel(i) == 0 ? "." : "#") #give contrast to the output
        if(i % 25 == 0) printf("\n")
    }
}
