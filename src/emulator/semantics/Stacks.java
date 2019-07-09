package emulator.semantics;

import pojos.BitVec;
import utils.Arithmetic;
import utils.Logs;
import utils.MyStr;

import java.util.BitSet;
import java.util.Stack;

public class Stacks {
    public Stack<BitVec> stack;
    public int length;

    public Stacks(int length) {
        stack = new java.util.Stack<>();
        this.length = length;
    }

    public boolean push(BitVec b) {
        stack.push(b);
        return true;
    }

    public BitVec pop() {
        if (stack.size() == 0) {
            Logs.infoLn("Error: Stack is empty !");
            return null;
        }
        return stack.pop();
    }

    @Override
    public String toString() {
        MyStr myStr = new MyStr("+ Stack:\n");
        for (int i = 0; i < stack.size(); i++) {
            BitSet b = stack.get(i).getVal();
            String bs = stack.get(i).getSym();
            String val = Arithmetic.bitsetToStr(b);
            if (val.length() > 0) {
                myStr.append("\t- ", i, "\t:", val, "\t", bs, "\n");
            }
        }
        return myStr.value();
    }
}
