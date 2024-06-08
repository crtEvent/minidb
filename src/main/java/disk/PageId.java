package disk;

public record PageId(
    long pageId
) {
    public long toLong() {
        return pageId;
    }
}
