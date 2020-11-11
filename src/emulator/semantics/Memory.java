package emulator.semantics;

import executor.Configs;
import executor.DBDriver;
import pojos.BitVec;
import utils.Arithmetic;
import utils.Logs;
import utils.MyStr;
import utils.SysUtils;

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

    public static void loadMemory(String filePath) {
        String[] nparts = filePath.split("[\\/]");
        String dbname = nparts[nparts.length - 1];
        String colname = "col_" + dbname.substring(0, 6);
        DBDriver.startConnection(colname);
        Logs.infoLn(" + Parsing " + filePath + " ...");
        String disassembleCmd = "arm-linux-gnueabi-objdump -D -S ";
        String execResult = SysUtils.execCmd(disassembleCmd + filePath);
        if (execResult == null) {
            Logs.infoLn("-> Parsing binary file error !");
        }
        String[] resultLines = execResult.split("\n");
        HashMap<String, BitVec> mem = new HashMap<>();
        String funcLabel;
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
                            if (parts[1].contains("<")) {
                                funcLabel = parts[1].contains("+") ? parts[1].substring(parts[1].indexOf('<') + 1, parts[1].indexOf('+')) : parts[1].substring(parts[1].indexOf('<') + 1, parts[1].indexOf('>'));
                                DBDriver.addMemoryDocument(label, contents[1], funcLabel);
                            } else {
                                DBDriver.addMemoryDocument(label, contents[1]);
                            }

                    }
                }
            }
        }
        // set up initial contents in the stack
        setupStack();
        Logs.infoLn();
    }

    public static void setupStack() {
        // Initialize the value at the stack's top as 0x01
        // argc = 1; args.length = 0
        String currentAddress = Configs.topStack;
        DBDriver.addMemoryDocument(currentAddress, "#x00000001");

        currentAddress = SysUtils.getNextAdress(currentAddress);
        DBDriver.addMemoryDocument(currentAddress, SysUtils.addSymVar());

        currentAddress = SysUtils.getNextAdress(currentAddress);
        DBDriver.addMemoryDocument(currentAddress, "#x00000000");

        for (int i = 0; i < 17; i++) {
            currentAddress = SysUtils.getNextAdress(currentAddress);
            DBDriver.addMemoryDocument(currentAddress, SysUtils.addSymVar());
        }
        currentAddress = SysUtils.getNextAdress(currentAddress);
        DBDriver.addMemoryDocument(currentAddress, "#x00000000");
    }

    public static BitVec get(BitVec address) {
        //Address is in hex
        String key = SysUtils.getAddressValue(address.getSym().trim());
        String findRes = DBDriver.getValue(key); //hex value
        return findRes.matches("-?[0-9a-fA-F]+") ? Arithmetic.fromHexStr(findRes) : new BitVec(findRes);
        //return memory.containsKey(key) ? memory.get(key) : new BitVec(0);
    }
    /*
        Get the value at an address (address can contain #x)
     */
    public static BitVec get(String address) {
        String findRes = DBDriver.getValue(SysUtils.getAddressValue(address)); //hex value
      //  System.out.println(findRes);
        return findRes.matches("-?[0-9a-fA-F]+") ? Arithmetic.fromHexStr(findRes) : new BitVec(findRes);
        //return memory.containsKey(key) ? memory.get(key) : new BitVec(0);
    }

    public static void set(BitVec address, BitVec value) {
        DBDriver.updateMemoryDocument(address.getSym(), value.getSym());
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