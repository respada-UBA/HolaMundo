package primer;

public class Primer {
    public enum Direction { FORWARD, REVERSE }

    public String name;
    public String sequence; // 5' -> 3'
    public Direction direction;
    public int positionAA; // amino-acid position mutated (1-based)
    public double tm; // Wallace estimated
    public double gcPercent;

    public Primer(String name, String seq, Direction dir, int posAA) {
        this.name = name;
        this.sequence = seq.toUpperCase();
        this.direction = dir;
        this.positionAA = posAA;
        this.gcPercent = Utils.gcPercent(this.sequence);
        this.tm = Utils.wallaceTm(this.sequence);
    }

    @Override
    public String toString() {
        return String.format("%s\t%s\t%s\tAApos=%d\tTm=%.1f\tGC=%.1f%%",
                name, sequence, direction, positionAA, tm, gcPercent);
    }
}