package buffer;

import disk.DiskManager;
import disk.PageId;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class BufferPoolManager {
    private final DiskManager disk;
    private final BufferPool pool;
    private final Map<PageId, BufferId> pageTable;

    public BufferPoolManager(DiskManager disk, BufferPool pool) {
        this.disk = disk;
        this.pool = pool;
        this.pageTable = new HashMap<>();
    }

    /**
     * 페이지 할당
     * @param pageId
     * @return
     * @throws Exception
     */
    public synchronized AtomicReference<Buffer> fetchPage(PageId pageId) throws Exception {
        // Page가 BufferPool에 있는 경우 해당 Buffer 할당
        if (pageTable.containsKey(pageId)) {
            BufferId bufferId = pageTable.get(pageId);
            BufferFrame bufferFrame = pool.getFrame(bufferId);
            bufferFrame.usageCount += 1;
            return new AtomicReference<>(bufferFrame.buffer);
        }

        // Page가 BufferPool에 없는 경우
        BufferId evictBufferId = pool.evict()
            .orElseThrow(() -> new RuntimeException("No free buffer available"));
        BufferFrame evictBufferFrame = pool.getFrame(evictBufferId);
        PageId evictPageId = evictBufferFrame.buffer.pageId;

        Buffer evictBuffer = evictBufferFrame.buffer;
        if (evictBuffer.isDirty()) {
            disk.writePageData(evictPageId, evictBuffer.page.getData());
        }

        evictBuffer.pageId = pageId;
        evictBuffer.toClean();
        disk.readPageData(pageId, evictBuffer.page.getData());
        evictBufferFrame.usageCount = 1;

        AtomicReference<Buffer> page = new AtomicReference<>(evictBuffer);
        pageTable.remove(evictPageId);
        pageTable.put(pageId, evictBufferId);

        return page;
    }

    public synchronized AtomicReference<Buffer> createPage() throws Exception {
        BufferId bufferId = pool.evict()
            .orElseThrow(() -> new RuntimeException("No free buffer available"));
        BufferFrame bufferFrame = pool.getFrame(bufferId);
        PageId evictPageId = bufferFrame.buffer.pageId;

        Buffer buffer = bufferFrame.buffer;
        if (buffer.isDirty()) {
            disk.writePageData(evictPageId, buffer.page.getData());
        }

        PageId pageId = disk.allocatePage();
        buffer = new Buffer(pageId, new Page(), true);
        bufferFrame.buffer = (buffer);
        bufferFrame.usageCount = 1;

        AtomicReference<Buffer> page = new AtomicReference<>(buffer);
        pageTable.remove(evictPageId);
        pageTable.put(pageId, bufferId);

        return page;
    }

    public synchronized void flush() throws Error, IOException {
        for (Map.Entry<PageId, BufferId> entry : pageTable.entrySet()) {
            PageId pageId = entry.getKey();
            BufferId bufferId = entry.getValue();
            BufferFrame bufferFrame = pool.getFrame(bufferId);
            Buffer buffer = bufferFrame.buffer;

            if (buffer.isDirty()) {
                disk.writePageData(pageId, buffer.page.getData());
                buffer.toClean();
            }
        }

        disk.sync();
    }
}
