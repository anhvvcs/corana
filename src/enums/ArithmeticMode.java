package enums;

public enum ArithmeticMode {
    BINARY("binary"), FLOATING_POINT("floatingPoint");
    private final String s;

    ArithmeticMode(String s) {
        this.s = s;
    }
}
