import groovy.transform.Immutable
import groovy.transform.Field

@Immutable
class Ingredient {
    int quantity
    String name

    @Override
    String toString() { "($quantity,$name)" }
    
    Ingredient plus(Ingredient rhs) {
        if(!canCombine(rhs))
            throw new IllegalArgumentException()
        return new Ingredient(quantity + rhs.quantity, name)
    }

    Ingredient multiply(Integer num) {
        return new Ingredient(num * quantity, name)
    }

    boolean canCombine(Ingredient rhs) {
        return name == rhs.name
    }

    Integer multiplier(Reaction r) {
        if(r.production.quantity >= quantity) return 1
        else if(quantity % r.production.quantity != 0) return 1 + ((int) (quantity / r.production.quantity))
        else return (int) (quantity / r.production.quantity)
    }

    List<Ingredient> substitute(Reaction r) {
        if(!canCombine(r.production)) return [ this ]
        else return r.precursors.collect { it * multiplier(r) }
    }

    static List<Ingredient> combineAll(List<Ingredient> ingredients) {
        def names = ingredients.collect { it.name } as Set
        names.inject([]) { list, name ->
            list << ingredients.findAll { it.name == name }.sum() }
    }
    
    static Ingredient from(String s) {
        def ary = s.split(' ')
        return new Ingredient(ary[0] as int, ary[1])
    }
}

@Immutable
class Reaction {
    List<Ingredient> precursors
    Ingredient production

    Reaction substitute(Reaction other) {
        List<Ingredient> tmp = precursors.inject([]) { list, ing -> list + ing.substitute(other) }
        new Reaction(Ingredient.combineAll(tmp), production);
    }

    boolean isOreReaction() { precursors.size() == 1 && precursors[0].name == 'ORE' }

    Reaction findSubstitution(List<Reaction> reactions, boolean oreReaction) {
        for(Ingredient ing in precursors) {
            Reaction r = reactions.find { r -> r.production.name == ing.name && oreReaction == r.oreReaction }
            if(r) return r
        }

        return null
    }
    
    Reaction intermediate(List<Reaction> reactions) {
        Reaction tmp = this
        Reaction sub = null
        println tmp
        while((sub = tmp.findSubstitution(reactions, false)) != null) {
            tmp = tmp.substitute(sub)
            println tmp
        }

        return tmp
    }

    Reaction finalReaction(List<Reaction> reactions) {
        Reaction tmp = this
        Reaction sub = null
        while((sub = tmp.findSubstitution(reactions, true)) != null) {
            tmp = tmp.substitute(sub)
        }

        return tmp
    }
    
    static Reaction from(String line) {
        def tmp = line.replace('=>', ',').split(', ').collect { Ingredient.from(it) }
        return new Reaction(tmp[0..<(tmp.size()-1)], tmp[-1])
    }    
}

def reactions = new File("data/14").readLines().collect { Reaction.from(it) }
def fuel = reactions.find { r -> r.production.name == 'FUEL' }
def i = fuel.intermediate(reactions)
println i
def f = i.finalReaction(reactions)
println f
