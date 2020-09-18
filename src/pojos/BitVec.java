package pojos;

import utils.Arithmetic;
import utils.Z3Solver;

import java.util.BitSet;

public class BitVec {
    private String sym;
    private BitSet val;

    public BitVec(String sym, int n) {
        this.sym = Arithmetic.intToHexSmt(n);
        this.val = Arithmetic.intToBitSet(n);
    }

    public BitVec(String sym, BitSet val) {
        String result = (sym.matches("[01][01]+") || sym.matches("^(0x|0X|#x)?[a-fA-F0-9]+$")) ? sym: Z3Solver.solveBitVecArithmetic(sym);
        this.sym = (!result.equals("ERROR")) ? result : sym;
        this.val = val;
    }

    public BitVec(BitVec bv) {
        this.sym = bv.sym;
        this.val = bv.val;
    }

    public BitVec(Integer n) {
        this.sym = Arithmetic.intToHexSmt(n);
        this.val = Arithmetic.intToBitSet(n);
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
        if (!result.equals("ERROR"))  {
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
}
