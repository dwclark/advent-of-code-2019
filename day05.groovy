def bus = new IoBus().write(1)
def vm = Intcode.from(new File("data/05"), bus).call()
def v1 = 0

while(bus.hasMore()) {
    v1 = bus.read()
    assert v1 == 0 || !bus.hasMore()
}

vm = vm.reset()
vm.bus.write(5)
v2 = vm.call().bus.last()

println "1: ${v1}, 2: ${v2}"
