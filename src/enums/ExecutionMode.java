package enums;

public enum ExecutionMode {
    RUN("run"), DEBUG("debug");
    private final String s;

    ExecutionMode(String s) {
        this.s = s;
    }
}