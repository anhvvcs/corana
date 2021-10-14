package enums;

public enum Mode {
    LEFT(1), RIGHT(2), ASR(3), LSL(4);
    private final int s;

    Mode(int s) {
        this.s = s;
    }
}
