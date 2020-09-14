package emulator.semantics;

import com.mongodb.DBCollection;
import executor.BinParser;
import executor.DBDriver;
import pojos.BitVec;
import utils.Arithmetic;
import utils.Logs;
import utils.MyStr;
import utils.SysUtils;

import java.util.ArrayList;
import java.util.HashMap;

public class Memory {
    private HashMap<String, BitVec> memory;
    private int length;

    public Memory(int length) {
        this.memory = new HashMap<>();
        this.length = length;
    }

    public void put(BitVec address, BitVec value) {
        memory.put(address.toString().trim(), value);
    }

    public void put(String address, BitVec value) {
        memory.put(address.trim(), value);
    }

    public static void loadMemory(String filePath, String name) {

        if (!DBDriver.startConnection("col_" + name.substring(0, 6))) {
            return;
        }

        Logs.infoLn(" + Parsing " + filePath + " ...");
        String disassembleCmd = "arm-none-eabi-objdump -D -b binary -marm ";
        String execResult = SysUtils.execCmd(disassembleCmd + filePath);
        if (execResult == null) {
            Logs.infoLn("-> Parsing binary file error !");
        }
        String[] resultLines = execResult.split("\n");
        HashMap<String, BitVec> mem = new HashMap<>();
        for (String line : resultLines) {
            line = line.trim().replaceAll(" +", " ");
            if (line.contains(";")) {
                line = line.split("\\;")[0];
            }
            if (line.contains("\t")) {
                line = line.replace("\t", " ");
                line = line.replaceAll(" +", " ");
                String[] parts = line.split("\\:");
                String label = parts[0];
                String[] contents = parts[1].split("\\s+");
                if (contents.length > 2) {
                    if (contents[1].matches("-?[0-9a-fA-F]+")) {
                        DBDriver.addDocument(label, contents[1]);
                    }
                }
            }
        }
        Logs.infoLn();
    }

    public static BitVec get(BitVec address) {
        //Address is in hex
        String key = address.getSym().trim();
        String findRes = DBDriver.getValue(key); //hex value
        return findRes.matches("-?[0-9a-fA-F]+") ? Arithmetic.fromHexStr(findRes) : new BitVec(0);
        //return memory.containsKey(key) ? memory.get(key) : new BitVec(0);
    }

    public static BitVec get(String address) {
        String findRes = DBDriver.getValue(address); //hex value
      //  System.out.println(findRes);
        return findRes.matches("-?[0-9a-fA-F]+") ? Arithmetic.fromHexStr(findRes) : new BitVec(0);
        //return memory.containsKey(key) ? memory.get(key) : new BitVec(0);
    }

    @Override
    public String toString() {
        MyStr result = new MyStr("+ Memory:\n");
        Logs.infoLn(memory.size());
        for (String k : memory.keySet()) {
            result.append("\t- ", k, " : ", memory.get(k).toString(), "\n");
        }
        return result.value();
    }
}