/************************************************************************
*                                                                       *
*       SerialIO.h                                                      *
*                                                                       *
*       Contains Serial IO Routines.                                    *
*                                                                       *
*       8.Aug.1998 Jamie Lisa Finch.                                    *
*                                                                       *
************************************************************************/

#ifndef __SERIALIO_INCLUDED__
#define __SERIALIO_INCLUDED__

#ifdef __cplusplus
extern "C" {
#endif

#define STOPBITS1       0
#define STOPBITS1_5     1
#define STOPBITS2       2

#define NOPARITY        0
#define ODDPARITY       1
#define EVENPARITY      2
#define MARKPARITY      3
#define SPACEPARITY     4

#define MAXSERIALBUFFER (8*1024)

typedef enum _serialerror {
  se_None = 0,
  se_WriteError1,
  se_WriteError2,
  se_WriteTimeOut,
  se_WriteOtherError,
  se_ReadIOPending,
  se_ReadError1,
  se_ReadError2,
  se_ReadOtherError,
  se_WatchProcOverflow,
  } SERIALERROR;

typedef BOOL (READSERIALCALLBACK)( void *, char *, int );

typedef struct _serialio {
  BOOL             (*Close      )( struct _serialio * );
  BOOL             (*SetBaudRate)( struct _serialio *, unsigned long, unsigned char, unsigned char, unsigned char, BOOL );
  BOOL             (*Write      )( struct _serialio *, unsigned char *, long, DWORD * );
  BOOL             (*Read       )( struct _serialio *, unsigned char *, long, DWORD * );
  HANDLE           hWatchThread;
  HANDLE           File;
  BOOL             Connected;
  DWORD            ThreadID;
  OVERLAPPED       OverLappedWrite;
  OVERLAPPED       OverLappedRead;
  CRITICAL_SECTION CriticalSection;
  SERIALERROR      SerialError;
  int              SerialErrorData;
  DWORD            SerialBufferUsed;
  char             SerialBuffer[ MAXSERIALBUFFER ];
  } SERIALIO;

SERIALIO *OpenSerialDevice( char );

#ifdef __cplusplus
};
#endif
#endif
