package executor;

import capstone.Capstone;

public class Configs {
    public static int RANDOM_SEED = 0;
    public static String smtFuncs = "smt-funcs";

    public static int instructionSize = 4;
    public static int executionMode = Capstone.CS_MODE_ARM;
    public static String neo4jUser = "neo4j";
    public static String neo4jPassword = "password";

    public static int architecture = Integer.SIZE;
    public static String topStack =  "sp_SYM"; //"#xbefffc08" #xff0000000, befffc88
    public static String argc = "r0_SYM"; //"#x00000001";
    public static int envVarCount = 17; // default in GCC
    public static int wordSize = architecture / 8;

    public static byte getCharSize() {
        return 1;
    }

    public static byte getShortSize() {
        return 2;
    }

    public static byte getIntSize() {
        return 4;
    }

    public static byte getLongSize() {
        return (byte) ((byte) architecture / 8);
    }
}
