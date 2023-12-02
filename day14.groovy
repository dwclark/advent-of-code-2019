import static Aoc.*

class Reaction {
    private String _name(String raw) { raw.split(' ')[1] }
    private Long _quantity(String raw) { raw.split(' ')[0].toLong() }
    
    Reaction(String line) {
        def tmp = line.replace('=>', ',').split(', ')
        inputs = tmp[0..<(tmp.size()-1)].inject([:]) { map, raw -> map << new MapEntry(_name(raw), _quantity(raw)) }.asImmutable()
        produces = _name(tmp[-1])
        quantity = _quantity(tmp[-1])
    }
    
    final Map<String,Long> inputs
    final Long quantity
    final String produces
    boolean isOre() { inputs.containsKey('ORE') }
}

class Warehouse {
    List<Reaction> reactions
    long oreCost
    Map<String,Long> stock = [:]
    Warehouse(List<Reaction> reactions) { this.reactions = reactions.asImmutable() }

    long multiplier(String name, long requested, long produced) {
        long needed = requested - stock.get(name, 0)
        return Math.ceil(needed / produced) as long
    }
    
    Warehouse produce(String name, Long quantity) {
        Reaction r = reactions.find { it.produces == name; }
        long m = multiplier(name, quantity, r.quantity)
        if(r.ore) {
            if(stock.get(name, 0) < quantity) {
                oreCost += (m * r.inputs['ORE'])
                stock[r.produces] = stock[r.produces] + (m * r.quantity)
            }
        }
        else {
            while(stock.get(name, 0) < quantity) {
                r.inputs.each { inputName, inputQuantity ->
                    long multiplied = inputQuantity * m
                    if(stock.get(inputName, 0) < multiplied)
                        produce(inputName, multiplied);
                    assert stock[inputName] >= multiplied
                    stock[inputName] = stock[inputName] - multiplied
                }

                stock[r.produces] = stock.get(r.produces, 0) + (m * r.quantity);
            }
        }

        return this
    }
}

//Slightly odd version of binary search. Basically, need to go just one beyond the
//absolute max to make sure we go just beyond the 1 trillion mark, then subtract one
//to stay under the 1 trillion limit
long binarySearchOreCost(List<Reaction> reactions, long lbound, long ubound, long oreTarget) {
    long low = lbound;
    long high = ubound;
    
    while(low <= high) {
        long mid = (low + high) >>> 1
        Warehouse wh = new Warehouse(reactions)
        wh.produce("FUEL", mid)
        
        if(wh.oreCost < oreTarget) low = mid + 1L;
        else if(wh.oreCost > oreTarget) high = mid - 1L;
        else return mid;
    }

    return low-1L
}

def reactions = new File("data/14").readLines().collect { new Reaction(it) }
printAssert("Part 1:", new Warehouse(reactions).produce('FUEL', 1).oreCost, 870051,
	    "Part 2:", binarySearchOreCost(reactions, 0L, 1000000000000L, 1000000000000L), 1863741)
