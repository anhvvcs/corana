package external.jni;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Structure;

public interface CTime extends Library {
    CTime INSTANCE = (CTime)
            Native.load(("c"), CTime.class);

    @Structure.FieldOrder({"tv_sec", "tv_usec"})
    class timeval extends Structure {
        public long tv_sec, tv_usec;
    }

    @Structure.FieldOrder({ "tz_minuteswest", "tz_dsttime" })
    class timezone extends Structure {
        public int tz_minuteswest, tz_dsttime;
    }

    //int gettimeofday(struct timeval *tv, struct timezone *tz);
    int gettimeofday(timeval timeval, timezone timezone);

    @Structure.FieldOrder({ "tms_utime", "tms_stime", "tms_cutime", "tms_cstime" })
    class tms extends Structure {
        public long tms_utime;  /* user time */
        public long tms_stime;  /* system time */
        public long tms_cutime; /* user time of children */
        public long tms_cstime; /* system time of children */
    }

    long clock();
    long times(tms __buffer);

    //int clock_getres(clockid_t clockid, struct timespec *res);
    @Structure.FieldOrder({ "tv_sec", "tv_nsec"})
    class timespec extends Structure {
        public long tv_sec; /* seconds */
        public long tv_nsec;  /* nanoseconds */
    }
    int clock_getres(int clockid, timespec res);

    //int clock_gettime(clockid_t clockid, struct timespec *tp);
    int clock_gettime(int clockid, timespec tp);

    //int nanosleep(const struct timespec *req, struct timespec *rem);
    int nanosleep(timespec req, timespec rem);
}
