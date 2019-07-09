package enums;

public enum ChangeMode {
    FAULTMASK("faultmask"), PRIMASK("primask");
    private String s;

    ChangeMode(String s) {
        this.s = s;
    }
}
