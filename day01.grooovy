static single(w) { ((int) (w/3)) - 2; }
static cont(f) { return single(f) <= 0 ? 0 : single(f) + cont(single(f)) }
def lines = new File("data/01").readLines().collect { it.toInteger() }
println "1: ${lines.sum { single(it) }}, 2: ${lines.sum { cont(it) }}"
                              
