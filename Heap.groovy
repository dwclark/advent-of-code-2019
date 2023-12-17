class Heap<D,C extends Comparable<C>> {

    static class Natural<C> implements Comparator<C> {
	int compare(C one, C two) {
	    return one.compareTo(two)
	}
    }

    static class Opposite<C> implements Comparator<C> {
	int compare(C one, C two) {
	    return two.compareTo(one)
	}
    }
    
    static class Data<D, C extends Comparable<C>> implements Map.Entry<D,C> {
	private final D d
	private C c
	private int index
	
	public Data(D data, C comparable) {
	    this.d = data
	    this.c = comparable
	}

	public D getKey() { d }
	public C getValue() { c }
	public C setValue(C c) { throw new UnsupportedOperationException() }
    }
    
    final List<Data> heap = new ArrayList<>()
    final Comparator<C> cmp;
    
    private Heap(final Comparator<C> cmp) {
	this.cmp = cmp
    }

    public static <D,C extends Comparable<C>> min() {
	return new Heap<>(new Natural<>())
    }

    public static <D,C extends Comparable<C>> max() {
	return new Heap<>(new Opposite<>())
    }

    public int size() {
	return heap.size()
    }

    public void clear() {
	heap.clear()
    }
    
    public Map.Entry<D,C> insert(D d, C c) {
	return _floatUp(_place(d, c))
    }

    public D next() {
	Map.Entry<D,C> n = nextWithCost();
	return (n == null) ? null : n.key
    }

    public Map.Entry<D,C> nextWithCost() {
	if(heap.size() == 0) {
	    return null
	}
	
	Data ret = heap[0]
	_swap(0, heap.size()-1)
	heap.remove(heap.size()-1)

	if(heap.size() > 0) {
	    _pushDown(heap[0])
	}
	
	return ret
    }
    
    public D extremum() {
	if(heap.size() == 0) {
	    return null
	}
	else {
	    return heap[0].d
	}
    }

    public void extremize(final Map.Entry<D,C> o, final C newVal) {
	final Data data = (Data) o
	final int res = cmp.compare(newVal, data.c)
	if(res == 1) {
	    throw new IllegalArgumentException("${newVal} cannot replace ${data.c}")
	}
	else if(res == -1) {
	    data.c = newVal
	    _floatUp(data)
	}
    }
    
    private Data _floatUp(final Data data) {
	int pindex = _parent(data.index)
	
	while(0 <= pindex) {
	    if(cmp.compare(data.c, heap[pindex].c) == -1) {
		_swap(data.index, pindex)
		pindex = _parent(data.index)
	    }
	    else {
		break
	    }
	}

	return data
    }

    private void _pushDown(final Data data) {
	int leftIndex = _left(data.index)
	int rightIndex = _right(data.index)
	int extremeIndex = data.index

	while(leftIndex < heap.size()) {
	    if(cmp.compare(heap[leftIndex].c, heap[extremeIndex].c) == -1)
		extremeIndex = leftIndex
	    
	    if(rightIndex < heap.size() && cmp.compare(heap[rightIndex].c, heap[extremeIndex].c) == -1)
		extremeIndex = rightIndex
	    
	    if(extremeIndex != data.index) {
		_swap(extremeIndex, data.index)
		leftIndex = _left(data.index)
		rightIndex = _right(data.index)
		extremeIndex = data.index
	    }
	    else {
		break
	    }
	}
    }

    private void _swap(int i, int j) {
	Data idata = heap[i]
	Data jdata = heap[j]
	idata.index = j
	jdata.index = i
	heap[idata.index] = idata
	heap[jdata.index] = jdata
    }

    private Data _place(D d, C c) {
	final Data ret = new Data(d, c)
	ret.index = heap.size()
	heap[ret.index] = ret
	return ret
    }
    
    private int _parent(int i) { (i-1) >> 1 }
    private int _left(int i) { (i << 1) + 1  }
    private int _right(int i) { (i << 1) + 2  }
    
    static void main(String[] args) {
	def pq = min()
	(0..10).each { pq._parent(pq._left(it)) == it }
	(0..10).each { pq._parent(pq._right(it)) == it }

	(1..10).reverse().each { pq.insert(it, it) }
	(1..10).each { assert it == pq.next() }
	assert pq.size() == 0
	assert pq.next() == null

	def testList = (0..227).toList()
	pq.clear()
	def data = testList.shuffled().inject([]) { list, i -> list << pq.insert(i, i) }
	def resorted = []
	while(pq.size() != 0) {
	    resorted.add(pq.next())
	}

	assert testList == resorted

	data = testList.inject([]) { list, i -> list << pq.insert(i, i) }
	assert pq.extremum() == 0
	pq.extremize(data[227], -1)
	assert pq.extremum() == 227

	//basic test max map
	pq = max()
	(1..119).toList().shuffled().each { pq.insert(it, it) }
	pq.extremum() == 119
	resorted = []
	while(pq.size() != 0) {
	    resorted.add(pq.next())
	}

	assert (1..119).reverse().toList() == resorted
    }
}
