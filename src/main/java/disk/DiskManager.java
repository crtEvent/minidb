package disk;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;

public class DiskManager {
    private final RandomAccessFile heapFile;
    private long nextPageId;
    private static final int PAGE_SIZE = 4096;

    /**
     *
     * @param heapFile
     * @throws IOException 지정된 경로에 해당하는 파일이 존재하지 않거나, Disk Full 상태로 인해 I/O 관련된 에러가 발생할 수 있다
     */
    public DiskManager(RandomAccessFile heapFile) throws IOException {
        this.heapFile = heapFile;
        long heapFileSize = heapFile.length();
        this.nextPageId = heapFileSize / PAGE_SIZE;
    }

    /**
     * 파일 경로를 지정해서 HeapFile을 연다. HeapFile이 없으면 파일을 새로 생성한다.
     * @param heapFilePath
     * @return
     * @throws IOException
     */
    public static DiskManager open(Path heapFilePath) throws IOException {
        RandomAccessFile heapFile =
            new RandomAccessFile(heapFilePath.toFile(), "rw"); // rw: 읽기, 쓰기 허용
        return new DiskManager(heapFile);
    }

    /**
     * 다음 페이지 ID를 생성한다.
     * @return
     */
    public PageId allocatePage() {
        long pageId = nextPageId;
        nextPageId++;
        return new PageId(pageId);
    }

    /**
     * 페이지 데이터를 Read한다. 매개변수로 넣어준 data에 읽어온 데이터를 덮어씌워 준다
     * @param pageId
     * @param data
     * @throws IOException
     */
    public void readPageData(PageId pageId, byte[] data) throws IOException {
        long offset = PAGE_SIZE * pageId.toLong();
        heapFile.seek(offset);
        heapFile.readFully(data);
    }

    /**
     * 데이터를 페이지에 Write한다.
     * 이터를 파일에 기록하지만, 운영 체제의 파일 버퍼에 기록되기 때문에 데이터가 실제로 디스크에 쓰여졌다고 보장하진 않는다.
     * 데이터를 영구적으로 기록하려면 sunc() 메서드를 호출해야 한다.
     * @param pageId
     * @param data
     * @throws IOException
     */
    public void writePageData(PageId pageId, byte[] data) throws IOException {
        long offset = PAGE_SIZE * pageId.toLong();
        heapFile.seek(offset);
        heapFile.write(data);
    }

    /**
     * 디스크에 데이터를 영구적으로 기록.
     * @throws IOException
     */
    public void sync() throws IOException {
        heapFile.getFD().sync();
    }
}
