// FNCTNS.CPP
// C++ Windows Programs for 2G Enterprises Magnetometer Instrument Control
// Copyright 2G Enterprises, All Rights Reserved

#include <windows.h>
#include <stdio.h>
#include <string.h>
#include <ctype.h>
#include <math.h>
#include "..\h\SerialIO.h"
#include "..\h\PAcquire.h"
#include "..\h\Fnctns.h"

#define	PI	3.1415926535897932384626433832795

#define	UM_ERROR	(WM_USER + 1)



// Functions
int PASCAL NThreeSixty(int nAngle)	// Converts an integer angle to a value between -180 and 360
{
	while (nAngle >= 360)	// Subtract 360
		nAngle -= 360;

	while (nAngle <= -180)	// Add 360
		nAngle += 360;

	return nAngle;
}

int PASCAL DThreeSixty(double dAngle)	// Converts a double angle to a value between -180.0 and 360.0
{
	while (dAngle >= 360.0)	// Subtract 360.0
		dAngle -= 360.0;

	while (dAngle <= -180.0)	// Add 360.0
		dAngle += 360.0;

	return (int) dAngle;
}

int PASCAL Complement(int nAngle)	// Returns the complement of an angle
{
	if (nAngle >= 0)	// Positive angles
		nAngle = 90 - nAngle;
	else	// Negative angles
		nAngle = -90 - nAngle;

	return nAngle;
}

int PASCAL ChangeSign(int nAngle)	// Changes sign of an angle
{
	nAngle *= -1;

	return nAngle;
}

void PASCAL PrinterInfo(char achDvc[], char achDrv[], char achPrt[])	// Retrieve printer configuration information from WIN.INI
{
	char achTemp[ 256 ];
	char *p1, *p2;

	GetProfileString("windows", "device", ",,,", achTemp, sizeof( achTemp ) );
	p1 = achTemp;
	p2 = achDvc;	// Get the name of the device
	while (*p1 && *p1 != ',')
		*p2++ = *p1++;

	*p2 = '\0';	// End the string with a null character
	if (*p1)
		++p1;	// Skip the comma

	p2 = achDrv;	// Get the name of the driver
	while (*p1 && *p1 != ',' && *p1 != ' ')
		*p2++ = *p1++;

	*p2 = '\0';	// End the string with a null character
	if (*p1)
		++p1;	// Skip the comma

	p2 = achPrt;	// Get the name of the port
	while (*p1)
		*p2++ = *p1++;

	*p2 = '\0';	// End the string with a null character
}

void PASCAL TemporaryDirectory(char achTempDir[], int Size )	// Returns path to temporary directory as specified by the TEMP= command
{
	int nLength;
//	BYTE cDrive;
	LPSTR lpstr, String;

	// Determine directory and name for temporary files
	strcpy(achTempDir, "\0");	// Clear temporary directory string
	String = lpstr = GetEnvironmentStrings();	// Pointer to DOS environment string buffer
	do {	// Look for the string "TEMP=" or "temp="
		if ((*lpstr == 'T' || *lpstr == 't') && (*(lpstr + 1) == 'E' || *(lpstr + 1) == 'e') && (*(lpstr + 2) == 'M' || *(lpstr + 2) == 'm') && (*(lpstr + 3) == 'P' || *(lpstr + 3) == 'p') && (*(lpstr + 4) == '='))
			strcpy(achTempDir, lpstr + 5);	// Save path

	} while(*lpstr++ + *lpstr);
        FreeEnvironmentStrings( String );

	nLength = strlen(achTempDir);
	if (nLength <= 0) {	// If a temporary directory is not found
//		cDrive = GetTempDrive(0);	// Determine drive used for temporary files
//		wsprintf(achTempDir, "%c", cDrive); // Jamie
//		strcat(achTempDir, ":\\");	// Root directory
                GetTempPath( Size, achTempDir );
	}
	else {	// If a temporary directory is found
		lpstr = achTempDir;
		if (*(lpstr + nLength - 1) != '\\')
			strcat(achTempDir, "\\");	// Add a \ if necessary
	}
}

void PASCAL Wait(DWORD dwInterval)	// Sets cursor to an hourglass shape and loops for specified millisecond time interval
{
	DWORD dwDelay;
	HCURSOR hcrArrow, hcrWait;
  char outbuffer[255];

  sprintf( outbuffer, "waiting %d ms \n", dwInterval );
  OutputDebugString( outbuffer );

	hcrWait = LoadCursor(NULL, IDC_WAIT);	// Get handle to the hourglass cursor
	hcrArrow = SetCursor(hcrWait);	// Change cursor to an hourglass
	dwDelay = GetCurrentTime();	// Get start time
	while (GetCurrentTime() < (dwDelay + dwInterval)) {	// Loop for specified time interval
		Sleep( 1 );
		}

	SetCursor(hcrArrow);	// Change cursor back to an arrow
}

void PASCAL Pause(DWORD dwInterval)	// Loops for specified millisecond time interval
{
	DWORD dwDelay;

	dwDelay = GetCurrentTime();	// Get start time
	while (GetCurrentTime() < (dwDelay + dwInterval)) {	// Loop for specified time interval
		Sleep( 1 );
		}
}

BOOL PASCAL DigitsAndSign(char ach[])	// Returns true if string consists of just digits and an optional, single hyphen
{
	char *p;

	for (p = ach; *p != '\0'; p++) {	// Cycle through each character in the string
		if (!isdigit(*p)) {	// If a non-digit is found
			if (p != ach)	// If the non-digit is not the first character,
				return FALSE;
			else {	// If the non-digit is the first character,
				if (*p != '-')	// it must be a '-'
					return FALSE;
			}
		}
	}

	return TRUE;	// String must consists of just digits and one, optional hyphen
}

BOOL PASCAL DigitsPointAndSign(char ach[])	// Returns true if string consists of just digits, an optional single decimal point, and an optional sign
{
	char *p;
	BOOL bPoint = FALSE;

	for (p = ach; *p != '\0'; p++) {	// Cycle through each character in the string
		if (!isdigit(*p)) {	// If a non-digit is found
			if ((*p != '.') && (*p != '-'))	// The only non-digits allowed are a '.' and a '-'
				return FALSE;

			if ((*p == '-') && (p != ach))	// If a character other than the first is a hyphen,
				return FALSE;

			if ((*p == '.') && bPoint)	// If the non-digit is not a decimal point, or if it is a second decimal point,
				return FALSE;

			if ((*p == '.') && !bPoint)	// If the non-digit is the first decimal point,
				bPoint = TRUE;	// indicate its existence
		}
	}

	return TRUE;	// String must consists of just digits, one optional decimal point, and an optional sign
}

BOOL PASCAL ScientificAndSign(char ach[])	// Returns true if string consists of a number in proper scientific notation (e.g. -8.888e-08)
{
	char *p;
	BOOL bHyphen = FALSE;
	BOOL bPoint = FALSE;
	BOOL bE = FALSE;

	for (p = ach; *p != '\0'; p++) {	// Cycle through each character in the string
		if (!isdigit(*p)) {	// If a non-digit is found
			if ((*p != '.') && (*p != '-') && (*p != 'e'))	// The only non-digits allowed are '.', '-', and 'e'
				return FALSE;

			if ((*p == '.') && bPoint)	// If the non-digit is a second decimal point,
				return FALSE;

			if ((*p == '.') && !bPoint)	// If the non-digit is the first decimal point,
				bPoint = TRUE;	// indicate its existence

			if ((*p == 'e') && bE)	// If the non-digit is a second 'e',
				return FALSE;

			if ((*p == 'e') && !bE)	// If the non-digit is the first 'e',
				bE = TRUE;	// indicate its existence

			if ((*p == '-') && (p != ach) && bHyphen)	// If the non-digit is a third hyphen,
				return FALSE;

			if ((*p == '-') && (p != ach) && !bHyphen)	// The second hyphen cannot be the first character
				bHyphen = TRUE;	// Indicate that there is a second hyphen

			if ((*p == '.') && bE)	// The decimal point must come before the 'e'
				return FALSE;

			if ((*p == 'e') && bHyphen)	// The second hyphen must come after the 'e'
				return FALSE;
		}
	}

	return TRUE;	// String must consist of the appropriate scientific notation
}

BOOL PASCAL GoodSampleName(char ach[])	// Returns true if string consists of characters acceptable for a DOS file name
{
	char *p;

	for (p = ach; *p != '\0'; p++) {	// Cycle through each character in the string
		if (!isalnum(*p)) {	// If a non-alphabetic or a non-digit character is found
			if ((*p != '_') && (*p != '^') && (*p != '$') && (*p != '~') && (*p != '!') && (*p != '#') &&
          (*p != '%') && (*p != '&') && (*p != '-') && (*p != '{') && (*p != '}') && (*p != '(') && (*p != ' ') &&
          (*p != ')') && (*p != '@') && (*p != '\'') && (*p != '`') && (*p != ':') && (*p != '\\') && (*p != '.') )	// If the non-digit is not one of these characters,
				return FALSE;
		}
	}

	return TRUE;	// String must consists of just digits and one, optional hyphen
}

int PASCAL WriteSerial(HWND hwnd, SERIALIO *nComID, char ach[], int nLen)	// Returns the number of characters transmitted to the serial port
{
	DWORD nRetBytes;
	int   nErrorCode;
//	COMSTAT ComStat;

        nRetBytes = 0;                // Jamie
        if( nComID == (SERIALIO *) NULL ) return 0;
        if( !nComID->Write( nComID, ach, nLen, &nRetBytes ) ) {

//	nRetBytes = WriteComm(nComID, ach, nLen);	// Write command to device
//	if (nRetBytes < 0) {	// If there is an error

//		if (nComID == nMagComID)	// Identify the device
//			bMagComWrite = TRUE;	// Magnetometer
//		else if (nComID == nAFComID)
//			bAFComWrite = TRUE;	// AF demagnetizer
//		else if (nComID == nSHComID)
//			bSHComWrite = TRUE;	// Sample handler

//		nErrorCode = GetCommError(nComID, &ComStat);	// Get error and clear port
                nErrorCode = GetLastError();
		PostMessage(hwnd, UM_ERROR, nErrorCode, MAKELONG(1, nComID));	// Notify error routine
	}

	return nRetBytes;
}

int PASCAL ReadSerial(HWND hwnd, SERIALIO *nComID, char ach[], int nLen)	// Returns the number of characters actually read from the serial port
{
	DWORD nRetBytes;
	int   nErrorCode;
	COMSTAT ComStat;
        DWORD   dwErrorFlags;

	strcpy(ach, "\0");	// Clear read buffer

        nRetBytes = 0;                // Jamie
        if( nComID == (SERIALIO *) NULL ) return 0;
        if( !nComID->Read( nComID, ach, nLen, &nRetBytes ) ) {

//	nRetBytes = ReadComm(nComID, ach, nLen);	// Read device
//	FlushComm(nComID, 1);	// Clear the receive data queue
//	if (nRetBytes < 0) {	// If there is an error

//		if (nComID == nMagComID)	// Identify the device
//			bMagComRead = TRUE;	// Magnetometer
//		else if (nComID == nAFComID)
//			bAFComRead = TRUE;	// AF demagnetizer
//		else if (nComID == nSHComID)
//			bSHComRead = TRUE;	// Sample handler

//		nErrorCode = GetCommError(nComID, &ComStat);	// Get error and clear port
                nErrorCode = GetLastError();
		PostMessage(hwnd, UM_ERROR, nErrorCode, MAKELONG(2, nComID));	// Notify error routine
	}

	if (nRetBytes == 0)	// If no characters are read
//		GetCommError(nComID, &ComStat);	// Clear port
                ClearCommError( nComID->File, &dwErrorFlags, &ComStat );

	return nRetBytes;
}

BOOL PASCAL ConfirmMagStatus(HWND hwnd, SERIALIO *nComID, char achAxis[], char achParameter[])	// Returns true if status is confirmed
{
	int nCount = 0;
	char achCommand[ 128 ], achReply[ 1024 ], achMessage[ 128 ];

	// Set up message string
	strcpy(achMessage, achAxis);	// "X", "Y", or "Z"
	strcat(achMessage, " Axis:  ");

	// Confirm status command
	strcpy(achCommand, achAxis);	// "X", "Y", or "Z"
	strcat(achCommand, "SSA\r");	// Send Status All with a terminating carriage return

	// Request status information
	WriteSerial(hwnd, nComID, achCommand, 5);
	Wait(220);	// Wait
        memset( achReply, 0, sizeof( achReply ) );
	while ((ReadSerial(hwnd, nComID, achReply, sizeof( achReply ) ) < 1) && (nCount < 3)) {	// Read serial port a maximum of 3 times
		Sleep( 1 );
		nCount += 1;	// Increment counter
		}

	if (nCount == 3) {	// If read was not successful
		strcat(achMessage, "No Response");
		MessageBox(hwnd, achMessage, "Magnetometer Status Error", MB_ICONHAND | MB_OK);	// Notify user
		return FALSE;
	}

	if (strstr(achReply, achParameter) != NULL)	// If configuration is confirmed (e.g. "R1" is found in reply string)
		return TRUE;
	else {	// If configuration is not confirmed
		strcat(achMessage, achParameter);
		MessageBox(hwnd, achMessage, "Magnetometer Status Error", MB_ICONHAND | MB_OK);	// Notify user
		return FALSE;
	}
}

BOOL PASCAL ConfirmAFStatus(HWND hwnd, SERIALIO *nComID, char ach[])	// Returns true if status is confirmed
{
	int nCount = 0;
	char achReply[ 1024 ];

//	WriteSerial(hwnd, nComID, "DSS\r", 4);	// Request status information by sending command string

	WriteSerial(hwnd, nComID, "D", 1);	// Request status information by sending command character by character
	Wait(220);	// Wait
	WriteSerial(hwnd, nComID, "S", 1);
	Wait(220);	// Wait
	WriteSerial(hwnd, nComID, "S", 1);
	Wait(220);	// Wait
	WriteSerial(hwnd, nComID, "\r", 1);

	Wait(440);	// Wait
        memset( achReply, 0, sizeof( achReply ) );
	while ((ReadSerial(hwnd, nComID, achReply, sizeof( achReply ) ) < 1) && (nCount < 3)) {	// Read serial port a maximum of 3 times
		Wait(440);	// Wait
		nCount += 1;	// Increment counter
		}

	if (nCount >= 3) {	// If read was not successful
		MessageBox(hwnd, "No Response", "Degausser Status Error", MB_ICONHAND | MB_OK);	// Notify user
		return FALSE;
	}

	if (strstr(achReply, ach) != NULL)	// If configuration is confirmed
		return TRUE;
	else {	// If configuration is not confirmed
		MessageBox(hwnd, ach, "Degausser Status Error", MB_ICONHAND | MB_OK);	// Notify user
		return FALSE;
	}
}

BOOL PASCAL ConfirmSHStatus(HWND hwnd, SERIALIO *nComID, char achRegister[], char achContents[])	// Returns true if status is confirmed
{
	int nCount = 0;
	char achStatus[ 128 ], achMessage[ 128 ], achReply[ 1024 ];

#ifdef SH_HACKOUT
  return TRUE;
#else
	// Verify contents command
	strcpy(achStatus, "V");	// Verify
	if (strstr(achRegister, "O") != NULL)	// If axis selection is to be confirmed
		strcat(achStatus, "O");	// Append just an "O"
	else	// For all other confirmations
		strcat(achStatus, achRegister);	// Append contents of register string

	strcat(achStatus, ",");	// Sample handler commands must end with a comma character

	// Request status information
	WriteSerial(hwnd, nComID, achStatus, 3);
	Wait(110);	// Wait
        memset( achReply, 0, sizeof( achReply ) );
	while ((ReadSerial(hwnd, nComID, achReply, sizeof( achReply ) ) < 1) && (nCount < 3)) {	// Read serial port a maximum of 3 times
		Sleep( 1 );
		nCount += 1;	// Increment counter
		}

	if (nCount == 3) {	// If read was not successful
		MessageBox(hwnd, "No Response", "Handler Status Error", MB_ICONHAND | MB_OK);	// Notify user
		return FALSE;
	}

	// Confirm setting
	if (strstr(achRegister, "M") == NULL) {	// Confirm all settings except velocity (11/03/93 M6000 will set as 6023)
		if (strstr(achReply, achContents) != NULL)	// If configuration is confirmed (e.g. "1200" is found in "M1200")
			return TRUE;
		else {	// If configuration is not confirmed
			strcpy(achMessage, achRegister);	// Set up error notification
			strcat(achMessage, achContents);
			MessageBox(hwnd, achMessage, "Handler Status Error", MB_ICONHAND | MB_OK);	// Notify user
			MessageBox(hwnd, achReply, "achSHReply", MB_ICONHAND | MB_OK);	// Notify user
			return FALSE;
		}
	}
	return TRUE;
#endif
}
/*
BOOL PASCAL Translate(HWND hwnd, BOOL bPos, LONG lPos ) // Moves automatic sample handler translator to flag position, and returns TRUE when finished
{
	char achCount[8];
	LONG lCount;

	// Determine number of counts to travel
	if (bPos)	// If translator is already at the specified position
		return TRUE;

	if (bTHome)	// If translator is at the home position
		lCount = lPos;

	else if (bTLoad)	// If translator is at the load position
		lCount = lPos - lTLoad;

	else if (bTAFX)	// If translator is at the AF X coil position
		lCount = lPos - lTAFX;

	else if (bTAFY)	// If translator is at the AF Y coil position
		lCount = lPos - lTAFY;

	else if (bTAFZ)	// If translator is at the AF Z coil position
		lCount = lPos - lTAFZ;

	else if (bTBack)	// If translator is at the background position
		lCount = lPos - lTBack;

	else if (bTMeas)	// If translator is at the measurement position
		lCount = lPos - lTMeas;

	else if (bTRight)	// If translator is at the right limit position
		lCount = lPos;

	else if (bTLeft)	// If translator is at the left limit position
		lCount = lPos;

	// Select translation axis
	if (!SHSetParameter(hwnd, "O1,", "0"))	// If selection is not confirmed
		return FALSE;

	// If current position is one of the limit positions, move to home position first
	if (bTRight || bTLeft) {
		if (bTRight) {	// For right limit switch
			if (!SHSendCommand(hwnd, "-"))	// Set motor direction
				return FALSE;
		}
		else {	// For left limit switch
			if (!SHSendCommand(hwnd, "+"))	// Set motor direction
				return FALSE;
		}

		if (!SHSendCommand(hwnd, "H1"))	// Seek home sensor
			return FALSE;

		Wait(110);
		SHSendCommand(hwnd, "F%");	// Sample handler command
		Wait(110);
		while ((ReadSerial(hwnd, nSHComID, achSHReply, 80) < 1)) {
			Wait(440);
		}
	}

	// Set motor indexing direction (and velocity)
	if (lCount > 0L) {	// If movement is to be toward the magnetometer
		if (!SHSendCommand(hwnd, "+"))	// Set motor direction
			return FALSE;

		if (bTBack){	// If translator is at the background position
			if (!SHSetParameter(hwnd, "M", HandlerDData.achVelM))	// Set velocity as measurement
				return FALSE;
		}
	}

	if (lCount < 0L) {	// If movement is to be away from the magnetometer
		if (!SHSendCommand(hwnd, "-"))	// Set motor direction
			return FALSE;

		lCount *= -1L;	// Positive value for count
		if (bTBack){	// If translator is at the background position
			if (!SHSetParameter(hwnd, "M", HandlerDData.achVel))	// Set velocity as regular
				return FALSE;
		}
	}

	// Set number of counts to travel
	wsprintf(achCount, "%ld", lCount);	// Convert count value to a string	
	if (!SHSetParameter(hwnd, "N", achCount))	// If selection is not confirmed
		return FALSE;

	// Send translate command
	if ((lPos == 0) && (lCount != 0L)) {	// To home position
		if (!SHSendCommand(hwnd, "H1"))	// Start motor indexing
			return FALSE;
	}

	if (lPos != 0) {	// To any other position
		if (!SHSendCommand(hwnd, "G"))	// Start motor indexing
			return FALSE;
	}

	// Wait for translation to end
	Wait(110);
	SHSendCommand(hwnd, "F%");	// Sample handler command
	Wait(110);
	while ((ReadSerial(hwnd, nSHComID, achSHReply, 80) < 1)) {
		Wait(440);
	}

	// Clear position flags
	bTHome = FALSE;
	bTLoad = FALSE;
	bTAFX = FALSE;
	bTAFY = FALSE;
	bTAFZ = FALSE;
	bTBack = FALSE;
	bTMeas = FALSE;
	bTRight = FALSE;
	bTLeft = FALSE;

	return TRUE;
}

BOOL PASCAL Rotate(HWND hwnd, BOOL bPos, int nPos)      // Moves automatic sample handler rotator to flag position, and returns TRUE when finished
{
	char achCount[5];
	int nCount;

	// Determine number of counts to travel
	if (bPos)	// If translator is already at the specified position
		return TRUE;

	if (bRHome)	// If rotator is at the home or 0 degree position
		nCount = nPos;

	else if (bR90)	// If rotator is at the 90 degree position
		nCount = nPos - nR90;

	else if (bR180)	// If rotator is at the 180 degree position
		nCount = nPos - nR180;

	else if (bR270)	// If rotator is at the 270 degree position
		nCount = nPos - nR270;

	//if (nCount > 1000)	// The largest single rotation is limited to 180 degrees
		//nCount -= 2000;

	//if (nCount < -1000)	// The largest single rotation is limited to 180 degrees
		//nCount += 2000;

	// Select rotation axis
	if (!SHSetParameter(hwnd, "O1,", "1"))	// If selection is not confirmed
		return FALSE;

	// Set motor indexing direction
	if (nCount < 0) {	// If rotation is to be counterclockwise
		if (!SHSendCommand(hwnd, "-"))	// If command is not sent successfully
			return FALSE;

		nCount *= -1;	// Positive value for count
	}

	else {	// If rotation is to be clockwise
		if (!SHSendCommand(hwnd, "+"))	// If command is not sent successfully
			return FALSE;
	}

	// Set number of counts to travel
	if (nPos == 0) {	// If the 0 degree position is the destination
		if (!SHSendCommand(hwnd, "+H1"))	// Seek the home sensor to eliminate creep accumulation
			return FALSE;
	}
	else {	// For other destinations
		wsprintf(achCount, "%d", nCount);	// Convert count value to a string	
		if (!SHSetParameter(hwnd, "N", achCount))	// If selection is not confirmed
			return FALSE;

		if (!SHSendCommand(hwnd, "G"))	// Start rotation
			return FALSE;
	}

	// Wait for rotation to end
	Wait(110);
	SHSendCommand(hwnd, "F%");	// Sample handler command
	Wait(110);
	while ((ReadSerial(hwnd, nSHComID, achSHReply, 80) < 1)) {
		Wait(440);
	}

	// Clear position flags
	bRHome = FALSE;
	bR90 = FALSE;
	bR180 = FALSE;
	bR270 = FALSE;

	return TRUE;
}
*/
BOOL PASCAL DegaussCycle(HWND hwnd, SERIALIO *nComID, char achCoil[], double dAmplitude)	// Performs a Degausser Execute Ramp Cycle for the specified coil and amplitude, and returns TRUE if successful
{
//	int nRetBytes;
	WORD wAmp;
	DWORD dwDelay;
	double dAmp;
	char achCommand[ 128 ];
	char /*achChar[2],*/ achConfirm[ 128 ], achAmp[ 128 ], achdAmp[ 128 ], achReply[ 1024 ];
//	char *p1, *p2;
	BOOL bPause = TRUE;

	// Configure degausser for amplitude
	dAmp = dAmplitude * 10.0;	// Convert from mT to gauss
	sprintf(achdAmp, "%.0f", dAmplitude);	// String representation of amplitude in gauss
	wAmp = (WORD) dAmp;	// Convert double representation to a WORD value
	wsprintf(achAmp, "%04u", wAmp);	// Set up amplitude string
	strcpy(achCommand, "DCA");	// Setup command string
	strcat(achCommand, achAmp);	// Amplitude digits
	strcat(achCommand, "\r");
	WriteSerial(hwnd, nComID, achCommand, strlen(achCommand));	// Send command string
/*
	WriteSerial(hwnd, nComID, "D", 1);	// Send command character by character
	Wait(220);	// Wait
	WriteSerial(hwnd, nComID, "C", 1);
	Wait(220);	// Wait
	WriteSerial(hwnd, nComID, "A", 1);
	p2 = achChar;	// Set pointer
	for (p1 = achAmp; *p1 != '\0'; p1++) {	// Cycle through each amplitude character
		*p2 = *p1;	// Copy current amplitude character
		Wait(220);	// Wait
		WriteSerial(hwnd, nComID, achChar, 1);	// Send character
	}

	Wait(220);	// Wait
	WriteSerial(hwnd, nComID, "\r", 1);	// End command with a carriage return
*/
	Wait(440);	// Wait

	// Confirm amplitude configuration
	strcpy(achConfirm, achdAmp);	// Setup confirmation string
	if (!ConfirmAFStatus(hwnd, nComID, achConfirm))	// If amplitude configuration is not confirmed
		return FALSE;

	// Configure degausser for coil
	strcpy(achCommand, "DCC");	// Setup command string
	strcat(achCommand, achCoil);	// "X", "Y", or "Z"
	strcat(achCommand, "\r");
	WriteSerial(hwnd, nComID, achCommand, strlen(achCommand));	// Send command string
/*
	WriteSerial(hwnd, nComID, "D", 1);	// Send command character by character
	Wait(220);	// Wait
	WriteSerial(hwnd, nComID, "C", 1);
	Wait(220);	// Wait
	WriteSerial(hwnd, nComID, "C", 1);
	Wait(220);	// Wait
	WriteSerial(hwnd, nComID, achCoil, 1);	// "X", "Y", or "Z"
	Wait(220);	// Wait
	WriteSerial(hwnd, nComID, "\r", 1);
*/
	Wait(440);	// Wait

	// Confirm coil configuration
	strcpy(achConfirm, "C ");	// Setup confirmation string
	strcat(achConfirm, achCoil);
	if (!ConfirmAFStatus(hwnd, nComID, achConfirm))	// If coil configuration is not confirmed
		return FALSE;

	// Set up ramp cycle command
	WriteSerial(hwnd, nComID, "DERC\r", 5);	// Send command string
/*
	WriteSerial(hwnd, nComID, "D", 1);	// Send command character by character
	Wait(220);	// Wait
	WriteSerial(hwnd, nComID, "E", 1);
	Wait(220);	// Wait
	WriteSerial(hwnd, nComID, "R", 1);
	Wait(220);	// Wait
	WriteSerial(hwnd, nComID, "C", 1);
	Wait(220);	// Wait
	WriteSerial(hwnd, nComID, "\r", 1);
*/
	Wait(440);	// Wait

	dwDelay = GetCurrentTime();	// Limit time within while loop to 4 minutes
	// Monitor ramp cycle progress
	while (bPause && (GetCurrentTime() < (dwDelay + 240020UL))) {
//		Wait(1100);	// Wait
		Wait(440);	// Wait
                memset( achReply, 0, sizeof( achReply ) );
		ReadSerial(hwnd, nComID, achReply, sizeof( achReply ) );
		if (strstr(achReply, "DO") != NULL)	// If cycle is complete
			bPause = FALSE;	// Exit loop

		if (strstr(achReply, "ON") != NULL)	// If cycle is complete
			bPause = FALSE;	// Exit loop

		if (strstr(achReply, "NE") != NULL)	// If cycle is complete
			bPause = FALSE;	// Exit loop

		if (strstr(achReply, "TRACK ERROR") != NULL) {	// If cycle is unsuccessful
			bPause = FALSE;	// Exit loop
			MessageBox(hwnd, "TRACK ERROR", "Degausser Cycle Error", MB_ICONHAND | MB_OK);	// Notify user
		}

		if (strstr(achReply, "ZERO ERROR") != NULL) {	// If cycle is unsuccessful
			bPause = FALSE;	// Exit loop
			MessageBox(hwnd, "ZERO ERROR", "Degausser Cycle Error", MB_ICONHAND | MB_OK);	// Notify user
		}
/*
		if (!bPause) {	// If pause flag has not been set
			WriteSerial(hwnd, nComID, "DSS\r", 4);	// Request status
			Wait(440);	// Wait
			ReadSerial(hwnd, nComID, achReply, 31);
			if (strstr(achReply, "S Z") != NULL)	// If status is zero
				bPause = FALSE;	// Exit loop
			else
				FlushComm(nComID, 0);	// Clear transmit data queue
		}
*/
	}

	return TRUE;
}

BOOL PASCAL DegaussSetRamp(HWND hwnd, SERIALIO *nComID, DWORD dwRamp)	// Configures degausser for ramp, and returns TRUE if successful
{
	char achCommand[ 128 ];
	char achRamp[ 128 ], achConfirm[ 128 ];

	// Configure degausser for ramp
	switch (dwRamp) {	// Identify ramp value
		case 0UL:
			strcpy(achRamp, "3");
			break;			
		case 1UL:
			strcpy(achRamp, "5");
			break;			
		case 2UL:
			strcpy(achRamp, "7");
			break;			
		case 3UL:
			strcpy(achRamp, "9");
			break;			
		default:
			strcpy(achRamp, "3");
	}

//	strcpy(achCommand, "DCR");	// Setup command string
//	strcat(achCommand, achRamp);
//	strcat(achCommand, "\r");
//	WriteSerial(hwnd, nComID, achCommand, strlen(achCommand));	// Send command string

	WriteSerial(hwnd, nComID, "D", 1);	// Send command character by character
	Wait(220);	// Wait
	WriteSerial(hwnd, nComID, "C", 1);
	Wait(220);	// Wait
	WriteSerial(hwnd, nComID, "R", 1);
	Wait(220);	// Wait
	WriteSerial(hwnd, nComID, achRamp, strlen(achRamp));
	Wait(220);	// Wait
	WriteSerial(hwnd, nComID, "\r", 1);

	Wait(440);	// Wait

	// Confirm configuration
	strcpy(achConfirm, "R ");	// Setup confirmation string
	strcat(achConfirm, achRamp);
	if (!ConfirmAFStatus(hwnd, nComID, achConfirm))	// If ramp configuration is not confirmed
		return FALSE;

	return TRUE;
}

BOOL PASCAL DegaussSetDelay(HWND hwnd, SERIALIO *nComID, DWORD dwDelay)	// Configures degausser for delay, and returns TRUE if successful
{
	char achCommand[ 128 ], achDelay[ 128], achConfirm[ 128 ];

	// Configure degausser for delay
	switch (dwDelay) {	// Identify delay value
		case 0UL:
			strcpy(achDelay, "1");
			break;			
		case 1UL:
			strcpy(achDelay, "2");
			break;			
		case 2UL:
			strcpy(achDelay, "3");
			break;			
		case 3UL:
			strcpy(achDelay, "4");
			break;			
		case 4UL:
			strcpy(achDelay, "5");
			break;			
		case 5UL:
			strcpy(achDelay, "6");
			break;			
		case 6UL:
			strcpy(achDelay, "7");
			break;			
		case 7UL:
			strcpy(achDelay, "8");
			break;			
		case 8UL:
			strcpy(achDelay, "9");
			break;			
		default:
			strcpy(achDelay, "1");
	}

//	strcpy(achCommand, "DCD");	// Setup command string
//	strcat(achCommand, achDelay);
//	strcat(achCommand, "\r");
//	WriteSerial(hwnd, nComID, achCommand, strlen(achCommand));	// Send command string

	WriteSerial(hwnd, nComID, "D", 1);	// Send command character by character
	Wait(220);	// Wait
	WriteSerial(hwnd, nComID, "C", 1);
	Wait(220);	// Wait
	WriteSerial(hwnd, nComID, "D", 1);
	Wait(220);	// Wait
	WriteSerial(hwnd, nComID, achDelay, strlen(achDelay));
	Wait(220);	// Wait
	WriteSerial(hwnd, nComID, "\r", 1);

	Wait(440);	// Wait

	// Confirm configuration
	strcpy(achConfirm, "D ");	// Setup confirmation string
	strcat(achConfirm, achDelay);
	if (!ConfirmAFStatus(hwnd, nComID, achConfirm))	// If delay configuration is not confirmed
		return FALSE;

	return TRUE;
}

BOOL PASCAL SHSetParameter(HWND hwnd, SERIALIO *nComID, char achRegister[], char achContents[])	// Returns true if parameter is set successfully
{
	char achCommand[ 128 ];

#ifdef SH_HACKOUT
  return TRUE;
#else
  // Set up command
	strcpy(achCommand, achRegister);	// Register "A", "D", "M", "N", or "O1,"
	strcat(achCommand, achContents);	// Contents "20", "2000", etc.
	strcat(achCommand, ",");	// Sample handler commands must end with a comma character

	// Send command
	WriteSerial(hwnd, nComID, achCommand, strlen(achCommand));
	Wait(110);	// Wait before sending another command
	if (!ConfirmSHStatus(hwnd, nComID, achRegister, achContents))	// If parameter configuration is not confirmed
		return FALSE;

	return TRUE;
#endif
}

BOOL PASCAL SHSendCommand(HWND hwnd, SERIALIO *nComID, char ach[])	// Returns true if command is sent successfully
{
	char achCommand[ 128 ];

#ifdef SH_HACKOUT
  return TRUE;
#else
	// Set up command
	strcpy(achCommand, ach);	// Command "@0", "+", "-", "S", "Q", "%", or "H1"
	strcat(achCommand, ",");	// Sample handler commands must end with a comma character

	// Send command
	if ((WriteSerial(hwnd, nComID, achCommand, strlen(achCommand))) > 0) {	// If write is successful
		Wait(110);	// Wait before sending another command
		return TRUE;
	}
	else	// If write is not successful
		return FALSE;
#endif
}

BOOL PASCAL MagSetRange(HWND hwnd, SERIALIO *nComID, char achAxis[], DWORD dwRange)	// Configures specified axis for range, and returns TRUE if successful
{
	char achRange[ 128 ], achCommand[ 128 ];

	// Configure range
	switch (dwRange) {	// Identify range value
		case 0UL:	// 1X
			strcpy(achRange, "R1");
			break;			
		case 1UL:	// 10X
			strcpy(achRange, "RT");
			break;			
		case 2UL:	// 100X
			strcpy(achRange, "RH");
			break;			
		case 3UL:	// 200X
			strcpy(achRange, "RE");
			break;			
		default:	// 1X
			strcpy(achRange, "R1");
	}

	strcpy(achCommand, achAxis);	// "X", "Y", or "Z"
	strcat(achCommand, "C");	// Configure
	strcat(achCommand, achRange);	// Range string
	strcat(achCommand, "\r");	// Carriage return terminates all magnetometer commands

	// Send range configure command
	WriteSerial(hwnd, nComID, achCommand, 5);
	Wait(110);	// Wait before sending another command
	if (!ConfirmMagStatus(hwnd, nComID, achAxis, achRange))	// If range configuration is not confirmed
		return FALSE;

	return TRUE;
}

BOOL PASCAL MagSetFilter(HWND hwnd, SERIALIO *nComID, char achAxis[], DWORD dwFilter)	// Configures specified axis for filter, and returns TRUE if successful
{
	char achFilter[ 128 ], achCommand[ 128 ];

	// Configure filter
	switch (dwFilter) {	// Identify filter value
		case 0UL:	// 1Hz
			strcpy(achFilter, "F1");
			break;			
		case 1UL:	// 10Hz
			strcpy(achFilter, "FT");
			break;			
		case 2UL:	// 100Hz
			strcpy(achFilter, "FH");
			break;			
		case 3UL:	// WB
			strcpy(achFilter, "FW");
			break;			
		default:	// 1Hz
			strcpy(achFilter, "F1");
	}

	strcpy(achCommand, achAxis);	// "X", "Y", or "Z"
	strcat(achCommand, "C");	// Configure
	strcat(achCommand, achFilter);	// Filter string
	strcat(achCommand, "\r");	// Carriage return terminates all magnetometer commands

	// Send filter configure command
	WriteSerial(hwnd, nComID, achCommand, 5);
	Wait(110);	// Wait before sending another command
	if (!ConfirmMagStatus(hwnd, nComID, achAxis, achFilter))	// If filter configuration is not confirmed
		return FALSE;

	return TRUE;
}

BOOL PASCAL MagSetSlew(HWND hwnd, SERIALIO *nComID, char achAxis[], DWORD dwSlew)	// Configures specified axis for fast slew option, and returns TRUE if successful
{
	char achSlew[ 128 ], achCommand[ 128 ];

	// Configure fast slew option
	if (dwSlew)	// Enable fast slew
		strcpy(achSlew, "SE");
	else	// Disable fast slew
		strcpy(achSlew, "SD");

	strcpy(achCommand, achAxis);	// "X", "Y", or "Z"
	strcat(achCommand, "C");	// Configure
	strcat(achCommand, achSlew);	// Slew string
	strcat(achCommand, "\r");	// Carriage return terminates all magnetometer commands

	// Send slew configure command
	WriteSerial(hwnd, nComID, achCommand, 5);
	Wait(110);	// Wait before sending another command
	if (!ConfirmMagStatus(hwnd, nComID, achAxis, achSlew))	// If slew configuration is not confirmed
		return FALSE;

	return TRUE;
}

//BOOL PASCAL MagSetPanel(HWND hwnd, SERIALIO *nComID, char achAxis[], DWORD dwPanel)	// Configures specified axis for front panel option, and returns TRUE if successful
//{
//	char achPanel[3], achCommand[6];
//
//	// Configure front panel option
//	if (dwPanel)	// Lock front panel
//		strcpy(achPanel, "PL");
//	else	// Unlock front panel
//		strcpy(achPanel, "PU");
//
//	strcpy(achCommand, achAxis);	// "X", "Y", or "Z"
//	strcat(achCommand, "C");	// Configure
//	strcat(achCommand, achPanel);	// Panel string
//	strcat(achCommand, "\r");	// Carriage return terminates all magnetometer commands
//
//	// Send panel configure command
//	WriteSerial(hwnd, nComID, achCommand, 5);
//	Wait(110);	// Wait before sending another command
//	if (!ConfirmMagStatus(hwnd, nComID, achAxis, achPanel))	// If panel configuration is not confirmed
//		return FALSE;
//
//	return TRUE;
//}

BOOL PASCAL MagResetCount(HWND hwnd, SERIALIO *nComID, char achAxis[])	// Resets flux counter to zero for specified axis, and returns TRUE if successful
{
	char achCommand[ 128 ];


	// Reset command
	strcpy(achCommand, achAxis);	// "A", "X", "Y", or "Z"
	strcat(achCommand, "CLC\r");	// Close the Feedback loop.

	// Send latch feedback loop command
	WriteSerial(hwnd, nComID, achCommand, strlen( achCommand ) );


  memset( achCommand, 0, sizeof( achCommand ) );

	// Reset command
	strcpy(achCommand, achAxis);	// "A", "X", "Y", or "Z"
	strcat(achCommand, "RC\r");	// Reset Counter plus a terminating carriage return

	// Send reset command
	if ((WriteSerial(hwnd, nComID, achCommand, 4)) > 0)	// If write is successful
		return TRUE;
	else	// If write is unsuccessful
		return FALSE;
}

BOOL PASCAL MagPulseLoop(HWND hwnd, SERIALIO *nComID, char achAxis[])	// Opens then closes feedback loop for specified axis, and returns TRUE if successful
{
	char achCommand[ 128 ];

	// Pulse command
	strcpy(achCommand, achAxis);	// "A", "X", "Y", or "Z"
	strcat(achCommand, "CLP\r");	// Configure Loop Pulse plus a terminating carriage return

	// Send pulse command
	if ((WriteSerial(hwnd, nComID, achCommand, 5)) > 0)	// If write is successful
		return TRUE;
	else	// If write is unsuccessful
		return FALSE;
}

BOOL PASCAL MagLatchCount(HWND hwnd, SERIALIO *nComID, char achAxis[])	// Latches flux counter for the specified axis, and returns TRUE if successful
{
	char achCommand[ 128 ];

	// Latch counter command
	strcpy(achCommand, achAxis);	// "A", "X", "Y", or "Z"
	strcat(achCommand, "LC\r");	// Latch Counter plus a terminating carriage return

	// Send latch counter command
	if ((WriteSerial(hwnd, nComID, achCommand, 4)) > 0) {	// If write is successful
		Wait(220);	// Wait
		return TRUE;
	}
	else	// If write is unsuccessful
		return FALSE;
}

BOOL PASCAL MagLatchData(HWND hwnd, SERIALIO *nComID, char achAxis[])	// Latches analog data for the specified axis, and returns TRUE if successful
{
	char achCommand[ 128 ];

	// Latch analog data command
	strcpy(achCommand, achAxis);	// "A", "X", "Y", or "Z"
	strcat(achCommand, "LD\r");	// Latch Data plus a terminating carriage return

	// Send latch analog data command
	if ((WriteSerial(hwnd, nComID, achCommand, 4)) > 0) {	// If write is successful
		Wait(770);	// Wait for analog data to be latched
//		Wait(990);	// Wait for analog data to be latched
		return TRUE;
	}
	else	// If write is unsuccessful
		return FALSE;
}



int ReadNChars( HWND hwnd, SERIALIO *nComID, int AmtToRead, char *Buffer ) {

  int numread = 0;
  int totalread;
  int errorread = 0;

  numread = ReadSerial(hwnd, nComID, Buffer, AmtToRead );
  totalread = numread;

  while( totalread < AmtToRead ) {
    Sleep(100);
    numread = ReadSerial(hwnd, nComID, Buffer+totalread, AmtToRead-totalread );
    totalread += numread;
    if( numread == 0 )  
      errorread++;
    if( errorread == 5 )
      break;
    }
  return totalread; 
  }



int PASCAL MagSendCount(HWND hwnd, SERIALIO *nComID, char achAxis[])	// Reads flux counter for the specified axis, and returns the int value
{
	int nCount, nRead = 0;
	char achMessage[ 128 ], achCommand[ 128 ], achReply[ 1024 ];
  char trashbuffer[1024];

	// Set up message string
	strcpy(achMessage, achAxis);	// "X", "Y", or "Z"
	strcat(achMessage, " Axis:  ");

	// Send counter command
	strcpy(achCommand, achAxis);	// "X", "Y", or "Z"
	strcat(achCommand, "SC\r");	// Send Counter plus a terminating carriage return

  ReadSerial( hwnd, nComID, trashbuffer, sizeof( trashbuffer ) );    // try flushing out anything left in the buffer.

	// Write send counter command
	WriteSerial(hwnd, nComID, achCommand, 4);
  Sleep(500);
	Wait(220);	// Wait
  memset( achReply, 0, sizeof( achReply ) );


  if( ReadNChars( hwnd, nComID, 7, achReply ) < 7 ) {
		strcat(achMessage, "Counter");
		MessageBox(hwnd, achMessage, "Magnetometer Read Error", MB_ICONHAND | MB_OK);	// Notify user
		return 0;
    }
  

#if 0
  while ((ReadSerial(hwnd, nComID, achReply, sizeof( achReply ) ) < 1) && (nRead < 3)) {	// Read serial port a maximum of 3 times
		Sleep( 100 );
		nRead += 1;	// Increment read index
		}

	if ( (nRead == 3) || (strlen( achReply ) < 7) ) {	// If read was not successful
		strcat(achMessage, "Counter");
		MessageBox(hwnd, achMessage, "Magnetometer Read Error", MB_ICONHAND | MB_OK);	// Notify user
		return 0;
	}
#endif

//MessageBox(hwnd, achReply, "Count", MB_ICONHAND | MB_OK);	// Notify user

	nCount = atoi(achReply);	// Convert counter string to an int value
	return nCount;
}

double PASCAL MagSendData(HWND hwnd, SERIALIO *nComID, char achAxis[])	// Reads analog data for the specified axis, and returns the double value
{
	int nRead = 0;
	double dAnalog;
	char achMessage[ 128 ], achCommand[ 128 ], achReply[ 1024 ];

	// Set up message string
	strcpy(achMessage, achAxis);	// "X", "Y", or "Z"
	strcat(achMessage, " Axis:  ");

	// Send data command
	strcpy(achCommand, achAxis);	// "X", "Y", or "Z"
	strcat(achCommand, "SD\r");	// Send Data plus a terminating carriage return

	// Write send data command
	WriteSerial(hwnd, nComID, achCommand, 4);
	Wait(220);	// Wait
  Sleep(500);
  memset( achReply, 0, sizeof( achReply ) );
	while ((ReadSerial(hwnd, nComID, achReply, sizeof( achReply ) ) < 1) && (nRead < 3)) {	// Read serial port a maximum of 3 times
		Sleep( 100 );
		nRead += 1;	// Increment read index
		}

	if (nRead == 3) {	// If read was not successful
		strcat(achMessage, "Analog Data");
		MessageBox(hwnd, achMessage, "Magnetometer Read Error", MB_ICONHAND | MB_OK);	// Notify user
		return 0.0;
	}
//MessageBox(hwnd, achReply, "Analog", MB_ICONHAND | MB_OK);	// Notify user

	dAnalog = atof(achReply);	// Convert analog data string to a double value
	return dAnalog;
}

double PASCAL CombineCountAndData(int nCount, double dData, char achAxis[], BOOL bFlux, BOOL bDC, DWORD dwRange, double dXCalib, double dYCalib, double dZCalib)	// Combines count and analog data for the specified axis, and returns the double value in emu
{
	double dConstant, dCount, dDivisor, dCombined;

	// Convert count to a double value
	dCount = (double) nCount;

	// Select appropriate calibation constant in emu per flux quantum
	if (strstr(achAxis, "X") != NULL)	// For the X axis
		dConstant = dXCalib;

	else if (strstr(achAxis, "Y") != NULL)	// For the Y axis
		dConstant = dYCalib;

	else if (strstr(achAxis, "Z") != NULL)	// For the Z axis
		dConstant = dZCalib;


/*   New DC Squids have a 1 fenon to 1 volt range.    */
/*   May need to add switch to the setup panel.       */
/*   2004.05.10 DEH                                   */

#if 0   //DeadCode For now we only use DC Squids with a Divisor of 1

  // Select appropriate divisor for the analog data
	if (!bDC && dwRange == 3UL)	// Extended range with RF squids
		dDivisor = 10.0;
	else
		dDivisor = 2.0;
*/
  // Select appropriate divisor for the analog data
//	if (!bDC && dwRange == 3UL)	// Extended range with RF squids
//		dDivisor = 10.0;
//	else
//		dDivisor = 1.0;
/*
	switch (dwRange) {	// Identify range setting
		case 0UL:	// 1X
			dDivisor = 2.0;
			break;
		case 1UL:	// 10X
			dDivisor = 2.0;
			break;			
		case 2UL:	// 100X
			dDivisor = 2.0;
			break;			
		case 3UL:	// ER
			dDivisor = 10.0;
	}
*/
	// Flux quanta in analog data
  //   
  dCombined = dData / dDivisor;

#endif //DeadCode For now we only use DC Squids with a Divisor of 1

  dCombined = dData;

 	// For flux counting, combine analog data and count
	if (bFlux)
		dCombined += dCount;

	// Convert flux quanta to emu
	dCombined *= dConstant;

	return dCombined;
}
/*
BOOL PASCAL MagRangeUp(HWND hwnd, char achAxis[])       // Increases range setting for specified magnetometer axis, and returns TRUE if successful
{
	// Increment the global range variable for the appropriate magnetometer axis
	if (strstr(achAxis, "X") != NULL) {	// For the X axis
		if (!bXFlux) {	// For ranges other than flux counting
			MagnetometerDData.dwXRange += 1UL;
			if (MagnetometerDData.dwXRange > 3UL) {	// From extended range, set flux counting
				MagnetometerDData.dwXRange = 0UL;
				bXFlux = TRUE;	// Set global flux counting flag for the axis
			}

			MagSetRange(hwnd, achAxis, MagnetometerDData.dwXRange);	// Set new range
			return TRUE;
		}
		else {	// If current range is flux counting, sample is too strong to measure
			MessageBox(hwnd, "Sample Too Strong To Measure", "Range Up Error", MB_ICONHAND | MB_OK);	// Notify user
			return FALSE;
		}
	}

	else if (strstr(achAxis, "Y") != NULL) {	// For the Y axis
		if (!bYFlux) {	// For ranges other than flux counting
			MagnetometerDData.dwYRange += 1UL;
			if (MagnetometerDData.dwYRange > 3UL) {	// From extended range, set flux counting
				MagnetometerDData.dwYRange = 0UL;
				bYFlux = TRUE;	// Set global flux counting flag for the axis
			}

			MagSetRange(hwnd, achAxis, MagnetometerDData.dwYRange);	// Set new range
			return TRUE;
		}
		else {	// If current range is flux counting, sample is too strong to measure
			MessageBox(hwnd, "Sample Too Strong To Measure", "Range Up Error", MB_ICONHAND | MB_OK);	// Notify user
			return FALSE;
		}
	}

	else if (strstr(achAxis, "Z") != NULL) {	// For the Z axis
		if (!bZFlux) {	// For ranges other than flux counting
			MagnetometerDData.dwZRange += 1UL;
			if (MagnetometerDData.dwZRange > 3UL) {	// From extended range, set flux counting
				MagnetometerDData.dwZRange = 0UL;
				bZFlux = TRUE;	// Set global flux counting flag for the axis
			}

			MagSetRange(hwnd, achAxis, MagnetometerDData.dwZRange);	// Set new range
			return TRUE;
		}
		else {	// If current range is flux counting, sample is too strong to measure
			MessageBox(hwnd, "Sample Too Strong To Measure", "Range Up Error", MB_ICONHAND | MB_OK);	// Notify user
			return FALSE;
		}
	}

	return FALSE;
}

void PASCAL MagRangeDown(HWND hwnd, char achAxis[])     // Decreases range setting for specified magnetometer axis
{
	// Decrement the global range variable for the appropriate magnetometer axis
	if (strstr(achAxis, "X") != NULL) {	// For the X axis
		if (!bXFlux) {	// For ranges other than flux counting
			if (MagnetometerDData.dwXRange >= 1UL)	// Cannot decrease range setting from 1X
				MagnetometerDData.dwXRange -= 1UL;

			MagSetRange(hwnd, achAxis, MagnetometerDData.dwXRange);	// Set new range
		}
	}

	else if (strstr(achAxis, "Y") != NULL) {	// For the Y axis
		if (!bYFlux) {	// For ranges other than flux counting
			if (MagnetometerDData.dwYRange >= 1UL)	// Cannot decrease range setting from 1X
				MagnetometerDData.dwYRange -= 1UL;

			MagSetRange(hwnd, achAxis, MagnetometerDData.dwYRange);	// Set new range
		}
	}

	else if (strstr(achAxis, "Z") != NULL) {	// For the Z axis
		if (!bZFlux) {	// For ranges other than flux counting
			if (MagnetometerDData.dwZRange >= 1UL)	// Cannot decrease range setting from 1X
				MagnetometerDData.dwZRange -= 1UL;

			MagSetRange(hwnd, achAxis, MagnetometerDData.dwZRange);	// Set new range
		}
	}
}
*/
BOOL PASCAL MyTranslateMessage(HWND hwnd, char achMessage[])	// Notifies user to translate manual sample handler
{
	int nReturn;
	char ach[ 128 ];

	strcpy(ach, "To ");	// Set up message string
	strcat(ach, achMessage);
	strcat(ach, " Position");

//	MessageBox(hwnd, ach, "Translate Sample Handler", MB_ICONEXCLAMATION | MB_OK);
	nReturn = MessageBox(hwnd, ach, "Translate Sample Handler", MB_ICONEXCLAMATION | MB_OKCANCEL);
	if (nReturn == IDOK)	// Ok
		return FALSE;
	else	// Cancel
		return TRUE;
}

BOOL PASCAL RotateMessage(HWND hwnd, char achMessage[])	// Notifies user to rotate manual sample handler
{
	int nReturn;
	char ach[ 128 ];

	strcpy(ach, "To ");	// Set up message string
	strcat(ach, achMessage);
	strcat(ach, " Position");

//	MessageBox(hwnd, ach, "Rotate Sample Handler", MB_ICONEXCLAMATION | MB_OK);
	nReturn = MessageBox(hwnd, ach, "Rotate Sample Handler", MB_ICONEXCLAMATION | MB_OKCANCEL);
	if (nReturn == IDOK)	// Ok
		return FALSE;
	else	// Cancel
		return TRUE;
}

void PASCAL LoadMessage(HWND hwnd, char achMessage[])	// Notifies user to load sample
{
	MessageBox(hwnd, achMessage, "Load Sample", MB_ICONEXCLAMATION | MB_OK);
}

void PASCAL NotifyMessage(HWND hwnd, char achMessage[])	// Notifies user that a threshold value was not met
{
	MessageBox(hwnd, "Threshold not met", achMessage, MB_ICONEXCLAMATION | MB_OK);
}

void PASCAL NormalSums(double dXSignal[], double dYSignal[], double dZSignal[], int nCount, BOOL bXAxis, BOOL bYAxis, BOOL bZAxis, BOOL bNegativeZ, double dSums[])	// Returns normal statistic sums for each component
{
	// Parameter descriptions
	// Input:
	// dXSignal[] - Magnetometer X axis measurements
	//	Positive Z position (bNegativeZ = FALSE)	Negative Z position (bNegativeZ = TRUE)
	//	dXSignal[0] = +x of sample			dXSignal[0] = +x of sample
	//	dXSignal[1] = -y				dXSignal[1] = +y
	//	dXSignal[2] = -x				dXSignal[2] = -x
	//	dXSignal[3] = +y				dXSignal[3] = -y
	//	dXSignal[4] = +x				dXSignal[4] = +x
	//	etc.						etc.
	// dYSignal[] - Magnetometer Y axis measurements
	//	Positive Z position (bNegativeZ = FALSE)	Negative Z position (bNegativeZ = TRUE)
	//	dYSignal[0] = +y of sample			dYSignal[0] = -y of sample
	//	dYSignal[1] = +x				dYSignal[1] = +x
	//	dYSignal[2] = -y				dYSignal[2] = +y
	//	dYSignal[3] = -x				dYSignal[3] = -x
	//	dYSignal[4] = +y				dYSignal[4] = -y
	//	etc.						etc.
	// dZSignal[] - Magnetometer Z axis measurements
	//	Positive Z position (bNegativeZ = FALSE)	Negative Z position (bNegativeZ = TRUE)
	//	dZSignal[0] = +z of sample			dZSignal[0] = -z of sample
	//	dZSignal[1] = +z				dZSignal[1] = -z
	//	dZSignal[2] = +z				dZSignal[2] = -z
	//	dZSignal[3] = +z				dZSignal[3] = -z
	//	dZSignal[4] = +z				dZSignal[4] = -z
	//	etc.						etc.
	// Output:
	// dSums[] - Will contain statistic sums and count for each component
	//	dSums[0] = Sum of sample X components
	//	dSums[1] = Sum of the squares of the sample X components
	//	dSums[2] = Total number of sample X components
	//	dSums[3] = Sum of sample Y components
	//	dSums[4] = Sum of the squares of the sample Y components
	//	dSums[5] = Total number of sample Y components
	//	dSums[6] = Sum of sample Z components
	//	dSums[7] = Sum of the squares of the sample Z components
	//	dSums[8] = Total number of sample Z components

	int i;	// Index
	double dXSign1 = -1.0, dXSign2 = -1.0;	// Alternating signs
	double dYSign1 = 1.0, dYSign2 = -1.0, dYSign3 = -1.0, dYSign4 = 1.0;	// Alternating signs
	double dXCount = 0.0, dYCount = 0.0, dZCount = 0.0;	// Numbers constituting sums for each component
	double dSampleXSum = 0.0, dSampleXSum2 = 0.0;	// Sample X component sums
	double dSampleYSum = 0.0, dSampleYSum2 = 0.0;	// Sample Y component sums
	double dSampleZSum = 0.0, dSampleZSum2 = 0.0;	// Sample Z component sums

	for (i = 0; i <= nCount; i++) {	// Loop through each value in the signal arrays
		if (bXAxis) {	// Magnetometer X axis signal
			if (!(i % 2)) {	// If index is even
				dXSign1 *= -1.0;	// Alternating sign starts positive
				dSampleXSum += dXSignal[i] * dXSign1;	// Accumulate sample X component sum while correcting for sign
				dSampleXSum2 += dXSignal[i] * dXSignal[i];	// Accumulate sum of sample X component squared
				dXCount += 1.0;	// Increment counter
			}
			else {	// If index is odd
				if (bNegativeZ) {	// If sample has been rotated about its +X axis in order to measure along is -Z axis
					dYSign2 *= -1.0;	// Alternating sign starts positive
					dSampleYSum += dXSignal[i] * dYSign2;	// Accumulate sample Y component sum while correcting for sign
				}
				else {	// If sample is in its normal position to measure along its +Z axis
					dYSign1 *= -1.0;	// Alternating sign starts negative
					dSampleYSum += dXSignal[i] * dYSign1;	// Accumulate sample Y component sum while correcting for sign
				}

				dSampleYSum2 += dXSignal[i] * dXSignal[i];	// Accumulate sum of sample Y component squared
				dYCount += 1.0;	// Increment counter
			}
		}

		if (bYAxis) {	// Magnetometer Y axis signal
			if (i % 2) {	// If index is odd
				dXSign2 *= -1.0;	// Alternating sign starts positive
				dSampleXSum += dYSignal[i] * dXSign2;	// Accumulate sample X component sum while correcting for sign
				dSampleXSum2 += dYSignal[i] * dYSignal[i];	// Accumulate sum of sample X component squared
				dXCount += 1.0;	// Increment counter
			}
			else {	// If index is even
				if (bNegativeZ) {	// If sample has been rotated about its +X axis in order to measure along is -Z axis
					dYSign4 *= -1.0;	// Alternating sign starts negative
					dSampleYSum += dYSignal[i] * dYSign4;	// Accumulate sample Y component sum while correcting for sign
				}
				else {	// If sample is in its normal position to measure along its +Z axis
					dYSign3 *= -1.0;	// Alternating sign starts positive
					dSampleYSum += dYSignal[i] * dYSign3;	// Accumulate sample Y component sum while correcting for sign
				}

				dSampleYSum2 += dYSignal[i] * dYSignal[i];	// Accumulate sum of sample Y component squared
				dYCount += 1.0;	// Increment counter
			}
		}

		if (bZAxis) {	// Magnetometer Z axis signal
			if (bNegativeZ)	// If sample has been rotated about its +X axis in order to measure along is -Z axis
				dSampleZSum += -1.0 * dZSignal[i];	// Accumulate sample Z component sum while correcting for sign
			else	// If sample is in its normal position to measure along its +Z axis
				dSampleZSum += dZSignal[i];	// Accumulate sample Z component sum

			dSampleZSum2 += dZSignal[i] * dZSignal[i];	// Accumulate sum of sample Z component squared
			dZCount += 1.0;	// Increment counter
		}
	}

	// Load resulting sums and counts into the return array
	dSums[0] = dSampleXSum;	// Sum of sample X components
	dSums[1] = dSampleXSum2;	// Sum of the squares of the sample X components
	dSums[2] = dXCount;	// Number of additions in sums
	dSums[3] = dSampleYSum;	// Sum of sample Y components
	dSums[4] = dSampleYSum2;	// Sum of the squares of the sample Y components
	dSums[5] = dYCount;	// Number of additions in sums
	dSums[6] = dSampleZSum;	// Sum of sample Z components
	dSums[7] = dSampleZSum2;	// Sum of the squares of the sample Z components
	dSums[8] = dZCount;	// Number of additions in sums
}

void PASCAL NormalStatistics(double dSums[], double dStatistics[])	// Returns the mean and standard deviation for each sample component of magnetization
{
	// Parameter descriptions
	// Input:
	// dSums[] - Will contain statistic sums and count for each component
	//	dSums[0] = Sum of sample X components
	//	dSums[1] = Sum of the squares of the sample X components
	//	dSums[2] = Total number of sample X components
	//	dSums[3] = Sum of sample Y components
	//	dSums[4] = Sum of the squares of the sample Y components
	//	dSums[5] = Total number of sample Y components
	//	dSums[6] = Sum of sample Z components
	//	dSums[7] = Sum of the squares of the sample Z components
	//	dSums[8] = Total number of sample Z components
	// Output:
	//	dStatistics[0] = Mean of sample X components
	//	dStatistics[1] = Standard deviation of sample X components
	//	dStatistics[2] = Mean of sample Y components
	//	dStatistics[3] = Standard deviation of sample Y components
	//	dStatistics[4] = Mean of sample Z components
	//	dStatistics[5] = Standard deviation of sample Z components

	// Load means and standard deviations into the return array
	dStatistics[0] = dSums[0] / dSums[2];	// Mean of sample X components
	if (dSums[2] > 1.0) {
		dStatistics[1] = (dSums[2] * dSums[1] - dSums[0] * dSums[0]) / (dSums[2] * (dSums[2] - 1.0));
		if (dStatistics[1] > 0.0)
			dStatistics[1] = sqrt(dStatistics[1]);	// Standard deviation of sample X components
		else
			dStatistics[1] = 0.0;	// Standard deviation of sample X components
	}
	else
		dStatistics[1] = 0.0;	// Standard deviation of sample X components

	dStatistics[2] = dSums[3] / dSums[5];	// Mean of sample Y components
	if (dSums[5] > 1.0) {
		dStatistics[3] = (dSums[5] * dSums[4] - dSums[3] * dSums[3]) / (dSums[5] * (dSums[5] - 1.0));	// Standard deviation of sample Y components
		if (dStatistics[3] > 0.0)
			dStatistics[3] = sqrt(dStatistics[3]);	// Standard deviation of sample Y components
		else
			dStatistics[3] = 0.0;	// Standard deviation of sample Y components
	}
	else
		dStatistics[3] = 0.0;	// Standard deviation of sample Y components

	dStatistics[4] = dSums[6] / dSums[8];	// Mean of sample Z components
	if (dSums[8] > 1.0) {
		dStatistics[5] = (dSums[8] * dSums[7] - dSums[6] * dSums[6]) / (dSums[8] * (dSums[8] - 1.0));	// Standard deviation of sample Z components
		if (dStatistics[5] > 0.0)
			dStatistics[5] = sqrt(dStatistics[5]);	// Standard deviation of sample Z components
		else
			dStatistics[5] = 0.0;	// Standard deviation of sample Z components
	}
	else
		dStatistics[5] = 0.0;	// Standard deviation of sample Z components
}

void PASCAL OffsetAndDrift(double dXSignal[], double dYSignal[], double dZSignal[], int nCount, BOOL bXAxis, BOOL bYAxis, BOOL bZAxis, double dBackground[])	// Applies linear drift offset correction to magnetometer data
{
	// Parameter descriptions
	// Input:
	// dXSignal[] - Magnetometer X axis measurements
	//	Positive Z position				Negative Z position	
	//	dXSignal[0] = +x of sample			dXSignal[0] = +x of sample
	//	dXSignal[1] = -y				dXSignal[1] = +y
	//	dXSignal[2] = -x				dXSignal[2] = -x
	//	dXSignal[3] = +y				dXSignal[3] = -y
	//	dXSignal[4] = +x				dXSignal[4] = +x
	//	etc.						etc.
	// dYSignal[] - Magnetometer Y axis measurements
	//	Positive Z position				Negative Z position	
	//	dYSignal[0] = +y of sample			dYSignal[0] = -y of sample
	//	dYSignal[1] = +x				dYSignal[1] = +x
	//	dYSignal[2] = -y				dYSignal[2] = +y
	//	dYSignal[3] = -x				dYSignal[3] = -x
	//	dYSignal[4] = +y				dYSignal[4] = -y
	//	etc.						etc.
	// dZSignal[] - Magnetometer Z axis measurements
	//	Positive Z position				Negative Z position
	//	dZSignal[0] = +z of sample			dZSignal[0] = -z of sample
	//	dZSignal[1] = +z				dZSignal[1] = -z
	//	dZSignal[2] = +z				dZSignal[2] = -z
	//	dZSignal[3] = +z				dZSignal[3] = -z
	//	dZSignal[4] = +z				dZSignal[4] = -z
	//	etc.						etc.
	// dBackground[] - Background measurements made before and after sample measurement cycle
	//	dBackground[0] = Magnetometer X axis reading before sample measurement
	//	dBackground[1] = Magnetometer Y axis reading before sample measurement
	//	dBackground[2] = Magnetometer Z axis reading before sample measurement
	//	dBackground[3] = Magnetometer X axis reading after sample measurement
	//	dBackground[4] = Magnetometer Y axis reading after sample measurement
	//	dBackground[5] = Magnetometer Z axis reading after sample measurement
	// Output:
	// dXSignal[], dYSignal[], dZSignal[] - Values will be corrected for offset and linear drift

	int i;	// Index
	double dRun;	// Run for drift slope calculation
	double dStep = 0.0;	// Position of measurement relative to first background reading
	double dXDrift, dYDrift, dZDrift;	// Total drift
	double dXSlope, dYSlope, dZSlope;	// Drift slope
	double dCorrection;

	// Total drift (rise) for each axis
	dXDrift = dBackground[3] - dBackground[0];
	dYDrift = dBackground[4] - dBackground[1];
	dZDrift = dBackground[5] - dBackground[2];

	// Run
	dRun = ((double) nCount) + 2.0;	// Add 2 to include each background measurement

	// Drift slope for each axis
	dXSlope = dXDrift / dRun;
	dYSlope = dYDrift / dRun;
	dZSlope = dZDrift / dRun;

	for (i = 0; i <= nCount; i++) {	// Loop through each value in the signal arrays
		dStep += 1.0;	// Increment step value
		if (bXAxis) {	// Magnetometer X axis signal
			dCorrection = dXSlope * dStep + dBackground[0];	// y = mx + b
			dXSignal[i] -= dCorrection;	// Subtract from reading drifting offset
		}

		if (bYAxis) {	// Magnetometer Y axis signal
			dCorrection = dYSlope * dStep + dBackground[1];	// y = mx + b
			dYSignal[i] -= dCorrection;	// Subtract from reading drifting offset
		}

		if (bZAxis) {	// Magnetometer Z axis signal
			dCorrection = dZSlope * dStep + dBackground[2];	// y = mx + b
			dZSignal[i] -= dCorrection;	// Subtract from reading drifting offset
		}
	}
}

/*
void CheckOpenFiles(void *P, void *ptCheck)	// Compares name of file to be opened with those already opened
{
	char achText[MAXFILENAME];
	char *p;

	GetWindowText(((PChild) P) -> HWindow, achText, MAXFILENAME);	// Retrieve child window's caption
	for (p = achText; *p != '\0'; p++) {	// For each character in the caption string
		if (isalpha(*p) && isupper(*p))	// If a character is alphabetic and uppercase
			*p = tolower(*p);	// Convert letter to lowercase
	}

	if (strstr(achFileOpen, achText) != NULL)	// If caption matches name of file to be opened
		++*(int *)ptCheck;	// Increment counter
}
*/
double PASCAL DegToRad(double dDeg)	// Converts degrees to radians
{
	double dRad;

	dRad = dDeg * PI / 180.0;

	return dRad;
}

double PASCAL RadToDeg(double dRad)	// Converts radians to degrees
{
	double dDeg;

	dDeg = dRad / PI * 180.0;

	return dDeg;
}

double PASCAL ArcTangent(double dX, double dY)	// Returns the inverse tangent in radians of dY/dX
{
	double dArcTan;

	if (dX == 0.0) {	// If X is 0
		if (dY >= 0.0)	// If Y is 0 or greater
			return (PI / 2.0);	// 90 degrees
		else	// If Y is negative
			return (3.0 * PI / 2.0);	// 270 degrees
	}

	dArcTan = atan2(dY, dX);	// Calculate inverse tangent
	if (dArcTan < 0.0)	// Only return a value from 0 to 2PI
		dArcTan += 2.0 * PI;

	return dArcTan;
}

void PASCAL HolderCorrection(double dXSignal[], double dYSignal[], double dZSignal[], int nCount, BOOL bXAxis, BOOL bYAxis, BOOL bZAxis, double dHolderX, double dHolderY, double dHolderZ)	// Subtract holder moments
{
	// Parameter descriptions
	// Input:
	// dXSignal[] - Magnetometer X axis measurements
	//	Positive Z position				Negative Z position
	//	dXSignal[0] = +x of sample			dXSignal[0] = +x of sample
	//	dXSignal[1] = -y				dXSignal[1] = +y
	//	dXSignal[2] = -x				dXSignal[2] = -x
	//	dXSignal[3] = +y				dXSignal[3] = -y
	//	dXSignal[4] = +x				dXSignal[4] = +x
	//	etc.						etc.
	// dYSignal[] - Magnetometer Y axis measurements
	//	Positive Z position				Negative Z position
	//	dYSignal[0] = +y of sample			dYSignal[0] = -y of sample
	//	dYSignal[1] = +x				dYSignal[1] = +x
	//	dYSignal[2] = -y				dYSignal[2] = +y
	//	dYSignal[3] = -x				dYSignal[3] = -x
	//	dYSignal[4] = +y				dYSignal[4] = -y
	//	etc.						etc.
	// dZSignal[] - Magnetometer Z axis measurements
	//	Positive Z position				Negative Z position
	//	dZSignal[0] = +z of sample			dZSignal[0] = -z of sample
	//	dZSignal[1] = +z				dZSignal[1] = -z
	//	dZSignal[2] = +z				dZSignal[2] = -z
	//	dZSignal[3] = +z				dZSignal[3] = -z
	//	dZSignal[4] = +z				dZSignal[4] = -z
	//	etc.						etc.
	// Output:
	// dXSignal[], dYSignal[], dZSignal[] - Values will be corrected for sample holder moments

	int i;	// Index
	double dXSign1 = -1.0, dXSign2 = -1.0;	// Alternating signs
	double dYSign1 = 1.0, dYSign2 = -1.0;	// Alternating signs

	for (i = 0; i <= nCount; i++) {	// Loop through each value in the signal arrays
		if (bXAxis) {	// Magnetometer X axis signal
			if (!(i % 2)) {	// If index is even
				dXSign1 *= -1.0;	// Alternating sign starts positive
				dXSignal[i] -= dHolderX * dXSign1;	// Subtract/add holder X component
			}
			else {	// If index is odd
				dYSign1 *= -1.0;	// Alternating sign starts negative
				dXSignal[i] -= dHolderY * dYSign1;	// Subtract/add holder Y component
			}
		}

		if (bYAxis) {	// Magnetometer Y axis signal
			if (i % 2) {	// If index is odd
				dXSign2 *= -1.0;	// Alternating sign starts positive
				dYSignal[i] -= dHolderX * dXSign2;	// Subtract holder X component
			}
			else {	// If index is even
				dYSign2 *= -1.0;	// Alternating sign starts positive
				dYSignal[i] -= dHolderY * dYSign2;	// Subtract holder Y component
			}
		}

		if (bZAxis)	// Magnetometer Z axis signal
			dZSignal[i] -= dHolderZ;	// Subtract holder Z component
	}
}

double PASCAL Magnitude(double dX, double dY, double dZ)	// Returns length of vector
{
	double dLength;

	dLength = sqrt((dX * dX) + (dY * dY) + (dZ * dZ));

	return dLength;
}

void PASCAL CartesianToSpherical(double dX, double dY, double dZ, double adSphere[])	// Converts cartesian coordinates to spherical coordinates
{
	// Parameter descriptions
	// Input:
	// dX - Cartesian X (positive X points north) 
	// dY - Cartesian Y (positive Y points east)
	// dZ - Cartesian Z (positive Z points down)
	// Output:
	// adSphere[] - Declination and inclination in degrees, and magnitude 
	//	adSphere[0] = Declination (angle in X-Y plane:  North = 0; East = 90; South = 180; West = 270)
	//	adSphere[1] = Inclination (angle between component in X-Y plane and Z:  Angle positive for +Z; negative for -Z)
	//	adSphere[2] = Magnitude

	double dXY;

	adSphere[0] = RadToDeg(ArcTangent(dX, dY));	// Declination in degrees
	dXY = Magnitude(dX, dY, 0.0);	// Magnitude of component in the X-Y plane
	adSphere[1] = RadToDeg(ArcTangent(dXY, dZ));	// Inclination in degrees
	if (adSphere[1] > 90.0)	// Inclination must range between -90 and 90
		adSphere[1] -= 360.0;

	adSphere[2] = Magnitude(dX, dY, dZ);	// Vector length
}

void PASCAL RotateVector(double adPrior[], double adPole[], double dAngle, double adAfter[])	// Rotates vector about pole
{
	// Parameter descriptions
	// Input:
	// adPrior[] - Cartesian coordinates of vector to be rotated (North = +X; East = +Y; Down = +Z)
	//	adPrior[0] = X coordinate
	//	adPrior[1] = Y coordinate
	//	adPrior[2] = Z coordinate
	// adPole[] - Cartesian coordinates of pole to rotate about
	//	adPole[0] = X coordinate
	//	adPole[1] = Y coordinate
	//	adPole[2] = Z coordinate
	// dAngle - Angle of rotation in degrees (+ for clockwise rotations; - for counterclockwise rotations)
	// Output:
	// adAfter[] - Cartesian coordinates of rotated vector
	//	adAfter[0] = X coordinate
	//	adAfter[1] = Y coordinate
	//	adAfter[2] = Z coordinate

	double dOneMinusCosine, dCosine, dSine;
	double dR11, dR12, dR13, dR21, dR22, dR23, dR31, dR32, dR33;

	// Sin, cos, and 1 - cos of rotation angle (angle must be converted to radians)
	dSine = sin(DegToRad(dAngle));
	dCosine = cos(DegToRad(dAngle));
	dOneMinusCosine = 1.0 - dCosine;

	// Elements of rotation matrix
	dR11 = adPole[0] * adPole[0] * dOneMinusCosine + dCosine;
	dR12 = adPole[0] * adPole[1] * dOneMinusCosine - adPole[2] * dSine;
	dR13 = adPole[0] * adPole[2] * dOneMinusCosine + adPole[1] * dSine;
	dR21 = adPole[1] * adPole[0] * dOneMinusCosine + adPole[2] * dSine;
	dR22 = adPole[1] * adPole[1] * dOneMinusCosine + dCosine;
	dR23 = adPole[1] * adPole[2] * dOneMinusCosine - adPole[0] * dSine;
	dR31 = adPole[2] * adPole[0] * dOneMinusCosine - adPole[1] * dSine;
	dR32 = adPole[2] * adPole[1] * dOneMinusCosine + adPole[0] * dSine;
	dR33 = adPole[2] * adPole[2] * dOneMinusCosine + dCosine;

	// Components of rotated vector
	adAfter[0] = dR11 * adPrior[0] + dR12 * adPrior[1] + dR13 * adPrior[2];	// X component
	adAfter[1] = dR21 * adPrior[0] + dR22 * adPrior[1] + dR23 * adPrior[2];	// Y component
	adAfter[2] = dR31 * adPrior[0] + dR32 * adPrior[1] + dR33 * adPrior[2];	// Z component
}

void PASCAL SphericalToCartesian(double dDec, double dInc, double dMag, double adCart[])	// Transforms spherical coordinates to cartesian coordinates
{
	// Parameter descriptions
	// Input:
	// dDec - Declination in degrees (clockwise from north)
	// dInc - Inclination in degrees (from horizontal, positive down)
	// dMag - Magnitude
	// Output:
	// adCart[] - X, Y, and Z
	//	adCart[0] = X (positive X due north)
	//	adCart[1] = Y (positive Y due east)
	//	adCart[2] = Z (positive Z down)

	double dSineI, dSineD, dCosineI, dCosineD;

	dSineI = sin(DegToRad(dInc));	// Sin of inclination
	dSineD = sin(DegToRad(dDec));	// Sin of declination
	dCosineI = cos(DegToRad(dInc));	// Cos of inclination
	dCosineD = cos(DegToRad(dDec));	// Cos of declination
	adCart[0] = dMag * dCosineI * dCosineD;	// X
	adCart[1] = dMag * dCosineI * dSineD;	// Y
	adCart[2] = dMag * dSineI;	// Z
}

BOOL PASCAL AbortProc( HDC hDC, short Short )	// Abort procedure for printing
{
	MSG msg;

	while (PeekMessage(&msg, NULL, 0, 0, PM_REMOVE))
		DispatchMessage(&msg);

	return TRUE;
}
