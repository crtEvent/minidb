package buffer;

public class Frame {
    public long usageCount; // 버퍼 이용 횟수
    public Buffer buffer;

    public Frame(long usageCount, Buffer buffer) {
        this.usageCount = usageCount;
        this.buffer = buffer;
    }
}
