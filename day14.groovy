import groovy.transform.Immutable
import groovy.transform.Field

class Reaction {
    private String _name(String raw) { raw.split(' ')[1] }
    private Integer _quantity(String raw) { raw.split(' ')[0].toInteger() }
    
    Reaction(String line) {
        def tmp = line.replace('=>', ',').split(', ')
        inputs = tmp[0..<(tmp.size()-1)].inject([:]) { map, raw -> map << new MapEntry(_name(raw), _quantity(raw)) }.asImmutable()
        produces = _name(tmp[-1])
        quantity = _quantity(tmp[-1])
    }
    
    final Map<String,Integer> inputs
    final Integer quantity
    final String produces
    boolean isOre() { inputs.containsKey('ORE') }
}

class Warehouse {
    List<Reaction> reactions
    long oreCost
    Map<String,Integer> stock = [:]
    Warehouse(List<Reaction> reactions) { this.reactions = reactions.asImmutable() }

    void produce(String name, Integer quantity) {
        Reaction r = reactions.find { it.produces == name; }
        if(r.ore) {
            while(stock.get(name, 0) < quantity) {
                oreCost += r.inputs['ORE']
                stock[r.produces] = stock[r.produces] + r.quantity
            }
        }
        else {
            while(stock.get(name, 0) < quantity) {
                r.inputs.each { inputName, inputQuantity ->
                    while(stock.get(inputName, 0) < inputQuantity)
                        produce(inputName, inputQuantity);
                    assert stock[inputName] >= inputQuantity
                    stock[inputName] = stock[inputName] - inputQuantity
                }

                stock[r.produces] = stock.get(r.produces, 0) + r.quantity;
            }
        }
    }
}

def wareHouse = new Warehouse(new File("data/14").readLines().collect { new Reaction(it) })
wareHouse.produce("FUEL", 1)
println "1: ${wareHouse.oreCost}"
