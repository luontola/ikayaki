/************************************************************************
*                                                                       *
*       Main.c                                                          *
*                                                                       *
*       Contains The Code For The PAcquire Program.                     *
*                                                                       *
*       24.Sept.2002 Jamie Lisa Finch.                                  *
*                                                                       *
************************************************************************/

#include <windows.h>
#include <windowsx.h>
#include <commctrl.h>
#include <time.h>
#include "..\h\PAcquire.h"
#include "..\h\Text.h"
#include "..\h\Messages.h"
#include "..\h\Fnctns.h"
#include "..\h\MyChild.h"
#include "..\h\Configure.h"
#include "..\h\FileRequestor.h"
#define APSTUDIO_INVOKED 1
#include "..\res\Resource.h"

// Static data

char achPrFile[]      = "pacquire.ini";                 // Name of profile file which holds program initialization parameters
char achPrMConfig[]   = "Magnetometer Configuration";   // Profile file key name for magnetometer configuration parameters
char achPrSHConfig[]  = "Sample Handler Configuration"; // Profile file key name for sample handler configuration parameters
char achPrAFConfig[]  = "AF Demagnetizer Configuration";// Profile file key name for af demagnetizer configuration parameters
char achPrRSConfig[]  = "RS-232 Configuration";         // Profile file key name for RS-232 configuration parameters
char achPrMCalib[]    = "Calibration Constants";        // Profile file key name for magnetometer calibration constants
char achPrAFCalib[]   = "AF Demagnetizer Parameters";   // Profile file key name for af demagnetizer operation parameters
char achPrSHCalib[]   = "Sample Handler Parameters";    // Profile file key name for sample handler operation parameters
char achPrOrient[]    = "Orientation Conventions";      // Profile file key name for core and structure orientation conventions
char achPrMSettings[] = "Magnetometer Settings";        // Profile file key name for magnetometer settings
char achPrHolder[]    = "Holder Moments";               // Profile file key name for sample holder moments
char achPrStnd[]      = "Standard Moments";             // Profile file key name for standard sample moments
char achPrMOptions[]  = "Measure Options";              // Profile file key name for measure options
//char achPrPOptions[]= "Projection Options";           // Profile file key name for projection options

ACQUIRE *GAcquire;


#ifdef _DEBUG
#define ONEAXISTEST 1        //allows samples to be taken with only one axis
#else
#define ONEAXISTEST 0        //do not allows samples to be taken with only one axis
#endif



/************************************************************************
*                                                                       *
*       Create a new sample child window                                *
*                                                                       *
************************************************************************/


// Compares name of file to be opened with those already opened

void CheckOpenFiles( ACQUIRE *Acquire, int *ptCheck ) {

  HWND  hWindow;
  char *p;
  char  achText[ MAXFILENAME ];

  for( hWindow = GetWindow( Acquire->ac_hWindowMDIClient, GW_CHILD ); hWindow; hWindow = GetWindow( hWindow, GW_HWNDNEXT ) ) {

    /* Skip if an icon title window */

    if( GetWindow( hWindow, GW_OWNER ) )
      continue;

    GetWindowText( hWindow, achText, sizeof( achText ) );

    for( p = achText; *p != '\0'; p++ ) {                       // For each character in the caption string
      if( isalpha( *p ) && isupper( *p ) )                      // If a character is alphabetic and uppercase
        *p = tolower( *p );                                     // Convert letter to lowercase
      }

    if( strstr( Acquire->ac_achFileOpen, achText ) != NULL )    // If caption matches name of file to be opened
      (*ptCheck)++;                                             // Increment counter
    }
  }


// Counts the number of existing child windows

void CountChildren( ACQUIRE *Acquire, int *ptCount) {

  HWND hWindow;

  for( hWindow = GetWindow( Acquire->ac_hWindowMDIClient, GW_CHILD ); hWindow; hWindow = GetWindow( hWindow, GW_HWNDNEXT ) ) {

    /* Skip if an icon title window */

    if( GetWindow( hWindow, GW_OWNER ) )
      continue;

    (*ptCount)++;
    }
  }


// TMyMDIFrame Class Member

void TMyMDIFrameWMDestroy( ACQUIRE *Acquire ) {

  char ach[ 15 ];

  if( Acquire != (ACQUIRE *) NULL ) {

    // Save current magnetometer configuration parameters

    wsprintf( ach, "%lu", Acquire->ac_ConfigureDData.dwX );
    WritePrivateProfileString( achPrMConfig, "x", ach, achPrFile );
    wsprintf( ach, "%lu", Acquire->ac_ConfigureDData.dwY );
    WritePrivateProfileString( achPrMConfig, "y", ach, achPrFile );
    wsprintf( ach, "%lu", Acquire->ac_ConfigureDData.dwZ );
    WritePrivateProfileString( achPrMConfig, "z", ach, achPrFile );
    wsprintf( ach, "%lu", Acquire->ac_ConfigureDData.dwDCSquids );
    WritePrivateProfileString( achPrMConfig, "dc", ach, achPrFile );

    // Save current sample handler configuration parameters

    wsprintf( ach, "%lu", Acquire->ac_ConfigureDData.dwSHAuto );
    WritePrivateProfileString( achPrSHConfig, "auto", ach, achPrFile );
    wsprintf( ach, "%lu", Acquire->ac_ConfigureDData.dwSHMan );
    WritePrivateProfileString( achPrSHConfig, "manual", ach, achPrFile );

    // Save current AF demagnetizer configuration parameters

    wsprintf(ach, "%lu", Acquire->ac_ConfigureDData.dwAFAuto);
    WritePrivateProfileString(achPrAFConfig, "auto", ach, achPrFile);
    wsprintf(ach, "%lu", Acquire->ac_ConfigureDData.dwAFMan);
    WritePrivateProfileString(achPrAFConfig, "manual", ach, achPrFile);
    wsprintf(ach, "%lu", Acquire->ac_ConfigureDData.dwAFNone);
    WritePrivateProfileString(achPrAFConfig, "none", ach, achPrFile);

    // Save current RS-232 configuration parameters

    wsprintf(ach, "%lu", Acquire->ac_ConfigureDData.dwMagCom);
    WritePrivateProfileString(achPrRSConfig, "Mag port", ach, achPrFile);
    wsprintf(ach, "%lu", Acquire->ac_ConfigureDData.dwAFCom);
    WritePrivateProfileString(achPrRSConfig, "AF port", ach, achPrFile);
    wsprintf(ach, "%lu", Acquire->ac_ConfigureDData.dwSHCom);
    WritePrivateProfileString(achPrRSConfig, "SH port", ach, achPrFile);

    // Save current magnetometer calibration constants

    WritePrivateProfileString(achPrMCalib, "x", Acquire->ac_ConstantsDData.achX, achPrFile);
    WritePrivateProfileString(achPrMCalib, "y", Acquire->ac_ConstantsDData.achY, achPrFile);
    WritePrivateProfileString(achPrMCalib, "z", Acquire->ac_ConstantsDData.achZ, achPrFile);
/*
    wsprintf(ach, "%s", (LPSTR) Acquire->ac_ConstantsDData.achX);
    WritePrivateProfileString(achPrMCalib, "x", ach, achPrFile);
    wsprintf(ach, "%s", (LPSTR) Acquire->ac_ConstantsDData.achY);
    WritePrivateProfileString(achPrMCalib, "y", ach, achPrFile);
    wsprintf(ach, "%s", (LPSTR) Acquire->ac_ConstantsDData.achZ);
    WritePrivateProfileString(achPrMCalib, "z", ach, achPrFile);
*/
    // Save current af demagnetizer operation parameters

    wsprintf(ach, "%lu", Acquire->ac_DemagDData.dwX);
    WritePrivateProfileString(achPrAFCalib, "x", ach, achPrFile);
    wsprintf(ach, "%lu", Acquire->ac_DemagDData.dwY);
    WritePrivateProfileString(achPrAFCalib, "y", ach, achPrFile);
    wsprintf(ach, "%lu", Acquire->ac_DemagDData.dwZ);
    WritePrivateProfileString(achPrAFCalib, "z", ach, achPrFile);
    wsprintf(ach, "%lu", Acquire->ac_DemagDData.dw2G601S);
    WritePrivateProfileString(achPrAFCalib, "2G601S", ach, achPrFile);
    wsprintf(ach, "%lu", Acquire->ac_DemagDData.dw2G601T);
    WritePrivateProfileString(achPrAFCalib, "2G601T", ach, achPrFile);
    WritePrivateProfileString(achPrAFCalib, "field", Acquire->ac_DemagDData.achField, achPrFile);
//  wsprintf(ach, "%s", (LPSTR) Acquire->ac_DemagDData.achField);
//  WritePrivateProfileString(achPrAFCalib, "field", ach, achPrFile);
    wsprintf(ach, "%lu", Acquire->ac_DemagDData.dwRamp);
    WritePrivateProfileString(achPrAFCalib, "ramp", ach, achPrFile);
    wsprintf(ach, "%lu", Acquire->ac_DemagDData.dwDelay);
    WritePrivateProfileString(achPrAFCalib, "delay", ach, achPrFile);

    // Save current sample handler operation parameters

//  wsprintf(ach, "%lu", Acquire->ac_HandlerDData.dw2G810);
//  WritePrivateProfileString(achPrSHCalib, "2G810", ach, achPrFile);
//  wsprintf(ach, "%lu", Acquire->ac_HandlerDData.dw2G811);
//  WritePrivateProfileString(achPrSHCalib, "2G811", ach, achPrFile);
    wsprintf(ach, "%lu", Acquire->ac_HandlerDData.dwTrans);
    WritePrivateProfileString(achPrSHCalib, "trans", ach, achPrFile);
    wsprintf(ach, "%lu", Acquire->ac_HandlerDData.dwRot);
    WritePrivateProfileString(achPrSHCalib, "rot", ach, achPrFile);
//  wsprintf(ach, "%lu", Acquire->ac_HandlerDData.dwFlip);
//  WritePrivateProfileString(achPrSHCalib, "flip", ach, achPrFile);
//  wsprintf(ach, "%lu", Acquire->ac_HandlerDData.dwNoFlip);
//  WritePrivateProfileString(achPrSHCalib, "no flip", ach, achPrFile);
    WritePrivateProfileString(achPrSHCalib, "accel", Acquire->ac_HandlerDData.achAccel, achPrFile);
    WritePrivateProfileString(achPrSHCalib, "decel", Acquire->ac_HandlerDData.achDecel, achPrFile);
    WritePrivateProfileString(achPrSHCalib, "vel", Acquire->ac_HandlerDData.achVel, achPrFile);

    WritePrivateProfileString(achPrSHCalib, "accelRot", Acquire->ac_HandlerDData.achRotAccel, achPrFile);
    WritePrivateProfileString(achPrSHCalib, "decelRot", Acquire->ac_HandlerDData.achRotDecel, achPrFile);
    WritePrivateProfileString(achPrSHCalib, "velRot", Acquire->ac_HandlerDData.achRotVel, achPrFile);
    
    WritePrivateProfileString(achPrSHCalib, "vel meas", Acquire->ac_HandlerDData.achVelM, achPrFile);
    WritePrivateProfileString(achPrSHCalib, "x AF", Acquire->ac_HandlerDData.achPosXAF, achPrFile);
    WritePrivateProfileString(achPrSHCalib, "y AF", Acquire->ac_HandlerDData.achPosYAF, achPrFile);
    WritePrivateProfileString(achPrSHCalib, "z AF", Acquire->ac_HandlerDData.achPosZAF, achPrFile);
    WritePrivateProfileString(achPrSHCalib, "load", Acquire->ac_HandlerDData.achPosLoad, achPrFile);
    WritePrivateProfileString(achPrSHCalib, "back", Acquire->ac_HandlerDData.achPosBack, achPrFile);
    WritePrivateProfileString(achPrSHCalib, "meas", Acquire->ac_HandlerDData.achPosMeas, achPrFile);
    WritePrivateProfileString(achPrSHCalib, "counts", Acquire->ac_HandlerDData.achRotCounts, achPrFile);
/*
    wsprintf(ach, "%s", (LPSTR) Acquire->ac_HandlerDData.achAccel);
    WritePrivateProfileString(achPrSHCalib, "accel", ach, achPrFile);
    wsprintf(ach, "%s", (LPSTR) Acquire->ac_HandlerDData.achDecel);
    WritePrivateProfileString(achPrSHCalib, "decel", ach, achPrFile);
    wsprintf(ach, "%s", (LPSTR) Acquire->ac_HandlerDData.achVel);
    WritePrivateProfileString(achPrSHCalib, "vel", ach, achPrFile);
    wsprintf(ach, "%s", (LPSTR) Acquire->ac_HandlerDData.achVelM);
    WritePrivateProfileString(achPrSHCalib, "vel meas", ach, achPrFile);
    wsprintf(ach, "%s", (LPSTR) Acquire->ac_HandlerDData.achPosXAF);
    WritePrivateProfileString(achPrSHCalib, "x AF", ach, achPrFile);
    wsprintf(ach, "%s", (LPSTR) Acquire->ac_HandlerDData.achPosYAF);
    WritePrivateProfileString(achPrSHCalib, "y AF", ach, achPrFile);
    wsprintf(ach, "%s", (LPSTR) Acquire->ac_HandlerDData.achPosZAF);
    WritePrivateProfileString(achPrSHCalib, "z AF", ach, achPrFile);
    wsprintf(ach, "%s", (LPSTR) Acquire->ac_HandlerDData.achPosLoad);
    WritePrivateProfileString(achPrSHCalib, "load", ach, achPrFile);
    wsprintf(ach, "%s", (LPSTR) Acquire->ac_HandlerDData.achPosBack);
    WritePrivateProfileString(achPrSHCalib, "back", ach, achPrFile);
    wsprintf(ach, "%s", (LPSTR) Acquire->ac_HandlerDData.achPosMeas);
    WritePrivateProfileString(achPrSHCalib, "meas", ach, achPrFile);
    wsprintf(ach, "%s", (LPSTR) Acquire->ac_HandlerDData.achRotCounts);
    WritePrivateProfileString(achPrSHCalib, "counts", ach, achPrFile);
*/
    wsprintf(ach, "%lu", Acquire->ac_HandlerDData.dwRight);
    WritePrivateProfileString(achPrSHCalib, "right", ach, achPrFile);

    // Save current core and structure orientation conventions

    WritePrivateProfileString(achPrOrient, "core az", Acquire->ac_OrientDData.achCoreAz, achPrFile);
    wsprintf(ach, "%lu", Acquire->ac_OrientDData.dwCorePComp);
    WritePrivateProfileString(achPrOrient, "core pl comp", ach, achPrFile);
    wsprintf(ach, "%lu", Acquire->ac_OrientDData.dwCorePSign);
    WritePrivateProfileString(achPrOrient, "core pl sign", ach, achPrFile);
    WritePrivateProfileString(achPrOrient, "bed az", Acquire->ac_OrientDData.achBeddingAz, achPrFile);
    wsprintf(ach, "%lu", Acquire->ac_OrientDData.dwBeddingPComp);
    WritePrivateProfileString(achPrOrient, "bed pl comp", ach, achPrFile);
    wsprintf(ach, "%lu", Acquire->ac_OrientDData.dwBeddingPSign);
    WritePrivateProfileString(achPrOrient, "bed pl sign", ach, achPrFile);
    WritePrivateProfileString(achPrOrient, "fold az", Acquire->ac_OrientDData.achFoldAz, achPrFile);
    wsprintf(ach, "%lu", Acquire->ac_OrientDData.dwFoldPComp);
    WritePrivateProfileString(achPrOrient, "fold pl comp", ach, achPrFile);
    wsprintf(ach, "%lu", Acquire->ac_OrientDData.dwFoldPSign);
    WritePrivateProfileString(achPrOrient, "fold pl sign", ach, achPrFile);

    // Save current magnetometer settings

    wsprintf(ach, "%lu", Acquire->ac_MagnetometerDData.dwXRange);
    WritePrivateProfileString(achPrMSettings, "xrange", ach, achPrFile);
    wsprintf(ach, "%lu", Acquire->ac_MagnetometerDData.dwXFlux);
    WritePrivateProfileString(achPrMSettings, "xflux", ach, achPrFile);
    wsprintf(ach, "%lu", Acquire->ac_MagnetometerDData.dwXFilter);
    WritePrivateProfileString(achPrMSettings, "xfilter", ach, achPrFile);
    wsprintf(ach, "%lu", Acquire->ac_MagnetometerDData.dwXSlew);
    WritePrivateProfileString(achPrMSettings, "xslew", ach, achPrFile);
    wsprintf(ach, "%lu", Acquire->ac_MagnetometerDData.dwXPanel);
    WritePrivateProfileString(achPrMSettings, "xpanel", ach, achPrFile);
    wsprintf(ach, "%lu", Acquire->ac_MagnetometerDData.dwYRange);
    WritePrivateProfileString(achPrMSettings, "yrange", ach, achPrFile);
    wsprintf(ach, "%lu", Acquire->ac_MagnetometerDData.dwYFlux);
    WritePrivateProfileString(achPrMSettings, "yflux", ach, achPrFile);
    wsprintf(ach, "%lu", Acquire->ac_MagnetometerDData.dwYFilter);
    WritePrivateProfileString(achPrMSettings, "yfilter", ach, achPrFile);
    wsprintf(ach, "%lu", Acquire->ac_MagnetometerDData.dwYSlew);
    WritePrivateProfileString(achPrMSettings, "yslew", ach, achPrFile);
    wsprintf(ach, "%lu", Acquire->ac_MagnetometerDData.dwYPanel);
    WritePrivateProfileString(achPrMSettings, "ypanel", ach, achPrFile);
    wsprintf(ach, "%lu", Acquire->ac_MagnetometerDData.dwZRange);
    WritePrivateProfileString(achPrMSettings, "zrange", ach, achPrFile);
    wsprintf(ach, "%lu", Acquire->ac_MagnetometerDData.dwZFlux);
    WritePrivateProfileString(achPrMSettings, "zflux", ach, achPrFile);
    wsprintf(ach, "%lu", Acquire->ac_MagnetometerDData.dwZFilter);
    WritePrivateProfileString(achPrMSettings, "zfilter", ach, achPrFile);
    wsprintf(ach, "%lu", Acquire->ac_MagnetometerDData.dwZSlew);
    WritePrivateProfileString(achPrMSettings, "zslew", ach, achPrFile);
    wsprintf(ach, "%lu", Acquire->ac_MagnetometerDData.dwZPanel);
    WritePrivateProfileString(achPrMSettings, "zpanel", ach, achPrFile);
    WritePrivateProfileString(achPrMSettings, "delay", Acquire->ac_MagnetometerDData.achDelay, achPrFile);

    // Save current sample holder moments

    WritePrivateProfileString(achPrHolder, "x", Acquire->ac_HolderDData.achX, achPrFile);
    WritePrivateProfileString(achPrHolder, "y", Acquire->ac_HolderDData.achY, achPrFile);
    WritePrivateProfileString(achPrHolder, "z", Acquire->ac_HolderDData.achZ, achPrFile);
    WritePrivateProfileString(achPrHolder, "total", Acquire->ac_HolderDData.achTotal, achPrFile);
/*
    wsprintf(ach, "%s", (LPSTR) Acquire->ac_HolderDData.achX);
    WritePrivateProfileString(achPrHolder, "x", ach, achPrFile);
    wsprintf(ach, "%s", (LPSTR) Acquire->ac_HolderDData.achY);
    WritePrivateProfileString(achPrHolder, "y", ach, achPrFile);
    wsprintf(ach, "%s", (LPSTR) Acquire->ac_HolderDData.achZ);
    WritePrivateProfileString(achPrHolder, "z", ach, achPrFile);
    wsprintf(ach, "%s", (LPSTR) Acquire->ac_HolderDData.achTotal);
    WritePrivateProfileString(achPrHolder, "total", ach, achPrFile);
*/
    // Save current standard moments and angle

    WritePrivateProfileString(achPrStnd, "x", Acquire->ac_StandardDData.achX, achPrFile);
    WritePrivateProfileString(achPrStnd, "y", Acquire->ac_StandardDData.achY, achPrFile);
    WritePrivateProfileString(achPrStnd, "z", Acquire->ac_StandardDData.achZ, achPrFile);
    WritePrivateProfileString(achPrStnd, "total", Acquire->ac_StandardDData.achTotal, achPrFile);
    WritePrivateProfileString(achPrStnd, "angle", Acquire->ac_StandardDData.achAngle, achPrFile);
/*
    wsprintf(ach, "%s", (LPSTR) Acquire->ac_StandardDData.achX);
    WritePrivateProfileString(achPrStnd, "x", ach, achPrFile);
    wsprintf(ach, "%s", (LPSTR) Acquire->ac_StandardDData.achY);
    WritePrivateProfileString(achPrStnd, "y", ach, achPrFile);
    wsprintf(ach, "%s", (LPSTR) Acquire->ac_StandardDData.achZ);
    WritePrivateProfileString(achPrStnd, "z", ach, achPrFile);
    wsprintf(ach, "%s", (LPSTR) Acquire->ac_StandardDData.achTotal);
    WritePrivateProfileString(achPrStnd, "total", ach, achPrFile);
    wsprintf(ach, "%s", (LPSTR) Acquire->ac_StandardDData.achAngle);
    WritePrivateProfileString(achPrStnd, "angle", ach, achPrFile);
*/
    // Save current measure options

    WritePrivateProfileString(achPrMOptions, "xyz", Acquire->ac_MeasureDData.achXYZ, achPrFile);
//  WritePrivateProfileString(achPrMOptions, "x", Acquire->ac_MeasureDData.achX, achPrFile);
//  WritePrivateProfileString(achPrMOptions, "y", Acquire->ac_MeasureDData.achY, achPrFile);
//  WritePrivateProfileString(achPrMOptions, "z", Acquire->ac_MeasureDData.achZ, achPrFile);
/*
    wsprintf(ach, "%s", (LPSTR) Acquire->ac_MeasureDData.achX);
    WritePrivateProfileString(achPrMOptions, "x", ach, achPrFile);
    wsprintf(ach, "%s", (LPSTR) Acquire->ac_MeasureDData.achY);
    WritePrivateProfileString(achPrMOptions, "y", ach, achPrFile);
    wsprintf(ach, "%s", (LPSTR) Acquire->ac_MeasureDData.achZ);
    WritePrivateProfileString(achPrMOptions, "z", ach, achPrFile);
*/
    wsprintf(ach, "%lu", Acquire->ac_MeasureDData.dwMin);
    WritePrivateProfileString(achPrMOptions, "min", ach, achPrFile);
    wsprintf(ach, "%lu", Acquire->ac_MeasureDData.dwSingle);
    WritePrivateProfileString(achPrMOptions, "single", ach, achPrFile);
    wsprintf(ach, "%lu", Acquire->ac_MeasureDData.dwMult);
    WritePrivateProfileString(achPrMOptions, "mult", ach, achPrFile);
    WritePrivateProfileString(achPrMOptions, "rot", Acquire->ac_MeasureDData.achMult, achPrFile);
//  wsprintf(ach, "%s", (LPSTR) Acquire->ac_MeasureDData.achMult);
//  WritePrivateProfileString(achPrMOptions, "rot", ach, achPrFile);
    wsprintf(ach, "%lu", Acquire->ac_MeasureDData.dwZ);
    WritePrivateProfileString(achPrMOptions, "+/-z", ach, achPrFile);
    WritePrivateProfileString(achPrMOptions, "S/N", Acquire->ac_MeasureDData.achSN, achPrFile);
//  wsprintf(ach, "%s", (LPSTR) Acquire->ac_MeasureDData.achSN);
//  WritePrivateProfileString(achPrMOptions, "S/N", ach, achPrFile);
    wsprintf(ach, "%lu", Acquire->ac_MeasureDData.dwSNR);
    WritePrivateProfileString(achPrMOptions, "S/N remeas", ach, achPrFile);
    wsprintf(ach, "%lu", Acquire->ac_MeasureDData.dwSNN);
    WritePrivateProfileString(achPrMOptions, "S/N notify", ach, achPrFile);
    WritePrivateProfileString(achPrMOptions, "drift", Acquire->ac_MeasureDData.achDrift, achPrFile);
//  wsprintf(ach, "%s", (LPSTR) Acquire->ac_MeasureDData.achDrift);
//  WritePrivateProfileString(achPrMOptions, "drift", ach, achPrFile);
    wsprintf(ach, "%lu", Acquire->ac_MeasureDData.dwDR);
    WritePrivateProfileString(achPrMOptions, "drift remeas", ach, achPrFile);
    wsprintf(ach, "%lu", Acquire->ac_MeasureDData.dwDN);
    WritePrivateProfileString(achPrMOptions, "drift notify", ach, achPrFile);

    // Save current projection options

/*  wsprintf(ach, "%lu", ProjOptionsDData.dwCore);
    WritePrivateProfileString(achPrPOptions, "core", ach, achPrFile);
    wsprintf(ach, "%lu", ProjOptionsDData.dwInSitu);
    WritePrivateProfileString(achPrPOptions, "in situ", ach, achPrFile);
    wsprintf(ach, "%lu", ProjOptionsDData.dwRotated);
    WritePrivateProfileString(achPrPOptions, "rotated", ach, achPrFile);
*/
    // Close opened serial ports

    if( Acquire->ac_bMagCom && Acquire->ac_nMagComID ) {
      Acquire->ac_nMagComID->Close( Acquire->ac_nMagComID );
      if( Acquire->ac_nAFComID == Acquire->ac_nMagComID )       // If the AF demagnetizer and the magnetometer share a serial port
        Acquire->ac_nAFComID = (SERIALIO *) NULL;
      Acquire->ac_nMagComID = (SERIALIO *) NULL;
      }
    if( Acquire->ac_bAFCom  && Acquire->ac_nAFComID  ) {
      Acquire->ac_nAFComID->Close( Acquire->ac_nAFComID );
      Acquire->ac_nAFComID = (SERIALIO *) NULL;
      }
    if( Acquire->ac_bSHCom  && Acquire->ac_nSHComID  ) {
      Acquire->ac_nSHComID->Close( Acquire->ac_nSHComID );
      Acquire->ac_nSHComID = (SERIALIO *) NULL;
      }
    PostQuitMessage( 0 );
    }
  }


// TMyMDIFrame Class Member

void TMyMDIFrameWMSerialError( ACQUIRE *Acquire, WPARAM wParam, LPARAM lParam ) {

  char achPort[ 128 ], achTitle[ 128 ], achError[ 128 ] = "\0";

  if( Acquire != (ACQUIRE *) NULL ) {

    strcpy( achTitle, "Communications Error" );         // Default title string
    if(      HIWORD( lParam ) == 2U )                   // If error occurred with a read,
      strcpy( achTitle, "Communications Read Error" );  // set up title string
    else if( HIWORD( lParam ) == 1U )                   // If error occurred with a write,
      strcpy( achTitle, "Communications Write Error" ); // set up title string

    if(      LOWORD( lParam ) ==  LOWORD(Acquire->ac_nMagComID ) ) {    // Magnetometer serial port error
      wsprintf(achPort, "%lu", Acquire->ac_ConfigureDData.dwMagCom );   // Set up serial port number
      strcpy( achError, "COM"  );                       // Set up error string
      strcat( achError, achPort);                       // Set up error string
      strcat( achError, "  "   );                       // Set up error string
      }
    else if( LOWORD( lParam ) == LOWORD( Acquire->ac_nAFComID ) ) {     // AF demagnetizer serial port error
      wsprintf(achPort, "%lu", Acquire->ac_ConfigureDData.dwAFCom );    // Set up serial port number
      strcpy( achError, "COM"  );                       // Set up error string
      strcat( achError, achPort);                       // Set up error string
      strcat( achError, "  "   );                       // Set up error string
      }
    else if( LOWORD( lParam ) == LOWORD( Acquire->ac_nSHComID ) ) {     // Sample handler serial port error
      wsprintf( achPort, "%lu", Acquire->ac_ConfigureDData.dwSHCom );   // Set up serial port number
      strcpy( achError, "COM"  );                       // Set up error string
      strcat( achError, achPort);                       // Set up error string
      strcat( achError, "  "   );                       // Set up error string
      }

    switch( wParam ) {  // Concatenate specific error code
      case CE_BREAK:
        strcat( achError, "CE_BREAK" );
        break;
//    case CE_CTSTO:
//      strcat( achError, "CE_CTSTO" );
//      break;
//    case CE_DNS:
//      strcat( achError, "CE_DNS" );
//      break;
//    case CE_DSRTO:
//      strcat( achError, "CE_DSRTO" );
//      break;
      case CE_FRAME:
        strcat( achError, "CE_FRAME" );
        break;
//    case CE_IOE:
//      strcat( achError, "CE_IOE" );
//      break;
      case CE_MODE:
        strcat( achError, "CE_MODE" );
        break;
//    case CE_OOP:
//      strcat( achError, "CE_OOP" );
//      break;
      case CE_OVERRUN:
        if( !strcmp( achError, "\0" ) ) // Open error
          strcpy( achError, "IE_OPEN" );
        else                            // Read/write error
          strcat( achError, "CE_OVERRUN" );
        break;
//    case CE_PTO:
//      strcat( achError, "CE_PTO" );
//      break;
//    case CE_RLSDTO:
//      strcat( achError, "CE_RLSDTO" );
//      break;
      case CE_RXOVER:
        if( !strcmp( achError, "\0" ) ) // Open error
          strcpy( achError, "IE_BADID" );
        else                            // Read/write error
          strcat( achError, "CE_RXOVER" );
        break;
      case CE_RXPARITY:
        if( !strcmp( achError, "\0" ) ) // Open error
          strcpy( achError, "IE_MEMORY" );
        else                            // Read/write error
          strcat( achError, "CE_RXPARITY" );
        break;
      case CE_TXFULL:
        strcat( achError, "CE_TXFULL" );
        break;
      case -IE_NOPEN:
        strcat( achError, "IE_NOPEN" );
        break;
      case -IE_DEFAULT:
        strcat( achError, "IE_DEFAULT" );
        break;
      case -IE_HARDWARE:
        strcat( achError, "IE_HARDWARE" );
        break;
      case -IE_BYTESIZE:
        strcat( achError, "IE_BYTESIZE" );
        break;
      case -IE_BAUDRATE:
        strcat( achError, "IE_BAUDRATE" );
        break;
      default:
        strcat( achError, "UNKNOWN" );
      }

    // Display error

    MessageBox( Acquire->ac_hMainWindow, achError, achTitle, MB_ICONHAND | MB_OK );
    }
  }




void TMyMDIFrameCMFileNew( ACQUIRE *Acquire ) {

  int    nFileCheck = 0;
  int    nCoreAz, nBedAz, nFoldAz, nCorePl, nBedPl, nFoldPl;
  char  *p;
  BOOL   bOverWrite = TRUE;
  time_t now;
  char   path[ 128 ], name[ 128 ], ext[ 128 ];

  // Read current time and date

  now = time( NULL );
  strftime( Acquire->ac_SampleDData.achTime, 18, "%b %d %Y %H:%M", localtime( &now ) );
  if( DialogBoxParam( Acquire->ac_hInstance, MAKEINTRESOURCE( Sample_Dialog ), Acquire->ac_hMainWindow, TSampleDialogProcess, (long) Acquire ) == IDOK ) {

    strcpy( Acquire->ac_achFileOpen, Acquire->ac_SampleDData.achName );                         // Initial caption will be just the sample name with a .dat extension

    if( strstr( Acquire->ac_achFileOpen, ".dat" ) == NULL )
      strcat( Acquire->ac_achFileOpen, ".dat" );

    for( p = Acquire->ac_achFileOpen; *p != '\0'; p++ ) {                                       // For each character in the caption string
      if( isalpha( *p ) && isupper( *p ) )                                                      // If a character is alphabetic and uppercase
        *p = tolower( *p );                                                                     // Convert letter to lowercase
      }

    // Need to fix Acquire->ac_SampleDData.achName so it is just the sample name without the path.
    ParseFileName( path, name, ext, Acquire->ac_SampleDData.achName );
    strcpy( Acquire->ac_SampleDData.achName, name );

    Acquire->ac_FocusFile->CloseIt( Acquire->ac_FocusFile );                                    // Close file focus.tmp
    Acquire->ac_FocusFile->ChangeName( Acquire->ac_FocusFile, Acquire->ac_achFileOpen );        // Update the name of the file to the new caption
    if( Acquire->ac_FocusFile->Exists( Acquire->ac_FocusFile ) ) {                              // If a file in the current directory already exists by that name
      if( MessageBox( Acquire->ac_hMainWindow, "File with that sample name exists!\rOverwrite?", "File New", MB_ICONEXCLAMATION | MB_YESNO ) == IDNO )
        bOverWrite = FALSE;
      }
    Acquire->ac_FocusFile->CloseIt( Acquire->ac_FocusFile );                                    // Close the open disk file
//  Acquire->ac_FocusFile->ChangeName( Acquire->ac_FocusFile, "focus.tmp" );                    // Restore the name of the disk file
    Acquire->ac_FocusFile->ChangeName( Acquire->ac_FocusFile, Acquire->ac_achFocusFile );       // Restore the name of the disk file
    if( bOverWrite ) {                                          // If a new window and a new file are to be created
      CheckOpenFiles( Acquire, &nFileCheck );                   // Compare open file names with the name of the file to be opened
      if( nFileCheck == 0 ) {                                   // If a comparison is not made
        Acquire->ac_bFileNew = TRUE;                            // No existing file (used in WMMDIActivate)
        Acquire->ac_FileSummary.nCurrentStep = 0;               // And therefore no existing measurements
        Acquire->ac_nChildrenCount = 0;
        CountChildren( Acquire, &Acquire->ac_nChildrenCount );  // Count existing child windows

        // Enable menu items which are applicable only if a child window exists

        EnableMenuItem( Acquire->ac_hMainMenu, IDM_FILE_SAVE,    MF_ENABLED | MF_BYCOMMAND );           // File Save
        EnableMenuItem( Acquire->ac_hMainMenu, IDM_FILE_SAVEAS,  MF_ENABLED | MF_BYCOMMAND );           // File Save as
        EnableMenuItem( Acquire->ac_hMainMenu, IDM_FILE_PRINT,   MF_ENABLED | MF_BYCOMMAND );           // File Print
//      EnableMenuItem( Acquire->ac_hMainMenu, IDM_PROJECT_CART, MF_ENABLED | MF_BYCOMMAND );           // Projection Cartesian

#if ONEAXISTEST        
        if( ( Acquire->ac_bMagXAxis || Acquire->ac_bMagYAxis || Acquire->ac_bMagZAxis ) ||
            ( Acquire->ac_bMagZAxis && ( Acquire->ac_bMagXAxis || Acquire->ac_bMagYAxis ) && Acquire->ac_bSHAuto && Acquire->ac_bSHTrans && Acquire->ac_bSHRot ) ||
            ( Acquire->ac_bMagZAxis && ( Acquire->ac_bMagXAxis || Acquire->ac_bMagYAxis ) && Acquire->ac_bSHManual ) ) {
          EnableMenuItem( Acquire->ac_hMainMenu, IDM_MEASURE_SINGLE,   MF_ENABLED | MF_BYCOMMAND );     // Measure Single step
          EnableMenuItem( Acquire->ac_hMainMenu, IDM_MEASURE_HOLDER,   MF_ENABLED | MF_BYCOMMAND );     // Measure Sample holder
          EnableMenuItem( Acquire->ac_hMainMenu, IDM_MEASURE_STANDARD, MF_ENABLED | MF_BYCOMMAND );     // Measure Standard
          }
#else
        if( ( Acquire->ac_bMagXAxis && Acquire->ac_bMagYAxis && Acquire->ac_bMagZAxis ) ||
            ( Acquire->ac_bMagZAxis && ( Acquire->ac_bMagXAxis || Acquire->ac_bMagYAxis ) && Acquire->ac_bSHAuto && Acquire->ac_bSHTrans && Acquire->ac_bSHRot ) ||
            ( Acquire->ac_bMagZAxis && ( Acquire->ac_bMagXAxis || Acquire->ac_bMagYAxis ) && Acquire->ac_bSHManual ) ) {
          EnableMenuItem( Acquire->ac_hMainMenu, IDM_MEASURE_SINGLE,   MF_ENABLED | MF_BYCOMMAND );     // Measure Single step
          EnableMenuItem( Acquire->ac_hMainMenu, IDM_MEASURE_HOLDER,   MF_ENABLED | MF_BYCOMMAND );     // Measure Sample holder
          EnableMenuItem( Acquire->ac_hMainMenu, IDM_MEASURE_STANDARD, MF_ENABLED | MF_BYCOMMAND );     // Measure Standard
          }
#endif

        if( Acquire->ac_bAFAuto && Acquire->ac_bSHAuto && ( ( Acquire->ac_bSHTrans && Acquire->ac_bSHRot && Acquire->ac_bAFZAxis && ( Acquire->ac_bAFXAxis || Acquire->ac_bAFYAxis ) ) ||
          ( Acquire->ac_bSHTrans && Acquire->ac_bAFXAxis && Acquire->ac_bAFYAxis && Acquire->ac_bAFZAxis && Acquire->ac_bMagXAxis && Acquire->ac_bMagYAxis && Acquire->ac_bMagZAxis) ) )
          EnableMenuItem( Acquire->ac_hMainMenu, IDM_MEASURE_SEQUENCE, MF_ENABLED | MF_BYCOMMAND );     // Measure Sequence

        // Convert orientation data

        nCoreAz = atoi( Acquire->ac_SampleDData.achCoreAz   ) - atoi( Acquire->ac_OrientDData.achCoreAz   );    // Core azimuth correction
        nBedAz  = atoi( Acquire->ac_SampleDData.achBeddingAz) - atoi( Acquire->ac_OrientDData.achBeddingAz);    // Bedding azimuth correction
        nFoldAz = atoi( Acquire->ac_SampleDData.achFoldAz   ) - atoi( Acquire->ac_OrientDData.achFoldAz   );    // Plunging fold azimuth correction
        nCoreAz = NThreeSixty( nCoreAz );
        nBedAz  = NThreeSixty( nBedAz  );
        nFoldAz = NThreeSixty( nFoldAz );
        wsprintf( Acquire->ac_SampleDData.achCoreAz,    "%d", nCoreAz );
        wsprintf( Acquire->ac_SampleDData.achBeddingAz, "%d", nBedAz  );
        wsprintf( Acquire->ac_SampleDData.achFoldAz,    "%d", nFoldAz );
        nCorePl = atoi( Acquire->ac_SampleDData.achCoreP   );   // Core plunge
        nBedPl  = atoi( Acquire->ac_SampleDData.achBeddingP);   // Bedding plunge
        nFoldPl = atoi( Acquire->ac_SampleDData.achFoldP   );   // Plunging fold plunge

        if( Acquire->ac_OrientDData.dwCorePComp     )           // Complement
          nCorePl = Complement( nCorePl );
        if( Acquire->ac_OrientDData.dwBeddingPComp  )           // Complement
          nBedPl  = Complement( nBedPl  );
        if( Acquire->ac_OrientDData.dwFoldPComp     )           // Complement
          nFoldPl = Complement( nFoldPl );
        if( Acquire->ac_OrientDData.dwCorePSign     )           // Sign
          nCorePl = ChangeSign( nCorePl );
        if( Acquire->ac_OrientDData.dwBeddingPSign  )           // Sign
          nBedPl  = ChangeSign( nBedPl  );
        if( Acquire->ac_OrientDData.dwFoldPSign     )           // Sign
          nFoldPl = ChangeSign( nFoldPl );

        wsprintf( Acquire->ac_SampleDData.achCoreP,    "%d", nCorePl );
        wsprintf( Acquire->ac_SampleDData.achBeddingP, "%d", nBedPl  );
        wsprintf( Acquire->ac_SampleDData.achFoldP,    "%d", nFoldPl );
        OpenChildWindow( Acquire, Acquire->ac_achFileOpen );    // Create a new child window
        }
      else      // If a comparison is made
        MessageBox( Acquire->ac_hMainWindow, Acquire->ac_achFileOpen, "File Is Opened", MB_ICONEXCLAMATION | MB_OK );    // Notify user
      }
    }
  }


// Open a file which has already been created

void TMyMDIFrameCMFileOpen( ACQUIRE *Acquire ) {

  int nRetVal, nFileCheck = 0;
//  TFileDialog *ptFileOpen;

    _fstrcpy( Acquire->ac_achFileOpen, ".dat" );                        // Provide an initial file name and path
    nRetVal = GetFileRequestorLoad( Acquire->ac_achFileOpen, "dat", "Data Files" );

  // Respond to user input

  if( nRetVal == IDOK ) {                                               // If OK button pushed
    OemToAnsi( Acquire->ac_achFileOpen, Acquire->ac_achFileOpen );      // Convert the open file name string from oem to ansi characters
    CheckOpenFiles( Acquire, &nFileCheck );                             // Compare open file names with the name of the file to be opened
    if( nFileCheck == 0 ) {                                             // If a comparison is not made
      Acquire->ac_FocusFile->CloseIt(    Acquire->ac_FocusFile );                               // Close the focus.tmp disk file
      Acquire->ac_FocusFile->ChangeName( Acquire->ac_FocusFile, Acquire->ac_achFileOpen );      // Update the name of the file to that just chosen
      Acquire->ac_FocusFile->OpenIt(     Acquire->ac_FocusFile );                               // Open the file just chosen
      Acquire->ac_FocusFile->Load(       Acquire->ac_FocusFile, "-1" );                         // Load the specimen summary record
      Acquire->ac_FocusFile->CopyFrom(   Acquire->ac_FocusFile, (LPSTR) &Acquire->ac_FileSummary, sizeof( SPECIMENSUMMARY ) );  // Read in specimen summary data
      Acquire->ac_FocusFile->Load(       Acquire->ac_FocusFile, "0" );                          // Load the sample information record
      Acquire->ac_FocusFile->CopyFrom(   Acquire->ac_FocusFile, (LPSTR) &Acquire->ac_SampleDData, sizeof( INFO ) );             // Read in sample information data
      Acquire->ac_FocusFile->CloseIt(    Acquire->ac_FocusFile );                               // Close the open disk file
//    Acquire->ac_FocusFile->ChangeName( Acquire->ac_FocusFile, "focus.tmp" );                  // Restore the name of the disk file
      Acquire->ac_FocusFile->ChangeName( Acquire->ac_FocusFile, Acquire->ac_achFocusFile );     // Restore the name of the disk file
      Acquire->ac_bFileOpen = TRUE;                                                             // Existing file (used in WMMDIActivate)
      Acquire->ac_nChildrenCount = 0;
      CountChildren( Acquire, &Acquire->ac_nChildrenCount );                                    // Count existing child windows

      // Enable menu items which are applicable only if a child window exists

      EnableMenuItem( Acquire->ac_hMainMenu, IDM_FILE_SAVE,    MF_ENABLED | MF_BYCOMMAND );             // File Save
      EnableMenuItem( Acquire->ac_hMainMenu, IDM_FILE_SAVEAS,  MF_ENABLED | MF_BYCOMMAND );             // File Save as
      EnableMenuItem( Acquire->ac_hMainMenu, IDM_FILE_PRINT,   MF_ENABLED | MF_BYCOMMAND );             // File Print
//    EnableMenuItem( Acquire->ac_hMainMenu, IDM_PROJECT_CART, MF_ENABLED | MF_BYCOMMAND );             // Projection Cartesian

      if( ( Acquire->ac_bMagXAxis && Acquire->ac_bMagYAxis && Acquire->ac_bMagZAxis ) ||
        ( Acquire->ac_bMagZAxis && ( Acquire->ac_bMagXAxis || Acquire->ac_bMagYAxis ) && Acquire->ac_bSHAuto && Acquire->ac_bSHTrans && Acquire->ac_bSHRot ) ||
        ( Acquire->ac_bMagZAxis && ( Acquire->ac_bMagXAxis || Acquire->ac_bMagYAxis ) && Acquire->ac_bSHManual ) ) {
        EnableMenuItem( Acquire->ac_hMainMenu, IDM_MEASURE_SINGLE,   MF_ENABLED | MF_BYCOMMAND );       // Measure Single step
        EnableMenuItem( Acquire->ac_hMainMenu, IDM_MEASURE_HOLDER,   MF_ENABLED | MF_BYCOMMAND );       // Measure Sample holder
        EnableMenuItem( Acquire->ac_hMainMenu, IDM_MEASURE_STANDARD, MF_ENABLED | MF_BYCOMMAND );       // Measure Standard
        }
      if( Acquire->ac_bAFAuto && Acquire->ac_bSHAuto &&
        ( ( Acquire->ac_bSHTrans && Acquire->ac_bSHRot && Acquire->ac_bAFZAxis && ( Acquire->ac_bAFXAxis || Acquire->ac_bAFYAxis ) ) ||
        ( Acquire->ac_bSHTrans && Acquire->ac_bAFXAxis && Acquire->ac_bAFYAxis && Acquire->ac_bAFZAxis && Acquire->ac_bMagXAxis && Acquire->ac_bMagYAxis && Acquire->ac_bMagZAxis ) ) )
        EnableMenuItem( Acquire->ac_hMainMenu, IDM_MEASURE_SEQUENCE, MF_ENABLED | MF_BYCOMMAND );       // Measure Sequence
      OpenChildWindow( Acquire, Acquire->ac_achFileOpen );                                              // Create a new child window
      }
    else // If a comparison is made
      MessageBox( Acquire->ac_hMainWindow, Acquire->ac_achFileOpen, "File Is Opened", MB_ICONEXCLAMATION | MB_OK );     // Notify user
    }
  }


// Setup printer

void TMyMDIFrameCMPrnSetup( ACQUIRE *Acquire ) {

  GetInitializationData( Acquire, Acquire->ac_hMainWindow );
  }


// Configure instrument

void TMyMDIFrameCMConfigure( ACQUIRE *Acquire ) {

  // Reconfirm configuration change

  if( MessageBox( Acquire->ac_hMainWindow, "Are you sure?", "Configure Instrument", MB_ICONEXCLAMATION | MB_YESNO ) == IDYES ) {
    Acquire->ac_TemporaryDData.dwX = Acquire->ac_ConfigureDData.dwX;                    // Initialize configuration temporary variables
    Acquire->ac_TemporaryDData.dwY = Acquire->ac_ConfigureDData.dwY;
    Acquire->ac_TemporaryDData.dwZ = Acquire->ac_ConfigureDData.dwZ;
//  memset( &Acquire->ac_TemporaryDData, 0, sizeof( Acquire->ac_TemporaryDData ) );     // Clear temporary configure data structure

    // Start configure dialog box

    if( DialogBoxParam( Acquire->ac_hInstance, MAKEINTRESOURCE( Configure_Dialog ), Acquire->ac_hMainWindow, TConfigureDialogProcess, (long) Acquire ) == IDOK ) {
      PostQuitMessage( 0 );
      }
    }
  }


// TMyMDIFrame Class Member - Exit program

void TMyMDIFrameCMQuit( HWND hWindow ) {

//  if( MessageBox( hWindow, "Are you sure?", "Exit Program", MB_ICONEXCLAMATION | MB_YESNO ) == IDYES )
////    DestroyWindow( hWindow );
  PostQuitMessage( 0 );
  }


// Set magnetometers

void TMyMDIFrameCMMagSettings( ACQUIRE *Acquire, HWND hWindow ) {

  // If the dialog box is closed with an Ok

  if( DialogBoxParam( Acquire->ac_hInstance, MAKEINTRESOURCE( Magnetometer_Dialog ), Acquire->ac_hMainWindow, TMagSettingsDialogProcess, (long) Acquire ) == IDOK ) {

    if( Acquire->ac_bMagXAxis ) {       // If the magnetometer is configured with an X axis
      MagSetRange( hWindow, Acquire->ac_nMagComID, "X", Acquire->ac_MagnetometerDData.dwXRange );       // Range
      MagSetFilter(hWindow, Acquire->ac_nMagComID, "X", Acquire->ac_MagnetometerDData.dwXFilter);       // Filter
      MagSetSlew(  hWindow, Acquire->ac_nMagComID, "X", Acquire->ac_MagnetometerDData.dwXSlew  );       // Slew
//      MagSetPanel( hWindow, Acquire->ac_nMagComID, "X", Acquire->ac_MagnetometerDData.dwXPanel );       // Front panel lock

      // Flux counting

      if( Acquire->ac_MagnetometerDData.dwXFlux )
        Acquire->ac_bXFlux = TRUE;
      else
        Acquire->ac_bXFlux = FALSE;
      }

    if( Acquire->ac_bMagYAxis ) {       // If the magnetometer is configured with a Y axis
      MagSetRange( hWindow, Acquire->ac_nMagComID, "Y", Acquire->ac_MagnetometerDData.dwYRange );       // Range
      MagSetFilter(hWindow, Acquire->ac_nMagComID, "Y", Acquire->ac_MagnetometerDData.dwYFilter);       // Filter
      MagSetSlew(  hWindow, Acquire->ac_nMagComID, "Y", Acquire->ac_MagnetometerDData.dwYSlew  );       // Slew
//      MagSetPanel( hWindow, Acquire->ac_nMagComID, "Y", Acquire->ac_MagnetometerDData.dwYPanel );       // Front panel lock

      // Flux counting

      if( Acquire->ac_MagnetometerDData.dwYFlux )
        Acquire->ac_bYFlux = TRUE;
      else
        Acquire->ac_bYFlux = FALSE;
      }

    if( Acquire->ac_bMagZAxis ) {       // If the magnetometer is configured with a Z axis
      MagSetRange( hWindow, Acquire->ac_nMagComID, "Z", Acquire->ac_MagnetometerDData.dwZRange );       // Range
      MagSetFilter(hWindow, Acquire->ac_nMagComID, "Z", Acquire->ac_MagnetometerDData.dwZFilter);       // Filter
      MagSetSlew(  hWindow, Acquire->ac_nMagComID, "Z", Acquire->ac_MagnetometerDData.dwZSlew  );       // Slew
//      MagSetPanel( hWindow, Acquire->ac_nMagComID, "Z", Acquire->ac_MagnetometerDData.dwZPanel );       // Front panel lock

      // Flux counting

      if( Acquire->ac_MagnetometerDData.dwZFlux )
        Acquire->ac_bZFlux = TRUE;
      else
        Acquire->ac_bZFlux = FALSE;
      }
    MagPulseLoop( hWindow, Acquire->ac_nMagComID, "A" );        // Pulse-reset feedback loop for all axes
    MagResetCount(hWindow, Acquire->ac_nMagComID, "A" );        // Clear flux counter for all axes
    Acquire->ac_dwSettlingDelay = (DWORD) atol( Acquire->ac_MagnetometerDData.achDelay );       // Magnetometer settling time delay in ms
    }
  }


// TMyMDIFrame Class Member - Move sample handler

void TMyMDIFrameCMSHMove( ACQUIRE *Acquire, HWND hWindow ) {

  if( ( Acquire != (ACQUIRE *) NULL ) &&
      ( hWindow != (HWND)      NULL ) ) {

    if( DialogBoxParam( Acquire->ac_hInstance, MAKEINTRESOURCE( SampleHandlerMove_Dialog ), Acquire->ac_hMainWindow, TSHMoveDialogProcess, (long) Acquire ) == IDOK ) { // If the dialog box is closed with an Ok

      // Rotate

      if( Acquire->ac_HandlerDData.dwRot ) {                    // Jamie Added For Bill.
        switch( Acquire->ac_SHMoveDData.dwRot ) {               // Identify position
          case 0UL:                                             // Home
            Acquire->ac_bRHome  = Rotate( Acquire, hWindow, Acquire->ac_bRHome,Acquire->ac_nRHome );
            break;
          case 1UL:                                             // 90
            Acquire->ac_bR90    = Rotate( Acquire, hWindow, Acquire->ac_bR90,  Acquire->ac_nR90 );
            break;
          case 2UL:                                             // 180
            Acquire->ac_bR180   = Rotate( Acquire, hWindow, Acquire->ac_bR180, Acquire->ac_nR180 );
            break;
          case 3UL:                                             // 270
            Acquire->ac_bR270   = Rotate( Acquire, hWindow, Acquire->ac_bR270, Acquire->ac_nR270 );
            break;
          default:                                              // Home
            Acquire->ac_bRHome  = Rotate( Acquire, hWindow, Acquire->ac_bRHome,Acquire->ac_nRHome );
            }
          }

      // Translate

      if( Acquire->ac_HandlerDData.dwTrans ) {                  // Jamie Added For Bill.
        switch( Acquire->ac_SHMoveDData.dwTrans ) {             // Identify position
          case 0UL:                                             // Home
            Acquire->ac_bTHome  = Translate( Acquire, hWindow, Acquire->ac_bTHome, Acquire->ac_lTHome );
            break;
          case 1UL:                                             // Load
            Acquire->ac_bTLoad  = Translate( Acquire, hWindow, Acquire->ac_bTLoad, Acquire->ac_lTLoad );
            break;
          case 2UL:                                             // Background
            Acquire->ac_bTBack  = Translate( Acquire, hWindow, Acquire->ac_bTBack, Acquire->ac_lTBack );
            break;
          case 3UL:                                             // Measurement
            Acquire->ac_bTMeas  = Translate( Acquire, hWindow, Acquire->ac_bTMeas, Acquire->ac_lTMeas );
            break;
          case 4UL:                                             // Right limit
            Acquire->ac_bTRight = Translate( Acquire, hWindow, Acquire->ac_bTRight,Acquire->ac_lTRight );
            break;
          case 5UL:                                             // Left limit
            Acquire->ac_bTLeft  = Translate( Acquire, hWindow, Acquire->ac_bTLeft, Acquire->ac_lTLeft );
            break;
          case 6UL:                                             // AF Z axis, AF Y axis, or AF X axis
            if( Acquire->ac_bAFZAxis )                          // AF Z axis
              Acquire->ac_bTAFZ = Translate( Acquire, hWindow, Acquire->ac_bTAFZ,  Acquire->ac_lTAFZ );
            if( !Acquire->ac_bAFZAxis && Acquire->ac_bAFYAxis ) // AF Y axis
              Acquire->ac_bTAFY = Translate( Acquire, hWindow, Acquire->ac_bTAFY,  Acquire->ac_lTAFY );
           if( !Acquire->ac_bAFZAxis && !Acquire->ac_bAFYAxis ) // AF X axis
              Acquire->ac_bTAFX = Translate( Acquire, hWindow, Acquire->ac_bTAFX,  Acquire->ac_lTAFX );
            break;
          case 7UL:                                             // AF Y axis or AF X axis
            if( Acquire->ac_bAFYAxis && Acquire->ac_bAFZAxis )  // AF Y axis
              Acquire->ac_bTAFY = Translate( Acquire, hWindow, Acquire->ac_bTAFY,  Acquire->ac_lTAFY );
            else                                                // AF X axis
             Acquire->ac_bTAFX = Translate( Acquire, hWindow, Acquire->ac_bTAFX,  Acquire->ac_lTAFX );
            break;
          case 8UL:                                             // AF X axis
            Acquire->ac_bTAFX   = Translate( Acquire, hWindow, Acquire->ac_bTAFX,  Acquire->ac_lTAFX );
            break;
          default:                                              // Home
            Acquire->ac_bTHome  = Translate( Acquire, hWindow, Acquire->ac_bTHome, Acquire->ac_lTHome );
          }
        }
      }
    }
  }


// Measurement options

void TMyMDIFrameCMOptions( ACQUIRE *Acquire ) {

  if( Acquire != (ACQUIRE *) NULL ) {

    // Start dialog box

    if( DialogBoxParam( Acquire->ac_hInstance, MAKEINTRESOURCE( Measure_Dialog ), Acquire->ac_hMainWindow, TOptionsDialogProcess, (long) Acquire ) == IDOK ) {

      // Clear measure options flags

      Acquire->ac_bMinMeasure     = FALSE;      // Minimum required readings per sample
      Acquire->ac_bSingleRotation = FALSE;      // Single rotation per sample
      Acquire->ac_bMultRotation   = FALSE;      // Multiple rotations per sample
      Acquire->ac_bMeasureNegZ    = FALSE;      // Measure -Z
      Acquire->ac_bStoNRemeasure  = FALSE;      // Remeasure if S/N threshold is not met
      Acquire->ac_bStoNNotify     = FALSE;      // Notify if S/N threshold is not met
      Acquire->ac_bDriftRemeasure = FALSE;      // Remeasure if Drift threshold is not met
      Acquire->ac_bDriftNotify    = FALSE;      // Notify if Drift threshold is not met

      // Update measure options flags

      /*
      if( Acquire->ac_MeasureDData.dwMin   )
        Acquire->ac_bMinMeasure = TRUE;
      if( Acquire->ac_MeasureDData.dwSingle)
        Acquire->ac_bSingleRotation = TRUE;
      if( Acquire->ac_MeasureDData.dwMult  )
        Acquire->ac_bMultRotation = TRUE;
    */

      if( Acquire->ac_MeasureDData.dwMult )
        Acquire->ac_bMultRotation = TRUE;
      else if( Acquire->ac_MeasureDData.dwSingle )
        Acquire->ac_bSingleRotation = TRUE;
      else
        Acquire->ac_bMinMeasure = TRUE;

      if( Acquire->ac_MeasureDData.dwZ )        // Measure -Z
        Acquire->ac_bMeasureNegZ = TRUE;
      if( Acquire->ac_MeasureDData.dwSNR )
        Acquire->ac_bStoNRemeasure = TRUE;

      //if( MeasureDData.dwSNN )
      else
        Acquire->ac_bStoNNotify = TRUE;
      if( Acquire->ac_MeasureDData.dwDR )
        Acquire->ac_bDriftRemeasure = TRUE;
      //if( Acquire->ac_MeasureDData.dwDN )
      else
        Acquire->ac_bDriftNotify = TRUE;

      // Update measure options values

      Acquire->ac_nXYZAxisReadings = atoi( Acquire->ac_MeasureDData.achXYZ  );  // Analog data readings
//    Acquire->ac_nXAxisReadings   = atoi( Acquire->ac_MeasureDData.achX    );  // Analog data readings per axis
//    Acquire->ac_nYAxisReadings   = atoi( Acquire->ac_MeasureDData.achY    );
//    Acquire->ac_nZAxisReadings   = atoi( Acquire->ac_MeasureDData.achZ    );
      Acquire->ac_nMultRotations   = atoi( Acquire->ac_MeasureDData.achMult );  // Rotations per sample
      Acquire->ac_dStoNThreshold   = atof( Acquire->ac_MeasureDData.achSN   );  // Thresholds
      Acquire->ac_dDriftThreshold  = atof( Acquire->ac_MeasureDData.achDrift);
      }
    }
  }


// Construct the frame window object

TMyMDIFrameTMyMDIFrame( ACQUIRE *Acquire, char *ATitle ) {

  int nx, ny, ncx, ncy; //, i;
//  char ach[ 2 ];

  // Determine directory and name for temporary files

  TemporaryDirectory( Acquire->ac_achFocusFile, sizeof( Acquire->ac_achFocusFile ) );   // Create temporary focus file name

  strcat( Acquire->ac_achFocusFile, "focus1.tmp" );
  Acquire->ac_FocusFile->ChangeName( Acquire->ac_FocusFile, Acquire->ac_achFocusFile ); // Establish temporary file

  // Make sure that the focus.tmp file isn't write protected.

  SetFileAttributes( Acquire->ac_achFocusFile, FILE_ATTRIBUTE_NORMAL );

  // Set window size and location

  nx  =  5;                                       // x coordinate of top left corner
  ny  = 70;                                       // y coordinate of top left corner
  ncx = GetSystemMetrics( SM_CXSCREEN ) - 10;     // Get screen width in pixels
  ncy = GetSystemMetrics( SM_CYSCREEN ) - 70 - 5; // Get screen height in pixels
  Acquire->ac_MainWindowX     = nx;               // Load values into TWindowAttr structure
  Acquire->ac_MainWindowY     = ny;
  Acquire->ac_MainWindowWidth = ncx;
  Acquire->ac_MainWindowHeight= ncy;

  // Zero data structures

  memset( &Acquire->ac_SampleDData,       0x0, sizeof( Acquire->ac_SampleDData       ) );       // Clear Sample Information data structure
  memset( &Acquire->ac_ConfigureDData,    0x0, sizeof( Acquire->ac_ConfigureDData    ) );       // Clear Configure Instrument data structure
  memset( &Acquire->ac_OrientDData,       0x0, sizeof( Acquire->ac_OrientDData       ) );       // Clear Core and Structure Orientation Conventions data structure
  memset( &Acquire->ac_ConstantsDData,    0x0, sizeof( Acquire->ac_ConstantsDData    ) );       // Clear Magnetometer Calibration Constants data structure
  memset( &Acquire->ac_DemagDData,        0x0, sizeof( Acquire->ac_DemagDData        ) );       // Clear AF Demagnetizer Parameters data structure
  memset( &Acquire->ac_HandlerDData,      0x0, sizeof( Acquire->ac_HandlerDData      ) );       // Clear Sample Handler Parameters and Positions data structure
  memset( &Acquire->ac_MagnetometerDData, 0x0, sizeof( Acquire->ac_MagnetometerDData ) );       // Clear Magnetometer Settings data structure
  memset( &Acquire->ac_SHMoveDData,       0x0, sizeof( Acquire->ac_SHMoveDData       ) );       // Clear Sample Handler Move data structure
  memset( &Acquire->ac_SingleDData,       0x0, sizeof( Acquire->ac_SingleDData       ) );       // Clear Measure Single Step data structure
  memset( &Acquire->ac_SequenceDData,     0x0, sizeof( Acquire->ac_SequenceDData     ) );       // Clear Measure Sequence data structure
  memset( &Acquire->ac_HolderDData,       0x0, sizeof( Acquire->ac_HolderDData       ) );       // Clear Measure Sample Holder data structure
  memset( &Acquire->ac_StandardDData,     0x0, sizeof( Acquire->ac_StandardDData     ) );       // Clear Measure Standard data structure
  memset( &Acquire->ac_MeasureDData,      0x0, sizeof( Acquire->ac_MeasureDData      ) );       // Clear Measure Options data structure
//memset( &Acquire->ac_ProjOptionsDData,  0x0, sizeof( Acquire->ac_ProjOptionsDData  ) );       // Clear Projection Options data structure
  memset( &Acquire->ac_FileData,          0x0, sizeof( Acquire->ac_FileData          ) );       // Clear disk file measurement data structure
  memset( &Acquire->ac_FileSummary,       0x0, sizeof( Acquire->ac_FileSummary       ) );       // Clear disk file measurement summary structure

  // Null pointers to modeless dialog box objects

  Acquire->ac_CancelDialog  = (HWND) NULL;      // Cancel measurement cycle dialog box is not active
  Acquire->ac_RangeUpDialog = (HWND) NULL;      // Magnetometer automatic range up dialog box is not active

  // Clear configuration flags

  Acquire->ac_bMagXAxis       = FALSE;      // Magnetometer axes
  Acquire->ac_bMagYAxis       = FALSE;
  Acquire->ac_bMagZAxis       = FALSE;
  Acquire->ac_bDCSquids       = FALSE;      // DC squids
  Acquire->ac_bSHAuto         = FALSE;      // Sample handler
  Acquire->ac_bSHManual       = FALSE;
  Acquire->ac_bAFAuto         = FALSE;      // AF demagnetizer
  Acquire->ac_bAFManual       = FALSE;
  Acquire->ac_bAFNone         = FALSE;
  Acquire->ac_bAFXAxis        = FALSE;      // AF demagnetizer axes
  Acquire->ac_bAFYAxis        = FALSE;
  Acquire->ac_bAFZAxis        = FALSE;
  Acquire->ac_bSHTrans        = FALSE;      // Sample handler axes
  Acquire->ac_bSHRot          = FALSE;
  Acquire->ac_bXFlux          = FALSE;      // Flux counting
  Acquire->ac_bYFlux          = FALSE;
  Acquire->ac_bZFlux          = FALSE;
  Acquire->ac_bMinMeasure     = FALSE;      // Measure options
  Acquire->ac_bSingleRotation = FALSE;
  Acquire->ac_bMultRotation   = FALSE;
  Acquire->ac_bMeasureNegZ    = FALSE;
  Acquire->ac_bStoNRemeasure  = FALSE;
  Acquire->ac_bStoNNotify     = FALSE;
  Acquire->ac_bDriftRemeasure = FALSE;
  Acquire->ac_bDriftNotify    = FALSE;

  // Clear serial port flags

  Acquire->ac_bMagCom = FALSE;
  Acquire->ac_bAFCom  = FALSE;
  Acquire->ac_bSHCom  = FALSE;

  // Clear sample handler position flags

  Acquire->ac_bTHome  = FALSE;
  Acquire->ac_bTLoad  = FALSE;
  Acquire->ac_bTAFX   = FALSE;
  Acquire->ac_bTAFY   = FALSE;
  Acquire->ac_bTAFZ   = FALSE;
  Acquire->ac_bTBack  = FALSE;
  Acquire->ac_bTMeas  = FALSE;
  Acquire->ac_bTRight = FALSE;
  Acquire->ac_bTLeft  = FALSE;
  Acquire->ac_bRHome  = FALSE;
  Acquire->ac_bR90    = FALSE;
  Acquire->ac_bR180   = FALSE;
  Acquire->ac_bR270   = FALSE;

  // Clear okay and cancel measurement cycle flags

//Acquire->ac_bOkCycle     = FALSE;
  Acquire->ac_bCancelCycle = FALSE;

  // Clear new and open file flags

  Acquire->ac_bFileNew  = FALSE;
  Acquire->ac_bFileOpen = FALSE;

  // Initialize paint flags

  Acquire->ac_bFilePaint    = TRUE;
  Acquire->ac_bMeasurePaint = FALSE;

  // Initialize variables

  strcpy( Acquire->ac_SampleDData.achSize,      "1" );      // Sample information and orientation data
  strcpy( Acquire->ac_SampleDData.achCoreAz,    "0" );
  strcpy( Acquire->ac_SampleDData.achCoreP,     "0" );
  strcpy( Acquire->ac_SampleDData.achBeddingAz, "0" );
  strcpy( Acquire->ac_SampleDData.achBeddingP,  "0" );
  strcpy( Acquire->ac_SampleDData.achFoldAz,    "0" );
  strcpy( Acquire->ac_SampleDData.achFoldP,     "0" );
  strcpy( Acquire->ac_SampleDData.achMagDecl,   "0" );

  Acquire->ac_ConfigureDData.dwX        = GetPrivateProfileInt( achPrMConfig, "x",         0, achPrFile );      // Magnetometer configuration
  Acquire->ac_ConfigureDData.dwY        = GetPrivateProfileInt( achPrMConfig, "y",         0, achPrFile );
  Acquire->ac_ConfigureDData.dwZ        = GetPrivateProfileInt( achPrMConfig, "z",         0, achPrFile );
  Acquire->ac_ConfigureDData.dwDCSquids = GetPrivateProfileInt( achPrMConfig, "dc",        0, achPrFile );

  Acquire->ac_ConfigureDData.dwSHAuto   = GetPrivateProfileInt( achPrSHConfig, "auto",     0, achPrFile );      // Sample handler configuration
  Acquire->ac_ConfigureDData.dwSHMan    = GetPrivateProfileInt( achPrSHConfig, "manual",   1, achPrFile );

  Acquire->ac_ConfigureDData.dwAFAuto   = GetPrivateProfileInt( achPrAFConfig, "auto",     0, achPrFile );      // AF demagnetizer configuration
  Acquire->ac_ConfigureDData.dwAFMan    = GetPrivateProfileInt( achPrAFConfig, "manual",   0, achPrFile );
  Acquire->ac_ConfigureDData.dwAFNone   = GetPrivateProfileInt( achPrAFConfig, "none",     1, achPrFile );

  Acquire->ac_ConfigureDData.dwMagCom   = GetPrivateProfileInt( achPrRSConfig, "Mag port", 0, achPrFile );      // RS-232 port configuration
  Acquire->ac_ConfigureDData.dwAFCom    = GetPrivateProfileInt( achPrRSConfig, "AF port",  0, achPrFile );
  Acquire->ac_ConfigureDData.dwSHCom    = GetPrivateProfileInt( achPrRSConfig, "SH port",  0, achPrFile );

  GetPrivateProfileString( achPrMCalib, "x", "0.000e-00", Acquire->ac_ConstantsDData.achX, 11, achPrFile );     // Magnetometer calibration constants
  GetPrivateProfileString( achPrMCalib, "y", "0.000e-00", Acquire->ac_ConstantsDData.achY, 11, achPrFile );
  GetPrivateProfileString( achPrMCalib, "z", "0.000e-00", Acquire->ac_ConstantsDData.achZ, 11, achPrFile );

  Acquire->ac_DemagDData.dwX = GetPrivateProfileInt( achPrAFCalib, "x", 0, achPrFile );                         // AF demagnetizer operation parameters
  Acquire->ac_DemagDData.dwY = GetPrivateProfileInt( achPrAFCalib, "y", 0, achPrFile );
  Acquire->ac_DemagDData.dwZ = GetPrivateProfileInt( achPrAFCalib, "z", 0, achPrFile );
  Acquire->ac_DemagDData.dw2G601S = GetPrivateProfileInt( achPrAFCalib, "2G601S", 0, achPrFile );
  Acquire->ac_DemagDData.dw2G601T = GetPrivateProfileInt( achPrAFCalib, "2G601T", 0, achPrFile );
  GetPrivateProfileString(achPrAFCalib, "field", "0", Acquire->ac_DemagDData.achField, 5, achPrFile );
  Acquire->ac_DemagDData.dwRamp   = GetPrivateProfileInt( achPrAFCalib, "ramp",   0, achPrFile );
  Acquire->ac_DemagDData.dwDelay  = GetPrivateProfileInt( achPrAFCalib, "delay",  0, achPrFile );

//Acquire->ac_HandlerDData.dw2G810  = GetPrivateProfileInt( achPrSHCalib, "2G810",   0, achPrFile );        // Sample handler operation parameters
//Acquire->ac_HandlerDData.dw2G811  = GetPrivateProfileInt( achPrSHCalib, "2G811",   0, achPrFile );
  Acquire->ac_HandlerDData.dwTrans  = GetPrivateProfileInt( achPrSHCalib, "trans",   0, achPrFile );
  Acquire->ac_HandlerDData.dwRot    = GetPrivateProfileInt( achPrSHCalib, "rot",     0, achPrFile );
//Acquire->ac_HandlerDData.dwFlip   = GetPrivateProfileInt( achPrSHCalib, "flip",    0, achPrFile );
//Acquire->ac_HandlerDData.dwNoFlip = GetPrivateProfileInt( achPrSHCalib, "no flip", 0, achPrFile );

  GetPrivateProfileString( achPrSHCalib, "accelRot", "0", Acquire->ac_HandlerDData.achRotAccel,  4, achPrFile );
  GetPrivateProfileString( achPrSHCalib, "decelRot", "0", Acquire->ac_HandlerDData.achRotDecel,  4, achPrFile );
  GetPrivateProfileString( achPrSHCalib, "velRot",   "0", Acquire->ac_HandlerDData.achRotVel,    6, achPrFile );
  
  GetPrivateProfileString( achPrSHCalib, "accel",    "0", Acquire->ac_HandlerDData.achAccel,     4, achPrFile );
  GetPrivateProfileString( achPrSHCalib, "decel",    "0", Acquire->ac_HandlerDData.achDecel,     4, achPrFile );
  GetPrivateProfileString( achPrSHCalib, "vel",      "0", Acquire->ac_HandlerDData.achVel,       6, achPrFile );

  GetPrivateProfileString( achPrSHCalib, "vel meas", "0", Acquire->ac_HandlerDData.achVelM,      6, achPrFile );
  GetPrivateProfileString( achPrSHCalib, "x AF",     "0", Acquire->ac_HandlerDData.achPosXAF,    7, achPrFile );
  GetPrivateProfileString( achPrSHCalib, "y AF",     "0", Acquire->ac_HandlerDData.achPosYAF,    7, achPrFile );
  GetPrivateProfileString( achPrSHCalib, "z AF",     "0", Acquire->ac_HandlerDData.achPosZAF,    7, achPrFile );
  GetPrivateProfileString( achPrSHCalib, "load",     "0", Acquire->ac_HandlerDData.achPosLoad,   7, achPrFile );
  GetPrivateProfileString( achPrSHCalib, "back",     "0", Acquire->ac_HandlerDData.achPosBack,   7, achPrFile );
  GetPrivateProfileString( achPrSHCalib, "meas",     "0", Acquire->ac_HandlerDData.achPosMeas,   7, achPrFile );
  GetPrivateProfileString( achPrSHCalib, "counts",   "0", Acquire->ac_HandlerDData.achRotCounts, 6, achPrFile );
  Acquire->ac_HandlerDData.dwRight = GetPrivateProfileInt( achPrSHCalib, "right", 0, achPrFile );               // Right limit switch

  GetPrivateProfileString( achPrOrient, "core az", "0", Acquire->ac_OrientDData.achCoreAz,    5, achPrFile );   // Core and structure orientation conventions
  Acquire->ac_OrientDData.dwCorePComp    = GetPrivateProfileInt( achPrOrient, "core pl comp", 0, achPrFile );
  Acquire->ac_OrientDData.dwCorePSign    = GetPrivateProfileInt( achPrOrient, "core pl sign", 0, achPrFile );
  GetPrivateProfileString( achPrOrient, "bed az",  "0", Acquire->ac_OrientDData.achBeddingAz, 5, achPrFile );
  Acquire->ac_OrientDData.dwBeddingPComp = GetPrivateProfileInt( achPrOrient, "bed pl comp",  0, achPrFile );
  Acquire->ac_OrientDData.dwBeddingPSign = GetPrivateProfileInt( achPrOrient, "bed pl sign",  0, achPrFile );
  GetPrivateProfileString( achPrOrient, "fold az", "0", Acquire->ac_OrientDData.achFoldAz,    5, achPrFile );
  Acquire->ac_OrientDData.dwFoldPComp    = GetPrivateProfileInt( achPrOrient, "fold pl comp", 0, achPrFile );
  Acquire->ac_OrientDData.dwFoldPSign    = GetPrivateProfileInt( achPrOrient, "fold pl sign", 0, achPrFile );

  Acquire->ac_MagnetometerDData.dwXRange  = GetPrivateProfileInt( achPrMSettings, "xrange",  0, achPrFile );        // Magnetometer settings
  Acquire->ac_MagnetometerDData.dwXFlux   = GetPrivateProfileInt( achPrMSettings, "xflux",   0, achPrFile );
  Acquire->ac_MagnetometerDData.dwXFilter = GetPrivateProfileInt( achPrMSettings, "xfilter", 0, achPrFile );
  Acquire->ac_MagnetometerDData.dwXSlew   = GetPrivateProfileInt( achPrMSettings, "xslew",   0, achPrFile );
  Acquire->ac_MagnetometerDData.dwXPanel  = GetPrivateProfileInt( achPrMSettings, "xpanel",  0, achPrFile );
  Acquire->ac_MagnetometerDData.dwYRange  = GetPrivateProfileInt( achPrMSettings, "yrange",  0, achPrFile );
  Acquire->ac_MagnetometerDData.dwYFlux   = GetPrivateProfileInt( achPrMSettings, "yflux",   0, achPrFile );
  Acquire->ac_MagnetometerDData.dwYFilter = GetPrivateProfileInt( achPrMSettings, "yfilter", 0, achPrFile );
  Acquire->ac_MagnetometerDData.dwYSlew   = GetPrivateProfileInt( achPrMSettings, "yslew",   0, achPrFile );
  Acquire->ac_MagnetometerDData.dwYPanel  = GetPrivateProfileInt( achPrMSettings, "ypanel",  0, achPrFile );
  Acquire->ac_MagnetometerDData.dwZRange  = GetPrivateProfileInt( achPrMSettings, "zrange",  0, achPrFile );
  Acquire->ac_MagnetometerDData.dwZFlux   = GetPrivateProfileInt( achPrMSettings, "zflux",   0, achPrFile );
  Acquire->ac_MagnetometerDData.dwZFilter = GetPrivateProfileInt( achPrMSettings, "zfilter", 0, achPrFile );
  Acquire->ac_MagnetometerDData.dwZSlew   = GetPrivateProfileInt( achPrMSettings, "zslew",   0, achPrFile );
  Acquire->ac_MagnetometerDData.dwZPanel  = GetPrivateProfileInt( achPrMSettings, "zpanel",  0, achPrFile );
  GetPrivateProfileString( achPrMSettings, "delay", "3000", Acquire->ac_MagnetometerDData.achDelay, 6, achPrFile );

  GetPrivateProfileString( achPrHolder, "x",     "0.000e-00", Acquire->ac_HolderDData.achX,       11, achPrFile );      // Sample holder moments
  GetPrivateProfileString( achPrHolder, "y",     "0.000e-00", Acquire->ac_HolderDData.achY,       11, achPrFile );
  GetPrivateProfileString( achPrHolder, "z",     "0.000e-00", Acquire->ac_HolderDData.achZ,       11, achPrFile );
  GetPrivateProfileString( achPrHolder, "total", "0.000e-00", Acquire->ac_HolderDData.achTotal,   11, achPrFile );

  GetPrivateProfileString( achPrStnd, "x",       "0.000e-00", Acquire->ac_StandardDData.achX,     11, achPrFile );      // Standard sample moments, and angle
  GetPrivateProfileString( achPrStnd, "y",       "0.000e-00", Acquire->ac_StandardDData.achY,     11, achPrFile );
  GetPrivateProfileString( achPrStnd, "z",       "0.000e-00", Acquire->ac_StandardDData.achZ,     11, achPrFile );
  GetPrivateProfileString( achPrStnd, "total",   "0.000e-00", Acquire->ac_StandardDData.achTotal, 11, achPrFile );
  GetPrivateProfileString( achPrStnd, "angle",   "0.0",       Acquire->ac_StandardDData.achAngle,  6, achPrFile );

  GetPrivateProfileString( achPrMOptions, "xyz", "1", Acquire->ac_MeasureDData.achXYZ, 2, achPrFile );  // Measure options
//GetPrivateProfileString( achPrMOptions, "x",   "1", Acquire->ac_MeasureDData.achX,   2, achPrFile );  // Measure options
//GetPrivateProfileString( achPrMOptions, "y",   "1", Acquire->ac_MeasureDData.achY,   2, achPrFile );
//GetPrivateProfileString( achPrMOptions, "z",   "1", Acquire->ac_MeasureDData.achZ,   2, achPrFile );
  Acquire->ac_MeasureDData.dwMin    = GetPrivateProfileInt( achPrMOptions, "min",    1, achPrFile );
  Acquire->ac_MeasureDData.dwSingle = GetPrivateProfileInt( achPrMOptions, "single", 0, achPrFile );
  Acquire->ac_MeasureDData.dwMult   = GetPrivateProfileInt( achPrMOptions, "mult",   0, achPrFile );
  GetPrivateProfileString( achPrMOptions, "rot", "2", Acquire->ac_MeasureDData.achMult, 2, achPrFile );
  Acquire->ac_MeasureDData.dwZ      = GetPrivateProfileInt( achPrMOptions, "+/-z", 0, achPrFile );
  GetPrivateProfileString( achPrMOptions, "S/N", "10", Acquire->ac_MeasureDData.achSN, 5, achPrFile );
  Acquire->ac_MeasureDData.dwSNR    = GetPrivateProfileInt( achPrMOptions, "S/N remeas", 0, achPrFile );
  Acquire->ac_MeasureDData.dwSNN    = GetPrivateProfileInt( achPrMOptions, "S/N notify", 1, achPrFile );
//GetPrivateProfileString( achPrMOptions, "drift", "1.0e-07", Acquire->ac_MeasureDData.achDrift, 8, achPrFile );
  GetPrivateProfileString( achPrMOptions, "drift", "1000",    Acquire->ac_MeasureDData.achDrift, 6, achPrFile );
  Acquire->ac_MeasureDData.dwDR     = GetPrivateProfileInt( achPrMOptions, "drift remeas", 0, achPrFile );
  Acquire->ac_MeasureDData.dwDN     = GetPrivateProfileInt( achPrMOptions, "drift notify", 1, achPrFile );
/*
  ProjOptionsDData.dwCore    = GetPrivateProfileInt( achPrPOptions, "core",    0, achPrFile );  // Projection Options
  ProjOptionsDData.dwInSitu  = GetPrivateProfileInt( achPrPOptions, "in situ", 0, achPrFile );
  ProjOptionsDData.dwRotated = GetPrivateProfileInt( achPrPOptions, "rotated", 0, achPrFile );
*/
  Acquire->ac_FileSummary.nCurrentStep = 0;     // Existing measurements in a data file

  // Set configuration flags and parameters

  if( Acquire->ac_ConfigureDData.dwX )          // Magnetometer axes
    Acquire->ac_bMagXAxis = TRUE;

  if( Acquire->ac_ConfigureDData.dwY )
    Acquire->ac_bMagYAxis = TRUE;

  if( Acquire->ac_ConfigureDData.dwZ )
    Acquire->ac_bMagZAxis = TRUE;

  if( Acquire->ac_ConfigureDData.dwDCSquids )   // DC squids
    Acquire->ac_bDCSquids = TRUE;

  Acquire->ac_dXCalibration = atof( Acquire->ac_ConstantsDData.achX );  // Magnetometer calibration constants in emu per flux quantum
  Acquire->ac_dYCalibration = atof( Acquire->ac_ConstantsDData.achY );
  Acquire->ac_dZCalibration = atof( Acquire->ac_ConstantsDData.achZ );

  Acquire->ac_dHolderXComponent = atof( Acquire->ac_HolderDData.achX ); // Sample holder moments in emu
  Acquire->ac_dHolderYComponent = atof( Acquire->ac_HolderDData.achY );
  Acquire->ac_dHolderZComponent = atof( Acquire->ac_HolderDData.achZ );

  Acquire->ac_dXYAngle = atof( Acquire->ac_StandardDData.achAngle );    // Angle between the magnetometer and the sample reference frames in the x-y plane

  if( Acquire->ac_ConfigureDData.dwSHAuto ) {                   // Sample handler
    Acquire->ac_bSHAuto = TRUE;                                 // Set flag
    if( Acquire->ac_HandlerDData.dwTrans ) {                    // Translation axis
      Acquire->ac_bSHTrans = TRUE;
      Acquire->ac_lTHome   = 0L;                                            // Translation positions
      Acquire->ac_lTLoad   = atol( Acquire->ac_HandlerDData.achPosLoad );   // Convert position strings to LONG values
      Acquire->ac_lTAFX    = atol( Acquire->ac_HandlerDData.achPosXAF  );
      Acquire->ac_lTAFY    = atol( Acquire->ac_HandlerDData.achPosYAF  );
      Acquire->ac_lTAFZ    = atol( Acquire->ac_HandlerDData.achPosZAF  );
      Acquire->ac_lTBack   = atol( Acquire->ac_HandlerDData.achPosBack );
      Acquire->ac_lTMeas   = atol( Acquire->ac_HandlerDData.achPosMeas );
//    Acquire->ac_lTRight  =  900000L;
//    Acquire->ac_lTLeft   = -900000L;
      if( Acquire->ac_HandlerDData.dwRight ) {                  // Right limit -
        Acquire->ac_lTRight = -900000L;
        Acquire->ac_lTLeft  =  900000L;
        }
      else {                                                    // Right limit +
        Acquire->ac_lTRight =  900000L;
        Acquire->ac_lTLeft  = -900000L;
        }
      }

    if( Acquire->ac_HandlerDData.dwRot ) {                      // Rotation axis
      Acquire->ac_bSHRot = TRUE;
      Acquire->ac_nRHome = atoi( Acquire->ac_HandlerDData.achRotCounts );   // Rotation positions
      Acquire->ac_nR360  = Acquire->ac_nRHome;
      Acquire->ac_nR90   = Acquire->ac_nRHome / 4;
      Acquire->ac_nR180  = Acquire->ac_nRHome / 2;
      Acquire->ac_nR270  = 3 * Acquire->ac_nRHome / 4;
      Acquire->ac_nRHome = 0;
      }
    }
// if( Acquire->ac_ConfigureDData.dwSHMan )
  else Acquire->ac_bSHManual = TRUE;

  if( Acquire->ac_ConfigureDData.dwAFAuto ) {           // AF demagnetizer
    Acquire->ac_bAFAuto = TRUE;
    Acquire->ac_dMaxAFField  = strtod( Acquire->ac_DemagDData.achField, NULL ); // Maximum field
    Acquire->ac_dMaxAFField /= 10.0;                                // G to mT
    }
  else if( Acquire->ac_ConfigureDData.dwAFMan ) {
    Acquire->ac_bAFManual   = TRUE;
    Acquire->ac_dMaxAFField = 300.0;                                // Maximum field
    }
//if( Acquire->ac_ConfigureDData.dwAFNone )
  else Acquire->ac_bAFNone = TRUE;

  if( Acquire->ac_DemagDData.dwX )                      // AF demagnetizer axes
    Acquire->ac_bAFXAxis = TRUE;

  if( Acquire->ac_DemagDData.dwY )
    Acquire->ac_bAFYAxis = TRUE;

  if( Acquire->ac_DemagDData.dwZ )
    Acquire->ac_bAFZAxis = TRUE;

  if( Acquire->ac_MagnetometerDData.dwXFlux && Acquire->ac_ConfigureDData.dwX ) // Flux counting
    Acquire->ac_bXFlux = TRUE;

  if( Acquire->ac_MagnetometerDData.dwYFlux && Acquire->ac_ConfigureDData.dwY )
    Acquire->ac_bYFlux = TRUE;

  if( Acquire->ac_MagnetometerDData.dwZFlux && Acquire->ac_ConfigureDData.dwZ )
    Acquire->ac_bZFlux = TRUE;

  Acquire->ac_dwSettlingDelay = (DWORD) atol( Acquire->ac_MagnetometerDData.achDelay );     // Magnetometer settling time delay in ms
/*
  if( Acquire->ac_MeasureDData.dwMin )                  // Measure options
    Acquire->ac_bMinMeasure = TRUE;

  if( Acquire->ac_MeasureDData.dwSingle )
    Acquire->ac_bSingleRotation = TRUE;

  if( Acquire->ac_MeasureDData.dwMult )
    Acquire->ac_bMultRotation = TRUE;
*/
  if( Acquire->ac_MeasureDData.dwMult )                 // Measure options
    Acquire->ac_bMultRotation   = TRUE;
  else if( Acquire->ac_MeasureDData.dwSingle )
    Acquire->ac_bSingleRotation = TRUE;
  else
    Acquire->ac_bMinMeasure     = TRUE;

  if( Acquire->ac_MeasureDData.dwZ )
    Acquire->ac_bMeasureNegZ    = TRUE;

  if( Acquire->ac_MeasureDData.dwSNR )
    Acquire->ac_bStoNRemeasure  = TRUE;
//if( Acquire->ac_MeasureDData.dwSNN )
  else Acquire->ac_bStoNNotify = TRUE;

  if( Acquire->ac_MeasureDData.dwDR )
    Acquire->ac_bDriftRemeasure = TRUE;
//if( Acquire->ac_MeasureDData.dwDN )
  else Acquire->ac_bDriftNotify = TRUE;

  Acquire->ac_nXYZAxisReadings = atoi( Acquire->ac_MeasureDData.achXYZ  );  // Analog data readings
//Acquire->ac_nXAxisReadings   = atoi( Acquire->ac_MeasureDData.achX    );  // Analog data readings per axis
//Acquire->ac_nYAxisReadings   = atoi( Acquire->ac_MeasureDData.achY    );
//Acquire->ac_nZAxisReadings   = atoi( Acquire->ac_MeasureDData.achZ    );

  Acquire->ac_nMultRotations   = atoi( Acquire->ac_MeasureDData.achMult );  // Rotations per sample

  Acquire->ac_dStoNThreshold   = atof( Acquire->ac_MeasureDData.achSN   );  // Thresholds
  Acquire->ac_dDriftThreshold  = atof( Acquire->ac_MeasureDData.achDrift);
  }


// Create the initial child window

void TMyMDIFrameSetupWindow( ACQUIRE *Acquire ) {

  char ErrorText[ 1024 ];

  // Retrieve printer configuration information from WIN.INI

  PrinterInfo( Acquire->ac_achDevice, Acquire->ac_achDriver, Acquire->ac_achPort );

  // Initialize serial ports

  if( Acquire->ac_ConfigureDData.dwMagCom ) {           // If the magnetometer is configured for use via a serial port
    if( Acquire->ac_nMagComID = OpenSerialDevice( (char) Acquire->ac_ConfigureDData.dwMagCom ) ) {
      Acquire->ac_bMagCom = TRUE;                       // Set flag
      Acquire->ac_nMagComID->SetBaudRate( Acquire->ac_nMagComID, 1200, 8, NOPARITY, ONESTOPBIT, FALSE );
      }
    else {                                              // If port did not open
      SystemError( ErrorText, "Cannot Open Mag Com Serial Port" );
      ErrorMessage(ErrorText, ProgramTitle );
      }
    }

  if( Acquire->ac_ConfigureDData.dwAFCom ) {            // If the AF demagnetizer is configured for use via a serial port
    if( Acquire->ac_ConfigureDData.dwAFCom == Acquire->ac_ConfigureDData.dwMagCom ) { // If the AF demagnetizer and the magnetometer share a serial port
      Acquire->ac_bAFCom   = TRUE;                      // Set flag since serial port is already opened
      Acquire->ac_nAFComID = Acquire->ac_nMagComID;     // Both will have the same serial port ID
      }
    else {                                              // Otherwise, open another serial port
      if( Acquire->ac_nAFComID = OpenSerialDevice( (char) Acquire->ac_ConfigureDData.dwAFCom ) ) {
        Acquire->ac_bAFCom = TRUE;                      // Set flag
        Acquire->ac_nAFComID->SetBaudRate( Acquire->ac_nAFComID, 1200, 8, NOPARITY, ONESTOPBIT, FALSE );
        }
      else {                                            // If port did not open
        SystemError( ErrorText, "Cannot Open AF Com Serial Port" );
        ErrorMessage(ErrorText, ProgramTitle );
        }
      }
    }

  if( Acquire->ac_ConfigureDData.dwSHCom ) {            // If the sample handler is configured for use via a serial port
    if( Acquire->ac_nSHComID = OpenSerialDevice( (char) Acquire->ac_ConfigureDData.dwSHCom ) ) {
      Acquire->ac_bSHCom = TRUE;                        // Set flag
      Acquire->ac_nSHComID->SetBaudRate( Acquire->ac_nSHComID, 1200, 8, NOPARITY, ONESTOPBIT, FALSE );
      }
    else {                                              // If port did not open
      SystemError( ErrorText, "Cannot Open SH Com Serial Port" );
      ErrorMessage(ErrorText, ProgramTitle );
      }
    }

  Acquire->ac_ChildCount = 0;
  Acquire->ac_hwndParent = Acquire->ac_hMainWindow;             // Handle to frame window
  Acquire->ac_hMainMenu  = GetMenu( Acquire->ac_hMainWindow );  // Get handle to the main menu
//hcrWait    = LoadCursor( NULL, IDC_WAIT );                    // Get handle to the hourglass cursor

  // Initialize devices

  if( Acquire->ac_bAFCom ) {                                // If the AF demagnetizer port is opened
    DegaussSetRamp( Acquire->ac_hMainWindow, Acquire->ac_nAFComID, Acquire->ac_DemagDData.dwRamp  );            // Set ramp
    DegaussSetDelay(Acquire->ac_hMainWindow, Acquire->ac_nAFComID, Acquire->ac_DemagDData.dwDelay );            // Set delay
    }

  if( Acquire->ac_bSHCom ) {                                                        // If the sample handler port is opened
    SHSendCommand( Acquire->ac_hMainWindow, Acquire->ac_nSHComID, "@0" );                                       // Place sample handler on line
    SHSetParameter(Acquire->ac_hMainWindow, Acquire->ac_nSHComID, "A", Acquire->ac_HandlerDData.achAccel );     // Set acceleration
    SHSetParameter(Acquire->ac_hMainWindow, Acquire->ac_nSHComID, "D", Acquire->ac_HandlerDData.achDecel );     // Set deceleration
    SHSetParameter(Acquire->ac_hMainWindow, Acquire->ac_nSHComID, "M", Acquire->ac_HandlerDData.achVel   );     // Set velocity
    if( Acquire->ac_bSHTrans ) {                                                    // Translation axis
      Acquire->ac_bTHome  = TRUE;                                                   // Set translation home flag
      Acquire->ac_bTRight = Translate( Acquire, Acquire->ac_hMainWindow, Acquire->ac_bTRight, Acquire->ac_lTRight );                 // Translate to right limit
      Acquire->ac_bTHome  = Translate( Acquire, Acquire->ac_hMainWindow, Acquire->ac_bTHome,  Acquire->ac_lTHome  );                 // Translate to home
      }
    if( Acquire->ac_bSHRot ) {                                                      // Rotation axis
      Acquire->ac_bRHome = Rotate( Acquire, Acquire->ac_hMainWindow, Acquire->ac_bRHome, Acquire->ac_nRHome );                       // Rotate to 0 position
      }
    }

  if( Acquire->ac_bMagCom ) {                                                       // If the magnetometer port is opened
    if( Acquire->ac_bMagXAxis ) {                                                   // X axis
      MagSetRange( Acquire->ac_hMainWindow, Acquire->ac_nMagComID, "X", Acquire->ac_MagnetometerDData.dwXRange ); // Set range
      MagSetFilter(Acquire->ac_hMainWindow, Acquire->ac_nMagComID, "X", Acquire->ac_MagnetometerDData.dwXFilter); // Set filter
      MagSetSlew(  Acquire->ac_hMainWindow, Acquire->ac_nMagComID, "X", Acquire->ac_MagnetometerDData.dwXSlew  ); // Set slew
//      MagSetPanel( Acquire->ac_hMainWindow, Acquire->ac_nMagComID, "X", Acquire->ac_MagnetometerDData.dwXPanel ); // Set panel
      }
    if( Acquire->ac_bMagYAxis ) {                                                   // Y axis
      MagSetRange( Acquire->ac_hMainWindow, Acquire->ac_nMagComID, "Y", Acquire->ac_MagnetometerDData.dwYRange ); // Set range
      MagSetFilter(Acquire->ac_hMainWindow, Acquire->ac_nMagComID, "Y", Acquire->ac_MagnetometerDData.dwYFilter); // Set filter
      MagSetSlew(  Acquire->ac_hMainWindow, Acquire->ac_nMagComID, "Y", Acquire->ac_MagnetometerDData.dwYSlew  ); // Set slew
//      MagSetPanel( Acquire->ac_hMainWindow, Acquire->ac_nMagComID, "Y", Acquire->ac_MagnetometerDData.dwYPanel ); // Set panel
      }
    if( Acquire->ac_bMagZAxis ) {                                                   // Z axis
      MagSetRange( Acquire->ac_hMainWindow, Acquire->ac_nMagComID, "Z", Acquire->ac_MagnetometerDData.dwZRange ); // Set range
      MagSetFilter(Acquire->ac_hMainWindow, Acquire->ac_nMagComID, "Z", Acquire->ac_MagnetometerDData.dwZFilter); // Set filter
      MagSetSlew(  Acquire->ac_hMainWindow, Acquire->ac_nMagComID, "Z", Acquire->ac_MagnetometerDData.dwZSlew  ); // Set slew
//      MagSetPanel( Acquire->ac_hMainWindow, Acquire->ac_nMagComID, "Z", Acquire->ac_MagnetometerDData.dwZPanel ); // Set panel
      }
    MagPulseLoop( Acquire->ac_hMainWindow, Acquire->ac_nMagComID, "A" );                            // Pulse feedback loop for each axis
    MagResetCount(Acquire->ac_hMainWindow, Acquire->ac_nMagComID, "A" );                            // Clear flux counter for each axis
    }

  // Disable menu items

  EnableMenuItem( Acquire->ac_hMainMenu, IDM_FILE_SAVE,        MF_GRAYED | MF_BYCOMMAND );   // File Save
  EnableMenuItem( Acquire->ac_hMainMenu, IDM_FILE_SAVEAS,      MF_GRAYED | MF_BYCOMMAND );   // File Save as
  EnableMenuItem( Acquire->ac_hMainMenu, IDM_FILE_PRINT,       MF_GRAYED | MF_BYCOMMAND );   // File Print
  EnableMenuItem( Acquire->ac_hMainMenu, IDM_SH_MOVE,          MF_GRAYED | MF_BYCOMMAND );   // Sample Handler Move
  EnableMenuItem( Acquire->ac_hMainMenu, IDM_MEASURE_SINGLE,   MF_GRAYED | MF_BYCOMMAND );   // Measure Single step
  EnableMenuItem( Acquire->ac_hMainMenu, IDM_MEASURE_SEQUENCE, MF_GRAYED | MF_BYCOMMAND );   // Measure Sequence
  EnableMenuItem( Acquire->ac_hMainMenu, IDM_MEASURE_HOLDER,   MF_GRAYED | MF_BYCOMMAND );   // Measure Sample holder
  EnableMenuItem( Acquire->ac_hMainMenu, IDM_MEASURE_STANDARD, MF_GRAYED | MF_BYCOMMAND );   // Measure Standard
//EnableMenuItem( Acquire->ac_hMainMenu, IDM_PROJECT_CART,     MF_GRAYED | MF_BYCOMMAND );   // Projection Cartesian

// Enable menu items

  if( Acquire->ac_bSHAuto ) // Automatic sample handler
    EnableMenuItem( Acquire->ac_hMainMenu, IDM_SH_MOVE, MF_ENABLED | MF_BYCOMMAND );        // Sample Handler Move
  }


/************************************************************************
*                                                                       *
*       CloseMainWindowClass:                                           *
*                                                                       *
*       Removes The Main Window Class From The System.                  *
*                                                                       *
*       Input:                                                          *
*                                                                       *
*       Acquire = Address Of Acquire Base.                              *
*                                                                       *
*       Output:                                                         *
*                                                                       *
*       Returns -> TRUE  == Main Window Class Was Removed From System.  *
*                  FALSE == Nothing To Do, Window Class Not Removed.    *
*                                                                       *
*       7.July 2002 Jamie Lisa Finch.                                   *
*                                                                       *
************************************************************************/

BOOL CloseMainWindowClass( ACQUIRE *Acquire ) {

  BOOL Return;

  Return = FALSE;

  if( Acquire->ac_hMDIWindowClass != (ATOM) NULL ) {
    Return = UnregisterClass( (LPCTSTR) Acquire->ac_hMDIWindowClass, Acquire->ac_hInstance );
    Acquire->ac_hMDIWindowClass = (ATOM) NULL;
    Return = TRUE;
    }

  if( Acquire->ac_hMainWindowClass != (ATOM) NULL ) {
    Return = UnregisterClass( (LPCTSTR) Acquire->ac_hMainWindowClass, Acquire->ac_hInstance );
    Acquire->ac_hMainWindowClass = (ATOM) NULL;
    Return = TRUE;
    }

  return Return;
  }


/************************************************************************
*                                                                       *
*       OpenMainWindowClass:                                            *
*                                                                       *
*       Attaches A Window Process To A Window hInstance.                *
*                                                                       *
*       Input:                                                          *
*                                                                       *
*       Acquire       = Address Of Acquire Base.                        *
*       WindowProcess = Address Of WindowProcess To Attach.             *
*                                                                       *
*       Output:                                                         *
*                                                                       *
*       Returns -> TRUE  == Window Process Was Attached.                *
*                  FALSE == Failed To Attach Window Process.            *
*                                                                       *
*       7.July 2002 Jamie Lisa Finch.                                   *
*                                                                       *
************************************************************************/

BOOL OpenMainWindowClass( ACQUIRE *Acquire, WNDPROC WindowProcess, WNDPROC MCIWindowProcess, WNDPROC ChildWindowProcess ) {

  WNDCLASS wc;

  wc.style         = CS_HREDRAW | CS_VREDRAW;
  wc.lpfnWndProc   = WindowProcess;
  wc.cbClsExtra    = 0;
  wc.cbWndExtra    = 0;
  wc.hInstance     = Acquire->ac_hInstance;
  wc.hIcon         = LoadIcon( Acquire->ac_hInstance, MAKEINTRESOURCE( IDI_APS_Logo ) );
  wc.hCursor       = LoadCursor( NULL, IDC_ARROW );
  wc.hbrBackground = GetStockObject( GRAY_BRUSH );
  wc.lpszMenuName  = MAKEINTRESOURCE( IDR_MainMenu );
  wc.lpszClassName = ProgramTitle;

  if( ( Acquire->ac_hMainWindowClass = RegisterClass( &wc ) ) == (ATOM) NULL )
    return FALSE;

  /* Register the MDI child class */

  wc.lpfnWndProc   = (WNDPROC) MCIWindowProcess;
  wc.hIcon         = LoadIcon( Acquire->ac_hInstance, MAKEINTRESOURCE( IDI_Vector ) );
  wc.lpszMenuName  = NULL;
  wc.cbWndExtra    = GWPARENTEXTRA;
  wc.hbrBackground = GetStockObject( WHITE_BRUSH );
  wc.lpszClassName = MDIClass;

  if( ( Acquire->ac_hMDIWindowClass = RegisterClass( &wc ) ) == (ATOM) NULL )
    return FALSE;

  /* Register the Child class */

  wc.lpfnWndProc   = (WNDPROC) ChildWindowProcess;
  wc.lpszMenuName  = NULL;
  wc.cbWndExtra    = GWCHILDEXTRA;
  wc.hbrBackground = GetStockObject( WHITE_BRUSH );
  wc.lpszClassName = ChildClass;

  if( ( Acquire->ac_hChildWindowClass = RegisterClass( &wc ) ) == (ATOM) NULL )
    return FALSE;

  return TRUE;
  }


/************************************************************************
*                                                                       *
*       SaveFileOnCloseAll:                                             *
*                                                                       *
*       This is called when all the windows close.  It copies the       *
*       Temp file to the Focus File.                                    *
*                                                                       *
*       Input:                                                          *
*                                                                       *
*       Acquire = Addess Of Acquire Base.                               *
*                                                                       *
*       Output:                                                         *
*                                                                       *
*       The Focus File Is Updated From The Temp File.                   *
*                                                                       *
*       Returns -> TRUE  == File Was Copied.                            *
*                  FALSE == No File In Focus.                           *
*                                                                       *
*       04.Nov.2002 Jamie Lisa Finch.                                   *
*                                                                       *
************************************************************************/

BOOL SaveFileOnCloseAll( ACQUIRE *Acquire ) {

  char achTitleStr[ MAXFILENAME ];

  if( ( Acquire != (ACQUIRE *) NULL  ) &&
      ( Acquire->ac_hwndChildInFocus ) ) {
    memset( achTitleStr, 0, sizeof( achTitleStr ) );
    GetWindowText( (HWND) GetWindowLong( Acquire->ac_hwndChildInFocus, GWL_HWNDParent ), achTitleStr, sizeof( achTitleStr ) );

    if( strlen( achTitleStr ) > 0 ) {
      Acquire->ac_FocusFile->CloseIt( Acquire->ac_FocusFile );  // Close the focus.tmp disk file
      CopyTheFile( achTitleStr, Acquire->ac_achFocusFile );

      return TRUE;
      }
    }
  return FALSE;
  }


/************************************************************************
*                                                                       *
*       CloseAllChildren:                                               *
*                                                                       *
************************************************************************/

VOID CloseAllChildren( ACQUIRE *Acquire ) {

  HWND hWindow;

  if( Acquire->ac_hWindowMDIClient ) {

    /* hide the MDI client window to avoid multiple repaints */

    ShowWindow( Acquire->ac_hWindowMDIClient, SW_HIDE );

    /* As long as the MDI client has a child, destroy it */

    while( hWindow = GetWindow( Acquire->ac_hWindowMDIClient, GW_CHILD ) ) {

      /* Skip the icon title windows */

      while( hWindow && GetWindow( hWindow, GW_OWNER ) )
        hWindow = GetWindow( hWindow, GW_HWNDNEXT );
      if( !hWindow )
        break;
      SendMessage( Acquire->ac_hWindowMDIClient, WM_MDIDESTROY, (UINT) hWindow, 0L );
      }
    ShowWindow( Acquire->ac_hWindowMDIClient, SW_SHOW );
    }
  }


/************************************************************************
*                                                                       *
*       CloseMainWindow:                                                *
*                                                                       *
*       Closes The Main Window.                                         *
*                                                                       *
*       Input:                                                          *
*                                                                       *
*       Acquire = Address Of Acquire Base.                              *
*                                                                       *
*       Output:                                                         *
*                                                                       *
*       The Main Window Is Closed.                                      *
*                                                                       *
*       Returns -> TRUE  == Window Was Opended And Closed.              *
*                  FALSE == Window Was Already Closed.                  *
*                                                                       *
*       7.July 2002 Jamie Lisa Finch.                                   *
*                                                                       *
************************************************************************/

BOOL CloseMainWindow( ACQUIRE *Acquire ) {

  BOOL Return = FALSE;

  CloseAllChildren( Acquire );

  if( Acquire->ac_hWindowMDIClient != (HWND) NULL ) {
    DestroyWindow( Acquire->ac_hWindowMDIClient );
    Acquire->ac_hWindowMDIClient = (HWND) NULL;
    Return = TRUE;
    }

  if( Acquire->ac_hMainWindow != (HWND) NULL ) {
    DestroyWindow( Acquire->ac_hMainWindow );
    Acquire->ac_hMainWindow = (HWND) NULL;
    Return = TRUE;
    }

  return Return;
  }


/************************************************************************
*                                                                       *
*       FrameWindowProcess:                                             *
*                                                                       *
*       The WindowProc function is an application-defined callback      *
*       function that processes messages sent to a window.              *
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
*       The return value is the result of the message processing        *
*       and depends on the message sent.                                *
*                                                                       *
*       7.July 2002 Jamie Lisa Finch.                                   *
*                                                                       *
************************************************************************/

LRESULT CALLBACK FrameWindowProcess( HWND hWindow, UINT uMsg, WPARAM wParam, LPARAM lParam ) {

  ACQUIRE    *Acquire;
  HMENU       hMenu;
  CLIENTCREATESTRUCT ccs;

  switch( uMsg ) {

    case WM_CREATE:
      if( ( Acquire = GAcquire ) != (ACQUIRE *) NULL ) {
        memset( &ccs, 0, sizeof( CLIENTCREATESTRUCT ) );

        /* Find window menu where children will be listed */

        ccs.hWindowMenu  = GetSubMenu( GetMenu( hWindow ), ChildMenuPos );
        ccs.idFirstChild = IDM_WINDOWCHILD;

        /* Create the MDI client filling the client area */

        Acquire->ac_hWindowMDIClient = CreateWindow(
          "mdiclient",
          NULL,
          WS_CHILD | WS_CLIPCHILDREN | WS_VSCROLL | WS_HSCROLL,
          0, 0,
          0, 0,
          hWindow,
          (HMENU) NULL,
          Acquire->ac_hInstance,
          (LPSTR) &ccs );

        ShowWindow( Acquire->ac_hWindowMDIClient, SW_SHOW );
        }
      return 0;

    case WM_DESTROY:
      if( ( Acquire = (ACQUIRE *) GetWindowLong( hWindow, GWL_USERDATA ) ) != (ACQUIRE *) NULL ) {
        TMyMDIFrameWMDestroy( Acquire );
        Acquire->ac_hMainWindow      = (HWND) NULL;
        Acquire->ac_hWindowMDIClient = (HWND) NULL;
        }
      return 0;

    /* This Gets Called When The Window Changes Size. */

    case WM_SIZE:
      if( ( Acquire = (ACQUIRE *) GetWindowLong( hWindow, GWL_USERDATA ) ) != (ACQUIRE *) NULL ) {
        Acquire->ac_MainWindowWidth  = LOWORD( lParam );
        Acquire->ac_MainWindowHeight = HIWORD( lParam );
        }
      break;    /* This Message Must Be Passed Through. */

    /* This Gets Called When The X Is Pressed On Window Border. */

    case WM_CLOSE:
      if( ( Acquire = (ACQUIRE *) GetWindowLong( hWindow, GWL_USERDATA ) ) != (ACQUIRE *) NULL ) {
        TMyMDIFrameWMDestroy( Acquire );
        Acquire->ac_hMainWindow      = (HWND) NULL;
        Acquire->ac_hWindowMDIClient = (HWND) NULL;
        }
      return 0;

    /* This Get Called When Windows Shuts Down, Asking Ok To Close Application. */

    case WM_QUERYENDSESSION:
      return TRUE;

    /* This Get Called When Windows Shuts Diwn. Telling To Close Application. */

    case WM_ENDSESSION:
      if( wParam ) {
        PostQuitMessage( 0 );
        }

    case WM_COMMAND:
      if( ( Acquire = (ACQUIRE *) GetWindowLong( hWindow, GWL_USERDATA ) ) != (ACQUIRE *) NULL ) {

        /* Check For Menu Items. */


        if( hMenu = GetMenu( hWindow ) ) {
          switch( LOWORD( wParam ) ) {

            case IDM_FILE_NEW:
              TMyMDIFrameCMFileNew( Acquire );
              return 0;

            case IDM_FILE_OPEN:
              TMyMDIFrameCMFileOpen( Acquire );
              return 0;

            case IDM_FILE_SAVE:
              SendMessage( hWindow, UM_SAVE, 0, 0 );    // Send a save message directly to the message function
              return 0;

            case IDM_FILE_SAVEAS:
              SendMessage( hWindow, UM_SAVEAS, 0, 0 );  // Send a save message directly to the message function
              return 0;

            case IDM_FILE_PRINT:
              SendMessage( hWindow, UM_TEXT, 1U, 0 );   // Send a text message directly to the message function
              return 0;

            case IDM_FILE_SETUP:
              TMyMDIFrameCMPrnSetup( Acquire );
              return 0;

            case IDM_FILE_CONFIGURE:
              TMyMDIFrameCMConfigure( Acquire );
              return 0;

            case IDM_FILE_EXIT:
              TMyMDIFrameCMQuit( hWindow );
              return 0;

            case IDM_MAG_SETTINGS:
              TMyMDIFrameCMMagSettings( Acquire, hWindow );
              return 0;

            case IDM_MEASURE_OPTIONS:
              TMyMDIFrameCMOptions( Acquire );
              return 0;

            case IDM_MEASURE_SINGLE:
              TMyMDIChildCMSingle(  Acquire, hWindow );
              break;

            case IDM_MEASURE_SEQUENCE:
              TMyMDIChildCMSequence(Acquire, hWindow );
              break;

            case IDM_MEASURE_HOLDER:
              TMyMDIChildCMHolder(  Acquire, hWindow );
              break;

            case IDM_MEASURE_STANDARD:
              TMyMDIChildCMStandard(Acquire, hWindow );
              break;

            case IDM_HELP_ABOUT:
              DialogBoxParam( Acquire->ac_hInstance, MAKEINTRESOURCE( About_Dialog ), Acquire->ac_hMainWindow, TAboutDialogProcess, (long) Acquire );
              return 0;

            case IDM_SH_MOVE:
              TMyMDIFrameCMSHMove( Acquire, hWindow );
              return 0;

            case IDM_WINDOW_ICONS:              /* Arrange Icons. */
              SendMessage( Acquire->ac_hWindowMDIClient, WM_MDIICONARRANGE, 0, 0L );
              return 0;

            case IDM_WINDOW_TILEHORIZONTALLY:   /* Tile Horizontally.*/
              SendMessage( Acquire->ac_hWindowMDIClient, WM_MDITILE, MDITILE_HORIZONTAL, 0L );
              return 0;

            case IDM_WINDOW_TILEVERTICALLY:     /* Tile Vertically.  */
              SendMessage( Acquire->ac_hWindowMDIClient, WM_MDITILE, MDITILE_VERTICAL, 0L );
              return 0;

            case IDM_WINDOW_CASCADE:            /* Cascade. */
              SendMessage( Acquire->ac_hWindowMDIClient, WM_MDICASCADE, MDITILE_SKIPDISABLED, 0L );
              return 0;

            case IDM_WINDOW_CLOSEALL:   /* Close All. */
              SaveFileOnCloseAll(Acquire );
              CloseAllChildren(  Acquire );
              return 0;

//            case IDM_SA:
//              EnableMenuItem( Acquire->ac_hMainMenu, IDM_MEASURE_SINGLE,   MF_ENABLED | MF_BYCOMMAND );     // Measure Single step
//              EnableMenuItem( Acquire->ac_hMainMenu, IDM_MEASURE_HOLDER,   MF_ENABLED | MF_BYCOMMAND );     // Measure Sample holder
//              EnableMenuItem( Acquire->ac_hMainMenu, IDM_MEASURE_STANDARD, MF_ENABLED | MF_BYCOMMAND );     // Measure Standard
//              return 0;

            }
          }
        }
      break;

    /* Redraw With New Palette. */

    case WM_PALETTECHANGED:
      if( hWindow != (HWND) wParam )    /* Advoid Infinite Loop. */
        RedrawWindow( hWindow, 0, 0, RDW_ERASE | RDW_INVALIDATE | RDW_UPDATENOW | RDW_FRAME );
      break;

    /* These Are The Application Personal Messages. */

    case UM_ERROR:
      if( ( Acquire = (ACQUIRE *) GetWindowLong( hWindow, GWL_USERDATA ) ) != (ACQUIRE *) NULL ) {
        TMyMDIFrameWMSerialError( Acquire, wParam, lParam );
        }
      return 0;

    case UM_SAVE:
      if( ( Acquire = (ACQUIRE *) GetWindowLong( hWindow, GWL_USERDATA ) ) != (ACQUIRE *) NULL ) {
        TMyMDIChildWMSave( Acquire, Acquire->ac_hwndChildInFocus );
        }
      return 0;

    case UM_SAVEAS:
      if( ( Acquire = (ACQUIRE *) GetWindowLong( hWindow, GWL_USERDATA ) ) != (ACQUIRE *) NULL ) {
        TMyMDIChildCMSaveAs( Acquire, Acquire->ac_hwndChildInFocus );
        }
      return 0;

    case UM_DEMAGNETIZE:
      if( ( Acquire = (ACQUIRE *) GetWindowLong( hWindow, GWL_USERDATA ) ) != (ACQUIRE *) NULL ) {
        TMyMDIChildWMDemagnetize( Acquire, hWindow );
        }
      return 0;

    case UM_MEASURE:
      if( ( Acquire = (ACQUIRE *) GetWindowLong( hWindow, GWL_USERDATA ) ) != (ACQUIRE *) NULL ) {
        TMyMDIChildWMMeasure( Acquire, hWindow );
        }
      return 0;

    case UM_REDUCE:
      if( ( Acquire = (ACQUIRE *) GetWindowLong( hWindow, GWL_USERDATA ) ) != (ACQUIRE *) NULL ) {
        TMyMDIChildWMReduce( Acquire, hWindow );
        }
      return 0;

    case UM_TEXT:
      if( ( Acquire = (ACQUIRE *) GetWindowLong( hWindow, GWL_USERDATA ) ) != (ACQUIRE *) NULL ) {
        TMyMDIChildWMText( Acquire, Acquire->ac_hwndChildInFocus, wParam );
        }
      return 0;

    case UM_LOAD:
      if( ( Acquire = (ACQUIRE *) GetWindowLong( hWindow, GWL_USERDATA ) ) != (ACQUIRE *) NULL ) {
        TMyMDIChildWMLoad( Acquire, hWindow );
        }
      return 0;

    }
  return DefFrameProc( hWindow, GAcquire->ac_hWindowMDIClient, uMsg, wParam, lParam );
  }


/************************************************************************
*                                                                       *
*       OpenMainWindow:                                                 *
*                                                                       *
*       Creates The Main Window And Gets A Handle To The Window.        *
*                                                                       *
*       Input:                                                          *
*                                                                       *
*       Acquire = Address Of Acquire Base.                              *
*                                                                       *
*       Output:                                                         *
*                                                                       *
*       Returns -> TRUE  == New Window Was Created.                     *
*                  FALSE == Failed To Open New Window.                  *
*                                                                       *
*       7.July 2002 Jamie Lisa Finch.                                   *
*                                                                       *
************************************************************************/

BOOL OpenMainWindow( ACQUIRE *Acquire ) {

  if( ( Acquire->ac_hMainWindow = CreateWindow(
    ProgramTitle,
    ProgramTitle,
    WS_OVERLAPPEDWINDOW | WS_CLIPCHILDREN,
    Acquire->ac_MainWindowX,            /* Initial Position. */
    Acquire->ac_MainWindowY,
    Acquire->ac_MainWindowWidth,        /* Initial Size. */
    Acquire->ac_MainWindowHeight,
    NULL,
    NULL,
    Acquire->ac_hInstance,
    NULL /*Acquire*/ ) ) == (HWND) NULL ) return FALSE;

  SetWindowLong(Acquire->ac_hMainWindow, GWL_USERDATA, (long) Acquire );
//  SetClassLong( Acquire->ac_hMainWindow, GCL_HICON, (LONG) LoadIcon( Acquire->ac_hInstance, "VECTOR" ) );
  SetWindowText(Acquire->ac_hMainWindow, ProgramTitle );

  return TRUE;
  }


/************************************************************************
*                                                                       *
*       WinMain:                                                        *
*                                                                       *
*       Contains The Main Entry Point For Acquire Program.              *
*                                                                       *
*       Input:                                                          *
*                                                                       *
*       hInstance     = Handle  to current instance.                    *
*       hPrevInstance = Handle  to previous instance.                   *
*       lpCmdLine     = Pointer to command line.                        *
*       nCmdShow      = Show state of window.                           *
*                                                                       *
*       Output:                                                         *
*                                                                       *
*       Runs The Acquire Program.                                       *
*                                                                       *
*       7.July 2002 Jamie Lisa Finch.                                   *
*                                                                       *
************************************************************************/

int WINAPI WinMain( HINSTANCE hInstance, HINSTANCE hPrevInstance, LPSTR lpCmdLine, int nCmdShow ) {

  ACQUIRE *Acquire;
  BOOL     Ok;
  INITCOMMONCONTROLSEX Controls;

  if( Acquire = malloc( sizeof( ACQUIRE ) ) ) {
    memset( Acquire, 0, sizeof( ACQUIRE ) );
    GAcquire = Acquire;

    Acquire->ac_hInstance        = hInstance;
    Acquire->ac_hPrevInstance    = hPrevInstance;
    Acquire->ac_lpCmdLine        = lpCmdLine;
    Acquire->ac_nCmdShow         = nCmdShow;
    Acquire->ac_MainWindowX      = CW_USEDEFAULT;
    Acquire->ac_MainWindowY      = CW_USEDEFAULT;
    Acquire->ac_MainWindowWidth  = CW_USEDEFAULT;
    Acquire->ac_MainWindowHeight = CW_USEDEFAULT;

    /* Get The Extra Dialog Controls. */

    memset( &Controls, 0, sizeof( INITCOMMONCONTROLSEX ) );
    Controls.dwSize = sizeof( INITCOMMONCONTROLSEX );
    Controls.dwICC  = ICC_PROGRESS_CLASS | ICC_COOL_CLASSES | ICC_USEREX_CLASSES;
    InitCommonControlsEx( &Controls );

    Acquire->ac_hFont = Courier_Font( 13, TRUE );

    Acquire->ac_FocusFile = FileObject( "" );
    TMyMDIFrameTMyMDIFrame( Acquire, ProgramTitle );

    /* Open The Window Classes. */

    Ok = TRUE;

    /* Read The Options Information. */

    if( Ok ) {

      if( OpenMainWindowClass( Acquire, FrameWindowProcess, MDIWindowProcess, ChildWindowProcess ) ) {

        /* Read In Configuration Data. */

        if( OpenMainWindow( Acquire ) ) {
          TMyMDIFrameSetupWindow( Acquire );

          GetWindowBorders( Acquire->ac_hMainWindow, &Acquire->ac_MainWindowBorder );
          MoveWindow( Acquire->ac_hMainWindow,
                      Acquire->ac_MainWindowX      - Acquire->ac_MainWindowBorder.left,
                      Acquire->ac_MainWindowY      - Acquire->ac_MainWindowBorder.top,
                      Acquire->ac_MainWindowWidth  + Acquire->ac_MainWindowBorder.left + Acquire->ac_MainWindowBorder.right,
                      Acquire->ac_MainWindowHeight + Acquire->ac_MainWindowBorder.top  + Acquire->ac_MainWindowBorder.bottom, FALSE );

          MoveWindowToFront( Acquire->ac_hMainWindow, Acquire->ac_nCmdShow );   /* Place Window On Screen. */

          /* Loop and Read Messages. */

          if( Ok ) {
            while( GetMessage( &Acquire->ac_Message, NULL, 0, 0 ) ) {
              if(      Acquire->ac_CancelDialog  && IsDialogMessage( Acquire->ac_CancelDialog,  &Acquire->ac_Message ) );
              else if( Acquire->ac_RangeUpDialog && IsDialogMessage( Acquire->ac_RangeUpDialog, &Acquire->ac_Message ) );
              else {
                Ok = TRUE;
                if( Ok ) {
                  TranslateMessage(&Acquire->ac_Message );
                  DispatchMessage( &Acquire->ac_Message );
                  }
                }
              }
            }
          }
        else ErrorMessage( CannotOpenWindow, ProgramTitle );

        /* Close The Windows. */

        SaveFileOnCloseAll(Acquire );
        CloseMainWindow(   Acquire );
        }
      else ErrorMessage( CannotOpenWindowClass, ProgramTitle );
      }
    else ErrorMessage( CannotOpenWindowClass, ProgramTitle );

    /* Free The Classes. */

    CloseMainWindowClass(    Acquire );

    if( Acquire->ac_FocusFile ) {
      Acquire->ac_FocusFile->DeleteFileObject( Acquire->ac_FocusFile );
      Acquire->ac_FocusFile = (FILEOBJECT *) NULL;
      }
    DeleteObject( Acquire->ac_hFont ); Acquire->ac_hFont = 0;

    /* Give Back The Printer. */

    if( Acquire->ac_hInitData ) {
      LocalFree( Acquire->ac_hInitData );
      Acquire->ac_hInitData = 0;
      }

    GAcquire = (ACQUIRE *) NULL;
    free( Acquire );
    }
  return 0;
  }
