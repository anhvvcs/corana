package executor;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sun.jna.Native;
import emulator.base.Emulator;
import emulator.cortex.*;
import emulator.semantics.EnvModel;
import emulator.semantics.Environment;
import emulator.semantics.Memory;
import enums.Variation;
import external.handler.ExternalCall;
import pojos.AsmNode;
import pojos.BitVec;
import utils.*;


import java.lang.reflect.Type;
import java.util.*;

public class Executor {

    private static HashMap<String, Integer> nodeLabelToIndex = new HashMap<>();
    private static ArrayList<AsmNode> asmNodes = null;
    private static String jumpFrom = null;
    private static String jumpTo = null;
    private static int loopLimitation = 50;
    private static HashMap<String, Integer> countJumpedPair = new HashMap<>();
    private static HashMap<String, EnvModel> labelToEnvModel = new HashMap<>();
    private static Stack<Map.Entry<EnvModel, HashMap<String, EnvModel>>> envStack = new Stack<>();
    private static Map.Entry<EnvModel, HashMap<String, EnvModel>> recentPop = null;
    private static String triggerPrevLabelTwoUnsat = null;
    private static List<String> internalFunctions = new ArrayList<>();

    private static long startTime;

    public static void execute(Variation variation, String inpFile) {

        if (!FileUtils.isExist(inpFile)) {
            Logs.infoLn("-> Input file doest not exist.");
        } else if (!isARM(inpFile)) {
            Logs.infoLn("-> Input file is not an ARM variation.");
        } else {

            long startCapstone = System.currentTimeMillis();
            asmNodes = BinParser.parseBySection(inpFile);
            internalFunctions = BinParser.getInternalSymbols(inpFile);

            Logs.infoLn("-> Capstone disassembler elapsed: " + (System.currentTimeMillis() - startCapstone) + "ms");
            if (asmNodes != null) {
                for (AsmNode n : asmNodes) {
                    String saveAsm = n.getLabel() + " " + n.getOpcode() + n.getCondSuffix() + (n.isUpdateFlag() ? "s" : "") + " " + n.getParams();
                    Exporter.addOriginAsm(saveAsm);
                }
                Exporter.exportOriginAsm(Corana.inpFile + ".capstone-asm");
                asmNodes = BinParser.expand(asmNodes);

                for (int i = 0; i < asmNodes.size(); i++) {
                    if (!nodeLabelToIndex.containsKey(asmNodes.get(i).getLabel())) {
                        nodeLabelToIndex.put(asmNodes.get(i).getLabel(), i);
                    }
                }

                EnvModel genesis = new EnvModel("-", "");
                labelToEnvModel.put(genesis.label, genesis);
                startTime = System.currentTimeMillis();

                //Load memory and initialize stack pointer
                Environment env = new Environment();
                Memory.loadMemory(inpFile);

                // TODO: Start from asmNodes.get(_start), not the first node
                if (variation == Variation.M0) {
                    execFrom(new M0(env), genesis.label, String.valueOf(BinParser.get_start()));
                } else if (variation == Variation.M0_PLUS) {
                    execFrom(new M0_Plus(env), genesis.label, String.valueOf(BinParser.get_start()));
                } else if (variation == Variation.M3) {
                    execFrom(new M3(env), genesis.label, String.valueOf(BinParser.get_start()));
                } else if (variation == Variation.M4) {
                    execFrom(new M4(env), genesis.label, String.valueOf(BinParser.get_start()));
                } else if (variation == Variation.M7) {
                    execFrom(new M7(env), genesis.label, String.valueOf(BinParser.get_start()));
                } else if (variation == Variation.M33) {
                    execFrom(new M33(env), genesis.label, String.valueOf(BinParser.get_start()));
                } else {
                    Logs.infoLn("-> Unsupported ARM Variation.");
                    return;
                }
                gg();
            }
        }
    }


    private static void execFrom(Emulator emulator, String prevLabel, String label) {
        AsmNode n = asmNodes.get(nodeLabelToIndex.get(label));
        Logs.info("-> Executing", n.getAddress(), ":", n.getOpcode(), n.getParams(), '\n');
        if (label.equals(String.valueOf(BinParser.end))) {
            System.exit(0);
        }
        if (emulator.getClass() == M0.class) {
            singleExec((M0) emulator, prevLabel, n);
        } else if (emulator.getClass() == M0_Plus.class) {
            singleExec((M0_Plus) emulator, prevLabel, n);
        } else if (emulator.getClass() == M3.class) {
            singleExec((M3) emulator, prevLabel, n);
        } else if (emulator.getClass() == M4.class) {
            singleExec((M4) emulator, prevLabel, n);
        } else if (emulator.getClass() == M7.class) {
            singleExec((M7) emulator, prevLabel, n);
        } else if (emulator.getClass() == M33.class) {
            singleExec((M33) emulator, prevLabel, n);
        } else {
            Logs.infoLn("-> Wrong Variation!");
            return;
        }

        String newLabel = (jumpTo == null) ? nextInst(label) : jumpTo;
        String address = n.getAddress();
        String newAddress = "";
        // Check if function call is external
        if (nodeLabelToIndex.get(newLabel) == null) {
            // Deal with external call
            newAddress = asmNodes.get(nodeLabelToIndex.get(nextInst(label))).getAddress();
        } else {
            newAddress = asmNodes.get(nodeLabelToIndex.get(newLabel)).getAddress();
        }

        boolean isFault = false;

        if (jumpTo != null) {
            if (nodeLabelToIndex.containsKey(newLabel)) {
                String pair = jumpFrom + " --> " + jumpTo;
                Logs.info(String.format("\t-> Start Jumping from %s --> %s\n", asmNodes.get(nodeLabelToIndex.get(jumpFrom)).getAddress(), asmNodes.get(nodeLabelToIndex.get(jumpTo)).getAddress()));
                countJumpedPair.put(pair, countJumpedPair.containsKey(pair) ? countJumpedPair.get(pair) + 1 : 1);
                if (countJumpedPair.get(pair) <= loopLimitation) {
                    jumpTo = null;
                    jumpFrom = null;
                    Exporter.add(address + "," + newAddress + "," + countJumpedPair.get(pair) + "\n");

                    int savedAsmSize = Exporter.savedAsm.size();
                    String lastSaved = Exporter.savedAsm.get(savedAsmSize - 1);
                    String[] arr = lastSaved.split(" ");
                    Exporter.savedAsm.set(savedAsmSize - 1, arr[0] + " " + arr[1] + " " + newLabel);

                    String finalPrevLabel = triggerPrevLabelTwoUnsat == null ? label : triggerPrevLabelTwoUnsat;
                    triggerPrevLabelTwoUnsat = null;
                    execFrom(emulator, finalPrevLabel, newLabel);
                } else {
                    Logs.infoLn("\t-> Loop limitation exceeded, break.");
                    isFault = true;
                }
            } else {
                Logs.infoLn("\t-> Non-existing label, break.");
                isFault = true;
            }
        } else {
            isFault = true;
        }
        if (isFault) {
            jumpTo = null;
            jumpFrom = null;
            newLabel = nextInst(label);
            newAddress = asmNodes.get(nodeLabelToIndex.get(newLabel)).getAddress();
            if (nodeLabelToIndex.containsKey(newLabel)) {
                Exporter.add(address + "," + newAddress);
                execFrom(emulator, label, newLabel);
            }
        }
    }
    
    private static void singleExec(M0 emulator, String prevLabel, AsmNode n) {
        String nLabel = n.getLabel();
        if (nLabel == null) System.exit(0);
        if (!nLabel.contains("+") && !nLabel.contains("-")) emulator.write('p', new BitVec(Integer.parseInt(nLabel) + 8));
        Exporter.addAsm(n.getLabel() + " " + n.getOpcode() + n.getCondSuffix() + (n.isUpdateFlag() ? "s" : "") + " " + n.getParams());
        Exporter.exportDot(Corana.inpFile + ".dot");

        Character suffix = n.isUpdateFlag() ? 's' : null;
        String[] arrParams = Objects.requireNonNull(n.getParams()).split("\\,");
        if (n.getOpcode() != null) {
            String opcode = n.getOpcode();
            if ("b".equals(opcode) || "bl".equals(opcode) || "bx".equals(opcode) || "blx".equals((opcode))) {
                Character condSuffix = Mapping.condStrToChar.get(Objects.requireNonNull(n.getCondSuffix()).toUpperCase());
                String preCond = recentPop == null ? "" : recentPop.getKey().pathCondition;
                jumpFrom = nLabel;

                EnvModel thisEnvModel = new EnvModel(jumpFrom, preCond);
                labelToEnvModel.put(thisEnvModel.label, thisEnvModel);

                String strLabel = null;
                Character charLabel = null;
                if (isConcreteLabel(arrParams[0])) {
                    strLabel = arrParams[0].contains("-") ? arrParams[0] :
                            String.valueOf(Arithmetic.hexToInt(arrParams[0].replace("#0x", "").replace("0x", "")));
                } else { 
                    charLabel = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                }
                Map.Entry<EnvModel, EnvModel> envPair;
                switch (opcode) {
                    case "b":
                        envPair = emulator.b(preCond, condSuffix, strLabel == null ? charLabel : strLabel);
                        break;
                    case "bl":
                        envPair = emulator.bl(Integer.parseInt(nLabel), preCond, condSuffix, strLabel == null ? charLabel : strLabel);
                        break;
                    case "bx":
                        envPair = emulator.bx(preCond, condSuffix, strLabel == null ? charLabel : strLabel);
                        break;
                    case "blx":
                        envPair = emulator.bl(Integer.parseInt(nLabel), preCond, condSuffix, strLabel == null ? charLabel : strLabel);
                        break;
                    default:
                        envPair = null;
                        break;
                }
                if (envPair == null) {return;}
                EnvModel modelTrue = envPair.getKey();
                EnvModel modelFalse = envPair.getValue();
                // If it is a direct jump
                if (isConcreteLabel(arrParams[0])) {
                    if (modelTrue != null && modelTrue.envData != null) {
                        if (!ExternalCall.isExternalFucntion(arrParams[0])) {
                            //Internal Function
                            modelTrue.label = strLabel;
                            modelTrue.prevLabel = prevLabel;
                            labelToEnvModel.put(modelTrue.label, modelTrue);
                        }
                        if (ExternalCall.isExternalFucntion(arrParams[0])) {
                            Logs.infoLn("\t === Call to " + ExternalCall.findFunctionName(arrParams[0]) + " function");
                            //emulator.write('0', new BitVec(SysUtils.addSymVar()));
                            emulator.call(arrParams[0]);
                            modelTrue.label = nextInst(jumpFrom);
                            modelTrue.prevLabel = prevLabel;
                            labelToEnvModel.put(modelTrue.label, modelTrue);
                        }
                    }
                } else {
                    // Indirect jump
                    if (modelTrue != null && modelTrue.envData != null) { 
                        modelTrue.label = modelTrue.envData.eval;
                        modelTrue.prevLabel = prevLabel;
                        if (modelTrue.label == null) {
                            int foundLabel = Arithmetic.bitSetToInt(emulator.getEnv().register.regs.get('l').getVal());
                            if (foundLabel != 0 && foundLabel % 4 == 0) {
                                modelTrue.label = String.valueOf(foundLabel);
                                labelToEnvModel.put(modelTrue.label, modelTrue);
                                Logs.infoLn("\t-> Found the destination: " + foundLabel);
                            } else {
                                Logs.infoLn("\t-> Destination is undetectable.");
                            }
                        } else {
                            if (modelTrue.label.substring(0, 2).equals("#x")) {
                                modelTrue.label = String.valueOf(Arithmetic.hexToInt(modelTrue.label.replace("#x", "")));
                            }
                            labelToEnvModel.put(modelTrue.label, modelTrue);
                        }
                    }
                }
                if (modelFalse != null && modelFalse.envData != null) { 
                    modelFalse.label = nextInst(jumpFrom);
                    modelFalse.prevLabel = prevLabel;
                    labelToEnvModel.put(modelFalse.label, modelFalse);
                }
                decideToJump(modelTrue, modelFalse);
            } else if ("nop".equals(opcode)) {
                emulator.nop(suffix);
            } else if ("bic".equals(opcode)) {
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Integer im = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                if (im == null) {
                    emulator.bics(p0, p1, p2, suffix);
                } else {
                    emulator.bic(p0, p1, im, suffix);
                }
            } else if ("lsl".equals(opcode)) { 
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Integer im = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                emulator.lsls(p0, p1, p2, im, suffix);
            } else if ("lsr".equals(opcode)) { 
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Integer im = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                emulator.lsrs(p0, p1, p2, im, suffix);
            } else if ("mul".equals(opcode)) { 
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Character p2 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                emulator.muls(p0, p1, p2, suffix);
            } else if ("mvn".equals(opcode)) {
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = arrParams[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Integer im = arrParams[1].contains("#") ? SysUtils.normalizeNumInParam(arrParams[1].trim()) : null;
                if (im == null) {
                    emulator.mvns(p0, p1, suffix);
                } else {
                    emulator.mvn(p0, im, suffix);
                }
            } else if ("mov".equals(opcode) || "movw".equals(opcode) || "movt".equals(opcode)) {
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = arrParams[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Integer im = arrParams[1].contains("#") ? SysUtils.normalizeNumInParam(arrParams[1].trim()) : null;
                switch (opcode) {
                    case "mov":
                        emulator.mov(p0, p1, im, suffix);
                        break;
                    case "movw":
                        emulator.movw(p0, p1, im, suffix);
                        break;
                    case "movt":
                        emulator.movt(p0, p1, im, suffix);
                        break;
                }

            } else if ("adc".equals(opcode)) { 
                Integer im;
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                if (arrParams[2].contains("#")) {
                    im = SysUtils.normalizeNumInParam(arrParams[2].trim());
                    emulator.adc(p0, p1, im, suffix);
                } else {
                    Character p2 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                    emulator.adcs(p0, p1, p2, suffix);
                }
            } else if ("and".equals(opcode)) {
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Integer p2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                Character p2And = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                if (arrParams.length == 3) {
                    if (p2 != null) {
                        emulator.and(p0, p1, p2, suffix);
                    } else {
                        emulator.ands(p0, p1, p2And, suffix);
                    }
                } else {
                    String[] extArr = arrParams[3].split("\\s+");
                    String extType = extArr[0];
                    Integer extraNum = null;
                    Character extraChar = null;
                    if (extArr.length > 1) {
                        if (extArr[1].contains("#")) {
                            extraNum = Integer.parseInt(extArr[1].replace("#", ""));
                        } else {
                            extraChar = Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                        }
                    }
                    emulator.and(p0, p1, p2And, p2, extType, extraChar, extraNum, suffix);
                }
            } else if ("rsb".equals(opcode)) {
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Integer im = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                if (arrParams.length == 3) {
                    if (im != null) {
                        emulator.rsb(p0, p1, im, suffix);
                    } else {
                        emulator.rsbs(p0, p1, p2, suffix);
                    }
                } else {
                    String[] extArr = arrParams[3].split("\\s+");
                    String extType = extArr[0];
                    Integer extraNum = null;
                    Character extraChar = null;
                    if (extArr.length > 1) {
                        if (extArr[1].contains("#")) {
                            extraNum = Integer.parseInt(extArr[1].replace("#", ""));
                        } else {
                            extraChar = Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                        }
                    }
                    emulator.rsb(p0, p1, p2, im, extType, extraChar, extraNum, suffix);
                }
            } else if ("orr".equals(opcode)) {
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Integer im = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                if (arrParams.length == 3) {
                    if (im != null) {
                        emulator.orr(p0, p1, im, suffix);
                    } else {
                        emulator.orrs(p0, p1, p2, suffix);
                    }
                } else {
                    String[] extArr = arrParams[3].split("\\s+");
                    String extType = extArr[0];
                    Integer extraNum = null;
                    Character extraChar = null;
                    if (extArr.length > 1) {
                        if (extArr[1].contains("#")) {
                            extraNum = Integer.parseInt(extArr[1].replace("#", ""));
                        } else {
                            extraChar = Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                        }
                    }
                    emulator.orr(p0, p1, p2, im, extType, extraChar, extraNum, suffix);
                }
            } else if ("eor".equals(opcode)) {
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Integer im = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                if (arrParams.length == 3) {
                    if (im != null) {
                        emulator.eor(p0, p1, im, suffix);
                    } else {
                        emulator.eors(p0, p1, p2, suffix);
                    }
                } else {
                    String[] extArr = arrParams[3].split("\\s+");
                    String extType = extArr[0];
                    Integer extraNum = null;
                    Character extraChar = null;
                    if (extArr.length > 1) {
                        if (extArr[1].contains("#")) {
                            extraNum = Integer.parseInt(extArr[1].replace("#", ""));
                        } else {
                            extraChar = Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                        }
                    }
                    emulator.eor(p0, p1, p2, im, extType, extraChar, extraNum, suffix);
                }
            } else if ("add".equals(opcode)) {
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Integer im = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                if (arrParams.length == 3) {
                    emulator.add(p0, p1, p2, im, suffix);
                } else {
                    String[] extArr = arrParams[3].split("\\s+");
                    String extType = extArr[0];
                    Integer extraNum = null;
                    Character extraChar = null;
                    if (extArr.length > 1) {
                        if (extArr[1].contains("#")) {
                            extraNum = Integer.parseInt(extArr[1].replace("#", ""));
                        } else {
                            extraChar = Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                        }
                    }
                    emulator.add(p0, p1, p2, im, extType, extraChar, extraNum, suffix);
                }
            } else if ("sub".equals(opcode)) {
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Integer im = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                if (arrParams.length == 3) {
                    emulator.sub(p0, p1, p2, im, suffix);
                } else {
                    String[] extArr = arrParams[3].split("\\s+");
                    String extType = extArr[0];
                    Integer extraNum = null;
                    Character extraChar = null;
                    if (extArr.length > 1) {
                        if (extArr[1].contains("#")) {
                            extraNum = Integer.parseInt(extArr[1].replace("#", ""));
                        } else {
                            extraChar = Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                        }
                    }
                    emulator.sub(p0, p1, p2, im, extType, extraChar, extraNum, suffix);
                }
            } else if ("cmp".equals(opcode)) {
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Integer im = arrParams[1].contains("#") ? SysUtils.normalizeNumInParam(arrParams[1].trim()) : null;
                Character p1 = arrParams[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                if (arrParams.length == 2) {
                    emulator.cmp(p0, p1, im, suffix);
                } else {
                    String[] extArr = arrParams[2].split("\\s+");
                    String extType = extArr[0];
                    Integer extraNum = null;
                    Character extraChar = null;
                    if (extArr.length > 1) {
                        if (extArr[1].contains("#")) {
                            extraNum = Integer.parseInt(extArr[1].replace("#", ""));
                        } else {
                            extraChar = Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                        }
                    }
                    emulator.cmp(p0, p1, im, extType, extraChar, extraNum, suffix);
                }
            } else if ("cmn".equals(opcode)) {
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Integer im = arrParams[1].contains("#") ? SysUtils.normalizeNumInParam(arrParams[1].trim()) : null;
                Character p1 = arrParams[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                if (arrParams.length == 2) {
                    if (im != null) {
                        emulator.cmn(p0, im, suffix);
                    } else {
                        emulator.cmn(p0, p1, suffix);
                    }
                } else {
                    String[] extArr = arrParams[2].split("\\s+");
                    String extType = extArr[0];
                    Integer extraNum = null;
                    Character extraChar = null;
                    if (extArr.length > 1) {
                        if (extArr[1].contains("#")) {
                            extraNum = Integer.parseInt(extArr[1].replace("#", ""));
                        } else {
                            extraChar = Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                        }
                    }
                    emulator.cmn(p0, p1, im, extType, extraChar, extraNum, suffix);
                }
            } else if ("svc".equals(opcode)) {
                Integer imSvc = arrParams[0].contains("#") ? SysUtils.normalizeNumInParam(arrParams[0].trim()) : null;
                emulator.svc(imSvc);
            } else if ("pop".equals(opcode)) {
                for (String p : arrParams) {
                    p = p.replace("{", "").replace("}", "");
                    Character c = Mapping.regStrToChar.get(SysUtils.normalizeRegName(p));
                    emulator.pop(c);
                }
            } else if ("push".equals(opcode)) {
                for (int i = arrParams.length - 1; i >= 0; i--) {
                    String p = arrParams[i];
                    p = p.replace("{", "").replace("}", "");
                    Character c = Mapping.regStrToChar.get(SysUtils.normalizeRegName(p));
                    emulator.push(c);
                }
            } else if ("ldrb".equals(opcode)) {
                int type;
                if (arrParams.length == 2) {
                    type = 0;
                } else {
                    String lastP = arrParams[arrParams.length - 1];
                    type = lastP.endsWith("]") ? 1 : (lastP.endsWith("!") ? 2 : 3);
                }
                for (int k = 0; k < arrParams.length; k++) {
                    arrParams[k] = arrParams[k].trim().replace("[", "").replace("]", "").replace("!", "");
                }
                switch (type) {
                    case 0:
                        Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));
                        emulator.ldrb(p0, p1);
                        break;
                    case 1: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        Integer i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        BitVec extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        BitVec newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            newValue = emulator.add(newValue, emulator.handleExtra(extValue, extType, extraChar, extraNum));
                        }
                        emulator.ldrb(p0, newValue);
                        break;
                    case 2: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.add(newValue, extValue);
                            extValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);

                            newValue = emulator.add(newValue, handledExt);
                            extValue = emulator.add(newValue, handledExt);
                        }
                        emulator.ldrb(p0, newValue);
                        emulator.write(p1, extValue);
                        break;
                    case 3: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = newValue;
                            extValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);

                            newValue = newValue;
                            extValue = emulator.add(newValue, handledExt);
                        }
                        emulator.ldrb(p0, newValue);
                        emulator.write(p1, extValue);
                        break;
                    default:
                        break;
                }
            } else if ("ldr".equals(opcode)) {
                int type;
                if (arrParams.length == 2) {
                    type = 0;
                } else {
                    String lastP = arrParams[arrParams.length - 1];
                    type = lastP.endsWith("]") ? 1 : (lastP.endsWith("!") ? 2 : 3);
                    // TODO: tmp
                    if (arrParams[1].contains("]")) {
                        type = 4;
                    }
                }

                for (int k = 0; k < arrParams.length; k++) {
                    arrParams[k] = arrParams[k].trim().replace("[", "").replace("]", "").replace("!", "");
                }
                switch (type) {
                    case 0:
                        Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));
                        emulator.ldr(p0, p1);
                        break;
                    case 1: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        Integer i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        BitVec extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        BitVec newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            newValue = emulator.add(newValue, emulator.handleExtra(extValue, extType, extraChar, extraNum));
                        }
                        emulator.ldr(p0, newValue);
                        break;
                    case 2: //@address mode: pre-indexed
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.add(newValue, extValue);
                            extValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);

                            newValue = emulator.add(newValue, handledExt);
                            extValue = emulator.add(newValue, handledExt);
                        }
                        emulator.ldr(p0, newValue);
                        emulator.write(p1, extValue);
                        break;
                    case 3: //@address mode: offset
                            p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                            p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                            i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                            p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                            extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                            newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                            if (arrParams.length == 3) {
                                newValue = newValue;
                                extValue = emulator.add(newValue, extValue);
                            } else {
                                String[] extArr = arrParams[3].split("\\s+");
                                String extType = extArr[0];
                                Integer extraNum = null;
                                Character extraChar = null;
                                if (extArr.length > 1) {
                                    extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                    extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                                }
                                BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);

                                newValue = newValue;
                                extValue = emulator.add(newValue, handledExt);
                            }
                        emulator.ldr(p0, newValue);
                        emulator.write(p1, extValue);
                        break;
                    case 4: //@address mode: post-indexed
                        // ldr r3, [r1], #4    Load the value at memory address found in R1 to register R3. Base register (R1) modified: R1 = R1+4
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].replace("[", "").replace("]","")));

                        i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.val(p1);
                            extValue = emulator.add(newValue, extValue);
                        } else  {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);
                            newValue = emulator.val(p1);
                            extValue = emulator.add(newValue, handledExt);
                        }
                        emulator.ldrAt(p0, newValue);
                        emulator.write(p1, extValue);
                        break;
                    default:
                        break;
                }
            } else if ("ldrh".equals(opcode)) {
                int type;
                if (arrParams.length == 2) {
                    type = 0;
                } else {
                    String lastP = arrParams[arrParams.length - 1];
                    type = lastP.endsWith("]") ? 1 : (lastP.endsWith("!") ? 2 : 3);
                }
                for (int k = 0; k < arrParams.length; k++) {
                    arrParams[k] = arrParams[k].trim().replace("[", "").replace("]", "").replace("!", "");
                }
                switch (type) {
                    case 0:
                        Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));
                        emulator.ldrh(p0, p1);
                        break;
                    case 1: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        Integer i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        BitVec extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        BitVec newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            newValue = emulator.add(newValue, emulator.handleExtra(extValue, extType, extraChar, extraNum));
                        }
                        emulator.ldrh(p0, newValue);
                        break;
                    case 2: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.add(newValue, extValue);
                            extValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);

                            newValue = emulator.add(newValue, handledExt);
                            extValue = emulator.add(newValue, handledExt);
                        }
                        emulator.ldrh(p0, newValue);
                        emulator.write(p1, extValue);
                        break;
                    case 3: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = newValue;
                            extValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);

                            newValue = newValue;
                            extValue = emulator.add(newValue, handledExt);
                        }
                        emulator.ldrh(p0, newValue);
                        emulator.write(p1, extValue);
                        break;
                    default:
                        break;
                }
            } else if ("str".equals(opcode)) {
                int type;
                if (arrParams.length == 2) {
                    type = 0;
                } else {
                    String lastP = arrParams[arrParams.length - 1];
                    type = lastP.endsWith("]") ? 1 : (lastP.endsWith("!") ? 2 : 3);
                }
                for (int k = 0; k < arrParams.length; k++) {
                    arrParams[k] = arrParams[k].trim().replace("[", "").replace("]", "").replace("!", "");
                }
                switch (type) {
                    case 0:
                        Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));
                        emulator.str(emulator.val(p0), emulator.val(p1));
                        break;
                    case 1: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        Integer i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        BitVec extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        BitVec newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            newValue = emulator.add(newValue, emulator.handleExtra(extValue, extType, extraChar, extraNum));
                        }
                        emulator.str(emulator.val(p0), newValue);
                        break;
                    case 2: //@ address mode: offset.  str r2, [r1, #2]
                        // Store the value found in R2 (0x03) to the memory address found in R1 plus 2.
                        // Base register (R1) unmodified.
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.add(newValue, extValue);
                            extValue = newValue;
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);

                            newValue = emulator.add(newValue, handledExt);
                            extValue = emulator.add(newValue, handledExt);
                        }
                        emulator.str(emulator.val(p0), newValue);
                        emulator.write(p1, extValue); //base register is unmodified
                        break;
                    case 3: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = newValue;
                            extValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);

                            newValue = newValue;
                            extValue = emulator.add(newValue, handledExt);
                        }
                        emulator.str(emulator.val(p0), newValue);
                        emulator.write(p1, extValue);
                        break;
                    default:
                        break;
                }
            } else if ("stmib".equals(opcode)) {
                // Store multiple increase before
                // addr = Rn + 4; for each Ri in params: addr = addr + 4; ri = M[addr]
                for (int k = 0; k < arrParams.length; k++) {
                    arrParams[k] = arrParams[k].trim().replace("{", "").replace("}", "").replace("!", "");
                }
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                BitVec baseAddr = emulator.val(p0);
                for (int i = 1; i < arrParams.length; i++) {
                    baseAddr = emulator.add(baseAddr, new BitVec(4));
                    Character pi = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[i]));
                    emulator.str(emulator.val(pi), baseAddr);
                }
            } else if ("strb".equals(opcode)) {
                int type;
                if (arrParams.length == 2) {
                    type = 0;
                } else {
                    String lastP = arrParams[arrParams.length - 1];
                    type = lastP.endsWith("]") ? 1 : (lastP.endsWith("!") ? 2 : 3);
                }
                for (int k = 0; k < arrParams.length; k++) {
                    arrParams[k] = arrParams[k].trim().replace("[", "").replace("]", "").replace("!", "");
                }
                switch (type) {
                    case 0:
                        Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));
                        emulator.strb(emulator.val(p0), emulator.val(p1));
                        break;
                    case 1: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        Integer i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        BitVec extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        BitVec newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            newValue = emulator.add(newValue, emulator.handleExtra(extValue, extType, extraChar, extraNum));
                        }
                        emulator.strb(emulator.val(p0), newValue);
                        break;
                    case 2: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.add(newValue, extValue);
                            extValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);

                            newValue = emulator.add(newValue, handledExt);
                            extValue = emulator.add(newValue, handledExt);
                        }
                        emulator.strb(emulator.val(p0), newValue);
                        emulator.write(p1, extValue);
                        break;
                    case 3: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = newValue;
                            extValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);

                            newValue = newValue;
                            extValue = emulator.add(newValue, handledExt);
                        }
                        emulator.strb(emulator.val(p0), newValue);
                        emulator.write(p1, extValue);
                        break;
                    default:
                        break;
                }
            } else if ("strh".equals(opcode)) {
                int type;
                if (arrParams.length == 2) {
                    type = 0;
                } else {
                    String lastP = arrParams[arrParams.length - 1];
                    type = lastP.endsWith("]") ? 1 : (lastP.endsWith("!") ? 2 : 3);
                }
                for (int k = 0; k < arrParams.length; k++) {
                    arrParams[k] = arrParams[k].trim().replace("[", "").replace("]", "").replace("!", "");
                }
                switch (type) {
                    case 0:
                        Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));
                        emulator.strh(emulator.val(p0), emulator.val(p1));
                        break;
                    case 1: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        Integer i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        BitVec extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        BitVec newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            newValue = emulator.add(newValue, emulator.handleExtra(extValue, extType, extraChar, extraNum));
                        }
                        emulator.strh(emulator.val(p0), newValue);
                        break;
                    case 2: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.add(newValue, extValue);
                            extValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);

                            newValue = emulator.add(newValue, handledExt);
                            extValue = emulator.add(newValue, handledExt);
                        }
                        emulator.strh(emulator.val(p0), newValue);
                        emulator.write(p1, extValue);
                        break;
                    case 3: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = newValue;
                            extValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);

                            newValue = newValue;
                            extValue = emulator.add(newValue, handledExt);
                        }
                        emulator.strh(emulator.val(p0), newValue);
                        emulator.write(p1, extValue);
                        break;
                    default:

                        break;
                }
            }
            // Added instructions

        }
        else {
            Logs.infoLn("Error: Opcode is null!");
        }
    }

    private static void singleExec(M0_Plus emulator, String prevLabel, AsmNode n) {
        String nLabel = n.getLabel();
        if (nLabel == null) System.exit(0);
        if (!nLabel.contains("+") && !nLabel.contains("-")) emulator.write('p', new BitVec(Integer.parseInt(nLabel)));
        Exporter.addAsm(nLabel + " " + n.getOpcode() + n.getCondSuffix() + (n.isUpdateFlag() ? "s" : "") + " " + n.getParams());
        Exporter.exportDot(Corana.inpFile + ".dot");

        Character suffix = n.isUpdateFlag() ? 's' : null;
        String[] arrParams = Objects.requireNonNull(n.getParams()).split("\\,");
        if (n.getOpcode() != null) {
            String opcode = n.getOpcode();
            if ("b".equals(opcode) || "bl".equals(opcode) || "bx".equals(opcode)) {
                Character condSuffix = Mapping.condStrToChar.get(Objects.requireNonNull(n.getCondSuffix()).toUpperCase());
                String preCond = recentPop == null ? "" : recentPop.getKey().pathCondition;
                jumpFrom = nLabel;
                EnvModel thisEnvModel = new EnvModel(jumpFrom, preCond);
                labelToEnvModel.put(thisEnvModel.label, thisEnvModel);

                String strLabel = null;
                Character charLabel = null;
                if (isConcreteLabel(arrParams[0])) {
                    strLabel = arrParams[0].contains("-") ? arrParams[0] :
                            String.valueOf(Arithmetic.hexToInt(arrParams[0].replace("#0x", "").replace("0x", "")));
                } else { 
                    charLabel = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                }
                Map.Entry<EnvModel, EnvModel> envPair;
                switch (opcode) {
                    case "b":
                        envPair = emulator.b(preCond, condSuffix, strLabel == null ? charLabel : strLabel);
                        break;
                    case "bl":
                        envPair = emulator.bl(Integer.parseInt(nLabel), preCond, condSuffix, strLabel == null ? charLabel : strLabel);
                        break;
                    case "bx":
                        envPair = emulator.bx(preCond, condSuffix, strLabel == null ? charLabel : strLabel);
                        break;
                    default:
                        envPair = null;
                        break;
                }
                EnvModel modelTrue = envPair.getKey();
                EnvModel modelFalse = envPair.getValue();
                if (isConcreteLabel(arrParams[0])) {
                    if (modelTrue != null && modelTrue.envData != null) { 
                        modelTrue.label = strLabel;
                        modelTrue.prevLabel = prevLabel;
                        labelToEnvModel.put(modelTrue.label, modelTrue);
                    }
                } else {
                    if (modelTrue != null && modelTrue.envData != null) { 
                        modelTrue.label = modelTrue.envData.eval;
                        modelTrue.prevLabel = prevLabel;
                        if (modelTrue.label == null) {
                            
                            
                            int foundLabel = Arithmetic.bitSetToInt(emulator.getEnv().register.regs.get('l').getVal());
                            if (foundLabel != 0 && foundLabel % 4 == 0) {
                                modelTrue.label = String.valueOf(foundLabel);
                                labelToEnvModel.put(modelTrue.label, modelTrue);
                                Logs.infoLn("\t-> Found the destination: " + foundLabel);
                            } else {
                                Logs.infoLn("\t-> Destination is undetectable.");
                            }
                        } else {
                            if (modelTrue.label.substring(0, 2).equals("#x")) {
                                modelTrue.label = String.valueOf(Arithmetic.hexToInt(modelTrue.label.replace("#x", "")));
                            }
                            labelToEnvModel.put(modelTrue.label, modelTrue);
                        }
                    }
                }
                if (modelFalse != null && modelFalse.envData != null) { 
                    modelFalse.label = nextInst(jumpFrom);
                    modelFalse.prevLabel = prevLabel;
                    labelToEnvModel.put(modelFalse.label, modelFalse);
                }
                decideToJump(modelTrue, modelFalse);
            } else if ("nop".equals(opcode)) {
                emulator.nop(suffix);
            } else if ("bic".equals(opcode)) {
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Integer im = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                if (im == null) {
                    emulator.bics(p0, p1, p2, suffix);
                } else {
                    emulator.bic(p0, p1, im, suffix);
                }
            } else if ("lsl".equals(opcode)) { 
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Integer im = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                emulator.lsls(p0, p1, p2, im, suffix);
            } else if ("lsr".equals(opcode)) { 
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Integer im = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                emulator.lsrs(p0, p1, p2, im, suffix);
            } else if ("mul".equals(opcode)) { 
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Character p2 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                emulator.muls(p0, p1, p2, suffix);
            } else if ("mvn".equals(opcode)) {
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = arrParams[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Integer im = arrParams[1].contains("#") ? SysUtils.normalizeNumInParam(arrParams[1].trim()) : null;
                if (im == null) {
                    emulator.mvns(p0, p1, suffix);
                } else {
                    emulator.mvn(p0, im, suffix);
                }
            } else if ("mov".equals(opcode)) { 
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = arrParams[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Integer im = arrParams[1].contains("#") ? SysUtils.normalizeNumInParam(arrParams[1].trim()) : null;
                emulator.mov(p0, p1, im, suffix);
            } else if ("adc".equals(opcode)) { 
                Integer im;
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                if (arrParams[2].contains("#")) {
                    im = SysUtils.normalizeNumInParam(arrParams[2].trim());
                    emulator.adc(p0, p1, im, suffix);
                } else {
                    Character p2 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                    emulator.adcs(p0, p1, p2, suffix);
                }
            } else if ("and".equals(opcode)) {
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Integer p2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                Character p2And = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                if (arrParams.length == 3) {
                    if (p2 != null) {
                        emulator.and(p0, p1, p2, suffix);
                    } else {
                        emulator.ands(p0, p1, p2And, suffix);
                    }
                } else {
                    String[] extArr = arrParams[3].split("\\s+");
                    String extType = extArr[0];
                    Integer extraNum = null;
                    Character extraChar = null;
                    if (extArr.length > 1) {
                        if (extArr[1].contains("#")) {
                            extraNum = Integer.parseInt(extArr[1].replace("#", ""));
                        } else {
                            extraChar = Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                        }
                    }
                    emulator.and(p0, p1, p2And, p2, extType, extraChar, extraNum, suffix);
                }
            } else if ("rsb".equals(opcode)) {
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Integer im = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                if (arrParams.length == 3) {
                    if (im != null) {
                        emulator.rsb(p0, p1, im, suffix);
                    } else {
                        emulator.rsbs(p0, p1, p2, suffix);
                    }
                } else {
                    String[] extArr = arrParams[3].split("\\s+");
                    String extType = extArr[0];
                    Integer extraNum = null;
                    Character extraChar = null;
                    if (extArr.length > 1) {
                        if (extArr[1].contains("#")) {
                            extraNum = Integer.parseInt(extArr[1].replace("#", ""));
                        } else {
                            extraChar = Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                        }
                    }
                    emulator.rsb(p0, p1, p2, im, extType, extraChar, extraNum, suffix);
                }
            } else if ("orr".equals(opcode)) {
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Integer im = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                if (arrParams.length == 3) {
                    if (im != null) {
                        emulator.orr(p0, p1, im, suffix);
                    } else {
                        emulator.orrs(p0, p1, p2, suffix);
                    }
                } else {
                    String[] extArr = arrParams[3].split("\\s+");
                    String extType = extArr[0];
                    Integer extraNum = null;
                    Character extraChar = null;
                    if (extArr.length > 1) {
                        if (extArr[1].contains("#")) {
                            extraNum = Integer.parseInt(extArr[1].replace("#", ""));
                        } else {
                            extraChar = Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                        }
                    }
                    emulator.orr(p0, p1, p2, im, extType, extraChar, extraNum, suffix);
                }
            } else if ("eor".equals(opcode)) {
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Integer im = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                if (arrParams.length == 3) {
                    if (im != null) {
                        emulator.eor(p0, p1, im, suffix);
                    } else {
                        emulator.eors(p0, p1, p2, suffix);
                    }
                } else {
                    String[] extArr = arrParams[3].split("\\s+");
                    String extType = extArr[0];
                    Integer extraNum = null;
                    Character extraChar = null;
                    if (extArr.length > 1) {
                        if (extArr[1].contains("#")) {
                            extraNum = Integer.parseInt(extArr[1].replace("#", ""));
                        } else {
                            extraChar = Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                        }
                    }
                    emulator.eor(p0, p1, p2, im, extType, extraChar, extraNum, suffix);
                }
            } else if ("add".equals(opcode)) {
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Integer im = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                if (arrParams.length == 3) {
                    emulator.add(p0, p1, p2, im, suffix);
                } else {
                    String[] extArr = arrParams[3].split("\\s+");
                    String extType = extArr[0];
                    Integer extraNum = null;
                    Character extraChar = null;
                    if (extArr.length > 1) {
                        if (extArr[1].contains("#")) {
                            extraNum = Integer.parseInt(extArr[1].replace("#", ""));
                        } else {
                            extraChar = Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                        }
                    }
                    emulator.add(p0, p1, p2, im, extType, extraChar, extraNum, suffix);
                }
            } else if ("sub".equals(opcode)) {
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Integer im = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                if (arrParams.length == 3) {
                    emulator.sub(p0, p1, p2, im, suffix);
                } else {
                    String[] extArr = arrParams[3].split("\\s+");
                    String extType = extArr[0];
                    Integer extraNum = null;
                    Character extraChar = null;
                    if (extArr.length > 1) {
                        if (extArr[1].contains("#")) {
                            extraNum = Integer.parseInt(extArr[1].replace("#", ""));
                        } else {
                            extraChar = Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                        }
                    }
                    emulator.sub(p0, p1, p2, im, extType, extraChar, extraNum, suffix);
                }
            } else if ("cmp".equals(opcode)) {
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Integer im = arrParams[1].contains("#") ? SysUtils.normalizeNumInParam(arrParams[1].trim()) : null;
                Character p1 = arrParams[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                if (arrParams.length == 2) {
                    emulator.cmp(p0, p1, im, suffix);
                } else {
                    String[] extArr = arrParams[2].split("\\s+");
                    String extType = extArr[0];
                    Integer extraNum = null;
                    Character extraChar = null;
                    if (extArr.length > 1) {
                        if (extArr[1].contains("#")) {
                            extraNum = Integer.parseInt(extArr[1].replace("#", ""));
                        } else {
                            extraChar = Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                        }
                    }
                    emulator.cmp(p0, p1, im, extType, extraChar, extraNum, suffix);
                }
            } else if ("cmn".equals(opcode)) {
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Integer im = arrParams[1].contains("#") ? SysUtils.normalizeNumInParam(arrParams[1].trim()) : null;
                Character p1 = arrParams[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                if (arrParams.length == 2) {
                    if (im != null) {
                        emulator.cmn(p0, im, suffix);
                    } else {
                        emulator.cmn(p0, p1, suffix);
                    }
                } else {
                    String[] extArr = arrParams[2].split("\\s+");
                    String extType = extArr[0];
                    Integer extraNum = null;
                    Character extraChar = null;
                    if (extArr.length > 1) {
                        if (extArr[1].contains("#")) {
                            extraNum = Integer.parseInt(extArr[1].replace("#", ""));
                        } else {
                            extraChar = Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                        }
                    }
                    emulator.cmn(p0, p1, im, extType, extraChar, extraNum, suffix);
                }
            } else if ("svc".equals(opcode)) {
                Integer imSvc = arrParams[0].contains("#") ? SysUtils.normalizeNumInParam(arrParams[0].trim()) : null;
                emulator.svc(imSvc);
            } else if ("pop".equals(opcode)) {
                for (String p : arrParams) {
                    p = p.replace("{", "").replace("}", "");
                    Character c = Mapping.regStrToChar.get(SysUtils.normalizeRegName(p));
                    emulator.pop(c);
                }
            } else if ("push".equals(opcode)) {
                for (String p : arrParams) {
                    p = p.replace("{", "").replace("}", "");
                    Character c = Mapping.regStrToChar.get(SysUtils.normalizeRegName(p));
                    emulator.push(c);
                }
            } else if ("ldrb".equals(opcode)) {
                int type;
                if (arrParams.length == 2) {
                    type = 0;
                } else {
                    String lastP = arrParams[arrParams.length - 1];
                    type = lastP.endsWith("]") ? 1 : (lastP.endsWith("!") ? 2 : 3);
                }
                for (int k = 0; k < arrParams.length; k++) {
                    arrParams[k] = arrParams[k].trim().replace("[", "").replace("]", "").replace("!", "");
                }
                switch (type) {
                    case 0:
                        Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));
                        emulator.ldrb(p0, p1);
                        break;
                    case 1: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        Integer i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        BitVec extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        BitVec newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            newValue = emulator.add(newValue, emulator.handleExtra(extValue, extType, extraChar, extraNum));
                        }
                        emulator.ldrb(p0, newValue);
                        break;
                    case 2: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.add(newValue, extValue);
                            extValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);

                            newValue = emulator.add(newValue, handledExt);
                            extValue = emulator.add(newValue, handledExt);
                        }
                        emulator.ldrb(p0, newValue);
                        emulator.write(p1, extValue);
                        break;
                    case 3: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = newValue;
                            extValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);

                            newValue = newValue;
                            extValue = emulator.add(newValue, handledExt);
                        }
                        emulator.ldrb(p0, newValue);
                        emulator.write(p1, extValue);
                        break;
                    default:
                        break;
                }
            } else if ("ldr".equals(opcode)) {
                int type;
                if (arrParams.length == 2) {
                    type = 0;
                } else {
                    String lastP = arrParams[arrParams.length - 1];
                    type = lastP.endsWith("]") ? 1 : (lastP.endsWith("!") ? 2 : 3);
                }
                for (int k = 0; k < arrParams.length; k++) {
                    arrParams[k] = arrParams[k].trim().replace("[", "").replace("]", "").replace("!", "");
                }
                switch (type) {
                    case 0:
                        Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));
                        emulator.ldr(p0, p1);
                        break;
                    case 1: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        Integer i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        BitVec extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        BitVec newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            newValue = emulator.add(newValue, emulator.handleExtra(extValue, extType, extraChar, extraNum));
                        }
                        emulator.ldr(p0, newValue);
                        break;
                    case 2: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.add(newValue, extValue);
                            extValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);

                            newValue = emulator.add(newValue, handledExt);
                            extValue = emulator.add(newValue, handledExt);
                        }
                        emulator.ldr(p0, newValue);
                        emulator.write(p1, extValue);
                        break;
                    case 3: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = newValue;
                            extValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);

                            newValue = newValue;
                            extValue = emulator.add(newValue, handledExt);
                        }
                        emulator.ldr(p0, newValue);
                        emulator.write(p1, extValue);
                        break;
                    default:
                        break;
                }
            } else if ("ldrh".equals(opcode)) {
                int type;
                if (arrParams.length == 2) {
                    type = 0;
                } else {
                    String lastP = arrParams[arrParams.length - 1];
                    type = lastP.endsWith("]") ? 1 : (lastP.endsWith("!") ? 2 : 3);
                }
                for (int k = 0; k < arrParams.length; k++) {
                    arrParams[k] = arrParams[k].trim().replace("[", "").replace("]", "").replace("!", "");
                }
                switch (type) {
                    case 0:
                        Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));
                        emulator.ldrh(p0, p1);
                        break;
                    case 1: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        Integer i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        BitVec extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        BitVec newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            newValue = emulator.add(newValue, emulator.handleExtra(extValue, extType, extraChar, extraNum));
                        }
                        emulator.ldrh(p0, newValue);
                        break;
                    case 2: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.add(newValue, extValue);
                            extValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);

                            newValue = emulator.add(newValue, handledExt);
                            extValue = emulator.add(newValue, handledExt);
                        }
                        emulator.ldrh(p0, newValue);
                        emulator.write(p1, extValue);
                        break;
                    case 3: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = newValue;
                            extValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);

                            newValue = newValue;
                            extValue = emulator.add(newValue, handledExt);
                        }
                        emulator.ldrh(p0, newValue);
                        emulator.write(p1, extValue);
                        break;
                    default:
                        break;
                }
            } else if ("str".equals(opcode)) {
                int type;
                if (arrParams.length == 2) {
                    type = 0;
                } else {
                    String lastP = arrParams[arrParams.length - 1];
                    type = lastP.endsWith("]") ? 1 : (lastP.endsWith("!") ? 2 : 3);
                }
                for (int k = 0; k < arrParams.length; k++) {
                    arrParams[k] = arrParams[k].trim().replace("[", "").replace("]", "").replace("!", "");
                }
                switch (type) {
                    case 0:
                        Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));
                        emulator.str(emulator.val(p0), emulator.val(p1));
                        break;
                    case 1: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        Integer i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        BitVec extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        BitVec newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            newValue = emulator.add(newValue, emulator.handleExtra(extValue, extType, extraChar, extraNum));
                        }
                        emulator.str(emulator.val(p0), newValue);
                        break;
                    case 2: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.add(newValue, extValue);
                            extValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);

                            newValue = emulator.add(newValue, handledExt);
                            extValue = emulator.add(newValue, handledExt);
                        }
                        emulator.str(emulator.val(p0), newValue);
                        emulator.write(p1, extValue);
                        break;
                    case 3: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = newValue;
                            extValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);

                            newValue = newValue;
                            extValue = emulator.add(newValue, handledExt);
                        }
                        emulator.str(emulator.val(p0), newValue);
                        emulator.write(p1, extValue);
                        break;
                    default:
                        break;
                }
            } else if ("strb".equals(opcode)) {
                int type;
                if (arrParams.length == 2) {
                    type = 0;
                } else {
                    String lastP = arrParams[arrParams.length - 1];
                    type = lastP.endsWith("]") ? 1 : (lastP.endsWith("!") ? 2 : 3);
                }
                for (int k = 0; k < arrParams.length; k++) {
                    arrParams[k] = arrParams[k].trim().replace("[", "").replace("]", "").replace("!", "");
                }
                switch (type) {
                    case 0:
                        Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));
                        emulator.strb(emulator.val(p0), emulator.val(p1));
                        break;
                    case 1: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        Integer i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        BitVec extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        BitVec newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            newValue = emulator.add(newValue, emulator.handleExtra(extValue, extType, extraChar, extraNum));
                        }
                        emulator.strb(emulator.val(p0), newValue);
                        break;
                    case 2: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.add(newValue, extValue);
                            extValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);

                            newValue = emulator.add(newValue, handledExt);
                            extValue = emulator.add(newValue, handledExt);
                        }
                        emulator.strb(emulator.val(p0), newValue);
                        emulator.write(p1, extValue);
                        break;
                    case 3: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = newValue;
                            extValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);

                            newValue = newValue;
                            extValue = emulator.add(newValue, handledExt);
                        }
                        emulator.strb(emulator.val(p0), newValue);
                        emulator.write(p1, extValue);
                        break;
                    default:
                        break;
                }
            } else if ("strh".equals(opcode)) {
                int type;
                if (arrParams.length == 2) {
                    type = 0;
                } else {
                    String lastP = arrParams[arrParams.length - 1];
                    type = lastP.endsWith("]") ? 1 : (lastP.endsWith("!") ? 2 : 3);
                }
                for (int k = 0; k < arrParams.length; k++) {
                    arrParams[k] = arrParams[k].trim().replace("[", "").replace("]", "").replace("!", "");
                }
                switch (type) {
                    case 0:
                        Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));
                        emulator.strh(emulator.val(p0), emulator.val(p1));
                        break;
                    case 1: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        Integer i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        BitVec extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        BitVec newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            newValue = emulator.add(newValue, emulator.handleExtra(extValue, extType, extraChar, extraNum));
                        }
                        emulator.strh(emulator.val(p0), newValue);
                        break;
                    case 2: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.add(newValue, extValue);
                            extValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);

                            newValue = emulator.add(newValue, handledExt);
                            extValue = emulator.add(newValue, handledExt);
                        }
                        emulator.strh(emulator.val(p0), newValue);
                        emulator.write(p1, extValue);
                        break;
                    case 3: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = newValue;
                            extValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);

                            newValue = newValue;
                            extValue = emulator.add(newValue, handledExt);
                        }
                        emulator.strh(emulator.val(p0), newValue);
                        emulator.write(p1, extValue);
                        break;
                    default:

                        break;
                }
            }
        } else {
            Logs.infoLn("Error: Opcode is null!");
        }
    }

    private static void singleExec(M3 emulator, String prevLabel, AsmNode n) {
        String nLabel = n.getLabel();
        if (nLabel == null) System.exit(0);
        if (!nLabel.contains("+") && !nLabel.contains("-")) emulator.write('p', new BitVec(Integer.parseInt(nLabel)));
        Exporter.addAsm(nLabel + " " + n.getOpcode() + n.getCondSuffix() + (n.isUpdateFlag() ? "s" : "") + " " + n.getParams());
        Exporter.exportDot(Corana.inpFile + ".dot");

        Character suffix = n.isUpdateFlag() ? 's' : null;
        String[] arrParams = Objects.requireNonNull(n.getParams()).split("\\,");
        if (n.getOpcode() != null) {
            String opcode = n.getOpcode();
            if ("b".equals(opcode) || "bl".equals(opcode) || "bx".equals(opcode)) {
                Character condSuffix = Mapping.condStrToChar.get(Objects.requireNonNull(n.getCondSuffix()).toUpperCase());
                String preCond = recentPop == null ? "" : recentPop.getKey().pathCondition;
                jumpFrom = nLabel;
                EnvModel thisEnvModel = new EnvModel(jumpFrom, preCond);
                labelToEnvModel.put(thisEnvModel.label, thisEnvModel);

                String strLabel = null;
                Character charLabel = null;
                if (isConcreteLabel(arrParams[0])) {
                    strLabel = arrParams[0].contains("-") ? arrParams[0] :
                            String.valueOf(Arithmetic.hexToInt(arrParams[0].replace("#0x", "").replace("0x", "")));
                } else {
                    charLabel = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                }
                Map.Entry<EnvModel, EnvModel> envPair;
                switch (opcode) {
                    case "b":
                        envPair = emulator.b(preCond, condSuffix, strLabel == null ? charLabel : strLabel);
                        break;
                    case "bl":
                        envPair = emulator.bl(Integer.parseInt(nLabel), preCond, condSuffix, strLabel == null ? charLabel : strLabel);
                        break;
                    case "bx":
                        envPair = emulator.bx(preCond, condSuffix, strLabel == null ? charLabel : strLabel);
                        break;
                    default:
                        envPair = null;
                        break;
                }
                EnvModel modelTrue = envPair.getKey();
                EnvModel modelFalse = envPair.getValue();
                if (isConcreteLabel(arrParams[0])) {
                    if (modelTrue != null && modelTrue.envData != null) {
                        modelTrue.label = strLabel;
                        modelTrue.prevLabel = prevLabel;
                        labelToEnvModel.put(modelTrue.label, modelTrue);
                    }
                } else {
                    if (modelTrue != null && modelTrue.envData != null) {
                        modelTrue.label = modelTrue.envData.eval;
                        modelTrue.prevLabel = prevLabel;
                        if (modelTrue.label == null) {
                            int foundLabel = Arithmetic.bitSetToInt(emulator.getEnv().register.regs.get('l').getVal());
                            if (foundLabel != 0 && foundLabel % 4 == 0) {
                                modelTrue.label = String.valueOf(foundLabel);
                                labelToEnvModel.put(modelTrue.label, modelTrue);
                                Logs.infoLn("\t-> Found the destination: " + foundLabel);
                            } else {
                                Logs.infoLn("\t-> Destination is undetectable.");
                            }
                        } else {
                            if (modelTrue.label.substring(0, 2).equals("#x")) {
                                modelTrue.label = String.valueOf(Arithmetic.hexToInt(modelTrue.label.replace("#x", "")));
                            }
                            labelToEnvModel.put(modelTrue.label, modelTrue);
                        }
                    }
                }
                if (modelFalse != null && modelFalse.envData != null) {
                    modelFalse.label = nextInst(jumpFrom);
                    modelFalse.prevLabel = prevLabel;
                    labelToEnvModel.put(modelFalse.label, modelFalse);
                }
                decideToJump(modelTrue, modelFalse);
            } else if ("nop".equals(opcode)) {
                emulator.nop(suffix);
            } else if ("bic".equals(opcode)) {
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Integer im = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                if (im == null) {
                    emulator.bic(p0, p1, p2, suffix);
                } else {
                    emulator.bic(p0, p1, im, suffix);
                }
            } else if ("lsl".equals(opcode)) { 
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Integer im = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                emulator.lsl(p0, p1, p2, im, suffix);
            } else if ("lsr".equals(opcode)) { 
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Integer im = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                emulator.lsr(p0, p1, p2, im, suffix);
            } else if ("asr".equals(opcode)) {
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Integer im = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                emulator.asr(p0, p1, p2, im, suffix);
            } else if ("mul".equals(opcode)) { 
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Character p2 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                emulator.mul(p0, p1, p2, suffix);
            } else if ("mvn".equals(opcode)) {
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = arrParams[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Integer im = arrParams[1].contains("#") ? SysUtils.normalizeNumInParam(arrParams[1].trim()) : null;
                if (im == null) {
                    emulator.mvn(p0, p1, suffix);
                } else {
                    emulator.mvn(p0, im, suffix);
                }
            } else if ("mov".equals(opcode)) { 
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = arrParams[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Integer im = arrParams[1].contains("#") ? SysUtils.normalizeNumInParam(arrParams[1].trim()) : null;
                if (im == null) {
                    emulator.mov(p0, p1, suffix);
                } else {
                    emulator.movw(p0, im, suffix);
                }
            } else if ("movw".equals(opcode)) { 
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Integer im = SysUtils.normalizeNumInParam(arrParams[1].trim());
                emulator.movw(p0, im, suffix);
            } else if ("umull".equals(opcode)) { 
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Character p2 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                Character p3 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[3].trim()));
                emulator.umull(p0, p1, p2, p3);
            } else if ("smull".equals(opcode)) { 
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Character p2 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                Character p3 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[3].trim()));
                emulator.smull(p0, p1, p2, p3);
            } else if ("adc".equals(opcode)) { 
                Integer im;
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                if (arrParams[2].contains("#")) {
                    im = SysUtils.normalizeNumInParam(arrParams[2].trim());
                    emulator.adc(p0, p1, im, suffix);
                } else {
                    Character p2 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                    emulator.adc(p0, p1, p2, suffix);
                }
            } else if ("and".equals(opcode)) {
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Integer p2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                Character p2And = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                if (arrParams.length == 3) {
                    if (p2 != null) {
                        emulator.and(p0, p1, p2, suffix);
                    } else {
                        emulator.and(p0, p1, p2And, suffix);
                    }
                } else {
                    String[] extArr = arrParams[3].split("\\s+");
                    String extType = extArr[0];
                    Integer extraNum = null;
                    Character extraChar = null;
                    if (extArr.length > 1) {
                        if (extArr[1].contains("#")) {
                            extraNum = Integer.parseInt(extArr[1].replace("#", ""));
                        } else {
                            extraChar = Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                        }
                    }
                    emulator.and(p0, p1, p2And, p2, extType, extraChar, extraNum, suffix);
                }
            } else if ("rsb".equals(opcode)) {
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Integer im = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                if (arrParams.length == 3) {
                    if (im != null) {
                        emulator.rsb(p0, p1, im, suffix);
                    } else {
                        emulator.rsb(p0, p1, p2, suffix);
                    }
                } else {
                    String[] extArr = arrParams[3].split("\\s+");
                    String extType = extArr[0];
                    Integer extraNum = null;
                    Character extraChar = null;
                    if (extArr.length > 1) {
                        if (extArr[1].contains("#")) {
                            extraNum = Integer.parseInt(extArr[1].replace("#", ""));
                        } else {
                            extraChar = Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                        }
                    }
                    emulator.rsb(p0, p1, p2, im, extType, extraChar, extraNum, suffix);
                }
            } else if ("orr".equals(opcode)) {
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Integer im = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                if (arrParams.length == 3) {
                    if (im != null) {
                        emulator.orr(p0, p1, im, suffix);
                    } else {
                        emulator.orr(p0, p1, p2, suffix);
                    }
                } else {
                    String[] extArr = arrParams[3].split("\\s+");
                    String extType = extArr[0];
                    Integer extraNum = null;
                    Character extraChar = null;
                    if (extArr.length > 1) {
                        if (extArr[1].contains("#")) {
                            extraNum = Integer.parseInt(extArr[1].replace("#", ""));
                        } else {
                            extraChar = Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                        }
                    }
                    emulator.orr(p0, p1, p2, im, extType, extraChar, extraNum, suffix);
                }
            } else if ("eor".equals(opcode)) {
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Integer im = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                if (arrParams.length == 3) {
                    if (im != null) {
                        emulator.eor(p0, p1, im, suffix);
                    } else {
                        emulator.eor(p0, p1, p2, suffix);
                    }
                } else {
                    String[] extArr = arrParams[3].split("\\s+");
                    String extType = extArr[0];
                    Integer extraNum = null;
                    Character extraChar = null;
                    if (extArr.length > 1) {
                        if (extArr[1].contains("#")) {
                            extraNum = Integer.parseInt(extArr[1].replace("#", ""));
                        } else {
                            extraChar = Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                        }
                    }
                    emulator.eor(p0, p1, p2, im, extType, extraChar, extraNum, suffix);
                }
            } else if ("add".equals(opcode)) {
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Integer im = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                if (arrParams.length == 3) {
                    emulator.add(p0, p1, p2, im, suffix);
                } else {
                    String[] extArr = arrParams[3].split("\\s+");
                    String extType = extArr[0];
                    Integer extraNum = null;
                    Character extraChar = null;
                    if (extArr.length > 1) {
                        if (extArr[1].contains("#")) {
                            extraNum = Integer.parseInt(extArr[1].replace("#", ""));
                        } else {
                            extraChar = Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                        }
                    }
                    emulator.add(p0, p1, p2, im, extType, extraChar, extraNum, suffix);
                }
            } else if ("sub".equals(opcode)) {
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Integer im = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                if (arrParams.length == 3) {
                    emulator.sub(p0, p1, p2, im, suffix);
                } else {
                    String[] extArr = arrParams[3].split("\\s+");
                    String extType = extArr[0];
                    Integer extraNum = null;
                    Character extraChar = null;
                    if (extArr.length > 1) {
                        if (extArr[1].contains("#")) {
                            extraNum = Integer.parseInt(extArr[1].replace("#", ""));
                        } else {
                            extraChar = Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                        }
                    }
                    emulator.sub(p0, p1, p2, im, extType, extraChar, extraNum, suffix);
                }
            } else if ("cmp".equals(opcode)) {
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Integer im = arrParams[1].contains("#") ? SysUtils.normalizeNumInParam(arrParams[1].trim()) : null;
                Character p1 = arrParams[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                if (arrParams.length == 2) {
                    if (im != null) {
                        emulator.cmp(p0, im, suffix);
                    } else {
                        emulator.cmp(p0, p1, suffix);
                    }
                } else {
                    String[] extArr = arrParams[2].split("\\s+");
                    String extType = extArr[0];
                    Integer extraNum = null;
                    Character extraChar = null;
                    if (extArr.length > 1) {
                        if (extArr[1].contains("#")) {
                            extraNum = Integer.parseInt(extArr[1].replace("#", ""));
                        } else {
                            extraChar = Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                        }
                    }
                    emulator.cmp(p0, p1, im, extType, extraChar, extraNum, suffix);
                }
            } else if ("cmn".equals(opcode)) {
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Integer im = arrParams[1].contains("#") ? SysUtils.normalizeNumInParam(arrParams[1].trim()) : null;
                Character p1 = arrParams[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                if (arrParams.length == 2) {
                    if (im != null) {
                        emulator.cmn(p0, im, suffix);
                    } else {
                        emulator.cmn(p0, p1, suffix);
                    }
                } else {
                    String[] extArr = arrParams[2].split("\\s+");
                    String extType = extArr[0];
                    Integer extraNum = null;
                    Character extraChar = null;
                    if (extArr.length > 1) {
                        if (extArr[1].contains("#")) {
                            extraNum = Integer.parseInt(extArr[1].replace("#", ""));
                        } else {
                            extraChar = Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                        }
                    }
                    emulator.cmn(p0, p1, im, extType, extraChar, extraNum, suffix);
                }
            } else if ("svc".equals(opcode)) {
                Integer imSvc = arrParams[0].contains("#") ? SysUtils.normalizeNumInParam(arrParams[0].trim()) : null;
                emulator.svc(imSvc);
            } else if ("pop".equals(opcode)) {
                for (String p : arrParams) {
                    p = p.replace("{", "").replace("}", "");
                    Character c = Mapping.regStrToChar.get(SysUtils.normalizeRegName(p));
                    emulator.pop(c);
                }
            } else if ("push".equals(opcode)) {
                for (String p : arrParams) {
                    p = p.replace("{", "").replace("}", "");
                    Character c = Mapping.regStrToChar.get(SysUtils.normalizeRegName(p));
                    emulator.push(c);
                }
            } else if ("ldrb".equals(opcode)) {
                int type;
                if (arrParams.length == 2) {
                    type = 0;
                } else {
                    String lastP = arrParams[arrParams.length - 1];
                    type = lastP.endsWith("]") ? 1 : (lastP.endsWith("!") ? 2 : 3);
                }
                for (int k = 0; k < arrParams.length; k++) {
                    arrParams[k] = arrParams[k].trim().replace("[", "").replace("]", "").replace("!", "");
                }
                switch (type) {
                    case 0:
                        Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));
                        emulator.ldrb(p0, p1);
                        break;
                    case 1: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        Integer i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        BitVec extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        BitVec newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            newValue = emulator.add(newValue, emulator.handleExtra(extValue, extType, extraChar, extraNum));
                        }
                        emulator.ldrb(p0, newValue);
                        break;
                    case 2: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.add(newValue, extValue);
                            extValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);

                            newValue = emulator.add(newValue, handledExt);
                            extValue = emulator.add(newValue, handledExt);
                        }
                        emulator.ldrb(p0, newValue);
                        emulator.write(p1, extValue);
                        break;
                    case 3: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = newValue;
                            extValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);

                            newValue = newValue;
                            extValue = emulator.add(newValue, handledExt);
                        }
                        emulator.ldrb(p0, newValue);
                        emulator.write(p1, extValue);
                        break;
                    default:
                        break;
                }
            } else if ("ldr".equals(opcode)) {
                int type;
                if (arrParams.length == 2) {
                    type = 0;
                } else {
                    String lastP = arrParams[arrParams.length - 1];
                    type = lastP.endsWith("]") ? 1 : (lastP.endsWith("!") ? 2 : 3);
                }
                for (int k = 0; k < arrParams.length; k++) {
                    arrParams[k] = arrParams[k].trim().replace("[", "").replace("]", "").replace("!", "");
                }
                switch (type) {
                    case 0:
                        Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));
                        emulator.ldr(p0, p1);
                        break;
                    case 1: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        Integer i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        BitVec extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        BitVec newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            newValue = emulator.add(newValue, emulator.handleExtra(extValue, extType, extraChar, extraNum));
                        }
                        emulator.ldr(p0, newValue);
                        break;
                    case 2: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.add(newValue, extValue);
                            extValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);

                            newValue = emulator.add(newValue, handledExt);
                            extValue = emulator.add(newValue, handledExt);
                        }
                        emulator.ldr(p0, newValue);
                        emulator.write(p1, extValue);
                        break;
                    case 3: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = newValue;
                            extValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);

                            newValue = newValue;
                            extValue = emulator.add(newValue, handledExt);
                        }
                        emulator.ldr(p0, newValue);
                        emulator.write(p1, extValue);
                        break;
                    default:
                        break;
                }
            } else if ("ldrh".equals(opcode)) {
                int type;
                if (arrParams.length == 2) {
                    type = 0;
                } else {
                    String lastP = arrParams[arrParams.length - 1];
                    type = lastP.endsWith("]") ? 1 : (lastP.endsWith("!") ? 2 : 3);
                }
                for (int k = 0; k < arrParams.length; k++) {
                    arrParams[k] = arrParams[k].trim().replace("[", "").replace("]", "").replace("!", "");
                }
                switch (type) {
                    case 0:
                        Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));
                        emulator.ldrh(p0, p1);
                        break;
                    case 1: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        Integer i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        BitVec extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        BitVec newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            newValue = emulator.add(newValue, emulator.handleExtra(extValue, extType, extraChar, extraNum));
                        }
                        emulator.ldrh(p0, newValue);
                        break;
                    case 2: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.add(newValue, extValue);
                            extValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);

                            newValue = emulator.add(newValue, handledExt);
                            extValue = emulator.add(newValue, handledExt);
                        }
                        emulator.ldrh(p0, newValue);
                        emulator.write(p1, extValue);
                        break;
                    case 3: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = newValue;
                            extValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);

                            newValue = newValue;
                            extValue = emulator.add(newValue, handledExt);
                        }
                        emulator.ldrh(p0, newValue);
                        emulator.write(p1, extValue);
                        break;
                    default:
                        break;
                }
            } else if ("str".equals(opcode)) {
                int type;
                if (arrParams.length == 2) {
                    type = 0;
                } else {
                    String lastP = arrParams[arrParams.length - 1];
                    type = lastP.endsWith("]") ? 1 : (lastP.endsWith("!") ? 2 : 3);
                }
                for (int k = 0; k < arrParams.length; k++) {
                    arrParams[k] = arrParams[k].trim().replace("[", "").replace("]", "").replace("!", "");
                }
                switch (type) {
                    case 0:
                        Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));
                        emulator.str(emulator.val(p0), emulator.val(p1));
                        break;
                    case 1: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        Integer i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        BitVec extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        BitVec newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            newValue = emulator.add(newValue, emulator.handleExtra(extValue, extType, extraChar, extraNum));
                        }
                        emulator.str(emulator.val(p0), newValue);
                        break;
                    case 2: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.add(newValue, extValue);
                            extValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);

                            newValue = emulator.add(newValue, handledExt);
                            extValue = emulator.add(newValue, handledExt);
                        }
                        emulator.str(emulator.val(p0), newValue);
                        emulator.write(p1, extValue);
                        break;
                    case 3: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = newValue;
                            extValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);

                            newValue = newValue;
                            extValue = emulator.add(newValue, handledExt);
                        }
                        emulator.str(emulator.val(p0), newValue);
                        emulator.write(p1, extValue);
                        break;
                    default:
                        break;
                }
            } else if ("strb".equals(opcode)) {
                int type;
                if (arrParams.length == 2) {
                    type = 0;
                } else {
                    String lastP = arrParams[arrParams.length - 1];
                    type = lastP.endsWith("]") ? 1 : (lastP.endsWith("!") ? 2 : 3);
                }
                for (int k = 0; k < arrParams.length; k++) {
                    arrParams[k] = arrParams[k].trim().replace("[", "").replace("]", "").replace("!", "");
                }
                switch (type) {
                    case 0:
                        Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));
                        emulator.strb(emulator.val(p0), emulator.val(p1));
                        break;
                    case 1: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        Integer i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        BitVec extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        BitVec newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            newValue = emulator.add(newValue, emulator.handleExtra(extValue, extType, extraChar, extraNum));
                        }
                        emulator.strb(emulator.val(p0), newValue);
                        break;
                    case 2: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.add(newValue, extValue);
                            extValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);

                            newValue = emulator.add(newValue, handledExt);
                            extValue = emulator.add(newValue, handledExt);
                        }
                        emulator.strb(emulator.val(p0), newValue);
                        emulator.write(p1, extValue);
                        break;
                    case 3: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = newValue;
                            extValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);

                            newValue = newValue;
                            extValue = emulator.add(newValue, handledExt);
                        }
                        emulator.strb(emulator.val(p0), newValue);
                        emulator.write(p1, extValue);
                        break;
                    default:
                        break;
                }
            } else if ("strh".equals(opcode)) {
                int type;
                if (arrParams.length == 2) {
                    type = 0;
                } else {
                    String lastP = arrParams[arrParams.length - 1];
                    type = lastP.endsWith("]") ? 1 : (lastP.endsWith("!") ? 2 : 3);
                }
                for (int k = 0; k < arrParams.length; k++) {
                    arrParams[k] = arrParams[k].trim().replace("[", "").replace("]", "").replace("!", "");
                }
                switch (type) {
                    case 0:
                        Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));
                        emulator.strh(emulator.val(p0), emulator.val(p1));
                        break;
                    case 1: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        Integer i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        BitVec extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        BitVec newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            newValue = emulator.add(newValue, emulator.handleExtra(extValue, extType, extraChar, extraNum));
                        }
                        emulator.strh(emulator.val(p0), newValue);
                        break;
                    case 2: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.add(newValue, extValue);
                            extValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);

                            newValue = emulator.add(newValue, handledExt);
                            extValue = emulator.add(newValue, handledExt);
                        }
                        emulator.strh(emulator.val(p0), newValue);
                        emulator.write(p1, extValue);
                        break;
                    case 3: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = newValue;
                            extValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);

                            newValue = newValue;
                            extValue = emulator.add(newValue, handledExt);
                        }
                        emulator.strh(emulator.val(p0), newValue);
                        emulator.write(p1, extValue);
                        break;
                    default:

                        break;
                }
            }
        } else {
            Logs.infoLn("Error: Opcode is null!");
        }
    }

    private static void singleExec(M4 emulator, String prevLabel, AsmNode n) {
        String nLabel = n.getLabel();
        if (nLabel == null) System.exit(0);
        if (!nLabel.contains("+") && !nLabel.contains("-")) emulator.write('p', new BitVec(Integer.parseInt(nLabel)));
        Exporter.addAsm(nLabel + " " + n.getOpcode() + n.getCondSuffix() + (n.isUpdateFlag() ? "s" : "") + " " + n.getParams());
        Exporter.exportDot(Corana.inpFile + ".dot");

        Character suffix = n.isUpdateFlag() ? 's' : null;
        String[] arrParams = Objects.requireNonNull(n.getParams()).split("\\,");
        if (n.getOpcode() != null) {
            String opcode = n.getOpcode();
            if ("b".equals(opcode) || "bl".equals(opcode) || "bx".equals(opcode)) {
                Character condSuffix = Mapping.condStrToChar.get(Objects.requireNonNull(n.getCondSuffix()).toUpperCase());
                String preCond = recentPop == null ? "" : recentPop.getKey().pathCondition;
                jumpFrom = nLabel;
                EnvModel thisEnvModel = new EnvModel(jumpFrom, preCond);
                labelToEnvModel.put(thisEnvModel.label, thisEnvModel);

                String strLabel = null;
                Character charLabel = null;
                if (isConcreteLabel(arrParams[0])) {
                    strLabel = arrParams[0].contains("-") ? arrParams[0] :
                            String.valueOf(Arithmetic.hexToInt(arrParams[0].replace("#0x", "").replace("0x", "")));
                } else { 
                    charLabel = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                }
                Map.Entry<EnvModel, EnvModel> envPair;
                switch (opcode) {
                    case "b":
                        envPair = emulator.b(preCond, condSuffix, strLabel == null ? charLabel : strLabel);
                        break;
                    case "bl":
                        envPair = emulator.bl(Integer.parseInt(nLabel), preCond, condSuffix, strLabel == null ? charLabel : strLabel);
                        break;
                    case "bx":
                        envPair = emulator.bx(preCond, condSuffix, strLabel == null ? charLabel : strLabel);
                        break;
                    default:
                        envPair = null;
                        break;
                }
                EnvModel modelTrue = envPair.getKey();
                EnvModel modelFalse = envPair.getValue();
                if (isConcreteLabel(arrParams[0])) {
                    if (modelTrue != null && modelTrue.envData != null) { 
                        modelTrue.label = strLabel;
                        modelTrue.prevLabel = prevLabel;
                        labelToEnvModel.put(modelTrue.label, modelTrue);
                    }
                } else {
                    if (modelTrue != null && modelTrue.envData != null) { 
                        modelTrue.label = modelTrue.envData.eval;
                        modelTrue.prevLabel = prevLabel;
                        if (modelTrue.label == null) {
                            
                            
                            int foundLabel = Arithmetic.bitSetToInt(emulator.getEnv().register.regs.get('l').getVal());
                            if (foundLabel != 0 && foundLabel % 4 == 0) {
                                modelTrue.label = String.valueOf(foundLabel);
                                labelToEnvModel.put(modelTrue.label, modelTrue);
                                Logs.infoLn("\t-> Found the destination: " + foundLabel);
                            } else {
                                Logs.infoLn("\t-> Destination is undetectable.");
                            }
                        } else {
                            if (modelTrue.label.substring(0, 2).equals("#x")) {
                                modelTrue.label = String.valueOf(Arithmetic.hexToInt(modelTrue.label.replace("#x", "")));
                            }
                            labelToEnvModel.put(modelTrue.label, modelTrue);
                        }
                    }
                }
                if (modelFalse != null && modelFalse.envData != null) { 
                    modelFalse.label = nextInst(jumpFrom);
                    modelFalse.prevLabel = prevLabel;
                    labelToEnvModel.put(modelFalse.label, modelFalse);
                }
                decideToJump(modelTrue, modelFalse);
            } else if ("nop".equals(opcode)) {
                emulator.nop(suffix);
            } else if ("bic".equals(opcode)) {
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Integer im = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                if (im == null) {
                    emulator.bic(p0, p1, p2, suffix);
                } else {
                    emulator.bic(p0, p1, im, suffix);
                }
            } else if ("lsl".equals(opcode)) { 
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Integer im = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                emulator.lsl(p0, p1, p2, im, suffix);
            } else if ("lsr".equals(opcode)) { 
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Integer im = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                emulator.lsr(p0, p1, p2, im, suffix);
            } else if ("asr".equals(opcode)) {
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Integer im = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                emulator.asr(p0, p1, p2, im, suffix);
            } else if ("mul".equals(opcode)) { 
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Character p2 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                emulator.mul(p0, p1, p2, suffix);
            } else if ("mvn".equals(opcode)) {
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = arrParams[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Integer im = arrParams[1].contains("#") ? SysUtils.normalizeNumInParam(arrParams[1].trim()) : null;
                if (im == null) {
                    emulator.mvn(p0, p1, suffix);
                } else {
                    emulator.mvn(p0, im, suffix);
                }
            } else if ("mov".equals(opcode)) { 
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = arrParams[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Integer im = arrParams[1].contains("#") ? SysUtils.normalizeNumInParam(arrParams[1].trim()) : null;
                emulator.mov(p0, p1, im, suffix);
            } else if ("movw".equals(opcode)) { 
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Integer im = SysUtils.normalizeNumInParam(arrParams[1].trim());
                emulator.movw(p0, im, suffix);
            } else if ("umull".equals(opcode)) { 
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Character p2 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                Character p3 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[3].trim()));
                emulator.umull(p0, p1, p2, p3);
            } else if ("smull".equals(opcode)) { 
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Character p2 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                Character p3 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[3].trim()));
                emulator.smull(p0, p1, p2, p3);
            } else if ("adc".equals(opcode)) { 
                Integer im;
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                if (arrParams[2].contains("#")) {
                    im = SysUtils.normalizeNumInParam(arrParams[2].trim());
                    emulator.adc(p0, p1, im, suffix);
                } else {
                    Character p2 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                    emulator.adc(p0, p1, p2, suffix);
                }
            } else if ("and".equals(opcode)) {
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Integer p2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                Character p2And = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                if (arrParams.length == 3) {
                    if (p2 != null) {
                        emulator.and(p0, p1, p2, suffix);
                    } else {
                        emulator.and(p0, p1, p2And, suffix);
                    }
                } else {
                    String[] extArr = arrParams[3].split("\\s+");
                    String extType = extArr[0];
                    Integer extraNum = null;
                    Character extraChar = null;
                    if (extArr.length > 1) {
                        if (extArr[1].contains("#")) {
                            extraNum = Integer.parseInt(extArr[1].replace("#", ""));
                        } else {
                            extraChar = Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                        }
                    }
                    emulator.and(p0, p1, p2And, p2, extType, extraChar, extraNum, suffix);
                }
            } else if ("rsb".equals(opcode)) {
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Integer im = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                if (arrParams.length == 3) {
                    if (im != null) {
                        emulator.rsb(p0, p1, im, suffix);
                    } else {
                        emulator.rsb(p0, p1, p2, suffix);
                    }
                } else {
                    String[] extArr = arrParams[3].split("\\s+");
                    String extType = extArr[0];
                    Integer extraNum = null;
                    Character extraChar = null;
                    if (extArr.length > 1) {
                        if (extArr[1].contains("#")) {
                            extraNum = Integer.parseInt(extArr[1].replace("#", ""));
                        } else {
                            extraChar = Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                        }
                    }
                    emulator.rsb(p0, p1, p2, im, extType, extraChar, extraNum, suffix);
                }
            } else if ("orr".equals(opcode)) {
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Integer im = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                if (arrParams.length == 3) {
                    if (im != null) {
                        emulator.orr(p0, p1, im, suffix);
                    } else {
                        emulator.orr(p0, p1, p2, suffix);
                    }
                } else {
                    String[] extArr = arrParams[3].split("\\s+");
                    String extType = extArr[0];
                    Integer extraNum = null;
                    Character extraChar = null;
                    if (extArr.length > 1) {
                        if (extArr[1].contains("#")) {
                            extraNum = Integer.parseInt(extArr[1].replace("#", ""));
                        } else {
                            extraChar = Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                        }
                    }
                    emulator.orr(p0, p1, p2, im, extType, extraChar, extraNum, suffix);
                }
            } else if ("eor".equals(opcode)) {
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Integer im = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                if (arrParams.length == 3) {
                    if (im != null) {
                        emulator.eor(p0, p1, im, suffix);
                    } else {
                        emulator.eor(p0, p1, p2, suffix);
                    }
                } else {
                    String[] extArr = arrParams[3].split("\\s+");
                    String extType = extArr[0];
                    Integer extraNum = null;
                    Character extraChar = null;
                    if (extArr.length > 1) {
                        if (extArr[1].contains("#")) {
                            extraNum = Integer.parseInt(extArr[1].replace("#", ""));
                        } else {
                            extraChar = Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                        }
                    }
                    emulator.eor(p0, p1, p2, im, extType, extraChar, extraNum, suffix);
                }
            } else if ("add".equals(opcode)) {
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Integer im = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                if (arrParams.length == 3) {
                    emulator.add(p0, p1, p2, im, suffix);
                } else {
                    String[] extArr = arrParams[3].split("\\s+");
                    String extType = extArr[0];
                    Integer extraNum = null;
                    Character extraChar = null;
                    if (extArr.length > 1) {
                        if (extArr[1].contains("#")) {
                            extraNum = Integer.parseInt(extArr[1].replace("#", ""));
                        } else {
                            extraChar = Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                        }
                    }
                    emulator.add(p0, p1, p2, im, extType, extraChar, extraNum, suffix);
                }
            } else if ("sub".equals(opcode)) {
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Integer im = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                if (arrParams.length == 3) {
                    emulator.sub(p0, p1, p2, im, suffix);
                } else {
                    String[] extArr = arrParams[3].split("\\s+");
                    String extType = extArr[0];
                    Integer extraNum = null;
                    Character extraChar = null;
                    if (extArr.length > 1) {
                        if (extArr[1].contains("#")) {
                            extraNum = Integer.parseInt(extArr[1].replace("#", ""));
                        } else {
                            extraChar = Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                        }
                    }
                    emulator.sub(p0, p1, p2, im, extType, extraChar, extraNum, suffix);
                }
            } else if ("cmp".equals(opcode)) {
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Integer im = arrParams[1].contains("#") ? SysUtils.normalizeNumInParam(arrParams[1].trim()) : null;
                Character p1 = arrParams[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                if (arrParams.length == 2) {
                    if (im != null) {
                        emulator.cmp(p0, im, suffix);
                    } else {
                        emulator.cmp(p0, p1, suffix);
                    }
                } else {
                    String[] extArr = arrParams[2].split("\\s+");
                    String extType = extArr[0];
                    Integer extraNum = null;
                    Character extraChar = null;
                    if (extArr.length > 1) {
                        if (extArr[1].contains("#")) {
                            extraNum = Integer.parseInt(extArr[1].replace("#", ""));
                        } else {
                            extraChar = Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                        }
                    }
                    emulator.cmp(p0, p1, im, extType, extraChar, extraNum, suffix);
                }
            } else if ("cmn".equals(opcode)) {
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Integer im = arrParams[1].contains("#") ? SysUtils.normalizeNumInParam(arrParams[1].trim()) : null;
                Character p1 = arrParams[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                if (arrParams.length == 2) {
                    if (im != null) {
                        emulator.cmn(p0, im, suffix);
                    } else {
                        emulator.cmn(p0, p1, suffix);
                    }
                } else {
                    String[] extArr = arrParams[2].split("\\s+");
                    String extType = extArr[0];
                    Integer extraNum = null;
                    Character extraChar = null;
                    if (extArr.length > 1) {
                        if (extArr[1].contains("#")) {
                            extraNum = Integer.parseInt(extArr[1].replace("#", ""));
                        } else {
                            extraChar = Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                        }
                    }
                    emulator.cmn(p0, p1, im, extType, extraChar, extraNum, suffix);
                }
            } else if ("svc".equals(opcode)) {
                Integer imSvc = arrParams[0].contains("#") ? SysUtils.normalizeNumInParam(arrParams[0].trim()) : null;
                emulator.svc(imSvc);
            } else if ("pop".equals(opcode) || "vpop".equals(opcode)) {
                for (String p : arrParams) {
                    p = p.replace("{", "").replace("}", "");
                    Character c = Mapping.regStrToChar.get(SysUtils.normalizeRegName(p));
                    emulator.vpop(c);
                }
            } else if ("push".equals(opcode) || "vpush".equals(opcode)) {
                for (String p : arrParams) {
                    p = p.replace("{", "").replace("}", "");
                    Character c = Mapping.regStrToChar.get(SysUtils.normalizeRegName(p));
                    emulator.vpush(c);
                }
            } else if ("ldrb".equals(opcode)) {
                int type;
                if (arrParams.length == 2) {
                    type = 0;
                } else {
                    String lastP = arrParams[arrParams.length - 1];
                    type = lastP.endsWith("]") ? 1 : (lastP.endsWith("!") ? 2 : 3);
                }
                for (int k = 0; k < arrParams.length; k++) {
                    arrParams[k] = arrParams[k].trim().replace("[", "").replace("]", "").replace("!", "");
                }
                switch (type) {
                    case 0:
                        Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));
                        emulator.ldrb(p0, p1);
                        break;
                    case 1: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        Integer i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        BitVec extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        BitVec newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            newValue = emulator.add(newValue, emulator.handleExtra(extValue, extType, extraChar, extraNum));
                        }
                        emulator.ldrb(p0, newValue);
                        break;
                    case 2: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.add(newValue, extValue);
                            extValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);

                            newValue = emulator.add(newValue, handledExt);
                            extValue = emulator.add(newValue, handledExt);
                        }
                        emulator.ldrb(p0, newValue);
                        emulator.write(p1, extValue);
                        break;
                    case 3: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = newValue;
                            extValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);

                            newValue = newValue;
                            extValue = emulator.add(newValue, handledExt);
                        }
                        emulator.ldrb(p0, newValue);
                        emulator.write(p1, extValue);
                        break;
                    default:
                        break;
                }
            } else if ("ldr".equals(opcode)) {
                int type;
                if (arrParams.length == 2) {
                    type = 0;
                } else {
                    String lastP = arrParams[arrParams.length - 1];
                    type = lastP.endsWith("]") ? 1 : (lastP.endsWith("!") ? 2 : 3);
                }
                for (int k = 0; k < arrParams.length; k++) {
                    arrParams[k] = arrParams[k].trim().replace("[", "").replace("]", "").replace("!", "");
                }
                switch (type) {
                    case 0:
                        Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));
                        emulator.ldr(p0, p1);
                        break;
                    case 1: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        Integer i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        BitVec extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        BitVec newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            newValue = emulator.add(newValue, emulator.handleExtra(extValue, extType, extraChar, extraNum));
                        }
                        emulator.ldr(p0, newValue);
                        break;
                    case 2: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.add(newValue, extValue);
                            extValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);

                            newValue = emulator.add(newValue, handledExt);
                            extValue = emulator.add(newValue, handledExt);
                        }
                        emulator.ldr(p0, newValue);
                        emulator.write(p1, extValue);
                        break;
                    case 3: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = newValue;
                            extValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);

                            newValue = newValue;
                            extValue = emulator.add(newValue, handledExt);
                        }
                        emulator.ldr(p0, newValue);
                        emulator.write(p1, extValue);
                        break;
                    default:
                        break;
                }
            } else if ("ldrh".equals(opcode)) {
                int type;
                if (arrParams.length == 2) {
                    type = 0;
                } else {
                    String lastP = arrParams[arrParams.length - 1];
                    type = lastP.endsWith("]") ? 1 : (lastP.endsWith("!") ? 2 : 3);
                }
                for (int k = 0; k < arrParams.length; k++) {
                    arrParams[k] = arrParams[k].trim().replace("[", "").replace("]", "").replace("!", "");
                }
                switch (type) {
                    case 0:
                        Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));
                        emulator.ldrh(p0, p1);
                        break;
                    case 1: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        Integer i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        BitVec extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        BitVec newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            newValue = emulator.add(newValue, emulator.handleExtra(extValue, extType, extraChar, extraNum));
                        }
                        emulator.ldrh(p0, newValue);
                        break;
                    case 2: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.add(newValue, extValue);
                            extValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);

                            newValue = emulator.add(newValue, handledExt);
                            extValue = emulator.add(newValue, handledExt);
                        }
                        emulator.ldrh(p0, newValue);
                        emulator.write(p1, extValue);
                        break;
                    case 3: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = newValue;
                            extValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);

                            newValue = newValue;
                            extValue = emulator.add(newValue, handledExt);
                        }
                        emulator.ldrh(p0, newValue);
                        emulator.write(p1, extValue);
                        break;
                    default:
                        break;
                }
            } else if ("str".equals(opcode)) {
                int type;
                if (arrParams.length == 2) {
                    type = 0;
                } else {
                    String lastP = arrParams[arrParams.length - 1];
                    type = lastP.endsWith("]") ? 1 : (lastP.endsWith("!") ? 2 : 3);
                }
                for (int k = 0; k < arrParams.length; k++) {
                    arrParams[k] = arrParams[k].trim().replace("[", "").replace("]", "").replace("!", "");
                }
                switch (type) {
                    case 0:
                        Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));
                        emulator.str(emulator.val(p0), emulator.val(p1));
                        break;
                    case 1: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        Integer i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        BitVec extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        BitVec newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            newValue = emulator.add(newValue, emulator.handleExtra(extValue, extType, extraChar, extraNum));
                        }
                        emulator.str(emulator.val(p0), newValue);
                        break;
                    case 2: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.add(newValue, extValue);
                            extValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);

                            newValue = emulator.add(newValue, handledExt);
                            extValue = emulator.add(newValue, handledExt);
                        }
                        emulator.str(emulator.val(p0), newValue);
                        emulator.write(p1, extValue);
                        break;
                    case 3: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = newValue;
                            extValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);

                            newValue = newValue;
                            extValue = emulator.add(newValue, handledExt);
                        }
                        emulator.str(emulator.val(p0), newValue);
                        emulator.write(p1, extValue);
                        break;
                    default:
                        break;
                }
            } else if ("strb".equals(opcode)) {
                int type;
                if (arrParams.length == 2) {
                    type = 0;
                } else {
                    String lastP = arrParams[arrParams.length - 1];
                    type = lastP.endsWith("]") ? 1 : (lastP.endsWith("!") ? 2 : 3);
                }
                for (int k = 0; k < arrParams.length; k++) {
                    arrParams[k] = arrParams[k].trim().replace("[", "").replace("]", "").replace("!", "");
                }
                switch (type) {
                    case 0:
                        Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));
                        emulator.strb(emulator.val(p0), emulator.val(p1));
                        break;
                    case 1: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        Integer i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        BitVec extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        BitVec newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            newValue = emulator.add(newValue, emulator.handleExtra(extValue, extType, extraChar, extraNum));
                        }
                        emulator.strb(emulator.val(p0), newValue);
                        break;
                    case 2: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.add(newValue, extValue);
                            extValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);

                            newValue = emulator.add(newValue, handledExt);
                            extValue = emulator.add(newValue, handledExt);
                        }
                        emulator.strb(emulator.val(p0), newValue);
                        emulator.write(p1, extValue);
                        break;
                    case 3: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = newValue;
                            extValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);

                            newValue = newValue;
                            extValue = emulator.add(newValue, handledExt);
                        }
                        emulator.strb(emulator.val(p0), newValue);
                        emulator.write(p1, extValue);
                        break;
                    default:
                        break;
                }
            } else if ("strh".equals(opcode)) {
                int type;
                if (arrParams.length == 2) {
                    type = 0;
                } else {
                    String lastP = arrParams[arrParams.length - 1];
                    type = lastP.endsWith("]") ? 1 : (lastP.endsWith("!") ? 2 : 3);
                }
                for (int k = 0; k < arrParams.length; k++) {
                    arrParams[k] = arrParams[k].trim().replace("[", "").replace("]", "").replace("!", "");
                }
                switch (type) {
                    case 0:
                        Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));
                        emulator.strh(emulator.val(p0), emulator.val(p1));
                        break;
                    case 1: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        Integer i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        BitVec extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        BitVec newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            newValue = emulator.add(newValue, emulator.handleExtra(extValue, extType, extraChar, extraNum));
                        }
                        emulator.strh(emulator.val(p0), newValue);
                        break;
                    case 2: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.add(newValue, extValue);
                            extValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);

                            newValue = emulator.add(newValue, handledExt);
                            extValue = emulator.add(newValue, handledExt);
                        }
                        emulator.strh(emulator.val(p0), newValue);
                        emulator.write(p1, extValue);
                        break;
                    case 3: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = newValue;
                            extValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);

                            newValue = newValue;
                            extValue = emulator.add(newValue, handledExt);
                        }
                        emulator.strh(emulator.val(p0), newValue);
                        emulator.write(p1, extValue);
                        break;
                    default:

                        break;
                }
            }
        } else {
            Logs.infoLn("Error: Opcode is null!");
        }
    }

    private static void singleExec(M7 emulator, String prevLabel, AsmNode n) {
        String nLabel = n.getLabel();
        if (nLabel == null) System.exit(0);
        if (!nLabel.contains("+") && !nLabel.contains("-")) emulator.write('p', new BitVec(Integer.parseInt(nLabel)));
        Exporter.addAsm(nLabel + " " + n.getOpcode() + n.getCondSuffix() + (n.isUpdateFlag() ? "s" : "") + " " + n.getParams());
        Exporter.exportDot(Corana.inpFile + ".dot");

        Character suffix = n.isUpdateFlag() ? 's' : null;
        String[] arrParams = Objects.requireNonNull(n.getParams()).split("\\,");
        if (n.getOpcode() != null) {
            String opcode = n.getOpcode();
            if ("b".equals(opcode) || "bl".equals(opcode) || "bx".equals(opcode)) {
                Character condSuffix = Mapping.condStrToChar.get(Objects.requireNonNull(n.getCondSuffix()).toUpperCase());
                String preCond = recentPop == null ? "" : recentPop.getKey().pathCondition;
                jumpFrom = nLabel;
                EnvModel thisEnvModel = new EnvModel(jumpFrom, preCond);
                labelToEnvModel.put(thisEnvModel.label, thisEnvModel);

                String strLabel = null;
                Character charLabel = null;
                if (isConcreteLabel(arrParams[0])) {
                    strLabel = arrParams[0].contains("-") ? arrParams[0] :
                            String.valueOf(Arithmetic.hexToInt(arrParams[0].replace("#0x", "").replace("0x", "")));
                } else { 
                    charLabel = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                }
                Map.Entry<EnvModel, EnvModel> envPair;
                switch (opcode) {
                    case "b":
                        envPair = emulator.b(preCond, condSuffix, strLabel == null ? charLabel : strLabel);
                        break;
                    case "bl":
                        envPair = emulator.bl(Integer.parseInt(nLabel), preCond, condSuffix, strLabel == null ? charLabel : strLabel);
                        break;
                    case "bx":
                        envPair = emulator.bx(preCond, condSuffix, strLabel == null ? charLabel : strLabel);
                        break;
                    default:
                        envPair = null;
                        break;
                }
                EnvModel modelTrue = envPair.getKey();
                EnvModel modelFalse = envPair.getValue();
                if (isConcreteLabel(arrParams[0])) {
                    if (modelTrue != null && modelTrue.envData != null) { 
                        modelTrue.label = strLabel;
                        modelTrue.prevLabel = prevLabel;
                        labelToEnvModel.put(modelTrue.label, modelTrue);
                    }
                } else {
                    if (modelTrue != null && modelTrue.envData != null) { 
                        modelTrue.label = modelTrue.envData.eval;
                        modelTrue.prevLabel = prevLabel;
                        if (modelTrue.label == null) {
                            
                            
                            int foundLabel = Arithmetic.bitSetToInt(emulator.getEnv().register.regs.get('l').getVal());
                            if (foundLabel != 0 && foundLabel % 4 == 0) {
                                modelTrue.label = String.valueOf(foundLabel);
                                labelToEnvModel.put(modelTrue.label, modelTrue);
                                Logs.infoLn("\t-> Found the destination: " + foundLabel);
                            } else {
                                Logs.infoLn("\t-> Destination is undetectable.");
                            }
                        } else {
                            if (modelTrue.label.substring(0, 2).equals("#x")) {
                                modelTrue.label = String.valueOf(Arithmetic.hexToInt(modelTrue.label.replace("#x", "")));
                            }
                            labelToEnvModel.put(modelTrue.label, modelTrue);
                        }
                    }
                }
                if (modelFalse != null && modelFalse.envData != null) { 
                    modelFalse.label = nextInst(jumpFrom);
                    modelFalse.prevLabel = prevLabel;
                    labelToEnvModel.put(modelFalse.label, modelFalse);
                }
                decideToJump(modelTrue, modelFalse);
            } else if ("nop".equals(opcode)) {
                emulator.nop(suffix);
            } else if ("bic".equals(opcode)) {
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Integer im = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                if (im == null) {
                    emulator.bic(p0, p1, p2, suffix);
                } else {
                    emulator.bic(p0, p1, im, suffix);
                }
            } else if ("lsl".equals(opcode)) { 
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Integer im = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                emulator.lsl(p0, p1, p2, im, suffix);
            } else if ("lsr".equals(opcode)) { 
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Integer im = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                emulator.lsr(p0, p1, p2, im, suffix);
            } else if ("asr".equals(opcode)) {
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Integer im = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                emulator.asr(p0, p1, p2, im, suffix);
            } else if ("mul".equals(opcode)) { 
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Character p2 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                emulator.mul(p0, p1, p2, suffix);
            } else if ("mvn".equals(opcode)) {
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = arrParams[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Integer im = arrParams[1].contains("#") ? SysUtils.normalizeNumInParam(arrParams[1].trim()) : null;
                if (im == null) {
                    emulator.mvn(p0, p1, suffix);
                } else {
                    emulator.mvn(p0, im, suffix);
                }
            } else if ("mov".equals(opcode)) { 
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = arrParams[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Integer im = arrParams[1].contains("#") ? SysUtils.normalizeNumInParam(arrParams[1].trim()) : null;
                emulator.mov(p0, p1, im, suffix);
            } else if ("movw".equals(opcode)) { 
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Integer im = SysUtils.normalizeNumInParam(arrParams[1].trim());
                emulator.movw(p0, im, suffix);
            } else if ("umull".equals(opcode)) { 
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Character p2 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                Character p3 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[3].trim()));
                emulator.umull(p0, p1, p2, p3);
            } else if ("smull".equals(opcode)) { 
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Character p2 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                Character p3 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[3].trim()));
                emulator.smull(p0, p1, p2, p3);
            } else if ("adc".equals(opcode)) { 
                Integer im;
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                if (arrParams[2].contains("#")) {
                    im = SysUtils.normalizeNumInParam(arrParams[2].trim());
                    emulator.adc(p0, p1, im, suffix);
                } else {
                    Character p2 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                    emulator.adc(p0, p1, p2, suffix);
                }
            } else if ("and".equals(opcode)) {
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Integer p2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                Character p2And = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                if (arrParams.length == 3) {
                    if (p2 != null) {
                        emulator.and(p0, p1, p2, suffix);
                    } else {
                        emulator.and(p0, p1, p2And, suffix);
                    }
                } else {
                    String[] extArr = arrParams[3].split("\\s+");
                    String extType = extArr[0];
                    Integer extraNum = null;
                    Character extraChar = null;
                    if (extArr.length > 1) {
                        if (extArr[1].contains("#")) {
                            extraNum = Integer.parseInt(extArr[1].replace("#", ""));
                        } else {
                            extraChar = Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                        }
                    }
                    emulator.and(p0, p1, p2And, p2, extType, extraChar, extraNum, suffix);
                }
            } else if ("rsb".equals(opcode)) {
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Integer im = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                if (arrParams.length == 3) {
                    if (im != null) {
                        emulator.rsb(p0, p1, im, suffix);
                    } else {
                        emulator.rsb(p0, p1, p2, suffix);
                    }
                } else {
                    String[] extArr = arrParams[3].split("\\s+");
                    String extType = extArr[0];
                    Integer extraNum = null;
                    Character extraChar = null;
                    if (extArr.length > 1) {
                        if (extArr[1].contains("#")) {
                            extraNum = Integer.parseInt(extArr[1].replace("#", ""));
                        } else {
                            extraChar = Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                        }
                    }
                    emulator.rsb(p0, p1, p2, im, extType, extraChar, extraNum, suffix);
                }
            } else if ("orr".equals(opcode)) {
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Integer im = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                if (arrParams.length == 3) {
                    if (im != null) {
                        emulator.orr(p0, p1, im, suffix);
                    } else {
                        emulator.orr(p0, p1, p2, suffix);
                    }
                } else {
                    String[] extArr = arrParams[3].split("\\s+");
                    String extType = extArr[0];
                    Integer extraNum = null;
                    Character extraChar = null;
                    if (extArr.length > 1) {
                        if (extArr[1].contains("#")) {
                            extraNum = Integer.parseInt(extArr[1].replace("#", ""));
                        } else {
                            extraChar = Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                        }
                    }
                    emulator.orr(p0, p1, p2, im, extType, extraChar, extraNum, suffix);
                }
            } else if ("eor".equals(opcode)) {
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Integer im = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                if (arrParams.length == 3) {
                    if (im != null) {
                        emulator.eor(p0, p1, im, suffix);
                    } else {
                        emulator.eor(p0, p1, p2, suffix);
                    }
                } else {
                    String[] extArr = arrParams[3].split("\\s+");
                    String extType = extArr[0];
                    Integer extraNum = null;
                    Character extraChar = null;
                    if (extArr.length > 1) {
                        if (extArr[1].contains("#")) {
                            extraNum = Integer.parseInt(extArr[1].replace("#", ""));
                        } else {
                            extraChar = Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                        }
                    }
                    emulator.eor(p0, p1, p2, im, extType, extraChar, extraNum, suffix);
                }
            } else if ("add".equals(opcode)) {
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Integer im = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                if (arrParams.length == 3) {
                    emulator.add(p0, p1, p2, im, suffix);
                } else {
                    String[] extArr = arrParams[3].split("\\s+");
                    String extType = extArr[0];
                    Integer extraNum = null;
                    Character extraChar = null;
                    if (extArr.length > 1) {
                        if (extArr[1].contains("#")) {
                            extraNum = Integer.parseInt(extArr[1].replace("#", ""));
                        } else {
                            extraChar = Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                        }
                    }
                    emulator.add(p0, p1, p2, im, extType, extraChar, extraNum, suffix);
                }
            } else if ("sub".equals(opcode)) {
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Integer im = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                if (arrParams.length == 3) {
                    emulator.sub(p0, p1, p2, im, suffix);
                } else {
                    String[] extArr = arrParams[3].split("\\s+");
                    String extType = extArr[0];
                    Integer extraNum = null;
                    Character extraChar = null;
                    if (extArr.length > 1) {
                        if (extArr[1].contains("#")) {
                            extraNum = Integer.parseInt(extArr[1].replace("#", ""));
                        } else {
                            extraChar = Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                        }
                    }
                    emulator.sub(p0, p1, p2, im, extType, extraChar, extraNum, suffix);
                }
            } else if ("cmp".equals(opcode)) {
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Integer im = arrParams[1].contains("#") ? SysUtils.normalizeNumInParam(arrParams[1].trim()) : null;
                Character p1 = arrParams[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                if (arrParams.length == 2) {
                    if (im != null) {
                        emulator.cmp(p0, im, suffix);
                    } else {
                        emulator.cmp(p0, p1, suffix);
                    }
                } else {
                    String[] extArr = arrParams[2].split("\\s+");
                    String extType = extArr[0];
                    Integer extraNum = null;
                    Character extraChar = null;
                    if (extArr.length > 1) {
                        if (extArr[1].contains("#")) {
                            extraNum = Integer.parseInt(extArr[1].replace("#", ""));
                        } else {
                            extraChar = Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                        }
                    }
                    emulator.cmp(p0, p1, im, extType, extraChar, extraNum, suffix);
                }
            } else if ("cmn".equals(opcode)) {
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Integer im = arrParams[1].contains("#") ? SysUtils.normalizeNumInParam(arrParams[1].trim()) : null;
                Character p1 = arrParams[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                if (arrParams.length == 2) {
                    if (im != null) {
                        emulator.cmn(p0, im, suffix);
                    } else {
                        emulator.cmn(p0, p1, suffix);
                    }
                } else {
                    String[] extArr = arrParams[2].split("\\s+");
                    String extType = extArr[0];
                    Integer extraNum = null;
                    Character extraChar = null;
                    if (extArr.length > 1) {
                        if (extArr[1].contains("#")) {
                            extraNum = Integer.parseInt(extArr[1].replace("#", ""));
                        } else {
                            extraChar = Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                        }
                    }
                    emulator.cmn(p0, p1, im, extType, extraChar, extraNum, suffix);
                }
            } else if ("svc".equals(opcode)) {
                Integer imSvc = arrParams[0].contains("#") ? SysUtils.normalizeNumInParam(arrParams[0].trim()) : null;
                emulator.svc(imSvc);
            } else if ("pop".equals(opcode) || "vpop".equals(opcode)) {
                for (String p : arrParams) {
                    p = p.replace("{", "").replace("}", "");
                    Character c = Mapping.regStrToChar.get(SysUtils.normalizeRegName(p));
                    emulator.vpop(c);
                }
            } else if ("push".equals(opcode) || "vpush".equals(opcode)) {
                for (String p : arrParams) {
                    p = p.replace("{", "").replace("}", "");
                    Character c = Mapping.regStrToChar.get(SysUtils.normalizeRegName(p));
                    emulator.vpush(c);
                }
            } else if ("ldrb".equals(opcode)) {
                int type;
                if (arrParams.length == 2) {
                    type = 0;
                } else {
                    String lastP = arrParams[arrParams.length - 1];
                    type = lastP.endsWith("]") ? 1 : (lastP.endsWith("!") ? 2 : 3);
                }
                for (int k = 0; k < arrParams.length; k++) {
                    arrParams[k] = arrParams[k].trim().replace("[", "").replace("]", "").replace("!", "");
                }
                switch (type) {
                    case 0:
                        Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));
                        emulator.ldrb(p0, p1);
                        break;
                    case 1: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        Integer i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        BitVec extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        BitVec newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            newValue = emulator.add(newValue, emulator.handleExtra(extValue, extType, extraChar, extraNum));
                        }
                        emulator.ldrb(p0, newValue);
                        break;
                    case 2: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.add(newValue, extValue);
                            extValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);

                            newValue = emulator.add(newValue, handledExt);
                            extValue = emulator.add(newValue, handledExt);
                        }
                        emulator.ldrb(p0, newValue);
                        emulator.write(p1, extValue);
                        break;
                    case 3: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = newValue;
                            extValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);

                            newValue = newValue;
                            extValue = emulator.add(newValue, handledExt);
                        }
                        emulator.ldrb(p0, newValue);
                        emulator.write(p1, extValue);
                        break;
                    default:
                        break;
                }
            } else if ("ldr".equals(opcode)) {
                int type;
                if (arrParams.length == 2) {
                    type = 0;
                } else {
                    String lastP = arrParams[arrParams.length - 1];
                    type = lastP.endsWith("]") ? 1 : (lastP.endsWith("!") ? 2 : 3);
                }
                for (int k = 0; k < arrParams.length; k++) {
                    arrParams[k] = arrParams[k].trim().replace("[", "").replace("]", "").replace("!", "");
                }
                switch (type) {
                    case 0:
                        Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));
                        emulator.ldr(p0, p1);
                        break;
                    case 1: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        Integer i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        BitVec extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        BitVec newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            newValue = emulator.add(newValue, emulator.handleExtra(extValue, extType, extraChar, extraNum));
                        }
                        emulator.ldr(p0, newValue);
                        break;
                    case 2: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.add(newValue, extValue);
                            extValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);

                            newValue = emulator.add(newValue, handledExt);
                            extValue = emulator.add(newValue, handledExt);
                        }
                        emulator.ldr(p0, newValue);
                        emulator.write(p1, extValue);
                        break;
                    case 3: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = newValue;
                            extValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);

                            newValue = newValue;
                            extValue = emulator.add(newValue, handledExt);
                        }
                        emulator.ldr(p0, newValue);
                        emulator.write(p1, extValue);
                        break;
                    default:
                        break;
                }
            } else if ("ldrh".equals(opcode)) {
                int type;
                if (arrParams.length == 2) {
                    type = 0;
                } else {
                    String lastP = arrParams[arrParams.length - 1];
                    type = lastP.endsWith("]") ? 1 : (lastP.endsWith("!") ? 2 : 3);
                }
                for (int k = 0; k < arrParams.length; k++) {
                    arrParams[k] = arrParams[k].trim().replace("[", "").replace("]", "").replace("!", "");
                }
                switch (type) {
                    case 0:
                        Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));
                        emulator.ldrh(p0, p1);
                        break;
                    case 1: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        Integer i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        BitVec extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        BitVec newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            newValue = emulator.add(newValue, emulator.handleExtra(extValue, extType, extraChar, extraNum));
                        }
                        emulator.ldrh(p0, newValue);
                        break;
                    case 2: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.add(newValue, extValue);
                            extValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);

                            newValue = emulator.add(newValue, handledExt);
                            extValue = emulator.add(newValue, handledExt);
                        }
                        emulator.ldrh(p0, newValue);
                        emulator.write(p1, extValue);
                        break;
                    case 3: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = newValue;
                            extValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);

                            newValue = newValue;
                            extValue = emulator.add(newValue, handledExt);
                        }
                        emulator.ldrh(p0, newValue);
                        emulator.write(p1, extValue);
                        break;
                    default:
                        break;
                }
            } else if ("str".equals(opcode)) {
                int type;
                if (arrParams.length == 2) {
                    type = 0;
                } else {
                    String lastP = arrParams[arrParams.length - 1];
                    type = lastP.endsWith("]") ? 1 : (lastP.endsWith("!") ? 2 : 3);
                }
                for (int k = 0; k < arrParams.length; k++) {
                    arrParams[k] = arrParams[k].trim().replace("[", "").replace("]", "").replace("!", "");
                }
                switch (type) {
                    case 0:
                        Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));
                        emulator.str(emulator.val(p0), emulator.val(p1));
                        break;
                    case 1: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        Integer i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        BitVec extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        BitVec newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            newValue = emulator.add(newValue, emulator.handleExtra(extValue, extType, extraChar, extraNum));
                        }
                        emulator.str(emulator.val(p0), newValue);
                        break;
                    case 2: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.add(newValue, extValue);
                            extValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);

                            newValue = emulator.add(newValue, handledExt);
                            extValue = emulator.add(newValue, handledExt);
                        }
                        emulator.str(emulator.val(p0), newValue);
                        emulator.write(p1, extValue);
                        break;
                    case 3: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = newValue;
                            extValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);

                            newValue = newValue;
                            extValue = emulator.add(newValue, handledExt);
                        }
                        emulator.str(emulator.val(p0), newValue);
                        emulator.write(p1, extValue);
                        break;
                    default:
                        break;
                }
            } else if ("strb".equals(opcode)) {
                int type;
                if (arrParams.length == 2) {
                    type = 0;
                } else {
                    String lastP = arrParams[arrParams.length - 1];
                    type = lastP.endsWith("]") ? 1 : (lastP.endsWith("!") ? 2 : 3);
                }
                for (int k = 0; k < arrParams.length; k++) {
                    arrParams[k] = arrParams[k].trim().replace("[", "").replace("]", "").replace("!", "");
                }
                switch (type) {
                    case 0:
                        Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));
                        emulator.strb(emulator.val(p0), emulator.val(p1));
                        break;
                    case 1: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        Integer i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        BitVec extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        BitVec newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            newValue = emulator.add(newValue, emulator.handleExtra(extValue, extType, extraChar, extraNum));
                        }
                        emulator.strb(emulator.val(p0), newValue);
                        break;
                    case 2: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.add(newValue, extValue);
                            extValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);

                            newValue = emulator.add(newValue, handledExt);
                            extValue = emulator.add(newValue, handledExt);
                        }
                        emulator.strb(emulator.val(p0), newValue);
                        emulator.write(p1, extValue);
                        break;
                    case 3: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = newValue;
                            extValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);

                            newValue = newValue;
                            extValue = emulator.add(newValue, handledExt);
                        }
                        emulator.strb(emulator.val(p0), newValue);
                        emulator.write(p1, extValue);
                        break;
                    default:
                        break;
                }
            } else if ("strh".equals(opcode)) {
                int type;
                if (arrParams.length == 2) {
                    type = 0;
                } else {
                    String lastP = arrParams[arrParams.length - 1];
                    type = lastP.endsWith("]") ? 1 : (lastP.endsWith("!") ? 2 : 3);
                }
                for (int k = 0; k < arrParams.length; k++) {
                    arrParams[k] = arrParams[k].trim().replace("[", "").replace("]", "").replace("!", "");
                }
                switch (type) {
                    case 0:
                        Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));
                        emulator.strh(emulator.val(p0), emulator.val(p1));
                        break;
                    case 1: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        Integer i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        BitVec extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        BitVec newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            newValue = emulator.add(newValue, emulator.handleExtra(extValue, extType, extraChar, extraNum));
                        }
                        emulator.strh(emulator.val(p0), newValue);
                        break;
                    case 2: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.add(newValue, extValue);
                            extValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);

                            newValue = emulator.add(newValue, handledExt);
                            extValue = emulator.add(newValue, handledExt);
                        }
                        emulator.strh(emulator.val(p0), newValue);
                        emulator.write(p1, extValue);
                        break;
                    case 3: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = newValue;
                            extValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);

                            newValue = newValue;
                            extValue = emulator.add(newValue, handledExt);
                        }
                        emulator.strh(emulator.val(p0), newValue);
                        emulator.write(p1, extValue);
                        break;
                    default:

                        break;
                }
            }
        } else {
            Logs.infoLn("Error: Opcode is null!");
        }
    }

    private static void singleExec(M33 emulator, String prevLabel, AsmNode n) {
        String nLabel = n.getLabel();
        if (nLabel == null) System.exit(0);
        if (!nLabel.contains("+") && !nLabel.contains("-")) emulator.write('p', new BitVec(Integer.parseInt(nLabel)));
        Exporter.addAsm(nLabel + " " + n.getOpcode() + n.getCondSuffix() + (n.isUpdateFlag() ? "s" : "") + " " + n.getParams());
        Exporter.exportDot(Corana.inpFile + ".dot");

        Character suffix = n.isUpdateFlag() ? 's' : null;
        String[] arrParams = Objects.requireNonNull(n.getParams()).split("\\,");
        if (n.getOpcode() != null) {
            String opcode = n.getOpcode();
            if ("b".equals(opcode) || "bl".equals(opcode) || "bx".equals(opcode)) {
                Character condSuffix = Mapping.condStrToChar.get(Objects.requireNonNull(n.getCondSuffix()).toUpperCase());
                String preCond = recentPop == null ? "" : recentPop.getKey().pathCondition;
                jumpFrom = nLabel;
                EnvModel thisEnvModel = new EnvModel(jumpFrom, preCond);
                labelToEnvModel.put(thisEnvModel.label, thisEnvModel);

                String strLabel = null;
                Character charLabel = null;
                if (isConcreteLabel(arrParams[0])) {
                    strLabel = arrParams[0].contains("-") ? arrParams[0] :
                            String.valueOf(Arithmetic.hexToInt(arrParams[0].replace("#0x", "").replace("0x", "")));
                } else { 
                    charLabel = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                }
                Map.Entry<EnvModel, EnvModel> envPair;
                switch (opcode) {
                    case "b":
                        envPair = emulator.b(preCond, condSuffix, strLabel == null ? charLabel : strLabel);
                        break;
                    case "bl":
                        envPair = emulator.bl(Integer.parseInt(nLabel), preCond, condSuffix, strLabel == null ? charLabel : strLabel);
                        break;
                    case "bx":
                        envPair = emulator.bx(preCond, condSuffix, strLabel == null ? charLabel : strLabel);
                        break;
                    default:
                        envPair = null;
                        break;
                }
                EnvModel modelTrue = envPair.getKey();
                EnvModel modelFalse = envPair.getValue();
                if (isConcreteLabel(arrParams[0])) {
                    if (modelTrue != null && modelTrue.envData != null) { 
                        modelTrue.label = strLabel;
                        modelTrue.prevLabel = prevLabel;
                        labelToEnvModel.put(modelTrue.label, modelTrue);
                    }
                } else {
                    if (modelTrue != null && modelTrue.envData != null) { 
                        modelTrue.label = modelTrue.envData.eval;
                        modelTrue.prevLabel = prevLabel;
                        if (modelTrue.label == null) {
                            int foundLabel = Arithmetic.bitSetToInt(emulator.getEnv().register.regs.get('l').getVal());
                            if (foundLabel != 0 && foundLabel % 4 == 0) {
                                modelTrue.label = String.valueOf(foundLabel);
                                labelToEnvModel.put(modelTrue.label, modelTrue);
                                Logs.infoLn("\t-> Found the destination: " + foundLabel);
                            } else {
                                Logs.infoLn("\t-> Destination is undetectable.");
                            }
                        } else {
                            if (modelTrue.label.substring(0, 2).equals("#x")) {
                                modelTrue.label = String.valueOf(Arithmetic.hexToInt(modelTrue.label.replace("#x", "")));
                            }
                            labelToEnvModel.put(modelTrue.label, modelTrue);
                        }
                    }
                }
                if (modelFalse != null && modelFalse.envData != null) { 
                    modelFalse.label = nextInst(jumpFrom);
                    modelFalse.prevLabel = prevLabel;
                    labelToEnvModel.put(modelFalse.label, modelFalse);
                }
                decideToJump(modelTrue, modelFalse);
            } else if ("nop".equals(opcode)) {
                emulator.nop(suffix);
            } else if ("bic".equals(opcode)) {
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Integer im = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                if (im == null) {
                    emulator.bic(p0, p1, p2, suffix);
                } else {
                    emulator.bic(p0, p1, im, suffix);
                }
            } else if ("lsl".equals(opcode)) { 
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Integer im = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                emulator.lsl(p0, p1, p2, im, suffix);
            } else if ("lsr".equals(opcode)) { 
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Integer im = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                emulator.lsr(p0, p1, p2, im, suffix);
            } else if ("asr".equals(opcode)) {
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Integer im = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                emulator.asr(p0, p1, p2, im, suffix);
            } else if ("mul".equals(opcode)) { 
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Character p2 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                emulator.mul(p0, p1, p2, suffix);
            } else if ("mvn".equals(opcode)) {
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = arrParams[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Integer im = arrParams[1].contains("#") ? SysUtils.normalizeNumInParam(arrParams[1].trim()) : null;
                if (im == null) {
                    emulator.mvn(p0, p1, suffix);
                } else {
                    emulator.mvn(p0, im, suffix);
                }
            } else if ("mov".equals(opcode)) { 
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = arrParams[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Integer im = arrParams[1].contains("#") ? SysUtils.normalizeNumInParam(arrParams[1].trim()) : null;
                emulator.mov(p0, p1, im, suffix);
            } else if ("movw".equals(opcode)) { 
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Integer im = SysUtils.normalizeNumInParam(arrParams[1].trim());
                emulator.movw(p0, im, suffix);
            } else if ("umull".equals(opcode)) { 
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Character p2 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                Character p3 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[3].trim()));
                emulator.umull(p0, p1, p2, p3);
            } else if ("smull".equals(opcode)) { 
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Character p2 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                Character p3 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[3].trim()));
                emulator.smull(p0, p1, p2, p3);
            } else if ("adc".equals(opcode)) { 
                Integer im;
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                if (arrParams[2].contains("#")) {
                    im = SysUtils.normalizeNumInParam(arrParams[2].trim());
                    emulator.adc(p0, p1, im, suffix);
                } else {
                    Character p2 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                    emulator.adc(p0, p1, p2, suffix);
                }
            } else if ("and".equals(opcode)) {
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Integer p2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                Character p2And = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                if (arrParams.length == 3) {
                    if (p2 != null) {
                        emulator.and(p0, p1, p2, suffix);
                    } else {
                        emulator.and(p0, p1, p2And, suffix);
                    }
                } else {
                    String[] extArr = arrParams[3].split("\\s+");
                    String extType = extArr[0];
                    Integer extraNum = null;
                    Character extraChar = null;
                    if (extArr.length > 1) {
                        if (extArr[1].contains("#")) {
                            extraNum = Integer.parseInt(extArr[1].replace("#", ""));
                        } else {
                            extraChar = Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                        }
                    }
                    emulator.and(p0, p1, p2And, p2, extType, extraChar, extraNum, suffix);
                }
            } else if ("rsb".equals(opcode)) {
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Integer im = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                if (arrParams.length == 3) {
                    if (im != null) {
                        emulator.rsb(p0, p1, im, suffix);
                    } else {
                        emulator.rsb(p0, p1, p2, suffix);
                    }
                } else {
                    String[] extArr = arrParams[3].split("\\s+");
                    String extType = extArr[0];
                    Integer extraNum = null;
                    Character extraChar = null;
                    if (extArr.length > 1) {
                        if (extArr[1].contains("#")) {
                            extraNum = Integer.parseInt(extArr[1].replace("#", ""));
                        } else {
                            extraChar = Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                        }
                    }
                    emulator.rsb(p0, p1, p2, im, extType, extraChar, extraNum, suffix);
                }
            } else if ("orr".equals(opcode)) {
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Integer im = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                if (arrParams.length == 3) {
                    if (im != null) {
                        emulator.orr(p0, p1, im, suffix);
                    } else {
                        emulator.orr(p0, p1, p2, suffix);
                    }
                } else {
                    String[] extArr = arrParams[3].split("\\s+");
                    String extType = extArr[0];
                    Integer extraNum = null;
                    Character extraChar = null;
                    if (extArr.length > 1) {
                        if (extArr[1].contains("#")) {
                            extraNum = Integer.parseInt(extArr[1].replace("#", ""));
                        } else {
                            extraChar = Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                        }
                    }
                    emulator.orr(p0, p1, p2, im, extType, extraChar, extraNum, suffix);
                }
            } else if ("eor".equals(opcode)) {
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Integer im = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                if (arrParams.length == 3) {
                    if (im != null) {
                        emulator.eor(p0, p1, im, suffix);
                    } else {
                        emulator.eor(p0, p1, p2, suffix);
                    }
                } else {
                    String[] extArr = arrParams[3].split("\\s+");
                    String extType = extArr[0];
                    Integer extraNum = null;
                    Character extraChar = null;
                    if (extArr.length > 1) {
                        if (extArr[1].contains("#")) {
                            extraNum = Integer.parseInt(extArr[1].replace("#", ""));
                        } else {
                            extraChar = Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                        }
                    }
                    emulator.eor(p0, p1, p2, im, extType, extraChar, extraNum, suffix);
                }
            } else if ("add".equals(opcode)) {
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Integer im = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                if (arrParams.length == 3) {
                    emulator.add(p0, p1, p2, im, suffix);
                } else {
                    String[] extArr = arrParams[3].split("\\s+");
                    String extType = extArr[0];
                    Integer extraNum = null;
                    Character extraChar = null;
                    if (extArr.length > 1) {
                        if (extArr[1].contains("#")) {
                            extraNum = Integer.parseInt(extArr[1].replace("#", ""));
                        } else {
                            extraChar = Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                        }
                    }
                    emulator.add(p0, p1, p2, im, extType, extraChar, extraNum, suffix);
                }
            } else if ("sub".equals(opcode)) {
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                Integer im = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                if (arrParams.length == 3) {
                    emulator.sub(p0, p1, p2, im, suffix);
                } else {
                    String[] extArr = arrParams[3].split("\\s+");
                    String extType = extArr[0];
                    Integer extraNum = null;
                    Character extraChar = null;
                    if (extArr.length > 1) {
                        if (extArr[1].contains("#")) {
                            extraNum = Integer.parseInt(extArr[1].replace("#", ""));
                        } else {
                            extraChar = Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                        }
                    }
                    emulator.sub(p0, p1, p2, im, extType, extraChar, extraNum, suffix);
                }
            } else if ("cmp".equals(opcode)) {
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Integer im = arrParams[1].contains("#") ? SysUtils.normalizeNumInParam(arrParams[1].trim()) : null;
                Character p1 = arrParams[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                if (arrParams.length == 2) {
                    if (im != null) {
                        emulator.cmp(p0, im, suffix);
                    } else {
                        emulator.cmp(p0, p1, suffix);
                    }
                } else {
                    String[] extArr = arrParams[2].split("\\s+");
                    String extType = extArr[0];
                    Integer extraNum = null;
                    Character extraChar = null;
                    if (extArr.length > 1) {
                        if (extArr[1].contains("#")) {
                            extraNum = Integer.parseInt(extArr[1].replace("#", ""));
                        } else {
                            extraChar = Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                        }
                    }
                    emulator.cmp(p0, p1, im, extType, extraChar, extraNum, suffix);
                }
            } else if ("cmn".equals(opcode)) {
                Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0].trim()));
                Integer im = arrParams[1].contains("#") ? SysUtils.normalizeNumInParam(arrParams[1].trim()) : null;
                Character p1 = arrParams[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1].trim()));
                if (arrParams.length == 2) {
                    if (im != null) {
                        emulator.cmn(p0, im, suffix);
                    } else {
                        emulator.cmn(p0, p1, suffix);
                    }
                } else {
                    String[] extArr = arrParams[2].split("\\s+");
                    String extType = extArr[0];
                    Integer extraNum = null;
                    Character extraChar = null;
                    if (extArr.length > 1) {
                        if (extArr[1].contains("#")) {
                            extraNum = Integer.parseInt(extArr[1].replace("#", ""));
                        } else {
                            extraChar = Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                        }
                    }
                    emulator.cmn(p0, p1, im, extType, extraChar, extraNum, suffix);
                }
            } else if ("svc".equals(opcode)) {
                Integer imSvc = arrParams[0].contains("#") ? SysUtils.normalizeNumInParam(arrParams[0].trim()) : null;
                emulator.svc(imSvc);
            } else if ("pop".equals(opcode) || "vpop".equals(opcode)) {
                for (String p : arrParams) {
                    p = p.replace("{", "").replace("}", "");
                    Character c = Mapping.regStrToChar.get(SysUtils.normalizeRegName(p));
                    emulator.vpop(c);
                }
            } else if ("push".equals(opcode) || "vpush".equals(opcode)) {
                for (String p : arrParams) {
                    p = p.replace("{", "").replace("}", "");
                    Character c = Mapping.regStrToChar.get(SysUtils.normalizeRegName(p));
                    emulator.vpush(c);
                }
            } else if ("ldrb".equals(opcode)) {
                int type;
                if (arrParams.length == 2) {
                    type = 0;
                } else {
                    String lastP = arrParams[arrParams.length - 1];
                    type = lastP.endsWith("]") ? 1 : (lastP.endsWith("!") ? 2 : 3);
                }
                for (int k = 0; k < arrParams.length; k++) {
                    arrParams[k] = arrParams[k].trim().replace("[", "").replace("]", "").replace("!", "");
                }
                switch (type) {
                    case 0:
                        Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));
                        emulator.ldrb(p0, p1);
                        break;
                    case 1: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        Integer i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        BitVec extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        BitVec newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            newValue = emulator.add(newValue, emulator.handleExtra(extValue, extType, extraChar, extraNum));
                        }
                        emulator.ldrb(p0, newValue);
                        break;
                    case 2: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.add(newValue, extValue);
                            extValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);

                            newValue = emulator.add(newValue, handledExt);
                            extValue = emulator.add(newValue, handledExt);
                        }
                        emulator.ldrb(p0, newValue);
                        emulator.write(p1, extValue);
                        break;
                    case 3: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = newValue;
                            extValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);

                            newValue = newValue;
                            extValue = emulator.add(newValue, handledExt);
                        }
                        emulator.ldrb(p0, newValue);
                        emulator.write(p1, extValue);
                        break;
                    default:
                        break;
                }
            } else if ("ldr".equals(opcode)) {
                int type;
                if (arrParams.length == 2) {
                    type = 0;
                } else {
                    String lastP = arrParams[arrParams.length - 1];
                    type = lastP.endsWith("]") ? 1 : (lastP.endsWith("!") ? 2 : 3);
                }
                for (int k = 0; k < arrParams.length; k++) {
                    arrParams[k] = arrParams[k].trim().replace("[", "").replace("]", "").replace("!", "");
                }
                switch (type) {
                    case 0:
                        Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));
                        emulator.ldr(p0, p1);
                        break;
                    case 1: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        Integer i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        BitVec extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        BitVec newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            newValue = emulator.add(newValue, emulator.handleExtra(extValue, extType, extraChar, extraNum));
                        }
                        emulator.ldr(p0, newValue);
                        break;
                    case 2: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.add(newValue, extValue);
                            extValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);

                            newValue = emulator.add(newValue, handledExt);
                            extValue = emulator.add(newValue, handledExt);
                        }
                        emulator.ldr(p0, newValue);
                        emulator.write(p1, extValue);
                        break;
                    case 3: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = newValue;
                            extValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);

                            newValue = newValue;
                            extValue = emulator.add(newValue, handledExt);
                        }
                        emulator.ldr(p0, newValue);
                        emulator.write(p1, extValue);
                        break;
                    default:
                        break;
                }
            } else if ("ldrh".equals(opcode)) {
                int type;
                if (arrParams.length == 2) {
                    type = 0;
                } else {
                    String lastP = arrParams[arrParams.length - 1];
                    type = lastP.endsWith("]") ? 1 : (lastP.endsWith("!") ? 2 : 3);
                }
                for (int k = 0; k < arrParams.length; k++) {
                    arrParams[k] = arrParams[k].trim().replace("[", "").replace("]", "").replace("!", "");
                }
                switch (type) {
                    case 0:
                        Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));
                        emulator.ldrh(p0, p1);
                        break;
                    case 1: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        Integer i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        BitVec extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        BitVec newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            newValue = emulator.add(newValue, emulator.handleExtra(extValue, extType, extraChar, extraNum));
                        }
                        emulator.ldrh(p0, newValue);
                        break;
                    case 2: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.add(newValue, extValue);
                            extValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);

                            newValue = emulator.add(newValue, handledExt);
                            extValue = emulator.add(newValue, handledExt);
                        }
                        emulator.ldrh(p0, newValue);
                        emulator.write(p1, extValue);
                        break;
                    case 3: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = newValue;
                            extValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);

                            newValue = newValue;
                            extValue = emulator.add(newValue, handledExt);
                        }
                        emulator.ldrh(p0, newValue);
                        emulator.write(p1, extValue);
                        break;
                    default:
                        break;
                }
            } else if ("str".equals(opcode)) {
                int type;
                if (arrParams.length == 2) {
                    type = 0;
                } else {
                    String lastP = arrParams[arrParams.length - 1];
                    type = lastP.endsWith("]") ? 1 : (lastP.endsWith("!") ? 2 : 3);
                }
                for (int k = 0; k < arrParams.length; k++) {
                    arrParams[k] = arrParams[k].trim().replace("[", "").replace("]", "").replace("!", "");
                }
                switch (type) {
                    case 0:
                        Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));
                        emulator.str(emulator.val(p0), emulator.val(p1));
                        break;
                    case 1: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        Integer i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        BitVec extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        BitVec newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            newValue = emulator.add(newValue, emulator.handleExtra(extValue, extType, extraChar, extraNum));
                        }
                        emulator.str(emulator.val(p0), newValue);
                        break;
                    case 2: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.add(newValue, extValue);
                            extValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);

                            newValue = emulator.add(newValue, handledExt);
                            extValue = emulator.add(newValue, handledExt);
                        }
                        emulator.str(emulator.val(p0), newValue);
                        emulator.write(p1, extValue);
                        break;
                    case 3: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = newValue;
                            extValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);

                            newValue = newValue;
                            extValue = emulator.add(newValue, handledExt);
                        }
                        emulator.str(emulator.val(p0), newValue);
                        emulator.write(p1, extValue);
                        break;
                    default:
                        break;
                }
            } else if ("strb".equals(opcode)) {
                int type;
                if (arrParams.length == 2) {
                    type = 0;
                } else {
                    String lastP = arrParams[arrParams.length - 1];
                    type = lastP.endsWith("]") ? 1 : (lastP.endsWith("!") ? 2 : 3);
                }
                for (int k = 0; k < arrParams.length; k++) {
                    arrParams[k] = arrParams[k].trim().replace("[", "").replace("]", "").replace("!", "");
                }
                switch (type) {
                    case 0:
                        Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));
                        emulator.strb(emulator.val(p0), emulator.val(p1));
                        break;
                    case 1: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        Integer i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        BitVec extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        BitVec newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            newValue = emulator.add(newValue, emulator.handleExtra(extValue, extType, extraChar, extraNum));
                        }
                        emulator.strb(emulator.val(p0), newValue);
                        break;
                    case 2: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.add(newValue, extValue);
                            extValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);

                            newValue = emulator.add(newValue, handledExt);
                            extValue = emulator.add(newValue, handledExt);
                        }
                        emulator.strb(emulator.val(p0), newValue);
                        emulator.write(p1, extValue);
                        break;
                    case 3: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = newValue;
                            extValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);

                            newValue = newValue;
                            extValue = emulator.add(newValue, handledExt);
                        }
                        emulator.strb(emulator.val(p0), newValue);
                        emulator.write(p1, extValue);
                        break;
                    default:
                        break;
                }
            } else if ("strh".equals(opcode)) {
                int type;
                if (arrParams.length == 2) {
                    type = 0;
                } else {
                    String lastP = arrParams[arrParams.length - 1];
                    type = lastP.endsWith("]") ? 1 : (lastP.endsWith("!") ? 2 : 3);
                }
                for (int k = 0; k < arrParams.length; k++) {
                    arrParams[k] = arrParams[k].trim().replace("[", "").replace("]", "").replace("!", "");
                }
                switch (type) {
                    case 0:
                        Character p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        Character p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));
                        emulator.strh(emulator.val(p0), emulator.val(p1));
                        break;
                    case 1: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        Integer i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        Character p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        BitVec extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        BitVec newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            newValue = emulator.add(newValue, emulator.handleExtra(extValue, extType, extraChar, extraNum));
                        }
                        emulator.strh(emulator.val(p0), newValue);
                        break;
                    case 2: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = emulator.add(newValue, extValue);
                            extValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);

                            newValue = emulator.add(newValue, handledExt);
                            extValue = emulator.add(newValue, handledExt);
                        }
                        emulator.strh(emulator.val(p0), newValue);
                        emulator.write(p1, extValue);
                        break;
                    case 3: 
                        p0 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[0]));
                        p1 = Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[1]));

                        i2 = arrParams[2].contains("#") ? SysUtils.normalizeNumInParam(arrParams[2].trim()) : null;
                        p2 = arrParams[2].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(arrParams[2].trim()));
                        extValue = (i2 == null) ? emulator.valCheckNeg(p2, arrParams[2].contains("-")) : new BitVec(i2);

                        newValue = emulator.valCheckNeg(p1, arrParams[1].contains("-"));
                        if (arrParams.length == 3) {
                            newValue = newValue;
                            extValue = emulator.add(newValue, extValue);
                        } else {
                            String[] extArr = arrParams[3].split("\\s+");
                            String extType = extArr[0];
                            Integer extraNum = null;
                            Character extraChar = null;
                            if (extArr.length > 1) {
                                extraNum = extArr[1].contains("#") ? Integer.parseInt(extArr[1].replace("#", "")) : null;
                                extraChar = extArr[1].contains("#") ? null : Mapping.regStrToChar.get(SysUtils.normalizeRegName(extArr[1]));
                            }
                            BitVec handledExt = emulator.handleExtra(extValue, extType, extraChar, extraNum);

                            newValue = newValue;
                            extValue = emulator.add(newValue, handledExt);
                        }
                        emulator.strh(emulator.val(p0), newValue);
                        emulator.write(p1, extValue);
                        break;
                    default:

                        break;
                }
            }
        } else {
            Logs.infoLn("Error: Opcode is null!");
        }
    }

    private static void decideToJump(EnvModel modelTrue, EnvModel modelFalse) {
        Gson gson = new Gson();
        String jsonString = gson.toJson(labelToEnvModel);
        Type type = new TypeToken<HashMap<String, EnvModel>>() {
        }.getType();
        HashMap<String, EnvModel> clonedMap = gson.fromJson(jsonString, type);
        if (modelFalse != null && labelToEnvModel.containsKey(modelFalse.label)) {
            if (modelFalse.label != null) {
                envStack.push(Pair.of(new EnvModel(modelFalse), clonedMap));
            }
        }
        if (modelTrue != null && labelToEnvModel.containsKey(modelTrue.label)) {
            if (modelTrue.label != null) {
                envStack.push(Pair.of(new EnvModel(modelTrue), clonedMap));
            }
        }
        if (envStack.empty()) gg();
        Map.Entry<EnvModel, HashMap<String, EnvModel>> model = envStack.pop();
        recentPop = model;
        jumpTo = model.getKey().label; 
        labelToEnvModel = model.getValue(); 
        if ((modelTrue == null || modelTrue.label == null || !jumpTo.equals(modelTrue.label))
                && (modelFalse == null || modelFalse.label == null || !jumpTo.equals(modelFalse.label))) {
            String triggerPrevLabelTwoUnsat = prevInst(jumpTo);
            if (Integer.parseInt(triggerPrevLabelTwoUnsat) < 0) gg();
            Logs.infoLn("-----> Recursively roll back to the parent branch: " + jumpTo);
        }
    }

    
    private static boolean isARM(String inpFile) {
        return Objects.requireNonNull(SysUtils.execCmd("file " + inpFile)).contains("ARM");
    }
    
    private static String nextInstLabel(String label) {
        //TODO: temporary skip external call
        if (label.contains("+")) {
            label = label.split("\\+")[0];
        }
        return label.contains("-") ? label.replace("-", "+") : String.valueOf(Integer.parseInt(label) + 4);
    }

    private static String nextInst(String label) {
        label = nextInstLabel(label);
        while (nodeLabelToIndex.get(label) == null) {
            label = nextInstLabel(label);
        }
        return label;
    }
    private static String prevInst(String label) {
        return label.contains("+") ? label.replace("+", "-") : String.valueOf(Integer.parseInt(label) - 4);
    }

    private static boolean isConcreteLabel(String label) {
        return label.contains("0x") || label.contains("-") || label.contains("+");
    }

    private static void gg() {
        Logs.infoLn("-> Time elapsed: " + (System.currentTimeMillis() - startTime) + "ms");
        System.exit(0);
    }
}
