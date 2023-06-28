package emulator.semantics;

import executor.Configs;
import pojos.BitVec;
import utils.Arithmetic;
import utils.MyStr;

import java.util.BitSet;
import java.util.Random;

public class Environment {
    public Flags flags;
    public Register register;
    public Memory memory;
    public Stacks stacks;
    public Random generator;

    public Environment(Environment env) {
        this.flags = env.flags;
        this.register = env.register;
        this.memory = env.memory;
        this.stacks = env.stacks;
    }
    public Environment(boolean n, boolean z, boolean c, boolean v, boolean q, boolean ge, BitSet r0, BitSet r1, BitSet r2, BitSet r3, BitSet r4, BitSet r5, BitSet r6,
                       BitSet r7, BitSet r8, BitSet r9, BitSet r10, BitSet r11, BitSet r12, BitSet sp, BitSet lr, BitSet pc) {
        flags = new Flags(n, z, c, v, q, ge);
        register = new Register(Configs.architecture, r0, r1, r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, sp, lr, pc);
        memory = new Memory(Configs.architecture);
        stacks = new Stacks(Configs.architecture);
    }

    public Environment(boolean n, boolean z, boolean c, boolean v, boolean q, boolean ge, int r0, int r1, int r2, int r3, int r4, int r5, int r6,
                       int r7, int r8, int r9, int r10, int r11, int r12, int sp, int lr, int pc) {
        flags = new Flags(n, z, c, v, q, ge);
        register = new Register(Configs.architecture, Arithmetic.intToBitSet(r0), Arithmetic.intToBitSet(r1), Arithmetic.intToBitSet(r2),
                Arithmetic.intToBitSet(r3), Arithmetic.intToBitSet(r4), Arithmetic.intToBitSet(r5),
                Arithmetic.intToBitSet(r6), Arithmetic.intToBitSet(r7), Arithmetic.intToBitSet(r8),
                Arithmetic.intToBitSet(r9), Arithmetic.intToBitSet(r10), Arithmetic.intToBitSet(r11),
                Arithmetic.intToBitSet(r12), Arithmetic.intToBitSet(sp), Arithmetic.intToBitSet(lr),
                Arithmetic.intToBitSet(pc));
        memory = new Memory(Configs.architecture);
        stacks = new Stacks(Configs.architecture);
    }

    public Environment(boolean... isRandom) {
        Configs.RANDOM_SEED += 1;
        generator = new Random(Configs.RANDOM_SEED);
        if (isRandom != null && isRandom.length >= 1 && isRandom[0]) {
            flags = new Flags(rand() % 2 == 0, rand() % 2 == 0, rand() % 2 == 0, rand() % 2 == 0, rand() % 2 == 0, rand() % 2 == 0);
            register = new Register(Configs.architecture, Arithmetic.intToBitSet(rand()), Arithmetic.intToBitSet(rand()),
                    Arithmetic.intToBitSet(rand()), Arithmetic.intToBitSet(rand()),
                    Arithmetic.intToBitSet(rand()), Arithmetic.intToBitSet(rand()),
                    Arithmetic.intToBitSet(rand()), Arithmetic.intToBitSet(rand()),
                    Arithmetic.intToBitSet(rand()), Arithmetic.intToBitSet(rand()),
                    Arithmetic.intToBitSet(rand()), Arithmetic.intToBitSet(rand()),
                    Arithmetic.intToBitSet(rand()), Arithmetic.intToBitSet(rand()),
                    Arithmetic.intToBitSet(rand()), Arithmetic.intToBitSet(rand()));
            memory = new Memory(Configs.architecture);
            stacks = new Stacks(Configs.architecture);
        } else {
            flags = new Flags(false, false, false, false, false, false);
            register = new Register(Configs.architecture, Arithmetic.intToBitSet(0), Arithmetic.intToBitSet(0),
                    Arithmetic.intToBitSet(0), Arithmetic.intToBitSet(0),
                    Arithmetic.intToBitSet(0), Arithmetic.intToBitSet(0),
                    Arithmetic.intToBitSet(0), Arithmetic.intToBitSet(0),
                    Arithmetic.intToBitSet(0), Arithmetic.intToBitSet(0),
                    Arithmetic.intToBitSet(0), Arithmetic.intToBitSet(0),
                    Arithmetic.intToBitSet(0), Arithmetic.intToBitSet(0),
                    Arithmetic.intToBitSet(0), Arithmetic.intToBitSet(0));
            memory = new Memory(Configs.architecture);
            stacks = new Stacks(Configs.architecture);
        }
    }

    public String takeInstance() {
        MyStr myStr = new MyStr();
        myStr.append("{\"n\":", flags.N.isConcreteValue(), ",\"z\":", flags.Z.isConcreteValue(), ",\"c\":", flags.C.isConcreteValue(), ",\"v\":",
                flags.V.isConcreteValue(), ",\"q\":", flags.Q.isConcreteValue(), ",\"g\":", flags.GE.isConcreteValue());
        for (char c = '0'; c <= '9'; c++) {
            myStr.append(",\"", c, "\":", Arithmetic.bitSetToLong(register.regs.get(c).getVal()));
        }
        myStr.append(",\"x\":", Arithmetic.bitSetToLong(register.regs.get('x').getVal()));
        myStr.append(",\"e\":", Arithmetic.bitSetToLong(register.regs.get('e').getVal()));
        myStr.append(",\"t\":", Arithmetic.bitSetToLong(register.regs.get('t').getVal()));
        myStr.append(",\"s\":", Arithmetic.bitSetToLong(register.regs.get('s').getVal()));
        myStr.append(",\"l\":", Arithmetic.bitSetToLong(register.regs.get('l').getVal()));
        myStr.append(",\"p\":", Arithmetic.bitSetToLong(register.regs.get('p').getVal()), "}");
        return myStr.value();
    }

    public BitVec value(Character r) {
        return new BitVec(register.get(r));
    }

    @Override
    public String toString() {
        return new MyStr(flags.toString(), register.toString(), stacks.toString(), memory.toString()).value();
    }

    public int rand() {
        return generator.nextInt((int) Math.pow(2, Configs.architecture >> 2));
    }
}