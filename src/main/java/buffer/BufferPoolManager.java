package buffer;

import disk.DiskManager;
import disk.PageId;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class BufferPoolManager {
    private DiskManager disk;
    private BufferPool pool;
    private Map<PageId, BufferId> pageTable;

    public BufferPoolManager(DiskManager disk, BufferPool pool) {
        this.disk = disk;
        this.pool = pool;
        this.pageTable = new HashMap<>();
    }

    public synchronized AtomicReference<Buffer> fetchPage(PageId pageId) throws Exception {
        if (pageTable.containsKey(pageId)) {
            BufferId bufferId = pageTable.get(pageId);
            Frame frame = pool.getFrame(bufferId);
            frame.usageCount += 1;
            return new AtomicReference<>(frame.buffer);
        }

        BufferId bufferId = pool.evict()
            .orElseThrow(() -> new RuntimeException("No free buffer available"));
        Frame frame = pool.getFrame(bufferId);
        PageId evictPageId = frame.buffer.pageId;

        Buffer buffer = frame.buffer;
        if (buffer.isDirty.get()) {
            disk.writePageData(evictPageId, buffer.page.getData());
        }

        buffer.pageId = pageId;
        buffer.isDirty.set(false);
        disk.readPageData(pageId, buffer.page.getData());
        frame.usageCount = 1;

        AtomicReference<Buffer> page = new AtomicReference<>(buffer);
        pageTable.remove(evictPageId);
        pageTable.put(pageId, bufferId);

        return page;
    }

    public synchronized AtomicReference<Buffer> createPage() throws Exception {
        BufferId bufferId = pool.evict()
            .orElseThrow(() -> new RuntimeException("No free buffer available"));
        Frame frame = pool.getFrame(bufferId);
        PageId evictPageId = frame.buffer.pageId;

        Buffer buffer = frame.buffer;
        if (buffer.isDirty.get()) {
            disk.writePageData(evictPageId, buffer.page.getData());
        }

        PageId pageId = disk.allocatePage();
        buffer = new Buffer(pageId, new Page(), true);
        frame.buffer = (buffer);
        frame.usageCount = 1;

        AtomicReference<Buffer> page = new AtomicReference<>(buffer);
        pageTable.remove(evictPageId);
        pageTable.put(pageId, bufferId);

        return page;
    }

    public synchronized void flush() throws Error, IOException {
        for (Map.Entry<PageId, BufferId> entry : pageTable.entrySet()) {
            PageId pageId = entry.getKey();
            BufferId bufferId = entry.getValue();
            Frame frame = pool.getFrame(bufferId);
            Buffer buffer = frame.buffer;

            if (buffer.isDirty.get()) {
                disk.writePageData(pageId, buffer.page.getData());
                buffer.isDirty.set(false);
            }
        }

        disk.sync();
    }
}
