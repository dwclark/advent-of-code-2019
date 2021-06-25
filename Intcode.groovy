import groovy.transform.ToString

class Intcode {

    volatile boolean DEBUG = false
    
    enum Mode {
        POSITION, IMMEDIATE, RELATIVE;

        static Mode fromOrdinal(int ordinal) {
            if(POSITION.ordinal() == ordinal) return POSITION;
            else if(IMMEDIATE.ordinal() == ordinal) return IMMEDIATE;
            else if(RELATIVE.ordinal() == ordinal) return RELATIVE;
            else throw new IllegalArgumentException("${ordinal} is not a valid mode");
        }
    }

    @ToString
    static class Instruction {
        Integer opcode;
        List<Long> parameters;
        List<Mode> modes;

        static List<Mode> populateModes(int val) {
            int soFar = (int) (val / 100)
            Mode mode1 = Mode.fromOrdinal(soFar % 10)
            soFar = (int) (soFar / 10)
            Mode mode2 = Mode.fromOrdinal(soFar % 10)
            Mode mode3 = Mode.fromOrdinal((int) (soFar / 10))
            return [ mode1, mode2, mode3 ]
        }

        static Instruction from(Map<Integer,Long> memory, Integer address) {
            new Instruction(opcode: (int) (memory.get(address, 0L) % 100),
                            parameters: ((address+1)..(address+3)).collect { memory.get(it, 0L) },
                            modes: populateModes((int) memory.get(address, 0L)))
        }

        Long parameter(int idx) { return parameters[idx-1] }
        Mode mode(int idx) { return modes[idx-1] }
    }

    private int ptr = 0;
    private int baseOffset = 0;
    private List<Long> code;
    private Map<Integer,Long> memory = [:]
    final IoBus bus;

    public Intcode store(Number addr, Long value) {
        if(DEBUG) println "Storing ${value} at ${addr}"
        memory[(int) addr] = value
        return this
    }

    public Long load(Number addr) {
        if(addr < 0) {
            throw new IllegalArgumentException("trying to read from address ${addr}")
        }
        
        return memory.get((int) addr, 0L);
    }

    public int pvalueWrite(Instruction ins, Integer pos) {
        Mode m = ins.mode(pos)
        if(m == Mode.POSITION || m == Mode.IMMEDIATE) {
            return ins.parameter(pos)
        }
        else {
            return baseOffset + ins.parameter(pos);
        }
    }
    
    public Long pvalueRead(Instruction ins, Integer pos) {
        Mode m = ins.mode(pos)
        if(m == Mode.POSITION) {
            if(DEBUG) println "Reading POSITION from address ${ins.parameter(pos)}"
            return load(ins.parameter(pos))
        }
        else if(m == Mode.IMMEDIATE) {
            if(DEBUG) println "Returning immediate ${ins.parameter(pos)}"
            return ins.parameter(pos)
        }
        else if(m == Mode.RELATIVE) {
            if(DEBUG) println "Reading RELATIVE from address ${baseOffset + ins.parameter(pos)}"
            return load(baseOffset + ins.parameter(pos))
        }
    }

    private Map<Integer,Closure> ops = [
        (1): { ins -> store(pvalueWrite(ins, 3), pvalueRead(ins, 1) + pvalueRead(ins, 2)); return 4; },
        (2): { ins -> store(pvalueWrite(ins, 3), pvalueRead(ins, 1) * pvalueRead(ins, 2)); return 4; },
        (3): { ins -> store(pvalueWrite(ins, 1), bus.read()); return 2; },
        (4): { ins -> bus.write(pvalueRead(ins, 1)); return 2; },
        (5): { ins ->
            if(pvalueRead(ins, 1) != 0) {
                ptr = pvalueRead(ins, 2)
                return 0
            }
            else return 3
        },
        (6): { ins ->
            if(pvalueRead(ins, 1) == 0) {
                ptr = pvalueRead(ins, 2)
                return 0
            }
            else return 3
        },
        (7): { ins ->
            if(pvalueRead(ins, 1) < pvalueRead(ins, 2))
                store(pvalueWrite(ins, 3), 1)
            else
                store(pvalueWrite(ins, 3), 0)
            return 4
        },
        (8): { ins ->
            if(pvalueRead(ins, 1) == pvalueRead(ins, 2))
                store(pvalueWrite(ins, 3), 1)
            else
                store(pvalueWrite(ins, 3), 0)
            return 4
        },
        (9): { ins -> baseOffset += pvalueRead(ins, 1); return 2; },
        (99): { ins -> return -1; }
    ]

    private void code2Memory() {
        code.eachWithIndex { ins, addr -> memory[addr] = ins }        
    }
    
    private Intcode(final List<Long> code, final IoBus bus) {
        this.code = code;
        this.bus = bus;
        code2Memory()
    }

    Intcode reset() {
        ptr = 0
        baseOffset = 0
        memory.clear()
        code2Memory()
        bus.reset()
        return this
    }

    static Intcode from(String str, IoBus bus) {
        new Intcode(str.split(",").collect { it.toLong() }.asImmutable(), bus)
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
                if(DEBUG) println ins
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
