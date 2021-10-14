package enums;

public enum Type {
    BYTE("byte"), HALFWORD("halfword"), WORD("WORD"),
    BOTTOM_HALFWORD("bottomHalfWord"), TOP_HALFWORD("topHalfWord"),
    BOTTOM_BYTE("bottomByte"), ENTIRE("entire");
    private final String s;

    Type(String s) {
        this.s = s;
    }
}
