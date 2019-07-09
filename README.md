# Corana

**Corana** is an on-going project providing a Dynamic Symbolic Execution tool for ARM Cortex-M. It takes an ARM binary file as the input and outputs its precise Control Flow Graph (CFG) under the presence of obfuscations like indirect jumps. Since it is currently a preliminary version and still being regularly improved, bugs may occur. The number of supported instruction is also limited.

## Installation

**Important note:** This installation is for Linux/MacOS only. For Windows, please take a look at the detailed instruction of each individual component.

### Java and File 
**Corana** is written entirely in Java. Thus, make sure that you already installed Java (version 1.8+). In addition, **Corana** use `file` command to check the format of an input binary file. In fact, `file` is already installed by default in Linux/MacOS, but for Windows, please install it first.

### Capstone Engine
**Corana** utilizes Capstone as a single-step disassembler engine. It is worth noting that, we use the older version cloned by TRANScurity instead of the latest one since the maven library used in **Corana** is not compatible with newer releases. Please follow the installation below:
* Clone the Github repository: https://github.com/TRANScurity/capstone (or using the Capstone repository included in /libs)
* Build: `cd capstone; ./make.sh; sudo ./make.sh install`
    
### Z3 Solver
**Corana** uses Z3 as a back-end SMT Solver to check the satisfiability of path constraints. Z3 can be installed either from source code or command line.
* Using command line: `sudo apt-get update -y; sudo apt-get install -y z3`
* From source code:  Please clone Z3 repository https://github.com/Z3Prover/z3 (or using the Z3 repository included in /libs) and follow its instruction.

### Build Corana
We provide a pre-built **Corana** as a `.jar` file. However, you can still re-build it by simply creating a new artifact from sources. After successfully building, make sure that `corana.jar` is successfully generated.

## Execution 
**Corana** inputs an ARM binary file and outputs its CFG. The CFG is represented as `.dot` file, thus you can arbitrarily further plot it in any graphic or data structure format as you want. Use this command to execute **Corana**:

     java -Xss16m -Xmx10240m -jar corana.jar -execute /path/to/input/file

where

 - `-Xss`: the maximum memory allocated for stack size. We recommend to set it around 16MB or larger.
 - `-Xmx`: the maximum memory allocated for the execution. We recommend to set it as much as possible since the dynamic symbolic execution consumes a lot of memory.
 - `/path/to/input/file`: the path to the ARM binary file for analyzing.

If you want to specify an ARM variation, please append the variation name  (M0, M0_Plus, M3, M4, M7, M33) to the end of the command above. Otherwise, **Corana** runs with the general ARM configurations.

     java -Xss16m -Xmx10240m -jar corana.jar -execute /path/to/input/file M7

## Contact
Anh V. Vu - Project maintainer - [Email](mailto:anhvvcs@gmail.com)

## License
This project is licensed under the [MIT License](http://www.opensource.org/licenses/mit-license.php).

## Acknowledgments

We thank JAIST for financially supporting our project and thank [Jan Willem Janssen](https://www.lxtreme.nl/) for his useful library for effectively parsing ELF binary file: https://github.com/jawi/java-binutils

