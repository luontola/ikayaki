/************************************************************************
*                                                                       *
*       Configure.c                                                     *
*                                                                       *
*       Contains The Code For The PAcquire Configure Program.           *
*                                                                       *
*       24.Sept.2002 Jamie Lisa Finch.                                  *
*                                                                       *
************************************************************************/

#include <windows.h>
#include <windowsx.h>
#include <time.h>
#include <stdio.h>
#include "..\h\PAcquire.h"
#include "..\h\Text.h"
#include "..\h\Fnctns.h"
#include "..\h\FileRequestor.h"
#include "..\h\MyChild.h"
#include "..\h\Configure.h"
#include "..\h\messages.h"

#include "..\res\Resource.h"


/************************************************************************
*                                                                       *
*       TOrientDialog:                                                  *
*                                                                       *
************************************************************************/

// Initialize controls

void TOrientDialogSetupWindow( HWND hWindow, ORIENT *OrientDData ) {

  // Set text in edit controls

  SetDlgItemText( hWindow, IDD_ORIENT_COREAZ,    OrientDData->achCoreAz    );   // Core azimuth minus 2G azimuth
  SetDlgItemText( hWindow, IDD_ORIENT_BEDDINGAZ, OrientDData->achBeddingAz );   // Bedding azimuth minus 2G azimuth
  SetDlgItemText( hWindow, IDD_ORIENT_FOLDAZ,    OrientDData->achFoldAz    );   // Plunging fold azimuth minus 2G azimuth

  // Set current states for check boxes

  if( OrientDData->dwCorePComp    )                                             // Core plunge complement
    Button_SetCheck( GetDlgItem( hWindow, IDD_ORIENT_COREPCOMP    ), 1 );       // Check box
  if( OrientDData->dwCorePSign    )                                             // Core plunge sign
    Button_SetCheck( GetDlgItem( hWindow, IDD_ORIENT_COREPSIGN    ), 1 );       // Check box
  if( OrientDData->dwBeddingPComp )                                             // Bedding plunge complement
    Button_SetCheck( GetDlgItem( hWindow, IDD_ORIENT_BEDDINGPCOMP ), 1 );       // Check box
  if( OrientDData->dwBeddingPSign )                                             // Bedding plunge sign
    Button_SetCheck( GetDlgItem( hWindow, IDD_ORIENT_BEDDINGPSIGN ), 1 );       // Check box
  if( OrientDData->dwFoldPComp    )                                             // Plunging fold plunge complement
    Button_SetCheck( GetDlgItem( hWindow, IDD_ORIENT_FOLDPCOMP    ), 1 );       // Check box
  if( OrientDData->dwFoldPSign    )                                             // Plunging fold plunge sign
    Button_SetCheck( GetDlgItem( hWindow, IDD_ORIENT_FOLDPSIGN    ), 1 );       // Check box
  }


// Respond to the Ok button

void TOrientDialogIDOk( HWND hWindow, ORIENT *OrientDData ) {

  // Retrieve text in edit controls

  GetDlgItemText( hWindow, IDD_ORIENT_COREAZ,    OrientDData->achCoreAz,    5 );// Core azimuth minus 2G azimuth
  GetDlgItemText( hWindow, IDD_ORIENT_BEDDINGAZ, OrientDData->achBeddingAz, 5 );// Bedding azimuth minus 2G azimuth
  GetDlgItemText( hWindow, IDD_ORIENT_FOLDAZ,    OrientDData->achFoldAz,    5 );// Plunging fold azimuth minus 2G azimuth

  // Determine states of check boxes

  OrientDData->dwCorePComp    = Button_GetCheck( GetDlgItem( hWindow, IDD_ORIENT_COREPCOMP    ) );      // Core plunge complement
  OrientDData->dwCorePSign    = Button_GetCheck( GetDlgItem( hWindow, IDD_ORIENT_COREPSIGN    ) );      // Core plunge sign
  OrientDData->dwBeddingPComp = Button_GetCheck( GetDlgItem( hWindow, IDD_ORIENT_BEDDINGPCOMP ) );      // Bedding plunge complement
  OrientDData->dwBeddingPSign = Button_GetCheck( GetDlgItem( hWindow, IDD_ORIENT_BEDDINGPSIGN ) );      // Bedding plunge sign
  OrientDData->dwFoldPComp    = Button_GetCheck( GetDlgItem( hWindow, IDD_ORIENT_FOLDPCOMP    ) );      // Plunging fold plunge complement
  OrientDData->dwFoldPSign    = Button_GetCheck( GetDlgItem( hWindow, IDD_ORIENT_FOLDPSIGN    ) );      // Plunging fold plunge sign
  //TDialogOk( Msg );
  }


// TRUE if dialog box can close

BOOL TOrientDialogCanClose( HWND hWindow ) {

  char achCoreAz[ 5 ], achBeddingAz[ 5 ], achFoldAz[ 5 ];
  int    nCoreAz,        nBeddingAz,        nFoldAz;

  // Retrieve text in edit controls

  GetDlgItemText( hWindow, IDD_ORIENT_COREAZ,    achCoreAz,    5 );     // Core azimuth minus 2G azimuth
  GetDlgItemText( hWindow, IDD_ORIENT_BEDDINGAZ, achBeddingAz, 5 );     // Bedding azimuth minus 2G azimuth
  GetDlgItemText( hWindow, IDD_ORIENT_FOLDAZ,    achFoldAz,    5 );     // Plunging fold azimuth minus 2G azimuth

  // Convert strings to integer values

  nCoreAz    = atoi( achCoreAz    );    // Core azimuth minus 2G azimuth
  nBeddingAz = atoi( achBeddingAz );    // Bedding azimuth minus 2G azimuth
  nFoldAz    = atoi( achFoldAz    );    // Plunging fold azimuth minus 2G azimuth

  // Each entry must be in the proper format, and its value must be within bounds

  if( !DigitsAndSign( achCoreAz ) || nCoreAz < -180 || nCoreAz > 360 ) {        // If entry is not just digits with one, optional hyphen,
    MessageBeep( 0 );                                                           // beep the speaker in warning,
    SetDlgItemText( hWindow, IDD_ORIENT_COREAZ, '\0' );                         // clear the entry field, and
    return FALSE;
    }
  if( !DigitsAndSign( achBeddingAz ) || nBeddingAz < -180 || nBeddingAz > 360) {// If entry is not just digits with one, optional hyphen,
    MessageBeep( 0 );                                                           // beep the speaker in warning,
    SetDlgItemText( hWindow, IDD_ORIENT_BEDDINGAZ, '\0' );                      // clear the entry field, and
    return FALSE;
    }
  if( !DigitsAndSign( achFoldAz ) || nFoldAz < -180 || nFoldAz > 360) {         // If entry is not just digits with one, optional hyphen,
    MessageBeep( 0 );                                                           // beep the speaker in warning,
    SetDlgItemText( hWindow, IDD_ORIENT_FOLDAZ, '\0' );                         // clear the entry field, and
    return FALSE;
    }
  return TRUE;  // Selected values must all be valid
  }


BOOL CALLBACK TOrientDialogProcess( HWND hWindow, UINT uMsg, WPARAM wParam, LPARAM lParam ) {

  ACQUIRE *Acquire;

  switch( uMsg ) {

    /* This Get Called When Windows Shuts Down, Asking Ok To Close Application. */

    case WM_QUERYENDSESSION:
      return TRUE;

    /* This Get Called When Windows Shuts Diwn. Telling To Close Application. */

    case WM_ENDSESSION:
      if( wParam && ( ( Acquire = (ACQUIRE *) GetWindowLong( hWindow, GWL_USERDATA ) ) != (ACQUIRE *) NULL ) ) {
        EndDialog( hWindow, 0 );
        }
      return TRUE;

    case WM_INITDIALOG:
      if( ( Acquire = (ACQUIRE *) lParam ) != (ACQUIRE *) NULL ) {
        SetWindowLong( hWindow, GWL_USERDATA, lParam );
        TOrientDialogSetupWindow( hWindow, &Acquire->ac_OrientDData );
        return TRUE;
        }
      break;

    case WM_CLOSE:
      if( ( Acquire = (ACQUIRE *) GetWindowLong( hWindow, GWL_USERDATA ) ) != (ACQUIRE *) NULL ) {
        EndDialog( hWindow, 0 );
        }
      return TRUE;

    case WM_COMMAND:
      if( ( Acquire = (ACQUIRE *) GetWindowLong( hWindow, GWL_USERDATA ) ) != (ACQUIRE *) NULL ) {
        switch( LOWORD( wParam ) ) {

          // Responds to the Ok button

          case IDD_ORIENT_OK:
            if( TOrientDialogCanClose( hWindow ) ) {
              TOrientDialogIDOk( hWindow, &Acquire->ac_OrientDData );
              EndDialog( hWindow, 0 );
              }
            return TRUE;

          case IDCANCEL:
            EndDialog( hWindow, 0 );
            return TRUE;
          }
        }
      break;
    }
  return FALSE;
  }


/************************************************************************
*                                                                       *
*       TDemagDialog:                                                   *
*                                                                       *
************************************************************************/

// Initialize controls

void TDemagDialogSetupWindow( HWND hWindow, AF *DemagDData ) {

  // Set current states for the check boxes

  if( DemagDData->dwX )                                                 // If AF demagnetizer X axis exists
    Button_SetCheck( GetDlgItem( hWindow, IDD_DEMAG_X ), 1 );           // Check box
  if( DemagDData->dwY )                                                 // If AF demagnetizer Y axis exists
    Button_SetCheck( GetDlgItem( hWindow, IDD_DEMAG_Y ), 1 );           // Check box
  if( DemagDData->dwZ )                                                 // If AF demagnetizer Z axis exists
    Button_SetCheck( GetDlgItem( hWindow, IDD_DEMAG_Z ), 1 );           // Check box
  if( DemagDData->dw2G601S )                                            // If coil 2G601S is present
    Button_SetCheck( GetDlgItem( hWindow, IDD_DEMAG_2G601S ), 1 );      // Check box
  if( DemagDData->dw2G601T )                                            // If coil 2G601T is present
    Button_SetCheck( GetDlgItem( hWindow, IDD_DEMAG_2G601T ), 1 );      // Check box

  // Set text in edit field

  SetDlgItemText( hWindow, IDD_DEMAG_FIELD, DemagDData->achField );     // Set text in maximum field edit control

  // Add choices to the ramp combo box

  ComboBox_InsertString( GetDlgItem( hWindow, IDD_DEMAG_RAMP ), 0, "3" );
  ComboBox_InsertString( GetDlgItem( hWindow, IDD_DEMAG_RAMP ), 1, "5" );
  ComboBox_InsertString( GetDlgItem( hWindow, IDD_DEMAG_RAMP ), 2, "7" );
  ComboBox_InsertString( GetDlgItem( hWindow, IDD_DEMAG_RAMP ), 3, "9" );

  // Add choices to the delay combo box

  ComboBox_InsertString( GetDlgItem( hWindow, IDD_DEMAG_DELAY ), 0, "1" );
  ComboBox_InsertString( GetDlgItem( hWindow, IDD_DEMAG_DELAY ), 1, "2" );
  ComboBox_InsertString( GetDlgItem( hWindow, IDD_DEMAG_DELAY ), 2, "3" );
  ComboBox_InsertString( GetDlgItem( hWindow, IDD_DEMAG_DELAY ), 3, "4" );
  ComboBox_InsertString( GetDlgItem( hWindow, IDD_DEMAG_DELAY ), 4, "5" );
  ComboBox_InsertString( GetDlgItem( hWindow, IDD_DEMAG_DELAY ), 5, "6" );
  ComboBox_InsertString( GetDlgItem( hWindow, IDD_DEMAG_DELAY ), 6, "7" );
  ComboBox_InsertString( GetDlgItem( hWindow, IDD_DEMAG_DELAY ), 7, "8" );
  ComboBox_InsertString( GetDlgItem( hWindow, IDD_DEMAG_DELAY ), 8, "9" );

  // Set current values in combo boxes

  ComboBox_SetCurSel( GetDlgItem( hWindow, IDD_DEMAG_RAMP  ), DemagDData->dwRamp  );
  ComboBox_SetCurSel( GetDlgItem( hWindow, IDD_DEMAG_DELAY ), DemagDData->dwDelay );
  }


// Respond to the Ok button

void TDemagDialogIDOk( HWND hWindow, AF *DemagDData ) {

  // Determine state of check boxes

  DemagDData->dwX      = Button_GetCheck( GetDlgItem( hWindow, IDD_DEMAG_X      ) );    // AF demagnetizer X axis
  DemagDData->dwY      = Button_GetCheck( GetDlgItem( hWindow, IDD_DEMAG_Y      ) );    // AF demagnetizer Y axis
  DemagDData->dwZ      = Button_GetCheck( GetDlgItem( hWindow, IDD_DEMAG_Z      ) );    // AF demagnetizer Z axis
  DemagDData->dw2G601S = Button_GetCheck( GetDlgItem( hWindow, IDD_DEMAG_2G601S ) );    // 2G601S coil
  DemagDData->dw2G601T = Button_GetCheck( GetDlgItem( hWindow, IDD_DEMAG_2G601T ) );    // 2G601T coil

  // Retrieve text in edit control

  GetDlgItemText( hWindow, IDD_DEMAG_FIELD, DemagDData->achField, 5 );                  // Maximum field

  // Store selected items for the combo boxes

  DemagDData->dwRamp  = ComboBox_GetCurSel( GetDlgItem( hWindow, IDD_DEMAG_RAMP  ) );   // Ramp selection
  DemagDData->dwDelay = ComboBox_GetCurSel( GetDlgItem( hWindow, IDD_DEMAG_DELAY ) );   // Ramp selection
//  TDialogOk( Msg );
  }


// TRUE if dialog box can close

BOOL TDemagDialogCanClose( HWND hWindow ) {

  char  achField[ 5 ];
  int   nField;
  DWORD dwX, dwY, dwZ, dw2G601S, dw2G601T;

  // Determine state of check boxes

  dwX      = Button_GetCheck( GetDlgItem( hWindow, IDD_DEMAG_X      ) );// AF demagnetizer X axis
  dwY      = Button_GetCheck( GetDlgItem( hWindow, IDD_DEMAG_Y      ) );// AF demagnetizer Y axis
  dwZ      = Button_GetCheck( GetDlgItem( hWindow, IDD_DEMAG_Z      ) );// AF demagnetizer Z axis
  dw2G601S = Button_GetCheck( GetDlgItem( hWindow, IDD_DEMAG_2G601S ) );// 2G601S coil
  dw2G601T = Button_GetCheck( GetDlgItem( hWindow, IDD_DEMAG_2G601T ) );// 2G601T coil

  // Retrieve entry text from the edit field

  GetDlgItemText( hWindow, IDD_DEMAG_FIELD, achField, 5 );              // Maximum field

  // The Z axis and at least one transverse axis must be selected

  if( !dwZ || !( dwX || dwY ) ) {                                       // If not,
    MessageBeep( 0 );                                                   // beep the speaker in warning, and
    return FALSE;
    }

  // Both coil types must be selected

  if( !dw2G601T || !dw2G601S ) {                                        // the 2G601T coil must be selected
    MessageBeep( 0 );                                                   // If not, beep the speaker in warning, and
    return FALSE;
    }

  // Entry text in edit field must be in a valid format, and its value must be within bounds

  nField = atoi( achField );                                            // Convert string to an integer value
  if( !DigitsAndSign( achField ) || nField < 0 || nField > 1800 ) {     // If entry is not just digits with one optional hyphen, and within bounds,
    MessageBeep( 0 );                                                   // beep the speaker in warning,
    SetDlgItemText( hWindow, IDD_DEMAG_FIELD, '\0' );                   // clear the entry field, and
    return FALSE;
    }
  return TRUE;	// Entry values must all be valid
  }


BOOL CALLBACK TDemagDialogProcess( HWND hWindow, UINT uMsg, WPARAM wParam, LPARAM lParam ) {

  ACQUIRE *Acquire;

  switch( uMsg ) {

    /* This Get Called When Windows Shuts Down, Asking Ok To Close Application. */

    case WM_QUERYENDSESSION:
      return TRUE;

    /* This Get Called When Windows Shuts Diwn. Telling To Close Application. */

    case WM_ENDSESSION:
      if( wParam && ( ( Acquire = (ACQUIRE *) GetWindowLong( hWindow, GWL_USERDATA ) ) != (ACQUIRE *) NULL ) ) {
        EndDialog( hWindow, 0 );
        }
      return TRUE;

    case WM_INITDIALOG:
      if( ( Acquire = (ACQUIRE *) lParam ) != (ACQUIRE *) NULL ) {
        SetWindowLong( hWindow, GWL_USERDATA, lParam );
        TDemagDialogSetupWindow( hWindow, &Acquire->ac_DemagDData );
        return TRUE;
        }
      break;

    case WM_CLOSE:
      if( ( Acquire = (ACQUIRE *) GetWindowLong( hWindow, GWL_USERDATA ) ) != (ACQUIRE *) NULL ) {
        EndDialog( hWindow, 0 );
        }
      return TRUE;

    case WM_COMMAND:
      if( ( Acquire = (ACQUIRE *) GetWindowLong( hWindow, GWL_USERDATA ) ) != (ACQUIRE *) NULL ) {
        switch( LOWORD( wParam ) ) {

          // Responds to the Ok button

          case IDD_DEMAG_OK:
            if( TDemagDialogCanClose( hWindow ) ) {
              TDemagDialogIDOk( hWindow, &Acquire->ac_DemagDData );
              EndDialog( hWindow, 0 );
              }
            return TRUE;

          case IDCANCEL:
            EndDialog( hWindow, 0 );
            return TRUE;
          }
        }
      break;
    }
  return FALSE;
  }


/************************************************************************
*                                                                       *
*       THandlerDialog:                                                 *
*                                                                       *
************************************************************************/

// Initialize controls and set current values

void THandlerDialogSetupWindow( HWND hWindow, THANDLERDIALOG *THandlerDialog, CONFIG *TemporaryDData, SH *HandlerDData, AF *DemagDData ) {

  // Retrieve window handles for dialog box controls to be enabled/disabled

  THandlerDialog->h2G811      = GetDlgItem( hWindow, IDD_HANDLER_2G811      );  // 2G811 system radio button
  THandlerDialog->hFlip       = GetDlgItem( hWindow, IDD_HANDLER_FLIP       );  // Flip capability radio button
  THandlerDialog->hAccelT     = GetDlgItem( hWindow, IDD_HANDLER_ACCELT     );  // Acceleration title string
  THandlerDialog->hAccel      = GetDlgItem( hWindow, IDD_HANDLER_ACCEL      );  // Acceleration edit field
  THandlerDialog->hDecelT     = GetDlgItem( hWindow, IDD_HANDLER_DECELT     );  // Deceleration title string
  THandlerDialog->hDecel      = GetDlgItem( hWindow, IDD_HANDLER_DECEL      );  // Deceleration edit field
  THandlerDialog->hVelT       = GetDlgItem( hWindow, IDD_HANDLER_VELT       );  // Velocity title string
  THandlerDialog->hVel        = GetDlgItem( hWindow, IDD_HANDLER_VEL        );  // Velocity edit field
  THandlerDialog->hVelMT      = GetDlgItem( hWindow, IDD_HANDLER_VELMT      );  // Velocity into measurement region title string
  THandlerDialog->hVelM       = GetDlgItem( hWindow, IDD_HANDLER_VELM       );  // Velocity into measurement region edit field
  THandlerDialog->hPos        = GetDlgItem( hWindow, IDD_HANDLER_POS        );  // Translation positions group box label
  THandlerDialog->hPosTransXT = GetDlgItem( hWindow, IDD_HANDLER_POSTRANSXT );  // Transverse X AF position title string
  THandlerDialog->hPosTransX  = GetDlgItem( hWindow, IDD_HANDLER_POSTRANSX  );  // Transverse X AF position edit field
  THandlerDialog->hPosTransYT = GetDlgItem( hWindow, IDD_HANDLER_POSTRANSYT );  // Transverse Y AF position title string
  THandlerDialog->hPosTransY  = GetDlgItem( hWindow, IDD_HANDLER_POSTRANSY  );  // Transverse Y AF position edit field
  THandlerDialog->hPosAxialT  = GetDlgItem( hWindow, IDD_HANDLER_POSAXIALT  );  // Axial AF position title string
  THandlerDialog->hPosAxial   = GetDlgItem( hWindow, IDD_HANDLER_POSAXIAL   );  // Axial AF position edit field
  THandlerDialog->hPosLoadT   = GetDlgItem( hWindow, IDD_HANDLER_POSLOADT   );  // Load position title string
  THandlerDialog->hPosLoad    = GetDlgItem( hWindow, IDD_HANDLER_POSLOAD    );  // Load position edit field
  THandlerDialog->hPosBackT   = GetDlgItem( hWindow, IDD_HANDLER_POSBACKT   );  // Background position title string
  THandlerDialog->hPosBack    = GetDlgItem( hWindow, IDD_HANDLER_POSBACK    );  // Background position edit field
  THandlerDialog->hPosMeasT   = GetDlgItem( hWindow, IDD_HANDLER_POSMEAST   );  // Measurement position title string
  THandlerDialog->hPosMeas    = GetDlgItem( hWindow, IDD_HANDLER_POSMEAS    );  // Measurement position edit field
  THandlerDialog->hRotation   = GetDlgItem( hWindow, IDD_HANDLER_ROTATION   );  // Rotation group box label
  THandlerDialog->hCountsT    = GetDlgItem( hWindow, IDD_HANDLER_COUNTST    );  // Counts per one full revolution title string
  THandlerDialog->hCounts     = GetDlgItem( hWindow, IDD_HANDLER_COUNTS     );  // Counts per one full revolution edit field
  THandlerDialog->hRightT     = GetDlgItem( hWindow, IDD_HANDLER_RIGHTT     );  // Right limit title string
  THandlerDialog->hRight      = GetDlgItem( hWindow, IDD_HANDLER_RIGHT      );  // Right limit combo box

  // Add choices to the Right limit combo box

  ComboBox_InsertString( THandlerDialog->hRight, 0, "plus"  );
  ComboBox_InsertString( THandlerDialog->hRight, 1, "minus" );

  // Set current value in combo box

  ComboBox_SetCurSel( THandlerDialog->hRight, HandlerDData->dwRight );

  // Disable those controls for which the instrument is not configured

  if( !HandlerDData->dwRot ) {                          // If the Rotation axis is not selected,
    EnableWindow( THandlerDialog->hRotation, FALSE );   // disable the Rotation group box,
    EnableWindow( THandlerDialog->hCountsT,  FALSE );   // disable the Counts per one full revolution title string,
    EnableWindow( THandlerDialog->hCounts,   FALSE );   // and disable the Counts per one full revolution edit field
    // New Rotational controls.
    EnableWindow( GetDlgItem( hWindow, IDD_HANDLER_ACCEL2 ),    FALSE );   // and disable the Rot acc field
    EnableWindow( GetDlgItem( hWindow, IDD_HANDLER_ACCELT2 ),   FALSE );   // and disable the Rot acc field

    EnableWindow( GetDlgItem( hWindow, IDD_HANDLER_DECEL2 ),    FALSE );   // and disable the Rot decl field
    EnableWindow( GetDlgItem( hWindow, IDD_HANDLER_DECELT2 ),   FALSE );   // and disable the Rot decl field

    EnableWindow( GetDlgItem( hWindow, IDD_HANDLER_VEL2 ),      FALSE );   // and disable the Rot velocity field
    EnableWindow( GetDlgItem( hWindow, IDD_HANDLER_VELT2 ),     FALSE );   // and disable the Rot velocity field
    }
  else {                                                // If the Rotation axis is selected,
    Button_SetCheck( GetDlgItem( hWindow, IDD_HANDLER_ROT ), 1 );               // check box,
    SetDlgItemText( hWindow, IDD_HANDLER_ACCEL2,  HandlerDData->achRotAccel     );  // set text in the Acceleration edit field,
    SetDlgItemText( hWindow, IDD_HANDLER_DECEL2,  HandlerDData->achRotDecel     );  // set text in the Deceleration edit field,
    SetDlgItemText( hWindow, IDD_HANDLER_VEL2,    HandlerDData->achRotVel       );  // set the text in the Velocity edit field,
    SetDlgItemText( hWindow, IDD_HANDLER_COUNTS, HandlerDData->achRotCounts );  // and set the text in the Counts per one full revolution edit field
    }

  if( !HandlerDData->dwTrans ) {                        // If the Translation axis is not selected,
    EnableWindow( THandlerDialog->hVelMT,      FALSE ); // disable the Velocity into measurement region title string,
    EnableWindow( THandlerDialog->hVelM,       FALSE ); // disable the Velocity into measurement region edit field,
    EnableWindow( THandlerDialog->hPos,        FALSE ); // disable the Translation positions group box
    EnableWindow( THandlerDialog->hPosTransXT, FALSE ); // disable the Transverse X AF position title string,
    EnableWindow( THandlerDialog->hPosTransX,  FALSE ); // disable the Transverse X AF position edit field,
    EnableWindow( THandlerDialog->hPosTransYT, FALSE ); // disable the Transverse Y AF position title string,
    EnableWindow( THandlerDialog->hPosTransY,  FALSE ); // disable the Transverse Y AF position edit field,
    EnableWindow( THandlerDialog->hPosAxialT,  FALSE ); // disable the Axial AF position title string,
    EnableWindow( THandlerDialog->hPosAxial,   FALSE ); // disable the Axial AF position edit field,
    EnableWindow( THandlerDialog->hPosLoadT,   FALSE ); // disable the sample load position title string,
    EnableWindow( THandlerDialog->hPosLoad,    FALSE ); // disable the Sample load position edit field,
    EnableWindow( THandlerDialog->hPosBackT,   FALSE ); // disable the Background measurement position title string,
    EnableWindow( THandlerDialog->hPosBack,    FALSE ); // disable the Background measurement position edit field,
    EnableWindow( THandlerDialog->hPosMeasT,   FALSE ); // disable the Sample measurement position title string,
    EnableWindow( THandlerDialog->hPosMeas,    FALSE ); // disable the Sample measurement position edit field,
    EnableWindow( THandlerDialog->hRightT,     FALSE ); // disable the Right limit title string,
    EnableWindow( THandlerDialog->hRight,      FALSE ); // and disable the Right limit combo box
    }
  else { // If the Translation axis is selected,
    Button_SetCheck( GetDlgItem( hWindow, IDD_HANDLER_TRANS ), 1 );             // check box,
    SetDlgItemText( hWindow, IDD_HANDLER_ACCEL, HandlerDData->achAccel );       // set text in the Acceleration edit field,
    SetDlgItemText( hWindow, IDD_HANDLER_DECEL, HandlerDData->achDecel );       // set text in the Deceleration edit field,
    SetDlgItemText( hWindow, IDD_HANDLER_VEL,   HandlerDData->achVel   );       // set text in the Velocity edit field,
    SetDlgItemText( hWindow, IDD_HANDLER_VELM,  HandlerDData->achVelM  );       // and set the text in the Velocity into measurement region edit field

    if( DemagDData->dwX )                                               // If the AF demagnetizer X axis is also selected,
      SetDlgItemText( hWindow, IDD_HANDLER_POSTRANSX, HandlerDData->achPosXAF );// set the text in the Transverse X AF edit field
    else {                                                              // If the AF demagnetizer X axis is not selected,
      EnableWindow( THandlerDialog->hPosTransXT, FALSE );               // disable the Transverse X AF position title string, and
      EnableWindow( THandlerDialog->hPosTransX,  FALSE );               // disable the Transverse X AF position edit field
      }

    if( DemagDData->dwY )                                               // If the AF demagnetizer Y axis is also selected,
      SetDlgItemText( hWindow, IDD_HANDLER_POSTRANSY, HandlerDData->achPosYAF );// set the text in the Transverse Y AF edit field
    else {                                                              // If the AF demagnetizer Y axis is not selected,
      EnableWindow( THandlerDialog->hPosTransYT, FALSE );               // disable the Transverse Y AF position title string, and
      EnableWindow( THandlerDialog->hPosTransY,  FALSE );               // disable the Transverse Y AF position edit field
      }

    if( DemagDData->dwZ )                                               // If the AF demagnetizer Z axis is also selected,
      SetDlgItemText( hWindow, IDD_HANDLER_POSAXIAL,  HandlerDData->achPosZAF );// set the text in the Axial AF edit field
    else {                                                              // If the AF demagnetizer Z axis is not selected,
      EnableWindow( THandlerDialog->hPosAxialT, FALSE );                // disable the Axial AF position title string, and
      EnableWindow( THandlerDialog->hPosAxial,  FALSE );                // disable the Axial AF position edit field
      }

    SetDlgItemText( hWindow, IDD_HANDLER_POSLOAD, HandlerDData->achPosLoad );   // Set the text in the Sample load edit field

    if( TemporaryDData->dwX || TemporaryDData->dwY || TemporaryDData->dwZ ) {   // If at least one of the magnetometer axes is also selected,
      SetDlgItemText( hWindow, IDD_HANDLER_POSBACK, HandlerDData->achPosBack ); // set the text in the Background edit field,
      SetDlgItemText( hWindow, IDD_HANDLER_POSMEAS, HandlerDData->achPosMeas ); // and set the text in the Measurement edit field
      }
    else {                                                              // For no magnetometer axes,
      EnableWindow( THandlerDialog->hPosBackT, FALSE );                 // disable the Background measurement position title string,
      EnableWindow( THandlerDialog->hPosBack,  FALSE );                 // disable the Background measurement position edit field,
      EnableWindow( THandlerDialog->hPosMeasT, FALSE );                 // disable the Sample measurement position title string,
      EnableWindow( THandlerDialog->hPosMeas,  FALSE );                 // and disable the Sample measurement position edit field
      }
    }

//  if( !HandlerDData->dwRot && !HandlerDData->dwTrans) {                 // If neither axis is selected,
  if( !HandlerDData->dwTrans) {                 // If neither axis is selected,
    EnableWindow( THandlerDialog->hAccelT, FALSE );                     // disable the Acceleration title string,
    EnableWindow( THandlerDialog->hAccel,  FALSE );                     // disable the Acceleration edit field,
    EnableWindow( THandlerDialog->hDecelT, FALSE );                     // disable the Deceleration title string,
    EnableWindow( THandlerDialog->hDecel,  FALSE );                     // disable the Deceleration edit field,
    EnableWindow( THandlerDialog->hVelT,   FALSE );                     // disable the Velocity title string,
    EnableWindow( THandlerDialog->hVel,    FALSE );                     // and disable the Velocity edit field
    }

  // Set current states for the radio buttons

  EnableWindow( THandlerDialog->h2G811, FALSE );                        // Only the 2G810 system is accommodated as of 9/29/93
  Button_SetCheck( GetDlgItem( hWindow, IDD_HANDLER_2G810  ), 1 );      // Check box
  EnableWindow( THandlerDialog->hFlip,  FALSE );                        // The flip option is not yet accommodated as of 9/29/93
  Button_SetCheck( GetDlgItem( hWindow, IDD_HANDLER_NOFLIP ), 1 );      // Check box
  }


// Respond to the Translation axis check box

void THandlerDialogIDTrans( HWND hWindow, THANDLERDIALOG *THandlerDialog, CONFIG *TemporaryDData, SH *HandlerDData, AF *DemagDData ) {

  DWORD dwTrans, dwRot;

  // Determine state of check boxes

  dwTrans = Button_GetCheck( GetDlgItem( hWindow, IDD_HANDLER_TRANS ) );        // Translation axis
  dwRot   = Button_GetCheck( GetDlgItem( hWindow, IDD_HANDLER_ROT   ) );        // Rotation axis

  if( dwTrans ) {                                                               // If the translation axis is selected,
    EnableWindow( THandlerDialog->hAccelT, TRUE );                              // enable the Acceleration title string,
    EnableWindow( THandlerDialog->hAccel,  TRUE );                              // enable the Acceleration edit field,
    EnableWindow( THandlerDialog->hDecelT, TRUE );                              // enable the Deceleration title string,
    EnableWindow( THandlerDialog->hDecel,  TRUE );                              // enable the Deceleration edit field,
    EnableWindow( THandlerDialog->hVelT,   TRUE );                              // enable the Velocity title string,
    EnableWindow( THandlerDialog->hVel,    TRUE );                              // enable the Velocity edit field,
    EnableWindow( THandlerDialog->hVelMT,  TRUE );                              // enable the Velocity into measurement region title string,
    EnableWindow( THandlerDialog->hVelM,   TRUE );                              // enable the Velocity into measurement region edit field,
    EnableWindow( THandlerDialog->hPos,    TRUE );                              // enable the Translation positions group box
    SetDlgItemText( hWindow, IDD_HANDLER_ACCEL, HandlerDData->achAccel );       // set text in the Acceleration edit field,
    SetDlgItemText( hWindow, IDD_HANDLER_DECEL, HandlerDData->achDecel );       // set text in the Deceleration edit field,
    SetDlgItemText( hWindow, IDD_HANDLER_VEL,   HandlerDData->achVel   );       // set text in the Velocity edit field
    SetDlgItemText( hWindow, IDD_HANDLER_VELM,  HandlerDData->achVelM  );       // and set the text in the Velocity into measurement region edit field

    if( DemagDData->dwX ) {                                                     // If the AF demagnetizer X axis is also selected,
      EnableWindow( THandlerDialog->hPosTransXT, TRUE );                        // enable the Transverse X AF position title string,
      EnableWindow( THandlerDialog->hPosTransX,  TRUE );                        // enable the Transverse X AF position edit field,
      SetDlgItemText( hWindow, IDD_HANDLER_POSTRANSX, HandlerDData->achPosXAF );// and set the text in the Transverse X AF edit field
      }

    if( DemagDData->dwY ) {                                                     // If the AF demagnetizer Y axis is also selected,
      EnableWindow( THandlerDialog->hPosTransYT, TRUE );                        // enable the Transverse Y AF position title string,
      EnableWindow( THandlerDialog->hPosTransY,  TRUE );                        // enable the Transverse Y AF position edit field,
      SetDlgItemText( hWindow, IDD_HANDLER_POSTRANSY, HandlerDData->achPosYAF );// and set the text in the Transverse Y AF edit field
      }

    if( DemagDData->dwZ ) {                                                     // If the AF demagnetizer Z axis is also selected,
      EnableWindow( THandlerDialog->hPosAxialT, TRUE );                         // enable the Axial AF position title string,
      EnableWindow( THandlerDialog->hPosAxial,  TRUE );                         // enable the Axial AF position edit field,
      SetDlgItemText( hWindow, IDD_HANDLER_POSAXIAL, HandlerDData->achPosZAF ); // and set the text in the Axial AF edit field
      }

    EnableWindow( THandlerDialog->hPosLoadT, TRUE );                            // Enable the sample load position title string,
    EnableWindow( THandlerDialog->hPosLoad,  TRUE );                            // enable the Sample load position edit field,
    SetDlgItemText( hWindow, IDD_HANDLER_POSLOAD, HandlerDData->achPosLoad );   // and set the text in the Sample load edit field

    if( TemporaryDData->dwX || TemporaryDData->dwY || TemporaryDData->dwZ ) {   // If at least one of the magnetometer axes is also selected,
      EnableWindow( THandlerDialog->hPosBackT, TRUE );                          // enable the Background measurement position title string,
      EnableWindow( THandlerDialog->hPosBack,  TRUE );                          // enable the Background measurement position edit field,
      EnableWindow( THandlerDialog->hPosMeasT, TRUE );                          // enable the Sample measurement position title string,
      EnableWindow( THandlerDialog->hPosMeas,  TRUE );                          // enable the Sample measurement position edit field,
      SetDlgItemText( hWindow, IDD_HANDLER_POSBACK, HandlerDData->achPosBack ); // set the text in the Background edit field,
      SetDlgItemText( hWindow, IDD_HANDLER_POSMEAS, HandlerDData->achPosMeas ); // and set the text in the Measurement edit field
      }
    EnableWindow( THandlerDialog->hRightT, TRUE );      // Enable the Right limit title string,
    EnableWindow( THandlerDialog->hRight,  TRUE );      // and enable the Right limit combo box
    }

  else {                                                // If the translation axis is not selected,
    EnableWindow( THandlerDialog->hVelMT,      FALSE ); // disable the Velocity into measurement region title string,
    EnableWindow( THandlerDialog->hVelM,       FALSE ); // disable the Velocity into measurement region edit field,
    EnableWindow( THandlerDialog->hPos,        FALSE ); // disable the Translation positions group box,
    EnableWindow( THandlerDialog->hPosTransXT, FALSE ); // disable the Transverse X AF position title string,
    EnableWindow( THandlerDialog->hPosTransX,  FALSE ); // disable the Transverse X AF position edit field,
    EnableWindow( THandlerDialog->hPosTransYT, FALSE ); // disable the Transverse Y AF position title string,
    EnableWindow( THandlerDialog->hPosTransY,  FALSE ); // disable the Transverse Y AF position edit field,
    EnableWindow( THandlerDialog->hPosAxialT,  FALSE ); // disable the Axial AF position title string,
    EnableWindow( THandlerDialog->hPosAxial,   FALSE ); // disable the Axial AF position edit field,
    EnableWindow( THandlerDialog->hPosLoadT,   FALSE ); // disable the Sample load position title string,
    EnableWindow( THandlerDialog->hPosLoad,    FALSE ); // disable the Sample load position edit field,
    EnableWindow( THandlerDialog->hPosBackT,   FALSE ); // disable the Background measurement position title string,
    EnableWindow( THandlerDialog->hPosBack,    FALSE ); // disable the Background measurement position edit field,
    EnableWindow( THandlerDialog->hPosMeasT,   FALSE ); // disable the Sample measurement position title string,
    EnableWindow( THandlerDialog->hPosMeas,    FALSE ); // disable the Sample measurement position edit field,
    EnableWindow( THandlerDialog->hRightT,     FALSE ); // disable the Right limit title string,
    EnableWindow( THandlerDialog->hRight,      FALSE ); // and disable the Right limit combo box

    EnableWindow( THandlerDialog->hAccelT, FALSE );   // disable the Acceleration title string,
    EnableWindow( THandlerDialog->hAccel,  FALSE );   // disable the Acceleration edit field,
    EnableWindow( THandlerDialog->hDecelT, FALSE );   // disable the Deceleration title string,
    EnableWindow( THandlerDialog->hDecel,  FALSE );   // disable the Deceleration edit field,
    EnableWindow( THandlerDialog->hVelT,   FALSE );   // disable the Velocity title string,
    EnableWindow( THandlerDialog->hVel,    FALSE );   // and disable the Velocity edit field

    }
  }


// Respond to the Rotation axis check box

void THandlerDialogIDRot( HWND hWindow, THANDLERDIALOG *THandlerDialog, SH *HandlerDData ) {

  DWORD dwTrans, dwRot;

  // Determine state of check boxes

  dwTrans = Button_GetCheck( GetDlgItem( hWindow, IDD_HANDLER_TRANS ) );        // Translation axis
  dwRot   = Button_GetCheck( GetDlgItem( hWindow, IDD_HANDLER_ROT   ) );        // Rotation axis

  if( dwRot ) {                                         // If the rotation axis is selected,
    EnableWindow( THandlerDialog->hRotation, TRUE );    // enable the Rotation group box,
    EnableWindow( THandlerDialog->hCountsT,  TRUE );    // enable the Counts per one full revolution title string,
    EnableWindow( THandlerDialog->hCounts,   TRUE );    // enable the Counts per one full revolution edit field,

    // New Rotational controls.
    EnableWindow( GetDlgItem( hWindow, IDD_HANDLER_ACCEL2 ),    TRUE );   // and disable the Rot acc field
    EnableWindow( GetDlgItem( hWindow, IDD_HANDLER_ACCELT2 ),   TRUE );   // and disable the Rot acc field

    EnableWindow( GetDlgItem( hWindow, IDD_HANDLER_DECEL2 ),    TRUE );   // and disable the Rot decl field
    EnableWindow( GetDlgItem( hWindow, IDD_HANDLER_DECELT2 ),   TRUE );   // and disable the Rot decl field

    EnableWindow( GetDlgItem( hWindow, IDD_HANDLER_VEL2 ),      TRUE );   // and disable the Rot velocity field
    EnableWindow( GetDlgItem( hWindow, IDD_HANDLER_VELT2 ),     TRUE );   // and disable the Rot velocity field

    SetDlgItemText( hWindow, IDD_HANDLER_ACCEL2,  HandlerDData->achRotAccel     );  // set text in the Acceleration edit field,
    SetDlgItemText( hWindow, IDD_HANDLER_DECEL2,  HandlerDData->achRotDecel     );  // set text in the Deceleration edit field,
    SetDlgItemText( hWindow, IDD_HANDLER_VEL2,    HandlerDData->achRotVel       );  // set text in the Velocity edit field,
    SetDlgItemText( hWindow, IDD_HANDLER_COUNTS,  HandlerDData->achRotCounts );  // and set the text in the Counts per one full revolution edit field
    }

  else {                                                // If the rotation axis is not selected,
    EnableWindow( THandlerDialog->hRotation, FALSE );   // disable the Rotation group box,
    EnableWindow( THandlerDialog->hCountsT,  FALSE );   // disable the Counts per one full revolution title string,
    EnableWindow( THandlerDialog->hCounts,   FALSE );   // and disable the Counts per one full revolution edit field

    // New Rotational controls.
    EnableWindow( GetDlgItem( hWindow, IDD_HANDLER_ACCEL2 ),    FALSE );   // and disable the Rot acc field
    EnableWindow( GetDlgItem( hWindow, IDD_HANDLER_ACCELT2 ),   FALSE );   // and disable the Rot acc field

    EnableWindow( GetDlgItem( hWindow, IDD_HANDLER_DECEL2 ),    FALSE );   // and disable the Rot decl field
    EnableWindow( GetDlgItem( hWindow, IDD_HANDLER_DECELT2 ),   FALSE );   // and disable the Rot decl field

    EnableWindow( GetDlgItem( hWindow, IDD_HANDLER_VEL2 ),      FALSE );   // and disable the Rot velocity field
    EnableWindow( GetDlgItem( hWindow, IDD_HANDLER_VELT2 ),     FALSE );   // and disable the Rot velocity field

    }
  }


// Respond to the Ok button

void THandlerDialogIDOk( HWND hWindow, SH *HandlerDData ) {

  // Store selected item for the combo box

  HandlerDData->dwRight = Button_GetCheck( GetDlgItem( hWindow, IDD_HANDLER_RIGHT ) );  // Right limit switch

  // Determine state of check boxes

  HandlerDData->dwTrans = Button_GetCheck( GetDlgItem( hWindow, IDD_HANDLER_TRANS ) );  // Translation axis
  HandlerDData->dwRot   = Button_GetCheck( GetDlgItem( hWindow, IDD_HANDLER_ROT   ) );  // Rotation axis

  // Retrieve text in edit control

  GetDlgItemText( hWindow, IDD_HANDLER_ACCEL,     HandlerDData->achAccel,     4 );      // Acceleration
  GetDlgItemText( hWindow, IDD_HANDLER_DECEL,     HandlerDData->achDecel,     4 );      // Deceleration
  GetDlgItemText( hWindow, IDD_HANDLER_VEL,       HandlerDData->achVel,       6 );      // Velocity
  GetDlgItemText( hWindow, IDD_HANDLER_VELM,      HandlerDData->achVelM,      6 );      // Velocity into measurement region
  GetDlgItemText( hWindow, IDD_HANDLER_POSTRANSX, HandlerDData->achPosXAF,    7 );      // Transverse X AF position
  GetDlgItemText( hWindow, IDD_HANDLER_POSTRANSY, HandlerDData->achPosYAF,    7 );      // Transverse Y AF position
  GetDlgItemText( hWindow, IDD_HANDLER_POSAXIAL,  HandlerDData->achPosZAF,    7 );      // Axial AF position
  GetDlgItemText( hWindow, IDD_HANDLER_POSLOAD,   HandlerDData->achPosLoad,   7 );      // Sample load position
  GetDlgItemText( hWindow, IDD_HANDLER_POSBACK,   HandlerDData->achPosBack,   7 );      // Background position
  GetDlgItemText( hWindow, IDD_HANDLER_POSMEAS,   HandlerDData->achPosMeas,   7 );      // Measurement position
  GetDlgItemText( hWindow, IDD_HANDLER_COUNTS,    HandlerDData->achRotCounts, 6 );      // Counts per one full revolution
  
  GetDlgItemText( hWindow, IDD_HANDLER_ACCEL2,     HandlerDData->achRotAccel, 4 );      // Acceleration
  GetDlgItemText( hWindow, IDD_HANDLER_DECEL2,     HandlerDData->achRotDecel, 4 );      // Deceleration
  GetDlgItemText( hWindow, IDD_HANDLER_VEL2,       HandlerDData->achRotVel,   6 );      // Velocity

//  TDialogOk( Msg );
  }


// TRUE if dialog box can close

BOOL THandlerDialogCanClose( HWND hWindow, CONFIG *TemporaryDData, AF *DemagDData ) {
  
  char  achRotAccel[ 4 ],  achRotDecel[ 4 ], achAccel[ 4 ],  achDecel[ 4 ],    achVel[ 6 ],    achVelM[ 6 ], achRotVel[ 6 ];
  char  achPosXAF[ 7 ], achPosYAF[ 7 ], achPosZAF[ 7 ], achPosLoad[ 7 ], achPosBack[ 7 ], achPosMeas[ 7 ];
  char  achCounts[ 6 ];
  int   nAccelRot, nDecelRot, nAccel, nDecel, nCounts;
  LONG  lVelRot;
  LONG  lVel, lVelM, lPosXAF, lPosYAF, lPosZAF, lPosLoad, lPosBack, lPosMeas;
  DWORD dwTrans, dwRot;
  
  // Determine state of check boxes
  
  dwTrans = Button_GetCheck( GetDlgItem( hWindow, IDD_HANDLER_TRANS ) );// Translation axis
  dwRot   = Button_GetCheck( GetDlgItem( hWindow, IDD_HANDLER_ROT   ) );// Rotation axis
  
  // Retrieve entry text from edit fields
  
  
  GetDlgItemText( hWindow, IDD_HANDLER_ACCEL2,    achRotAccel, 4 );      // Acceleration
  GetDlgItemText( hWindow, IDD_HANDLER_DECEL2,    achRotDecel, 4 );      // Deceleration
  GetDlgItemText( hWindow, IDD_HANDLER_VEL2,      achRotVel,   6 );      // Velocity
  
  GetDlgItemText( hWindow, IDD_HANDLER_ACCEL,     achAccel,   4 );      // Acceleration
  GetDlgItemText( hWindow, IDD_HANDLER_DECEL,     achDecel,   4 );      // Deceleration
  GetDlgItemText( hWindow, IDD_HANDLER_VEL,       achVel,     6 );      // Velocity
  GetDlgItemText( hWindow, IDD_HANDLER_VELM,      achVelM,    6 );      // Velocity into measurement region
  GetDlgItemText( hWindow, IDD_HANDLER_POSLOAD,   achPosLoad, 7 );      // Sample Load position
  GetDlgItemText( hWindow, IDD_HANDLER_POSTRANSX, achPosXAF,  7 );      // Transverse X AF position
  GetDlgItemText( hWindow, IDD_HANDLER_POSTRANSY, achPosYAF,  7 );      // Transverse Y AF position
  GetDlgItemText( hWindow, IDD_HANDLER_POSAXIAL,  achPosZAF,  7 );      // Axial AF position
  GetDlgItemText( hWindow, IDD_HANDLER_POSBACK,   achPosBack, 7 );      // Background position
  GetDlgItemText( hWindow, IDD_HANDLER_POSMEAS,   achPosMeas, 7 );      // Measurement position
  GetDlgItemText( hWindow, IDD_HANDLER_COUNTS,    achCounts,  6 );      // Counts per one full revolution
  
  // Convert strings to numeric values
  
  nAccel   = atoi( achAccel   );        // Acceleration
  nDecel   = atoi( achDecel   );        // Deceleration
  lVel     = atol( achVel     );        // Velocity
  lVelM    = atol( achVelM    );        // Velocity into measurement region
  lPosLoad = atol( achPosLoad );        // Sample Load position
  lPosXAF  = atol( achPosXAF  );        // Transverse X AF position
  lPosYAF  = atol( achPosYAF  );        // Transverse Y AF position
  lPosZAF  = atol( achPosZAF  );        // Axial AF position
  lPosBack = atol( achPosBack );        // Background position
  lPosMeas = atol( achPosMeas );        // Measurement position
  nCounts  = atoi( achCounts  );        // Counts per one full revolution
  nAccelRot = atoi( achRotAccel );
  nDecelRot = atoi( achRotDecel );
  lVelRot   = atol( achRotVel );
  
  
  // At least one axis of motion must be selected
  
  if( !dwTrans && !dwRot ) {            // If both axes are unchecked,
    MessageBeep( 0 );                   // beep the speaker in warning, and
    return FALSE;
    }
  
  // Entry text must be in a valid format and values must be within bounds
  
  if( dwTrans ) {
    if( !DigitsAndSign( achAccel ) || nAccel < 0 || nAccel > 127 ) {              // If Acceleration entry is not just digits with one, optional hyphen,
      MessageBeep( 0 );                                                           // beep the speaker in warning,
      SetDlgItemText( hWindow, IDD_HANDLER_ACCEL, '\0' );                         // clear the entry field, and
      return FALSE;
      }
    
    if( !DigitsAndSign( achDecel ) || nDecel < 0 || nDecel > 127 ) {              // If Deceleration entry is not just digits with one, optional hyphen,
      MessageBeep( 0 );                                                           // beep the speaker in warning,
      SetDlgItemText( hWindow, IDD_HANDLER_DECEL, '\0' );                         // clear the entry field, and
      return FALSE;
      }
    
    if( !DigitsAndSign( achVel ) || lVel < 50L || lVel > 12000L ) {               // If Velocity entry is not just digits with one, optional hyphen,
      MessageBeep( 0 );                                                           // beep the speaker in warning,
      SetDlgItemText( hWindow, IDD_HANDLER_VEL, '\0' );                           // clear the entry field, and
      return FALSE;
      }
    
    if( dwTrans ) {                                                               // Translation axis
      
      if( !DigitsAndSign( achVelM ) || lVelM < 50L || lVelM > 12000L) {           // If Velocity into measurement region entry is not just digits with one optional hyphen,
        MessageBeep( 0 );                                                         // beep the speaker in warning,
        SetDlgItemText( hWindow, IDD_HANDLER_VELM, '\0' );                        // clear the entry field, and
        return FALSE;
        }
      if( !DigitsAndSign( achPosLoad ) || lPosLoad % 10L || labs( lPosLoad ) > 100000L ) {// If Sample load position entry is not just digits with one optional hyphen, and evenly divisible by 10,
        MessageBeep( 0 );                                                         // beep the speaker in warning,
        SetDlgItemText( hWindow, IDD_HANDLER_POSLOAD, '\0' );                     // clear the entry field, and
        return FALSE;
        }
      if( DemagDData->dwX ) {                                                     // If the AF demagnetizer X axis is also selected
        if( !DigitsAndSign( achPosXAF ) || lPosXAF % 10L || labs( lPosXAF ) > 100000L ) { // If Transverse X AF position entry is not just digits with one optional hyphen, and evenly divisible by 10,
          MessageBeep( 0 );                                                       // beep the speaker in warning,
          SetDlgItemText( hWindow, IDD_HANDLER_POSTRANSX, '\0' );                 // clear the entry field, and
          return FALSE;
          }
        }
      if( DemagDData->dwY ) {                                                     // If the AF demagnetizer Y axis is also selected
        if( !DigitsAndSign( achPosYAF ) || lPosYAF % 10L || labs( lPosYAF ) > 100000L ) { // If Transverse Y AF position entry is not just digits with one optional hyphen, and evenly divisible by 10,
          MessageBeep( 0 );                                                       // beep the speaker in warning,
          SetDlgItemText( hWindow, IDD_HANDLER_POSTRANSY, '\0' );                 // clear the entry field, and
          return FALSE;
          }
        }
      if( DemagDData->dwZ ) {                                                     // If the AF demagnetizer Z axis is also selected
        if( !DigitsAndSign( achPosZAF ) || lPosZAF % 10L || labs(lPosZAF) > 100000L ) {   // If Transverse Z AF position entry is not just digits with one optional hyphen, and evenly divisible by 10,
          MessageBeep( 0 );                                                       // beep the speaker in warning,
          SetDlgItemText( hWindow, IDD_HANDLER_POSAXIAL, '\0' );                  // clear the entry field, and
          return FALSE;
          }
        }
      if( TemporaryDData->dwX || TemporaryDData->dwY || TemporaryDData->dwZ ) {   // If at least one of the magnetometer axes is also selected
        if( !DigitsAndSign( achPosBack ) || lPosBack % 10L || labs(lPosBack) > 100000L ) { // If Background position entry is not just digits with one optional hyphen, and evenly divisible by 10,
          MessageBeep( 0 );                                                       // beep the speaker in warning,
          SetDlgItemText( hWindow, IDD_HANDLER_POSBACK, '\0' );                   // clear the entry field, and
          return FALSE;
          }
        if( !DigitsAndSign( achPosMeas ) || lPosMeas % 10L || labs( lPosMeas ) > 100000L) { // If Measurement position entry is not just digits with one optional hyphen, and evenly divisible by 10,
          MessageBeep( 0 );                                                       // beep the speaker in warning,
          SetDlgItemText( hWindow, IDD_HANDLER_POSMEAS, '\0' );                   // clear the entry field, and
          return FALSE;
          }
        }
      }
    }
  
  // If the rotation axis is selected, retrieve entry text from an additional edit field
  
  if( dwRot ) {                                                                 // Rotation axis
    if( !DigitsAndSign( achCounts ) || nCounts % 10 || abs( nCounts ) > 5000 ) {// If Counts per one full sample revolution entry is not just digits with one optional hyphen, and evenly divisible by 10,
      MessageBeep( 0 );                                                         // beep the speaker in warning,
      SetDlgItemText( hWindow, IDD_HANDLER_COUNTS, '\0' );                      // clear the entry field, and
      return FALSE;
      }
    
    if( !DigitsAndSign( achRotVel ) || lVelRot < 50L || lVelRot > 12000L) {     // If Velocity into measurement region entry is not just digits with one optional hyphen,
      MessageBeep( 0 );                                                         // beep the speaker in warning,
      SetDlgItemText( hWindow, IDD_HANDLER_VEL2, '\0' );                       // clear the entry field, and
      return FALSE;
      }
    
    if( !DigitsAndSign( achRotAccel ) || nAccelRot < 0 || nAccelRot > 127 ) {   // If Acceleration entry is not just digits with one, optional hyphen,
      MessageBeep( 0 );                                                         // beep the speaker in warning,
      SetDlgItemText( hWindow, IDD_HANDLER_ACCEL2, '\0' );                      // clear the entry field, and
      return FALSE;
      }
    
    if( !DigitsAndSign( achRotDecel ) || nDecelRot< 0 || nDecelRot > 127 ) {    // If Deceleration entry is not just digits with one, optional hyphen,
      MessageBeep( 0 );                                                         // beep the speaker in warning,
      SetDlgItemText( hWindow, IDD_HANDLER_DECEL2, '\0' );                      // clear the entry field, and
      return FALSE;
      }
    }
    return TRUE;  // Entry values must all be valid
  }


BOOL CALLBACK THandlerDialogProcess( HWND hWindow, UINT uMsg, WPARAM wParam, LPARAM lParam ) {

  ACQUIRE *Acquire;

  switch( uMsg ) {

    /* This Get Called When Windows Shuts Down, Asking Ok To Close Application. */

    case WM_QUERYENDSESSION:
      return TRUE;

    /* This Get Called When Windows Shuts Diwn. Telling To Close Application. */

    case WM_ENDSESSION:
      if( wParam && ( ( Acquire = (ACQUIRE *) GetWindowLong( hWindow, GWL_USERDATA ) ) != (ACQUIRE *) NULL ) ) {
        EndDialog( hWindow, 0 );
        }
      return TRUE;

    case WM_INITDIALOG:
      if( ( Acquire = (ACQUIRE *) lParam ) != (ACQUIRE *) NULL ) {
        SetWindowLong( hWindow, GWL_USERDATA, lParam );
        THandlerDialogSetupWindow( hWindow, &Acquire->ac_THandlerDialog, &Acquire->ac_TemporaryDData, &Acquire->ac_HandlerDData, &Acquire->ac_DemagDData );
        return TRUE;
        }
      break;

    case WM_CLOSE:
      if( ( Acquire = (ACQUIRE *) GetWindowLong( hWindow, GWL_USERDATA ) ) != (ACQUIRE *) NULL ) {
        EndDialog( hWindow, 0 );
        }
      return TRUE;

    case WM_COMMAND:
      if( ( Acquire = (ACQUIRE *) GetWindowLong( hWindow, GWL_USERDATA ) ) != (ACQUIRE *) NULL ) {
        switch( LOWORD( wParam ) ) {

          // Responds to the Translation axis check box

          case IDD_HANDLER_TRANS:
            THandlerDialogIDTrans( hWindow, &Acquire->ac_THandlerDialog, &Acquire->ac_TemporaryDData, &Acquire->ac_HandlerDData, &Acquire->ac_DemagDData );
            return TRUE;

          // Responds to the Rotation axis check box

          case IDD_HANDLER_ROT:
            THandlerDialogIDRot( hWindow, &Acquire->ac_THandlerDialog, &Acquire->ac_HandlerDData );
            return TRUE;

          // Responds to the Ok button

          case IDD_HANDLER_OK:
            if( THandlerDialogCanClose( hWindow, &Acquire->ac_TemporaryDData, &Acquire->ac_DemagDData ) ) {
              THandlerDialogIDOk( hWindow, &Acquire->ac_HandlerDData );
              EndDialog( hWindow, 0 );
              }
            return TRUE;

          case IDCANCEL:
            EndDialog( hWindow, 0 );
            return TRUE;
          }
        }
      break;
    }
  return FALSE;
  }


/************************************************************************
*                                                                       *
*       TAxesDialog:                                                    *
*                                                                       *
************************************************************************/

void TAxesDialogSetupWindow( HWND hWindow, CALIB *ConstantsDData, CONFIG *TemporaryDData ) {

  HWND hXGroup, hXCalib, hX, hXEmu;
  HWND hYGroup, hYCalib, hY, hYEmu;
  HWND hZGroup, hZCalib, hZ, hZEmu;

  // Retrieve window handles for dialog box controls to be enabled/disabled

  hXGroup = GetDlgItem( hWindow, IDD_CALIB_XGROUP );    // X axis group box label
  hXCalib = GetDlgItem( hWindow, IDD_CALIB_XCALIB );    // X axis title string
  hX      = GetDlgItem( hWindow, IDD_CALIB_X      );    // X axis edit field
  hXEmu   = GetDlgItem( hWindow, IDD_CALIB_XEMU   );    // X axis emu string

  hYGroup = GetDlgItem( hWindow, IDD_CALIB_YGROUP );    // Y axis group box label
  hYCalib = GetDlgItem( hWindow, IDD_CALIB_YCALIB );    // Y axis title string
  hY      = GetDlgItem( hWindow, IDD_CALIB_Y      );    // Y axis edit field
  hYEmu   = GetDlgItem( hWindow, IDD_CALIB_YEMU   );    // Y axis emu string

  hZGroup = GetDlgItem( hWindow, IDD_CALIB_ZGROUP );    // Z axis group box label
  hZCalib = GetDlgItem( hWindow, IDD_CALIB_ZCALIB );    // Z axis title string
  hZ      = GetDlgItem( hWindow, IDD_CALIB_Z      );    // Z axis edit field
  hZEmu   = GetDlgItem( hWindow, IDD_CALIB_ZEMU   );    // Z axis emu string

  // Disable those controls for which the instrument is not configured

  if( TemporaryDData->dwX )                                             // X axis magnetometer
    SetDlgItemText( hWindow, IDD_CALIB_X, ConstantsDData->achX );       // Set text in edit control
  else { // No X axis
    EnableWindow( hXGroup, FALSE );
    EnableWindow( hXCalib, FALSE );
    EnableWindow( hX,      FALSE );
    EnableWindow( hXEmu,   FALSE );
    }

  if( TemporaryDData->dwY )                                             // Y axis magnetometer
    SetDlgItemText( hWindow, IDD_CALIB_Y, ConstantsDData->achY );       // Set text in edit control
  else { // No Y axis
    EnableWindow( hYGroup, FALSE );
    EnableWindow( hYCalib, FALSE );
    EnableWindow( hY,      FALSE );
    EnableWindow( hYEmu,   FALSE );
    }

  if( TemporaryDData->dwZ )                                             // Z axis magnetometer
    SetDlgItemText( hWindow, IDD_CALIB_Z, ConstantsDData->achZ );       // Set text in edit control
  else { // No Z axis
    EnableWindow( hZGroup, FALSE );
    EnableWindow( hZCalib, FALSE );
    EnableWindow( hZ,      FALSE );
    EnableWindow( hZEmu,   FALSE );
    }
  }


// Respond to the Ok button

void TAxesDialogIDOk( HWND hWindow, CALIB *ConstantsDData ) {

  // Retrieve text in edit controls

  GetDlgItemText( hWindow, IDD_CALIB_X, ConstantsDData->achX, 11 );     // X axis
  GetDlgItemText( hWindow, IDD_CALIB_Y, ConstantsDData->achY, 11 );     // Y axis
  GetDlgItemText( hWindow, IDD_CALIB_Z, ConstantsDData->achZ, 11 );     // Z axis
////  TDialogOk( Msg );
  }


// TRUE if dialog box can close

BOOL TAxesDialogCanClose( HWND hWindow ) {

  char   achX[ 11 ], achY[ 11 ], achZ[ 11 ];
  double dX, dY, dZ;

  // Retrieve text in edit controls

  GetDlgItemText( hWindow, IDD_CALIB_X, achX, 11 );     // X axis
  GetDlgItemText( hWindow, IDD_CALIB_Y, achY, 11 );     // Y axis
  GetDlgItemText( hWindow, IDD_CALIB_Z, achZ, 11 );     // Z axis

  // Convert strings to double values

  dX = strtod( achX, NULL );    // X axis
  dY = strtod( achY, NULL );    // Y axis
  dZ = strtod( achZ, NULL );    // Z axis

  // Each entry must be in the proper format, and its value must be within bounds

  if( !ScientificAndSign( achX ) || dX < -9.999e-03 || dX > 9.999e-03 ) {       // If entry is not in proper scientific notation,
    MessageBeep( 0 );                                                           // beep the speaker in warning,
    SetDlgItemText( hWindow, IDD_CALIB_X, '\0' );                               // clear the entry field, and
    return FALSE;
    }

  if( !ScientificAndSign( achY ) || dY < -9.999e-03 || dY > 9.999e-03 ) {       // If entry is not in proper scientific notation,
    MessageBeep( 0 );                                                           // beep the speaker in warning,
    SetDlgItemText( hWindow, IDD_CALIB_Y, '\0' );                               // clear the entry field, and
    return FALSE;
    }

  if( !ScientificAndSign( achZ ) || dZ < -9.999e-03 || dZ > 9.999e-03 ) {       // If entry is not in proper scientific notation,
    MessageBeep( 0 );                                                           // beep the speaker in warning,
    SetDlgItemText( hWindow, IDD_CALIB_Z, '\0' );                               // clear the entry field, and
    return FALSE;
    }
  return TRUE;                                                                  // Selected values must all be valid
  }


BOOL CALLBACK TAxesDialogProcess( HWND hWindow, UINT uMsg, WPARAM wParam, LPARAM lParam ) {

  ACQUIRE *Acquire;

  switch( uMsg ) {

    /* This Get Called When Windows Shuts Down, Asking Ok To Close Application. */

    case WM_QUERYENDSESSION:
      return TRUE;

    /* This Get Called When Windows Shuts Diwn. Telling To Close Application. */

    case WM_ENDSESSION:
      if( wParam && ( ( Acquire = (ACQUIRE *) GetWindowLong( hWindow, GWL_USERDATA ) ) != (ACQUIRE *) NULL ) ) {
        EndDialog( hWindow, 0 );
        }
      return TRUE;

    case WM_INITDIALOG:
      if( ( Acquire = (ACQUIRE *) lParam ) != (ACQUIRE *) NULL ) {
        SetWindowLong( hWindow, GWL_USERDATA, lParam );
        TAxesDialogSetupWindow( hWindow, &Acquire->ac_ConstantsDData, &Acquire->ac_TemporaryDData );
        return TRUE;
        }
      break;

    case WM_CLOSE:
      if( ( Acquire = (ACQUIRE *) GetWindowLong( hWindow, GWL_USERDATA ) ) != (ACQUIRE *) NULL ) {
        EndDialog( hWindow, 0 );
        }
      return TRUE;

    case WM_COMMAND:
      if( ( Acquire = (ACQUIRE *) GetWindowLong( hWindow, GWL_USERDATA ) ) != (ACQUIRE *) NULL ) {
        switch( LOWORD( wParam ) ) {

          // Responds to the Ok button

          case IDD_CALIB_OK:
            if( TAxesDialogCanClose( hWindow  ) ) {
               TAxesDialogIDOk( hWindow, &Acquire->ac_ConstantsDData );
              EndDialog( hWindow, 0 );
              }
            return TRUE;

          case IDCANCEL:
            EndDialog( hWindow, 0 );
            return TRUE;
          }
        }
      break;
    }
  return FALSE;
  }


/************************************************************************
*                                                                       *
*       TConfigureDialog:                                               *
*                                                                       *
************************************************************************/

// Initialize controls and set current values

void TConfigureDialogSetupWindow( HWND hWindow, CONFIGUREDIALOG *ConfigureDialog, CONFIG *ConfigureDData ) {

  // Retrieve window handles for dialog box controls to be enabled/disabled

  ConfigureDialog->hCalib  = GetDlgItem( hWindow, IDD_CONFIG_CALIB  );  // Calibration constants button
  ConfigureDialog->hPos    = GetDlgItem( hWindow, IDD_CONFIG_POS    );  // Parameters and positions button
  ConfigureDialog->hParam  = GetDlgItem( hWindow, IDD_CONFIG_PARAM  );  // Parameters button
  ConfigureDialog->hMag    = GetDlgItem( hWindow, IDD_CONFIG_MAG    );  // Magnetometer serial port title string
  ConfigureDialog->hAF     = GetDlgItem( hWindow, IDD_CONFIG_AF     );  // AF demagnetizer serial port title string
  ConfigureDialog->hSH     = GetDlgItem( hWindow, IDD_CONFIG_SH     );  // Sample handler serial port title string
  ConfigureDialog->hMagCom = GetDlgItem( hWindow, IDD_CONFIG_MAGCOM );  // Magnetometer serial port combo box
  ConfigureDialog->hAFCom  = GetDlgItem( hWindow, IDD_CONFIG_AFCOM  );  // AF demagnetizer serial port combo box
  ConfigureDialog->hSHCom  = GetDlgItem( hWindow, IDD_CONFIG_SHCOM  );  // Sample handler serial port combo box

  // Add choices to the Magnetometer combo box

  ComboBox_InsertString( ConfigureDialog->hMagCom,  0, (DWORD) "    "  );
  ComboBox_InsertString( ConfigureDialog->hMagCom,  1, (DWORD) "COM1"  );
  ComboBox_InsertString( ConfigureDialog->hMagCom,  2, (DWORD) "COM2"  );
  ComboBox_InsertString( ConfigureDialog->hMagCom,  3, (DWORD) "COM3"  );
  ComboBox_InsertString( ConfigureDialog->hMagCom,  4, (DWORD) "COM4"  );
  ComboBox_InsertString( ConfigureDialog->hMagCom,  5, (DWORD) "COM5"  );
  ComboBox_InsertString( ConfigureDialog->hMagCom,  6, (DWORD) "COM6"  );
  ComboBox_InsertString( ConfigureDialog->hMagCom,  7, (DWORD) "COM7"  );
  ComboBox_InsertString( ConfigureDialog->hMagCom,  8, (DWORD) "COM8"  );
  ComboBox_InsertString( ConfigureDialog->hMagCom,  9, (DWORD) "COM9"  );
  ComboBox_InsertString( ConfigureDialog->hMagCom, 10, (DWORD) "COM10" );
  ComboBox_InsertString( ConfigureDialog->hMagCom, 11, (DWORD) "COM11" );
  ComboBox_InsertString( ConfigureDialog->hMagCom, 12, (DWORD) "COM12" );

  // Add choices to the AF demagnetizer combo box

  ComboBox_InsertString( ConfigureDialog->hAFCom,  0, (DWORD) "    "  );
  ComboBox_InsertString( ConfigureDialog->hAFCom,  1, (DWORD) "COM1"  );
  ComboBox_InsertString( ConfigureDialog->hAFCom,  2, (DWORD) "COM2"  );
  ComboBox_InsertString( ConfigureDialog->hAFCom,  3, (DWORD) "COM3"  );
  ComboBox_InsertString( ConfigureDialog->hAFCom,  4, (DWORD) "COM4"  );
  ComboBox_InsertString( ConfigureDialog->hAFCom,  5, (DWORD) "COM5"  );
  ComboBox_InsertString( ConfigureDialog->hAFCom,  6, (DWORD) "COM6"  );
  ComboBox_InsertString( ConfigureDialog->hAFCom,  7, (DWORD) "COM7"  );
  ComboBox_InsertString( ConfigureDialog->hAFCom,  8, (DWORD) "COM8"  );
  ComboBox_InsertString( ConfigureDialog->hAFCom,  9, (DWORD) "COM9"  );
  ComboBox_InsertString( ConfigureDialog->hAFCom, 10, (DWORD) "COM10" );
  ComboBox_InsertString( ConfigureDialog->hAFCom, 11, (DWORD) "COM11" );
  ComboBox_InsertString( ConfigureDialog->hAFCom, 12, (DWORD) "COM12" );

  // Add choices to the Sample handler combo box

  ComboBox_InsertString( ConfigureDialog->hSHCom,  0, (DWORD) "    "  );
  ComboBox_InsertString( ConfigureDialog->hSHCom,  1, (DWORD) "COM1"  );
  ComboBox_InsertString( ConfigureDialog->hSHCom,  2, (DWORD) "COM2"  );
  ComboBox_InsertString( ConfigureDialog->hSHCom,  3, (DWORD) "COM3"  );
  ComboBox_InsertString( ConfigureDialog->hSHCom,  4, (DWORD) "COM4"  );
  ComboBox_InsertString( ConfigureDialog->hSHCom,  5, (DWORD) "COM5"  );
  ComboBox_InsertString( ConfigureDialog->hSHCom,  6, (DWORD) "COM6"  );
  ComboBox_InsertString( ConfigureDialog->hSHCom,  7, (DWORD) "COM7"  );
  ComboBox_InsertString( ConfigureDialog->hSHCom,  8, (DWORD) "COM8"  );
  ComboBox_InsertString( ConfigureDialog->hSHCom,  9, (DWORD) "COM9"  );
  ComboBox_InsertString( ConfigureDialog->hSHCom, 10, (DWORD) "COM10" );
  ComboBox_InsertString( ConfigureDialog->hSHCom, 11, (DWORD) "COM11" );
  ComboBox_InsertString( ConfigureDialog->hSHCom, 12, (DWORD) "COM12" );

  // Set current values in combo boxes

  ComboBox_SetCurSel( ConfigureDialog->hMagCom, ConfigureDData->dwMagCom );
  ComboBox_SetCurSel( ConfigureDialog->hAFCom,  ConfigureDData->dwAFCom  );
  ComboBox_SetCurSel( ConfigureDialog->hSHCom,  ConfigureDData->dwSHCom  );

  // Set current states for the check boxes

  if( ConfigureDData->dwX )                                             // X axis magnetometer
    Button_SetCheck( GetDlgItem( hWindow, IDD_CONFIG_X ), 1 );          // Check box
  if( ConfigureDData->dwY )                                             // Y axis magnetometer
    Button_SetCheck( GetDlgItem( hWindow, IDD_CONFIG_Y ), 1 );          // Check box
  if( ConfigureDData->dwZ )                                             // Z axis magnetometer
    Button_SetCheck( GetDlgItem( hWindow, IDD_CONFIG_Z ), 1 );          // Check box
  if( ConfigureDData->dwDCSquids )                                      // DC squids
    Button_SetCheck( GetDlgItem( hWindow, IDD_CONFIG_DCSQUIDS ), 1 );   // Check box
  if( !ConfigureDData->dwX &&
      !ConfigureDData->dwY &&
      !ConfigureDData->dwZ ) {                                          // If none of the axes has been selected,
    EnableWindow( ConfigureDialog->hCalib,  FALSE );                    // disable the Calibration constants button,
    EnableWindow( ConfigureDialog->hMag,    FALSE );                    // disable the Magnetometer serial port title string,
    EnableWindow( ConfigureDialog->hMagCom, FALSE );                    // and disable the Magnetometer serial port combo box
    }

  // Set current states for the radio buttons

  if( ConfigureDData->dwSHAuto ) {                                      // Automatic sample handler
    Button_SetCheck( GetDlgItem( hWindow, IDD_CONFIG_SHAUTO ), 1 );     // Check radio button,
    EnableWindow( ConfigureDialog->  hPos, TRUE );                      // enable the Parameters and positions button,
    EnableWindow( ConfigureDialog->   hSH, TRUE );                      // enable the Sample handler serial port title string,
    EnableWindow( ConfigureDialog->hSHCom, TRUE );                      // and enable the Sample handler serial port combo box
    }
  else { // Manual sample handler
    Button_SetCheck( GetDlgItem( hWindow, IDD_CONFIG_SHMAN ), 1 );      // Check radio button,
    EnableWindow( ConfigureDialog->  hPos, FALSE );                     // disable the Parameters and positions button,
    EnableWindow( ConfigureDialog->   hSH, FALSE );                     // disable the Sample handler serial port title string,
    EnableWindow( ConfigureDialog->hSHCom, FALSE );                     // and disable the Sample handler serial port combo box
    }

  EnableWindow( ConfigureDialog->hParam, FALSE );                       // Disable the Parameters button
  EnableWindow( ConfigureDialog->   hAF, FALSE );                       // Disable the AF demagnetizer serial port title string
  EnableWindow( ConfigureDialog->hAFCom, FALSE );                       // Disable the AF demagnetizer serial port combo box

  if( ConfigureDData->dwAFAuto ) {                                      // Automatic AF demagnetizer
    Button_SetCheck( GetDlgItem( hWindow, IDD_CONFIG_AFAUTO ), 1 );     // Check radio button if it exists,
    EnableWindow( ConfigureDialog->hParam, TRUE );                      // enable the Parameters button,
    EnableWindow( ConfigureDialog->   hAF, TRUE );                      // enable the AF demagnetizer serial port title string,
    EnableWindow( ConfigureDialog->hAFCom, TRUE );                      // and enable the AF demagnetizer serial port combo box
    }

  if( ConfigureDData->dwAFMan  )                                        // Manual AF demagnetizer
    Button_SetCheck( GetDlgItem( hWindow, IDD_CONFIG_AFMAN  ), 1 );     // Check radio button if it exists
  if( ConfigureDData->dwAFNone )                                        // No AF demagnetizer
    Button_SetCheck( GetDlgItem( hWindow, IDD_CONFIG_AFNONE ), 1 );     // Check radio button if it exists
  }


// Respond to the Magnetometer X axis check box

void TConfigureDialogIDXAxis( HWND hWindow, CONFIGUREDIALOG *ConfigureDialog, CONFIG *ConfigureDData, CONFIG *TemporaryDData ) {

  if( Button_GetCheck( GetDlgItem( hWindow, IDD_CONFIG_X ) ) ) {                // If check box is checked,
    EnableWindow( ConfigureDialog->hCalib,  TRUE );                             // enable the Calibration constants button,
    EnableWindow( ConfigureDialog->hMag,    TRUE );                             // enable the Magnetometer serial port title string,
    EnableWindow( ConfigureDialog->hMagCom, TRUE );                             // enable the Magnetometer serial port combo box,
    ComboBox_SetCurSel( ConfigureDialog->hMagCom, ConfigureDData->dwMagCom );   // and set selection
    TemporaryDData->dwX = 1;
    }

  // If check box is unchecked,

  else {
    TemporaryDData->dwX = 0;
    if( !Button_GetCheck( GetDlgItem( hWindow, IDD_CONFIG_Y ) ) &&
        !Button_GetCheck( GetDlgItem( hWindow, IDD_CONFIG_Z ) ) ) {             // If both Y and Z axes are also unchecked,
      EnableWindow( ConfigureDialog->hCalib, FALSE );                           // disable the Calibration constants button,
      ComboBox_SetCurSel( ConfigureDialog->hMagCom, 0 );                        // clear selection,
      EnableWindow( ConfigureDialog->hMag,    FALSE );                          // disable the Magnetometer serial port title string,
      EnableWindow( ConfigureDialog->hMagCom, FALSE );                          // and disable the Magnetometer serial port combo box
      }
    }
  }


// Respond to the Magnetometer Y axis check box

void TConfigureDialogIDYAxis( HWND hWindow, CONFIGUREDIALOG *ConfigureDialog, CONFIG *ConfigureDData, CONFIG *TemporaryDData ) {

  if( Button_GetCheck( GetDlgItem( hWindow, IDD_CONFIG_Y ) ) ) {                // If check box is checked,
    EnableWindow( ConfigureDialog-> hCalib, TRUE );                             // enable the Calibration constants button,
    EnableWindow( ConfigureDialog->   hMag, TRUE );                             // enable the Magnetometer serial port title string,
    EnableWindow( ConfigureDialog->hMagCom, TRUE );                             // enable the Magnetometer serial port combo box,
    ComboBox_SetCurSel( ConfigureDialog->hMagCom, ConfigureDData->dwMagCom );   // and set selection
    TemporaryDData->dwY = 1;
    }

  // If check box is unchecked,

  else {
    TemporaryDData->dwY = 0;
    if( !Button_GetCheck( GetDlgItem( hWindow, IDD_CONFIG_X ) ) &&
        !Button_GetCheck( GetDlgItem( hWindow, IDD_CONFIG_Z ) ) ) {             // If both X and Z axes are also unchecked,
      EnableWindow( ConfigureDialog-> hCalib, FALSE );                          // disable the Calibration constants button
      ComboBox_SetCurSel( ConfigureDialog->hMagCom, 0 );                        // clear selection,
      EnableWindow( ConfigureDialog->   hMag, FALSE );                          // disable the Magnetometer serial port title string,
      EnableWindow( ConfigureDialog->hMagCom, FALSE );                          // and disable the Magnetometer serial port combo box
      }
    }
  }


// Respond to the Magnetometer Z axis check box

void TConfigureDialogIDZAxis( HWND hWindow, CONFIGUREDIALOG *ConfigureDialog, CONFIG *ConfigureDData, CONFIG *TemporaryDData ) {

  if( Button_GetCheck( GetDlgItem( hWindow, IDD_CONFIG_Z ) ) ) {                // If check box is checked,
    EnableWindow( ConfigureDialog-> hCalib, TRUE );                             // enable the Calibration constants button,
    EnableWindow( ConfigureDialog->   hMag, TRUE );                             // enable the Magnetometer serial port title string,
    EnableWindow( ConfigureDialog->hMagCom, TRUE );                             // enable the Magnetometer serial port combo box,
    ComboBox_SetCurSel( ConfigureDialog->hMagCom, ConfigureDData->dwMagCom );   // and set selection
    TemporaryDData->dwZ = 1;
    }

  // If check box is unchecked,

  else {
    TemporaryDData->dwZ = 0;
    if( !Button_GetCheck( GetDlgItem( hWindow, IDD_CONFIG_X ) ) &&
        !Button_GetCheck( GetDlgItem( hWindow, IDD_CONFIG_Y ) ) ) {             // If both X and Y axes are also unchecked,
      EnableWindow( ConfigureDialog-> hCalib, FALSE );                          // disable the Calibration constants button
      ComboBox_SetCurSel( ConfigureDialog->hMagCom, 0 );                        // clear selection,
      EnableWindow( ConfigureDialog->   hMag, FALSE );                          // disable the Magnetometer serial port title string,
      EnableWindow( ConfigureDialog->hMagCom, FALSE );                          // and disable the Magnetometer serial port combo box
      }
    }
  }


// Respond to the Sample Handler Auto radio button

void TConfigureDialogIDSHAuto( HWND hWindow, CONFIGUREDIALOG *ConfigureDialog, CONFIG *ConfigureDData ) {

  EnableWindow( ConfigureDialog->  hPos, TRUE );                                // Enable the Parameters and positions button
  EnableWindow( ConfigureDialog->   hSH, TRUE );                                // Enable the Sample handler serial port title string
  EnableWindow( ConfigureDialog->hSHCom, TRUE );                                // Enable the Sample handler serial port combo box
  ComboBox_SetCurSel( ConfigureDialog->hSHCom, ConfigureDData->dwSHCom );       // Set selection
  }


// Respond to the Sample Handler Manual radio button

void TConfigureDialogIDSHMan( HWND hWindow, CONFIGUREDIALOG *ConfigureDialog, CONFIG *ConfigureDData ) {

  EnableWindow( ConfigureDialog->  hPos, FALSE );                               // Disable the Parameters and positions button
  ComboBox_SetCurSel( ConfigureDialog->hSHCom, 0 );                             // Clear selection
  EnableWindow( ConfigureDialog->   hSH, FALSE );                               // Disable the Sample handler serial port title string
  EnableWindow( ConfigureDialog->hSHCom, FALSE );                               // Disable the Sample handler serial port combo box
  }


// Respond to the AF Demagnetizer Auto radio button

void TConfigureDialogIDAFAuto( HWND hWindow, CONFIGUREDIALOG *ConfigureDialog, CONFIG *ConfigureDData ) {

  EnableWindow( ConfigureDialog->hParam, TRUE );                                // Enable the Parameters button
  EnableWindow( ConfigureDialog->hAF,    TRUE );                                // Enable the AF demagnetizer serial port title string
  EnableWindow( ConfigureDialog->hAFCom, TRUE );                                // Enable the AF demagnetizer serial port combo box
  ComboBox_SetCurSel( ConfigureDialog->hAFCom, ConfigureDData->dwAFCom );       // Set selection
  }


// Respond to the AF Demagnetizer Manual radio button

void TConfigureDialogIDAFMan( HWND hWindow, CONFIGUREDIALOG *ConfigureDialog, CONFIG *ConfigureDData ) {

  EnableWindow( ConfigureDialog->hParam, FALSE );                               // Disable the Parameters button
  ComboBox_SetCurSel( ConfigureDialog->hAFCom, 0 );                             // Clear selection
  EnableWindow( ConfigureDialog->   hAF, FALSE );                               // Disable the AF demagnetizer serial port title string
  EnableWindow( ConfigureDialog->hAFCom, FALSE );                               // Disable the AF demagnetizer serial port combo box
  }


// Respond to the AF Demagnetizer None radio button

void TConfigureDialogIDAFNone( HWND hWindow, CONFIGUREDIALOG *ConfigureDialog, CONFIG *ConfigureDData ) {

  EnableWindow( ConfigureDialog->hParam, FALSE );                               // Disable the Parameters button
  ComboBox_SetCurSel( ConfigureDialog->hAFCom, 0 );                             // Clear selection
  EnableWindow( ConfigureDialog->   hAF, FALSE );                               // Disable the AF demagnetizer serial port title string
  EnableWindow( ConfigureDialog->hAFCom, FALSE );                               // Disable the AF demagnetizer serial port combo box
  }


// Open the Magnetometer Calibration Constants dialog box

void TConfigureDialogIDCalibrate( ACQUIRE *Acquire ) {

  DialogBoxParam( Acquire->ac_hInstance, MAKEINTRESOURCE( Axes_Dialog ), Acquire->ac_hMainWindow, TAxesDialogProcess, (long) Acquire );
  }

// Open the Sample Handler Parameters and Positions dialog box

void TConfigureDialogIDPositions( ACQUIRE *Acquire ) {

  DialogBoxParam( Acquire->ac_hInstance, MAKEINTRESOURCE( Handler_Dialog ), Acquire->ac_hMainWindow, THandlerDialogProcess, (long) Acquire );
  }

// Open the AF Demagnetizer Parameters dialog box

void TConfigureDialogIDParameters( ACQUIRE *Acquire ) {

  DialogBoxParam( Acquire->ac_hInstance, MAKEINTRESOURCE( Demagnetizer_Dialog ),  Acquire->ac_hMainWindow, TDemagDialogProcess,   (long) Acquire );
  }


// Open the Core and Structure Orientation Conventions dialog box

void TConfigureDialogIDOrientation( ACQUIRE *Acquire ) {

  DialogBoxParam( Acquire->ac_hInstance, MAKEINTRESOURCE( Orientation_Dialog ), Acquire->ac_hMainWindow, TOrientDialogProcess,  (long) Acquire );
  }


// Respond to the Ok button

void TConfigureDialogIDOk( HWND hWindow, CONFIGUREDIALOG *ConfigureDialog, CONFIG *ConfigureDData ) {

  // Store selected items for the combo boxes

  ConfigureDData->dwMagCom   = ComboBox_GetCurSel( ConfigureDialog->hMagCom );
  ConfigureDData->dwAFCom    = ComboBox_GetCurSel( ConfigureDialog->hAFCom  );
  ConfigureDData->dwSHCom    = ComboBox_GetCurSel( ConfigureDialog->hSHCom  );

  // Determine state of check boxes

  ConfigureDData->dwX        = Button_GetCheck( GetDlgItem( hWindow, IDD_CONFIG_X        ) );
  ConfigureDData->dwY        = Button_GetCheck( GetDlgItem( hWindow, IDD_CONFIG_Y        ) );
  ConfigureDData->dwZ        = Button_GetCheck( GetDlgItem( hWindow, IDD_CONFIG_Z        ) );
  ConfigureDData->dwDCSquids = Button_GetCheck( GetDlgItem( hWindow, IDD_CONFIG_DCSQUIDS ) );

  // Determine which radio buttons are set

  ConfigureDData->dwSHAuto   = Button_GetCheck( GetDlgItem( hWindow, IDD_CONFIG_SHAUTO ) );
  ConfigureDData->dwSHMan    = Button_GetCheck( GetDlgItem( hWindow, IDD_CONFIG_SHMAN  ) );
  ConfigureDData->dwAFAuto   = Button_GetCheck( GetDlgItem( hWindow, IDD_CONFIG_AFAUTO ) );
  ConfigureDData->dwAFMan    = Button_GetCheck( GetDlgItem( hWindow, IDD_CONFIG_AFMAN  ) );
  ConfigureDData->dwAFNone   = Button_GetCheck( GetDlgItem( hWindow, IDD_CONFIG_AFNONE ) );

  /////TDialog::Ok(Msg);
  }


// TRUE if dialog box can close

BOOL TConfigureDialogCanClose( HWND hWindow, CONFIGUREDIALOG *ConfigureDialog ) {

  DWORD dwX, dwY, dwZ;
  DWORD dwSHAuto, dwAFAuto;
  DWORD dwMagCom, dwAFCom, dwSHCom;

  // Determine state of check boxes

  dwX = Button_GetCheck( GetDlgItem( hWindow, IDD_CONFIG_X ) );
  dwY = Button_GetCheck( GetDlgItem( hWindow, IDD_CONFIG_Y ) );
  dwZ = Button_GetCheck( GetDlgItem( hWindow, IDD_CONFIG_Z ) );

  // Determine which radio buttons are set

  dwSHAuto = Button_GetCheck( GetDlgItem( hWindow, IDD_CONFIG_SHAUTO ) );
  dwAFAuto = Button_GetCheck( GetDlgItem( hWindow, IDD_CONFIG_AFAUTO ) );

  // Get selected items for the combo boxes

  dwMagCom = ComboBox_GetCurSel( ConfigureDialog->hMagCom );
  dwAFCom  = ComboBox_GetCurSel( ConfigureDialog->hAFCom  );
  dwSHCom  = ComboBox_GetCurSel( ConfigureDialog->hSHCom  );

  // If at least one magnetometer axis is selected, there must be a dedicated serial port

  if( dwX || dwY || dwZ ) {
    if( dwMagCom == 0UL ) {
      MessageBeep( 0 );
      SetFocus( ConfigureDialog->hMagCom );
      return FALSE;
      }
    }

  // If an automatic AF demagnetizer is selected, there must be a dedicated serial port

  if( dwAFAuto ) {
    if( dwAFCom == 0UL ) {
      MessageBeep( 0 );
      SetFocus( ConfigureDialog->hAFCom );
      return FALSE;
      }
    }

  // If an automatic sample handler is selected, there must be a dedicated serial port

  if( dwSHAuto ) {
    if( dwSHCom == 0UL ) {
      MessageBeep( 0 );
      SetFocus( ConfigureDialog->hSHCom );
      return FALSE;
      }
    }

  // If there is a sample handler, it cannot share a serial port

  if( dwSHAuto ) {
    if( ( dwSHCom == dwMagCom ) || ( dwSHCom == dwAFCom ) ) {
      MessageBeep( 0 );
      SetFocus( ConfigureDialog->hSHCom );
      return FALSE;
      }
    }
  return TRUE;  // Selected values must all be valid
  }


BOOL CALLBACK TConfigureDialogProcess( HWND hWindow, UINT uMsg, WPARAM wParam, LPARAM lParam ) {

  ACQUIRE *Acquire;

  switch( uMsg ) {

    /* This Get Called When Windows Shuts Down, Asking Ok To Close Application. */

    case WM_QUERYENDSESSION:
      return TRUE;

    /* This Get Called When Windows Shuts Diwn. Telling To Close Application. */

    case WM_ENDSESSION:
      if( wParam && ( ( Acquire = (ACQUIRE *) GetWindowLong( hWindow, GWL_USERDATA ) ) != (ACQUIRE *) NULL ) ) {
        EndDialog( hWindow, IDCANCEL );
        }
      return TRUE;

    case WM_INITDIALOG:
      if( ( Acquire = (ACQUIRE *) lParam ) != (ACQUIRE *) NULL ) {
        SetWindowLong( hWindow, GWL_USERDATA, lParam );
        TConfigureDialogSetupWindow( hWindow, &Acquire->ac_ConfigureDialog, &Acquire->ac_ConfigureDData );
        return TRUE;
        }
      break;

    case WM_CLOSE:
      if( ( Acquire = (ACQUIRE *) GetWindowLong( hWindow, GWL_USERDATA ) ) != (ACQUIRE *) NULL ) {
        EndDialog( hWindow, IDCANCEL );
        }
      return TRUE;

    case WM_COMMAND:
      if( ( Acquire = (ACQUIRE *) GetWindowLong( hWindow, GWL_USERDATA ) ) != (ACQUIRE *) NULL ) {
        switch( LOWORD( wParam ) ) {

          // Responds to the Magnetometer X axis check box

          case IDD_CONFIG_X:
            TConfigureDialogIDXAxis( hWindow, &Acquire->ac_ConfigureDialog, &Acquire->ac_ConfigureDData, &Acquire->ac_TemporaryDData );
            return TRUE;

          // Responds to the Magnetometer Y axis check box

          case IDD_CONFIG_Y:
            TConfigureDialogIDYAxis( hWindow, &Acquire->ac_ConfigureDialog, &Acquire->ac_ConfigureDData, &Acquire->ac_TemporaryDData );
            return TRUE;

          // Responds to the Magnetometer Z axis check box

          case IDD_CONFIG_Z:
            TConfigureDialogIDZAxis( hWindow, &Acquire->ac_ConfigureDialog, &Acquire->ac_ConfigureDData, &Acquire->ac_TemporaryDData );
            return TRUE;

          // Responds to the Sample Handler Auto radio button

          case IDD_CONFIG_SHAUTO:
            TConfigureDialogIDSHAuto( hWindow, &Acquire->ac_ConfigureDialog, &Acquire->ac_ConfigureDData );
            return TRUE;

          // Responds to the Sample Handler Manual radio button

          case IDD_CONFIG_SHMAN:
            TConfigureDialogIDSHMan( hWindow, &Acquire->ac_ConfigureDialog, &Acquire->ac_ConfigureDData );
            return TRUE;

          // Responds to the AF Demagnetizer Auto radio button

          case IDD_CONFIG_AFAUTO:
            TConfigureDialogIDAFAuto( hWindow, &Acquire->ac_ConfigureDialog, &Acquire->ac_ConfigureDData );
            return TRUE;

          // Responds to the AF Demagnetizer Manual radio button

          case IDD_CONFIG_AFMAN:
            TConfigureDialogIDAFMan( hWindow, &Acquire->ac_ConfigureDialog, &Acquire->ac_ConfigureDData );
            return TRUE;

          // Responds to the AF Demagnetizer None radio button

          case IDD_CONFIG_AFNONE:
            TConfigureDialogIDAFNone( hWindow, &Acquire->ac_ConfigureDialog, &Acquire->ac_ConfigureDData );
            return TRUE;

          // Responds to the Calibration constants button

          case IDD_CONFIG_CALIB:
            TConfigureDialogIDCalibrate( Acquire );
            return TRUE;

          // Responds to the Parameters and positions button

          case IDD_CONFIG_POS:
            TConfigureDialogIDPositions( Acquire );
            return TRUE;

          // Responds to the Parameters button

          case IDD_CONFIG_PARAM:
            TConfigureDialogIDParameters( Acquire );
            return TRUE;

          // Responds to the Orientation conventions button

          case IDD_CONFIG_ORIENT:
            TConfigureDialogIDOrientation( Acquire );
            return TRUE;

          // Responds to the Ok button

          case IDD_CONFIG_OK:
            if( TConfigureDialogCanClose( hWindow, &Acquire->ac_ConfigureDialog  ) ) {
              TConfigureDialogIDOk( hWindow, &Acquire->ac_ConfigureDialog, &Acquire->ac_ConfigureDData );
              EndDialog( hWindow, IDOK );
              }
            return TRUE;

          case IDCANCEL:
            EndDialog( hWindow, IDCANCEL );
            return TRUE;
          }
        }
      break;
    }
  return FALSE;
  }


/************************************************************************
*                                                                       *
*       TMagSettingsDialog:                                             *
*                                                                       *
************************************************************************/

// Initialize controls and set current values

void TMagSettingsDialogSetupWindow( HWND hWindow, TMAGSETTINGSDIALOG *TMagSettingsDialog, CONFIG *ConfigureDData, SETTINGS *MagnetometerDData ) {

  // Retrieve window handles for dialog box controls to be enabled/disabled

  TMagSettingsDialog->hXGroup   = GetDlgItem( hWindow, IDD_MAG_XGROUP   );      // X axis group box
  TMagSettingsDialog->hXRangeT  = GetDlgItem( hWindow, IDD_MAG_XRANGET  );      // X axis range title string
  TMagSettingsDialog->hXRange   = GetDlgItem( hWindow, IDD_MAG_XRANGE   );      // X axis range combo box
  TMagSettingsDialog->hXFlux    = GetDlgItem( hWindow, IDD_MAG_XFLUX    );      // X axis flux counting check box
  TMagSettingsDialog->hXFilterT = GetDlgItem( hWindow, IDD_MAG_XFILTERT );      // X axis filter title string
  TMagSettingsDialog->hXFilter  = GetDlgItem( hWindow, IDD_MAG_XFILTER  );      // X axis filter combo box
  TMagSettingsDialog->hXSlew    = GetDlgItem( hWindow, IDD_MAG_XSLEW    );      // X axis fast slew check box
  TMagSettingsDialog->hXReset   = GetDlgItem( hWindow, IDD_MAG_XRESET   );      // X axis reset radio button
  TMagSettingsDialog->hXPanel   = GetDlgItem( hWindow, IDD_MAG_XPANEL   );      // X axis disable panel check box

  TMagSettingsDialog->hYGroup   = GetDlgItem( hWindow, IDD_MAG_YGROUP   );      // Y axis group box
  TMagSettingsDialog->hYRangeT  = GetDlgItem( hWindow, IDD_MAG_YRANGET  );      // Y axis range title string
  TMagSettingsDialog->hYRange   = GetDlgItem( hWindow, IDD_MAG_YRANGE   );      // Y axis range combo box
  TMagSettingsDialog->hYFlux    = GetDlgItem( hWindow, IDD_MAG_YFLUX    );      // Y axis flux counting check box
  TMagSettingsDialog->hYFilterT = GetDlgItem( hWindow, IDD_MAG_YFILTERT );      // Y axis filter title string
  TMagSettingsDialog->hYFilter  = GetDlgItem( hWindow, IDD_MAG_YFILTER  );      // Y axis filter combo box
  TMagSettingsDialog->hYSlew    = GetDlgItem( hWindow, IDD_MAG_YSLEW    );      // Y axis fast slew check box
  TMagSettingsDialog->hYReset   = GetDlgItem( hWindow, IDD_MAG_YRESET   );      // Y axis reset radio button
  TMagSettingsDialog->hYPanel   = GetDlgItem( hWindow, IDD_MAG_YPANEL   );      // Y axis disable panel check box

  TMagSettingsDialog->hZGroup   = GetDlgItem( hWindow, IDD_MAG_ZGROUP   );      // Z axis group box
  TMagSettingsDialog->hZRangeT  = GetDlgItem( hWindow, IDD_MAG_ZRANGET  );      // Z axis range title string
  TMagSettingsDialog->hZRange   = GetDlgItem( hWindow, IDD_MAG_ZRANGE   );      // Z axis range combo box
  TMagSettingsDialog->hZFlux    = GetDlgItem( hWindow, IDD_MAG_ZFLUX    );      // Z axis flux counting check box
  TMagSettingsDialog->hZFilterT = GetDlgItem( hWindow, IDD_MAG_ZFILTERT );      // Z axis filter title string
  TMagSettingsDialog->hZFilter  = GetDlgItem( hWindow, IDD_MAG_ZFILTER  );      // Z axis filter combo box
  TMagSettingsDialog->hZSlew    = GetDlgItem( hWindow, IDD_MAG_ZSLEW    );      // Z axis fast slew check box
  TMagSettingsDialog->hZReset   = GetDlgItem( hWindow, IDD_MAG_ZRESET   );      // Z axis reset radio button
  TMagSettingsDialog->hZPanel   = GetDlgItem( hWindow, IDD_MAG_ZPANEL   );      // Z axis disable panel check box

  // Initialize controls

  if( ConfigureDData->dwX ) {                                   // X axis magnetometer
    ComboBox_InsertString( GetDlgItem( hWindow, IDD_MAG_XRANGE ), 0,   "1" );           // Range choices
    ComboBox_InsertString( GetDlgItem( hWindow, IDD_MAG_XRANGE ), 1,  "10" );
    ComboBox_InsertString( GetDlgItem( hWindow, IDD_MAG_XRANGE ), 2, "100" );
    ComboBox_InsertString( GetDlgItem( hWindow, IDD_MAG_XRANGE ), 3,  "ER" );
    ComboBox_SetCurSel(    GetDlgItem( hWindow, IDD_MAG_XRANGE ), MagnetometerDData->dwXRange );// Current range selection
    if( !MagnetometerDData->dwXRange ) {                                                // If range is set to "1",
      if( MagnetometerDData->dwXFlux )                                                  // If X axis flux counting is also selected,
        Button_SetCheck( GetDlgItem( hWindow, IDD_MAG_XFLUX ), 1 );                     // check box
      }
    else        // For other ranges,
      EnableWindow( TMagSettingsDialog->hXFlux, FALSE );                                // disable X axis flux counting check box,
    ComboBox_InsertString( GetDlgItem( hWindow, IDD_MAG_XFILTER ), 0,   "1 Hz" );       // Filter choices
    ComboBox_InsertString( GetDlgItem( hWindow, IDD_MAG_XFILTER ), 1,  "10 Hz" );
    ComboBox_InsertString( GetDlgItem( hWindow, IDD_MAG_XFILTER ), 2, "100 Hz" );
    ComboBox_InsertString( GetDlgItem( hWindow, IDD_MAG_XFILTER ), 3,     "WB" );
    ComboBox_SetCurSel(    GetDlgItem( hWindow, IDD_MAG_XFILTER ), MagnetometerDData->dwXFilter );      // Current filter selection

    if( MagnetometerDData->dwXSlew )                                    // If X axis fast slew is selected,
      Button_SetCheck( GetDlgItem( hWindow, IDD_MAG_XSLEW  ), 1 );      // check box
    if( MagnetometerDData->dwXPanel )                                   // If X axis disable panel is selected,
      Button_SetCheck( GetDlgItem( hWindow, IDD_MAG_XPANEL ), 1 );      // check box
    }
  else {                                                                // Disable X axis controls
    EnableWindow( TMagSettingsDialog->hXGroup,  FALSE );                // X axis group box
    EnableWindow( TMagSettingsDialog->hXRangeT, FALSE );                // X axis range title string
    EnableWindow( TMagSettingsDialog->hXRange,  FALSE );                // X axis range combo box
    EnableWindow( TMagSettingsDialog->hXFlux,   FALSE );                // X axis flux counting check box
    EnableWindow( TMagSettingsDialog->hXFilterT,FALSE );                // X axis filter title string
    EnableWindow( TMagSettingsDialog->hXFilter, FALSE );                // X axis filter combo box
    EnableWindow( TMagSettingsDialog->hXSlew,   FALSE );                // X axis fast slew check box
    EnableWindow( TMagSettingsDialog->hXReset,  FALSE );                // X axis reset radio button
    EnableWindow( TMagSettingsDialog->hXPanel,  FALSE );                // X axis disable panel check box
    }

  if( ConfigureDData->dwY ) {           // Y axis magnetometer
    ComboBox_InsertString( GetDlgItem( hWindow, IDD_MAG_YRANGE ), 0,   "1" );        // Range choices
    ComboBox_InsertString( GetDlgItem( hWindow, IDD_MAG_YRANGE ), 1,  "10" );
    ComboBox_InsertString( GetDlgItem( hWindow, IDD_MAG_YRANGE ), 2, "100" );
    ComboBox_InsertString( GetDlgItem( hWindow, IDD_MAG_YRANGE ), 3,  "ER" );
    ComboBox_SetCurSel(    GetDlgItem( hWindow, IDD_MAG_YRANGE ), MagnetometerDData->dwYRange );        // Current range selection
    if( !MagnetometerDData->dwYRange ) {                                // If range is set to "1",
      if( MagnetometerDData->dwYFlux )                                  // If Y axis flux counting is also selected,
        Button_SetCheck( GetDlgItem( hWindow, IDD_MAG_YFLUX ), 1 );     // check box
      }
    else        // For other ranges,
      EnableWindow( TMagSettingsDialog->hYFlux, FALSE );                                // disable Y axis flux counting check box,
    ComboBox_InsertString( GetDlgItem( hWindow, IDD_MAG_YFILTER ), 0,   "1 Hz" );       // Filter choices
    ComboBox_InsertString( GetDlgItem( hWindow, IDD_MAG_YFILTER ), 1,  "10 Hz" );
    ComboBox_InsertString( GetDlgItem( hWindow, IDD_MAG_YFILTER ), 2, "100 Hz" );
    ComboBox_InsertString( GetDlgItem( hWindow, IDD_MAG_YFILTER ), 3,     "WB" );
    ComboBox_SetCurSel(    GetDlgItem( hWindow, IDD_MAG_YFILTER ), MagnetometerDData->dwYFilter );       // Current filter selection
    if( MagnetometerDData->dwYSlew )                                    // If Y axis fast slew is selected,
      Button_SetCheck( GetDlgItem( hWindow, IDD_MAG_YSLEW ), 1 );       // check box
    if( MagnetometerDData->dwYPanel )                                   // If Y axis disable panel is selected,
      Button_SetCheck( GetDlgItem( hWindow, IDD_MAG_YPANEL ), 1 );      // check box
    }
  else {	                                                        // Disable Y axis controls
    EnableWindow( TMagSettingsDialog->hYGroup,   FALSE );               // Y axis group box
    EnableWindow( TMagSettingsDialog->hYRangeT,  FALSE );               // Y axis range title string
    EnableWindow( TMagSettingsDialog->hYRange,   FALSE );               // Y axis range combo box
    EnableWindow( TMagSettingsDialog->hYFlux,    FALSE );               // Y axis flux counting check box
    EnableWindow( TMagSettingsDialog->hYFilterT, FALSE );               // Y axis filter title string
    EnableWindow( TMagSettingsDialog->hYFilter,  FALSE );               // Y axis filter combo box
    EnableWindow( TMagSettingsDialog->hYSlew,    FALSE );               // Y axis fast slew check box
    EnableWindow( TMagSettingsDialog->hYReset,   FALSE );               // Y axis reset radio button
    EnableWindow( TMagSettingsDialog->hYPanel,   FALSE );               // Y axis disable panel check box
    }

  if( ConfigureDData->dwZ ) {           // Z axis magnetometer
    ComboBox_InsertString( GetDlgItem( hWindow, IDD_MAG_ZRANGE ), 0,   "1" );   // Range choices
    ComboBox_InsertString( GetDlgItem( hWindow, IDD_MAG_ZRANGE ), 1,  "10" );
    ComboBox_InsertString( GetDlgItem( hWindow, IDD_MAG_ZRANGE ), 2, "100" );
    ComboBox_InsertString( GetDlgItem( hWindow, IDD_MAG_ZRANGE ), 3,  "ER" );
    ComboBox_SetCurSel(    GetDlgItem( hWindow, IDD_MAG_ZRANGE ), MagnetometerDData->dwZRange );        // Current range selection
    if( !MagnetometerDData->dwZRange ) {                                // If range is set to "1",
      if( MagnetometerDData->dwZFlux )                                  // If Z axis flux counting is also selected,
        Button_SetCheck( GetDlgItem( hWindow, IDD_MAG_ZFLUX ), 1 );     // check box
      }
    else        // For other ranges,
      EnableWindow( TMagSettingsDialog->hZFlux, FALSE );                // disable Z axis flux counting check box,
    ComboBox_InsertString( GetDlgItem( hWindow, IDD_MAG_ZFILTER ), 0,   "1 Hz" );       // Filter choices
    ComboBox_InsertString( GetDlgItem( hWindow, IDD_MAG_ZFILTER ), 1,  "10 Hz" );
    ComboBox_InsertString( GetDlgItem( hWindow, IDD_MAG_ZFILTER ), 2, "100 Hz" );
    ComboBox_InsertString( GetDlgItem( hWindow, IDD_MAG_ZFILTER ), 3,     "WB" );
    ComboBox_SetCurSel(    GetDlgItem( hWindow, IDD_MAG_ZFILTER ), MagnetometerDData->dwZFilter );      // Current filter selection
    if( MagnetometerDData->dwZSlew  )                                   // If Z axis fast slew is selected,
      Button_SetCheck( GetDlgItem( hWindow, IDD_MAG_ZSLEW ), 1 );       // check box
    if (MagnetometerDData->dwZPanel )                                   // If Z axis disable panel is selected,
      Button_SetCheck( GetDlgItem( hWindow, IDD_MAG_ZPANEL ), 1 );      // check box
    }
  else {	                                                        // Disable Z axis controls
    EnableWindow( TMagSettingsDialog->hZGroup,   FALSE );               // Z axis group box
    EnableWindow( TMagSettingsDialog->hZRangeT,  FALSE );               // Z axis range title string
    EnableWindow( TMagSettingsDialog->hZRange,   FALSE );               // Z axis range combo box
    EnableWindow( TMagSettingsDialog->hZFlux,    FALSE );               // Z axis flux counting check box
    EnableWindow( TMagSettingsDialog->hZFilterT, FALSE );               // Z axis filter title string
    EnableWindow( TMagSettingsDialog->hZFilter,  FALSE );               // Z axis filter combo box
    EnableWindow( TMagSettingsDialog->hZSlew,    FALSE );               // Z axis fast slew check box
    EnableWindow( TMagSettingsDialog->hZReset,   FALSE );               // Z axis reset radio button
    EnableWindow( TMagSettingsDialog->hZPanel,   FALSE );               // Z axis disable panel check box
    }

  // Set text in edit field

  SetDlgItemText( hWindow, IDD_MAG_DELAY, MagnetometerDData->achDelay );// Magnetometer settling delay edit field
  }


// Respond to the X axis range combo box

void TMagSettingsDialogIDXRange( HWND hWindow, TMAGSETTINGSDIALOG *TMagSettingsDialog, SETTINGS *MagnetometerDData ) {

  DWORD dwXRange;

  dwXRange = Button_GetCheck( GetDlgItem( hWindow, IDD_MAG_XRANGE ) );  // Current range selection
  if( !dwXRange ) {                                                     // If range is set to "1",
    EnableWindow( TMagSettingsDialog->hXFlux, TRUE );                   // enable X axis flux counting check box
    if( MagnetometerDData->dwXFlux )                                    // If X axis flux counting is also selected,
      Button_SetCheck( GetDlgItem( hWindow, IDD_MAG_XFLUX ), 1 );       // check box
    }
  else {                                                                // If range is set to something other than "1",
    Button_SetCheck( GetDlgItem( hWindow, IDD_MAG_XFLUX ), 0 );         // uncheck the box,
    EnableWindow( TMagSettingsDialog->hXFlux, FALSE );                  // and disable the X axis flux counting check box
    }
  }


// Respond to the Y axis range combo box

void TMagSettingsDialogIDYRange( HWND hWindow, TMAGSETTINGSDIALOG *TMagSettingsDialog, SETTINGS *MagnetometerDData  ) {

  DWORD dwYRange;

  dwYRange = Button_GetCheck( GetDlgItem( hWindow, IDD_MAG_YRANGE ) );  // Current range selection
  if( !dwYRange ) {                                                     // If range is set to "1",
    EnableWindow( TMagSettingsDialog->hYFlux, TRUE );                   // enable Y axis flux counting check box
    if( MagnetometerDData->dwYFlux )                                    // If Y axis flux counting is also selected,
      Button_SetCheck( GetDlgItem( hWindow, IDD_MAG_YFLUX ), 1 );       // check box
    }
  else {                                                                // If range is set to something other than "1",
    Button_SetCheck( GetDlgItem( hWindow, IDD_MAG_YFLUX ), 0 );         // uncheck the box,
    EnableWindow( TMagSettingsDialog->hYFlux, FALSE );                  // and disable the Y axis flux counting check box
    }
  }


// Respond to the Z axis range combo box

void TMagSettingsDialogIDZRange( HWND hWindow, TMAGSETTINGSDIALOG *TMagSettingsDialog, SETTINGS *MagnetometerDData  ) {

  DWORD dwZRange;

  dwZRange = Button_GetCheck( GetDlgItem( hWindow, IDD_MAG_ZRANGE ) );  // Current range selection
  if( !dwZRange ) {                                                     // If range is set to "1",
    EnableWindow( TMagSettingsDialog->hZFlux, TRUE );                   // enable Z axis flux counting check box
    if( MagnetometerDData->dwZFlux )                                    // If Z axis flux counting is also selected,
      Button_SetCheck( GetDlgItem( hWindow, IDD_MAG_ZFLUX ), 1 );       // check box
    }
  else {                                                                // If range is set to something other than "1",
    Button_SetCheck( GetDlgItem( hWindow, IDD_MAG_ZFLUX ), 0 );         // uncheck the box,
    EnableWindow( TMagSettingsDialog->hZFlux, FALSE );                  // and disable the Z axis flux counting check box
    }
  }


// Respond to the X axis reset radio button

void TMagSettingsDialogIDXReset( ACQUIRE *Acquire, HWND hWindow ) {

  Button_SetCheck( GetDlgItem( hWindow, IDD_MAG_XRESET ), 1 );          // Fill radio button
  Pause( 495UL );                                                       // Delay
  MagPulseLoop( Acquire->ac_hwndParent, Acquire->ac_nMagComID, "X" );   // Pulse-reset feedback loop
  MagResetCount(Acquire->ac_hwndParent, Acquire->ac_nMagComID, "X" );   // Clear flux counter
  Button_SetCheck( GetDlgItem( hWindow, IDD_MAG_XRESET ), 0 );          // Clear radio button
  }


// Respond to the Y axis reset radio button

void TMagSettingsDialogIDYReset( ACQUIRE *Acquire, HWND hWindow ) {

  Button_SetCheck( GetDlgItem( hWindow, IDD_MAG_YRESET ), 1 );          // Fill radio button
  Pause( 495UL );                                                       // Delay
  MagPulseLoop( Acquire->ac_hwndParent, Acquire->ac_nMagComID, "Y" );   // Pulse-reset feedback loop
  MagResetCount(Acquire->ac_hwndParent, Acquire->ac_nMagComID, "Y" );   // Clear flux counter
  Button_SetCheck( GetDlgItem( hWindow, IDD_MAG_YRESET ), 0 );          // Clear radio button
  }


// Respond to the Z axis reset radio button

void TMagSettingsDialogIDZReset( ACQUIRE *Acquire, HWND hWindow ) {

  Button_SetCheck( GetDlgItem( hWindow, IDD_MAG_ZRESET ), 1 );          // Fill radio button
  Pause( 495UL );                                                       // Delay
  MagPulseLoop( Acquire->ac_hwndParent, Acquire->ac_nMagComID, "Z" );   // Pulse-reset feedback loop
  MagResetCount(Acquire->ac_hwndParent, Acquire->ac_nMagComID, "Z" );   // Clear flux counter
  Button_SetCheck( GetDlgItem( hWindow, IDD_MAG_ZRESET ), 0 );          // Clear radio button
  }


// Respond to the Ok button

void TMagSettingsDialogIDOk( HWND hWindow, SETTINGS *MagnetometerDData ) {

  // X axis magnetometer

  MagnetometerDData->dwXRange  = ComboBox_GetCurSel( GetDlgItem( hWindow, IDD_MAG_XRANGE  ) );  // Range
  MagnetometerDData->dwXFilter = ComboBox_GetCurSel( GetDlgItem( hWindow, IDD_MAG_XFILTER ) );  // Filter
  MagnetometerDData->dwXFlux   = Button_GetCheck(    GetDlgItem( hWindow, IDD_MAG_XFLUX   ) );  // Flux counting
  MagnetometerDData->dwXSlew   = Button_GetCheck(    GetDlgItem( hWindow, IDD_MAG_XSLEW   ) );  // Fast slew
  MagnetometerDData->dwXPanel  = Button_GetCheck(    GetDlgItem( hWindow, IDD_MAG_XPANEL  ) );  // Disable panel

  // Y axis magnetometer

  MagnetometerDData->dwYRange  = ComboBox_GetCurSel( GetDlgItem( hWindow, IDD_MAG_YRANGE  ) );  // Range
  MagnetometerDData->dwYFilter = ComboBox_GetCurSel( GetDlgItem( hWindow, IDD_MAG_YFILTER ) );  // Filter
  MagnetometerDData->dwYFlux   = Button_GetCheck(    GetDlgItem( hWindow, IDD_MAG_YFLUX   ) );  // Flux counting
  MagnetometerDData->dwYSlew   = Button_GetCheck(    GetDlgItem( hWindow, IDD_MAG_YSLEW   ) );  // Fast slew
  MagnetometerDData->dwYPanel  = Button_GetCheck(    GetDlgItem( hWindow, IDD_MAG_YPANEL  ) );  // Disable panel

  // Z axis magnetometer

  MagnetometerDData->dwZRange  = ComboBox_GetCurSel( GetDlgItem( hWindow, IDD_MAG_ZRANGE  ) );  // Range
  MagnetometerDData->dwZFilter = ComboBox_GetCurSel( GetDlgItem( hWindow, IDD_MAG_ZFILTER ) );  // Filter
  MagnetometerDData->dwZFlux   = Button_GetCheck(    GetDlgItem( hWindow, IDD_MAG_ZFLUX   ) );  // Flux counting
  MagnetometerDData->dwZSlew   = Button_GetCheck(    GetDlgItem( hWindow, IDD_MAG_ZSLEW   ) );  // Fast slew
  MagnetometerDData->dwZPanel  = Button_GetCheck(    GetDlgItem( hWindow, IDD_MAG_ZPANEL  ) );  // Disable panel

  // Retrieve text in edit control

  GetDlgItemText( hWindow, IDD_MAG_DELAY, MagnetometerDData->achDelay, 15 );     // Magnetometer settling time delay in ms
//  TDialogOk(Msg);
  }


// TRUE if dialog box can close

BOOL TMagSettingsDialogCanClose( HWND hWindow ) {

  char achDelay[ 16 ];
  LONG lDelay;

  // Retrieve entry text from edit field

  GetDlgItemText( hWindow, IDD_MAG_DELAY, achDelay, 15 );// Magnetomter settling time in ms

  // Convert string to numeric value

  lDelay = atol( achDelay );

  // Entry text must be in a valid format and value must be within bounds
                                                                 
  if( !DigitsAndSign( achDelay ) || lDelay <= 0L || lDelay > 99999999L ) { // If settling delay entry is not just digits with one, optional hyphen,
    MessageBeep( 0 );                                   // beep the speaker in warning,
    SetDlgItemText( hWindow, IDD_MAG_DELAY, '\0' );     // clear the entry field, and
    return FALSE;
    }
  return TRUE;                                          // Entry values must all be valid
  }


BOOL CALLBACK TMagSettingsDialogProcess( HWND hWindow, UINT uMsg, WPARAM wParam, LPARAM lParam ) {

  ACQUIRE *Acquire;

  switch( uMsg ) {

    /* This Get Called When Windows Shuts Down, Asking Ok To Close Application. */

    case WM_QUERYENDSESSION:
      return TRUE;

    /* This Get Called When Windows Shuts Diwn. Telling To Close Application. */

    case WM_ENDSESSION:
      if( wParam && ( ( Acquire = (ACQUIRE *) GetWindowLong( hWindow, GWL_USERDATA ) ) != (ACQUIRE *) NULL ) ) {
        EndDialog( hWindow, 0 );
        }
      return TRUE;

    case WM_INITDIALOG:
      if( ( Acquire = (ACQUIRE *) lParam ) != (ACQUIRE *) NULL ) {
        SetWindowLong( hWindow, GWL_USERDATA, lParam );
        TMagSettingsDialogSetupWindow( hWindow, &Acquire->ac_TMagSettingsDialog, &Acquire->ac_ConfigureDData, &Acquire->ac_MagnetometerDData );
        return TRUE;
        }
      break;

    case WM_CLOSE:
      if( ( Acquire = (ACQUIRE *) GetWindowLong( hWindow, GWL_USERDATA ) ) != (ACQUIRE *) NULL ) {
        EndDialog( hWindow, IDCANCEL );
        }
      return TRUE;

    case WM_COMMAND:
      if( ( Acquire = (ACQUIRE *) GetWindowLong( hWindow, GWL_USERDATA ) ) != (ACQUIRE *) NULL ) {
        switch( LOWORD( wParam ) ) {

          // Responds to the X axis Range combo box

          case IDD_MAG_XRANGE:
            TMagSettingsDialogIDXRange( hWindow, &Acquire->ac_TMagSettingsDialog, &Acquire->ac_MagnetometerDData );
            return TRUE;

          // Responds to the Y axis Range combo box

          case IDD_MAG_YRANGE:
            TMagSettingsDialogIDYRange( hWindow, &Acquire->ac_TMagSettingsDialog, &Acquire->ac_MagnetometerDData );
            return TRUE;

          // Responds to the Z axis Range combo box

          case IDD_MAG_ZRANGE:
            TMagSettingsDialogIDZRange( hWindow, &Acquire->ac_TMagSettingsDialog, &Acquire->ac_MagnetometerDData );
            return TRUE;

          // Responds to the X axis Reset radio button

          case IDD_MAG_XRESET:
            TMagSettingsDialogIDXReset( Acquire, hWindow );
            return TRUE;

          // Responds to the Y axis Reset radio button

          case IDD_MAG_YRESET:
            TMagSettingsDialogIDYReset( Acquire, hWindow );
            return TRUE;

          // Responds to the Z axis Reset radio button

          case IDD_MAG_ZRESET:
            TMagSettingsDialogIDZReset( Acquire, hWindow );
            return TRUE;

          // Responds to the Ok button

          case IDD_MAG_OK:
            if( TMagSettingsDialogCanClose( hWindow ) ) {
              TMagSettingsDialogIDOk( hWindow, &Acquire->ac_MagnetometerDData );
              EndDialog( hWindow, IDOK );
              }
            return TRUE;

          case IDCANCEL:
            EndDialog( hWindow, IDCANCEL );
            return TRUE;
          }
        }
      break;
    }
  return FALSE;
  }


/************************************************************************
*                                                                       *
*       TOptionsDialog:                                                 *
*                                                                       *
************************************************************************/

// Initialize controls and set current values

void TOptionsDialogSetupWindow( HWND hWindow, TOPTIONSDIALOG *TOptionsDialog, CONFIG *ConfigureDData, SH *HandlerDData, OPTIONS *MeasureDData ) {

  // Retrieve window handles for dialog box controls to be enabled/disabled

  TOptionsDialog->hXYZT      = GetDlgItem( hWindow, IDD_OPTIONS_XYZ        );   // All axes analog readings title string
  TOptionsDialog->hXYZ       = GetDlgItem( hWindow, IDD_OPTIONS_XYZAVERAGE );   // All axes analog readings edit field
/*
  TOptionsDialog->hXT        = GetDlgItem( hWindow, IDD_OPTIONS_X          );   // X axis readings title string
  TOptionsDialog->hX         = GetDlgItem( hWindow, IDD_OPTIONS_XAVERAGE   );   // X axis readings edit field
  TOptionsDialog->hYT        = GetDlgItem( hWindow, IDD_OPTIONS_Y          );   // Y axis readings title string
  TOptionsDialog->hY         = GetDlgItem( hWindow, IDD_OPTIONS_YAVERAGE   );   // Y axis readings edit field
  TOptionsDialog->hZT        = GetDlgItem( hWindow, IDD_OPTIONS_Z          );   // Z axis readings title string
  TOptionsDialog->hZ         = GetDlgItem( hWindow, IDD_OPTIONS_ZAVERAGE   );   // Z axis readings edit field
*/
  TOptionsDialog->hSingle    = GetDlgItem( hWindow, IDD_OPTIONS_SINGLE     );   // Single rotation radio button
  TOptionsDialog->hMult      = GetDlgItem( hWindow, IDD_OPTIONS_MULTIPLE   );   // Multiple rotations radio button
  TOptionsDialog->hRotations = GetDlgItem( hWindow, IDD_OPTIONS_ROTATIONS  );   // Multiple rotations edit field

  // Initialize controls

  if( ConfigureDData->dwSHMan || ( ConfigureDData->dwSHAuto && HandlerDData->dwRot ) ) {// Rotational capability
    if(      MeasureDData->dwSingle )                                                   // If the single rotation option is selected,
      Button_SetCheck( GetDlgItem( hWindow, IDD_OPTIONS_SINGLE   ), 1 );                // fill radio button
    else if( MeasureDData->dwMult  ) {                                                  // If the multiple rotation option is selected,
      Button_SetCheck( GetDlgItem( hWindow, IDD_OPTIONS_MULTIPLE ), 1 );                // fill radio button,
      SetDlgItemText( hWindow, IDD_OPTIONS_ROTATIONS, MeasureDData->achMult );          // and set the text in the Multiple rotations edit field
      }
    else                                                                                // If the minimum required option is selected,
      Button_SetCheck( GetDlgItem( hWindow, IDD_OPTIONS_MINIMUM  ), 1 );                // fill radio button
    }
  else {                                                                                // No rotational capability
    EnableWindow( TOptionsDialog->hSingle,    FALSE );                                  // Disable the Single rotation radio button
    EnableWindow( TOptionsDialog->hMult,      FALSE );                                  // Disable the Multiple rotations radio button
    EnableWindow( TOptionsDialog->hRotations, FALSE );                                  // Disable the Multiple rotations edit field
    Button_SetCheck( GetDlgItem( hWindow, IDD_OPTIONS_MINIMUM ), 1 );                   // Minimum required readings per sample
    }
  if( !ConfigureDData->dwX && !ConfigureDData->dwY && !ConfigureDData->dwZ ) {          // No magnetometer
    EnableWindow( TOptionsDialog->hXYZT, FALSE );                                       // Disable the All axes analog readings title string
    EnableWindow( TOptionsDialog->hXYZ,  FALSE );                                       // Disable the All axes analog readings edit field
    }
  else                                                                                  // At least one magnetometer axis
    SetDlgItemText( hWindow, IDD_OPTIONS_XYZAVERAGE, MeasureDData->achXYZ );            // Set text in the All axes analog readings edit field
/*
  if( !ConfigureDData->dwX ) {                                                          // No X axis
    EnableWindow( TOptionsDialog->hXT, FALSE );                                         // Disable the X axis readings title string
    EnableWindow( TOptionsDialog->hX,  FALSE );                                         // Disable the X axis readings edit field
    }
  else                                                                                  // X axis
    SetDlgItemText( hWindow, IDD_OPTIONS_XAVERAGE, MeasureDData->achX );                // Set text in the X axis readings edit field
  if( !ConfigureDData->dwY ) {                                                          // No Y axis
    EnableWindow( TOptionsDialog->hYT, FALSE );                                         // Disable the Y axis readings title string
    EnableWindow( TOptionsDialog->hY,  FALSE );                                         // Disable the Y axis readings edit field
    }
  else                                                                                  // Y axis
    SetDlgItemText( hWindow, IDD_OPTIONS_YAVERAGE, MeasureDData->achY );                // Set text in the Y axis readings edit field
  if( !ConfigureDData->dwZ ) {                                                          // No Z axis
    EnableWindow( TOptionsDialog->hZT, FALSE );                                         // Disable the Z axis readings title string
    EnableWindow( TOptionsDialog->hZ,  FALSE );                                         // Disable the Z axis readings edit field
    }
  else                                                                                  // Z axis
    SetDlgItemText( hWindow, IDD_OPTIONS_ZAVERAGE, MeasureDData->achZ );                // Set text in the Z axis readings edit field
*/
  if( MeasureDData->dwZ )                                                               // If the measure -Z option is selected,
    Button_SetCheck( GetDlgItem( hWindow, IDD_OPTIONS_MEASUREZ ), 1 );                  // check box

  SetDlgItemText( hWindow, IDD_OPTIONS_SNTHRESHOLD,    MeasureDData->achSN   );         // Set the text in the S/N threshold edit field
  SetDlgItemText( hWindow, IDD_OPTIONS_DRIFTTHRESHOLD, MeasureDData->achDrift);         // Set the text in the Drift threshold edit field

  if( MeasureDData->dwSNR )                                                             // If the S/N threshold remeasure option is selected,
    Button_SetCheck( GetDlgItem( hWindow, IDD_OPTIONS_SNREMEASURE ), 1 );               // fill radio button
  else
    Button_SetCheck( GetDlgItem( hWindow, IDD_OPTIONS_SNNOTIFY    ), 1 );               // Else, fill the notify radio button

  if( MeasureDData->dwDR )                                                              // If the Drift threshold remeasure option is selected,
    Button_SetCheck( GetDlgItem( hWindow, IDD_OPTIONS_DRIFTREMEASURE ), 1 );            // fill radio button
  else
    Button_SetCheck( GetDlgItem( hWindow, IDD_OPTIONS_DRIFTNOTIFY    ), 1 );            // Else, fill the notify radio button
  }


// Respond to the Ok button

void TOptionsDialogIDOk( HWND hWindow, OPTIONS *MeasureDData ) {

  // Determine state of radio buttons and check box

  MeasureDData->dwMin    = Button_GetCheck( GetDlgItem( hWindow, IDD_OPTIONS_MINIMUM        ) ); // Minimum required readings per sample
  MeasureDData->dwSingle = Button_GetCheck( GetDlgItem( hWindow, IDD_OPTIONS_SINGLE         ) ); // Single rotation
  MeasureDData->dwMult   = Button_GetCheck( GetDlgItem( hWindow, IDD_OPTIONS_MULTIPLE       ) ); // Multiple rotations
  MeasureDData->dwZ      = Button_GetCheck( GetDlgItem( hWindow, IDD_OPTIONS_MEASUREZ       ) ); // Measure -Z
  MeasureDData->dwSNR    = Button_GetCheck( GetDlgItem( hWindow, IDD_OPTIONS_SNREMEASURE    ) ); // S/N threshold remeasure
  MeasureDData->dwSNN    = Button_GetCheck( GetDlgItem( hWindow, IDD_OPTIONS_SNNOTIFY       ) ); // S/N threshold notify
  MeasureDData->dwDR     = Button_GetCheck( GetDlgItem( hWindow, IDD_OPTIONS_DRIFTREMEASURE ) ); // Drift threshold remeasure
  MeasureDData->dwDN     = Button_GetCheck( GetDlgItem( hWindow, IDD_OPTIONS_DRIFTNOTIFY    ) ); // Drift threshold notify

  // Retrieve text in edit controls

  GetDlgItemText( hWindow, IDD_OPTIONS_ROTATIONS,      MeasureDData->achMult,  2 ); // Multiple rotations
  GetDlgItemText( hWindow, IDD_OPTIONS_SNTHRESHOLD,    MeasureDData->achSN,    5 ); // S/N threshold
//GetDlgItemText( hWindow, IDD_OPTIONS_DRIFTTHRESHOLD, MeasureDData->achDrift, 8 ); // Drift threshold
  GetDlgItemText( hWindow, IDD_OPTIONS_DRIFTTHRESHOLD, MeasureDData->achDrift, 6 ); // S/D threshold
  GetDlgItemText( hWindow, IDD_OPTIONS_XYZAVERAGE,     MeasureDData->achXYZ,   2 ); // Analog readings
//GetDlgItemText( hWindow, IDD_OPTIONS_XAVERAGE,       MeasureDData->achX,     2 ); // Readings
//GetDlgItemText( hWindow, IDD_OPTIONS_YAVERAGE,       MeasureDData->achY,     2 ); // Readings
//GetDlgItemText( hWindow, IDD_OPTIONS_ZAVERAGE,       MeasureDData->achZ,     2 ); // Readings
//TDialogOk(Msg);
}


// TRUE if dialog box can close

BOOL TOptionsDialogCanClose( HWND hWindow, CONFIG *ConfigureDData ) {

//char   achX[2], achY[2], achZ[2], achMult[2], achSN[5], achDrift[8];
//char   achX[2], achY[2], achZ[2], achMult[2], achSN[5], achDrift[6];
  char   achXYZ[2], achMult[2], achSN[5], achDrift[6];
//int    nX, nY, nZ, nMult, nSN;
  int    nXYZ, nMult, nSN;
  double dDrift;
  DWORD  dwMult;

  // Determine state of Mulitple rotations radio button

  dwMult = Button_GetCheck( GetDlgItem( hWindow, IDD_OPTIONS_MULTIPLE ) );      // Multiple rotations

  // Retrieve entry text from edit fields

  GetDlgItemText( hWindow, IDD_OPTIONS_XYZAVERAGE,     achXYZ,   2 );   // Analog readings
//GetDlgItemText( hWindow, IDD_OPTIONS_XAVERAGE,       achX,     2 );   // Readings per X axis
//GetDlgItemText( hWindow, IDD_OPTIONS_YAVERAGE,       achY,     2 );   // Readings per Y axis
//GetDlgItemText( hWindow, IDD_OPTIONS_ZAVERAGE,       achZ,     2 );   // Readings per Z axis
  GetDlgItemText( hWindow, IDD_OPTIONS_ROTATIONS,      achMult,  2 );   // Multiple rotations
  GetDlgItemText( hWindow, IDD_OPTIONS_SNTHRESHOLD,    achSN,    5 );   // S/N threshold
//GetDlgItemText( hWindow, IDD_OPTIONS_DRIFTTHRESHOLD, achDrift, 8 );   // Drift threshold
  GetDlgItemText( hWindow, IDD_OPTIONS_DRIFTTHRESHOLD, achDrift, 6 );   // S/D threshold

  // Convert strings to numeric values

  nXYZ   = atoi(  achXYZ );             // Analog readings
//nX     = atoi(  achX   );             // Readings per X axis
//nY     = atoi(  achY   );             // Readings per Y axis
//nZ     = atoi(  achZ   );             // Readings per Z axis
  nMult  = atoi(  achMult);             // Multiple rotations
  nSN    = atoi(  achSN  );             // S/N threshold
  dDrift = strtod(achDrift, NULL );     // Drift threshold

  // Entry text in edit fields must be in a valid format and must be in bounds

  if( ConfigureDData->dwX || ConfigureDData->dwY || ConfigureDData->dwZ ) {     // If the magnetometer is configured with at least one axis
    if( !DigitsAndSign( achXYZ ) || nXYZ < 1 || nXYZ > 9 ) {                    // If entry is not just digits with one, optional hyphen,
      MessageBeep( 0 );                                                         // beep the speaker in warning,
      SetDlgItemText( hWindow, IDD_OPTIONS_XYZAVERAGE, '\0' );                  // clear the entry field, and
      return FALSE;
      }
    }
/*
  if( ConfigureDData->dwX ) {                                   // If the magnetometer is configured with an X axis
    if( !DigitsAndSign( achX ) || nX < 1 || nX > 9 ) {          // If entry is not just digits with one, optional hyphen,
      MessageBeep( 0 );                                         // beep the speaker in warning,
      SetDlgItemText( hWindow, IDD_OPTIONS_XAVERAGE, '\0' );    // clear the entry field, and
      return FALSE;
      }
    }
  if( ConfigureDData->dwY ) {                                   // If the magnetometer is configured with a Y axis
    if( !DigitsAndSign( achY ) || nY < 1 || nY > 9 ) {          // If entry is not just digits with one, optional hyphen,
      MessageBeep( 0 );                                         // beep the speaker in warning,
      SetDlgItemText( hWindow, IDD_OPTIONS_YAVERAGE, '\0' );    // clear the entry field, and
      return FALSE;
      }
    }
  if( ConfigureDData->dwZ ) {                                   // If the magnetometer is configured with a Z axis
    if( !DigitsAndSign( achZ ) || nZ < 1 || nZ > 9 ) {          // If entry is not just digits with one, optional hyphen,
      MessageBeep( 0 );                                         // beep the speaker in warning,
      SetDlgItemText( hWindow, IDD_OPTIONS_ZAVERAGE, '\0' );    // clear the entry field, and
      return FALSE;
      }
    }
*/
  if( dwMult ) {                // For multiple rotations
    if( !DigitsAndSign( achMult ) || nMult < 2 || nMult > 9 ) { // If entry is not just digits with one, optional hyphen,
      MessageBeep( 0 );         // beep the speaker in warning,
      SetDlgItemText( hWindow, IDD_OPTIONS_ROTATIONS, '\0' );   // clear the entry field, and
      return FALSE;
      }
    }
//if( !DigitsAndSign( achSN ) || nSN < 1 || nSN > 9999 ) {      // If S/N threshold entry is not just digits with one, optional hyphen,
  if( !DigitsAndSign( achSN ) || nSN < 1 ) {                    // If S/N threshold entry is not just digits with one, optional hyphen,
    MessageBeep( 0 );           // beep the speaker in warning,
    SetDlgItemText( hWindow, IDD_OPTIONS_SNTHRESHOLD, '\0' );   // clear the entry field, and
    return FALSE;
    }
//if( !ScientificAndSign( achDrift ) || dDrift < 9.9e-09 || dDrift > 9.9e-03 ) {// If Drift threshold entry is not in the proper scientific notation,
  if( !DigitsAndSign( achDrift ) || dDrift < 1.0 ) {                            // If S/D threshold entry is not just digits with one optional hyphen,
    MessageBeep( 0 );                                                   // beep the speaker in warning,
    SetDlgItemText( hWindow, IDD_OPTIONS_DRIFTTHRESHOLD, '\0' );        // clear the entry field, and
    return FALSE;
    }
  return TRUE;  // Entry values must all be valid
  }


BOOL CALLBACK TOptionsDialogProcess( HWND hWindow, UINT uMsg, WPARAM wParam, LPARAM lParam ) {

  ACQUIRE *Acquire;

  switch( uMsg ) {

    /* This Get Called When Windows Shuts Down, Asking Ok To Close Application. */

    case WM_QUERYENDSESSION:
      return TRUE;

    /* This Get Called When Windows Shuts Diwn. Telling To Close Application. */

    case WM_ENDSESSION:
      if( wParam && ( ( Acquire = (ACQUIRE *) GetWindowLong( hWindow, GWL_USERDATA ) ) != (ACQUIRE *) NULL ) ) {
        EndDialog( hWindow, 0 );
        }
      return TRUE;

    case WM_INITDIALOG:
      if( ( Acquire = (ACQUIRE *) lParam ) != (ACQUIRE *) NULL ) {
        SetWindowLong( hWindow, GWL_USERDATA, lParam );
        TOptionsDialogSetupWindow( hWindow, &Acquire->ac_TOptionsDialog, &Acquire->ac_ConfigureDData, &Acquire->ac_HandlerDData, &Acquire->ac_MeasureDData );
        return TRUE;
        }
      break;

    case WM_CLOSE:
      if( ( Acquire = (ACQUIRE *) GetWindowLong( hWindow, GWL_USERDATA ) ) != (ACQUIRE *) NULL ) {
        EndDialog( hWindow, IDCANCEL );
        }
      return TRUE;

    case WM_COMMAND:
      if( ( Acquire = (ACQUIRE *) GetWindowLong( hWindow, GWL_USERDATA ) ) != (ACQUIRE *) NULL ) {
        switch( LOWORD( wParam ) ) {

          // Responds to the Ok button

          case IDD_OPTIONS_OK:
            if( TOptionsDialogCanClose( hWindow, &Acquire->ac_ConfigureDData ) ) {
              TOptionsDialogIDOk( hWindow, &Acquire->ac_MeasureDData );
              EndDialog( hWindow, IDOK );
              }
            return TRUE;

          case IDCANCEL:
            EndDialog( hWindow, IDCANCEL );
            return TRUE;
          }
        }
      break;
    }
  return FALSE;
  }


/************************************************************************
*                                                                       *
*       TSampleDialog:                                                  *
*                                                                       *
************************************************************************/

// Initialize controls

void TSampleDialogSetupWindow( HWND hWindow, TSAMPLEDIALOG *TSampleDialog, INFO *SampleDData ) {

  // Retrieve window handles for dialog box controls

  TSampleDialog->hName   = GetDlgItem( hWindow, IDD_SAMPLE_NAME   );    // Sample name
  TSampleDialog->hSize   = GetDlgItem( hWindow, IDD_SAMPLE_SIZE   );    // Sample size
  TSampleDialog->hCoreAz = GetDlgItem( hWindow, IDD_SAMPLE_COREAZ );    // Core down dip azimuth
  TSampleDialog->hCorePl = GetDlgItem( hWindow, IDD_SAMPLE_COREPL );    // Core plunge
  TSampleDialog->hBedAz  = GetDlgItem( hWindow, IDD_SAMPLE_BEDAZ  );    // Bedding dip vector azimuth
  TSampleDialog->hBedPl  = GetDlgItem( hWindow, IDD_SAMPLE_BEDPL  );    // Bedding dip vector plunge
  TSampleDialog->hFoldAz = GetDlgItem( hWindow, IDD_SAMPLE_FOLDAZ );    // Plunging fold hinge line azimuth
  TSampleDialog->hFoldPl = GetDlgItem( hWindow, IDD_SAMPLE_FOLDPL );    // Plunging fold hinge line plunge
  TSampleDialog->hDecl   = GetDlgItem( hWindow, IDD_SAMPLE_DECL   );    // Magnetic declination

  // Set text in edit fields

  SetDlgItemText( hWindow, IDD_SAMPLE_NAME,    SampleDData->achName      );     // Set text in sample name edit control
  SetDlgItemText( hWindow, IDD_SAMPLE_SIZE,    SampleDData->achSize      );     // Set text in sample size edit control
  SetDlgItemText( hWindow, IDD_SAMPLE_TIME,    SampleDData->achTime      );     // Set text in time edit control
  SetDlgItemText( hWindow, IDD_SAMPLE_COMMENT, SampleDData->achComment   );     // Set text in sample comment edit control
  SetDlgItemText( hWindow, IDD_SAMPLE_COREAZ,  SampleDData->achCoreAz    );     // Set text in core azimuth edit control
  SetDlgItemText( hWindow, IDD_SAMPLE_COREPL,  SampleDData->achCoreP     );     // Set text in core plunge edit control
  SetDlgItemText( hWindow, IDD_SAMPLE_BEDAZ,   SampleDData->achBeddingAz );     // Set text in structure azimuth edit control
  SetDlgItemText( hWindow, IDD_SAMPLE_BEDPL,   SampleDData->achBeddingP  );     // Set text in structure plunge edit control
  SetDlgItemText( hWindow, IDD_SAMPLE_FOLDAZ,  SampleDData->achFoldAz    );     // Set text in fold azimuth edit control
  SetDlgItemText( hWindow, IDD_SAMPLE_FOLDPL,  SampleDData->achFoldP     );     // Set text in fold plunge edit control
  SetDlgItemText( hWindow, IDD_SAMPLE_DECL,    SampleDData->achMagDecl   );     // Set text in magnetic declination edit control

  // Set current states for the check box

  if( SampleDData->dwOver )                                             // Overturned bedding
    Button_SetCheck( GetDlgItem( hWindow, IDD_SAMPLE_BEDOVER ), 1 );    // Check box

  // Set current states for the radio buttons

  if( SampleDData->dwCC )                                               // Sample size in cubic centimeters
    Button_SetCheck( GetDlgItem( hWindow, IDD_SAMPLE_CC ), 1 );         // Check radio button
  else  // Sample size in grams
    Button_SetCheck( GetDlgItem( hWindow, IDD_SAMPLE_GM ), 1 );         // Check radio button
  }


// Respond to the Ok button

void TSampleDialogIDOk( HWND hWindow, INFO *SampleDData ) {

  // Retrieve text in edit controls

  GetDlgItemText( hWindow, IDD_SAMPLE_NAME,    SampleDData->achName,    128 );   // Get text in sample name edit control
  GetDlgItemText( hWindow, IDD_SAMPLE_SIZE,    SampleDData->achSize,     15 );   // Get text in sample size edit control
  GetDlgItemText( hWindow, IDD_SAMPLE_COMMENT, SampleDData->achComment,  56 );   // Get text in sample comment edit control
  GetDlgItemText( hWindow, IDD_SAMPLE_COREAZ,  SampleDData->achCoreAz,    5 );   // Get text in core azimuth edit control
  GetDlgItemText( hWindow, IDD_SAMPLE_COREPL,  SampleDData->achCoreP,     4 );   // Get text in core plunge edit control
  GetDlgItemText( hWindow, IDD_SAMPLE_BEDAZ,   SampleDData->achBeddingAz, 5 );   // Get text in structure azimuth edit control
  GetDlgItemText( hWindow, IDD_SAMPLE_BEDPL,   SampleDData->achBeddingP,  4 );   // Get text in structure plunge edit control
  GetDlgItemText( hWindow, IDD_SAMPLE_FOLDAZ,  SampleDData->achFoldAz,    5 );   // Get text in fold azimuth edit control
  GetDlgItemText( hWindow, IDD_SAMPLE_FOLDPL,  SampleDData->achFoldP,     4 );   // Get text in fold plunge edit control
  GetDlgItemText( hWindow, IDD_SAMPLE_DECL,    SampleDData->achMagDecl,   5 );   // Get text in magnetic declination edit control

  // Determine state of check box

  SampleDData->dwOver = Button_GetCheck( GetDlgItem( hWindow, IDD_SAMPLE_BEDOVER ) );

  // Determine which radio button is set

  SampleDData->dwCC   = Button_GetCheck( GetDlgItem( hWindow, IDD_SAMPLE_CC      ) );
  SampleDData->dwGM   = Button_GetCheck( GetDlgItem( hWindow, IDD_SAMPLE_GM      ) );
//TDialog::Ok(Msg);
  }


// TRUE if dialog box can close

BOOL TSampleDialogCanClose( HWND hWindow, TSAMPLEDIALOG *TSampleDialog ) {

  char   achName[ 128 ], achSize[ 25 ], achCoreAz[ 25 ], achCorePl[ 25 ], achBedAz[ 25 ], achBedPl[ 25 ], achFoldAz[ 25 ], achFoldPl[ 25 ], achDecl[ 25 ];
  int    nCoreAz, nCorePl, nBedAz, nBedPl, nFoldAz, nFoldPl;
  double dSize,   dDecl;

  // Retrieve entry text from the edit fields

  GetDlgItemText( hWindow, IDD_SAMPLE_NAME,   achName,   128 );   // Sample name
  GetDlgItemText( hWindow, IDD_SAMPLE_SIZE,   achSize,   15 );  // Sample size     //20041011 DEH increase to a big number so little numbers could be entered.
  GetDlgItemText( hWindow, IDD_SAMPLE_COREAZ, achCoreAz, 5 );   // Sample core down dip azimuth
  GetDlgItemText( hWindow, IDD_SAMPLE_COREPL, achCorePl, 4 );   // Sample core plunge
  GetDlgItemText( hWindow, IDD_SAMPLE_BEDAZ,  achBedAz,  5 );   // Bedding dip vector azimuth
  GetDlgItemText( hWindow, IDD_SAMPLE_BEDPL,  achBedPl,  4 );   // Bedding dip vector plunge
  GetDlgItemText( hWindow, IDD_SAMPLE_FOLDAZ, achFoldAz, 5 );   // Plunging fold hinge line azimuth
  GetDlgItemText( hWindow, IDD_SAMPLE_FOLDPL, achFoldPl, 4 );   // Plunging fold hinge line plunge
  GetDlgItemText( hWindow, IDD_SAMPLE_DECL,   achDecl,   5 );   // Magnetic declination

  // Convert strings to numeric values

  dSize   = strtod( achSize, NULL );    // Size
  nCoreAz = atoi( achCoreAz );          // Core azimuth
  nCorePl = atoi( achCorePl );          // Core plunge
  nBedAz  = atoi( achBedAz  );          // Bedding azimuth
  nBedPl  = atoi( achBedPl  );          // Bedding plunge
  nFoldAz = atoi( achFoldAz );          // Plunging fold azimuth
  nFoldPl = atoi( achFoldPl );          // Plunging fold plunge
  dDecl   = atof( achDecl   );          // Magnetic declination

  // Check entries for valid format, and for values within bounds

  if( !GoodSampleName( achName ) ) {                                    // If entry is not an appropriate DOS filename,
    MessageBeep( 0 );                                                   // beep the speaker in warning,
    SetFocus( TSampleDialog->hName );                                   // highlight the entry field, and
    return FALSE;
    }
  if( !DigitsPointAndSign( achSize ) || dSize <= 0.0 || dSize > 50.0 ) {// If entry is not just digits with an optional period and an optional sign,
    MessageBeep( 0 );                                                   // beep the speaker in warning,
    SetFocus( TSampleDialog->hSize );                                   // highlight the entry field, and
    return FALSE;
    }
  if( !DigitsAndSign( achCoreAz ) || nCoreAz < -180 || nCoreAz > 360 ) {// If entry is not just digits with one, optional hyphen,
    MessageBeep( 0 );                                                   // beep the speaker in warning,
    SetFocus( TSampleDialog->hCoreAz );                                 // highlight the entry field, and
    return FALSE;
    }
  if( !DigitsAndSign( achCorePl ) || nCorePl <  -90 || nCorePl >  90 ) {// If entry is not just digits with one, optional hyphen,
    MessageBeep( 0 );                                                   // beep the speaker in warning,
    SetFocus( TSampleDialog->hCorePl );                                 // highlight the entry field, and
    return FALSE;
    }
  if( !DigitsAndSign( achBedAz  ) || nBedAz  < -180 || nBedAz  > 360 ) {// If entry is not just digits with one, optional hyphen,
    MessageBeep( 0 );                                                   // beep the speaker in warning,
    SetFocus( TSampleDialog->hBedAz );                                  // highlight the entry field, and
    return FALSE;
    }
  if( !DigitsAndSign( achBedPl  ) || nBedPl  <  -90 || nBedPl  > 90  ) {// If entry is not just digits with one, optional hyphen,
    MessageBeep( 0 );                                                   // beep the speaker in warning,
    SetFocus( TSampleDialog->hBedPl );                                  // highlight the entry field, and
    return FALSE;
    }
  if( !DigitsAndSign( achFoldAz ) || nFoldAz < -180 || nFoldAz > 360 ) {// If entry is not just digits with one, optional hyphen,
    MessageBeep( 0 );                                                   // beep the speaker in warning,
    SetFocus( TSampleDialog->hFoldAz );                                 // highlight the entry field, and
    return FALSE;
    }
  if( !DigitsAndSign( achFoldPl ) || nFoldPl <  -90 || nFoldPl >  90 ) {// If entry is not just digits with one, optional hyphen,
    MessageBeep( 0 );                                                   // beep the speaker in warning,
    SetFocus( TSampleDialog->hFoldPl );                                 // highlight the entry field, and
    return FALSE;
    }
  if( !DigitsPointAndSign( achDecl ) || dDecl < -45.0 || dDecl > 45.0 ) {// If entry is not just digits with an optional period and an optional hyphen,
    MessageBeep( 0 );                                                   // beep the speaker in warning,
    SetFocus( TSampleDialog->hDecl );                                   // highlight the entry field, and
    return FALSE;
    }
  return TRUE;  // Entry values must all be valid
  }


BOOL CALLBACK TSampleDialogProcess( HWND hWindow, UINT uMsg, WPARAM wParam, LPARAM lParam ) {

  ACQUIRE *Acquire;

  switch( uMsg ) {

    /* This Get Called When Windows Shuts Down, Asking Ok To Close Application. */

    case WM_QUERYENDSESSION:
      return TRUE;

    /* This Get Called When Windows Shuts Diwn. Telling To Close Application. */

    case WM_ENDSESSION:
      if( wParam && ( ( Acquire = (ACQUIRE *) GetWindowLong( hWindow, GWL_USERDATA ) ) != (ACQUIRE *) NULL ) ) {
        EndDialog( hWindow, 0 );
        }
      return TRUE;

    case WM_INITDIALOG:
      if( ( Acquire = (ACQUIRE *) lParam ) != (ACQUIRE *) NULL ) {
        SetWindowLong( hWindow, GWL_USERDATA, lParam );
        TSampleDialogSetupWindow( hWindow, &Acquire->ac_TSampleDialog, &Acquire->ac_SampleDData );
        return TRUE;
        }
      break;

    case WM_CLOSE:
      if( ( Acquire = (ACQUIRE *) GetWindowLong( hWindow, GWL_USERDATA ) ) != (ACQUIRE *) NULL ) {
        EndDialog( hWindow, IDCANCEL );
        }
      return TRUE;

    case WM_COMMAND:
      if( ( Acquire = (ACQUIRE *) GetWindowLong( hWindow, GWL_USERDATA ) ) != (ACQUIRE *) NULL ) {
        switch( LOWORD( wParam ) ) {

          // Responds to the Ok button

          case IDC_PATH: {
            char FilePathName[255];

            memset( FilePathName, 0, sizeof( FilePathName ) );
            GetFileRequestorSave( FilePathName, "dat", "Data Files" );
            SetDlgItemText( hWindow, IDD_SAMPLE_NAME, FilePathName );

            }
            return TRUE;

          case IDD_SAMPLE_OK:
            if( TSampleDialogCanClose( hWindow, &Acquire->ac_TSampleDialog ) ) {
              TSampleDialogIDOk( hWindow, &Acquire->ac_SampleDData );
              EndDialog( hWindow, IDOK );
              }
            return TRUE;

          case IDCANCEL:
            EndDialog( hWindow, IDCANCEL );
            return TRUE;
          }
        }
      break;
    }
  return FALSE;
  }


// TSingleDialog Class Member - Initialize controls and set current values

void TSingleDialogSetupWindow( ACQUIRE *Acquire, HWND hWindow ) {

  HWND  hAF;
  HWND  hField;
  HWND  hmT;

  // Retrieve window handles for dialog box controls to be enabled / disabled

  hAF    = GetDlgItem( hWindow, IDD_SINGLE_AF    );                     // AF radio button
  hField = GetDlgItem( hWindow, IDD_SINGLE_FIELD );                     // AF edit field
  hmT    = GetDlgItem( hWindow, IDD_SINGLE_MT    );                     // mT string

  // Initialize controls

  if( Acquire->ac_ConfigureDData.dwAFNone ) {                           // No AF demagnetizer
    EnableWindow( hAF,    FALSE );                                      // Disable the AF radio button
    EnableWindow( hField, FALSE );                                      // Disable the AF edit field
    EnableWindow( hmT,    FALSE );                                      // Disable the mT string
    if( Acquire->ac_SingleDData.dwThermal ) {                           // If the Thermal option is selected,
      Button_SetCheck( GetDlgItem( hWindow, IDD_SINGLE_THERMAL ), 1 );  // fill the radio button,
      SetDlgItemText( hWindow, IDD_SINGLE_TEMP, Acquire->ac_SingleDData.achTemp ); // and set the text in the edit field
      }
    else                                                                // If the None option is selected,
      Button_SetCheck( GetDlgItem( hWindow, IDD_SINGLE_NONE ), 1 );     // fill the radio button
    }

  // AF demagnetizer

  else {
    if( Acquire->ac_SingleDData.dwAF ) {                                // If the AF option is selected,
      Button_SetCheck( GetDlgItem( hWindow, IDD_SINGLE_AF ), 1 );       // fill the radio button,
      SetDlgItemText( hWindow, IDD_SINGLE_FIELD, Acquire->ac_SingleDData.achField );// and set the text in the edit field
      }
    else if( Acquire->ac_SingleDData.dwThermal ) {                      // If the Thermal option is selected,
      Button_SetCheck( GetDlgItem( hWindow, IDD_SINGLE_THERMAL ), 1 );  // fill the radio button,
      SetDlgItemText( hWindow, IDD_SINGLE_TEMP, Acquire->ac_SingleDData.achTemp );  // and set the text in the edit field
      }
    else                                                                // If the None option is selected,
      Button_SetCheck( GetDlgItem( hWindow, IDD_SINGLE_NONE ), 1 );     // fill the radio button
    }
  }


// TSingleDialog Class Member - Respond to the Ok button

void TSingleDialogIDOk( ACQUIRE *Acquire, HWND hWindow ) {

  // Determine state of radio buttons

  Acquire->ac_SingleDData.dwAF      = Button_GetCheck( GetDlgItem( hWindow, IDD_SINGLE_AF      ) );     // AF
  Acquire->ac_SingleDData.dwThermal = Button_GetCheck( GetDlgItem( hWindow, IDD_SINGLE_THERMAL ) );     // Thermal
  Acquire->ac_SingleDData.dwNone    = Button_GetCheck( GetDlgItem( hWindow, IDD_SINGLE_NONE    ) );     // None

  // Retrieve text in edit fields

  GetDlgItemText( hWindow, IDD_SINGLE_FIELD, Acquire->ac_SingleDData.achField, 6 );                 // AF field strength
  GetDlgItemText( hWindow, IDD_SINGLE_TEMP,  Acquire->ac_SingleDData.achTemp,  5 );                 // Temperature
  }


// TSingleDialog Class Member - TRUE if dialog box can close

BOOL TSingleDialogCanClose( ACQUIRE *Acquire, HWND hWindow ) {

  char   achField[ 6 ], achTemp[ 5 ];
  int    nTemp;
  double dField;
  DWORD  dwAF, dwThermal, dwNone;

  // Determine state of radio buttons

  dwAF      = Button_GetCheck( GetDlgItem( hWindow, IDD_SINGLE_AF      ) );     // AF
  dwThermal = Button_GetCheck( GetDlgItem( hWindow, IDD_SINGLE_THERMAL ) );     // Thermal
  dwNone    = Button_GetCheck( GetDlgItem( hWindow, IDD_SINGLE_NONE    ) );     // None

  // Retrieve entry text from edit fields

  GetDlgItemText( hWindow, IDD_SINGLE_FIELD, achField, 6 );             // AF strength
  GetDlgItemText( hWindow, IDD_SINGLE_TEMP,  achTemp,  5 );             // Temperature

  // Convert strings to numeric values

  dField = strtod(achField, NULL );                                     // AF field strength
  nTemp  = atoi(  achTemp );                                            // Convert string to an integer value

  // Entries must be in proper format, and values must be within bounds

  if( dwAF ) {                                                          // AF  demagnetization
    if( !DigitsPointAndSign( achField ) || dField < 0.0 || dField > Acquire->ac_dMaxAFField ) { // If entry is not just digits with an optional point and hyphen,
      MessageBeep( 0 );                                                 // beep the speaker in warning,
      SetDlgItemText( hWindow, IDD_SINGLE_FIELD, '\0' );                // clear the entry field, and
      return FALSE;
      }
    }
  else if( dwThermal ) {                                                // Thermal  demagnetization
    if( !DigitsAndSign( achTemp ) || nTemp < 25 || nTemp > 1100 ) {     // If entry is not just digits with one, optional hyphen,
      MessageBeep( 0 );                                                 // beep the speaker in warning,
      SetDlgItemText( hWindow, IDD_SINGLE_TEMP, '\0' );                 // clear the entry field, and
      return FALSE;
      }
    }
  else if( dwNone ) {                                                   // No demagnetization
    }
  else {                                                                // Nothing has been selected
    MessageBeep( 0 );                                                   // Beep speaker in warning, and
    return FALSE;
    }
  return TRUE;                                                          // Entry values must all be valid
  }


BOOL CALLBACK TSingleDialogProcess( HWND hWindow, UINT uMsg, WPARAM wParam, LPARAM lParam ) {

  ACQUIRE *Acquire;

  switch( uMsg ) {

    /* This Get Called When Windows Shuts Down, Asking Ok To Close Application. */

    case WM_QUERYENDSESSION:
      return TRUE;

    /* This Get Called When Windows Shuts Diwn. Telling To Close Application. */

    case WM_ENDSESSION:
      if( wParam && ( ( Acquire = (ACQUIRE *) GetWindowLong( hWindow, GWL_USERDATA ) ) != (ACQUIRE *) NULL ) ) {
        EndDialog( hWindow, 0 );
        }
      return TRUE;

    case WM_INITDIALOG:
      if( ( Acquire = (ACQUIRE *) lParam ) != (ACQUIRE *) NULL ) {
        SetWindowLong( hWindow, GWL_USERDATA, lParam );
        TSingleDialogSetupWindow( Acquire, hWindow );
        return TRUE;
        }
      break;

    case WM_CLOSE:
      if( ( Acquire = (ACQUIRE *) GetWindowLong( hWindow, GWL_USERDATA ) ) != (ACQUIRE *) NULL ) {
        EndDialog( hWindow, IDCANCEL );
        }
      return TRUE;

    case WM_COMMAND:
      if( ( Acquire = (ACQUIRE *) GetWindowLong( hWindow, GWL_USERDATA ) ) != (ACQUIRE *) NULL ) {
        switch( LOWORD( wParam ) ) {

          // Responds to the Ok button

          case IDD_SINGLE_OK:
            if( TSingleDialogCanClose( Acquire, hWindow ) ) {
              TSingleDialogIDOk( Acquire, hWindow );
              EndDialog( hWindow, IDOK );
              }
            return TRUE;

          case IDCANCEL:
            EndDialog( hWindow, IDCANCEL );
            return TRUE;
          }
        }
      break;
    }
  return FALSE;
  }


// TCancelDialog Class Member - Initialize controls

void TCancelDialogSetupWindow( HWND hWindow ) {

  HWND hwndTCancelOkay;

  // Retrieve window handles for dialog box controls to be enabled/disabled

  hwndTCancelOkay   = GetDlgItem( hWindow, IDD_OK_MEASURE     );        // Ok button
//hwndTCancelCancel = GetDlgItem( hWindow, IDD_CANCEL_MEASURE );        // Cancel button

  // Disable Ok button

  EnableWindow( hwndTCancelOkay, FALSE );
  }


// TCancelDialog Class Member - Okay measurement cycle

void TCancelDialogIDOkay( ACQUIRE *Acquire ) {

  char achRecord[ 4 ];

  Log( "-- %x TCancelDialogIDOkay\n", __LINE__ );

//Acquire->ac_bOkCycle      = TRUE;                             // Set okay measurement cycle flag
  Acquire->ac_bCancelCycle  = FALSE;                            // Clear cancel measurement cycle flag
  Acquire->ac_bFilePaint    = TRUE;
  Acquire->ac_bMeasurePaint = FALSE;
  Acquire->ac_FocusFile->Save(  Acquire->ac_FocusFile, "-1", sizeof( SPECIMENSUMMARY ) );                               // Save specimen summary record number to disk
  Acquire->ac_FocusFile->CopyTo(Acquire->ac_FocusFile, (LPSTR) &Acquire->ac_FileSummary, sizeof( SPECIMENSUMMARY ) );   // Save specimen summary data to disk
  sprintf( achRecord, "%d", Acquire->ac_FileSummary.nCurrentStep );                                                     // Set up record header string
  Acquire->ac_FocusFile->Save(  Acquire->ac_FocusFile, achRecord, sizeof( SPECIMENDATA ) );                             // Save measurement data record number to disk
  Acquire->ac_FocusFile->CopyTo(Acquire->ac_FocusFile, (LPSTR) &Acquire->ac_FileData, sizeof( SPECIMENDATA ) );         // Save measurement data to disk

  /* fix for temp files getting lost if program crashes. */
  Acquire->ac_FocusFile->HardFlush( Acquire->ac_FocusFile );                                    // flush out the file. 
  CopyTheFile( Acquire->ac_achTitleStr, Acquire->ac_achFocusFile );                             // copy the flushed file to the data file.    
  
  if( Acquire->ac_CancelDialog ) {                              // If cancel measurement cycle dialog box still exists
    DestroyWindow( Acquire->ac_CancelDialog );                  // Close it
    Acquire->ac_CancelDialog = (HWND) NULL;                     // And clear pointer
    }

  InvalidateRect(Acquire->ac_hwndChildInFocus, NULL, TRUE );    // Generate a WM_PAINT message
  UpdateWindow(  Acquire->ac_hwndChildInFocus );
  }


BOOL CALLBACK TCancelDialogProcess( HWND hWindow, UINT uMsg, WPARAM wParam, LPARAM lParam ) {

  ACQUIRE *Acquire;

  switch( uMsg ) {

    /* This Get Called When Windows Shuts Down, Asking Ok To Close Application. */

    case WM_QUERYENDSESSION:
      return TRUE;

    /* This Get Called When Windows Shuts Diwn. Telling To Close Application. */

    case WM_ENDSESSION:
      if( wParam && ( ( Acquire = (ACQUIRE *) GetWindowLong( hWindow, GWL_USERDATA ) ) != (ACQUIRE *) NULL ) ) {
        EndDialog( hWindow, 0 );
        }
      return TRUE;

    case WM_INITDIALOG:
      if( ( Acquire = (ACQUIRE *) lParam ) != (ACQUIRE *) NULL ) {
        SetWindowLong( hWindow, GWL_USERDATA, lParam );
        TCancelDialogSetupWindow( hWindow );
        return TRUE;
        }
      break;

    case WM_CLOSE:
      if( ( Acquire = (ACQUIRE *) GetWindowLong( hWindow, GWL_USERDATA ) ) != (ACQUIRE *) NULL ) {
        EndDialog( hWindow, IDCANCEL );
        }
      return TRUE;

    case WM_COMMAND:
      if( ( Acquire = (ACQUIRE *) GetWindowLong( hWindow, GWL_USERDATA ) ) != (ACQUIRE *) NULL ) {
        switch( LOWORD( wParam ) ) {

          // Responds to the Ok button

          case IDD_OK_MEASURE:
            TCancelDialogIDOkay( Acquire );
////            EndDialog( hWindow, IDOK );
            return TRUE;

          case IDD_CANCEL_MEASURE:
            EndDialog( hWindow, IDCANCEL );
            return TRUE;
          }
        }
      break;
    }
  return FALSE;
  }


BOOL CALLBACK TRangeUpDialogProcess( HWND hWindow, UINT uMsg, WPARAM wParam, LPARAM lParam ) {

  ACQUIRE *Acquire;

  switch( uMsg ) {

    /* This Get Called When Windows Shuts Down, Asking Ok To Close Application. */

    case WM_QUERYENDSESSION:
      return TRUE;

    /* This Get Called When Windows Shuts Diwn. Telling To Close Application. */

    case WM_ENDSESSION:
      if( wParam && ( ( Acquire = (ACQUIRE *) GetWindowLong( hWindow, GWL_USERDATA ) ) != (ACQUIRE *) NULL ) ) {
        EndDialog( hWindow, 0 );
        }
      return TRUE;

    case WM_INITDIALOG:
      if( ( Acquire = (ACQUIRE *) lParam ) != (ACQUIRE *) NULL ) {
        SetWindowLong( hWindow, GWL_USERDATA, lParam );
        return TRUE;
        }
      break;

    case WM_CLOSE:
      if( ( Acquire = (ACQUIRE *) GetWindowLong( hWindow, GWL_USERDATA ) ) != (ACQUIRE *) NULL ) {
        EndDialog( hWindow, IDCANCEL );
        }
      return TRUE;
    }
  return FALSE;
  }


// TSequenceDialog Class Member - Initialize controls and set current values

void TSequenceDialogSetupWindow( ACQUIRE *Acquire, HWND hWindow ) {

  // Initialize edit fields

  SetDlgItemText( hWindow, IDD_SEQUENCE_START,      Acquire->ac_SequenceDData.achStart );   // Start setting
  SetDlgItemText( hWindow, IDD_SEQUENCE_STOP1,      Acquire->ac_SequenceDData.achStop1 );   // First stop setting
  SetDlgItemText( hWindow, IDD_SEQUENCE_INCREMENT1, Acquire->ac_SequenceDData.achIncr1 );   // First increment setting
  SetDlgItemText( hWindow, IDD_SEQUENCE_STOP2,      Acquire->ac_SequenceDData.achStop2 );   // Second stop setting
  SetDlgItemText( hWindow, IDD_SEQUENCE_INCREMENT2, Acquire->ac_SequenceDData.achIncr2 );   // Second increment setting
  }


// TSequenceDialog Class Member - Respond to the Ok button

void TSequenceDialogIDOk( ACQUIRE *Acquire, HWND hWindow ) {

  // Retrieve values in edit fields

  GetDlgItemText( hWindow, IDD_SEQUENCE_START,      Acquire->ac_SequenceDData.achStart, 6 );    // Start value
  GetDlgItemText( hWindow, IDD_SEQUENCE_STOP1,      Acquire->ac_SequenceDData.achStop1, 6 );    // First stop value
  GetDlgItemText( hWindow, IDD_SEQUENCE_INCREMENT1, Acquire->ac_SequenceDData.achIncr1, 6 );    // First increment value
  GetDlgItemText( hWindow, IDD_SEQUENCE_STOP2,      Acquire->ac_SequenceDData.achStop2, 6 );    // Second stop value
  GetDlgItemText( hWindow, IDD_SEQUENCE_INCREMENT2, Acquire->ac_SequenceDData.achIncr2, 6 );    // Second increment value
  }


// TSequenceDialog Class Member - TRUE if dialog box can close

BOOL TSequenceDialogCanClose( ACQUIRE *Acquire, HWND hWindow ) {

  char achStart[ 6 ], achStop1[ 6 ], achIncr1[ 6 ], achStop2[ 6 ], achIncr2[ 6 ];
  double dStart, dStop1, dIncr1, dStop2, dIncr2;

  // Retrieve entry text from edit fields

  GetDlgItemText( hWindow, IDD_SEQUENCE_START,      achStart, 6 );       // Start field
  GetDlgItemText( hWindow, IDD_SEQUENCE_STOP1,      achStop1, 6 );       // First stop field
  GetDlgItemText( hWindow, IDD_SEQUENCE_INCREMENT1, achIncr1, 6 );       // First increment
  GetDlgItemText( hWindow, IDD_SEQUENCE_STOP2,      achStop2, 6 );       // Second stop field
  GetDlgItemText( hWindow, IDD_SEQUENCE_INCREMENT2, achIncr2, 6 );       // Second increment

  // Convert strings to numeric values

  dStart = strtod( achStart, NULL );    // Start field
  dStop1 = strtod( achStop1, NULL );    // First stop field
  dIncr1 = strtod( achIncr1, NULL );    // First increment
  dStop2 = strtod( achStop2, NULL );    // Second stop field
  dIncr2 = strtod( achIncr2, NULL );    // Second increment

  // Entries must be in proper format, and values must be within bounds

  if( !DigitsPointAndSign( achStart ) || dStart < 0.0 || dStart > Acquire->ac_dMaxAFField ) {   // If entry is not just digits with an optional point and hyphen,
    MessageBeep( 0 );                                                                           // beep the speaker in warning,
    SetDlgItemText( hWindow, IDD_SEQUENCE_START, '\0' );                                        // clear the entry field, and
    return FALSE;
    }
  if( !DigitsPointAndSign( achStop1 ) || dStop1 <= 0.0 || dStop1 > Acquire->ac_dMaxAFField ) {  // If entry is not just digits with an optional point and hyphen,
    MessageBeep( 0 );                                                                           // beep the speaker in warning,
    SetDlgItemText( hWindow, IDD_SEQUENCE_STOP1, '\0' );                                        // clear the entry field, and
    return FALSE;
    }
  if( !DigitsPointAndSign( achIncr1 ) || dIncr1 < 0.1 || dIncr1 > Acquire->ac_dMaxAFField ) {   // If entry is not just digits with an optional point and hyphen,
    MessageBeep( 0 );                                                                           // beep the speaker in warning,
    SetDlgItemText( hWindow, IDD_SEQUENCE_INCREMENT1, '\0' );                                   // clear the entry field, and
    return FALSE;
    }
  if( !DigitsPointAndSign( achStop2 ) || dStop2 < 0.0 || dStop2 > Acquire->ac_dMaxAFField ) {   // If entry is not just digits with an optional point and hyphen,
    MessageBeep( 0 );                                                                           // beep the speaker in warning,
    SetDlgItemText( hWindow, IDD_SEQUENCE_STOP2, '\0' );                                        // clear the entry field, and
    return FALSE;
    }
  if( !DigitsPointAndSign( achIncr2 ) || dIncr2 < 0.0 || dIncr2 > Acquire->ac_dMaxAFField ) {   // If entry is not just digits with an optional point and hyphen,
    MessageBeep( 0 );                                                                           // beep the speaker in warning,
    SetDlgItemText( hWindow, IDD_SEQUENCE_INCREMENT2, '\0' );                                   // clear the entry field, and
    return FALSE;
    }
  if( dStop2 > dStop1 && dIncr2 < 0.1 ) {                                                       // If entry is not just digits with an optional point and hyphen,
    MessageBeep( 0 );                                                                           // beep the speaker in warning,
    SetDlgItemText( hWindow, IDD_SEQUENCE_INCREMENT2, '\0' );                                   // clear the entry field, and
    return FALSE;
    }
  return TRUE;                                                                                  // Entry values must all be valid
  }


BOOL CALLBACK TSequenceDialogProcess( HWND hWindow, UINT uMsg, WPARAM wParam, LPARAM lParam ) {

  ACQUIRE *Acquire;

  switch( uMsg ) {

    /* This Get Called When Windows Shuts Down, Asking Ok To Close Application. */

    case WM_QUERYENDSESSION:
      return TRUE;

    /* This Get Called When Windows Shuts Diwn. Telling To Close Application. */

    case WM_ENDSESSION:
      if( wParam && ( ( Acquire = (ACQUIRE *) GetWindowLong( hWindow, GWL_USERDATA ) ) != (ACQUIRE *) NULL ) ) {
        EndDialog( hWindow, 0 );
        }
      return TRUE;

    case WM_INITDIALOG:
      if( ( Acquire = (ACQUIRE *) lParam ) != (ACQUIRE *) NULL ) {
        SetWindowLong( hWindow, GWL_USERDATA, lParam );
        TSequenceDialogSetupWindow( Acquire, hWindow );
        return TRUE;
        }
      break;

    case WM_CLOSE:
      if( ( Acquire = (ACQUIRE *) GetWindowLong( hWindow, GWL_USERDATA ) ) != (ACQUIRE *) NULL ) {
        EndDialog( hWindow, IDCANCEL );
        }
      return TRUE;

    case WM_COMMAND:
      if( ( Acquire = (ACQUIRE *) GetWindowLong( hWindow, GWL_USERDATA ) ) != (ACQUIRE *) NULL ) {
        switch( LOWORD( wParam ) ) {

          // Responds to the Ok button

          case IDD_SEQUENCE_OK:
            if( TSequenceDialogCanClose( Acquire, hWindow ) ) {
              TSequenceDialogIDOk( Acquire, hWindow );
              EndDialog( hWindow, IDOK );
              }
            return TRUE;

          case IDCANCEL:
            EndDialog( hWindow, IDCANCEL );
            return TRUE;
          }
        }
      break;
    }
  return FALSE;
  }


// THolderDialog Class Member - Initialize controls and set current values

void THolderDialogSetupWindow( ACQUIRE *Acquire, HWND hWindow ) {

  // Fill edit fields with initial values

  SetDlgItemText( hWindow, IDD_HOLDER_X,     Acquire->ac_HolderDData.achX     );
  SetDlgItemText( hWindow, IDD_HOLDER_Y,     Acquire->ac_HolderDData.achY     );
  SetDlgItemText( hWindow, IDD_HOLDER_Z,     Acquire->ac_HolderDData.achZ     );
  SetDlgItemText( hWindow, IDD_HOLDER_TOTAL, Acquire->ac_HolderDData.achTotal );
  }


// THolderDialog Class Member - Clear sample holder moments

void THolderDialogIDClear( ACQUIRE *Acquire ) {

  // Zero moment edit fields

  strcpy( Acquire->ac_HolderDData.achX,     "0.000e-00" );      // X moment
  strcpy( Acquire->ac_HolderDData.achY,     "0.000e-00" );      // Y moment
  strcpy( Acquire->ac_HolderDData.achZ,     "0.000e-00" );      // Z moment
  strcpy( Acquire->ac_HolderDData.achTotal, "0.000e-00" );      // Total moment

  // Zero holder moments

  Acquire->ac_dHolderXComponent = 0.0;
  Acquire->ac_dHolderYComponent = 0.0;
  Acquire->ac_dHolderZComponent = 0.0;
  }


BOOL CALLBACK THolderDialogProcess( HWND hWindow, UINT uMsg, WPARAM wParam, LPARAM lParam ) {

  ACQUIRE *Acquire;

  switch( uMsg ) {

    /* This Get Called When Windows Shuts Down, Asking Ok To Close Application. */

    case WM_QUERYENDSESSION:
      return TRUE;

    /* This Get Called When Windows Shuts Diwn. Telling To Close Application. */

    case WM_ENDSESSION:
      if( wParam && ( ( Acquire = (ACQUIRE *) GetWindowLong( hWindow, GWL_USERDATA ) ) != (ACQUIRE *) NULL ) ) {
        EndDialog( hWindow, 0 );
        }
      return TRUE;

    case WM_INITDIALOG:
      if( ( Acquire = (ACQUIRE *) lParam ) != (ACQUIRE *) NULL ) {
        SetWindowLong( hWindow, GWL_USERDATA, lParam );
        THolderDialogSetupWindow( Acquire, hWindow );
        return TRUE;
        }
      break;

    case WM_CLOSE:
      if( ( Acquire = (ACQUIRE *) GetWindowLong( hWindow, GWL_USERDATA ) ) != (ACQUIRE *) NULL ) {
        EndDialog( hWindow, IDCANCEL );
        }
      return TRUE;

    case WM_COMMAND:
      if( ( Acquire = (ACQUIRE *) GetWindowLong( hWindow, GWL_USERDATA ) ) != (ACQUIRE *) NULL ) {
        switch( LOWORD( wParam ) ) {

          // Responds to the Ok button

          case IDOK:
            EndDialog( hWindow, IDOK );
            return TRUE;

          case IDCANCEL:
            EndDialog( hWindow, IDCANCEL );
            return TRUE;

          case IDD_HOLDER_CLEAR:
            THolderDialogIDClear( Acquire );
            EndDialog( hWindow, IDCANCEL );
            return TRUE;
          }
        }
      break;
    }
  return FALSE;
  }


// TStandardDialog Class Member - Initialize controls and set current values

void TStandardDialogSetupWindow( ACQUIRE *Acquire, HWND hWindow ) {

  // Fill edit fields with initial values

  SetDlgItemText( hWindow, IDD_STANDARD_X,     Acquire->ac_StandardDData.achX     );
  SetDlgItemText( hWindow, IDD_STANDARD_Y,     Acquire->ac_StandardDData.achY     );
  SetDlgItemText( hWindow, IDD_STANDARD_Z,     Acquire->ac_StandardDData.achZ     );
  SetDlgItemText( hWindow, IDD_STANDARD_TOTAL, Acquire->ac_StandardDData.achTotal );
  SetDlgItemText( hWindow, IDD_STANDARD_ANGLE, Acquire->ac_StandardDData.achAngle );
  }


// TStandardDialog Class Member - Clear angle between sample and magnetometer X axes

void TStandardDialogIDClearAngle( ACQUIRE *Acquire ) {

  // Zero angle

  strcpy( Acquire->ac_StandardDData.achAngle, "0.0" );  // Angle between sample and magnetometer x axes
  Acquire->ac_dXYAngle = 0.0;
  }


BOOL CALLBACK TStandardDialogProcess( HWND hWindow, UINT uMsg, WPARAM wParam, LPARAM lParam ) {

  ACQUIRE *Acquire;

  switch( uMsg ) {

    /* This Get Called When Windows Shuts Down, Asking Ok To Close Application. */

    case WM_QUERYENDSESSION:
      return TRUE;

    /* This Get Called When Windows Shuts Diwn. Telling To Close Application. */

    case WM_ENDSESSION:
      if( wParam && ( ( Acquire = (ACQUIRE *) GetWindowLong( hWindow, GWL_USERDATA ) ) != (ACQUIRE *) NULL ) ) {
        EndDialog( hWindow, 0 );
        }
      return TRUE;

    case WM_INITDIALOG:
      if( ( Acquire = (ACQUIRE *) lParam ) != (ACQUIRE *) NULL ) {
        SetWindowLong( hWindow, GWL_USERDATA, lParam );
        TStandardDialogSetupWindow( Acquire, hWindow );
        return TRUE;
        }
      break;

    case WM_CLOSE:
      if( ( Acquire = (ACQUIRE *) GetWindowLong( hWindow, GWL_USERDATA ) ) != (ACQUIRE *) NULL ) {
        EndDialog( hWindow, IDCANCEL );
        }
      return TRUE;

    case WM_COMMAND:
      if( ( Acquire = (ACQUIRE *) GetWindowLong( hWindow, GWL_USERDATA ) ) != (ACQUIRE *) NULL ) {
        switch( LOWORD( wParam ) ) {

          // Responds to the Ok button

          case IDOK:
            EndDialog( hWindow, IDOK );
            return TRUE;

          case IDCANCEL:
            EndDialog( hWindow, IDCANCEL );
            return TRUE;

          case IDD_STANDARD_CLEAR:
            TStandardDialogIDClearAngle( Acquire );
            EndDialog( hWindow, IDCANCEL );
            return TRUE;
          }
        }
      break;
    }
  return FALSE;
  }


// TSHMoveDialog Class Member - Initialize controls and set current values

void TSHMoveDialogSetupWindow( ACQUIRE *Acquire, HWND hWindow ) {

  HWND  hTransT, hTrans, hRotT, hRot;

  // Retrieve window handles for dialog box controls to be enabled / disabled

  hTransT = GetDlgItem( hWindow, IDD_MOVE_TRANST );             // Translate to title string
  hTrans  = GetDlgItem( hWindow, IDD_MOVE_TRANS  );             // Translate to combo box
  hRotT   = GetDlgItem( hWindow, IDD_MOVE_ROTT   );             // Rotate to title string
  hRot    = GetDlgItem( hWindow, IDD_MOVE_ROT    );             // Rotate to combo box

  // Add choices to the Translate combo box

  ComboBox_InsertString( hTrans, 0, (DWORD) "Home"        );    // Position choices
  ComboBox_InsertString( hTrans, 1, (DWORD) "Load"        );
  ComboBox_InsertString( hTrans, 2, (DWORD) "Background"  );
  ComboBox_InsertString( hTrans, 3, (DWORD) "Measurement" );
  ComboBox_InsertString( hTrans, 4, (DWORD) "Right Limit" );
  ComboBox_InsertString( hTrans, 5, (DWORD) "Left Limit"  );
  ComboBox_InsertString( hTrans, 6, (DWORD) "AF Z Axis"   );
  ComboBox_InsertString( hTrans, 7, (DWORD) "AF Y Axis"   );
  ComboBox_InsertString( hTrans, 8, (DWORD) "AF X Axis"   );

  if( !Acquire->ac_bAFAuto || !Acquire->ac_bAFXAxis )           // For no AF X axis
    ComboBox_DeleteString( hTrans, 8 );                         // Delete position selection
  if( !Acquire->ac_bAFAuto || !Acquire->ac_bAFYAxis )           // For no AF Y axis
    ComboBox_DeleteString( hTrans, 7 );                         // Delete position selection
  if( !Acquire->ac_bAFAuto || !Acquire->ac_bAFZAxis )           // For no AF Z axis
    ComboBox_DeleteString( hTrans, 6 );                         // Delete position selection

  // Add choices to the Rotate combo box

  ComboBox_InsertString( hRot, 0, (DWORD) "Home" );             // Position choices
  ComboBox_InsertString( hRot, 1, (DWORD) "90"   );
  ComboBox_InsertString( hRot, 2, (DWORD) "180"  );
  ComboBox_InsertString( hRot, 3, (DWORD) "270"  );

  // Disable controls which do not apply

  if( !Acquire->ac_bSHTrans ) {                                 // No translation axis
    EnableWindow( hTransT, FALSE );                             // Translate to title string
    EnableWindow( hTrans,  FALSE );                             // Translate to combo box
    }
  if( !Acquire->ac_bSHRot ) {                                   // No rotation axis
    EnableWindow( hRotT, FALSE );                               // Rotate to title string
    EnableWindow( hRot,  FALSE );                               // Rotate to combo box
    }

  // Set current selections in combo boxes

  ComboBox_SetCurSel( hTrans, Acquire->ac_SHMoveDData.dwTrans );// Current position selection
  ComboBox_SetCurSel( hRot,   Acquire->ac_SHMoveDData.dwRot   );// Current position selection
  }


// TSHMoveDialog Class Member - Respond to the Ok button

void TSHMoveDialogIDOk( ACQUIRE *Acquire, HWND hWindow ) {

  Acquire->ac_SHMoveDData.dwTrans = ComboBox_GetCurSel( GetDlgItem( hWindow, IDD_MOVE_TRANS ) );// Translate
  Acquire->ac_SHMoveDData.dwRot   = ComboBox_GetCurSel( GetDlgItem( hWindow, IDD_MOVE_ROT   ) );// Rotate
  }


BOOL CALLBACK TSHMoveDialogProcess( HWND hWindow, UINT uMsg, WPARAM wParam, LPARAM lParam ) {

  ACQUIRE *Acquire;

  switch( uMsg ) {

    /* This Get Called When Windows Shuts Down, Asking Ok To Close Application. */

    case WM_QUERYENDSESSION:
      return TRUE;

    /* This Get Called When Windows Shuts Diwn. Telling To Close Application. */

    case WM_ENDSESSION:
      if( wParam && ( ( Acquire = (ACQUIRE *) GetWindowLong( hWindow, GWL_USERDATA ) ) != (ACQUIRE *) NULL ) ) {
        EndDialog( hWindow, 0 );
        }
      return TRUE;

    case WM_INITDIALOG:
      if( ( Acquire = (ACQUIRE *) lParam ) != (ACQUIRE *) NULL ) {
        SetWindowLong( hWindow, GWL_USERDATA, lParam );
        TSHMoveDialogSetupWindow( Acquire, hWindow );
        return TRUE;
        }
      break;

    case WM_CLOSE:
      if( ( Acquire = (ACQUIRE *) GetWindowLong( hWindow, GWL_USERDATA ) ) != (ACQUIRE *) NULL ) {
        EndDialog( hWindow, IDCANCEL );
        }
      return TRUE;

    case WM_COMMAND:
      if( ( Acquire = (ACQUIRE *) GetWindowLong( hWindow, GWL_USERDATA ) ) != (ACQUIRE *) NULL ) {
        switch( LOWORD( wParam ) ) {

          // Responds to the Ok button

          case IDD_MOVE_OK:
            TSHMoveDialogIDOk( Acquire, hWindow );
            EndDialog( hWindow, IDOK );
            return TRUE;

          case IDCANCEL:
            EndDialog( hWindow, IDCANCEL );
            return TRUE;
          }
        }
      break;
    }
  return FALSE;
  }


/************************************************************************
*                                                                       *
*       TAboutDialogProcess:                                            *
*                                                                       *
*       Handles The About Window Messages.                              *
*                                                                       *
*       Input:                                                          *
*                                                                       *
*       hWindow = Handle of Window.                                     *
*       uMsg    = Message identifier.                                   *
*       wParam  = First  message parameter.                             *
*       lParam  = Second message parameter.                             *
*                                                                       *
*       Output:                                                         *
*                                                                       *
*       The Ok Messages Is Handled In The About Box.                    *
*                                                                       *
*       Returns -> TRUE  == Message Was Handled.                        *
*                  FALSE == Message Was Not Handled.                    *
*                                                                       *
*       24.Sept.2002 Jamie Lisa Finch.                                  *
*                                                                       *
************************************************************************/

BOOL CALLBACK TAboutDialogProcess( HWND hWindow, UINT uMsg, WPARAM wParam, LPARAM lParam ) {

  switch( uMsg ) {

    case WM_QUERYENDSESSION:
      return TRUE;

    case WM_ENDSESSION:
      if( wParam )
        EndDialog( hWindow, 0 );
      return TRUE;

    case WM_INITDIALOG:
      if( lParam ) {
        SetWindowLong( hWindow, GWL_USERDATA, lParam );
        return TRUE;
        }
      break;

    case WM_CLOSE:
      EndDialog( hWindow, 0 );
      return TRUE;

    case WM_COMMAND:
      switch( LOWORD( wParam ) ) {
        case IDOK:
          EndDialog( hWindow, 0 );
          return TRUE;
        }
      break;
    }
  return FALSE;
  }
