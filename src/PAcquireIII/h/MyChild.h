/************************************************************************
*                                                                       *
*       MyChild.h:                                                      *
*                                                                       *
*       27.Sept.2002 Jamie Lisa Finch.                                  *
*                                                                       *
************************************************************************/

#ifndef __MYCHILD_INCLUDED__
#define __MYCHILD_INCLUDED__

#ifdef __cplusplus
extern "C" {
#endif

void Eprintf( char *, char *, double );
BOOL MoveWindowToFront( HWND, int );
BOOL GetWindowBorders( HWND, RECT * );

// Function prototypes

BOOL Translate( ACQUIRE *, HWND, BOOL, LONG  );         // Moves automatic sample handler translator to flag position, and returns TRUE when finished
BOOL Rotate(    ACQUIRE *, HWND, BOOL, int   );         // Moves automatic sample handler rotator to flag position, and returns TRUE when finished
BOOL MagRangeUp(ACQUIRE *, HWND, SERIALIO *, char * );  // Increases range setting for specified magnetometer axis, and returns TRUE if successful

BOOL CopyTheFile( char *, char * );
BOOL APIENTRY GetInitializationData( ACQUIRE *, HWND );

void TMyMDIChildSetupWindow( HWND );
void TMyMDIChildWMSave(       ACQUIRE *, HWND );
void TMyMDIChildCMSaveAs(     ACQUIRE *, HWND );
void TMyMDIChildWMDemagnetize(ACQUIRE *, HWND );
void TMyMDIChildWMMeasure(    ACQUIRE *, HWND );
void TMyMDIChildWMReduce(     ACQUIRE *, HWND );
void TMyMDIChildWMText(       ACQUIRE *, HWND, WPARAM );
void TMyMDIChildWMLoad(       ACQUIRE *, HWND );
void TMyMDIChildCMSingle(     ACQUIRE *, HWND );
void TMyMDIChildCMSequence(   ACQUIRE *, HWND );
void TMyMDIChildCMHolder(     ACQUIRE *, HWND );
void TMyMDIChildCMStandard(   ACQUIRE *, HWND );

// Constructor for child window objects

BOOL OpenChildWindow( ACQUIRE *, char * );
LRESULT CALLBACK MDIWindowProcess(   HWND, UINT, WPARAM, LPARAM );
LRESULT CALLBACK ChildWindowProcess( HWND, UINT, WPARAM, LPARAM );

#ifdef __cplusplus
};
#endif
#endif
