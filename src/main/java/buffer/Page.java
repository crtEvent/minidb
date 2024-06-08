package buffer;

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
