package executor;

import capstone.Capstone;
import elfutils.Elf;
import elfutils.SectionHeader;
import external.handler.APIStub;
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
import java.util.stream.Collectors;

public class BinParser {
    private static HashMap<String, String> symbolTable; // <hex address, symbol name>
    public static long _init = 0;
    public static long _start = 0;
    public static long main = 0;
    public static long end = 0;

    public static long get_start() {
        return main;
    }

    public static HashMap<String, String> getSymbolTable() {
        return symbolTable;
    }

    public static ArrayList<AsmNode> parseBySection(String inp) {
        Logs.infoLn(" + Analyzing " + inp + " ...");
        try {
            // Load symbol table init and start address
            // findEntryPoint(inp);
            loadSymbolTable(inp);

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

    private static void loadSymbolTable(String binpath) {
        String objCmd = "arm-none-eabi-objdump -t " + binpath;
        String exRes = SysUtils.execCmd(objCmd);
        String[] resultLines = exRes.split("\n");
        long main_size;
        HashMap<String, String> symTable = new HashMap<>();
        for (String line : resultLines) {
            String[] contents = line.split("\\s+");
            if (contents.length >= 6) {
                if (!symTable.containsKey(contents[0])) {
                    symTable.put(contents[0], contents[contents.length-1].trim()); // e.g. 00014fa0 printf
                } else {
                    if (!contents[5].contains("_") && !contents[5].equals(".hidden")) {
                        symTable.put(contents[0], contents[5].trim());
                    }
                }
                // Find the address of init and _start
                if (contents[5].trim().equals(".init")) {
                    _init = Arithmetic.hexToInt(contents[0]);
                } else if (contents[5].trim().equals("_start")) {
                    _start = Arithmetic.hexToInt(contents[0]);
                } else if (contents[5].trim().equals("main")) {
                    main = Arithmetic.hexToInt(contents[0]);
                    main_size = Arithmetic.hexToInt(contents[4]);
                    end = main + main_size - 4;
                }
            }
        }
        symbolTable = symTable;
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

    public static List<String> getExternalFunctions(String binpath) {
        String objCmd = "nm -g -C " + binpath;
        String exRes = SysUtils.execCmd(objCmd);
        List<String> externalFuncs = new ArrayList<>();
        String[] resultLines = exRes.split("\n");

        for (String line : resultLines) {
            String[] ls = line.split(" ");
            if (ls.length > 2) {
                externalFuncs.add(ls[2]);
            }
        }
        List<String> alllist = Arrays.stream(APIStub.class.getMethods()).map(s -> s.getName()).collect(Collectors.toList());
        List<String> res = new ArrayList<>();
        for (String func : externalFuncs) {
            if (alllist.contains(func)) {
                res.add(func);
            }
        }
        return res;
    }

    public static List<String> getInternalSymbols(String binpath) {
        String objCmd = "nm -g -C " + binpath;
        String exRes = SysUtils.execCmd(objCmd);
        List<String> externalFuncs = new ArrayList<>();
        String[] resultLines = exRes.split("\n");

        for (String line : resultLines) {
            String[] ls = line.split(" ");
            if (ls.length > 2) {
                externalFuncs.add(ls[2]);
            }
        }

        objCmd = "nm -C " + binpath;
        exRes = SysUtils.execCmd(objCmd);
        List<String> inFuncs = new ArrayList<>();
        resultLines = exRes.split("\n");

        for (String line : resultLines) {
            String[] ls = line.split(" ");
            if (ls.length > 2) {
                if (!externalFuncs.contains(ls[2]) && !ls[2].contains("$"))
                    inFuncs.add(ls[2]);
            }
        }
        return inFuncs;
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
