import static Aoc.*

def bus = new IoBus().write(1)
def vm = Intcode.from(new File("data/05"), bus).call()
def p1 = vm.bus.lastWrite()

vm = vm.reset()
vm.bus.write(5)
p2 = vm.call().bus.lastWrite()

printAssert("Part 1:", p1, 13547311, "Part 2:", p2, 236453)
