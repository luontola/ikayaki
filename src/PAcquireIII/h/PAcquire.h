/************************************************************************
*                                                                       *
*       PAcquire.h                                                      *
*                                                                       *
*       Contains The Main Header Code For The P Acquire Program.        *
*                                                                       *
*       24.Sept.2002 Jamie Lisa Finch.                                  *
*                                                                       *
************************************************************************/

#include "..\h\DlgData.h"
#include "..\h\SerialIO.h"
#include "..\h\FileObj.h"

#define	UM_ERROR        (WM_APP + 0)
#define	UM_SAVE         (WM_APP + 1)
#define	UM_SAVEAS       (WM_APP + 2)
#define	UM_DEMAGNETIZE  (WM_APP + 3)
#define	UM_MEASURE      (WM_APP + 4)
#define	UM_REDUCE       (WM_APP + 5)
#define	UM_TEXT         (WM_APP + 6)
#define	UM_LOAD         (WM_APP + 7)

#define MAXFILENAME 80  // Maximum length in characters of a file name and path
#define HSBPMIN     0   // Horizontal scroll bar thumb minimum position
#define HSBPMAX     128 // Horizontal scroll bar thumb maximum position
#define VSBPMIN     0   // Vertical scroll bar thumb minimum position
#define VSBPMAX     128 // Vertical scroll bar thumb maximum position

#define ChildMenuPos    1       // Indicate which menu gets the window information
#define IDM_WINDOWCHILD _APS_NEXT_COMMAND_VALUE

#define SAMPLERHANDLERTIMEOUT 60


#ifdef _DEBUG
#define SH_HACKOUT        // test without a sample handler
#endif


typedef struct _configuredialog {
  HWND hCalib,  hPos,   hParam;
  HWND hMag,    hAF,    hSH;
  HWND hMagCom, hAFCom, hSHCom;
  } CONFIGUREDIALOG;

typedef struct _THandlerDialog {
  HWND h2G811, hFlip;
  HWND hAccelT, hAccel, hDecelT, hDecel, hVelT, hVel, hVelMT, hVelM;
  HWND hPos, hPosTransXT, hPosTransX, hPosTransYT, hPosTransY, hPosAxialT, hPosAxial, hPosLoadT, hPosLoad, hPosBackT, hPosBack, hPosMeasT, hPosMeas;
  HWND hRotation, hCountsT, hCounts;
  HWND hRightT, hRight;
  } THANDLERDIALOG;

typedef struct _TMagSettingsDialog {
  HWND hXGroup, hXRangeT, hXRange, hXFlux, hXFilterT, hXFilter, hXSlew, hXReset, hXPanel;
  HWND hYGroup, hYRangeT, hYRange, hYFlux, hYFilterT, hYFilter, hYSlew, hYReset, hYPanel;
  HWND hZGroup, hZRangeT, hZRange, hZFlux, hZFilterT, hZFilter, hZSlew, hZReset, hZPanel;
  } TMAGSETTINGSDIALOG;

typedef struct _TOptionsDialog {
  HWND hXYZT, hXYZ;
//HWND hXT, hX, hYT, hY, hZT, hZ;
  HWND hSingle, hMult, hRotations;
  } TOPTIONSDIALOG;

typedef struct _TSampleDialog {
  HWND hName, hSize, hCoreAz, hCorePl, hBedAz, hBedPl, hFoldAz, hFoldPl, hDecl;
  } TSAMPLEDIALOG;

/* Window word values for parent windows */

typedef enum _gwparent {
  GLW_HWNDChild = 0,
  GWPARENTEXTRA= GLW_HWNDChild + 4,
  } GWPARENT;

/* Window word values for child windows */

typedef enum _gwchild {
  GWL_HWNDParent = 0,
  GWL_nHScroll   = GWL_HWNDParent + 4,  // Position of the horizontal scroll bar thumb.
  GWL_nVScroll   = GWL_nHScroll   + 4,  // Position of the vertical   scroll bar thumb.
  GWCHILDEXTRA   = GWL_nVScroll   + 4,
  } GWCHILD;

typedef struct _SPECIMENDATANODE {

  struct _SPECIMENDATANODE *sdn_Next;

  SPECIMENDATA sdn_SpecimenData;
  
  } SPECIMENDATANODE;


typedef struct _SPECIMENWINDOWINFO {
  /* List of Specimen data sample nodes */ 
  int               swi_NumberOfSamples;
  INFO              swi_Info;
  SPECIMENDATANODE  *swi_DataNode;
  } SPECIMENWINDOWINFO;


typedef struct st_acquire {

  /* Original Parameters At Startup. */

  HINSTANCE        ac_hInstance;
  HINSTANCE        ac_hPrevInstance;
  LPSTR            ac_lpCmdLine;
  int              ac_nCmdShow;
  MSG              ac_Message;

  /* Main Window Definitions. */

  ATOM             ac_hMainWindowClass;
  ATOM             ac_hMDIWindowClass;
  ATOM             ac_hChildWindowClass;
  HWND             ac_hMainWindow;
  HWND             ac_hWindowMDIClient;
  int              ac_MainWindowX;
  int              ac_MainWindowY;
  int              ac_MainWindowWidth;
  int              ac_MainWindowHeight;
  RECT             ac_MainWindowBorder;
  HWND             ac_hWindowToolBar;
  CONFIGUREDIALOG  ac_ConfigureDialog;
  CONFIG           ac_ConfigureDData;
  CONFIG           ac_TemporaryDData;
  CALIB            ac_ConstantsDData;
  THANDLERDIALOG   ac_THandlerDialog;
  TMAGSETTINGSDIALOG ac_TMagSettingsDialog;
  TOPTIONSDIALOG   ac_TOptionsDialog;
  TSAMPLEDIALOG    ac_TSampleDialog;
  SH               ac_HandlerDData;
  AF               ac_DemagDData;
  ORIENT           ac_OrientDData;
  SETTINGS         ac_MagnetometerDData;// Magnetometer Settings dialog box
  HANDLE           ac_hInitData;        /* Handle to initialization data. */
  char             ac_achTitleStr[ MAXFILENAME ];
  char             ac_achDriver[ 40 ];  // Printer driver name
  char             ac_achDevice[ 40 ];  // Printer device name
  char             ac_achPort[   10 ];  // Printer port name
  HFONT            ac_hFont;


  SERIALIO        *ac_nMagComID;
  SERIALIO        *ac_nAFComID;
  SERIALIO        *ac_nSHComID;
  BOOL             ac_bMagCom;
  BOOL             ac_bAFCom;
  BOOL             ac_bSHCom;

  int              ac_ChildCount;
  HWND             ac_hwndParent;       // Handle to frame window
  HWND             ac_hwndChildInFocus; // Handles to the frame window and to the child window with input focus
  HMENU            ac_hMainMenu;        // Get handle to the main menu

  BOOL             ac_bSHAuto,  ac_bSHManual;                           // Sample handler
  BOOL             ac_bSHTrans, ac_bSHRot;                              // Sample handler axes
  BOOL             ac_bTHome, ac_bTLoad, ac_bTAFX, ac_bTAFY, ac_bTAFZ, ac_bTBack, ac_bTMeas, ac_bTRight, ac_bTLeft; // Flags to indicate sample handler translation position
  LONG             ac_lTHome, ac_lTLoad, ac_lTAFX, ac_lTAFY, ac_lTAFZ, ac_lTBack, ac_lTMeas, ac_lTRight, ac_lTLeft; // Sample handler translation positions in pulses*10 relative to the home sensor
  BOOL             ac_bRHome, ac_bR90, ac_bR180, ac_bR270;              // Flags to indicate sample handler rotation position
  int              ac_nRHome, ac_nR90, ac_nR180, ac_nR270, ac_nR360;    // Sample handler rotation positions in pulses*10 relative to the home sensor
  BOOL             ac_bMagXAxis, ac_bMagYAxis, ac_bMagZAxis;            // Magnetometer axes
  BOOL             ac_bDCSquids;                                        // DC squids
  BOOL             ac_bFilePaint, ac_bMeasurePaint;                     // Flags to indicate content of a repainted child window
  char             ac_achSHReply[ 1024 ];                               // Read buffers for serial port communications
  BOOL             ac_bCancelCycle;                                     // Flag to cancel measurement cycle
  BOOL             ac_bXFlux, ac_bYFlux, ac_bZFlux;                     // Magnetometer axes flux counting status
  DWORD            ac_dwSettlingDelay;                                  // Magnetometer settling time delay in ms
  BOOL             ac_bMinMeasure, ac_bSingleRotation, ac_bMultRotation, ac_bMeasureNegZ, ac_bStoNRemeasure, ac_bStoNNotify, ac_bDriftRemeasure, ac_bDriftNotify; // Measure options
  OPTIONS          ac_MeasureDData;                                     // Measure Options dialog box
  int              ac_nXYZAxisReadings;                                 // Analog data readings
  int              ac_nMultRotations;                                   // Rotations per sample
  double           ac_dStoNThreshold, ac_dDriftThreshold;               // Threshold values
  INFO             ac_SampleDData;                                      // Sample Information dialog box
  char             ac_achFileOpen[ MAXFILENAME ];                       // Name of file to be opened via the File Open dialog box
  char             ac_achFocusFile[MAXFILENAME ];                       // Path and name of temporary focus file
  FILEOBJECT      *ac_FocusFile;
  BOOL             ac_bFileNew, ac_bFileOpen;                           // Flags to indicate new files and existing files
  SPECIMENSUMMARY  ac_FileSummary;                                      // Disk file specimen data summary
  int              ac_nChildrenCount;                                   // Number of existing children windows
  int              ac_nActivateCycle;
  BOOL             ac_bAFAuto, ac_bAFManual, ac_bAFNone;                // AF demagnetizer
  BOOL             ac_bAFXAxis, ac_bAFYAxis, ac_bAFZAxis;               // AF demagnetizer axes
  MOVE             ac_SHMoveDData;
  SINGLE           ac_SingleDData;                                      // Measure Single Step dialog box
  SEQUENCE         ac_SequenceDData;                                    // Measure Sequence dialog box
  HOLDER           ac_HolderDData;                                      // Measure Sample Holder dialog box
  STND             ac_StandardDData;                                    // Measure Standard dialog box
  SPECIMENDATA     ac_FileData;                                         // Disk file specimen data
  HWND             ac_CancelDialog;                                     // Cancel measurement cycle dialog box is not active
  HWND             ac_RangeUpDialog;                                    // Magnetometer automatic range up dialog box is not active
  double           ac_dXCalibration, ac_dYCalibration, ac_dZCalibration;// Calibration constants for each magnetometer axis in emu per flux quantum
  double           ac_dHolderXComponent, ac_dHolderYComponent, ac_dHolderZComponent;    // Holder component moments (emu)
  double           ac_dXYAngle;                                         // Angle between the magnetometer and the sample reference frames in the x-y plane
  double           ac_dMaxAFField;                                      // Maximum allowed AF field
  double           ac_dAFLevel;                                         // Current AF demagnetization level in mT
  BOOL             ac_bUpRange;                                         // Flag to indicate that magnetometer ranges are to be increased
  double           ac_adBGSignal[ 6 ];                                  // Background readings (emu) before and after sample measurement
  double           ac_adDrift[ 3 ];                                     // Total drift (rise) for each magnetometer axis
  int              ac_idMagSignal;                                      // Index to magnetometer axis readings arrays
  double           ac_adMagXSignal[36], ac_adMagYSignal[36], ac_adMagZSignal[36]; // Readings (emu) for each magnetometer axis
  double           ac_adNormalSums[ 9 ];                                // Normal statistics sums for each component
  double           ac_adSampleComponents[ 6 ];                          // Sample component moments (emu) and standard deviations
  } ACQUIRE;

extern ACQUIRE *GAcquire;
