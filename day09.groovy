def p1 = Intcode.from(new File("data/09"), new IoBus().write(1L)).call().bus.writes
def p2 = Intcode.from(new File("data/09"), new IoBus().write(2L)).call().bus.writes

println "1: ${p1}, 2: ${p2}"
