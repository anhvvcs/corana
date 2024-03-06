# Corana

**Corana** is a Dynamic Symbolic Execution tool for ARM Cortex-M. It takes an ARM binary file and outputs its Control Flow Graph (CFG), handling various obfuscations such as indirect jumps and opaque predicates. It is an ongoing project, with more variants added regularly. Bugs may occur, and the number of supported instructions is still limited.

## Installation

This instruction is for Linux/MacOS only. For Windows, please refer to detailed installation instructions for each individual component.

### Java and File 
**Corana** is written in Java. Make sure that you have Java installed (v1.8+). **Corana** use `file` to check the binary file format, which should be installed by default in Linux/MacOS, but for Windows, please double check.

**Corana** is written in Java. Make sure that you have Java installed (v1.8+). **Corana** uses `file` to check the binary file format, which should be installed by default on Linux/MacOS. For Windows, please double-check.

### Capstone Engine

**Corana** utilises Capstone as a single-step disassembler engine. It uses an older cloned version by TRANScurity instead of the latest one, as the Maven library used in Corana is incompatible with newer releases. Please follow the installation steps below:

* Clone the Github repository: https://github.com/TRANScurity/capstone (or using the Capstone repository included in /libs)
* Build: `cd capstone; ./make.sh; sudo ./make.sh install`

### Z3 Solver
**Corana** uses Z3 as a back-end SMT Solver for checking the satisfiability of path constraints. Z3 can be installed either:
* Using the command line: `sudo apt-get update -y; sudo apt-get install -y z3`
* From source code: Please clone Z3 repository https://github.com/Z3Prover/z3 (or using the Z3 repository included in /libs) and follow its instruction.

### Build Corana
A pre-built **Corana** is available as a `.jar` file. However, you can still rebuild it by simply creating a new artifact from sources. After building, make sure that `corana.jar` is successfully generated.

## Execution 
**Corana** inputs an ARM binary file and outputs its CFG. The CFG is represented as `.dot` file, which can be further ploted it in any graphic or data structure format. To execute **Corana**:

     java -Xss16m -Xmx10240m -jar corana.jar -execute /path/to/input/file

where

 - `-Xss`: the maximum memory allocated for stack size. We recommend setting it to 16MB or larger.
 - `-Xmx`: the maximum memory allocated for the execution. We recommend setting it as much as possible since the DSE is menory-intensive.
 - `/path/to/input/file`: the path to the ARM binary file for analyzing.

If you want to specify an ARM variation, please append the variation name  (M0, M0_Plus, M3, M4, M7, M33) to the end of the command above. Otherwise, **Corana** runs with the general ARM configurations.

     java -Xss16m -Xmx10240m -jar corana.jar -execute /path/to/input/file M7

## Contact
Anh V. Vu - Project maintainer - [Email](mailto:anhvvcs@gmail.com)

Nguyen Thi Van Anh - Project maintainer - [Email](mailto:vananhnt97@gmail.com)

## References

Please cite our papers if you use (part of) our tool for analysis. 

    @inproceedings{vu2019formal,
        title = {{Formal Semantics Extraction from Natural Language Specifications for ARM}},
        author = {Vu, Anh V. and Ogawa, Mizuhito},
        booktitle = {Proceedings of the International Symposium on Formal Methods (FM)},
        year = {2019}
    }
    @inproceedings{nguyen2022automatic,
        title={{Automatic Stub Generation for Dynamic Symbolic Execution of ARM Binary}},
        author={Nguyen, Anh T.V. and Ogawa, Mizuhito},
        booktitle={Proceedings of the International Symposium on Information and Communication Technology (SoICT)},
        year={2022}
    }

## Acknowledgments

We thank JAIST for financially supporting the project and [Jan Willem Janssen](https://www.lxtreme.nl/) for his useful [library](https://github.com/jawi/java-binutils) for effectively parsing ELF binary files.

## License
This project is licensed under the [MIT License](http://www.opensource.org/licenses/mit-license.php).
