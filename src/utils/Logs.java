package utils;

public class Logs {
    public static void infoLn(Object o) {
        info(o);
        info("\n");
    }

    public static void infoLn(Object... os) {
        if (os.length == 0) {
            infoLn("");
        }
        for (Object o : os) {
            infoLn(o);
        }
    }

    public static void info(Object o) {
        System.out.print(toStr(o));
    }

    public static void info(Object... os) {
        for (int i = 0; i < os.length; i++) {
            info(os[i]);
            if (i < os.length - 1) {
                info(" ");
            }
        }
    }

    private static String toStr(Object o) {
        return o == null ? "null" : o.toString();
    }

    public static String shorten(String s) {
        return s.length() > 100 ? s.substring(0, 100) : s;
    }
}