import groovy.transform.Field;
import java.util.concurrent.*;

//extra tests
Intcode.Instruction.populateModes(1002) == [ Intcode.Mode.POSITION, Intcode.Mode.IMMEDIATE, Intcode.Mode.POSITION ]
Intcode.Instruction.populateModes(1001) == [ Intcode.Mode.POSITION, Intcode.Mode.IMMEDIATE, Intcode.Mode.POSITION ]
Intcode.Instruction.populateModes(10201) == [ Intcode.Mode.RELATIVE, Intcode.Mode.POSITION, Intcode.Mode.IMMEDIATE ]
//DAY 2 tests
assert 3500 == Intcode.from("1,9,10,3,2,3,11,0,99,30,40,50").call().load(0)
assert 2 == Intcode.from("1,0,0,0,99").call().load(0)
assert 6 == Intcode.from("2,3,0,3,99").call().load(3)
assert 9801  == Intcode.from("2,4,4,5,99,0").call().load(5)
assert 30 == Intcode.from("1,1,1,4,99,5,6,0,99").call().load(0)
assert 2 == Intcode.from("1,1,1,4,99,5,6,0,99").call().load(4)

@Field def vmDay2 = Intcode.from(new File("data/02"))

public day2_1() {
    vmDay2.store(1, 12).store(2, 2).call().load(0);
}

assert 3409710 == day2_1()

public day2_2() {
    def found = 0;
    for(int noun = 0; noun <= 99; ++noun) {
        for(int verb = 0; verb <= 99; ++verb) {
            def output = vmDay2.reset().store(1, noun).store(2, verb).call().load(0);
            if(output == 19690720) {
                return (100 * noun + verb)
            }
        }
    }
}

assert 7912 == day2_2()

// end Day 2 tests

// day 5 tests
def ioBus = new IoBus().write(17)
def vm = Intcode.from("3,0,4,0,99", ioBus).call()
assert 17 == vm.bus.read()
assert 99 == Intcode.from("1101,100,-1,4,0").call().load(4)

assert 1 == Intcode.from("3,9,8,9,10,9,4,9,99,-1,8", new IoBus().write(8)).call().bus.read();
assert 0 == Intcode.from("3,9,8,9,10,9,4,9,99,-1,8", new IoBus().write(7)).call().bus.read();
assert 1 == Intcode.from("3,9,7,9,10,9,4,9,99,-1,8", new IoBus().write(7)).call().bus.read();
assert 0 == Intcode.from("3,9,7,9,10,9,4,9,99,-1,8", new IoBus().write(8)).call().bus.read();
assert 1 == Intcode.from("3,3,1108,-1,8,3,4,3,99", new IoBus().write(8)).call().bus.read();
assert 0 == Intcode.from("3,3,1108,-1,8,3,4,3,99", new IoBus().write(7)).call().bus.read();
assert 1 == Intcode.from("3,3,1107,-1,8,3,4,3,99", new IoBus().write(7)).call().bus.read();
assert 0 == Intcode.from("3,3,1107,-1,8,3,4,3,99", new IoBus().write(8)).call().bus.read();
assert 0 == Intcode.from("3,12,6,12,15,1,13,14,13,4,13,99,-1,0,1,9", new IoBus().write(0)).call().bus.read();
assert 1 == Intcode.from("3,12,6,12,15,1,13,14,13,4,13,99,-1,0,1,9", new IoBus().write(100)).call().bus.read();
assert 0 == Intcode.from("3,3,1105,-1,9,1101,0,0,12,4,12,99,1", new IoBus().write(0)).call().bus.read();
assert 1 == Intcode.from("3,3,1105,-1,9,1101,0,0,12,4,12,99,1", new IoBus().write(127)).call().bus.read();

def d5long = "3,21,1008,21,8,20,1005,20,22,107,8,21,20,1006,20,31,1106,0,36,98,0,0,1002,21,125,20,4,20,1105,1,46,104,999,1105,1,46,1101,1000,1,20,4,20,1105,1,46,98,99"
assert 999 == Intcode.from(d5long, new IoBus().write(7)).call().bus.read();
assert 1000 == Intcode.from(d5long, new IoBus().write(8)).call().bus.read();
assert 1001 == Intcode.from(d5long, new IoBus().write(9)).call().bus.read();

assert 13547311 == Intcode.from(new File("data/05"), new IoBus().write(1)).call().bus.lastWrite();
assert 236453 == Intcode.from(new File("data/05"), new IoBus().write(5)).call().bus.lastWrite();

//day 7

def d7ex1Code = "3,15,3,16,1002,16,10,16,1,16,15,15,4,15,99,0,0"
def d7ex2Code = "3,23,3,24,1002,24,10,24,1002,23,-1,23,101,5,23,23,1,24,23,23,4,23,99,0,0"
def d7ex3Code = "3,31,3,32,1002,32,10,32,1001,31,-2,31,1007,31,0,33,1002,33,7,33,1,33,31,31,1,32,31,31,4,31,99,0,0,0"

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

assert 43210 == executePhase1(phase1AmpsInit(d7ex1Code), [4,3,2,1,0])
assert 54321 == executePhase1(phase1AmpsInit(d7ex2Code), [0,1,2,3,4])
assert 65210 == executePhase1(phase1AmpsInit(d7ex3Code), [1,0,4,3,2])

def day7 = { phases, amps, closure ->
    def thruster = -1
    
    phases.eachPermutation { phase ->
        def tmp = closure.call(amps, phase)
        thruster = Math.max(thruster, tmp)
    }

    return thruster
}

assert 359142 == day7([0,1,2,3,4], phase1AmpsInit(new File("data/07")), executePhase1)

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

assert 139629729 == executePhase2(phase2AmpsInit("3,26,1001,26,-4,26,3,27,1002,27,2,27,1,27,26," +
                                                 "27,4,27,1001,28,-1,28,1005,28,6,99,0,0,5"), [9,8,7,6,5])

assert 18216 == executePhase2(phase2AmpsInit("3,52,1001,52,-5,52,3,53,1,52,56,54,1007,54,5,55,1005,55,26,1001,54," +
                                             "-5,54,1105,1,12,1,53,54,53,1008,54,0,55,1001,55,1,55,2,53,55,53,4," +
                                             "53,1001,56,-1,56,1005,56,6,99,0,0,0,0,10"), [9,7,8,5,6])

assert 4374895 == day7([5,6,7,8,9], phase2AmpsInit(new File("data/07")), executePhase2)

assert (Intcode.from("109,1,204,-1,1001,100,1,100,1008,100,16,101,1006,101,0,99").call().bus.writes ==
        [109,1,204,-1,1001,100,1,100,1008,100,16,101,1006,101,0,99])

assert [1219070632396864L] == Intcode.from("1102,34915192,34915192,7,4,7,99,0").call().bus.writes

assert [1125899906842624L] == Intcode.from("104,1125899906842624,99").call().bus.writes

assert [-1L] == Intcode.from("109,-1,4,1,99").call().bus.writes
assert [1L] == Intcode.from("109,-1,104,1,99").call().bus.writes
assert [109L] == Intcode.from("109,-1,204,1,99").call().bus.writes
assert [204L] == Intcode.from("109,1,9,2,204,-6,99").call().bus.writes
assert [204L] == Intcode.from("109,1,109,9,204,-6,99").call().bus.writes
assert [204L] == Intcode.from("109,1,209,-1,204,-106,99").call().bus.writes
assert [77L] == Intcode.from("109,1,3,3,204,2,99", new IoBus().write(77L)).call().bus.writes
assert [107L] == Intcode.from("109,1,203,2,204,2,99", new IoBus().write(107L)).call().bus.writes
