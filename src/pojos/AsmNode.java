package pojos;

public class AsmNode {
    private String label;
    private String opcode;
    private String condSuffix;
    private String params;
    private boolean isUpdateFlag;
    private String address;

    public AsmNode(String label, String opcode, String condSuffix, String params, boolean isUpdateFlag) {
        this.label = label;
        this.opcode = opcode;
        this.condSuffix = condSuffix;
        this.params = params;
        this.isUpdateFlag = isUpdateFlag;
    }

    public AsmNode(String label, String opcode, String condSuffix, String params, boolean isUpdateFlag, String address) {
        this.label = label;
        this.opcode = opcode;
        this.condSuffix = condSuffix;
        this.params = params;
        this.isUpdateFlag = isUpdateFlag;
        this.address = address;
    }

    public AsmNode(AsmNode node) {
        this.label = node.label;
        this.opcode = node.opcode;
        this.condSuffix = node.condSuffix;
        this.params = node.params;
        this.isUpdateFlag = node.isUpdateFlag;
    }

    @Override
    public String toString() {
        return "AsmNode{" +
                "label='" + label + '\'' +
                ", opcode='" + opcode + '\'' +
                ", condSuffix='" + condSuffix + '\'' +
                ", params='" + params + '\'' +
                ", isUpdateFlag=" + isUpdateFlag +
                '}';
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getOpcode() {
        return opcode;
    }

    public void setOpcode(String opcode) {
        this.opcode = opcode;
    }
    public String getAddress() {
        return this.address;
    }
    public void setAddress(String add) {
        this.address = add;
    }

    public String getCondSuffix() {
        return condSuffix;
    }

    public void setCondSuffix(String condSuffix) {
        this.condSuffix = condSuffix;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public boolean isUpdateFlag() {
        return isUpdateFlag;
    }

    public void setUpdateFlag(boolean updateFlag) {
        isUpdateFlag = updateFlag;
    }
}
