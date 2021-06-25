def bus = new IoBus().write(1)
def vm = Intcode.from(new File("data/05"), bus).call()
def v1 = vm.bus.lastWrite()

vm = vm.reset()
vm.bus.write(5)
v2 = vm.call().bus.lastWrite()

println "1: ${v1}, 2: ${v2}"
