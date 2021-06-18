package executor;

import emulator.cortex.*;
import emulator.semantics.EnvModel;
import emulator.semantics.Environment;
import enums.Variation;
import utils.Arithmetic;
import utils.Logs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

public class Process implements Runnable {
    Variation variation;
    Environment env;
    String genesis;
    String startAddress;

    public Process(Variation v, Environment e, String label, String addr) {
        variation = v;
        env = e;
        genesis = label;
        startAddress = addr;
    }

    public void run() {
        //Instantiating the File class
        File file = new File("./stream_"+Arithmetic.intToHex(Integer.valueOf(startAddress))+".txt");
        //Instantiating the PrintStream class
        try {
            while (! Thread.interrupted()) {
                Thread.sleep(10);
                PrintStream stream = new PrintStream(file);
                System.setOut(stream);
                // TODO: Start from asmNodes.get(_start), not the first node
                if (variation == Variation.M0) {
                    Executor.execFrom(new M0(env), genesis, startAddress);
                    //execFrom(new M0(env), genesis.label, String.valueOf(Arithmetic.hexToInt("0000e854")));
                } else if (variation == Variation.M0_PLUS) {
                    Executor.execFrom(new M0_Plus(env), genesis, startAddress);
                } else if (variation == Variation.M3) {
                    Executor.execFrom(new M3(env), genesis, startAddress);
                } else if (variation == Variation.M4) {
                    Executor.execFrom(new M4(env), genesis, startAddress);
                } else if (variation == Variation.M7) {
                    Executor.execFrom(new M7(env), genesis, startAddress);
                } else if (variation == Variation.M33) {
                    Executor.execFrom(new M33(env), genesis, startAddress);
                } else {
                    Logs.infoLn("-> Unsupported ARM Variation.");
                    return;
                }
                Executor.gg();
            }
        } catch (Exception e) {
            Executor.gg();
        }
    }
}
