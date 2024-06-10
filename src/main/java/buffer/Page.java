package buffer;

/**
 * 실제 byte[]형의 data가 page size 단위로 들어 있음
 */
public class Page {
    public static final int PAGE_SIZE = 4096;
    private byte[] data;

    public Page() {
        this.data = new byte[PAGE_SIZE];
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        if (data.length != PAGE_SIZE) {
            throw new IllegalArgumentException("Data size must be " + PAGE_SIZE);
        }
        this.data = data;
    }
}
