package emulator.semantics;

import com.sun.jna.NativeLong;
import com.sun.jna.Structure;
import com.sun.jna.ptr.*;
import executor.Configs;
import executor.DBDriver;
import pojos.BitVec;
import utils.Arithmetic;
import utils.Logs;
import utils.MyStr;
import utils.SysUtils;

import java.nio.Buffer;
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
        DBDriver.addMemoryDocument(address.getSym(), value.getSym());
    }

    public void put(String address, BitVec value) {
        memory.put(address.trim(), value);
    }

    public static void loadMemory() {
        DBDriver.startConnection("tmp");
    }

    public static void loadMemory(String filePath) {
        String[] nparts = filePath.split("[\\/]");
        String dbname = nparts[nparts.length - 1];
        String colname = dbname.length() < 6 ? "col_" + dbname :  "col_" + dbname.substring(0, 6);
        DBDriver.startConnection(colname);
        Logs.infoLn(" + Parsing " + filePath + " ...");
        String disassembleCmd = "arm-none-eabi-objdump -D -S ";
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
                if (contents.length >= 2) {
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
        //Logs.infoLn(memory.size());
        if (memory != null && memory.size() > 0) {
            for (String k : memory.keySet()) {
                result.append("\t- ", k, " : ", memory.get(k).toString(), "\n");
            }
        }
        return result.value();
    }

    /*
     SETTERS
     */
    public void setTextReference(BitVec atAdd, String text) {
    }
    public void setStringReference(BitVec atAdd, String text) {
    }
    public void setPointer(BitVec add, Object p) {
        //int val = ((IntByReference) p).getValue();
        //long val =
        //memory.put(add.getSym(), new BitVec(val));
    }

    public void setByte(BitVec add, byte val) {}

    public void setInt(BitVec add, int val) {}

    public void setIntReference(BitVec atAdd, int val) {
        BitVec value = new BitVec(val);
        DBDriver.updateMemoryDocument(atAdd.getSym(), value.getSym());
    }
    public void setNativeLong(BitVec atAdd, NativeLong val) {

    }
    public void setNativeLongReference(BitVec atAdd, NativeLong val) {

    }
    public void setShortReference(BitVec atAdd, short val) {
    }
    public void setLongReference(BitVec atAdd, long val) {

    }
    public void setArray(BitVec add, int size, Object[] arr) {

    }

    public void setByteArray(BitVec add, int size, byte[] arr){
        int intSize = Configs.getIntSize(); // bytes
        for (int i = 0; i < size; i++) {
            String word = add.getSym();
            BitVec value = new BitVec(arr[i]);
            DBDriver.updateMemoryDocument(word, value.getSym());
            add = add.add(intSize);
        }
    }

    public void setIntArray(BitVec add, int size, int[] arr){
        int intSize = Configs.getIntSize(); // bytes
        for (int i = 0; i < size; i++) {
            String word = add.getSym();
            BitVec value = new BitVec(arr[i]);
            DBDriver.updateMemoryDocument(word, value.getSym());
            add = add.add(intSize);
        }
    }
    public void setShortArray(BitVec add, int size, short[] arr){

    }

    public void setBuffer(BitVec add, int size, char[] buf) {
        int intSize = Configs.getIntSize(); // bytes
        for (int i = 0; i < size; i++) {
            String word = add.getSym();
            BitVec value = new BitVec((int) buf[i]);
            DBDriver.updateMemoryDocument(word, value.getSym());
            add = add.add(intSize);
        }
    }

    /*
     GETTERS
     */
    public String getTextFromReference(BitVec atAddress) {
        String text = "";
        boolean flag = true;
        String word = atAddress.getSym();
        while (flag) {
            String memValue = DBDriver.getValue(word);
            String nextText = HexToASCII(memValue);
            if (DBDriver.getValue(word).contains("00") || memValue.length() < 8) flag = false;
            text += nextText;
            word = Arithmetic.intToHex(Arithmetic.hexToInt(word) + Configs.wordSize);
        }
        return text;
    }
    private String HexToASCII(String memstr) {
        StringBuilder result = new StringBuilder();
        for (int i = memstr.length() - 1; i > 0; i -= 2) {
            String hexStr = memstr.substring(i-1, i+1);
            if (hexStr.equals("00")) break;
            char c = (char) Integer.parseInt(hexStr, 16);
            result.append(c);
        }
        return result.toString();
    }

    public String getStringFromReference(BitVec add) {
        return getTextFromReference(add);
    }
    public Object[] getArray(BitVec atAddress, int size) {
        return null;
    }

    public byte[] getByteArray(BitVec atAddress, int size) {
        byte[] arr = new byte[size];
        int intSize = Configs.getIntSize(); // bytes
        for (int i = 0; i < size; i++) {
            String word = atAddress.getSym();
            String memValue = DBDriver.getValue(word);
            arr[i] = (byte) Arithmetic.hexToInt(memValue);
            atAddress = atAddress.add(intSize);
        }
        return arr;
    }

    public short[] getShortArray(BitVec atAddress, int size) {
        return null;
    }

    public int[] getIntArray(BitVec atAddress, int size) {
        int[] arr = new int[size];
        int intSize = Configs.getIntSize(); // bytes
        for (int i = 0; i < size; i++) {
            String word = atAddress.getSym();
            String memValue = DBDriver.getValue(word);
            arr[i] = (int) Arithmetic.hexToInt(memValue);
            atAddress = atAddress.add(intSize);
        }
        return arr;
    }
    public char[] getBuffer(BitVec atAddress, int size) {
        char[] arr = new char[size];
        int intSize = Configs.getIntSize(); // bytes
        for (int i = 0; i < size; i++) {
            String word = atAddress.getSym();
            String memValue = DBDriver.getValue(word);
            arr[i] = (char) Arithmetic.hexToInt(memValue);
            atAddress = atAddress.add(intSize);
        }
        return arr;
    }
    //for 32bit machine
    public BitVec getWordMemoryValue(BitVec atAddress) {
        String word = atAddress.getSym();
        String memValue = DBDriver.getValueOrNull(word);
        if (memValue == null) {
            return new BitVec(SysUtils.addSymVar());
        }
        return Arithmetic.fromHexStr(memValue);
    }

    public LongByReference getPointer(BitVec atAddress) {
        String word = atAddress.getSym();
        String memValue = DBDriver.getValue(word);
//        Pointer ptr = new com.sun.jna.Memory(8);
//        ptr.setLong(0, Arithmetic.hexToInt(memValue));
        return new LongByReference((int) Arithmetic.hexToInt(word));
    }

    public NativeLongByReference getPointerByRef(BitVec atAddress) {
        String word = atAddress.getSym();
        String memValue = DBDriver.getValue(word);
        NativeLongByReference ref = new NativeLongByReference(new NativeLong(Arithmetic.hexToInt(memValue)));
        return ref;
    }

    public IntByReference getIntRef(BitVec atAddress) {
        String word = atAddress.getSym();
        String memValue = DBDriver.getValue(word);
        //String haftWord = memValue.substring(Configs.wordSize-Configs.getIntSize(), Configs.wordSize);
        IntByReference ref = new IntByReference((int) Arithmetic.hexToInt(memValue));
        return ref;
    }
    public int getIntFromReference(BitVec atAddress) {
        String word = atAddress.getSym();
        try {
            String memValue = DBDriver.getValue(word);
            return (int ) Arithmetic.hexToInt(memValue);
        } catch (Exception e) {
            String address = atAddress.getSym();
            String hexStr = address;
            if (address.charAt(0) == '#') {
                hexStr = Arithmetic.intToHex(Arithmetic.hexToInt(address));
            }
            String memValue = memory.get("0x"+hexStr).getSym();
            return (int ) Arithmetic.hexToInt(memValue);
        }

    }

    public NativeLong getNativeLongFromReference(BitVec atAddress) {
        String word = atAddress.getSym();
        String memValue = DBDriver.getValue(word);
        return (NativeLong) new NativeLong(Arithmetic.hexToInt(memValue));
    }

    public NativeLongByReference getNativeLongRef(BitVec atAddress) {
        String word = atAddress.getSym();
        String memValue = DBDriver.getValue(word);
        return new NativeLongByReference(new NativeLong(Arithmetic.hexToInt(memValue)));
    }

    public long getLongFromReference(BitVec atAddress) {
        String word = atAddress.getSym();
        String memValue = DBDriver.getValue(word);
        return (long) Arithmetic.hexToInt(memValue);
    }
    public short getShortFromReference(BitVec atAddress) {
        String word = atAddress.getSym();
        String memValue = DBDriver.getValue(word);
        return (short) Arithmetic.hexToInt(memValue);
    }
    public ShortByReference getShortRef(BitVec atAddress) {
        String word = atAddress.getSym();
        String memValue = DBDriver.getValue(word);
        return new ShortByReference((short) Arithmetic.hexToInt(memValue));
    }
    public FloatByReference getFloatRef(BitVec atAddress) {
        String word = atAddress.getSym();
        String memValue = DBDriver.getValue(word);
        return new FloatByReference((float) Arithmetic.hexToInt(memValue));
    }

    public int getInt(BitVec atAddress) {
        String word = atAddress.getSym();
        return (int) Arithmetic.hexToInt(word);
    }

    public float getFloat(BitVec atAddress) {
        String word = atAddress.getSym();
        return (float) Arithmetic.hexToInt(word);
    }
    public short getShort(BitVec atAddress) {
        String word = atAddress.getSym();
        return (short) Arithmetic.hexToInt(word);
    }
    public byte getByte(BitVec atAddress) {
        String word = atAddress.getSym();
        return (byte) Arithmetic.hexToInt(word);
    }
    public double getDouble(BitVec address) {
        String word = address.getSym();
        return (int) Arithmetic.hexToInt(word);
    }
    public DoubleByReference getDoubleRef(BitVec address) {
        String word = address.getSym();
        return new DoubleByReference((double) Arithmetic.hexToInt(word));
    }
    public void setDouble(BitVec add, double val) {}
    public void setFloat(BitVec add, float val) {}

    public NativeLong getNativeLong(BitVec atAddress) {
        String word = atAddress.getSym();
        return new NativeLong(Arithmetic.hexToInt(word));
    }
    public long getLong(BitVec atAddress) {
        String word = atAddress.getSym();
        return (long) Arithmetic.hexToInt(word);
    }

}