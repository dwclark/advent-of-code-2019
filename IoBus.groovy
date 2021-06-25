import java.util.concurrent.*

public class IoBus {

    public static volatile boolean DEBUG = false

    BlockingQueue<Integer> writeChannel;
    BlockingQueue<Integer> readChannel;

    public IoBus() {
        writeChannel = (readChannel = new LinkedBlockingQueue<>())
    }

    public IoBus(BlockingQueue<Integer> readChannel, BlockingQueue<Integer> writeChannel) {
        this.readChannel = readChannel;
        this.writeChannel = writeChannel;
    }

    public IoBus write(Integer val) {
        if(DEBUG) println "Writing ${val}"
        writeChannel.put(val);
        return this
    }

    public Integer lastWrite() {
        def last = null
        writeChannel.each { i -> last = i }
        return last
    }

    public Integer read() {
        def val = readChannel.take();
        if(DEBUG) println "Read ${val}"
        return val
    }

    public IoBus seedRead(Integer... vals) {
        vals.each { readChannel.put(it) }
        return this
    }

    public IoBus reset() {
        writeChannel.clear()
        readChannel.clear()
        return this
    }
}
