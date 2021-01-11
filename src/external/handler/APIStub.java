package external.handler;

import emulator.semantics.Environment;
import external.jni.CStdio;
import pojos.BitVec;

public class APIStub {
    public static void printf(Environment env) {
        // number of params, type of each params, name

        // Get value from registers and stacks
        BitVec t0 = env.register.get('0');

        // Set parameters
        String param0 = env.memory.getText(t0);

        // Call API function
        CStdio.INSTANCE.printf(param0);

        // Update memory and r0 register
    }

}
