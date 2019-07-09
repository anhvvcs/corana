package emulator.semantics;

import pojos.BitVec;
import utils.Logs;
import utils.MyStr;

import java.util.HashMap;

public class Memory {
    public HashMap<String, BitVec> memory;
    private int length;

    public Memory(int length) {
        memory = new HashMap<>();
        this.length = length;
    }

    public void put(BitVec address, BitVec value) {
        memory.put(address.toString().trim(), value);
    }

    public BitVec get(BitVec address) {
        String key = address.toString().trim();
        return memory.containsKey(key) ? memory.get(key) : new BitVec(0);
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