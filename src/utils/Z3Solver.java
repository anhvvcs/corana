package utils;

import emulator.semantics.Flags;
import executor.Configs;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class Z3Solver {

    private static String getPredefinedFunctions() {
        MyStr smtDeclarations = new MyStr();
        try {
            List<String> files = FileUtils.getResourceFiles(Configs.smtFuncs);
            for (String f : files) {
                StringBuilder sb = new StringBuilder();
                FileUtils.readResource(Configs.smtFuncs + "/" + f, s -> sb.append(s).append("\n"));
                smtDeclarations.append(sb);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return smtDeclarations.value();
    }

    public static String solveBitVecArithmetic(String artm) {
        String z3Clause = "(simplify " + artm + ")";

        try {
            FileUtils.write(Configs.tempZ3Script, z3Clause);
            String result = SysUtils.execCmd("z3 -smt2 " + Configs.tempZ3Script);
            FileUtils.delete(Configs.tempZ3Script);
            if (result != null) {
                if (result.contains("ERR0R") || result.contains("error")) {
                    return artm;
                } else {
                    return result.split("\n")[0];
                }
            }
        } catch (Exception e) {
//            System.err.println("Command length: "+ Configs.tempZ3Script.length());
        }
        return "ERROR";
    }

    /**
     * Declare needed variables and which value we need to obtain if SAT
     *
     * @param eval:   value need to evaluate if SAT
     * @param bvVars: input variables
     * @return
     */
    private static String declareEvalAndVars(String eval, ArrayList<String> bvVars, ArrayList<String> boolVars) {
        MyStr str = new MyStr();
        for (String v : bvVars) {
            str.append("(declare-const " + v + " (_ BitVec 32))\n");
        }
        for (String v : boolVars) {
            str.append("(declare-const " + v + " Bool)\n");
        }
        str.append("$mainAssert\n", "(check-sat)\n");
        str.append("(get-model)\n");
        if (eval != null) {
            str.append("(eval " + eval + ")", "\n");
        }
        return str.value();
    }

    /**
     * Check the satisfiability of a path constraint.
     *
     * @param pathConstrain: path constraints
     * @param eval:          optional. null if do not need to evaluate anything
     * @return raw result returned by Z3 if SAT, null if UNSAT
     */
    public static String checkSAT(String pathConstrain, String eval) {
        try {
            Logs.info("\t-> Checking path constrains by Z3", "... ", Logs.shorten(pathConstrain));

            ArrayList<String> bvVars = new ArrayList<>(Mapping.regStrToChar.keySet());
            bvVars.addAll(new ArrayList<>(Mapping.intToSymVariable.values()));

            ArrayList<String> boolVars = new ArrayList<>();
            Field[] fields = Flags.class.getFields();
            for (Field f : fields) {
                boolVars.add(f.getName().toLowerCase());
            }

            String declaration = declareEvalAndVars(eval, bvVars, boolVars);
            String finalConstraint = pathConstrain.equals("") ? "" : "(assert " + pathConstrain + ")";
            String z3Clause = getPredefinedFunctions() + declaration.replace("$mainAssert", finalConstraint);
            FileUtils.write(Configs.tempZ3Script, z3Clause);
            String result = SysUtils.execCmd("z3 -smt2 " + Configs.tempZ3Script);
            FileUtils.delete(Configs.tempZ3Script);
            if (result != null) {
                if (result.split("\n")[0].equalsIgnoreCase("sat")) {
                    Logs.infoLn("SAT");
                    return result;
                }
            }
            Logs.infoLn("UNSAT");

        } catch (Exception e) {
            System.out.println(e.getStackTrace());
            System.out.println(pathConstrain);
        }
        return null;
    }

}
