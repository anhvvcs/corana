package emulator.cortex;

import executor.Configs;
import enums.*;
import pojos.BitVec;
import utils.Arithmetic;
import emulator.base.*;
import emulator.semantics.*;

public class M0_Plus extends Emulator {

	public M0_Plus(Environment env) {super(env);}

    public void adcs(Character xd, Character xn, Character xm, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N','Z','C','V'};
        if (xd == null) xd = xn;
		BitVec result = add(val(xn),val(xm),true);
            write(xd,result);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void add(Character xd, Character xn, Character xm, Integer im, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N','Z','C','V'};
        if (xd == null) xd = xn;
		BitVec result = add(val(xn),val(xm,im));
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

    public void ands(Character xd, Character xn, Character xm, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N','Z','C','V'};
        if (xd == null) xd = xn;
		BitVec result = and(val(xn),val(xm));
            write(xd,result);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void bics(Character xd, Character xn, Character xm, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N','Z','C','V'};
        if (xd == null) xd = xn;
		BitVec result = and(val(xn),comp(val(xm)));
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

    public void cmp(Character xn, Character xm, Integer im, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N','Z','C','V'};
        BitVec result = cmp(val(xn),val(xm,im));
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

    public void eors(Character xd, Character xn, Character xm, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N','Z','C','V'};
        if (xd == null) xd = xn;
		BitVec result = xor(val(xn),val(xm));
            write(xd,result);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void ldr(Character xt, Character xn, Integer xp, Integer im) {
        arithmeticMode = ArithmeticMode.BINARY;
        if (im == null) im = 0;
		BitVec result = load(add(val(xn,xp),val(im)));
		result = zeroExt(result,32);
		write(xt,result);
    }

    public void ldr(Character xt, Character xn, Character xm) {
        arithmeticMode = ArithmeticMode.BINARY;
        write(xt,load(add(val(xn),val(xm))));
		
    }

    public void ldr(Character xt, Character label) {
        arithmeticMode = ArithmeticMode.BINARY;
        load(xt,label);
    }

    public void ldrb(Character xt, Character xn, Integer im) {
        arithmeticMode = ArithmeticMode.BINARY;
        if (im == null) im = 0;
		BitVec result = load(val(xn));
		result = zeroExt(result,32);
		write(xt,result);
    }

    public void ldrb(Character xt, Character xn, Character xm) {
        arithmeticMode = ArithmeticMode.BINARY;
        write(xt,load(zeroExt(add(val(xn),val(xm)),32)));
		
    }

    public void ldrh(Character xt, Character xn, Integer im) {
        arithmeticMode = ArithmeticMode.BINARY;
        if (im == null) im = 0;
		BitVec result = load(val(xn));
		result = zeroExt(result,32);
		write(xt,result);
    }

    public void ldrh(Character xt, Character xn, Character xm) {
        arithmeticMode = ArithmeticMode.BINARY;
        write(xt,load(zeroExt(add(val(xn),val(xm)),32)));
		
    }

    public void ldrsb(Character xt, Character xn, Character xm) {
        arithmeticMode = ArithmeticMode.BINARY;
        write(xt,load(signedExt(add(val(xn),val(xm)),32)));
		
    }

    public void ldrsh(Character xt, Character xn, Character xm) {
        arithmeticMode = ArithmeticMode.BINARY;
        write(xt,load(signedExt(add(val(xn),val(xm)),32)));
		
    }

    public void lsls(Character xd, Character xm, Character xs, Integer im, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N','Z','C','V'};
        if (xd == null) xd = xm;
		BitVec result = shift(val(xm),Mode.valueOf("left".toUpperCase()),val(im,shift(val(xs),Mode.LEFT,8)));
            write(xd,result);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void lsrs(Character xd, Character xm, Character xs, Integer im, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N','Z','C','V'};
        if (xd == null) xd = xm;
		BitVec result = shift(val(xm),Mode.valueOf("right".toUpperCase()),val(im,shift(val(xs),Mode.LEFT,8)));
            write(xd,result);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void mov(Character xd, Character xm, Integer im, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N','Z','C','V'};
        BitVec result = val(xm,im);
            write(xd,result);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void muls(Character xd, Character xn, Character xm, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N','Z','C','V'};
        BitVec result = mul(val(xn),val(xm));
		write(xd,shift(shift(result,Mode.LEFT,32),Mode.RIGHT,32));
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void mvns(Character xd, Character xm, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N','Z','C','V'};
        BitVec result = val(xm);
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

    public void orrs(Character xd, Character xn, Character xm, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N','Z','C','V'};
        if (xd == null) xd = xn;
		BitVec result = or(val(xn),val(xm));
            write(xd,result);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
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

    public void rsbs(Character xd, Character xn, Character xm, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N','Z','C','V'};
        if (xd == null) xd = xn;
		BitVec result = sub(val(xn),val(0));
		result = neg(val(xm));
		write(xd,result);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void sbcs(Character xd, Character xn, Character xm, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N','Z','C','V'};
        if (xd == null) xd = xn;
		BitVec result = sub(val(xn),val(xm));
		result = sub(result,val(1),true);
            write(xd,result);
        if (suffix != null && suffix == 's') {
            if (result != null) {
                updateFlags(flags, result);
            }
        }
    }

    public void str(Character xt, Character xn, Integer xp, Integer im) {
        arithmeticMode = ArithmeticMode.BINARY;
        if (im == null) im = 0;
		Type lt = Type.WORD;
		store(add(val(xn,xp),val(im)),val(xt,lt));
    }

    public void str(Character xt, Character xn, Character xm) {
        arithmeticMode = ArithmeticMode.BINARY;
        Type lt = Type.WORD;
		store(add(val(xn),val(xm)),val(xt,lt));
    }

    public void strh(Character xt, Character xn, Integer im) {
        arithmeticMode = ArithmeticMode.BINARY;
        if (im == null) im = 0;
		Type lt = Type.BOTTOM_HALFWORD;
		store(add(val(xn),val(im)),val(xt,lt));
    }

    public void strh(Character xt, Character xn, Character xm) {
        arithmeticMode = ArithmeticMode.BINARY;
        Type lt = Type.BOTTOM_HALFWORD;
		store(add(val(xn),val(xm)),val(xt,lt));
    }

    public void sub(Character xd, Character xn, Character xm, Integer im, Character suffix) {
        arithmeticMode = ArithmeticMode.BINARY;
        char[] flags = new char[]{'N','Z','C','V'};
        if (xd == null) xd = xn;
		BitVec result = sub(val(xn),val(xm,im));
            write(xd,result);
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

    public void sxtb(Character xd, Character xm) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = shift(shift(val(xm),Mode.LEFT,0),Mode.RIGHT,Configs.architecture-7+0);
		result = signedExt(result,32);
    }

    public void sxth(Character xd, Character xm) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = shift(shift(val(xm),Mode.LEFT,0),Mode.RIGHT,Configs.architecture-15+0);
		result = signedExt(result,32);
    }

    public void uxtb(Character xd, Character xm) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = shift(shift(val(xm),Mode.LEFT,0),Mode.RIGHT,Configs.architecture-7+0);
		result = zeroExt(result,32);
    }

    public void uxth(Character xd, Character xm) {
        arithmeticMode = ArithmeticMode.BINARY;
        BitVec result = shift(shift(val(xm),Mode.LEFT,0),Mode.RIGHT,Configs.architecture-15+0);
		result = zeroExt(result,32);
    }

}
