package emulator.semantics;

import utils.Mapping;

import java.lang.reflect.Field;
import java.util.HashMap;

public class EnvData {
    public String eval;
    public HashMap<String, String> registersModel = new HashMap<>();
    public HashMap<String, Boolean> flagsModel = new HashMap<>();

    public EnvData() {
        eval = null;
        for (String k : Mapping.regStrToChar.keySet()) {
            registersModel.put(k, "#x00000000");
        }
        Field[] fields = Flags.class.getFields();
        for (Field f : fields) {
            flagsModel.put(f.getName().toLowerCase(), true);
        }
    }

    public EnvData(EnvData envData) {
        this.eval = envData.eval;
        this.registersModel = envData.registersModel;
        this.flagsModel = envData.flagsModel;
    }

    @Override
    public String toString() {
        return "\t:: EnvModel " +
                "eval='" + eval + '\'' +
                ", " + registersModel +
                ", " + flagsModel;
    }
}
