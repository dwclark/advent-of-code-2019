import java.util.concurrent.LinkedBlockingQueue;

def phase1AmpsInit = { code ->
    def queues = (0..5).collect { new LinkedBlockingQueue<>() }
    def ios = (1..5).collect { i -> new IoBus(queues[i-1], queues[i]) }
    def amps = (0..<5).collect { i -> Intcode.from(code, ios[i]) }
    return amps
}

def executePhase1 = { amps, List<Integer> phases ->
    amps.each {
        it.reset();
        it.bus.reset() }
    amps.eachWithIndex { amp, i ->
        if(i == 0) amp.bus.seedRead(phases[i], 0)
        else amp.bus.seedRead(phases[i])
    }

    amps.each { it.call(); }
    return amps[4].bus.lastWrite()
}

def phase2AmpsInit = { code ->
    def queues = (0..<5).collect { new LinkedBlockingQueue<>() }
    def ios = (0..<5).collect { i -> new IoBus(queues[i-1], queues[i]) }
    def amps = (0..<5).collect { i -> Intcode.from(code, ios[i]) }
    return amps
}

def executePhase2 = { amps, List<Integer> phases ->
    amps.each {
        it.reset();
        it.bus.reset() }
    amps.eachWithIndex { amp, i ->
        if(i == 0) amp.bus.seedRead(phases[i], 0)
        else amp.bus.seedRead(phases[i])
    }

    def threads = amps.collect { amp -> Thread.start { amp.call() } }
    threads.each { it.join() }
    return amps[4].bus.lastWrite()
}

def day7 = { phases, amps, closure ->
    def thruster = -1
    
    phases.eachPermutation { phase ->
        def tmp = closure.call(amps, phase)
        thruster = Math.max(thruster, tmp)
    }

    return thruster
}

def p1 = day7([0,1,2,3,4], phase1AmpsInit(new File("data/07")), executePhase1)
def p2 = day7([5,6,7,8,9], phase2AmpsInit(new File("data/07")), executePhase2)

println "1: ${p1}, 2: ${p2}"
