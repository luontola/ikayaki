/************************************************************************
*                                                                       *
*       Messages.c:                                                     *
*                                                                       *
*       Contains The Text For The Messages.                             *
*                                                                       *
*       29.Aug.2002 Jamie Lisa Finch.                                   *
*                                                                       *
************************************************************************/

#include <windows.h>
#include "..\h\messages.h"


/************************************************************************
*                                                                       *
*       ErrorMessage:                                                   *
*                                                                       *
*       Writes An Error Message Box On The Screen.                      *
*                                                                       *
*       Input:                                                          *
*                                                                       *
*       Message     = Address Of Text String To Display.                *
*       WindowTitle = Address Of Window Title Text.                     *
*                                                                       *
*       Output:                                                         *
*                                                                       *
*       An Error Message Is Display On The Screen Until The User        *
*       Presses "OK".                                                   *
*                                                                       *
*       29.Aug.2002 Jamie Lisa Finch.                                   *
*                                                                       *
************************************************************************/

void ErrorMessage( char *Message, char *WindowTitle ) {

  MessageBox( (HWND) NULL, Message, WindowTitle, MB_OK | MB_ICONSTOP );
  }


/************************************************************************
*                                                                       *
*       LastErrorToString:                                              *
*                                                                       *
*       Converts The Last Error Number To Text.                         *
*                                                                       *
*       Input:                                                          *
*                                                                       *
*       ErrorMessage = Address Of Address Of Resulting Text Message.    *
*                                                                       *
*       Output:                                                         *
*                                                                       *
*       The Last Error Is Converted To Text.                            *
*                                                                       *
*       29.Aug.2002 Jamie Lisa Finch.                                   *
*                                                                       *
************************************************************************/

void LastErrorToString( char **ErrorMessage ) {

  /* Convert The Error To Text. */

  FormatMessage(
    FORMAT_MESSAGE_ALLOCATE_BUFFER | FORMAT_MESSAGE_FROM_SYSTEM,
    NULL,
    GetLastError(),
    MAKELANGID( LANG_NEUTRAL, SUBLANG_DEFAULT ),
    (LPTSTR) ErrorMessage,
    0,
    NULL );

  /* Get The Error. */

  SetLastError( 0 );
  }


/************************************************************************
*                                                                       *
*       SystemError:                                                    *
*                                                                       *
*       Returns A Error Message With A Title, Followed By The Last      *
*       System Error Message.                                           *
*                                                                       *
*       Input:                                                          *
*                                                                       *
*       Destination  = Address Of Destination Message.                  *
*       TitleMessage = Address Of Title Error Message.                  *
*                                                                       *
*       Output:                                                         *
*                                                                       *
*       Destination  = Resulting Error Message Text Is Copied To This   *
*                      String.                                          *
*                                                                       *
*       29.Aug.2002 Jamie Lisa Finch.                                   *
*                                                                       *
************************************************************************/

void SystemError( char *Destination, char *TitleMessage ) {

  char *NewError;

  NewError = (char *) NULL;
  LastErrorToString( &NewError );
  strcpy( Destination, TitleMessage );
  strcat( Destination, "\n\nWindows error code: " );
  if( NewError ) {
    strcat( Destination, NewError );
    LocalFree( NewError );
    }
  }


/************************************************************************
*                                                                       *
*       GetTextLength:                                                  *
*                                                                       *
*       Returns The Pixel Length Of A Character String.                 *
*                                                                       *
*       Input:                                                          *
*                                                                       *
*       Buffer    = Address Of Text Buffer.                             *
*       Length    = Length  Of Text Buffer.                             *
*       CharWidth = Font Character Width.                               *
*                                                                       *
*       Output:                                                         *
*                                                                       *
*       Returns -> Length Of Text String In Pixels.                     *
*                                                                       *
*       09.Sept.2002 Jamie Lisa Finch.                                  *
*                                                                       *
************************************************************************/

int GetTextPixelLength( unsigned char *Buffer, int Length, INT *CharWidth ) {

  int TextPixelLength;

  for( TextPixelLength = 0; Length > 0; --Length )
    TextPixelLength += CharWidth[ *Buffer++ - FirstChar ];

  return TextPixelLength;
  }


/************************************************************************
*                                                                       *
*       Courier Font:                                                   *
*                                                                       *
*       Returns The Handle To An N Point Font.                          *
*                                                                       *
*       Input:                                                          *
*                                                                       *
*       Size    = Estimate Of Number Of Points In The Font.             *
*       Bold    = TRUE  == Create A Bold   Font.                        *
*                 FALSE == Create A Normal Font.                        *
*       Output:                                                         *
*                                                                       *
*       Returns -> Handle To Font.                                      *
*                                                                       *
*       10.Oct.2002 Jamie Lisa Finch.                                   *
*                                                                       *
************************************************************************/

static char CourierFontName[] = { "Courier New" };

HFONT Courier_Font( int Size, BOOL Bold ) {

  LOGFONT LogicalFont;

  /* Courier N Font. */

  memset( &LogicalFont, 0, sizeof( LOGFONT ) );
  LogicalFont.lfHeight         = -Size;
  LogicalFont.lfWeight         = ( Bold )? FW_BOLD: FW_NORMAL;
  LogicalFont.lfOutPrecision   = OUT_STROKE_PRECIS;
  LogicalFont.lfClipPrecision  = CLIP_STROKE_PRECIS;
  LogicalFont.lfQuality        = DRAFT_QUALITY;
  LogicalFont.lfPitchAndFamily = FF_SWISS | VARIABLE_PITCH;
  strcpy( LogicalFont.lfFaceName, CourierFontName );

  return CreateFontIndirect( &LogicalFont );
  }


/************************************************************************
*                                                                       *
*       Log:                                                            *
*                                                                       *
*       Writes A Log Message To Output Debug Device.                    *
*                                                                       *
*       Input:                                                          *
*                                                                       *
*       Format  = C String That Works Like A printf.                    *
*       ...     = Arguments Like A printf.                              *
*                                                                       *
*       Output:                                                         *
*                                                                       *
*       String Is Written To Debugging Device.                          *
*                                                                       *
*       04.Sept.2002 Jamie Lisa Finch.                                  *
*                                                                       *
************************************************************************/

void __cdecl Log( char *Format, ... ) {

  va_list va;
  char    Text[ 512 ];

  va_start( va, Format );
  wvsprintfA( Text, Format, va );
  va_end( va );
  OutputDebugString( Text );
  }
