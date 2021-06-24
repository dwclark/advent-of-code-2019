class Intcode {

    static class Instruction {
        Integer opcode;
        private List<Integer> parameters;

        static Instruction from(Map<Integer,Integer> memory, Integer address) {
            new Instruction(opcode: memory[address],
                            parameters: ((address+1)..(address+3)).collect { memory.containsKey(it) ? memory[it] : 0 })
        }

        int parameter(int idx) { return parameters[idx-1] }
    }

    private int ptr = 0;
    private List<Integer> code;
    private Map<Integer,Integer> memory = [:]

    public Intcode store(Integer addr, Integer value) {
        memory[addr] = value
        return this
    }

    public Integer load(Integer addr) {
        return memory[addr]
    }

    public Integer value(Instruction ins, Integer param) {
        return memory[ins.parameter(param)]
    }

    private Map<Integer,Closure> ops = [
        (1): { ins -> store(ins.parameter(3), value(ins, 1) + value(ins, 2)); return 4; },
        (2): { ins -> store(ins.parameter(3), value(ins, 1) * value(ins, 2)); return 4; },
        (99): { ins -> return -1; }
    ]

    private Intcode(final List<Integer> code) {
        this.code = code;
        code.eachWithIndex { ins, addr -> memory[addr] = ins }
    }

    Intcode reset() {
        new Intcode(code);
    }
    
    static Intcode from(String str) {
        new Intcode(str.split(",").collect { it.toInteger() }.asImmutable())
    }

    static Intcode from(File file) {
        from(file.text);
    }

    public Intcode call() {
        while(true) {
            Instruction ins = Instruction.from(memory, ptr);
            Integer by = ops[ins.opcode].call(ins);
            if(by < 0) break;
            else ptr += by;
        }

        return this
    }
}
