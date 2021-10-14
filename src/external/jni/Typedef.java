package external.jni;

import com.sun.jna.IntegerType;

public class Typedef {

    public class size_t extends IntegerType {
        public size_t() {
            super(32);
        }
    }

    public class clockid_t extends IntegerType {
        public clockid_t() {
            super(32);
        }
    }
}
