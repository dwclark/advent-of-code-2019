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
