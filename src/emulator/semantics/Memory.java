package emulator.semantics;

import pojos.BitVec;
import utils.Arithmetic;
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

       public static void set(BitVec address, BitVec value) {
        //DBDriver.updateMemoryDocument(SysUtils.getAddressValue(address.getSym()), value.getSym());
    }

    public void put(BitVec address, BitVec value) {
        memory.put(address.toString().trim(), value);
    }

    public static BitVec get(BitVec address) {
//        String key = address.toString().trim();
//        return memory.containsKey(key) ? memory.get(key) : new BitVec(0);
        return null;
    }

    public static BitVec get(String address) { //TODO: 2023
        //String findRes = SysUtils.getAddressFull(DBDriver.getValue(SysUtils.getAddressValue(address))); //hex value
        //  System.out.println(findRes);
        //return findRes.matches("-?[0-9a-fA-F]+") ? Arithmetic.fromHexStr(findRes) : new BitVec(findRes);
        //return memory.containsKey(key) ? memory.get(key) : new BitVec(0);
        return null;
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