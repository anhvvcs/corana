package external.handler;

import emulator.semantics.Environment;
import executor.Configs;
import external.jni.CStdio;
import external.jni.CTime;
import pojos.BitVec;
import utils.SysUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class APIStub {
    public static void printf(Environment env) {
        // number of params, type of each params, name
        // Get value from registers and stacks
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');
        BitVec t3 = env.register.get('3');

        // Set parameters
        String param0 = env.memory.getTextFromReference(t0) + "\n";

        List<Character> fmts = SysUtils.getPrintfFormat(param0);
        List<BitVec> regs = Arrays.asList(t1, t2, t3);

        List<Object> params = new ArrayList<>();

        for (Character f : fmts) {
            switch (f) {
                case 's':
                    String strpr = env.memory.getTextFromReference(regs.get(fmts.indexOf(f)));
                    params.add(strpr);
                    break;
                case 'd':
                    Integer intpr = env.memory.getInt(regs.get(fmts.indexOf(f)));
                    params.add(intpr);
                    break;
                case 'l':
                    Long lpr = env.memory.getLong(regs.get(fmts.indexOf(f)));
                    params.add(lpr);
                    break;
                default:
                    Integer pr = env.memory.getInt(regs.get(fmts.indexOf(f)));
                    params.add(pr);
                    break;
            }
        }
        // Call API function
        CStdio.INSTANCE.printf(param0, params.toArray());
        // Update memory and r0 register
    }

    //int gettimeofday(struct timeval *tv, struct timezone *tz);
    public static void gettimeofday(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        CTime.timeval.ByReference param0 = new CTime.timeval.ByReference();
        CTime.timezone.ByReference param1 = new CTime.timezone.ByReference();

        param0.tv_sec = (long) env.memory.getLongFromReference(t0);
        param0.tv_usec = (long) env.memory.getLongFromReference(t0.add(Configs.getLongSize()));

        param1.tz_dsttime = (int) env.memory.getIntFromReference(t1);
        param1.tz_minuteswest = (int) env.memory.getIntFromReference(t1.add(Configs.getIntSize()));

        int ret = CTime.INSTANCE.gettimeofday(param0, param1);

        // Update memory and r0 register
        env.register.set('0', new BitVec(ret));
        env.memory.put(t0, new BitVec(param0.tv_sec));
        env.memory.put(t0.add(Configs.getLongSize()), new BitVec(param0.tv_usec));
        env.memory.put(t1, new BitVec(param1.tz_dsttime));
        env.memory.put(t1.add(Configs.getLongSize()), new BitVec(param1.tz_minuteswest));
    }

    //int clock_gettime(clockid_t clockid, struct timespec *tp);
    public static void clock_gettime(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0 = env.memory.getIntFromReference(t0);
        CTime.timespec param1 = new CTime.timespec();

        param1.tv_nsec = env.memory.getLongFromReference(t1);
        param1.tv_sec = env.memory.getLongFromReference(t1.add(Configs.getLongSize()));

        int ret = CTime.INSTANCE.clock_gettime(param0, param1);

        env.register.set('0', new BitVec(ret));
        env.memory.put(t0, new BitVec(param0));
        env.memory.put(t1, new BitVec(param1.tv_nsec));
        env.memory.put(t1, new BitVec(param1.tv_sec));
    }


}
