import groovy.transform.Field

@Field List<Integer> base = [ 0, 1, 0, -1 ]

List<Integer> multiplier(Integer position, Integer needed) {
    int count = 0;
    List<Integer> ret = new ArrayList(needed)
    while(count <= needed) { //one extra for skipping the first element
        for(int i = 0; (i < position && count <= needed); ++i)
            ret[count++] = base[0]
        
        for(int i = 0; (i < position && count <= needed); ++i)
            ret[count++] = base[1]

        for(int i = 0; (i < position && count <= needed); ++i)
            ret[count++] = base[2]
        
        for(int i = 0; (i < position && count <= needed); ++i)
            ret[count++] = base[3]
    }

    return ret
}

List<Integer> phase(List<Integer> input) {
    List<Integer> ret = new ArrayList(input.size())
    for(int position = 0; position < input.size(); ++position) {
        Integer accum = 0
        List<Integer> mult = multiplier(position+1, input.size())
        for(int multPos = 1; multPos < mult.size(); ++multPos) {
            accum += (mult[multPos] * input[multPos-1])
        }

        ret[position] = Math.abs(accum) % 10
    }

    return ret;
}

def input = new File("data/16").text.trim().collect { Character.digit(it as char, 10) }
for(int i = 0; i < 100; ++i)
    input = phase(input)

println "1: ${input[0..<8].join()}"

