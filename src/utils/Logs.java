package utils;

import java.io.FileWriter;
import java.io.IOException;

public class Logs {
    private static String output = "null.out";
    private static FileWriter fileWriter;
    public static void logFile(String name) {
        output = name;
        try {
            fileWriter = new FileWriter(output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
        try {
            fileWriter.write(toStr(o));
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public static void closeLog() {
        try {
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String toStr(Object o) {
        return o == null ? "null" : o.toString();
    }

    public static String shorten(String s) {
        return s.length() > 100 ? s.substring(0, 100) : s;
    }
}