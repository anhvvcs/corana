package pojos;

public class BitBool {
    private String sym;
    private Boolean isConcreteValue;

    public BitBool(String sym, Boolean isConcreteValue) {
        this.sym = sym;
        this.isConcreteValue = isConcreteValue;
    }

    public BitBool(Boolean isConcreteValue) {
        this.sym = "";
        this.isConcreteValue = isConcreteValue;
    }

    public void setSymbolicValue(String symbolicValue) {
        this.sym = symbolicValue;
    }

    public void setConcreteValue(Boolean concreteValue) {
        isConcreteValue = concreteValue;
    }

    public String getSym() {
        return sym;
    }

    public Boolean isConcreteValue() {
        return isConcreteValue;
    }


}
