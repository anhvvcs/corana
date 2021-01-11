package external.jni;

import com.sun.jna.Library;
import com.sun.jna.Native;

public interface CStdio extends Library {
    CStdio INSTANCE = Native.load(("c"), CStdio.class);

    //printf (const char *__restrict __fmt, ...)
    void printf (String __restrict__fmt, Object... args);
}
