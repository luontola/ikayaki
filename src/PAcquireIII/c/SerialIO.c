/************************************************************************
*                                                                       *
*       SerialIO.c                                                      *
*                                                                       *
*       Contains Serial IO Routines.                                    *
*                                                                       *
*       8.Aug.1998 Jamie Lisa Finch.                                    *
*                                                                       *
************************************************************************/

#include <Windows.h>
#include <StdIO.h>
#include "..\h\SerialIO.h"
#include "..\h\Messages.h"

#define MAXSERIALBLOCK (4*1024)


/************************************************************************
*                                                                       *
*       SerialError:                                                    *
*                                                                       *
*       Places The First Error Encountered Into The Serial Buffer.      *
*                                                                       *
*       Input:                                                          *
*                                                                       *
*       SerialIO    = Address Of Serial IO Structure.                   *
*       SerialError = Value   Of Serial Error Enumeration.              *
*       Data        = Value   Of Data Accociated With Error.            *
*                                                                       *
*       Output:                                                         *
*                                                                       *
*       The First Serial Error Is Written Into The Buffer.              *
*                                                                       *
*       11.July 2002 Jamie Lisa Finch.                                  *
*                                                                       *
************************************************************************/

static void SerialError( SERIALIO *SerialIO, SERIALERROR SerialError, int Data ) {

//  char Text[ 256 ];

  if( ( SerialIO != (SERIALIO *) NULL ) &&
      ( SerialIO->SerialError != se_None ) ) {

    SerialIO->SerialError     = SerialError;
    SerialIO->SerialErrorData = Data;
    }

  /* Write Text Message For Failure. */

//  Log( "SerialError", "SerialError %d, Data %d", SerialError, Data);
//  wsprintf( Text, "Error %d, Data %d\n", SerialError, Data );
//  OutputDebugString( Text );
  }


/************************************************************************
*                                                                       *
*       WriteCommBlock:                                                 *
*                                                                       *
*       Writes A Block Of Data To The Serial Device.                    *
*                                                                       *
*       Input:                                                          *
*                                                                       *
*       SerialIO     = Address Of Serial IO Structure.                  *
*       Buffer       = Address Of Buffer To Write.                      *
*       BufferLength = Length  Of Buffer To Write.                      *
*                                                                       *
*       Output:                                                         *
*                                                                       *
*       Returns -> Number Of Bytes Written.                             *
*                                                                       *
*       11.July 2002 Jamie Lisa Finch.                                  *
*                                                                       *
************************************************************************/

static DWORD WriteCommBlock( SERIALIO *SerialIO, unsigned char *Buffer, DWORD BufferLength ) {

  BOOL    fWriteStat;
  DWORD   dwBytesWritten;
  DWORD   dwErrorFlags;
  DWORD   dwError;
  DWORD   dwBytesSent;
  COMSTAT ComStat;

  dwBytesSent = 0;
  if( SerialIO != (SERIALIO *) NULL ) {

    /* Note that normally the code will not execute the following because the driver caches write operations. */
    /* Small I/O requests (up to several thousand bytes) will normally be accepted immediately and WriteFile  */
    /* will return true even though an overlapped operation was specified.                                    */

    if( !( fWriteStat = WriteFile( SerialIO->File, Buffer, BufferLength, &dwBytesWritten, &SerialIO->OverLappedWrite ) ) ) {
      if( GetLastError() == ERROR_IO_PENDING ) {

        /* We should wait for the completion of the write operation so we know if it worked or not.           */

        /* This is only one way to do this. It might be beneficial to place the write operation in a separate */
        /* thread so that blocking on completion will not negatively affect the responsiveness of the UI      */

        /* If the write takes too long to complete, this function will timeout according to the CommTimeOuts. */
        /* WriteTotalTimeoutMultiplier variable.  This code logs the timeout but does not retry the write.    */

        while( !GetOverlappedResult( SerialIO->File, &SerialIO->OverLappedWrite, &dwBytesWritten, TRUE ) ) {

          /* Normal Result If Not Finished. */

          if( ( dwError = GetLastError() ) == ERROR_IO_INCOMPLETE ) {
            dwBytesSent += dwBytesWritten;
            continue;
            }

          /* An Error Occurred, Try To Recover. */

          else {
            SerialError( SerialIO, se_WriteError1, dwError );
            ClearCommError( SerialIO->File, &dwErrorFlags, &ComStat );
            if( dwErrorFlags > 0 )
              SerialError( SerialIO, se_WriteError2, dwErrorFlags );
            break;
            }
          }
        dwBytesSent += dwBytesWritten;

        if( dwBytesSent != BufferLength )
          SerialError( SerialIO, se_WriteTimeOut, dwBytesSent );
        }

      /* Some Other Error Occurred. */

      else {
        ClearCommError( SerialIO->File, &dwErrorFlags, &ComStat );
        if( dwErrorFlags > 0 )
          SerialError( SerialIO, se_WriteOtherError, dwErrorFlags );
        return dwBytesSent;
        }
      }
    else dwBytesSent = dwBytesWritten;
    }
  return dwBytesSent;
  }


/************************************************************************
*                                                                       *
*       ReadCommBlock:                                                  *
*                                                                       *
*       Reads A Block Of Data From The Serial Device.                   *
*                                                                       *
*       Input:                                                          *
*                                                                       *
*       SerialIO        = Address Of Serial IO Structure.               *
*       Buffer          = Address Of Buffer To Write.                   *
*       MaxBufferLength = Maximum Length Of Buffer To Write.            *
*                                                                       *
*       Output:                                                         *
*                                                                       *
*       Returns -> Number Of Bytes Read.                                *
*                                                                       *
*       11.July 2002 Jamie Lisa Finch.                                  *
*                                                                       *
************************************************************************/

static DWORD ReadCommBlock( SERIALIO *SerialIO, unsigned char *Buffer, DWORD MaxBufferLength ) {

  BOOL    fReadStat;
  COMSTAT ComStat;
  DWORD   dwErrorFlags;
  DWORD   dwLength;
  DWORD   dwError;

  dwLength = 0;
  if( ( SerialIO       != (SERIALIO *) NULL    ) &&
      ( SerialIO->File != INVALID_HANDLE_VALUE ) ) {

    /* Only Try To Read Number Of Bytes In Queue. */

    ClearCommError( SerialIO->File, &dwErrorFlags, &ComStat );
    if( ( dwLength = min( MaxBufferLength, ComStat.cbInQue ) ) > 0 ) {
      if( !( fReadStat = ReadFile( SerialIO->File, Buffer, dwLength, &dwLength, &SerialIO->OverLappedRead ) ) ) {
        if( GetLastError() == ERROR_IO_PENDING ) {
          SerialError( SerialIO, se_ReadIOPending, 0 );

          /* We have to wait for read to complete.  This function will timeout according to the CommTimeOuts. */
          /* ReadTotalTimeoutConstant variable Every time it times out, check for port errors.                */

          while( !GetOverlappedResult( SerialIO->File, &SerialIO->OverLappedRead, &dwLength, TRUE ) ) {

            /* Normal Result If Not Finished. */

            if( ( dwError = GetLastError() ) == ERROR_IO_INCOMPLETE )
              continue;

            /* An Error Occurred, Try To Recover. */

            else {
              SerialError( SerialIO, se_ReadError1, dwError );
              ClearCommError( SerialIO->File, &dwErrorFlags, &ComStat );
              if( dwErrorFlags > 0 )
                SerialError( SerialIO, se_ReadError2, dwErrorFlags );
              break;
              }
            }
          }

        /* Some Other Error Occurred. */

        else {
          dwLength = 0;
          ClearCommError( SerialIO->File, &dwErrorFlags, &ComStat );
          if( dwErrorFlags > 0 )
            SerialError( SerialIO, se_ReadOtherError, dwErrorFlags );
          }
        }
      }
    }
  return dwLength;
  }


/************************************************************************
*                                                                       *
*       CommWatchProc:                                                  *
*                                                                       *
*       Contains The Communication Thread.                              *
*                                                                       *
*       Input:                                                          *
*                                                                       *
*       SerialIO = Address Of Serial IO Structure.                      *
*                                                                       *
*       Output:                                                         *
*                                                                       *
*       The Data Is Read From Serial Device.                            *
*                                                                       *
*       11.July 2002 Jamie Lisa Finch.                                  *
*                                                                       *
************************************************************************/

static DWORD WINAPI CommWatchProc( LPVOID vSerialIO ) {

  SERIALIO  *SerialIO = (SERIALIO *) vSerialIO;
  DWORD      dwEvtMask;
  int        nLength;
  char       Buffer[ MAXSERIALBLOCK ];

  if( SerialIO != (SERIALIO *) NULL ) {

    while( ( SerialIO->Connected ) &&
           ( SerialIO->File != INVALID_HANDLE_VALUE ) ) {

      dwEvtMask = 0;
      WaitCommEvent( SerialIO->File, &dwEvtMask, NULL );

      if( ( dwEvtMask & EV_RXCHAR) == EV_RXCHAR ) {
        do {
          if( nLength = ReadCommBlock( SerialIO, Buffer, sizeof( Buffer ) ) ) {

            /* CallBack Protytype BOOL ReadCallback( UserData, Buffer, Length ); */

            EnterCriticalSection( &SerialIO->CriticalSection );
            if( ( SerialIO->SerialBufferUsed + nLength ) <= sizeof( SerialIO->SerialBuffer ) ) {
              memcpy( &SerialIO->SerialBuffer[ SerialIO->SerialBufferUsed ], Buffer, nLength );
              SerialIO->SerialBufferUsed += nLength;
              }
            else {
              SerialIO->SerialBufferUsed = 0;
              SerialError( SerialIO, se_WatchProcOverflow, nLength );
              }
            LeaveCriticalSection( &SerialIO->CriticalSection );
            }
          } while( nLength > 0 );
        }
      }

    /* Exit Subroutine. */

    SerialIO->ThreadID = 0;     /* Clear Information In Structure ( We Are Done Flag ). */

    return TRUE;
    }
  return FALSE;
  }

/************************************************************************
*                                                                       *
*       CloseSerialDevice:                                              *
*                                                                       *
*       Closes The Serial Device And Removes It's Resources From The    *
*       System.                                                         *
*                                                                       *
*       Input:                                                          *
*                                                                       *
*       SerialIO = Address Of Serial IO Structure.                      *
*                                                                       *
*       Output:                                                         *
*                                                                       *
*       Returns -> TRUE  == Device Was Opened And Was Closed.           *
*                  FALSE == Nothing To Do.                              *
*                                                                       *
*       9.Aug.1998 Jamie Lisa Finch.                                    *
*                                                                       *
************************************************************************/

static BOOL CloseSerialDevice( SERIALIO *SerialIO ) {

  COMSTAT    ComStat;
  DWORD dwErrorFlags;

  if( SerialIO != (SERIALIO *) NULL ) {

    /* Stop The Thread From Running. */

    SerialIO->Connected = FALSE;

    if( SerialIO->File != INVALID_HANDLE_VALUE ) {

      /* Stop The Task From Running. */

      SetCommMask( SerialIO->File, 0 ); /* Signal Task We Are Done. */
      Sleep( 1 );                       /* Let The OS Do It.        */
      ClearCommError( SerialIO->File, &dwErrorFlags, &ComStat );

      /* Close The Serial Device. */

      CloseHandle( SerialIO->File );
      SerialIO->File = INVALID_HANDLE_VALUE;
      DeleteCriticalSection( &SerialIO->CriticalSection );
      }

    /* Wait Till Thread Stops And Remove Thread Handle. */

    if( SerialIO->hWatchThread != NULL ) {
      CloseHandle( SerialIO->hWatchThread );
      SerialIO->hWatchThread = NULL;
      }

    /* Remove The Event Handles. */

    if( SerialIO->OverLappedRead.hEvent  != NULL ) {
      CloseHandle( SerialIO->OverLappedRead.hEvent );
      SerialIO->OverLappedRead.hEvent  = NULL;
      }
    if( SerialIO->OverLappedWrite.hEvent != NULL ) {
      CloseHandle( SerialIO->OverLappedWrite.hEvent );
      SerialIO->OverLappedWrite.hEvent = NULL;
      }

    /* Give Back Remaining Memory. */

    free( SerialIO );
    SerialIO = (SERIALIO *) NULL;

    return TRUE;
    }
  return FALSE;
  }


/************************************************************************
*                                                                       *
*       SetBaudRate:                                                    *
*                                                                       *
*       Send The Baud Rate To The Hardware.                             *
*                                                                       *
*       Input:                                                          *
*                                                                       *
*       SerialIO   = Address Of Serial IO Structure.                    *
*       BaudRate   = Baud Rate of Serial Device.                        *
*       DataBits   = Number of Data Bits, 4 - 8.                        *
*       Parity     = 'N' = 0, 'O' = 1, 'E' = 2, 'M' = 3, 'S' = 4.       *
*       StopBits   = Number of Stop Bits, 0, 1, 2 = 1, 1.5, 2.          *
*       ConfigMode = TRUE  == Sensor Is In Config Mode.                 *
*                    FALSE == Sensor Is In Run    Mode.                 *
*       Output:                                                         *
*                                                                       *
*       Returns -> TRUE  == Baud Rate Was Setup Properly.               *
*                  FALSE == Failed To Setup Baud Rate.                  *
*                                                                       *
*       9.Aug.1998 Jamie Lisa Finch.                                    *
*                                                                       *
************************************************************************/

static BOOL SetBaudRate(
  SERIALIO     *SerialIO,
  unsigned long BaudRate,
  unsigned char DataBits,
  unsigned char Parity,
  unsigned char StopBits,
  BOOL          ConfigMode ) {

  DCB  DeviceControlBlock;

  if( ( SerialIO       != (SERIALIO *) NULL    ) &&
      ( SerialIO->File != INVALID_HANDLE_VALUE ) ) {

    memset( &DeviceControlBlock, 0, sizeof( DCB ) );
    DeviceControlBlock.DCBlength  = sizeof( DCB );
    GetCommState( SerialIO->File, &DeviceControlBlock );

    DeviceControlBlock.DCBlength         = sizeof( DCB );
    DeviceControlBlock.BaudRate          = BaudRate;
    DeviceControlBlock.fBinary           = TRUE;
    DeviceControlBlock.fParity           = FALSE;
    DeviceControlBlock.fOutxCtsFlow      = FALSE;
    DeviceControlBlock.fOutxDsrFlow      = FALSE;
    DeviceControlBlock.fDtrControl       = ! ConfigMode;
    DeviceControlBlock.fDsrSensitivity   = FALSE;
    DeviceControlBlock.fTXContinueOnXoff = TRUE;
    DeviceControlBlock.fOutX             = FALSE;
    DeviceControlBlock.fInX              = FALSE;
    DeviceControlBlock.fErrorChar        = FALSE;
    DeviceControlBlock.fNull             = 0;
    DeviceControlBlock.fRtsControl       = ! ConfigMode;
    DeviceControlBlock.fAbortOnError     = TRUE;
    DeviceControlBlock.fDummy2           = 0;
    DeviceControlBlock.wReserved         = 0;
    DeviceControlBlock.XonLim            = 0;
    DeviceControlBlock.XoffLim           = 0;
    DeviceControlBlock.ByteSize          = DataBits;
    DeviceControlBlock.Parity            = Parity;
    DeviceControlBlock.StopBits          = StopBits;
    DeviceControlBlock.XonChar           = 0;
    DeviceControlBlock.XoffChar          = 0;
    DeviceControlBlock.ErrorChar         = 0;
    DeviceControlBlock.EofChar           = 0;
    DeviceControlBlock.EvtChar           = 0;
    DeviceControlBlock.wReserved1        = 0;

    return SetCommState( SerialIO->File, &DeviceControlBlock );
    }
  return FALSE;
  }


/************************************************************************
*                                                                       *
*       ReadSerialBuffer:                                               *
*                                                                       *
*       Reads The Current Content Of The Serial Buffer.                 *
*                                                                       *
*       Input:                                                          *
*                                                                       *
*       SerialIO     = Address Of Serial IO Structure.                  *
*       Buffer       = Address Of Buffer To Read Data Into.             *
*       BufferLength = Length In Bytes Of Serial Buffer.                *
*       DataRead     = Address Of Variable To Return Amount Of Data Read*
*                                                                       *
*       Output:                                                         *
*                                                                       *
*       Returns -> TRUE  == An Attempt Was Made To Read The Data.       *
*                  FALSE == No Attempt Was Made To Read The Data.       *
*                                                                       *
*       DataRead     = Number Of Bytes Of Data Read From Serial Device. *
*                                                                       *
*       9.Aug.1998 Jamie Lisa Finch.                                    *
*                                                                       *
************************************************************************/

static BOOL ReadSerialBuffer(
  SERIALIO      *SerialIO,
  unsigned char *Buffer,
  long           BufferLength,
  DWORD         *DataRead ) {

  long          Amount;
  long          EndCopy;

  *DataRead = 0;

  if( ( SerialIO       != (SERIALIO *) NULL    ) &&
      ( SerialIO->File != INVALID_HANDLE_VALUE ) ) {
    if( ( BufferLength               > 0 ) &&
        ( SerialIO->SerialBufferUsed > 0 ) ) {

      EnterCriticalSection( &SerialIO->CriticalSection );
      Amount = SerialIO->SerialBufferUsed;
      if( Amount > BufferLength ) Amount = BufferLength;
      memcpy( Buffer, SerialIO->SerialBuffer, Amount );
      if( ( EndCopy = SerialIO->SerialBufferUsed - Amount ) > 0 ) {
        memcpy( SerialIO->SerialBuffer, &SerialIO->SerialBuffer[ Amount ], EndCopy );
        }
      SerialIO->SerialBufferUsed -= Amount;
      *DataRead = Amount;
      LeaveCriticalSection( &SerialIO->CriticalSection );
      return TRUE;
      }
    }
  return FALSE;
  }


/************************************************************************
*                                                                       *
*       WriteSerialBuffer:                                              *
*                                                                       *
*       Writes The Current Content Of The Serial Buffer.                *
*                                                                       *
*       Input:                                                          *
*                                                                       *
*       SerialIO     = Address Of Serial IO Structure.                  *
*       Buffer       = Address Of Buffer To Write Data From.            *
*       BufferLength = Length In Bytes Of Serial Buffer.                *
*       DataWrite    = Address Of Variable To Return Amount Of Data     *
*                      Written.                                         *
*       Output:                                                         *
*                                                                       *
*       Returns -> TRUE  == Data Was Written.                           *
*                  FALSE == Failed To Write Data.                       *
*                                                                       *
*       DataWrite    = Number Of Bytes Of Data Written To Serial Device.*
*                                                                       *
*       9.Aug.1998 Jamie Lisa Finch.                                    *
*                                                                       *
************************************************************************/

static BOOL WriteSerialBuffer(
  SERIALIO      *SerialIO,
  unsigned char *Buffer,
  long           BufferLength,
  DWORD         *DataWrite ) {

  *DataWrite = 0;

  if( ( SerialIO       != (SERIALIO *) NULL    ) &&
      ( SerialIO->File != INVALID_HANDLE_VALUE ) ) {
    if( ( *DataWrite = WriteCommBlock( SerialIO, Buffer, BufferLength ) ) != 0 ) {
      return TRUE;
      }
    }
  return FALSE;
  }


/************************************************************************
*                                                                       *
*       OpenSerialDevice:                                               *
*                                                                       *
*       Opens The Serial Device.                                        *
*                                                                       *
*       Input:                                                          *
*                                                                       *
*       SerialPortNumber = 0 to 9, Which Serial Port To Use.            *
*                                                                       *
*       Output:                                                         *
*                                                                       *
*       Returns -> Address Of Serial IO Structure.                      *
*                  NULL If Failed.                                      *
*                                                                       *
*       9.Aug.1998 Jamie Lisa Finch.                                    *
*                                                                       *
************************************************************************/

SERIALIO *OpenSerialDevice( char SerialPortNumber ) {

  SERIALIO    *SerialIO;
  COMMTIMEOUTS CommTimeouts;
  COMSTAT      ComStat;
  DWORD        dwErrorFlags;
  char         ComNumber[ 12 ];

  if( SerialIO = malloc( sizeof( SERIALIO ) ) ) {
    memset( SerialIO, 0, sizeof( SERIALIO ) );

    /* Setup Function Pointers. */

    SerialIO->Close       = CloseSerialDevice;
    SerialIO->SetBaudRate = SetBaudRate;
    SerialIO->Write       = WriteSerialBuffer;
    SerialIO->Read        = ReadSerialBuffer;

    /* Build The String, COM1: */

    sprintf( ComNumber, "COM%d:", SerialPortNumber );
    if( ( SerialIO->File = CreateFile( ComNumber, GENERIC_READ | GENERIC_WRITE, 0, NULL, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL | FILE_FLAG_OVERLAPPED, NULL ) ) != INVALID_HANDLE_VALUE ) {
      ClearCommError( SerialIO->File, &dwErrorFlags, &ComStat );
      SetCommMask( SerialIO->File, EV_RXCHAR );

      /* Setup The Communications Timeout The Way We Need Them. */

      memset( &CommTimeouts, 0, sizeof( COMMTIMEOUTS ) );
      CommTimeouts.ReadIntervalTimeout         = MAXDWORD;
      CommTimeouts.ReadTotalTimeoutMultiplier  =        0;
      CommTimeouts.ReadTotalTimeoutConstant    =        0;
      CommTimeouts.WriteTotalTimeoutMultiplier =        0; /*   70 */
      CommTimeouts.WriteTotalTimeoutConstant   =        0; /* 1000 */

      SetCommTimeouts(SerialIO->File, &CommTimeouts );
      SetupComm(      SerialIO->File, MAXSERIALBLOCK, MAXSERIALBLOCK ); /* Setup Device Buffers. */

      /* Purge Any Information In The Buffer. */

      PurgeComm( SerialIO->File, PURGE_TXABORT | PURGE_RXABORT | PURGE_TXCLEAR | PURGE_RXCLEAR );
      InitializeCriticalSection( &SerialIO->CriticalSection );

      if( ( ( SerialIO->OverLappedRead.hEvent  = CreateEvent( NULL, TRUE, FALSE, NULL ) ) != NULL ) &&
          ( ( SerialIO->OverLappedWrite.hEvent = CreateEvent( NULL, TRUE, FALSE, NULL ) ) != NULL ) ) {

        SerialIO->Connected = TRUE;

        if( SerialIO->hWatchThread = CreateThread( (LPSECURITY_ATTRIBUTES) NULL, 0, CommWatchProc, (LPVOID) SerialIO, 0, &SerialIO->ThreadID ) ) {
          return SerialIO;
          }
        }
      }
    SerialIO->Close( SerialIO );
    SerialIO = (SERIALIO *) NULL;
    }
  return SerialIO;
  }
