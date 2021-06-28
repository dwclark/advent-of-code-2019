import java.util.concurrent.*

public class IoBus {

    public volatile boolean DEBUG = false

    public IoBus() {
        writeChannel = (readChannel = new LinkedBlockingQueue<>())
    }

    public IoBus(BlockingQueue<Long> readChannel, BlockingQueue<Long> writeChannel) {
        this.readChannel = readChannel;
        this.writeChannel = writeChannel;
    }

    public static IoBus separateChannels() {
        return new IoBus(new LinkedBlockingQueue<>(), new LinkedBlockingQueue());
    }

    //interface for intcode programs
    public IoBus write(Long val) {
        if(DEBUG) println "Writing ${val}"
        writeChannel.put(val);
        return this
    }

    public Long read() {
        def val = readChannel.take();
        if(DEBUG) println "Read ${val}"
        return val
    }

    //control interface for users of intcode programs
    final BlockingQueue<Long> writeChannel;
    final BlockingQueue<Long> readChannel;

    public Long lastWrite() {
        def last = null
        writeChannel.each { i -> last = i }
        return last
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
