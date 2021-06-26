boolean sameAdjacent(String str) {
    for(int i = 1; i < str.length(); ++i) {
        if(str[i-1] == str[i])
            return true
    }

    return false
}

boolean ascending(String str) {
    for(int i = 1; i < str.length(); ++i) {
        if(str[i-1].toInteger() > str[i].toInteger())
            return false
    }

    return true;
}

boolean hasA2Group(String str) {
    def groups = [:]
    for(c in str) groups[c] = groups.get(c, 0) + 1
    return groups.find { k,v -> v == 2 }
}

def p1matches = [], p2match = 0

(272091..815432).each { num ->
    String str = num.toString()
    if(sameAdjacent(str) && ascending(str))
        p1matches.add(str)
}


p1matches.each { if(hasA2Group(it)) p2match++ }

println "1: ${p1matches.size()}, 2: $p2match"
