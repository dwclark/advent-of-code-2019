final int TOTAL = 25 * 6

def layers = new File("data/08").text.trim().collect { it.toInteger() }.collate(TOTAL)
def minLayer = layers.min { l1, l2 -> l1.count(0) - l2.count(0) }
def outputChars = (0..<TOTAL).collect { i -> layers.find { layer -> layer[i] < 2 }[i] == 0 ? '.' : '#' }

println "1: ${minLayer.count(1) * minLayer.count(2)}"
outputChars.collate(25).collect { list -> new String(list as char[]) }.each { println it }
