package buffer;

import disk.PageId;
import java.util.concurrent.atomic.AtomicBoolean;

public class Buffer {
    public PageId pageId;
    public final Page page;
    private final AtomicBoolean dirty;

    public Buffer(PageId pageId, Page page, boolean isDirty) {
        this.pageId = pageId;
        this.page = page;
        this.dirty = new AtomicBoolean(isDirty);
    }

    public boolean isDirty() {
        return dirty.get();
    }

    public void toClean() {
        dirty.set(false);
    }

}
