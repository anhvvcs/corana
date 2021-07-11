package utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;

public class Logs {
    private static String output = "./output.out";
    private static FileWriter fileWriter;
    private static int processNo = 0;

    public static void logFile(String name) {
        output = name;
//        try {
//            fileWriter = new FileWriter(output);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    public static void forkLog() {
//        try {
//            fileWriter = new FileWriter(output + "_fork" + toStr(processNo));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
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
//        try {
//            fileWriter.write(toStr(o));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
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
//        try {
//            fileWriter.flush();
//            fileWriter.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    private static String toStr(Object o) {
        return o == null ? "null" : o.toString();
    }

    public static String shorten(String s) {
        return s.length() > 100 ? s.substring(s.length()-80, s.length()) : s;
    }
}