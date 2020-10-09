package emulator.base;

import emulator.semantics.Memory;
import executor.Configs;
import emulator.semantics.EnvData;
import emulator.semantics.EnvModel;
import emulator.semantics.Environment;
import enums.*;
import javafx.util.Pair;
import pojos.BitBool;
import pojos.BitVec;
import utils.*;

import java.util.BitSet;
import java.util.Objects;

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
    public Pair<EnvModel, EnvModel> b(String preCond, Character cond, Object label) {
        if (label instanceof String) {
            String finalLabel = (String) label;
            Logs.info("\t-> Direct Jump to", finalLabel, "if", Mapping.condCharToStr.get(cond), "\n");
            EnvModel modelTrue = checkPosCond(preCond, cond);
            EnvModel modelFalse = checkNegCond(preCond, cond);
            return new Pair<>(modelTrue, modelFalse);
        } else if (label instanceof Character) {
            Character finalLabel = (Character) label;
            Logs.info("\t-> Indirect Jump to", Mapping.regCharToStr.get(finalLabel), "if", Mapping.condCharToStr.get(cond), "\n");
            EnvModel modelTrue = checkPosCond(preCond, cond, val(finalLabel).getSym());
            EnvModel modelFalse = checkNegCond(preCond, cond, val(finalLabel).getSym());
            return new Pair<>(modelTrue, modelFalse);
        }
        Logs.infoLn("+ Wrong type of label!");
        return null;
    }

    public Pair<EnvModel, EnvModel> bl(int nextLabel, String preCond, Character cond, Object label) {
        write('l', new BitVec(nextLabel));
        return b(preCond, cond, label);
    }

    public Pair<EnvModel, EnvModel> bx(String preCond, Character cond, Object label) {
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
                return env.flags.C.getSym();
            case CC:
            case LO:
                return String.format("(not %s)", env.flags.C.getSym());
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
        BitVec popValue = env.stacks.pop();
        String sym = String.format("(bvadd %s #x00000004)", env.register.getFormula('s'));
        BitSet concreteValue = Arithmetic.intToBitSet(Arithmetic.bitSetToInt(env.register.get('s').getVal()) + 4);
        write('s', new BitVec(sym, concreteValue));
        if (popValue != null) {
            write(r, popValue);
        } else {
            write(r, new BitVec(SysUtils.addSymVar(), 0));
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
        return new BitVec(symValue, res.getVal());
    }

    protected void load(Character d, Character label) {
        BitVec res = val(label);
        write(d, Memory.get(val(label)));
      //  System.out.println("");
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

}