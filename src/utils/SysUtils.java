package utils;

import executor.Configs;
import pojos.BitVec;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

public class SysUtils {
    private static int count = 0;

    public static String execCmd(String cmd) {
        try {
            Process process = Runtime.getRuntime().exec(cmd);
            StringBuilder output = new StringBuilder();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();
            while (line != null) {
                output.append(line).append("\n");
                line = reader.readLine();
            }
            int exitVal = process.waitFor();
            if (exitVal != 0) return null;
            return output.toString();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Integer normalizeNumInParam(String s) {
        String raw = s.replace("#", "");
        //return raw.contains("0x") ? (int) Arithmetic.hexToInt(raw) : Integer.parseInt(raw);
        return (int) Arithmetic.hexToInt(raw);
    }

    public static String normalizeNumInHex(String s) {
        String raw = s.replace("#", "");
        raw = (raw.charAt(0) == 'x') ? raw.replaceFirst("x", "") : raw;
        while (raw.length() < 8) {
            raw = "0" + raw;
        }
        while (raw.length() > 8) {
            raw = raw.substring(1);
        }
        String symbolicValue = "#x" + raw;
        return symbolicValue;
    }

    public static String normalizeRegName(String n) {
        String newN = n.contains("-") ? n.replace("-", "") : n;
        if (newN.equals("ip")) return "r12";
        if (newN.equals("fp")) return "r11";
        if (newN.equals("sl")) return "r10";
        if (newN.equals("sb")) return "r9";
        return newN;
    }

    public static byte[] concatByteArray(final byte[] a1, byte[] a2) {
        byte[] joinedArray = Arrays.copyOf(a1, a1.length + a2.length);
        System.arraycopy(a2, 0, joinedArray, a1.length, a2.length);
        return joinedArray;
    }

    public static String getAddressValue(String hexStr) {
        if (hexStr.charAt(0) == 'x') hexStr = hexStr.substring(1, hexStr.length());
        return hexStr.replace("#", "")
                .replaceFirst("^0+(?!$)", "");
    }

    public static String addSymVar() {
        String var = "sym" + count;
        Mapping.intToSymVariable.put(count, var);
        count = count + 1;
        return var;
    }

    public static int getCountSyms() {
        return count;
    }

    public static String getNextAdress(String address) {
        return Z3Solver.solveBitVecArithmetic(String.format("(bvadd %s #x00000004)", address));
    }

    public static List<Character> getPrintfFormat(String fmt) {
        List<Character> result = new ArrayList<>();
        for (char s : fmt.toCharArray()) {
            if (s == '%') {
                result.add(fmt.charAt(fmt.indexOf(s) + 1));
            }
        }
        return result;
    }
}
