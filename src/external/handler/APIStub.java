package external.handler;

import com.sun.jna.*;
import com.sun.jna.ptr.*;
import emulator.semantics.Environment;
import emulator.semantics.Memory;
import external.jni.CStruct.*;
import external.jni.*;
import pojos.BitVec;
import utils.SysUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class APIStub {

    public static void __clear_cache(Environment env) {

    }

    //int connect(int __fd,sockaddr *__addr,socklen_t __len)
    public static void connect(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        int param0;
        sockaddr param1 = new sockaddr();
        int param2;

        param0 = env.memory.getIntFromReference(t0);
        param1.sa_ = env.memory.getIntFromReference(t1);
        param1.sa_data = env.memory.getIntArray(t1.add(4), 14);
        param2 = env.memory.getIntFromReference(t2);

        int ret = CLibrary.INSTANCE.connect(param0, param1, param2);
        //int ret = 0;
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t1, param1.sa_);
        env.memory.setIntArray(t1.add(4), 14, param1.sa_data);
    }

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
        CLibrary.INSTANCE.printf(param0, params.toArray());
        // Update memory and r0 register
    }

    public static void __errno_location(Environment env) {
        BitVec t0 = env.register.get('0');
        IntByReference ret = CLibrary.INSTANCE.__errno_location();
        //env.memory.setInt(t0, ret.getValue());
    }

    //int gettimeofday(struct timeval tv, struct timezone tz);
//    public static void gettimeofday(Environment env) {
//        BitVec t0 = env.register.get('0');
//        BitVec t1 = env.register.get('1');
//
//        timeval param0 = new timeval();
//        timezone param1 = new timezone();
//
//        param0.tv_sec = (int) env.memory.getIntRef(t0);
//        param0.tv_usec = (long) env.memory.getLongFromReference(t0.add(Configs.getLongSize()));
//
//        param1.tz_dsttime = (int) env.memory.getIntRef(t1);
//        param1.tz_minuteswest = (int) env.memory.getIntRef(t1.add(Configs.getIntSize()));
//
//        int ret = CTime.gettimeofday(param0, param1);
//
//        // Update memory and r0 register
//        env.register.set('0', new BitVec(ret));
//        env.memory.put(t0, new BitVec(param0.tv_sec));
//        env.memory.put(t0.add(Configs.getLongSize()), new BitVec(param0.tv_usec));
//        env.memory.put(t1, new BitVec(param1.tz_dsttime));
//        env.memory.put(t1.add(Configs.getLongSize()), new BitVec(param1.tz_minuteswest));
//    }
//
//    //int clock_gettime(clockid_t clockid, struct timespec tp);
//    public static void clock_gettime(Environment env) {
//        BitVec t0 = env.register.get('0');
//        BitVec t1 = env.register.get('1');
//
//        int param0;
//        timespec param1 = new timespec();
//
//        param0 = env.memory.getIntRef(t0);
//        param1.tv_nsec = env.memory.getLongFromReference(t1);
//        param1.tv_sec = env.memory.getLongFromReference(t1.add(Configs.getLongSize()));
//
//        int ret = CTime.clock_gettime(param0, param1);
//
//        env.register.set('0', new BitVec(ret));
//        env.memory.put(t0, new BitVec(param0));
//        env.memory.put(t1, new BitVec(param1.tv_nsec));
//        env.memory.put(t1, new BitVec(param1.tv_sec));
//    }
//
//    public static void sigemptyset (Environment env) throws Throwable {
//        BitVec t0 = env.register.get('0');
//
//        LongByReference param0 ;
//
//        param0 = env.memory.getPointer(t0);
//
//        int ret = CSignal.sigemptyset(param0);
//        env.register.set('0', new BitVec(ret));
//
//        env.memory.put(t0, new BitVec(param0.getValue()));
//        System.out.println(param0.getValue());
//    }
    public static void _exit(Environment env) {
        BitVec t0 = env.register.get('0');
        int param0;
        param0 = env.memory.getInt(t0);

        CLibrary.INSTANCE._exit(param0);
        //env.memory.setInt(t0, param0);
    }

    public static void _Exit(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);
        CLibrary.INSTANCE._Exit(param0);

        //env.memory.setInt(t0, param0);
    }

    public static void _flushlbf(Environment env) {

        CLibrary.INSTANCE._flushlbf();

    }

    public static void __fbufsize(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        int ret = CLibrary.INSTANCE.__fbufsize(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void __flbf(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        int ret = CLibrary.INSTANCE.__flbf(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void __fpending(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        int ret = CLibrary.INSTANCE.__fpending(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void __fpurge(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        CLibrary.INSTANCE.__fpurge(param0);

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void __freadable(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        int ret = CLibrary.INSTANCE.__freadable(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void __freading(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        int ret = CLibrary.INSTANCE.__freading(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void __fsetlocking(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        IntByReference param0;
        int param1;

        param0 = env.memory.getIntRef(t0);
        param1 = env.memory.getInt(t1);

        int ret = CLibrary.INSTANCE.__fsetlocking(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
        ////env.memory.setInt(t1, param1);
    }

    public static void __fwritable(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        int ret = CLibrary.INSTANCE.__fwritable(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void __fwriting(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        int ret = CLibrary.INSTANCE.__fwriting(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void __ppc_get_timebase(Environment env) {

        long ret = CLibrary.INSTANCE.__ppc_get_timebase();
        env.register.set('0', new BitVec(ret));

    }

    public static void __ppc_get_timebase_freq(Environment env) {

        long ret = CLibrary.INSTANCE.__ppc_get_timebase_freq();
        env.register.set('0', new BitVec(ret));

    }

    public static void __ppc_mdoio(Environment env) {

        CLibrary.INSTANCE.__ppc_mdoio();

    }

    public static void __ppc_mdoom(Environment env) {

        CLibrary.INSTANCE.__ppc_mdoom();

    }

    public static void __ppc_set_ppr_low(Environment env) {

        CLibrary.INSTANCE.__ppc_set_ppr_low();

    }

    public static void __ppc_set_ppr_med(Environment env) {

        CLibrary.INSTANCE.__ppc_set_ppr_med();

    }

    public static void __ppc_set_ppr_med_high(Environment env) {

        CLibrary.INSTANCE.__ppc_set_ppr_med_high();

    }

    public static void __ppc_set_ppr_med_low(Environment env) {

        CLibrary.INSTANCE.__ppc_set_ppr_med_low();

    }

    public static void __ppc_set_ppr_very_low(Environment env) {

        CLibrary.INSTANCE.__ppc_set_ppr_very_low();

    }

    public static void __ppc_yield(Environment env) {

        CLibrary.INSTANCE.__ppc_yield();

    }

    public static void a64l(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        NativeLong ret = CLibrary.INSTANCE.a64l(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void abort(Environment env) {

        CLibrary.INSTANCE.abort();

    }

    public static void abs(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.abs(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void accept(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        int param0;
        sockaddr param1 = new sockaddr();
        IntByReference param2;

        param0 = env.memory.getInt(t0);
        param1.sa_ = env.memory.getInt(t1);
        param1.sa_data = env.memory.getIntArray(t1.add(4), 14);
        param2 = env.memory.getIntRef(t2);

        int ret = CLibrary.INSTANCE.accept(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        env.memory.setIntReference(t1, param1.sa_);
        env.memory.setIntArray(t1.add(4), 14, param1.sa_data);
        env.memory.setIntReference(t2, param2.getValue());
    }

    public static void access(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        int param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getInt(t1);

        int ret = CLibrary.INSTANCE.access(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void acos(Environment env) {
        BitVec t0 = env.register.get('0');

        double param0;

        param0 = env.memory.getDouble(t0);

        double ret = CLibrary.INSTANCE.acos(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setDouble(t0, param0);
    }

    public static void acosf(Environment env) {
        BitVec t0 = env.register.get('0');

        float param0;

        param0 = env.memory.getFloat(t0);

        float ret = CLibrary.INSTANCE.acosf(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setFloat(t0, param0);
    }

    public static void acosh(Environment env) {
        BitVec t0 = env.register.get('0');

        double param0;

        param0 = env.memory.getDouble(t0);

        double ret = CLibrary.INSTANCE.acosh(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setDouble(t0, param0);
    }

    public static void acoshf(Environment env) {
        BitVec t0 = env.register.get('0');

        float param0;

        param0 = env.memory.getFloat(t0);

        float ret = CLibrary.INSTANCE.acoshf(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setFloat(t0, param0);
    }

    public static void acoshl(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        double ret = CLibrary.INSTANCE.acoshl(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
    }

    public static void acosl(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        double ret = CLibrary.INSTANCE.acosl(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
    }

    public static void addmntent(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        IntByReference param0;
        mntent param1 = new mntent();

        param0 = env.memory.getIntRef(t0);
        param1.mnt_fsname = env.memory.getStringFromReference(t1);
        param1.mnt_dir = env.memory.getStringFromReference(t1.add(4));
        param1.mnt_type = env.memory.getStringFromReference(t1.add(8));
        param1.mnt_opts = env.memory.getStringFromReference(t1.add(12));
        param1.mnt_freq = env.memory.getIntFromReference(t1.add(16));
        param1.mnt_passno = env.memory.getIntFromReference(t1.add(20));

        int ret = CLibrary.INSTANCE.addmntent(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
        env.memory.setStringReference(t1, param1.mnt_fsname);
        env.memory.setStringReference(t1.add(4), param1.mnt_dir);
        env.memory.setStringReference(t1.add(8), param1.mnt_type);
        env.memory.setStringReference(t1.add(12), param1.mnt_opts);
        env.memory.setIntReference(t1.add(16), param1.mnt_freq);
        env.memory.setIntReference(t1.add(20), param1.mnt_passno);
    }

    public static void addseverity(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        String param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getTextFromReference(t1);

        int ret = CLibrary.INSTANCE.addseverity(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        env.memory.setTextReference(t1, param1);
    }

    public static void adjtime(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        timeval param0 = new timeval();
        timeval param1 = new timeval();

        param0.tv_sec = env.memory.getIntFromReference(t0);
        param0.tv_usec = env.memory.getNativeLongFromReference(t0.add(4));
        param1.tv_sec = env.memory.getIntFromReference(t1);
        param1.tv_usec = env.memory.getNativeLongFromReference(t1.add(4));

        int ret = CLibrary.INSTANCE.adjtime(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.tv_sec);
        env.memory.setNativeLongReference(t0.add(4), param0.tv_usec);
        env.memory.setIntReference(t1, param1.tv_sec);
        env.memory.setNativeLongReference(t1.add(4), param1.tv_usec);
    }

    public static void adjtimex(Environment env) {
        BitVec t0 = env.register.get('0');

        timex param0 = new timex();

        param0.modes = env.memory.getIntFromReference(t0);
        param0.offset = env.memory.getNativeLongFromReference(t0.add(4));
        param0.freq = env.memory.getNativeLongFromReference(t0.add(8));
        param0.maxerror = env.memory.getNativeLongFromReference(t0.add(12));
        param0.esterror = env.memory.getNativeLongFromReference(t0.add(16));
        param0.status = env.memory.getIntFromReference(t0.add(20));
        param0.constant = env.memory.getNativeLongFromReference(t0.add(24));
        param0.precision = env.memory.getNativeLongFromReference(t0.add(28));
        param0.tolerance = env.memory.getNativeLongFromReference(t0.add(32));
        param0.time = env.memory.getIntFromReference(t0.add(36));
        param0.tick = env.memory.getNativeLongFromReference(t0.add(40));
        param0.ppsfreq = env.memory.getNativeLongFromReference(t0.add(44));
        param0.jitter = env.memory.getNativeLongFromReference(t0.add(48));
        param0.shift = env.memory.getIntFromReference(t0.add(52));
        param0.stabil = env.memory.getNativeLongFromReference(t0.add(56));
        param0.jitcnt = env.memory.getNativeLongFromReference(t0.add(60));
        param0.calcnt = env.memory.getNativeLongFromReference(t0.add(64));
        param0.errcnt = env.memory.getNativeLongFromReference(t0.add(68));
        param0.stbcnt = env.memory.getNativeLongFromReference(t0.add(72));
        param0.tai = env.memory.getIntFromReference(t0.add(76));

        int ret = CLibrary.INSTANCE.adjtimex(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.modes);
        env.memory.setNativeLongReference(t0.add(4), param0.offset);
        env.memory.setNativeLongReference(t0.add(8), param0.freq);
        env.memory.setNativeLongReference(t0.add(12), param0.maxerror);
        env.memory.setNativeLongReference(t0.add(16), param0.esterror);
        env.memory.setIntReference(t0.add(20), param0.status);
        env.memory.setNativeLongReference(t0.add(24), param0.constant);
        env.memory.setNativeLongReference(t0.add(28), param0.precision);
        env.memory.setNativeLongReference(t0.add(32), param0.tolerance);
        env.memory.setIntReference(t0.add(36), param0.time);
        env.memory.setNativeLongReference(t0.add(40), param0.tick);
        env.memory.setNativeLongReference(t0.add(44), param0.ppsfreq);
        env.memory.setNativeLongReference(t0.add(48), param0.jitter);
        env.memory.setIntReference(t0.add(52), param0.shift);
        env.memory.setNativeLongReference(t0.add(56), param0.stabil);
        env.memory.setNativeLongReference(t0.add(60), param0.jitcnt);
        env.memory.setNativeLongReference(t0.add(64), param0.calcnt);
        env.memory.setNativeLongReference(t0.add(68), param0.errcnt);
        env.memory.setNativeLongReference(t0.add(72), param0.stbcnt);
        env.memory.setIntReference(t0.add(76), param0.tai);
    }

    public static void aio_cancel(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        aiocb param1 = new aiocb();

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.aio_cancel(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void aio_cancel64(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        aiocb64 param1 = new aiocb64();

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.aio_cancel64(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void aio_error(Environment env) {
        BitVec t0 = env.register.get('0');

        aiocb param0 = new aiocb();

        int ret = CLibrary.INSTANCE.aio_error(param0);
        env.register.set('0', new BitVec(ret));

    }

    public static void aio_error64(Environment env) {
        BitVec t0 = env.register.get('0');

        aiocb64 param0 = new aiocb64();

        int ret = CLibrary.INSTANCE.aio_error64(param0);
        env.register.set('0', new BitVec(ret));

    }

    public static void aio_fsync(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        aiocb param1 = new aiocb();

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.aio_fsync(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void aio_fsync64(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        aiocb64 param1 = new aiocb64();

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.aio_fsync64(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void aio_init(Environment env) {
        BitVec t0 = env.register.get('0');

        aioinit param0 = new aioinit();

        param0.aio_threads = env.memory.getIntFromReference(t0);
        param0.aio_num = env.memory.getIntFromReference(t0.add(4));
        param0.aio_locks = env.memory.getIntFromReference(t0.add(8));
        param0.aio_usedba = env.memory.getIntFromReference(t0.add(12));
        param0.aio_debug = env.memory.getIntFromReference(t0.add(16));
        param0.aio_numusers = env.memory.getIntFromReference(t0.add(20));
        param0.aio_idle_time = env.memory.getIntFromReference(t0.add(24));
        param0.aio_reserved = env.memory.getIntFromReference(t0.add(28));

        CLibrary.INSTANCE.aio_init(param0);

        env.memory.setIntReference(t0, param0.aio_threads);
        env.memory.setIntReference(t0.add(4), param0.aio_num);
        env.memory.setIntReference(t0.add(8), param0.aio_locks);
        env.memory.setIntReference(t0.add(12), param0.aio_usedba);
        env.memory.setIntReference(t0.add(16), param0.aio_debug);
        env.memory.setIntReference(t0.add(20), param0.aio_numusers);
        env.memory.setIntReference(t0.add(24), param0.aio_idle_time);
        env.memory.setIntReference(t0.add(28), param0.aio_reserved);
    }

    public static void aio_read(Environment env) {
        BitVec t0 = env.register.get('0');

        aiocb param0 = new aiocb();

        int ret = CLibrary.INSTANCE.aio_read(param0);
        env.register.set('0', new BitVec(ret));

    }

    public static void aio_read64(Environment env) {
        BitVec t0 = env.register.get('0');

        aiocb64 param0 = new aiocb64();

        int ret = CLibrary.INSTANCE.aio_read64(param0);
        env.register.set('0', new BitVec(ret));

    }

    public static void aio_return(Environment env) {
        BitVec t0 = env.register.get('0');

        aiocb param0 = new aiocb();

        int ret = CLibrary.INSTANCE.aio_return(param0);
        env.register.set('0', new BitVec(ret));

    }

    public static void aio_return64(Environment env) {
        BitVec t0 = env.register.get('0');

        aiocb64 param0 = new aiocb64();

        int ret = CLibrary.INSTANCE.aio_return64(param0);
        env.register.set('0', new BitVec(ret));

    }

    public static void aio_suspend(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        aiocb[] param0;
        int param1;

        param0 = (aiocb[]) env.memory.getArray(t0, -1);
        param1 = env.memory.getInt(t1);

        int ret = CLibrary.INSTANCE.aio_suspend(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setArray(t0, -1, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void aio_suspend64(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        aiocb64[] param0;
        int param1;

        param0 = (aiocb64[]) env.memory.getArray(t0, -1);
        param1 = env.memory.getInt(t1);

        int ret = CLibrary.INSTANCE.aio_suspend64(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setArray(t0, -1, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void aio_write(Environment env) {
        BitVec t0 = env.register.get('0');

        aiocb param0 = new aiocb();

        int ret = CLibrary.INSTANCE.aio_write(param0);
        env.register.set('0', new BitVec(ret));

    }

    public static void aio_write64(Environment env) {
        BitVec t0 = env.register.get('0');

        aiocb64 param0 = new aiocb64();

        int ret = CLibrary.INSTANCE.aio_write64(param0);
        env.register.set('0', new BitVec(ret));

    }

    public static void alarm(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.alarm(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void aligned_alloc(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        int param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getInt(t1);

        CLibrary.INSTANCE.aligned_alloc(param0, param1);

        //env.memory.setInt(t0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void alloca(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        CLibrary.INSTANCE.alloca(param0);

        //env.memory.setInt(t0, param0);
    }

//    public static void argp_error (Environment env) {
//        BitVec t0 = env.register.get('0');
//
//        argp_state param0 = new argp_state();
//        CLibrary.INSTANCE.argp_error(param0);
//
//    }
//
//    public static void argp_failure (Environment env) {
//        BitVec t0 = env.register.get('0');
//
//        argp_state param0 = new argp_state();
//        CLibrary.INSTANCE.argp_failure(param0);
//
//    }

//    public static void argz_add (Environment env) {
//        BitVec t0 = env.register.get('0');
//        BitVec t1 = env.register.get('1');
//        BitVec t2 = env.register.get('2');
//
//        String[] param0 ;
//        int[] param1 ;
//        byte[] param2 ;
//
//        param0 = (String[]) env.memory.getArray(t0, -1);
//        param1 = env.memory.getIntArray(t1, param1);
//        param2 = env.memory.getByteArray(t2, param1);
//
//        int ret = CLibrary.INSTANCE.argz_add(param0, param1, param2);
//        env.register.set('0', new BitVec(ret));
//
//
//        env.memory.setArray(t0, -1, param0);
//        env.memory.setIntArray(t1, param1, param1);
//        env.memory.setByteArray(t2, param1, param2);
//    }

    public static void argz_add_sep(Environment env) {
        BitVec t0 = env.register.get('0');

        String[] param0;

        param0 = (String[]) env.memory.getArray(t0, -1);

        int ret = CLibrary.INSTANCE.argz_add_sep(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setArray(t0, -1, param0);
    }

    public static void argz_append(Environment env) {
        BitVec t0 = env.register.get('0');

        String[] param0;

        param0 = (String[]) env.memory.getArray(t0, -1);

        int ret = CLibrary.INSTANCE.argz_append(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setArray(t0, -1, param0);
    }

    public static void argz_count(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        byte[] param0;
        int param1;

        param1 = env.memory.getInt(t1);
        param0 = env.memory.getByteArray(t0, param1);

        int ret = CLibrary.INSTANCE.argz_count(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setByteArray(t0, param1, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void argz_create(Environment env) {
        BitVec t0 = env.register.get('0');

        String[] param0;

        param0 = (String[]) env.memory.getArray(t0, -1);

        int ret = CLibrary.INSTANCE.argz_create(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setArray(t0, -1, param0);
    }

    public static void argz_create_sep(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        int ret = CLibrary.INSTANCE.argz_create_sep(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

//    public static void argz_delete (Environment env) {
//        BitVec t0 = env.register.get('0');
//        BitVec t1 = env.register.get('1');
//        BitVec t2 = env.register.get('2');
//
//        String[] param0 ;
//        int[] param1 ;
//        byte[] param2 ;
//
//        param0 = (String[]) env.memory.getArray(t0, -1);
//        param1 = env.memory.getIntArray(t1, param1);
//        param2 = env.memory.getByteArray(t2, param1);
//
//
//        CLibrary.INSTANCE.argz_delete(param0, param1, param2);
//
//        env.memory.setArray(t0, -1, param0);
//        env.memory.setIntArray(t1, param1, param1);
//        env.memory.setByteArray(t2, param1, param2);
//    }

    public static void argz_extract(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        byte[] param0;
        int param1;
        byte param2;

        param1 = env.memory.getInt(t1);
        param0 = env.memory.getByteArray(t0, param1);
        param2 = env.memory.getByte(t2);

        CLibrary.INSTANCE.argz_extract(param0, param1, param2);

        env.memory.setByteArray(t0, param1, param0);
        ////env.memory.setInt(t1, param1);
        env.memory.setByte(t2, param2);
    }

    public static void argz_insert(Environment env) {
        BitVec t0 = env.register.get('0');

        String[] param0;

        param0 = (String[]) env.memory.getArray(t0, -1);

        int ret = CLibrary.INSTANCE.argz_insert(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setArray(t0, -1, param0);
    }

    public static void argz_next(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        byte[] param0;
        int param1;
        byte[] param2;

        param1 = env.memory.getInt(t1);
        param0 = env.memory.getByteArray(t0, param1);
        param2 = env.memory.getByteArray(t2, param1);

        byte ret = CLibrary.INSTANCE.argz_next(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setByteArray(t0, param1, param0);
        ////env.memory.setInt(t1, param1);
        env.memory.setByteArray(t2, param1, param2);
    }

    public static void argz_stringify(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        byte[] param0;
        int param1;
        int param2;

        param1 = env.memory.getInt(t1);
        param0 = env.memory.getByteArray(t0, param1);
        param2 = env.memory.getInt(t2);

        CLibrary.INSTANCE.argz_stringify(param0, param1, param2);

        env.memory.setByteArray(t0, param1, param0);
        ////env.memory.setInt(t1, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void asin(Environment env) {
        BitVec t0 = env.register.get('0');

        double param0;

        param0 = env.memory.getDouble(t0);

        double ret = CLibrary.INSTANCE.asin(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setDouble(t0, param0);
    }

    public static void asinf(Environment env) {
        BitVec t0 = env.register.get('0');

        float param0;

        param0 = env.memory.getFloat(t0);

        float ret = CLibrary.INSTANCE.asinf(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setFloat(t0, param0);
    }

    public static void asinh(Environment env) {
        BitVec t0 = env.register.get('0');

        double param0;

        param0 = env.memory.getDouble(t0);

        double ret = CLibrary.INSTANCE.asinh(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setDouble(t0, param0);
    }

    public static void asinhf(Environment env) {
        BitVec t0 = env.register.get('0');

        float param0;

        param0 = env.memory.getFloat(t0);

        float ret = CLibrary.INSTANCE.asinhf(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setFloat(t0, param0);
    }

    public static void asinhl(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        double ret = CLibrary.INSTANCE.asinhl(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
    }

    public static void asinl(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        double ret = CLibrary.INSTANCE.asinl(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
    }

    public static void asserT(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        CLibrary.INSTANCE.asserT(param0);

        //env.memory.setInt(t0, param0);
    }

    public static void assert_perror(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        CLibrary.INSTANCE.assert_perror(param0);

        //env.memory.setInt(t0, param0);
    }

    public static void atan(Environment env) {
        BitVec t0 = env.register.get('0');

        double param0;

        param0 = env.memory.getDouble(t0);

        double ret = CLibrary.INSTANCE.atan(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setDouble(t0, param0);
    }

    public static void atan2(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        double param0;
        double param1;

        param0 = env.memory.getDouble(t0);
        param1 = env.memory.getDouble(t1);

        double ret = CLibrary.INSTANCE.atan2(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setDouble(t0, param0);
        //env.memory.setDouble(t1, param1);
    }

    public static void atan2f(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        float param0;
        float param1;

        param0 = env.memory.getFloat(t0);
        param1 = env.memory.getFloat(t1);

        float ret = CLibrary.INSTANCE.atan2f(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setFloat(t0, param0);
        env.memory.setFloat(t1, param1);
    }

    public static void atan2l(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        NativeLong param0;
        NativeLong param1;

        param0 = env.memory.getNativeLong(t0);
        param1 = env.memory.getNativeLong(t1);

        double ret = CLibrary.INSTANCE.atan2l(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
        //env.memory.setNativeLongt1, param1);
    }

    public static void atanf(Environment env) {
        BitVec t0 = env.register.get('0');

        float param0;

        param0 = env.memory.getFloat(t0);

        float ret = CLibrary.INSTANCE.atanf(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setFloat(t0, param0);
    }

    public static void atanh(Environment env) {
        BitVec t0 = env.register.get('0');

        double param0;

        param0 = env.memory.getDouble(t0);

        double ret = CLibrary.INSTANCE.atanh(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setDouble(t0, param0);
    }

    public static void atanhf(Environment env) {
        BitVec t0 = env.register.get('0');

        float param0;

        param0 = env.memory.getFloat(t0);

        float ret = CLibrary.INSTANCE.atanhf(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setFloat(t0, param0);
    }

    public static void atanhl(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        double ret = CLibrary.INSTANCE.atanhl(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
    }

    public static void atanl(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        double ret = CLibrary.INSTANCE.atanl(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
    }

//    public static void atexit (Environment env) {
//        BitVec t0 = env.register.get('0');
//
//        LongByReference param0 ;
//
//        param0 = env.memory.getVoid(t0);
//
//        int ret = CLibrary.INSTANCE.atexit(param0);
//        env.register.set('0', new BitVec(ret));
//
//
//        env.memory.setVoid(t0, param0);
//    }

    public static void atof(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        double ret = CLibrary.INSTANCE.atof(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void atoi(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        int ret = CLibrary.INSTANCE.atoi(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void atol(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        NativeLong ret = CLibrary.INSTANCE.atol(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void atoll(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        long ret = CLibrary.INSTANCE.atoll(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void backtrace(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        LongByReference param0;
        int param1;

        param0 = env.memory.getPointer(t0);
        param1 = env.memory.getInt(t1);

        int ret = CLibrary.INSTANCE.backtrace(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setPointer(t0, param0.getValue());
        ////env.memory.setInt(t1, param1);
    }

    public static void backtrace_symbols(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        LongByReference param0;
        int param1;

        param0 = env.memory.getPointer(t0);
        param1 = env.memory.getInt(t1);

        byte ret = CLibrary.INSTANCE.backtrace_symbols(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setPointer(t0, param0.getValue());
        ////env.memory.setInt(t1, param1);
    }

    public static void backtrace_symbols_fd(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        LongByReference param0;
        int param1;
        int param2;

        param0 = env.memory.getPointer(t0);
        param1 = env.memory.getInt(t1);
        param2 = env.memory.getInt(t2);

        CLibrary.INSTANCE.backtrace_symbols_fd(param0, param1, param2);

        env.memory.setPointer(t0, param0.getValue());
        ////env.memory.setInt(t1, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void basename(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        byte ret = CLibrary.INSTANCE.basename(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void bcmp(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        LongByReference param0;
        LongByReference param1;
        int param2;

        param0 = env.memory.getPointer(t0);
        param1 = env.memory.getPointer(t1);
        param2 = env.memory.getInt(t2);

        int ret = CLibrary.INSTANCE.bcmp(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setPointer(t0, param0.getValue());
        env.memory.setPointer(t1, param1.getValue());
        //env.memory.setInt(t2, param2);
    }

    public static void bcopy(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        LongByReference param0;
        LongByReference param1;
        int param2;

        param0 = env.memory.getPointer(t0);
        param1 = env.memory.getPointer(t1);
        param2 = env.memory.getInt(t2);

        CLibrary.INSTANCE.bcopy(param0, param1, param2);

        env.memory.setPointer(t0, param0.getValue());
        env.memory.setPointer(t1, param1.getValue());
        //env.memory.setInt(t2, param2);
    }

    public static void bind(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        int param0;
        int param1;
        int param2;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getInt(t1);
        param2 = env.memory.getInt(t2);

        //int ret = CLibrary.INSTANCE.bind(param0, param1, param2);
        int ret = 0;
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        ////env.memory.setInt(t1, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void bindtextdomain(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        String param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getTextFromReference(t1);

        ByReference ret = CLibrary.INSTANCE.bindtextdomain(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setTextReference(t1, param1);
    }

    public static void bind_textdomain_codeset(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        byte ret = CLibrary.INSTANCE.bind_textdomain_codeset(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void brk(Environment env) {
        BitVec t0 = env.register.get('0');

        LongByReference param0;

        param0 = env.memory.getPointer(t0);

        int ret = CLibrary.INSTANCE.brk(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setPointer(t0, param0.getValue());
    }

    public static void bsearch(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        LongByReference param0;
        LongByReference param1;

        param0 = env.memory.getPointer(t0);
        param1 = env.memory.getPointer(t1);

        CLibrary.INSTANCE.bsearch(param0, param1);

        env.memory.setPointer(t0, param0.getValue());
        env.memory.setPointer(t1, param1.getValue());
    }

    public static void btowc(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.btowc(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void bzero(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        LongByReference param0;
        int param1;

        param0 = env.memory.getPointer(t0);
        param1 = env.memory.getInt(t1);

        CLibrary.INSTANCE.bzero(param0, param1);

        env.memory.setPointer(t0, param0.getValue());
        ////env.memory.setInt(t1, param1);
    }

    public static void cabs(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        double ret = CLibrary.INSTANCE.cabs(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void cabsf(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        float ret = CLibrary.INSTANCE.cabsf(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void cabsl(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        double ret = CLibrary.INSTANCE.cabsl(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
    }

    public static void cacos(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.cacos(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void cacosf(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.cacosf(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void cacosh(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.cacosh(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void cacoshf(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.cacoshf(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void cacoshl(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        int ret = CLibrary.INSTANCE.cacoshl(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
    }

    public static void cacosl(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        int ret = CLibrary.INSTANCE.cacosl(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
    }

    public static void calloc(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        int param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getInt(t1);

        CLibrary.INSTANCE.calloc(param0, param1);

        //env.memory.setInt(t0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void canonicalize_file_name(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        byte ret = CLibrary.INSTANCE.canonicalize_file_name(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void carg(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        double ret = CLibrary.INSTANCE.carg(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void cargf(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        float ret = CLibrary.INSTANCE.cargf(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void cargl(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        double ret = CLibrary.INSTANCE.cargl(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
    }

    public static void casin(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.casin(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void casinf(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.casinf(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void casinh(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.casinh(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void casinhf(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.casinhf(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void casinhl(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        int ret = CLibrary.INSTANCE.casinhl(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
    }

    public static void casinl(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        int ret = CLibrary.INSTANCE.casinl(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
    }

    public static void catan(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.catan(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void catanf(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.catanf(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void catanh(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.catanh(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void catanhf(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.catanhf(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void catanhl(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        int ret = CLibrary.INSTANCE.catanhl(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
    }

    public static void catanl(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        int ret = CLibrary.INSTANCE.catanl(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
    }

    public static void catclose(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.catclose(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void catgets(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        int param0;
        int param1;
        int param2;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getInt(t1);
        param2 = env.memory.getInt(t2);

        byte ret = CLibrary.INSTANCE.catgets(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        ////env.memory.setInt(t1, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void catopen(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        int param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getInt(t1);

        int ret = CLibrary.INSTANCE.catopen(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void cbrt(Environment env) {
        BitVec t0 = env.register.get('0');

        double param0;

        param0 = env.memory.getDouble(t0);

        double ret = CLibrary.INSTANCE.cbrt(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setDouble(t0, param0);
    }

    public static void cbrtf(Environment env) {
        BitVec t0 = env.register.get('0');

        float param0;

        param0 = env.memory.getFloat(t0);

        float ret = CLibrary.INSTANCE.cbrtf(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setFloat(t0, param0);
    }

    public static void cbrtl(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        double ret = CLibrary.INSTANCE.cbrtl(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
    }

    public static void ccos(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.ccos(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void ccosf(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.ccosf(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void ccosh(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.ccosh(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void ccoshf(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.ccoshf(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void ccoshl(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        int ret = CLibrary.INSTANCE.ccoshl(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
    }

    public static void ccosl(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        int ret = CLibrary.INSTANCE.ccosl(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
    }

    public static void ceil(Environment env) {
        BitVec t0 = env.register.get('0');

        double param0;

        param0 = env.memory.getDouble(t0);

        double ret = CLibrary.INSTANCE.ceil(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setDouble(t0, param0);
    }

    public static void ceilf(Environment env) {
        BitVec t0 = env.register.get('0');

        float param0;

        param0 = env.memory.getFloat(t0);

        float ret = CLibrary.INSTANCE.ceilf(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setFloat(t0, param0);
    }

    public static void ceill(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        double ret = CLibrary.INSTANCE.ceill(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
    }

    public static void cexp(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.cexp(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void cexpf(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.cexpf(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void cexpl(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        int ret = CLibrary.INSTANCE.cexpl(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
    }

    public static void cfgetispeed(Environment env) {
        BitVec t0 = env.register.get('0');

        termios param0 = new termios();

        int ret = CLibrary.INSTANCE.cfgetispeed(param0);
        env.register.set('0', new BitVec(ret));

    }

    public static void cfgetospeed(Environment env) {
        BitVec t0 = env.register.get('0');

        termios param0 = new termios();

        int ret = CLibrary.INSTANCE.cfgetospeed(param0);
        env.register.set('0', new BitVec(ret));

    }

    public static void cfmakeraw(Environment env) {
        BitVec t0 = env.register.get('0');

        termios param0 = new termios();

        CLibrary.INSTANCE.cfmakeraw(param0);

    }

    public static void cfsetispeed(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        termios param0 = new termios();
        int param1;

        param1 = env.memory.getInt(t1);

        int ret = CLibrary.INSTANCE.cfsetispeed(param0, param1);
        env.register.set('0', new BitVec(ret));

        ////env.memory.setInt(t1, param1);
    }

    public static void cfsetospeed(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        termios param0 = new termios();
        int param1;

        param1 = env.memory.getInt(t1);

        int ret = CLibrary.INSTANCE.cfsetospeed(param0, param1);
        env.register.set('0', new BitVec(ret));

        ////env.memory.setInt(t1, param1);
    }

    public static void cfsetspeed(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        termios param0 = new termios();
        int param1;

        param1 = env.memory.getInt(t1);

        int ret = CLibrary.INSTANCE.cfsetspeed(param0, param1);
        env.register.set('0', new BitVec(ret));

        ////env.memory.setInt(t1, param1);
    }

    public static void chdir(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        //int ret = CLibrary.INSTANCE.chdir(param0);
        env.register.set('0', new BitVec(0));

        env.memory.setTextReference(t0, param0);
    }

    public static void chmod(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        int param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getInt(t1);

        int ret = CLibrary.INSTANCE.chmod(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void chown(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        String param0;
        int param1;
        int param2;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getInt(t1);
        param2 = env.memory.getInt(t2);

        int ret = CLibrary.INSTANCE.chown(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        ////env.memory.setInt(t1, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void cimag(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        double ret = CLibrary.INSTANCE.cimag(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void cimagf(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        float ret = CLibrary.INSTANCE.cimagf(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void cimagl(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        double ret = CLibrary.INSTANCE.cimagl(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
    }

    public static void clearenv(Environment env) {

        int ret = CLibrary.INSTANCE.clearenv();
        env.register.set('0', new BitVec(ret));

    }

    public static void clearerr(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        CLibrary.INSTANCE.clearerr(param0);

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void clearerr_unlocked(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        CLibrary.INSTANCE.clearerr_unlocked(param0);

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void clock(Environment env) {

        NativeLong ret = CLibrary.INSTANCE.clock();
        env.register.set('0', new BitVec(ret));

    }

    public static void clock_getres(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        timespec param1 = new timespec();

        param0 = env.memory.getInt(t0);
        param1.tv_sec = env.memory.getIntFromReference(t1);
        param1.tv_nsec = env.memory.getNativeLongFromReference(t1.add(4));

        int ret = CLibrary.INSTANCE.clock_getres(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        env.memory.setIntReference(t1, param1.tv_sec);
        env.memory.setNativeLongReference(t1.add(4), param1.tv_nsec);
    }

    public static void clock_gettime(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        timespec param1 = new timespec();

        param0 = env.memory.getInt(t0);
        param1.tv_sec = env.memory.getIntFromReference(t1);
        param1.tv_nsec = env.memory.getNativeLongFromReference(t1.add(4));

        int ret = CLibrary.INSTANCE.clock_gettime(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        env.memory.setIntReference(t1, param1.tv_sec);
        env.memory.setNativeLongReference(t1.add(4), param1.tv_nsec);
    }

    public static void clock_settime(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        timespec param1 = new timespec();

        param0 = env.memory.getInt(t0);
        param1.tv_sec = env.memory.getIntFromReference(t1);
        param1.tv_nsec = env.memory.getNativeLongFromReference(t1.add(4));

        int ret = CLibrary.INSTANCE.clock_settime(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        env.memory.setIntReference(t1, param1.tv_sec);
        env.memory.setNativeLongReference(t1.add(4), param1.tv_nsec);
    }

    public static void clog(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.clog(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void clog10(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.clog10(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void clog10f(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.clog10f(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void clog10l(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        int ret = CLibrary.INSTANCE.clog10l(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
    }

    public static void clogf(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.clogf(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void clogl(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        int ret = CLibrary.INSTANCE.clogl(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
    }

    public static void close(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        //int ret = CLibrary.INSTANCE.close(param0);
        int ret = 0;
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void closedir(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        int ret = CLibrary.INSTANCE.closedir(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void closelog(Environment env) {
        CLibrary.INSTANCE.closelog();
    }

    public static void confstr(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        int param0;
        byte[] param1;
        int param2;

        param0 = env.memory.getInt(t0);
        param2 = env.memory.getInt(t2);
        param1 = env.memory.getByteArray(t1, param2);

        int ret = CLibrary.INSTANCE.confstr(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        env.memory.setByteArray(t1, param2, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void conj(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.conj(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void conjf(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.conjf(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void conjl(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        int ret = CLibrary.INSTANCE.conjl(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
    }

    public static void copysign(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        double param0;
        double param1;

        param0 = env.memory.getDouble(t0);
        param1 = env.memory.getDouble(t1);

        double ret = CLibrary.INSTANCE.copysign(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setDouble(t0, param0);
        //env.memory.setDouble(t1, param1);
    }

    public static void copysignf(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        float param0;
        float param1;

        param0 = env.memory.getFloat(t0);
        param1 = env.memory.getFloat(t1);

        float ret = CLibrary.INSTANCE.copysignf(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setFloat(t0, param0);
        env.memory.setFloat(t1, param1);
    }

    public static void copysignl(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        NativeLong param0;
        NativeLong param1;

        param0 = env.memory.getNativeLong(t0);
        param1 = env.memory.getNativeLong(t1);

        double ret = CLibrary.INSTANCE.copysignl(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
        //env.memory.setNativeLongt1, param1);
    }

    public static void cos(Environment env) {
        BitVec t0 = env.register.get('0');

        double param0;

        param0 = env.memory.getDouble(t0);

        double ret = CLibrary.INSTANCE.cos(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setDouble(t0, param0);
    }

    public static void cosf(Environment env) {
        BitVec t0 = env.register.get('0');

        float param0;

        param0 = env.memory.getFloat(t0);

        float ret = CLibrary.INSTANCE.cosf(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setFloat(t0, param0);
    }

    public static void cosh(Environment env) {
        BitVec t0 = env.register.get('0');

        double param0;

        param0 = env.memory.getDouble(t0);

        double ret = CLibrary.INSTANCE.cosh(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setDouble(t0, param0);
    }

    public static void coshf(Environment env) {
        BitVec t0 = env.register.get('0');

        float param0;

        param0 = env.memory.getFloat(t0);

        float ret = CLibrary.INSTANCE.coshf(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setFloat(t0, param0);
    }

    public static void coshl(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        double ret = CLibrary.INSTANCE.coshl(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
    }

    public static void cosl(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        double ret = CLibrary.INSTANCE.cosl(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
    }

    public static void cpow(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        int param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getInt(t1);

        int ret = CLibrary.INSTANCE.cpow(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void cpowf(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        int param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getInt(t1);

        int ret = CLibrary.INSTANCE.cpowf(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void cproj(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.cproj(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void cprojf(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.cprojf(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void cprojl(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        int ret = CLibrary.INSTANCE.cprojl(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
    }

    public static void CPU_CLR(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        IntByReference param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getIntRef(t1);

        CLibrary.INSTANCE.CPU_CLR(param0, param1);

        //env.memory.setInt(t0, param0);
        env.memory.setIntReference(t1, param1.getValue());
    }

    public static void CPU_ISSET(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        IntByReference param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getIntRef(t1);

        int ret = CLibrary.INSTANCE.CPU_ISSET(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        env.memory.setIntReference(t1, param1.getValue());
    }

    public static void CPU_SET(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        IntByReference param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getIntRef(t1);

        CLibrary.INSTANCE.CPU_SET(param0, param1);

        //env.memory.setInt(t0, param0);
        env.memory.setIntReference(t1, param1.getValue());
    }

    public static void CPU_ZERO(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        CLibrary.INSTANCE.CPU_ZERO(param0);

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void creal(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        double ret = CLibrary.INSTANCE.creal(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void crealf(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        float ret = CLibrary.INSTANCE.crealf(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void creall(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        double ret = CLibrary.INSTANCE.creall(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
    }

    public static void creat(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        int param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getInt(t1);

        int ret = CLibrary.INSTANCE.creat(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void creat64(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        int param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getInt(t1);

        int ret = CLibrary.INSTANCE.creat64(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void crypt(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        String param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getTextFromReference(t1);

        byte ret = CLibrary.INSTANCE.crypt(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setTextReference(t1, param1);
    }

    public static void crypt_r(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        String param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getTextFromReference(t1);

        byte ret = CLibrary.INSTANCE.crypt_r(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setTextReference(t1, param1);
    }

    public static void csin(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.csin(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void csinf(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.csinf(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void csinh(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.csinh(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void csinhf(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.csinhf(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void csinhl(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        int ret = CLibrary.INSTANCE.csinhl(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
    }

    public static void csinl(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        int ret = CLibrary.INSTANCE.csinl(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
    }

    public static void csqrt(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.csqrt(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void csqrtf(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.csqrtf(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void csqrtl(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        int ret = CLibrary.INSTANCE.csqrtl(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
    }

    public static void ctan(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.ctan(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void ctanf(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.ctanf(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void ctanh(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.ctanh(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void ctanhf(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.ctanhf(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void ctanhl(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        int ret = CLibrary.INSTANCE.ctanhl(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
    }

    public static void ctanl(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        int ret = CLibrary.INSTANCE.ctanl(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
    }

    public static void ctermid(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        byte ret = CLibrary.INSTANCE.ctermid(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void asctime(Environment env) {
        BitVec t0 = env.register.get('0');

        tm param0 = new tm();

        param0.tm_sec = env.memory.getIntFromReference(t0);
        param0.tm_min = env.memory.getIntFromReference(t0.add(4));
        param0.tm_hour = env.memory.getIntFromReference(t0.add(8));
        param0.tm_mday = env.memory.getIntFromReference(t0.add(12));
        param0.tm_mon = env.memory.getIntFromReference(t0.add(16));
        param0.tm_year = env.memory.getIntFromReference(t0.add(20));
        param0.tm_wday = env.memory.getIntFromReference(t0.add(24));
        param0.tm_yday = env.memory.getIntFromReference(t0.add(28));
        param0.tm_isdst = env.memory.getIntFromReference(t0.add(32));
        param0.__tm_gmtoff = env.memory.getNativeLongFromReference(t0.add(36));
        param0.__tm_zone = env.memory.getStringFromReference(t0.add(40));

        byte ret = CLibrary.INSTANCE.asctime(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.tm_sec);
        env.memory.setIntReference(t0.add(4), param0.tm_min);
        env.memory.setIntReference(t0.add(8), param0.tm_hour);
        env.memory.setIntReference(t0.add(12), param0.tm_mday);
        env.memory.setIntReference(t0.add(16), param0.tm_mon);
        env.memory.setIntReference(t0.add(20), param0.tm_year);
        env.memory.setIntReference(t0.add(24), param0.tm_wday);
        env.memory.setIntReference(t0.add(28), param0.tm_yday);
        env.memory.setIntReference(t0.add(32), param0.tm_isdst);
        env.memory.setNativeLongReference(t0.add(36), param0.__tm_gmtoff);
        env.memory.setStringReference(t0.add(40), param0.__tm_zone);
    }

    public static void asctime_r(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        tm param0 = new tm();
        String param1;

        param0.tm_sec = env.memory.getIntFromReference(t0);
        param0.tm_min = env.memory.getIntFromReference(t0.add(4));
        param0.tm_hour = env.memory.getIntFromReference(t0.add(8));
        param0.tm_mday = env.memory.getIntFromReference(t0.add(12));
        param0.tm_mon = env.memory.getIntFromReference(t0.add(16));
        param0.tm_year = env.memory.getIntFromReference(t0.add(20));
        param0.tm_wday = env.memory.getIntFromReference(t0.add(24));
        param0.tm_yday = env.memory.getIntFromReference(t0.add(28));
        param0.tm_isdst = env.memory.getIntFromReference(t0.add(32));
        param0.__tm_gmtoff = env.memory.getNativeLongFromReference(t0.add(36));
        param0.__tm_zone = env.memory.getStringFromReference(t0.add(40));
        param1 = env.memory.getTextFromReference(t1);

        byte ret = CLibrary.INSTANCE.asctime_r(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.tm_sec);
        env.memory.setIntReference(t0.add(4), param0.tm_min);
        env.memory.setIntReference(t0.add(8), param0.tm_hour);
        env.memory.setIntReference(t0.add(12), param0.tm_mday);
        env.memory.setIntReference(t0.add(16), param0.tm_mon);
        env.memory.setIntReference(t0.add(20), param0.tm_year);
        env.memory.setIntReference(t0.add(24), param0.tm_wday);
        env.memory.setIntReference(t0.add(28), param0.tm_yday);
        env.memory.setIntReference(t0.add(32), param0.tm_isdst);
        env.memory.setNativeLongReference(t0.add(36), param0.__tm_gmtoff);
        env.memory.setStringReference(t0.add(40), param0.__tm_zone);
        env.memory.setTextReference(t1, param1);
    }

    public static void cuserid(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        byte ret = CLibrary.INSTANCE.cuserid(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void dcgettext(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        byte ret = CLibrary.INSTANCE.dcgettext(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void dcngettext(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        String param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getTextFromReference(t1);

        byte ret = CLibrary.INSTANCE.dcngettext(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setTextReference(t1, param1);
    }

    public static void dgettext(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        String param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getTextFromReference(t1);

        ByReference ret = CLibrary.INSTANCE.dgettext(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setTextReference(t1, param1);
    }

    public static void difftime(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        int param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getInt(t1);

        double ret = CLibrary.INSTANCE.difftime(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void dirfd(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        int ret = CLibrary.INSTANCE.dirfd(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void dirname(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        byte ret = CLibrary.INSTANCE.dirname(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void div(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        int param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getInt(t1);

        int ret = CLibrary.INSTANCE.div(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void dngettext(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        String param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getTextFromReference(t1);

        byte ret = CLibrary.INSTANCE.dngettext(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setTextReference(t1, param1);
    }

    public static void drand48(Environment env) {

        double ret = CLibrary.INSTANCE.drand48();
        env.register.set('0', new BitVec(ret));

    }

//    public static void drand48_r (Environment env) {
//        BitVec t0 = env.register.get('0');
//        BitVec t1 = env.register.get('1');
//
//        drand48_data param0 = new drand48_data();
//        DoubleByReference param1 ;
//
//        param0.__x = env.memory.getIntFromReference(t0);
//        param0.__old_x = env.memory.getIntFromReference(t0.add(4));
//        param0.__c = env.memory.getShortRef(t0.add(8));
//        param0.__init = env.memory.getShortRef(t0.add(10));
//        param0.__a = env.memory.getLongFromReference(t0.add(12));
//        param1 = env.memory.getDoubleRef(t1);
//
//        int ret = CLibrary.INSTANCE.drand48_r(param0, param1);
//        env.register.set('0', new BitVec(ret));
//
//
//        env.memory.setIntReference(t0, param0.__x);
//        env.memory.setIntReference(t0.add(4), param0.__old_x);
//        env.memory.setShortReference(t0.add(8), param0.__c);
//        env.memory.setShortReference(t0.add(10), param0.__init);
//        env.memory.setLongReference(t0.add(12), param0.__a);
//        env.memory.setDoubleReference(t1, param1.getValue());
//    }

    public static void drem(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        double param0;
        double param1;

        param0 = env.memory.getDouble(t0);
        param1 = env.memory.getDouble(t1);

        double ret = CLibrary.INSTANCE.drem(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setDouble(t0, param0);
        //env.memory.setDouble(t1, param1);
    }

    public static void dremf(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        float param0;
        float param1;

        param0 = env.memory.getFloat(t0);
        param1 = env.memory.getFloat(t1);

        float ret = CLibrary.INSTANCE.dremf(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setFloat(t0, param0);
        env.memory.setFloat(t1, param1);
    }

    public static void dreml(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        NativeLong param0;
        NativeLong param1;

        param0 = env.memory.getNativeLong(t0);
        param1 = env.memory.getNativeLong(t1);

        double ret = CLibrary.INSTANCE.dreml(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
        //env.memory.setNativeLongt1, param1);
    }

    public static void dup(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.dup(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void dup2(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        int param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getInt(t1);

        int ret = CLibrary.INSTANCE.dup2(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void ecvt(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');
        BitVec t3 = env.register.get('3');

        double param0;
        int param1;
        IntByReference param2;
        IntByReference param3;

        param0 = env.memory.getDouble(t0);
        param1 = env.memory.getInt(t1);
        param2 = env.memory.getIntRef(t2);
        param3 = env.memory.getIntRef(t3);

        byte ret = CLibrary.INSTANCE.ecvt(param0, param1, param2, param3);
        env.register.set('0', new BitVec(ret));

        //env.memory.setDouble(t0, param0);
        ////env.memory.setInt(t1, param1);
        env.memory.setIntReference(t2, param2.getValue());
        env.memory.setIntReference(t3, param3.getValue());
    }

    public static void ecvt_r(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        double param0;
        int param1;
        IntByReference param2;

        param0 = env.memory.getDouble(t0);
        param1 = env.memory.getInt(t1);
        param2 = env.memory.getIntRef(t2);

        int ret = CLibrary.INSTANCE.ecvt_r(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        //env.memory.setDouble(t0, param0);
        ////env.memory.setInt(t1, param1);
        env.memory.setIntReference(t2, param2.getValue());
    }

    public static void endfsent(Environment env) {

        CLibrary.INSTANCE.endfsent();

    }

    public static void endgrent(Environment env) {

        CLibrary.INSTANCE.endgrent();

    }

    public static void endhostent(Environment env) {

        CLibrary.INSTANCE.endhostent();

    }

    public static void endmntent(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        int ret = CLibrary.INSTANCE.endmntent(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void endnetent(Environment env) {

        CLibrary.INSTANCE.endnetent();

    }

    public static void endnetgrent(Environment env) {

        CLibrary.INSTANCE.endnetgrent();

    }

    public static void endprotoent(Environment env) {

        CLibrary.INSTANCE.endprotoent();

    }

    public static void endpwent(Environment env) {

        CLibrary.INSTANCE.endpwent();

    }

    public static void endservent(Environment env) {

        CLibrary.INSTANCE.endservent();

    }

    public static void endutent(Environment env) {

        CLibrary.INSTANCE.endutent();

    }

    public static void envz_add(Environment env) {
        BitVec t0 = env.register.get('0');

        String[] param0;

        param0 = (String[]) env.memory.getArray(t0, -1);

        int ret = CLibrary.INSTANCE.envz_add(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setArray(t0, -1, param0);
    }

    public static void envz_entry(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        byte[] param0;
        int param1;
        byte[] param2;

        param1 = env.memory.getInt(t1);
        param0 = env.memory.getByteArray(t0, param1);
        param2 = env.memory.getByteArray(t2, param1);

        byte ret = CLibrary.INSTANCE.envz_entry(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setByteArray(t0, param1, param0);
        ////env.memory.setInt(t1, param1);
        env.memory.setByteArray(t2, param1, param2);
    }

    public static void envz_get(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        byte[] param0;
        int param1;
        byte[] param2;

        param1 = env.memory.getInt(t1);
        param0 = env.memory.getByteArray(t0, param1);
        param2 = env.memory.getByteArray(t2, param1);

        byte ret = CLibrary.INSTANCE.envz_get(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setByteArray(t0, param1, param0);
        ////env.memory.setInt(t1, param1);
        env.memory.setByteArray(t2, param1, param2);
    }

    public static void envz_merge(Environment env) {
        BitVec t0 = env.register.get('0');

        String[] param0;

        param0 = (String[]) env.memory.getArray(t0, -1);

        int ret = CLibrary.INSTANCE.envz_merge(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setArray(t0, -1, param0);
    }

//    public static void envz_remove (Environment env) {
//        BitVec t0 = env.register.get('0');
//        BitVec t1 = env.register.get('1');
//        BitVec t2 = env.register.get('2');
//
//        String[] param0 ;
//        int[] param1 ;
//        byte[] param2 ;
//
//        param0 = (String[]) env.memory.getArray(t0, -1);
//        param1 = env.memory.getIntArray(t1, param1);
//        param2 = env.memory.getByteArray(t2, param1);
//
//
//        CLibrary.INSTANCE.envz_remove(param0, param1, param2);
//
//        env.memory.setArray(t0, -1, param0);
//        env.memory.setIntArray(t1, param1, param1);
//        env.memory.setByteArray(t2, param1, param2);
//    }

//    public static void envz_strip (Environment env) {
//        BitVec t0 = env.register.get('0');
//        BitVec t1 = env.register.get('1');
//
//        String[] param0 ;
//        int[] param1 ;
//
//        param0 = (String[]) env.memory.getArray(t0, -1);
//        param1 = env.memory.getIntArray(t1, param1);
//
//
//        CLibrary.INSTANCE.envz_strip(param0, param1);
//
//        env.memory.setArray(t0, -1, param0);
//        env.memory.setIntArray(t1, param1, param1);
//    }

    public static void erand48(Environment env) {
        BitVec t0 = env.register.get('0');

        short[] param0;

        param0 = env.memory.getShortArray(t0, 3);

        double ret = CLibrary.INSTANCE.erand48(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setShortArray(t0, 3, param0);
    }

    public static void erand48_r(Environment env) {
        BitVec t0 = env.register.get('0');

        int[] param0;

        param0 = env.memory.getIntArray(t0, 3);

        int ret = CLibrary.INSTANCE.erand48_r(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntArray(t0, 3, param0);
    }

    public static void erf(Environment env) {
        BitVec t0 = env.register.get('0');

        double param0;

        param0 = env.memory.getDouble(t0);

        double ret = CLibrary.INSTANCE.erf(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setDouble(t0, param0);
    }

    public static void erfc(Environment env) {
        BitVec t0 = env.register.get('0');

        double param0;

        param0 = env.memory.getDouble(t0);

        double ret = CLibrary.INSTANCE.erfc(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setDouble(t0, param0);
    }

    public static void erfcf(Environment env) {
        BitVec t0 = env.register.get('0');

        float param0;

        param0 = env.memory.getFloat(t0);

        float ret = CLibrary.INSTANCE.erfcf(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setFloat(t0, param0);
    }

    public static void erfcl(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        double ret = CLibrary.INSTANCE.erfcl(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
    }

    public static void erff(Environment env) {
        BitVec t0 = env.register.get('0');

        float param0;

        param0 = env.memory.getFloat(t0);

        float ret = CLibrary.INSTANCE.erff(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setFloat(t0, param0);
    }

    public static void erfl(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        double ret = CLibrary.INSTANCE.erfl(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
    }

    public static void error_at_line(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        int param0;
        int param1;
        String param2;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getInt(t1);
        param2 = env.memory.getTextFromReference(t2);

        CLibrary.INSTANCE.error_at_line(param0, param1, param2);

        //env.memory.setInt(t0, param0);
        ////env.memory.setInt(t1, param1);
        env.memory.setTextReference(t2, param2);
    }

    public static void execv(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        int ret = CLibrary.INSTANCE.execv(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void execve(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        int ret = CLibrary.INSTANCE.execve(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void execvp(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        int ret = CLibrary.INSTANCE.execvp(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void exit(Environment env) throws Exception {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        //CLibrary.INSTANCE.exit(param0);
        throw new Exception("exit");
        //env.memory.setInt(t0, param0);
    }

    public static void exp(Environment env) {
        BitVec t0 = env.register.get('0');

        double param0;

        param0 = env.memory.getDouble(t0);

        double ret = CLibrary.INSTANCE.exp(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setDouble(t0, param0);
    }

    public static void exp10(Environment env) {
        BitVec t0 = env.register.get('0');

        double param0;

        param0 = env.memory.getDouble(t0);

        double ret = CLibrary.INSTANCE.exp10(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setDouble(t0, param0);
    }

    public static void exp10f(Environment env) {
        BitVec t0 = env.register.get('0');

        float param0;

        param0 = env.memory.getFloat(t0);

        float ret = CLibrary.INSTANCE.exp10f(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setFloat(t0, param0);
    }

    public static void exp10l(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        double ret = CLibrary.INSTANCE.exp10l(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
    }

    public static void exp2(Environment env) {
        BitVec t0 = env.register.get('0');

        double param0;

        param0 = env.memory.getDouble(t0);

        double ret = CLibrary.INSTANCE.exp2(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setDouble(t0, param0);
    }

    public static void exp2f(Environment env) {
        BitVec t0 = env.register.get('0');

        float param0;

        param0 = env.memory.getFloat(t0);

        float ret = CLibrary.INSTANCE.exp2f(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setFloat(t0, param0);
    }

    public static void exp2l(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        double ret = CLibrary.INSTANCE.exp2l(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
    }

    public static void expf(Environment env) {
        BitVec t0 = env.register.get('0');

        float param0;

        param0 = env.memory.getFloat(t0);

        float ret = CLibrary.INSTANCE.expf(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setFloat(t0, param0);
    }

    public static void expl(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        double ret = CLibrary.INSTANCE.expl(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
    }

    public static void explicit_bzero(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        LongByReference param0;
        int param1;

        param0 = env.memory.getPointer(t0);
        param1 = env.memory.getInt(t1);

        CLibrary.INSTANCE.explicit_bzero(param0, param1);

        env.memory.setPointer(t0, param0.getValue());
        ////env.memory.setInt(t1, param1);
    }

    public static void expm1(Environment env) {
        BitVec t0 = env.register.get('0');

        double param0;

        param0 = env.memory.getDouble(t0);

        double ret = CLibrary.INSTANCE.expm1(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setDouble(t0, param0);
    }

    public static void expm1f(Environment env) {
        BitVec t0 = env.register.get('0');

        float param0;

        param0 = env.memory.getFloat(t0);

        float ret = CLibrary.INSTANCE.expm1f(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setFloat(t0, param0);
    }

    public static void expm1l(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        double ret = CLibrary.INSTANCE.expm1l(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
    }

    public static void fabs(Environment env) {
        BitVec t0 = env.register.get('0');

        double param0;

        param0 = env.memory.getDouble(t0);

        double ret = CLibrary.INSTANCE.fabs(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setDouble(t0, param0);
    }

    public static void fabsf(Environment env) {
        BitVec t0 = env.register.get('0');

        float param0;

        param0 = env.memory.getFloat(t0);

        float ret = CLibrary.INSTANCE.fabsf(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setFloat(t0, param0);
    }

    public static void fabsl(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        double ret = CLibrary.INSTANCE.fabsl(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
    }

    public static void fchdir(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.fchdir(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void fchmod(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        int param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getInt(t1);

        int ret = CLibrary.INSTANCE.fchmod(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void fchown(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        int param0;
        int param1;
        int param2;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getInt(t1);
        param2 = env.memory.getInt(t2);

        int ret = CLibrary.INSTANCE.fchown(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        ////env.memory.setInt(t1, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void fclose(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        int ret = CLibrary.INSTANCE.fclose(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void fcloseall(Environment env) {

        int ret = CLibrary.INSTANCE.fcloseall();
        env.register.set('0', new BitVec(ret));

    }

    public static void fcntl(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        int param0;
        int param1;
        IntByReference param2;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getInt(t1);
        param2 = env.memory.getIntRef(t2);

        int ret = CLibrary.INSTANCE.fcntl(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        ////env.memory.setInt(t1, param1);
        env.memory.setIntReference(t2, param2.getValue());
    }

    public static void fcvt(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');
        BitVec t3 = env.register.get('3');

        double param0;
        int param1;
        IntByReference param2;
        IntByReference param3;

        param0 = env.memory.getDouble(t0);
        param1 = env.memory.getInt(t1);
        param2 = env.memory.getIntRef(t2);
        param3 = env.memory.getIntRef(t3);

        byte ret = CLibrary.INSTANCE.fcvt(param0, param1, param2, param3);
        env.register.set('0', new BitVec(ret));

        //env.memory.setDouble(t0, param0);
        ////env.memory.setInt(t1, param1);
        env.memory.setIntReference(t2, param2.getValue());
        env.memory.setIntReference(t3, param3.getValue());
    }

    public static void fcvt_r(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        double param0;
        int param1;
        IntByReference param2;

        param0 = env.memory.getDouble(t0);
        param1 = env.memory.getInt(t1);
        param2 = env.memory.getIntRef(t2);

        int ret = CLibrary.INSTANCE.fcvt_r(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        //env.memory.setDouble(t0, param0);
        ////env.memory.setInt(t1, param1);
        env.memory.setIntReference(t2, param2.getValue());
    }

    public static void fdatasync(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.fdatasync(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void fdim(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        double param0;
        double param1;

        param0 = env.memory.getDouble(t0);
        param1 = env.memory.getDouble(t1);

        double ret = CLibrary.INSTANCE.fdim(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setDouble(t0, param0);
        //env.memory.setDouble(t1, param1);
    }

    public static void fdimf(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        float param0;
        float param1;

        param0 = env.memory.getFloat(t0);
        param1 = env.memory.getFloat(t1);

        float ret = CLibrary.INSTANCE.fdimf(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setFloat(t0, param0);
        env.memory.setFloat(t1, param1);
    }

    public static void fdiml(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        NativeLong param0;
        NativeLong param1;

        param0 = env.memory.getNativeLong(t0);
        param1 = env.memory.getNativeLong(t1);

        double ret = CLibrary.INSTANCE.fdiml(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
        //env.memory.setNativeLongt1, param1);
    }

    public static void fdopen(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        String param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getTextFromReference(t1);

        int ret = CLibrary.INSTANCE.fdopen(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        env.memory.setTextReference(t1, param1);
    }

    public static void fdopendir(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.fdopendir(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void FD_CLR(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        IntByReference param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getIntRef(t1);

        CLibrary.INSTANCE.FD_CLR(param0, param1);

        //env.memory.setInt(t0, param0);
        env.memory.setIntReference(t1, param1.getValue());
    }

    public static void FD_ISSET(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        IntByReference param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getIntRef(t1);

        int ret = CLibrary.INSTANCE.FD_ISSET(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        env.memory.setIntReference(t1, param1.getValue());
    }

    public static void FD_SET(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        IntByReference param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getIntRef(t1);

        CLibrary.INSTANCE.FD_SET(param0, param1);

        //env.memory.setInt(t0, param0);
        env.memory.setIntReference(t1, param1.getValue());
    }

    public static void FD_ZERO(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        CLibrary.INSTANCE.FD_ZERO(param0);

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void feclearexcept(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.feclearexcept(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void fedisableexcept(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.fedisableexcept(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void feenableexcept(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.feenableexcept(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void fegetenv(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLongByReference param0;

        param0 = env.memory.getNativeLongRef(t0);

        int ret = CLibrary.INSTANCE.fegetenv(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setNativeLongReference(t0, param0.getValue());
    }

    public static void fegetexceptflag(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        ShortByReference param0;
        int param1;

        param0 = env.memory.getShortRef(t0);
        param1 = env.memory.getInt(t1);

        int ret = CLibrary.INSTANCE.fegetexceptflag(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setShortReference(t0, param0.getValue());
        ////env.memory.setInt(t1, param1);
    }

    public static void fegetmode(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLongByReference param0;

        param0 = env.memory.getNativeLongRef(t0);

        int ret = CLibrary.INSTANCE.fegetmode(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setNativeLongReference(t0, param0.getValue());
    }

    public static void fegetround(Environment env) {

        int ret = CLibrary.INSTANCE.fegetround();
        env.register.set('0', new BitVec(ret));

    }

    public static void feholdexcept(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLongByReference param0;

        param0 = env.memory.getNativeLongRef(t0);

        int ret = CLibrary.INSTANCE.feholdexcept(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setNativeLongReference(t0, param0.getValue());
    }

    public static void feof(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        int ret = CLibrary.INSTANCE.feof(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void feof_unlocked(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        int ret = CLibrary.INSTANCE.feof_unlocked(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void feraiseexcept(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.feraiseexcept(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void ferror(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        int ret = CLibrary.INSTANCE.ferror(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void ferror_unlocked(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        int ret = CLibrary.INSTANCE.ferror_unlocked(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void fesetenv(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLongByReference param0;

        param0 = env.memory.getNativeLongRef(t0);

        int ret = CLibrary.INSTANCE.fesetenv(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setNativeLongReference(t0, param0.getValue());
    }

    public static void fesetexcept(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.fesetexcept(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void fesetexceptflag(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        ShortByReference param0;
        int param1;

        param0 = env.memory.getShortRef(t0);
        param1 = env.memory.getInt(t1);

        int ret = CLibrary.INSTANCE.fesetexceptflag(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setShortReference(t0, param0.getValue());
        ////env.memory.setInt(t1, param1);
    }

    public static void fesetmode(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLongByReference param0;

        param0 = env.memory.getNativeLongRef(t0);

        int ret = CLibrary.INSTANCE.fesetmode(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setNativeLongReference(t0, param0.getValue());
    }

    public static void fesetround(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.fesetround(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void fetestexcept(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.fetestexcept(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void fetestexceptflag(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        ShortByReference param0;
        int param1;

        param0 = env.memory.getShortRef(t0);
        param1 = env.memory.getInt(t1);

        int ret = CLibrary.INSTANCE.fetestexceptflag(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setShortReference(t0, param0.getValue());
        ////env.memory.setInt(t1, param1);
    }

    public static void feupdateenv(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLongByReference param0;

        param0 = env.memory.getNativeLongRef(t0);

        int ret = CLibrary.INSTANCE.feupdateenv(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setNativeLongReference(t0, param0.getValue());
    }

    public static void fexecve(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.fexecve(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void fflush(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        int ret = CLibrary.INSTANCE.fflush(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void fflush_unlocked(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        int ret = CLibrary.INSTANCE.fflush_unlocked(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void fgetc(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        int ret = CLibrary.INSTANCE.fgetc(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void fgetc_unlocked(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        int ret = CLibrary.INSTANCE.fgetc_unlocked(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void fgetgrent(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        group ret = CLibrary.INSTANCE.fgetgrent(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void fgetpos(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        IntByReference param0;
        IntByReference param1;

        param0 = env.memory.getIntRef(t0);
        param1 = env.memory.getIntRef(t1);

        int ret = CLibrary.INSTANCE.fgetpos(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
        env.memory.setIntReference(t1, param1.getValue());
    }

    public static void fgetpwent(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        passwd ret = CLibrary.INSTANCE.fgetpwent(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void fgetpwent_r(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        int ret = CLibrary.INSTANCE.fgetpwent_r(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void fgets(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        String param0;
        int param1;
        IntByReference param2;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getInt(t1);
        param2 = env.memory.getIntRef(t2);

        byte ret = CLibrary.INSTANCE.fgets(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        ////env.memory.setInt(t1, param1);
        env.memory.setIntReference(t2, param2.getValue());
    }

    public static void fgets_unlocked(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        String param0;
        int param1;
        IntByReference param2;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getInt(t1);
        param2 = env.memory.getIntRef(t2);

        byte ret = CLibrary.INSTANCE.fgets_unlocked(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        ////env.memory.setInt(t1, param1);
        env.memory.setIntReference(t2, param2.getValue());
    }

    public static void fgetwc_unlocked(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        int ret = CLibrary.INSTANCE.fgetwc_unlocked(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void fgetws(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        String param0;
        int param1;
        IntByReference param2;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getInt(t1);
        param2 = env.memory.getIntRef(t2);

        char ret = CLibrary.INSTANCE.fgetws(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        ////env.memory.setInt(t1, param1);
        env.memory.setIntReference(t2, param2.getValue());
    }

    public static void fgetws_unlocked(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        String param0;
        int param1;
        IntByReference param2;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getInt(t1);
        param2 = env.memory.getIntRef(t2);

        char ret = CLibrary.INSTANCE.fgetws_unlocked(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        ////env.memory.setInt(t1, param1);
        env.memory.setIntReference(t2, param2.getValue());
    }

    public static void fileno(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        int ret = CLibrary.INSTANCE.fileno(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void fileno_unlocked(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        int ret = CLibrary.INSTANCE.fileno_unlocked(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void finite(Environment env) {
        BitVec t0 = env.register.get('0');

        double param0;

        param0 = env.memory.getDouble(t0);

        int ret = CLibrary.INSTANCE.finite(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setDouble(t0, param0);
    }

    public static void finitef(Environment env) {
        BitVec t0 = env.register.get('0');

        float param0;

        param0 = env.memory.getFloat(t0);

        int ret = CLibrary.INSTANCE.finitef(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setFloat(t0, param0);
    }

    public static void finitel(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        int ret = CLibrary.INSTANCE.finitel(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
    }

    public static void flockfile(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        CLibrary.INSTANCE.flockfile(param0);

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void floor(Environment env) {
        BitVec t0 = env.register.get('0');

        double param0;

        param0 = env.memory.getDouble(t0);

        double ret = CLibrary.INSTANCE.floor(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setDouble(t0, param0);
    }

    public static void floorf(Environment env) {
        BitVec t0 = env.register.get('0');

        float param0;

        param0 = env.memory.getFloat(t0);

        float ret = CLibrary.INSTANCE.floorf(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setFloat(t0, param0);
    }

    public static void floorl(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        double ret = CLibrary.INSTANCE.floorl(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
    }

    public static void fma(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        double param0;
        double param1;
        double param2;

        param0 = env.memory.getDouble(t0);
        param1 = env.memory.getDouble(t1);
        param2 = env.memory.getDouble(t2);

        double ret = CLibrary.INSTANCE.fma(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        //env.memory.setDouble(t0, param0);
        //env.memory.setDouble(t1, param1);
        //env.memory.setDouble(t2, param2);
    }

    public static void fmaf(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        float param0;
        float param1;
        float param2;

        param0 = env.memory.getFloat(t0);
        param1 = env.memory.getFloat(t1);
        param2 = env.memory.getFloat(t2);

        float ret = CLibrary.INSTANCE.fmaf(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setFloat(t0, param0);
        env.memory.setFloat(t1, param1);
        env.memory.setFloat(t2, param2);
    }

    public static void fmal(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        NativeLong param0;
        NativeLong param1;
        NativeLong param2;

        param0 = env.memory.getNativeLong(t0);
        param1 = env.memory.getNativeLong(t1);
        param2 = env.memory.getNativeLong(t2);

        double ret = CLibrary.INSTANCE.fmal(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
        //env.memory.setNativeLongt1, param1);
        //env.memory.setNativeLongt2, param2);
    }

    public static void fmax(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        double param0;
        double param1;

        param0 = env.memory.getDouble(t0);
        param1 = env.memory.getDouble(t1);

        double ret = CLibrary.INSTANCE.fmax(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setDouble(t0, param0);
        //env.memory.setDouble(t1, param1);
    }

    public static void fmemopen(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        char[] param0;
        int param1;
        byte[] param2;

        param1 = env.memory.getInt(t1);
        param0 = env.memory.getBuffer(t0, param1);
        param2 = env.memory.getByteArray(t2, param1);

        int ret = CLibrary.INSTANCE.fmemopen(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setBuffer(t0, param1, param0);
        ////env.memory.setInt(t1, param1);
        env.memory.setByteArray(t2, param1, param2);
    }

    public static void fmin(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        double param0;
        double param1;

        param0 = env.memory.getDouble(t0);
        param1 = env.memory.getDouble(t1);

        double ret = CLibrary.INSTANCE.fmin(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setDouble(t0, param0);
        //env.memory.setDouble(t1, param1);
    }

    public static void fminf(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        float param0;
        float param1;

        param0 = env.memory.getFloat(t0);
        param1 = env.memory.getFloat(t1);

        float ret = CLibrary.INSTANCE.fminf(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setFloat(t0, param0);
        env.memory.setFloat(t1, param1);
    }

    public static void fminl(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        NativeLong param0;
        NativeLong param1;

        param0 = env.memory.getNativeLong(t0);
        param1 = env.memory.getNativeLong(t1);

        double ret = CLibrary.INSTANCE.fminl(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
        //env.memory.setNativeLongt1, param1);
    }

    public static void fmod(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        double param0;
        double param1;

        param0 = env.memory.getDouble(t0);
        param1 = env.memory.getDouble(t1);

        double ret = CLibrary.INSTANCE.fmod(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setDouble(t0, param0);
        //env.memory.setDouble(t1, param1);
    }

    public static void fmodf(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        float param0;
        float param1;

        param0 = env.memory.getFloat(t0);
        param1 = env.memory.getFloat(t1);

        float ret = CLibrary.INSTANCE.fmodf(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setFloat(t0, param0);
        env.memory.setFloat(t1, param1);
    }

    public static void fmodl(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        NativeLong param0;
        NativeLong param1;

        param0 = env.memory.getNativeLong(t0);
        param1 = env.memory.getNativeLong(t1);

        double ret = CLibrary.INSTANCE.fmodl(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
        //env.memory.setNativeLongt1, param1);
    }

    public static void fnmatch(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        String param0;
        String param1;
        int param2;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getTextFromReference(t1);
        param2 = env.memory.getInt(t2);

        int ret = CLibrary.INSTANCE.fnmatch(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setTextReference(t1, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void fopen(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        String param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getTextFromReference(t1);

        int ret = CLibrary.INSTANCE.fopen(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setTextReference(t1, param1);
    }

    public static void fopen64(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        int ret = CLibrary.INSTANCE.fopen64(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void fopencookie(Environment env) {
        BitVec t0 = env.register.get('0');

        LongByReference param0;

        param0 = env.memory.getPointer(t0);

        int ret = CLibrary.INSTANCE.fopencookie(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setPointer(t0, param0.getValue());
    }

    public static void fork(Environment env) {
        //int ret = CLibrary.INSTANCE.fork();
        int ret = 0;
        env.register.set('1', new BitVec(ret));


        //env.register.set('0', new BitVec(SysUtils.addSymVar()));
    }

    public static void forkpty(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        IntByReference param0;
        String param1;

        param0 = env.memory.getIntRef(t0);
        param1 = env.memory.getTextFromReference(t1);

        int ret = CLibrary.INSTANCE.forkpty(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
        env.memory.setTextReference(t1, param1);
    }

    public static void fputc_unlocked(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        IntByReference param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getIntRef(t1);

        int ret = CLibrary.INSTANCE.fputc_unlocked(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        env.memory.setIntReference(t1, param1.getValue());
    }

    public static void fputs_unlocked(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        IntByReference param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getIntRef(t1);

        int ret = CLibrary.INSTANCE.fputs_unlocked(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setIntReference(t1, param1.getValue());
    }

    public static void fputws(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        IntByReference param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getIntRef(t1);

        int ret = CLibrary.INSTANCE.fputws(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setIntReference(t1, param1.getValue());
    }

    public static void fputws_unlocked(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        IntByReference param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getIntRef(t1);

        int ret = CLibrary.INSTANCE.fputws_unlocked(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setIntReference(t1, param1.getValue());
    }

    public static void fread(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');
        BitVec t3 = env.register.get('3');

        LongByReference param0;
        int param1;
        int param2;
        int[] param3;

        param0 = env.memory.getPointer(t0);
        param1 = env.memory.getInt(t1);
        param2 = env.memory.getInt(t2);
        param3 = env.memory.getIntArray(t3, param1);

        int ret = CLibrary.INSTANCE.fread(param0, param1, param2, param3);
        env.register.set('0', new BitVec(ret));

        env.memory.setPointer(t0, param0.getValue());
        ////env.memory.setInt(t1, param1);
        //env.memory.setInt(t2, param2);
        env.memory.setIntArray(t3, param1, param3);
    }

    public static void fread_unlocked(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        LongByReference param0;
        int param1;

        param0 = env.memory.getPointer(t0);
        param1 = env.memory.getInt(t1);

        int ret = CLibrary.INSTANCE.fread_unlocked(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setPointer(t0, param0.getValue());
        ////env.memory.setInt(t1, param1);
    }

    public static void free(Environment env) {
        BitVec t0 = env.register.get('0');

        LongByReference param0;

        param0 = env.memory.getPointer(t0);

        //CLibrary.INSTANCE.free(param0);

        env.memory.setPointer(t0, param0.getValue());
    }

    public static void freopen(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        String param0;
        String param1;
        IntByReference param2;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getTextFromReference(t1);
        param2 = env.memory.getIntRef(t2);

        int ret = CLibrary.INSTANCE.freopen(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setTextReference(t1, param1);
        env.memory.setIntReference(t2, param2.getValue());
    }

    public static void freopen64(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        int ret = CLibrary.INSTANCE.freopen64(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void frexp(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        double param0;
        IntByReference param1;

        param0 = env.memory.getDouble(t0);
        param1 = env.memory.getIntRef(t1);

        double ret = CLibrary.INSTANCE.frexp(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setDouble(t0, param0);
        env.memory.setIntReference(t1, param1.getValue());
    }

    public static void frexpf(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        float param0;
        IntByReference param1;

        param0 = env.memory.getFloat(t0);
        param1 = env.memory.getIntRef(t1);

        float ret = CLibrary.INSTANCE.frexpf(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setFloat(t0, param0);
        env.memory.setIntReference(t1, param1.getValue());
    }

    public static void frexpl(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        NativeLong param0;
        IntByReference param1;

        param0 = env.memory.getNativeLong(t0);
        param1 = env.memory.getIntRef(t1);

        double ret = CLibrary.INSTANCE.frexpl(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
        env.memory.setIntReference(t1, param1.getValue());
    }

    public static void fseek(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        IntByReference param0;
        NativeLong param1;
        int param2;

        param0 = env.memory.getIntRef(t0);
        param1 = env.memory.getNativeLong(t1);
        param2 = env.memory.getInt(t2);

        int ret = CLibrary.INSTANCE.fseek(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
        //env.memory.setNativeLongt1, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void fseeko(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        IntByReference param0;
        NativeLong param1;
        int param2;

        param0 = env.memory.getIntRef(t0);
        param1 = env.memory.getNativeLong(t1);
        param2 = env.memory.getInt(t2);

        int ret = CLibrary.INSTANCE.fseeko(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
        //env.memory.setNativeLongt1, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void fsetpos(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        IntByReference param0;
        IntByReference param1;

        param0 = env.memory.getIntRef(t0);
        param1 = env.memory.getIntRef(t1);

        int ret = CLibrary.INSTANCE.fsetpos(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
        env.memory.setIntReference(t1, param1.getValue());
    }

    /*public static void fstat (Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0 ;
        stat param1 = new stat();

        param0 = env.memory.getInt(t0);
        param1.st_dev = env.memory.getIntFromReference(t1);
        param1.st_ino = env.memory.getIntFromReference(t1.add(4));
        param1.st_mode = env.memory.getIntFromReference(t1.add(8));
        param1.st_nlink = env.memory.getIntFromReference(t1.add(12));
        param1.st_uid = env.memory.getIntFromReference(t1.add(16));
        param1.st_gid = env.memory.getIntFromReference(t1.add(20));
        param1.st_rdev = env.memory.getIntFromReference(t1.add(24));
        param1.st_size = env.memory.getNativeLongFromReference(t1.add(28));
        param1.st_blksize = env.memory.getIntFromReference(t1.add(32));
        param1.st_blocks = env.memory.getIntFromReference(t1.add(36));
        param1.st_atim = env.memory.getIntFromReference(t1.add(40));
        param1.st_mtim = env.memory.getIntFromReference(t1.add(44));
        param1.st_ctim = env.memory.getIntFromReference(t1.add(48));
        param1.st_ctim.tv_sec = env.memory.getIntFromReference(t1.add(52));

        int ret = CLibrary.INSTANCE.fstat(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        env.memory.setIntReference(t1, param1.st_dev);
        env.memory.setIntReference(t1.add(4), param1.st_ino);
        env.memory.setIntReference(t1.add(8), param1.st_mode);
        env.memory.setIntReference(t1.add(12), param1.st_nlink);
        env.memory.setIntReference(t1.add(16), param1.st_uid);
        env.memory.setIntReference(t1.add(20), param1.st_gid);
        env.memory.setIntReference(t1.add(24), param1.st_rdev);
        env.memory.setNativeLongReference(t1.add(28), param1.st_size);
        env.memory.setIntReference(t1.add(32), param1.st_blksize);
        env.memory.setIntReference(t1.add(36), param1.st_blocks);
        env.memory.setIntReference(t1.add(40), param1.st_atim);
        env.memory.setIntReference(t1.add(44), param1.st_mtim);
        env.memory.setIntReference(t1.add(48), param1.st_ctim);
        env.memory.setIntReference(t1.add(52), param1.st_ctim.tv_sec);
    }
*/
    public static void fstat64(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        stat64 param1 = new stat64();

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.fstat64(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void fsync(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.fsync(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void ftell(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        NativeLong ret = CLibrary.INSTANCE.ftell(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void ftello(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        NativeLong ret = CLibrary.INSTANCE.ftello(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void ftello64(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        NativeLong ret = CLibrary.INSTANCE.ftello64(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void ftruncate(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        NativeLong param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getNativeLong(t1);

        int ret = CLibrary.INSTANCE.ftruncate(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        //env.memory.setNativeLongt1, param1);
    }

    public static void ftruncate64(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        NativeLong param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getNativeLong(t1);

        int ret = CLibrary.INSTANCE.ftruncate64(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        //env.memory.setNativeLongt1, param1);
    }

    public static void ftrylockfile(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        int ret = CLibrary.INSTANCE.ftrylockfile(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void ftw(Environment env) {
        BitVec t0 = env.register.get('0');

        FTW param0 = new FTW();

        param0.level = env.memory.getInt(t0);
        param0.base = env.memory.getInt(t0.add(4));

        FTW ret = CLibrary.INSTANCE.ftw(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0.level);
    }

    public static void ftw64(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        int param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getInt(t1);

        int ret = CLibrary.INSTANCE.ftw64(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void funlockfile(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        CLibrary.INSTANCE.funlockfile(param0);

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void futimes(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        timeval[] param1;

        param0 = env.memory.getInt(t0);
        param1 = (timeval[]) env.memory.getArray(t1, 2);

        int ret = CLibrary.INSTANCE.futimes(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        env.memory.setArray(t1, 2, param1);
    }

    public static void fwide(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        IntByReference param0;
        int param1;

        param0 = env.memory.getIntRef(t0);
        param1 = env.memory.getInt(t1);

        int ret = CLibrary.INSTANCE.fwide(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
        ////env.memory.setInt(t1, param1);
    }

    public static void gamma(Environment env) {
        BitVec t0 = env.register.get('0');

        double param0;

        param0 = env.memory.getDouble(t0);

        double ret = CLibrary.INSTANCE.gamma(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setDouble(t0, param0);
    }

    public static void gammaf(Environment env) {
        BitVec t0 = env.register.get('0');

        float param0;

        param0 = env.memory.getFloat(t0);

        float ret = CLibrary.INSTANCE.gammaf(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setFloat(t0, param0);
    }

    public static void gammal(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        double ret = CLibrary.INSTANCE.gammal(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
    }

    public static void gcvt(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        double param0;
        int param1;
        String param2;

        param0 = env.memory.getDouble(t0);
        param1 = env.memory.getInt(t1);
        param2 = env.memory.getTextFromReference(t2);

        byte ret = CLibrary.INSTANCE.gcvt(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        //env.memory.setDouble(t0, param0);
        ////env.memory.setInt(t1, param1);
        env.memory.setTextReference(t2, param2);
    }

    public static void getauxval(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        NativeLong ret = CLibrary.INSTANCE.getauxval(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
    }


    public static void getchar(Environment env) {
        int ret = CLibrary.INSTANCE.getchar();
        env.register.set('0', new BitVec(ret));
    }

    public static void getchar_unlocked(Environment env) {
        int ret = CLibrary.INSTANCE.getchar_unlocked();
        env.register.set('0', new BitVec(ret));
    }

    public static void getcontext(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        int ret = CLibrary.INSTANCE.getcontext(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
    }

//    public static void getcpu (Environment env) {
//        BitVec t0 = env.register.get('0');
//        BitVec t1 = env.register.get('1');
//        BitVec t2 = env.register.get('2');
//
//        IntByReference param0 ;
//        IntByReference param1 ;
//        getcpu_cache param2 = new getcpu_cache();
//
//        param0 = env.memory.getIntRef(t0);
//        param1 = env.memory.getIntRef(t1);
//
//        int ret = CLibrary.INSTANCE.getcpu(param0, param1, param2);
//        env.register.set('0', new BitVec(ret));
//
//
//        env.memory.setIntReference(t0, param0.getValue());
//        env.memory.setIntReference(t1, param1.getValue());
//    }

    public static void getcwd(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        byte[] param0;
        int param1;

        param1 = env.memory.getInt(t1);
        param0 = env.memory.getByteArray(t0, param1);

        byte ret = CLibrary.INSTANCE.getcwd(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setByteArray(t0, param1, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void getc_unlocked(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        int ret = CLibrary.INSTANCE.getc_unlocked(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void getdate(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        tm ret = CLibrary.INSTANCE.getdate(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void getdate_r(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        tm param1 = new tm();

        param0 = env.memory.getTextFromReference(t0);
        param1.tm_sec = env.memory.getIntFromReference(t1);
        param1.tm_min = env.memory.getIntFromReference(t1.add(4));
        param1.tm_hour = env.memory.getIntFromReference(t1.add(8));
        param1.tm_mday = env.memory.getIntFromReference(t1.add(12));
        param1.tm_mon = env.memory.getIntFromReference(t1.add(16));
        param1.tm_year = env.memory.getIntFromReference(t1.add(20));
        param1.tm_wday = env.memory.getIntFromReference(t1.add(24));
        param1.tm_yday = env.memory.getIntFromReference(t1.add(28));
        param1.tm_isdst = env.memory.getIntFromReference(t1.add(32));
        param1.__tm_gmtoff = env.memory.getNativeLongFromReference(t1.add(36));
        param1.__tm_zone = env.memory.getStringFromReference(t1.add(40));

        int ret = CLibrary.INSTANCE.getdate_r(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setIntReference(t1, param1.tm_sec);
        env.memory.setIntReference(t1.add(4), param1.tm_min);
        env.memory.setIntReference(t1.add(8), param1.tm_hour);
        env.memory.setIntReference(t1.add(12), param1.tm_mday);
        env.memory.setIntReference(t1.add(16), param1.tm_mon);
        env.memory.setIntReference(t1.add(20), param1.tm_year);
        env.memory.setIntReference(t1.add(24), param1.tm_wday);
        env.memory.setIntReference(t1.add(28), param1.tm_yday);
        env.memory.setIntReference(t1.add(32), param1.tm_isdst);
        env.memory.setNativeLongReference(t1.add(36), param1.__tm_gmtoff);
        env.memory.setStringReference(t1.add(40), param1.__tm_zone);
    }

//    public static void getdelim (Environment env) {
//        BitVec t0 = env.register.get('0');
//        BitVec t1 = env.register.get('1');
//        BitVec t2 = env.register.get('2');
//        BitVec t3 = env.register.get('3');
//
//        String[] param0 ;
//        int[] param1 ;
//        int param2 ;
//        int[] param3 ;
//
//        param0 = (String[]) env.memory.getArray(t0, -1);
//        param1 = env.memory.getIntArray(t1, param1);
//        param2 = env.memory.getInt(t2);
//        param3 = env.memory.getIntArray(t3, param1);
//
//        int ret = CLibrary.INSTANCE.getdelim(param0, param1, param2, param3);
//        env.register.set('0', new BitVec(ret));
//
//
//        env.memory.setArray(t0, -1, param0);
//        env.memory.setIntArray(t1, param1, param1);
//        //env.memory.setInt(t2, param2);
//        env.memory.setIntArray(t3, param1, param3);
//    }

    public static void getdents64(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        int param0;
        char[] param1;
        int param2;

        param0 = env.memory.getInt(t0);
        param2 = env.memory.getInt(t2);
        param1 = env.memory.getBuffer(t1, param2);

        int ret = CLibrary.INSTANCE.getdents64(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        env.memory.setBuffer(t1, param2, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void getegid(Environment env) {

        int ret = CLibrary.INSTANCE.getegid();
        env.register.set('0', new BitVec(ret));

    }

    public static void getentropy(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        char[] param0;
        int param1;

        param1 = env.memory.getInt(t1);
        param0 = env.memory.getBuffer(t0, param1);

        int ret = CLibrary.INSTANCE.getentropy(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setBuffer(t0, param1, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void getenv(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        byte ret = CLibrary.INSTANCE.getenv(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void geteuid(Environment env) {
        int ret = CLibrary.INSTANCE.geteuid();
        env.register.set('0', new BitVec(ret));

    }

    public static void getfsent(Environment env) {
        fstab ret = CLibrary.INSTANCE.getfsent();
        env.register.set('0', new BitVec(ret));

    }

    public static void getfsfile(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        fstab ret = CLibrary.INSTANCE.getfsfile(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void getfsspec(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        fstab ret = CLibrary.INSTANCE.getfsspec(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void getgid(Environment env) {

        int ret = CLibrary.INSTANCE.getgid();
        env.register.set('0', new BitVec(ret));

    }

    public static void getgrent(Environment env) {

        group ret = CLibrary.INSTANCE.getgrent();
        env.register.set('0', new BitVec(ret));

    }

    public static void getgrgid(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        group ret = CLibrary.INSTANCE.getgrgid(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void getgrnam(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        group ret = CLibrary.INSTANCE.getgrnam(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void getgroups(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.getgroups(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void gethostbyname(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        hostent ret = CLibrary.INSTANCE.gethostbyname(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void gethostbyname2(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        int param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getInt(t1);

        hostent ret = CLibrary.INSTANCE.gethostbyname2(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void gethostent(Environment env) {

        hostent ret = CLibrary.INSTANCE.gethostent();
        env.register.set('0', new BitVec(ret));

    }

    public static void gethostid(Environment env) {

        NativeLong ret = CLibrary.INSTANCE.gethostid();
        env.register.set('0', new BitVec(ret));

    }

    public static void gethostname(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        byte[] param0;
        int param1;

        param1 = env.memory.getInt(t1);
        param0 = env.memory.getByteArray(t0, param1);

        int ret = CLibrary.INSTANCE.gethostname(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setByteArray(t0, param1, param0);
        ////env.memory.setInt(t1, param1);
    }

//    public static void getitimer (Environment env) {
//        BitVec t0 = env.register.get('0');
//        BitVec t1 = env.register.get('1');
//
//        int param0 ;
//        itimerval param1 = new itimerval();
//
//        param0 = env.memory.getInt(t0);
//        param1.it_interval = env.memory.getIntRef(t1);
//        param1.it_value = env.memory.getIntRef(t1.add(4));
//
//        int ret = CLibrary.INSTANCE.getitimer(param0, param1);
//        env.register.set('0', new BitVec(ret));
//
//
//        //env.memory.setInt(t0, param0);
//        env.memory.setIntReference(t1, param1.it_interval);
//        env.memory.setIntReference(t1.add(4), param1.it_value);
//    }

//    public static void getline (Environment env) {
//        BitVec t0 = env.register.get('0');
//        BitVec t1 = env.register.get('1');
//        BitVec t2 = env.register.get('2');
//
//        String[] param0 ;
//        int[] param1 ;
//        int[] param2 ;
//
//        param0 = (String[]) env.memory.getArray(t0, -1);
//        param1 = env.memory.getIntArray(t1, param1);
//        param2 = env.memory.getIntArray(t2, param1);
//
//        int ret = CLibrary.INSTANCE.getline(param0, param1, param2);
//        env.register.set('0', new BitVec(ret));
//
//
//        env.memory.setArray(t0, -1, param0);
//        env.memory.setIntArray(t1, param1, param1);
//        env.memory.setIntArray(t2, param1, param2);
//    }

    public static void getloadavg(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.getloadavg(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void getlogin(Environment env) {

        byte ret = CLibrary.INSTANCE.getlogin();
        env.register.set('0', new BitVec(ret));

    }

    public static void getmntent(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        mntent ret = CLibrary.INSTANCE.getmntent(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void getmntent_r(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        mntent ret = CLibrary.INSTANCE.getmntent_r(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void getnetbyaddr(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        int param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getInt(t1);

        netent ret = CLibrary.INSTANCE.getnetbyaddr(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void getnetbyname(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        netent ret = CLibrary.INSTANCE.getnetbyname(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void getnetent(Environment env) {

        netent ret = CLibrary.INSTANCE.getnetent();
        env.register.set('0', new BitVec(ret));

    }

    public static void getnetgrent(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        String[] param0;
        String[] param1;
        String[] param2;

        param0 = (String[]) env.memory.getArray(t0, -1);
        param1 = (String[]) env.memory.getArray(t1, -1);
        param2 = (String[]) env.memory.getArray(t2, -1);

        int ret = CLibrary.INSTANCE.getnetgrent(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setArray(t0, -1, param0);
        env.memory.setArray(t1, -1, param1);
        env.memory.setArray(t2, -1, param2);
    }

    public static void getopt_long(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        String param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getTextFromReference(t1);

        int ret = CLibrary.INSTANCE.getopt_long(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        env.memory.setTextReference(t1, param1);
    }

    public static void getopt_long_only(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        String param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getTextFromReference(t1);

        int ret = CLibrary.INSTANCE.getopt_long_only(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        env.memory.setTextReference(t1, param1);
    }

    public static void getpagesize(Environment env) {

        int ret = CLibrary.INSTANCE.getpagesize();
        env.register.set('0', new BitVec(ret));

    }

    public static void getpass(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        byte ret = CLibrary.INSTANCE.getpass(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void getpeername(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        int param0;
        sockaddr param1 = new sockaddr();
        IntByReference param2;

        param0 = env.memory.getInt(t0);
        param1.sa_ = env.memory.getIntFromReference(t1);
        param1.sa_data = env.memory.getIntArray(t1.add(4), 14);
        param2 = env.memory.getIntRef(t2);

        int ret = CLibrary.INSTANCE.getpeername(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        env.memory.setIntReference(t1, param1.sa_);
        env.memory.setIntArray(t1.add(4), 14, param1.sa_data);
        env.memory.setIntReference(t2, param2.getValue());
    }

    public static void getpgid(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.getpgid(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void getpgrp(Environment env) {

        int ret = CLibrary.INSTANCE.getpgrp();
        env.register.set('0', new BitVec(ret));

    }

    public static void getpid(Environment env) {

        int ret = CLibrary.INSTANCE.getpid();
        env.register.set('0', new BitVec(ret));

    }

    public static void getppid(Environment env) {

        int ret = CLibrary.INSTANCE.getppid();
        env.register.set('0', new BitVec(ret));

    }

    public static void getpriority(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        int param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getInt(t1);

        int ret = CLibrary.INSTANCE.getpriority(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void getprotobyname(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        protoent ret = CLibrary.INSTANCE.getprotobyname(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void getprotobynumber(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        protoent ret = CLibrary.INSTANCE.getprotobynumber(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void getprotoent(Environment env) {

        protoent ret = CLibrary.INSTANCE.getprotoent();
        env.register.set('0', new BitVec(ret));

    }

    public static void getpt(Environment env) {

        int ret = CLibrary.INSTANCE.getpt();
        env.register.set('0', new BitVec(ret));

    }

    public static void getpwent(Environment env) {

        passwd ret = CLibrary.INSTANCE.getpwent();
        env.register.set('0', new BitVec(ret));

    }

    public static void getpwent_r(Environment env) {
        BitVec t0 = env.register.get('0');

        passwd param0 = new passwd();

        param0.pw_name = env.memory.getPointer(t0);
        param0.pw_passwd = env.memory.getPointer(t0.add(4));
        param0.pw_uid = env.memory.getIntFromReference(t0.add(8));
        param0.pw_gid = env.memory.getIntFromReference(t0.add(12));
        param0.pw_gecos = env.memory.getPointer(t0.add(16));
        param0.pw_dir = env.memory.getPointer(t0.add(20));
        param0.pw_shell = env.memory.getPointer(t0.add(24));

        int ret = CLibrary.INSTANCE.getpwent_r(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setPointer(t0, param0.pw_name);
        env.memory.setPointer(t0.add(4), param0.pw_passwd);
        env.memory.setIntReference(t0.add(8), param0.pw_uid);
        env.memory.setIntReference(t0.add(12), param0.pw_gid);
        env.memory.setPointer(t0.add(16), param0.pw_gecos);
        env.memory.setPointer(t0.add(20), param0.pw_dir);
        env.memory.setPointer(t0.add(24), param0.pw_shell);
    }

    public static void getpwnam(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        passwd ret = CLibrary.INSTANCE.getpwnam(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void getpwnam_r(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        int ret = CLibrary.INSTANCE.getpwnam_r(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void getpwuid(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        passwd ret = CLibrary.INSTANCE.getpwuid(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void getpwuid_r(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.getpwuid_r(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void getrandom(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        char[] param0;
        int param1;
        int param2;

        param1 = env.memory.getInt(t1);
        param0 = env.memory.getBuffer(t0, param1);
        param2 = env.memory.getInt(t2);

        int ret = CLibrary.INSTANCE.getrandom(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setBuffer(t0, param1, param0);
        ////env.memory.setInt(t1, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void getrlimit(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        rlimit param1 = new rlimit();

        param0 = env.memory.getInt(t0);
        param1.rlim_cur = env.memory.getIntFromReference(t1);
        param1.rlim_max = env.memory.getIntFromReference(t1.add(4));

        int ret = CLibrary.INSTANCE.getrlimit(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        env.memory.setIntReference(t1, param1.rlim_cur);
        env.memory.setIntReference(t1.add(4), param1.rlim_max);
    }

    public static void getrlimit64(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.getrlimit64(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

//    public static void getrusage (Environment env) {
//        BitVec t0 = env.register.get('0');
//        BitVec t1 = env.register.get('1');
//
//        int param0 ;
//        rusage param1 = new rusage();
//
//        param0 = env.memory.getInt(t0);
//        param1.ru_utime = env.memory.getIntFromReference(t1);
//        param1.ru_stime = env.memory.getIntFromReference(t1.add(4));
//        param1.ru_maxrss = env.memory.getNativeLongFromReference(t1.add(8));
//        param1.ru_ixrss = env.memory.getNativeLongFromReference(t1.add(12));
//        param1.ru_idrss = env.memory.getNativeLongFromReference(t1.add(16));
//        param1.ru_isrss = env.memory.getNativeLongFromReference(t1.add(20));
//        param1.ru_minflt = env.memory.getNativeLongFromReference(t1.add(24));
//        param1.ru_majflt = env.memory.getNativeLongFromReference(t1.add(28));
//        param1.ru_nswap = env.memory.getNativeLongFromReference(t1.add(32));
//        param1.ru_inblock = env.memory.getNativeLongFromReference(t1.add(36));
//        param1.ru_oublock = env.memory.getNativeLongFromReference(t1.add(40));
//        param1.ru_msgsnd = env.memory.getNativeLongFromReference(t1.add(44));
//        param1.ru_msgrcv = env.memory.getNativeLongFromReference(t1.add(48));
//        param1.ru_nsignals = env.memory.getNativeLongFromReference(t1.add(52));
//        param1.ru_nvcsw = env.memory.getNativeLongFromReference(t1.add(56));
//        param1.ru_nivcsw = env.memory.getNativeLongFromReference(t1.add(60));
//
//        int ret = CLibrary.INSTANCE.getrusage(param0, param1);
//        env.register.set('0', new BitVec(ret));
//
//
//        //env.memory.setInt(t0, param0);
//        env.memory.setIntReference(t1, param1.ru_utime);
//        env.memory.setIntReference(t1.add(4), param1.ru_stime);
//        env.memory.setNativeLongReference(t1.add(8), param1.ru_maxrss);
//        env.memory.setNativeLongReference(t1.add(12), param1.ru_ixrss);
//        env.memory.setNativeLongReference(t1.add(16), param1.ru_idrss);
//        env.memory.setNativeLongReference(t1.add(20), param1.ru_isrss);
//        env.memory.setNativeLongReference(t1.add(24), param1.ru_minflt);
//        env.memory.setNativeLongReference(t1.add(28), param1.ru_majflt);
//        env.memory.setNativeLongReference(t1.add(32), param1.ru_nswap);
//        env.memory.setNativeLongReference(t1.add(36), param1.ru_inblock);
//        env.memory.setNativeLongReference(t1.add(40), param1.ru_oublock);
//        env.memory.setNativeLongReference(t1.add(44), param1.ru_msgsnd);
//        env.memory.setNativeLongReference(t1.add(48), param1.ru_msgrcv);
//        env.memory.setNativeLongReference(t1.add(52), param1.ru_nsignals);
//        env.memory.setNativeLongReference(t1.add(56), param1.ru_nvcsw);
//        env.memory.setNativeLongReference(t1.add(60), param1.ru_nivcsw);
//    }

    public static void gets(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        byte ret = CLibrary.INSTANCE.gets(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void getservbyname(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        String param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getTextFromReference(t1);

        servent ret = CLibrary.INSTANCE.getservbyname(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setTextReference(t1, param1);
    }

    public static void getservbyport(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        String param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getTextFromReference(t1);

        servent ret = CLibrary.INSTANCE.getservbyport(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        env.memory.setTextReference(t1, param1);
    }

    public static void getservent(Environment env) {

        servent ret = CLibrary.INSTANCE.getservent();
        env.register.set('0', new BitVec(ret));

    }

    public static void getsid(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.getsid(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void getsockname(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        int param0;
        sockaddr param1 = new sockaddr();
        IntByReference param2;

        param0 = env.memory.getInt(t0);
        param1.sa_ = env.memory.getIntFromReference(t1);
        param1.sa_data = env.memory.getIntArray(t1.add(4), 14);
        param2 = env.memory.getIntRef(t2);

        int ret = CLibrary.INSTANCE.getsockname(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        env.memory.setIntReference(t1, param1.sa_);
        env.memory.setIntArray(t1.add(4), 14, param1.sa_data);
        env.memory.setIntReference(t2, param2.getValue());
    }

    public static void getsockopt(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        int param0;
        int param1;
        int param2;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getInt(t1);
        param2 = env.memory.getInt(t2);

        int ret = CLibrary.INSTANCE.getsockopt(param0, param1, param2);

        env.register.set('0', new BitVec(ret));
    }

    public static void getsubopt(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        String[] param0;
        String param1;
        String[] param2;

        param0 = (String[]) env.memory.getArray(t0, -1);
        param1 = env.memory.getTextFromReference(t1);
        param2 = (String[]) env.memory.getArray(t2, -1);

        int ret = CLibrary.INSTANCE.getsubopt(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setArray(t0, -1, param0);
        env.memory.setTextReference(t1, param1);
        env.memory.setArray(t2, -1, param2);
    }

    public static void gettext(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        byte ret = CLibrary.INSTANCE.gettext(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void gettid(Environment env) {
        int ret = CLibrary.INSTANCE.gettid();
        env.register.set('0', new BitVec(ret));
    }

    public static void gettimeofday(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        timeval param0 = new timeval();
        timezone param1 = new timezone();

        param0.tv_sec = env.memory.getIntFromReference(t0);
        param0.tv_usec = env.memory.getNativeLongFromReference(t0.add(4));
        param1.tz_minuteswest = env.memory.getIntFromReference(t1);
        param1.tz_dsttime = env.memory.getIntFromReference(t1.add(4));

        int ret = CLibrary.INSTANCE.gettimeofday(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.tv_sec);
        env.memory.setNativeLongReference(t0.add(4), param0.tv_usec);
        env.memory.setIntReference(t1, param1.tz_minuteswest);
        env.memory.setIntReference(t1.add(4), param1.tz_dsttime);
    }

//    public static void gettimeofday (Environment env) {
//        // Load parameters from memory
//        BitVec t0 = env.register.get('0');
//        BitVec t1 = env.register.get('1');
//
//        timeval param0 = new timeval();
//        timezone param1 = new timezone();
//
//        param0.tv_sec = env.memory.getInt(t0);
//        param0.tv_usec = env.memory.getNativeLong(t0.add(4));
//        param1.tz_minuteswest = env.memory.getInt(t1);
//        param1.tz_dsttime = env.memory.getInt(t1.add(4));
//
//        // Invoke JNA interface
//        int ret = CLibrary.INSTANCE.gettimeofday(param0, param1);
//
//        // Update the memory
//        env.register.set('0', new BitVec(ret));
//        env.memory.setInt(t0, param0.tv_sec);
//        env.memory.setNativeLong(t0.add(4), param0.tv_usec);
//        env.memory.setInt(t1, param1.tz_minuteswest);
//        env.memory.setInt(t1.add(4), param1.tz_dsttime);
//    }

    public static void getuid(Environment env) {
        int ret = CLibrary.INSTANCE.getuid();
        env.register.set('0', new BitVec(ret));
    }

    public static void getumask(Environment env) {

        int ret = CLibrary.INSTANCE.getumask();
        env.register.set('0', new BitVec(ret));

    }

    public static void getutent(Environment env) {

        utmp ret = CLibrary.INSTANCE.getutent();
        env.register.set('0', new BitVec(ret));

    }

    public static void getutid(Environment env) {
        BitVec t0 = env.register.get('0');

        utmp param0 = new utmp();

        utmp ret = CLibrary.INSTANCE.getutid(param0);
        env.register.set('0', new BitVec(ret));

    }

    public static void getutid_r(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        utmp param0 = new utmp();
        utmp param1 = new utmp();

        int ret = CLibrary.INSTANCE.getutid_r(param0, param1);
        env.register.set('0', new BitVec(ret));

    }

    public static void getutline(Environment env) {
        BitVec t0 = env.register.get('0');

        utmp param0 = new utmp();

        utmp ret = CLibrary.INSTANCE.getutline(param0);
        env.register.set('0', new BitVec(ret));

    }

    public static void getutline_r(Environment env) {
        BitVec t0 = env.register.get('0');

        utmp param0 = new utmp();

        int ret = CLibrary.INSTANCE.getutline_r(param0);
        env.register.set('0', new BitVec(ret));

    }

    public static void getutmp(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        utmpx param0 = new utmpx();
        utmp param1 = new utmp();

        CLibrary.INSTANCE.getutmp(param0, param1);

    }

    public static void getutmpx(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        utmp param0 = new utmp();
        utmpx param1 = new utmpx();

        CLibrary.INSTANCE.getutmpx(param0, param1);

    }

    public static void getw(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        int ret = CLibrary.INSTANCE.getw(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void fgetwc(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        int ret = CLibrary.INSTANCE.fgetwc(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void getwchar(Environment env) {

        int ret = CLibrary.INSTANCE.getwchar();
        env.register.set('0', new BitVec(ret));

    }

    public static void getwchar_unlocked(Environment env) {

        int ret = CLibrary.INSTANCE.getwchar_unlocked();
        env.register.set('0', new BitVec(ret));

    }

    public static void getwc_unlocked(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        int ret = CLibrary.INSTANCE.getwc_unlocked(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void getwd(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        byte ret = CLibrary.INSTANCE.getwd(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void get_avphys_pages(Environment env) {

        NativeLong ret = CLibrary.INSTANCE.get_avphys_pages();
        env.register.set('0', new BitVec(ret));

    }

    public static void get_current_dir_name(Environment env) {

        byte ret = CLibrary.INSTANCE.get_current_dir_name();
        env.register.set('0', new BitVec(ret));

    }

    public static void get_nprocs(Environment env) {

        int ret = CLibrary.INSTANCE.get_nprocs();
        env.register.set('0', new BitVec(ret));

    }

    public static void get_nprocs_conf(Environment env) {

        int ret = CLibrary.INSTANCE.get_nprocs_conf();
        env.register.set('0', new BitVec(ret));

    }

    public static void get_phys_pages(Environment env) {

        NativeLong ret = CLibrary.INSTANCE.get_phys_pages();
        env.register.set('0', new BitVec(ret));

    }

    public static void glob64(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        int param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getInt(t1);

        int ret = CLibrary.INSTANCE.glob64(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void globfree(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        CLibrary.INSTANCE.globfree(param0);

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void globfree64(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        CLibrary.INSTANCE.globfree64(param0);

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void gmtime(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        tm ret = CLibrary.INSTANCE.gmtime(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void gmtime_r(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        IntByReference param0;
        tm param1 = new tm();

        param0 = env.memory.getIntRef(t0);
        param1.tm_sec = env.memory.getIntFromReference(t1);
        param1.tm_min = env.memory.getIntFromReference(t1.add(4));
        param1.tm_hour = env.memory.getIntFromReference(t1.add(8));
        param1.tm_mday = env.memory.getIntFromReference(t1.add(12));
        param1.tm_mon = env.memory.getIntFromReference(t1.add(16));
        param1.tm_year = env.memory.getIntFromReference(t1.add(20));
        param1.tm_wday = env.memory.getIntFromReference(t1.add(24));
        param1.tm_yday = env.memory.getIntFromReference(t1.add(28));
        param1.tm_isdst = env.memory.getIntFromReference(t1.add(32));
        param1.__tm_gmtoff = env.memory.getNativeLongFromReference(t1.add(36));
        param1.__tm_zone = env.memory.getStringFromReference(t1.add(40));

        tm ret = CLibrary.INSTANCE.gmtime_r(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
        env.memory.setIntReference(t1, param1.tm_sec);
        env.memory.setIntReference(t1.add(4), param1.tm_min);
        env.memory.setIntReference(t1.add(8), param1.tm_hour);
        env.memory.setIntReference(t1.add(12), param1.tm_mday);
        env.memory.setIntReference(t1.add(16), param1.tm_mon);
        env.memory.setIntReference(t1.add(20), param1.tm_year);
        env.memory.setIntReference(t1.add(24), param1.tm_wday);
        env.memory.setIntReference(t1.add(28), param1.tm_yday);
        env.memory.setIntReference(t1.add(32), param1.tm_isdst);
        env.memory.setNativeLongReference(t1.add(36), param1.__tm_gmtoff);
        env.memory.setStringReference(t1.add(40), param1.__tm_zone);
    }

    public static void grantpt(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.grantpt(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void gsignal(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.gsignal(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

//    public static void gtty (Environment env) {
//        BitVec t0 = env.register.get('0');
//        BitVec t1 = env.register.get('1');
//
//        int param0 ;
//        sgttyb param1 = new sgttyb();
//
//        param0 = env.memory.getInt(t0);
//
//        int ret = CLibrary.INSTANCE.gtty(param0, param1);
//        env.register.set('0', new BitVec(ret));
//
//
//        //env.memory.setInt(t0, param0);
//    }

    public static void hasmntopt(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        mntent param0 = new mntent();
        String param1;

        param0.mnt_fsname = env.memory.getStringFromReference(t0);
        param0.mnt_dir = env.memory.getStringFromReference(t0.add(4));
        param0.mnt_type = env.memory.getStringFromReference(t0.add(8));
        param0.mnt_opts = env.memory.getStringFromReference(t0.add(12));
        param0.mnt_freq = env.memory.getIntFromReference(t0.add(16));
        param0.mnt_passno = env.memory.getIntFromReference(t0.add(20));
        param1 = env.memory.getTextFromReference(t1);

        byte ret = CLibrary.INSTANCE.hasmntopt(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setStringReference(t0, param0.mnt_fsname);
        env.memory.setStringReference(t0.add(4), param0.mnt_dir);
        env.memory.setStringReference(t0.add(8), param0.mnt_type);
        env.memory.setStringReference(t0.add(12), param0.mnt_opts);
        env.memory.setIntReference(t0.add(16), param0.mnt_freq);
        env.memory.setIntReference(t0.add(20), param0.mnt_passno);
        env.memory.setTextReference(t1, param1);
    }

    public static void hcreate(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.hcreate(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

//    public static void hcreate_r (Environment env) {
//        BitVec t0 = env.register.get('0');
//        BitVec t1 = env.register.get('1');
//
//        int param0 ;
//        hsearch_data param1 = new hsearch_data();
//
//        param0 = env.memory.getInt(t0);
//
//        int ret = CLibrary.INSTANCE.hcreate_r(param0, param1);
//        env.register.set('0', new BitVec(ret));
//
//
//        //env.memory.setInt(t0, param0);
//    }

    public static void hdestroy(Environment env) {

        CLibrary.INSTANCE.hdestroy();

    }

//    public static void hdestroy_r (Environment env) {
//        BitVec t0 = env.register.get('0');
//
//        hsearch_data param0 = new hsearch_data();
//
//
//
//        CLibrary.INSTANCE.hdestroy_r(param0);
//
//    }

    public static void hsearch(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        int param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getInt(t1);

        int ret = CLibrary.INSTANCE.hsearch(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void hsearch_r(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        int param0;
        int param1;
        IntByReference param2;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getInt(t1);
        param2 = env.memory.getIntRef(t2);

        int ret = CLibrary.INSTANCE.hsearch_r(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        ////env.memory.setInt(t1, param1);
        env.memory.setIntReference(t2, param2.getValue());
    }

    public static void htonl(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.htonl(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void htons(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        short ret = CLibrary.INSTANCE.htons(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void hypot(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        double param0;
        double param1;

        param0 = env.memory.getDouble(t0);
        param1 = env.memory.getDouble(t1);

        double ret = CLibrary.INSTANCE.hypot(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setDouble(t0, param0);
        //env.memory.setDouble(t1, param1);
    }

    public static void hypotf(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        float param0;
        float param1;

        param0 = env.memory.getFloat(t0);
        param1 = env.memory.getFloat(t1);

        float ret = CLibrary.INSTANCE.hypotf(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setFloat(t0, param0);
        env.memory.setFloat(t1, param1);
    }

    public static void hypotl(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        NativeLong param0;
        NativeLong param1;

        param0 = env.memory.getNativeLong(t0);
        param1 = env.memory.getNativeLong(t1);

        double ret = CLibrary.INSTANCE.hypotl(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
        //env.memory.setNativeLongt1, param1);
    }

    public static void iconv_close(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.iconv_close(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void iconv_open(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        String param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getTextFromReference(t1);

        int ret = CLibrary.INSTANCE.iconv_open(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setTextReference(t1, param1);
    }

//    public static void if_freenameindex (Environment env) {
//        BitVec t0 = env.register.get('0');
//
//        if_nameindex param0 = new if_nameindex();
//
//
//
//        CLibrary.INSTANCE.if_freenameindex(param0);
//
//    }

    public static void if_indextoname(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        String param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getTextFromReference(t1);

        byte ret = CLibrary.INSTANCE.if_indextoname(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        env.memory.setTextReference(t1, param1);
    }

//    public static void if_nameindex (Environment env) {
//
//
//
//        CLibrary.INSTANCE.if_nameindex ret = CLibrary.INSTANCE.if_nameindex();
//        env.register.set('0', new BitVec(ret));
//
//
//    }

    public static void if_nametoindex(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        int ret = CLibrary.INSTANCE.if_nametoindex(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void ilogb(Environment env) {
        BitVec t0 = env.register.get('0');

        double param0;

        param0 = env.memory.getDouble(t0);

        int ret = CLibrary.INSTANCE.ilogb(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setDouble(t0, param0);
    }

    public static void ilogbf(Environment env) {
        BitVec t0 = env.register.get('0');

        float param0;

        param0 = env.memory.getFloat(t0);

        int ret = CLibrary.INSTANCE.ilogbf(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setFloat(t0, param0);
    }

    public static void ilogbl(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        int ret = CLibrary.INSTANCE.ilogbl(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
    }

    public static void imaxabs(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        long ret = CLibrary.INSTANCE.imaxabs(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void imaxdiv(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        int param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getInt(t1);

        int ret = CLibrary.INSTANCE.imaxdiv(param0, param1);
        env.register.set('0', new BitVec(ret));

    }

    public static void index(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        int param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getInt(t1);

        byte ret = CLibrary.INSTANCE.index(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void inet_addr(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        int ret = CLibrary.INSTANCE.inet_addr(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void inet_aton(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        in_addr param1 = new in_addr();

        param0 = env.memory.getTextFromReference(t0);
        param1.s_addr = env.memory.getIntFromReference(t1);

        int ret = CLibrary.INSTANCE.inet_aton(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setIntReference(t1, param1.s_addr);
    }

    public static void inet_lnaof(Environment env) {
        BitVec t0 = env.register.get('0');

        in_addr param0 = new in_addr();

        param0.s_addr = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.inet_lnaof(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0.s_addr);
    }

    public static void inet_makeaddr(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        int param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getInt(t1);

        in_addr ret = CLibrary.INSTANCE.inet_makeaddr(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void inet_netof(Environment env) {
        BitVec t0 = env.register.get('0');

        in_addr param0 = new in_addr();

        param0.s_addr = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.inet_netof(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0.s_addr);
    }

    public static void inet_network(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        int ret = CLibrary.INSTANCE.inet_network(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void inet_ntoa(Environment env) {
        BitVec t0 = env.register.get('0');

        in_addr param0 = new in_addr();

        param0.s_addr = env.memory.getInt(t0);

        byte ret = CLibrary.INSTANCE.inet_ntoa(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0.s_addr);
    }

    public static void inet_ntop(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        LongByReference param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getPointer(t1);

        byte ret = CLibrary.INSTANCE.inet_ntop(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        env.memory.setPointer(t1, param1.getValue());
    }

    public static void inet_pton(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        int param0;
        String param1;
        LongByReference param2;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getTextFromReference(t1);
        param2 = env.memory.getPointer(t2);

        int ret = CLibrary.INSTANCE.inet_pton(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        env.memory.setTextReference(t1, param1);
        env.memory.setPointer(t2, param2.getValue());
    }

    public static void initgroups(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        int param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getInt(t1);

        int ret = CLibrary.INSTANCE.initgroups(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void initstate(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        int param0;
        byte[] param1;
        int param2;

        param0 = env.memory.getInt(t0);
        param2 = env.memory.getInt(t2);
        param1 = env.memory.getByteArray(t1, param2);

        byte ret = CLibrary.INSTANCE.initstate(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        env.memory.setByteArray(t1, param2, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void initstate_r(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        String param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getTextFromReference(t1);

        int ret = CLibrary.INSTANCE.initstate_r(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        env.memory.setTextReference(t1, param1);
    }

    public static void isalnum(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.isalnum(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void isalpha(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.isalpha(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void isascii(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.isascii(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void isatty(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.isatty(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void isblank(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.isblank(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void iscntrl(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.iscntrl(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void isdigit(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.isdigit(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void isgraph(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.isgraph(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void isinff(Environment env) {
        BitVec t0 = env.register.get('0');

        float param0;

        param0 = env.memory.getFloat(t0);

        int ret = CLibrary.INSTANCE.isinff(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setFloat(t0, param0);
    }

    public static void isinfl(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        int ret = CLibrary.INSTANCE.isinfl(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
    }

    public static void islower(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.islower(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void isnanf(Environment env) {
        BitVec t0 = env.register.get('0');

        float param0;

        param0 = env.memory.getFloat(t0);

        int ret = CLibrary.INSTANCE.isnanf(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setFloat(t0, param0);
    }

    public static void isnanl(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        int ret = CLibrary.INSTANCE.isnanl(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
    }

    public static void isprint(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.isprint(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void ispunct(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.ispunct(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void isspace(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getIntFromReference(t0);

        int ret = CLibrary.INSTANCE.isspace(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void isupper(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.isupper(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void iswalnum(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.iswalnum(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void iswalpha(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.iswalpha(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void iswblank(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.iswblank(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void iswcntrl(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.iswcntrl(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void iswctype(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        NativeLong param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getNativeLong(t1);

        int ret = CLibrary.INSTANCE.iswctype(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        //env.memory.setNativeLongt1, param1);
    }

    public static void iswdigit(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.iswdigit(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void iswgraph(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.iswgraph(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void iswlower(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.iswlower(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void iswprint(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.iswprint(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void iswpunct(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.iswpunct(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void iswspace(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.iswspace(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void iswupper(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.iswupper(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void iswxdigit(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.iswxdigit(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void isxdigit(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.isxdigit(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void j0(Environment env) {
        BitVec t0 = env.register.get('0');

        double param0;

        param0 = env.memory.getDouble(t0);

        double ret = CLibrary.INSTANCE.j0(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setDouble(t0, param0);
    }

    public static void j0f(Environment env) {
        BitVec t0 = env.register.get('0');

        float param0;

        param0 = env.memory.getFloat(t0);

        float ret = CLibrary.INSTANCE.j0f(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setFloat(t0, param0);
    }

    public static void j0l(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        double ret = CLibrary.INSTANCE.j0l(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
    }

    public static void j1(Environment env) {
        BitVec t0 = env.register.get('0');

        double param0;

        param0 = env.memory.getDouble(t0);

        double ret = CLibrary.INSTANCE.j1(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setDouble(t0, param0);
    }

    public static void j1f(Environment env) {
        BitVec t0 = env.register.get('0');

        float param0;

        param0 = env.memory.getFloat(t0);

        float ret = CLibrary.INSTANCE.j1f(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setFloat(t0, param0);
    }

    public static void j1l(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        double ret = CLibrary.INSTANCE.j1l(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
    }

    public static void jn(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        double param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getDouble(t1);

        double ret = CLibrary.INSTANCE.jn(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        //env.memory.setDouble(t1, param1);
    }

    public static void jnf(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        float param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getFloat(t1);

        float ret = CLibrary.INSTANCE.jnf(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        env.memory.setFloat(t1, param1);
    }

    public static void jnl(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        NativeLong param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getNativeLong(t1);

        double ret = CLibrary.INSTANCE.jnl(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        //env.memory.setNativeLongt1, param1);
    }

    public static void jrand48(Environment env) {
        BitVec t0 = env.register.get('0');

        short[] param0;

        param0 = env.memory.getShortArray(t0, 3);

        NativeLong ret = CLibrary.INSTANCE.jrand48(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setShortArray(t0, 3, param0);
    }

    public static void jrand48_r(Environment env) {
        BitVec t0 = env.register.get('0');

        int[] param0;

        param0 = env.memory.getIntArray(t0, 3);

        int ret = CLibrary.INSTANCE.jrand48_r(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntArray(t0, 3, param0);
    }

    public static void kill(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        int param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getInt(t1);

        int ret = CLibrary.INSTANCE.kill(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void killpg(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        int param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getInt(t1);

        int ret = CLibrary.INSTANCE.killpg(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void l64a(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        byte ret = CLibrary.INSTANCE.l64a(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
    }

    public static void labs(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        NativeLong ret = CLibrary.INSTANCE.labs(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void lcong48(Environment env) {
        BitVec t0 = env.register.get('0');

        short[] param0;

        param0 = env.memory.getShortArray(t0, 7);

        CLibrary.INSTANCE.lcong48(param0);

        env.memory.setShortArray(t0, 7, param0);
    }

    public static void lcong48_r(Environment env) {
        BitVec t0 = env.register.get('0');

        int[] param0;

        param0 = env.memory.getIntArray(t0, 7);

        int ret = CLibrary.INSTANCE.lcong48_r(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntArray(t0, 7, param0);
    }

    public static void ldexp(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        double param0;
        int param1;

        param0 = env.memory.getDouble(t0);
        param1 = env.memory.getInt(t1);

        double ret = CLibrary.INSTANCE.ldexp(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setDouble(t0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void ldexpf(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        float param0;
        int param1;

        param0 = env.memory.getFloat(t0);
        param1 = env.memory.getInt(t1);

        float ret = CLibrary.INSTANCE.ldexpf(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setFloat(t0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void ldexpl(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        NativeLong param0;
        int param1;

        param0 = env.memory.getNativeLong(t0);
        param1 = env.memory.getInt(t1);

        double ret = CLibrary.INSTANCE.ldexpl(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void ldiv(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        NativeLong param0;
        NativeLong param1;

        param0 = env.memory.getNativeLong(t0);
        param1 = env.memory.getNativeLong(t1);

        int ret = CLibrary.INSTANCE.ldiv(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
        //env.memory.setNativeLongt1, param1);
    }

    public static void lgamma(Environment env) {
        BitVec t0 = env.register.get('0');

        double param0;

        param0 = env.memory.getDouble(t0);

        double ret = CLibrary.INSTANCE.lgamma(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setDouble(t0, param0);
    }

    public static void lgammaf(Environment env) {
        BitVec t0 = env.register.get('0');

        float param0;

        param0 = env.memory.getFloat(t0);

        float ret = CLibrary.INSTANCE.lgammaf(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setFloat(t0, param0);
    }

    public static void lgammaf_r(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        float param0;
        IntByReference param1;

        param0 = env.memory.getFloat(t0);
        param1 = env.memory.getIntRef(t1);

        float ret = CLibrary.INSTANCE.lgammaf_r(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setFloat(t0, param0);
        env.memory.setIntReference(t1, param1.getValue());
    }

    public static void lgammal(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        double ret = CLibrary.INSTANCE.lgammal(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
    }

    public static void lgammal_r(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        NativeLong param0;
        IntByReference param1;

        param0 = env.memory.getNativeLong(t0);
        param1 = env.memory.getIntRef(t1);

        double ret = CLibrary.INSTANCE.lgammal_r(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
        env.memory.setIntReference(t1, param1.getValue());
    }

    public static void lgamma_r(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        double param0;
        IntByReference param1;

        param0 = env.memory.getDouble(t0);
        param1 = env.memory.getIntRef(t1);

        double ret = CLibrary.INSTANCE.lgamma_r(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setDouble(t0, param0);
        env.memory.setIntReference(t1, param1.getValue());
    }

    public static void link(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        short ret = CLibrary.INSTANCE.link(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void linkat(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        int param0;
        String param1;
        int param2;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getTextFromReference(t1);
        param2 = env.memory.getInt(t2);

        int ret = CLibrary.INSTANCE.linkat(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        env.memory.setTextReference(t1, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void lio_listio(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.lio_listio(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void lio_listio64(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.lio_listio64(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void listen(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        int param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getInt(t1);

        int ret = CLibrary.INSTANCE.listen(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void llabs(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        long ret = CLibrary.INSTANCE.llabs(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void lldiv(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        NativeLong param0;
        NativeLong param1;

        param0 = env.memory.getNativeLong(t0);
        param1 = env.memory.getNativeLong(t1);

        int ret = CLibrary.INSTANCE.lldiv(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
        //env.memory.setNativeLongt1, param1);
    }

    public static void llrint(Environment env) {
        BitVec t0 = env.register.get('0');

        double param0;

        param0 = env.memory.getDouble(t0);

        long ret = CLibrary.INSTANCE.llrint(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setDouble(t0, param0);
    }

    public static void llrintf(Environment env) {
        BitVec t0 = env.register.get('0');

        float param0;

        param0 = env.memory.getFloat(t0);

        long ret = CLibrary.INSTANCE.llrintf(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setFloat(t0, param0);
    }

    public static void llrintl(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        long ret = CLibrary.INSTANCE.llrintl(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
    }

    public static void llround(Environment env) {
        BitVec t0 = env.register.get('0');

        double param0;

        param0 = env.memory.getDouble(t0);

        long ret = CLibrary.INSTANCE.llround(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setDouble(t0, param0);
    }

    public static void llroundf(Environment env) {
        BitVec t0 = env.register.get('0');

        float param0;

        param0 = env.memory.getFloat(t0);

        long ret = CLibrary.INSTANCE.llroundf(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setFloat(t0, param0);
    }

    public static void llroundl(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        long ret = CLibrary.INSTANCE.llroundl(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
    }

    public static void localeconv(Environment env) {
        lconv ret = CLibrary.INSTANCE.localeconv();
        env.register.set('0', new BitVec(ret));
    }

    public static void localtime(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        tm ret = CLibrary.INSTANCE.localtime(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void localtime_r(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        IntByReference param0;
        tm param1 = new tm();

        param0 = env.memory.getIntRef(t0);
        param1.tm_sec = env.memory.getIntFromReference(t1);
        param1.tm_min = env.memory.getIntFromReference(t1.add(4));
        param1.tm_hour = env.memory.getIntFromReference(t1.add(8));
        param1.tm_mday = env.memory.getIntFromReference(t1.add(12));
        param1.tm_mon = env.memory.getIntFromReference(t1.add(16));
        param1.tm_year = env.memory.getIntFromReference(t1.add(20));
        param1.tm_wday = env.memory.getIntFromReference(t1.add(24));
        param1.tm_yday = env.memory.getIntFromReference(t1.add(28));
        param1.tm_isdst = env.memory.getIntFromReference(t1.add(32));
        param1.__tm_gmtoff = env.memory.getNativeLongFromReference(t1.add(36));
        param1.__tm_zone = env.memory.getStringFromReference(t1.add(40));

        tm ret = CLibrary.INSTANCE.localtime_r(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
        env.memory.setIntReference(t1, param1.tm_sec);
        env.memory.setIntReference(t1.add(4), param1.tm_min);
        env.memory.setIntReference(t1.add(8), param1.tm_hour);
        env.memory.setIntReference(t1.add(12), param1.tm_mday);
        env.memory.setIntReference(t1.add(16), param1.tm_mon);
        env.memory.setIntReference(t1.add(20), param1.tm_year);
        env.memory.setIntReference(t1.add(24), param1.tm_wday);
        env.memory.setIntReference(t1.add(28), param1.tm_yday);
        env.memory.setIntReference(t1.add(32), param1.tm_isdst);
        env.memory.setNativeLongReference(t1.add(36), param1.__tm_gmtoff);
        env.memory.setStringReference(t1.add(40), param1.__tm_zone);
    }

    public static void log(Environment env) {
        BitVec t0 = env.register.get('0');

        double param0;

        param0 = env.memory.getDouble(t0);

        double ret = CLibrary.INSTANCE.log(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setDouble(t0, param0);
    }

    public static void log10(Environment env) {
        BitVec t0 = env.register.get('0');

        double param0;

        param0 = env.memory.getDouble(t0);

        double ret = CLibrary.INSTANCE.log10(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setDouble(t0, param0);
    }

    public static void log10f(Environment env) {
        BitVec t0 = env.register.get('0');

        float param0;

        param0 = env.memory.getFloat(t0);

        float ret = CLibrary.INSTANCE.log10f(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setFloat(t0, param0);
    }

    public static void log10l(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        double ret = CLibrary.INSTANCE.log10l(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
    }

    public static void log1p(Environment env) {
        BitVec t0 = env.register.get('0');

        double param0;

        param0 = env.memory.getDouble(t0);

        double ret = CLibrary.INSTANCE.log1p(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setDouble(t0, param0);
    }

    public static void log1pf(Environment env) {
        BitVec t0 = env.register.get('0');

        float param0;

        param0 = env.memory.getFloat(t0);

        float ret = CLibrary.INSTANCE.log1pf(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setFloat(t0, param0);
    }

    public static void log1pl(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        double ret = CLibrary.INSTANCE.log1pl(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
    }

    public static void log2(Environment env) {
        BitVec t0 = env.register.get('0');

        double param0;

        param0 = env.memory.getDouble(t0);

        double ret = CLibrary.INSTANCE.log2(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setDouble(t0, param0);
    }

    public static void log2f(Environment env) {
        BitVec t0 = env.register.get('0');

        float param0;

        param0 = env.memory.getFloat(t0);

        float ret = CLibrary.INSTANCE.log2f(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setFloat(t0, param0);
    }

    public static void log2l(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        double ret = CLibrary.INSTANCE.log2l(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
    }

    public static void logb(Environment env) {
        BitVec t0 = env.register.get('0');

        double param0;

        param0 = env.memory.getDouble(t0);

        double ret = CLibrary.INSTANCE.logb(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setDouble(t0, param0);
    }

    public static void logbf(Environment env) {
        BitVec t0 = env.register.get('0');

        float param0;

        param0 = env.memory.getFloat(t0);

        float ret = CLibrary.INSTANCE.logbf(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setFloat(t0, param0);
    }

    public static void logbl(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        double ret = CLibrary.INSTANCE.logbl(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
    }

    public static void logf(Environment env) {
        BitVec t0 = env.register.get('0');

        float param0;

        param0 = env.memory.getFloat(t0);

        float ret = CLibrary.INSTANCE.logf(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setFloat(t0, param0);
    }

    public static void login(Environment env) {
        BitVec t0 = env.register.get('0');

        utmp param0 = new utmp();

        CLibrary.INSTANCE.login(param0);

    }

    public static void login_tty(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.login_tty(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void logl(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        double ret = CLibrary.INSTANCE.logl(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLong(t0, param0);
    }

    public static void logout(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        int ret = CLibrary.INSTANCE.logout(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void logwtmp(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        String param0;
        String param1;
        String param2;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getTextFromReference(t1);
        param2 = env.memory.getTextFromReference(t2);

        CLibrary.INSTANCE.logwtmp(param0, param1, param2);

        env.memory.setTextReference(t0, param0);
        env.memory.setTextReference(t1, param1);
        env.memory.setTextReference(t2, param2);
    }

    public static void longjmp(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        int param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getInt(t1);

        CLibrary.INSTANCE.longjmp(param0, param1);

        //env.memory.setInt(t0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void lrand48(Environment env) {
        NativeLong ret = CLibrary.INSTANCE.lrand48();
        env.register.set('0', new BitVec(ret));
    }

//    public static void lrand48_r (Environment env) {
//        BitVec t0 = env.register.get('0');
//        BitVec t1 = env.register.get('1');
//
//        drand48_data param0 = new drand48_data();
//        IntByReference param1 ;
//
//        param0.__x = env.memory.getIntFromReference(t0);
//        param0.__old_x = env.memory.getIntFromReference(t0.add(4));
//        param0.__c = env.memory.getShortRef(t0.add(8));
//        param0.__init = env.memory.getShortRef(t0.add(10));
//        param0.__a = env.memory.getLongFromReference(t0.add(12));
//        param1 = env.memory.getIntFromReference(t1);
//
//        int ret = CLibrary.INSTANCE.lrand48_r(param0, param1);
//        env.register.set('0', new BitVec(ret));
//
//        env.memory.setIntReference(t0, param0.__x);
//        env.memory.setIntReference(t0.add(4), param0.__old_x);
//        env.memory.setShortReference(t0.add(8), param0.__c);
//        env.memory.setShortReference(t0.add(10), param0.__init);
//        env.memory.setLongReference(t0.add(12), param0.__a);
//        env.memory.setIntReference(t1, param1.getValue());
//    }

    public static void lrint(Environment env) {
        BitVec t0 = env.register.get('0');

        double param0;

        param0 = env.memory.getDouble(t0);

        NativeLong ret = CLibrary.INSTANCE.lrint(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setDouble(t0, param0);
    }

    public static void lrintf(Environment env) {
        BitVec t0 = env.register.get('0');

        float param0;

        param0 = env.memory.getFloat(t0);

        NativeLong ret = CLibrary.INSTANCE.lrintf(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setFloat(t0, param0);
    }

    public static void lrintl(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        NativeLong ret = CLibrary.INSTANCE.lrintl(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
    }

    public static void lround(Environment env) {
        BitVec t0 = env.register.get('0');

        double param0;

        param0 = env.memory.getDouble(t0);

        NativeLong ret = CLibrary.INSTANCE.lround(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setDouble(t0, param0);
    }

    public static void lroundf(Environment env) {
        BitVec t0 = env.register.get('0');

        float param0;

        param0 = env.memory.getFloat(t0);

        NativeLong ret = CLibrary.INSTANCE.lroundf(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setFloat(t0, param0);
    }

    public static void lroundl(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        NativeLong ret = CLibrary.INSTANCE.lroundl(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
    }

    public static void lseek(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        int param0;
        NativeLong param1;
        int param2;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getNativeLong(t1);
        param2 = env.memory.getInt(t2);

        NativeLong ret = CLibrary.INSTANCE.lseek(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        //env.memory.setNativeLongt1, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void lseek64(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        int param0;
        NativeLong param1;
        int param2;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getNativeLong(t1);
        param2 = env.memory.getInt(t2);

        NativeLong ret = CLibrary.INSTANCE.lseek64(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        //env.memory.setNativeLongt1, param1);
        //env.memory.setInt(t2, param2);
    }

//    public static void lstat (Environment env) {
//        BitVec t0 = env.register.get('0');
//        BitVec t1 = env.register.get('1');
//
//        String param0 ;
//        stat param1 = new stat();
//
//        param0 = env.memory.getTextFromReference(t0);
//        param1.st_dev = env.memory.getIntFromReference(t1);
//        param1.st_ino = env.memory.getIntFromReference(t1.add(4));
//        param1.st_mode = env.memory.getIntFromReference(t1.add(8));
//        param1.st_nlink = env.memory.getIntFromReference(t1.add(12));
//        param1.st_uid = env.memory.getIntFromReference(t1.add(16));
//        param1.st_gid = env.memory.getIntFromReference(t1.add(20));
//        param1.st_rdev = env.memory.getIntFromReference(t1.add(24));
//        param1.st_size = env.memory.getNativeLongFromReference(t1.add(28));
//        param1.st_blksize = env.memory.getIntFromReference(t1.add(32));
//        param1.st_blocks = env.memory.getIntFromReference(t1.add(36));
//        param1.st_atim = env.memory.getIntFromReference(t1.add(40));
//        param1.st_mtim = env.memory.getIntFromReference(t1.add(44));
//        param1.st_ctim = env.memory.getIntFromReference(t1.add(48));
//        param1.st_ctim.tv_sec = env.memory.getIntFromReference(t1.add(52));
//
//        int ret = CLibrary.INSTANCE.lstat(param0, param1);
//        env.register.set('0', new BitVec(ret));
//
//
//        env.memory.setTextReference(t0, param0);
//        env.memory.setIntReference(t1, param1.st_dev);
//        env.memory.setIntReference(t1.add(4), param1.st_ino);
//        env.memory.setIntReference(t1.add(8), param1.st_mode);
//        env.memory.setIntReference(t1.add(12), param1.st_nlink);
//        env.memory.setIntReference(t1.add(16), param1.st_uid);
//        env.memory.setIntReference(t1.add(20), param1.st_gid);
//        env.memory.setIntReference(t1.add(24), param1.st_rdev);
//        env.memory.setNativeLongReference(t1.add(28), param1.st_size);
//        env.memory.setIntReference(t1.add(32), param1.st_blksize);
//        env.memory.setIntReference(t1.add(36), param1.st_blocks);
//        env.memory.setIntReference(t1.add(40), param1.st_atim);
//        env.memory.setIntReference(t1.add(44), param1.st_mtim);
//        env.memory.setIntReference(t1.add(48), param1.st_ctim);
//        env.memory.setIntReference(t1.add(52), param1.st_ctim.tv_sec);
//    }

    public static void lstat64(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        int ret = CLibrary.INSTANCE.lstat64(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void lutimes(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        timeval[] param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = (timeval[]) env.memory.getArray(t1, 2);

        int ret = CLibrary.INSTANCE.lutimes(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setArray(t1, 2, param1);
    }

    public static void madvise(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        LongByReference param0;
        int param1;
        int param2;

        param0 = env.memory.getPointer(t0);
        param1 = env.memory.getInt(t1);
        param2 = env.memory.getInt(t2);

        int ret = CLibrary.INSTANCE.madvise(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setPointer(t0, param0.getValue());
        ////env.memory.setInt(t1, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void main(Environment env) {

        int ret = CLibrary.INSTANCE.main();
        env.register.set('0', new BitVec(ret));

    }

//    public static void makecontext (Environment env) {
//        BitVec t0 = env.register.get('0');
//        BitVec t1 = env.register.get('1');
//
//        IntByReference param0 ;
//        ByReference param1 ;
//
//        param0 = env.memory.getIntRef(t0);
//        param1 = env.memory.getVoid(t1);
//
//
//        CLibrary.INSTANCE.makecontext(param0, param1);
//
//        env.memory.setIntReference(t0, param0.getValue());
//        env.memory.setVoid(t1, param1);
//    }

    public static void mallinfo(Environment env) {
        CLibrary.INSTANCE.mallinfo();
    }

    public static void malloc(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        //CLibrary.INSTANCE.malloc(param0);

        //env.memory.setInt(t0, param0);
    }

    public static void mallopt(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        int param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getInt(t1);

//        int ret = CLibrary.INSTANCE.mallopt(param0, param1);
//        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void mblen(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        byte[] param0;
        int param1;

        param1 = env.memory.getInt(t1);
        param0 = env.memory.getByteArray(t0, param1);

        int ret = CLibrary.INSTANCE.mblen(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setByteArray(t0, param1, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void mbrlen(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        byte[] param0;
        int param1;
        int[] param2;

        param1 = env.memory.getInt(t1);
        param0 = env.memory.getByteArray(t0, param1);
        param2 = env.memory.getIntArray(t2, param1);

        int ret = CLibrary.INSTANCE.mbrlen(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setByteArray(t0, param1, param0);
        ////env.memory.setInt(t1, param1);
        env.memory.setIntArray(t2, param1, param2);
    }

    public static void mbrtowc(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');
        BitVec t3 = env.register.get('3');

        byte[] param0;
        byte[] param1;
        int param2;
        int[] param3;

        param2 = env.memory.getInt(t2);
        param0 = env.memory.getByteArray(t0, param2);
        param1 = env.memory.getByteArray(t1, param2);
        param3 = env.memory.getIntArray(t3, param2);

        int ret = CLibrary.INSTANCE.mbrtowc(param0, param1, param2, param3);
        env.register.set('0', new BitVec(ret));

        env.memory.setByteArray(t0, param2, param0);
        env.memory.setByteArray(t1, param2, param1);
        //env.memory.setInt(t2, param2);
        env.memory.setIntArray(t3, param2, param3);
    }

    public static void mbsinit(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        int ret = CLibrary.INSTANCE.mbsinit(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void mbsnrtowcs(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        int ret = CLibrary.INSTANCE.mbsnrtowcs(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void mbsrtowcs(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        int ret = CLibrary.INSTANCE.mbsrtowcs(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void mbstowcs(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        byte[] param0;
        byte[] param1;
        int param2;

        param2 = env.memory.getInt(t2);
        param0 = env.memory.getByteArray(t0, param2);
        param1 = env.memory.getByteArray(t1, param2);

        int ret = CLibrary.INSTANCE.mbstowcs(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setByteArray(t0, param2, param0);
        env.memory.setByteArray(t1, param2, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void mbtowc(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        byte[] param0;
        byte[] param1;
        int param2;

        param2 = env.memory.getInt(t2);
        param0 = env.memory.getByteArray(t0, param2);
        param1 = env.memory.getByteArray(t1, param2);

        int ret = CLibrary.INSTANCE.mbtowc(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setByteArray(t0, param2, param0);
        env.memory.setByteArray(t1, param2, param1);
        //env.memory.setInt(t2, param2);
    }

//    public static void mcheck (Environment env) {
//        BitVec t0 = env.register.get('0');
//
//        void param0 ;
//
//        param0 = env.memory.getVoid(t0);
//
//        int ret = CLibrary.INSTANCE.mcheck(param0);
//        env.register.set('0', new BitVec(ret));
//
//
//        env.memory.setVoid(t0, param0);
//    }

    public static void memccpy(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');
        BitVec t3 = env.register.get('3');

        LongByReference param0;
        LongByReference param1;
        int param2;
        int param3;

        param0 = env.memory.getPointer(t0);
        param1 = env.memory.getPointer(t1);
        param2 = env.memory.getInt(t2);
        param3 = env.memory.getInt(t3);

        CLibrary.INSTANCE.memccpy(param0, param1, param2, param3);

        env.memory.setPointer(t0, param0.getValue());
        env.memory.setPointer(t1, param1.getValue());
        //env.memory.setInt(t2, param2);
        //env.memory.setInt(t3, param3);
    }

    public static void memchr(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        LongByReference param0;
        int param1;
        int param2;

        param0 = env.memory.getPointer(t0);
        param1 = env.memory.getInt(t1);
        param2 = env.memory.getInt(t2);

        CLibrary.INSTANCE.memchr(param0, param1, param2);

        env.memory.setPointer(t0, param0.getValue());
        ////env.memory.setInt(t1, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void memcmp(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        LongByReference param0;
        LongByReference param1;
        int param2;

        param0 = env.memory.getPointer(t0);
        param1 = env.memory.getPointer(t1);
        param2 = env.memory.getInt(t2);

        int ret = CLibrary.INSTANCE.memcmp(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setPointer(t0, param0.getValue());
        env.memory.setPointer(t1, param1.getValue());
        //env.memory.setInt(t2, param2);
    }

    public static void memcpy(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        LongByReference param0;
        LongByReference param1;
        int param2;

        param0 = env.memory.getPointer(t0);
        param1 = env.memory.getPointer(t1);
        param2 = env.memory.getInt(t2);

        CLibrary.INSTANCE.memcpy(param0, param1, param2);

        env.memory.setPointer(t0, param0.getValue());
        env.memory.setPointer(t1, param1.getValue());
        //env.memory.setInt(t2, param2);
    }

    public static void memfd_create(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        int param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getInt(t1);

        int ret = CLibrary.INSTANCE.memfd_create(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void memfrob(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        LongByReference param0;
        int param1;

        param0 = env.memory.getPointer(t0);
        param1 = env.memory.getInt(t1);

        CLibrary.INSTANCE.memfrob(param0, param1);

        env.memory.setPointer(t0, param0.getValue());
        ////env.memory.setInt(t1, param1);
    }

    public static void memmem(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        LongByReference param0;
        int param1;

        param0 = env.memory.getPointer(t0);
        param1 = env.memory.getInt(t1);

        CLibrary.INSTANCE.memmem(param0, param1);

        env.memory.setPointer(t0, param0.getValue());
        ////env.memory.setInt(t1, param1);
    }

    public static void memmove(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        LongByReference param0;
        LongByReference param1;
        int param2;

        param0 = env.memory.getPointer(t0);
        param1 = env.memory.getPointer(t1);
        param2 = env.memory.getInt(t2);

//        CLibrary.INSTANCE.memmove(param0, param1, param2);
//
//        env.memory.setPointer(t0, param0.getValue());
//        env.memory.setPointer(t1, param1.getValue());
        //env.memory.setInt(t2, param2);
    }

    public static void mempcpy(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        LongByReference param0;
        LongByReference param1;
        int param2;

        param0 = env.memory.getPointer(t0);
        param1 = env.memory.getPointer(t1);
        param2 = env.memory.getInt(t2);

        CLibrary.INSTANCE.mempcpy(param0, param1, param2);

        env.memory.setPointer(t0, param0.getValue());
        env.memory.setPointer(t1, param1.getValue());
        //env.memory.setInt(t2, param2);
    }

    public static void memrchr(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        LongByReference param0;
        int param1;
        int param2;

        param0 = env.memory.getPointer(t0);
        param1 = env.memory.getInt(t1);
        param2 = env.memory.getInt(t2);

        CLibrary.INSTANCE.memrchr(param0, param1, param2);

        env.memory.setPointer(t0, param0.getValue());
        ////env.memory.setInt(t1, param1);
        //env.memory.setInt(t2, param2);
    }

    /* Manually implemented */
    public static void memset(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        BitVec param0;
        int param1;
        int param2;

        param0 = env.memory.getWordMemoryValue(t0);
        param1 = env.memory.getInt(t1);
        param2 = env.memory.getInt(t2);

        BitVec ptr = t0;

        for (int i = 0; i < param2; i += 4) {
            // For 'param2' number of bytes
            Memory.set(ptr, new BitVec(param1));
            ptr = ptr.add(4);
        }
        //CLibrary.INSTANCE.memset(param0, param1, param2);

        //env.memory.setPointer(t0, param0.getValue());

    }

    public static void mkdir(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        int param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getInt(t1);

        int ret = CLibrary.INSTANCE.mkdir(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void mkdtemp(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        byte ret = CLibrary.INSTANCE.mkdtemp(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void mkfifo(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        int param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getInt(t1);

        int ret = CLibrary.INSTANCE.mkfifo(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void mknod(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        String param0;
        int param1;
        int param2;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getInt(t1);
        param2 = env.memory.getInt(t2);

        int ret = CLibrary.INSTANCE.mknod(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        ////env.memory.setInt(t1, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void mkstemp(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        int ret = CLibrary.INSTANCE.mkstemp(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void mktemp(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        byte ret = CLibrary.INSTANCE.mktemp(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void mktime(Environment env) {
        BitVec t0 = env.register.get('0');

        tm param0 = new tm();

        param0.tm_sec = env.memory.getIntFromReference(t0);
        param0.tm_min = env.memory.getIntFromReference(t0.add(4));
        param0.tm_hour = env.memory.getIntFromReference(t0.add(8));
        param0.tm_mday = env.memory.getIntFromReference(t0.add(12));
        param0.tm_mon = env.memory.getIntFromReference(t0.add(16));
        param0.tm_year = env.memory.getIntFromReference(t0.add(20));
        param0.tm_wday = env.memory.getIntFromReference(t0.add(24));
        param0.tm_yday = env.memory.getIntFromReference(t0.add(28));
        param0.tm_isdst = env.memory.getIntFromReference(t0.add(32));
        param0.__tm_gmtoff = env.memory.getNativeLongFromReference(t0.add(36));
        param0.__tm_zone = env.memory.getStringFromReference(t0.add(40));

        int ret = CLibrary.INSTANCE.mktime(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.tm_sec);
        env.memory.setIntReference(t0.add(4), param0.tm_min);
        env.memory.setIntReference(t0.add(8), param0.tm_hour);
        env.memory.setIntReference(t0.add(12), param0.tm_mday);
        env.memory.setIntReference(t0.add(16), param0.tm_mon);
        env.memory.setIntReference(t0.add(20), param0.tm_year);
        env.memory.setIntReference(t0.add(24), param0.tm_wday);
        env.memory.setIntReference(t0.add(28), param0.tm_yday);
        env.memory.setIntReference(t0.add(32), param0.tm_isdst);
        env.memory.setNativeLongReference(t0.add(36), param0.__tm_gmtoff);
        env.memory.setStringReference(t0.add(40), param0.__tm_zone);
    }

    public static void mlock(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        LongByReference param0;
        int param1;

        param0 = env.memory.getPointer(t0);
        param1 = env.memory.getInt(t1);

        int ret = CLibrary.INSTANCE.mlock(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setPointer(t0, param0.getValue());
        ////env.memory.setInt(t1, param1);
    }

    public static void mlock2(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        LongByReference param0;
        int param1;
        int param2;

        param0 = env.memory.getPointer(t0);
        param1 = env.memory.getInt(t1);
        param2 = env.memory.getInt(t2);

        int ret = CLibrary.INSTANCE.mlock2(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setPointer(t0, param0.getValue());
        ////env.memory.setInt(t1, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void mlockall(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.mlockall(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void mmap(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        LongByReference param0;
        int param1;
        int param2;

        param0 = env.memory.getPointer(t0);
        param1 = env.memory.getInt(t1);
        param2 = env.memory.getInt(t2);

        CLibrary.INSTANCE.mmap(param0, param1, param2);

        env.memory.setPointer(t0, param0.getValue());
        ////env.memory.setInt(t1, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void mmap64(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        LongByReference param0;
        int param1;
        int param2;

        param0 = env.memory.getPointer(t0);
        param1 = env.memory.getInt(t1);
        param2 = env.memory.getInt(t2);

        CLibrary.INSTANCE.mmap64(param0, param1, param2);

        env.memory.setPointer(t0, param0.getValue());
        ////env.memory.setInt(t1, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void modf(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        double param0;
        DoubleByReference param1;

        param0 = env.memory.getDouble(t0);
        param1 = env.memory.getDoubleRef(t1);

        double ret = CLibrary.INSTANCE.modf(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setDouble(t0, param0);
        //env.memory.setDouble(t1, param1.getValue());
    }

    public static void modff(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        float param0;
        FloatByReference param1;

        param0 = env.memory.getFloat(t0);
        param1 = env.memory.getFloatRef(t1);

        float ret = CLibrary.INSTANCE.modff(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setFloat(t0, param0);
        env.memory.setFloat(t1, param1.getValue());
    }

    public static void modfl(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        NativeLong param0;
        NativeLongByReference param1;

        param0 = env.memory.getNativeLong(t0);
        param1 = env.memory.getNativeLongRef(t1);

        double ret = CLibrary.INSTANCE.modfl(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
        env.memory.setNativeLongReference(t1, param1.getValue());
    }

    public static void mount(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        String param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getTextFromReference(t1);

        int ret = CLibrary.INSTANCE.mount(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setTextReference(t1, param1);
    }

//    public static void mprobe (Environment env) {
//        BitVec t0 = env.register.get('0');
//
//        LongByReference param0 ;
//
//        param0 = env.memory.getPointer(t0);
//
//        enum mcheck_status ret = CLibrary.INSTANCE.mprobe(param0);
//        env.register.set('0', new BitVec(ret));
//
//
//        env.memory.setPointer(t0, param0.getValue());
//    }

    public static void mprotect(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        LongByReference param0;
        int param1;
        int param2;

        param0 = env.memory.getPointer(t0);
        param1 = env.memory.getInt(t1);
        param2 = env.memory.getInt(t2);

        int ret = CLibrary.INSTANCE.mprotect(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setPointer(t0, param0.getValue());
        ////env.memory.setInt(t1, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void mrand48(Environment env) {

        NativeLong ret = CLibrary.INSTANCE.mrand48();
        env.register.set('0', new BitVec(ret));

    }

    public static void mrand48_r(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        drand48_data param0 = new drand48_data();
        IntByReference param1;

        param0.__x = env.memory.getShortRef(t0);
        param0.__old_x = env.memory.getShortRef(t0.add(4));
        param0.__c = env.memory.getShort(t0.add(8));
        param0.__init = env.memory.getShort(t0.add(10));
        param0.__a = env.memory.getLongFromReference(t0.add(12));
        param1 = env.memory.getIntRef(t1);

        int ret = CLibrary.INSTANCE.mrand48_r(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setShortReference(t0, param0.__x.getValue());
        env.memory.setIntReference(t0.add(4), param0.__old_x.getValue());
        env.memory.setShortReference(t0.add(8), param0.__c);
        env.memory.setShortReference(t0.add(10), param0.__init);
        env.memory.setLongReference(t0.add(12), param0.__a);
        env.memory.setIntReference(t1, param1.getValue());
    }

    public static void mremap(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        LongByReference param0;
        int param1;
        int param2;

        param0 = env.memory.getPointer(t0);
        param1 = env.memory.getInt(t1);
        param2 = env.memory.getInt(t2);

        CLibrary.INSTANCE.mremap(param0, param1, param2);

        env.memory.setPointer(t0, param0.getValue());
        ////env.memory.setInt(t1, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void msync(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        LongByReference param0;
        int param1;
        int param2;

        param0 = env.memory.getPointer(t0);
        param1 = env.memory.getInt(t1);
        param2 = env.memory.getInt(t2);

        int ret = CLibrary.INSTANCE.msync(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setPointer(t0, param0.getValue());
        ////env.memory.setInt(t1, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void mtrace(Environment env) {

        CLibrary.INSTANCE.mtrace();

    }

    public static void munlock(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        LongByReference param0;
        int param1;

        param0 = env.memory.getPointer(t0);
        param1 = env.memory.getInt(t1);

        int ret = CLibrary.INSTANCE.munlock(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setPointer(t0, param0.getValue());
        ////env.memory.setInt(t1, param1);
    }

    public static void munlockall(Environment env) {

        int ret = CLibrary.INSTANCE.munlockall();
        env.register.set('0', new BitVec(ret));

    }

    public static void munmap(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        LongByReference param0;
        int param1;

        param0 = env.memory.getPointer(t0);
        param1 = env.memory.getInt(t1);

        int ret = CLibrary.INSTANCE.munmap(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setPointer(t0, param0.getValue());
        ////env.memory.setInt(t1, param1);
    }

    public static void muntrace(Environment env) {

        CLibrary.INSTANCE.muntrace();

    }

    public static void nanf(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        float ret = CLibrary.INSTANCE.nanf(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void nanl(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        double ret = CLibrary.INSTANCE.nanl(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void nanosleep(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        timespec param0 = new timespec();
        timespec param1 = new timespec();

        param0.tv_sec = env.memory.getIntFromReference(t0);
        param0.tv_nsec = env.memory.getNativeLongFromReference(t0.add(4));
        param1.tv_sec = env.memory.getIntFromReference(t1);
        param1.tv_nsec = env.memory.getNativeLongFromReference(t1.add(4));

        int ret = CLibrary.INSTANCE.nanosleep(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.tv_sec);
        env.memory.setNativeLongReference(t0.add(4), param0.tv_nsec);
        env.memory.setIntReference(t1, param1.tv_sec);
        env.memory.setNativeLongReference(t1.add(4), param1.tv_nsec);
    }

    public static void nearbyint(Environment env) {
        BitVec t0 = env.register.get('0');

        double param0;

        param0 = env.memory.getDouble(t0);

        double ret = CLibrary.INSTANCE.nearbyint(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setDouble(t0, param0);
    }

    public static void nearbyintf(Environment env) {
        BitVec t0 = env.register.get('0');

        float param0;

        param0 = env.memory.getFloat(t0);

        float ret = CLibrary.INSTANCE.nearbyintf(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setFloat(t0, param0);
    }

    public static void nearbyintl(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        double ret = CLibrary.INSTANCE.nearbyintl(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
    }

    public static void nextafter(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        double param0;
        double param1;

        param0 = env.memory.getDouble(t0);
        param1 = env.memory.getDouble(t1);

        double ret = CLibrary.INSTANCE.nextafter(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setDouble(t0, param0);
        //env.memory.setDouble(t1, param1);
    }

    public static void nextafterf(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        float param0;
        float param1;

        param0 = env.memory.getFloat(t0);
        param1 = env.memory.getFloat(t1);

        float ret = CLibrary.INSTANCE.nextafterf(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setFloat(t0, param0);
        env.memory.setFloat(t1, param1);
    }

    public static void nextafterl(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        NativeLong param0;
        NativeLong param1;

        param0 = env.memory.getNativeLong(t0);
        param1 = env.memory.getNativeLong(t1);

        double ret = CLibrary.INSTANCE.nextafterl(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
        //env.memory.setNativeLongt1, param1);
    }

    public static void nextdown(Environment env) {
        BitVec t0 = env.register.get('0');

        double param0;

        param0 = env.memory.getDouble(t0);

        double ret = CLibrary.INSTANCE.nextdown(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setDouble(t0, param0);
    }

    public static void nextdownf(Environment env) {
        BitVec t0 = env.register.get('0');

        float param0;

        param0 = env.memory.getFloat(t0);

        float ret = CLibrary.INSTANCE.nextdownf(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setFloat(t0, param0);
    }

    public static void nextdownl(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        double ret = CLibrary.INSTANCE.nextdownl(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
    }

    public static void nexttoward(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        double param0;
        NativeLong param1;

        param0 = env.memory.getDouble(t0);
        param1 = env.memory.getNativeLong(t1);

        double ret = CLibrary.INSTANCE.nexttoward(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setDouble(t0, param0);
        //env.memory.setNativeLongt1, param1);
    }

    public static void nexttowardf(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        float param0;
        NativeLong param1;

        param0 = env.memory.getFloat(t0);
        param1 = env.memory.getNativeLong(t1);

        float ret = CLibrary.INSTANCE.nexttowardf(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setFloat(t0, param0);
        //env.memory.setNativeLongt1, param1);
    }

    public static void nexttowardl(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        NativeLong param0;
        NativeLong param1;

        param0 = env.memory.getNativeLong(t0);
        param1 = env.memory.getNativeLong(t1);

        double ret = CLibrary.INSTANCE.nexttowardl(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
        //env.memory.setNativeLongt1, param1);
    }

    public static void nextup(Environment env) {
        BitVec t0 = env.register.get('0');

        double param0;

        param0 = env.memory.getDouble(t0);

        double ret = CLibrary.INSTANCE.nextup(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setDouble(t0, param0);
    }

    public static void nextupf(Environment env) {
        BitVec t0 = env.register.get('0');

        float param0;

        param0 = env.memory.getFloat(t0);

        float ret = CLibrary.INSTANCE.nextupf(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setFloat(t0, param0);
    }

    public static void nextupl(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        double ret = CLibrary.INSTANCE.nextupl(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
    }

    public static void nftw(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        String param0;
        int param1;
        int param2;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getInt(t1);
        param2 = env.memory.getInt(t2);

        int ret = CLibrary.INSTANCE.nftw(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        ////env.memory.setInt(t1, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void nftw64(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        int param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getInt(t1);

        int ret = CLibrary.INSTANCE.nftw64(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void ngettext(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        String param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getTextFromReference(t1);

        byte ret = CLibrary.INSTANCE.ngettext(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setTextReference(t1, param1);
    }

    public static void nice(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.nice(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void nl_langinfo(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        byte ret = CLibrary.INSTANCE.nl_langinfo(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void notfound(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.notfound(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void nrand48(Environment env) {
        BitVec t0 = env.register.get('0');

        short[] param0;

        param0 = env.memory.getShortArray(t0, 3);

        NativeLong ret = CLibrary.INSTANCE.nrand48(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setShortArray(t0, 3, param0);
    }

    public static void nrand48_r(Environment env) {
        BitVec t0 = env.register.get('0');

        int[] param0;

        param0 = env.memory.getIntArray(t0, 3);

        int ret = CLibrary.INSTANCE.nrand48_r(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntArray(t0, 3, param0);
    }

    public static void ntohl(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.ntohl(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void ntohs(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        short ret = CLibrary.INSTANCE.ntohs(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void ntp_adjtime(Environment env) {
        BitVec t0 = env.register.get('0');

        timex param0 = new timex();

        param0.modes = env.memory.getIntFromReference(t0);
        param0.offset = env.memory.getNativeLongFromReference(t0.add(4));
        param0.freq = env.memory.getNativeLongFromReference(t0.add(8));
        param0.maxerror = env.memory.getNativeLongFromReference(t0.add(12));
        param0.esterror = env.memory.getNativeLongFromReference(t0.add(16));
        param0.status = env.memory.getIntFromReference(t0.add(20));
        param0.constant = env.memory.getNativeLongFromReference(t0.add(24));
        param0.precision = env.memory.getNativeLongFromReference(t0.add(28));
        param0.tolerance = env.memory.getNativeLongFromReference(t0.add(32));
        param0.time = env.memory.getIntFromReference(t0.add(36));
        param0.tick = env.memory.getNativeLongFromReference(t0.add(40));
        param0.ppsfreq = env.memory.getNativeLongFromReference(t0.add(44));
        param0.jitter = env.memory.getNativeLongFromReference(t0.add(48));
        param0.shift = env.memory.getIntFromReference(t0.add(52));
        param0.stabil = env.memory.getNativeLongFromReference(t0.add(56));
        param0.jitcnt = env.memory.getNativeLongFromReference(t0.add(60));
        param0.calcnt = env.memory.getNativeLongFromReference(t0.add(64));
        param0.errcnt = env.memory.getNativeLongFromReference(t0.add(68));
        param0.stbcnt = env.memory.getNativeLongFromReference(t0.add(72));
        param0.tai = env.memory.getIntFromReference(t0.add(76));

        int ret = CLibrary.INSTANCE.ntp_adjtime(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.modes);
        env.memory.setNativeLongReference(t0.add(4), param0.offset);
        env.memory.setNativeLongReference(t0.add(8), param0.freq);
        env.memory.setNativeLongReference(t0.add(12), param0.maxerror);
        env.memory.setNativeLongReference(t0.add(16), param0.esterror);
        env.memory.setIntReference(t0.add(20), param0.status);
        env.memory.setNativeLongReference(t0.add(24), param0.constant);
        env.memory.setNativeLongReference(t0.add(28), param0.precision);
        env.memory.setNativeLongReference(t0.add(32), param0.tolerance);
        env.memory.setIntReference(t0.add(36), param0.time);
        env.memory.setNativeLongReference(t0.add(40), param0.tick);
        env.memory.setNativeLongReference(t0.add(44), param0.ppsfreq);
        env.memory.setNativeLongReference(t0.add(48), param0.jitter);
        env.memory.setIntReference(t0.add(52), param0.shift);
        env.memory.setNativeLongReference(t0.add(56), param0.stabil);
        env.memory.setNativeLongReference(t0.add(60), param0.jitcnt);
        env.memory.setNativeLongReference(t0.add(64), param0.calcnt);
        env.memory.setNativeLongReference(t0.add(68), param0.errcnt);
        env.memory.setNativeLongReference(t0.add(72), param0.stbcnt);
        env.memory.setIntReference(t0.add(76), param0.tai);
    }

    public static void ntp_gettime(Environment env) {
        BitVec t0 = env.register.get('0');

        ntptimeval param0 = new ntptimeval();

        param0.time = env.memory.getIntFromReference(t0);
        param0.maxerror = env.memory.getNativeLongFromReference(t0.add(4));
        param0.esterror = env.memory.getNativeLongFromReference(t0.add(8));
        param0.tai = env.memory.getNativeLongFromReference(t0.add(12));

        int ret = CLibrary.INSTANCE.ntp_gettime(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.time);
        env.memory.setNativeLongReference(t0.add(4), param0.maxerror);
        env.memory.setNativeLongReference(t0.add(8), param0.esterror);
        env.memory.setNativeLongReference(t0.add(12), param0.tai);
    }

//    public static void obstack_printf (Environment env) {
//        BitVec t0 = env.register.get('0');
//
//        obstack param0 = new obstack();
//
//
//        int ret = CLibrary.INSTANCE.obstack_printf(param0);
//        env.register.set('0', new BitVec(ret));
//
//
//    }
//
//    public static void obstack_vprintf (Environment env) {
//        BitVec t0 = env.register.get('0');
//
//        obstack param0 = new obstack();
//
//
//        int ret = CLibrary.INSTANCE.obstack_vprintf(param0);
//        env.register.set('0', new BitVec(ret));
//
//
//    }

//    public static void on_exit (Environment env) {
//        BitVec t0 = env.register.get('0');
//
//        void param0 ;
//
//        param0 = env.memory.getVoid(t0);
//
//        int ret = CLibrary.INSTANCE.on_exit(param0);
//        env.register.set('0', new BitVec(ret));
//
//
//        env.memory.setVoid(t0, param0);
//    }

    public static void open(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        int param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getInt(t1);

        int ret = CLibrary.INSTANCE.open(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void opendir(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        int ret = CLibrary.INSTANCE.opendir(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void openlog(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        String param0;
        int param1;
        int param2;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getInt(t1);
        param2 = env.memory.getInt(t2);

        CLibrary.INSTANCE.openlog(param0, param1, param2);

        env.memory.setTextReference(t0, param0);
        ////env.memory.setInt(t1, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void openpty(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        IntByReference param0;
        IntByReference param1;
        String param2;

        param0 = env.memory.getIntRef(t0);
        param1 = env.memory.getIntRef(t1);
        param2 = env.memory.getTextFromReference(t2);

        int ret = CLibrary.INSTANCE.openpty(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
        env.memory.setIntReference(t1, param1.getValue());
        env.memory.setTextReference(t2, param2);
    }

//    public static void open_memstream (Environment env) {
//        BitVec t0 = env.register.get('0');
//        BitVec t1 = env.register.get('1');
//
//        String[] param0 ;
//        int[] param1 ;
//
//        param0 = (String[]) env.memory.getArray(t0, -1);
//        param1 = env.memory.getIntArray(t1, param1);
//
//        int ret = CLibrary.INSTANCE.open_memstream(param0, param1);
//        env.register.set('0', new BitVec(ret));
//
//
//        env.memory.setArray(t0, -1, param0);
//        env.memory.setIntArray(t1, param1, param1);
//    }

    public static void parse_printf_format(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        byte[] param0;
        int param1;

        param1 = env.memory.getInt(t1);
        param0 = env.memory.getByteArray(t0, param1);

        int ret = CLibrary.INSTANCE.parse_printf_format(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setByteArray(t0, param1, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void fpathconf(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        int param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getInt(t1);

        NativeLong ret = CLibrary.INSTANCE.fpathconf(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void pause(Environment env) {

        int ret = CLibrary.INSTANCE.pause();
        env.register.set('0', new BitVec(ret));

    }

    public static void pclose(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        int ret = CLibrary.INSTANCE.pclose(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void perror(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        CLibrary.INSTANCE.perror(param0);

        env.memory.setTextReference(t0, param0);
    }

    public static void pipe(Environment env) {
        BitVec t0 = env.register.get('0');

        int[] param0;

        param0 = env.memory.getIntArray(t0, 2);

        int ret = CLibrary.INSTANCE.pipe(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntArray(t0, 2, param0);
    }

    public static void pkey_alloc(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        int param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getInt(t1);

        int ret = CLibrary.INSTANCE.pkey_alloc(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void pkey_free(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.pkey_free(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void pkey_get(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.pkey_get(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void pkey_mprotect(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');
        BitVec t3 = env.register.get('3');

        LongByReference param0;
        int param1;
        int param2;
        int param3;

        param0 = env.memory.getPointer(t0);
        param1 = env.memory.getInt(t1);
        param2 = env.memory.getInt(t2);
        param3 = env.memory.getInt(t3);

        int ret = CLibrary.INSTANCE.pkey_mprotect(param0, param1, param2, param3);
        env.register.set('0', new BitVec(ret));

        env.memory.setPointer(t0, param0.getValue());
        ////env.memory.setInt(t1, param1);
        //env.memory.setInt(t2, param2);
        //env.memory.setInt(t3, param3);
    }

    public static void pkey_set(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        int param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getInt(t1);

        int ret = CLibrary.INSTANCE.pkey_set(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void popen(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        String param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getTextFromReference(t1);

        int ret = CLibrary.INSTANCE.popen(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setTextReference(t1, param1);
    }

    public static void posix_fallocate(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        int param0;
        NativeLong param1;
        NativeLong param2;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getNativeLong(t1);
        param2 = env.memory.getNativeLong(t2);

        int ret = CLibrary.INSTANCE.posix_fallocate(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        //env.memory.setNativeLongt1, param1);
        //env.memory.setNativeLongt2, param2);
    }

    public static void posix_memalign(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        LongByReference param0;
        int param1;
        int param2;

        param0 = env.memory.getPointer(t0);
        param1 = env.memory.getInt(t1);
        param2 = env.memory.getInt(t2);

        int ret = CLibrary.INSTANCE.posix_memalign(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setPointer(t0, param0.getValue());
        ////env.memory.setInt(t1, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void pow(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        double param0;
        double param1;

        param0 = env.memory.getDouble(t0);
        param1 = env.memory.getDouble(t1);

        double ret = CLibrary.INSTANCE.pow(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setDouble(t0, param0);
        //env.memory.setDouble(t1, param1);
    }

    public static void powf(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        float param0;
        float param1;

        param0 = env.memory.getFloat(t0);
        param1 = env.memory.getFloat(t1);

        float ret = CLibrary.INSTANCE.powf(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setFloat(t0, param0);
        env.memory.setFloat(t1, param1);
    }

    public static void powl(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        NativeLong param0;
        NativeLong param1;

        param0 = env.memory.getNativeLong(t0);
        param1 = env.memory.getNativeLong(t1);

        double ret = CLibrary.INSTANCE.powl(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
        //env.memory.setNativeLongt1, param1);
    }

    public static void pread(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');
        BitVec t3 = env.register.get('3');

        int param0;
        char[] param1;
        int param2;
        NativeLong param3;

        param0 = env.memory.getInt(t0);
        param2 = env.memory.getInt(t2);
        param1 = env.memory.getBuffer(t1, param2);
        param3 = env.memory.getNativeLong(t3);

        int ret = CLibrary.INSTANCE.pread(param0, param1, param2, param3);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        env.memory.setBuffer(t1, param2, param1);
        //env.memory.setInt(t2, param2);
        //env.memory.setNativeLongt3, param3);
    }

    public static void pread64(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        int param0;
        char[] param1;
        int param2;

        param2 = env.memory.getInt(t2);
        param0 = env.memory.getInt(t0);
        param1 = env.memory.getBuffer(t1, param2);

        int ret = CLibrary.INSTANCE.pread64(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        env.memory.setBuffer(t1, param2, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void preadv(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        int param0;
        iovec param1 = new iovec();
        int param2;

        param0 = env.memory.getInt(t0);
        param1.iov_base = env.memory.getPointer(t1);
        param1.iov_len = env.memory.getIntFromReference(t1.add(4));
        param2 = env.memory.getInt(t2);

        int ret = CLibrary.INSTANCE.preadv(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        env.memory.setPointer(t1, param1.iov_base);
        env.memory.setIntReference(t1.add(4), param1.iov_len);
        //env.memory.setInt(t2, param2);
    }

    public static void preadv2(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        int param0;
        iovec param1 = new iovec();
        int param2;

        param0 = env.memory.getInt(t0);
        param1.iov_base = env.memory.getPointer(t1);
        param1.iov_len = env.memory.getIntFromReference(t1.add(4));
        param2 = env.memory.getInt(t2);

        int ret = CLibrary.INSTANCE.preadv2(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        env.memory.setPointer(t1, param1.iov_base);
        env.memory.setIntReference(t1.add(4), param1.iov_len);
        //env.memory.setInt(t2, param2);
    }

    public static void preadv64(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        int param0;
        iovec param1 = new iovec();
        int param2;

        param0 = env.memory.getInt(t0);
        param1.iov_base = env.memory.getPointer(t1);
        param1.iov_len = env.memory.getIntFromReference(t1.add(4));
        param2 = env.memory.getInt(t2);

        int ret = CLibrary.INSTANCE.preadv64(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        env.memory.setPointer(t1, param1.iov_base);
        env.memory.setIntReference(t1.add(4), param1.iov_len);
        //env.memory.setInt(t2, param2);
    }

    public static void preadv64v2(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        iovec param1 = new iovec();

        param0 = env.memory.getInt(t0);
        param1.iov_base = env.memory.getPointer(t1);
        param1.iov_len = env.memory.getIntFromReference(t1.add(4));

        int ret = CLibrary.INSTANCE.preadv64v2(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        env.memory.setPointer(t1, param1.iov_base);
        env.memory.setIntReference(t1.add(4), param1.iov_len);
    }

    public static void printf_size(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        int ret = CLibrary.INSTANCE.printf_size(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void printf_size_info(Environment env) {
        BitVec t0 = env.register.get('0');

        printf_info param0 = new printf_info();

        int ret = CLibrary.INSTANCE.printf_size_info(param0);
        env.register.set('0', new BitVec(ret));

    }

    public static void psignal(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        String param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getTextFromReference(t1);

        CLibrary.INSTANCE.psignal(param0, param1);

        //env.memory.setInt(t0, param0);
        env.memory.setTextReference(t1, param1);
    }

    public static void pthread_cond_clockwait(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        int ret = CLibrary.INSTANCE.pthread_cond_clockwait(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void pthread_getattr_default_np(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        int ret = CLibrary.INSTANCE.pthread_getattr_default_np(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void pthread_key_create(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        int ret = CLibrary.INSTANCE.pthread_key_create(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void pthread_key_delete(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.pthread_key_delete(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void pthread_rwlock_clockrdlock(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        int ret = CLibrary.INSTANCE.pthread_rwlock_clockrdlock(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void pthread_rwlock_clockwrlock(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        int ret = CLibrary.INSTANCE.pthread_rwlock_clockwrlock(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void pthread_setattr_default_np(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        int ret = CLibrary.INSTANCE.pthread_setattr_default_np(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void pthread_setspecific(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        LongByReference param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getPointer(t1);

        int ret = CLibrary.INSTANCE.pthread_setspecific(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        env.memory.setPointer(t1, param1.getValue());
    }

    public static void pthread_tryjoin_np(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        NativeLong param0;
        LongByReference param1;

        param0 = env.memory.getNativeLong(t0);
        param1 = env.memory.getPointer(t1);

        int ret = CLibrary.INSTANCE.pthread_tryjoin_np(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
        env.memory.setPointer(t1, param1.getValue());
    }

    public static void ptsname(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        byte ret = CLibrary.INSTANCE.ptsname(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void ptsname_r(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        int param0;
        byte[] param1;
        int param2;

        param0 = env.memory.getInt(t0);
        param2 = env.memory.getInt(t2);
        param1 = env.memory.getByteArray(t1, param2);

        int ret = CLibrary.INSTANCE.ptsname_r(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        env.memory.setByteArray(t1, param2, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void fputc(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        IntByReference param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getIntRef(t1);

        int ret = CLibrary.INSTANCE.fputc(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        env.memory.setIntReference(t1, param1.getValue());
    }

    public static void putchar(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.putchar(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void putchar_unlocked(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.putchar_unlocked(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void putc_unlocked(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        IntByReference param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getIntRef(t1);

        int ret = CLibrary.INSTANCE.putc_unlocked(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        env.memory.setIntReference(t1, param1.getValue());
    }

    public static void putenv(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        int ret = CLibrary.INSTANCE.putenv(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void putpwent(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        passwd param0 = new passwd();
        IntByReference param1;

        param0.pw_name = env.memory.getPointer(t0);
        param0.pw_passwd = env.memory.getPointer(t0.add(4));
        param0.pw_uid = env.memory.getIntFromReference(t0.add(8));
        param0.pw_gid = env.memory.getIntFromReference(t0.add(12));
        param0.pw_gecos = env.memory.getPointer(t0.add(16));
        param0.pw_dir = env.memory.getPointer(t0.add(20));
        param0.pw_shell = env.memory.getPointer(t0.add(24));
        param1 = env.memory.getIntRef(t1);

        int ret = CLibrary.INSTANCE.putpwent(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setPointer(t0, param0.pw_name);
        env.memory.setPointer(t0.add(4), param0.pw_passwd);
        env.memory.setIntReference(t0.add(8), param0.pw_uid);
        env.memory.setIntReference(t0.add(12), param0.pw_gid);
        env.memory.setPointer(t0.add(16), param0.pw_gecos);
        env.memory.setPointer(t0.add(20), param0.pw_dir);
        env.memory.setPointer(t0.add(24), param0.pw_shell);
        env.memory.setIntReference(t1, param1.getValue());
    }

    public static void puts(Environment env) {
        BitVec t0 = env.register.get('0');

        // Set parameters
        String param0 = env.memory.getTextFromReference(t0) + "\n";

        // Call API function
        CLibrary.INSTANCE.puts(param0);
        // Update memory and r0 register
    }

    public static void fputs(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        IntByReference param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getIntRef(t1);

        int ret = CLibrary.INSTANCE.fputs(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setIntReference(t1, param1.getValue());
    }

    public static void pututline(Environment env) {
        BitVec t0 = env.register.get('0');

        utmp param0 = new utmp();

        utmp ret = CLibrary.INSTANCE.pututline(param0);
        env.register.set('0', new BitVec(ret));

    }

    public static void putw(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        IntByReference param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getIntRef(t1);

        int ret = CLibrary.INSTANCE.putw(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        env.memory.setIntReference(t1, param1.getValue());
    }

    public static void fputwc(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        byte param0;
        IntByReference param1;

        param0 = env.memory.getByte(t0);
        param1 = env.memory.getIntRef(t1);

        int ret = CLibrary.INSTANCE.fputwc(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setByte(t0, param0);
        env.memory.setIntReference(t1, param1.getValue());
    }

    public static void putwchar(Environment env) {
        BitVec t0 = env.register.get('0');

        byte param0;

        param0 = env.memory.getByte(t0);

        int ret = CLibrary.INSTANCE.putwchar(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setByte(t0, param0);
    }

    public static void putwchar_unlocked(Environment env) {
        BitVec t0 = env.register.get('0');

        byte param0;

        param0 = env.memory.getByte(t0);

        int ret = CLibrary.INSTANCE.putwchar_unlocked(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setByte(t0, param0);
    }

    public static void fputwc_unlocked(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        byte param0;
        IntByReference param1;

        param0 = env.memory.getByte(t0);
        param1 = env.memory.getIntRef(t1);

        int ret = CLibrary.INSTANCE.fputwc_unlocked(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setByte(t0, param0);
        env.memory.setIntReference(t1, param1.getValue());
    }

    public static void pwrite(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');
        BitVec t3 = env.register.get('3');

        int param0;
        char[] param1;
        int param2;
        NativeLong param3;

        param0 = env.memory.getInt(t0);
        param2 = env.memory.getInt(t2);
        param1 = env.memory.getBuffer(t1, param2);
        param3 = env.memory.getNativeLong(t3);

        int ret = CLibrary.INSTANCE.pwrite(param0, param1, param2, param3);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        env.memory.setBuffer(t1, param2, param1);
        //env.memory.setInt(t2, param2);
        //env.memory.setNativeLongt3, param3);
    }

    public static void pwrite64(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        int param0;
        char[] param1;
        int param2;

        param0 = env.memory.getInt(t0);
        param2 = env.memory.getInt(t2);
        param1 = env.memory.getBuffer(t1, param2);

        int ret = CLibrary.INSTANCE.pwrite64(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        env.memory.setBuffer(t1, param2, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void pwritev(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        int param0;
        iovec param1 = new iovec();
        int param2;

        param0 = env.memory.getInt(t0);
        param1.iov_base = env.memory.getPointer(t1);
        param1.iov_len = env.memory.getIntFromReference(t1.add(4));
        param2 = env.memory.getInt(t2);

        int ret = CLibrary.INSTANCE.pwritev(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        env.memory.setPointer(t1, param1.iov_base);
        env.memory.setIntReference(t1.add(4), param1.iov_len);
        //env.memory.setInt(t2, param2);
    }

    public static void pwritev2(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        int param0;
        iovec param1 = new iovec();
        int param2;

        param0 = env.memory.getInt(t0);
        param1.iov_base = env.memory.getPointer(t1);
        param1.iov_len = env.memory.getIntFromReference(t1.add(4));
        param2 = env.memory.getInt(t2);

        int ret = CLibrary.INSTANCE.pwritev2(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        env.memory.setPointer(t1, param1.iov_base);
        env.memory.setIntReference(t1.add(4), param1.iov_len);
        //env.memory.setInt(t2, param2);
    }

    public static void pwritev64(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        int param0;
        iovec param1 = new iovec();
        int param2;

        param0 = env.memory.getInt(t0);
        param1.iov_base = env.memory.getPointer(t1);
        param1.iov_len = env.memory.getIntFromReference(t1.add(4));
        param2 = env.memory.getInt(t2);

        int ret = CLibrary.INSTANCE.pwritev64(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        env.memory.setPointer(t1, param1.iov_base);
        env.memory.setIntReference(t1.add(4), param1.iov_len);
        //env.memory.setInt(t2, param2);
    }

    public static void pwritev64v2(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        iovec param1 = new iovec();

        param0 = env.memory.getInt(t0);
        param1.iov_base = env.memory.getPointer(t1);
        param1.iov_len = env.memory.getIntFromReference(t1.add(4));

        int ret = CLibrary.INSTANCE.pwritev64v2(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        env.memory.setPointer(t1, param1.iov_base);
        env.memory.setIntReference(t1.add(4), param1.iov_len);
    }

    public static void qecvt(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');
        BitVec t3 = env.register.get('3');

        NativeLong param0;
        int param1;
        IntByReference param2;
        IntByReference param3;

        param0 = env.memory.getNativeLong(t0);
        param1 = env.memory.getInt(t1);
        param2 = env.memory.getIntRef(t2);
        param3 = env.memory.getIntRef(t3);

        byte ret = CLibrary.INSTANCE.qecvt(param0, param1, param2, param3);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
        ////env.memory.setInt(t1, param1);
        env.memory.setIntReference(t2, param2.getValue());
        env.memory.setIntReference(t3, param3.getValue());
    }

    public static void qecvt_r(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        NativeLong param0;
        int param1;

        param0 = env.memory.getNativeLong(t0);
        param1 = env.memory.getInt(t1);

        int ret = CLibrary.INSTANCE.qecvt_r(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void qfcvt(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');
        BitVec t3 = env.register.get('3');

        NativeLong param0;
        int param1;
        IntByReference param2;
        IntByReference param3;

        param0 = env.memory.getNativeLong(t0);
        param1 = env.memory.getInt(t1);
        param2 = env.memory.getIntRef(t2);
        param3 = env.memory.getIntRef(t3);

        byte ret = CLibrary.INSTANCE.qfcvt(param0, param1, param2, param3);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
        ////env.memory.setInt(t1, param1);
        env.memory.setIntReference(t2, param2.getValue());
        env.memory.setIntReference(t3, param3.getValue());
    }

    public static void qfcvt_r(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        NativeLong param0;
        int param1;

        param0 = env.memory.getNativeLong(t0);
        param1 = env.memory.getInt(t1);

        int ret = CLibrary.INSTANCE.qfcvt_r(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void qgcvt(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        NativeLong param0;
        int param1;
        String param2;

        param0 = env.memory.getNativeLong(t0);
        param1 = env.memory.getInt(t1);
        param2 = env.memory.getTextFromReference(t2);

        byte ret = CLibrary.INSTANCE.qgcvt(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
        ////env.memory.setInt(t1, param1);
        env.memory.setTextReference(t2, param2);
    }

    public static void qsort(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        LongByReference param0;
        int param1;
        int param2;

        param0 = env.memory.getPointer(t0);
        param1 = env.memory.getInt(t1);
        param2 = env.memory.getInt(t2);

        CLibrary.INSTANCE.qsort(param0, param1, param2);

        env.memory.setPointer(t0, param0.getValue());
        ////env.memory.setInt(t1, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void raise(Environment env) throws Exception {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        //int ret = CLibrary.INSTANCE.raise(param0);
        int ret = 0;
        //throw new Exception("raise");
        env.register.set('0', new BitVec(ret));
        //env.memory.setInt(t0, param0);
    }

    public static void rand(Environment env) {
        int ret = CLibrary.INSTANCE.rand();
        env.register.set('0', new BitVec(ret));
    }

    public static void random(Environment env) {
        NativeLong ret = CLibrary.INSTANCE.random();
        env.register.set('0', new BitVec(ret));
    }

    public static void random_r(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        random_data param0 = new random_data();
        IntByReference param1;

        param0.fptr = env.memory.getPointer(t0);
        param0.rptr = env.memory.getPointer(t0.add(4));
        param0.state = env.memory.getPointer(t0.add(8));
        param0.rand_type = env.memory.getIntFromReference(t0.add(12));
        param0.rand_deg = env.memory.getIntFromReference(t0.add(16));
        param0.rand_sep = env.memory.getIntFromReference(t0.add(20));
        param0.end_ptr = env.memory.getPointer(t0.add(24));
        param1 = env.memory.getIntRef(t1);

        int ret = CLibrary.INSTANCE.random_r(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setPointer(t0, param0.fptr);
        env.memory.setPointer(t0.add(4), param0.rptr);
        env.memory.setPointer(t0.add(8), param0.state);
        env.memory.setIntReference(t0.add(12), param0.rand_type);
        env.memory.setIntReference(t0.add(16), param0.rand_deg);
        env.memory.setIntReference(t0.add(20), param0.rand_sep);
        env.memory.setPointer(t0.add(24), param0.end_ptr);
        env.memory.setIntReference(t1, param1.getValue());
    }

    public static void rand_r(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        int ret = CLibrary.INSTANCE.rand_r(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void rawmemchr(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        LongByReference param0;
        int param1;

        param0 = env.memory.getPointer(t0);
        param1 = env.memory.getInt(t1);

        CLibrary.INSTANCE.rawmemchr(param0, param1);

        env.memory.setPointer(t0, param0.getValue());
        ////env.memory.setInt(t1, param1);
    }

    public static void read(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        int param0;
        char[] param1;
        int param2;

        param0 = env.memory.getInt(t0);
        param2 = env.memory.getInt(t2);
        param1 = env.memory.getBuffer(t1, param2);

        int ret = CLibrary.INSTANCE.read(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        env.memory.setBuffer(t1, param2, param1);
        //env.memory.setInt(t2, param2);
    }

//    public static void readdir (Environment env) {
//        BitVec t0 = env.register.get('0');
//
//        IntByReference param0 ;
//
//        param0 = env.memory.getIntRef(t0);
//
//        dirent ret = CLibrary.INSTANCE.readdir(param0);
//        env.register.set('0', new BitVec(ret));
//
//
//        env.memory.setIntReference(t0, param0.getValue());
//    }

//    public static void readdir64 (Environment env) {
//        BitVec t0 = env.register.get('0');
//
//        IntByReference param0 ;
//
//        param0 = env.memory.getIntRef(t0);
//
//        CLibrary.INSTANCE.dirent64 ret = CLibrary.INSTANCE.readdir64(param0);
//        env.register.set('0', new BitVec(ret));
//
//
//        env.memory.setIntReference(t0, param0.getValue());
//    }

    public static void readdir64_r(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        int ret = CLibrary.INSTANCE.readdir64_r(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void readlink(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        int ret = CLibrary.INSTANCE.readlink(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void readv(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        int param0;
        iovec param1 = new iovec();
        int param2;

        param0 = env.memory.getInt(t0);
        param1.iov_base = env.memory.getPointer(t1);
        param1.iov_len = env.memory.getIntFromReference(t1.add(4));
        param2 = env.memory.getInt(t2);

        int ret = CLibrary.INSTANCE.readv(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        env.memory.setPointer(t1, param1.iov_base);
        env.memory.setIntReference(t1.add(4), param1.iov_len);
        //env.memory.setInt(t2, param2);
    }

    public static void realloc(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        LongByReference param0;
        int param1;

        param0 = env.memory.getPointer(t0);
        param1 = env.memory.getInt(t1);

//        CLibrary.INSTANCE.realloc(param0, param1);
//
//        env.memory.setPointer(t0, param0.getValue());
        ////env.memory.setInt(t1, param1);
    }

    public static void reallocarray(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        LongByReference param0;
        int param1;
        int param2;

        param0 = env.memory.getPointer(t0);
        param1 = env.memory.getInt(t1);
        param2 = env.memory.getInt(t2);

        CLibrary.INSTANCE.reallocarray(param0, param1, param2);

        env.memory.setPointer(t0, param0.getValue());
        ////env.memory.setInt(t1, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void realpath(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        byte ret = CLibrary.INSTANCE.realpath(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void recv(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');
        BitVec t3 = env.register.get('3');

        int param0;
        char[] param1;
        int param2;
        int param3;

        param0 = env.memory.getInt(t0);
        param2 = env.memory.getInt(t2);
        param1 = env.memory.getBuffer(t1, param2);
        param3 = env.memory.getInt(t3);

        int ret = CLibrary.INSTANCE.recv(param0, param1, param2, param3);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        env.memory.setBuffer(t1, param2, param1);
        //env.memory.setInt(t2, param2);
        //env.memory.setInt(t3, param3);
    }

    public static void regcomp(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        IntByReference param0;
        String param1;
        int param2;

        param0 = env.memory.getIntRef(t0);
        param1 = env.memory.getTextFromReference(t1);
        param2 = env.memory.getInt(t2);

        int ret = CLibrary.INSTANCE.regcomp(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
        env.memory.setTextReference(t1, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void regfree(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        CLibrary.INSTANCE.regfree(param0);

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void register_printf_function(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        int param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getInt(t1);

        int ret = CLibrary.INSTANCE.register_printf_function(param0, param1);
        env.register.set('0', new BitVec(ret));
        //env.memory.setInt(t0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void remainder(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        double param0;
        double param1;

        param0 = env.memory.getDouble(t0);
        param1 = env.memory.getDouble(t1);

        double ret = CLibrary.INSTANCE.remainder(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setDouble(t0, param0);
        //env.memory.setDouble(t1, param1);
    }

    public static void remainderf(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        float param0;
        float param1;

        param0 = env.memory.getFloat(t0);
        param1 = env.memory.getFloat(t1);

        float ret = CLibrary.INSTANCE.remainderf(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setFloat(t0, param0);
        env.memory.setFloat(t1, param1);
    }

    public static void remainderl(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        NativeLong param0;
        NativeLong param1;

        param0 = env.memory.getNativeLong(t0);
        param1 = env.memory.getNativeLong(t1);

        double ret = CLibrary.INSTANCE.remainderl(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
        //env.memory.setNativeLongt1, param1);
    }

    public static void remove(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        int ret = CLibrary.INSTANCE.remove(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void rename(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        String param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getTextFromReference(t1);

        int ret = CLibrary.INSTANCE.rename(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setTextReference(t1, param1);
    }

    public static void rewind(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        CLibrary.INSTANCE.rewind(param0);

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void rewinddir(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        CLibrary.INSTANCE.rewinddir(param0);

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void rindex(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        int param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getInt(t1);

        byte ret = CLibrary.INSTANCE.rindex(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void rint(Environment env) {
        BitVec t0 = env.register.get('0');

        double param0;

        param0 = env.memory.getDouble(t0);

        double ret = CLibrary.INSTANCE.rint(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setDouble(t0, param0);
    }

    public static void rintf(Environment env) {
        BitVec t0 = env.register.get('0');

        float param0;

        param0 = env.memory.getFloat(t0);

        float ret = CLibrary.INSTANCE.rintf(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setFloat(t0, param0);
    }

    public static void rintl(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        double ret = CLibrary.INSTANCE.rintl(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
    }

    public static void rmdir(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        int ret = CLibrary.INSTANCE.rmdir(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void round(Environment env) {
        BitVec t0 = env.register.get('0');

        double param0;

        param0 = env.memory.getDouble(t0);

        double ret = CLibrary.INSTANCE.round(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setDouble(t0, param0);
    }

    public static void roundf(Environment env) {
        BitVec t0 = env.register.get('0');

        float param0;

        param0 = env.memory.getFloat(t0);

        float ret = CLibrary.INSTANCE.roundf(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setFloat(t0, param0);
    }

    public static void roundl(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        double ret = CLibrary.INSTANCE.roundl(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
    }

    public static void rpmatch(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        int ret = CLibrary.INSTANCE.rpmatch(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void scalb(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        double param0;
        double param1;

        param0 = env.memory.getDouble(t0);
        param1 = env.memory.getDouble(t1);

        double ret = CLibrary.INSTANCE.scalb(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setDouble(t0, param0);
        //env.memory.setDouble(t1, param1);
    }

    public static void scalbf(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        float param0;
        float param1;

        param0 = env.memory.getFloat(t0);
        param1 = env.memory.getFloat(t1);

        float ret = CLibrary.INSTANCE.scalbf(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setFloat(t0, param0);
        env.memory.setFloat(t1, param1);
    }

    public static void scalbl(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        NativeLong param0;
        NativeLong param1;

        param0 = env.memory.getNativeLong(t0);
        param1 = env.memory.getNativeLong(t1);

        double ret = CLibrary.INSTANCE.scalbl(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
        //env.memory.setNativeLongt1, param1);
    }

    public static void scalbln(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        double param0;
        int param1;

        param0 = env.memory.getDouble(t0);
        param1 = env.memory.getInt(t1);

        double ret = CLibrary.INSTANCE.scalbln(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setDouble(t0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void scalblnf(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        float param0;
        int param1;

        param0 = env.memory.getFloat(t0);
        param1 = env.memory.getInt(t1);

        float ret = CLibrary.INSTANCE.scalblnf(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setFloat(t0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void scalblnl(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        NativeLong param0;
        int param1;

        param0 = env.memory.getNativeLong(t0);
        param1 = env.memory.getInt(t1);

        double ret = CLibrary.INSTANCE.scalblnl(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void scalbn(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        double param0;
        int param1;

        param0 = env.memory.getDouble(t0);
        param1 = env.memory.getInt(t1);

        double ret = CLibrary.INSTANCE.scalbn(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setDouble(t0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void scalbnf(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        float param0;
        int param1;

        param0 = env.memory.getFloat(t0);
        param1 = env.memory.getInt(t1);

        float ret = CLibrary.INSTANCE.scalbnf(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setFloat(t0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void scalbnl(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        NativeLong param0;
        int param1;

        param0 = env.memory.getNativeLong(t0);
        param1 = env.memory.getInt(t1);

        double ret = CLibrary.INSTANCE.scalbnl(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void scandir(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        int ret = CLibrary.INSTANCE.scandir(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void scandir64(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        int ret = CLibrary.INSTANCE.scandir64(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void sched_getaffinity(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        int param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getInt(t1);

        int ret = CLibrary.INSTANCE.sched_getaffinity(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void sched_getparam(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        sched_param param1 = new sched_param();

        param0 = env.memory.getInt(t0);
        param1.sched_priority = env.memory.getIntFromReference(t1);

        int ret = CLibrary.INSTANCE.sched_getparam(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        env.memory.setIntReference(t1, param1.sched_priority);

    }

    public static void sched_getscheduler(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.sched_getscheduler(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void sched_get_priority_max(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.sched_get_priority_max(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void sched_get_priority_min(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.sched_get_priority_min(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void sched_rr_get_interval(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        timespec param1 = new timespec();

        param0 = env.memory.getInt(t0);
        param1.tv_sec = env.memory.getIntFromReference(t1);
        param1.tv_nsec = env.memory.getNativeLongFromReference(t1.add(4));

        int ret = CLibrary.INSTANCE.sched_rr_get_interval(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        env.memory.setIntReference(t1, param1.tv_sec);
        env.memory.setNativeLongReference(t1.add(4), param1.tv_nsec);
    }

    public static void sched_setaffinity(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        int param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getInt(t1);

        int ret = CLibrary.INSTANCE.sched_setaffinity(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void sched_setparam(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        sched_param param1 = new sched_param();

        param0 = env.memory.getInt(t0);
        param1.sched_priority = env.memory.getIntFromReference(t1);

        int ret = CLibrary.INSTANCE.sched_setparam(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        env.memory.setIntReference(t1, param1.sched_priority);

    }

    public static void sched_setscheduler(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        int param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getInt(t1);

        int ret = CLibrary.INSTANCE.sched_setscheduler(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void sched_yield(Environment env) {

        int ret = CLibrary.INSTANCE.sched_yield();
        env.register.set('0', new BitVec(ret));

    }

    public static void secure_getenv(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        byte ret = CLibrary.INSTANCE.secure_getenv(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void seed48(Environment env) {
        BitVec t0 = env.register.get('0');

        short[] param0;

        param0 = env.memory.getShortArray(t0, 3);

        short ret = CLibrary.INSTANCE.seed48(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setShortArray(t0, 3, param0);
    }

    public static void seed48_r(Environment env) {
        BitVec t0 = env.register.get('0');

        int[] param0;

        param0 = env.memory.getIntArray(t0, 3);

        int ret = CLibrary.INSTANCE.seed48_r(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntArray(t0, 3, param0);
    }

    public static void seekdir(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        IntByReference param0;
        NativeLong param1;

        param0 = env.memory.getIntRef(t0);
        param1 = env.memory.getNativeLong(t1);

        CLibrary.INSTANCE.seekdir(param0, param1);

        env.memory.setIntReference(t0, param0.getValue());
        //env.memory.setNativeLongt1, param1);
    }

    public static void semget(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        int param0;
        int param1;
        int param2;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getInt(t1);
        param2 = env.memory.getInt(t2);

        int ret = CLibrary.INSTANCE.semget(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        ////env.memory.setInt(t1, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void semop(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        int param0;
        sembuf param1 = new sembuf();
        int param2;

        param0 = env.memory.getInt(t0);
        param1.sem_num = env.memory.getShort(t1);
        param1.sem_op = env.memory.getShort(t1.add(2));
        param1.sem_flg = env.memory.getShort(t1.add(4));
        param2 = env.memory.getInt(t2);

        int ret = CLibrary.INSTANCE.semop(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        env.memory.setShortReference(t1, param1.sem_num);
        env.memory.setShortReference(t1.add(2), param1.sem_op);
        env.memory.setShortReference(t1.add(4), param1.sem_flg);
        //env.memory.setInt(t2, param2);
    }

    public static void semtimedop(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        int param0;
        sembuf param1 = new sembuf();
        int param2;

        param0 = env.memory.getInt(t0);
        param1.sem_num = env.memory.getShortFromReference(t1);
        param1.sem_op = env.memory.getShortFromReference(t1.add(2));
        param1.sem_flg = env.memory.getShortFromReference(t1.add(4));
        param2 = env.memory.getInt(t2);

        int ret = CLibrary.INSTANCE.semtimedop(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        env.memory.setShortReference(t1, param1.sem_num);
        env.memory.setShortReference(t1.add(2), param1.sem_op);
        env.memory.setShortReference(t1.add(4), param1.sem_flg);
        //env.memory.setInt(t2, param2);
    }

    public static void sem_clockwait(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        int ret = CLibrary.INSTANCE.sem_clockwait(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void sem_close(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        int ret = CLibrary.INSTANCE.sem_close(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void sem_destroy(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        int ret = CLibrary.INSTANCE.sem_destroy(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void sem_getvalue(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        IntByReference param0;
        IntByReference param1;

        param0 = env.memory.getIntRef(t0);
        param1 = env.memory.getIntRef(t1);

        int ret = CLibrary.INSTANCE.sem_getvalue(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
        env.memory.setIntReference(t1, param1.getValue());
    }

    public static void sem_init(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        IntByReference param0;
        int param1;
        int param2;

        param0 = env.memory.getIntRef(t0);
        param1 = env.memory.getInt(t1);
        param2 = env.memory.getInt(t2);

        int ret = CLibrary.INSTANCE.sem_init(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
        ////env.memory.setInt(t1, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void sem_post(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        int ret = CLibrary.INSTANCE.sem_post(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void sem_timedwait(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        IntByReference param0;
        timespec param1 = new timespec();

        param0 = env.memory.getIntRef(t0);
        param1.tv_sec = env.memory.getIntFromReference(t1);
        param1.tv_nsec = env.memory.getNativeLongFromReference(t1.add(4));

        int ret = CLibrary.INSTANCE.sem_timedwait(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
        env.memory.setIntReference(t1, param1.tv_sec);
        env.memory.setNativeLongReference(t1.add(4), param1.tv_nsec);
    }

    public static void sem_trywait(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        int ret = CLibrary.INSTANCE.sem_trywait(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void sem_unlink(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        int ret = CLibrary.INSTANCE.sem_unlink(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void sem_wait(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        int ret = CLibrary.INSTANCE.sem_wait(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void send(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');
        BitVec t3 = env.register.get('3');

        int param0;
        char[] param1;
        int param2;
        int param3;

        param2 = env.memory.getInt(t2);
        param0 = env.memory.getInt(t0);
        param1 = env.memory.getBuffer(t1, param2);
        param3 = env.memory.getInt(t3);

        int ret = CLibrary.INSTANCE.send(param0, param1, param2, param3);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        env.memory.setBuffer(t1, param2, param1);
        //env.memory.setInt(t2, param2);
        //env.memory.setInt(t3, param3);
    }

    public static void setbuf(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        IntByReference param0;
        String param1;

        param0 = env.memory.getIntRef(t0);
        param1 = env.memory.getTextFromReference(t1);

        CLibrary.INSTANCE.setbuf(param0, param1);

        env.memory.setIntReference(t0, param0.getValue());
        env.memory.setTextReference(t1, param1);
    }

    public static void setbuffer(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        int[] param0;
        byte[] param1;
        int param2;

        param2 = env.memory.getInt(t2);
        param0 = env.memory.getIntArray(t0, param2);
        param1 = env.memory.getByteArray(t1, param2);

        CLibrary.INSTANCE.setbuffer(param0, param1, param2);

        env.memory.setIntArray(t0, param2, param0);
        env.memory.setByteArray(t1, param2, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void setcontext(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        int ret = CLibrary.INSTANCE.setcontext(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void setdomainname(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        byte[] param0;
        int param1;

        param1 = env.memory.getInt(t1);
        param0 = env.memory.getByteArray(t0, param1);

        int ret = CLibrary.INSTANCE.setdomainname(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setByteArray(t0, param1, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void setegid(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.setegid(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void setenv(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        String param0;
        String param1;
        int param2;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getTextFromReference(t1);
        param2 = env.memory.getInt(t2);

        int ret = CLibrary.INSTANCE.setenv(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setTextReference(t1, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void seteuid(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.seteuid(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void setfsent(Environment env) {

        int ret = CLibrary.INSTANCE.setfsent();
        env.register.set('0', new BitVec(ret));

    }

    public static void setgid(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.setgid(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void setgrent(Environment env) {

        CLibrary.INSTANCE.setgrent();

    }

    public static void setgroups(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        int[] param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getIntArray(t1, param0);

        int ret = CLibrary.INSTANCE.setgroups(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        env.memory.setIntArray(t1, param0, param1);
    }

    public static void sethostent(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        CLibrary.INSTANCE.sethostent(param0);

        //env.memory.setInt(t0, param0);
    }

    public static void sethostid(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        int ret = CLibrary.INSTANCE.sethostid(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
    }

    public static void sethostname(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        byte[] param0;
        int param1;

        param1 = env.memory.getInt(t1);
        param0 = env.memory.getByteArray(t0, param1);

        int ret = CLibrary.INSTANCE.sethostname(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setByteArray(t0, param1, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void setitimer(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.setitimer(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void setjmp(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.setjmp(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void setlinebuf(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        CLibrary.INSTANCE.setlinebuf(param0);

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void setlocale(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        String param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getTextFromReference(t1);

        byte ret = CLibrary.INSTANCE.setlocale(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        env.memory.setTextReference(t1, param1);
    }

    public static void setlogmask(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.setlogmask(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void setmntent(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        String param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getTextFromReference(t1);

        int ret = CLibrary.INSTANCE.setmntent(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setTextReference(t1, param1);
    }

    public static void setnetent(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        CLibrary.INSTANCE.setnetent(param0);

        //env.memory.setInt(t0, param0);
    }

    public static void setnetgrent(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        int ret = CLibrary.INSTANCE.setnetgrent(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void setpgid(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        int param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getInt(t1);

        int ret = CLibrary.INSTANCE.setpgid(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void setpgrp(Environment env) {

        int ret = CLibrary.INSTANCE.setpgrp();
        env.register.set('0', new BitVec(ret));

    }

    public static void setpriority(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        int param0;
        int param1;
        int param2;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getInt(t1);
        param2 = env.memory.getInt(t2);

        int ret = CLibrary.INSTANCE.setpriority(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        ////env.memory.setInt(t1, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void setprotoent(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        CLibrary.INSTANCE.setprotoent(param0);

        //env.memory.setInt(t0, param0);
    }

    public static void setpwent(Environment env) {

        CLibrary.INSTANCE.setpwent();

    }

    public static void setregid(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        int param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getInt(t1);

        int ret = CLibrary.INSTANCE.setregid(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void setreuid(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        int param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getInt(t1);

        int ret = CLibrary.INSTANCE.setreuid(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void setrlimit(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        rlimit param1 = new rlimit();

        param0 = env.memory.getInt(t0);
        param1.rlim_cur = env.memory.getIntFromReference(t1);
        param1.rlim_max = env.memory.getIntFromReference(t1.add(4));

        int ret = CLibrary.INSTANCE.setrlimit(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        env.memory.setIntReference(t1, param1.rlim_cur);
        env.memory.setIntReference(t1.add(4), param1.rlim_max);
    }

    public static void setrlimit64(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.setrlimit64(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void setservent(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        CLibrary.INSTANCE.setservent(param0);

        //env.memory.setInt(t0, param0);
    }

    public static void setsid(Environment env) {

        int ret = CLibrary.INSTANCE.setsid();
        env.register.set('0', new BitVec(ret));

    }

    public static void setsockopt(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        int param0;
        int param1;
        int param2;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getInt(t1);
        param2 = env.memory.getInt(t2);

        int ret = CLibrary.INSTANCE.setsockopt(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        ////env.memory.setInt(t1, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void setstate(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        byte ret = CLibrary.INSTANCE.setstate(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void setstate_r(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        random_data param1 = new random_data();

        param0 = env.memory.getTextFromReference(t0);
        param1.fptr = env.memory.getPointer(t1);
        param1.rptr = env.memory.getPointer(t1.add(4));
        param1.state = env.memory.getPointer(t1.add(8));
        param1.rand_type = env.memory.getIntFromReference(t1.add(12));
        param1.rand_deg = env.memory.getIntFromReference(t1.add(16));
        param1.rand_sep = env.memory.getIntFromReference(t1.add(20));
        param1.end_ptr = env.memory.getPointer(t1.add(24));

        int ret = CLibrary.INSTANCE.setstate_r(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setPointer(t1, param1.fptr);
        env.memory.setPointer(t1.add(4), param1.rptr);
        env.memory.setPointer(t1.add(8), param1.state);
        env.memory.setIntReference(t1.add(12), param1.rand_type);
        env.memory.setIntReference(t1.add(16), param1.rand_deg);
        env.memory.setIntReference(t1.add(20), param1.rand_sep);
        env.memory.setPointer(t1.add(24), param1.end_ptr);
    }

    public static void settimeofday(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        timeval param0 = new timeval();
        timezone param1 = new timezone();

        param0.tv_sec = env.memory.getIntFromReference(t0);
        param0.tv_usec = env.memory.getNativeLongFromReference(t0.add(4));
        param1.tz_minuteswest = env.memory.getIntFromReference(t1);
        param1.tz_dsttime = env.memory.getIntFromReference(t1.add(4));

        int ret = CLibrary.INSTANCE.settimeofday(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.tv_sec);
        env.memory.setNativeLongReference(t0.add(4), param0.tv_usec);
        env.memory.setIntReference(t1, param1.tz_minuteswest);
        env.memory.setIntReference(t1.add(4), param1.tz_dsttime);
    }

    public static void setuid(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.setuid(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void setutent(Environment env) {

        CLibrary.INSTANCE.setutent();

    }

    public static void setvbuf(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');
        BitVec t3 = env.register.get('3');

        int[] param0;
        byte[] param1;
        int param2;
        int param3;

        param3 = env.memory.getInt(t3);
        param0 = env.memory.getIntArray(t0, param3);
        param1 = env.memory.getByteArray(t1, param3);
        param2 = env.memory.getInt(t2);

        int ret = CLibrary.INSTANCE.setvbuf(param0, param1, param2, param3);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntArray(t0, param3, param0);
        env.memory.setByteArray(t1, param3, param1);
        //env.memory.setInt(t2, param2);
        //env.memory.setInt(t3, param3);
    }

    public static void shm_open(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        String param0;
        int param1;
        int param2;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getInt(t1);
        param2 = env.memory.getInt(t2);

        int ret = CLibrary.INSTANCE.shm_open(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        ////env.memory.setInt(t1, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void shm_unlink(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        int ret = CLibrary.INSTANCE.shm_unlink(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void shutdown(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        int param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getInt(t1);

        int ret = CLibrary.INSTANCE.shutdown(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void sigabbrev_np(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        byte ret = CLibrary.INSTANCE.sigabbrev_np(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void sigaddset(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        NativeLongByReference param0;
        int param1;

        param0 = env.memory.getNativeLongRef(t0);
        param1 = env.memory.getInt(t1);

        int ret = CLibrary.INSTANCE.sigaddset(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setNativeLongReference(t0, param0.getValue());
        ////env.memory.setInt(t1, param1);
    }

    public static void sigaltstack(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        IntByReference param0;
        IntByReference param1;

        param0 = env.memory.getIntRef(t0);
        param1 = env.memory.getIntRef(t1);

        int ret = CLibrary.INSTANCE.sigaltstack(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
        env.memory.setIntReference(t1, param1.getValue());
    }

    public static void sigblock(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.sigblock(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void sigdelset(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        NativeLongByReference param0;
        int param1;

        param0 = env.memory.getNativeLongRef(t0);
        param1 = env.memory.getInt(t1);

        int ret = CLibrary.INSTANCE.sigdelset(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setNativeLongReference(t0, param0.getValue());
        ////env.memory.setInt(t1, param1);
    }

    public static void sigdescr_np(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        byte ret = CLibrary.INSTANCE.sigdescr_np(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void sigemptyset(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLongByReference param0;

        param0 = env.memory.getNativeLongRef(t0);

        int ret = CLibrary.INSTANCE.sigemptyset(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setNativeLongReference(t0, param0.getValue());
    }

    public static void sigfillset(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLongByReference param0;

        param0 = env.memory.getNativeLongRef(t0);

        int ret = CLibrary.INSTANCE.sigfillset(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setNativeLongReference(t0, param0.getValue());
    }

    public static void siginterrupt(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        int param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getInt(t1);

        int ret = CLibrary.INSTANCE.siginterrupt(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void sigismember(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        NativeLongByReference param0;
        int param1;

        param0 = env.memory.getNativeLongRef(t0);
        param1 = env.memory.getInt(t1);

        int ret = CLibrary.INSTANCE.sigismember(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setNativeLongReference(t0, param0.getValue());
        ////env.memory.setInt(t1, param1);
    }

    public static void siglongjmp(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        int param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getInt(t1);

        CLibrary.INSTANCE.siglongjmp(param0, param1);

        //env.memory.setInt(t0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void sigmask(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.sigmask(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void signal(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        int param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getInt(t1);

        int ret = CLibrary.INSTANCE.signal(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void significand(Environment env) {
        BitVec t0 = env.register.get('0');

        double param0;

        param0 = env.memory.getDouble(t0);

        double ret = CLibrary.INSTANCE.significand(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setDouble(t0, param0);
    }

    public static void significandf(Environment env) {
        BitVec t0 = env.register.get('0');

        float param0;

        param0 = env.memory.getFloat(t0);

        float ret = CLibrary.INSTANCE.significandf(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setFloat(t0, param0);
    }

    public static void significandl(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        double ret = CLibrary.INSTANCE.significandl(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
    }

    public static void sigpause(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.sigpause(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void sigpending(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLongByReference param0;

        param0 = env.memory.getNativeLongRef(t0);

        int ret = CLibrary.INSTANCE.sigpending(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setNativeLongReference(t0, param0.getValue());
    }

    public static void sigprocmask(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        int param0;
        NativeLongByReference param1;
        NativeLongByReference param2;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getNativeLongRef(t1);
        param2 = env.memory.getNativeLongRef(t2);

        int ret = CLibrary.INSTANCE.sigprocmask(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        env.memory.setNativeLongReference(t1, param1.getValue());
        env.memory.setNativeLongReference(t2, param2.getValue());
    }

    public static void sigsetjmp(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        int param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getInt(t1);

        int ret = CLibrary.INSTANCE.sigsetjmp(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void sigsetmask(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.sigsetmask(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void sigsuspend(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLongByReference param0;

        param0 = env.memory.getNativeLongRef(t0);

        int ret = CLibrary.INSTANCE.sigsuspend(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setNativeLongReference(t0, param0.getValue());
    }

    public static void sin(Environment env) {
        BitVec t0 = env.register.get('0');

        double param0;

        param0 = env.memory.getDouble(t0);

        double ret = CLibrary.INSTANCE.sin(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setDouble(t0, param0);
    }

    public static void sincos(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        double param0;
        DoubleByReference param1;
        DoubleByReference param2;

        param0 = env.memory.getDouble(t0);
        param1 = env.memory.getDoubleRef(t1);
        param2 = env.memory.getDoubleRef(t2);

        CLibrary.INSTANCE.sincos(param0, param1, param2);

        //env.memory.setDouble(t0, param0);
        //env.memory.setDouble(t1, param1.getValue());
        //env.memory.setDouble(t2, param2.getValue());
    }

    public static void sincosf(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        float param0;
        FloatByReference param1;
        FloatByReference param2;

        param0 = env.memory.getFloat(t0);
        param1 = env.memory.getFloatRef(t1);
        param2 = env.memory.getFloatRef(t2);

        CLibrary.INSTANCE.sincosf(param0, param1, param2);

        env.memory.setFloat(t0, param0);
        env.memory.setFloat(t1, param1.getValue());
        env.memory.setFloat(t2, param2.getValue());
    }

    public static void sincosl(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        NativeLong param0;
        NativeLongByReference param1;
        NativeLongByReference param2;

        param0 = env.memory.getNativeLong(t0);
        param1 = env.memory.getNativeLongRef(t1);
        param2 = env.memory.getNativeLongRef(t2);

        CLibrary.INSTANCE.sincosl(param0, param1, param2);

        //env.memory.setNativeLongt0, param0);
        env.memory.setNativeLongReference(t1, param1.getValue());
        env.memory.setNativeLongReference(t2, param2.getValue());
    }

    public static void sinf(Environment env) {
        BitVec t0 = env.register.get('0');

        float param0;

        param0 = env.memory.getFloat(t0);

        float ret = CLibrary.INSTANCE.sinf(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setFloat(t0, param0);
    }

    public static void sinh(Environment env) {
        BitVec t0 = env.register.get('0');

        double param0;

        param0 = env.memory.getDouble(t0);

        double ret = CLibrary.INSTANCE.sinh(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setDouble(t0, param0);
    }

    public static void sinhf(Environment env) {
        BitVec t0 = env.register.get('0');

        float param0;

        param0 = env.memory.getFloat(t0);

        float ret = CLibrary.INSTANCE.sinhf(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setFloat(t0, param0);
    }

    public static void sinhl(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        double ret = CLibrary.INSTANCE.sinhl(param0);
        env.register.set('0', new BitVec(ret));

    }

    public static void sinl(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        double ret = CLibrary.INSTANCE.sinl(param0);
        env.register.set('0', new BitVec(ret));

    }

    public static void socket(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        int param0;
        int param1;
        int param2;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getInt(t1);
        param2 = env.memory.getInt(t2);

        int ret = CLibrary.INSTANCE.socket(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

    }

    public static void socketpair(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');
        BitVec t3 = env.register.get('3');

        int param0;
        int param1;
        int param2;
        int[] param3;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getInt(t1);
        param2 = env.memory.getInt(t2);
        param3 = env.memory.getIntArray(t3, 2);

        int ret = CLibrary.INSTANCE.socketpair(param0, param1, param2, param3);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntArray(t3, 2, param3);
    }

    public static void sqrt(Environment env) {
        BitVec t0 = env.register.get('0');

        double param0;

        param0 = env.memory.getDouble(t0);

        double ret = CLibrary.INSTANCE.sqrt(param0);
        env.register.set('0', new BitVec(ret));
    }

    public static void sqrtf(Environment env) {
        BitVec t0 = env.register.get('0');

        float param0;

        param0 = env.memory.getFloat(t0);

        float ret = CLibrary.INSTANCE.sqrtf(param0);
        env.register.set('0', new BitVec(ret));

    }

    public static void sqrtl(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        double ret = CLibrary.INSTANCE.sqrtl(param0);
        env.register.set('0', new BitVec(ret));

    }

    public static void srand(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        CLibrary.INSTANCE.srand(param0);

    }

    public static void srand48(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        CLibrary.INSTANCE.srand48(param0);

    }

    public static void srand48_r(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        drand48_data param1 = new drand48_data();

        param0 = env.memory.getInt(t0);
        param1.__x = env.memory.getShortRef(t1);
        param1.__old_x = env.memory.getShortRef(t1.add(4));
        param1.__c = env.memory.getShortFromReference(t1.add(8));
        param1.__init = env.memory.getShortFromReference(t1.add(10));
        param1.__a = env.memory.getLongFromReference(t1.add(12));

        int ret = CLibrary.INSTANCE.srand48_r(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        env.memory.setShortReference(t1, param1.__x.getValue());
        env.memory.setShortReference(t1.add(4), param1.__old_x.getValue());
        env.memory.setShortReference(t1.add(8), param1.__c);
        env.memory.setShortReference(t1.add(10), param1.__init);
        env.memory.setLongReference(t1.add(12), param1.__a);
    }

    public static void srandom(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        CLibrary.INSTANCE.srandom(param0);

    }

    public static void srandom_r(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        random_data param1 = new random_data();

        param0 = env.memory.getInt(t0);
        param1.fptr = env.memory.getPointer(t1);
        param1.rptr = env.memory.getPointer(t1.add(4));
        param1.state = env.memory.getPointer(t1.add(8));
        param1.rand_type = env.memory.getIntFromReference(t1.add(12));
        param1.rand_deg = env.memory.getIntFromReference(t1.add(16));
        param1.rand_sep = env.memory.getIntFromReference(t1.add(20));
        param1.end_ptr = env.memory.getPointer(t1.add(24));

        int ret = CLibrary.INSTANCE.srandom_r(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setPointer(t1, param1.fptr);
        env.memory.setPointer(t1.add(4), param1.rptr);
        env.memory.setPointer(t1.add(8), param1.state);
        env.memory.setIntReference(t1.add(12), param1.rand_type);
        env.memory.setIntReference(t1.add(16), param1.rand_deg);
        env.memory.setIntReference(t1.add(20), param1.rand_sep);
        env.memory.setPointer(t1.add(24), param1.end_ptr);
    }

    public static void ssignal(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        int param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getInt(t1);

        int ret = CLibrary.INSTANCE.ssignal(param0, param1);
        env.register.set('0', new BitVec(ret));
    }

    public static void stime(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        int ret = CLibrary.INSTANCE.stime(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void stpcpy(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        String param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getTextFromReference(t1);

        byte ret = CLibrary.INSTANCE.stpcpy(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setTextReference(t1, param1);
    }

    public static void stpncpy(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        byte[] param0;
        byte[] param1;
        int param2;

        param2 = env.memory.getInt(t2);
        param0 = env.memory.getByteArray(t0, param2);
        param1 = env.memory.getByteArray(t1, param2);

        byte ret = CLibrary.INSTANCE.stpncpy(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setByteArray(t0, param2, param0);
        env.memory.setByteArray(t1, param2, param1);
    }

    public static void strcasecmp(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        String param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getTextFromReference(t1);

        int ret = CLibrary.INSTANCE.strcasecmp(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setTextReference(t1, param1);
    }

    public static void strcasestr(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        String param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getTextFromReference(t1);

        byte ret = CLibrary.INSTANCE.strcasestr(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setTextReference(t1, param1);
    }

    public static void strcat(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        String param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getTextFromReference(t1);

        byte ret = CLibrary.INSTANCE.strcat(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setTextReference(t1, param1);
    }

    public static void strchr(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        int param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getInt(t1);

        byte ret = CLibrary.INSTANCE.strchr(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void strchrnul(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        int param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getInt(t1);

        byte ret = CLibrary.INSTANCE.strchrnul(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void strcmp(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        String param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getTextFromReference(t1);

        int ret = CLibrary.INSTANCE.strcmp(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setTextReference(t1, param1);
    }

    public static void strcoll(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        String param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getTextFromReference(t1);

        int ret = CLibrary.INSTANCE.strcoll(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setTextReference(t1, param1);
    }

    public static void strcpy(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        String param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getTextFromReference(t1);

        byte ret = CLibrary.INSTANCE.strcpy(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setTextReference(t1, param1);
    }

    public static void strcspn(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        String param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getTextFromReference(t1);

        int ret = CLibrary.INSTANCE.strcspn(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setTextReference(t1, param1);
    }

    public static void strdup(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        byte ret = CLibrary.INSTANCE.strdup(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void strdupa(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        byte ret = CLibrary.INSTANCE.strdupa(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void strerror(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        byte ret = CLibrary.INSTANCE.strerror(param0);
        env.register.set('0', new BitVec(ret));
    }

    public static void strerrordesc_np(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        byte ret = CLibrary.INSTANCE.strerrordesc_np(param0);
        env.register.set('0', new BitVec(ret));
    }

    public static void strerrorname_np(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        byte ret = CLibrary.INSTANCE.strerrorname_np(param0);
        env.register.set('0', new BitVec(ret));

    }

    public static void strerror_r(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        int param0;
        byte[] param1;
        int param2;

        param2 = env.memory.getInt(t2);
        param0 = env.memory.getInt(t0);
        param1 = env.memory.getByteArray(t1, param2);

        int ret = CLibrary.INSTANCE.strerror_r(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setByteArray(t1, param2, param1);
    }

    public static void strfromd(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        byte[] param0;
        int param1;
        byte[] param2;

        param1 = env.memory.getInt(t1);
        param0 = env.memory.getByteArray(t0, param1);
        param2 = env.memory.getByteArray(t2, param1);

        int ret = CLibrary.INSTANCE.strfromd(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setByteArray(t0, param1, param0);
        env.memory.setByteArray(t2, param1, param2);
    }

    public static void strfromf(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        byte[] param0;
        int param1;
        byte[] param2;

        param1 = env.memory.getInt(t1);
        param0 = env.memory.getByteArray(t0, param1);
        param2 = env.memory.getByteArray(t2, param1);

        int ret = CLibrary.INSTANCE.strfromf(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setByteArray(t0, param1, param0);
        env.memory.setByteArray(t2, param1, param2);
    }

    public static void strfroml(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        byte[] param0;
        int param1;
        byte[] param2;

        param1 = env.memory.getInt(t1);
        param0 = env.memory.getByteArray(t0, param1);
        param2 = env.memory.getByteArray(t2, param1);

        int ret = CLibrary.INSTANCE.strfroml(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setByteArray(t0, param1, param0);
        ////env.memory.setInt(t1, param1);
        env.memory.setByteArray(t2, param1, param2);
    }

    public static void strfry(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        byte ret = CLibrary.INSTANCE.strfry(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void strftime(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        byte[] param0;
        int param1;

        param1 = env.memory.getInt(t1);
        param0 = env.memory.getByteArray(t0, param1);

        int ret = CLibrary.INSTANCE.strftime(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setByteArray(t0, param1, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void strlen(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        int ret = CLibrary.INSTANCE.strlen(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void strncasecmp(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        byte[] param0;
        byte[] param1;
        int param2;

        param2 = env.memory.getInt(t2);
        param0 = env.memory.getByteArray(t0, param2);
        param1 = env.memory.getByteArray(t1, param2);

        int ret = CLibrary.INSTANCE.strncasecmp(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setByteArray(t0, param2, param0);
        env.memory.setByteArray(t1, param2, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void strncat(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        byte[] param0;
        byte[] param1;
        int param2;

        param2 = env.memory.getInt(t2);
        param0 = env.memory.getByteArray(t0, param2);
        param1 = env.memory.getByteArray(t1, param2);

        byte ret = CLibrary.INSTANCE.strncat(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setByteArray(t0, param2, param0);
        env.memory.setByteArray(t1, param2, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void strncmp(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        byte[] param0;
        byte[] param1;
        int param2;

        param2 = env.memory.getInt(t2);
        param0 = env.memory.getByteArray(t0, param2);
        param1 = env.memory.getByteArray(t1, param2);

        int ret = CLibrary.INSTANCE.strncmp(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setByteArray(t0, param2, param0);
        env.memory.setByteArray(t1, param2, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void strncpy(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        byte[] param0;
        byte[] param1;
        int param2;

        param2 = env.memory.getInt(t2);
        param0 = env.memory.getByteArray(t0, param2);
        param1 = env.memory.getByteArray(t1, param2);

        byte ret = CLibrary.INSTANCE.strncpy(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setByteArray(t0, param2, param0);
        env.memory.setByteArray(t1, param2, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void strndup(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        byte[] param0;
        int param1;

        param1 = env.memory.getInt(t1);
        param0 = env.memory.getByteArray(t0, param1);

        byte ret = CLibrary.INSTANCE.strndup(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setByteArray(t0, param1, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void strndupa(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        byte[] param0;
        int param1;

        param1 = env.memory.getInt(t1);
        param0 = env.memory.getByteArray(t0, param1);

        byte ret = CLibrary.INSTANCE.strndupa(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setByteArray(t0, param1, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void strnlen(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        byte[] param0;
        int param1;

        param1 = env.memory.getInt(t1);
        param0 = env.memory.getByteArray(t0, param1);

        int ret = CLibrary.INSTANCE.strnlen(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setByteArray(t0, param1, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void strpbrk(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        String param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getTextFromReference(t1);

        byte ret = CLibrary.INSTANCE.strpbrk(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setTextReference(t1, param1);
    }

    public static void strptime(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        String param0;
        String param1;
        tm param2 = new tm();

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getTextFromReference(t1);
        param2.tm_sec = env.memory.getIntFromReference(t2);
        param2.tm_min = env.memory.getIntFromReference(t2.add(4));
        param2.tm_hour = env.memory.getIntFromReference(t2.add(8));
        param2.tm_mday = env.memory.getIntFromReference(t2.add(12));
        param2.tm_mon = env.memory.getIntFromReference(t2.add(16));
        param2.tm_year = env.memory.getIntFromReference(t2.add(20));
        param2.tm_wday = env.memory.getIntFromReference(t2.add(24));
        param2.tm_yday = env.memory.getIntFromReference(t2.add(28));
        param2.tm_isdst = env.memory.getIntFromReference(t2.add(32));
        param2.__tm_gmtoff = env.memory.getNativeLongFromReference(t2.add(36));
        param2.__tm_zone = env.memory.getStringFromReference(t2.add(40));

        byte ret = CLibrary.INSTANCE.strptime(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setTextReference(t1, param1);
        env.memory.setIntReference(t2, param2.tm_sec);
        env.memory.setIntReference(t2.add(4), param2.tm_min);
        env.memory.setIntReference(t2.add(8), param2.tm_hour);
        env.memory.setIntReference(t2.add(12), param2.tm_mday);
        env.memory.setIntReference(t2.add(16), param2.tm_mon);
        env.memory.setIntReference(t2.add(20), param2.tm_year);
        env.memory.setIntReference(t2.add(24), param2.tm_wday);
        env.memory.setIntReference(t2.add(28), param2.tm_yday);
        env.memory.setIntReference(t2.add(32), param2.tm_isdst);
        env.memory.setNativeLongReference(t2.add(36), param2.__tm_gmtoff);
        env.memory.setStringReference(t2.add(40), param2.__tm_zone);
    }

    public static void strrchr(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        int param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getInt(t1);

        byte ret = CLibrary.INSTANCE.strrchr(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void strsep(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String[] param0;
        String param1;

        param0 = (String[]) env.memory.getArray(t0, -1);
        param1 = env.memory.getTextFromReference(t1);

        byte ret = CLibrary.INSTANCE.strsep(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setArray(t0, -1, param0);
        env.memory.setTextReference(t1, param1);
    }

    public static void strsignal(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        byte ret = CLibrary.INSTANCE.strsignal(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void strspn(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        String param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getTextFromReference(t1);

        int ret = CLibrary.INSTANCE.strspn(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setTextReference(t1, param1);
    }

    public static void strstr(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        String param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getTextFromReference(t1);

        byte ret = CLibrary.INSTANCE.strstr(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setTextReference(t1, param1);
    }

    public static void strtod(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        String[] param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = (String[]) env.memory.getArray(t1, -1);

        double ret = CLibrary.INSTANCE.strtod(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setArray(t1, -1, param1);
    }

    public static void strtof(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        String[] param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = (String[]) env.memory.getArray(t1, -1);

        float ret = CLibrary.INSTANCE.strtof(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setArray(t1, -1, param1);
    }

    public static void strtoimax(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        String param0;
        String[] param1;
        int param2;

        param0 = env.memory.getTextFromReference(t0);
        param1 = (String[]) env.memory.getArray(t1, -1);
        param2 = env.memory.getInt(t2);

        long ret = CLibrary.INSTANCE.strtoimax(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setArray(t1, -1, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void strtok(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        String param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getTextFromReference(t1);

        byte ret = CLibrary.INSTANCE.strtok(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setTextReference(t1, param1);
    }

    public static void strtok_r(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        String param0;
        String param1;
        String[] param2;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getTextFromReference(t1);
        param2 = (String[]) env.memory.getArray(t2, -1);

        byte ret = CLibrary.INSTANCE.strtok_r(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setTextReference(t1, param1);
        env.memory.setArray(t2, -1, param2);
    }

    public static void strtol(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        String param0;
        String[] param1;
        int param2;

        param0 = env.memory.getTextFromReference(t0);
        param1 = (String[]) env.memory.getArray(t1, -1);
        param2 = env.memory.getInt(t2);

        NativeLong ret = CLibrary.INSTANCE.strtol(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setArray(t1, -1, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void strtold(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        String[] param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = (String[]) env.memory.getArray(t1, -1);

        double ret = CLibrary.INSTANCE.strtold(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setArray(t1, -1, param1);
    }

    public static void strtoll(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        String param0;
        String[] param1;
        int param2;

        param0 = env.memory.getTextFromReference(t0);
        param1 = (String[]) env.memory.getArray(t1, -1);
        param2 = env.memory.getInt(t2);

        long ret = CLibrary.INSTANCE.strtoll(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setArray(t1, -1, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void strtoq(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        long ret = CLibrary.INSTANCE.strtoq(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void strtoul(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        String param0;
        String[] param1;
        int param2;

        param0 = env.memory.getTextFromReference(t0);
        param1 = (String[]) env.memory.getArray(t1, -1);
        param2 = env.memory.getInt(t2);

        NativeLong ret = CLibrary.INSTANCE.strtoul(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setArray(t1, -1, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void strtoull(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        long ret = CLibrary.INSTANCE.strtoull(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void strtoumax(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        String param0;
        String[] param1;
        int param2;

        param0 = env.memory.getTextFromReference(t0);
        param1 = (String[]) env.memory.getArray(t1, -1);
        param2 = env.memory.getInt(t2);

        long ret = CLibrary.INSTANCE.strtoumax(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setArray(t1, -1, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void strtouq(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        long ret = CLibrary.INSTANCE.strtouq(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void strverscmp(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        String param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getTextFromReference(t1);

        int ret = CLibrary.INSTANCE.strverscmp(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setTextReference(t1, param1);
    }

    public static void strxfrm(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        byte[] param0;
        byte[] param1;
        int param2;

        param2 = env.memory.getInt(t2);
        param0 = env.memory.getByteArray(t0, param2);
        param1 = env.memory.getByteArray(t1, param2);

        int ret = CLibrary.INSTANCE.strxfrm(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setByteArray(t0, param2, param0);
        env.memory.setByteArray(t1, param2, param1);
        //env.memory.setInt(t2, param2);
    }

//    public static void stty (Environment env) {
//        BitVec t0 = env.register.get('0');
//        BitVec t1 = env.register.get('1');
//
//        int param0 ;
//        sgttyb param1 = new sgttyb();
//
//        param0 = env.memory.getInt(t0);
//
//        int ret = CLibrary.INSTANCE.stty(param0, param1);
//        env.register.set('0', new BitVec(ret));
//
//
//        //env.memory.setInt(t0, param0);
//    }

    public static void success(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.success(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void swapcontext(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        IntByReference param0;
        IntByReference param1;

        param0 = env.memory.getIntRef(t0);
        param1 = env.memory.getIntRef(t1);

        int ret = CLibrary.INSTANCE.swapcontext(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
        env.memory.setIntReference(t1, param1.getValue());
    }

    public static void sprintf(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        char[] param0;
        String param1;
        String param2;

        param0 = env.memory.getBuffer(t0, 100);
        param1 = env.memory.getTextFromReference(t1);
        param2 = Integer.toString(env.memory.getInt(t2));

        int ret = CLibrary.INSTANCE.sprintf(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setBuffer(t0, param1.length(), param0);
    }

    public static void swprintf(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        byte[] param0;
        int param1;

        param1 = env.memory.getInt(t1);
        param0 = env.memory.getByteArray(t0, param1);

        int ret = CLibrary.INSTANCE.swprintf(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setByteArray(t0, param1, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void swscanf(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        int ret = CLibrary.INSTANCE.swscanf(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void symlink(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        String param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getTextFromReference(t1);

        int ret = CLibrary.INSTANCE.symlink(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setTextReference(t1, param1);
    }

    public static void sync(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.sync(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void sysconf(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        NativeLong ret = CLibrary.INSTANCE.sysconf(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void system(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        int ret = CLibrary.INSTANCE.system(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void sysv_signal(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        int param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getInt(t1);

        int ret = CLibrary.INSTANCE.sysv_signal(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void tan(Environment env) {
        BitVec t0 = env.register.get('0');

        double param0;

        param0 = env.memory.getDouble(t0);

        double ret = CLibrary.INSTANCE.tan(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setDouble(t0, param0);
    }

    public static void tanf(Environment env) {
        BitVec t0 = env.register.get('0');

        float param0;

        param0 = env.memory.getFloat(t0);

        float ret = CLibrary.INSTANCE.tanf(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setFloat(t0, param0);
    }

    public static void tanh(Environment env) {
        BitVec t0 = env.register.get('0');

        double param0;

        param0 = env.memory.getDouble(t0);

        double ret = CLibrary.INSTANCE.tanh(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setDouble(t0, param0);
    }

    public static void tanhf(Environment env) {
        BitVec t0 = env.register.get('0');

        float param0;

        param0 = env.memory.getFloat(t0);

        float ret = CLibrary.INSTANCE.tanhf(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setFloat(t0, param0);
    }

    public static void tanhl(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        double ret = CLibrary.INSTANCE.tanhl(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
    }

    public static void tanl(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        double ret = CLibrary.INSTANCE.tanl(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
    }

    public static void tcdrain(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.tcdrain(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void tcflow(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        int param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getInt(t1);

        int ret = CLibrary.INSTANCE.tcflow(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void tcflush(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        int param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getInt(t1);

        int ret = CLibrary.INSTANCE.tcflush(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void tcgetattr(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        termios param1 = new termios();

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.tcgetattr(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void tcgetpgrp(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.tcgetpgrp(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void tcgetsid(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.tcgetsid(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void tcsendbreak(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        int param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getInt(t1);

        int ret = CLibrary.INSTANCE.tcsendbreak(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void tcsetattr(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        int param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getInt(t1);

        int ret = CLibrary.INSTANCE.tcsetattr(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void tcsetpgrp(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        int param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getInt(t1);

        int ret = CLibrary.INSTANCE.tcsetpgrp(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        ////env.memory.setInt(t1, param1);
    }

//    public static void tdestroy (Environment env) {
//        BitVec t0 = env.register.get('0');
//        BitVec t1 = env.register.get('1');
//
//        LongByReference param0 ;
//        void param1 ;
//
//        param0 = env.memory.getPointer(t0);
//        param1 = env.memory.getVoid(t1);
//
//
//        CLibrary.INSTANCE.tdestroy(param0, param1);
//
//        env.memory.setPointer(t0, param0.getValue());
//        env.memory.setVoid(t1, param1);
//    }

    public static void telldir(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        NativeLong ret = CLibrary.INSTANCE.telldir(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void tempnam(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        String param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getTextFromReference(t1);

        byte ret = CLibrary.INSTANCE.tempnam(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setTextReference(t1, param1);
    }

    public static void textdomain(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        ByReference ret = CLibrary.INSTANCE.textdomain(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void tgamma(Environment env) {
        BitVec t0 = env.register.get('0');

        double param0;

        param0 = env.memory.getDouble(t0);

        double ret = CLibrary.INSTANCE.tgamma(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setDouble(t0, param0);
    }

    public static void tgammaf(Environment env) {
        BitVec t0 = env.register.get('0');

        float param0;

        param0 = env.memory.getFloat(t0);

        float ret = CLibrary.INSTANCE.tgammaf(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setFloat(t0, param0);
    }

    public static void tgammal(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        double ret = CLibrary.INSTANCE.tgammal(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
    }

    public static void tgkill(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        int param0;
        int param1;
        int param2;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getInt(t1);
        param2 = env.memory.getInt(t2);

        int ret = CLibrary.INSTANCE.tgkill(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        ////env.memory.setInt(t1, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void thrd_exit(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        CLibrary.INSTANCE.thrd_exit(param0);

        //env.memory.setInt(t0, param0);
    }

    public static void time(Environment env) {
        BitVec t0 = env.register.get('0');

        lc_time_data param0 = new lc_time_data();
        lc_time_data ret = CLibrary.INSTANCE.time(param0);
        env.register.set('0', new BitVec(ret));

    }

    public static void timegm(Environment env) {
        BitVec t0 = env.register.get('0');

        tm param0 = new tm();

        param0.tm_sec = env.memory.getIntFromReference(t0);
        param0.tm_min = env.memory.getIntFromReference(t0.add(4));
        param0.tm_hour = env.memory.getIntFromReference(t0.add(8));
        param0.tm_mday = env.memory.getIntFromReference(t0.add(12));
        param0.tm_mon = env.memory.getIntFromReference(t0.add(16));
        param0.tm_year = env.memory.getIntFromReference(t0.add(20));
        param0.tm_wday = env.memory.getIntFromReference(t0.add(24));
        param0.tm_yday = env.memory.getIntFromReference(t0.add(28));
        param0.tm_isdst = env.memory.getIntFromReference(t0.add(32));
        param0.__tm_gmtoff = env.memory.getNativeLongFromReference(t0.add(36));
        param0.__tm_zone = env.memory.getStringFromReference(t0.add(40));

        int ret = CLibrary.INSTANCE.timegm(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.tm_sec);
        env.memory.setIntReference(t0.add(4), param0.tm_min);
        env.memory.setIntReference(t0.add(8), param0.tm_hour);
        env.memory.setIntReference(t0.add(12), param0.tm_mday);
        env.memory.setIntReference(t0.add(16), param0.tm_mon);
        env.memory.setIntReference(t0.add(20), param0.tm_year);
        env.memory.setIntReference(t0.add(24), param0.tm_wday);
        env.memory.setIntReference(t0.add(28), param0.tm_yday);
        env.memory.setIntReference(t0.add(32), param0.tm_isdst);
        env.memory.setNativeLongReference(t0.add(36), param0.__tm_gmtoff);
        env.memory.setStringReference(t0.add(40), param0.__tm_zone);
    }

    public static void timelocal(Environment env) {
        BitVec t0 = env.register.get('0');

        tm param0 = new tm();

        param0.tm_sec = env.memory.getIntFromReference(t0);
        param0.tm_min = env.memory.getIntFromReference(t0.add(4));
        param0.tm_hour = env.memory.getIntFromReference(t0.add(8));
        param0.tm_mday = env.memory.getIntFromReference(t0.add(12));
        param0.tm_mon = env.memory.getIntFromReference(t0.add(16));
        param0.tm_year = env.memory.getIntFromReference(t0.add(20));
        param0.tm_wday = env.memory.getIntFromReference(t0.add(24));
        param0.tm_yday = env.memory.getIntFromReference(t0.add(28));
        param0.tm_isdst = env.memory.getIntFromReference(t0.add(32));
        param0.__tm_gmtoff = env.memory.getNativeLongFromReference(t0.add(36));
        param0.__tm_zone = env.memory.getStringFromReference(t0.add(40));

        int ret = CLibrary.INSTANCE.timelocal(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.tm_sec);
        env.memory.setIntReference(t0.add(4), param0.tm_min);
        env.memory.setIntReference(t0.add(8), param0.tm_hour);
        env.memory.setIntReference(t0.add(12), param0.tm_mday);
        env.memory.setIntReference(t0.add(16), param0.tm_mon);
        env.memory.setIntReference(t0.add(20), param0.tm_year);
        env.memory.setIntReference(t0.add(24), param0.tm_wday);
        env.memory.setIntReference(t0.add(28), param0.tm_yday);
        env.memory.setIntReference(t0.add(32), param0.tm_isdst);
        env.memory.setNativeLongReference(t0.add(36), param0.__tm_gmtoff);
        env.memory.setStringReference(t0.add(40), param0.__tm_zone);
    }

    public static void times(Environment env) {
        BitVec t0 = env.register.get('0');

        tms param0 = new tms();

        param0.tms_utime = env.memory.getNativeLongFromReference(t0);
        param0.tms_stime = env.memory.getNativeLongFromReference(t0.add(4));
        param0.tms_cutime = env.memory.getNativeLongFromReference(t0.add(8));
        param0.tms_cstime = env.memory.getNativeLongFromReference(t0.add(12));

        NativeLong ret = CLibrary.INSTANCE.times(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setNativeLongReference(t0, param0.tms_utime);
        env.memory.setNativeLongReference(t0.add(4), param0.tms_stime);
        env.memory.setNativeLongReference(t0.add(8), param0.tms_cutime);
        env.memory.setNativeLongReference(t0.add(12), param0.tms_cstime);
    }

    public static void tmpfile(Environment env) {

        int ret = CLibrary.INSTANCE.tmpfile();
        env.register.set('0', new BitVec(ret));

    }

    public static void tmpfile64(Environment env) {

        int ret = CLibrary.INSTANCE.tmpfile64();
        env.register.set('0', new BitVec(ret));

    }

    public static void tmpnam(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        byte ret = CLibrary.INSTANCE.tmpnam(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void tmpnam_r(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        byte ret = CLibrary.INSTANCE.tmpnam_r(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void toascii(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.toascii(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void tolower(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.tolower(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void toupper(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.toupper(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void towctrans(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        int param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getInt(t1);

        int ret = CLibrary.INSTANCE.towctrans(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void towlower(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.towlower(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void towupper(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.towupper(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void trunc(Environment env) {
        BitVec t0 = env.register.get('0');

        double param0;

        param0 = env.memory.getDouble(t0);

        double ret = CLibrary.INSTANCE.trunc(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setDouble(t0, param0);
    }

    public static void truncate(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        int param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getInt(t1);

        int ret = CLibrary.INSTANCE.truncate(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void truncate64(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        NativeLong param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getNativeLong(t1);

        int ret = CLibrary.INSTANCE.truncate64(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        //env.memory.setNativeLongt1, param1);
    }

    public static void truncf(Environment env) {
        BitVec t0 = env.register.get('0');

        float param0;

        param0 = env.memory.getFloat(t0);

        float ret = CLibrary.INSTANCE.truncf(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setFloat(t0, param0);
    }

    public static void truncl(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        double ret = CLibrary.INSTANCE.truncl(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
    }

    public static void tryagain(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.tryagain(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void ttyname(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        byte ret = CLibrary.INSTANCE.ttyname(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void ttyname_r(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        int param0;
        byte[] param1;
        int param2;

        param0 = env.memory.getInt(t0);
        param2 = env.memory.getInt(t2);
        param1 = env.memory.getByteArray(t1, param2);

        int ret = CLibrary.INSTANCE.ttyname_r(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        env.memory.setByteArray(t1, param2, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void tzset(Environment env) {

        CLibrary.INSTANCE.tzset();

    }

    public static void ulimit(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        NativeLong param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getNativeLong(t1);

        NativeLong ret = CLibrary.INSTANCE.ulimit(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        //env.memory.setNativeLongt1, param1);
    }

    public static void umask(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.umask(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void umount(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        int ret = CLibrary.INSTANCE.umount(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void umount2(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        int param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getInt(t1);

        int ret = CLibrary.INSTANCE.umount2(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void uname(Environment env) {
        BitVec t0 = env.register.get('0');

        utsname param0 = new utsname();

        param0.sysname = env.memory.getByteArray(t0, -1);
        param0.nodename = env.memory.getByteArray(t0.add(4), -1);
        param0.release = env.memory.getByteArray(t0.add(8), -1);
        param0.version = env.memory.getByteArray(t0.add(12), -1);
        param0.machine = env.memory.getByteArray(t0.add(16), -1);
        param0.domainname = env.memory.getByteArray(t0.add(20), -1);

        int ret = CLibrary.INSTANCE.uname(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setByteArray(t0.add(8), 4, param0.release);
        env.memory.setByteArray(t0.add(12), 4, param0.version);
        env.memory.setByteArray(t0.add(16), 4, param0.machine);
        env.memory.setByteArray(t0.add(20), 4, param0.domainname);
    }

    public static void ungetc(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        IntByReference param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getIntRef(t1);

        int ret = CLibrary.INSTANCE.ungetc(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        env.memory.setIntReference(t1, param1.getValue());
    }

    public static void ungetwc(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        IntByReference param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getIntRef(t1);

        int ret = CLibrary.INSTANCE.ungetwc(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        env.memory.setIntReference(t1, param1.getValue());
    }

    public static void unlink(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        int ret = CLibrary.INSTANCE.unlink(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void unlockpt(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.unlockpt(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void unsetenv(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        int ret = CLibrary.INSTANCE.unsetenv(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void updwtmp(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        utmp param1 = new utmp();

        param0 = env.memory.getTextFromReference(t0);

        CLibrary.INSTANCE.updwtmp(param0, param1);

        env.memory.setTextReference(t0, param0);
    }

    public static void utime(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        utimbuf param1 = new utimbuf();

        param0 = env.memory.getTextFromReference(t0);
        param1.actime = env.memory.getIntFromReference(t1);
        param1.modtime = env.memory.getIntFromReference(t1.add(4));

        int ret = CLibrary.INSTANCE.utime(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setIntReference(t1, param1.actime);
        env.memory.setIntReference(t1.add(4), param1.modtime);
    }

    public static void utimes(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        timeval[] param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = (timeval[]) env.memory.getArray(t1, 2);

        int ret = CLibrary.INSTANCE.utimes(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setArray(t1, 2, param1);
    }

    public static void utmpname(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        int ret = CLibrary.INSTANCE.utmpname(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void valloc(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        CLibrary.INSTANCE.valloc(param0);

        //env.memory.setInt(t0, param0);
    }

    public static void vasprintf(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        String[] param0;
        String param1;
        int param2;

        param0 = (String[]) env.memory.getArray(t0, 100);
        param1 = env.memory.getTextFromReference(t1);
        param2 = env.memory.getInt(t2);

        int ret = CLibrary.INSTANCE.vasprintf(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setArray(t0, -1, param0);
        env.memory.setTextReference(t1, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void va_copy(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        int param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getInt(t1);

        CLibrary.INSTANCE.va_copy(param0, param1);

        //env.memory.setInt(t0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void va_end(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        CLibrary.INSTANCE.va_end(param0);

        //env.memory.setInt(t0, param0);
    }

    public static void verr(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        int param0;
        String param1;
        int param2;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getTextFromReference(t1);
        param2 = env.memory.getInt(t2);

        CLibrary.INSTANCE.verr(param0, param1, param2);

        //env.memory.setInt(t0, param0);
        env.memory.setTextReference(t1, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void verrx(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        int param0;
        String param1;
        int param2;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getTextFromReference(t1);
        param2 = env.memory.getInt(t2);

        CLibrary.INSTANCE.verrx(param0, param1, param2);

        //env.memory.setInt(t0, param0);
        env.memory.setTextReference(t1, param1);
    }

    public static void vfork(Environment env) {

        int ret = CLibrary.INSTANCE.vfork();
        env.register.set('0', new BitVec(ret));

    }

    public static void vfprintf(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        IntByReference param0;
        String param1;
        int param2;

        param0 = env.memory.getIntRef(t0);
        param1 = env.memory.getTextFromReference(t1);
        param2 = env.memory.getInt(t2);

        int ret = CLibrary.INSTANCE.vfprintf(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
        env.memory.setTextReference(t1, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void vfscanf(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        IntByReference param0;
        String param1;
        int param2;

        param0 = env.memory.getIntRef(t0);
        param1 = env.memory.getTextFromReference(t1);
        param2 = env.memory.getInt(t2);

        int ret = CLibrary.INSTANCE.vfscanf(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
        env.memory.setTextReference(t1, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void vfwprintf(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        IntByReference param0;
        String param1;
        int param2;

        param0 = env.memory.getIntRef(t0);
        param1 = env.memory.getTextFromReference(t1);
        param2 = env.memory.getInt(t2);

        int ret = CLibrary.INSTANCE.vfwprintf(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
        env.memory.setTextReference(t1, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void vlimit(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        int param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getInt(t1);

        int ret = CLibrary.INSTANCE.vlimit(param0, param1);
        env.register.set('0', new BitVec(ret));
    }

    public static void vprintf(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        int param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getInt(t1);

        int ret = CLibrary.INSTANCE.vprintf(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void vscanf(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        int param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getInt(t1);

        int ret = CLibrary.INSTANCE.vscanf(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void vsnprintf(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');
        BitVec t3 = env.register.get('3');

        char[] param0;
        int param1;
        String param2;
        int param3;

        param1 = env.memory.getIntFromReference(t1);
        param0 = env.memory.getBuffer(t0, param1);
        param2 = env.memory.getStringFromReference(t2);
        param3 = env.memory.getIntFromReference(t3);

        //int ret = CLibrary.INSTANCE.vsnprintf(param0, param1, param2, param3);
        int ret = 0;
        env.register.set('0', new BitVec(ret));

        env.memory.setBuffer(t0, param1, param0);
    }

    public static void vsprintf(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        char[] param0;
        String param1;
        Object[] param2;

        param0 = env.memory.getBuffer(t0, 100);
        param1 = env.memory.getTextFromReference(t1);
        param2 = env.memory.getArray(t2,1);

        int ret = CLibrary.INSTANCE.vsprintf(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setBuffer(t0, 1, param0);
    }

    public static void vsscanf(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        String param0;
        String param1;
        int param2;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getTextFromReference(t1);
        param2 = env.memory.getInt(t2);

        int ret = CLibrary.INSTANCE.vsscanf(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setTextReference(t1, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void vswprintf(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        byte[] param0;
        int param1;

        param1 = env.memory.getInt(t1);
        param0 = env.memory.getByteArray(t0, param1);

        int ret = CLibrary.INSTANCE.vswprintf(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setByteArray(t0, param1, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void vswscanf(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        int ret = CLibrary.INSTANCE.vswscanf(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void vsyslog(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        int param0;
        String param1;
        int param2;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getTextFromReference(t1);
        param2 = env.memory.getInt(t2);

        CLibrary.INSTANCE.vsyslog(param0, param1, param2);

        //env.memory.setInt(t0, param0);
        env.memory.setTextReference(t1, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void vwarn(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        int param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getInt(t1);

        CLibrary.INSTANCE.vwarn(param0, param1);

        env.memory.setTextReference(t0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void vwarnx(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        int param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getInt(t1);

        CLibrary.INSTANCE.vwarnx(param0, param1);

        env.memory.setTextReference(t0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void vwprintf(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        int param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getInt(t1);

        int ret = CLibrary.INSTANCE.vwprintf(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void wait(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        int ret = CLibrary.INSTANCE.wait(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void wait3(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        IntByReference param0;
        int param1;

        param0 = env.memory.getIntRef(t0);
        param1 = env.memory.getInt(t1);

        int ret = CLibrary.INSTANCE.wait3(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
        ////env.memory.setInt(t1, param1);
    }

    public static void wait4(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        int param0;
        IntByReference param1;
        int param2;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getIntRef(t1);
        param2 = env.memory.getInt(t2);

        int ret = CLibrary.INSTANCE.wait4(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        env.memory.setIntReference(t1, param1.getValue());
        //env.memory.setInt(t2, param2);
    }

    public static void waitpid(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        int param0;
        IntByReference param1;
        int param2;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getIntRef(t1);
        param2 = env.memory.getInt(t2);

        int ret = CLibrary.INSTANCE.waitpid(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        env.memory.setIntReference(t1, param1.getValue());
        //env.memory.setInt(t2, param2);
    }

    public static void wcpcpy(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        String param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getTextFromReference(t1);

        char ret = CLibrary.INSTANCE.wcpcpy(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setTextReference(t1, param1);
    }

    public static void wcpncpy(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        byte[] param0;
        byte[] param1;
        int param2;

        param2 = env.memory.getInt(t2);
        param0 = env.memory.getByteArray(t0, param2);
        param1 = env.memory.getByteArray(t1, param2);

        char ret = CLibrary.INSTANCE.wcpncpy(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setByteArray(t0, param2, param0);
        env.memory.setByteArray(t1, param2, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void wcrtomb(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        String param0;
        byte param1;
        IntByReference param2;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getByte(t1);
        param2 = env.memory.getIntRef(t2);

        int ret = CLibrary.INSTANCE.wcrtomb(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setByte(t1, param1);
        env.memory.setIntReference(t2, param2.getValue());
    }

    public static void wcscasecmp(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        String param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getTextFromReference(t1);

        int ret = CLibrary.INSTANCE.wcscasecmp(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setTextReference(t1, param1);
    }

    public static void wcscat(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        String param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getTextFromReference(t1);

        char ret = CLibrary.INSTANCE.wcscat(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setTextReference(t1, param1);
    }

    public static void wcschr(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        byte param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getByte(t1);

        char ret = CLibrary.INSTANCE.wcschr(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setByte(t1, param1);
    }

    public static void wcschrnul(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        byte param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getByte(t1);

        char ret = CLibrary.INSTANCE.wcschrnul(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setByte(t1, param1);
    }

    public static void wcscmp(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        String param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getTextFromReference(t1);

        int ret = CLibrary.INSTANCE.wcscmp(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setTextReference(t1, param1);
    }

    public static void wcscoll(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        String param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getTextFromReference(t1);

        int ret = CLibrary.INSTANCE.wcscoll(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setTextReference(t1, param1);
    }

    public static void wcscpy(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        String param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getTextFromReference(t1);

        char ret = CLibrary.INSTANCE.wcscpy(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setTextReference(t1, param1);
    }

    public static void wcscspn(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        String param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getTextFromReference(t1);

        int ret = CLibrary.INSTANCE.wcscspn(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setTextReference(t1, param1);
    }

    public static void wcsdup(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        char ret = CLibrary.INSTANCE.wcsdup(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void wcsftime(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        byte[] param0;
        int param1;

        param1 = env.memory.getInt(t1);
        param0 = env.memory.getByteArray(t0, param1);

        int ret = CLibrary.INSTANCE.wcsftime(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setByteArray(t0, param1, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void wcslen(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        int ret = CLibrary.INSTANCE.wcslen(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void wcsncasecmp(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        byte[] param0;
        byte[] param1;
        int param2;

        param2 = env.memory.getInt(t2);
        param0 = env.memory.getByteArray(t0, param2);
        param1 = env.memory.getByteArray(t1, param2);

        int ret = CLibrary.INSTANCE.wcsncasecmp(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setByteArray(t0, param2, param0);
        env.memory.setByteArray(t1, param2, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void wcsncat(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        byte[] param0;
        byte[] param1;
        int param2;

        param2 = env.memory.getInt(t2);
        param0 = env.memory.getByteArray(t0, param2);
        param1 = env.memory.getByteArray(t1, param2);

        char ret = CLibrary.INSTANCE.wcsncat(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setByteArray(t0, param2, param0);
        env.memory.setByteArray(t1, param2, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void wcsncmp(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        byte[] param0;
        byte[] param1;
        int param2;

        param2 = env.memory.getInt(t2);
        param0 = env.memory.getByteArray(t0, param2);
        param1 = env.memory.getByteArray(t1, param2);

        int ret = CLibrary.INSTANCE.wcsncmp(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setByteArray(t0, param2, param0);
        env.memory.setByteArray(t1, param2, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void wcsncpy(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        byte[] param0;
        byte[] param1;
        int param2;

        param2 = env.memory.getInt(t2);
        param0 = env.memory.getByteArray(t0, param2);
        param1 = env.memory.getByteArray(t1, param2);

        char ret = CLibrary.INSTANCE.wcsncpy(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setByteArray(t0, param2, param0);
        env.memory.setByteArray(t1, param2, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void wcsnlen(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        byte[] param0;
        int param1;

        param1 = env.memory.getInt(t1);
        param0 = env.memory.getByteArray(t0, param1);

        int ret = CLibrary.INSTANCE.wcsnlen(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setByteArray(t0, param1, param0);
        ////env.memory.setInt(t1, param1);
    }

    public static void wcsnrtombs(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        int ret = CLibrary.INSTANCE.wcsnrtombs(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void wcspbrk(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        String param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getTextFromReference(t1);

        char ret = CLibrary.INSTANCE.wcspbrk(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setTextReference(t1, param1);
    }

    public static void wcsrchr(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        byte param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getByte(t1);

        char ret = CLibrary.INSTANCE.wcsrchr(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setByte(t1, param1);
    }

    public static void wcsrtombs(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        int ret = CLibrary.INSTANCE.wcsrtombs(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void wcsspn(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        String param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getTextFromReference(t1);

        int ret = CLibrary.INSTANCE.wcsspn(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setTextReference(t1, param1);
    }

    public static void wcsstr(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        String param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getTextFromReference(t1);

        char ret = CLibrary.INSTANCE.wcsstr(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setTextReference(t1, param1);
    }

    public static void wcstod(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        double ret = CLibrary.INSTANCE.wcstod(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void wcstof(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        float ret = CLibrary.INSTANCE.wcstof(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void wcstoimax(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        String param0;
        String param1;
        int param2;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getTextFromReference(t1);
        param2 = env.memory.getInt(t2);

        long ret = CLibrary.INSTANCE.wcstoimax(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setTextReference(t1, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void wcstok(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        String param0;
        String param1;
        String param2;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getTextFromReference(t1);
        param2 = env.memory.getTextFromReference(t2);

        char ret = CLibrary.INSTANCE.wcstok(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setTextReference(t1, param1);
        env.memory.setTextReference(t2, param2);
    }

    public static void wcstol(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        NativeLong ret = CLibrary.INSTANCE.wcstol(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void wcstold(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        double ret = CLibrary.INSTANCE.wcstold(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void wcstoll(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        long ret = CLibrary.INSTANCE.wcstoll(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void wcstombs(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        byte[] param0;
        byte[] param1;
        int param2;

        param2 = env.memory.getInt(t2);
        param0 = env.memory.getByteArray(t0, param2);
        param1 = env.memory.getByteArray(t1, param2);

        int ret = CLibrary.INSTANCE.wcstombs(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setByteArray(t0, param2, param0);
        env.memory.setByteArray(t1, param2, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void wcstoq(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        long ret = CLibrary.INSTANCE.wcstoq(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void wcstoul(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        NativeLong ret = CLibrary.INSTANCE.wcstoul(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void wcstoull(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        long ret = CLibrary.INSTANCE.wcstoull(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void wcstoumax(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        String param0;
        String param1;
        int param2;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getTextFromReference(t1);
        param2 = env.memory.getInt(t2);

        long ret = CLibrary.INSTANCE.wcstoumax(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setTextReference(t1, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void wcstouq(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        long ret = CLibrary.INSTANCE.wcstouq(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void wcswcs(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        String param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getTextFromReference(t1);

        char ret = CLibrary.INSTANCE.wcswcs(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setTextReference(t1, param1);
    }

    public static void wcsxfrm(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        int ret = CLibrary.INSTANCE.wcsxfrm(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void wctob(Environment env) {
        BitVec t0 = env.register.get('0');

        int param0;

        param0 = env.memory.getInt(t0);

        int ret = CLibrary.INSTANCE.wctob(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
    }

    public static void wctomb(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        String param0;
        byte param1;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getByte(t1);

        int ret = CLibrary.INSTANCE.wctomb(param0, param1);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setByte(t1, param1);
    }

    public static void wctrans(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        int ret = CLibrary.INSTANCE.wctrans(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void wctype(Environment env) {
        BitVec t0 = env.register.get('0');

        String param0;

        param0 = env.memory.getTextFromReference(t0);

        NativeLong ret = CLibrary.INSTANCE.wctype(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
    }

    public static void wmemchr(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        byte[] param0;
        byte param1;
        int param2;

        param2 = env.memory.getInt(t2);
        param0 = env.memory.getByteArray(t0, param2);
        param1 = env.memory.getByte(t1);

        char ret = CLibrary.INSTANCE.wmemchr(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setByteArray(t0, param2, param0);
        env.memory.setByte(t1, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void wmemcmp(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        byte[] param0;
        byte[] param1;
        int param2;

        param2 = env.memory.getInt(t2);
        param0 = env.memory.getByteArray(t0, param2);
        param1 = env.memory.getByteArray(t1, param2);

        int ret = CLibrary.INSTANCE.wmemcmp(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setByteArray(t0, param2, param0);
        env.memory.setByteArray(t1, param2, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void wmemcpy(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        byte[] param0;
        byte[] param1;
        int param2;

        param2 = env.memory.getInt(t2);
        param0 = env.memory.getByteArray(t0, param2);
        param1 = env.memory.getByteArray(t1, param2);

        char ret = CLibrary.INSTANCE.wmemcpy(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setByteArray(t0, param2, param0);
        env.memory.setByteArray(t1, param2, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void wmemmove(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        byte[] param0;
        byte[] param1;
        int param2;

        param2 = env.memory.getInt(t2);
        param0 = env.memory.getByteArray(t0, param2);
        param1 = env.memory.getByteArray(t1, param2);

        char ret = CLibrary.INSTANCE.wmemmove(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setByteArray(t0, param2, param0);
        env.memory.setByteArray(t1, param2, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void wmempcpy(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        byte[] param0;
        byte[] param1;
        int param2;

        param2 = env.memory.getInt(t2);
        param0 = env.memory.getByteArray(t0, param2);
        param1 = env.memory.getByteArray(t1, param2);

        char ret = CLibrary.INSTANCE.wmempcpy(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setByteArray(t0, param2, param0);
        env.memory.setByteArray(t1, param2, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void wmemset(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        byte[] param0;
        byte param1;
        int param2;

        param2 = env.memory.getInt(t2);
        param0 = env.memory.getByteArray(t0, param2);
        param1 = env.memory.getByte(t1);

        char ret = CLibrary.INSTANCE.wmemset(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setByteArray(t0, param2, param0);
        //env.memory.setByte(t1, param1);
        //env.memory.setInt(t2, param2);
    }

    public static void wordexp(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        String param0;
        IntByReference param1;
        int param2;

        param0 = env.memory.getTextFromReference(t0);
        param1 = env.memory.getIntRef(t1);
        param2 = env.memory.getInt(t2);

        int ret = CLibrary.INSTANCE.wordexp(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        env.memory.setTextReference(t0, param0);
        env.memory.setIntReference(t1, param1.getValue());
        //env.memory.setInt(t2, param2);
    }

    public static void wordfree(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        CLibrary.INSTANCE.wordfree(param0);

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void write(Environment env) {
        BitVec t0 = env.register.get('0');

        IntByReference param0;

        param0 = env.memory.getIntRef(t0);

        int ret = CLibrary.INSTANCE.write(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setIntReference(t0, param0.getValue());
    }

    public static void writev(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');
        BitVec t2 = env.register.get('2');

        int param0;
        iovec param1 = new iovec();
        int param2;

        param0 = env.memory.getInt(t0);
        param1.iov_base = env.memory.getPointer(t1);
        param1.iov_len = env.memory.getIntFromReference(t1.add(4));
        param2 = env.memory.getInt(t2);

        int ret = CLibrary.INSTANCE.writev(param0, param1, param2);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        env.memory.setPointer(t1, param1.iov_base);
        env.memory.setIntReference(t1.add(4), param1.iov_len);
        //env.memory.setInt(t2, param2);
    }

    public static void y0(Environment env) {
        BitVec t0 = env.register.get('0');

        double param0;

        param0 = env.memory.getDouble(t0);

        double ret = CLibrary.INSTANCE.y0(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setDouble(t0, param0);
    }

    public static void y0f(Environment env) {
        BitVec t0 = env.register.get('0');

        float param0;

        param0 = env.memory.getFloat(t0);

        float ret = CLibrary.INSTANCE.y0f(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setFloat(t0, param0);
    }

    public static void y0l(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        double ret = CLibrary.INSTANCE.y0l(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
    }

    public static void y1(Environment env) {
        BitVec t0 = env.register.get('0');

        double param0;

        param0 = env.memory.getDouble(t0);

        double ret = CLibrary.INSTANCE.y1(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setDouble(t0, param0);
    }

    public static void y1f(Environment env) {
        BitVec t0 = env.register.get('0');

        float param0;

        param0 = env.memory.getFloat(t0);

        float ret = CLibrary.INSTANCE.y1f(param0);
        env.register.set('0', new BitVec(ret));

        env.memory.setFloat(t0, param0);
    }

    public static void y1l(Environment env) {
        BitVec t0 = env.register.get('0');

        NativeLong param0;

        param0 = env.memory.getNativeLong(t0);

        double ret = CLibrary.INSTANCE.y1l(param0);
        env.register.set('0', new BitVec(ret));

        //env.memory.setNativeLongt0, param0);
    }

    public static void yn(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        double param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getDouble(t1);

        double ret = CLibrary.INSTANCE.yn(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        //env.memory.setDouble(t1, param1);
    }

    public static void ynf(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        float param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getFloat(t1);

        float ret = CLibrary.INSTANCE.ynf(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        env.memory.setFloat(t1, param1);
    }

    public static void ynl(Environment env) {
        BitVec t0 = env.register.get('0');
        BitVec t1 = env.register.get('1');

        int param0;
        NativeLong param1;

        param0 = env.memory.getInt(t0);
        param1 = env.memory.getNativeLong(t1);

        double ret = CLibrary.INSTANCE.ynl(param0, param1);
        env.register.set('0', new BitVec(ret));

        //env.memory.setInt(t0, param0);
        //env.memory.setNativeLongt1, param1);
    }
}