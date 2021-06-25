import java.util.concurrent.*

public class IoBus {

    public static volatile boolean DEBUG = false

    BlockingQueue<Long> writeChannel;
    BlockingQueue<Long> readChannel;

    public IoBus() {
        writeChannel = (readChannel = new LinkedBlockingQueue<>())
    }

    public IoBus(BlockingQueue<Long> readChannel, BlockingQueue<Long> writeChannel) {
        this.readChannel = readChannel;
        this.writeChannel = writeChannel;
    }

    public IoBus write(Long val) {
        if(DEBUG) println "Writing ${val}"
        writeChannel.put(val);
        return this
    }

    public Long lastWrite() {
        def last = null
        writeChannel.each { i -> last = i }
        return last
    }

    public Long read() {
        def val = readChannel.take();
        if(DEBUG) println "Read ${val}"
        return val
    }

    public IoBus seedRead(Long... vals) {
        vals.each { readChannel.put(it) }
        return this
    }

    public IoBus reset() {
        writeChannel.clear()
        readChannel.clear()
        return this
    }

    public List<Long> getWrites() {
        writeChannel.inject([]) { ret, val -> ret << val }
    }
}
