import groovy.transform.CompileStatic
import static Aoc.*

@CompileStatic
class Fft {
    int[] base = [ 0, 1, 0, -1 ]
    int[] input
    int[] multiplier
    
    Fft(String str) {
        input = str.collect { Character.digit(it as char, 10) } as int[]
        multiplier = new int[input.length]
    }

    void populateMultiplier(int position) {
        int count = -1;
        int needed = multiplier.length
        while(count < needed) {
            for(int i = 0; (i < position && count < needed); ++i) {
                if(count >= 0) multiplier[count++] = base[0]
                else count++;
            }
            
            for(int i = 0; (i < position && count < needed); ++i)
                multiplier[count++] = base[1]
            
            for(int i = 0; (i < position && count < needed); ++i)
                multiplier[count++] = base[2]
            
            for(int i = 0; (i < position && count < needed); ++i)
                multiplier[count++] = base[3]
        }
    }
    
    String part1() {
        100.times {
            for(int position = 0; position < input.size(); ++position) {
                int accum = 0
                populateMultiplier(position+1)
                for(int multPos = 0; multPos < multiplier.length; ++multPos)
                    accum += (multiplier[multPos] * input[multPos])
                
                input[position] = Math.abs(accum) % 10
            }
        }

        return input[0..<8].join('')
    }

    String part2() {
        int skip = input[0..<7].join('').toInteger();
        100.times {
            int accum = 0;
            for(int i = input.length-1; i >= skip; --i) {
                accum += input[i]
                input[i] = accum % 10
            }
        }

        return input[skip..<(skip+8)].join('')
    }
}

Fft fft1 = new Fft(new File("data/16").text.trim())
Fft fft2 = new Fft(new File("data/16").text.trim() * 10_000)
printAssert("Part 1:", fft1.part1(), '17978331', "Part 2:", fft2.part2(), '19422575')
