import groovy.transform.Field;

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

assert 13547311 == Intcode.from(new File("data/05"), new IoBus().write(1)).call().bus.last();
assert 236453 == Intcode.from(new File("data/05"), new IoBus().write(5)).call().bus.last();

