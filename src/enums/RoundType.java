package enums;

public enum RoundType {
    TOWARDS_ZERO("tz"), TOWARDS_MINUS_INF("tmi"),
    TOWARDS_PLUS_INF("tpi"), NEAREST_EVEN("ne"),
    NEAREST_TIE("nt"), NORMAL("n");

    private final String s;

    RoundType(String s) {
        this.s = s;
    }
}
