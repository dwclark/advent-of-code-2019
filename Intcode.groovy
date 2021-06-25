class Intcode {

    static final int POSITION = 0
    static final int IMMEDIATE = 1

    static class Instruction {
        Integer opcode;
        private List<Integer> parameters;
        private List<Integer> modes;

        static List<Integer> populateModes(int val) {
            int soFar = (int) (val / 100)
            int mode1 = soFar % 10;
            soFar = (int) (soFar / 10)
            int mode2 = soFar % 10;
            int mode3 = (int) (soFar / 10)
            return [ mode1, mode2, mode3 ]
        }

        static Instruction from(Map<Integer,Integer> memory, Integer address) {
            new Instruction(opcode: (int) (memory[address] % 100),
                            parameters: ((address+1)..(address+3)).collect { memory.containsKey(it) ? memory[it] : 0 },
                            modes: populateModes(memory[address]))
        }

        int parameter(int idx) { return parameters[idx-1] }
        int mode(int idx) { return modes[idx-1] }
    }

    private int ptr = 0;
    private List<Integer> code;
    private Map<Integer,Integer> memory = [:]
    final IoBus bus;

    public Intcode store(Integer addr, Integer value) {
        memory[addr] = value
        return this
    }

    public Integer load(Integer addr) {
        return memory[addr]
    }

    public Integer value(Instruction ins, Integer param) {
        if(ins.mode(param) == POSITION) {
            return memory[ins.parameter(param)]
        }
        else if(ins.mode(param) == IMMEDIATE) {
            return ins.parameter(param)
        }
        else {
            throw new IllegalStateException("bad mode")
        }
    }

    private Map<Integer,Closure> ops = [
        (1): { ins -> store(ins.parameter(3), value(ins, 1) + value(ins, 2)); return 4; },
        (2): { ins -> store(ins.parameter(3), value(ins, 1) * value(ins, 2)); return 4; },
        (3): { ins -> store(ins.parameter(1), bus.read()); return 2; },
        (4): { ins -> bus.write(value(ins, 1)); return 2; },
        (5): { ins ->
            if(value(ins, 1) != 0) {
                ptr = value(ins, 2)
                return 0
            }
            else return 3
        },
        (6): { ins ->
            if(value(ins, 1) == 0) {
                ptr = value(ins, 2)
                return 0
            }
            else return 3
        },
        (7): { ins ->
            if(value(ins, 1) < value(ins, 2))
                store(ins.parameter(3), 1)
            else
                store(ins.parameter(3), 0)
            return 4
        },
        (8): { ins ->
            if(value(ins, 1) == value(ins, 2))
                store(ins.parameter(3), 1)
            else
                store(ins.parameter(3), 0)
            return 4
        },
            
        (99): { ins -> return -1; }
    ]

    private void code2Memory() {
        code.eachWithIndex { ins, addr -> memory[addr] = ins }        
    }
    
    private Intcode(final List<Integer> code, final IoBus bus) {
        this.code = code;
        this.bus = bus;
        code2Memory()
    }

    Intcode reset() {
        ptr = 0;
        memory.clear()
        code2Memory()
        bus.reset()
        return this
    }

    static Intcode from(String str, IoBus bus) {
        new Intcode(str.split(",").collect { it.toInteger() }.asImmutable(), bus)
    }
    
    static Intcode from(String str) {
        return from(str, new IoBus())
    }

    static Intcode from(File file, IoBus bus) {
        return from(file.text, bus)
    }

    static Intcode from(File file) {
        from(file, new IoBus());
    }

    public Intcode call() {
        try {
            while(true) {
                Instruction ins = Instruction.from(memory, ptr);
                Integer by = ops[ins.opcode].call(ins);
                if(by < 0) break;
                else ptr += by;
            }
            
            return this
        }
        catch(Exception e) {
            println memory
            throw e
        }
    }
}
