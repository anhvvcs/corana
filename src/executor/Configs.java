package executor;

public class Configs {
    public static int RANDOM_SEED = 0;
    public static String smtFuncs = "smt-funcs";
    public static String tempZ3Script = "z3.smt";

    public static String neo4jUser = "neo4j";
    public static String neo4jPassword = "password";

    public static int architecture = Integer.SIZE;
    public static String topStack = "#xbefffc08"; // #xff0000000, befffc88
    public static String argc = "#x00000001";
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
