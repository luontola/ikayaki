
#ifndef __FNCTNS_INCLUDED__
#define __FNCTNS_INCLUDED__

#ifdef __cplusplus
extern "C" {
#endif

// Function prototypes

int  PASCAL NThreeSixty( int );                                         // Converts an integer angle to a value between -180 and 360
int  PASCAL DThreeSixty( double );                                      // Converts a double angle to a value between -180.0 and 360.0
int  PASCAL Complement(  int );                                         // Returns the complement of an angle
int  PASCAL ChangeSign(  int );                                         // Changes sign of an angle
void PASCAL PrinterInfo( char *, char *, char * );                      // Retrieve printer configuration information from WIN.INI
void PASCAL TemporaryDirectory(  char *, int    );                      // Returns path to temporary directory as specified by the TEMP= command
void PASCAL Wait( DWORD );                                              // Sets cursor to an hourglass shape and loops for specified millisecond time interval
void PASCAL Pause(DWORD );                                              // Loops for specified millisecond time interval
BOOL PASCAL DigitsAndSign(     char * );                                // Returns true if string consists of just digits and an optional, single hyphen
BOOL PASCAL DigitsPointAndSign(char * );                                // Returns true if string consists of just digits, an optional single decimal point, and an optional sign
BOOL PASCAL ScientificAndSign( char * );                                // Returns true if string consists of a number in proper scientific notation (e.g. -8.888e-08)
BOOL PASCAL GoodSampleName(    char * );                                // Returns true if string consists of characters acceptable for a DOS file name
int  PASCAL WriteSerial(     HWND, SERIALIO *, char *, int );           // Returns the number of characters transmitted to the serial port
int  PASCAL ReadSerial(      HWND, SERIALIO *, char *, int );           // Returns the number of characters actually read from the serial port
BOOL PASCAL ConfirmMagStatus(HWND, SERIALIO *, char *, char * );        // Returns true if status is confirmed
BOOL PASCAL ConfirmAFStatus( HWND, SERIALIO *, char * );                // Returns true if status is confirmed
BOOL PASCAL ConfirmSHStatus( HWND, SERIALIO *, char *, char * );        // Returns true if status is confirmed
//BOOL PASCAL Translate(     HWND, BOOL, LONG );                        // Moves automatic sample handler translator to flag position, and returns TRUE when finished
//BOOL PASCAL Rotate(        HWND, BOOL, int  );                        // Moves automatic sample handler rotator to flag position, and returns TRUE when finished
BOOL PASCAL DegaussCycle(    HWND, SERIALIO *, char *, double );        // Performs a Degausser Execute Ramp Cycle for the specified coil and amplitude, and returns TRUE if successful
BOOL PASCAL DegaussSetRamp(  HWND, SERIALIO *, DWORD );                 // Configures degausser for ramp, and returns TRUE if successful
BOOL PASCAL DegaussSetDelay( HWND, SERIALIO *, DWORD );                 // Configures degausser for delay, and returns TRUE if successful
BOOL PASCAL SHSetParameter(  HWND, SERIALIO *, char *, char * );        // Returns true if parameter is set successfully
BOOL PASCAL SHSendCommand(   HWND, SERIALIO *, char * );                // Returns true if command is sent successfully
BOOL PASCAL MagSetRange(     HWND, SERIALIO *, char *, DWORD );         // Configures specified axis for range, and returns TRUE if successful
BOOL PASCAL MagSetFilter(    HWND, SERIALIO *, char *, DWORD );         // Configures specified axis for filter, and returns TRUE if successful
BOOL PASCAL MagSetSlew(      HWND, SERIALIO *, char *, DWORD );         // Configures specified axis for fast slew option, and returns TRUE if successful
//BOOL PASCAL MagSetPanel(     HWND, SERIALIO *, char *, DWORD );         // Configures specified axis for front panel option, and returns TRUE if successful
BOOL PASCAL MagResetCount(   HWND, SERIALIO *, char * );                // Resets flux counter to zero for specified axis, and returns TRUE if successful
BOOL PASCAL MagPulseLoop(    HWND, SERIALIO *, char * );                // Opens then closes feedback loop for specified axis, and returns TRUE if successful
BOOL PASCAL MagLatchCount(   HWND, SERIALIO *, char * );                // Latches flux counter for the specified axis, and returns TRUE if successful
BOOL PASCAL MagLatchData(    HWND, SERIALIO *, char * );                // Latches analog data for the specified axis, and returns TRUE if successful
int PASCAL MagSendCount(     HWND, SERIALIO *, char * );                // Reads flux counter for the specified axis, and returns the int value
double PASCAL MagSendData(   HWND, SERIALIO *, char * );                // Reads analog data for the specified axis, and returns the double value
double PASCAL CombineCountAndData( int, double, char *, BOOL, BOOL, DWORD, double, double, double );    // Combines count and analog data for the specified axis, and returns the double value in emu
//BOOL PASCAL MagRangeUp(       HWND, char * );                         // Increases range setting for specified magnetometer axis, and returns TRUE if successful
//void PASCAL MagRangeDown(     HWND, char * );                         // Decreases range setting for specified magnetometer axis
BOOL PASCAL MyTranslateMessage( HWND, char * );                         // Notifies user to translate manual sample handler
BOOL PASCAL RotateMessage(      HWND, char * );                         // Notifies user to rotate manual sample handler
void PASCAL LoadMessage(        HWND, char * );                         // Notifies user to load sample
void PASCAL NotifyMessage(      HWND, char * );                         // Notifies user that a threshold value was not met
void PASCAL NormalSums( double *, double *, double *, int, BOOL, BOOL, BOOL, BOOL, double * );  // Returns normal statistic sums for each component
void PASCAL NormalStatistics( double *, double * );                     // Returns the mean and standard deviation for each sample component of magnetization
void PASCAL OffsetAndDrift(   double *, double *, double *, int, BOOL, BOOL, BOOL, double * );  // Applies linear drift offset correction to magnetometer data
//void CheckOpenFiles( void *, void * );                                // Compares name of file to be opened with those already opened
double PASCAL DegToRad( double );                                       // Converts degrees to radians
double PASCAL RadToDeg( double );                                       // Converts radians to degrees
double PASCAL ArcTangent(double, double );                              // Returns the inverse tangent in radians of dY/dX
void   PASCAL HolderCorrection( double *, double *, double *, int, BOOL, BOOL, BOOL, double, double, double );  // Subtract holder moments
double PASCAL Magnitude( double, double, double );                      // Returns length of vector
void   PASCAL CartesianToSpherical(double, double, double, double * );  // Converts cartesian coordinates to spherical coordinates
void   PASCAL RotateVector(double *, double *, double, double * );      // Rotates vector about pole
void   PASCAL SphericalToCartesian( double, double, double, double * ); // Transforms spherical coordinates to cartesian coordinates
BOOL   PASCAL AbortProc( HDC, short );                                  // Abort procedure for printing

#ifdef __cplusplus
};
#endif
#endif
