class Aoc {
    
    static void printAssert(Object... args) {
	List grouped = (args as List).collate(3)
	String toPrint = ""
	grouped.each { sub -> 
	    assert sub[1] == sub[2]
	    toPrint += "${sub[0]} ${sub[1]} "
	}

	println toPrint.trim()
    }

    static List<String> lines(String path) {
	new File(path).readLines()
    }
    
    static List<String> lines(String path, Closure transform) {
	new File(path).readLines().collect(transform)
    }

    static void main(String[] args) {
	def shell = new GroovyShell()
	(1..17).each { n ->
	    String name = "day${n.toString().padLeft(2, '0')}.groovy"
	    print "[Day ${n}] "
	    shell.evaluate(new File(name))
	}
    }
}
