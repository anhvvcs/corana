package external.handler;

import emulator.base.Emulator;
import executor.BinParser;
import pojos.BitVec;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ExternalCall {

    // statically linked
    public static boolean isExternalFucntion(String functionSym) {
        // add all function interfaces to the database
        // query function name
        List<String> apilist = new ArrayList<>();
        try {
            List<String> list = Files.readAllLines(new File("./resources/external_funcs").toPath(), Charset.defaultCharset());
            if (list.contains(findFunctionName(functionSym))) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String findFunctionName(String jmpAddress){
    // find Function name from jmpAddress
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
