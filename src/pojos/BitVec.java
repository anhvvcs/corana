package pojos;

import com.sun.jna.NativeLong;
import utils.Arithmetic;

import java.util.BitSet;

public class BitVec {
    private String sym;
    private BitSet val;

    public BitVec(String sym, int n) {
        this.sym = Arithmetic.intToHexSmt(n);
        this.val = Arithmetic.intToBitSet(n);
    }

    public BitVec(String sym, BitSet val) {
        this.sym = sym;
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
    public BitVec(NativeLong n) {
        this.sym = Arithmetic.intToHexSmt(n.longValue());
        this.val = Arithmetic.longToBitSet(n.longValue());
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

    @Override
    public String toString() {
        return "BitVec{" +
                "sym='" + sym + '\'' +
                ", val=" + val +
                '}';
    }
}
