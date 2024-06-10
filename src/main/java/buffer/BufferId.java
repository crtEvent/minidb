package buffer;

public record BufferId(
    int id
) {

    public BufferId increment(int size) {
        return new BufferId((this.id + 1) % size);
    }

}
