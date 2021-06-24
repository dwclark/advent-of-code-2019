import groovy.transform.Field;

@Field def vm = Intcode.from(new File("data/02"))

public part1() {
    vm.store(1, 12).store(2, 2).call().load(0);
}

public part2() {
    def found = 0;
    for(int noun = 0; noun <= 99; ++noun) {
        for(int verb = 0; verb <= 99; ++verb) {
            def output = vm.reset().store(1, noun).store(2, verb).call().load(0);
            if(output == 19690720) {
                return (100 * noun + verb)
            }
        }
    }
}

println "1: ${part1()}, 2: ${part2()}"


