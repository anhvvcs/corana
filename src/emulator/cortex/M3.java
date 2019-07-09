package emulator.cortex;

import executor.Configs;
import enums.*;
import pojos.BitVec;
import utils.Arithmetic;
import emulator.base.*;
import emulator.semantics.*;

public class M3 extends Emulator {

	public M3(Environment env) {super(env);}

    public void adc(Character xd, Character xn, Character op, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N','Z','C','V'};
        if (xd == null) xd = xn;
		BitVec result = add(val(xn),val(op),true);
            write(xd,result);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void add(Character xd, Character xn, Character op, Integer im, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N','Z','C','V'};
        if (xd == null) xd = xn;
		BitVec result = add(val(xn),val(op,im));
            write(xd,result);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void addw(Character xd, Character xn, Integer im, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N','Z','C','V'};
        if (xd == null) xd = xn;
		BitVec result = add(val(xn),val(im));
            write(xd,result);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void adr(Character xd, Character label) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = add(env.register.get('p'),val(label));
		write(xd,result);
    }

    public void and(Character xd, Character xn, Character op, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N','Z','C','V'};
        if (xd == null) xd = xn;
		BitVec result = and(val(xn),val(op));
            write(xd,result);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void asr(Character xd, Character xm, Character xs, Integer n, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N','Z','C'};
        BitVec result = shift(val(xm),Mode.RIGHT,val(xs,n));
            write(xd,result);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void bfc(Character xd, Integer lsb, Integer width) {
        arithmeticMode = ArithmeticMode.BINARY;
        clearBitfield(xd,lsb,width);
		
    }

    public void bfi(Character xd, Character xn, Integer lsb, Integer width) {
        arithmeticMode = ArithmeticMode.BINARY;
        copyBitfield(xd,xn,lsb,width);
		
    }

    public void bic(Character xd, Character xn, Character op, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N','Z','C','V'};
        if (xd == null) xd = xn;
		BitVec result = and(val(xn),comp(val(op)));
            write(xd,result);
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
        cmp(xn,0,null);
        b("",new Throwable().getStackTrace()[0].getMethodName().equals("cbz") ?'e' : 'n',label);
    }

    public void cbz(Character xn, Character label) {
        arithmeticMode = ArithmeticMode.BINARY;
        cmp(xn,0,null);
        b("",new Throwable().getStackTrace()[0].getMethodName().equals("cbz") ?'e' : 'n',label);
    }

    public void cmp(Character xn, Character op, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N','Z','C','V'};
        BitVec result = cmp(val(xn),val(op));
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
        char[] flags = new char[]{'N','Z','C','V'};
        if (xd == null) xd = xn;
		BitVec result = xor(val(xn),val(op));
            write(xd,result);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
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
        load(xt,label);
    }

    public void ldrb(Character xt, Character xn, Integer offset, Integer offsetType) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = add(val(xn), val(offset));
        assert offsetType >= 1 && offsetType <= 3;
        write(xt, offsetType == 3 ? val(xn) : load(result));
        write(xn, offsetType == 1 ? val(xn) : result);
		result = zeroExt(result,32);
    }

    public void ldrb(Character xt, Character label) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = load(val(label));
		result = zeroExt(result,32);
    }

    public void ldrd(Character xt, Character xt2, Character xn, Integer offset, Integer offsetType) {
        arithmeticMode = ArithmeticMode.BINARY;
        write(xt,load(add(val(xn),val(offset))));
            write(xt2,load(add(val(xn),val(offset))));
    }

    public void ldrd(Character xt, Character xt2, Character label) {
        arithmeticMode = ArithmeticMode.BINARY;
        write(xt,load(val(label)));
            write(xt2,load(val(label)));
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
        write(xt,load(val(xn)));
    }

    public void ldrexh(Character xt, Character xn) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = load(val(xn));
            write(xt,result);
    }

    public void ldrh(Character xt, Character xn, Integer offset, Integer offsetType) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = add(val(xn), val(offset));
        assert offsetType >= 1 && offsetType <= 3;
        write(xt, offsetType == 3 ? val(xn) : load(result));
        write(xn, offsetType == 1 ? val(xn) : result);
		result = zeroExt(result,32);
    }

    public void ldrh(Character xt, Character label) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = load(val(label));
		result = zeroExt(result,32);
    }

    public void ldrsb(Character xt, Character xn, Integer offset, Integer offsetType) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = add(val(xn), val(offset));
        assert offsetType >= 1 && offsetType <= 3;
        write(xt, offsetType == 3 ? val(xn) : load(result));
        write(xn, offsetType == 1 ? val(xn) : result);
		result = signedExt(result,32);
    }

    public void ldrsb(Character xt, Character label) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = load(val(label));
		result = signedExt(result,32);
    }

    public void ldrsh(Character xt, Character xn, Integer offset, Integer offsetType) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = add(val(xn), val(offset));
        assert offsetType >= 1 && offsetType <= 3;
        write(xt, offsetType == 3 ? val(xn) : load(result));
        write(xn, offsetType == 1 ? val(xn) : result);
		result = signedExt(result,32);
    }

    public void ldrsh(Character xt, Character label) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = load(val(label));
		result = signedExt(result,32);
    }

    public void lsl(Character xd, Character xm, Character xs, Integer n, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N','Z','C'};
        BitVec result = shift(val(xm),Mode.LEFT,val(xs,n));
            write(xd,result);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void lsr(Character xd, Character xm, Character xs, Integer n, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N','Z','C'};
        BitVec result = shift(val(xm),Mode.RIGHT,val(xs,n));
            write(xd,result);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void mla(Character xd, Character xn, Character xm, Character xa) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = mul(val(xn),val(xm));
		result = add(result,val(xa));
		write(xd,shift(shift(result,Mode.LEFT,32),Mode.RIGHT,32));
    }

    public void mls(Character xd, Character xn, Character xm, Character xa) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = mul(val(xn),val(xm));
		result = sub(val(xa),result);
		write(xd,shift(shift(result,Mode.LEFT,32),Mode.RIGHT,32));
    }

    public void mov(Character xd, Character op, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N','Z','C','V'};
        BitVec result = val(op);
            write(xd,result);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void movw(Character xd, Integer im, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N','Z','C','V'};
        BitVec result = val(im);
            write(xd,result);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void mul(Character xd, Character xn, Character xm, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N','Z','C','V'};
        if (xd == null) xd = xn;
		BitVec result = mul(val(xn),val(xm));
		write(xd,shift(shift(result,Mode.LEFT,32),Mode.RIGHT,32));
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void mvn(Character xd, Character op, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N','Z','C','V'};
        BitVec result = val(op);
		result = not(result);
		write(xd,result);
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
        char[] flags = new char[]{'N','Z','C','V'};
        if (xd == null) xd = xn;
		BitVec result = or(val(xn),comp(val(op)));
            write(xd,result);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void orr(Character xd, Character xn, Character op, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N','Z','C','V'};
        if (xd == null) xd = xn;
		BitVec result = or(val(xn),val(op));
            write(xd,result);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void rbit(Character xd, Character xn) {
        arithmeticMode = ArithmeticMode.BINARY;
        write(xd,rev(val(xn),Type.WORD));
    }

    public void rev(Character xd, Character xn) {
        arithmeticMode = ArithmeticMode.BINARY;
        write(xd,rev(val(xn),Type.WORD));
    }

    public void rev16(Character xd, Character xn) {
        arithmeticMode = ArithmeticMode.BINARY;
        write(xd,rev(val(xn),Type.HALFWORD));
    }

    public void revsh(Character xd, Character xn) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = rev(val(xn),Type.HALFWORD);
        result = signedExt(result,16);
        write(xd,result);
    }

    public void ror(Character xd, Character xm, Character xs, Integer n, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N','Z','C'};
        BitVec result = shift(val(xm),Mode.RIGHT,val(xs,n));
            write(xd,result);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void rrx(Character xd, Character xm, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N','Z','C'};
        BitVec result = shift(val(xm),Mode.RIGHT,val(1));
            write(xd,result);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void rsb(Character xd, Character xn, Character op, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N','Z','C','V'};
        if (xd == null) xd = xn;
		BitVec result = sub(val(op),val(xn));
            write(xd,result);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void sbc(Character xd, Character xn, Character op, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N','Z','C','V'};
        if (xd == null) xd = xn;
		BitVec result = sub(val(op),val(xn));
            write(xd,result);
		result = sub(result,val(1),true);
            write(xd,result);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void sbfx(Character xd, Character xn, Integer lsb, Integer width) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = shift(shift(val(xn),Mode.LEFT,sub(add(val(lsb),val(width)),val(1))),Mode.RIGHT,
                    add(sub(new BitVec(Configs.architecture),new BitVec(lsb)),sub(add(val(lsb),val(width)),val(1))));
		result = signedExt(result,32);
		write(xd,result);
    }

    public void smlal(Character xlo, Character xhi, Character xn, Character xm) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = mul(comp(val(xn)),comp(val(xm)));
		result = add(result,xlo,xhi);
		write(xlo,shift(shift(result,Mode.LEFT,32),Mode.RIGHT,32));
            write(xhi,shift(result,Mode.RIGHT,32));
    }

    public void smull(Character xlo, Character xhi, Character xn, Character xm) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = mul(val(xn),val(xm));
		write(xlo,shift(shift(result,Mode.LEFT,32),Mode.RIGHT,32));
		write(xhi,shift(result,Mode.RIGHT,32));
    }

    public void ssat(Character xd, Integer n, Character xm, int shiftMode, int s, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'Q'};
        BitVec[] resultArr = toArray(shift(val(xm),shiftMode,s));
		BitVec result = sat(resultArr,-Math.pow(2,n-1),Math.pow(2,n-1)-1);
            write(xd,result);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void str(Character xt, Character xn, Integer offset, Integer offsetType) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = add(val(xn), val(offset));
        assert offsetType >= 1 && offsetType <= 3;
        store(val(xt),offsetType == 3 ? val(xn) : result);
        write(xn, offsetType == 1 ? val(xn) : result);
    }

    public void strd(Character xt, Character xt2, Character xn, Integer offset, Integer offsetType) {
        arithmeticMode = ArithmeticMode.BINARY;
        store(val(xn),val(xt),val(xt2));
    }

    public void strex(Character xd, Character xt, Character xn, Integer offset, Integer offsetType) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = add(val(xn), val(offset));
        assert offsetType >= 1 && offsetType <= 3;
        store(val(xt),offsetType == 3 ? val(xn) : result);
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
        store(val(xt),offsetType == 3 ? val(xn) : result);
        write(xn, offsetType == 1 ? val(xn) : result);
    }

    public void strh(Character xt, Character xn, Integer offset, Integer offsetType) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = add(val(xn), val(offset));
        assert offsetType >= 1 && offsetType <= 3;
        store(val(xt), offsetType == 3 ? val(xn) : result);
        write(xn, offsetType == 1 ? val(xn) : result);
		result = zeroExt(result,32);
    }

    public void strhd(Character xt, Character xt2, Character xn, Integer offset, Integer offsetType) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = val(xt);
            BitVec result_2 = val(xt2);
            store(val(xn),result,result_2);
		result = zeroExt(result,32);
    }

    public void sub(Character xd, Character xn, Character op, Integer im, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N','Z','C','V'};
        if (xd == null) xd = xn;
		BitVec result = sub(val(xn),val(op,im));
            write(xd,result);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void subw(Character xd, Character xn, Integer im, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N','Z','C','V'};
        if (xd == null) xd = xn;
		BitVec result = sub(val(xn),val(im));
            write(xd,result);
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

    public void sxtb(Character xd, Character xm, Integer rorn) {
        arithmeticMode = ArithmeticMode.BINARY;
        if (xd == null) xd = xm;
		BitVec result = rot(val(xm),rorn,Mode.RIGHT);
		result = shift(shift(val(xm),Mode.LEFT,0),Mode.RIGHT,Configs.architecture-7+0);
		result = signedExt(result,32);
    }

    public void sxth(Character xd, Character xm, Integer rorn) {
        arithmeticMode = ArithmeticMode.BINARY;
        if (xd == null) xd = xm;
		BitVec result = rot(val(xm),rorn,Mode.RIGHT);
		result = shift(shift(val(xm),Mode.LEFT,0),Mode.RIGHT,Configs.architecture-15+0);
		result = signedExt(result,32);
    }

    public void ubfx(Character xd, Character xn, Integer lsb, Integer width) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = shift(shift(val(xn),Mode.LEFT,sub(add(val(lsb),val(width)),val(1))),Mode.RIGHT,
                    add(sub(new BitVec(Configs.architecture),new BitVec(lsb)),sub(add(val(lsb),val(width)),val(1))));
		result = zeroExt(result,32);
		write(xd,result);
    }

    public void udiv(Character xd, Character xn, Character xm) {
        arithmeticMode = ArithmeticMode.BINARY;
        if (xd == null) xd = xn;
		BitVec result = div(val(xn),val(xm));
		if (!checkDiv(val(xn), val(xm))) result = round(result,RoundType.TOWARDS_ZERO);
            write(xd,result);
    }

    public void umlal(Character xlo, Character xhi, Character xn, Character xm) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = mul(val(xn),val(xm));
		result = add(result,xlo,xhi);
		write(xlo,shift(shift(result,Mode.LEFT,32),Mode.RIGHT,32));
            write(xhi,shift(result,Mode.RIGHT,32));
    }

    public void umull(Character xlo, Character xhi, Character xn, Character xm) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = mul(val(xn),val(xm));
		write(xlo,shift(shift(result,Mode.LEFT,32),Mode.RIGHT,32));
		write(xhi,shift(result,Mode.RIGHT,32));
    }

    public void usat(Character xd, Integer n, Character xm, int shiftMode, int s, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'Q'};
        BitVec[] resultArr = toArray(shift(val(xm),shiftMode,s));
		BitVec result = sat(resultArr,0,Math.pow(2,n)-1);
            write(xd,result);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void uxtb(Character xd, Character xm, Integer rorn) {
        arithmeticMode = ArithmeticMode.BINARY;
        if (xd == null) xd = xm;
		BitVec result = rot(val(xm),rorn,Mode.RIGHT);
		result = shift(shift(val(xm),Mode.LEFT,0),Mode.RIGHT,Configs.architecture-7+0);
		result = zeroExt(result,32);
    }

    public void uxth(Character xd, Character xm, Integer rorn) {
        arithmeticMode = ArithmeticMode.BINARY;
        if (xd == null) xd = xm;
		BitVec result = rot(val(xm),rorn,Mode.RIGHT);
		result = shift(shift(val(xm),Mode.LEFT,0),Mode.RIGHT,Configs.architecture-15+0);
		result = zeroExt(result,32);
    }

    public void clz(Character xd, Character xm) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = clz(val(xm));
            write(xd,result);
		
    }

}
