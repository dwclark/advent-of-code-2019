import static Aoc.*

def part1(Intcode vm) {
    vm.store(1, 12).store(2, 2).call().load(0);
}

def part2(Intcode vm) {
    for(int noun in (0..99)) {
        for(int verb in (0..99)) {
            if(19690720 == vm.reset().store(1, noun).store(2, verb).call().load(0)) {
               return (100 * noun + verb)
            }
        }
    }
}

def vm = Intcode.from(new File("data/02"))
printAssert("Part 1:", part1(vm), 3409710,
	    "Part 2:", part2(vm), 7912)


