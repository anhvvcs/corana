# Java binding as Maven artifact for Capstone disassembly framework

This is a fork of the excellent original [Capstone disassembly framework](https://github.com/aquynh/capstone). The only difference is that the Java binding library was transformed into a Maven project. The resulting Maven artifact is available via the usual Maven repository.

## Release notes
### 3.0.5-rc2

* This is the initial compatible Java binding version distributed as Maven artifact

## Roadmap

There are no plans for further modifications on this fork (except the conversion of the Java binding code into a Maven project). Every time when the original Capstone project ([aquynh/capstone](https://github.com/aquynh/capstone)) provides a new release then this code (inclusive Java binding) will be forked again, modified for Maven and distributed as Maven artifact for public use.

There's an open pull request on the original project which is intended for the transformation of the existing Java binding into a Maven project. Unfortunately, the progress came to a standstill. If you have ideas for moving it forward, feel free to participate in:

* [Converted Java binding into Maven project](https://github.com/aquynh/capstone/pull/609)
* [change makefile for Maven construct package](https://github.com/aquynh/capstone/pull/678)

## Issue tracking

Bugs, feature requests or pull requests should be reported via Github's issue tracking system. We only accept requests that concern the conversion to Maven project of the Java binding part. Issues in Capstone itself or in the Java binding code should be reported at the [original project site](https://github.com/aquynh/capstone/issues).

## Contributions

If you would like to help us please contact us via issue tracking or email.

## License & Author

The license and author is the same as in the original project. We ([TRANScurity](http://transcurity.co/)) only modify the structure of Java binding code in order to make it compatible with Maven.

# Developer Guide
## Getting started

You can import the artifact by:

```xml
<dependency>
    <groupId>com.github.transcurity</groupId>
    <artifactId>capstone</artifactId>
    <version>W.X.Y-rcZ</version>
</dependency>
```

Furthermore, you need to obtain the native binaries from <http://www.capstone-engine.org/> or from the releases page: <https://github.com/aquynh/capstone/releases>

Please note that the provided command line based test classes from the original project of Java binding are not intact anymore, because the binding code classes were moved in order to create a Maven compatible structure.

## Troubleshooting
### The Java wrapper cannot find the natives and aborts with: ``UnsatisfiedLinkError``

If you get following Exception:

```
...\Disassembler\target>java -jar disassembler-1.0-SNAPSHOT.jar
Exception in thread "main" java.lang.UnsatisfiedLinkError: Unable to load library 'capstone': Native library (win32-x86-64/capstone.dll) not found in resource path ([file:/.../Disassembler/target/disassembler-1.0-SNAPSHOT.jar])
        at com.sun.jna.NativeLibrary.loadLibrary(NativeLibrary.java:303)
        at com.sun.jna.NativeLibrary.getInstance(NativeLibrary.java:427)
        at com.sun.jna.Library$Handler.<init>(Library.java:179)
        at com.sun.jna.Native.loadLibrary(Native.java:569)
        at com.sun.jna.Native.loadLibrary(Native.java:544)
        at capstone.Capstone.<init>(Capstone.java:372)
        at co.transcurity.disassembler.Main.main(Main.java:157)
```

...it may give the impression that ``capstone.dll`` cannot be located. First, make sure that your ``capstone.dll`` is located in the same directory just like your JAR file or that it is installed in any other lookup path (e. g. ``C:\Windows\System32`` or respectively ``C:\Windows\SysWOW64`` for 32 bit JVM) examined by Java. You can also modify the lookup path on start up:

```
java -Djava.library.path=C:\my\lookup\path\folder -jar disassembler-1.0-SNAPSHOT.jar
```

Another way to advise the directory location of ``capstone.dll`` is to set a special property of JNA library:

```
java -Djna.library.path=./lib/natives -jar disassembler-1.0-SNAPSHOT.jar
```

This error also occurs if you try to execute a 64 bit JVM with a 32 bit ``capstone.dll``. You cannot mix 32 bit and 64 bit architectures.

If Capstone is accessible for the JVM and has a matching architecture but this error still occurs then indeed there may be further DLLs missing that are imported by Capstone. One of these DLLs is ``vcruntime140.dll``. Usually, it comes with the installation of Visual Studio but you could also obtain it anywhere else and put it near ``capstone.dll``. [Dependency Walker](http://www.dependencywalker.com/) is a tool that shows you which dependent DLL in ``capstone.dll`` cannot be found.

https://github.com/adam-p/markdown-here/wiki/Markdown-Cheatsheet
