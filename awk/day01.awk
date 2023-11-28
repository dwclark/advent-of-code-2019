#!/usr/bin/awk -f

function single(w) { return (int(w/3) - 2) }
function cont(f, _tmp) { return single(f) <= 0 ? 0 : (single(f) + cont(single(f))); }
{ a += single($1); b += cont($1); }
END { print "1: " a " 2: " b }
