/************************************************************************
*                                                                       *
*       MyChild.c:                                                      *
*                                                                       *
*       27.Sept.2002 Jamie Lisa Finch.                                  *
*                                                                       *
************************************************************************/

#include <windows.h>
#include <windowsx.h>
#include <stdio.h>
#include <fcntl.h>
#include <sys/stat.h>
#include <io.h>
#include <time.h>
#include "..\h\PAcquire.h"
#include "..\h\Messages.h"
#include "..\h\MyChild.h"
#include "..\h\FileRequestor.h"
#include "..\h\Text.h"
#include "..\h\Fnctns.h"
#include "..\h\Configure.h"
#include "..\res\resource.h"


/************************************************************************
*                                                                       *
*       Eprintf:                                                        *
*                                                                       *
*       Changes The New E Print Format Into The old E Print Format.     *
*                                                                       *
*       Input:                                                          *
*                                                                       *
*       String  = Address Of Destination String.                        *
*       Format  = Address Of String With 'E' Format.                    *
*       Number  = Value   Of Number To Make String Of.                  *
*                                                                       *
*       Output:                                                         *
*                                                                       *
*       String  = At This Address The Value Of The String Of Copied.    *
*                                                                       *
*       31.Oct.2002 Jamie Lisa Finch.                                   *
*                                                                       *
************************************************************************/

void Eprintf( char *String, char *Format, double Number ) {

  char *Inc, Temp[ 256 ];

  sprintf( Temp, Format, Number );
  for( Inc = Temp; *Inc != 0; Inc++ ) {
    *String++ = *Inc;
    if( ( *Inc == 'e' ) ||
        ( *Inc == 'E' ) ) break;
    }

  if( *Inc != 0 ) {
    Inc++;                      /* Point Past 'e' */
    if( *Inc != 0 ) {
      *String++ = *Inc++;       /* Get the + or - */
      if( *Inc != 0 ) {

        /* Skip Over the Extra 0. */

        for( Inc++; *Inc != 0; Inc++ )
          *String++ = *Inc;     /* Copy Rest Of String. */
        }
      }
    }
  *String = 0;                  /* Nil Terimanate The String. */
  }


// Functions

BOOL Translate( ACQUIRE *Acquire, HWND hwnd, BOOL bPos, LONG lPos ) { // Moves automatic sample handler translator to flag position, and returns TRUE when finished

  char achCount[ 256 ];
  LONG lCount;
  MSG  Message;
  DWORD dwDelay;

#ifdef SH_HACKOUT
  return TRUE;


#else


  // Determine number of counts to travel

  if( bPos ) // If translator is already at the specified position
    return TRUE;

  if( Acquire->ac_bTHome )      // If translator is at the home position
    lCount = lPos;

  else if( Acquire->ac_bTLoad )	// If translator is at the load position
    lCount = lPos - Acquire->ac_lTLoad;

  else if( Acquire->ac_bTAFX )  // If translator is at the AF X coil position
    lCount = lPos - Acquire->ac_lTAFX;

  else if( Acquire->ac_bTAFY )  // If translator is at the AF Y coil position
    lCount = lPos - Acquire->ac_lTAFY;

  else if( Acquire->ac_bTAFZ )  // If translator is at the AF Z coil position
    lCount = lPos - Acquire->ac_lTAFZ;

  else if( Acquire->ac_bTBack ) // If translator is at the background position
    lCount = lPos - Acquire->ac_lTBack;

  else if( Acquire->ac_bTMeas ) // If translator is at the measurement position
    lCount = lPos - Acquire->ac_lTMeas;

  else if( Acquire->ac_bTRight )// If translator is at the right limit position
    lCount = lPos;

  else if( Acquire->ac_bTLeft ) // If translator is at the left limit position
    lCount = lPos;

  // Select translation axis

  if( !SHSetParameter( hwnd, Acquire->ac_nSHComID, "O1,", "0" ) )       // If selection is not confirmed
    return FALSE;

  if( !SHSetParameter( hwnd, Acquire->ac_nSHComID, "M", Acquire->ac_HandlerDData.achVel ) )  // Set velocity
    return FALSE;

  if( !SHSetParameter( hwnd, Acquire->ac_nSHComID, "A", Acquire->ac_HandlerDData.achAccel ) )  // Set Accel 
    return FALSE;

  if( !SHSetParameter( hwnd, Acquire->ac_nSHComID, "D", Acquire->ac_HandlerDData.achDecel ) )  // Set Decel 
    return FALSE;

  // If current position is one of the limit positions, move to home position first

  if( Acquire->ac_bTRight || Acquire->ac_bTLeft ) {

/*
    if( Acquire->ac_bTRight ) { // For right limit switch
      if( !SHSendCommand( hwnd, Acquire->ac_nSHComID, "-" ) )   // Set motor direction
        return FALSE;
      }
    else {                      // For left limit switch
      if( !SHSendCommand( hwnd, Acquire->ac_nSHComID, "+" ) )   // Set motor direction
        return FALSE;
      }
*/

    if( Acquire->ac_HandlerDData.dwRight ) {    // Right limit -
      if( Acquire->ac_bTRight ) {               // For right limit switch
        if( !SHSendCommand( hwnd, Acquire->ac_nSHComID, "+" ) ) // Set motor direction
          return FALSE;
        }
      else {    // For left limit switch
        if( !SHSendCommand( hwnd, Acquire->ac_nSHComID, "-" ) ) // Set motor direction
          return FALSE;
        }
      }
    else {      // Right limit +
      if( Acquire->ac_bTRight ) {       // For right limit switch
        if( !SHSendCommand( hwnd, Acquire->ac_nSHComID, "-" ) ) // Set motor direction
          return FALSE;
        }
      else {    // For left limit switch
        if( !SHSendCommand( hwnd, Acquire->ac_nSHComID, "+" ) ) // Set motor direction
          return FALSE;
        }
      }

    if( !SHSendCommand( hwnd, Acquire->ac_nSHComID, "H1" ) )    // Seek home sensor
      return FALSE;

    Wait( 110 );
    SHSendCommand( hwnd, Acquire->ac_nSHComID, "F%" );          // Sample handler command
    Wait( 110 );
    while( ( ReadSerial( hwnd, Acquire->ac_nSHComID, Acquire->ac_achSHReply, sizeof( Acquire->ac_achSHReply ) ) < 1 ) ) {
      Wait( 440 );
      }
    }

  // Set motor indexing direction

  if( lCount > 0L ) {   // If movement is to be toward the magnetometer
    if( !SHSendCommand( hwnd, Acquire->ac_nSHComID, "+" ) )     // Set motor direction
      return FALSE;

//  if( bTBack ) {      // If translator is at the background position
//    if( !SHSetParameter( hwnd, nSHComID, "M", HandlerDData.achVelM ) )// Set velocity as measurement
//      return FALSE;
//    }
    }

  if( lCount < 0L ) {   // If movement is to be away from the magnetometer
    if( !SHSendCommand( hwnd, Acquire->ac_nSHComID, "-") )      // Set motor direction
      return FALSE;

    lCount *= -1L;      // Positive value for count
//  if( bTBack ) {      // If translator is at the background position
//    if( !SHSetParameter( hwnd, nSHComID, "M", HandlerDData.achVel ) ) // Set velocity as regular
//      return FALSE;
//    }
    }

  // Set velocity

  if( ( Acquire->ac_bTBack && lPos == Acquire->ac_lTMeas ) || ( Acquire->ac_bTMeas && lPos == Acquire->ac_lTBack ) ) {  // For motion between the background and measurement positions
    if( !SHSetParameter( hwnd, Acquire->ac_nSHComID, "M", Acquire->ac_HandlerDData.achVelM ) )  // Set velocity as measurement
      return FALSE;
    }
  else {        // For all other translations
    if( !SHSetParameter( hwnd, Acquire->ac_nSHComID, "M", Acquire->ac_HandlerDData.achVel  ) )  // Set velocity as regular
      return FALSE;
    }

  // Set number of counts to travel

  wsprintf( achCount, "%ld", lCount );                                  // Convert count value to a string	
  if( !SHSetParameter( hwnd, Acquire->ac_nSHComID, "N", achCount ) )    // If selection is not confirmed
    return FALSE;

  // Send translate command

  if( ( lPos == 0 ) && ( lCount != 0L ) ) {                             // To home position
    if( !SHSendCommand( hwnd, Acquire->ac_nSHComID, "H1" ) )            // Start motor indexing
      return FALSE;
    }

  if( lPos != 0 ) {     // To any other position
    if( !SHSendCommand( hwnd, Acquire->ac_nSHComID, "G" ) )             // Start motor indexing
      return FALSE;
    }

  // Wait for translation to end

  Wait( 110 );
  SHSendCommand( hwnd, Acquire->ac_nSHComID, "F%" );                    // Sample handler command
  Wait( 110 );
  dwDelay = GetCurrentTime();                                           // Get start time
  while( ( ReadSerial( hwnd, Acquire->ac_nSHComID, Acquire->ac_achSHReply, sizeof( Acquire->ac_achSHReply ) ) < 1 ) ) {
    Wait( 440 );

    if( PeekMessage( &Message, hwnd, WM_MOUSEFIRST, WM_MOUSELAST, PM_REMOVE ) ) {       // Look for a mouse click to cancel measurement cycle
      if( Message.message == WM_RBUTTONDOWN ) {                         // Look for a mouse right button down click to continue cancel procedure
        Acquire->ac_bCancelCycle = TRUE;                                // Set cancel measurement cycle flag
/*
        MessageBeep( 0 );
        Acquire->ac_bFilePaint    = TRUE;                               // Set file paint flag
        Acquire->ac_bMeasurePaint = FALSE;                              // Clear measure paint flag
        if( ptCancelDialog ) {                                          // If cancel measurement cycle dialog box still exists
          ptCancelDialog->CloseWindow();                                // Close it
          ptCancelDialog = NULL;                                        // And clear pointer
          }
        InvalidateRect(Acquire->ac_hwndChildInFocus, NULL, TRUE );      // Generate a WM_PAINT message
        UpdateWindow(  Acquire->ac_hwndChildInFocus );
*/
        break;
        }
      }

    else if( GetCurrentTime() > ( dwDelay + ( SAMPLERHANDLERTIMEOUT * 1000 ) ) ) {
      Acquire->ac_bCancelCycle = TRUE;                                  // Set cancel measurement cycle flag
      break;
      }
    }

  // Clear position flags

  Acquire->ac_bTHome  = FALSE;
  Acquire->ac_bTLoad  = FALSE;
  Acquire->ac_bTAFX   = FALSE;
  Acquire->ac_bTAFY   = FALSE;
  Acquire->ac_bTAFZ   = FALSE;
  Acquire->ac_bTBack  = FALSE;
  Acquire->ac_bTMeas  = FALSE;
  Acquire->ac_bTRight = FALSE;
  Acquire->ac_bTLeft  = FALSE;

  return TRUE;
#endif
  }


BOOL Rotate( ACQUIRE *Acquire, HWND hwnd, BOOL bPos, int nPos) { // Moves automatic sample handler rotator to flag position, and returns TRUE when finished

  char achCount[ 256 ];
  int  nCount;
  MSG  Message;
  DWORD dwDelay;

// Determine number of counts to travel

#ifdef SH_HACKOUT
  return TRUE; 

#else

  if( bPos )                    // If translator is already at the specified position
    return TRUE;

  nCount = 0;
  if( Acquire->ac_bRHome )      // If rotator is at the home or 0 degree position
    nCount = nPos;
  else if( Acquire->ac_bR90 )   // If rotator is at the 90 degree position
    nCount = nPos - Acquire->ac_nR90;
  else if( Acquire->ac_bR180 )  // If rotator is at the 180 degree position
    nCount = nPos - Acquire->ac_nR180;
  else if( Acquire->ac_bR270 )  // If rotator is at the 270 degree position
    nCount = nPos - Acquire->ac_nR270;

//if( nCount > 1000 )           // The largest single rotation is limited to 180 degrees
//  nCount -= 2000;
//if( nCount < -1000 )          // The largest single rotation is limited to 180 degrees
//  nCount += 2000;

  // Select rotation axis

  if( !SHSetParameter( hwnd, Acquire->ac_nSHComID, "O1,", "1" ) )       // If selection is not confirmed
    return FALSE;

  if( !SHSetParameter( hwnd, Acquire->ac_nSHComID, "M", Acquire->ac_HandlerDData.achRotVel ) )  // Set velocity
    return FALSE;

  if( !SHSetParameter( hwnd, Acquire->ac_nSHComID, "A", Acquire->ac_HandlerDData.achRotAccel ) )  // Set Accel 
    return FALSE;

  if( !SHSetParameter( hwnd, Acquire->ac_nSHComID, "D", Acquire->ac_HandlerDData.achRotDecel ) )  // Set Decel 
    return FALSE;

  // Set motor indexing direction

  if( nCount < 0 ) {            // If rotation is to be counterclockwise
    if( !SHSendCommand( hwnd, Acquire->ac_nSHComID, "-" ) )             // If command is not sent successfully
      return FALSE;
    nCount *= -1;               // Positive value for count
    }
  else {                        // If rotation is to be clockwise
    if( !SHSendCommand( hwnd, Acquire->ac_nSHComID, "+" ) )             // If command is not sent successfully
      return FALSE;
    }

  // Set number of counts to travel

  if( nPos == 0 ) {             // If the 0 degree position is the destination
    if( !SHSendCommand( hwnd, Acquire->ac_nSHComID, "+H1" ) )          // Seek the home sensor to eliminate creep accumulation
      return FALSE;
    }
  else {                        // For other destinations
    wsprintf( achCount, "%d", nCount );                                 // Convert count value to a string	
    if( !SHSetParameter( hwnd, Acquire->ac_nSHComID, "N", achCount ) )  // If selection is not confirmed
      return FALSE;
    if( !SHSendCommand( hwnd, Acquire->ac_nSHComID, "G" ) )             // Start rotation
    return FALSE;
    }

  // Wait for rotation to end

  Wait( 110 );
  SHSendCommand( hwnd, Acquire->ac_nSHComID, "F%" );                    // Sample handler command
  Wait( 110 );
  dwDelay = GetCurrentTime();                                           // Get start time
  while( ( ReadSerial(hwnd, Acquire->ac_nSHComID, Acquire->ac_achSHReply, sizeof( Acquire->ac_achSHReply ) ) < 1 ) ) {
    Wait( 440 );

    if( PeekMessage( &Message, hwnd, WM_MOUSEFIRST, WM_MOUSELAST, PM_REMOVE ) ) {       // Look for a mouse click to cancel measurement cycle
      if( Message.message == WM_RBUTTONDOWN ) {                         // Look for a mouse right button down click to continue cancel procedure
        Acquire->ac_bCancelCycle = TRUE;                                // Set cancel measurement cycle flag
/*
        MessageBeep( 0 );
        Acquire->ac_bFilePaint   = TRUE;                                // Set file paint flag
        Acquire->ac_bMeasurePaint = FALSE;                              // Clear measure paint flag
        if( ptCancelDialog ) {                                          // If cancel measurement cycle dialog box still exists
          ptCancelDialog->CloseWindow();                                // Close it
          ptCancelDialog = NULL;                                        // And clear pointer
          }
        InvalidateRect(Acquire->ac_hwndChildInFocus, NULL, TRUE );      // Generate a WM_PAINT message
        UpdateWindow(  Acquire->ac_hwndChildInFocus );
*/
        break;
        }
      }

    else if( GetCurrentTime() > ( dwDelay + ( SAMPLERHANDLERTIMEOUT * 1000 ) ) ) {
      Acquire->ac_bCancelCycle = TRUE;                                  // Set cancel measurement cycle flag
      break;
      }
    }

  // Clear position flags

  Acquire->ac_bRHome = FALSE;
  Acquire->ac_bR90   = FALSE;
  Acquire->ac_bR180  = FALSE;
  Acquire->ac_bR270  = FALSE;

  return TRUE;
#endif
  }


// Increases range setting for specified magnetometer axis, and returns TRUE if successful

BOOL MagRangeUp( ACQUIRE *Acquire, HWND hwnd, SERIALIO *nComID, char achAxis[] ) {

  // Increment the global range variable for the appropriate magnetometer axis

  if( strstr(achAxis, "X" ) != NULL ) {                         // For the X axis
    if( !Acquire->ac_bXFlux ) {                                 // For ranges other than flux counting
      Acquire->ac_MagnetometerDData.dwXRange += 1UL;
      if( Acquire->ac_MagnetometerDData.dwXRange > 3UL ) {      // From extended range, set flux counting
        Acquire->ac_MagnetometerDData.dwXRange = 0UL;
        Acquire->ac_bXFlux = TRUE;                              // Set global flux counting flag for the axis
        }
      MagSetRange( hwnd, nComID, achAxis, Acquire->ac_MagnetometerDData.dwXRange );     // Set new range
      return TRUE;
      }
    else {      // If current range is flux counting, sample is too strong to measure
      MessageBox( hwnd, "Sample Too Strong To Measure", "Range Up Error", MB_ICONHAND | MB_OK );        // Notify user
      return FALSE;
      }
    }

  else if( strstr( achAxis, "Y" ) != NULL ) {                   // For the Y axis
    if( !Acquire->ac_bYFlux ) {                                 // For ranges other than flux counting
      Acquire->ac_MagnetometerDData.dwYRange += 1UL;
      if( Acquire->ac_MagnetometerDData.dwYRange > 3UL ) {      // From extended range, set flux counting
        Acquire->ac_MagnetometerDData.dwYRange = 0UL;
        Acquire->ac_bYFlux = TRUE;                              // Set global flux counting flag for the axis
        }
      MagSetRange( hwnd, nComID, achAxis, Acquire->ac_MagnetometerDData.dwYRange );     // Set new range
      return TRUE;
      }
    else {      // If current range is flux counting, sample is too strong to measure
      MessageBox( hwnd, "Sample Too Strong To Measure", "Range Up Error", MB_ICONHAND | MB_OK );        // Notify user
      return FALSE;
      }
    }

  else if( strstr(achAxis, "Z" ) != NULL ) {                    // For the Z axis
    if( !Acquire->ac_bZFlux ) {                                 // For ranges other than flux counting
      Acquire->ac_MagnetometerDData.dwZRange += 1UL;
      if( Acquire->ac_MagnetometerDData.dwZRange > 3UL ) {      // From extended range, set flux counting
        Acquire->ac_MagnetometerDData.dwZRange = 0UL;
        Acquire->ac_bZFlux = TRUE;                              // Set global flux counting flag for the axis
        }
      MagSetRange( hwnd, nComID, achAxis, Acquire->ac_MagnetometerDData.dwZRange );     // Set new range
      return TRUE;
      }
    else {      // If current range is flux counting, sample is too strong to measure
      MessageBox( hwnd, "Sample Too Strong To Measure", "Range Up Error", MB_ICONHAND | MB_OK );        // Notify user
      return FALSE;
      }
    }
  return FALSE;
  }


/************************************************************************
*                                                                       *
*       CopyTheFile:                                                    *
*                                                                       *
************************************************************************/

BOOL CopyTheFile( char *Dest, char *Sorc ) {

  BOOL Return;
  int In, Out;
  char NewChar;

  Return = FALSE;
  if( ( Dest != (char *) NULL ) &&
      ( Sorc != (char *) NULL ) ) {

    SetFileAttributes( Sorc, FILE_ATTRIBUTE_NORMAL );

    if( ( In = _open( Sorc, _O_RDONLY | _O_BINARY  ) ) != -1 ) {
//    if( ( Out = _open( Dest, _O_WRONLY | _O_CREAT | _O_TEXT, _S_IWRITE ) ) != - 1 ) {
      if( ( Out = _open( Dest, _O_WRONLY | _O_CREAT | _O_BINARY |  _O_TRUNC , _S_IREAD | _S_IWRITE ) ) != - 1 ) {
        while( _read( In, &NewChar, 1 ) == 1 ) {
          _write( Out, &NewChar, 1 );
          }
        _close( Out );
        Return = TRUE;
        }
      _close( In );
      }
    }
  return Return;
  }


/************************************************************************
*                                                                       *
*       MoveWindowToFront:                                              *
*                                                                       *
*       Makes The Window The Foreground Window.                         *
*                                                                       *
*       Input:                                                          *
*                                                                       *
*       hWindow  = Handle To Window.                                    *
*       nCmdShow = Value Of Show Command.                               *
*                                                                       *
*       Output:                                                         *
*                                                                       *
*       Returns -> TRUE  == Execution Without Error.                    *
*                  FALSE == Failed To Make The Foreground Window.       *
*                                                                       *
*       12.Sept.2002 Jamie Lisa Finch.                                  *
*                                                                       *
************************************************************************/

BOOL MoveWindowToFront( HWND hWindow, int nCmdShow ) {

  if( hWindow != (HWND) NULL ) {

    #ifndef SPI_SETFOREGROUNDLOCKTIMEOUT
    #define SPI_SETFOREGROUNDLOCKTIMEOUT      0x2001
    #endif

    ShowWindow( hWindow, nCmdShow );
    SystemParametersInfo( SPI_SETFOREGROUNDLOCKTIMEOUT, 0, (VOID *) 0, SPIF_SENDWININICHANGE | SPIF_UPDATEINIFILE );
    SetForegroundWindow( hWindow );
    return TRUE;
    }
  return FALSE;
  }


/************************************************************************
*                                                                       *
*       GetWindowBorders:                                               *
*                                                                       *
*       Finds Out The Size Of The Child Window Borders.                 *
*                                                                       *
*       Input:                                                          *
*                                                                       *
*       hWindow = Handle  To Window To Get Borders.                     *
*       Border  = Address Of Windows Border Structure.                  *
*                                                                       *
*       Output:                                                         *
*                                                                       *
*       Returns -> TRUE  == Execution Without Error.                    *
*                  FALSE == Failed To Get Window Borders.               *
*                                                                       *
*       03.Sept.2002 Jamie Lisa Finch.                                  *
*                                                                       *
************************************************************************/

BOOL GetWindowBorders( HWND hWindow, RECT *Border ) {

  WINDOWINFO WindowInfo;

  if( ( hWindow != (HWND)   NULL ) &&
      ( Border  != (RECT *) NULL ) ) {

    memset( &WindowInfo, 0, sizeof( WindowInfo ) );
    WindowInfo.cbSize = sizeof( WindowInfo );

    if( GetWindowInfo( hWindow, &WindowInfo ) ) {

      Border->left   = WindowInfo.rcClient.left   - WindowInfo.rcWindow.left;
      Border->top    = WindowInfo.rcClient.top    - WindowInfo.rcWindow.top;
      Border->right  = WindowInfo.rcWindow.right  - WindowInfo.rcClient.right;
      Border->bottom = WindowInfo.rcWindow.bottom - WindowInfo.rcClient.bottom;
      return TRUE;
      }
    }
  return FALSE;
  }


// TMyMDIChild Class Member

void TMyMDIChildSetupWindow( HWND hWindow ) {

  // Set scroll bar range

  SetScrollRange( hWindow, SB_HORZ, HSBPMIN, HSBPMAX, FALSE );  // Horizontal
  SetScrollRange( hWindow, SB_VERT, VSBPMIN, VSBPMAX, FALSE );  // Vertical
  }


BOOL TMyMDIChildCanClose( ACQUIRE *Acquire, HWND hWindow ) {

//if( !Child->bFileSaved ) {                    // If sample file is not current
//  if( MessageBox( hWindow, "File is not current!\rSave before closing?", Child->achTitleStr, MB_ICONEXCLAMATION | MB_YESNO ) == IDYES )
    SendMessage( hWindow, UM_SAVE, 0, 0 );      // Send a save message directly to the message function
//  }
  return TRUE;
  }


void TMyMDIChildWMDestroy( ACQUIRE *Acquire, HWND hWindow ) {

  if( Acquire != (ACQUIRE *) NULL ) {
    Acquire->ac_nChildrenCount -= 1;            // As window closes, decrement window count
    if( Acquire->ac_nChildrenCount <= 0 ) {     // As the last child window closes, disable applicable menu items
      EnableMenuItem( Acquire->ac_hMainMenu, IDM_FILE_SAVE,        MF_GRAYED | MF_BYCOMMAND );  // File Save
      EnableMenuItem( Acquire->ac_hMainMenu, IDM_FILE_SAVEAS,      MF_GRAYED | MF_BYCOMMAND );  // File Save as
      EnableMenuItem( Acquire->ac_hMainMenu, IDM_FILE_PRINT,       MF_GRAYED | MF_BYCOMMAND );  // File Print
      EnableMenuItem( Acquire->ac_hMainMenu, IDM_MEASURE_SINGLE,   MF_GRAYED | MF_BYCOMMAND );  // Measure Single step
      EnableMenuItem( Acquire->ac_hMainMenu, IDM_MEASURE_SEQUENCE, MF_GRAYED | MF_BYCOMMAND );  // Measure Sequence
      EnableMenuItem( Acquire->ac_hMainMenu, IDM_MEASURE_HOLDER,   MF_GRAYED | MF_BYCOMMAND );  // Measure Sample holder
      EnableMenuItem( Acquire->ac_hMainMenu, IDM_MEASURE_STANDARD, MF_GRAYED | MF_BYCOMMAND );  // Measure Standard
//    EnableMenuItem( Acquire->ac_hMainMenu, IDM_PROJECT_CART,     MF_GRAYED | MF_BYCOMMAND );  // Projection Cartesian
      }
    }
  }


// TMyMDIChild Class Member

void TMyMDIChildWMPaint( ACQUIRE *Acquire, HWND hWindow ) {

  PAINTSTRUCT ps;

  if( Acquire != (ACQUIRE *) NULL ) {
    if( hWindow == Acquire->ac_hwndChildInFocus )               // Only paint the child window with input focus
      SendMessage( Acquire->ac_hMainWindow, UM_TEXT, 0U, 0 );   // Send a text message directly to the message function
    else {
      BeginPaint( hWindow, &ps );
      EndPaint(   hWindow, &ps );
      }
    }
  }


void TMyMDIChildWMMDIActivate( ACQUIRE *Acquire, HWND hWindow ) {

  char achTitleStr[ MAXFILENAME ];

  if( ( Acquire != (ACQUIRE *) NULL ) &&
      ( hWindow != (HWND)      NULL ) ) {

    GetWindowText( (HWND) GetWindowLong( hWindow, GWL_HWNDParent ), achTitleStr, sizeof( achTitleStr ) );

    /* Keep track of the active window name for doing hard flush on the dat/tmp file. */
    strcpy( Acquire->ac_achTitleStr, achTitleStr );

    Acquire->ac_nActivateCycle += 1;                              // Increment the activate cycle counter

    if( Acquire->ac_nChildrenCount < 1 ) {                        // If no child windows yet exist
      if( Acquire->ac_bFileNew ) {                                // No existing sample file
        Acquire->ac_bFileNew = FALSE;                             // Clear new file flag
        Acquire->ac_FocusFile->Delete( Acquire->ac_FocusFile );   // Delete any previous focus.tmp disk file
        Acquire->ac_FocusFile->Save(   Acquire->ac_FocusFile, "-1", sizeof( SPECIMENSUMMARY ) );                            // Save specimen summary record number to disk
        Acquire->ac_FocusFile->CopyTo( Acquire->ac_FocusFile, (LPSTR) &Acquire->ac_FileSummary, sizeof( SPECIMENSUMMARY ) );// Save specimen summary data to disk
        Acquire->ac_FocusFile->Save(   Acquire->ac_FocusFile, "0", sizeof( INFO ) );                                        // Save sample information record number to disk
        Acquire->ac_FocusFile->CopyTo( Acquire->ac_FocusFile, (LPSTR) &Acquire->ac_SampleDData, sizeof( INFO ) );           // Save sample information data to disk
        }
      else {                                                      // Sample file already exists
        Acquire->ac_bFileOpen = FALSE;                            // Clear open file flag
        Acquire->ac_FocusFile->CloseIt( Acquire->ac_FocusFile );  // Delete any previous focus.tmp disk file
        CopyTheFile( Acquire->ac_achFocusFile, achTitleStr );
        }
      Acquire->ac_hwndChildInFocus = hWindow;                     // Update handle to child window with input focus
      Acquire->ac_nActivateCycle   = 0;                           // Reset the activate cycle counter
      }

    else {                                                        // If other child windows do exist
      if( Acquire->ac_nActivateCycle == 1 ) {                     // On the first activate cycle, save the file associated with the window which is losing focus
        Log( "Window %s is losing Focus.\n", achTitleStr );
        Acquire->ac_FocusFile->CloseIt( Acquire->ac_FocusFile );  // Close the focus.tmp disk file
        CopyTheFile( achTitleStr, Acquire->ac_achFocusFile );
        }
      if( Acquire->ac_nActivateCycle == 2 ) {                     // On the second activate cycle, copy the file associated with the window which is coming into focus
        Log( "Window %s is getting Focus.\n", achTitleStr );
        if( Acquire->ac_bFileNew ) {                              // No existing sample file
          Log( "%s is a New File\n", achTitleStr );
          Acquire->ac_bFileNew = FALSE;                           // Clear new file flag
          Acquire->ac_FocusFile->Delete( Acquire->ac_FocusFile ); // Delete any previous focus.tmp disk file
          Acquire->ac_FocusFile->Save(   Acquire->ac_FocusFile, "-1", sizeof( SPECIMENSUMMARY ) );                                // Save specimen summary record number to disk
          Acquire->ac_FocusFile->CopyTo( Acquire->ac_FocusFile, (LPSTR) &Acquire->ac_FileSummary, sizeof( SPECIMENSUMMARY ) );    // Save specimen summary data to disk
          Acquire->ac_FocusFile->Save(   Acquire->ac_FocusFile, "0", sizeof( INFO ) );                                            // Save sample information record number to disk
          Acquire->ac_FocusFile->CopyTo( Acquire->ac_FocusFile, (LPSTR) &Acquire->ac_SampleDData, sizeof( INFO ) );               // Save sample information data to disk
          }
        else {                                                    // Either sample file exists to open, or sample file name is the child window's caption
          Acquire->ac_FocusFile->CloseIt( Acquire->ac_FocusFile );// Delete any previous focus.tmp disk file
          //Shouldn't the old Focus File be deleted?   //Mabe the CopyTheFile doesn't remove the org file?
          CopyTheFile( Acquire->ac_achFocusFile, achTitleStr );
          if( Acquire->ac_bFileOpen )                             // Sample file exists
            Acquire->ac_bFileOpen = FALSE;                        // Clear open file flag
          else {                                                  // Sample file name is the child window's caption
            Acquire->ac_FocusFile->Load(     Acquire->ac_FocusFile, "-1" );                                                       // Load the specimen summary record
            Acquire->ac_FocusFile->CopyFrom( Acquire->ac_FocusFile, (LPSTR) &Acquire->ac_FileSummary, sizeof( SPECIMENSUMMARY ) );// Read in specimen summary data
            Acquire->ac_FocusFile->Load(     Acquire->ac_FocusFile, "0" );                                                        // Load the sample information record
            Acquire->ac_FocusFile->CopyFrom( Acquire->ac_FocusFile, (LPSTR) &Acquire->ac_SampleDData, sizeof( INFO ) );           // Read in sample information data
            }
          }
        Acquire->ac_hwndChildInFocus = hWindow;                   // Update handle to child window with input focus
        InvalidateRect( hWindow, NULL, TRUE );                    // Redraw child window as it regains input focus
        UpdateWindow( hWindow );
        Acquire->ac_nActivateCycle = 0;                           // Reset the activate cycle counter
        }
      }
    }
  }


// TMyMDIChild Class Member

void TMyMDIChildWMHScroll( ACQUIRE *Acquire, HWND hWindow, WPARAM wParam ) {

  int nHScroll;

  if( ( Acquire != (ACQUIRE *) NULL ) &&
      ( hWindow != (HWND)      NULL ) ) {

    nHScroll = GetWindowLong( hWindow, GWL_nHScroll );

    switch( LOWORD( wParam ) ) {        // Message generated by scroll bar action
//    case SB_BOTTOM:                   // Ignore
//      break;
      case SB_LINEDOWN:                 // Scroll to the right message
        if( nHScroll < HSBPMAX )
          nHScroll += 1;                // Increment position
        break;
      case SB_LINEUP:                   // Scroll to the left message
        if( nHScroll > HSBPMIN )
          nHScroll -= 1;                // Decrement position
        break;
      case SB_PAGEDOWN:                 // Page to the right
        if( nHScroll < ( HSBPMAX - 50 ) )
             nHScroll += 50;            // Increment position
        else nHScroll = HSBPMAX;        // Maximum position
        break;
      case SB_PAGEUP:                   // Page to the left
        if( nHScroll > ( HSBPMIN + 50 ) )
             nHScroll -= 50;            // Decrement position
        else nHScroll  = HSBPMIN;       // Minimum position
        break;
      case SB_THUMBPOSITION:            // Move thumb message
        nHScroll = HIWORD( wParam );    // Get position of thumb within the scroll bar
        break;
//    case SB_TOP:                      // Ignore
//      break;
      default:                          // Intercepts button release messages
        return;
      }
    }

  SetWindowLong( hWindow, GWL_nHScroll, nHScroll );
  InvalidateRect(hWindow, NULL, TRUE ); // Generate a WM_PAINT message
  UpdateWindow(  hWindow );
  SetScrollPos(  hWindow, SB_HORZ, nHScroll, TRUE );    // Set scroll bar thumb position
  }


// TMyMDIChild Class Member

void TMyMDIChildWMVScroll( ACQUIRE *Acquire, HWND hWindow, WPARAM wParam ) {

  int nVScroll;

  if( ( Acquire != (ACQUIRE *) NULL ) &&
      ( hWindow != (HWND)      NULL ) ) {

    nVScroll = GetWindowLong( hWindow, GWL_nVScroll );

    switch( LOWORD( wParam ) ) {        // Message generated by scroll bar action
//    case SB_BOTTOM:                   // Ignore
//      break;
      case SB_LINEDOWN:                 // Scroll to the right message
        if( nVScroll < VSBPMAX )
          nVScroll += 1;                // Increment position
          break;
      case SB_LINEUP:                   // Scroll to the left message
        if( nVScroll > VSBPMIN )
          nVScroll -= 1;                // Decrement position
        break;
      case SB_PAGEDOWN:                 // Page to the right
        if( nVScroll < ( VSBPMAX - 10 ) )
             nVScroll += 10;            // Increment position
        else nVScroll  = VSBPMAX;       // Maximum position
        break;
      case SB_PAGEUP:                   // Page to the left
        if( nVScroll > ( VSBPMIN + 10 ) )
             nVScroll -= 10;            // Decrement position
        else nVScroll  = VSBPMIN;       // Minimum position
        break;
      case SB_THUMBPOSITION:            // Move thumb message
        nVScroll = HIWORD( wParam );    // Get position of thumb within the scroll bar
        break;
//    case SB_TOP:                      // Ignore
//      break;
      default:                          // Intercepts button release messages
        return;
      }
    }
  SetWindowLong( hWindow, GWL_nVScroll, nVScroll );
  InvalidateRect(hWindow, NULL, TRUE );	// Generate a WM_PAINT message
  UpdateWindow(  hWindow );
  SetScrollPos(  hWindow, SB_VERT, nVScroll, TRUE );    // Set scroll bar thumb position
  }


// TMyMDIChild Class Member - Saves a file

void TMyMDIChildWMSave( ACQUIRE *Acquire, HWND hWindow ){

  char achTitleStr[ MAXFILENAME ];

  if( ( Acquire != (ACQUIRE *) NULL ) &&
      ( hWindow != (HWND)      NULL ) ) {

    GetWindowText( (HWND) GetWindowLong( hWindow, GWL_HWNDParent ), achTitleStr, sizeof( achTitleStr ) );

    Acquire->ac_FocusFile->CloseIt( Acquire->ac_FocusFile );      // Close the focus.tmp disk file
    CopyTheFile( achTitleStr, Acquire->ac_achFocusFile );
//  bFileSaved = TRUE;                                            // Sample file is current
    }
  }


// TMyMDIChild Class Member - Posts a message to save a file

void TMyMDIChildCMSave( HWND hWindow ) {

  SendMessage( hWindow, UM_SAVE, 0, 0 );  // Send a save message directly to the message function
  }


// TMyMDIChild Class Member - Save a file with a different name

void TMyMDIChildCMSaveAs( ACQUIRE *Acquire, HWND hWindow ) {

  int   nRetVal;
  char  achFileSaveAs[ MAXFILENAME ];
  BOOL  bOverWrite = TRUE;

  char achTitleStr[MAXFILENAME ];

  if( ( Acquire != (ACQUIRE *) NULL ) &&
      ( hWindow != (HWND)      NULL ) ) {

    GetWindowText( (HWND) GetWindowLong( hWindow, GWL_HWNDParent ), achTitleStr, sizeof( achTitleStr ) );

    strcpy( achFileSaveAs, achTitleStr );                                       // Provide a default file name and path
    nRetVal = GetFileRequestorSave( achFileSaveAs, "dat", "Data Files" );       // Display the dialog box

    // Respond to user input

    if( nRetVal == IDOK ) {                                                     // OK button pushed
      OemToAnsi( achFileSaveAs, achFileSaveAs );                                // Convert the save as file name string from oem to ansi characters
      Acquire->ac_FocusFile->CloseIt( Acquire->ac_FocusFile );                  // Close file focus.tmp
      Acquire->ac_FocusFile->ChangeName( Acquire->ac_FocusFile, achFileSaveAs );// Update the name of the file to that just chosen
      if( Acquire->ac_FocusFile->Exists( Acquire->ac_FocusFile ) ) {            // If a file in the current directory already exists by that name
        if( MessageBox( hWindow, "File exists!\rOverwrite?", "File Save As", MB_ICONEXCLAMATION | MB_YESNO ) == IDNO )
          bOverWrite = FALSE;
        }
      if( bOverWrite ) {                                          // If file is to be saved
        CopyTheFile( achFileSaveAs, Acquire->ac_achFocusFile );
        strcpy( achTitleStr, achFileSaveAs );              // Update caption
        SetWindowText( (HWND) GetWindowLong( hWindow, GWL_HWNDParent ), achTitleStr );
//      bFileSaved = TRUE;                                        // Sample file is current 
        }
//    Acquire->ac_FocusFile->ChangeName( Acquire->ac_FocusFile, "focus.tmp"  );            // Restore the name of the disk file
      Acquire->ac_FocusFile->ChangeName( Acquire->ac_FocusFile, Acquire->ac_achFocusFile );// Restore the name of the disk file
      }
    }
  }


// TMyMDIChild Class Member - Demagnetize a sample

void TMyMDIChildWMDemagnetize( ACQUIRE *Acquire, HWND hWindow ) {

  if( ( Acquire != (ACQUIRE *) NULL ) &&
      ( hWindow != (HWND)      NULL ) ) {

    // Demagnetization

    if( !Acquire->ac_bCancelCycle ) {                                                   // If cycle has not been canceled

      // Move sample handler to AF Z axis position

      if( Acquire->ac_bAFZAxis ) {                                                      // Automatic AF demagnetizer Z axis
        if( Acquire->ac_bSHAuto ) {                                                     // Automatic
          if( Acquire->ac_bSHTrans )                                                    // Automatic translation
            Acquire->ac_bTAFZ = Translate( Acquire, hWindow, Acquire->ac_bTAFZ, Acquire->ac_lTAFZ );         // Translate to AF Z axis position
          else
            Acquire->ac_bCancelCycle = MyTranslateMessage( hWindow, "AF Z Axis" );
          }
        // Manual
        else 
          Acquire->ac_bCancelCycle = MyTranslateMessage( hWindow, "AF Z Axis" );
        DegaussCycle( hWindow, Acquire->ac_nAFComID, "Z", Acquire->ac_dAFLevel );       // Demagnetize
        }

      // Move sample handler to AF X axis position

      if( Acquire->ac_bAFXAxis ) {                                                      // Automatic AF demagnetizer X axis
        if( Acquire->ac_bSHAuto ) {                                                     // Automatic
         if( Acquire->ac_bSHTrans )                                                     // Automatic translation
            Acquire->ac_bTAFX        = Translate( Acquire, hWindow, Acquire->ac_bTAFX, Acquire->ac_lTAFX );  // Translate to AF X axis position
          else
            Acquire->ac_bCancelCycle = MyTranslateMessage( hWindow, "AF X Axis");
          }
        else  // Manual
          Acquire->ac_bCancelCycle = MyTranslateMessage( hWindow, "AF X Axis" );
        DegaussCycle( hWindow, Acquire->ac_nAFComID, "X", Acquire->ac_dAFLevel );       // Demagnetize
        
        // If there is not an AF Y Axis
        if( !Acquire->ac_bAFYAxis ) {                                                   
          // Rotate sample handler by 90
          if( Acquire->ac_bSHAuto ) {                                                   // Automatic
            if( Acquire->ac_bSHRot )                                                    // Automatic rotation
              Acquire->ac_bR90 = Rotate( Acquire, hWindow, Acquire->ac_bR90, Acquire->ac_nR90 );             // Rotate to 90 position
            else
              Acquire->ac_bCancelCycle = RotateMessage( hWindow, "90" );
            }
          // Manual
          else Acquire->ac_bCancelCycle = RotateMessage( hWindow, "90" );
          DegaussCycle( hWindow, Acquire->ac_nAFComID, "X", Acquire->ac_dAFLevel );     // Demagnetize
          }
        }

      // Move sample handler to AF Y axis position

      if( Acquire->ac_bAFYAxis ) {                                                      // Automatic AF demagnetizer Y axis
        if( Acquire->ac_bSHAuto ) {                                                     // Automatic
          if( Acquire->ac_bSHTrans )                                                    // Automatic translation
            Acquire->ac_bTAFY        = Translate( Acquire, hWindow, Acquire->ac_bTAFY, Acquire->ac_lTAFY );  // Translate to AF Y axis position
          else
            Acquire->ac_bCancelCycle = MyTranslateMessage( hWindow, "AF Y Axis" );
          }
        else  // Manual
          Acquire->ac_bCancelCycle = MyTranslateMessage( hWindow, "AF Y Axis");
        DegaussCycle( hWindow, Acquire->ac_nAFComID, "Y", Acquire->ac_dAFLevel );       // Demagnetize

        // If there is not an AF X Axis
        if( !Acquire->ac_bAFXAxis ) {                                                   
          // Rotate sample handler by 90
          if( Acquire->ac_bSHAuto ) {                                                   // Automatic
            if( Acquire->ac_bSHRot )                                                    // Automatic rotation
              Acquire->ac_bR90 = Rotate( Acquire, hWindow, Acquire->ac_bR90, Acquire->ac_nR90 );             // Rotate to 90 position
            else 
              Acquire->ac_bCancelCycle = RotateMessage( hWindow, "90" );
            }
          else // Manual
            Acquire->ac_bCancelCycle = RotateMessage( hWindow, "90" );
          DegaussCycle( hWindow, Acquire->ac_nAFComID, "Y", Acquire->ac_dAFLevel );     // Demagnetize
          }
        }
      }
    }
  }


// TMyMDIChild Class Member - Measure a sample

void TMyMDIChildWMMeasure( ACQUIRE *Acquire, HWND hWindow ) {

  int    i, iRotation, iTemporary, nXCount, nYCount, nZCount;
  double dXAnalog, dYAnalog, dZAnalog;
  BOOL   bMeasure = TRUE;
//char achM[ 15 ];

  if( ( Acquire != (ACQUIRE *) NULL ) &&
      ( hWindow != (HWND)      NULL ) ) {


    Log( "-- TMyMDIChildWMMeasure\n" );

    i = iRotation = iTemporary = nXCount = nYCount = nZCount = 0;  // Jamie
    dXAnalog = dYAnalog = dZAnalog = 0;

    // Clear automatic magnetometer uprange flag

    Acquire->ac_bUpRange = FALSE;

    // Measure background before sample measurement

    if( !Acquire->ac_bCancelCycle ) {                                   // If cycle has not been canceled

      // Zero background measurements array and drift array

      for( i = 0; i < 6; i++ )
        Acquire->ac_adBGSignal[ i ] = 0.0;
      for( i = 0; i < 3; i++ )
        Acquire->ac_adDrift[ i ] = 0.0;

      // Move sample handler to background position

      if( Acquire->ac_bSHAuto ) {                                                                               // Automatic
        if( Acquire->ac_bSHTrans )                                                                              // Automatic translation
             Acquire->ac_bTBack       = Translate( Acquire, hWindow, Acquire->ac_bTBack, Acquire->ac_lTBack );  // Translate to background position
        else Acquire->ac_bCancelCycle = MyTranslateMessage( hWindow, "Background");
        if( Acquire->ac_bSHRot )                                                                                // Automatic rotation
             Acquire->ac_bRHome       = Rotate( Acquire, hWindow, Acquire->ac_bRHome, Acquire->ac_nRHome );     // Rotate to 0 position
        else Acquire->ac_bCancelCycle = RotateMessage( hWindow, "0" );
        }
      else {// Manual
        Acquire->ac_bCancelCycle = MyTranslateMessage( hWindow, "Background");
        Acquire->ac_bCancelCycle = RotateMessage( hWindow, "0" );
        }

      // Measure background

      MagPulseLoop( hWindow, Acquire->ac_nMagComID, "A" );              // Begin by pulsing feedback loop for each axis
      MagResetCount(hWindow, Acquire->ac_nMagComID, "A" );              // And by clearing flux counter for each axis
      Wait( Acquire->ac_dwSettlingDelay );                              // Wait for magnetometer to settle

      // Latch all counters

      MagLatchCount( hWindow, Acquire->ac_nMagComID, "A" );

      // Read counters

      if( Acquire->ac_bMagXAxis )                                       // Magnetometer X axis
        nXCount = MagSendCount( hWindow, Acquire->ac_nMagComID, "X" );  // Send count
      if( Acquire->ac_bMagYAxis )                                       // Magnetometer Y axis
        nYCount = MagSendCount( hWindow, Acquire->ac_nMagComID, "Y" );  // Send count
      if( Acquire->ac_bMagZAxis )                                       // Magnetometer Z axis
        nZCount = MagSendCount( hWindow, Acquire->ac_nMagComID, "Z" );  // Send count

      // Clear analog variables

      dXAnalog = 0.0;     // X axis data
      dYAnalog = 0.0;     // Y axis data
      dZAnalog = 0.0;     // Z axis data

      // Analog readings average

      for( i = 0; i < Acquire->ac_nXYZAxisReadings; i++ ) {             // Multiple analog readings
        MagLatchData( hWindow, Acquire->ac_nMagComID, "A" );            // Latch analog data
        if( Acquire->ac_bMagXAxis )                                     // Magnetometer X axis
          dXAnalog += MagSendData( hWindow, Acquire->ac_nMagComID, "X" );//Send analog data and accumulate
        if( Acquire->ac_bMagYAxis )                                     // Magnetometer Y axis
          dYAnalog += MagSendData( hWindow, Acquire->ac_nMagComID, "Y" );//Send analog data and accumulate
        if( Acquire->ac_bMagZAxis )                                     // Magnetometer Z axis
          dZAnalog += MagSendData( hWindow, Acquire->ac_nMagComID, "Z" );//Send analog data and accumulate
        }

      // Combine count and analog readings average

      if( Acquire->ac_bMagXAxis ) {                                                             // Magnetometer X axis
        if( Acquire->ac_nXYZAxisReadings ) dXAnalog /= (double) Acquire->ac_nXYZAxisReadings;   // Mean analog value
        Acquire->ac_adBGSignal[ 0 ] = CombineCountAndData( nXCount, dXAnalog, "X", Acquire->ac_bXFlux, Acquire->ac_bDCSquids, Acquire->ac_MagnetometerDData.dwXRange, Acquire->ac_dXCalibration, Acquire->ac_dYCalibration, Acquire->ac_dZCalibration );  // Reading in emu
        }
      if( Acquire->ac_bMagYAxis ) {                                                             // Magnetometer Y axis
        if( Acquire->ac_nXYZAxisReadings ) dYAnalog /= (double) Acquire->ac_nXYZAxisReadings;   // Mean analog value
        Acquire->ac_adBGSignal[ 1 ] = CombineCountAndData( nYCount, dYAnalog, "Y", Acquire->ac_bYFlux, Acquire->ac_bDCSquids, Acquire->ac_MagnetometerDData.dwYRange, Acquire->ac_dXCalibration, Acquire->ac_dYCalibration, Acquire->ac_dZCalibration );  // Reading in emu
        }
      if( Acquire->ac_bMagZAxis ) {                                                             // Magnetometer Z axis
        if( Acquire->ac_nXYZAxisReadings ) dZAnalog /= (double) Acquire->ac_nXYZAxisReadings;   // Mean analog value
        Acquire->ac_adBGSignal[ 2 ] = CombineCountAndData( nZCount, dZAnalog, "Z", Acquire->ac_bZFlux, Acquire->ac_bDCSquids, Acquire->ac_MagnetometerDData.dwZRange, Acquire->ac_dXCalibration, Acquire->ac_dYCalibration, Acquire->ac_dZCalibration );  // Reading in emu
        }
/*
      if( Acquire->ac_bMagXAxis ) {                                     // Magnetometer X axis
//      MagLatchCount( hWindow, Acquire->ac_nMagComID, "X" );           // Latch counter
        nXCount = MagSendCount( hWindow, Acquire->ac_nMagComID, "X" );  // Send count
        dXAnalog = 0.0;                                                 // Clear analog value
        for( i = 0; i < nXAxisReadings; i++ ) {                         // Multiple analog readings
          MagLatchData( hWindow, Acquire->ac_nMagComID, "X" );          // Latch analog data
          dXAnalog += MagSendData( hWindow, Acquire->ac_nMagComID, "X" );//Send analog data and accumulate
          }
        dXAnalog /= (double) nXAxisReadings;                            // Mean analog value
        Acquire->ac_adBGSignal[ 0 ] = CombineCountAndData( nXCount, dXAnalog, "X", Acquire->ac_bXFlux, Acquire->ac_MagnetometerDData.dwXRange, Acquire->ac_dXCalibration, Acquire->ac_dYCalibration, Acquire->ac_dZCalibration ); // Reading in emu
        }
      if( Acquire->ac_bMagYAxis ) {                                     // Magnetometer Y axis
//      MagLatchCount( hWindow, Acquire->ac_nMagComID, "Y" );           // Latch counter
        nYCount = MagSendCount( hWindow, Acquire->ac_nMagComID, "Y" );  // Send count
        dYAnalog = 0.0;                                                 // Clear analog value
        for( i = 0; i < nYAxisReadings; i++ ) {                         // Multiple analog readings
          MagLatchData( hWindow, Acquire->ac_nMagComID, "Y" );          // Latch analog data
          dYAnalog += MagSendData( hWindow, Acquire->ac_nMagComID, "Y" );//Send analog data and accumulate
          }
        dYAnalog /= (double) nYAxisReadings;                            // Mean analog value
        Acquire->ac_adBGSignal[ 1 ] = CombineCountAndData( nYCount, dYAnalog, "Y", Acquire->ac_bYFlux, Acquire->ac_MagnetometerDData.dwYRange, Acquire->ac_dXCalibration, Acquire->ac_dYCalibration, Acquire->ac_dZCalibration ); // Reading in emu
        }
      if( Acquire->ac_bMagZAxis ) {                                     // Magnetometer Z axis
//      MagLatchCount( hWindow, Acquire->ac_nMagComID, "Z" );           // Latch counter
        nZCount = MagSendCount( hWindow, Acquire->ac_nMagComID, "Z" );  // Send count
        dZAnalog = 0.0;                                                 // Clear analog value
        for( i = 0; i < nZAxisReadings; i++ ) {                         // Multiple analog readings
          MagLatchData( hWindow, Acquire->ac_nMagComID, "Z" );          // Latch analog data
          dZAnalog += MagSendData( hWindow, Acquire->ac_nMagComID, "Z" );//Send analog data and accumulate
          }
        dZAnalog /= (double) nZAxisReadings;                            // Mean analog value
        Acquire->ac_adBGSignal[ 2 ] = CombineCountAndData(nZCount, dZAnalog, "Z", Acquire->ac_bZFlux, Acquire->ac_MagnetometerDData.dwZRange, Acquire->ac_dXCalibration, Acquire->ac_dYCalibration, Acquire->ac_dZCalibration );  // Reading in emu
        }
*/
      InvalidateRect(Acquire->ac_hwndChildInFocus, NULL, TRUE );        // Generate a WM_PAINT message
      UpdateWindow(  Acquire->ac_hwndChildInFocus );
      }

    // Measure sample

    if( !Acquire->ac_bCancelCycle ) {                                   // If cycle has not been canceled
      // Move sample handler to measurement position (translation only)
      if( Acquire->ac_bSHAuto && Acquire->ac_bSHTrans )                 // Automatic sample handler and automatic translation
        Acquire->ac_bTMeas = Translate( Acquire, hWindow, Acquire->ac_bTMeas, Acquire->ac_lTMeas );     // Translate to measurement position
      else // Manual
        Acquire->ac_bCancelCycle = MyTranslateMessage( hWindow, "Measurement" );
/*
      // Move sample handler to measurement position

      if( Acquire->ac_bSHAuto ) {                                                                       // Automatic
        if( Acquire->ac_bSHTrans )                                                                      // Automatic translation
          Acquire->ac_bTMeas = Translate( Acquire, hWindow, Acquire->ac_bTMeas, Acquire->ac_lTMeas );   // Translate to measurement position
        else MyTranslateMessage( hWindow, "Measurement" );
        if( Acquire->ac_bSHRot )                                                                        // Automatic rotation
          Acquire->ac_bRHome = Rotate( Acquire, hWindow, Acquire->ac_bRHome, Acquire->ac_nRHome );      // Rotate to 0 position
        else RotateMessage( hWindow, "0" );
        }
      else {    // Manual
        MyTranslateMessage( hWindow, "Measurement" );
        RotateMessage( hWindow, "0" );
        }
*/
      // Measure sample

      do {                                                              // Loop to complete measurement
        Wait( Acquire->ac_dwSettlingDelay );                            // Wait for magnetometer to settle
        Acquire->ac_idMagSignal += 1;                                   // Increment signal arrays index

        // Latch all counters

        MagLatchCount( hWindow, Acquire->ac_nMagComID, "A" );           // Latch all counters

        // Read counters and set magnetometer up range flag if necessary

        if( Acquire->ac_bMagXAxis ) {                                                   // Magnetometer X axis
          nXCount = MagSendCount( hWindow, Acquire->ac_nMagComID, "X" );                // Send count
          if( ( ( nXCount != 0 ) && !Acquire->ac_bXFlux ) || ( abs( nXCount ) > 9900 ) )// If sample moment is too large for current setting
            Acquire->ac_bUpRange = TRUE;                                                // Set uprange flag
          }
        if( Acquire->ac_bMagYAxis ) {                                                   // Magnetometer Y axis
          nYCount = MagSendCount( hWindow, Acquire->ac_nMagComID, "Y" );                // Send count
          if( ( ( nYCount != 0 ) && !Acquire->ac_bYFlux ) || ( abs( nYCount ) > 9900 ) )// If sample moment is too large for current setting
            Acquire->ac_bUpRange = TRUE;                                                // Set uprange flag
          }
        if (Acquire->ac_bMagZAxis ) {                                                   // Magnetometer Z axis
          nZCount = MagSendCount( hWindow, Acquire->ac_nMagComID, "Z" );                // Send count
          if( ( ( nZCount != 0 ) && !Acquire->ac_bZFlux ) || ( abs( nZCount ) > 9900 ) )// If sample moment is too large for current setting
            Acquire->ac_bUpRange = TRUE;                                                // Set uprange flag
          }

        // Clear analog variables

        dXAnalog = 0.0;         // X axis data
        dYAnalog = 0.0;         // Y axis data
        dZAnalog = 0.0;         // Z axis data

        // Analog readings average

        for( i = 0; i < Acquire->ac_nXYZAxisReadings; i++ ) {                           // Multiple analog readings
          MagLatchData( hWindow, Acquire->ac_nMagComID, "A" );                          // Latch analog data
          if( Acquire->ac_bMagXAxis )                                                   // Magnetometer X axis
            dXAnalog += MagSendData( hWindow, Acquire->ac_nMagComID, "X" );             // Send analog data and accumulate
          if( Acquire->ac_bMagYAxis )                                                   // Magnetometer Y axis
            dYAnalog += MagSendData( hWindow, Acquire->ac_nMagComID, "Y" );             // Send analog data and accumulate
          if( Acquire->ac_bMagZAxis )                                                   // Magnetometer Z axis
            dZAnalog += MagSendData( hWindow, Acquire->ac_nMagComID, "Z" );             // Send analog data and accumulate
          }

        // Combine count and analog readings average

        if( Acquire->ac_bMagXAxis ) {                                                           // Magnetometer X axis
          if( Acquire->ac_nXYZAxisReadings ) dXAnalog /= (double) Acquire->ac_nXYZAxisReadings; // Mean analog value
          Acquire->ac_adMagXSignal[ Acquire->ac_idMagSignal ] = CombineCountAndData( nXCount, dXAnalog, "X", Acquire->ac_bXFlux, Acquire->ac_bDCSquids, Acquire->ac_MagnetometerDData.dwXRange, Acquire->ac_dXCalibration, Acquire->ac_dYCalibration, Acquire->ac_dZCalibration );      // Reading in emu
          }
        if( Acquire->ac_bMagYAxis ) {                                                           // Magnetometer Y axis
          if( Acquire->ac_nXYZAxisReadings ) dYAnalog /= (double) Acquire->ac_nXYZAxisReadings; // Mean analog value
          Acquire->ac_adMagYSignal[ Acquire->ac_idMagSignal ] = CombineCountAndData( nYCount, dYAnalog, "Y", Acquire->ac_bYFlux, Acquire->ac_bDCSquids, Acquire->ac_MagnetometerDData.dwYRange, Acquire->ac_dXCalibration, Acquire->ac_dYCalibration, Acquire->ac_dZCalibration );      // Reading in emu
          }
        if( Acquire->ac_bMagZAxis ) {                                                           // Magnetometer Z axis
          if( Acquire->ac_nXYZAxisReadings ) dZAnalog /= (double) Acquire->ac_nXYZAxisReadings; // Mean analog value
          Acquire->ac_adMagZSignal[ Acquire->ac_idMagSignal ] = CombineCountAndData( nZCount, dZAnalog, "Z", Acquire->ac_bZFlux, Acquire->ac_bDCSquids, Acquire->ac_MagnetometerDData.dwZRange, Acquire->ac_dXCalibration, Acquire->ac_dYCalibration, Acquire->ac_dZCalibration );      // Reading in emu
          }
/*
        if( Acquire->ac_bMagXAxis ) {                                                   // Magnetometer X axis
//        MagLatchCount( hWindow, Acquire->ac_nMagComID, "X" );                         // Latch counter
          nXCount = MagSendCount( hWindow, Acquire->ac_nMagComID, "X" );                // Send count
          if( ( ( nXCount != 0 ) && !Acquire->ac_bXFlux ) || ( abs( nXCount ) > 9900 ) )// If sample moment is too large for current setting
            Acquire->ac_bUpRange = TRUE;                                                // Set uprange flag
          dXAnalog = 0.0;                                                               // Clear analog value
          for( i = 0; i < nXAxisReadings; i++ ) {                                       // Multiple analog readings
            MagLatchData( hWindow, Acquire->ac_nMagComID, "X" );                        // Latch analog data
            dXAnalog += MagSendData( hWindow, Acquire->ac_nMagComID, "X" );             // Send analog data and accumulate
            }
          dXAnalog /= (double) nXAxisReadings;                                          // Mean analog value
          Acquire->ac_adMagXSignal[ Acquire->ac_idMagSignal ] = CombineCountAndData( nXCount, dXAnalog, "X", Acquire->ac_bXFlux, Acquire->ac_MagnetometerDData.dwXRange, Acquire->ac_dXCalibration, Acquire->ac_dYCalibration, Acquire->ac_dZCalibration );     // Reading in emu
//        Eprintf( achM, "%+.3e", Acquire->ac_adMagXSignal[ Acquire->ac_idMagSignal ] );// Notify user
//        MessageBox( HWindow, achM, "X Axis Read", MB_ICONHAND | MB_OK );              // Notify user
          }
        if( Acquire->ac_bMagYAxis ) {                                                   // Magnetometer Y axis
//        MagLatchCount( hWindow, Acquire->ac_nMagComID, "Y" );                         // Latch counter
          nYCount = MagSendCount( hWindow, Acquire->ac_nMagComID, "Y" );                // Send count
          if( ( ( nYCount != 0 ) && !Acquire->ac_bYFlux ) || ( abs( nYCount ) > 9900 ) )// If sample moment is too large for current setting
            Acquire->ac_bUpRange = TRUE;                                                // Set uprange flag
          dYAnalog = 0.0;                                                               // Clear analog value
          for( i = 0; i < nYAxisReadings; i++ ) {                                       // Multiple analog readings
            MagLatchData( hWindow, Acquire->ac_nMagComID, "Y" );                        // Latch analog data
            dYAnalog += MagSendData( hWindow, Acquire->ac_nMagComID, "Y" );             // Send analog data and accumulate
            }
          dYAnalog /= (double) nYAxisReadings;                                          // Mean analog value
          Acquire->ac_adMagYSignal[ Acquire->ac_idMagSignal ] = CombineCountAndData( nYCount, dYAnalog, "Y", Acquire->ac_bYFlux, Acquire->ac_MagnetometerDData.dwYRange, Acquire->ac_dXCalibration, Acquire->ac_dYCalibration, Acquire->ac_dZCalibration );     // Reading in emu
          }
        if( Acquire->ac_bMagZAxis ) {                                                   // Magnetometer Z axis
//        MagLatchCount( hWindow, Acquire->ac_nMagComID, "Z" );                         // Latch counter
          nZCount = MagSendCount( hWindow, Acquire->ac_nMagComID, "Z" );                // Send count
          if( ( ( nZCount != 0 ) && !Acquire->ac_bZFlux ) || ( abs( nZCount ) > 9900 ) )// If sample moment is too large for current setting
            Acquire->ac_bUpRange = TRUE;                                                // Set uprange flag
          dZAnalog = 0.0;                                                               // Clear analog value
          for( i = 0; i < nZAxisReadings; i++ ) {                                       // Multiple analog readings
            MagLatchData( hWindow, Acquire->ac_nMagComID, "Z" );                        // Latch analog data
            dZAnalog += MagSendData( hWindow, Acquire->ac_nMagComID, "Z" );             // Send analog data and accumulate
            }
          dZAnalog /= (double) nZAxisReadings;                                          // Mean analog value
          Acquire->ac_adMagZSignal[ Acquire->ac_idMagSignal ] = CombineCountAndData( nZCount, dZAnalog, "Z", Acquire->ac_bZFlux, Acquire->ac_MagnetometerDData.dwZRange, Acquire->ac_dXCalibration, Acquire->ac_dYCalibration, Acquire->ac_dZCalibration );     // Reading in emu
          }
*/
        InvalidateRect( Acquire->ac_hwndChildInFocus, NULL, TRUE );                     // Generate a WM_PAINT message
        UpdateWindow( Acquire->ac_hwndChildInFocus );

        if( Acquire->ac_bMinMeasure ) {                                                 // Minimum required number of measurements
          if( Acquire->ac_bMagXAxis && Acquire->ac_bMagYAxis && Acquire->ac_bMagZAxis ) // Do not rotate with 3 magnetometer axes
            bMeasure = FALSE;                                                           // End measurement loop
          else {                                                                        // Rotate to 90 with 2 magnetometer axes
            if( Acquire->ac_idMagSignal < 1 ) {                                         // Rotate sample handler after first measurement set
              if( Acquire->ac_bSHAuto && Acquire->ac_bSHRot )                           // Automatic sample handler and automatic rotation
                Acquire->ac_bR90 = Rotate( Acquire, hWindow, Acquire->ac_bR90, Acquire->ac_nR90 );
              else                                                                      // Manual
                Acquire->ac_bCancelCycle = RotateMessage( hWindow, "90" );
/*
              if( Acquire->ac_bSHAuto ) {                                               // Automatic
                if( Acquire->ac_bSHRot )                                                // Automatic rotation
                  Acquire->ac_bR90 = Rotate( Acquire, hWindow, Acquire->ac_bR90, Acquire->ac_nR90 );
                else RotateMessage( hWindow, "90" );
                }
              else                                                      // Manual
                RotateMessage( hWindow, "90" );
*/
              }
            else                                                        // After second measurement set
              bMeasure = FALSE;                                         // End measurement loop
            }
          }
        else {                                                          // At least one full rotation
          iRotation = Acquire->ac_idMagSignal;                          // Set rotation index to array index

          // Rotation index can only range from 0 to 3

          if( !( iRotation % 4 ) ) {                                    // If array index is evenly divisible by 4
            iTemporary = iRotation;                                     // Store the index
            iRotation  = 0;                                             // Set rotation index to 0
            }
          else                                                          // If array index is not evenly divisible by 4
            iRotation -= iTemporary;                                    // Subtract from it the last array index that is evenly divisible by 4

          switch( iRotation ) {                                         // Identify rotation position

            case 0:                                                     // Rotate to 90
              if( Acquire->ac_bSHAuto && Acquire->ac_bSHRot )           // Automatic sample handler and automatic rotation
                Acquire->ac_bR90 = Rotate( Acquire, hWindow, Acquire->ac_bR90, Acquire->ac_nR90 );
              else                                                      // Manual
                Acquire->ac_bCancelCycle = RotateMessage( hWindow, "90" );
/*
              if( Acquire->ac_bSHAuto ) {                               // Automatic
                if( Acquire->ac_bSHRot )                                // Automatic rotation
                  Acquire->ac_bR90 = Rotate( Acquire, hWindow, Acquire->ac_bR90, Acquire->ac_nR90 );
                else RotateMessage( hWindow, "90" );
                }
              else                                                      // Manual
                RotateMessage( hWindow, "90" );
*/
              break;

            case 1:                                                     // Rotate to 180
              if( Acquire->ac_bSHAuto && Acquire->ac_bSHRot )           // Automatic sample handler and automatic rotation
                Acquire->ac_bR180 = Rotate( Acquire, hWindow, Acquire->ac_bR180, Acquire->ac_nR180 );
              else                                                      // Manual
                Acquire->ac_bCancelCycle = RotateMessage( hWindow, "180" );
/*
              if( Acquire->ac_bSHAuto ) {                               // Automatic
                if( Acquire->ac_bSHRot )                                // Automatic rotation
                  Acquire->ac_bR180 = Rotate( Acquire, hWindow, Acquire->ac_bR180, Acquire->ac_nR180 );
                else RotateMessage( hWindow, "180" );
                }
              else                                                      // Manual
                RotateMessage( hWindow, "180" );
*/
              break;

            case 2:                                                     // Rotate to 270
              if( Acquire->ac_bSHAuto && Acquire->ac_bSHRot )           // Automatic sample handler and automatic rotation
                Acquire->ac_bR270 = Rotate( Acquire, hWindow, Acquire->ac_bR270, Acquire->ac_nR270 );
              else                                                      // Manual
                Acquire->ac_bCancelCycle = RotateMessage( hWindow, "270" );
/*
              if( Acquire->ac_bSHAuto ) {                               // Automatic
                if( Acquire->ac_bSHRot )                                // Automatic rotation
                  Acquire->ac_bR270 = Rotate( Acquire, hWindow, Acquire->ac_bR270, Acquire->ac_nR270 );
                else RotateMessage( hWindow, "270" );
                }
              else                                                      // Manual
                RotateMessage( hWindow, "270" );
*/
              break;

            case 3:                                                     // Rotate to home
              if( Acquire->ac_bSHAuto && Acquire->ac_bSHRot )           // Automatic sample handler and automatic rotation
                Acquire->ac_bRHome = Rotate( Acquire, hWindow, Acquire->ac_bRHome, Acquire->ac_nR360 ); // Acquire->ac_bRHome = Rotate( Acquire, hWindow, Acquire->ac_bRHome, Acquire->ac_nRHome );
              else                                                      // Manual
                Acquire->ac_bCancelCycle = RotateMessage( hWindow, "0" );
/*
              if( Acquire->ac_bSHAuto ) {                               // Automatic
                if( Acquire->ac_bSHRot )                                // Automatic rotation
                  Acquire->ac_bRHome = Rotate( Acquire, hWindow, Acquire->ac_bRHome, Acquire->ac_nRHome );
                else RotateMessage( hWindow, "0" );
                }
              else                                                      // Manual
                RotateMessage( hWindow, "0" );
*/
              break;

            default:
              break;
            }

          if( Acquire->ac_bSingleRotation ) {                           // Single rotation
            if( iRotation > 2 )                                         // Exit measurement loop once one rotation is complete
              bMeasure = FALSE;                                         // End measurement loop
            }
          else {                                                        // Multiple rotations
            if( Acquire->ac_idMagSignal >= ( Acquire->ac_nMultRotations * 4 - 1 ) )     // Exit measurement loop once specified rotations are complete
              bMeasure = FALSE;                                         // End measurement loop
            }
          }
        } while( bMeasure && !Acquire->ac_bCancelCycle );
      }

    // Measure background after sample measurement

    if( !Acquire->ac_bCancelCycle ) {                                   // If cycle has not been canceled

      // Move sample handler to background position

      if( Acquire->ac_bSHAuto ) {                                                                               // Automatic
        if( Acquire->ac_bSHTrans )                                                                              // Automatic translation
             Acquire->ac_bTBack       = Translate( Acquire, hWindow, Acquire->ac_bTBack, Acquire->ac_lTBack );  // Translate to background position
        else Acquire->ac_bCancelCycle = MyTranslateMessage( hWindow, "Background" );
        if( Acquire->ac_bSHRot )                                                                                // Automatic rotation
             Acquire->ac_bRHome       = Rotate( Acquire, hWindow, Acquire->ac_bRHome, Acquire->ac_nRHome );     // Rotate to 0 position
        else Acquire->ac_bCancelCycle = RotateMessage( hWindow, "0" );
        }
      else {                                                            // Manual
        Acquire->ac_bCancelCycle = MyTranslateMessage( hWindow, "Background" );
        Acquire->ac_bCancelCycle = RotateMessage( hWindow, "0" );
        }

      // Measure background

      Wait( Acquire->ac_dwSettlingDelay );                              // Wait for magnetometer to settle

      // Latch all counters

      MagLatchCount( hWindow, Acquire->ac_nMagComID, "A" );

      // Read counters

      if( Acquire->ac_bMagXAxis )                                       // Magnetometer X axis
        nXCount = MagSendCount( hWindow, Acquire->ac_nMagComID, "X" );  // Send count
      if( Acquire->ac_bMagYAxis )                                       // Magnetometer Y axis
        nYCount = MagSendCount( hWindow, Acquire->ac_nMagComID, "Y" );  // Send count
      if( Acquire->ac_bMagZAxis )                                       // Magnetometer Z axis
        nZCount = MagSendCount( hWindow, Acquire->ac_nMagComID, "Z" );  // Send count

      // Clear analog variables

      dXAnalog = 0.0;   // X axis data
      dYAnalog = 0.0;   // Y axis data
      dZAnalog = 0.0;   // Z axis data

    // Analog readings average

    for( i = 0; i < Acquire->ac_nXYZAxisReadings; i++ ) {               // Multiple analog readings
      MagLatchData( hWindow, Acquire->ac_nMagComID, "A" );              // Latch analog data
      if( Acquire->ac_bMagXAxis )                                       // Magnetometer X axis
        dXAnalog += MagSendData( hWindow, Acquire->ac_nMagComID, "X" ); // Send analog data and accumulate
      if( Acquire->ac_bMagYAxis )                                       // Magnetometer Y axis
        dYAnalog += MagSendData( hWindow, Acquire->ac_nMagComID, "Y" ); // Send analog data and accumulate
      if( Acquire->ac_bMagZAxis )                                       // Magnetometer Z axis
        dZAnalog += MagSendData( hWindow, Acquire->ac_nMagComID, "Z" ); // Send analog data and accumulate
        }

      // Combine count and analog readings average, and calculate drift

      if( Acquire->ac_bMagXAxis ) {                                                             // Magnetometer X axis
        if( Acquire->ac_nXYZAxisReadings ) dXAnalog /= (double) Acquire->ac_nXYZAxisReadings;   // Mean analog value
        Acquire->ac_adBGSignal[ 3 ] = CombineCountAndData( nXCount, dXAnalog, "X", Acquire->ac_bXFlux, Acquire->ac_bDCSquids, Acquire->ac_MagnetometerDData.dwXRange, Acquire->ac_dXCalibration, Acquire->ac_dYCalibration, Acquire->ac_dZCalibration );// Reading in emu
        Acquire->ac_adDrift[ 0 ] = Acquire->ac_adBGSignal[ 3 ] - Acquire->ac_adBGSignal[ 0 ];   // X drift (total rise)
        }
      if( Acquire->ac_bMagYAxis ) {                                                             // Magnetometer Y axis
        if( Acquire->ac_nXYZAxisReadings ) dYAnalog /= (double) Acquire->ac_nXYZAxisReadings;   // Mean analog value
        Acquire->ac_adBGSignal[ 4 ] = CombineCountAndData( nYCount, dYAnalog, "Y", Acquire->ac_bYFlux, Acquire->ac_bDCSquids, Acquire->ac_MagnetometerDData.dwYRange, Acquire->ac_dXCalibration, Acquire->ac_dYCalibration, Acquire->ac_dZCalibration );// Reading in emu
        Acquire->ac_adDrift[ 1 ] = Acquire->ac_adBGSignal[ 4 ] - Acquire->ac_adBGSignal[ 1 ];   // Y drift (total rise)
        }
      if( Acquire->ac_bMagZAxis ) {                                                             // Magnetometer Z axis
        if( Acquire->ac_nXYZAxisReadings ) dZAnalog /= (double) Acquire->ac_nXYZAxisReadings;   // Mean analog value
        Acquire->ac_adBGSignal[ 5 ] = CombineCountAndData( nZCount, dZAnalog, "Z", Acquire->ac_bZFlux, Acquire->ac_bDCSquids, Acquire->ac_MagnetometerDData.dwZRange, Acquire->ac_dXCalibration, Acquire->ac_dYCalibration, Acquire->ac_dZCalibration );// Reading in emu
        Acquire->ac_adDrift[ 2 ] = Acquire->ac_adBGSignal[ 5 ] - Acquire->ac_adBGSignal[ 2 ];   // Z drift (total rise)
        }
/*
      MagLatchCount( hWindow, Acquire->ac_nMagComID, "A" );                                     // Latch all counters
      if( Acquire->ac_bMagXAxis ) {                                                             // Magnetometer X axis
//      MagLatchCount( hWindow, Acquire->ac_nMagComID, "X" );                                   // Latch counter
        nXCount  = MagSendCount( hWindow, Acquire->ac_nMagComID, "X" );                         // Send count
        dXAnalog = 0.0;                                                                         // Clear analog value
        for( i = 0; i < nXAxisReadings; i++ ) {                                                 // Multiple analog readings
          MagLatchData( hWindow, Acquire->ac_nMagComID, "X" );                                  // Latch analog data
          dXAnalog += MagSendData( hWindow, Acquire->ac_nMagComID, "X" );                       // Send analog data and accumulate
          }
        dXAnalog /= (double) nXAxisReadings;                                                    // Mean analog value
        Acquire->ac_adBGSignal[ 3 ] = CombineCountAndData( nXCount, dXAnalog, "X", Acquire->ac_bXFlux, Acquire->ac_MagnetometerDData.dwXRange, Acquire->ac_dXCalibration, Acquire->ac_dYCalibration, Acquire->ac_dZCalibration );       // Reading in emu
        Acquire->ac_adDrift[0] = Acquire->ac_adBGSignal[ 3 ] - Acquire->ac_adBGSignal[ 0 ];     // X drift (total rise)
        }
      if( Acquire->ac_bMagYAxis ) {                                                             // Magnetometer Y axis
//      MagLatchCount( hWindow, Acquire->ac_nMagComID, "Y" );                                   // Latch counter
        nYCount  = MagSendCount( hWindow, Acquire->ac_nMagComID, "Y" );                         // Send count
        dYAnalog = 0.0;                                                                         // Clear analog value
        for( i = 0; i < nYAxisReadings; i++ ) {                                                 // Multiple analog readings
          MagLatchData( hWindow, Acquire->ac_nMagComID, "Y" );                                  // Latch analog data
          dYAnalog += MagSendData( hWindow, Acquire->ac_nMagComID, "Y" );                       // Send analog data and accumulate
          }
        dYAnalog /= (double) nYAxisReadings;                                                    // Mean analog value
        Acquire->ac_adBGSignal[ 4 ] = CombineCountAndData( nYCount, dYAnalog, "Y", Acquire->ac_bYFlux, Acquire->ac_MagnetometerDData.dwYRange, Acquire->ac_dXCalibration, Acquire->ac_dYCalibration, Acquire->ac_dZCalibration );       // Reading in emu
        Acquire->ac_adDrift[ 1 ] = Acquire->ac_adBGSignal[ 4 ] - Acquire->ac_adBGSignal[ 1 ];   // Y drift (total rise)
        }
      if( Acquire->ac_bMagZAxis ) {                                                             // Magnetometer Z axis
//      MagLatchCount( hWindow, Acquire->ac_nMagComID, "Z" );                                   // Latch counter
        nZCount  = MagSendCount( hWindow, Acquire->ac_nMagComID, "Z" );                         // Send count
        dZAnalog = 0.0;                                                                         // Clear analog value
        for( i = 0; i < nZAxisReadings; i++ ) {                                                 // Multiple analog readings
          MagLatchData( hWindow, Acquire->ac_nMagComID, "Z" );                                  // Latch analog data
          dZAnalog += MagSendData( hWindow, Acquire->ac_nMagComID, "Z" );                       // Send analog data and accumulate
          }
        dZAnalog /= (double) nZAxisReadings;                                                    // Mean analog value
        Acquire->ac_adBGSignal[ 5 ] = CombineCountAndData( nZCount, dZAnalog, "Z", Acquire->ac_bZFlux, Acquire->ac_MagnetometerDData.dwZRange, Acquire->ac_dXCalibration, Acquire->ac_dYCalibration, Acquire->ac_dZCalibration );       // Reading in emu
        Acquire->ac_adDrift[ 2 ] = Acquire->ac_adBGSignal[ 5 ] - Acquire->ac_adBGSignal[ 2 ];   // Z drift (total rise)
        }
*/
      InvalidateRect(Acquire->ac_hwndChildInFocus, NULL, TRUE );                                // Generate a WM_PAINT message
      UpdateWindow(  Acquire->ac_hwndChildInFocus );
      }
    }
  }


// TMyMDIChild Class Member - Reduce measurement data

void TMyMDIChildWMReduce( ACQUIRE *Acquire, HWND hWindow ) {

  double dAngle, dDirection, dError, dTotalMoment, adDirection1[ 3 ], adDirection2[ 3 ], adDirection3[ 3 ], adEuler[ 3 ];
  time_t now;

  if( ( Acquire != (ACQUIRE *) NULL ) &&
      ( hWindow != (HWND)      NULL ) ) {

    // Reduce measurement data

    if( !Acquire->ac_bCancelCycle ) {                                                           // If cycle has not been canceled

      // Subtract holder moments

//    MessageBox( HWindow, "HolderCorrection", "Reduce", MB_ICONHAND | MB_OK );                 // Notify user
      HolderCorrection( Acquire->ac_adMagXSignal, Acquire->ac_adMagYSignal, Acquire->ac_adMagZSignal, Acquire->ac_idMagSignal, Acquire->ac_bMagXAxis, Acquire->ac_bMagYAxis, Acquire->ac_bMagZAxis, Acquire->ac_dHolderXComponent, Acquire->ac_dHolderYComponent, Acquire->ac_dHolderZComponent );

      // Correct for offset and drift

//    MessageBox( HWindow, "OffsetAndDrift", "Reduce", MB_ICONHAND | MB_OK );                   // Notify user
      OffsetAndDrift( Acquire->ac_adMagXSignal, Acquire->ac_adMagYSignal, Acquire->ac_adMagZSignal, Acquire->ac_idMagSignal, Acquire->ac_bMagXAxis, Acquire->ac_bMagYAxis, Acquire->ac_bMagZAxis, Acquire->ac_adBGSignal );

      // Accumulate normal statistics sums

//    MessageBox( HWindow, "NormalSums", "Reduce", MB_ICONHAND | MB_OK );                       // Notify user
      NormalSums( Acquire->ac_adMagXSignal, Acquire->ac_adMagYSignal, Acquire->ac_adMagZSignal, Acquire->ac_idMagSignal, Acquire->ac_bMagXAxis, Acquire->ac_bMagYAxis, Acquire->ac_bMagZAxis, Acquire->ac_bMeasureNegZ, Acquire->ac_adNormalSums );
      sprintf( Acquire->ac_FileData.achNX, "%.0f", Acquire->ac_adNormalSums[ 2 ] );             // Number of X measurements
      sprintf( Acquire->ac_FileData.achNY, "%.0f", Acquire->ac_adNormalSums[ 5 ] );             // Number of Y measurements
      sprintf( Acquire->ac_FileData.achNZ, "%.0f", Acquire->ac_adNormalSums[ 8 ] );             // Number of Z measurements

      // Calculate sample components and standard deviations

//    MessageBox( HWindow, "NormalStatistics", "Reduce", MB_ICONHAND | MB_OK );                 // Notify user
      NormalStatistics( Acquire->ac_adNormalSums, Acquire->ac_adSampleComponents );
      Eprintf( Acquire->ac_FileData.achSigmaX, "%.3e", Acquire->ac_adSampleComponents[ 1 ] );   // X standard deviation string for disk file
      Eprintf( Acquire->ac_FileData.achSigmaY, "%.3e", Acquire->ac_adSampleComponents[ 3 ] );   // Y standard deviation string for disk file
      Eprintf( Acquire->ac_FileData.achSigmaZ, "%.3e", Acquire->ac_adSampleComponents[ 5 ] );   // Z standard deviation string for disk file

      // Calculate percent error, total moment, and magnetization

//    MessageBox( HWindow, "Percent error", "Reduce", MB_ICONHAND | MB_OK );                    // Notify user
//    if( Acquire->ac_adSampleComponents[ 0 ] == 0.0 )                                          // Prevent division by zero
//      Acquire->ac_adSampleComponents[ 0 ] = 1.0;
      dError = 100.0 * Acquire->ac_adSampleComponents[ 1 ] / ( Acquire->ac_adSampleComponents[ 0 ] + 1.0e-15 );  // X error
      if( dError < 0.0 )
        dError *= -1;
      sprintf( Acquire->ac_FileData.achErrorX, "%.1f", dError );                                // Error string for disk file

//    if( Acquire->ac_adSampleComponents[ 2 ] == 0.0 )                                          // Prevent division by zero
//      Acquire->ac_adSampleComponents[ 2 ] = 1.0;
      dError = 100.0 * Acquire->ac_adSampleComponents[ 3 ] / ( Acquire->ac_adSampleComponents[ 2 ] + 1.0e-15 );  // Y error
      if( dError < 0.0 )
        dError *= -1;
      sprintf( Acquire->ac_FileData.achErrorY, "%.1f", dError );                                // Error string for disk file

//    if( Acquire->ac_adSampleComponents[ 4 ] == 0.0 )                                          // Prevent division by zero
//      Acquire->ac_adSampleComponents[ 4 ] = 1.0;
      dError = 100.0 * Acquire->ac_adSampleComponents[ 5 ] / ( Acquire->ac_adSampleComponents[ 4 ] + 1.0e-15 ); // Z error
      if( dError < 0.0 )
        dError *= -1;
      sprintf( Acquire->ac_FileData.achErrorZ, "%.1f", dError );                                // Error string for disk file

      dTotalMoment = Magnitude( Acquire->ac_adSampleComponents[ 0 ], Acquire->ac_adSampleComponents[ 2 ], Acquire->ac_adSampleComponents[ 4 ] );// Total moment (emu)
      Eprintf( Acquire->ac_FileData.achTotalMoment,   "%.3e", dTotalMoment );                   // Total moment
//    dTotalMoment /= atof( SampleDData.achSize );                                              // Magnetization (emu/cc or emu/gm)
//    Eprintf( Acquire->ac_FileData.achMagnetization, "%.3e", dTotalMoment );                   // Magnetization
      Eprintf( Acquire->ac_FileData.achMagnetization, "%.3e", ( dTotalMoment / atof(Acquire->ac_SampleDData.achSize ) ) );      // Magnetization (emu/cc or emu/gm)

      // Rotate components from magnetometer reference frame to core reference frame

//    MessageBox( HWindow, "Rotate angle", "Reduce", MB_ICONHAND | MB_OK );                     // Notify user
      adEuler[ 0 ] =  0.0;                                                                      // Rotate about the magnetometer/sample Z axis
      adEuler[ 1 ] =  0.0;
      adEuler[ 2 ] = -1.0;

//    dAngle = atof( Acquire->ac_StandardDData.achAngle );                                      // Angle of rotation
      adDirection1[ 0 ] = Acquire->ac_adSampleComponents[ 0 ];                                  // Sample X component in magnetometer reference frame
      adDirection1[ 1 ] = Acquire->ac_adSampleComponents[ 2 ];                                  // Sample Y component in magnetometer reference frame
      adDirection1[ 2 ] = Acquire->ac_adSampleComponents[ 4 ];                                  // Sample Z component in magnetometer reference frame
      RotateVector( adDirection1, adEuler, Acquire->ac_dXYAngle, adDirection2 );                // Rotate
      Eprintf( Acquire->ac_FileData.achMomentX, "%+.3e", adDirection2[ 0 ] );                   // Rotated X moment
      Eprintf( Acquire->ac_FileData.achMomentY, "%+.3e", adDirection2[ 1 ] );                   // Rotated Y moment
      Eprintf( Acquire->ac_FileData.achMomentZ, "%+.3e", adDirection2[ 2 ] );                   // Rotated Z moment

      // Calculate core declination and inclination

//    MessageBox( HWindow, "Core D and I", "Reduce", MB_ICONHAND | MB_OK );                     // Notify user
      CartesianToSpherical( adDirection2[ 0 ], adDirection2[ 1 ], adDirection2[ 2 ], adDirection1 );
      sprintf( Acquire->ac_FileData.achCoreDec, "%.1f", adDirection1[ 0 ] );                    // Declination
      sprintf( Acquire->ac_FileData.achCoreInc, "%.1f", adDirection1[ 1 ] );                    // Inclination

      // Calculate in situ declination and inclination

//    MessageBox( HWindow, "Rotate in situ", "Reduce", MB_ICONHAND | MB_OK );                   // Notify user
      adEuler[ 0 ] =  0.0;                                                                      // Rotate about the sample Y axis
      adEuler[ 1 ] = -1.0;
      adEuler[ 2 ] =  0.0;
      dAngle = atof( Acquire->ac_SampleDData.achCoreP );                                        // Angle of rotation
      adDirection1[ 0 ] = adDirection2[ 2 ];                                                    // Sample Z component maps to in situ X
      adDirection1[ 1 ] = adDirection2[ 1 ];                                                    // Sample Y component maps to in situ Y
      adDirection1[ 2 ] = -1.0 * adDirection2[ 0 ];                                             // Sample X component maps to in situ Z
      RotateVector( adDirection1, adEuler, dAngle, adDirection2 );                              // Rotate
      CartesianToSpherical( adDirection2[ 0 ], adDirection2[ 1 ], adDirection2[ 2 ], adDirection1 );
      adDirection1[ 0 ] += atof( Acquire->ac_SampleDData.achCoreAz ) + atof( Acquire->ac_SampleDData.achMagDecl );      // Add core DDA and declination
      if( adDirection1[ 0 ] >= 360.0 )
        adDirection1[ 0 ] -= 360.0;
      sprintf( Acquire->ac_FileData.achInSituDec, "%.1f", adDirection1[ 0 ] );                  // Declination
      sprintf( Acquire->ac_FileData.achInSituInc, "%.1f", adDirection1[ 1 ] );                  // Inclination

      // Calculate rotated declination and inclination

//    MessageBox( HWindow, "Rotate rotated", "Reduce", MB_ICONHAND | MB_OK );                   // Notify user
      SphericalToCartesian( adDirection1[ 0 ], adDirection1[ 1 ], 1.0, adDirection2 );          // Transform in situ spherical coordinates to cartesian coordinates
      dDirection = atof( Acquire->ac_SampleDData.achBeddingAz ) + atof( Acquire->ac_SampleDData.achMagDecl ) - 180.0;   // Pole to local bedding azimuth (corrected for magnetic declination)
      dAngle     = 90.0 - atof( Acquire->ac_SampleDData.achBeddingP );                          // Pole to local bedding plunge

      SphericalToCartesian( dDirection, dAngle, 1.0, adDirection1 );                            // Transform pole to local bedding spherical coordinates to cartesian coordinates
      dDirection = atof( Acquire->ac_SampleDData.achFoldAz ) + atof( Acquire->ac_SampleDData.achMagDecl ) + 90.0;       // Plunging fold hinge line azimuth plus magnetic declination plus 90
      SphericalToCartesian( dDirection, 0.0, 1.0, adEuler );                                    // Create the first rotation pole by converting corrected hinge line azimuth to cartesian coordinates
      dAngle     = atof( Acquire->ac_SampleDData.achFoldP );                                    // Angle of rotation

      RotateVector( adDirection2, adEuler, dAngle, adDirection3 );                              // Rotate in situ magnetization
      RotateVector( adDirection1, adEuler, dAngle, adDirection2 );                              // Rotate pole to local bedding
      CartesianToSpherical( adDirection2[ 0 ], adDirection2[ 1 ], adDirection2[ 2 ], adDirection1 );    // Convert rotated pole to local bedding cartesian coordinates to spherical coordinates
      adDirection1[ 0 ] -= 90.0;                                                                // Subtract 90 to get rotated local bedding strike
      SphericalToCartesian( adDirection1[ 0 ], 0.0, 1.0, adEuler );                             // Create second rotation pole by converting rotated strike to cartesian coordinates
      dAngle = 90.0 - adDirection1[ 1 ];                                                        // Angle of rotation (90 minus rotated local bedding pole plunge)
      if( Acquire->ac_SampleDData.dwOver )                                                      // If bed if overturned
        dAngle += 180.0;

      RotateVector( adDirection3, adEuler, dAngle, adDirection2 );                              // Rotate
      CartesianToSpherical( adDirection2[ 0 ], adDirection2[ 1 ], adDirection2[ 2 ], adDirection1 );
      if( adDirection1[ 0 ] >= 360.0 )
        adDirection1[0] -= 360.0;
      sprintf( Acquire->ac_FileData.achRotatedDec, "%.1f", adDirection1[ 0 ] );                 // Declination
      sprintf( Acquire->ac_FileData.achRotatedInc, "%.1f", adDirection1[ 1 ] );                 // Inclination

/*    dDirection = atof( Acquire->ac_SampleDData.achBeddingAz ) + atof( Acquire->ac_SampleDData.achMagDecl ) + 90.0;    // Dip vector azimuth plus declination plus 90
      SphericalToCartesian( dDirection, 0.0, 1.0, adEuler );                                    // Create rotation pole by converting corrected dip vector azimuth to cartesian coordinates
      dAngle     = atof( Acquire->ac_SampleDData.achBeddingP );                                 // Angle of rotation
      if( Acquire->ac_SampleDData.dwOver )                                                      // If bed if overturned
        dAngle += 180.0;
      RotateVector( adDirection2, adEuler, dAngle, adDirection1 );                              // Rotate
      CartesianToSpherical( adDirection1[ 0 ], adDirection1[ 1 ], adDirection1[ 2 ], adDirection2 );
      if( adDirection2[ 0 ] >= 360.0 )
        adDirection2[0] -= 360.0;
      sprintf( Acquire->ac_FileData.achRotatedDec, "%.1f", adDirection2[ 0 ] );                 // Declination
      sprintf( Acquire->ac_FileData.achRotatedInc, "%.1f", adDirection2[ 1 ] );                 // Inclination
*/
      // Calculate signal to noise, drift, and holder ratios

//    MessageBox( HWindow, "S/N Ratio", "Reduce", MB_ICONHAND | MB_OK );                        // Notify user
      Eprintf( Acquire->ac_FileData.achStoN, "%.1e", ( dTotalMoment / ( Magnitude( Acquire->ac_adSampleComponents[ 1 ], Acquire->ac_adSampleComponents[ 3 ], Acquire->ac_adSampleComponents[ 5 ] ) + 1.0e-15 ) ) );
//    MessageBox( HWindow, "S/D Ratio", "Reduce", MB_ICONHAND | MB_OK );                        // Notify user
      Eprintf( Acquire->ac_FileData.achStoD, "%.1e", ( dTotalMoment / ( Magnitude( Acquire->ac_adDrift[ 0 ], Acquire->ac_adDrift[ 1 ], Acquire->ac_adDrift[ 2 ] ) + 1.0e-15 ) ) );
//    MessageBox( HWindow, "S/H Ratio", "Reduce", MB_ICONHAND | MB_OK );                        // Notify user
      Eprintf( Acquire->ac_FileData.achStoH, "%.1e", ( dTotalMoment / ( atof( Acquire->ac_HolderDData.achTotal ) + 1.0e-15 ) ) );

      // + or - Z

      if( Acquire->ac_bMeasureNegZ )
           strcpy( Acquire->ac_FileData.achZAxis, "-z" );
      else strcpy( Acquire->ac_FileData.achZAxis, "+z" );

      // Read current time and date

      now = time( NULL );
      strftime( Acquire->ac_FileData.achTime, 18, "%b %d %Y %H:%M", localtime( &now ) );

      // Increment the current measurement step index

      Acquire->ac_FileSummary.nCurrentStep += 1;
      InvalidateRect(Acquire->ac_hwndChildInFocus, NULL, TRUE );                                // Generate a WM_PAINT message
      UpdateWindow(  Acquire->ac_hwndChildInFocus );
      }
    }
  }


static CHAR szExtDeviceMode[] = { "EXTDEVICEMODE" };

BOOL APIENTRY GetInitializationData( ACQUIRE *Acquire, HWND hWindow ) {

  LPSTR     lpOld;
  LPSTR     lpNew;
  FARPROC   lpfn;
  HANDLE    hT,hDrv;
  CHAR      sz[ 256 ];
  int       cb;
  INT       flag;

  /* Pop up dialog for user and retain data in app buffer */

  flag = DM_PROMPT | DM_COPY;

  /* Load the device driver and find the ExtDeviceMode() function */

  wsprintf( sz, "%s.drv", (LPSTR) Acquire->ac_achDriver );
  if( (int) ( hDrv = LoadLibrary( sz ) ) < 32 )
    return FALSE;
  if( !(lpfn = GetProcAddress( hDrv, szExtDeviceMode ) ) )
    return FALSE;

  /* We have some old data... we want to modify the previously    */
  /* specified setup rather than starting with the default setup. */

  if( Acquire->ac_hInitData ) {
    lpOld = (LPSTR) LocalLock( Acquire->ac_hInitData );
    flag |= DM_MODIFY;
    }
  else lpOld = NULL;

  /* Get the number of bytes needed for the init data */

  cb = (*lpfn)( hWindow, hDrv, (LPDEVMODE) NULL, (LPSTR) Acquire->ac_achDevice, (LPSTR) Acquire->ac_achPort, (LPDEVMODE) NULL, (LPSTR) NULL, 0 );

  /* Grab some memory for the new data and lock it. */

  hT = LocalAlloc( LHND, cb );
  if( !hT ) {
    MessageBox( hWindow, "<GetInitializationData> Not enough memory.", NULL, MB_OK | MB_ICONHAND );
    LocalUnlock(Acquire->ac_hInitData );
    LocalFree(  Acquire->ac_hInitData );
    FreeLibrary( hDrv );
    return FALSE;
    }
  lpNew = (LPSTR) LocalLock( hT );

  /* Post the device mode dialog. 0 flag iff user hits OK button */

  if( (*lpfn)( hWindow, hDrv, (LPDEVMODE) lpNew, (LPSTR) Acquire->ac_achDevice, (LPSTR) Acquire->ac_achPort, (LPDEVMODE) lpOld, (LPSTR) NULL, flag ) == IDOK )
    flag = 0;

  /* Unlock the input structures */

  LocalUnlock( hT );
  if( Acquire->ac_hInitData )
    LocalUnlock( Acquire->ac_hInitData );

  /* If the user hit OK and everything worked, free the original init. */
  /* data and retain the new one.  Otherwise, toss the new buffer.     */

  if( flag )
    LocalFree( hT );
  else {
    if( Acquire->ac_hInitData )
      LocalFree( Acquire->ac_hInitData );
    Acquire->ac_hInitData = hT;
    }

  FreeLibrary( hDrv );
  return !flag;
  }


HDC APIENTRY GetPrinterDC( ACQUIRE *Acquire ) {

  HDC       hdc;
  LPDEVMODE lpdevmode;

  /* Create the printer display context */

  lpdevmode = NULL;
  if( Acquire->ac_hInitData ) {

    /* Get a pointer to the initialization data */

    lpdevmode = (LPDEVMODE) LocalLock( Acquire->ac_hInitData );
    if( lstrcmp( Acquire->ac_achDevice, (LPSTR) lpdevmode ) ) {

      /* User has changed the device... cancel this setup, as it is invalid (although if we worked harder we could retain some of it). */

      lpdevmode = NULL;
      LocalUnlock(Acquire->ac_hInitData );
      LocalFree(  Acquire->ac_hInitData );
      Acquire->ac_hInitData = NULL;
      }
    }

  hdc = CreateDC( Acquire->ac_achDriver, Acquire->ac_achDevice, Acquire->ac_achPort, lpdevmode );

  /* Unlock initialization data */

  if( Acquire->ac_hInitData )
    LocalUnlock( Acquire->ac_hInitData );

  return hdc;
  }


// TMyMDIChild Class Member - Draw text

void TMyMDIChildWMText( ACQUIRE *Acquire, HWND hWindow, WPARAM wParam ) {

  int  i, nVOffset, nHOffset;
  char ach[ 145 ], achRecord[ 4 ];
  HDC         hdcPRN;
  PAINTSTRUCT ps;
  TEXTMETRIC  tm;
  FARPROC     AbortInst;
  HCURSOR     hcrArrow, hcrWait;
  HFONT       hFont, hOldFont;

  int nHScroll, nVScroll;

  if( ( Acquire != (ACQUIRE *) NULL ) &&
      ( hWindow != (HWND)      NULL ) ) {

    if( wParam == 0U ) {                                        // Draw to screen
      nHScroll = GetWindowLong( hWindow, GWL_nHScroll );
      nVScroll = GetWindowLong( hWindow, GWL_nVScroll );
      BeginPaint( hWindow, &ps );
      SelectObject( ps.hdc, Acquire->ac_hFont );
      }
    else {                                                      // Draw to paper
      nHScroll = 0;
      nVScroll = 0;
      if( !( hdcPRN = GetPrinterDC( Acquire ) ) )
        return;
      AbortInst = MakeProcInstance( (FARPROC) AbortProc, Acquire->ac_hInstance );       // Set up abort procedure
      Escape( hdcPRN, SETABORTPROC, 0, (LPSTR) AbortInst, NULL );
      Escape( hdcPRN, STARTDOC, 2, "2G", NULL );                // Begin a document
      ps.hdc   = hdcPRN;
      hcrWait  = LoadCursor( NULL, IDC_WAIT );                  // Get handle to the hourglass cursor
      hcrArrow = SetCursor( hcrWait );                          // Change cursor to an hourglass
      hFont    = Courier_Font( GetDeviceCaps( hdcPRN, HORZRES ) / 124, FALSE );
      hOldFont = SelectObject( ps.hdc, hFont );
      }

//  if( hWindow == Acquire->ac_hwndChildInFocus ) {             // Only paint the child window with input focus
      GetTextMetrics( ps.hdc, &tm );                            // Get measurement data for current font
      if( Acquire->ac_bFilePaint ) {                            // Repaint the child window with contents of disk file
        nHOffset = -1 * tm.tmAveCharWidth * nHScroll;           // Horizontal offset
        SetTextAlign( ps.hdc, TA_LEFT | TA_TOP );               // Set text align attributes
        strcpy( ach, Acquire->ac_SampleDData.achName );         // Copy sample name
        strcat( ach, "     " );                                 // Append spaces
        strcat( ach, Acquire->ac_SampleDData.achComment );      // Append comment
        strcat( ach, "     " );                                 // Append spaces
        strcat( ach, Acquire->ac_SampleDData.achTime );         // Append date and time
        TextOut( ps.hdc, nHOffset, tm.tmHeight * ( -nVScroll + 0 ), ach, lstrlen( ach ) );

        // Orientation information

        strcpy( ach, "Size = " );                               // Copy
        strcat( ach, Acquire->ac_SampleDData.achSize );         // Append size
        if( Acquire->ac_SampleDData.dwCC )
             strcat( ach, " cc    " );                          // Append
        else strcat( ach, " gm    " );                          // Append
        strcat( ach, "Core A = " );                             // Append
        strcat( ach, Acquire->ac_SampleDData.achCoreAz );       // Append core DDA
        strcat( ach, "  P = " );                                // Append
        strcat( ach, Acquire->ac_SampleDData.achCoreP  );       // Append core plunge
        strcat( ach, "    Dip A = " );                          // Append
        strcat( ach, Acquire->ac_SampleDData.achBeddingAz );    // Append dip vector azimuth
        strcat( ach, "  P = " );                                // Append
        strcat( ach, Acquire->ac_SampleDData.achBeddingP  );    // Append dip vector plunge
        if( Acquire->ac_SampleDData.dwOver )
             strcat( ach, "  overturned    " );                 // Append
        else strcat( ach, "  not overturned    " );             // Append
        strcat( ach, "Hinge A = " );                            // Append
        strcat( ach, Acquire->ac_SampleDData.achFoldAz );       // Append plunging fold hinge line azimuth
        strcat( ach, "  P = " );                                // Append
        strcat( ach, Acquire->ac_SampleDData.achFoldP  );       // Append plunging fold hinge line plunge
        strcat( ach, "    Decl = " );                           // Append
        strcat( ach, Acquire->ac_SampleDData.achMagDecl );      // Append magnetic declination
        TextOut( ps.hdc, nHOffset, tm.tmHeight * ( -nVScroll + 1 ), ach, lstrlen( ach ) );

        // Column labels

        nVOffset = tm.tmHeight * ( -nVScroll + 2 );                                     // Vertical offset
        TextOut( ps.hdc, tm.tmAveCharWidth * (  0 - nHScroll ), nVOffset, "#",     1 ); // Measurement number
        SetTextAlign( ps.hdc, TA_RIGHT | TA_TOP );                                      // Set text align attributes
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 11 - nHScroll ), nVOffset, "DEMAG", 5 ); // Demagnetization level
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 16 - nHScroll ), nVOffset, "CD",    2 ); // Core declination
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 22 - nHScroll ), nVOffset, "CI",    2 ); // Core inclination
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 28 - nHScroll ), nVOffset, "ISD",   3 ); // In situ declination
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 34 - nHScroll ), nVOffset, "ISI",   3 ); // In situ inclination
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 40 - nHScroll ), nVOffset, "RD",    2 ); // Rotated declination
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 46 - nHScroll ), nVOffset, "RI",    2 ); // Rotated inclination
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 53 - nHScroll ), nVOffset, "M",     1 ); // Total moment
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 63 - nHScroll ), nVOffset, "J",     1 ); // Magnetization
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 74 - nHScroll ), nVOffset, "X",     1 ); // X moment
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 84 - nHScroll ), nVOffset, "SX",    2 ); // X standard deviation
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 91 - nHScroll ), nVOffset, "NX",    2 ); // Number of X measurements
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 99 - nHScroll ), nVOffset, "EX",    2 ); // X percent error
        TextOut( ps.hdc, tm.tmAveCharWidth * (106 - nHScroll ), nVOffset, "Y",     1 ); // Y moment
        TextOut( ps.hdc, tm.tmAveCharWidth * (116 - nHScroll ), nVOffset, "SY",    2 ); // Y standard deviation
        TextOut( ps.hdc, tm.tmAveCharWidth * (123 - nHScroll ), nVOffset, "NY",    2 ); // Number of Y measurements
        TextOut( ps.hdc, tm.tmAveCharWidth * (131 - nHScroll ), nVOffset, "EY",    2 ); // Y percent error
        TextOut( ps.hdc, tm.tmAveCharWidth * (138 - nHScroll ), nVOffset, "Z",     1 ); // Z moment
        TextOut( ps.hdc, tm.tmAveCharWidth * (148 - nHScroll ), nVOffset, "SZ",    2 ); // Z standard deviation
        TextOut( ps.hdc, tm.tmAveCharWidth * (155 - nHScroll ), nVOffset, "NZ",    2 ); // Number of Z measurements
        TextOut( ps.hdc, tm.tmAveCharWidth * (163 - nHScroll ), nVOffset, "EZ",    2 ); // Z percent error
        TextOut( ps.hdc, tm.tmAveCharWidth * (169 - nHScroll ), nVOffset, "S/N",   3 ); // Signal to noise ratio
        TextOut( ps.hdc, tm.tmAveCharWidth * (177 - nHScroll ), nVOffset, "S/D",   3 ); // Signal to drift ratio
        TextOut( ps.hdc, tm.tmAveCharWidth * (185 - nHScroll ), nVOffset, "S/H",   3 ); // Signal to holder ratio

        // Measurements

        for( i = 1; i <= Acquire->ac_FileSummary.nCurrentStep; i++ ) {                  // Display results of each measurement
          nVOffset = tm.tmHeight * ( -nVScroll + 2 + i );                               // Vertical offset
          SetTextAlign( ps.hdc, TA_LEFT | TA_TOP );                                     // Set text align attributes
          wsprintf( achRecord, "%d", i );                                               // Set up file record name
          Acquire->ac_FocusFile->Load(    Acquire->ac_FocusFile, achRecord );           // Locate record in file
          Acquire->ac_FocusFile->CopyFrom(Acquire->ac_FocusFile, (LPSTR) &Acquire->ac_FileData, sizeof( SPECIMENDATA ) );                                             // Copy measurement data
          TextOut( ps.hdc, tm.tmAveCharWidth * (  0 - nHScroll ), nVOffset, achRecord,                             lstrlen( achRecord                            ) ); // Copy record name (also measurement number)
          SetTextAlign( ps.hdc, TA_RIGHT | TA_TOP );                                    // Set text align attributes
          TextOut( ps.hdc, tm.tmAveCharWidth * ( 11 - nHScroll ), nVOffset, Acquire->ac_FileData.achDemag,         lstrlen( Acquire->ac_FileData.achDemag        ) ); // Demagnetization level
          TextOut( ps.hdc, tm.tmAveCharWidth * ( 17 - nHScroll ), nVOffset, Acquire->ac_FileData.achCoreDec,       lstrlen( Acquire->ac_FileData.achCoreDec      ) ); // Core declination
          TextOut( ps.hdc, tm.tmAveCharWidth * ( 23 - nHScroll ), nVOffset, Acquire->ac_FileData.achCoreInc,       lstrlen( Acquire->ac_FileData.achCoreInc      ) ); // Core inclination
          TextOut( ps.hdc, tm.tmAveCharWidth * ( 29 - nHScroll ), nVOffset, Acquire->ac_FileData.achInSituDec,     lstrlen( Acquire->ac_FileData.achInSituDec    ) ); // In situ declination
          TextOut( ps.hdc, tm.tmAveCharWidth * ( 35 - nHScroll ), nVOffset, Acquire->ac_FileData.achInSituInc,     lstrlen( Acquire->ac_FileData.achInSituInc    ) ); // In situ inclination
          TextOut( ps.hdc, tm.tmAveCharWidth * ( 41 - nHScroll ), nVOffset, Acquire->ac_FileData.achRotatedDec,    lstrlen( Acquire->ac_FileData.achRotatedDec   ) ); // Rotated declination
          TextOut( ps.hdc, tm.tmAveCharWidth * ( 47 - nHScroll ), nVOffset, Acquire->ac_FileData.achRotatedInc,    lstrlen( Acquire->ac_FileData.achRotatedInc   ) ); // Rotated inclination
          TextOut( ps.hdc, tm.tmAveCharWidth * ( 57 - nHScroll ), nVOffset, Acquire->ac_FileData.achTotalMoment,   lstrlen( Acquire->ac_FileData.achTotalMoment  ) ); // Total moment
          TextOut( ps.hdc, tm.tmAveCharWidth * ( 67 - nHScroll ), nVOffset, Acquire->ac_FileData.achMagnetization, lstrlen( Acquire->ac_FileData.achMagnetization) ); // Magnetization
          TextOut( ps.hdc, tm.tmAveCharWidth * ( 78 - nHScroll ), nVOffset, Acquire->ac_FileData.achMomentX,       lstrlen( Acquire->ac_FileData.achMomentX      ) ); // X moment
          TextOut( ps.hdc, tm.tmAveCharWidth * ( 88 - nHScroll ), nVOffset, Acquire->ac_FileData.achSigmaX,        lstrlen( Acquire->ac_FileData.achSigmaX       ) ); // X moment standard deviation
          TextOut( ps.hdc, tm.tmAveCharWidth * ( 91 - nHScroll ), nVOffset, Acquire->ac_FileData.achNX,            lstrlen( Acquire->ac_FileData.achNX           ) ); // Number of X moment measurements
          TextOut( ps.hdc, tm.tmAveCharWidth * ( 99 - nHScroll ), nVOffset, Acquire->ac_FileData.achErrorX,        lstrlen( Acquire->ac_FileData.achErrorX       ) ); // X error
          TextOut( ps.hdc, tm.tmAveCharWidth * (110 - nHScroll ), nVOffset, Acquire->ac_FileData.achMomentY,       lstrlen( Acquire->ac_FileData.achMomentY      ) ); // Y moment
          TextOut( ps.hdc, tm.tmAveCharWidth * (120 - nHScroll ), nVOffset, Acquire->ac_FileData.achSigmaY,        lstrlen( Acquire->ac_FileData.achSigmaY       ) ); // Y moment standard deviation
          TextOut( ps.hdc, tm.tmAveCharWidth * (123 - nHScroll ), nVOffset, Acquire->ac_FileData.achNY,            lstrlen( Acquire->ac_FileData.achNY           ) ); // Number of Y moment measurements
          TextOut( ps.hdc, tm.tmAveCharWidth * (131 - nHScroll ), nVOffset, Acquire->ac_FileData.achErrorY,        lstrlen( Acquire->ac_FileData.achErrorY       ) ); // Y error
          TextOut( ps.hdc, tm.tmAveCharWidth * (142 - nHScroll ), nVOffset, Acquire->ac_FileData.achMomentZ,       lstrlen( Acquire->ac_FileData.achMomentZ      ) ); // Z moment
          TextOut( ps.hdc, tm.tmAveCharWidth * (152 - nHScroll ), nVOffset, Acquire->ac_FileData.achSigmaZ,        lstrlen( Acquire->ac_FileData.achSigmaZ       ) ); // Z moment standard deviation
          TextOut( ps.hdc, tm.tmAveCharWidth * (155 - nHScroll ), nVOffset, Acquire->ac_FileData.achNZ,            lstrlen( Acquire->ac_FileData.achNZ           ) ); // Number of Z moment measurements
          TextOut( ps.hdc, tm.tmAveCharWidth * (163 - nHScroll ), nVOffset, Acquire->ac_FileData.achErrorZ,        lstrlen( Acquire->ac_FileData.achErrorZ       ) ); // Z error
          TextOut( ps.hdc, tm.tmAveCharWidth * (171 - nHScroll ), nVOffset, Acquire->ac_FileData.achStoN,          lstrlen( Acquire->ac_FileData.achStoN         ) ); // Signal to noise ratio
          TextOut( ps.hdc, tm.tmAveCharWidth * (179 - nHScroll ), nVOffset, Acquire->ac_FileData.achStoD,          lstrlen( Acquire->ac_FileData.achStoD         ) ); // Signal to drift ratio
          TextOut( ps.hdc, tm.tmAveCharWidth * (187 - nHScroll ), nVOffset, Acquire->ac_FileData.achStoH,          lstrlen( Acquire->ac_FileData.achStoH         ) ); // Signal to holder ratio
          TextOut( ps.hdc, tm.tmAveCharWidth * (190 - nHScroll ), nVOffset, Acquire->ac_FileData.achZAxis,         lstrlen( Acquire->ac_FileData.achZAxis        ) ); // Direction along which Z was measured
          TextOut( ps.hdc, tm.tmAveCharWidth * (208 - nHScroll ), nVOffset, Acquire->ac_FileData.achTime,          lstrlen( Acquire->ac_FileData.achTime         ) ); // Date and time of measurement
          }
        }

      if( Acquire->ac_bMeasurePaint ) {                                 // Repaint the child window with the results of a measure cycle

        // Sample name, demagnetization level, and time of measurement

        SetTextAlign( ps.hdc, TA_LEFT | TA_TOP );                       // Set text align attributes
        strcpy( ach, Acquire->ac_SampleDData.achName );                 // Copy sample name
        strcat( ach, "     " );                                         // Append spaces
        strcat( ach, Acquire->ac_FileData.achDemag );                   // Append demagnetization level
        strcat( ach, "     " );                                         // Append spaces
        strcat( ach, Acquire->ac_FileData.achTime );                    // Append date and time
        TextOut(ps.hdc, tm.tmAveCharWidth * ( 0 - nHScroll ), tm.tmHeight * ( 0 - nVScroll ), ach, lstrlen( ach ) );

        // Measurements title, first and second background measurements, and total drift values

        SetTextAlign( ps.hdc, TA_CENTER | TA_TOP );                     // Text align attributes
        if( Acquire->ac_bMagXAxis ) {                                   // Magnetometer X axis
          nHOffset = tm.tmAveCharWidth * ( 18 - nHScroll );             // Horizontal offset
//        strcpy( ach, "Magnetometer X Axis" );                         // Copy caption
//        TextOut( ps.hdc, tm.tmAveCharWidth * ( 18 - nHScroll ), tm.tmHeight * ( 1 - nVScroll ), ach, lstrlen( ach ) );
          TextOut( ps.hdc, nHOffset, tm.tmHeight * ( 1 - nVScroll ), "Magnetometer X Axis", 19 );       // Copy caption
          Eprintf( ach, "%+.3e", Acquire->ac_adBGSignal[ 0 ] );         // X axis background
          TextOut( ps.hdc, nHOffset, tm.tmHeight * ( 2 - nVScroll ), ach, lstrlen( ach ) );
          Eprintf( ach, "%+.3e", Acquire->ac_adBGSignal[ 3 ] );         // X axis background
          TextOut( ps.hdc, nHOffset, tm.tmHeight * ( 4 + Acquire->ac_idMagSignal - nVScroll ), ach, lstrlen( ach ) );
          Eprintf( ach, "%+.3e", Acquire->ac_adDrift[ 0 ] );            // X axis drift
          TextOut( ps.hdc, nHOffset, tm.tmHeight * ( 5 + Acquire->ac_idMagSignal - nVScroll ), ach, lstrlen( ach ) );
          }
        if( Acquire->ac_bMagYAxis ) {                                   // Magnetometer Y axis
          nHOffset = tm.tmAveCharWidth * ( 44 - nHScroll );             // Horizontal offset
//        strcpy( ach, "Magnetometer Y Axis" );                         // Copy caption
//        TextOut( ps.hdc, tm.tmAveCharWidth * ( 44 - nHScroll ), tm.tmHeight * ( 1 - nVScroll ), ach, lstrlen( ach ) );
          TextOut( ps.hdc, nHOffset, tm.tmHeight * ( 1 - nVScroll ), "Magnetometer Y Axis", 19 );       // Copy caption
          Eprintf( ach, "%+.3e", Acquire->ac_adBGSignal[ 1 ] );         // Y axis background
          TextOut( ps.hdc, nHOffset, tm.tmHeight * ( 2 - nVScroll), ach, lstrlen( ach ) );
          Eprintf( ach, "%+.3e", Acquire->ac_adBGSignal[ 4 ] );         // Y axis background
          TextOut( ps.hdc, nHOffset, tm.tmHeight * ( 4 + Acquire->ac_idMagSignal - nVScroll ), ach, lstrlen( ach ) );
          Eprintf( ach, "%+.3e", Acquire->ac_adDrift[ 1 ] );            // Y axis drift
          TextOut( ps.hdc, nHOffset, tm.tmHeight * ( 5 + Acquire->ac_idMagSignal - nVScroll ), ach, lstrlen( ach ) );
          }
        if( Acquire->ac_bMagZAxis ) {                                   // Magnetometer Z axis
          nHOffset = tm.tmAveCharWidth * ( 70 - nHScroll );             // Horizontal offset
//        strcpy( ach, "Magnetometer Z Axis" );                         // Copy caption
//        TextOut( ps.hdc, tm.tmAveCharWidth * ( 70 - nHScroll ), tm.tmHeight * ( 1 - nVScroll ), ach, lstrlen( ach ) );
          TextOut( ps.hdc, nHOffset, tm.tmHeight * ( 1 - nVScroll ), "Magnetometer Z Axis", 19 );       // Copy caption
          Eprintf( ach, "%+.3e", Acquire->ac_adBGSignal[ 2 ] );         // Z axis background
          TextOut( ps.hdc, nHOffset, tm.tmHeight * ( 2 - nVScroll ), ach, lstrlen( ach ) );
          Eprintf( ach, "%+.3e", Acquire->ac_adBGSignal[ 5 ] );         // Z axis background
          TextOut( ps.hdc, nHOffset, tm.tmHeight * ( 4 + Acquire->ac_idMagSignal - nVScroll ), ach, lstrlen( ach ) );
          Eprintf( ach, "%+.3e", Acquire->ac_adDrift[ 2 ] );            // Z axis drift
          TextOut( ps.hdc, nHOffset, tm.tmHeight * ( 5 + Acquire->ac_idMagSignal - nVScroll ), ach, lstrlen( ach ) );
          }

        // Sample measurements

        for( i = 0; i <= Acquire->ac_idMagSignal; i++ ) {               // Loop once for each measurement, to current measurement
          nVOffset = tm.tmHeight * ( 3 + i - nVScroll );                // Vertical offset
          SetTextAlign( ps.hdc, TA_LEFT | TA_TOP );                     // Text align attributes
          sprintf( ach, "%d", i );                                      // Measurement number label
          TextOut( ps.hdc, tm.tmAveCharWidth * ( 2 - nHScroll ), nVOffset, ach, lstrlen( ach ) );
          SetTextAlign( ps.hdc, TA_CENTER | TA_TOP );                   // Text align attributes
          if( Acquire->ac_bMagXAxis ) {                                 // Magnetometer X axis
            Eprintf( ach, "%+.3e", Acquire->ac_adMagXSignal[ i ] );     // X axis measurement
            TextOut( ps.hdc, tm.tmAveCharWidth * ( 18 - nHScroll ), nVOffset, ach, lstrlen( ach ) );
            }
          if( Acquire->ac_bMagYAxis ) {                                 // Magnetometer Y axis
            Eprintf( ach, "%+.3e", Acquire->ac_adMagYSignal[ i ] );     // Y axis measurement
            TextOut( ps.hdc, tm.tmAveCharWidth * ( 44 - nHScroll ), nVOffset, ach, lstrlen( ach ) );
            }
          if( Acquire->ac_bMagZAxis ) {                                 // Magnetometer Z axis
            Eprintf( ach, "%+.3e", Acquire->ac_adMagZSignal[ i ] );     // Z axis measurement
            TextOut( ps.hdc, tm.tmAveCharWidth * ( 70 - nHScroll ), nVOffset, ach, lstrlen( ach ) );
            }
          }

        // Labels

        nHOffset = tm.tmAveCharWidth * ( 2 - nHScroll );                // Horizontal offset
        SetTextAlign( ps.hdc, TA_LEFT | TA_TOP );                       // Text align attributes
        TextOut( ps.hdc, nHOffset, tm.tmHeight * ( 2 - nVScroll ), "BG", 2 );                                // Copy caption
        TextOut( ps.hdc, nHOffset, tm.tmHeight * ( 4 + Acquire->ac_idMagSignal - nVScroll ), "BG",      2 ); // Copy caption
        TextOut( ps.hdc, nHOffset, tm.tmHeight * ( 5 + Acquire->ac_idMagSignal - nVScroll ), "Drift",   5 ); // Copy caption
        TextOut( ps.hdc, nHOffset, tm.tmHeight * ( 8 + Acquire->ac_idMagSignal - nVScroll ), "Moment",  6 ); // Copy caption
        TextOut( ps.hdc, nHOffset, tm.tmHeight * ( 9 + Acquire->ac_idMagSignal - nVScroll ), "Std dev", 7 ); // Copy caption
        TextOut( ps.hdc, nHOffset, tm.tmHeight * (10 + Acquire->ac_idMagSignal - nVScroll ), "N",       1 ); // Copy caption
        TextOut( ps.hdc, nHOffset, tm.tmHeight * (11 + Acquire->ac_idMagSignal - nVScroll ), "Error",   5 ); // Copy caption
        TextOut( ps.hdc, nHOffset, tm.tmHeight * (14 + Acquire->ac_idMagSignal - nVScroll ), "Core",    4 ); // Copy caption
        TextOut( ps.hdc, nHOffset, tm.tmHeight * (15 + Acquire->ac_idMagSignal - nVScroll ), "In situ", 7 ); // Copy caption
        TextOut( ps.hdc, nHOffset, tm.tmHeight * (16 + Acquire->ac_idMagSignal - nVScroll ), "Rotated", 7 ); // Copy caption
        TextOut( ps.hdc, nHOffset, tm.tmHeight * (19 + Acquire->ac_idMagSignal - nVScroll ), "S/N",     3 ); // Copy caption
        TextOut( ps.hdc, nHOffset, tm.tmHeight * (20 + Acquire->ac_idMagSignal - nVScroll ), "S/D",     3 ); // Copy caption
        TextOut( ps.hdc, nHOffset, tm.tmHeight * (21 + Acquire->ac_idMagSignal - nVScroll ), "S/H",     3 ); // Copy caption
/*
        strcpy( ach, "BG" );                                                                    // Copy caption
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 2 - nHScroll ), tm.tmHeight * ( 2 - nVScroll ), ach, lstrlen( ach ) );
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 2 - nHScroll ), tm.tmHeight * ( 4 + Acquire->ac_idMagSignal - nVScroll ), ach, lstrlen( ach ) );
        strcpy( ach, "Drift" );                                                                 // Copy caption
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 2 - nHScroll ), tm.tmHeight * ( 5 + Acquire->ac_idMagSignal - nVScroll ), ach, lstrlen( ach ) );
        strcpy( ach, "Moment" );                                                                // Copy caption
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 2 - nHScroll ), tm.tmHeight * ( 8 + Acquire->ac_idMagSignal - nVScroll ), ach, lstrlen( ach ) );
        strcpy( ach, "Std dev" );                                                               // Copy caption
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 2 - nHScroll ), tm.tmHeight * ( 9 + Acquire->ac_idMagSignal - nVScroll ), ach, lstrlen( ach ) );
        strcpy( ach, "N" );                                                                     // Copy caption
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 2 - nHScroll ), tm.tmHeight * (10 + Acquire->ac_idMagSignal - nVScroll ), ach, lstrlen( ach ) );
        strcpy( ach, "Error" );                                                                 // Copy caption
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 2 - nHScroll ), tm.tmHeight * (11 + Acquire->ac_idMagSignal - nVScroll ), ach, lstrlen( ach ) );
        strcpy( ach, "Core" );                                                                  // Copy caption
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 2 - nHScroll ), tm.tmHeight * (14 + Acquire->ac_idMagSignal - nVScroll ), ach, lstrlen( ach ) );
        strcpy( ach, "In situ" );                                                               // Copy caption
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 2 - nHScroll ), tm.tmHeight * (15 + Acquire->ac_idMagSignal - nVScroll ), ach, lstrlen( ach ) );
        strcpy( ach, "Rotated" );                                                               // Copy caption
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 2 - nHScroll ), tm.tmHeight * (16 + Acquire->ac_idMagSignal - nVScroll ), ach, lstrlen( ach ) );
        strcpy( ach, "S/N" );                                                                   // Copy caption
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 2 - nHScroll ), tm.tmHeight * (19 + Acquire->ac_idMagSignal - nVScroll ), ach, lstrlen( ach ) );
        strcpy( ach, "S/D" );                                                                   // Copy caption
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 2 - nHScroll ), tm.tmHeight * (20 + Acquire->ac_idMagSignal - nVScroll ), ach, lstrlen( ach ) );
        strcpy( ach, "S/H" );                                                                   // Copy caption
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 2 - nHScroll ), tm.tmHeight * (21 + Acquire->ac_idMagSignal - nVScroll ), ach, lstrlen( ach ) );
*/
        // Components title

        nVOffset = tm.tmHeight * ( 7 + Acquire->ac_idMagSignal - nVScroll );                    // Vertical offset
        SetTextAlign( ps.hdc, TA_CENTER | TA_TOP );                                             // Text align attributes
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 18 - nHScroll ), nVOffset, "Sample X Axis", 13 );// Copy caption
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 44 - nHScroll ), nVOffset, "Sample Y Axis", 13 );// Copy caption
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 70 - nHScroll ), nVOffset, "Sample Z Axis", 13 );// Copy caption
/*
        strcpy( ach, "Sample X Axis" );                                                         // Copy caption
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 18 - nHScroll ), tm.tmHeight * ( 7 + Acquire->ac_idMagSignal - nVScroll ), ach, lstrlen( ach ) );
        strcpy( ach, "Sample Y Axis" );                                                         // Copy caption
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 44 - nHScroll ), tm.tmHeight * ( 7 + Acquire->ac_idMagSignal - nVScroll ), ach, lstrlen( ach ) );
        strcpy( ach, "Sample Z Axis" );                                                         // Copy caption
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 70 - nHScroll ), tm.tmHeight * ( 7 + Acquire->ac_idMagSignal - nVScroll ), ach, lstrlen( ach ) );
*/
        // Moments

        nVOffset = tm.tmHeight * ( 8 + Acquire->ac_idMagSignal - nVScroll );                    // Vertical offset
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 18 - nHScroll ), nVOffset, Acquire->ac_FileData.achMomentX, lstrlen( Acquire->ac_FileData.achMomentX ) );// X moment
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 44 - nHScroll ), nVOffset, Acquire->ac_FileData.achMomentY, lstrlen( Acquire->ac_FileData.achMomentY ) );// Y moment
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 70 - nHScroll ), nVOffset, Acquire->ac_FileData.achMomentZ, lstrlen( Acquire->ac_FileData.achMomentZ ) );// Z moment
/*
        strcpy( ach, Acquire->ac_FileData.achMomentX );                                         // X moment
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 18 - nHScroll ), tm.tmHeight * ( 8 + Acquire->ac_idMagSignal - nVScroll ), ach, lstrlen( ach ) );
        strcpy( ach, Acquire->ac_FileData.achMomentY );                                         // Y moment
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 44 - nHScroll ), tm.tmHeight * ( 8 + Acquire->ac_idMagSignal - nVScroll ), ach, lstrlen( ach ) );
        strcpy( ach, Acquire->ac_FileData.achMomentZ );                                         // Z moment
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 70 - nHScroll ), tm.tmHeight * ( 8 + Acquire->ac_idMagSignal - nVScroll ), ach, lstrlen( ach ) );
*/
        // Standard deviation

        nVOffset = tm.tmHeight * ( 9 + Acquire->ac_idMagSignal - nVScroll );                    // Vertical offset
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 18 - nHScroll ), nVOffset, Acquire->ac_FileData.achSigmaX, lstrlen( Acquire->ac_FileData.achSigmaX ) );  // X standard deviation
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 44 - nHScroll ), nVOffset, Acquire->ac_FileData.achSigmaY, lstrlen( Acquire->ac_FileData.achSigmaY ) );  // Y standard deviation
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 70 - nHScroll ), nVOffset, Acquire->ac_FileData.achSigmaZ, lstrlen( Acquire->ac_FileData.achSigmaZ ) );  // Z standard deviation
/*
        strcpy( ach, Acquire->ac_FileData.achSigmaX );                                          // X standard deviation
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 18 - nHScroll ), tm.tmHeight * ( 9 + Acquire->ac_idMagSignal - nVScroll ), ach, lstrlen( ach ) );
        strcpy( ach, Acquire->ac_FileData.achSigmaY );                                          // Y standard deviation
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 44 - nHScroll ), tm.tmHeight * ( 9 + Acquire->ac_idMagSignal - nVScroll ), ach, lstrlen( ach ) );
        strcpy( ach, Acquire->ac_FileData.achSigmaZ );                                          // Z standard deviation
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 70 - nHScroll ), tm.tmHeight * ( 9 + Acquire->ac_idMagSignal - nVScroll ), ach, lstrlen( ach ) );
*/
        // N

        nVOffset = tm.tmHeight * ( 10 + Acquire->ac_idMagSignal - nVScroll );                   // Vertical offset
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 18 - nHScroll ), nVOffset, Acquire->ac_FileData.achNX, lstrlen(Acquire->ac_FileData.achNX ) );   // Number of X measurements
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 44 - nHScroll ), nVOffset, Acquire->ac_FileData.achNY, lstrlen(Acquire->ac_FileData.achNY ) );   // Number of Y measurements
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 70 - nHScroll ), nVOffset, Acquire->ac_FileData.achNZ, lstrlen(Acquire->ac_FileData.achNZ ) );   // Number of Z measurements
/*
        strcpy( ach, Acquire->ac_FileData.achNX );                                              // Number of X measurements
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 18 - nHScroll ), tm.tmHeight * ( 10 + Acquire->ac_idMagSignal - nVScroll ), ach, lstrlen( ach ) );
        strcpy( ach, Acquire->ac_FileData.achNY );                                              // Number of Y measurements
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 44 - nHScroll ), tm.tmHeight * ( 10 + Acquire->ac_idMagSignal - nVScroll ), ach, lstrlen( ach ) );
        strcpy( ach, Acquire->ac_FileData.achNZ );                                              // Number of Z measurements
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 70 - nHScroll ), tm.tmHeight * ( 10 + Acquire->ac_idMagSignal - nVScroll ), ach, lstrlen( ach ) );
*/
        // Percent error

        nVOffset = tm.tmHeight * ( 11 + Acquire->ac_idMagSignal - nVScroll );                   // Vertical offset
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 18 - nHScroll ), nVOffset, Acquire->ac_FileData.achErrorX, lstrlen( Acquire->ac_FileData.achErrorX ) );  // Percent X error
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 44 - nHScroll ), nVOffset, Acquire->ac_FileData.achErrorY, lstrlen( Acquire->ac_FileData.achErrorY ) );  // Percent Y error
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 70 - nHScroll ), nVOffset, Acquire->ac_FileData.achErrorZ, lstrlen( Acquire->ac_FileData.achErrorZ ) );  // Percent Z error
/*
        strcpy( ach, Acquire->ac_FileData.achErrorX );
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 18 - nHScroll ), tm.tmHeight * ( 11 + Acquire->ac_idMagSignal - nVScroll ), ach, lstrlen( ach ) );
        strcpy( ach, Acquire->ac_FileData.achErrorY );
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 44 - nHScroll ), tm.tmHeight * ( 11 + Acquire->ac_idMagSignal - nVScroll ), ach, lstrlen( ach ) );
        strcpy( ach, Acquire->ac_FileData.achErrorZ );
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 70 - nHScroll ), tm.tmHeight * ( 11 + Acquire->ac_idMagSignal - nVScroll ), ach, lstrlen( ach ) );
*/
        // Directions title

        nVOffset = tm.tmHeight * ( 13 + Acquire->ac_idMagSignal - nVScroll );                           // Vertical offset
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 18 - nHScroll ), nVOffset, "Declination",   11 );        // Copy caption
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 35 - nHScroll ), nVOffset, "Inclination",   11 );        // Copy caption
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 53 - nHScroll ), nVOffset, "Moment",         6 );        // Copy caption
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 70 - nHScroll ), nVOffset, "Magnetization", 13 );        // Copy caption
/*
        strcpy( ach, "Declination"   );                                                                 // Copy caption
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 18 - nHScroll ), tm.tmHeight * ( 13 + Acquire->ac_idMagSignal - nVScroll ), ach, lstrlen( ach ) );
        strcpy( ach, "Inclination"   );                                                                 // Copy caption
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 35 - nHScroll ), tm.tmHeight * ( 13 + Acquire->ac_idMagSignal - nVScroll ), ach, lstrlen( ach ) );
        strcpy( ach, "Moment"        );	                                                                // Copy caption
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 53 - nHScroll ), tm.tmHeight * ( 13 + Acquire->ac_idMagSignal - nVScroll ), ach, lstrlen( ach ) );
        strcpy( ach, "Magnetization" );	                                                                // Copy caption
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 70 - nHScroll ), tm.tmHeight * ( 13 + Acquire->ac_idMagSignal - nVScroll ), ach, lstrlen( ach ) );
*/
        // Core

        nVOffset = tm.tmHeight * ( 14 + Acquire->ac_idMagSignal - nVScroll );   // Vertical offset
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 18 - nHScroll ), nVOffset, Acquire->ac_FileData.achCoreDec,       lstrlen( Acquire->ac_FileData.achCoreDec      ) ); // Declination
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 35 - nHScroll ), nVOffset, Acquire->ac_FileData.achCoreInc,       lstrlen( Acquire->ac_FileData.achCoreInc      ) ); // Inclination
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 53 - nHScroll ), nVOffset, Acquire->ac_FileData.achTotalMoment,   lstrlen( Acquire->ac_FileData.achTotalMoment  ) ); // Moment
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 70 - nHScroll ), nVOffset, Acquire->ac_FileData.achMagnetization, lstrlen( Acquire->ac_FileData.achMagnetization) ); // Magnetization
/*
        strcpy( ach, Acquire->ac_FileData.achCoreDec );                         // Declination
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 18 - nHScroll ), tm.tmHeight * ( 14 + Acquire->ac_idMagSignal - nVScroll ), ach, lstrlen( ach ) );
        strcpy( ach, Acquire->ac_FileData.achCoreInc );                         // Inclination
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 35 - nHScroll ), tm.tmHeight * ( 14 + Acquire->ac_idMagSignal - nVScroll ), ach, lstrlen( ach ) );
        strcpy( ach, Acquire->ac_FileData.achTotalMoment );                     // Moment
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 53 - nHScroll ), tm.tmHeight * ( 14 + Acquire->ac_idMagSignal - nVScroll ), ach, lstrlen( ach ) );
//      TextOut( ps.hdc, tm.tmAveCharWidth * ( 53 - nHScroll ), tm.tmHeight * ( 15 + Acquire->ac_idMagSignal - nVScroll ), ach, lstrlen( ach ) );
//      TextOut( ps.hdc, tm.tmAveCharWidth * ( 53 - nHScroll ), tm.tmHeight * ( 16 + Acquire->ac_idMagSignal - nVScroll ), ach, lstrlen( ach ) );
        strcpy( ach, Acquire->ac_FileData.achMagnetization );                   // Magnetization
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 70 - nHScroll ), tm.tmHeight * ( 14 + Acquire->ac_idMagSignal - nVScroll ), ach, lstrlen( ach ) );
//      TextOut( ps.hdc, tm.tmAveCharWidth * ( 70 - nHScroll ), tm.tmHeight * ( 15 + Acquire->ac_idMagSignal - nVScroll ), ach, lstrlen( ach ) );
//      TextOut( ps.hdc, tm.tmAveCharWidth * ( 70 - nHScroll ), tm.tmHeight * ( 16 + Acquire->ac_idMagSignal - nVScroll ), ach, lstrlen( ach ) );
*/
        // In situ

        nVOffset = tm.tmHeight * ( 15 + Acquire->ac_idMagSignal - nVScroll );   // Vertical offset
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 18 - nHScroll ), nVOffset, Acquire->ac_FileData.achInSituDec, lstrlen( Acquire->ac_FileData.achInSituDec ) );    // Declination
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 35 - nHScroll ), nVOffset, Acquire->ac_FileData.achInSituInc, lstrlen( Acquire->ac_FileData.achInSituInc ) );    // Inclination
/*
        strcpy( ach, Acquire->ac_FileData.achInSituDec );                       // Declination
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 18 - nHScroll ), tm.tmHeight * ( 15 + Acquire->ac_idMagSignal - nVScroll ), ach, lstrlen( ach ) );
        strcpy( ach, Acquire->ac_FileData.achInSituInc );                       // Inclination
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 35 - nHScroll ), tm.tmHeight * ( 15 + Acquire->ac_idMagSignal - nVScroll ), ach, lstrlen( ach ) );
*/
        // Rotated

        nVOffset = tm.tmHeight * ( 16 + Acquire->ac_idMagSignal - nVScroll );   // Vertical offset
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 18 - nHScroll ), nVOffset, Acquire->ac_FileData.achRotatedDec, lstrlen( Acquire->ac_FileData.achRotatedDec ) );  // Declination
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 35 - nHScroll ), nVOffset, Acquire->ac_FileData.achRotatedInc, lstrlen( Acquire->ac_FileData.achRotatedInc ) );  // Inclination
/*
        strcpy( ach, Acquire->ac_FileData.achRotatedDec );                      // Declination
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 18 - nHScroll ), tm.tmHeight * ( 16 + Acquire->ac_idMagSignal - nVScroll ), ach, lstrlen( ach ) );
        strcpy( ach, Acquire->ac_FileData.achRotatedInc );                      // Inclination
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 35 - nHScroll ), tm.tmHeight * ( 16 + Acquire->ac_idMagSignal - nVScroll ), ach, lstrlen( ach ) );
*/
        // Error title

        nHOffset = tm.tmAveCharWidth * ( 18 - nHScroll );                       // Horizontal offset
        TextOut( ps.hdc, nHOffset, tm.tmHeight * ( 18 + Acquire->ac_idMagSignal - nVScroll ), "Error", 5 );     // Copy caption
/*
        strcpy( ach, "Error" );                                                 // Copy caption
        TextOut( ps.hdc, tm.tmAveCharWidth * ( 18 - nHScroll ), tm.tmHeight * ( 18 + Acquire->ac_idMagSignal - nVScroll ), ach, lstrlen( ach ) );
*/
        // S/N

        TextOut( ps.hdc, nHOffset, tm.tmHeight * ( 19 + Acquire->ac_idMagSignal - nVScroll ), Acquire->ac_FileData.achStoN, lstrlen( Acquire->ac_FileData.achStoN ) );  // Ratio
//      strcpy( ach, Acquire->ac_FileData.achStoN );                            // Ratio
//      TextOut( ps.hdc, tm.tmAveCharWidth * ( 18 - nHScroll ), tm.tmHeight * ( 19 + Acquire->ac_idMagSignal - nVScroll ), ach, lstrlen( ach ) );

        // S/D

        TextOut(ps.hdc, nHOffset, tm.tmHeight * ( 20 + Acquire->ac_idMagSignal - nVScroll ), Acquire->ac_FileData.achStoD, lstrlen(Acquire->ac_FileData.achStoD ) );    // Ratio
//      strcpy( ach, Acquire->ac_FileData.achStoD );                            // Ratio
//      TextOut( ps.hdc, tm.tmAveCharWidth * ( 18 - nHScroll ), tm.tmHeight * ( 20 + Acquire->ac_idMagSignal - nVScroll ), ach, lstrlen( ach ) );

        // S/H

        TextOut( ps.hdc, nHOffset, tm.tmHeight * ( 21 + Acquire->ac_idMagSignal - nVScroll ), Acquire->ac_FileData.achStoH, lstrlen( Acquire->ac_FileData.achStoH ) );  // Ratio
//      strcpy( ach, Acquire->ac_FileData.achStoH );        // Ratio
//      TextOut( ps.hdc, tm.tmAveCharWidth * ( 18 - nHScroll ), tm.tmHeight * ( 21 + Acquire->ac_idMagSignal - nVScroll ), ach, lstrlen( ach ) );
        }
//    }
//  EndPaint( hWindowr, &ps );

    if( wParam == 0U ) {                                                // Draw to screen
      EndPaint( hWindow, &ps );
      }
    else {                                                              // Draw to paper
      SelectObject( ps.hdc, hOldFont );
      DeleteObject( hFont );
      hFont = (HFONT) NULL;
      Escape( hdcPRN, NEWFRAME, 0, NULL, NULL );                        // Eject the page
      Escape( hdcPRN, ENDDOC,   0, NULL, NULL );                        // End the document
      FreeProcInstance( AbortInst );                                    // Release the abort procedure
//    EnableWindow( hWindow, TRUE );                                    // Allow screen I/O
      DeleteDC( hdcPRN );                                               // Release device context for printer output
      SetCursor(hcrArrow );                                             // Change cursor back to an arrow
      }
    }
  }


// TMyMDIChild Class Member - Move sample handler to load position

void TMyMDIChildWMLoad( ACQUIRE *Acquire, HWND hWindow ) {

  if( ( Acquire != (ACQUIRE *) NULL ) &&
      ( hWindow != (HWND)      NULL ) ) {

    // Move sample handler to load position

    if( Acquire->ac_bSHAuto ) {                                                                         // Automatic
      if( Acquire->ac_bSHTrans ) {                                                                      // Automatic translation
        Acquire->ac_bTHome = Translate( Acquire, hWindow, Acquire->ac_bTHome, Acquire->ac_lTHome );     // Translate to home position first
        Acquire->ac_bTLoad = Translate( Acquire, hWindow, Acquire->ac_bTLoad, Acquire->ac_lTLoad );     // Translate to load position
        }
      else Acquire->ac_bCancelCycle = MyTranslateMessage( hWindow, "Load");

      if( Acquire->ac_bSHRot )                                                                          // Automatic rotation
           Acquire->ac_bRHome       = Rotate( Acquire, hWindow, Acquire->ac_bRHome, Acquire->ac_nRHome );// Rotate to 0 position
      else Acquire->ac_bCancelCycle = RotateMessage( hWindow, "0");
      }
    else {                                                                                              // Manual
      Acquire->ac_bCancelCycle = MyTranslateMessage(hWindow, "Load" );
      Acquire->ac_bCancelCycle = RotateMessage(     hWindow, "0"    );
      }
    }
  }


// TMyMDIChild Class Member - Single measurement

void TMyMDIChildCMSingle( ACQUIRE *Acquire, HWND hWindow ) {

  int nRepeat = 0;
  char achRecord[ 4 ], achDemag[ 8 ];
  BOOL bAF = FALSE, bRepeat = FALSE;

  if( ( Acquire != (ACQUIRE *) NULL ) &&
      ( hWindow != (HWND)      NULL ) ) {


    Log( "-- TMyMDIChildCMSingle \n" );

    // Clear cancel measurement flag

    Acquire->ac_bCancelCycle = FALSE;

    // Move sample handler to load position

    SendMessage( hWindow, UM_LOAD, 0, 0 );                                                      // Send a load message directly to the message function
/*
    // Move sample handler to load position

    if( Acquire->ac_bSHAuto ) {                                                                 // Automatic
      if( Acquire->ac_bSHTrans ) {                                                              // Automatic translation
        Acquire->ac_bTHome = Translate( hWindow, Acquire->ac_bTHome, Acquire->ac_lTHome );      // Translate to home position first
        Acquire->ac_bTLoad = Translate( hWindow, Acquire->ac_bTLoad, Acquire->ac_lTLoad );      // Translate to load position
        }
      else TranslateMessage( hWindow, "Load" );
      if( Acquire->ac_bSHRot )                                                                  // Automatic rotation
        Acquire->ac_bRHome = Rotate(  hWindow, Acquire->ac_bRHome, Acquire->ac_nRHome );        // Rotate to 0 position
      else RotateMessage( hWindow, "0" );
      }
    else {                                                                                      // Manual
      TranslateMessage( hWindow, "Load" );
      RotateMessage(    hWindow, "0"    );
      }
*/
    // Start the Measure Single Step dialog box

    if( DialogBoxParam( Acquire->ac_hInstance, MAKEINTRESOURCE( MeasureSingle_Dialog ), Acquire->ac_hMainWindow, TSingleDialogProcess, (long) Acquire ) == IDOK ) {
      if( Acquire->ac_CancelDialog )                                    // If the cancel measurement cycle dialog box has already been created
        SetActiveWindow( Acquire->ac_CancelDialog );                    // Make it active

      // Create it and make it active

      else {                                                            // If it has not been created
        Acquire->ac_CancelDialog = CreateDialogParam( Acquire->ac_hInstance, MAKEINTRESOURCE( Cancel_Dialog ), Acquire->ac_hMainWindow, TCancelDialogProcess, (long) Acquire );
        }

      // Load standard

      if( Acquire->ac_bMeasureNegZ )
           LoadMessage( hWindow, "-Z Position" );
      else LoadMessage( hWindow, "+Z Position" );

      // Initialize structure and variables

      memset( &Acquire->ac_FileData, 0x0, sizeof( Acquire->ac_FileData ) );             // Clear disk file measurement data structure
      Acquire->ac_bFilePaint    = FALSE;                                                // Paint measurements to screen
      Acquire->ac_bMeasurePaint = TRUE;
      Acquire->ac_idMagSignal   = -1;                                                   // Signal array index

      // Retrieve dialog data

      if( Acquire->ac_SingleDData.dwAF ) {                                              // AF demagnetization
        strcpy( Acquire->ac_FileData.achDemag, Acquire->ac_SingleDData.achField );      // Demagnetization string for disk file
        strcat( Acquire->ac_FileData.achDemag, "mT" );
        Acquire->ac_dAFLevel = atof( Acquire->ac_SingleDData.achField );                // AF level
        bAF = TRUE;
        }
      else if( Acquire->ac_SingleDData.dwThermal ) {                                    // Thermal demagnetization
        strcpy( Acquire->ac_FileData.achDemag, Acquire->ac_SingleDData.achTemp );       // Demagnetization string for disk file
        strcat( Acquire->ac_FileData.achDemag, "C" );
        }
//    else if( SingleDData.dwNone )                                     // No demagnetization
      else                                                              // No demagnetization
        strcpy( Acquire->ac_FileData.achDemag, "NRM" );                 // Demagnetization string for disk file
      strcpy( achDemag, Acquire->ac_FileData.achDemag );                // Save demagnetization string

      // Demagnetization

      if( bAF && Acquire->ac_bAFAuto )                                  // If sample is to be automatically AF demagnetized
        SendMessage( hWindow, UM_DEMAGNETIZE, 0, 0 );                   // Send a demagnetize message directly to the message function

      do {                                                              // Threshold and uprange loop

        // Measurement

        SendMessage( hWindow, UM_MEASURE, 0, 0 );                       // Send a measure message directly to the message function

        // Data reduction

        SendMessage( hWindow, UM_REDUCE, 0, 0 );                        // Send a reduce message directly to the message function
        if( !Acquire->ac_bCancelCycle ) {                               // Continue only if measurement cycle has not been canceled

          // Threshold repeat

          nRepeat += 1;                                                 // Increment repeat counter
//        if( atof( Acquire->ac_FileData.achStoN ) < atof( Acquire->ac_MeasureDData.achSN ) ) { // If the S/N threshold is not met
          if( atof( Acquire->ac_FileData.achStoN ) < Acquire->ac_dStoNThreshold ) {             // If the S/N threshold is not met
            if( Acquire->ac_MeasureDData.dwSNR )                        // Repeat measurement
              bRepeat = TRUE;
            else                                                        // Notify user
              NotifyMessage( hWindow, "S/N Value" );
            }
//        if( atof( Acquire->ac_MeasureDData.achDrift ) < Magnitude( adDrift[ 0 ], adDrift[ 1 ], adDrift[ 2 ] ) ) { // If the drift threshold is not met
          if( atof( Acquire->ac_FileData.achStoD ) < Acquire->ac_dDriftThreshold ) {    // If the drift threshold is not met
            if( Acquire->ac_MeasureDData.dwDR )                         // Repeat measurement
              bRepeat = TRUE;
            else                                                        // Notify user
              NotifyMessage( hWindow, "Drift Value" );
            }

          // Uprange repeat

          if( Acquire->ac_bUpRange ) {
            if( Acquire->ac_RangeUpDialog )                             // If the magnetometer automatic range up dialog box has already been created
              SetActiveWindow( Acquire->ac_RangeUpDialog );             // Make it active

            // Create it and make it active

            else {                                                      // If it has not been created
              Acquire->ac_RangeUpDialog = CreateDialogParam( Acquire->ac_hInstance, MAKEINTRESOURCE( RangeUp_Dialog ), Acquire->ac_hMainWindow, TRangeUpDialogProcess, (long) Acquire );
              }
            Pause( 2970 );
            if( Acquire->ac_RangeUpDialog ) {                           // If magnetometer automatic range up dialog box still exists
              DestroyWindow( Acquire->ac_RangeUpDialog );               // Close it
              Acquire->ac_RangeUpDialog = (HWND) NULL;                  // And clear pointer
              }
            if( !MagRangeUp( Acquire, hWindow, Acquire->ac_nMagComID, "X" ) ) { // Range up
              Acquire->ac_bUpRange = FALSE;                             // If sample is too strong
              bRepeat  = FALSE;
              }
            if( !MagRangeUp( Acquire, hWindow, Acquire->ac_nMagComID, "Y" ) ) {
              Acquire->ac_bUpRange = FALSE;
              bRepeat  = FALSE;
              }
            if( !MagRangeUp( Acquire, hWindow, Acquire->ac_nMagComID, "Z" ) ) {
              Acquire->ac_bUpRange = FALSE;
              bRepeat  = FALSE;
              }
            Acquire->ac_idMagSignal               = -1;                 // Reset signal array index
            Acquire->ac_FileSummary.nCurrentStep -=  1;                 // Decrement measurement step index
            bRepeat = FALSE;                                            // Clear repeat flag
            nRepeat = 0;                                                // Reset repeat index
            memset( &Acquire->ac_FileData, 0x0, sizeof( Acquire->ac_FileData ) );                       // Clear disk file measurement data structure
            strcpy( Acquire->ac_FileData.achDemag, achDemag );                                          // Retrieve demagnetization string
            }
          //Acquire->ac_FocusFile->HardFlush( Acquire->ac_FocusFile );                                    // flush out the file. 
          //CopyTheFile( Acquire->ac_achTitleStr, Acquire->ac_achFocusFile );                             // copy the flushed file to the data file.    



          if( bRepeat && ( nRepeat < 2 ) ) {                                                            // If measurement is to be repeated
            Acquire->ac_FocusFile->Save(  Acquire->ac_FocusFile, "-1", sizeof( SPECIMENSUMMARY ) );     // Save specimen summary record number to disk
            Acquire->ac_FocusFile->CopyTo(Acquire->ac_FocusFile, ( LPSTR) &Acquire->ac_FileSummary, sizeof( SPECIMENSUMMARY ) );// Save specimen summary data to disk
            sprintf(achRecord, "%d", Acquire->ac_FileSummary.nCurrentStep );                            // Set up record header string                         
            Acquire->ac_FocusFile->Save(  Acquire->ac_FocusFile, achRecord, sizeof( SPECIMENDATA ) );   // Save measurement data record number to disk
            Acquire->ac_FocusFile->CopyTo(Acquire->ac_FocusFile, (LPSTR) &Acquire->ac_FileData, sizeof( SPECIMENDATA ) );       // Save measurement data to disk

//            Acquire->ac_FocusFile->HardFlush( Acquire->ac_FocusFile );                                  // flush out the file. 
//            CopyTheFile( Acquire->ac_achTitleStr, Acquire->ac_achFocusFile );                           // copy the flushed file to the data file.    


            Acquire->ac_idMagSignal = -1;                                                               // Reset signal array index
            memset( &Acquire->ac_FileData, 0x0, sizeof( Acquire->ac_FileData ) );                       // Clear disk file measurement data structure
            strcpy( Acquire->ac_FileData.achDemag, achDemag );                                          // Retrieve demagnetization string
            }
          }
//      } while( ( bRepeat && ( nRepeat < 2 ) ) || Acquire->ac_bUpRange );
        } while( ( ( bRepeat && ( nRepeat < 2 ) ) || Acquire->ac_bUpRange ) && !Acquire->ac_bCancelCycle );

//    EnableWindow( hwndTCancelCancel, FALSE );                         // Disable Cancel button
//    EnableWindow( hwndTCancelOkay,   TRUE  );                         // Enable Ok button

      if( Acquire->ac_bCancelCycle ) {                                  // If measurement cycle has been canceled
        MessageBeep( 0 );
        Acquire->ac_bFilePaint    = TRUE;                               // Set file paint flag
        Acquire->ac_bMeasurePaint = FALSE;                              // Clear measure paint flag
        if( Acquire->ac_CancelDialog ) {                                // If cancel measurement cycle dialog box still exists
          DestroyWindow( Acquire->ac_CancelDialog );                    // Close it
          Acquire->ac_CancelDialog = (HWND) NULL;                       // And clear pointer
          }
        InvalidateRect(Acquire->ac_hwndChildInFocus, NULL, TRUE );      // Generate a WM_PAINT message
        UpdateWindow(  Acquire->ac_hwndChildInFocus );
        }
      else                                                              // Otherwise
        EnableWindow( GetDlgItem( Acquire->ac_CancelDialog, IDD_OK_MEASURE ), TRUE );   // Enable Ok button
        //Acquire->ac_FocusFile->HardFlush( Acquire->ac_FocusFile );                      // flush out the file. 
      }
    }
  //Acquire->ac_FocusFile->CloseIt( Acquire->ac_FocusFile );              // Close the focus.tmp disk file
  //CopyTheFile( Acquire->ac_achTitleStr, Acquire->ac_achFocusFile );     // copy the flushed file to the data file.    
  }


// TMyMDIChild Class Member - Measurement and demagnetization sequence

void TMyMDIChildCMSequence( ACQUIRE *Acquire, HWND hWindow ) {

  int    nRepeat;
  char   achRecord[ 4 ];
  double dStart, dStop, dStop1, dIncr1, dStop2, dIncr2;
  BOOL   bRepeat = FALSE;

  if( ( Acquire != (ACQUIRE *) NULL ) &&
      ( hWindow != (HWND)      NULL ) ) {

    // Clear cancel measurement flag

    Acquire->ac_bCancelCycle = FALSE;

    // Move sample handler to load position

    SendMessage( hWindow, UM_LOAD, 0, 0 );                                                      // Send a load message directly to the message function
/*
    // Move sample handler to load position
    if( Acquire->ac_bSHAuto ) {                                                                 // Automatic
      if( Acquire->ac_bSHTrans ) {                                                              // Automatic translation
        Acquire->ac_bTHome = Translate( hWindow, Acquire->ac_bTHome, Acquire->ac_lTHome );      // Translate to home position first
        Acquire->ac_bTLoad = Translate( hWindow, Acquire->ac_bTLoad, Acquire->ac_lTLoad );      // Translate to load position
        }
      else TranslateMessage( hWindow, "Load" );
      if( bSHRot )                                                                              // Automatic rotation
        Acquire->ac_bRHome = Rotate( hWindow, Acquire->ac_bRHome, Acquire->ac_nRHome );         // Rotate to 0 position
      else RotateMessage(hWindow, "0" );
      }
    else {                                                                                      // Manual
      TranslateMessage( hWindow, "Load" );
      RotateMessage(    hWindow, "0" );
      }
*/
    // Start the Measure Sequence dialog box

    if( DialogBoxParam( Acquire->ac_hInstance, MAKEINTRESOURCE( MeasureSequence_Dialog ), Acquire->ac_hMainWindow, TSequenceDialogProcess, (long) Acquire ) == IDOK ) { // If the Ok button is clicked
      if( Acquire->ac_CancelDialog )                                            // If the cancel measurement cycle dialog box has already been created
        SetActiveWindow( Acquire->ac_CancelDialog );                            // Make it active

      // Create it and make it active

      else {                                                                    // If it has not been created
        Acquire->ac_CancelDialog = CreateDialogParam( Acquire->ac_hInstance, MAKEINTRESOURCE( Cancel_Dialog ), Acquire->ac_hMainWindow, TCancelDialogProcess, (long) Acquire );
        }

      // Load standard

      if( Acquire->ac_bMeasureNegZ )
           LoadMessage( hWindow, "-Z Position" );
      else LoadMessage( hWindow, "+Z Position" );

      // Retrieve dialog data

      dStart = atof( Acquire->ac_SequenceDData.achStart );                      // Start AF level
      dStop1 = atof( Acquire->ac_SequenceDData.achStop1 );                      // First stop AF level
      dIncr1 = atof( Acquire->ac_SequenceDData.achIncr1 );                      // First increment AF level
      dStop2 = atof( Acquire->ac_SequenceDData.achStop2 );                      // Second stop AF level
      dIncr2 = atof( Acquire->ac_SequenceDData.achIncr2 );                      // Second increment AF level
      Acquire->ac_dAFLevel = dStart;                                            // Initialize current AF level to start AF level
      if( dStop2 > dStop1 )                                                     // Set final AF level
           dStop = dStop2;
      else dStop = dStop1;

      do {                                                                      // Loop through each demagnetization-measurement cycle

        // Initialize structure and variables

        memset( &Acquire->ac_FileData, 0x0, sizeof( Acquire->ac_FileData ) );   // Clear disk file measurement data structure
        Acquire->ac_bFilePaint    = FALSE;                                      // Paint measurements to screen
        Acquire->ac_bMeasurePaint = TRUE;
        Acquire->ac_idMagSignal   = -1;                                         // Signal array index

        // Demagnetization

        sprintf(Acquire->ac_FileData.achDemag, "%.1f", Acquire->ac_dAFLevel );  // Demagnetization string for disk file
        strcat( Acquire->ac_FileData.achDemag, "mT" );
        if( Acquire->ac_dAFLevel > 0.0 )
          SendMessage( hWindow, UM_DEMAGNETIZE, 0, 0 );                         // Send a demagnetize message directly to the message function
          nRepeat = 0;                                                          // Clear repeat counter
          do {                                                                  // Threshold and uprange loop

            // Measurement

            SendMessage( hWindow, UM_MEASURE, 0, 0 );                           // Send a measure message directly to the message function

            // Data reduction

            SendMessage( hWindow, UM_REDUCE, 0, 0 );                            // Send a reduce message directly to the message function
            if( !Acquire->ac_bCancelCycle ) {                                   // Only if measurement cycle has not been canceled

              // Threshold repeat

              nRepeat += 1;                                                     // Increment repeat counter
//            if( atof( Acquire->ac_FileData.achStoN ) < atof( Acquire->ac_MeasureDData.achSN ) ) {                     // If the S/N threshold is not met
              if( atof( Acquire->ac_FileData.achStoN ) < Acquire->ac_dStoNThreshold ) {                                 // If the S/N threshold is not met
                if( Acquire->ac_MeasureDData.dwSNR )                            // Repeat measurement
                  bRepeat = TRUE;
                }
//            if( atof( Acquire->ac_MeasureDData.achDrift ) < Magnitude( adDrift[ 0 ], adDrift[ 1 ], adDrift[ 2 ] ) ) { // If the drift threshold is not met
              if( atof( Acquire->ac_FileData.achStoD ) < Acquire->ac_dDriftThreshold ) {                                // If the drift threshold is not met
                if( Acquire->ac_MeasureDData.dwDR )                             // Repeat measurement
                  bRepeat = TRUE;
                }

              // Uprange repeat

              if( Acquire->ac_bUpRange ) {
                if( Acquire->ac_RangeUpDialog )                                 // If the magnetometer automatic range up dialog box has already been created
                  SetActiveWindow( Acquire->ac_RangeUpDialog );                 // Make it active

                // Create it and make it active

                else {                                                          // If it has not been created
                  Acquire->ac_RangeUpDialog = CreateDialogParam( Acquire->ac_hInstance, MAKEINTRESOURCE( RangeUp_Dialog ), Acquire->ac_hMainWindow, TRangeUpDialogProcess, (long) Acquire );
                  }
                Pause( 2970 );
                if( Acquire->ac_RangeUpDialog ) {                                       // If magnetometer automatic range up dialog box still exists
                  DestroyWindow( Acquire->ac_RangeUpDialog );                           // Close it
                  Acquire->ac_RangeUpDialog = (HWND) NULL;                              // And clear pointer
                  }
                if( !MagRangeUp( Acquire, hWindow, Acquire->ac_nMagComID, "X" ) ) {     // Range up
                  Acquire->ac_bUpRange = FALSE;                                         // If sample is too strong
                  bRepeat  = FALSE;
                  }
                if( !MagRangeUp( Acquire, hWindow, Acquire->ac_nMagComID, "Y" ) ) {
                  Acquire->ac_bUpRange = FALSE;
                  bRepeat  = FALSE;
                  }
                if( !MagRangeUp( Acquire, hWindow, Acquire->ac_nMagComID, "Z" ) ) {
                  Acquire->ac_bUpRange = FALSE;
                  bRepeat  = FALSE;
                  }
                Acquire->ac_idMagSignal = -1;                                           // Reset signal array index
                Acquire->ac_FileSummary.nCurrentStep -= 1;                              // Decrement measurement step index
                bRepeat     = FALSE;                                                    // Clear repeat flag
                nRepeat     = 0;                                                        // Reset repeat index
                memset( &Acquire->ac_FileData, 0x0, sizeof( Acquire->ac_FileData ) );   // Clear disk file measurement data structure
                sprintf( Acquire->ac_FileData.achDemag, "%.1f", Acquire->ac_dAFLevel ); // Retrieve demagnetization string for disk file
                strcat( Acquire->ac_FileData.achDemag, "mT" );
//              strcpy( Acquire->ac_FileData.achDemag, achDemag );                      // Retrieve demagnetization string
                }

              if( bRepeat && ( nRepeat < 2 ) ) {                                        // If measurement is to be repeated
   
                /*  Save the Sample Data */  
                Acquire->ac_FocusFile->Save(   Acquire->ac_FocusFile, "-1", sizeof( SPECIMENSUMMARY ) );                                // Save specimen summary record number to disk
                Acquire->ac_FocusFile->CopyTo( Acquire->ac_FocusFile, (LPSTR) &Acquire->ac_FileSummary, sizeof( SPECIMENSUMMARY ) );    // Save specimen summary data to disk
                sprintf( achRecord, "%d", Acquire->ac_FileSummary.nCurrentStep );       // Set up record header string
                Acquire->ac_FocusFile->Save(   Acquire->ac_FocusFile, achRecord, sizeof( SPECIMENDATA ) );                              // Save measurement data record number to disk
                Acquire->ac_FocusFile->CopyTo( Acquire->ac_FocusFile, (LPSTR) &Acquire->ac_FileData, sizeof( SPECIMENDATA ) );          // Save measurement data to disk

                /* This data go into a buffer waiting to be written to disk.
                   If the program ends that buffer is discarded and the data is lost. */
                /* Flushing the buffer or closing and opening the file could assure that no
                   data is lost if the program crashes. */
                Acquire->ac_FocusFile->HardFlush( Acquire->ac_FocusFile );              // flush out the file. 
                CopyTheFile( Acquire->ac_achTitleStr, Acquire->ac_achFocusFile );       // copy the flushed file to the data file.    

                /*   */

                Acquire->ac_idMagSignal = -1;                                           // Reset signal array index
                memset( &Acquire->ac_FileData, 0x0, sizeof( Acquire->ac_FileData ) );   // Clear disk file measurement data structure
                sprintf( Acquire->ac_FileData.achDemag, "%.1f", Acquire->ac_dAFLevel ); // Retrieve demagnetization string for disk file
                strcat(  Acquire->ac_FileData.achDemag, "mT" );
//              strcpy(  Acquire->ac_FileData.achDemag, achDemag );                     // Retrieve demagnetization string
                }
              }
//          } while( ( bRepeat && ( nRepeat < 2 ) ) || Acquire->ac_bUpRange );
            } while( ( ( bRepeat && ( nRepeat < 2 ) ) || Acquire->ac_bUpRange ) && !Acquire->ac_bCancelCycle );

          // Increment AF level

          if( dStop == dStop1 )                                         // To first stop
            Acquire->ac_dAFLevel += dIncr1;                             // Add first increment AF level to current AF level
          else {                                                        // To second stop
            if( Acquire->ac_dAFLevel < dStop1 )
                 Acquire->ac_dAFLevel += dIncr1;                        // Add first increment AF level to current AF level
            else Acquire->ac_dAFLevel += dIncr2;                        // Add second increment AF level to current AF level
            }
/*
          if( Acquire->ac_dAFLevel > dStop1 )
               Acquire->ac_dAFLevel += dIncr2;                          // Add second increment AF level to current AF level
          else Acquire->ac_dAFLevel += dIncr1;                          // Add first increment AF level to current AF level
*/
          // Update sample disk file

          if( !Acquire->ac_bCancelCycle ) {                             // Only if measurement cycle has not been canceled
            Acquire->ac_bFilePaint    = TRUE;                           // Paint file contents to screen
            Acquire->ac_bMeasurePaint = FALSE;

            /*  Save the Sample Data */  
            Acquire->ac_FocusFile->Save(   Acquire->ac_FocusFile, "-1", sizeof( SPECIMENSUMMARY ) );    // Save specimen summary record number to disk
            Acquire->ac_FocusFile->CopyTo( Acquire->ac_FocusFile, (LPSTR) &Acquire->ac_FileSummary, sizeof( SPECIMENSUMMARY ) );// Save specimen summary data to disk
            sprintf( achRecord, "%d", Acquire->ac_FileSummary.nCurrentStep );                           // Set up record header string
            Acquire->ac_FocusFile->Save(   Acquire->ac_FocusFile, achRecord, sizeof( SPECIMENDATA ) );  // Save measurement data record number to disk
            Acquire->ac_FocusFile->CopyTo( Acquire->ac_FocusFile, (LPSTR) &Acquire->ac_FileData, sizeof( SPECIMENDATA ) );      // Save measurement data to disk
            //Acquire->ac_FocusFile->HardFlush( Acquire->ac_FocusFile );                                  // flush out the file. 
            CopyTheFile( Acquire->ac_achTitleStr, Acquire->ac_achFocusFile );                           // copy the flushed file to the data file.    

            InvalidateRect(Acquire->ac_hwndChildInFocus, NULL, TRUE );                                  // Generate a WM_PAINT message
            UpdateWindow(  Acquire->ac_hwndChildInFocus );
            }
          } while( ( Acquire->ac_dAFLevel <= dStop ) && !Acquire->ac_bCancelCycle );

        if( Acquire->ac_CancelDialog ) {                                // If cancel measurement cycle dialog box still exists
          DestroyWindow( Acquire->ac_CancelDialog );                    // Close it
          Acquire->ac_CancelDialog = (HWND) NULL;                       // And clear pointer
          }

      // If measurement cycle is canceled

      if( Acquire->ac_bCancelCycle ) {                                  // If measurement cycle has been canceled
        MessageBeep( 0 );
        Acquire->ac_bFilePaint    = TRUE;                               // Set file paint flag
        Acquire->ac_bMeasurePaint = FALSE;                              // Clear measure paint flag
        InvalidateRect( Acquire->ac_hwndChildInFocus, NULL, TRUE );     // Generate a WM_PAINT message
        UpdateWindow( Acquire->ac_hwndChildInFocus );
        }
//    EnableWindow( hwndTCancelCancel, FALSE );                         // Disable Cancel button
//    EnableWindow( hwndTCancelOkay,   TRUE  );                         // Enable Ok button
      Acquire->ac_FocusFile->HardFlush( Acquire->ac_FocusFile );        // flush out the file. 
      CopyTheFile( Acquire->ac_achTitleStr, Acquire->ac_achFocusFile ); // copy the flushed file to the data file.    
      }
    }
  }



// TMyMDIChild Class Member - Sample holder measurement

void TMyMDIChildCMHolder( ACQUIRE *Acquire, HWND hWindow ) {

  double dHolderX, dHolderY, dHolderZ;

  if( ( Acquire != (ACQUIRE *) NULL ) &&
      ( hWindow != (HWND)      NULL ) ) {

    // Clear cancel measurement flag

    Acquire->ac_bCancelCycle = FALSE;

    // Move sample handler to load position

    SendMessage( hWindow, UM_LOAD, 0, 0 );                                      // Send a load message directly to the message function
/*
    // Move sample handler to load position

    if( Acquire->ac_bSHAuto ) {                                                                 // Automatic
      if( Acquire->ac_bSHTrans ) {                                                              // Automatic translation
        Acquire->ac_bTHome = Translate( hWindow, Acquire->ac_bTHome, Acquire->ac_lTHome );      // Translate to home position first
        Acquire->ac_bTLoad = Translate( hWindow, Acquire->ac_bTLoad, Acquire->ac_lTLoad );      // Translate to load position
        }
      else TranslateMessage( hWindow, "Load" );
      if( Acquire->ac_bSHRot )                                                                  // Automatic rotation
        Acquire->ac_bRHome = Rotate( hWindow, Acquire->ac_bRHome, Acquire->ac_nRHome );         // Rotate to 0 position
      else RotateMessage( hWindow, "0");
      }
    else {                                                                                      // Manual
      TranslateMessage( hWindow, "Load" );
      RotateMessage(    hWindow, "0"    );
      }
*/
    if( DialogBoxParam( Acquire->ac_hInstance, MAKEINTRESOURCE( MeasureSample_Dialog ), Acquire->ac_hMainWindow, THolderDialogProcess, (long) Acquire ) == IDOK ) { // If the Ok button is clicked
      if( Acquire->ac_CancelDialog )                                            // If the cancel measurement cycle dialog box has already been created
        SetActiveWindow( Acquire->ac_CancelDialog );                            // Make it active

      // Create it and make it active

      else {                                                                    // If it has not been created
        Acquire->ac_CancelDialog = CreateDialogParam( Acquire->ac_hInstance, MAKEINTRESOURCE( Cancel_Dialog ), Acquire->ac_hMainWindow, TCancelDialogProcess, (long) Acquire );
        }

      // Emply sample holder

      LoadMessage( hWindow, "Empty Sample Holder" );

      // Initialize structure and variables

      memset( &Acquire->ac_FileData, 0x0, sizeof( Acquire->ac_FileData ) );     // Clear disk file measurement data structure
      Acquire->ac_bFilePaint      = FALSE;                                      // Paint measurements to screen
      Acquire->ac_bMeasurePaint   = TRUE;
      Acquire->ac_idMagSignal     = -1;                                         // Signal array index
      Acquire->ac_bMeasureNegZ    = FALSE;                                      // Holder can only be measured in the +Z position
      Acquire->ac_MeasureDData.dwZ = 0UL;
      dHolderX = Acquire->ac_dHolderXComponent;                                 // Save current holder moments
      dHolderY = Acquire->ac_dHolderYComponent;
      dHolderZ = Acquire->ac_dHolderZComponent;
      Acquire->ac_dHolderXComponent = 0.0;                                      // Zero holder moments
      Acquire->ac_dHolderYComponent = 0.0;
      Acquire->ac_dHolderZComponent = 0.0;
      strcpy( Acquire->ac_FileData.achDemag, "Holder" );                        // Demagnetization string for disk file

      do {                                                                      // Uprange loop

        // Measurement

        SendMessage( hWindow, UM_MEASURE, 0, 0 );                               // Send a measure message directly to the message function

        // Data reduction

        SendMessage( hWindow, UM_REDUCE, 0, 0 );                                // Send a reduce message directly to the message function

        // Uprange repeat

        if( Acquire->ac_bUpRange && !Acquire->ac_bCancelCycle ) {
          if( Acquire->ac_RangeUpDialog )                                       // If the magnetometer automatic range up dialog box has already been created
            SetActiveWindow( Acquire->ac_RangeUpDialog );                       // Make it active

          // Create it and make it active

          else {                                                                // If it has not been created
            Acquire->ac_RangeUpDialog = CreateDialogParam( Acquire->ac_hInstance, MAKEINTRESOURCE( RangeUp_Dialog ), Acquire->ac_hMainWindow, TRangeUpDialogProcess, (long) Acquire );
            }
          Pause( 2970 );
          if( Acquire->ac_RangeUpDialog ) {                                     // If magnetometer automatic range up dialog box still exists
            DestroyWindow( Acquire->ac_RangeUpDialog );                         // Close it
            Acquire->ac_RangeUpDialog = (HWND) NULL;                            // And clear pointer
            }
          if( !MagRangeUp( Acquire, hWindow, Acquire->ac_nMagComID, "X" ) )     // Range up
            Acquire->ac_bUpRange = FALSE;                                       // If sample is too strong
          if( !MagRangeUp( Acquire, hWindow, Acquire->ac_nMagComID, "Y" ) )
            Acquire->ac_bUpRange = FALSE;
          if( !MagRangeUp( Acquire, hWindow, Acquire->ac_nMagComID, "Z" ) )
            Acquire->ac_bUpRange = FALSE;
          Acquire->ac_idMagSignal = -1;                                         // Reset signal array index
          Acquire->ac_FileSummary.nCurrentStep -= 1;                            // Decrement measurement step index
          memset( &Acquire->ac_FileData, 0x0, sizeof( Acquire->ac_FileData ) ); // Clear disk file measurement data structure
          }

        // Update holder moments

        if( !Acquire->ac_bCancelCycle && !Acquire->ac_bUpRange ) {              // If measurement cycle has not been canceled
          Acquire->ac_dHolderXComponent = Acquire->ac_adSampleComponents[ 0 ];
          Acquire->ac_dHolderYComponent = Acquire->ac_adSampleComponents[ 2 ];
          Acquire->ac_dHolderZComponent = Acquire->ac_adSampleComponents[ 4 ];
          Eprintf( Acquire->ac_HolderDData.achX, "%+.3e", Acquire->ac_dHolderXComponent );      // X moment
          Eprintf( Acquire->ac_HolderDData.achY, "%+.3e", Acquire->ac_dHolderYComponent );      // Y moment
          Eprintf( Acquire->ac_HolderDData.achZ, "%+.3e", Acquire->ac_dHolderZComponent );      // Z moment
          strcpy(  Acquire->ac_HolderDData.achTotal, Acquire->ac_FileData.achTotalMoment );     // Total moment
          }
        } while( Acquire->ac_bUpRange && !Acquire->ac_bCancelCycle );

//    EnableWindow( hwndTCancelCancel, FALSE );                                         // Disable Cancel button
//    EnableWindow( hwndTCancelOkay,   TRUE  );                                         // Enable Ok button

      if( Acquire->ac_bCancelCycle ) {                                                  // If measurement cycle has been canceled
        MessageBeep( 0 );
        Acquire->ac_dHolderXComponent = dHolderX;                                       // Restore holder moments
        Acquire->ac_dHolderYComponent = dHolderY;
        Acquire->ac_dHolderZComponent = dHolderZ;
        Acquire->ac_bFilePaint        = TRUE;                                           // Set file paint flag
        Acquire->ac_bMeasurePaint     = FALSE;                                          // Clear measure paint flag

        if( Acquire->ac_CancelDialog ) {                                                // If cancel measurement cycle dialog box still exists
          DestroyWindow( Acquire->ac_CancelDialog );                                    // Close it
          Acquire->ac_CancelDialog = (HWND) NULL;                                       // And clear pointer
          }

        InvalidateRect( Acquire->ac_hwndChildInFocus, NULL, TRUE );                     // Generate a WM_PAINT message
        UpdateWindow( Acquire->ac_hwndChildInFocus );
        }
      else                                                                              // Otherwise
        EnableWindow( GetDlgItem( Acquire->ac_CancelDialog, IDD_OK_MEASURE ), TRUE );   // Enable Ok button
      }
    }
  }


// TMyMDIChild Class Member - Standard sample measurement

void TMyMDIChildCMStandard( ACQUIRE *Acquire, HWND hWindow ) {

  double dAngle1, dAngle2, dXYAngleOld;
  char   achRecord[ 4 ];

  if( ( Acquire != (ACQUIRE *) NULL ) &&
      ( hWindow != (HWND)      NULL ) ) {

    Log( "TMyMDIChildCMStandard\n");

    // Clear cancel measurement flag

    Acquire->ac_bCancelCycle = FALSE;

    // Move sample handler to load position

    SendMessage( hWindow, UM_LOAD, 0, 0 );                      // Send a load message directly to the message function
    if( DialogBoxParam( Acquire->ac_hInstance, MAKEINTRESOURCE( MeasureStandard_Dialog ), Acquire->ac_hMainWindow, TStandardDialogProcess, (long) Acquire ) == IDOK ) { // If the Ok button is clicked
      if( Acquire->ac_CancelDialog )                            // If the cancel measurement cycle dialog box has already been created
        SetActiveWindow( Acquire->ac_CancelDialog );            // Make it active

      // Create it and make it active

      else {                                                    // If it has not been created
        Acquire->ac_CancelDialog = CreateDialogParam( Acquire->ac_hInstance, MAKEINTRESOURCE( Cancel_Dialog ), Acquire->ac_hMainWindow, TCancelDialogProcess, (long) Acquire );
        }

      // Load standard

      LoadMessage( hWindow, "-Z Position" );

      // Initialize structure and variables

      memset( &Acquire->ac_FileData, 0x0, sizeof( Acquire->ac_FileData ) );     // Clear disk file measurement data structure
      Acquire->ac_bFilePaint       = FALSE;                     // Paint measurements to screen
      Acquire->ac_bMeasurePaint    = TRUE;
      Acquire->ac_idMagSignal      = -1;                        // Signal array index
      Acquire->ac_bMeasureNegZ     = FALSE;                     // +Z position flag must be cleared
      Acquire->ac_MeasureDData.dwZ = 0UL;
      dXYAngleOld                  = Acquire->ac_dXYAngle;      // Save current correction angle
      Acquire->ac_dXYAngle         = 0.0;                       // Zero correction angle
      strcpy( Acquire->ac_FileData.achDemag, "Std -Z" );        // Demagnetization string for disk file

      do {                                                      // Uprange loop

        // Measurement

        SendMessage( hWindow, UM_MEASURE, 0, 0 );               // Send a measure message directly to the message function

        // Data reduction

        SendMessage( hWindow, UM_REDUCE,  0, 0 );               // Send a reduce message directly to the message function
        if( !Acquire->ac_bCancelCycle ) {                       // If measurement cycle has not been canceled

          // Uprange repeat

          if( Acquire->ac_bUpRange ) {

            if( Acquire->ac_RangeUpDialog )                     // If the magnetometer automatic range up dialog box has already been created
              SetActiveWindow( Acquire->ac_RangeUpDialog );     // Make it active

            // Create it and make it active

            else {                                              // If it has not been created
              Acquire->ac_RangeUpDialog = CreateDialogParam( Acquire->ac_hInstance, MAKEINTRESOURCE( RangeUp_Dialog ), Acquire->ac_hMainWindow, TRangeUpDialogProcess, (long) Acquire );
              }
            Pause( 2970 );
            if( Acquire->ac_RangeUpDialog ) {                   // If magnetometer automatic range up dialog box still exists
              DestroyWindow( Acquire->ac_RangeUpDialog );       // Close it
              Acquire->ac_RangeUpDialog = (HWND) NULL;          // And clear pointer
              }


//            if( ptRangeUpDialog && IsWindow( ptRangeUpDialog->hWindow ) )                     // If the magnetometer automatic range up dialog box has already been created
//              SetActiveWindow( ptRangeUpDialog->HWindow );                                    // Make it active
//            else {                                                                            // If it has not been created
//              ptRangeUpDialog = new TRangeUpDialog( this, MAKEINTRESOURCE( RangeUp_Dialog ) );// Create it and make it active
//              GetApplication()->MakeWindow( ptRangeUpDialog );
//              }
//            Pause( 2970 );
//            if( ptRangeUpDialog ) {                                                           // If magnetometer automatic range up dialog box still exists
//              ptRangeUpDialog->CloseWindow();                                                 // Close it
//              ptRangeUpDialog = NULL;                                                         // And clear pointer
//              }


            if( !MagRangeUp( Acquire, hWindow, Acquire->ac_nMagComID, "X" ) )                   // Range up
              Acquire->ac_bUpRange = FALSE;                                                     // If sample is too strong
            if( !MagRangeUp( Acquire, hWindow, Acquire->ac_nMagComID, "Y" ) )
              Acquire->ac_bUpRange = FALSE;
            if( !MagRangeUp( Acquire, hWindow, Acquire->ac_nMagComID, "Z" ) )
              Acquire->ac_bUpRange = FALSE;
            Acquire->ac_FileSummary.nCurrentStep -= 1;                                          // Decrement measurement step index
            }
          else {
          // Save first measurement

            // Save specimen summary record number to disk
            Acquire->ac_FocusFile->Save(   Acquire->ac_FocusFile, "-1", sizeof( SPECIMENSUMMARY ) );
            
            // Save specimen summary data to disk
            Acquire->ac_FocusFile->CopyTo( Acquire->ac_FocusFile, (LPSTR) &Acquire->ac_FileSummary, sizeof( SPECIMENSUMMARY ) );
    
            // Set up record header string
            sprintf( achRecord, "%d", Acquire->ac_FileSummary.nCurrentStep );                           
            // Save measurement data record number to disk
            Acquire->ac_FocusFile->Save(   Acquire->ac_FocusFile, achRecord, sizeof( SPECIMENDATA ) );  

            // Save measurement data to disk
            Acquire->ac_FocusFile->CopyTo( Acquire->ac_FocusFile, (LPSTR) &Acquire->ac_FileData, sizeof( SPECIMENDATA ) );

            // Make sure the data gets to both the disk files even if the program errors out before copying back the focus.tmp.
            Acquire->ac_FocusFile->HardFlush( Acquire->ac_FocusFile );                          // flush out the file. 
            CopyTheFile( Acquire->ac_achTitleStr, Acquire->ac_achFocusFile );                   // copy the flushed file to the data file.    

            // Angle

            dAngle1 = atof( Acquire->ac_FileData.achCoreDec );                                  // First angle in sample x-y plane
            if( dAngle1 > 180.0 )                                                               // Angle must range from -180 to +180
              dAngle1 -= 360.0;

            // Move sample handler to load position

            SendMessage( hWindow, UM_LOAD, 0, 0 );                                              // Send a load message directly to the message function

            // Load standard

            LoadMessage( hWindow, "+Z Position" );
            }

          // Initialize structure and variables

          memset( &Acquire->ac_FileData, 0x0, sizeof( Acquire->ac_FileData ) );                 // Clear disk file measurement data structure
          strcpy( Acquire->ac_FileData.achDemag, "Std +Z" );                                    // Demagnetization string for disk file
          Acquire->ac_idMagSignal = -1;                                                         // Signal array index
          }
        } while( Acquire->ac_bUpRange && !Acquire->ac_bCancelCycle );

      // Measurement

      SendMessage( hWindow, UM_MEASURE, 0, 0 );                                                 // Send a measure message directly to the message function

      // Data reduction

      SendMessage( hWindow, UM_REDUCE,  0, 0 );                                                 // Send a reduce message directly to the message function

      // New angle between magnetometer and sample X axes

      if( !Acquire->ac_bCancelCycle ) {                                                         // If measurement cycle has not been canceled
        dAngle2 = atof( Acquire->ac_FileData.achCoreDec );                                      // Second angle in sample x-y plane
        if( dAngle2 > 180.0 )                                                                   // Angle must range from -180 to +180
          dAngle2 -= 360.0;
          Acquire->ac_dXYAngle = ( dAngle1 + dAngle2 ) / 2.0;

          // Update standard moments and angle

         strcpy( Acquire->ac_StandardDData.achX,     Acquire->ac_FileData.achMomentX );         // X moment
         strcpy( Acquire->ac_StandardDData.achY,     Acquire->ac_FileData.achMomentY );         // Y moment
         strcpy( Acquire->ac_StandardDData.achZ,     Acquire->ac_FileData.achMomentZ );         // Z moment
         strcpy( Acquire->ac_StandardDData.achTotal, Acquire->ac_FileData.achTotalMoment );     // Total moment
         sprintf(Acquire->ac_StandardDData.achAngle, "%+.1f", Acquire->ac_dXYAngle );           // Angle
         EnableWindow( GetDlgItem( Acquire->ac_CancelDialog, IDD_OK_MEASURE ), TRUE );          // Enable Ok button
         }
      else {                                                                            // If measurement cycle has been canceled
        MessageBeep( 0 );
        Acquire->ac_dXYAngle      = dXYAngleOld;                                        // Restore correction angle
        Acquire->ac_bFilePaint    = TRUE;                                               // Set file paint flag
        Acquire->ac_bMeasurePaint = FALSE;                                              // Clear measure paint flag
        if( Acquire->ac_CancelDialog ) {                                                // If cancel measurement cycle dialog box still exists
          DestroyWindow( Acquire->ac_CancelDialog );                                    // Close it
          Acquire->ac_CancelDialog = (HWND) NULL;                                       // And clear pointer
          }
        InvalidateRect(Acquire->ac_hwndChildInFocus, NULL, TRUE );                      // Generate a WM_PAINT message
        UpdateWindow(  Acquire->ac_hwndChildInFocus );
        }
      }
    }
  }


LRESULT CALLBACK ChildWindowProcess( HWND hWindow, UINT uMsg, WPARAM wParam, LPARAM lParam ) {

  ACQUIRE *Acquire;

  switch( uMsg ) {

    case WM_DESTROY:
      TMyMDIChildWMDestroy( (ACQUIRE *) GetWindowLong( hWindow, GWL_USERDATA ), hWindow );
      break;

    case WM_PAINT:
      TMyMDIChildWMPaint( (ACQUIRE *) GetWindowLong( hWindow, GWL_USERDATA ), hWindow );
      break;

    case WM_CLOSE:
      if( Acquire = (ACQUIRE *) GetWindowLong( hWindow, GWL_USERDATA ) ) {
        if( TMyMDIChildCanClose( Acquire, Acquire->ac_hMainWindow ) ) {
          return 0;
          }
        }
      break;

    case WM_QUERYENDSESSION:
      return FALSE;

    case WM_HSCROLL:
      TMyMDIChildWMHScroll( (ACQUIRE *) GetWindowLong( hWindow, GWL_USERDATA ), hWindow, wParam );
      break;

    case WM_VSCROLL:
      TMyMDIChildWMVScroll( (ACQUIRE *) GetWindowLong( hWindow, GWL_USERDATA ), hWindow, wParam );
      break;
    }
  return DefWindowProc( hWindow, uMsg, wParam, lParam );
  }


LRESULT CALLBACK MDIWindowProcess( HWND hWindow, UINT uMsg, WPARAM wParam, LPARAM lParam ) {

  HWND     hWindowEdit;
  RECT     rc;
  ACQUIRE *Acquire;

  switch( uMsg ) {

    case WM_CREATE:
      Acquire = GAcquire;

      /* Create an edit control */

      if( hWindowEdit = CreateWindow(
        ChildClass,
        NULL,
        WS_CHILD | WS_MAXIMIZE | WS_VISIBLE | WS_HSCROLL | WS_VSCROLL,
        0, 0,
        0, 0,
        hWindow,
        (HMENU) NULL,
        Acquire->ac_hInstance,
        NULL ) ) {

        /* Remember the window handle and initialize some window attributes */

        SetWindowLong( hWindow,     GWL_USERDATA,  (LONG) Acquire     );
        SetWindowLong( hWindow,     GLW_HWNDChild, (LONG) hWindowEdit );
        SetWindowLong( hWindowEdit, GWL_USERDATA,  (LONG) Acquire     );
        SetWindowLong( hWindowEdit, GWL_HWNDParent,(LONG) hWindow     );
        SetWindowLong( hWindowEdit, GWL_nHScroll, 0 );
        SetWindowLong( hWindowEdit, GWL_nVScroll, 0 );

        TMyMDIChildSetupWindow( hWindowEdit );
        SetFocus( hWindowEdit );
        }
      return FALSE;

    /* On creation or resize, size the edit control. */

    case WM_SIZE:
      GetClientRect( hWindow, &rc );
      MoveWindow( (HWND) GetWindowLong( hWindow, GLW_HWNDChild ), rc.left, rc.top, rc.right - rc.left, rc.bottom - rc.top, TRUE );
      break;

    case WM_SETFOCUS:
      SetFocus( (HWND) GetWindowLong( hWindow, GLW_HWNDChild ) );
      break;

    case WM_QUERYENDSESSION:
      return FALSE;

    /* If we're activating this child, remember it */

    case WM_MDIACTIVATE:
//      if( GET_WM_MDIACTIVATE_FACTIVATE( hWindow, wParam, lParam ) ) {
        TMyMDIChildWMMDIActivate( (ACQUIRE *) GetWindowLong( hWindow, GWL_USERDATA ), (HWND) GetWindowLong( hWindow, GLW_HWNDChild ) );
//        }
      return 0;
    }
  return DefMDIChildProc( hWindow, uMsg, wParam, lParam );
  }


/************************************************************************
*                                                                       *
*       OpenChildWindow:                                                *
*                                                                       *
************************************************************************/

BOOL OpenChildWindow( ACQUIRE *Acquire, char *WindowName ) {

  MDICREATESTRUCT mcs;

  if( Acquire != (ACQUIRE *) NULL ) {
    memset( &mcs, 0, sizeof( MDICREATESTRUCT ) );

    mcs.szTitle = WindowName;
    mcs.szClass = MDIClass;
    mcs.hOwner  = Acquire->ac_hInstance;

    /* Use the default size for the window */

    mcs.x = mcs.cx = CW_USEDEFAULT;
    mcs.y = mcs.cy = CW_USEDEFAULT;

    /* Set the style DWORD of the window to default */

    mcs.style = 0; //styleDefault;

    /* tell the MDI Client to create the child */

    return SendMessage( Acquire->ac_hWindowMDIClient, WM_MDICREATE, 0, (LONG) (LPMDICREATESTRUCT) &mcs ) != (long) NULL;
    }
  return FALSE;
  }
