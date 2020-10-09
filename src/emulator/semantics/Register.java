package emulator.semantics;

import executor.Configs;
import pojos.BitVec;
import utils.Arithmetic;
import utils.Mapping;
import utils.MyStr;

import java.util.*;

public class Register {
    public HashMap<Character, BitVec> regs;
    public int length;

    public Register(int length, BitSet r0, BitSet r1, BitSet r2, BitSet r3, BitSet r4, BitSet r5, BitSet r6,
                    BitSet r7, BitSet r8, BitSet r9, BitSet r10, BitSet r11, BitSet r12, BitSet sp, BitSet lr, BitSet pc) {
        this.length = length;
        regs = new HashMap<>();
        regs.put('0', new BitVec("r0", r0));
        regs.put('1', new BitVec("r1", r1));
        regs.put('2', new BitVec("r2", r2));
        regs.put('3', new BitVec("r3", r3));
        regs.put('4', new BitVec("r4", r4));
        regs.put('5', new BitVec("r5", r5));
        regs.put('6', new BitVec("r6", r6));
        regs.put('7', new BitVec("r7", r7));
        regs.put('8', new BitVec("r8", r8));
        regs.put('9', new BitVec("r9", r9));
        regs.put('x', new BitVec("r10", r10));
        regs.put('e', new BitVec("r11", r11));
        regs.put('t', new BitVec("r12", r12));
        regs.put('s', new BitVec("#xff000000", sp)); //#xfffefd70
        regs.put('l', new BitVec("lr", lr));
        regs.put('p', new BitVec("pc", pc));

        Mapping.regStrToChar.put("r0", '0');
        Mapping.regStrToChar.put("r1", '1');
        Mapping.regStrToChar.put("r2", '2');
        Mapping.regStrToChar.put("r3", '3');
        Mapping.regStrToChar.put("r4", '4');
        Mapping.regStrToChar.put("r5", '5');
        Mapping.regStrToChar.put("r6", '6');
        Mapping.regStrToChar.put("r7", '7');
        Mapping.regStrToChar.put("r8", '8');
        Mapping.regStrToChar.put("r9", '9');
        Mapping.regStrToChar.put("r10", 'x');
        Mapping.regStrToChar.put("r11", 'e');
        Mapping.regStrToChar.put("r12", 't');
        Mapping.regStrToChar.put("sp", 's');
        Mapping.regStrToChar.put("lr", 'l');
        Mapping.regStrToChar.put("pc", 'p');

        for (String key : Mapping.regStrToChar.keySet()) {
            Mapping.regCharToStr.put(Mapping.regStrToChar.get(key), key);
        }
    }

    public String getValue(char r) {
        if (!regs.containsKey(r)) return null;
        StringBuilder sb = new StringBuilder();
        String result = Arithmetic.bitsetToStr(regs.get(r).getVal());
        for (int i = 0; i < length - result.length(); i++)
            sb.append("0");
        sb.append(result);
        return sb.toString();
    }

    public String getFormula(char r) {
        return regs.get(r).getSym();
    }

    public String toString() {
        MyStr s = new MyStr("+ Register (" + length + "-bit):\n");
        SortedSet<Character> keys = new TreeSet<>(regs.keySet());
        for (Character key : keys) {
            String val = getValue(key);
            long intValue = Arithmetic.bitSetToLong(Objects.requireNonNull(regs.get(key).getVal()));

            StringBuilder intStr = new StringBuilder(String.valueOf(intValue));
            int remainingLen = (int) Math.round(Math.log10(Math.pow(2, Configs.architecture))) - intStr.length();
            for (int i = 0; i < remainingLen; i++) intStr.insert(0, "0");

            StringBuilder hexStr = new StringBuilder(Arithmetic.intToHex(intValue));
            remainingLen = Configs.architecture / 4 - hexStr.length();
            for (int i = 0; i < remainingLen; i++) hexStr.insert(0, "0");

            String symbolicValue = regs.get(key).getSym();
            s.append("\t- ", Mapping.regCharToStr.get(key).toUpperCase(), "\t: ", val, " ",
                    "0x" + hexStr.toString().toUpperCase(), " (", intValue, ") | " + symbolicValue + "\n");
        }
        return s.value();
    }

    public BitVec get(char r) {
        if (!regs.containsKey(r)) return null;
        return regs.get(r);
    }

    public void set(char r, BitVec bitSet) {
        regs.put(r, bitSet);
    }
}
