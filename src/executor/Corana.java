package executor;

import enums.Variation;
import utils.Logs;

public class Corana {

    static String inpFile;

    public static void main(String[] args) {
        String missingArgsMsg = "-> Missing arguments. Please check the document.";
        if (args.length == 0) {
            Logs.infoLn(missingArgsMsg);
        } else {
            String type = args[0];
            if ("-execute".equals(type)) {
                if (args.length < 2) {
                    Logs.infoLn(missingArgsMsg);
                } else {
                    inpFile = args[1];
                    String variation = (args.length == 3) ? args[2] : "M0";
                    Executor.execute(Variation.valueOf(variation), inpFile);
                }
            } else {
                Logs.infoLn("-> Wrong argument. Please check the document");
            }
        }
    }
}