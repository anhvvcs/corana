package external.jni;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;
import jdk.jfr.Unsigned;

import java.nio.Buffer;
import java.util.Arrays;
import java.util.List;

public class CStruct {

    @Structure.FieldOrder({ "tms_utime", "tms_stime", "tms_cutime", "tms_cstime" })
    public static class tms extends Structure {
        public NativeLong tms_utime;  /* user time */
        public NativeLong tms_stime;  /* system time */
        public NativeLong tms_cutime; /* user time of children */
        public NativeLong tms_cstime; /* system time of children */
    }

    //
//    //int clock_getres(clockid_t clockid, struct timespec *res);
//    @Structure.FieldOrder({ "tv_sec", "tv_nsec"})
//    public static class timespec extends Structure {
//        public long tv_sec; /* seconds */
//        public long tv_nsec;  /* nanoseconds */
//    }

    @Structure.FieldOrder({ "tv_sec", "tv_usec" })
    public static class timeval extends Structure {
        public int tv_sec;
        public NativeLong tv_usec;
    }
    @Structure.FieldOrder({ "tm_sec", "tm_min", "tm_hour", "tm_mday", "tm_mon", "tm_year", "tm_wday", "tm_yday", "tm_isdst", "__tm_gmtoff", "__tm_zone" })
    public static class tm extends Structure {
        public int tm_sec;
        public int tm_min;
        public int tm_hour;
        public int tm_mday;
        public int tm_mon;
        public int tm_year;
        public int tm_wday;
        public int tm_yday;
        public int tm_isdst;
        public NativeLong __tm_gmtoff;
        public String __tm_zone;
    }
    @Structure.FieldOrder({ "tv_sec", "tv_nsec" })
    public static class timespec extends Structure {
        public int tv_sec;
        public NativeLong tv_nsec;
    }
    @Structure.FieldOrder({ "d", "m", "y" })
    public static class date_t extends Structure {
        public int d;
        public int m;
        public int y;
    }
    @Structure.FieldOrder({ "name", "y", "m", "d", "w", "hh", "mm", "ss", "locale", "format", "printed" })
    public static class Data extends Structure {
        public String name;
        public int y;
        public int m;
        public int d;
        public int w;
        public int hh;
        public int mm;
        public int ss;
        public String locale;
        public String format;
        public String printed;
    }
    @Structure.FieldOrder({ "it_interval", "it_value" })
    public static class itimerspec extends Structure {
        public timespec it_interval;
        public timespec it_value;
    }
    @Structure.FieldOrder({ "it_interval", "it_value" })
    public static class itimerval extends Structure {
        public timeval it_interval;
        public timeval it_value;
    }
    @Structure.FieldOrder({ "tz_minuteswest", "tz_dsttime" })
    public static class timezone extends Structure {
        public int tz_minuteswest;
        public int tz_dsttime;
    }
    @Structure.FieldOrder({ "name", " type", "m", "n", "d", "secs", "offset", "change", "computed_for" })
    public static class tz_rule extends Structure {
        public String name;
        public enum  type {J0, J1, M};
        public short m;
        public short n;
        public short d;
        public int secs;
        public int offset;
        public NativeLong change;
        public int computed_for;
    }

    @Structure.FieldOrder({ "pm_prog", "pm_vers", "pm_prot", "pm_port" })
    public static class pmap extends Structure {
        public int pm_prog;
        public int pm_vers;
        public int pm_prot;
        public int pm_port;
    }
    @Structure.FieldOrder({ "aup_time", "aup_machname", "aup_uid", "aup_gid", "aup_len", "aup_gids" })
    public static class authunix_parms extends Structure {
        public NativeLong aup_time;
        public String aup_machname;
        public int aup_uid;
        public int aup_gid;
        public int aup_len;
        public Pointer aup_gids;
    }

//    @Structure.FieldOrder({ "cb_rpcvers", "cb_prog", "cb_vers", "cb_proc", "cb_cred", "cb_verf" })
//    public static class call_body extends Structure {
//        public NativeLong cb_rpcvers;
//        public NativeLong cb_prog;
//        public NativeLong cb_vers;
//        public NativeLong cb_proc;
//        public opaque_auth cb_cred;
//        public opaque_auth cb_verf;
//    }
    @Structure.FieldOrder({ "name", "key", "window" })
    public static class authdes_fullname extends Structure {
        public String name;
        public int key;
        public int window;
    }
    @Structure.FieldOrder({ "adc_fullname", "adc_nickname" })
    public static class authdes_cred extends Structure {
        public authdes_fullname adc_fullname;
        public int adc_nickname;
    }
    @Structure.FieldOrder({ "tv_sec", "tv_usec" })
    public static class rpc_timeval extends Structure {
        public int tv_sec;
        public int tv_usec;
    }
    @Structure.FieldOrder({ "prog", "vers", "proc", "arglen", "args_ptr", "xdr_args" })
    public static class rmtcallargs extends Structure {
        public NativeLong prog;
        public NativeLong vers;
        public NativeLong proc;
        public NativeLong arglen;
        public int args_ptr;
        public int xdr_args;
    }
//    @Structure.FieldOrder({ "rq_prog", "rq_vers", "rq_proc", "rq_cred", "rq_clntcred", "rq_xprt" })
//    public static class svc_req extends Structure {
//        public NativeLong rq_prog;
//        public NativeLong rq_vers;
//        public NativeLong rq_proc;
//        public opaque_auth rq_cred;
//        public int rq_clntcred;
//        public Pointer rq_xprt;
//    }
    @Structure.FieldOrder({ "client", "socket", "oldprognum", "oldversnum", "valid", "oldhost" })
    public static class callrpc_private_s extends Structure {
        public Pointer client;
        public int socket;
        public NativeLong oldprognum;
        public NativeLong oldversnum;
        public NativeLong valid;
        public String oldhost;
    }
    @Structure.FieldOrder({ "set_called", "unset_called" })
    public static class test_state extends Structure {
        public int set_called;
        public int unset_called;
    }
    @Structure.FieldOrder({ "cu_sock", "cu_closeit", "cu_raddr", "cu_rlen", "cu_wait", "cu_total", "cu_error", "cu_outxdrs", "cu_xdrpos", "cu_sendsz", "cu_outbuf", "cu_recvsz", "cu_inbuf" })
    public static class cu_data extends Structure {
        public int cu_sock;
        public int cu_closeit;
        public sockaddr_in cu_raddr;
        public int cu_rlen;
        public timeval cu_wait;
        public timeval cu_total;
        public int cu_error;
        public int cu_outxdrs;
        public int cu_xdrpos;
        public int cu_sendsz;
        public String cu_outbuf;
        public int cu_recvsz;
        public byte[] cu_inbuf;
    }
    //Manually
    @Structure.FieldOrder({ "sin_famil", "sin_port","sin_addr" })
    public static class sockaddr_in extends Structure {
        public int    sin_family; /* address family: AF_INET */
        public int      sin_port;   /* port in network byte order */
        public in_addr sin_addr;   /* internet address */
    }
    @Structure.FieldOrder({ "s_addr"})
    public static class in_addr extends Structure {
        public int s_addr;   /* internet address */
    }

    @Structure.FieldOrder({ "ct_sock", "ct_closeit", "ct_wait", "ct_waitset", "ct_addr", "ct_error", "ct_mcall", "ct_mpos", "ct_xdrs" })
    public static class ct_data extends Structure {
        public int ct_sock;
        public int ct_closeit;
        public timeval ct_wait;
        public int ct_waitset;
        public sockaddr_in ct_addr;
        public int ct_error;
        public byte[] ct_mcall;
        public int ct_mpos;
        public int ct_xdrs;
    }

    @Structure.FieldOrder({ "no_client", "marshalled_client", "mcnt" })
    public static class authnone_private_s extends Structure {
        public int no_client;
        public byte[] marshalled_client;
        public int mcnt;
    }
    @Structure.FieldOrder({ "client", "pid", "uid" })
    public static class key_call_private extends Structure {
        public Pointer client;
        public int pid;
        public int uid;
    }
    @Structure.FieldOrder({ "sendsize", "recvsize" })
    public static class tcp_rendezvous extends Structure {
        public int sendsize;
        public int recvsize;
    }
    @Structure.FieldOrder({ "x_id", "xdrs", "verf_body" })
    public static class tcp_conn extends Structure {
        public NativeLong x_id;
        public int xdrs;
        public byte[] verf_body;
    }
    @Structure.FieldOrder({ "a", "b", "timeout_ms", "wait_for_seq", "garbage_packets" })
    public static class test_query extends Structure {
        public int a;
        public int b;
        public int timeout_ms;
        public int wait_for_seq;
        public int garbage_packets;
    }
    @Structure.FieldOrder({ "seq", "sum" })
    public static class test_response extends Structure {
        public int seq;
        public int sum;
    }
    @Structure.FieldOrder({ "_raw_buf", "server", "xdr_stream", "verf_body" })
    public static class svcraw_private_s extends Structure {
        public byte[] _raw_buf;
        public int server;
        public int xdr_stream;
        public byte[] verf_body;
    }
    @Structure.FieldOrder({ "client", "proc" })
    public static class rpc_arg extends Structure {
        public Pointer client;
        public NativeLong proc;
    }

//    @Structure.FieldOrder({ "au_origcred", "au_shcred", "au_shfaults", "au_marshed", "au_mpos" })
//    public static class audata extends Structure {
//        public opaque_auth au_origcred;
//        public opaque_auth au_shcred;
//        public NativeLong au_shfaults;
//        public byte[] au_marshed;
//        public int au_mpos;
//    }
    @Structure.FieldOrder({ "message_off", })
    public static class rpc_errtab extends Structure {
        public int message_off;
    }
    @Structure.FieldOrder({ "message_off", })
    public static class auth_errtab extends Structure {
        public int message_off;
    }
    @Structure.FieldOrder({ "res1", "res2", "ch" })
    public static class combine extends Structure {
        public int res1;
        public short res2;
        public int[] ch;
    }
    @Structure.FieldOrder({ "ucs", "val" })
    public static class map extends Structure {
        public short ucs;
        public byte[] val;
    }
    @Structure.FieldOrder({ "start", "end", "idx" })
    public static class gap extends Structure {
        public short start;
        public short end;
        public int idx;
    }
    @Structure.FieldOrder({ "res1", "res2" })
    public static class divide extends Structure {
        public short res1;
        public short res2;
    }
    @Structure.FieldOrder({ "start", "end", "idx" })
    public static class jisx0208_ucs_idx extends Structure {
        public short start;
        public short end;
        public short idx;
    }

    @Structure.FieldOrder({ "start", "end", "idx" })
    public static class jisx0212_idx extends Structure {
        public short start;
        public short end;
        public short idx;
    }
    @Structure.FieldOrder({ "sem_num", "sem_op", "sem_flg" })
    public static class sembuf extends Structure {
        public short sem_num;
        public short sem_op;
        public short sem_flg;
    }
    @Structure.FieldOrder({ "type", "type", "buf" })
    public static class msgbuf_t extends Structure {
        public int type;
        public int[] buf;
    }
    @Structure.FieldOrder({ "number", "message", "fname", "line", "symbol", "next" })
    public static class message_list extends Structure {
        public int number;
        public String message;
        public String fname;
        public int line;
        public String symbol;
        public message_list next;
    }
    @Structure.FieldOrder({ "spin_count", })
    public static class mutex_config extends Structure {
        public int spin_count;
    }
    @Structure.FieldOrder({ "name", "addr", "size", "align" })
    public static class tls_obj extends Structure {
        public String name;
        public int addr;
        public int size;
        public int align;
    }

    @Structure.FieldOrder({ "list", "futex_offset", "list_op_pending" })
    public static class robust_list_head extends Structure {
        public Pointer list;
        public NativeLong futex_offset;
        public Pointer list_op_pending;
    }
    @Structure.FieldOrder({ "priomax", "priomap" })
    public static class priority_protection_data extends Structure {
        public int priomax;
        public int[] priomap;
    }
    @Structure.FieldOrder({ "rwlock", "clockid", "fnname" })
    public static class thread_args extends Structure {
        public Pointer rwlock;
        public int clockid;
        public String fnname;
    }
    @Structure.FieldOrder({ "thread", "set", "size", "get", "result" })
    public static class affinity_access_task extends Structure {
        public int thread;
        public Pointer set;
        public int size;
        public boolean get;
        public int result;
    }

    @Structure.FieldOrder({ "uctx", "guard" })
    public static class tst_context_t extends Structure {
        public int uctx;
        public int[] guard;
    }
    @Structure.FieldOrder({ "the_sem", "rec" })
    public static class walk_closure extends Structure {
        public Pointer the_sem;
        public int rec;
    }
    @Structure.FieldOrder({ "low", "high" })
    public static class _condvar_lohi extends Structure {
        public int low;
        public int high;
    }

    @Structure.FieldOrder({ "starts", "threads" })
    public static class start_info extends Structure {
        public int starts;
        public int threads;
    }
    @Structure.FieldOrder({ "parent_mutex", "child_mutex" })
    public static class shared extends Structure {
        public int parent_mutex;
        public int child_mutex;
    }

//    @Structure.FieldOrder({ "_f", "vtable" })
//    public static class _IO_streambuf extends Structure {
//        public int _f;
//        public _IO_jump_t vtable;
//    }
//    @Structure.FieldOrder({ "_sbf", "_s" })
//    public static class _IO_strfile extends Structure {
//        public _IO_streambuf _sbf;
//        public _IOtr_fields _s;
//    }
    @Structure.FieldOrder({ "f", "overflow_buf" })
    public static class _IO_strnfile extends Structure {
        public int f;
        public byte[] overflow_buf;
    }
    @Structure.FieldOrder({ "f", "overflow_buf" })
    public static class _IO_wstrnfile extends Structure {
        public int f;
        public char[] overflow_buf;
    }
//    @Structure.FieldOrder({ "step", "step_data" })
//    public static class _IO_iconv_t extends Structure {
//        public __gconv_ step;
//        public __gconv_ step_data;
//    }
    @Structure.FieldOrder({ "_next", "_sbuf", "_pos" })
    public static class _IO_marker extends Structure {
        public _IO_marker _next;
        public Pointer _sbuf;
        public int _pos;
    }
    @Structure.FieldOrder({ "__cd_in", "__cd_out" })
    public static class _IO_codecvt extends Structure {
        public int __cd_in;
        public int __cd_out;
    }
//    @Structure.FieldOrder({ "_IO_read_ptr", "_IO_read_end", "_IO_read_base", "_IO_write_base", "_IO_write_ptr", "_IO_write_end", "_IO_buf_base", "_IO_buf_end", "_IO_save_base", "_IO_backup_base", "_IO_save_end", "_IO_state", "_IO_last_state", "_codecvt", "_shortbuf", "_wide_vtable" })
//    public static class _IO_wide_data extends Structure {
//        public String _IO_read_ptr;
//        public String _IO_read_end;
//        public String _IO_read_base;
//        public String _IO_write_base;
//        public String _IO_write_ptr;
//        public String _IO_write_end;
//        public String _IO_buf_base;
//        public String _IO_buf_end;
//        public String _IO_save_base;
//        public String _IO_backup_base;
//        public String _IO_save_end;
//        public int _IO_state;
//        public int _IO_last_state;
//        public _IO _codecvt;
//        public char[] _shortbuf;
//        public _IO_jump_t _wide_vtable;
//    }
    @Structure.FieldOrder({ "__pos", "__state" })
    public static class __fpos64_t extends Structure {
        public NativeLong __pos;
        public int __state;
    }
    @Structure.FieldOrder({ "__pos", "__state" })
    public static class __fpos_t extends Structure {
        public int __pos;
        public int __state;
    }
    @Structure.FieldOrder({ "read", "write", "seek", "close" })
    public static class cookie_io_functions_t extends Structure {
        public Pointer read;
        public Pointer write;
        public Pointer seek;
        public Pointer close;
    }
    @Structure.FieldOrder({ "buffer", "mybuffer", "append", "size", "pos", "maxpos" })
    public static class fmemopen_cookie_struct extends Structure {
        public Pointer buffer;
        public int mybuffer;
        public int append;
        public int size;
        public NativeLong pos;
        public int maxpos;
    }

    @Structure.FieldOrder({ "_sf", "*bufloc", "sizeloc" })
    public static class _IO_FILE_wmemstream extends Structure {
        public int _sf;
        public String[] bufloc;
        public Pointer sizeloc;
    }
    @Structure.FieldOrder({ "_sf", "*bufloc", "sizeloc" })
    public static class _IO_FILE_memstream extends Structure {
        public int _sf;
        public String[] bufloc;
        public Pointer sizeloc;
    }


    @Structure.FieldOrder({ "len", "val", "idxmax", "idxcnt", "backw", "backw_stop", "us", "rule", "idx", "save_idx", "back_us" })
    public static class coll_seq extends Structure {
        public int len;
        public int val;
        public int idxmax;
        public int idxcnt;
        public int backw;
        public int backw_stop;
        public Pointer us;
        public byte rule;
        public int idx;
        public int save_idx;
        public Pointer back_us;
    }
    @Structure.FieldOrder({ "nrules", "rulesets", "weights", "table", "extra", "indirect" })
    public static class locale_data_t extends Structure {
        public int nrules;
        public Pointer rulesets;
        public Pointer weights;
        public Pointer table;
        public Pointer extra;
        public Pointer indirect;
    }
    @Structure.FieldOrder({ "line", "expected" })
    public static class input extends Structure {
        public String line;
        public String expected;
    }
    @Structure.FieldOrder({ "n_name", "*n_aliases", "n_addrtype", "n_net" })
    public static class netent extends Structure {
        public String n_name;
        public String n_aliases;
        public int n_addrtype;
        public int n_net;
    }
    @Structure.FieldOrder({ "rlim_cur", "rlim_max" })
    public static class rlimit extends Structure {
        public int rlim_cur;
        public int rlim_max;
    }
    @Structure.FieldOrder({ "key", "uid", "gid", "cuid", "cgid", "mode", "seq"})
    public static class ipc_perm extends Structure {
        public int key;
        public short uid;   /* owner euid and egid */
        public short gid;
        public short cuid;  /* creator euid and egid */
        public short cgid;
        public short mode;  /* access modes see mode flags below */
        public short seq;   /* slot usage sequence number */
    }
    @Structure.FieldOrder({ "msg_perm", "msg_stime", "msg_rtime", "msg_ctime", "msg_qnum", "msg_qbytes", "msg_lspid", "msg_lrpid" })
    public static class msqid_ds extends Structure {
        public ipc_perm msg_perm;
        public int msg_stime;
        public int msg_rtime;
        public int msg_ctime;
        public int msg_qnum;
        public int msg_qbytes;
        public int msg_lspid;
        public int msg_lrpid;
    }
    @Structure.FieldOrder({ "sc_onstack", "sc_mask" })
    public static class sigcontext extends Structure {
        public int sc_onstack;
        public NativeLong sc_mask;
    }
    @Structure.FieldOrder({ "ll_time", "ll_line", "ll_host" })
    public static class lastlog extends Structure {
        public int ll_time;
        public byte[] ll_line;
        public byte[] ll_host;
    }
    @Structure.FieldOrder({ "f_bsize", "f_frsize", "f_blocks", "f_bfree", "f_bavail", "f_files", "f_ffree", "f_favail", "f_fsid", "f_flag", "f_namemax", "f_spare" })
    public static class statvfs extends Structure {
        public NativeLong f_bsize;
        public NativeLong f_frsize;
        public int f_blocks;
        public int f_bfree;
        public int f_bavail;
        public int f_files;
        public int f_ffree;
        public int f_favail;
        public int f_fsid;
        public NativeLong f_flag;
        public NativeLong f_namemax;
        public int[] f_spare;
    }
    @Structure.FieldOrder({ "f_type", "f_bsize", "f_blocks", "f_bfree", "f_bavail", "f_files", "f_ffree", "f_fsid", "f_namelen", "f_spare" })
    public static class statfs extends Structure {
        public int f_type;
        public int f_bsize;
        public int f_blocks;
        public int f_bfree;
        public int f_bavail;
        public int f_files;
        public int f_ffree;
        public int f_fsid;
        public int f_namelen;
        public int[] f_spare;
    }
    @Structure.FieldOrder({ "mq_flags", "mq_maxmsg", "mq_msgsize", "mq_curmsgs" })
    public static class mq_attr extends Structure {
        public NativeLong mq_flags;
        public NativeLong mq_maxmsg;
        public NativeLong mq_msgsize;
        public NativeLong mq_curmsgs;
    }
    @Structure.FieldOrder({ "shm_perm", "shm_segsz", "shm_atime", "shm_dtime", "shm_ctime", "shm_cpid", "shm_lpid", "shm_nattch" })
    public static class shmid_ds extends Structure {
        public ipc_perm shm_perm;
        public int shm_segsz;
        public int shm_atime;
        public int shm_dtime;
        public int shm_ctime;
        public int shm_cpid;
        public int shm_lpid;
        public int shm_nattch;
    }
    @Structure.FieldOrder({ "sa_handler", })
    public static class sigaction extends Structure {
        public int sa_handler;
    }
    @Structure.FieldOrder({ "t_intrc", "t_quitc", "t_startc", "t_stopc", "t_eofc", "t_brkc" })
    public static class tchars extends Structure {
        public byte t_intrc;
        public byte t_quitc;
        public byte t_startc;
        public byte t_stopc;
        public byte t_eofc;
        public byte t_brkc;
    }
    @Structure.FieldOrder({ "sa_", "sa_data" })
    public static class sockaddr extends Structure {
        public int sa_;
        public int sa_data;
    }
    @Structure.FieldOrder({ "sem_perm", "sem_otime", "sem_ctime", "sem_nsems" })
    public static class semid_ds extends Structure {
        public ipc_perm sem_perm;
        public int sem_otime;
        public int sem_ctime;
        public short sem_nsems;
    }
    @Structure.FieldOrder({ "sigev_value", "sigev_signo", "sigev_notify", "__sigval_t", "sigev_notify_attributes" })
    public static class sigevent_t extends Structure {
        public int sigev_value;
        public int sigev_signo;
        public int sigev_notify;
        public Pointer __sigval_t;
        public Pointer sigev_notify_attributes;
    }
    @Structure.FieldOrder({ "sched_priority" })
    public static class sched_param extends Structure {
        public int sched_priority;
    }
    @Structure.FieldOrder({ "si_signo", "si_errno", "si_code", "si_pid", "si_uid", "si_addr", "si_status", "si_band", "si_value" })
    public static class siginfo_t extends Structure {
        public int si_signo;
        public int si_errno;
        public int si_code;
        public int si_pid;
        public int si_uid;
        public Pointer si_addr;
        public int si_status;
        public NativeLong si_band;
        public int si_value;
    }
    @Structure.FieldOrder({ "ss_sp", "ss_size", "ss_flags" })
    public static class stack_t extends Structure {
        public Pointer ss_sp;
        public int ss_size;
        public int ss_flags;
    }
    @Structure.FieldOrder({ "string_offset", "module_idx" })
    public static class hash_entry extends Structure {
        public short string_offset;
        public short module_idx;
    }
    @Structure.FieldOrder({ "canonname_offset", "fromdir_offset", "fromname_offset", "todir_offset", "toname_offset", "extra_offset" })
    public static class module_entry extends Structure {
        public short canonname_offset;
        public short fromdir_offset;
        public short fromname_offset;
        public short todir_offset;
        public short toname_offset;
        public short extra_offset;
    }
    @Structure.FieldOrder({ "fromname", "toname" })
    public static class gconv_alias extends Structure {
        public String fromname;
        public String toname;
    }
    @Structure.FieldOrder({ "name", "counter", "handle", "fct", "init_fct", "end_fct" })
    public static class __gconv_loaded_object extends Structure {
        public String name;
        public int counter;
        public Pointer handle;
        public int fct;
        public int init_fct;
        public int end_fct;
    }
    @Structure.FieldOrder({ "from_string", "to_string", "cost_hi", "cost_lo", "module_name", "left", "same", "right" })
    public static class gconv_module extends Structure {
        public String from_string;
        public String to_string;
        public int cost_hi;
        public int cost_lo;
        public String module_name;
        public gconv_module left;
        public gconv_module same;
        public gconv_module right;
    }
    @Structure.FieldOrder({ "fromcode", "tocode", "translit", "ignore" })
    public static class gconv_spec extends Structure {
        public String fromcode;
        public String tocode;
        public boolean translit;
        public boolean ignore;
    }
    @Structure.FieldOrder({ "__shlib_handle", "__modname", "__counter", "__from_name", "__to_name", "__fct", "__btowc_fct", "__init_fct", "__end_fct", "__min_needed_from", "__max_needed_from", "__min_needed_to", "__max_needed_to", "__stateful", "__data" })
    public static class __gconv_step extends Structure {
        public __gconv_loaded_object __shlib_handle;
        public String __modname;
        public int __counter;
        public String __from_name;
        public String __to_name;
        public int __fct;
        public int __btowc_fct;
        public int __init_fct;
        public int __end_fct;
        public int __min_needed_from;
        public int __max_needed_from;
        public int __min_needed_to;
        public int __max_needed_to;
        public int __stateful;
        public Pointer __data;
    }
    @Structure.FieldOrder({ "__outbuf", "__outbufend", "__flags", "__invocation_counter", "__internal_use", "__statep", "__state" })
    public static class __gconv_step_data extends Structure {
        public Pointer __outbuf;
        public Pointer __outbufend;
        public int __flags;
        public int __invocation_counter;
        public int __internal_use;
        public Pointer __statep;
        public int __state;
    }
    @Structure.FieldOrder({ "tocode", "fromcode" })
    public static class convcode extends Structure {
        public String tocode;
        public String fromcode;
    }
//    @Structure.FieldOrder({ "__allocated", "__used", "__actions", "__pad" })
//    public static class posix_spawn_file_actions_t extends Structure {
//        public int __allocated;
//        public int __used;
//        public __spawn_action __actions;
//        public int[] __pad;
//    }
    @Structure.FieldOrder({ "__bits", })
    public static class cpu_set_t extends Structure {
        public int[] __bits;
    }

    @Structure.FieldOrder({ "sysname", "nodename", "release", "version", "machine", "domainname" })
    public static class utsname extends Structure {
        public byte[] sysname;
        public byte[] nodename;
        public byte[] release;
        public byte[] version;
        public byte[] machine;
        public byte[] domainname;
    }
//    @Structure.FieldOrder({ "level", "idx", "d", "room_for_dirent" })
//    public static class my_DIR extends Structure {
//        public int level;
//        public int idx;
//        public irent d;
//        public byte[] room_for_dirent;
//    }
    @Structure.FieldOrder({ "name", "type" })
    public static class readdir_result extends Structure {
        public String name;
        public int type;
    }
    @Structure.FieldOrder({ "pattern", "string", "no_leading_period" })
    public static class STRUCT extends Structure {
        public Pointer pattern;
        public Pointer string;
        public int no_leading_period;
    }

    @Structure.FieldOrder({ "sun_", "sun_path" })
    public static class sockaddr_un extends Structure {
        public int sun_;
        public byte[] sun_path;
    }
//    @Structure.FieldOrder({ "msg_hdr", "msg_len" })
//    public static class mmsghdr extends Structure {
//        public msghdr msg_hdr;
//        public int msg_len;
//    }
    @Structure.FieldOrder({ "socket", "address" })
    public static class client extends Structure {
        public int socket;
        public sockaddr_in address;
    }
    @Structure.FieldOrder({ "hashval", "name_offset", "locrec_offset" })
    public static class namehashent extends Structure {
        public int hashval;
        public int name_offset;
        public int locrec_offset;
    }
    @Structure.FieldOrder({ "sum", "file_offset" })
    public static class sumhashent extends Structure {
        public byte[] sum;
        public int file_offset;
    }
    @Structure.FieldOrder({ "fname", "fd", "addr", "mmaped", "reserved", "mmap_base", "mmap_len" })
    public static class locarhandle extends Structure {
        public String fname;
        public int fd;
        public Pointer addr;
        public int mmaped;
        public int reserved;
        public Pointer mmap_base;
        public int mmap_len;
    }
//    @Structure.FieldOrder({ "__locales", "__ctype_b", "__ctype_tolower", "__ctype_toupper", "__names" })
//    public static class __locale_struct extends Structure {
//        public __locale_data[] __locales;
//        public Pointer __ctype_b;
//        public Pointer __ctype_tolower;
//        public Pointer __ctype_toupper;
//        public Pointer __names;
//    }

//    @Structure.FieldOrder({ "n_elements", "next_element", "offsets", "data", "structure_stage" })
//    public static class locale_file extends Structure {
//        public int n_elements;
//        public int next_element;
//        public Pointer offsets;
//        public obstack data;
//        public int structure_stage;
//    }
//    @Structure.FieldOrder({ "name", "mem_pool", "char_table", "reverse_table", "seq_table" })
//    public static class repertoire_t extends Structure {
//        public String name;
//        public obstack mem_pool;
//        public int char_table;
//        public int reverse_table;
//        public int seq_table;
//    }
    @Structure.FieldOrder({ "cur_locale", })
    public static class locale_state extends Structure {
        public String cur_locale;
    }
    @Structure.FieldOrder({ "from", "to", "width" })
    public static class width_rule extends Structure {
        public charseq from;
        public charseq to;
        public int width;
    }
//    @Structure.FieldOrder({ "code_set_name", "repertoiremap", "mb_cur_min", "mb_cur_max", "width_rules", "nwidth_rules", "nwidth_rules_max", "width_default", "mem_pool", "char_table", "byte_table", "ucs4_table" })
//    public static class charmap_t extends Structure {
//        public String code_set_name;
//        public String repertoiremap;
//        public int mb_cur_min;
//        public int mb_cur_max;
//        public width_rule width_rules;
//        public int nwidth_rules;
//        public int nwidth_rules_max;
//        public int width_default;
//        public obstack mem_pool;
//        public int char_table;
//        public int byte_table;
//        public int ucs4_table;
//    }
    @Structure.FieldOrder({ "name", "ucs4", "nbytes", "bytes" })
    public static class charseq extends Structure {
        public String name;
        public int ucs4;
        public int nbytes;
        public byte[] bytes;
    }
    @Structure.FieldOrder({ "p", "q", "level1_alloc", "level1_size", "level1", "level2_alloc", "level2_size", "level2", "level3_alloc", "level3_size", "level3", "result_size" })
    public static class TABLE extends Structure {
        public int p;
        public int q;
        public int level1_alloc;
        public int level1_size;
        public Pointer level1;
        public int level2_alloc;
        public int level2_size;
        public Pointer level2;
        public int level3_alloc;
        public int level3_size;
        public Pointer level3;
        public int result_size;
    }
    @Structure.FieldOrder({ "name", "symname_or_ident", "locale" })
    public static class keyword_t extends Structure {
        public String name;
        public int symname_or_ident;
        public int locale;
    }
    @Structure.FieldOrder({ "argv", "exp", "complocaledir" })
    public static class test_closure extends Structure {
        public Pointer argv;
        public String exp;
        public String complocaledir;
    }
    @Structure.FieldOrder({ "int_curr_symbol", "currency_symbol", "mon_decimal_point", "mon_thousands_sep", "mon_decimal_point_wc", "mon_thousands_sep_wc", "mon_grouping", "mon_grouping_len", "positive_sign", "negative_sign", "int_frac_digits", "frac_digits", "p_cs_precedes", "p_sep_by_space", "n_cs_precedes", "n_sep_by_space", "p_sign_posn", "n_sign_posn", "int_p_cs_precedes", "int_p_sep_by_space", "int_n_cs_precedes", "int_n_sep_by_space", "int_p_sign_posn", "int_n_sign_posn", "duo_int_curr_symbol", "duo_currency_symbol", "duo_int_frac_digits", "duo_frac_digits", "duo_p_cs_precedes", "duo_p_sep_by_space", "duo_n_cs_precedes", "duo_n_sep_by_space", "duo_p_sign_posn", "duo_n_sign_posn", "duo_int_p_cs_precedes", "duo_int_p_sep_by_space", "duo_int_n_cs_precedes", "duo_int_n_sep_by_space", "duo_int_p_sign_posn", "duo_int_n_sign_posn", "uno_valid_from", "uno_valid_to", "duo_valid_from", "duo_valid_to", "conversion_rate", "crncystr" })
    public static class locale_monetary_t extends Structure {
        public String int_curr_symbol;
        public String currency_symbol;
        public String mon_decimal_point;
        public String mon_thousands_sep;
        public int mon_decimal_point_wc;
        public int mon_thousands_sep_wc;
        public String mon_grouping;
        public int mon_grouping_len;
        public String positive_sign;
        public String negative_sign;
        public byte int_frac_digits;
        public byte frac_digits;
        public byte p_cs_precedes;
        public byte p_sep_by_space;
        public byte n_cs_precedes;
        public byte n_sep_by_space;
        public byte p_sign_posn;
        public byte n_sign_posn;
        public byte int_p_cs_precedes;
        public byte int_p_sep_by_space;
        public byte int_n_cs_precedes;
        public byte int_n_sep_by_space;
        public byte int_p_sign_posn;
        public byte int_n_sign_posn;
        public String duo_int_curr_symbol;
        public String duo_currency_symbol;
        public byte duo_int_frac_digits;
        public byte duo_frac_digits;
        public byte duo_p_cs_precedes;
        public byte duo_p_sep_by_space;
        public byte duo_n_cs_precedes;
        public byte duo_n_sep_by_space;
        public byte duo_p_sign_posn;
        public byte duo_n_sign_posn;
        public byte duo_int_p_cs_precedes;
        public byte duo_int_p_sep_by_space;
        public byte duo_int_n_cs_precedes;
        public byte duo_int_n_sep_by_space;
        public byte duo_int_p_sign_posn;
        public byte duo_int_n_sign_posn;
        public int uno_valid_from;
        public int uno_valid_to;
        public int duo_valid_from;
        public int duo_valid_to;
        public int[] conversion_rate;
        public String crncystr;
    }
    @Structure.FieldOrder({ "postal_fmt", "country_name", "country_post", "country_ab2", "country_ab3", "country_num", "country_car", "country_isbn", "lang_name", "lang_ab", "lang_term", "lang_lib" })
    public static class locale_address_t extends Structure {
        public String postal_fmt;
        public String country_name;
        public String country_post;
        public String country_ab2;
        public String country_ab3;
        public int country_num;
        public String country_car;
        public String country_isbn;
        public String lang_name;
        public String lang_ab;
        public String lang_term;
        public String lang_lib;
    }
    @Structure.FieldOrder({ "name_fmt", "name_gen", "name_mr", "name_mrs", "name_miss", "name_ms" })
    public static class locale_name_t extends Structure {
        public String name_fmt;
        public String name_gen;
        public String name_mr;
        public String name_mrs;
        public String name_miss;
        public String name_ms;
    }
    @Structure.FieldOrder({ "height", "width" })
    public static class locale_paper_t extends Structure {
        public int height;
        public int width;
    }
    @Structure.FieldOrder({ "direction", "offset", "start_date", "stop_date", "name", "format", "wname", "wformat" })
    public static class era_data extends Structure {
        public int direction;
        public int offset;
        public int[] start_date;
        public int[] stop_date;
        public String name;
        public String format;
        public Pointer wname;
        public Pointer wformat;
    }
    @Structure.FieldOrder({ "decimal_point", "thousands_sep", "grouping", "grouping_len", "decimal_point_wc", "thousands_sep_wc" })
    public static class locale_numeric_t extends Structure {
        public String decimal_point;
        public String thousands_sep;
        public String grouping;
        public int grouping_len;
        public int decimal_point_wc;
        public int thousands_sep_wc;
    }
    @Structure.FieldOrder({ "tel_int_fmt", "tel_dom_fmt", "int_select", "int_prefix" })
    public static class locale_telephone_t extends Structure {
        public String tel_int_fmt;
        public String tel_dom_fmt;
        public String int_select;
        public String int_prefix;
    }
    @Structure.FieldOrder({ "name", "locrec_offset" })
    public static class nameent extends Structure {
        public String name;
        public int locrec_offset;
    }
    @Structure.FieldOrder({ "yesexpr", "noexpr", "yesstr", "nostr" })
    public static class locale_messages_t extends Structure {
        public String yesexpr;
        public String noexpr;
        public String yesstr;
        public String nostr;
    }
    @Structure.FieldOrder({ "dir", "directory", "directory_len", "pathname", "pathname_size" })
    public static class charmap_dir extends Structure {
        public Pointer dir;
        public String directory;
        public int directory_len;
        public String pathname;
        public int pathname_size;
    }
    @Structure.FieldOrder({ "title", "source", "address", "contact", "email", "tel", "fax", "language", "territory", "audience", "application", "abbreviation", "revision", "date", "category" })
    public static class locale_identification_t extends Structure {
        public String title;
        public String source;
        public String address;
        public String contact;
        public String email;
        public String tel;
        public String fax;
        public String language;
        public String territory;
        public String audience;
        public String application;
        public String abbreviation;
        public String revision;
        public String date;
        public Pointer category;
    }
//    @Structure.FieldOrder({ "cnt", "locrec" })
//    public static class oldlocrecent extends Structure {
//        public int cnt;
//        public ent locrec;
//    }

    @Structure.FieldOrder({ "sum", "file_offset", "nlink" })
    public static class dataent extends Structure {
        public Pointer sum;
        public int file_offset;
        public int nlink;
    }
    @Structure.FieldOrder({ "base", "level" })
    public static class FTW extends Structure {
        public int base;
        public int level;
    }
    @Structure.FieldOrder({ "tv_sec", "tv_nsec", "__statx_timestamp_pad1" })
    public static class statx_timestamp extends Structure {
        public long tv_sec;
        public int tv_nsec;
        public int[] __statx_timestamp_pad1;
    }
    @Structure.FieldOrder({ "stx_mask", "stx_blksize", "stx_attributes", "stx_nlink", "stx_uid", "stx_gid", "stx_mode", "__statx_pad1", "stx_ino", "stx_size", "stx_blocks", "stx_attributes_mask", "stx_atime", "stx_btime", "stx_ctime", "stx_mtime", "stx_rdev_major", "stx_rdev_minor", "stx_dev_major", "stx_dev_minor", "__statx_pad2" })
    public static class statx extends Structure {
        public int stx_mask;
        public int stx_blksize;
        public long stx_attributes;
        public int stx_nlink;
        public int stx_uid;
        public int stx_gid;
        public short stx_mode;
        public short[] __statx_pad1;
        public long stx_ino;
        public long stx_size;
        public long stx_blocks;
        public long stx_attributes_mask;
        public statx_timestamp stx_atime;
        public statx_timestamp stx_btime;
        public statx_timestamp stx_ctime;
        public statx_timestamp stx_mtime;
        public int stx_rdev_major;
        public int stx_rdev_minor;
        public int stx_dev_major;
        public int stx_dev_minor;
        public long[] __statx_pad2;
    }
    @Structure.FieldOrder({ "fd", "events", "revents" })
    public static class pollfd extends Structure {
        public int fd;
        public short events;
        public short revents;
    }
    @Structure.FieldOrder({ "value", "internal" })
    public static class speed_struct extends Structure {
        public int value;
        public int internal;
    }

    @Structure.FieldOrder({ "count", "next", "*ptrs" })
    public static class ptrs_to_free extends Structure {
        public int count;
        public ptrs_to_free next;
        public String[] ptrs;
    }
    @Structure.FieldOrder({ "current", "end", "scratch" })
    public static class char_buffer extends Structure {
        public Pointer current;
        public Pointer end;
        public Buffer scratch;
    }
//    @Structure.FieldOrder({ "_f", "_put_stream", "lock" })
//    public static class helper_file extends Structure {
//        public _IO_FILE_plus _f;
//        public Pointer _put_stream;
//        public int lock;
//    }
    @Structure.FieldOrder({ "d", "fmt", "ru" })
    public static class dec_test extends Structure {
        public double d;
        public String fmt;
        public Pointer ru;
    }
    @Structure.FieldOrder({ "d", "fmt", "ru" })
    public static class hex_test extends Structure {
        public double d;
        public String fmt;
        public Pointer ru;
    }
    @Structure.FieldOrder({ "line", "value", "result", "format_string" })
    public static class sprint_int_type extends Structure {
        public int line;
        public long value;
        public String result;
        public String format_string;
    }

    @Structure.FieldOrder({ "value", "fmt", "expect" })
    public static class testcase extends Structure {
        public double value;
        public Pointer fmt;
        public Pointer expect;
    }
    @Structure.FieldOrder({ "i", "d" })
    public static class two_argument extends Structure {
        public NativeLong i;
        public double d;
    }
    @Structure.FieldOrder({ "line", "value", "result", "format_string" })
    public static class sprint_double_type extends Structure {
        public int line;
        public double value;
        public String result;
        public String format_string;
    }
    @Structure.FieldOrder({ "fp", "indent_level", "first_element" })
    public static class json_ctx extends Structure {
        public Pointer fp;
        public int indent_level;
        public boolean first_element;
    }
    @Structure.FieldOrder({ "iters", "size", "n", "elapsed" })
    public static class malloc_args extends Structure {
        public int iters;
        public int size;
        public int n;
        public long elapsed;
    }

    @Structure.FieldOrder({ "size", "*words" })
    public static class word_list extends Structure {
        public int size;
        public String[] words;
    }
    @Structure.FieldOrder({ "name", "fn" })
    public static class impl_t extends Structure {
        public String name;
        public int fn;
    }
    @Structure.FieldOrder({ "fd", "next" })
    public static class fdlist extends Structure {
        public int fd;
        public fdlist next;
    }
//    @Structure.FieldOrder({ "head", "resp", "strdata" })
//    public static class dataset extends Structure {
//        public data head;
//        public int resp;
//        public byte[] strdata;
//    }
    @Structure.FieldOrder({ "enabled", "check_file", "shared", "persistent", "module", "postimeout", "negtimeout", "nentries", "maxnentries", "maxnsearched", "datasize", "dataused", "poshit", "neghit", "posmiss", "negmiss", "rdlockdelayed", "wrlockdelayed", "addfailed" })
    public static class dbstat extends Structure {
        public int enabled;
        public int check_file;
        public int shared;
        public int persistent;
        public int module;
        public NativeLong postimeout;
        public NativeLong negtimeout;
        public int nentries;
        public int maxnentries;
        public int maxnsearched;
        public int datasize;
        public int dataused;
        public long poshit;
        public long neghit;
        public long posmiss;
        public long negmiss;
        public long rdlockdelayed;
        public long wrlockdelayed;
        public long addfailed;
    }
//    @Structure.FieldOrder({ "version", "debug_level", "runtime", "client_queued", "nthreads", "max_nthreads", "paranoia", "restart_interval", "reload_count", "ndbs", "dbs" })
//    public static class statdata extends Structure {
//        public int version;
//        public int debug_level;
//        public int runtime;
//        public NativeLong client_queued;
//        public int nthreads;
//        public int max_nthreads;
//        public int paranoia;
//        public int restart_interval;
//        public int reload_count;
//        public int ndbs;
//        public tat[] dbs;
//    }

    @Structure.FieldOrder({ "type_code", "min", "max" })
    public static class tunable_type_t extends Structure {
        public int type_code;
        public long min;
        public long max;
    }
    @Structure.FieldOrder({ "a", "b", "c" })
    public static class A extends Structure {
        public int a;
        public int b;
        public long c;
    }
    @Structure.FieldOrder({ "value", })
    public static class unique_symbol extends Structure {
        public int value;
    }
    @Structure.FieldOrder({ "i", "j" })
    public static class S extends Structure {
        public int i;
        public int j;
    }

    @Structure.FieldOrder({ "soname", "flag" })
    public static class known_names extends Structure {
        public String soname;
        public int flag;
    }
//    @Structure.FieldOrder({ "exception", "errcode", "env" })
//    public static class catch extends Structure {
//        public dl_ exception;
//        public Pointer errcode;
//        public int env;
//    }
    @Structure.FieldOrder({ "next", })
    public static class strct extends Structure {
        public Pointer next;
    }
    @Structure.FieldOrder({ "lib", "path", "flags", "osversion", "hwcap", "bits_hwcap", "next" })
    public static class cache_entry extends Structure {
        public String lib;
        public String path;
        public int flags;
        public int osversion;
        public long hwcap;
        public int bits_hwcap;
        public cache_entry next;
    }
//    @Structure.FieldOrder({ "map", "trace_mode", "open_mode", "strtab", "name", "aux" })
//    public static class openaux_args extends Structure {
//        public link_ map;
//        public int trace_mode;
//        public int open_mode;
//        public String strtab;
//        public String name;
//        public link_map aux;
//    }
    @Structure.FieldOrder({ "path", "flag", "ino", "dev", "from_file", "from_line", "next" })
    public static class dir_entry extends Structure {
        public String path;
        public int flag;
        public int ino;
        public int dev;
        public String from_file;
        public int from_line;
        public dir_entry next;
    }
    @Structure.FieldOrder({ "next", })
    public static class testdat extends Structure {
        public Pointer next;
    }
    @Structure.FieldOrder({ "nsid", "file", "mode", "neW", "caller" })
    public static class dlmopen_args extends Structure {
        public NativeLong nsid;
        public String file;
        public int mode;
        public Pointer neW;
        public Pointer caller;
    }
    @Structure.FieldOrder({ "file", "mode", "neW", "caller" })
    public static class dlopen_args extends Structure {
        public String file;
        public int mode;
        public Pointer neW;
        public Pointer caller;
    }

    @Structure.FieldOrder({ "handle", "name", "who", "sym" })
    public static class dlsym_args extends Structure {
        public Pointer handle;
        public String name;
        public Pointer who;
        public Pointer sym;
    }
    @Structure.FieldOrder({ "handle", "name", "version", "who", "sym" })
    public static class dlvsym_args extends Structure {
        public Pointer handle;
        public String name;
        public String version;
        public Pointer who;
        public Pointer sym;
    }

    @Structure.FieldOrder({ "args", "silent", "verbose", "output_file" })
    public static class arguments extends Structure {
        public String[] args;
        public int silent;
        public int verbose;
        public String output_file;
    }
    @Structure.FieldOrder({ "ar_name", "ar_date", "ar_gid", "ar_mode", "ar_size", "ar_fmag" })
    public static class ar_hdr extends Structure {
        public byte[] ar_name;
        public byte[] ar_date;
        public int[] ar_gid;
        public byte[] ar_mode;
        public byte[] ar_size;
        public byte[] ar_fmag;
    }

    @Structure.FieldOrder({ "iov_base", "iov_len" })
    public static class iovec extends Structure {
        public Pointer iov_base;
        public int iov_len;
    }

    @Structure.FieldOrder({ "fds_bits", })
    public static class fd_set extends Structure {
        public NativeLong[] fds_bits;
    }

    @Structure.FieldOrder({ "f_tfree", "f_tinode", "f_fname", "f_fpack" })
    public static class ustat extends Structure {
        public int f_tfree;
        public int f_tinode;
        public byte[] f_fname;
        public byte[] f_fpack;
    }
    @Structure.FieldOrder({ "buf", "oldaction" })
    public static class cleanup_arg extends Structure {
        public Pointer buf;
        public sigaction oldaction;
    }
    @Structure.FieldOrder({ "callback", "expected" })
    public static class tests extends Structure {
        public Pointer callback;
        public String expected;
    }
    @Structure.FieldOrder({ "A", "B", "C", "D", "total", "buflen" })
    public static class md5_ctx extends Structure {
        public NativeLong A;
        public NativeLong B;
        public NativeLong C;
        public NativeLong D;
        public NativeLong[] total;
        public NativeLong buflen;
    }
    @Structure.FieldOrder({ "buffer", "length", "region_start", "region_size" })
    public static class support_next_to_fault extends Structure {
        public String buffer;
        public int length;
        public Pointer region_start;
        public int region_size;
    }

    @Structure.FieldOrder({ "stdout_pipe", "stderr_pipe", "pid" })
    public static class support_subprocess extends Structure {
        public int[] stdout_pipe;
        public int[] stderr_pipe;
        public int pid;
    }
    @Structure.FieldOrder({ "resolv_conf", "hosts", "host_conf", "aliases" })
    public static class support_chroot_configuration extends Structure {
        public String resolv_conf;
        public String hosts;
        public String host_conf;
        public String aliases;
    }
    @Structure.FieldOrder({ "path_chroot", "path_resolv_conf", "path_hosts", "path_host_conf", "path_aliases" })
    public static class support_chroot extends Structure {
        public String path_chroot;
        public String path_resolv_conf;
        public String path_hosts;
        public String path_host_conf;
        public String path_aliases;
    }
    @Structure.FieldOrder({ "out", "buffer", "length" })
    public static class xmemstream extends Structure {
        public Pointer out;
        public String buffer;
        public int length;
    }
    @Structure.FieldOrder({ "out", "err", "status" })
    public static class support_capture_subprocess extends Structure {
        public xmemstream out;
        public xmemstream err;
        public int status;
    }
    @Structure.FieldOrder({ "active", "extended_rcode", "version", "flags", "payload_size" })
    public static class resolv_edns_info extends Structure {
        public boolean active;
        public byte extended_rcode;
        public byte version;
        public short flags;
        public short payload_size;
    }
//    @Structure.FieldOrder({ "query_buffer", "query_length", "server_index", "tcp", "edns" })
//    public static class resolv_response_context extends Structure {
//        public Pointer query_buffer;
//        public int query_length;
//        public int server_index;
//        public boolean tcp;
//        public resolv__info edns;
//    }
    @Structure.FieldOrder({ "disable_tcp", "disable_udp" })
    public static class resolv_redirect_server_config extends Structure {
        public boolean disable_tcp;
        public boolean disable_udp;
    }
    @Structure.FieldOrder({ "i2", "i3", "u2", "u3", "i31", "u31", "i63", "u63" })
    public static class bitfield extends Structure {
        public int i2;
        public int i3;
        public int u2;
        public int u3;
        public int i31;
        public int u31;
        public long i63;
        public long u63;
    }
    @Structure.FieldOrder({ "alloc_base", "alloc_size", "alt_stack", "old_stack" })
    public static class sigstack_desc extends Structure {
        public Pointer alloc_base;
        public int alloc_size;
        public int alt_stack;
        public int old_stack;
    }
    @Structure.FieldOrder({ "buf", "len", "size" })
    public static class path_buf extends Structure {
        public String buf;
        public int len;
        public int size;
    }

    @Structure.FieldOrder({ "expected", "observed", "upper_bound", "lower_bound", "result" })
    public static class timespec_test_case extends Structure {
        public timespec expected;
        public timespec observed;
        public double upper_bound;
        public double lower_bound;
        public int result;
    }
    @Structure.FieldOrder({ "out", "err", "signal", "status" })
    public static class test extends Structure {
        public String out;
        public String err;
        public int signal;
        public int status;
    }
    @Structure.FieldOrder({ "counter", "failed" })
    public static class test_failures extends Structure {
        public int counter;
        public int failed;
    }
    @Structure.FieldOrder({ "data", "size" })
    public static class in_buffer extends Structure {
        public Pointer data;
        public int size;
    }
    @Structure.FieldOrder({ "alias_name", "alias_members_len", "alias_members", "alias_local" })
    public static class aliasent extends Structure {
        public String alias_name;
        public int alias_members_len;
        public String[] alias_members;
        public int alias_local;
    }
    @Structure.FieldOrder({ "next", "name" })
    public static class name_list extends Structure {
        public name_list next;
        public byte[] name;
    }
    @Structure.FieldOrder({ "igmp_type", "igmp_code", "igmp_cksum", "igmp_group" })
    public static class igmp extends Structure {
        public byte igmp_type;
        public byte igmp_code;
        public short igmp_cksum;
        public in_addr igmp_group;
    }
    @Structure.FieldOrder({ "icmp6_filt", })
    public static class icmp6_filter extends Structure {
        public int[] icmp6_filt;
    }
    @Structure.FieldOrder({ "rip_dst", "int\trip_metric" })
    public static class netinfo extends Structure {
        public sockaddr rip_dst;
        public int	rip_metric;
    }

    @Structure.FieldOrder({ "name", "b3", "q" })
    public static class thr_data extends Structure {
        public String name;
        public Pointer b3;
        public int q;
    }

    @Structure.FieldOrder({ "dbid", "extra_string", "next", "entries", "nentries", "nhashentries", "hashtable", "keystrlen", "keyidxtab", "keystrtab" })
    public static class database extends Structure {
        public byte dbid;
        public boolean extra_string;
        public database next;
        public Pointer entries;
        public int nentries;
        public int nhashentries;
        public Pointer hashtable;
        public int keystrlen;
        public Pointer keyidxtab;
        public String keystrtab;
    }
    @Structure.FieldOrder({ "validx", "hashval", "str" })
    public static class dbentry extends Structure {
        public int validx;
        public int hashval;
        public byte[] str;
    }
    @Structure.FieldOrder({ "idx", "extra_string", "str" })
    public static class valstrentry extends Structure {
        public int idx;
        public boolean extra_string;
        public byte[] str;
    }
    @Structure.FieldOrder({ "host_addr", "h_addr_ptrs" })
    public static class hostent_data extends Structure {
        public byte[] host_addr;
        public String[] h_addr_ptrs;
    }
    @Structure.FieldOrder({ "length", "offset" })
    public static class string_desc extends Structure {
        public NativeLong length;
        public NativeLong offset;
    }
    @Structure.FieldOrder({ "length", "offset" })
    public static class sysdep_segment extends Structure {
        public NativeLong length;
        public NativeLong offset;
    }
    @Structure.FieldOrder({ "segsize", "sysdepref" })
    public static class segment_pair extends Structure {
        public NativeLong segsize;
        public NativeLong sysdepref;
    }
    @Structure.FieldOrder({ "offset", "segments" })
    public static class sysdep_string extends Structure {
        public NativeLong offset;
        public segment_pair[] segments;
    }
    @Structure.FieldOrder({ "filename", "decided", "data", "next", "successor" })
    public static class loaded_l10nfile extends Structure {
        public String filename;
        public int decided;
        public Pointer data;
        public loaded_l10nfile next;
        public loaded_l10nfile[] successor;
    }
    @Structure.FieldOrder({ "alias", "value" })
    public static class alias_map extends Structure {
        public String alias;
        public String value;
    }
    @Structure.FieldOrder({ "quot", "rem" })
    public static class imaxdiv_t extends Structure {
        public NativeLong quot;
        public NativeLong rem;
    }

    @Structure.FieldOrder({ "chars_per_limb", "chars_per_bit_exactly", "big_base", "big_base_inverted" })
    public static class bases extends Structure {
        public int chars_per_limb;
        public float chars_per_bit_exactly;
        public NativeLong big_base;
        public NativeLong big_base_inverted;
    }
    @Structure.FieldOrder({ "quot", "rem" })
    public static class div_t extends Structure {
        public int quot;
        public int rem;
    }
    @Structure.FieldOrder({ "quot", "rem" })
    public static class ldiv_t extends Structure {
        public NativeLong quot;
        public NativeLong rem;
    }
    @Structure.FieldOrder({ "fptr", "rptr", "state", "rand_type", "rand_deg", "rand_sep", "end_ptr" })
    public static class random_data extends Structure {
        public Pointer fptr;
        public Pointer rptr;
        public Pointer state;
        public int rand_type;
        public int rand_deg;
        public int rand_sep;
        public Pointer end_ptr;
    }
    @Structure.FieldOrder({ "__x", "__old_x", "__c", "__init", "__a" })
    public static class drand48_data extends Structure {
        public short[] __x;
        public short[] __old_x;
        public short __c;
        public short __init;
        public long __a;
    }
    @Structure.FieldOrder({ "val", "str" })
    public static class item extends Structure {
        public int val;
        public String str;
    }
    @Structure.FieldOrder({ "str", "expect", "base", "left", "err" })
    public static class ltest extends Structure {
        public String str;
        public long expect;
        public int base;
        public byte left;
        public int err;
    }

    @Structure.FieldOrder({ "positive", "negative" })
    public static class testcase_pair extends Structure {
        public testcase positive;
        public testcase negative;
    }

    @Structure.FieldOrder({ "severity", "string", "next" })
    public static class severity_info extends Structure {
        public int severity;
        public String string;
        public severity_info next;
    }
    @Structure.FieldOrder({ "lo", "hi" })
    public static class stack_node extends Structure {
        public String lo;
        public String hi;
    }
    @Structure.FieldOrder({ "command", "exit_status", "term_sig", "path" })
    public static class args extends Structure {
        public String command;
        public int exit_status;
        public int term_sig;
        public String path;
    }

    @Structure.FieldOrder({ "cnt", "size" })
    public static class trace_arg extends Structure {
        public int cnt;
        public int size;
    }
    @Structure.FieldOrder({ "seps", "degrees" })
    public static class random_poly_info extends Structure {
        public int[] seps;
        public int[] degrees;
    }
    @Structure.FieldOrder({ "base64", "value" })
    public static class a64l_test extends Structure {
        public String base64;
        public NativeLong value;
    }
    @Structure.FieldOrder({ "s", "var", "cmp", "arg", "t" })
    public static class msort_param extends Structure {
        public int s;
        public int var;
        public int cmp;
        public Pointer arg;
        public String t;
    }


//    @Structure.FieldOrder({ "__refcount", "nameserver_list", "nameserver_list_size", "search_list", "search_list_size", "sort_list", "sort_list_size", "options", "retrans", "retry", "ndots" })
//    public static class resolv_conf extends Structure {
//        public int __refcount;
//        public sockaddr[] nameserver_list;
//        public int nameserver_list_size;
//        public Pointer search_list;
//        public int search_list_size;
//        public resolv_sortlist_entry sort_list;
//        public int sort_list_size;
//        public int options;
//        public int retrans;
//        public int retry;
//        public int ndots;
//    }
    @Structure.FieldOrder({ "h_name", "h_aliases", "h_addrtype", "h_length", "h_addr_list" })
    public static class hostent extends Structure {
        public String h_name;
        public String h_aliases;
        public int h_addrtype;
        public int h_length;
        public String h_addr_list;
    }
    @Structure.FieldOrder({ "s_name", "s_aliases", "s_port", "s_proto" })
    public static class servent extends Structure {
        public String s_name;
        public String s_aliases;
        public int s_port;
        public String s_proto;
    }
    @Structure.FieldOrder({ "p_name", "p_aliases", "p_proto" })
    public static class protoent extends Structure {
        public String p_name;
        public String p_aliases;
        public int p_proto;
    }
    @Structure.FieldOrder({ "ai_flags", "ai_family", "ai_socktype", "ai_protocol", "ai_addrlen", "ai_addr", "ai_canonname", "ai_next" })
    public static class addrinfo extends Structure {
        public int ai_flags;
        public int ai_family;
        public int ai_socktype;
        public int ai_protocol;
        public int ai_addrlen;
        public sockaddr ai_addr;
        public String ai_canonname;
        public addrinfo ai_next;
    }
    @Structure.FieldOrder({ "ar_name", "ar_service", "ar_request", "ar_result", "__return", "__glibc_reserved" })
    public static class gaicb extends Structure {
        public String ar_name;
        public String ar_service;
        public addrinfo ar_request;
        public addrinfo ar_result;
        public int __return;
        public int[] __glibc_reserved;
    }
//    @Structure.FieldOrder({ "resp", "conf", "__refcount", "__from_res", "__next" })
//    public static class resolv_context extends Structure {
//        public __res_state resp;
//        public resolv_ conf;
//        public int __refcount;
//        public boolean __from_res;
//        public resolv_context __next;
//    }
    @Structure.FieldOrder({ "initialized", "unused1", "unused2", "num_trimdomains", "trimdomain", "flags" })
    public static class hconf extends Structure {
        public int initialized;
        public int unused1;
        public int[] unused2;
        public int num_trimdomains;
        public Pointer trimdomain;
        public int flags;
    }
    @Structure.FieldOrder({ "input", "ipv4_ok", "ipv6_ok", "ipv4_expected", "ipv6_expected" })
    public static class test_case extends Structure {
        public String input;
        public boolean ipv4_ok;
        public boolean ipv6_ok;
        public byte[] ipv4_expected;
        public byte[] ipv6_expected;
    }

    @Structure.FieldOrder({ "data", "length" })
    public static class buffer extends Structure {
        public Pointer data;
        public int length;
    }
//    @Structure.FieldOrder({ "qname", "qtype", "edns" })
//    public static class response_data extends Structure {
//        public String qname;
//        public short qtype;
//        public resolv__info edns;
//    }

    @Structure.FieldOrder({ "used", "allocated", "array" })
    public static class dynarray_header extends Structure {
        public int used;
        public int allocated;
        public Pointer array;
    }
    @Structure.FieldOrder({ "array", "length" })
    public static class dynarray_finalize_result extends Structure {
        public Pointer array;
        public int length;
    }
    @Structure.FieldOrder({ "limit", "prev", "contents" })
    public static class _obstack_chunk extends Structure {
        public String limit;
        public _obstack_chunk prev;
        public byte[] contents;
    }
    @Structure.FieldOrder({ "arena", "ordblks", "smblks", "hblks", "hblkhd", "usmblks", "fsmblks", "uordblks", "fordblks", "keepcost" })
    public static class mallinfo extends Structure {
        public int arena;
        public int ordblks;
        public int smblks;
        public int hblks;
        public int hblkhd;
        public int usmblks;
        public int fsmblks;
        public int uordblks;
        public int fordblks;
        public int keepcost;
    }
    @Structure.FieldOrder({ "pointer", "alignment" })
    public static class allocate_result extends Structure {
        public Pointer pointer;
        public int alignment;
    }
    @Structure.FieldOrder({ "ifd", "real_stderr" })
    public static class buffer_tp_args extends Structure {
        public int ifd;
        public Pointer real_stderr;
    }
    @Structure.FieldOrder({ "array", "length" })
    public static class long_array extends Structure {
        public Pointer array;
        public int length;
    }
    @Structure.FieldOrder({ "next", })
    public static class heap_filler extends Structure {
        public heap_filler next;
    }
    @Structure.FieldOrder({ "magic", "version", "av", "sbrk_base", "sbrked_mem_bytes", "trim_threshold", "top_pad", "n_mmaps_max", "mmap_threshold", "check_action", "max_sbrked_mem", "max_total_mem", "n_mmaps", "max_n_mmaps", "mmapped_mem", "max_mmapped_mem", "using_malloc_checking", "max_fast", "arena_test", "arena_max", "narenas" })
    public static class malloc_save_state extends Structure {
        public NativeLong magic;
        public NativeLong version;
        public int[] av;
        public String sbrk_base;
        public int sbrked_mem_bytes;
        public NativeLong trim_threshold;
        public NativeLong top_pad;
        public int n_mmaps_max;
        public NativeLong mmap_threshold;
        public int check_action;
        public NativeLong max_sbrked_mem;
        public NativeLong max_total_mem;
        public int n_mmaps;
        public int max_n_mmaps;
        public NativeLong mmapped_mem;
        public NativeLong max_mmapped_mem;
        public int using_malloc_checking;
        public NativeLong max_fast;
        public NativeLong arena_test;
        public NativeLong arena_max;
        public NativeLong narenas;
    }
    @Structure.FieldOrder({ "heap", "stack", "time_low", "time_high" })
    public static class entry extends Structure {
        public long heap;
        public long stack;
        public int time_low;
        public int time_high;
    }
    @Structure.FieldOrder({ "length", "magic" })
    public static class header extends Structure {
        public int length;
        public int magic;
    }
    @Structure.FieldOrder({ "data", "size", "seed" })
    public static class allocation extends Structure {
        public Pointer data;
        public int size;
        public int seed;
    }
    @Structure.FieldOrder({ "size", "magic", "prev", "next", "block", "magic2" })
    public static class hdr extends Structure {
        public int size;
        public NativeLong magic;
        public hdr prev;
        public hdr next;
        public Pointer block;
        public NativeLong magic2;
    }
    @Structure.FieldOrder({ "next", "prev" })
    public static class list_t extends Structure {
        public list_head next;
        public list_head prev;
    }
    @Structure.FieldOrder({ "next", "prev" })
    public static class list_head extends Structure {
        public list_head next;
        public list_head prev;
    }

    @Structure.FieldOrder({ "tv_sec", "tv_nsec" })
    public static class __timespec64 extends Structure {
        public NativeLong tv_sec;
        public int tv_nsec;
    }
    @Structure.FieldOrder({ "dp", "v", "cnt" })
    public static class scandir_cancel_struct extends Structure {
        public Pointer dp;
        public Pointer v;
        public int cnt;
    }
//    @Structure.FieldOrder({ "r_list", "r_nlist" })
//    public static class r_scope_elem extends Structure {
//        public link_map r_list;
//        public int r_nlist;
//    }
//    @Structure.FieldOrder({ "*dirs", "malloced" })
//    public static class r_search_path_struct extends Structure {
//        public r_search_path_elem *dirs;
//        public int malloced;
//    }
    @Structure.FieldOrder({ "cookie", "bindflags" })
    public static class auditstate extends Structure {
        public int cookie;
        public int bindflags;
    }

    @Structure.FieldOrder({ "size", "msg" })
    public static class abort_msg_s extends Structure {
        public int size;
        public byte[] msg;
    }
    @Structure.FieldOrder({ "env", "updated_status" })
    public static class rm_ctx extends Structure {
        public int env;
        public boolean updated_status;
    }
    @Structure.FieldOrder({ "tv_sec", "tv_usec" })
    public static class __timeval64 extends Structure {
        public NativeLong tv_sec;
        public NativeLong tv_usec;
    }
    @Structure.FieldOrder({ "size", "ino", "mtime", "ctime" })
    public static class file_change_detection extends Structure {
        public NativeLong size;
        public int ino;
        public timespec mtime;
        public timespec ctime;
    }
//    @Structure.FieldOrder({ "e_name", "e_addr" })
//    public static class etherent extends Structure {
//        public String e_name;
//        public ether_addr e_addr;
//    }

    @Structure.FieldOrder({ "oa_rights", "oa_otype" })
    public static class oar_mask extends Structure {
        public int oa_rights;
        public int oa_otype;
    }
    @Structure.FieldOrder({ "uaddr", "family", "proto" })
    public static class endpoint extends Structure {
        public String uaddr;
        public String family;
        public String proto;
    }
    @Structure.FieldOrder({ "tc_name", "tc_flags", "tc_rights" })
    public static class table_col extends Structure {
        public String tc_name;
        public int tc_flags;
        public int tc_rights;
    }
    @Structure.FieldOrder({ "zo_oid", "zo_name", "zo_owner", "zo_group", "zo_domain", "zo_access", "zo_ttl", "zo_data" })
    public static class nis_object extends Structure {
        public int zo_oid;
        public int zo_name;
        public int zo_owner;
        public int zo_group;
        public int zo_domain;
        public int zo_access;
        public int zo_ttl;
        public int zo_data;
    }
    @Structure.FieldOrder({ "dir", "stamp" })
    public static class ping_args extends Structure {
        public int dir;
        public int stamp;
    }
    @Structure.FieldOrder({ "cp_status", "cp_zticks", "cp_dticks" })
    public static class cp_result extends Structure {
        public int cp_status;
        public int cp_zticks;
        public int cp_dticks;
    }
    @Structure.FieldOrder({ "tag_type", "tag_val" })
    public static class nis_tag extends Structure {
        public int tag_type;
        public String tag_val;
    }
    @Structure.FieldOrder({ "dir_name", "requester" })
    public static class fd_args extends Structure {
        public int dir_name;
        public int requester;
    }

    @Structure.FieldOrder({ "sin", "xid", "server_nr", "server_ep" })
    public static class findserv_req extends Structure {
        public sockaddr_in sin;
        public int xid;
        public int server_nr;
        public int server_ep;
    }
//    @Structure.FieldOrder({ "lock", "users", "port" })
//    public static class hurd_port extends Structure {
//        public int lock;
//        public hurd_userlink users;
//        public int port;
//    }
    @Structure.FieldOrder({ "first_request", "last_request", "handler", "next" })
    public static class ioctl_handler extends Structure {
        public int first_request;
        public int last_request;
        public int handler;
        public ioctl_handler next;
    }

    @Structure.FieldOrder({ "pw_name", "pw_passwd", "pw_uid", "pw_gid", "pw_gecos", "pw_dir", "pw_shell" })
    public static class passwd extends Structure {
        public Pointer pw_name;
        public Pointer pw_passwd;
        public int pw_uid;
        public int pw_gid;
        public Pointer pw_gecos;
        public Pointer pw_dir;
        public Pointer pw_shell;
    }
    @Structure.FieldOrder({ "st_dev", "st_ino", "st_mode", "st_nlink", "st_uid", "st_gid", "st_rdev", "st_size", "st_blksize", "st_blocks", "st_atim", "st_mtim", "st_ctim", "st_ctim.tv_sec" })
    public static class stat extends Structure {
        public int st_dev;
        public int st_ino;
        public int st_mode;
        public int st_nlink;
        public int st_uid;
        public int st_gid;
        public int st_rdev;
        public NativeLong st_size;
        public int st_blksize;
        public int st_blocks;
        public timespec st_atim;
        public timespec st_mtim;
        public timespec st_ctim;
        public int tv_sec;
    }

    @Structure.FieldOrder({ "st_dev", "st_ino", "st_mode", "st_nlink", "st_uid", "st_gid", "st_rdev", "st_size", "st_blksize", "st_blocks", "st_atim", "st_mtim", "st_ctim", "st_ctim.tv_sec" })
    public static class stat64 extends Structure {
        public int st_dev;
        public int st_ino;
        public int st_mode;
        public int st_nlink;
        public int st_uid;
        public int st_gid;
        public int st_rdev;
        public NativeLong st_size;
        public int st_blksize;
        public int st_blocks;
        public timespec st_atim;
        public timespec st_mtim;
        public timespec st_ctim;
        public int tv_sec;
    }

    @Structure.FieldOrder({ "ru_utime", "ru_stime", "ru_maxrss", "ru_ixrss", "ru_idrss", "ru_isrss", "ru_minflt", "ru_majflt", "ru_nswap", "ru_inblock", "ru_oublock", "ru_msgsnd", "ru_msgrcv", "ru_nsignals", "ru_nvcsw", "ru_nivcsw" })
    public static class rusage extends Structure {
        public timeval ru_utime;
        public timeval ru_stime;
        public NativeLong ru_maxrss;
        public NativeLong ru_ixrss;
        public NativeLong ru_idrss;
        public NativeLong ru_isrss;
        public NativeLong ru_minflt;
        public NativeLong ru_majflt;
        public NativeLong ru_nswap;
        public NativeLong ru_inblock;
        public NativeLong ru_oublock;
        public NativeLong ru_msgsnd;
        public NativeLong ru_msgrcv;
        public NativeLong ru_nsignals;
        public NativeLong ru_nvcsw;
        public NativeLong ru_nivcsw;
    }

    @Structure.FieldOrder({ "aio_threads", "aio_num", "aio_locks", "aio_usedba", "aio_debug", "aio_numusers", "aio_idle_time", "aio_reserved" })
    public static class aioinit extends Structure {
        public int aio_threads;
        public int aio_num;
        public int aio_locks;
        public int aio_usedba;
        public int aio_debug;
        public int aio_numusers;
        public int aio_idle_time;
        public int aio_reserved;
    }

    // Addition


    @Structure.FieldOrder({ "mnt_fsname", "mnt_dir", "mnt_type", "mnt_opts", "mnt_freq", "mnt_passno" })
    public static class mntent extends Structure {
        public String mnt_fsname;
        public String mnt_dir;
        public String mnt_type;
        public String mnt_opts;
        public int mnt_freq;
        public int mnt_passno;
    }
    @Structure.FieldOrder({ "modes", "offset", "freq", "maxerror", "esterror", "status", "constant", "precision", "tolerance", "time", "tick", "ppsfreq", "jitter", "shift", "stabil", "jitcnt", "calcnt", "errcnt", "stbcnt", "tai" })
    public static class timex extends Structure {
        public int modes;
        public NativeLong offset;
        public NativeLong freq;
        public NativeLong maxerror;
        public NativeLong esterror;
        public int status;
        public NativeLong constant;
        public NativeLong precision;
        public NativeLong tolerance;
        //public val time;
        public int time;
        public NativeLong tick;
        public NativeLong ppsfreq;
        public NativeLong jitter;
        public int shift;
        public NativeLong stabil;
        public NativeLong jitcnt;
        public NativeLong calcnt;
        public NativeLong errcnt;
        public NativeLong stbcnt;
        public int tai;
    }
    @Structure.FieldOrder({ "time", "maxerror", "esterror", "tai" })
    public static class ntptimeval extends Structure {
        //public val time;
        public int time;
        public NativeLong maxerror;
        public NativeLong esterror;
        public NativeLong tai;
    }
    @Structure.FieldOrder({ "actime", "modtime" })
    public static class utimbuf extends Structure {
        public int actime;
        public int modtime;
    }

    @Structure.FieldOrder({ "aio_fildes", "aio_offset;", "aio_buf","aio_nbytes", "aio_reqprio", "aio_sigevent", "aio_lio_opcode"})
    public static class aiocb extends Structure {
        public int             aio_fildes;     /* File descriptor */
        public int           aio_offset;     /* File offset */
        public Pointer aio_buf;        /* Location of buffer */
        public int          aio_nbytes;     /* Length of transfer */
        public int             aio_reqprio;    /* Request priority */
        //sigevent aio_sigevent;   /* Notification method */
        public int aio_sigevent;
        public int aio_lio_opcode; /* Operation to be performed;*/
    }
    @Structure.FieldOrder({ "aio_fildes", "aio_offset", "aio_buf","aio_nbytes", "aio_reqprio", "aio_sigevent", "aio_lio_opcode"})
    public static class aiocb64 extends Structure {
        public int             aio_fildes;     /* File descriptor */
        public int           aio_offset;     /* File offset */
        public Pointer aio_buf;        /* Location of buffer */
        public int          aio_nbytes;     /* Length of transfer */
        public int             aio_reqprio;    /* Request priority */
        //sigevent aio_sigevent;   /* Notification method */
        public int aio_sigevent;
        public int aio_lio_opcode; /* Operation to be performed;*/
    }

    @Structure.FieldOrder({ "gr_name", "gr_passwd;", "gr_gid","gr_mem"})
    public static class group extends Structure {
        public String gr_name;       /* group name */
        public String gr_passwd;     /* group password */
        public int   gr_gid;        /* group ID */
        public String[] gr_mem;        /* group members */
    }
    @Structure.FieldOrder({ "gr_name", "gr_passwd;", "gr_gid","gr_mem"})
    public static class utmp extends Structure {
        public char[]    ut_user; //100
        public char[]    ut_id;     //4
        public char[]    ut_line;          //100
        public int   ut_pid;
        public short   ut_type;
        public short   ut_spare;
        public int  ut_time;
    }

    @Structure.FieldOrder({ "gr_name", "gr_passwd", "gr_gid","gr_mem"})
    public static class utmpx extends Structure {
        public char[]    ut_user; //100
        public char[]    ut_id;     //4
        public char[]    ut_line;          //100
        public int   ut_pid;
        public short   ut_type;
        public timeval ut_tv;
    }

    @Structure.FieldOrder({ "fs_spec", "fs_file", "fs_vfstype","fs_mntops", "fs_type", "fs_freq", "fs_passno"})
    public static class fstab extends Structure {
        public String fs_spec;                        /* block special device name */
        public String fs_file;                        /* file system path prefix */
        public String fs_vfstype;                        /* File system type, ufs, nfs */
        public String fs_mntops;                        /* Mount options ala -o */
        public String fs_type;                /* FSTAB_* from fs_mntops */
        public int fs_freq;                        /* dump frequency, in days */
        public int fs_passno;
    }

    @Structure.FieldOrder({ "decimal_point", "thousands_sep", "grouping","int_curr_symbol", "currency_symbol", "mon_decimal_point", "mon_thousands_sep", "mon_grouping", "positive_sign", "negative_sign", "int_frac_digits",
        "frac_digits", "p_cs_precedes", "p_sep_by_space", "n_cs_precedes", "n_sep_by_space", "p_sign_posn", "n_sign_posn"})
    public static class lconv extends Structure {
        public String decimal_point;
        public String thousands_sep;
        public String grouping;
        public String int_curr_symbol;
        public String currency_symbol;
        public String mon_decimal_point;
        public String mon_thousands_sep;
        public String mon_grouping;
        public String positive_sign;
        public String negative_sign;
        public char int_frac_digits;
        public char frac_digits;
        public char p_cs_precedes;
        public char p_sep_by_space;
        public char n_cs_precedes;
        public char n_sep_by_space;
        public char p_sign_posn;
        public char n_sign_posn;
    }
    @Structure.FieldOrder({ "prec", "width", "spec","is_long_double", "is_short", "alt", "space", "left", "showsign", "group", "extra",
            "is_char", "wide", "i18n", "is_binary128", "__pad", "user", "pad"})
    public static class printf_info extends Structure {
        public int prec;                        /* Precision.  */
        public int width;                        /* Width.  */
        public char spec;                        /* Format letter.  */
        public int is_long_double;/* L flag.  */
        public int is_short;        /* h flag.  */
        public int is_long;        /* l flag.  */
        public int alt;                /* # flag.  */
        public int space;                /* Space flag.  */
        public int left;                /* - flag.  */
        public int showsign;        /* + flag.  */
        public int group;                /* ' flag.  */
        public int extra;                /* For special use.  */
        public int is_char;        /* hh flag.  */
        public int wide;                /* Nonzero for wide character streams.  */
        public int i18n;                /* I flag.  */
        public int is_binary128;        /* Floating-point argument is ABI-compatible
                                   with IEC 60559 binary128.  */
        public int __pad;                /* Unused so far.  */
        public short user;        /* Bits for user-installed modifiers.  */
        public char pad;                        /* Padding character.  */
    }

    // from JNAerator
    public static class termios extends Structure {
        /**
         * input mode flags<br>
         * C type : tcflag_t
         */
        public int c_iflag;
        /**
         * output mode flags<br>
         * C type : tcflag_t
         */
        public int c_oflag;
        /**
         * control mode flags<br>
         * C type : tcflag_t
         */
        public int c_cflag;
        /**
         * local mode flags<br>
         * C type : tcflag_t
         */
        public int c_lflag;
        /**
         * line discipline<br>
         * C type : cc_t
         */
        public byte c_line;
        /**
         * control characters<br>
         * C type : cc_t[32]
         */
        public byte[] c_cc = new byte[32];
        /**
         * input speed<br>
         * C type : speed_t
         */
        public int c_ispeed;
        /**
         * output speed<br>
         * C type : speed_t
         */
        public int c_ospeed;
//        public termios() {
//            super();
//        }
//
//        public termios(int c_iflag, int c_oflag, int c_cflag, int c_lflag, byte c_line, byte c_cc[], int c_ispeed, int c_ospeed) {
//            super();
//            this.c_iflag = c_iflag;
//            this.c_oflag = c_oflag;
//            this.c_cflag = c_cflag;
//            this.c_lflag = c_lflag;
//            this.c_line = c_line;
//            if ((c_cc.length != this.c_cc.length))
//                throw new IllegalArgumentException("Wrong array size !");
//            this.c_cc = c_cc;
//            this.c_ispeed = c_ispeed;
//            this.c_ospeed = c_ospeed;
//        }
    }



}
