package external.handler;

import emulator.base.Emulator;
import pojos.BitVec;

public class ExternalCall {

    // statically linked
    public static boolean isExternalFucntion(String functionSym) {
        // add all function interfaces to the database
        // query function name
        // TODO;
        return true;
    }

    public static String findFunctionName(String jmpAddress){
    // find Function name from jmpAddress
        String tmp = "printf"; // e.g.;
        if (jmpAddress.equals("#0x207e0")) {
            tmp = "gettimeofday";
        } else if (jmpAddress.equals("#0x14fb0")) {
            tmp = "printf";
        }
        return tmp;
    }

    public static String getStringParam(BitVec memoryValue) {
        return "";
    }

}
