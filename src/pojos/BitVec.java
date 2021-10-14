package pojos;

import com.sun.jna.NativeLong;
import executor.Configs;
import utils.Arithmetic;
import utils.SysUtils;
import utils.Z3Solver;

import java.math.BigInteger;
import java.util.BitSet;
import java.util.Random;
import java.util.function.Predicate;

public class BitVec {
    private String sym;
    private BitSet val;
    public Random generator;

    public BitVec(String sym, int n) {
        this.sym = Arithmetic.intToHexSmt(n);
        this.val = Arithmetic.intToBitSet(n);
    }

    public BitVec(String sym, BitSet val) {
        String result = (sym.matches("[01][01]+") || sym.matches("^(0x|0X|#x)?[a-fA-F0-9]+$")) ? sym : Z3Solver.solveBitVecArithmetic(sym);
        this.sym = (!result.equals("ERROR")) ? SysUtils.normalizeNumInHex(result) : sym;
        this.val = val;
    }

    public BitVec(byte bv) {
        this.sym = Arithmetic.intToHexSmt(bv);
        this.val = Arithmetic.longToBitSet(bv);
    }

    public BitVec(NativeLong n) {
        this.sym = Arithmetic.intToHexSmt(n.longValue());
        this.val = Arithmetic.longToBitSet(n.longValue());
    }

    public BitVec(BitVec bv) {
        this.sym = bv.sym;
        this.val = bv.val;
    }

    public BitVec(String sym) {
        Configs.RANDOM_SEED += 1;
        generator = new Random(Configs.RANDOM_SEED);
        this.sym = sym;
        this.val = Arithmetic.intToBitSet(rand());
    }

    public BitVec(Integer n) {
        this.sym = Arithmetic.intToHexSmt(n);
        this.val = Arithmetic.intToBitSet(n);
    }

    public BitVec(Object n) {
    }

    public BitVec(Short n) {
        this.sym = Arithmetic.intToHexSmt(n);
        this.val = Arithmetic.longToBitSet(n);
    }

    public BitVec(Long n) {
        this.sym = Arithmetic.intToHexSmt(n);
        this.val = Arithmetic.longToBitSet(n);
    }

    public String getSym() {
        return sym;
    }

    public void setSym(String sym) {
        this.sym = sym;
    }

    public BitSet getVal() {
        return val;
    }

    public void setVal(BitSet val) {
        this.val = val;
    }

    public void calculate() {
        String result = (getSym().matches("[01][01]+") || getSym().matches("^(0x|0X|#x)?[a-fA-F0-9]+$")) ? getSym() : Z3Solver.solveBitVecArithmetic(getSym());
        if (!result.equals("ERROR")) {
            this.setSym(result);
            this.setVal(Arithmetic.intToBitSet((int) Arithmetic.hexToInt(result)));
        }
    }

    @Override
    public String toString() {
        return "BitVec{" +
                "sym='" + sym + '\'' +
                ", val=" + val +
                '}';
    }

    public int rand() {
        return generator.nextInt((int) Math.pow(2, Configs.architecture >> 2));
    }

    public BitVec add(int byte_step) {
        String hex = this.sym;
        return Arithmetic.fromHexStr(Arithmetic.intToHex(Arithmetic.hexToInt(hex) + byte_step));
    }

//    public String normalizeZeros(String s) {
//        long digits = s.length() - (s.indexOf('x') + 1);
//        if (digits > 8) {
//            long zeros =  s.chars().mapToObj(i -> (char) i).filter(i -> i == '0').count();
//            long notNeed = digits - (8 - zeros);
//            for (int i = 0; i < notNeed; i++) {
//                s = s.replaceFirst("0", "");
//            }
//        }
//        return s;
//    }
}
