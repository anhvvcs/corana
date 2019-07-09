package emulator.semantics;

public class EnvModel {
    public String label;
    public String prevLabel;
    public String pathCondition;
    public EnvData envData;

    public EnvModel(String pathCondition) {
        this.pathCondition = pathCondition;
        envData = new EnvData();
    }

    public EnvModel(String label, String pathCondition) {
        this.label = label;
        this.pathCondition = pathCondition;
        envData = new EnvData();
    }

    public EnvModel(String pathCondition, EnvData envData) {
        this.pathCondition = pathCondition;
        this.envData = envData;
    }

    public EnvModel(EnvModel envModel) {
        this.label = envModel.label;
        this.prevLabel = envModel.prevLabel;
        this.pathCondition = envModel.pathCondition;
        this.envData = new EnvData(envModel.envData);
    }
}
