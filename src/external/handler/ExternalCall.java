package external.handler;

import emulator.base.Emulator;
import executor.BinParser;
import pojos.BitVec;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class ExternalCall {

    // statically linked
    public static boolean isExternalFucntion(String functionSym) {
        // add all function interfaces to the database
        // query function name
        List<String> list = Arrays.stream(APIStub.class.getMethods()).map(s -> s.getName()).collect(Collectors.toList());
            if (list.contains(findFunctionName(functionSym))) {
                return true;
            }
        return false;
    }

    public static String findFunctionName(String jmpAddress){
    // find Function name from jmpAddress
        List<String> prefixes = Arrays.asList("__libc");
        String fullSym = jmpAddress.replace("#0x","");

        while (fullSym.length() < 8) {
            fullSym = "0" + fullSym;
        }

        HashMap <String, String> tbl = BinParser.getSymbolTable();
        String res = "";
        if (tbl.containsKey(fullSym)) {
            res = tbl.get(fullSym);
        }
        return res;
    }

    public static String getStringParam(BitVec memoryValue) {
        return "";
    }

}
