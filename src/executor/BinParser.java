package executor;

import capstone.Capstone;
import elfutils.Elf;
import elfutils.SectionHeader;
import pojos.AsmNode;
import utils.Arithmetic;
import utils.Logs;
import utils.Mapping;
import utils.SysUtils;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class BinParser {
    private static HashMap<String, String> symbolTable; // <hex address, symbol name>
    public static long _init = 0;
    public static long _start = 0;

    public static long get_start() {
        return _start;
    }

    public static ArrayList<AsmNode> parseBySection(String inp) {
        Logs.infoLn(" + Analyzing " + inp + " ...");
        try {
            // Load symbol table init and start address
            findEntryPoint(inp);

            ArrayList<AsmNode> totalNodes = new ArrayList<>();
            Elf e = new Elf(new File(inp));
            SectionHeader[] sections = e.sectionHeaders;
            int beginCodeSection = -1;
            int endCodeSection = -1;
            for (int i = 0; i < sections.length; i++) {
                SectionHeader sh = sections[i];
                if (sh.getName().equals(".init")) beginCodeSection = i;
                if (sh.getName().equals(".fini")) endCodeSection = i;
            }
            for (int i = beginCodeSection; i <= endCodeSection; i++) {
                SectionHeader sh = sections[i];
                totalNodes.addAll(parseSection(e, sh));
            }
            return totalNodes;
        } catch (Exception ex) {
            Logs.infoLn("-> Cannot read header section. File might be corrupted.");
            return null;
        }
    }

    private static HashMap<String, String> loadSymbolTable(String binpath) {
        String objCmd = "arm-none-eabi-objdump -t " + binpath;
        String exRes = SysUtils.execCmd(objCmd);
        String[] resultLines = exRes.split("\n");
        HashMap<String, String> symTable = new HashMap<>();
        for (String line : resultLines) {
            String[] contents = line.split("\\s+");
            if (contents.length > 5 && !symTable.containsKey(contents[0])) {
                //symTable.put(contents[0], contents[5].trim());
                // Find the address of init and _start
                if (contents[5].trim().equals(".init")) {
                    _init = Arithmetic.hexToInt(contents[0]);
                } else if (contents[5].trim().equals("_start")) {
                    _start = Arithmetic.hexToInt(contents[0]);
                }
            }
        }
        return symTable;
    }

    private static ArrayList<AsmNode> parseSection(Elf e, SectionHeader sh) throws IOException {
        byte[] totalByteArray = new byte[0];
        ByteBuffer buff = e.getSection(sh);
        byte[] arr = new byte[buff.remaining()];
        buff.get(arr);
        totalByteArray = SysUtils.concatByteArray(totalByteArray, arr);
        return parse(totalByteArray, sh.virtualAddress);
    }

    public static ArrayList<AsmNode> parse(String inp) {
        Logs.infoLn(" + Analyzing " + inp + " ...");
        try {
            Elf e = new Elf(new File(inp));
            SectionHeader[] sections = e.sectionHeaders;
            int beginCodeSection = -1;
            int endCodeSection = -1;
            for (int i = 0; i < sections.length; i++) {
                SectionHeader sh = sections[i];
                if (sh.getName().equals(".init")) beginCodeSection = i;
                if (sh.getName().equals(".fini")) endCodeSection = i;
            }
            byte[] totalByteArray = new byte[0];
            for (int i = beginCodeSection; i <= endCodeSection; i++) {
                SectionHeader sh = sections[i];
                ByteBuffer buff = e.getSection(sh);
                byte[] arr = new byte[buff.remaining()];
                buff.get(arr);
                totalByteArray = SysUtils.concatByteArray(totalByteArray, arr);
            }
            // Set init and start address

            return parse(totalByteArray, 0);
        } catch (Exception ex) {
            Logs.infoLn("-> Cannot read header section. File might be corrupted.");
            return null;
        }
    }

    public static ArrayList<AsmNode> parse(byte[] bytes, long virtualAddress) {
        ArrayList<AsmNode> asmNodes = new ArrayList<>();
        long label = virtualAddress;
        int instrSize = 4;
        for (int i = 0; (i + 3) < bytes.length; i += instrSize) {
            byte[] bs = {bytes[i], bytes[i + 1], bytes[i + 2], bytes[i + 3]};
            Capstone cs = new Capstone(Capstone.CS_ARCH_ARM, Capstone.CS_MODE_ARM);
            Capstone.CsInsn[] insn = cs.disasm(bs, label);
            // If the instruction is unidentified
            if (insn.length == 0) {
                label += instrSize;
            }

            // Else encode capstone inst as an asmNode
            for (Capstone.CsInsn csInsn : insn) {
                String opcode = csInsn.mnemonic;
                String condSuffix = "";
                if (opcode.length() >= 3) {
                    String temp = opcode.substring(opcode.length() - 2);
                    if (Mapping.condStrToChar.get(temp.toUpperCase()) != null) {
                        condSuffix = temp;
                        opcode = opcode.substring(0, opcode.length() - 2);
                    }
                }
                boolean updateFlag = opcode.endsWith("s");
                if (updateFlag) opcode = opcode.substring(0, opcode.length() - 1);

                StringBuilder params = new StringBuilder();
                String[] paramsArr = csInsn.opStr.split("\\,");
                for (int p = 0; p < paramsArr.length; p++) {
                    params.append(paramsArr[p].trim());
                    if (p < paramsArr.length - 1) {
                        params.append(",");
                    }
                }
                AsmNode n = new AsmNode(String.valueOf(csInsn.address), opcode, condSuffix, params.toString(), updateFlag, Arithmetic.intToHex(csInsn.address));
                asmNodes.add(n);
                label += instrSize;
            }
        }
        return asmNodes;
    }

    public static ArrayList<AsmNode> expand(ArrayList<AsmNode> asmNodes) {
        ArrayList<AsmNode> expandedNodes = new ArrayList<>();
        for (AsmNode an : asmNodes) {
            String label = an.getLabel();
            String opcode = an.getOpcode();
            String condSuffix = an.getCondSuffix();
            String address = an.getAddress();
            List<String> branches = Arrays.asList("b", "bx", "bl", "blx");
            if (label != null && opcode != null && condSuffix != null && !condSuffix.equals("") && !branches.contains(opcode)) {
                AsmNode originNode = new AsmNode(an);
                originNode.setCondSuffix("");
                String newOriginLabel = Integer.parseInt(label) + "-2";
                originNode.setLabel(newOriginLabel);
                expandedNodes.add(originNode);
                expandedNodes.add(new AsmNode(label, "b", condSuffix, newOriginLabel, false, address));
                expandedNodes.add(new AsmNode(Integer.parseInt(label) + "+2", "b", "",
                        "0x" + Arithmetic.intToHex(Integer.parseInt(label) + 4), false, address + "+2"));
            } else {
                expandedNodes.add(an);
            }
        }
        return expandedNodes;
    }

    private static void findEntryPoint(String binpath) {
        String objCmd = "readelf -h " + binpath;
        String exRes = SysUtils.execCmd(objCmd);
        String[] resultLines = exRes.split("\n");
        String hexstart = "000000";
        for (String line : resultLines) {
            if (line.contains("Entry point address")) {
                hexstart = line.split(":")[1].trim();
            }
        }
        _start = Arithmetic.hexToInt(hexstart);
    }

    public static ArrayList<AsmNode> parseObjDump(String inp) {
        Logs.infoLn(" + Parsing " + inp + " ...");
        String disassembleCmd = "arm-none-eabi-objdump -D -b binary -marm ";
        String execResult = SysUtils.execCmd(disassembleCmd + inp);
        if (execResult == null) {
            Logs.infoLn("-> Parsing binary file error !");
            return null;
        }
        String[] resultLines = execResult.split("\n");
        ArrayList<AsmNode> asmNodes = new ArrayList<>();
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
                if (contents.length > 3) {
                    String opcode = contents[2];
                    String condSuffix = "";
                    if (opcode.length() >= 3) {
                        String temp = opcode.substring(opcode.length() - 2);
                        if (Mapping.condStrToChar.get(temp.toUpperCase()) != null) {
                            condSuffix = temp;
                            opcode = opcode.substring(0, opcode.length() - 2);
                        }
                    }
                    StringBuilder params = new StringBuilder();
                    for (int i = 3; i < contents.length; i++) {
                        params.append(contents[i]).append(" ");
                    }
                    boolean updateFlag = opcode.endsWith("s");
                    if (updateFlag) {
                        opcode = opcode.substring(0, opcode.length() - 1);
                    }
                    AsmNode n = new AsmNode(label, opcode, condSuffix, params.toString().trim(), updateFlag);
                    asmNodes.add(n);
                }
            }
        }
        return asmNodes;
    }
}
