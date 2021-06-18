/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package executor;

import capstone.Capstone;
import enums.Variation;

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
//            Memory.loadMemory(f, name);
            Executor.execute(Variation.M0, f);
            //Logs.endLog();
        }
    }

    public static void main(String[] args) throws FileNotFoundException {
        //37c81e56604c3c55dc652bddbce5229d - mirai
        //d154a62c7926d98f78b974253c03f77c // arbiter
        //e24443e10da03abfbb6c355515400953 // snoopy

        //372a36dd19d9772efa60bf469c0a7dba // atxhua
        //f13ef9b00d619f8efcfc9859d0448f11 // hajime
        //f4d371707213aebf4bb9df9197906b57 // mirai
        //f57db6105503b09210559eb98dbc2cc6 // gafgyt
        //f543b5bdc58aa9e8fc9dea5ddbc8e8aa // gemini or mirai
        //f2341ed0ad08b3dee3edcc23483c2fad // mirai
        //f72879fe3176fd797fc1febd4a71f39c // gafgyt.vfmnn
        //f2341ed0ad08b3dee3edcc23483c2fad // mirai
        //f6680126509ce6adab0cf532979ee228
        //fd99d3ef65a4caeedf3f88b048851f0d // miori
        //fd0117ae47a7a8d39519b2aa12b8bbe4 // echobot
        //7b0dcb1e8e84149cc2343de1e83b1de2 // mirai
        //7c67c942e5d1609715f3ca18a053e887 // mirai
        //7d88b9f58e41c030b490501a6626ff67 // mirai kurco
        //7d9223a90c7efbeca41085fbe53e1f1c // mirai
        //72f2d7f12ab091afca9d53035ed195e8 // gafgyt
        //77e059e31858420b9ed1373e2e425ab6 // miori - mirai
        //89bfbc8232ff375a98733322eeb4d7cc // yowai - mirai
        //8dea1949cef646176685153203cc289e // variant of mirai
        //c10c502e115e615609caa3ee4011841d // corona.arm6 - gafgyt variant
        //8617fef86bed2663e0fa55d5247af9a1

        //3466bc7438faa880bd82cc8faabd461c$
        //341febc5fa4ce975aa0bedc7c33ad132$
        //3416bcf55e5abd43476f05dd254c1a92$
        //3451e130d74c8b8f7243bdba8dcfaa5e$
        //3410541c08d378224661f5d5dafdf8dd$
        //34a08507e081522291dcda73d828b614$
        //34a26407025b063a3e81c5bc1a95fdef$

        //f8b512d02c2f17dcd1a58e5ca6dc1c37$

        //File dir = new File("samples/32caf/32caff26a4dfa373cd0ed869544a30b7$");
        File dir = new File("samples/fork");
        long startTime = System.nanoTime();

        try {
            exeFiles(dir);
        }
        catch (Exception e) {
            System.out.println(e);
        }
        long stopTime = System.nanoTime();
        System.out.println(stopTime - startTime);
    }
}
