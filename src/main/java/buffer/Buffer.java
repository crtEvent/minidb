package buffer;

import disk.PageId;
import java.util.concurrent.atomic.AtomicBoolean;

public class Buffer {
    public PageId pageId;
    public Page page;
    public AtomicBoolean isDirty;

    public Buffer(PageId pageId, Page page, boolean isDirty) {
        this.pageId = pageId;
        this.page = page;
        this.isDirty = new AtomicBoolean(isDirty);
    }
}
