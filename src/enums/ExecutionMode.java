package enums;

public enum ExecutionMode {
    RUN("run"), DEBUG("debug");
    private String s;

    ExecutionMode(String s) {
        this.s = s;
    }
}