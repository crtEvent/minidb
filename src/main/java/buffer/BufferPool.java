package buffer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BufferPool {
    public List<BufferFrame> buffers;
    public BufferId nextVictimId;

    public BufferPool(int poolSize) {
        this.buffers = new ArrayList<>(poolSize);
        for (int i = 0; i < poolSize; i++) {
            this.buffers.add(new BufferFrame(0, null));  // Initialize frames with default values
        }
        this.nextVictimId = new BufferId(0);  // Initialize nextVictimId
    }

    /**
     * Buffer Pool에서 삭제할 Buffer를 결정해서, 해당 Buffer의 ID 를 반환한다.
     * 만약 모든 Buffer가 할당된 상태이고 삭제할 수 있는 Buffer가 하나도 없는 경우에는 Optional.empty()를 반환한다.
     * @return 삭제할 Buffer의 ID
     */
    public Optional<BufferId> evict() {
        int poolSize = this.buffers.size();
        int consecutivePinned = 0;
        BufferId victimId;

        while (true) {
            BufferId nextVictimId = this.nextVictimId;
            BufferFrame bufferFrame = this.buffers.get(nextVictimId.id());

            if (bufferFrame.usageCount == 0) {
                victimId = nextVictimId;
                break;
            }

            if (bufferFrame.buffer != null) {
                bufferFrame.usageCount -= 1;
                consecutivePinned = 0;
            } else {
                consecutivePinned += 1;
                if (consecutivePinned >= poolSize) {
                    return Optional.empty();
                }
            }

            this.nextVictimId = this.nextVictimId.increment(poolSize);
        }

        return Optional.of(victimId);
    }

    public BufferFrame getFrame(BufferId bufferId) {
        return this.buffers.get(bufferId.id());
    }

    public void setFrame(BufferId bufferId, BufferFrame bufferFrame) {
        this.buffers.set(bufferId.id(), bufferFrame);
    }
}
