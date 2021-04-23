#include <stdio.h>
#include <sys/time.h>

int main()
{
    struct timeval tv;
    struct timezone tz;

    gettimeofday(&tv,&tz);

    printf("Seconds since 1/1/1970: %lu\n",tv.tv_sec);
    printf("Microseconds: %d\n",tv.tv_usec);
    printf("Minutes west of Greenwich: %d\n",tz.tz_minuteswest);
    printf("Daylight Saving Time adjustment: %d\n",tz.tz_dsttime);

    return(0);
}

