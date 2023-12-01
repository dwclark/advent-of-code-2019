class BestCost<D,C extends Comparable<C>> {
    final Map<D,Map.Entry<D,C>> soFar = new HashMap<>()
    final Heap<D,C> heap

    BestCost(Heap<D,C> heap) {
	this.heap = heap
    }

    D next() {
	return heap.next()
    }

    C cost(D d) {
	return soFar[d].value
    }

    void add(D d, C c) {
	final Map.Entry<D,C> entry = soFar[d]
	if(!entry) {
	    soFar[d] = heap.insert(d, c)
	}
	else if(heap.cmp.compare(c, entry.value) == -1) {
	    heap.extremize(entry, c)
	}
    }

    static void main(String[] args) {
	new BestCost<>(Heap.min()).with {
	    add "one", 100
	    add "two", 200
	    add "three", 300
	    add "three", 700
	    add "three", 30

	    assert next() == "three"
	    assert next() == "one"
	    assert next() == "two"
	    assert !next()
	}
    }
}
