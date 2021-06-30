import groovy.transform.Immutable
import groovy.transform.Field

@Immutable
class Ingredient {
    int quantity
    String name

    static Ingredient from(String s) {
        def ary = s.split(' ')
        return new Ingredient(ary[0] as int, ary[1])
    }
}

@Immutable
class Reaction {
    Ingredient production
    List<Ingredient> precursors

    static Reaction from(String line) {
        def tmp = line.replace('=>', ',').split(', ').collect { Ingredient.from(it) }
        return new Reaction(tmp[-1], tmp[0..<(tmp.size()-1)])
    }    
}

@Field def reactions = new File("data/14").readLines().collect { Reaction.from(it) }

