public class IoBus {
    
    private int readPtr = 0;
    private List<Integer> channel = []

    public boolean hasMore() {
        return readPtr < channel.size()
    }
    
    public IoBus write(Integer val) {
        channel << val
        return this
    }

    public Integer read() {
       channel[readPtr++]
    }

    public Integer last() {
        channel[-1]
    }

    public String toString() {
        return "${readPtr}, channel: ${channel}"
    }
}
