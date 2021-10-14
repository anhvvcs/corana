package emulator.cortex;

import executor.Configs;
import enums.*;
import pojos.BitVec;
import utils.Arithmetic;
import emulator.base.*;
import emulator.semantics.*;
import utils.Logs;

public class M33 extends Emulator {

    public M33(Environment env) {
        super(env);
    }

    public void adc(Character xd, Character xn, Character op, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N', 'Z', 'C', 'V'};
        if (xd == null) xd = xn;
        BitVec result = add(val(xn), val(op), true);
        write(xd, result);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void add(Character xd, Character xn, Character op, Integer im, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N', 'Z', 'C', 'V'};
        if (xd == null) xd = xn;
        BitVec result = add(val(xn), val(op, im));
        write(xd, result);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void addw(Character xd, Character xn, Integer im, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N', 'Z', 'C', 'V'};
        if (xd == null) xd = xn;
        BitVec result = add(val(xn), val(im));
        write(xd, result);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void adr(Character xd, Character label) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = add(env.register.get('p'), val(label));
        write(xd, result);
    }

    public void and(Character xd, Character xn, Character op, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N', 'Z', 'C', 'V'};
        if (xd == null) xd = xn;
        BitVec result = and(val(xn), val(op));
        write(xd, result);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void asr(Character xd, Character xm, Character xs, Integer n, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N', 'Z', 'C'};
        BitVec result = shift(val(xm), Mode.RIGHT, val(xs, n));
        write(xd, result);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void bfc(Character xd, Integer lsb, Integer width) {
        arithmeticMode = ArithmeticMode.BINARY;
        clearBitfield(xd, lsb, width);

    }

    public void bfi(Character xd, Character xn, Integer lsb, Integer width) {
        arithmeticMode = ArithmeticMode.BINARY;
        copyBitfield(xd, xn, lsb, width);

    }

    public void bic(Character xd, Character xn, Character op, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N', 'Z', 'C', 'V'};
        if (xd == null) xd = xn;
        BitVec result = and(val(xn), comp(val(op)));
        write(xd, result);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void bkpt(Integer im) {
        arithmeticMode = ArithmeticMode.BINARY;
        switchMode(ExecutionMode.DEBUG);
    }

    public void cbnz(Character xn, Character label) {
        arithmeticMode = ArithmeticMode.BINARY;
        cmp(xn, 0, null);
        b("", new Throwable().getStackTrace()[0].getMethodName().equals("cbz") ? 'e' : 'n', label);
    }

    public void cbz(Character xn, Character label) {
        arithmeticMode = ArithmeticMode.BINARY;
        cmp(xn, 0, null);
        b("", new Throwable().getStackTrace()[0].getMethodName().equals("cbz") ? 'e' : 'n', label);
    }

    public void cmp(Character xn, Character op, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N', 'Z', 'C', 'V'};
        BitVec result = cmp(val(xn), val(op));
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void cpsid(Character mode) {
        arithmeticMode = ArithmeticMode.BINARY;
        change(mode == 'i' ? ChangeMode.PRIMASK : ChangeMode.FAULTMASK);
    }

    public void cpsie(Character mode) {
        arithmeticMode = ArithmeticMode.BINARY;
        change(mode == 'i' ? ChangeMode.PRIMASK : ChangeMode.FAULTMASK);
    }

    public void eor(Character xd, Character xn, Character op, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N', 'Z', 'C', 'V'};
        if (xd == null) xd = xn;
        BitVec result = xor(val(xn), val(op));
        write(xd, result);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void lda(Character xt, Character xn) {
        arithmeticMode = ArithmeticMode.BINARY;
        load(xt, xn);
    }

    public void ldab(Character xt, Character xn) {
        arithmeticMode = ArithmeticMode.BINARY;
        write(xt, load(val(xn)));
    }

    public void ldah(Character xt, Character xn) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = load(val(xn));
        write(xt, result);
    }

    public void ldr(Character xt, Character xn, Integer offset, Integer offsetType) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = add(val(xn), val(offset));
        assert offsetType >= 1 && offsetType <= 3;
        write(xt, offsetType == 3 ? val(xn) : load(result));
        write(xn, offsetType == 1 ? val(xn) : result);
    }

    public void ldr(Character xt, Character label) {
        arithmeticMode = ArithmeticMode.BINARY;
        load(xt, label);
    }

    public void ldrb(Character xt, Character xn, Integer offset, Integer offsetType) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = add(val(xn), val(offset));
        assert offsetType >= 1 && offsetType <= 3;
        write(xt, offsetType == 3 ? val(xn) : load(result));
        write(xn, offsetType == 1 ? val(xn) : result);
        result = zeroExt(result, 32);
    }

    public void ldrb(Character xt, Character label) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = load(val(label));
        result = zeroExt(result, 32);
    }

    public void ldrd(Character xt, Character xt2, Character xn, Integer offset, Integer offsetType) {
        arithmeticMode = ArithmeticMode.BINARY;
        write(xt, load(add(val(xn), val(offset))));
        write(xt2, load(add(val(xn), val(offset))));
    }

    public void ldrd(Character xt, Character xt2, Character label) {
        arithmeticMode = ArithmeticMode.BINARY;
        write(xt, load(val(label)));
        write(xt2, load(val(label)));
    }

    public void ldrex(Character xt, Character xn, Integer offset, Integer offsetType) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = add(val(xn), val(offset));
        assert offsetType >= 1 && offsetType <= 3;
        write(xt, offsetType == 3 ? val(xn) : load(result));
        write(xn, offsetType == 1 ? val(xn) : result);
    }

    public void ldrexb(Character xt, Character xn) {
        arithmeticMode = ArithmeticMode.BINARY;
        write(xt, load(val(xn)));
    }

    public void ldrexh(Character xt, Character xn) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = load(val(xn));
        write(xt, result);
    }

    public void ldrh(Character xt, Character xn, Integer offset, Integer offsetType) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = add(val(xn), val(offset));
        assert offsetType >= 1 && offsetType <= 3;
        write(xt, offsetType == 3 ? val(xn) : load(result));
        write(xn, offsetType == 1 ? val(xn) : result);
        result = zeroExt(result, 32);
    }

    public void ldrh(Character xt, Character label) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = load(val(label));
        result = zeroExt(result, 32);
    }

    public void ldrsb(Character xt, Character xn, Integer offset, Integer offsetType) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = add(val(xn), val(offset));
        assert offsetType >= 1 && offsetType <= 3;
        write(xt, offsetType == 3 ? val(xn) : load(result));
        write(xn, offsetType == 1 ? val(xn) : result);
        result = signedExt(result, 32);
    }

    public void ldrsb(Character xt, Character label) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = load(val(label));
        result = signedExt(result, 32);
    }

    public void ldrsh(Character xt, Character xn, Integer offset, Integer offsetType) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = add(val(xn), val(offset));
        assert offsetType >= 1 && offsetType <= 3;
        write(xt, offsetType == 3 ? val(xn) : load(result));
        write(xn, offsetType == 1 ? val(xn) : result);
        result = signedExt(result, 32);
    }

    public void ldrsh(Character xt, Character label) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = load(val(label));
        result = signedExt(result, 32);
    }

    public void lsl(Character xd, Character xm, Character xs, Integer n, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N', 'Z', 'C'};
        BitVec result = shift(val(xm), Mode.LEFT, val(xs, n));
        write(xd, result);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void lsr(Character xd, Character xm, Character xs, Integer n, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N', 'Z', 'C'};
        BitVec result = shift(val(xm), Mode.RIGHT, val(xs, n));
        write(xd, result);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void mla(Character xd, Character xn, Character xm, Character xa, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N', 'Z', 'C', 'V'};
        BitVec result = mul(val(xn), val(xm));
        result = add(result, val(xa));
        write(xd, shift(shift(result, Mode.LEFT, 32), Mode.RIGHT, 32));
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void mls(Character xd, Character xn, Character xm, Character xa) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = mul(val(xn), val(xm));
        result = sub(val(xa), result);
        write(xd, shift(shift(result, Mode.LEFT, 32), Mode.RIGHT, 32));
    }

    public void mov(Character xd, Character op, Integer im, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N', 'Z', 'C', 'V'};
        BitVec result = val(op, im);
        write(xd, result);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void movw(Character xd, Integer im, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N', 'Z', 'C', 'V'};
        BitVec result = val(im);
        write(xd, result);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void mul(Character xd, Character xn, Character xm, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N', 'Z', 'C', 'V'};
        if (xd == null) xd = xn;
        BitVec result = mul(val(xn), val(xm));
        write(xd, shift(shift(result, Mode.LEFT, 32), Mode.RIGHT, 32));
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void mvn(Character xd, Character op, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N', 'Z', 'C', 'V'};
        BitVec result = val(op);
        result = not(result);
        write(xd, result);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void nop(Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;

    }

    public void orn(Character xd, Character xn, Character op, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N', 'Z', 'C', 'V'};
        if (xd == null) xd = xn;
        BitVec result = or(val(xn), comp(val(op)));
        write(xd, result);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void orr(Character xd, Character xn, Character op, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N', 'Z', 'C', 'V'};
        if (xd == null) xd = xn;
        BitVec result = or(val(xn), val(op));
        write(xd, result);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void pkhbt(Character xd, Character xn, Character xm, int s, boolean b) {
        Mode shiftMode = Mode.LSL;
        arithmeticMode = ArithmeticMode.BINARY;
        if (xd == null) xd = xn;
        write(xd, val(xn), Type.BOTTOM_HALFWORD);
        write(xd, shift(val(xm), shiftMode, s), Type.TOP_HALFWORD);
    }

    public void pkhtb(Character xd, Character xn, Character xm, int s) {
        Mode shiftMode = Mode.ASR;
        arithmeticMode = ArithmeticMode.BINARY;
        if (xd == null) xd = xn;
        write(xd, val(xn), Type.TOP_HALFWORD);
        write(xd, shift(val(xm), shiftMode, s), Type.BOTTOM_HALFWORD);
    }

    public void qadd(Character xd, Character xn, Character xm, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'Q'};
        if (xd == null) xd = xn;
        BitVec[] resultArr = add(val(xn), val(xm), Type.WORD);
        BitVec result = concat(resultArr);
        result = sat(resultArr, -Math.pow(2, 32 - 1), Math.pow(2, 32 - 1) - 1);
        write(xd, result);
        write(xd, result);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void qadd16(Character xd, Character xn, Character xm, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'Q'};
        if (xd == null) xd = xn;
        BitVec[] resultArr = add(val(xn), val(xm), Type.HALFWORD);
        BitVec result = concat(resultArr);
        result = sat(resultArr, -Math.pow(2, 16 - 1), Math.pow(2, 16 - 1) - 1);
        write(xd, result);
        write(xd, result);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void qadd8(Character xd, Character xn, Character xm, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'Q'};
        if (xd == null) xd = xn;
        BitVec[] resultArr = add(val(xn), val(xm), Type.BYTE);
        BitVec result = concat(resultArr);
        result = sat(resultArr, -Math.pow(2, 8 - 1), Math.pow(2, 8 - 1) - 1);
        write(xd, result);
        write(xd, result);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void qdadd(Character xd, Character xm, Character xn, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'Q'};
        if (xd == null) xd = xm;
        BitVec result = mul(val(xm), val(2));
        write(xn, add(result, sat(toArray(val(xn)), -Math.pow(2, 31), Math.pow(2, 31) - 1)));
        write(xd, result);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void qdsub(Character xd, Character xm, Character xn, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'Q'};
        if (xd == null) xd = xm;
        BitVec result = mul(val(xm), val(2));
        write(xn, sub(result, sat(toArray(val(xn)), -Math.pow(2, 31), Math.pow(2, 31) - 1)));
        write(xd, result);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void qsub(Character xd, Character xn, Character xm, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'Q'};
        if (xd == null) xd = xn;
        BitVec[] resultArr = sub(val(xn), val(xm), Type.WORD);
        BitVec result = concat(resultArr);
        result = sat(resultArr, -Math.pow(2, 32 - 1), Math.pow(2, 32 - 1) - 1);
        write(xd, result);
        write(xd, result);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void qsub16(Character xd, Character xn, Character xm, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'Q'};
        if (xd == null) xd = xn;
        BitVec[] resultArr = sub(val(xn), val(xm), Type.HALFWORD);
        BitVec result = concat(resultArr);
        result = sat(resultArr, -Math.pow(2, 16 - 1), Math.pow(2, 16 - 1) - 1);
        write(xd, result);
        write(xd, result);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void qsub8(Character xd, Character xn, Character xm, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'Q'};
        if (xd == null) xd = xn;
        BitVec[] resultArr = sub(val(xn), val(xm), Type.BYTE);
        BitVec result = concat(resultArr);
        result = sat(resultArr, -Math.pow(2, 8 - 1), Math.pow(2, 8 - 1) - 1);
        write(xd, result);
        write(xd, result);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void rbit(Character xd, Character xn) {
        arithmeticMode = ArithmeticMode.BINARY;
        write(xd, rev(val(xn), Type.WORD));
    }

    public void rev(Character xd, Character xn) {
        arithmeticMode = ArithmeticMode.BINARY;
        write(xd, rev(val(xn), Type.WORD));
    }

    public void rev16(Character xd, Character xn) {
        arithmeticMode = ArithmeticMode.BINARY;
        write(xd, rev(val(xn), Type.HALFWORD));
    }

    public void revsh(Character xd, Character xn) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = rev(val(xn), Type.HALFWORD);
        result = signedExt(result, 16);
        write(xd, result);
    }

    public void ror(Character xd, Character xm, Character xs, Integer n, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N', 'Z', 'C'};
        BitVec result = shift(val(xm), Mode.RIGHT, val(xs, n));
        write(xd, result);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void rrx(Character xd, Character xm, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N', 'Z', 'C'};
        BitVec result = shift(val(xm), Mode.RIGHT, val(1));
        write(xd, result);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void rsb(Character xd, Character xn, Character op, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N', 'Z', 'C', 'V'};
        if (xd == null) xd = xn;
        BitVec result = sub(val(op), val(xn));
        write(xd, result);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void sadd16(Character xd, Character xn, Character xm, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'G'};
        if (xd == null) xd = xn;
        BitVec[] resultArr = add(val(xn), val(xm), Type.HALFWORD);
        BitVec result = concat(resultArr);
        write(xd, result);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void sadd8(Character xd, Character xn, Character xm, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'G'};
        if (xd == null) xd = xn;
        BitVec[] resultArr = add(val(xn), val(xm), Type.BYTE);
        BitVec result = concat(resultArr);
        write(xd, result);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void sasx(Character xd, Character xn, Character xm, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'G'};
        if (xd == null) xd = xn;
        BitVec result = add(shift(val(xn), Mode.RIGHT, 16), shift(shift(val(xm), Mode.LEFT, 16), Mode.RIGHT, 16));
        write(xd, result, Type.TOP_HALFWORD);
        result = sub(shift(val(xn), Mode.RIGHT, 16), shift(shift(val(xm), Mode.LEFT, 16), Mode.RIGHT, 16));
        write(xd, result, Type.BOTTOM_HALFWORD);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void sbc(Character xd, Character xn, Character op, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N', 'Z', 'C', 'V'};
        if (xd == null) xd = xn;
        BitVec result = sub(val(op), val(xn));
        write(xd, result);
        result = sub(result, val(1), true);
        write(xd, result);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void sbfx(Character xd, Character xn, Integer lsb, Integer width) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = shift(shift(val(xn), Mode.LEFT, sub(add(val(lsb), val(width)), val(1))), Mode.RIGHT,
                add(sub(new BitVec(Configs.architecture), new BitVec(lsb)), sub(add(val(lsb), val(width)), val(1))));
        result = signedExt(result, 32);
        write(xd, result);
    }

    public void shadd16(Character xd, Character xn, Character xm) {
        arithmeticMode = ArithmeticMode.BINARY;
        if (xd == null) xd = xn;
        BitVec[] resultArr = add(val(xn), val(xm), Type.HALFWORD);
        BitVec result = concat(resultArr);
        result = shift(resultArr, Mode.RIGHT, 1);
        write(xd, result);
    }

    public void shadd8(Character xd, Character xn, Character xm) {
        arithmeticMode = ArithmeticMode.BINARY;
        if (xd == null) xd = xn;
        BitVec[] resultArr = add(val(xn), val(xm), Type.BYTE);
        BitVec result = concat(resultArr);
        result = shift(resultArr, Mode.RIGHT, 1);
        write(xd, result);
    }

    public void shsub16(Character xd, Character xn, Character xm) {
        arithmeticMode = ArithmeticMode.BINARY;
        if (xd == null) xd = xn;
        BitVec[] resultArr = sub(val(xn), val(xm), Type.HALFWORD);
        BitVec result = concat(resultArr);
        result = shift(resultArr, Mode.RIGHT, 1);
        write(xd, result);
    }

    public void shsub8(Character xd, Character xn, Character xm) {
        arithmeticMode = ArithmeticMode.BINARY;
        if (xd == null) xd = xn;
        BitVec[] resultArr = sub(val(xn), val(xm), Type.BYTE);
        BitVec result = concat(resultArr);
        result = shift(resultArr, Mode.RIGHT, 1);
        write(xd, result);
    }

    public void smlad(Character xd, Character xn, Character xm, Character xa, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'Q'};
        BitVec result_1 = mul(shift(val(xn), Mode.RIGHT, 32), shift(val(xm), Mode.RIGHT, 32));
        BitVec result_2 = mul(shift(val(xn), Mode.LEFT, 32), shift(val(xm), Mode.LEFT, 32));
        BitVec result = add(add(result_1, result_2), val(xa));
        write(xd, result);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void smladx(Character xd, Character xn, Character xm, Character xa, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'Q'};
        BitVec result_1 = mul(shift(val(xn), Mode.RIGHT, 32), shift(val(xm), Mode.LEFT, 32));
        BitVec result_2 = mul(shift(val(xn), Mode.LEFT, 32), shift(val(xm), Mode.RIGHT, 32));
        BitVec result = add(add(result_1, result_2), val(xa));
        write(xd, result);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void smlal(Character xlo, Character xhi, Character xn, Character xm) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = mul(comp(val(xn)), comp(val(xm)));
        result = add(result, xlo, xhi);
        write(xlo, shift(shift(result, Mode.LEFT, 32), Mode.RIGHT, 32));
        write(xhi, shift(result, Mode.RIGHT, 32));
    }

    public void smlalbb(Character xlo, Character xhi, Character xn, Character xm) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = mul(shift(shift(val(xn), Mode.LEFT, 16), Mode.RIGHT, 16), shift(shift(val(xm), Mode.LEFT, 16), Mode.RIGHT, 16));
        result = add(signedExt(result, 32), xlo, xhi);
        write(xlo, shift(shift(result, Mode.LEFT, 32), Mode.RIGHT, 32));
        write(xhi, shift(result, Mode.RIGHT, 32));
    }

    public void smlalbt(Character xlo, Character xhi, Character xn, Character xm) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = mul(shift(shift(val(xn), Mode.LEFT, 16), Mode.RIGHT, 16), shift(val(xm), Mode.RIGHT, 16));
        result = add(signedExt(result, 32), xlo, xhi);
        write(xlo, shift(shift(result, Mode.LEFT, 32), Mode.RIGHT, 32));
        write(xhi, shift(result, Mode.RIGHT, 32));
    }

    public void smlald(Character xlo, Character xhi, Character xn, Character xm) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result_1 = mul(shift(val(xn), Mode.RIGHT, 32), shift(val(xm), Mode.RIGHT, 32));
        BitVec result_2 = mul(shift(val(xn), Mode.LEFT, 32), shift(val(xm), Mode.LEFT, 32));
        BitVec result = mul(result_1, result_2);
        write(xlo, shift(shift(result, Mode.LEFT, 32), Mode.RIGHT, 32));
        write(xhi, shift(result, Mode.RIGHT, 32));
    }

    public void smlaldx(Character xlo, Character xhi, Character xn, Character xm) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result_1 = mul(shift(val(xn), Mode.RIGHT, 32), shift(val(xm), Mode.LEFT, 32));
        BitVec result_2 = mul(shift(val(xn), Mode.LEFT, 32), shift(val(xm), Mode.RIGHT, 32));
        BitVec result = mul(result_1, result_2);
        write(xlo, shift(shift(result, Mode.LEFT, 32), Mode.RIGHT, 32));
        write(xhi, shift(result, Mode.RIGHT, 32));
    }

    public void smlaltb(Character xlo, Character xhi, Character xn, Character xm) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = mul(shift(val(xn), Mode.RIGHT, 16), shift(shift(val(xm), Mode.LEFT, 16), Mode.RIGHT, 16));
        result = add(signedExt(result, 32), xlo, xhi);
        write(xlo, shift(shift(result, Mode.LEFT, 32), Mode.RIGHT, 32));
        write(xhi, shift(result, Mode.RIGHT, 32));
    }

    public void smlaltt(Character xlo, Character xhi, Character xn, Character xm) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = mul(shift(val(xn), Mode.RIGHT, 16), shift(val(xm), Mode.RIGHT, 16));
        result = add(signedExt(result, 32), xlo, xhi);
        write(xlo, shift(shift(result, Mode.LEFT, 32), Mode.RIGHT, 32));
        write(xhi, shift(result, Mode.RIGHT, 32));
    }

    public void smlawb(Character xd, Character xn, Character xm, Character xa, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'Q'};
        BitVec result = mul(val(xn), val(xm), Type.BOTTOM_HALFWORD);
        result = add(val(xa), shift(result, Mode.RIGHT, 32));
        write(xd, result);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void smlawt(Character xd, Character xn, Character xm, Character xa, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'Q'};
        BitVec result = mul(val(xn), val(xm), Type.TOP_HALFWORD);
        result = add(val(xa), shift(result, Mode.RIGHT, 32));
        write(xd, result);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void smmla(Character xd, Character xn, Character xm, Character xa, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = mul(val(xn), val(xm));
        result = suffix == 'r' ? add(result, Arithmetic.fromHexStr("80000000")) : result;
        result = shift(result, Mode.RIGHT, 32);
        result = add(val(xa), result);
        write(xd, result);
    }

    public void smmls(Character xd, Character xn, Character xm, Character xa, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = mul(val(xn), val(xm));
        result = suffix == 'r' ? add(result, Arithmetic.fromHexStr("80000000")) : result;
        result = shift(result, Mode.RIGHT, 32);
        result = sub(val(xa), result);
        write(xd, result);
    }

    public void smmul(Character xd, Character xn, Character xm, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = mul(val(xn), val(xm));
        result = suffix == 'r' ? round(result, RoundType.TOWARDS_ZERO) : shift(result, Mode.RIGHT, 32);
        write(xd, shift(result, Mode.RIGHT, 32));
    }

    public void smulbb(Character xd, Character xn, Character xm) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = mul(shift(shift(val(xn), Mode.LEFT, 16), Mode.RIGHT, 16), shift(shift(val(xm), Mode.LEFT, 16), Mode.RIGHT, 16));
        write(xd, result);
    }

    public void smulbt(Character xd, Character xn, Character xm) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = mul(shift(shift(val(xn), Mode.LEFT, 16), Mode.RIGHT, 16), shift(val(xm), Mode.RIGHT, 16));
        write(xd, result);
    }

    public void smull(Character xlo, Character xhi, Character xn, Character xm) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = mul(val(xn), val(xm));
        write(xlo, shift(shift(result, Mode.LEFT, 32), Mode.RIGHT, 32));
        write(xhi, shift(result, Mode.RIGHT, 32));
    }

    public void smultb(Character xd, Character xn, Character xm) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = mul(shift(val(xn), Mode.RIGHT, 16), shift(shift(val(xm), Mode.LEFT, 16), Mode.RIGHT, 16));
        write(xd, result);
    }

    public void smultt(Character xd, Character xn, Character xm) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = mul(shift(val(xn), Mode.RIGHT, 16), shift(val(xm), Mode.RIGHT, 16));
        write(xd, result);
    }

    public void smulwb(Character xd, Character xn, Character xm) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = mul(val(xn), shift(shift(val(xm), Mode.LEFT, 16), Mode.RIGHT, 16));
        write(xd, shift(result, Mode.RIGHT, 32));
    }

    public void smulwt(Character xd, Character xn, Character xm) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = mul(val(xn), shift(val(xm), Mode.RIGHT, 16));
        write(xd, shift(result, Mode.RIGHT, 32));
    }

    public void ssat(Character xd, Integer n, Character xm, int shiftMode, int s, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'Q'};
        BitVec[] resultArr = toArray(shift(val(xm), shiftMode, s));
        BitVec result = sat(resultArr, -Math.pow(2, n - 1), Math.pow(2, n - 1) - 1);
        write(xd, result);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void ssat16(Character xd, Integer n, Character xm, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'Q'};
        BitVec result = sat(valArr(xm, Type.HALFWORD), -Math.pow(2, n) + 1, Math.pow(2, n) - 1);
        write(xd, result);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void ssax(Character xd, Character xn, Character xm, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'G'};
        if (xd == null) xd = xn;
        BitVec result = sub(shift(val(xn), Mode.RIGHT, 16), shift(shift(val(xm), Mode.LEFT, 16), Mode.RIGHT, 16));
        write(xd, result, Type.BOTTOM_HALFWORD);
        result = add(shift(val(xn), Mode.RIGHT, 16), shift(shift(val(xm), Mode.LEFT, 16), Mode.RIGHT, 16));
        write(xd, result, Type.TOP_HALFWORD);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void ssub16(Character xd, Character xn, Character xm, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'G'};
        if (xd == null) xd = xn;
        BitVec[] resultArr = sub(val(xn), val(xm), Type.HALFWORD);
        BitVec result = concat(resultArr);
        write(xd, result);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void ssub8(Character xd, Character xn, Character xm, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'G'};
        if (xd == null) xd = xn;
        BitVec[] resultArr = sub(val(xn), val(xm), Type.BYTE);
        BitVec result = concat(resultArr);
        write(xd, result);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void stl(Character xt, Character xn, Integer offset, Integer offsetType) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = add(val(xn), val(offset));
        assert offsetType >= 1 && offsetType <= 3;
        store(val(xt), offsetType == 3 ? val(xn) : result);
        write(xn, offsetType == 1 ? val(xn) : result);
    }

    public void stlb(Character xt, Character xn, Integer offset, Integer offsetType) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = add(val(xn), val(offset));
        assert offsetType >= 1 && offsetType <= 3;
        store(val(xt), offsetType == 3 ? val(xn) : result);
        write(xn, offsetType == 1 ? val(xn) : result);
    }

    public void stlh(Character xt, Character xn, Integer offset, Integer offsetType) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = add(val(xn), val(offset));
        assert offsetType >= 1 && offsetType <= 3;
        store(val(xt), offsetType == 3 ? val(xn) : result);
        write(xn, offsetType == 1 ? val(xn) : result);
    }

    public void str(Character xt, Character xn, Integer offset, Integer offsetType) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = add(val(xn), val(offset));
        assert offsetType >= 1 && offsetType <= 3;
        store(val(xt), offsetType == 3 ? val(xn) : result);
        write(xn, offsetType == 1 ? val(xn) : result);
    }

    public void strd(Character xt, Character xt2, Character xn, Integer offset, Integer offsetType) {
        arithmeticMode = ArithmeticMode.BINARY;
        store(val(xn), val(xt), val(xt2));
    }

    public void strex(Character xd, Character xt, Character xn, Integer offset, Integer offsetType) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = add(val(xn), val(offset));
        assert offsetType >= 1 && offsetType <= 3;
        store(val(xt), offsetType == 3 ? val(xn) : result);
        write(xn, offsetType == 1 ? val(xn) : result);
    }

    public void strexb(Character xd, Character xt, Character xn, Integer offset, Integer offsetType) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = add(val(xn), val(offset));
        assert offsetType >= 1 && offsetType <= 3;
        store(val(xt), offsetType == 3 ? val(xn) : result);
        write(xn, offsetType == 1 ? val(xn) : result);
    }

    public void strexh(Character xd, Character xt, Character xn, Integer offset, Integer offsetType) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = add(val(xn), val(offset));
        assert offsetType >= 1 && offsetType <= 3;
        store(val(xt), offsetType == 3 ? val(xn) : result);
        write(xn, offsetType == 1 ? val(xn) : result);
    }

    public void strh(Character xt, Character xn, Integer offset, Integer offsetType) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = add(val(xn), val(offset));
        assert offsetType >= 1 && offsetType <= 3;
        store(val(xt), offsetType == 3 ? val(xn) : result);
        write(xn, offsetType == 1 ? val(xn) : result);
        result = zeroExt(result, 32);
    }

    public void strhd(Character xt, Character xt2, Character xn, Integer offset, Integer offsetType) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = val(xt);
        BitVec result_2 = val(xt2);
        store(val(xn), result, result_2);
        result = zeroExt(result, 32);
    }

    public void sub(Character xd, Character xn, Character op, Integer im, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N', 'Z', 'C', 'V'};
        if (xd == null) xd = xn;
        BitVec result = sub(val(xn), val(op, im));
        write(xd, result);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void subw(Character xd, Character xn, Integer im, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N', 'Z', 'C', 'V'};
        if (xd == null) xd = xn;
        BitVec result = sub(val(xn), val(im));
        write(xd, result);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void svc(Integer im) {
        arithmeticMode = ArithmeticMode.BINARY;
        throwExc("svc");

    }

    public void sxtab(Character xd, Character xn, Character xm, Integer rorn) {
        arithmeticMode = ArithmeticMode.BINARY;
        if (xd == null) xd = xn;
        BitVec result = rot(val(xm), rorn, Mode.RIGHT);
        result = shift(shift(val(xm), Mode.LEFT, 0), Mode.RIGHT, Configs.architecture - 7 + 0);
        result = signedExt(result, 32);
        result = add(result, val(xn));
        write(xd, result);
    }

    public void sxtab16(Character xd, Character xn, Character xm, Integer rorn) {
        arithmeticMode = ArithmeticMode.BINARY;
        if (xd == null) xd = xn;
        BitVec result = rot(val(xm), rorn, Mode.RIGHT);
        result = shift(shift(val(xm), Mode.LEFT, 0), Mode.RIGHT, Configs.architecture - 7 + 0);
        result = signedExt(result, 16);
        result = shift(shift(result, Mode.LEFT, 16), Mode.RIGHT, Configs.architecture - 23 + 16);
        result = signedExt(result, 16);
        result = add(result, val(xn));
        write(xd, result);
    }

    public void sxtah(Character xd, Character xn, Character xm, Integer rorn) {
        arithmeticMode = ArithmeticMode.BINARY;
        if (xd == null) xd = xn;
        BitVec result = rot(val(xm), rorn, Mode.RIGHT);
        result = shift(shift(val(xm), Mode.LEFT, 0), Mode.RIGHT, Configs.architecture - 15 + 0);
        result = signedExt(result, 32);
        result = add(result, val(xn));
        write(xd, result);
    }

    public void sxtb(Character xd, Character xm, Integer rorn) {
        arithmeticMode = ArithmeticMode.BINARY;
        if (xd == null) xd = xm;
        BitVec result = rot(val(xm), rorn, Mode.RIGHT);
        result = shift(shift(val(xm), Mode.LEFT, 0), Mode.RIGHT, Configs.architecture - 7 + 0);
        result = signedExt(result, 32);
    }

    public void sxtb16(Character xd, Character xm, Integer rorn) {
        arithmeticMode = ArithmeticMode.BINARY;
        if (xd == null) xd = xm;
        BitVec result = rot(val(xm), rorn, Mode.RIGHT);
        result = shift(shift(val(xm), Mode.LEFT, 0), Mode.RIGHT, Configs.architecture - 7 + 0);
        result = signedExt(result, 16);
        result = shift(shift(result, Mode.LEFT, 16), Mode.RIGHT, Configs.architecture - 23 + 16);
        result = signedExt(result, 16);
    }

    public void sxth(Character xd, Character xm, Integer rorn) {
        arithmeticMode = ArithmeticMode.BINARY;
        if (xd == null) xd = xm;
        BitVec result = rot(val(xm), rorn, Mode.RIGHT);
        result = shift(shift(val(xm), Mode.LEFT, 0), Mode.RIGHT, Configs.architecture - 15 + 0);
        result = signedExt(result, 32);
    }

    public void uadd16(Character xd, Character xn, Character xm, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'G'};
        if (xd == null) xd = xn;
        BitVec[] resultArr = add(val(xn), val(xm), Type.HALFWORD);
        BitVec result = concat(resultArr);
        write(xd, result);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void uadd8(Character xd, Character xn, Character xm, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'G'};
        if (xd == null) xd = xn;
        BitVec[] resultArr = add(val(xn), val(xm), Type.BYTE);
        BitVec result = concat(resultArr);
        write(xd, result);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void uasx(Character xd, Character xn, Character xm, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'G'};
        if (xd == null) xd = xn;
        BitVec result = sub(shift(val(xm), Mode.RIGHT, 16), shift(shift(val(xn), Mode.LEFT, 16), Mode.RIGHT, 16));
        write(xd, result, Type.BOTTOM_HALFWORD);
        result = add(shift(val(xn), Mode.RIGHT, 16), shift(shift(val(xm), Mode.LEFT, 16), Mode.RIGHT, 16));
        write(xd, result, Type.TOP_HALFWORD);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void ubfx(Character xd, Character xn, Integer lsb, Integer width) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = shift(shift(val(xn), Mode.LEFT, sub(add(val(lsb), val(width)), val(1))), Mode.RIGHT,
                add(sub(new BitVec(Configs.architecture), new BitVec(lsb)), sub(add(val(lsb), val(width)), val(1))));
        result = zeroExt(result, 32);
        write(xd, result);
    }

    public void udiv(Character xd, Character xn, Character xm) {
        arithmeticMode = ArithmeticMode.BINARY;
        if (xd == null) xd = xn;
        BitVec result = div(val(xn), val(xm));
        if (!checkDiv(val(xn), val(xm))) result = round(result, RoundType.TOWARDS_ZERO);
        write(xd, result);
    }

    public void uhadd16(Character xd, Character xn, Character xm) {
        arithmeticMode = ArithmeticMode.BINARY;
        if (xd == null) xd = xn;
        BitVec[] resultArr = add(val(xn), val(xm), Type.HALFWORD);
        BitVec result = concat(resultArr);
        result = shift(resultArr, Mode.RIGHT, 1);
        write(xd, result);
    }

    public void uhadd8(Character xd, Character xn, Character xm) {
        arithmeticMode = ArithmeticMode.BINARY;
        if (xd == null) xd = xn;
        BitVec[] resultArr = add(val(xn), val(xm), Type.BYTE);
        BitVec result = concat(resultArr);
        result = shift(resultArr, Mode.RIGHT, 1);
        write(xd, result);
    }

    public void uhsub16(Character xd, Character xn, Character xm) {
        arithmeticMode = ArithmeticMode.BINARY;
        if (xd == null) xd = xn;
        BitVec[] resultArr = sub(val(xn), val(xm), Type.HALFWORD);
        BitVec result = concat(resultArr);
        result = shift(resultArr, Mode.RIGHT, 1);
        write(xd, result);
    }

    public void uhsub8(Character xd, Character xn, Character xm) {
        arithmeticMode = ArithmeticMode.BINARY;
        if (xd == null) xd = xn;
        BitVec[] resultArr = sub(val(xn), val(xm), Type.BYTE);
        BitVec result = concat(resultArr);
        result = shift(resultArr, Mode.RIGHT, 1);
        write(xd, result);
    }

    public void umaal(Character xlo, Character xhi, Character xn, Character xm) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = mul(val(xn), val(xm));
        result = add(result, val(xhi));
        result = add(result, val(xlo));
        write(xhi, shift(result, Mode.RIGHT, 32));
        write(xlo, shift(shift(result, Mode.LEFT, 32), Mode.RIGHT, 32));
    }

    public void umlal(Character xlo, Character xhi, Character xn, Character xm) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = mul(val(xn), val(xm));
        result = add(result, xlo, xhi);
        write(xlo, shift(shift(result, Mode.LEFT, 32), Mode.RIGHT, 32));
        write(xhi, shift(result, Mode.RIGHT, 32));
    }

    public void umull(Character xlo, Character xhi, Character xn, Character xm) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = mul(val(xn), val(xm));
        write(xlo, shift(shift(result, Mode.LEFT, 32), Mode.RIGHT, 32));
        write(xhi, shift(result, Mode.RIGHT, 32));
    }

    public void uqadd16(Character xd, Character xn, Character xm) {
        arithmeticMode = ArithmeticMode.BINARY;
        if (xd == null) xd = xn;
        BitVec[] resultArr = add(val(xn), val(xm), Type.HALFWORD);
        BitVec result = concat(resultArr);
        result = sat(resultArr, 0, Math.pow(2, 16) - 1);
        write(xd, result);
    }

    public void uqadd8(Character xd, Character xn, Character xm) {
        arithmeticMode = ArithmeticMode.BINARY;
        if (xd == null) xd = xn;
        BitVec[] resultArr = add(val(xn), val(xm), Type.BYTE);
        BitVec result = concat(resultArr);
        result = sat(resultArr, 0, Math.pow(2, 8) - 1);
        write(xd, result);
    }

    public void uqsub16(Character xd, Character xn, Character xm) {
        arithmeticMode = ArithmeticMode.BINARY;
        if (xd == null) xd = xn;
        BitVec[] resultArr = sub(val(xn), val(xm), Type.HALFWORD);
        BitVec result = concat(resultArr);
        result = sat(resultArr, 0, Math.pow(2, 16) - 1);
        write(xd, result);
    }

    public void uqsub8(Character xd, Character xn, Character xm) {
        arithmeticMode = ArithmeticMode.BINARY;
        if (xd == null) xd = xn;
        BitVec[] resultArr = sub(val(xn), val(xm), Type.BYTE);
        BitVec result = concat(resultArr);
        result = sat(resultArr, 0, Math.pow(2, 8) - 1);
        write(xd, result);
    }

    public void usad8(Character xd, Character xn, Character xm) {
        arithmeticMode = ArithmeticMode.BINARY;
        if (xd == null) xd = xn;
        BitVec[] resultArr = sub(val(xn), val(xm), Type.BYTE);
        BitVec result = concat(resultArr);
        result = add(abs(shift(result, Mode.RIGHT, 8)), abs(shift(result, Mode.LEFT, 8)));
        write(xd, result);
    }

    public void usada8(Character xd, Character xn, Character xm, Character xa) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec[] resultArr = sub(val(xn), val(xm), Type.BYTE);
        BitVec result = concat(resultArr);
        result = add(abs(shift(result, Mode.RIGHT, 8)), abs(shift(result, Mode.LEFT, 8)));
        result = add(val(xa), result);
        write(xd, result);
    }

    public void usat(Character xd, Integer n, Character xm, int shiftMode, int s, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'Q'};
        BitVec[] resultArr = toArray(shift(val(xm), shiftMode, s));
        BitVec result = sat(resultArr, 0, Math.pow(2, n) - 1);
        write(xd, result);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void usat16(Character xd, Integer n, Character xm, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'Q'};
        BitVec result = sat(toArray(val(xm, Type.HALFWORD)), 0, Math.pow(2, n) - 1);
        write(xd, result);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void usax(Character xd, Character xn, Character xm, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'G'};
        if (xd == null) xd = xn;
        BitVec result = add(shift(shift(val(xn), Mode.LEFT, 16), Mode.RIGHT, 16), shift(val(xm), Mode.RIGHT, 16));
        write(xd, result, Type.BOTTOM_HALFWORD);
        result = sub(shift(val(xn), Mode.RIGHT, 16), shift(shift(val(xm), Mode.LEFT, 16), Mode.RIGHT, 16));
        write(xd, result, Type.TOP_HALFWORD);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void usub16(Character xd, Character xn, Character xm, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'G'};
        if (xd == null) xd = xn;
        BitVec[] resultArr = sub(val(xn), val(xm), Type.HALFWORD);
        BitVec result = concat(resultArr);
        write(xd, result);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void usub8(Character xd, Character xn, Character xm, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'G'};
        if (xd == null) xd = xn;
        BitVec[] resultArr = sub(val(xn), val(xm), Type.BYTE);
        BitVec result = concat(resultArr);
        write(xd, result);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void uxtab(Character xd, Character xn, Character xm, Integer rorn) {
        arithmeticMode = ArithmeticMode.BINARY;
        if (xd == null) xd = xn;
        BitVec result = rot(val(xm), rorn, Mode.RIGHT);
        result = shift(shift(val(xm), Mode.LEFT, 0), Mode.RIGHT, Configs.architecture - 7 + 0);
        result = zeroExt(result, 32);
        result = add(result, val(xn));
        write(xd, result);
    }

    public void uxtab16(Character xd, Character xn, Character xm, Integer rorn) {
        arithmeticMode = ArithmeticMode.BINARY;
        if (xd == null) xd = xn;
        BitVec result = rot(val(xm), rorn, Mode.RIGHT);
        result = shift(shift(val(xm), Mode.LEFT, 0), Mode.RIGHT, Configs.architecture - 7 + 0);
        result = zeroExt(result, 16);
        result = shift(shift(result, Mode.LEFT, 16), Mode.RIGHT, Configs.architecture - 23 + 16);
        result = zeroExt(result, 16);
        result = add(result, val(xn));
        write(xd, result);
    }

    public void uxtah(Character xd, Character xn, Character xm, Integer rorn) {
        arithmeticMode = ArithmeticMode.BINARY;
        if (xd == null) xd = xn;
        BitVec result = rot(val(xm), rorn, Mode.RIGHT);
        result = shift(shift(val(xm), Mode.LEFT, 0), Mode.RIGHT, Configs.architecture - 15 + 0);
        result = zeroExt(result, 32);
        result = add(result, val(xn));
        write(xd, result);
    }

    public void uxtb(Character xd, Character xm, Integer rorn) {
        arithmeticMode = ArithmeticMode.BINARY;
        if (xd == null) xd = xm;
        BitVec result = rot(val(xm), rorn, Mode.RIGHT);
        result = shift(shift(val(xm), Mode.LEFT, 0), Mode.RIGHT, Configs.architecture - 7 + 0);
        result = zeroExt(result, 32);
    }

    public void uxtb16(Character xd, Character xm, Integer rorn) {
        arithmeticMode = ArithmeticMode.BINARY;
        if (xd == null) xd = xm;
        BitVec result = rot(val(xm), rorn, Mode.RIGHT);
        result = shift(shift(val(xm), Mode.LEFT, 0), Mode.RIGHT, Configs.architecture - 7 + 0);
        result = zeroExt(result, 16);
        result = shift(shift(result, Mode.LEFT, 16), Mode.RIGHT, Configs.architecture - 23 + 16);
        result = zeroExt(result, 16);
    }

    public void uxth(Character xd, Character xm, Integer rorn) {
        arithmeticMode = ArithmeticMode.BINARY;
        if (xd == null) xd = xm;
        BitVec result = rot(val(xm), rorn, Mode.RIGHT);
        result = shift(shift(val(xm), Mode.LEFT, 0), Mode.RIGHT, Configs.architecture - 15 + 0);
        result = zeroExt(result, 32);
    }

    public void vabs(Character xd, Character xm) {
        arithmeticMode = ArithmeticMode.FLOATING_POINT;
        BitVec result = abs(val(xm));
        write(xd, result);
    }

    public void vadd(Character xd, Character xn, Character xm) {
        arithmeticMode = ArithmeticMode.FLOATING_POINT;
        if (xd == null) xd = xn;
        BitVec result = add(val(xn), val(xm));
        write(xd, result);
    }

    public void vcvta(Character xd, Character xm) {
        arithmeticMode = ArithmeticMode.FLOATING_POINT;
        BitVec result = val(xm);
        result = round(result, RoundType.NEAREST_TIE);
        write(xd, result);
    }

    public void vcvtm(Character xd, Character xm) {
        arithmeticMode = ArithmeticMode.FLOATING_POINT;
        BitVec result = val(xm);
        result = round(result, RoundType.NEAREST_EVEN);
        write(xd, result);
    }

    public void vcvtn(Character xd, Character xm) {
        arithmeticMode = ArithmeticMode.FLOATING_POINT;
        BitVec result = val(xm);
        result = round(result, RoundType.TOWARDS_PLUS_INF);
        write(xd, result);
    }

    public void vcvtp(Character xd, Character xm) {
        arithmeticMode = ArithmeticMode.FLOATING_POINT;
        BitVec result = val(xm);
        result = round(result, RoundType.TOWARDS_MINUS_INF);
        write(xd, result);
    }

    public void vdiv(Character xd, Character xn, Character xm) {
        arithmeticMode = ArithmeticMode.FLOATING_POINT;
        if (xd == null) xd = xn;
        BitVec result = div(val(xn), val(xm));
        write(xd, result);
    }

    public void vfma(Character xd, Character xn, Character xm) {
        arithmeticMode = ArithmeticMode.FLOATING_POINT;
        if (xd == null) xd = xn;
        BitVec result = mul(val(xn), val(xm));
        write(xd, add(val(xd), result));

    }

    public void vfms(Character xd, Character xn, Character xm) {
        arithmeticMode = ArithmeticMode.FLOATING_POINT;
        if (xd == null) xd = xn;
        write(xn, neg(val(xn)));
        BitVec result = mul(val(xn), val(xm));
        result = add(result, val(xd));
        write(xd, result);

    }

    public void vfnma(Character xd, Character xn, Character xm) {
        arithmeticMode = ArithmeticMode.FLOATING_POINT;
        if (xd == null) xd = xn;
        write(xn, neg(val(xn)));
        BitVec result = mul(val(xn), val(xm));
        result = add(neg(val(xd)), result);
        write(xd, result);

    }

    public void vfnms(Character xd, Character xn, Character xm) {
        arithmeticMode = ArithmeticMode.FLOATING_POINT;
        if (xd == null) xd = xn;
        BitVec result = mul(val(xn), val(xm));
        result = add(result, neg(val(xd)));
        write(xd, result);

    }

    public void vmaxnm(Character xd, Character xn, Character xm) {
        arithmeticMode = ArithmeticMode.FLOATING_POINT;
        BitVec result = cmp(val(xn), val(xm));
        write(xd, max(val(xn), val(xm)));
    }

    public void vminnm(Character xd, Character xn, Character xm) {
        arithmeticMode = ArithmeticMode.FLOATING_POINT;
        BitVec result = cmp(val(xn), val(xm));
        write(xd, min(val(xn), val(xm)));
    }

    public void vmla(Character xd, Character xn, Character xm) {
        arithmeticMode = ArithmeticMode.FLOATING_POINT;
        BitVec result = mul(val(xn), val(xm));
        write(xd, add(result, val(xd)));
    }

    public void vmls(Character xd, Character xn, Character xm) {
        arithmeticMode = ArithmeticMode.FLOATING_POINT;
        BitVec result = mul(val(xn), val(xm));
        result = sub(val(xd), result);
        write(xd, result);
    }

    public void vmov(Character xd, Character xm) {
        arithmeticMode = ArithmeticMode.FLOATING_POINT;
        BitVec result = val(xm);
        write(xd, result);
    }

    public void vmov(Character xd, Integer im) {
        arithmeticMode = ArithmeticMode.FLOATING_POINT;
        BitVec result = val(im);
        write(xd, result);
    }

    public void vmsr(Character fpscr, Character xt) {
        arithmeticMode = ArithmeticMode.FLOATING_POINT;
        write('F', val(xt));
    }

    public void vmul(Character xd, Character xn, Character xm) {
        arithmeticMode = ArithmeticMode.FLOATING_POINT;
        if (xd == null) xd = xn;
        BitVec result = mul(val(xn), val(xm));
        write(xd, result);
    }

    public void vneg(Character xd, Character xm) {
        arithmeticMode = ArithmeticMode.FLOATING_POINT;
        BitVec result = neg(val(xm));
        write(xd, result);
    }

    public void vnmla(Character xd, Character xn, Character xm) {
        arithmeticMode = ArithmeticMode.FLOATING_POINT;
        BitVec result = mul(val(xn), val(xm));
        result = add(neg(val(xd)), result);
        write(xd, result);
    }

    public void vnmls(Character xd, Character xn, Character xm) {
        arithmeticMode = ArithmeticMode.FLOATING_POINT;
        BitVec result = mul(val(xn), val(xm));
        result = add(neg(val(xd)), result);
        write(xd, result);
    }

    public void vnmul(Character xd, Character xn, Character xm) {
        arithmeticMode = ArithmeticMode.FLOATING_POINT;
        if (xd == null) xd = xn;
        BitVec result = mul(val(xn), val(xm));
        write(xd, neg(result));
    }

    public void vpop(Character list) {
        arithmeticMode = ArithmeticMode.FLOATING_POINT;
        pop(list);
    }

    public void vpush(Character list) {
        arithmeticMode = ArithmeticMode.FLOATING_POINT;
        push(list);
    }

    public void vsqrt(Character xd, Character xm) {
        arithmeticMode = ArithmeticMode.FLOATING_POINT;
        BitVec result = sqrt(val(xm));
        write(xm, result);
    }

    public void vsub(Character xd, Character xn, Character xm) {
        arithmeticMode = ArithmeticMode.FLOATING_POINT;
        if (xd == null) xd = xn;
        BitVec result = sub(val(xn), val(xm));
        write(xd, result);
    }

    public void clz(Character xd, Character xm) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = clz(val(xm));
        write(xd, result);

    }

}
