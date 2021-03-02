#include <stdio.h>
#include <signal.h>

void main()
  {
    sigset_t set;

    sigemptyset( &set );
    print( set, SIGINT );

    sigfillset( &set );
    print( set, SIGINT );

    sigdelset( &set, SIGINT );
    print( set, SIGINT );

    sigaddset( &set, SIGINT );
    print( set, SIGINT );
  }

void print( sigset_t set, int signo )
  {

    printf( "Set %8.8lx. Signal %d is ", set, signo );
    if( sigismember( &set, signo ) )
      printf( "a member.\n" );
    else
      printf( "not a member.\n" );
  }