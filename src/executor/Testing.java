/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package executor;

import capstone.Capstone;
import enums.Variation;
import utils.Arithmetic;
import utils.Logs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

public class Testing {

    private static void exeFiles(File inpFile) throws Exception {
        if (inpFile.isDirectory()) {
            System.out.println("Directory: " + inpFile.getName());
            File[] files = inpFile.listFiles();
            for (File file : files) {
                exeFiles(file);
            }
        } else {
            String f = inpFile.getPath();
            String name = inpFile.getName();
            //Logs.initLog("./results/" + name + ".log");
            Corana.inpFile = name;
            //Corana.outFile = "./results/" + name;
            Executor.execute(Variation.M0, f);
            //Logs.endLog();
        }
    }

    public static void main(String[] args) {

        File dir = new File("./samples/arm_full/35a82cc5587b885699a703455542fb5f$");
        long startTime = System.nanoTime();

        try {
            exeFiles(dir);
        } catch (Exception e) {
            //System.out.println(e);
        }
        long stopTime = System.nanoTime();
        System.out.println(stopTime - startTime);
    }
}
