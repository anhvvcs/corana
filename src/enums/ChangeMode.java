package enums;

public enum ChangeMode {
    FAULTMASK("faultmask"), PRIMASK("primask");
    private final String s;

    ChangeMode(String s) {
        this.s = s;
    }
}
