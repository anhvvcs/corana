package emulator.base;

import emulator.semantics.Memory;
import executor.Configs;
import emulator.semantics.EnvData;
import emulator.semantics.EnvModel;
import emulator.semantics.Environment;
import enums.*;
import external.handler.APIStub;
import external.handler.ExternalCall;

import pojos.BitBool;
import pojos.BitVec;
import utils.*;

import java.util.BitSet;
import java.util.Map;
import java.util.Objects;

import static java.lang.Character.isDigit;

public class Emulator {

    protected ArithmeticMode arithmeticMode = ArithmeticMode.BINARY;
    private ExecutionMode executionMode = ExecutionMode.RUN;
    private ChangeMode changeMode = null;
    protected Environment env;

    protected Emulator(Environment env) {
        this.env = env;
    }

    public Environment getEnv() {
        return env;
    }

    /**
     * Add two bitvector values
     *
     * @param a: first bitvector value
     * @param b: second bitvector value
     * @return addition of a and b
     */
    public BitVec add(BitVec a, BitVec b, boolean... carry) {
        if (a == null || b == null) return null;
        String symVal = String.format("(bvadd %s %s)", a.getSym(), b.getSym());
        int[] params = Arithmetic.bitSetToInt(a.getVal(), b.getVal());
        BitSet conVal = Arithmetic.intToBitSet(params[0] + params[1]);
        if (carry != null && carry.length > 0 && carry[0]) {
            env.flags.C = new BitBool(true);
        }
        return new BitVec(symVal, conVal);
    }

    /**
     * Subtract two bitvector values
     *
     * @param a: first bitvector value
     * @param b: second bitvector value
     * @return subtraction of a and b
     */
    protected BitVec sub(BitVec a, BitVec b, boolean... carry) {
        if (a == null || b == null) return null;
        String symVal = String.format("(bvsub %s %s)", a.getSym(), b.getSym());
        int[] params = Arithmetic.bitSetToInt(a.getVal(), b.getVal());
        BitSet conVal = Arithmetic.intToBitSet(params[0] - params[1]);
        if (carry != null && carry.length > 0 && carry[0]) {
            env.flags.C = new BitBool(true);
        }
        return new BitVec(symVal, conVal);
    }

    public BitVec handleExtra(BitVec origin, String extraType, Character extraLabel, Integer extraNum) {
        BitVec extra;
        switch (extraType) {
            case "lsl":
                extra = (extraNum != null) ? new BitVec(extraNum) : val(extraLabel);
                return shift(origin, Mode.LEFT, extra);
            case "lsr":
                extra = (extraNum != null) ? new BitVec(extraNum) : val(extraLabel);
                return shift(origin, Mode.RIGHT, extra);
            case "ror":
                extra = (extraNum != null) ? new BitVec(extraNum) : val(extraLabel);
                return shift(origin, Mode.RIGHT, extra);
            case "asr":
                extra = (extraNum != null) ? new BitVec(extraNum) : val(extraLabel);
                return shift(origin, Mode.RIGHT, extra);
            case "rrx":
                return shift(origin, Mode.RIGHT, val(1));
        }
        return origin;
    }

    /**
     * Saturate a bitvec by lowerbound and upperbound with type
     *
     * @param b:          bitvec
     * @param lowerBound: lowerbound
     * @param upperBound: upperbound
     * @return saturated value
     */
    protected BitVec sat(BitVec[] b, double lowerBound, double upperBound) {
        if (b == null) return null;
        String sym = "(bvsat " + b[0].getSym() + ")";
        int result = 0;
        for (int i = 0; i < b.length; i++) {
            int v = Arithmetic.saturate(Arithmetic.bitSetToInt(b[i].getVal()), lowerBound, upperBound);
            Logs.infoLn(b.length + " " + Arithmetic.bitSetToInt(b[i].getVal()) + " " + v);
            int newLevel = Configs.architecture / b.length * (b.length - i - 1);
            result += v * Math.pow(2, newLevel);
        }
        return new BitVec(sym, Arithmetic.intToBitSet(result));
    }

    /**
     * Jump to a label if the condition is satisfied
     *
     * @param cond:  condition
     * @param label: target
     * @return true branch and false branch, each is represented by a graph node
     */
    public Map.Entry<EnvModel, EnvModel> b(String preCond, Character cond, Object label) {
        if (label instanceof String) {
            String finalLabel = (String) label;
            Logs.info("\t-> Direct Jump to", finalLabel, "if", Mapping.condCharToStr.get(cond), "\n");
            EnvModel modelTrue = checkPosCond(preCond, cond);
            EnvModel modelFalse = checkNegCond(preCond, cond);
            return Pair.of(modelTrue, modelFalse);
        } else if (label instanceof Character) {
            Character finalLabel = (Character) label;
            Logs.info("\t-> Indirect Jump to", Mapping.regCharToStr.get(finalLabel), "if", Mapping.condCharToStr.get(cond), "\n");
            EnvModel modelTrue = checkPosCond(preCond, cond, val(finalLabel).getSym());
            EnvModel modelFalse = checkNegCond(preCond, cond, val(finalLabel).getSym());
            return Pair.of(modelTrue, modelFalse);
        }
        Logs.infoLn("+ Wrong type of label!");
        return null;
    }

    public Map.Entry<EnvModel, EnvModel> bl(int nextLabel, String preCond, Character cond, Object label) {
        write('l', new BitVec(nextLabel));
        return b(preCond, cond, label);
    }

    public Map.Entry<EnvModel, EnvModel> bx(String preCond, Character cond, Object label) {
        return b(preCond, cond, label);
    }

    /**
     * Check the statisfiability of true branch
     *
     * @param preCond:   pre-condition
     * @param cond:      condition to check
     * @param evalValue: value to evaluate if any
     * @return: an environment model (including eval if needed) satifying the post-condition
     */
    private EnvModel checkPosCond(String preCond, Character cond, String... evalValue) {
        if (cond == null) return new EnvModel(preCond);
        String postCond;
        if (preCond != null && preCond.length() > 0) {
            postCond = String.format("(and %s %s)", preCond, getCondSuffixSMT(cond));
        } else {
            postCond = getCondSuffixSMT(cond);
        }
        String realEval = evalValue.length == 0 ? null : evalValue[0];
        String checkResult = Z3Solver.checkSAT(postCond, realEval);
        return new EnvModel(postCond, checkBaseCond(checkResult, realEval));
    }

    /**
     * Check the statisfiability of false branch
     *
     * @param preCond:   pre-condition
     * @param cond:      condition to check
     * @param evalValue: value to evaluate if any
     * @return: an environment model (including eval if needed) satifying the post-condition
     */
    private EnvModel checkNegCond(String preCond, Character cond, String... evalValue) {
        if (cond == null) return null;
        String postCond;
        if (preCond != null && preCond.length() > 0) {
            postCond = String.format("(and %s (not %s))", preCond, getCondSuffixSMT(cond));
        } else {
            postCond = String.format("(not %s)", getCondSuffixSMT(cond));
        }
        String realEval = evalValue.length == 0 ? null : evalValue[0];
        String checkResult = Z3Solver.checkSAT(postCond, realEval);
        return new EnvModel(postCond, checkBaseCond(checkResult, realEval));
    }

    /**
     * Base of check true/false condition
     *
     * @param checkResult: result obtained from Z3
     * @param realEval:    eval (optional)
     * @return an environment data model
     */
    private EnvData checkBaseCond(String checkResult, String realEval) {
        if (checkResult == null) return null;
        EnvData envData = new EnvData();
        String[] parseResultArr = checkResult.split("\n");
        for (int i = 0; i < parseResultArr.length; i++) {
            String p = parseResultArr[i].trim();
            if (p.contains("define-fun")) {
                String key = p.split("\\s+")[1];
                String value = parseResultArr[i + 1].trim().replace(")", "");
                if (Mapping.regStrToChar.containsKey(key)) {
                    envData.registersModel.put(key, value);
                } else {
                    envData.flagsModel.put(key, value.equalsIgnoreCase("true"));
                }
            }
        }
        if (realEval != null) {
            envData.eval = parseResultArr[parseResultArr.length - 1].trim();
        }
        return envData;
    }

    /**
     * Round a BitVec by a specific type
     *
     * @param b:         bitvec to be rounded
     * @param roundType: type of rounding
     * @return rounded BitVec
     */
    protected BitVec round(BitVec b, RoundType roundType) {
        if (b == null) return null;
        float val = Arithmetic.bitSetToFloat(b.getVal());
        String symVal = b.getSym();
        BitSet conVal;
        switch (roundType) {
            case NEAREST_TIE:
                conVal = Arithmetic.floatToBitSet(Math.round(val));
                symVal = "(bvrnt " + symVal + ")";
                return new BitVec(symVal, conVal);
            case NEAREST_EVEN:
                conVal = Arithmetic.floatToBitSet(Math.round(val / 2) * 2);
                symVal = "(bvrne " + symVal + ")";
                return new BitVec(symVal, conVal);
            case TOWARDS_ZERO:
                conVal = Arithmetic.floatToBitSet(val < 0 ? (float) Math.ceil(val) : (float) Math.floor(val));
                symVal = "(bvrtz " + symVal + ")";
                return new BitVec(symVal, conVal);
            case TOWARDS_PLUS_INF:
                conVal = Arithmetic.floatToBitSet((float) Math.ceil(val));
                symVal = "(bvrtpi " + symVal + ")";
                return new BitVec(symVal, conVal);
            case TOWARDS_MINUS_INF:
                conVal = Arithmetic.floatToBitSet((float) Math.floor(val));
                symVal = "(bvrtmi " + symVal + ")";
                return new BitVec(symVal, conVal);
            default:
                return null;
        }
    }

    /**
     * Update flags based on the result of operators
     *
     * @param f:      list of flags needed to update
     * @param result: operators' result
     */
    protected void updateFlags(char[] f, BitVec result) {
        int r = Arithmetic.bitSetToInt(result.getVal());
        for (char c : f) {
            switch (c) {
                case 'N':
                    env.flags.N.setConcreteValue(r < 0);
                    env.flags.N.setSymbolicValue("(bvslt " + result.getSym() + " #x00000000)");
                    break;
                case 'Z':
                    env.flags.Z.setConcreteValue(r == 0);
                    env.flags.Z.setSymbolicValue("(= " + result.getSym() + " #x00000000)");
                    break;
                case 'C':
                    env.flags.C.setConcreteValue(r > Math.pow(2, Configs.architecture));
                    env.flags.C.setSymbolicValue("(bvugt " + result.getSym() + " #xffffffff)");
                    break;
                case 'V':
                    env.flags.V.setConcreteValue(r > Math.pow(2, Configs.architecture) || r < -Math.pow(2, Configs.architecture));
                    env.flags.V.setSymbolicValue("(or (bvsgt " + result.getSym() + " #x7fffffff) (bvslt " + result.getSym() + " #x80000001))");
                    break;
                default:
                    break;
            }
        }
    }

    protected void copyBitfield(char rd, char xn, Integer lsb, Integer width) {
        BitSet valXn = val(xn).getVal();
        String symXn = val(xn).getSym();
        BitSet val = val(rd).getVal();
        String sym = val(rd).getSym();
        for (int i = lsb; i < lsb + width; i++) {
            val.set(i, valXn.get(i));
        }
        sym = "(bvbfi " + sym + " " + symXn + " " + lsb + " " + width + ")";
        write(rd, new BitVec(sym, val));
    }

    protected BitVec[] valArr(Character r, Type type) {
        BitVec val = val(r);
        if (type == Type.HALFWORD) {
            BitVec top = shift(val, Mode.RIGHT, 32);
            BitVec bottom = shift(shift(val, Mode.LEFT, 32), Mode.RIGHT, 32);
            return new BitVec[]{top, bottom};
        }
        return null;
    }

    public BitVec val(Character r, Type type) {
        if (type == Type.WORD) return env.value(r);
        if (type == Type.BYTE) return env.value(r);
        if (type == Type.BOTTOM_HALFWORD) return shift(shift(val(r), Mode.LEFT, 32), Mode.RIGHT, 32);
        if (type == Type.TOP_HALFWORD) return shift(val(r), Mode.RIGHT, 32);
        return env.value(r);
    }

    protected void clearBitfield(char rd, Integer start, Integer width) {
        BitSet val = val(rd).getVal();
        String sym = val(rd).getSym();
        for (int i = start; i < start + width; i++) {
            val.set(i, false);
        }
        sym = "(bvbfc " + sym + " " + start + " " + width + ")";
        write(rd, new BitVec(sym, val));
    }

    protected BitVec fromFloatingPoint(BitVec b, char suffix) {
        return new BitVec(b.getSym(), Arithmetic.floatToBitSet(Arithmetic.bitSetToInt(b.getVal())));
    }

    /**
     * Get condition needed to check before executing
     *
     * @param c: conditional suffix abbreviation
     * @return condition in SMT format
     */
    private String getCondSuffixSMT(Character c) {
        if (c == null) return "";
        CondSuffix cs = CondSuffix.getByName(c);
        if (cs == null) return "";
        switch (cs) {
            case EQ:
                return env.flags.Z.getSym();
            case NE:
                return String.format("(not %s)", env.flags.Z.getSym());
            case CS:
            case HS:
                return env.flags.C.getSym();                                //C == 1
            case CC:
            case LO:
                return String.format("(not %s)", env.flags.C.getSym()); // C == 0
            case MI:
                return env.flags.N.getSym();
            case PL:
                return String.format("(not %s)", env.flags.N.getSym());
            case VS:
                return env.flags.V.getSym();
            case VC:
                return String.format("(not %s)", env.flags.V.getSym());
            case HI:
                return String.format("(and %s(not %s))",
                        env.flags.C.getSym(), env.flags.Z.getSym());
            case LS:
                return String.format("(and %s(not %s))",
                        env.flags.Z.getSym(), env.flags.C.getSym());
            case GE:
                return String.format("(= %s %s)",
                        env.flags.N.getSym(), env.flags.V.getSym());
            case LT:
                return String.format("(not (= %s %s))",
                        env.flags.N.getSym(), env.flags.V.getSym());
            case GT:
                return String.format("(and (not %s) (= %s %s))",
                        env.flags.Z.getSym(), env.flags.N.getSym(), env.flags.V.getSym());
            case LE:
                return String.format("(and %s(not (= %s %s)))",
                        env.flags.Z.getSym(), env.flags.N.getSym(), env.flags.V.getSym());
            default:
                return "";
        }
    }

    public BitVec[] toArray(BitVec b) {
        return new BitVec[]{b};
    }

    public void pop(char r) {
        boolean isEmpty = env.stacks.stack.empty();
        BitVec popValue = env.stacks.pop();
        String sym = String.format("(bvadd %s #x00000004)", env.register.getFormula('s'));
        BitSet concreteValue = Arithmetic.intToBitSet(Arithmetic.bitSetToInt(env.register.get('s').getVal()) + 4);
        write('s', new BitVec(sym, concreteValue));
        if (popValue != null) {
            write(r, popValue);
        } else {
            if (isEmpty) {
                write(r, new BitVec(Configs.argc, Arithmetic.intToBitSet(env.rand())));
            } else {
                write(r, new BitVec("#x00000000", Arithmetic.intToBitSet(env.rand())));
            }
        }
    }

    public void push(char r) {
        // write new sp
        String sym = String.format("(bvadd %s (bvneg #x00000004))", env.register.getFormula('s'));
        BitSet concreteValue = Arithmetic.intToBitSet(Arithmetic.bitSetToInt(env.register.get('s').getVal()) - 4);
        write('s', new BitVec(sym, concreteValue));
        // store in stack
        Memory.set(env.register.get('s'), val(r));
        env.stacks.push(val(r));
    }

    protected void write(char r, BitVec value, Type lts) {
        switch (lts) {
            case TOP_HALFWORD:
                write(r, shift(value, Mode.RIGHT, 32));
                break;
            case BOTTOM_HALFWORD:
                write(r, shift(shift(value, Mode.LEFT, 32), Mode.RIGHT, 32));
                break;
        }
    }

    public void writeLower(char r, BitVec value) {
        env.register.set(r, value);
    }

    public void writeUpper(char r, BitVec value) {
        String upBin = Arithmetic.bitsetToStr(value.getVal());
        String lowBin = Arithmetic.bitsetToStr(env.register.get(r).getVal());
        String fullBin = upBin.substring(16, 32) + lowBin.substring(16, 32);
        write(r, Arithmetic.fromString(fullBin));
    }

    public void write(char r, BitVec value) {
        env.register.set(r, value);
    }

    public void str(BitVec xt, BitVec xn) {
        arithmeticMode = ArithmeticMode.BINARY;
        xt.calculate();
        xn.calculate();
        store(xn, xt);
    }

    public void strb(BitVec xt, BitVec xn) {
        arithmeticMode = ArithmeticMode.BINARY;
        store(xn, zeroExt(xt, 32));
    }

    public void strh(BitVec xt, BitVec xn) {
        arithmeticMode = ArithmeticMode.BINARY;
        store(xn, zeroExt(xt, 32));
    }

    public void ldr(Character xt, BitVec label) {
        arithmeticMode = ArithmeticMode.BINARY;
        //Check if the label is an address or arihmetic then get result of the bitvector arithmetic
        String result = (label.getSym().matches("[01][01]+") || label.getSym().contains("#x"))  ?
                                                                                        label.getSym() : Z3Solver.solveBitVecArithmetic(label.getSym());
        //String value = Memory.get(result);
        write(xt, (result.equals("ERROR") ?  env.memory.get(label) : Memory.get(SysUtils.getAddressValue(result))));
    }

    public void ldrb(Character xt, BitVec label) {
        arithmeticMode = ArithmeticMode.BINARY;
        write(xt, zeroExt(env.memory.get(label), 32));
    }

    public void ldrb(Character xt, Character label) {
        arithmeticMode = ArithmeticMode.BINARY;
        write(xt, zeroExt(env.memory.get(val(label)), 32));
    }

    public void ldrh(Character xt, BitVec label) {
        arithmeticMode = ArithmeticMode.BINARY;
        write(xt, zeroExt(env.memory.get(label), 32));
    }

    public void ldrh(Character xt, Character label) {
        arithmeticMode = ArithmeticMode.BINARY;
        write(xt, zeroExt(env.memory.get(val(label)), 32));
    }

    protected BitVec div(BitVec a, BitVec b) {
        if (a == null || b == null) return null;
        String symbolicValue = String.format("(bvsdiv %s %s)", a.getSym(), b.getSym());
        BitSet concreteValue = Arithmetic.intToBitSet(Arithmetic.bitSetToInt(a.getVal())
                / Arithmetic.bitSetToInt(b.getVal()));
        return new BitVec(symbolicValue, concreteValue);

    }

    protected BitVec zeroExt(BitVec b, int n) {
        long c = b.getSym().chars().mapToObj(i -> (char) i).filter(Character::isDigit).count();
        String symbolicValue = String.format("((_ zero_extend %s) %s)", n, b.getSym());
        return new BitVec(symbolicValue, b.getVal());
    }

    protected BitVec signedExt(BitVec b, int n) {
        if (b == null) return null;
        String symbolicValue = String.format("((_ sign_extend %s) %s)", n, b.getSym());
        int shift = Configs.architecture - n;
        BitSet concreteValue = Arithmetic.intToBitSet(Arithmetic.bitSetToInt(b.getVal()) << shift >> shift);
        return new BitVec(symbolicValue, concreteValue);
    }


    /**
     * Rotate a bitvector
     *
     * @param b:  input bitvector
     * @param d:  number of shift
     * @param rt: rotate type (right or left)
     * @return rotated bitset
     */
    protected BitVec rot(BitVec b, int d, Mode rt) {
        if (b == null) return null;
        int n = Arithmetic.bitSetToInt(b.getVal());
        BitSet concreteValue;
        String symbolicValue;
        switch (rt) {
            case RIGHT:
                concreteValue = Arithmetic.intToBitSet((n >> d) | (n << (Configs.architecture - d)));
                symbolicValue = String.format("((_ rotate_left %s) %s)", d, b.getSym());
                return new BitVec(symbolicValue, concreteValue);
            case LEFT:
                concreteValue = Arithmetic.intToBitSet((n << d) | (n >> (Configs.architecture - d)));
                symbolicValue = String.format("((_ rotate_right %s) %s)", d, b.getSym());
                return new BitVec(symbolicValue, concreteValue);
            default:
                return null;
        }
    }

    public void cmp(Character xn, Integer im, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N', 'Z', 'C', 'V'};
        BitVec result = cmp(val(xn), new BitVec(im));
        if (suffix != null && suffix == 's' && result != null) {
            updateFlags(flags, result);
        }
    }

    public void cmp(Character xn, Character xp, Integer im, String extraType, Character extraLabel, Integer extraNum, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N', 'Z', 'C', 'V'};
        BitVec addNumber = (im == null) ? val(xp) : new BitVec(im);
        BitVec result = cmp(val(xn), handleExtra(addNumber, extraType, extraLabel, extraNum));
        if (suffix != null && suffix == 's' && result != null) {
            updateFlags(flags, result);
        }
    }

    public void cmn(Character xn, Integer im, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N', 'Z', 'C', 'V'};
        BitVec result = cmn(val(xn), new BitVec(im));
        if (suffix != null && suffix == 's' && result != null) {
            updateFlags(flags, result);
        }
    }

    public void cmn(Character xn, Character xp, Integer im, String extraType, Character extraLabel, Integer extraNum, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N', 'Z', 'C', 'V'};
        BitVec addNumber = (im == null) ? val(xp) : new BitVec(im);
        BitVec result = cmn(val(xn), handleExtra(addNumber, extraType, extraLabel, extraNum));
        if (suffix != null && suffix == 's' && result != null) {
            updateFlags(flags, result);
        }
    }

    public void cmn(Character xn, Character op, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N', 'Z', 'C', 'V'};
        BitVec result = cmn(val(xn), val(op));
        if (suffix != null && suffix == 's' && result != null) {
            updateFlags(flags, result);
        }
    }

    public void bic(Character xd, Character xn, Integer imm, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N', 'Z', 'C', 'V'};
        if (xd == null) xd = xn;
        BitVec result = and(val(xn), comp(new BitVec(imm)));
        write(xd, result);
        if (suffix != null && suffix == 's' && result != null) {
            updateFlags(flags, result);
        }
    }

    public void mvn(Character xd, Integer im, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N', 'Z', 'C', 'V'};
        BitVec result = new BitVec(im);
        result = not(result);
        write(xd, result);
        if (suffix != null && suffix == 's' && result != null) {
            updateFlags(flags, result);
        }
    }

    public void eor(Character xd, Character xn, Integer im, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N', 'Z', 'C', 'V'};
        if (xd == null) xd = xn;
        BitVec result = xor(val(xn), new BitVec(im));
        write(xd, result);
        if (suffix != null && suffix == 's' && result != null) {
            updateFlags(flags, result);
        }
    }

    public void eor(Character xd, Character xn, Character xp, Integer imm, String extraType, Character extraLabel, Integer extraNum, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N', 'Z', 'C', 'V'};
        if (xd == null) xd = xn;
        BitVec addNumber = (imm == null) ? val(xp) : new BitVec(imm);
        BitVec result = xor(val(xn), handleExtra(addNumber, extraType, extraLabel, extraNum));
        write(xd, result);
        if (suffix != null && suffix == 's' && result != null) {
            updateFlags(flags, result);
        }
    }

    public void orr(Character xd, Character xn, Integer im, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N', 'Z', 'C', 'V'};
        if (xd == null) xd = xn;
        BitVec result = or(val(xn), new BitVec(im));
        write(xd, result);
        if (suffix != null && suffix == 's' && result != null) {
            updateFlags(flags, result);
        }
    }

    public void orr(Character xd, Character xn, Character xp, Integer imm, String extraType, Character extraLabel, Integer extraNum, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N', 'Z', 'C', 'V'};
        if (xd == null) xd = xn;
        BitVec addNumber = (imm == null) ? val(xp) : new BitVec(imm);
        BitVec result = or(val(xn), handleExtra(addNumber, extraType, extraLabel, extraNum));
        write(xd, result);
        if (suffix != null && suffix == 's' && result != null) {
            updateFlags(flags, result);
        }
    }

    public void adc(Character xd, Character xn, Integer im, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N', 'Z', 'C', 'V'};
        if (xd == null) xd = xn;
        BitVec result = add(val(xn), new BitVec(im), true);
        write(xd, result);
        if (suffix != null && suffix == 's' && result != null) {
            updateFlags(flags, result);
        }
    }

    public void and(Character xd, Character xn, Integer im, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N', 'Z', 'C', 'V'};
        if (xd == null) xd = xn;
        BitVec result = and(val(xn), new BitVec(im));
        write(xd, result);
        if (suffix != null && suffix == 's' && result != null) {
            updateFlags(flags, result);
        }
    }

    public void and(Character xd, Character xn, Character xp, Integer imm, String extraType, Character extraLabel, Integer extraNum, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N', 'Z', 'C', 'V'};
        if (xd == null) xd = xn;
        BitVec addNumber = (imm == null) ? val(xp) : new BitVec(imm);
        BitVec result = and(val(xn), handleExtra(addNumber, extraType, extraLabel, extraNum));
        write(xd, result);
        if (suffix != null && suffix == 's' && result != null) {
            updateFlags(flags, result);
        }
    }

    public void add(Character xd, Character xn, Character xp, Integer imm, String extraType, Character extraLabel, Integer extraNum, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N', 'Z', 'C', 'V'};
        if (xd == null) xd = xn;
        BitVec addNumber = (imm == null) ? val(xp) : new BitVec(imm);
        BitVec result = add(val(xn), handleExtra(addNumber, extraType, extraLabel, extraNum));
        write(xd, result);
        if (suffix != null && suffix == 's' && result != null) {
            updateFlags(flags, result);
        }
    }

    public void sub(Character xd, Character xn, Character xp, Integer imm, String extraType, Character extraLabel, Integer extraNum, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N', 'Z', 'C', 'V'};
        if (xd == null) xd = xn;
        BitVec addNumber = (imm == null) ? val(xp) : new BitVec(imm);
        BitVec result = sub(val(xn), handleExtra(addNumber, extraType, extraLabel, extraNum));
        write(xd, result);
        if (suffix != null && suffix == 's' && result != null) {
            updateFlags(flags, result);
        }
    }

    public void rsb(Character xd, Character xn, Integer im, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N', 'Z', 'C', 'V'};
        if (xd == null) xd = xn;
        BitVec result = sub(new BitVec(im), val(xn));
        write(xd, result);
        if (suffix != null && suffix == 's' && result != null) {
            updateFlags(flags, result);
        }
    }

    public void rsb(Character xd, Character xn, Character xp, Integer imm, String extraType, Character extraLabel, Integer extraNum, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N', 'Z', 'C', 'V'};
        if (xd == null) xd = xn;
        BitVec addNumber = (imm == null) ? val(xp) : new BitVec(imm);
        BitVec result = sub(val(xn), handleExtra(addNumber, extraType, extraLabel, extraNum));
        write(xd, result);
        if (suffix != null && suffix == 's' && result != null) {
            updateFlags(flags, result);
        }
    }

    protected BitVec load(BitVec value) {
        return env.memory.get(value);
    }

    protected BitVec cmp(BitVec a, BitVec b) {
        if (a == null || b == null) return null;
        BitVec result = sub(a, b);
        updateFlags(new char[]{'N', 'Z', 'C', 'V'}, result);
        return result;
    }

    protected BitVec cmn(BitVec a, BitVec b) {
        if (a == null || b == null) return null;
        BitVec result = add(a, b);
        updateFlags(new char[]{'N', 'Z', 'C', 'V'}, result);
        return result;
    }

    protected BitVec not(BitVec a) {
        String sym = "(bvnot " + a.getSym() + ")";
        BitSet val = (BitSet) a.getVal().clone();
        BitSet notVal = Arithmetic.intToBitSet(~Arithmetic.bitSetToInt(val));
        return new BitVec(sym, notVal);
    }

    protected BitVec rev(BitVec b, Type type) {
        String sym = "(bvrev" + b.getSym() + ")";
        BitSet val = Arithmetic.intToBitSet(Integer.reverse(Arithmetic.bitSetToInt(b.getVal())));
        return new BitVec(sym, val);
    }

    protected void switchMode(ExecutionMode m) {
        executionMode = m;
    }

    protected void change(ChangeMode cm) {
        changeMode = cm;
    }

    /**
     * Find min of two bitvector
     *
     * @param a: first bitvector
     * @param b: second bitvector
     * @return min of a and b
     */
    protected BitVec min(BitVec a, BitVec b) {
        if (a == null || b == null) return null;
        String symbolicValue = String.format("(bvmin %s %s)", a.getSym(), b.getSym());
        int ab = Arithmetic.bitSetToInt(a.getVal());
        int bb = Arithmetic.bitSetToInt(b.getVal());
        BitSet concreteValue = ab < bb ? a.getVal() : b.getVal();
        return new BitVec(symbolicValue, concreteValue);
    }

    /**
     * Find max of two bitvector
     *
     * @param a: first bitvector
     * @param b: second bitvector
     * @return max of a and b
     */
    protected BitVec max(BitVec a, BitVec b) {
        if (a == null || b == null) return null;
        String symbolicValue = String.format("(bvmax %s %s)", a.getSym(), b.getSym());
        int ab = Arithmetic.bitSetToInt(a.getVal());
        int bb = Arithmetic.bitSetToInt(b.getVal());
        BitSet concreteValue = ab > bb ? a.getVal() : b.getVal();
        return new BitVec(symbolicValue, concreteValue);
    }

    /**
     * Find negation of a bitvector
     *
     * @param b: input bitvector
     * @return negation of b
     */
    protected BitVec neg(BitVec b) {
        if (b == null) return null;
        String symbolicValue = String.format("(bvneg %s)", b.getSym());
        BitSet concreteValue = Arithmetic.intToBitSet(-Arithmetic.bitSetToInt(b.getVal()));
        return new BitVec(symbolicValue, concreteValue);
    }

    /**
     * Count number of leading zero of a bitvector
     *
     * @param b: input bitvector
     * @return number of leading zero of b
     */
    protected BitVec clz(BitVec b) {
        if (b == null) return null;
        String symbolicValue = String.format("(bvclz %s)", b.getSym());
        BitSet concreteValue;
        int count = Configs.architecture;
        int x = Arithmetic.bitSetToInt(b.getVal());
        while (x != 0) {
            x = x >> 1;
            count--;
        }
        concreteValue = Arithmetic.intToBitSet(count);
        return new BitVec(symbolicValue, concreteValue);
    }

    /**
     * Get absolute of a bitvector value
     *
     * @param b: input bitvector
     * @return absolute value of b
     */
    protected BitVec abs(BitVec b) {
        if (b == null) return null;
        String symbolicValue = "(bvabs " + b.getSym() + ")";
        BitSet concreteValue = Arithmetic.intToBitSet(Math.abs(Arithmetic.bitSetToInt(b.getVal())));
        return new BitVec(symbolicValue, concreteValue);
    }

    /**
     * Perform bitwise OR of two bitvector values
     *
     * @param a: first bitvector value
     * @param b: second bitvector value
     * @return OR of a and b
     */
    protected BitVec or(BitVec a, BitVec b) {
        if (a == null || b == null) return null;
        String symbolicValue = "(bvor " + a.getSym() + " " + b.getSym() + ")";
        BitSet concreteValue = (BitSet) a.getVal().clone();
        concreteValue.or(b.getVal());
        return new BitVec(symbolicValue, concreteValue);
    }

    /**
     * Perform bitwise exclusive OR of two bitvector values
     *
     * @param a: first bitvector value
     * @param b: second bitvector value
     * @return XOR of a and b
     */
    protected BitVec xor(BitVec a, BitVec b) {
        if (a == null || b == null) return null;
        String symbolicValue = "(bvxnor " + a.getSym() + " " + b.getSym() + ")";
        BitSet concreteValue = (BitSet) a.getVal().clone();
        concreteValue.xor(b.getVal());
        return new BitVec(symbolicValue, concreteValue);
    }

    /**
     * Perform bitwise AND of two bitvector values
     *
     * @param a: first bitvector value
     * @param b: second bitvector value
     * @return AND of a and b
     */
    protected BitVec and(BitVec a, BitVec b) {
        if (a == null || b == null) return null;
        String symbolicValue = String.format("(bvand %s %s)", a.getSym(), b.getSym());
        BitSet concreteValue = (BitSet) a.getVal().clone();
        concreteValue.and(b.getVal());
        return new BitVec(symbolicValue, concreteValue);
    }

    protected BitVec comp(BitVec b) {
        BitVec val = new BitVec(b);
        val.setSym("(bvcomp " + val.getSym() + ")");
        val.setVal(Arithmetic.intToBitSet(~Arithmetic.bitSetToInt(val.getVal())));
        return val;
    }

    protected void throwExc(String svc) {
        try {
            throw new Exception("svc");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public BitVec valCheckNeg(Character r, boolean isNegative) {
        if (!isNegative) return val(r);
        return neg(val(r));
    }

    public BitVec val(Character r) {
        BitVec res = env.value(r);
        // if the value is number in bitvec < 32 -> change to bitvec 32 (#x0 -> #x00000000)
        String symValue = (res.getSym().charAt(0)  == '#' && res.getSym().length() < 8) ? SysUtils.normalizeNumInHex(res.getSym().trim()) : res.getSym();
        BitVec valBitVec = new BitVec(symValue, res.getVal());
        return valBitVec;
    }

    protected void load(Character d, Character label) {
        BitVec res = val(label);
        write(d, Memory.get(val(label)));
      //  System.out.println("");
    }

    public void ldrAt(Character d, BitVec address) {
        write(d, Memory.get(address.getSym()));
    }

    public void store(BitVec address, BitVec value) {
        env.memory.put(address, value);
        Memory.set(address, value);
    }

    public void store(BitVec r, BitVec a, BitVec b) {
        store(r, a);
        String newSymbolicValue = "(bvadd " + r.getSym() + " #x00000001)";
        BitSet newConcreteValue = Arithmetic.intToBitSet(Arithmetic.bitSetToInt(r.getVal()) + 1);
        store(new BitVec(newSymbolicValue, newConcreteValue), b);
    }

    protected BitVec sqrt(BitVec b) {
        if (b == null) return null;
        String symbolicValue = "(bvsqrt " + b.getSym() + ")";
        BitSet concreteValue = Arithmetic.floatToBitSet((float) Math.sqrt(Arithmetic.bitSetToFloat(b.getVal())));
        return new BitVec(symbolicValue, concreteValue);
    }

    public BitVec val(Integer v) {
        BitSet concreteValue = Arithmetic.intToBitSet(v);
        String symbolicValue = Arithmetic.intToHexSmt(Arithmetic.bitSetToInt(concreteValue));
        return new BitVec(symbolicValue, concreteValue);
    }

    /**
     * Get a bitvector value of either a or b
     * input can be one of Integer, Bitset, or Character
     *
     * @param a: first input
     * @param b: second input
     * @return value of the non-null input
     */
    protected BitVec val(Object a, Object b) {
        if (a == null && b == null) return null;
        Object x = (a != null) ? a : b;
        if (x instanceof Integer) return val((int) x);
        if (x instanceof BitVec) return (BitVec) x;
        return val((char) x);
    }

    protected BitVec shift(BitVec b, int shiftMode, int s) {
        assert shiftMode >= 1 && shiftMode <= 2;
        return shift(b, shiftMode == 1 ? Mode.LEFT : Mode.RIGHT, s);
    }

    protected BitVec shift(BitVec b, Mode shiftMode, int s) {
        return shift(b, shiftMode, new BitVec(Arithmetic.intToHexSmt(s), Arithmetic.intToBitSet(s)));
    }

    /**
     * Shift a bitvec to a specific step
     *
     * @param b:         input bitvec
     * @param shiftMode: left or right
     * @param s:         number of shifting
     * @return
     */
    protected BitVec shift(BitVec b, Mode shiftMode, BitVec s) {
        String mode = (shiftMode == Mode.LEFT || shiftMode == Mode.LSL) ? "bvshl" : "bvlshr";
        int shiftValue = Arithmetic.bitSetToInt(s.getVal());
        String sym = "(" + mode + " " + b.getSym() + " " + s.getSym() + ")";
        BitSet val = null;
        switch (shiftMode) {
            case LEFT:
            case LSL:
                if (shiftValue >= 32) {
                    long l = Arithmetic.bitSetToLong(Objects.requireNonNull(b.getVal())) << shiftValue;
                    val = Arithmetic.longToBitSet(l);
                } else {
                    int i = Arithmetic.bitSetToInt(b.getVal()) << shiftValue;
                    val = Arithmetic.intToBitSet(i);
                }
                break;
            case RIGHT:
            case ASR:
                if (shiftValue >= 32) {
                    if (b.getVal().length() > 32) {
                        String str = Arithmetic.bitsetToFullStr(b.getVal());
                        val = Arithmetic.fromString(str.substring(0, 32)).getVal();
                    } else {
                        long l = Arithmetic.bitSetToLong(Objects.requireNonNull(b.getVal())) >> shiftValue;
                        val = Arithmetic.longToBitSet(l);
                    }
                } else {
                    int i = Arithmetic.bitSetToInt(b.getVal()) >> shiftValue;
                    val = Arithmetic.intToBitSet(i);
                }
                break;
        }
        return new BitVec(sym, val);
    }

    protected BitVec shift(BitVec[] b, Mode shiftMode, int n) {
        BitVec[] bvArr = new BitVec[b.length];
        for (int i = 0; i < b.length; i++) {
            bvArr[i] = shift(b[i], shiftMode, new BitVec(Arithmetic.intToHexSmt(n), Arithmetic.intToBitSet(n)));
        }
        return concat(bvArr);
    }

    protected BitVec toFloatingPoint(BitVec b) {
        return b;
    }

    protected BitVec add(BitVec r, char rdlo, char rdhi) {
        long result = Arithmetic.bitSetToLong(r.getVal());
        long partOne = (long) (result / (Math.pow(2, Configs.architecture))) + Arithmetic.bitSetToLong(val(rdhi).getVal());
        long partTwo = (long) (result % (Math.pow(2, Configs.architecture))) + Arithmetic.bitSetToLong(val(rdlo).getVal());
        long newResult = (long) (partOne * Math.pow(2, Configs.architecture)) + partTwo;
        return new BitVec("bvaddin " + val(rdhi).getSym() + " " + val(rdlo).getSym(), Arithmetic.longToBitSet(newResult));
    }

    protected BitVec[] add(BitVec a, BitVec b, Type type) {
        if (a == null || b == null) return null;
        int numPart = 1;
        if (type == Type.WORD) {
            numPart = 1;
        } else if (type == Type.HALFWORD) {
            numPart = 2;
        } else if (type == Type.BYTE) {
            numPart = 4;
        }
        String symbolicValue = "(bvadd" + Configs.architecture / numPart + " " + a.getSym() + " " + b.getSym() + ")";
        String[] aStrArr = Arithmetic.bitsetToStr(a.getVal()).split("(?<=\\G.{" + Configs.architecture / numPart + "})");
        String[] bStrArr = Arithmetic.bitsetToStr(b.getVal()).split("(?<=\\G.{" + Configs.architecture / numPart + "})");
        BitVec[] bvArr = new BitVec[numPart];
        for (int i = 0; i < aStrArr.length; i++) {
            int add = Arithmetic.bitSetToInt(Arithmetic.fromString(aStrArr[i]).getVal()) +
                    Arithmetic.bitSetToInt(Arithmetic.fromString(bStrArr[i]).getVal());
            int newLevel = (numPart - i - 1) * Configs.architecture / numPart;
            int result = (int) (add * Math.pow(2, newLevel));
            bvArr[i] = new BitVec(symbolicValue, Arithmetic.intToBitSet(result));
        }
        return bvArr;
    }

    protected BitVec[] sub(BitVec a, BitVec b, Type type) {
        if (a == null || b == null) return null;
        int numPart = 1;
        if (type == Type.WORD) {
            numPart = 1;
        } else if (type == Type.HALFWORD) {
            numPart = 2;
        } else if (type == Type.BYTE) {
            numPart = 4;
        }
        String symbolicValue = "(bvsub" + Configs.architecture / numPart + " " + a.getSym() + " " + b.getSym() + ")";
        String[] aStrArr = Arithmetic.bitsetToStr(a.getVal()).split("(?<=\\G.{" + Configs.architecture / numPart + "})");
        String[] bStrArr = Arithmetic.bitsetToStr(b.getVal()).split("(?<=\\G.{" + Configs.architecture / numPart + "})");
        BitVec[] bvArr = new BitVec[numPart];
        for (int i = 0; i < aStrArr.length; i++) {
            int add = Arithmetic.bitSetToInt(Arithmetic.fromString(aStrArr[i]).getVal()) - Arithmetic.bitSetToInt(Arithmetic.fromString(bStrArr[i]).getVal());
            int newLevel = (numPart - i - 1) * Configs.architecture / numPart;
            int result = (int) (add * Math.pow(2, newLevel));
            bvArr[i] = new BitVec(symbolicValue, Arithmetic.intToBitSet(result));
        }
        return bvArr;
    }

    protected boolean checkDiv(BitVec a, BitVec b) {
        return Arithmetic.bitSetToInt(a.getVal()) % Arithmetic.bitSetToInt(b.getVal()) == 0;
    }

    public BitVec concat(BitVec[] bArr) {
        int result = 0;
        int len = bArr.length;
        StringBuilder sym = new StringBuilder("(bvconcat ");
        for (int i = 0; i < len; i++) {
            int v = Arithmetic.bitSetToInt(bArr[i].getVal());
            int newLevel = Configs.architecture / len * (len - i - 1);
            result += v * Math.pow(2, newLevel);
            sym.append(bArr[i].getSym()).append(" ");
        }
        sym.append(")");
        return new BitVec(sym.toString(), Arithmetic.intToBitSet(result));
    }

    protected BitVec mul(BitVec a, BitVec b, Type type) {
        switch (type) {
            case BOTTOM_HALFWORD:
                return mul(shift(shift(a, Mode.LEFT, 32), Mode.RIGHT, 32), shift(shift(b, Mode.LEFT, 32), Mode.RIGHT, 32));
            case TOP_HALFWORD:
                return mul(shift(a, Mode.RIGHT, 32), shift(b, Mode.RIGHT, 32));
            default:
                return null;
        }
    }

    /**
     * Multiple two bitvector values
     *
     * @param a: first bitvector value
     * @param b: second bitvector value
     * @return multiplication of a and b
     */
    protected BitVec mul(BitVec a, BitVec b) {
        if (a == null || b == null) return null;
        String sym = String.format("(bvmul %s %s)", a.getSym(), b.getSym());
        int result = Arithmetic.bitSetToInt(a.getVal()) * Arithmetic.bitSetToInt(b.getVal());
        if (result > Math.pow(2, Configs.architecture)) {
            result = (int) Math.pow(2, Configs.architecture);
        }
        BitSet val = Arithmetic.intToBitSet(result);
        return new BitVec(sym, val);
    }

    /**
     * Call an external function
     */


    public EnvModel fork(EnvModel cur) {
        Logs.info("\t-> Fork a new process \n");
        EnvModel modelChild = new EnvModel(cur);
        return modelChild;
    }

    public void call(String jmpAdress) throws Exception{
       // try {
            String func = ExternalCall.findFunctionName(jmpAdress);
            if ("printf".equals(func)) {
                APIStub.printf(env);
            } else if ("__errno_location".equals(func)) {
                APIStub.__errno_location(env);
            } else if ("connect".equals(func)) {
                APIStub.connect(env);
            }
//            else if ("gettimeofday".equals(func)) {
//                APIStub.gettimeofday(env);
//            } else if ("sigemptyset".equals(func)) {
//                APIStub.sigemptyset(env);         }
            else if ("__flbf".equals(func)) {
                APIStub.__flbf(env); }
            else if ("__fpending".equals(func)) {
                APIStub.__fpending(env); }
            else if ("__fpurge".equals(func)) {
                APIStub.__fpurge(env); }
            else if ("__freadable".equals(func)) {
                APIStub.__freadable(env); }
            else if ("__freading".equals(func)) {
                APIStub.__freading(env); }
            else if ("__fsetlocking".equals(func)) {
                APIStub.__fsetlocking(env); }
            else if ("__fwritable".equals(func)) {
                APIStub.__fwritable(env); }
            else if ("__fwriting".equals(func)) {
                APIStub.__fwriting(env); }
            else if ("__ppc_get_timebase".equals(func)) {
                APIStub.__ppc_get_timebase(env); }
            else if ("__ppc_get_timebase_freq".equals(func)) {
                APIStub.__ppc_get_timebase_freq(env); }
            else if ("__ppc_mdoio".equals(func)) {
                APIStub.__ppc_mdoio(env); }
            else if ("__ppc_mdoom".equals(func)) {
                APIStub.__ppc_mdoom(env); }
            else if ("__ppc_set_ppr_low".equals(func)) {
                APIStub.__ppc_set_ppr_low(env); }
            else if ("__ppc_set_ppr_med".equals(func)) {
                APIStub.__ppc_set_ppr_med(env); }
            else if ("__ppc_set_ppr_med_high".equals(func)) {
                APIStub.__ppc_set_ppr_med_high(env); }
            else if ("__ppc_set_ppr_med_low".equals(func)) {
                APIStub.__ppc_set_ppr_med_low(env); }
            else if ("__ppc_set_ppr_very_low".equals(func)) {
                APIStub.__ppc_set_ppr_very_low(env); }
            else if ("__ppc_yield".equals(func)) {
                APIStub.__ppc_yield(env); }
            else if ("_Exit".equals(func)) {
                APIStub._Exit(env); }
            else if ("_exit".equals(func)) {
                APIStub._exit(env); }
            else if ("_flushlbf".equals(func)) {
                APIStub._flushlbf(env); }
            else if ("a64l".equals(func)) {
                APIStub.a64l(env); }
            else if ("abort".equals(func)) {
                APIStub.abort(env); }
            else if ("abs".equals(func)) {
                APIStub.abs(env); }
            else if ("accept".equals(func)) {
                APIStub.accept(env); }
            else if ("access".equals(func)) {
                APIStub.access(env); }
            else if ("acos".equals(func)) {
                APIStub.acos(env); }
            else if ("acosf".equals(func)) {
                APIStub.acosf(env); }
            else if ("acosh".equals(func)) {
                APIStub.acosh(env); }
            else if ("acoshf".equals(func)) {
                APIStub.acoshf(env); }
            else if ("acoshl".equals(func)) {
                APIStub.acoshl(env); }
            else if ("acosl".equals(func)) {
                APIStub.acosl(env); }
            else if ("addmntent".equals(func)) {
                APIStub.addmntent(env); }
            else if ("addseverity".equals(func)) {
                APIStub.addseverity(env); }
            else if ("adjtime".equals(func)) {
                APIStub.adjtime(env); }
            else if ("adjtimex".equals(func)) {
                APIStub.adjtimex(env); }
            else if ("aio_cancel".equals(func)) {
                APIStub.aio_cancel(env); }
            else if ("aio_cancel64".equals(func)) {
                APIStub.aio_cancel64(env); }
            else if ("aio_error".equals(func)) {
                APIStub.aio_error(env); }
            else if ("aio_error64".equals(func)) {
                APIStub.aio_error64(env); }
            else if ("aio_fsync".equals(func)) {
                APIStub.aio_fsync(env); }
            else if ("aio_fsync64".equals(func)) {
                APIStub.aio_fsync64(env); }
            else if ("aio_init".equals(func)) {
                APIStub.aio_init(env); }
            else if ("aio_read".equals(func)) {
                APIStub.aio_read(env); }
            else if ("aio_read64".equals(func)) {
                APIStub.aio_read64(env); }
            else if ("aio_return".equals(func)) {
                APIStub.aio_return(env); }
            else if ("aio_return64".equals(func)) {
                APIStub.aio_return64(env); }
            else if ("aio_suspend".equals(func)) {
                APIStub.aio_suspend(env); }
            else if ("aio_suspend64".equals(func)) {
                APIStub.aio_suspend64(env); }
            else if ("aio_write".equals(func)) {
                APIStub.aio_write(env); }
            else if ("aio_write64".equals(func)) {
                APIStub.aio_write64(env); }
            else if ("alarm".equals(func)) {
                APIStub.alarm(env); }
            else if ("aligned_alloc".equals(func)) {
                APIStub.aligned_alloc(env); }
            else if ("alloca".equals(func)) {
                APIStub.alloca(env); }
            else if ("argz_add_sep".equals(func)) {
                APIStub.argz_add_sep(env); }
            else if ("argz_append".equals(func)) {
                APIStub.argz_append(env); }
            else if ("argz_count".equals(func)) {
                APIStub.argz_count(env); }
            else if ("argz_create".equals(func)) {
                APIStub.argz_create(env); }
            else if ("argz_create_sep".equals(func)) {
                APIStub.argz_create_sep(env); }
            else if ("argz_extract".equals(func)) {
                APIStub.argz_extract(env); }
            else if ("argz_insert".equals(func)) {
                APIStub.argz_insert(env); }
            else if ("argz_next".equals(func)) {
                APIStub.argz_next(env); }
            else if ("argz_stringify".equals(func)) {
                APIStub.argz_stringify(env); }
            else if ("asctime".equals(func)) {
                APIStub.asctime(env); }
            else if ("asctime_r".equals(func)) {
                APIStub.asctime_r(env); }
            else if ("asin".equals(func)) {
                APIStub.asin(env); }
            else if ("asinf".equals(func)) {
                APIStub.asinf(env); }
            else if ("asinh".equals(func)) {
                APIStub.asinh(env); }
            else if ("asinhf".equals(func)) {
                APIStub.asinhf(env); }
            else if ("asinhl".equals(func)) {
                APIStub.asinhl(env); }
            else if ("asinl".equals(func)) {
                APIStub.asinl(env); }
            else if ("asserT".equals(func)) {
                APIStub.asserT(env); }
            else if ("assert_perror".equals(func)) {
                APIStub.assert_perror(env); }
            else if ("atan".equals(func)) {
                APIStub.atan(env); }
            else if ("atan2".equals(func)) {
                APIStub.atan2(env); }
            else if ("atan2f".equals(func)) {
                APIStub.atan2f(env); }
            else if ("atan2l".equals(func)) {
                APIStub.atan2l(env); }
            else if ("atanf".equals(func)) {
                APIStub.atanf(env); }
            else if ("atanh".equals(func)) {
                APIStub.atanh(env); }
            else if ("atanhf".equals(func)) {
                APIStub.atanhf(env); }
            else if ("atanhl".equals(func)) {
                APIStub.atanhl(env); }
            else if ("atanl".equals(func)) {
                APIStub.atanl(env); }
            else if ("atof".equals(func)) {
                APIStub.atof(env); }
            else if ("atoi".equals(func)) {
                APIStub.atoi(env); }
            else if ("atol".equals(func)) {
                APIStub.atol(env); }
            else if ("atoll".equals(func)) {
                APIStub.atoll(env); }
            else if ("backtrace".equals(func)) {
                APIStub.backtrace(env); }
            else if ("backtrace_symbols".equals(func)) {
                APIStub.backtrace_symbols(env); }
            else if ("backtrace_symbols_fd".equals(func)) {
                APIStub.backtrace_symbols_fd(env); }
            else if ("basename".equals(func)) {
                APIStub.basename(env); }
            else if ("bcmp".equals(func)) {
                APIStub.bcmp(env); }
            else if ("bcopy".equals(func)) {
                APIStub.bcopy(env); }
            else if ("bind".equals(func)) {
                APIStub.bind(env); }
            else if ("bind_textdomain_codeset".equals(func)) {
                APIStub.bind_textdomain_codeset(env); }
            else if ("bindtextdomain".equals(func)) {
                APIStub.bindtextdomain(env); }
            else if ("brk".equals(func)) {
                APIStub.brk(env); }
            else if ("bsearch".equals(func)) {
                APIStub.bsearch(env); }
            else if ("btowc".equals(func)) {
                APIStub.btowc(env); }
            else if ("bzero".equals(func)) {
                APIStub.bzero(env); }
            else if ("cabs".equals(func)) {
                APIStub.cabs(env); }
            else if ("cabsf".equals(func)) {
                APIStub.cabsf(env); }
            else if ("cabsl".equals(func)) {
                APIStub.cabsl(env); }
            else if ("cacos".equals(func)) {
                APIStub.cacos(env); }
            else if ("cacosf".equals(func)) {
                APIStub.cacosf(env); }
            else if ("cacosh".equals(func)) {
                APIStub.cacosh(env); }
            else if ("cacoshf".equals(func)) {
                APIStub.cacoshf(env); }
            else if ("cacoshl".equals(func)) {
                APIStub.cacoshl(env); }
            else if ("cacosl".equals(func)) {
                APIStub.cacosl(env); }
            else if ("calloc".equals(func)) {
                APIStub.calloc(env); }
            else if ("canonicalize_file_name".equals(func)) {
                APIStub.canonicalize_file_name(env); }
            else if ("carg".equals(func)) {
                APIStub.carg(env); }
            else if ("cargf".equals(func)) {
                APIStub.cargf(env); }
            else if ("cargl".equals(func)) {
                APIStub.cargl(env); }
            else if ("casin".equals(func)) {
                APIStub.casin(env); }
            else if ("casinf".equals(func)) {
                APIStub.casinf(env); }
            else if ("casinh".equals(func)) {
                APIStub.casinh(env); }
            else if ("casinhf".equals(func)) {
                APIStub.casinhf(env); }
            else if ("casinhl".equals(func)) {
                APIStub.casinhl(env); }
            else if ("casinl".equals(func)) {
                APIStub.casinl(env); }
            else if ("catan".equals(func)) {
                APIStub.catan(env); }
            else if ("catanf".equals(func)) {
                APIStub.catanf(env); }
            else if ("catanh".equals(func)) {
                APIStub.catanh(env); }
            else if ("catanhf".equals(func)) {
                APIStub.catanhf(env); }
            else if ("catanhl".equals(func)) {
                APIStub.catanhl(env); }
            else if ("catanl".equals(func)) {
                APIStub.catanl(env); }
            else if ("catclose".equals(func)) {
                APIStub.catclose(env); }
            else if ("catgets".equals(func)) {
                APIStub.catgets(env); }
            else if ("catopen".equals(func)) {
                APIStub.catopen(env); }
            else if ("cbrt".equals(func)) {
                APIStub.cbrt(env); }
            else if ("cbrtf".equals(func)) {
                APIStub.cbrtf(env); }
            else if ("cbrtl".equals(func)) {
                APIStub.cbrtl(env); }
            else if ("ccos".equals(func)) {
                APIStub.ccos(env); }
            else if ("ccosf".equals(func)) {
                APIStub.ccosf(env); }
            else if ("ccosh".equals(func)) {
                APIStub.ccosh(env); }
            else if ("ccoshf".equals(func)) {
                APIStub.ccoshf(env); }
            else if ("ccoshl".equals(func)) {
                APIStub.ccoshl(env); }
            else if ("ccosl".equals(func)) {
                APIStub.ccosl(env); }
            else if ("ceil".equals(func)) {
                APIStub.ceil(env); }
            else if ("ceilf".equals(func)) {
                APIStub.ceilf(env); }
            else if ("ceill".equals(func)) {
                APIStub.ceill(env); }
            else if ("cexp".equals(func)) {
                APIStub.cexp(env); }
            else if ("cexpf".equals(func)) {
                APIStub.cexpf(env); }
            else if ("cexpl".equals(func)) {
                APIStub.cexpl(env); }
            else if ("cfgetispeed".equals(func)) {
                APIStub.cfgetispeed(env); }
            else if ("cfgetospeed".equals(func)) {
                APIStub.cfgetospeed(env); }
            else if ("cfmakeraw".equals(func)) {
                APIStub.cfmakeraw(env); }
            else if ("cfsetispeed".equals(func)) {
                APIStub.cfsetispeed(env); }
            else if ("cfsetospeed".equals(func)) {
                APIStub.cfsetospeed(env); }
            else if ("cfsetspeed".equals(func)) {
                APIStub.cfsetspeed(env); }
            else if ("chdir".equals(func)) {
                APIStub.chdir(env); }
            else if ("chmod".equals(func)) {
                APIStub.chmod(env); }
            else if ("chown".equals(func)) {
                APIStub.chown(env); }
            else if ("cimag".equals(func)) {
                APIStub.cimag(env); }
            else if ("cimagf".equals(func)) {
                APIStub.cimagf(env); }
            else if ("cimagl".equals(func)) {
                APIStub.cimagl(env); }
            else if ("clearenv".equals(func)) {
                APIStub.clearenv(env); }
            else if ("clearerr".equals(func)) {
                APIStub.clearerr(env); }
            else if ("clearerr_unlocked".equals(func)) {
                APIStub.clearerr_unlocked(env); }
            else if ("clock".equals(func)) {
                APIStub.clock(env); }
            else if ("clock_getres".equals(func)) {
                APIStub.clock_getres(env); }
            else if ("clock_gettime".equals(func)) {
                APIStub.clock_gettime(env); }
            else if ("clock_settime".equals(func)) {
                APIStub.clock_settime(env); }
            else if ("clog".equals(func)) {
                APIStub.clog(env); }
            else if ("clog10".equals(func)) {
                APIStub.clog10(env); }
            else if ("clog10f".equals(func)) {
                APIStub.clog10f(env); }
            else if ("clog10l".equals(func)) {
                APIStub.clog10l(env); }
            else if ("clogf".equals(func)) {
                APIStub.clogf(env); }
            else if ("clogl".equals(func)) {
                APIStub.clogl(env); }
            else if ("close".equals(func)) {
                APIStub.close(env); }
            else if ("closedir".equals(func)) {
                APIStub.closedir(env); }
            else if ("closelog".equals(func)) {
                APIStub.closelog(env); }
            else if ("confstr".equals(func)) {
                APIStub.confstr(env); }
            else if ("conj".equals(func)) {
                APIStub.conj(env); }
            else if ("conjf".equals(func)) {
                APIStub.conjf(env); }
            else if ("conjl".equals(func)) {
                APIStub.conjl(env); }
            else if ("copysign".equals(func)) {
                APIStub.copysign(env); }
            else if ("copysignf".equals(func)) {
                APIStub.copysignf(env); }
            else if ("copysignl".equals(func)) {
                APIStub.copysignl(env); }
            else if ("cos".equals(func)) {
                APIStub.cos(env); }
            else if ("cosf".equals(func)) {
                APIStub.cosf(env); }
            else if ("cosh".equals(func)) {
                APIStub.cosh(env); }
            else if ("coshf".equals(func)) {
                APIStub.coshf(env); }
            else if ("coshl".equals(func)) {
                APIStub.coshl(env); }
            else if ("cosl".equals(func)) {
                APIStub.cosl(env); }
            else if ("cpow".equals(func)) {
                APIStub.cpow(env); }
            else if ("cpowf".equals(func)) {
                APIStub.cpowf(env); }
            else if ("cproj".equals(func)) {
                APIStub.cproj(env); }
            else if ("cprojf".equals(func)) {
                APIStub.cprojf(env); }
            else if ("cprojl".equals(func)) {
                APIStub.cprojl(env); }
            else if ("CPU_CLR".equals(func)) {
                APIStub.CPU_CLR(env); }
            else if ("CPU_ISSET".equals(func)) {
                APIStub.CPU_ISSET(env); }
            else if ("CPU_SET".equals(func)) {
                APIStub.CPU_SET(env); }
            else if ("CPU_ZERO".equals(func)) {
                APIStub.CPU_ZERO(env); }
            else if ("creal".equals(func)) {
                APIStub.creal(env); }
            else if ("crealf".equals(func)) {
                APIStub.crealf(env); }
            else if ("creall".equals(func)) {
                APIStub.creall(env); }
            else if ("creat".equals(func)) {
                APIStub.creat(env); }
            else if ("creat64".equals(func)) {
                APIStub.creat64(env); }
            else if ("crypt".equals(func)) {
                APIStub.crypt(env); }
            else if ("crypt_r".equals(func)) {
                APIStub.crypt_r(env); }
            else if ("csin".equals(func)) {
                APIStub.csin(env); }
            else if ("csinf".equals(func)) {
                APIStub.csinf(env); }
            else if ("csinh".equals(func)) {
                APIStub.csinh(env); }
            else if ("csinhf".equals(func)) {
                APIStub.csinhf(env); }
            else if ("csinhl".equals(func)) {
                APIStub.csinhl(env); }
            else if ("csinl".equals(func)) {
                APIStub.csinl(env); }
            else if ("csqrt".equals(func)) {
                APIStub.csqrt(env); }
            else if ("csqrtf".equals(func)) {
                APIStub.csqrtf(env); }
            else if ("csqrtl".equals(func)) {
                APIStub.csqrtl(env); }
            else if ("ctan".equals(func)) {
                APIStub.ctan(env); }
            else if ("ctanf".equals(func)) {
                APIStub.ctanf(env); }
            else if ("ctanh".equals(func)) {
                APIStub.ctanh(env); }
            else if ("ctanhf".equals(func)) {
                APIStub.ctanhf(env); }
            else if ("ctanhl".equals(func)) {
                APIStub.ctanhl(env); }
            else if ("ctanl".equals(func)) {
                APIStub.ctanl(env); }
            else if ("ctermid".equals(func)) {
                APIStub.ctermid(env); }
            else if ("cuserid".equals(func)) {
                APIStub.cuserid(env); }
            else if ("dcgettext".equals(func)) {
                APIStub.dcgettext(env); }
            else if ("dcngettext".equals(func)) {
                APIStub.dcngettext(env); }
            else if ("dgettext".equals(func)) {
                APIStub.dgettext(env); }
            else if ("difftime".equals(func)) {
                APIStub.difftime(env); }
            else if ("dirfd".equals(func)) {
                APIStub.dirfd(env); }
            else if ("dirname".equals(func)) {
                APIStub.dirname(env); }
            else if ("div".equals(func)) {
                APIStub.div(env); }
            else if ("dngettext".equals(func)) {
                APIStub.dngettext(env); }
            else if ("drand48".equals(func)) {
                APIStub.drand48(env); }
            else if ("drem".equals(func)) {
                APIStub.drem(env); }
            else if ("dremf".equals(func)) {
                APIStub.dremf(env); }
            else if ("dreml".equals(func)) {
                APIStub.dreml(env); }
            else if ("dup".equals(func)) {
                APIStub.dup(env); }
            else if ("dup2".equals(func)) {
                APIStub.dup2(env); }
            else if ("ecvt".equals(func)) {
                APIStub.ecvt(env); }
            else if ("ecvt_r".equals(func)) {
                APIStub.ecvt_r(env); }
            else if ("endfsent".equals(func)) {
                APIStub.endfsent(env); }
            else if ("endgrent".equals(func)) {
                APIStub.endgrent(env); }
            else if ("endhostent".equals(func)) {
                APIStub.endhostent(env); }
            else if ("endmntent".equals(func)) {
                APIStub.endmntent(env); }
            else if ("endnetent".equals(func)) {
                APIStub.endnetent(env); }
            else if ("endnetgrent".equals(func)) {
                APIStub.endnetgrent(env); }
            else if ("endprotoent".equals(func)) {
                APIStub.endprotoent(env); }
            else if ("endpwent".equals(func)) {
                APIStub.endpwent(env); }
            else if ("endservent".equals(func)) {
                APIStub.endservent(env); }
            else if ("endutent".equals(func)) {
                APIStub.endutent(env); }
            else if ("envz_add".equals(func)) {
                APIStub.envz_add(env); }
            else if ("envz_entry".equals(func)) {
                APIStub.envz_entry(env); }
            else if ("envz_get".equals(func)) {
                APIStub.envz_get(env); }
            else if ("envz_merge".equals(func)) {
                APIStub.envz_merge(env); }
            else if ("erand48".equals(func)) {
                APIStub.erand48(env); }
            else if ("erand48_r".equals(func)) {
                APIStub.erand48_r(env); }
            else if ("erf".equals(func)) {
                APIStub.erf(env); }
            else if ("erfc".equals(func)) {
                APIStub.erfc(env); }
            else if ("erfcf".equals(func)) {
                APIStub.erfcf(env); }
            else if ("erfcl".equals(func)) {
                APIStub.erfcl(env); }
            else if ("erff".equals(func)) {
                APIStub.erff(env); }
            else if ("erfl".equals(func)) {
                APIStub.erfl(env); }
            else if ("error_at_line".equals(func)) {
                APIStub.error_at_line(env); }
            else if ("execv".equals(func)) {
                APIStub.execv(env); }
            else if ("execve".equals(func)) {
                APIStub.execve(env); }
            else if ("execvp".equals(func)) {
                APIStub.execvp(env); }
            else if ("exit".equals(func)) {
                APIStub.exit(env); }
            else if ("exp".equals(func)) {
                APIStub.exp(env); }
            else if ("exp2".equals(func)) {
                APIStub.exp2(env); }
            else if ("exp2f".equals(func)) {
                APIStub.exp2f(env); }
            else if ("exp2l".equals(func)) {
                APIStub.exp2l(env); }
            else if ("exp10".equals(func)) {
                APIStub.exp10(env); }
            else if ("exp10f".equals(func)) {
                APIStub.exp10f(env); }
            else if ("exp10l".equals(func)) {
                APIStub.exp10l(env); }
            else if ("expf".equals(func)) {
                APIStub.expf(env); }
            else if ("expl".equals(func)) {
                APIStub.expl(env); }
            else if ("explicit_bzero".equals(func)) {
                APIStub.explicit_bzero(env); }
            else if ("expm1".equals(func)) {
                APIStub.expm1(env); }
            else if ("expm1f".equals(func)) {
                APIStub.expm1f(env); }
            else if ("expm1l".equals(func)) {
                APIStub.expm1l(env); }
            else if ("fabs".equals(func)) {
                APIStub.fabs(env); }
            else if ("fabsf".equals(func)) {
                APIStub.fabsf(env); }
            else if ("fabsl".equals(func)) {
                APIStub.fabsl(env); }
            else if ("fchdir".equals(func)) {
                APIStub.fchdir(env); }
            else if ("fchmod".equals(func)) {
                APIStub.fchmod(env); }
            else if ("fchown".equals(func)) {
                APIStub.fchown(env); }
            else if ("fclose".equals(func)) {
                APIStub.fclose(env); }
            else if ("fcloseall".equals(func)) {
                APIStub.fcloseall(env); }
            else if ("fcntl".equals(func)) {
                APIStub.fcntl(env); }
            else if ("fcvt".equals(func)) {
                APIStub.fcvt(env); }
            else if ("fcvt_r".equals(func)) {
                APIStub.fcvt_r(env); }
            else if ("FD_CLR".equals(func)) {
                APIStub.FD_CLR(env); }
            else if ("FD_ISSET".equals(func)) {
                APIStub.FD_ISSET(env); }
            else if ("FD_SET".equals(func)) {
                APIStub.FD_SET(env); }
            else if ("FD_ZERO".equals(func)) {
                APIStub.FD_ZERO(env); }
            else if ("fdatasync".equals(func)) {
                APIStub.fdatasync(env); }
            else if ("fdim".equals(func)) {
                APIStub.fdim(env); }
            else if ("fdimf".equals(func)) {
                APIStub.fdimf(env); }
            else if ("fdiml".equals(func)) {
                APIStub.fdiml(env); }
            else if ("fdopen".equals(func)) {
                APIStub.fdopen(env); }
            else if ("fdopendir".equals(func)) {
                APIStub.fdopendir(env); }
            else if ("feclearexcept".equals(func)) {
                APIStub.feclearexcept(env); }
            else if ("fedisableexcept".equals(func)) {
                APIStub.fedisableexcept(env); }
            else if ("feenableexcept".equals(func)) {
                APIStub.feenableexcept(env); }
            else if ("fegetenv".equals(func)) {
                APIStub.fegetenv(env); }
            else if ("fegetexceptflag".equals(func)) {
                APIStub.fegetexceptflag(env); }
            else if ("fegetmode".equals(func)) {
                APIStub.fegetmode(env); }
            else if ("fegetround".equals(func)) {
                APIStub.fegetround(env); }
            else if ("feholdexcept".equals(func)) {
                APIStub.feholdexcept(env); }
            else if ("feof".equals(func)) {
                APIStub.feof(env); }
            else if ("feof_unlocked".equals(func)) {
                APIStub.feof_unlocked(env); }
            else if ("feraiseexcept".equals(func)) {
                APIStub.feraiseexcept(env); }
            else if ("ferror".equals(func)) {
                APIStub.ferror(env); }
            else if ("ferror_unlocked".equals(func)) {
                APIStub.ferror_unlocked(env); }
            else if ("fesetenv".equals(func)) {
                APIStub.fesetenv(env); }
            else if ("fesetexcept".equals(func)) {
                APIStub.fesetexcept(env); }
            else if ("fesetexceptflag".equals(func)) {
                APIStub.fesetexceptflag(env); }
            else if ("fesetmode".equals(func)) {
                APIStub.fesetmode(env); }
            else if ("fesetround".equals(func)) {
                APIStub.fesetround(env); }
            else if ("fetestexcept".equals(func)) {
                APIStub.fetestexcept(env); }
            else if ("fetestexceptflag".equals(func)) {
                APIStub.fetestexceptflag(env); }
            else if ("feupdateenv".equals(func)) {
                APIStub.feupdateenv(env); }
            else if ("fexecve".equals(func)) {
                APIStub.fexecve(env); }
            else if ("fflush".equals(func)) {
                APIStub.fflush(env); }
            else if ("fflush_unlocked".equals(func)) {
                APIStub.fflush_unlocked(env); }
            else if ("fgetc".equals(func)) {
                APIStub.fgetc(env); }
            else if ("fgetc_unlocked".equals(func)) {
                APIStub.fgetc_unlocked(env); }
            else if ("fgetgrent".equals(func)) {
                APIStub.fgetgrent(env); }
            else if ("fgetpos".equals(func)) {
                APIStub.fgetpos(env); }
            else if ("fgetpwent".equals(func)) {
                APIStub.fgetpwent(env); }
            else if ("fgetpwent_r".equals(func)) {
                APIStub.fgetpwent_r(env); }
            else if ("fgets".equals(func)) {
                APIStub.fgets(env); }
            else if ("fgets_unlocked".equals(func)) {
                APIStub.fgets_unlocked(env); }
            else if ("fgetwc".equals(func)) {
                APIStub.fgetwc(env); }
            else if ("fgetwc_unlocked".equals(func)) {
                APIStub.fgetwc_unlocked(env); }
            else if ("fgetws".equals(func)) {
                APIStub.fgetws(env); }
            else if ("fgetws_unlocked".equals(func)) {
                APIStub.fgetws_unlocked(env); }
            else if ("fileno".equals(func)) {
                APIStub.fileno(env); }
            else if ("fileno_unlocked".equals(func)) {
                APIStub.fileno_unlocked(env); }
            else if ("finite".equals(func)) {
                APIStub.finite(env); }
            else if ("finitef".equals(func)) {
                APIStub.finitef(env); }
            else if ("finitel".equals(func)) {
                APIStub.finitel(env); }
            else if ("flockfile".equals(func)) {
                APIStub.flockfile(env); }
            else if ("floor".equals(func)) {
                APIStub.floor(env); }
            else if ("floorf".equals(func)) {
                APIStub.floorf(env); }
            else if ("floorl".equals(func)) {
                APIStub.floorl(env); }
            else if ("fma".equals(func)) {
                APIStub.fma(env); }
            else if ("fmaf".equals(func)) {
                APIStub.fmaf(env); }
            else if ("fmal".equals(func)) {
                APIStub.fmal(env); }
            else if ("fmax".equals(func)) {
                APIStub.fmax(env); }
            else if ("fmin".equals(func)) {
                APIStub.fmin(env); }
            else if ("fminf".equals(func)) {
                APIStub.fminf(env); }
            else if ("fminl".equals(func)) {
                APIStub.fminl(env); }
            else if ("fmod".equals(func)) {
                APIStub.fmod(env); }
            else if ("fmodf".equals(func)) {
                APIStub.fmodf(env); }
            else if ("fmodl".equals(func)) {
                APIStub.fmodl(env); }
            else if ("fnmatch".equals(func)) {
                APIStub.fnmatch(env); }
            else if ("fopen".equals(func)) {
                APIStub.fopen(env); }
            else if ("fopen64".equals(func)) {
                APIStub.fopen64(env); }
            else if ("fopencookie".equals(func)) {
                APIStub.fopencookie(env); }
            else if ("fork".equals(func)) {
                APIStub.fork(env); }
            else if ("forkpty".equals(func)) {
                APIStub.forkpty(env); }
            else if ("fpathconf".equals(func)) {
                APIStub.fpathconf(env); }
            else if ("fputc".equals(func)) {
                APIStub.fputc(env); }
            else if ("fputc_unlocked".equals(func)) {
                APIStub.fputc_unlocked(env); }
            else if ("fputs".equals(func)) {
                APIStub.fputs(env); }
            else if ("fputs_unlocked".equals(func)) {
                APIStub.fputs_unlocked(env); }
            else if ("fputwc".equals(func)) {
                APIStub.fputwc(env); }
            else if ("fputwc_unlocked".equals(func)) {
                APIStub.fputwc_unlocked(env); }
            else if ("fputws".equals(func)) {
                APIStub.fputws(env); }
            else if ("fputws_unlocked".equals(func)) {
                APIStub.fputws_unlocked(env); }
            else if ("fread".equals(func)) {
                APIStub.fread(env); }
            else if ("fread_unlocked".equals(func)) {
                APIStub.fread_unlocked(env); }
            else if ("free".equals(func)) {
                APIStub.free(env); }
            else if ("freopen".equals(func)) {
                APIStub.freopen(env); }
            else if ("freopen64".equals(func)) {
                APIStub.freopen64(env); }
            else if ("frexp".equals(func)) {
                APIStub.frexp(env); }
            else if ("frexpf".equals(func)) {
                APIStub.frexpf(env); }
            else if ("frexpl".equals(func)) {
                APIStub.frexpl(env); }
            else if ("fseek".equals(func)) {
                APIStub.fseek(env); }
            else if ("fseeko".equals(func)) {
                APIStub.fseeko(env); }
            else if ("fsetpos".equals(func)) {
                APIStub.fsetpos(env); }
            else if ("fstat64".equals(func)) {
                APIStub.fstat64(env); }
            else if ("fsync".equals(func)) {
                APIStub.fsync(env); }
            else if ("ftell".equals(func)) {
                APIStub.ftell(env); }
            else if ("ftello".equals(func)) {
                APIStub.ftello(env); }
            else if ("ftello64".equals(func)) {
                APIStub.ftello64(env); }
            else if ("ftruncate".equals(func)) {
                APIStub.ftruncate(env); }
            else if ("ftruncate64".equals(func)) {
                APIStub.ftruncate64(env); }
            else if ("ftrylockfile".equals(func)) {
                APIStub.ftrylockfile(env); }
            else if ("ftw".equals(func)) {
                APIStub.ftw(env); }
            else if ("ftw64".equals(func)) {
                APIStub.ftw64(env); }
            else if ("funlockfile".equals(func)) {
                APIStub.funlockfile(env); }
            else if ("futimes".equals(func)) {
                APIStub.futimes(env); }
            else if ("fwide".equals(func)) {
                APIStub.fwide(env); }
            else if ("gamma".equals(func)) {
                APIStub.gamma(env); }
            else if ("gammaf".equals(func)) {
                APIStub.gammaf(env); }
            else if ("gammal".equals(func)) {
                APIStub.gammal(env); }
            else if ("gcvt".equals(func)) {
                APIStub.gcvt(env); }
            else if ("get_avphys_pages".equals(func)) {
                APIStub.get_avphys_pages(env); }
            else if ("get_current_dir_name".equals(func)) {
                APIStub.get_current_dir_name(env); }
            else if ("get_nprocs".equals(func)) {
                APIStub.get_nprocs(env); }
            else if ("get_nprocs_conf".equals(func)) {
                APIStub.get_nprocs_conf(env); }
            else if ("get_phys_pages".equals(func)) {
                APIStub.get_phys_pages(env); }
            else if ("getauxval".equals(func)) {
                APIStub.getauxval(env); }
            else if ("getc_unlocked".equals(func)) {
                APIStub.getc_unlocked(env); }
            else if ("getchar".equals(func)) {
                APIStub.getchar(env); }
            else if ("getchar_unlocked".equals(func)) {
                APIStub.getchar_unlocked(env); }
            else if ("getcontext".equals(func)) {
                APIStub.getcontext(env); }
            else if ("getcwd".equals(func)) {
                APIStub.getcwd(env); }
            else if ("getdate".equals(func)) {
                APIStub.getdate(env); }
            else if ("getdate_r".equals(func)) {
                APIStub.getdate_r(env); }
            else if ("getdents64".equals(func)) {
                APIStub.getdents64(env); }
            else if ("getegid".equals(func)) {
                APIStub.getegid(env); }
            else if ("getentropy".equals(func)) {
                APIStub.getentropy(env); }
            else if ("getenv".equals(func)) {
                APIStub.getenv(env); }
            else if ("geteuid".equals(func)) {
                APIStub.geteuid(env); }
            else if ("getfsent".equals(func)) {
                APIStub.getfsent(env); }
            else if ("getfsfile".equals(func)) {
                APIStub.getfsfile(env); }
            else if ("getfsspec".equals(func)) {
                APIStub.getfsspec(env); }
            else if ("getgid".equals(func)) {
                APIStub.getgid(env); }
            else if ("getgrent".equals(func)) {
                APIStub.getgrent(env); }
            else if ("getgrgid".equals(func)) {
                APIStub.getgrgid(env); }
            else if ("getgrnam".equals(func)) {
                APIStub.getgrnam(env); }
            else if ("getgroups".equals(func)) {
                APIStub.getgroups(env); }
            else if ("gethostbyname".equals(func)) {
                APIStub.gethostbyname(env); }
            else if ("gethostbyname2".equals(func)) {
                APIStub.gethostbyname2(env); }
            else if ("gethostent".equals(func)) {
                APIStub.gethostent(env); }
            else if ("gethostid".equals(func)) {
                APIStub.gethostid(env); }
            else if ("gethostname".equals(func)) {
                APIStub.gethostname(env); }
            else if ("getloadavg".equals(func)) {
                APIStub.getloadavg(env); }
            else if ("getlogin".equals(func)) {
                APIStub.getlogin(env); }
            else if ("getmntent".equals(func)) {
                APIStub.getmntent(env); }
            else if ("getmntent_r".equals(func)) {
                APIStub.getmntent_r(env); }
            else if ("getnetbyaddr".equals(func)) {
                APIStub.getnetbyaddr(env); }
            else if ("getnetbyname".equals(func)) {
                APIStub.getnetbyname(env); }
            else if ("getnetent".equals(func)) {
                APIStub.getnetent(env); }
            else if ("getnetgrent".equals(func)) {
                APIStub.getnetgrent(env); }
            else if ("getopt_long".equals(func)) {
                APIStub.getopt_long(env); }
            else if ("getopt_long_only".equals(func)) {
                APIStub.getopt_long_only(env); }
            else if ("getpagesize".equals(func)) {
                APIStub.getpagesize(env); }
            else if ("getpass".equals(func)) {
                APIStub.getpass(env); }
            else if ("getpeername".equals(func)) {
                APIStub.getpeername(env); }
            else if ("getpgid".equals(func)) {
                APIStub.getpgid(env); }
            else if ("getpgrp".equals(func)) {
                APIStub.getpgrp(env); }
            else if ("getpid".equals(func)) {
                APIStub.getpid(env); }
            else if ("getppid".equals(func)) {
                APIStub.getppid(env); }
            else if ("getpriority".equals(func)) {
                APIStub.getpriority(env); }
            else if ("getprotobyname".equals(func)) {
                APIStub.getprotobyname(env); }
            else if ("getprotobynumber".equals(func)) {
                APIStub.getprotobynumber(env); }
            else if ("getprotoent".equals(func)) {
                APIStub.getprotoent(env); }
            else if ("getpt".equals(func)) {
                APIStub.getpt(env); }
            else if ("getpwent".equals(func)) {
                APIStub.getpwent(env); }
            else if ("getpwent_r".equals(func)) {
                APIStub.getpwent_r(env); }
            else if ("getpwnam".equals(func)) {
                APIStub.getpwnam(env); }
            else if ("getpwnam_r".equals(func)) {
                APIStub.getpwnam_r(env); }
            else if ("getpwuid".equals(func)) {
                APIStub.getpwuid(env); }
            else if ("getpwuid_r".equals(func)) {
                APIStub.getpwuid_r(env); }
            else if ("getrandom".equals(func)) {
                APIStub.getrandom(env); }
            else if ("getrlimit".equals(func)) {
                APIStub.getrlimit(env); }
            else if ("getrlimit64".equals(func)) {
                APIStub.getrlimit64(env); }
            else if ("gets".equals(func)) {
                APIStub.gets(env); }
            else if ("getservbyname".equals(func)) {
                APIStub.getservbyname(env); }
            else if ("getservbyport".equals(func)) {
                APIStub.getservbyport(env); }
            else if ("getservent".equals(func)) {
                APIStub.getservent(env); }
            else if ("getsid".equals(func)) {
                APIStub.getsid(env); }
            else if ("getsockname".equals(func)) {
                APIStub.getsockname(env); }
            else if ("getsockopt".equals(func)) {
                APIStub.getsockopt(env); }
            else if ("getsubopt".equals(func)) {
                APIStub.getsubopt(env); }
            else if ("gettext".equals(func)) {
                APIStub.gettext(env); }
            else if ("gettid".equals(func)) {
                APIStub.gettid(env); }
            else if ("gettimeofday".equals(func)) {
                APIStub.gettimeofday(env); }
            else if ("getuid".equals(func)) {
                APIStub.getuid(env); }
            else if ("getumask".equals(func)) {
                APIStub.getumask(env); }
            else if ("getutent".equals(func)) {
                APIStub.getutent(env); }
            else if ("getutid".equals(func)) {
                APIStub.getutid(env); }
            else if ("getutid_r".equals(func)) {
                APIStub.getutid_r(env); }
            else if ("getutline".equals(func)) {
                APIStub.getutline(env); }
            else if ("getutline_r".equals(func)) {
                APIStub.getutline_r(env); }
            else if ("getutmp".equals(func)) {
                APIStub.getutmp(env); }
            else if ("getutmpx".equals(func)) {
                APIStub.getutmpx(env); }
            else if ("getw".equals(func)) {
                APIStub.getw(env); }
            else if ("getwc_unlocked".equals(func)) {
                APIStub.getwc_unlocked(env); }
            else if ("getwchar".equals(func)) {
                APIStub.getwchar(env); }
            else if ("getwchar_unlocked".equals(func)) {
                APIStub.getwchar_unlocked(env); }
            else if ("getwd".equals(func)) {
                APIStub.getwd(env); }
            else if ("glob64".equals(func)) {
                APIStub.glob64(env); }
            else if ("globfree".equals(func)) {
                APIStub.globfree(env); }
            else if ("globfree64".equals(func)) {
                APIStub.globfree64(env); }
            else if ("gmtime".equals(func)) {
                APIStub.gmtime(env); }
            else if ("gmtime_r".equals(func)) {
                APIStub.gmtime_r(env); }
            else if ("grantpt".equals(func)) {
                APIStub.grantpt(env); }
            else if ("gsignal".equals(func)) {
                APIStub.gsignal(env); }
            else if ("hasmntopt".equals(func)) {
                APIStub.hasmntopt(env); }
            else if ("hcreate".equals(func)) {
                APIStub.hcreate(env); }
            else if ("hdestroy".equals(func)) {
                APIStub.hdestroy(env); }
            else if ("hsearch".equals(func)) {
                APIStub.hsearch(env); }
            else if ("hsearch_r".equals(func)) {
                APIStub.hsearch_r(env); }
            else if ("htonl".equals(func)) {
                APIStub.htonl(env); }
            else if ("htons".equals(func)) {
                APIStub.htons(env); }
            else if ("hypot".equals(func)) {
                APIStub.hypot(env); }
            else if ("hypotf".equals(func)) {
                APIStub.hypotf(env); }
            else if ("hypotl".equals(func)) {
                APIStub.hypotl(env); }
            else if ("iconv_close".equals(func)) {
                APIStub.iconv_close(env); }
            else if ("iconv_open".equals(func)) {
                APIStub.iconv_open(env); }
            else if ("if_indextoname".equals(func)) {
                APIStub.if_indextoname(env); }
            else if ("if_nametoindex".equals(func)) {
                APIStub.if_nametoindex(env); }
            else if ("ilogb".equals(func)) {
                APIStub.ilogb(env); }
            else if ("ilogbf".equals(func)) {
                APIStub.ilogbf(env); }
            else if ("ilogbl".equals(func)) {
                APIStub.ilogbl(env); }
            else if ("imaxabs".equals(func)) {
                APIStub.imaxabs(env); }
            else if ("imaxdiv".equals(func)) {
                APIStub.imaxdiv(env); }
            else if ("index".equals(func)) {
                APIStub.index(env); }
            else if ("inet_addr".equals(func)) {
                APIStub.inet_addr(env); }
            else if ("inet_aton".equals(func)) {
                APIStub.inet_aton(env); }
            else if ("inet_lnaof".equals(func)) {
                APIStub.inet_lnaof(env); }
            else if ("inet_makeaddr".equals(func)) {
                APIStub.inet_makeaddr(env); }
            else if ("inet_netof".equals(func)) {
                APIStub.inet_netof(env); }
            else if ("inet_network".equals(func)) {
                APIStub.inet_network(env); }
            else if ("inet_ntoa".equals(func)) {
                APIStub.inet_ntoa(env); }
            else if ("inet_ntop".equals(func)) {
                APIStub.inet_ntop(env); }
            else if ("inet_pton".equals(func)) {
                APIStub.inet_pton(env); }
            else if ("initgroups".equals(func)) {
                APIStub.initgroups(env); }
            else if ("initstate".equals(func)) {
                APIStub.initstate(env); }
            else if ("initstate_r".equals(func)) {
                APIStub.initstate_r(env); }
            else if ("isalnum".equals(func)) {
                APIStub.isalnum(env); }
            else if ("isalpha".equals(func)) {
                APIStub.isalpha(env); }
            else if ("isascii".equals(func)) {
                APIStub.isascii(env); }
            else if ("isatty".equals(func)) {
                APIStub.isatty(env); }
            else if ("isblank".equals(func)) {
                APIStub.isblank(env); }
            else if ("iscntrl".equals(func)) {
                APIStub.iscntrl(env); }
            else if ("isdigit".equals(func)) {
                APIStub.isdigit(env); }
            else if ("isgraph".equals(func)) {
                APIStub.isgraph(env); }
            else if ("isinff".equals(func)) {
                APIStub.isinff(env); }
            else if ("isinfl".equals(func)) {
                APIStub.isinfl(env); }
            else if ("islower".equals(func)) {
                APIStub.islower(env); }
            else if ("isnanf".equals(func)) {
                APIStub.isnanf(env); }
            else if ("isnanl".equals(func)) {
                APIStub.isnanl(env); }
            else if ("isprint".equals(func)) {
                APIStub.isprint(env); }
            else if ("ispunct".equals(func)) {
                APIStub.ispunct(env); }
            else if ("isspace".equals(func)) {
                APIStub.isspace(env); }
            else if ("isupper".equals(func)) {
                APIStub.isupper(env); }
            else if ("iswalnum".equals(func)) {
                APIStub.iswalnum(env); }
            else if ("iswalpha".equals(func)) {
                APIStub.iswalpha(env); }
            else if ("iswblank".equals(func)) {
                APIStub.iswblank(env); }
            else if ("iswcntrl".equals(func)) {
                APIStub.iswcntrl(env); }
            else if ("iswctype".equals(func)) {
                APIStub.iswctype(env); }
            else if ("iswdigit".equals(func)) {
                APIStub.iswdigit(env); }
            else if ("iswgraph".equals(func)) {
                APIStub.iswgraph(env); }
            else if ("iswlower".equals(func)) {
                APIStub.iswlower(env); }
            else if ("iswprint".equals(func)) {
                APIStub.iswprint(env); }
            else if ("iswpunct".equals(func)) {
                APIStub.iswpunct(env); }
            else if ("iswspace".equals(func)) {
                APIStub.iswspace(env); }
            else if ("iswupper".equals(func)) {
                APIStub.iswupper(env); }
            else if ("iswxdigit".equals(func)) {
                APIStub.iswxdigit(env); }
            else if ("isxdigit".equals(func)) {
                APIStub.isxdigit(env); }
            else if ("j0".equals(func)) {
                APIStub.j0(env); }
            else if ("j0f".equals(func)) {
                APIStub.j0f(env); }
            else if ("j0l".equals(func)) {
                APIStub.j0l(env); }
            else if ("j1".equals(func)) {
                APIStub.j1(env); }
            else if ("j1f".equals(func)) {
                APIStub.j1f(env); }
            else if ("j1l".equals(func)) {
                APIStub.j1l(env); }
            else if ("jn".equals(func)) {
                APIStub.jn(env); }
            else if ("jnf".equals(func)) {
                APIStub.jnf(env); }
            else if ("jnl".equals(func)) {
                APIStub.jnl(env); }
            else if ("jrand48".equals(func)) {
                APIStub.jrand48(env); }
            else if ("jrand48_r".equals(func)) {
                APIStub.jrand48_r(env); }
            else if ("kill".equals(func)) {
                APIStub.kill(env); }
            else if ("killpg".equals(func)) {
                APIStub.killpg(env); }
            else if ("l64a".equals(func)) {
                APIStub.l64a(env); }
            else if ("labs".equals(func)) {
                APIStub.labs(env); }
            else if ("lcong48".equals(func)) {
                APIStub.lcong48(env); }
            else if ("lcong48_r".equals(func)) {
                APIStub.lcong48_r(env); }
            else if ("ldexp".equals(func)) {
                APIStub.ldexp(env); }
            else if ("ldexpf".equals(func)) {
                APIStub.ldexpf(env); }
            else if ("ldexpl".equals(func)) {
                APIStub.ldexpl(env); }
            else if ("ldiv".equals(func)) {
                APIStub.ldiv(env); }
            else if ("lgamma".equals(func)) {
                APIStub.lgamma(env); }
            else if ("lgamma_r".equals(func)) {
                APIStub.lgamma_r(env); }
            else if ("lgammaf".equals(func)) {
                APIStub.lgammaf(env); }
            else if ("lgammaf_r".equals(func)) {
                APIStub.lgammaf_r(env); }
            else if ("lgammal".equals(func)) {
                APIStub.lgammal(env); }
            else if ("lgammal_r".equals(func)) {
                APIStub.lgammal_r(env); }
            else if ("link".equals(func)) {
                APIStub.link(env); }
            else if ("linkat".equals(func)) {
                APIStub.linkat(env); }
            else if ("lio_listio".equals(func)) {
                APIStub.lio_listio(env); }
            else if ("lio_listio64".equals(func)) {
                APIStub.lio_listio64(env); }
            else if ("listen".equals(func)) {
                APIStub.listen(env); }
            else if ("llabs".equals(func)) {
                APIStub.llabs(env); }
            else if ("lldiv".equals(func)) {
                APIStub.lldiv(env); }
            else if ("llrint".equals(func)) {
                APIStub.llrint(env); }
            else if ("llrintf".equals(func)) {
                APIStub.llrintf(env); }
            else if ("llrintl".equals(func)) {
                APIStub.llrintl(env); }
            else if ("llround".equals(func)) {
                APIStub.llround(env); }
            else if ("llroundf".equals(func)) {
                APIStub.llroundf(env); }
            else if ("llroundl".equals(func)) {
                APIStub.llroundl(env); }
            else if ("localeconv".equals(func)) {
                APIStub.localeconv(env); }
            else if ("localtime".equals(func)) {
                APIStub.localtime(env); }
            else if ("localtime_r".equals(func)) {
                APIStub.localtime_r(env); }
            else if ("log".equals(func)) {
                APIStub.log(env); }
            else if ("log1p".equals(func)) {
                APIStub.log1p(env); }
            else if ("log1pf".equals(func)) {
                APIStub.log1pf(env); }
            else if ("log1pl".equals(func)) {
                APIStub.log1pl(env); }
            else if ("log2".equals(func)) {
                APIStub.log2(env); }
            else if ("log2f".equals(func)) {
                APIStub.log2f(env); }
            else if ("log2l".equals(func)) {
                APIStub.log2l(env); }
            else if ("log10".equals(func)) {
                APIStub.log10(env); }
            else if ("log10f".equals(func)) {
                APIStub.log10f(env); }
            else if ("log10l".equals(func)) {
                APIStub.log10l(env); }
            else if ("logb".equals(func)) {
                APIStub.logb(env); }
            else if ("logbf".equals(func)) {
                APIStub.logbf(env); }
            else if ("logbl".equals(func)) {
                APIStub.logbl(env); }
            else if ("logf".equals(func)) {
                APIStub.logf(env); }
            else if ("login".equals(func)) {
                APIStub.login(env); }
            else if ("login_tty".equals(func)) {
                APIStub.login_tty(env); }
            else if ("logl".equals(func)) {
                APIStub.logl(env); }
            else if ("logout".equals(func)) {
                APIStub.logout(env); }
            else if ("logwtmp".equals(func)) {
                APIStub.logwtmp(env); }
            else if ("longjmp".equals(func)) {
                APIStub.longjmp(env); }
            else if ("lrand48".equals(func)) {
                APIStub.lrand48(env); }
            else if ("lrint".equals(func)) {
                APIStub.lrint(env); }
            else if ("lrintf".equals(func)) {
                APIStub.lrintf(env); }
            else if ("lrintl".equals(func)) {
                APIStub.lrintl(env); }
            else if ("lround".equals(func)) {
                APIStub.lround(env); }
            else if ("lroundf".equals(func)) {
                APIStub.lroundf(env); }
            else if ("lroundl".equals(func)) {
                APIStub.lroundl(env); }
            else if ("lseek".equals(func)) {
                APIStub.lseek(env); }
            else if ("lseek64".equals(func)) {
                APIStub.lseek64(env); }
            else if ("lstat64".equals(func)) {
                APIStub.lstat64(env); }
            else if ("lutimes".equals(func)) {
                APIStub.lutimes(env); }
            else if ("madvise".equals(func)) {
                APIStub.madvise(env); }
            else if ("main".equals(func)) {
                APIStub.main(env); }
            else if ("makecontext".equals(func)) {
                //APIStub.makecontext(env);
            }
            else if ("malloc".equals(func)) {
                APIStub.malloc(env); }
            else if ("mallopt".equals(func)) {
                APIStub.mallopt(env); }
            else if ("mblen".equals(func)) {
                APIStub.mblen(env); }
            else if ("mbrlen".equals(func)) {
                APIStub.mbrlen(env); }
            else if ("mbrtowc".equals(func)) {
                APIStub.mbrtowc(env); }
            else if ("mbsinit".equals(func)) {
                APIStub.mbsinit(env); }
            else if ("mbsnrtowcs".equals(func)) {
                APIStub.mbsnrtowcs(env); }
            else if ("mbsrtowcs".equals(func)) {
                APIStub.mbsrtowcs(env); }
            else if ("mbstowcs".equals(func)) {
                APIStub.mbstowcs(env); }
            else if ("mbtowc".equals(func)) {
                APIStub.mbtowc(env); }
            else if ("memccpy".equals(func)) {
                APIStub.memccpy(env); }
            else if ("memchr".equals(func)) {
                APIStub.memchr(env); }
            else if ("memcmp".equals(func)) {
                APIStub.memcmp(env); }
            else if ("memcpy".equals(func)) {
                APIStub.memcpy(env); }
            else if ("memfd_create".equals(func)) {
                APIStub.memfd_create(env); }
            else if ("memfrob".equals(func)) {
                APIStub.memfrob(env); }
            else if ("memmem".equals(func)) {
                APIStub.memmem(env); }
            else if ("memmove".equals(func)) {
                APIStub.memmove(env); }
            else if ("mempcpy".equals(func)) {
                APIStub.mempcpy(env); }
            else if ("memrchr".equals(func)) {
                APIStub.memrchr(env); }
            else if ("memset".equals(func)) {
                APIStub.memset(env); }
            else if ("mkdir".equals(func)) {
                APIStub.mkdir(env); }
            else if ("mkdtemp".equals(func)) {
                APIStub.mkdtemp(env); }
            else if ("mkfifo".equals(func)) {
                APIStub.mkfifo(env); }
            else if ("mknod".equals(func)) {
                APIStub.mknod(env); }
            else if ("mkstemp".equals(func)) {
                APIStub.mkstemp(env); }
            else if ("mktemp".equals(func)) {
                APIStub.mktemp(env); }
            else if ("mktime".equals(func)) {
                APIStub.mktime(env); }
            else if ("mlock".equals(func)) {
                APIStub.mlock(env); }
            else if ("mlock2".equals(func)) {
                APIStub.mlock2(env); }
            else if ("mlockall".equals(func)) {
                APIStub.mlockall(env); }
            else if ("mmap".equals(func)) {
                APIStub.mmap(env); }
            else if ("mmap64".equals(func)) {
                APIStub.mmap64(env); }
            else if ("modf".equals(func)) {
                APIStub.modf(env); }
            else if ("modff".equals(func)) {
                APIStub.modff(env); }
            else if ("modfl".equals(func)) {
                APIStub.modfl(env); }
            else if ("mount".equals(func)) {
                APIStub.mount(env); }
            else if ("mprotect".equals(func)) {
                APIStub.mprotect(env); }
            else if ("mrand48".equals(func)) {
                APIStub.mrand48(env); }
            else if ("mremap".equals(func)) {
                APIStub.mremap(env); }
            else if ("msync".equals(func)) {
                APIStub.msync(env); }
            else if ("mtrace".equals(func)) {
                APIStub.mtrace(env); }
            else if ("munlock".equals(func)) {
                APIStub.munlock(env); }
            else if ("munlockall".equals(func)) {
                APIStub.munlockall(env); }
            else if ("munmap".equals(func)) {
                APIStub.munmap(env); }
            else if ("muntrace".equals(func)) {
                APIStub.muntrace(env); }
            else if ("nanf".equals(func)) {
                APIStub.nanf(env); }
            else if ("nanl".equals(func)) {
                APIStub.nanl(env); }
            else if ("nanosleep".equals(func)) {
                APIStub.nanosleep(env); }
            else if ("nearbyint".equals(func)) {
                APIStub.nearbyint(env); }
            else if ("nearbyintf".equals(func)) {
                APIStub.nearbyintf(env); }
            else if ("nearbyintl".equals(func)) {
                APIStub.nearbyintl(env); }
            else if ("nextafter".equals(func)) {
                APIStub.nextafter(env); }
            else if ("nextafterf".equals(func)) {
                APIStub.nextafterf(env); }
            else if ("nextafterl".equals(func)) {
                APIStub.nextafterl(env); }
            else if ("nextdown".equals(func)) {
                APIStub.nextdown(env); }
            else if ("nextdownf".equals(func)) {
                APIStub.nextdownf(env); }
            else if ("nextdownl".equals(func)) {
                APIStub.nextdownl(env); }
            else if ("nexttoward".equals(func)) {
                APIStub.nexttoward(env); }
            else if ("nexttowardf".equals(func)) {
                APIStub.nexttowardf(env); }
            else if ("nexttowardl".equals(func)) {
                APIStub.nexttowardl(env); }
            else if ("nextup".equals(func)) {
                APIStub.nextup(env); }
            else if ("nextupf".equals(func)) {
                APIStub.nextupf(env); }
            else if ("nextupl".equals(func)) {
                APIStub.nextupl(env); }
            else if ("nftw".equals(func)) {
                APIStub.nftw(env); }
            else if ("nftw64".equals(func)) {
                APIStub.nftw64(env); }
            else if ("ngettext".equals(func)) {
                APIStub.ngettext(env); }
            else if ("nice".equals(func)) {
                APIStub.nice(env); }
            else if ("nl_langinfo".equals(func)) {
                APIStub.nl_langinfo(env); }
            else if ("notfound".equals(func)) {
                APIStub.notfound(env); }
            else if ("nrand48".equals(func)) {
                APIStub.nrand48(env); }
            else if ("nrand48_r".equals(func)) {
                APIStub.nrand48_r(env); }
            else if ("ntohl".equals(func)) {
                APIStub.ntohl(env); }
            else if ("ntohs".equals(func)) {
                APIStub.ntohs(env); }
            else if ("ntp_adjtime".equals(func)) {
                APIStub.ntp_adjtime(env); }
            else if ("ntp_gettime".equals(func)) {
                APIStub.ntp_gettime(env); }
            else if ("open".equals(func)) {
                APIStub.open(env); }
            else if ("opendir".equals(func)) {
                APIStub.opendir(env); }
            else if ("openlog".equals(func)) {
                APIStub.openlog(env); }
            else if ("openpty".equals(func)) {
                APIStub.openpty(env); }
            else if ("parse_printf_format".equals(func)) {
                APIStub.parse_printf_format(env); }
            else if ("pause".equals(func)) {
                APIStub.pause(env); }
            else if ("pclose".equals(func)) {
                APIStub.pclose(env); }
            else if ("perror".equals(func)) {
                APIStub.perror(env); }
            else if ("pipe".equals(func)) {
                APIStub.pipe(env); }
            else if ("pkey_alloc".equals(func)) {
                APIStub.pkey_alloc(env); }
            else if ("pkey_free".equals(func)) {
                APIStub.pkey_free(env); }
            else if ("pkey_get".equals(func)) {
                APIStub.pkey_get(env); }
            else if ("pkey_mprotect".equals(func)) {
                APIStub.pkey_mprotect(env); }
            else if ("pkey_set".equals(func)) {
                APIStub.pkey_set(env); }
            else if ("popen".equals(func)) {
                APIStub.popen(env); }
            else if ("posix_fallocate".equals(func)) {
                APIStub.posix_fallocate(env); }
            else if ("posix_memalign".equals(func)) {
                APIStub.posix_memalign(env); }
            else if ("pow".equals(func)) {
                APIStub.pow(env); }
            else if ("powf".equals(func)) {
                APIStub.powf(env); }
            else if ("powl".equals(func)) {
                APIStub.powl(env); }
            else if ("pread".equals(func)) {
                APIStub.pread(env); }
            else if ("pread64".equals(func)) {
                APIStub.pread64(env); }
            else if ("preadv".equals(func)) {
                APIStub.preadv(env); }
            else if ("preadv2".equals(func)) {
                APIStub.preadv2(env); }
            else if ("preadv64".equals(func)) {
                APIStub.preadv64(env); }
            else if ("preadv64v2".equals(func)) {
                APIStub.preadv64v2(env); }
            else if ("printf".equals(func)) {
                APIStub.printf(env); }
            else if ("printf_size".equals(func)) {
                APIStub.printf_size(env); }
            else if ("printf_size_info".equals(func)) {
                APIStub.printf_size_info(env); }
            else if ("psignal".equals(func)) {
                APIStub.psignal(env); }
            else if ("pthread_cond_clockwait".equals(func)) {
                APIStub.pthread_cond_clockwait(env); }
            else if ("pthread_getattr_default_np".equals(func)) {
                APIStub.pthread_getattr_default_np(env); }
            else if ("pthread_key_create".equals(func)) {
                APIStub.pthread_key_create(env); }
            else if ("pthread_key_delete".equals(func)) {
                APIStub.pthread_key_delete(env); }
            else if ("pthread_rwlock_clockrdlock".equals(func)) {
                APIStub.pthread_rwlock_clockrdlock(env); }
            else if ("pthread_rwlock_clockwrlock".equals(func)) {
                APIStub.pthread_rwlock_clockwrlock(env); }
            else if ("pthread_setattr_default_np".equals(func)) {
                APIStub.pthread_setattr_default_np(env); }
            else if ("pthread_setspecific".equals(func)) {
                APIStub.pthread_setspecific(env); }
            else if ("pthread_tryjoin_np".equals(func)) {
                APIStub.pthread_tryjoin_np(env); }
            else if ("ptsname".equals(func)) {
                APIStub.ptsname(env); }
            else if ("ptsname_r".equals(func)) {
                APIStub.ptsname_r(env); }
            else if ("putc_unlocked".equals(func)) {
                APIStub.putc_unlocked(env); }
            else if ("putchar".equals(func)) {
                APIStub.putchar(env); }
            else if ("putchar_unlocked".equals(func)) {
                APIStub.putchar_unlocked(env); }
            else if ("putenv".equals(func)) {
                APIStub.putenv(env); }
            else if ("putpwent".equals(func)) {
                APIStub.putpwent(env); }
            else if ("pututline".equals(func)) {
                APIStub.pututline(env); }
            else if ("putw".equals(func)) {
                APIStub.putw(env); }
            else if ("putwchar".equals(func)) {
                APIStub.putwchar(env); }
            else if ("putwchar_unlocked".equals(func)) {
                APIStub.putwchar_unlocked(env); }
            else if ("pwrite".equals(func)) {
                APIStub.pwrite(env); }
            else if ("pwrite64".equals(func)) {
                APIStub.pwrite64(env); }
            else if ("pwritev".equals(func)) {
                APIStub.pwritev(env); }
            else if ("pwritev2".equals(func)) {
                APIStub.pwritev2(env); }
            else if ("pwritev64".equals(func)) {
                APIStub.pwritev64(env); }
            else if ("pwritev64v2".equals(func)) {
                APIStub.pwritev64v2(env); }
            else if ("qecvt".equals(func)) {
                APIStub.qecvt(env); }
            else if ("qecvt_r".equals(func)) {
                APIStub.qecvt_r(env); }
            else if ("qfcvt".equals(func)) {
                APIStub.qfcvt(env); }
            else if ("qfcvt_r".equals(func)) {
                APIStub.qfcvt_r(env); }
            else if ("qgcvt".equals(func)) {
                APIStub.qgcvt(env); }
            else if ("qsort".equals(func)) {
                APIStub.qsort(env); }
            else if ("raise".equals(func)) {
                APIStub.raise(env); }
            else if ("rand".equals(func)) {
                APIStub.rand(env); }
            else if ("rand_r".equals(func)) {
                APIStub.rand_r(env); }
            else if ("random".equals(func)) {
                APIStub.random(env); }
            else if ("random_r".equals(func)) {
                APIStub.random_r(env); }
            else if ("rawmemchr".equals(func)) {
                APIStub.rawmemchr(env); }
            else if ("read".equals(func)) {
                APIStub.read(env); }
            else if ("readdir64_r".equals(func)) {
                APIStub.readdir64_r(env); }
            else if ("readlink".equals(func)) {
                APIStub.readlink(env); }
            else if ("readv".equals(func)) {
                APIStub.readv(env); }
            else if ("realloc".equals(func)) {
                APIStub.realloc(env); }
            else if ("reallocarray".equals(func)) {
                APIStub.reallocarray(env); }
            else if ("realpath".equals(func)) {
                APIStub.realpath(env); }
            else if ("recv".equals(func)) {
                APIStub.recv(env); }
            else if ("regcomp".equals(func)) {
                APIStub.regcomp(env); }
            else if ("regfree".equals(func)) {
                APIStub.regfree(env); }
            else if ("register_printf_function".equals(func)) {
                APIStub.register_printf_function(env); }
            else if ("remainder".equals(func)) {
                APIStub.remainder(env); }
            else if ("remainderf".equals(func)) {
                APIStub.remainderf(env); }
            else if ("remainderl".equals(func)) {
                APIStub.remainderl(env); }
            else if ("remove".equals(func)) {
                APIStub.remove(env); }
            else if ("rename".equals(func)) {
                APIStub.rename(env); }
            else if ("rewind".equals(func)) {
                APIStub.rewind(env); }
            else if ("rewinddir".equals(func)) {
                APIStub.rewinddir(env); }
            else if ("rindex".equals(func)) {
                APIStub.rindex(env); }
            else if ("rint".equals(func)) {
                APIStub.rint(env); }
            else if ("rintf".equals(func)) {
                APIStub.rintf(env); }
            else if ("rintl".equals(func)) {
                APIStub.rintl(env); }
            else if ("rmdir".equals(func)) {
                APIStub.rmdir(env); }
            else if ("round".equals(func)) {
                APIStub.round(env); }
            else if ("roundf".equals(func)) {
                APIStub.roundf(env); }
            else if ("roundl".equals(func)) {
                APIStub.roundl(env); }
            else if ("rpmatch".equals(func)) {
                APIStub.rpmatch(env); }
            else if ("scalb".equals(func)) {
                APIStub.scalb(env); }
            else if ("scalbf".equals(func)) {
                APIStub.scalbf(env); }
            else if ("scalbl".equals(func)) {
                APIStub.scalbl(env); }
            else if ("scalbln".equals(func)) {
                APIStub.scalbln(env); }
            else if ("scalblnf".equals(func)) {
                APIStub.scalblnf(env); }
            else if ("scalblnl".equals(func)) {
                APIStub.scalblnl(env); }
            else if ("scalbn".equals(func)) {
                APIStub.scalbn(env); }
            else if ("scalbnf".equals(func)) {
                APIStub.scalbnf(env); }
            else if ("scalbnl".equals(func)) {
                APIStub.scalbnl(env); }
            else if ("scandir".equals(func)) {
                APIStub.scandir(env); }
            else if ("scandir64".equals(func)) {
                APIStub.scandir64(env); }
            else if ("sched_get_priority_max".equals(func)) {
                APIStub.sched_get_priority_max(env); }
            else if ("sched_get_priority_min".equals(func)) {
                APIStub.sched_get_priority_min(env); }
            else if ("sched_getaffinity".equals(func)) {
                APIStub.sched_getaffinity(env); }
            else if ("sched_getparam".equals(func)) {
                APIStub.sched_getparam(env); }
            else if ("sched_getscheduler".equals(func)) {
                APIStub.sched_getscheduler(env); }
            else if ("sched_rr_get_interval".equals(func)) {
                APIStub.sched_rr_get_interval(env); }
            else if ("sched_setaffinity".equals(func)) {
                APIStub.sched_setaffinity(env); }
            else if ("sched_setparam".equals(func)) {
                APIStub.sched_setparam(env); }
            else if ("sched_setscheduler".equals(func)) {
                APIStub.sched_setscheduler(env); }
            else if ("sched_yield".equals(func)) {
                APIStub.sched_yield(env); }
            else if ("secure_getenv".equals(func)) {
                APIStub.secure_getenv(env); }
            else if ("seed48".equals(func)) {
                APIStub.seed48(env); }
            else if ("seed48_r".equals(func)) {
                APIStub.seed48_r(env); }
            else if ("seekdir".equals(func)) {
                APIStub.seekdir(env); }
            else if ("sem_clockwait".equals(func)) {
                APIStub.sem_clockwait(env); }
            else if ("sem_close".equals(func)) {
                APIStub.sem_close(env); }
            else if ("sem_destroy".equals(func)) {
                APIStub.sem_destroy(env); }
            else if ("sem_getvalue".equals(func)) {
                APIStub.sem_getvalue(env); }
            else if ("sem_init".equals(func)) {
                APIStub.sem_init(env); }
            else if ("sem_post".equals(func)) {
                APIStub.sem_post(env); }
            else if ("sem_timedwait".equals(func)) {
                APIStub.sem_timedwait(env); }
            else if ("sem_trywait".equals(func)) {
                APIStub.sem_trywait(env); }
            else if ("sem_unlink".equals(func)) {
                APIStub.sem_unlink(env); }
            else if ("sem_wait".equals(func)) {
                APIStub.sem_wait(env); }
            else if ("semget".equals(func)) {
                APIStub.semget(env); }
            else if ("semop".equals(func)) {
                APIStub.semop(env); }
            else if ("semtimedop".equals(func)) {
                APIStub.semtimedop(env); }
            else if ("send".equals(func)) {
                APIStub.send(env); }
            else if ("setbuf".equals(func)) {
                APIStub.setbuf(env); }
            else if ("setbuffer".equals(func)) {
                APIStub.setbuffer(env); }
            else if ("setcontext".equals(func)) {
                APIStub.setcontext(env); }
            else if ("setdomainname".equals(func)) {
                APIStub.setdomainname(env); }
            else if ("setegid".equals(func)) {
                APIStub.setegid(env); }
            else if ("setenv".equals(func)) {
                APIStub.setenv(env); }
            else if ("seteuid".equals(func)) {
                APIStub.seteuid(env); }
            else if ("setfsent".equals(func)) {
                APIStub.setfsent(env); }
            else if ("setgid".equals(func)) {
                APIStub.setgid(env); }
            else if ("setgrent".equals(func)) {
                APIStub.setgrent(env); }
            else if ("setgroups".equals(func)) {
                APIStub.setgroups(env); }
            else if ("sethostent".equals(func)) {
                APIStub.sethostent(env); }
            else if ("sethostid".equals(func)) {
                APIStub.sethostid(env); }
            else if ("sethostname".equals(func)) {
                APIStub.sethostname(env); }
            else if ("setitimer".equals(func)) {
                APIStub.setitimer(env); }
            else if ("setjmp".equals(func)) {
                APIStub.setjmp(env); }
            else if ("setlinebuf".equals(func)) {
                APIStub.setlinebuf(env); }
            else if ("setlocale".equals(func)) {
                APIStub.setlocale(env); }
            else if ("setlogmask".equals(func)) {
                APIStub.setlogmask(env); }
            else if ("setmntent".equals(func)) {
                APIStub.setmntent(env); }
            else if ("setnetent".equals(func)) {
                APIStub.setnetent(env); }
            else if ("setnetgrent".equals(func)) {
                APIStub.setnetgrent(env); }
            else if ("setpgid".equals(func)) {
                APIStub.setpgid(env); }
            else if ("setpgrp".equals(func)) {
                APIStub.setpgrp(env); }
            else if ("setpriority".equals(func)) {
                APIStub.setpriority(env); }
            else if ("setprotoent".equals(func)) {
                APIStub.setprotoent(env); }
            else if ("setpwent".equals(func)) {
                APIStub.setpwent(env); }
            else if ("setregid".equals(func)) {
                APIStub.setregid(env); }
            else if ("setreuid".equals(func)) {
                APIStub.setreuid(env); }
            else if ("setrlimit".equals(func)) {
                APIStub.setrlimit(env); }
            else if ("setrlimit64".equals(func)) {
                APIStub.setrlimit64(env); }
            else if ("setservent".equals(func)) {
                APIStub.setservent(env); }
            else if ("setsid".equals(func)) {
                APIStub.setsid(env); }
            else if ("setsockopt".equals(func)) {
                APIStub.setsockopt(env); }
            else if ("setstate".equals(func)) {
                APIStub.setstate(env); }
            else if ("setstate_r".equals(func)) {
                APIStub.setstate_r(env); }
            else if ("settimeofday".equals(func)) {
                APIStub.settimeofday(env); }
            else if ("setuid".equals(func)) {
                APIStub.setuid(env); }
            else if ("setutent".equals(func)) {
                APIStub.setutent(env); }
            else if ("setvbuf".equals(func)) {
                APIStub.setvbuf(env); }
            else if ("shm_open".equals(func)) {
                APIStub.shm_open(env); }
            else if ("shm_unlink".equals(func)) {
                APIStub.shm_unlink(env); }
            else if ("shutdown".equals(func)) {
                APIStub.shutdown(env); }
            else if ("sigabbrev_np".equals(func)) {
                APIStub.sigabbrev_np(env); }
            else if ("sigaddset".equals(func)) {
                APIStub.sigaddset(env); }
            else if ("sigaltstack".equals(func)) {
                APIStub.sigaltstack(env); }
            else if ("sigblock".equals(func)) {
                APIStub.sigblock(env); }
            else if ("sigdelset".equals(func)) {
                APIStub.sigdelset(env); }
            else if ("sigdescr_np".equals(func)) {
                APIStub.sigdescr_np(env); }
            else if ("sigemptyset".equals(func)) {
                APIStub.sigemptyset(env); }
            else if ("sigfillset".equals(func)) {
                APIStub.sigfillset(env); }
            else if ("siginterrupt".equals(func)) {
                APIStub.siginterrupt(env); }
            else if ("sigismember".equals(func)) {
                APIStub.sigismember(env); }
            else if ("siglongjmp".equals(func)) {
                APIStub.siglongjmp(env); }
            else if ("sigmask".equals(func)) {
                APIStub.sigmask(env); }
            else if ("signal".equals(func)) {
                APIStub.signal(env); }
            else if ("significand".equals(func)) {
                APIStub.significand(env); }
            else if ("significandf".equals(func)) {
                APIStub.significandf(env); }
            else if ("significandl".equals(func)) {
                APIStub.significandl(env); }
            else if ("sigpause".equals(func)) {
                APIStub.sigpause(env); }
            else if ("sigpending".equals(func)) {
                APIStub.sigpending(env); }
            else if ("sigprocmask".equals(func)) {
                APIStub.sigprocmask(env); }
            else if ("sigsetjmp".equals(func)) {
                APIStub.sigsetjmp(env); }
            else if ("sigsetmask".equals(func)) {
                APIStub.sigsetmask(env); }
            else if ("sigsuspend".equals(func)) {
                APIStub.sigsuspend(env); }
            else if ("sin".equals(func)) {
                APIStub.sin(env); }
            else if ("sincos".equals(func)) {
                APIStub.sincos(env); }
            else if ("sincosf".equals(func)) {
                APIStub.sincosf(env); }
            else if ("sincosl".equals(func)) {
                APIStub.sincosl(env); }
            else if ("sinf".equals(func)) {
                APIStub.sinf(env); }
            else if ("sinh".equals(func)) {
                APIStub.sinh(env); }
            else if ("sinhf".equals(func)) {
                APIStub.sinhf(env); }
            else if ("sinhl".equals(func)) {
                APIStub.sinhl(env); }
            else if ("sinl".equals(func)) {
                APIStub.sinl(env); }
            else if ("socket".equals(func)) {
                APIStub.socket(env); }
            else if ("socketpair".equals(func)) {
                APIStub.socketpair(env); }
            else if ("sqrt".equals(func)) {
                APIStub.sqrt(env); }
            else if ("sqrtf".equals(func)) {
                APIStub.sqrtf(env); }
            else if ("sqrtl".equals(func)) {
                APIStub.sqrtl(env); }
            else if ("srand".equals(func)) {
                APIStub.srand(env); }
            else if ("srand48".equals(func)) {
                APIStub.srand48(env); }
            else if ("srandom".equals(func)) {
                APIStub.srandom(env); }
            else if ("srandom_r".equals(func)) {
                APIStub.srandom_r(env); }
            else if ("srandom_r".equals(func)) {
                APIStub.srandom_r(env); }
            else if ("sprintf".equals(func)) {
                APIStub.sprintf(env); }
            else if ("ssignal".equals(func)) {
                APIStub.ssignal(env); }
            else if ("stime".equals(func)) {
                APIStub.stime(env); }
            else if ("stpcpy".equals(func)) {
                APIStub.stpcpy(env); }
            else if ("stpncpy".equals(func)) {
                APIStub.stpncpy(env); }
            else if ("strcasecmp".equals(func)) {
                APIStub.strcasecmp(env); }
            else if ("strcasestr".equals(func)) {
                APIStub.strcasestr(env); }
            else if ("strcat".equals(func)) {
                APIStub.strcat(env); }
            else if ("strchr".equals(func)) {
                APIStub.strchr(env); }
            else if ("strchrnul".equals(func)) {
                APIStub.strchrnul(env); }
            else if ("strcmp".equals(func)) {
                APIStub.strcmp(env); }
            else if ("strcoll".equals(func)) {
                APIStub.strcoll(env); }
            else if ("strcpy".equals(func)) {
                APIStub.strcpy(env); }
            else if ("strcspn".equals(func)) {
                APIStub.strcspn(env); }
            else if ("strdup".equals(func)) {
                APIStub.strdup(env); }
            else if ("strdupa".equals(func)) {
                APIStub.strdupa(env); }
            else if ("strerror".equals(func)) {
                APIStub.strerror(env); }
            else if ("strerror_r".equals(func)) {
                APIStub.strerror_r(env); }
            else if ("strerrordesc_np".equals(func)) {
                APIStub.strerrordesc_np(env); }
            else if ("strerrorname_np".equals(func)) {
                APIStub.strerrorname_np(env); }
            else if ("strfromd".equals(func)) {
                APIStub.strfromd(env); }
            else if ("strfromf".equals(func)) {
                APIStub.strfromf(env); }
            else if ("strfroml".equals(func)) {
                APIStub.strfroml(env); }
            else if ("strfry".equals(func)) {
                APIStub.strfry(env); }
            else if ("strftime".equals(func)) {
                APIStub.strftime(env); }
            else if ("strlen".equals(func)) {
                APIStub.strlen(env); }
            else if ("strncasecmp".equals(func)) {
                APIStub.strncasecmp(env); }
            else if ("strncat".equals(func)) {
                APIStub.strncat(env); }
            else if ("strncmp".equals(func)) {
                APIStub.strncmp(env); }
            else if ("strncpy".equals(func)) {
                APIStub.strncpy(env); }
            else if ("strndup".equals(func)) {
                APIStub.strndup(env); }
            else if ("strndupa".equals(func)) {
                APIStub.strndupa(env); }
            else if ("strnlen".equals(func)) {
                APIStub.strnlen(env); }
            else if ("strpbrk".equals(func)) {
                APIStub.strpbrk(env); }
            else if ("strptime".equals(func)) {
                APIStub.strptime(env); }
            else if ("strrchr".equals(func)) {
                APIStub.strrchr(env); }
            else if ("strsep".equals(func)) {
                APIStub.strsep(env); }
            else if ("strsignal".equals(func)) {
                APIStub.strsignal(env); }
            else if ("strspn".equals(func)) {
                APIStub.strspn(env); }
            else if ("strstr".equals(func)) {
                APIStub.strstr(env); }
            else if ("strtod".equals(func)) {
                APIStub.strtod(env); }
            else if ("strtof".equals(func)) {
                APIStub.strtof(env); }
            else if ("strtoimax".equals(func)) {
                APIStub.strtoimax(env); }
            else if ("strtok".equals(func)) {
                APIStub.strtok(env); }
            else if ("strtok_r".equals(func)) {
                APIStub.strtok_r(env); }
            else if ("strtol".equals(func)) {
                APIStub.strtol(env); }
            else if ("strtold".equals(func)) {
                APIStub.strtold(env); }
            else if ("strtoll".equals(func)) {
                APIStub.strtoll(env); }
            else if ("strtoq".equals(func)) {
                APIStub.strtoq(env); }
            else if ("strtoul".equals(func)) {
                APIStub.strtoul(env); }
            else if ("strtoull".equals(func)) {
                APIStub.strtoull(env); }
            else if ("strtoumax".equals(func)) {
                APIStub.strtoumax(env); }
            else if ("strtouq".equals(func)) {
                APIStub.strtouq(env); }
            else if ("strverscmp".equals(func)) {
                APIStub.strverscmp(env); }
            else if ("strxfrm".equals(func)) {
                APIStub.strxfrm(env); }
            else if ("success".equals(func)) {
                APIStub.success(env); }
            else if ("swapcontext".equals(func)) {
                APIStub.swapcontext(env); }
            else if ("swprintf".equals(func)) {
                APIStub.swprintf(env); }
            else if ("swscanf".equals(func)) {
                APIStub.swscanf(env); }
            else if ("symlink".equals(func)) {
                APIStub.symlink(env); }
            else if ("sync".equals(func)) {
                APIStub.sync(env); }
            else if ("sysconf".equals(func)) {
                APIStub.sysconf(env); }
            else if ("system".equals(func)) {
                APIStub.system(env); }
            else if ("sysv_signal".equals(func)) {
                APIStub.sysv_signal(env); }
            else if ("tan".equals(func)) {
                APIStub.tan(env); }
            else if ("tanf".equals(func)) {
                APIStub.tanf(env); }
            else if ("tanh".equals(func)) {
                APIStub.tanh(env); }
            else if ("tanhf".equals(func)) {
                APIStub.tanhf(env); }
            else if ("tanhl".equals(func)) {
                APIStub.tanhl(env); }
            else if ("tanl".equals(func)) {
                APIStub.tanl(env); }
            else if ("tcdrain".equals(func)) {
                APIStub.tcdrain(env); }
            else if ("tcflow".equals(func)) {
                APIStub.tcflow(env); }
            else if ("tcflush".equals(func)) {
                APIStub.tcflush(env); }
            else if ("tcgetattr".equals(func)) {
                APIStub.tcgetattr(env); }
            else if ("tcgetpgrp".equals(func)) {
                APIStub.tcgetpgrp(env); }
            else if ("tcgetsid".equals(func)) {
                APIStub.tcgetsid(env); }
            else if ("tcsendbreak".equals(func)) {
                APIStub.tcsendbreak(env); }
            else if ("tcsetattr".equals(func)) {
                APIStub.tcsetattr(env); }
            else if ("tcsetpgrp".equals(func)) {
                APIStub.tcsetpgrp(env); }
            else if ("telldir".equals(func)) {
                APIStub.telldir(env); }
            else if ("tempnam".equals(func)) {
                APIStub.tempnam(env); }
            else if ("textdomain".equals(func)) {
                APIStub.textdomain(env); }
            else if ("tgamma".equals(func)) {
                APIStub.tgamma(env); }
            else if ("tgammaf".equals(func)) {
                APIStub.tgammaf(env); }
            else if ("tgammal".equals(func)) {
                APIStub.tgammal(env); }
            else if ("tgkill".equals(func)) {
                APIStub.tgkill(env); }
            else if ("thrd_exit".equals(func)) {
                APIStub.thrd_exit(env); }
            else if ("timegm".equals(func)) {
                APIStub.timegm(env); }
            else if ("timelocal".equals(func)) {
                APIStub.timelocal(env); }
            else if ("times".equals(func)) {
                APIStub.times(env); }
            else if ("tmpfile".equals(func)) {
                APIStub.tmpfile(env); }
            else if ("tmpfile64".equals(func)) {
                APIStub.tmpfile64(env); }
            else if ("tmpnam".equals(func)) {
                APIStub.tmpnam(env); }
            else if ("tmpnam_r".equals(func)) {
                APIStub.tmpnam_r(env); }
            else if ("toascii".equals(func)) {
                APIStub.toascii(env); }
            else if ("tolower".equals(func)) {
                APIStub.tolower(env); }
            else if ("toupper".equals(func)) {
                APIStub.toupper(env); }
            else if ("towctrans".equals(func)) {
                APIStub.towctrans(env); }
            else if ("towlower".equals(func)) {
                APIStub.towlower(env); }
            else if ("towupper".equals(func)) {
                APIStub.towupper(env); }
            else if ("trunc".equals(func)) {
                APIStub.trunc(env); }
            else if ("truncate".equals(func)) {
                APIStub.truncate(env); }
            else if ("truncate64".equals(func)) {
                APIStub.truncate64(env); }
            else if ("truncf".equals(func)) {
                APIStub.truncf(env); }
            else if ("truncl".equals(func)) {
                APIStub.truncl(env); }
            else if ("tryagain".equals(func)) {
                APIStub.tryagain(env); }
            else if ("ttyname".equals(func)) {
                APIStub.ttyname(env); }
            else if ("ttyname_r".equals(func)) {
                APIStub.ttyname_r(env); }
            else if ("tzset".equals(func)) {
                APIStub.tzset(env); }
            else if ("ulimit".equals(func)) {
                APIStub.ulimit(env); }
            else if ("umask".equals(func)) {
                APIStub.umask(env); }
            else if ("umount".equals(func)) {
                APIStub.umount(env); }
            else if ("umount2".equals(func)) {
                APIStub.umount2(env); }
            else if ("ungetc".equals(func)) {
                APIStub.ungetc(env); }
            else if ("ungetwc".equals(func)) {
                APIStub.ungetwc(env); }
            else if ("unlink".equals(func)) {
                APIStub.unlink(env); }
            else if ("unlockpt".equals(func)) {
                APIStub.unlockpt(env); }
            else if ("unsetenv".equals(func)) {
                APIStub.unsetenv(env); }
            else if ("updwtmp".equals(func)) {
                APIStub.updwtmp(env); }
            else if ("utime".equals(func)) {
                APIStub.utime(env); }
            else if ("utimes".equals(func)) {
                APIStub.utimes(env); }
            else if ("utmpname".equals(func)) {
                APIStub.utmpname(env); }
            else if ("va_copy".equals(func)) {
                APIStub.va_copy(env); }
            else if ("va_end".equals(func)) {
                APIStub.va_end(env); }
            else if ("valloc".equals(func)) {
                APIStub.valloc(env); }
            else if ("vasprintf".equals(func)) {
                APIStub.vasprintf(env); }
            else if ("verr".equals(func)) {
                APIStub.verr(env); }
            else if ("verrx".equals(func)) {
                APIStub.verrx(env); }
            else if ("vfork".equals(func)) {
                APIStub.vfork(env); }
            else if ("vfprintf".equals(func)) {
                APIStub.vfprintf(env); }
            else if ("vfscanf".equals(func)) {
                APIStub.vfscanf(env); }
            else if ("vfwprintf".equals(func)) {
                APIStub.vfwprintf(env); }
            else if ("vprintf".equals(func)) {
                APIStub.vprintf(env); }
            else if ("vscanf".equals(func)) {
                APIStub.vscanf(env); }
            else if ("vsnprintf".equals(func)) {
                APIStub.vsnprintf(env); }
            else if ("vsprintf".equals(func)) {
                APIStub.vsprintf(env); }
            else if ("vsscanf".equals(func)) {
                APIStub.vsscanf(env); }
            else if ("vswprintf".equals(func)) {
                APIStub.vswprintf(env); }
            else if ("vswscanf".equals(func)) {
                APIStub.vswscanf(env); }
            else if ("vsyslog".equals(func)) {
                APIStub.vsyslog(env); }
            else if ("vwarn".equals(func)) {
                APIStub.vwarn(env); }
            else if ("vwarnx".equals(func)) {
                APIStub.vwarnx(env); }
            else if ("vwprintf".equals(func)) {
                APIStub.vwprintf(env); }
            else if ("wait".equals(func)) {
                APIStub.wait(env); }
            else if ("wait3".equals(func)) {
                APIStub.wait3(env); }
            else if ("wait4".equals(func)) {
                APIStub.wait4(env); }
            else if ("waitpid".equals(func)) {
                APIStub.waitpid(env); }
            else if ("wcpcpy".equals(func)) {
                APIStub.wcpcpy(env); }
            else if ("wcpncpy".equals(func)) {
                APIStub.wcpncpy(env); }
            else if ("wcrtomb".equals(func)) {
                APIStub.wcrtomb(env); }
            else if ("wcscasecmp".equals(func)) {
                APIStub.wcscasecmp(env); }
            else if ("wcscat".equals(func)) {
                APIStub.wcscat(env); }
            else if ("wcschr".equals(func)) {
                APIStub.wcschr(env); }
            else if ("wcschrnul".equals(func)) {
                APIStub.wcschrnul(env); }
            else if ("wcscmp".equals(func)) {
                APIStub.wcscmp(env); }
            else if ("wcscoll".equals(func)) {
                APIStub.wcscoll(env); }
            else if ("wcscpy".equals(func)) {
                APIStub.wcscpy(env); }
            else if ("wcscspn".equals(func)) {
                APIStub.wcscspn(env); }
            else if ("wcsdup".equals(func)) {
                APIStub.wcsdup(env); }
            else if ("wcsftime".equals(func)) {
                APIStub.wcsftime(env); }
            else if ("wcslen".equals(func)) {
                APIStub.wcslen(env); }
            else if ("wcsncasecmp".equals(func)) {
                APIStub.wcsncasecmp(env); }
            else if ("wcsncat".equals(func)) {
                APIStub.wcsncat(env); }
            else if ("wcsncmp".equals(func)) {
                APIStub.wcsncmp(env); }
            else if ("wcsncpy".equals(func)) {
                APIStub.wcsncpy(env); }
            else if ("wcsnlen".equals(func)) {
                APIStub.wcsnlen(env); }
            else if ("wcsnrtombs".equals(func)) {
                APIStub.wcsnrtombs(env); }
            else if ("wcspbrk".equals(func)) {
                APIStub.wcspbrk(env); }
            else if ("wcsrchr".equals(func)) {
                APIStub.wcsrchr(env); }
            else if ("wcsrtombs".equals(func)) {
                APIStub.wcsrtombs(env); }
            else if ("wcsspn".equals(func)) {
                APIStub.wcsspn(env); }
            else if ("wcsstr".equals(func)) {
                APIStub.wcsstr(env); }
            else if ("wcstod".equals(func)) {
                APIStub.wcstod(env); }
            else if ("wcstof".equals(func)) {
                APIStub.wcstof(env); }
            else if ("wcstoimax".equals(func)) {
                APIStub.wcstoimax(env); }
            else if ("wcstok".equals(func)) {
                APIStub.wcstok(env); }
            else if ("wcstol".equals(func)) {
                APIStub.wcstol(env); }
            else if ("wcstold".equals(func)) {
                APIStub.wcstold(env); }
            else if ("wcstoll".equals(func)) {
                APIStub.wcstoll(env); }
            else if ("wcstombs".equals(func)) {
                APIStub.wcstombs(env); }
            else if ("wcstoq".equals(func)) {
                APIStub.wcstoq(env); }
            else if ("wcstoul".equals(func)) {
                APIStub.wcstoul(env); }
            else if ("wcstoull".equals(func)) {
                APIStub.wcstoull(env); }
            else if ("wcstoumax".equals(func)) {
                APIStub.wcstoumax(env); }
            else if ("wcstouq".equals(func)) {
                APIStub.wcstouq(env); }
            else if ("wcswcs".equals(func)) {
                APIStub.wcswcs(env); }
            else if ("wcsxfrm".equals(func)) {
                APIStub.wcsxfrm(env); }
            else if ("wctob".equals(func)) {
                APIStub.wctob(env); }
            else if ("wctomb".equals(func)) {
                APIStub.wctomb(env); }
            else if ("wctrans".equals(func)) {
                APIStub.wctrans(env); }
            else if ("wctype".equals(func)) {
                APIStub.wctype(env); }
            else if ("wmemchr".equals(func)) {
                APIStub.wmemchr(env); }
            else if ("wmemcmp".equals(func)) {
                APIStub.wmemcmp(env); }
            else if ("wmemcpy".equals(func)) {
                APIStub.wmemcpy(env); }
            else if ("wmemmove".equals(func)) {
                APIStub.wmemmove(env); }
            else if ("wmempcpy".equals(func)) {
                APIStub.wmempcpy(env); }
            else if ("wmemset".equals(func)) {
                APIStub.wmemset(env); }
            else if ("wordexp".equals(func)) {
                APIStub.wordexp(env); }
            else if ("wordfree".equals(func)) {
                APIStub.wordfree(env); }
            else if ("write".equals(func)) {
                APIStub.write(env); }
            else if ("writev".equals(func)) {
                APIStub.writev(env); }
            else if ("y0".equals(func)) {
                APIStub.y0(env); }
            else if ("y0f".equals(func)) {
                APIStub.y0f(env); }
            else if ("y0l".equals(func)) {
                APIStub.y0l(env); }
            else if ("y1".equals(func)) {
                APIStub.y1(env); }
            else if ("y1f".equals(func)) {
                APIStub.y1f(env); }
            else if ("y1l".equals(func)) {
                APIStub.y1l(env); }
            else if ("yn".equals(func)) {
                APIStub.yn(env); }
            else if ("ynf".equals(func)) {
                APIStub.ynf(env); }
            else if ("ynl".equals(func)) {
                APIStub.ynl(env); }
//        } catch (Throwable e){
//            Logs.infoLn("\t -> Error in native thread. " + e);
//        }

    }

}