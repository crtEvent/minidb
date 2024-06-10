package buffer;

public class BufferFrame {
    public long usageCount; // 버퍼 이용 횟수
    public Buffer buffer;

    public BufferFrame(long usageCount, Buffer buffer) {
        this.usageCount = usageCount;
        this.buffer = buffer;
    }
}
