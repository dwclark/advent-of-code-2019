import static Aoc.*

def run(Intcode vm, int noun, int verb) {
    vm.reset().store(1, noun).store(2, verb).call().load(0);
}

def part2(Intcode vm) {
    (0..99).findResult { noun -> (0..99).findResult { verb -> 19690720 == run(vm, noun, verb) ? (100 * noun + verb) : null }}
}

def vm = Intcode.from(new File("data/02"))
printAssert("Part 1:", run(vm, 12, 2), 3409710,
	    "Part 2:", part2(vm), 7912)


