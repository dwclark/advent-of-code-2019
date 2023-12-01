import static Aoc.*
import java.util.concurrent.LinkedBlockingQueue;

List init(File code, Range qrange, Range iosRange, Range ampsRange) {
    def queues = qrange.collect { new LinkedBlockingQueue<>() }
    def ios = iosRange.collect { i -> new IoBus(queues[i-1], queues[i]) }
    ampsRange.collect { i -> Intcode.from(code, ios[i]) }
}

def execute = { boolean threaded, List amps, List<Integer> phases ->
    amps.each { it.reset(); it.bus.reset() }
    amps.eachWithIndex { amp, i ->
        if(i == 0) amp.bus.seedRead(phases[i], 0)
        else amp.bus.seedRead(phases[i])
    }

    if(threaded)
	amps.collect { amp -> Thread.start { amp.call() } }.each { it.join() }
    else 
	amps.each { it.call(); }

    return amps[4].bus.lastWrite()
}

int findMax(List<Integer> phases, List amps, Closure toExec) {
    phases.permutations { perm -> toExec.call(amps, perm) }.max()
}

File file = new File("data/07")
def p1 = findMax([0,1,2,3,4], init(file, (0..5), (1..5), (0..<5)), execute.curry(false))
def p2 = findMax([5,6,7,8,9], init(file, (0..<5), (0..<5), (0..<5)), execute.curry(true))
printAssert("Part 1:", p1, 359142, "Part 2:", p2, 4374895)
