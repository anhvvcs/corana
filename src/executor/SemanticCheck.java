package executor;


import emulator.base.Emulator;
import emulator.cortex.M0;
import emulator.semantics.Environment;
import enums.Variation;
import pojos.BitVec;
import utils.Z3Solver;

import java.util.Map;

//For instruction (Armv8 Aarch32, little endian) :
//MOV R1, 0x12
//that is :
//12 10 A0 E3
public class SemanticCheck {
    public static void main(String[] args) {
        long startTime = System.nanoTime();
        try {
            //"\x00\x20\x90\xe5"
            //byte[] bytes = new byte[]{(byte) 0x00, (byte) 0x20, (byte) 0x90, (byte) 0xE5, 00, (byte) 0xf0, (byte) 0x20, (byte)0xe3};
            //byte[] bytes = new byte[]{(byte) 0x12, (byte) 0x10, (byte)0xA0, (byte) 0xE3, 00, (byte) 0xf0, (byte) 0x20, (byte)0xe3};
            //byte[] bytes = new byte[] {(byte) "\x37\x10\x80\xc2"};
            //byte[] bytes = new byte[]{(byte) 0x37, (byte) 0x10, (byte) 0x80, (byte) 0xc2, 00, (byte) 0xf0, (byte) 0x20, (byte)0xe3};
            //MOVEQ  R0, R1
            //byte[] bytes = new byte[]{(byte) 0x01, (byte) 0x00, (byte) 0xa0, (byte) 0x01, 00, (byte) 0xf0, (byte) 0x20, (byte)0xe3};
            byte[] bytes = SemanticCheck.readByteFromString("\\x09\\x00\\x52\\xe1");
            Z3Solver.init();
//            //Load memory and initialize stack pointer
            //byte[] bytes = SemanticCheck.readByteFromString(args[0]);
            Environment env = new Environment();
//            env.register.set('6', new BitVec(Long.valueOf(1073741823L)));
//            env.register.set('9', new BitVec(Long.valueOf(195263897L)));
            env.register.set('2',  new BitVec(Long.valueOf( 2147483648L)));
            env.register.set('9',  new BitVec(Long.valueOf( 0)));
            Emulator emulator = new M0(env);
            Map.Entry<Environment, String> result = Executor.byteExecute(Variation.M0, bytes, env);
            String pc = (Executor.recentPop != null) ? Executor.recentPop.getKey().pathCondition: "";
            System.out.println(Z3Solver.getSMTFormula(pc, result.getKey()));
            System.out.println(result.getKey());

            //singleExec
        } catch (Exception e) {
            System.out.println(e);
        }
        long stopTime = System.nanoTime();
        System.out.println(stopTime - startTime);
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
    private static byte[] readByteFromString(String str) {
        byte[] byteArr = new byte[]{};

        String hexStr = str.replace("\\x", "");
        byteArr = hexStringToByteArray(hexStr);
        byte[] nop = new byte[]{00, (byte) 0xf0, (byte) 0x20, (byte)0xe3};
        int j = 0;
        byte[] res = new byte[byteArr.length + nop.length];
        for (int i = 0; i < byteArr.length; i++) {
            res[i] = byteArr[i];
        }
        for (int i = byteArr.length; i < byteArr.length + nop.length; i++) {
            res[i] = nop[j++];
        }
        return res;
    }
}