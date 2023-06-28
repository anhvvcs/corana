package utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SysUtils {
    private static int count = 0;

    public static String execCmd(String cmd) {
        try {
            List<String> list = Arrays.asList(cmd.split(" "));
            ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", cmd);
            Process process = processBuilder.start();
            StringBuilder output = new StringBuilder();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line + "\n");
            }
            int exitVal = process.waitFor();
            if (exitVal != 0) return null;
            return output.toString();
        } catch (Exception e) {
            //e.printStackTrace();
        } catch (Error e) {
            //
        }
        return null;
    }

    public static Integer normalizeNumInParam(String s) {
        String raw = s.replace("#", "");
        //return raw.contains("0x") ? (int) Arithmetic.hexToInt(raw) : Integer.parseInt(raw);
        return (int) Arithmetic.hexToInt(raw);
    }

    public static String normalizeNumInHex(String s) {
        if (s.matches("[01][01]+") || s.matches("^(0x|0X|#x)?[a-fA-F0-9]+$")) {
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
       return s;
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
        if (hexStr.contains("SYM") || hexStr.contains("POINTER")) return hexStr;
        if (hexStr.charAt(0) == 'x') hexStr = hexStr.substring(1, hexStr.length());
        return hexStr.replace("#", "")
                .replaceFirst("^0+(?!$)", "");
    }
    public static String getAddressFull(String hexValue) {
        if (hexValue.contains("SYM")) return hexValue;
        if (hexValue.charAt(0) == 'x' && !hexValue.contains("#")) {
            hexValue = "#" + hexValue;
        }
        return hexValue;
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
