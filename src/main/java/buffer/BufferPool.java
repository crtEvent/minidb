package buffer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BufferPool {
    public List<Frame> buffers;
    public BufferId nextVictimId;

    public BufferPool(int poolSize) {
        this.buffers = new ArrayList<>(poolSize);
        for (int i = 0; i < poolSize; i++) {
            this.buffers.add(new Frame(0, null));  // Initialize frames with default values
        }
        this.nextVictimId = new BufferId(0);  // Initialize nextVictimId
    }

    /**
     * 삭제할 버퍼를 결정해서, 그 버퍼 ID 를 반환한다.
     * 만약 모든 버퍼가 할당된 상태이고 삭제할 수 있는 버퍼가 하나도 없는 경우에는 Optional.empty()를 반환한다.
     * @return 삭제할 버퍼의 ID
     */
    public Optional<BufferId> evict() {
        int poolSize = this.buffers.size();
        int consecutivePinned = 0;
        BufferId victimId;

        while (true) {
            BufferId nextVictimId = this.nextVictimId;
            Frame frame = this.buffers.get(nextVictimId.id());

            if (frame.usageCount == 0) {
                victimId = nextVictimId;
                break;
            }

            if (frame.buffer != null) {
                frame.usageCount -= 1;
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

    public Frame getFrame(BufferId bufferId) {
        return this.buffers.get(bufferId.id());
    }

    public void setFrame(BufferId bufferId, Frame frame) {
        this.buffers.set(bufferId.id(), frame);
    }
}
