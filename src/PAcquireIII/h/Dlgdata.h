#ifndef __DLGDATA_INCLUDED__
#define __DLGDATA_INCLUDED__

#ifdef __cplusplus
extern "C" {
#endif

// dlgdata.h

typedef struct _info {          // Structure for Sample Information dialog box object
  char  achName[128];            // Sample name
  char  sampPath[128];
  char  achSize[15];            // Sample size
  DWORD dwCC;                   // Sample size in cubic centimeters
  DWORD dwGM;                   // Sample size in grams
  char  achTime[18];            // Time and date
  char  achComment[56];         // Sample comment
  char  achCoreAz[5];           // Sample core down dip azimuth
  char  achCoreP[4];            // Sample core plunge
  char  achBeddingAz[5];        // Bedding dip vector azimuth
  char  achBeddingP[4];         // Bedding dip vector plunge
  DWORD dwOver;                 // Overturned bedding
  char  achFoldAz[5];           // Plunging fold hinge line azimuth
  char  achFoldP[4];            // Plunging fold hinge line plunge
  char  achMagDecl[5];          // Magnetic declination
  } INFO;

typedef struct _config {        // Structure for Configure Instrument dialog box object
  DWORD dwX;                    // Magnetometer X axis
  DWORD dwY;                    // Magnetometer Y axis
  DWORD dwZ;                    // Magnetometer Z axis
  DWORD dwDCSquids;             // DC squids
  DWORD dwSHAuto;               // Automatic sample handler
  DWORD dwSHMan;                // Manual sample handler
  DWORD dwAFAuto;               // Automatic AF demagnetizer
  DWORD dwAFMan;                // Manual AF demagnetizer
  DWORD dwAFNone;               // No AF demagnetizer
  DWORD dwMagCom;               // Magnetometer serial port
  DWORD dwAFCom;                // AF demagnetizer serial port
  DWORD dwSHCom;                // Sample handler serial port
  } CONFIG;

typedef struct _orient {        // Structure for Core and Structure Orientation Conventions dialog box object
  char  achCoreAz[5];           // Sample core azimuth minus 2G azimuth
  DWORD dwCorePComp;            // Sample core plunge complement
  DWORD dwCorePSign;            // Sample core plunge sign
  char  achBeddingAz[5];        // Bedding dip vector azimuth minus 2G azimuth
  DWORD dwBeddingPComp;         // Bedding dip vector plunge complement
  DWORD dwBeddingPSign;         // Bedding dip vector plunge sign
  char  achFoldAz[5];           // Plunging fold hinge line azimuth minus 2G azimuth
  DWORD dwFoldPComp;            // Plunging fold hinge line plunge complement
  DWORD dwFoldPSign;            // Plunging fold hinge line plunge sign
  } ORIENT;

typedef struct _calib {         // Structure for Magnetometer Calibration Constants dialog box object
  char  achX[11];               // X axis
  char  achY[11];               // Y axis
  char  achZ[11];               // Z axis
  } CALIB;

typedef struct _ab {            // Structure for AF Demagnetizer Parameters dialog box object
  DWORD dwX;                    // AF demagnetizer X axis
  DWORD dwY;                    // AF demagnetizer Y axis
  DWORD dwZ;                    // AF demagnetizer Z axis
  DWORD dw2G601S;               // 2G601S coil
  DWORD dw2G601T;               // 2G601T coil
  char  achField[5];            // Maximum field
  DWORD dwRamp;                 // Ramp
  DWORD dwDelay;                // Delay
  } AF;

typedef struct _sh {            // Structure for Sample Handler Parameters and Positions dialog box object
  DWORD dw2G810;                // 2G810 system
  DWORD dw2G811;                // 2G811 system
  DWORD dwTrans;                // Translation axis
  DWORD dwRot;                  // Rotation axis
  DWORD dwFlip;                 // Flip capability
  DWORD dwNoFlip;               // No flip capability
  char  achAccel[4];            // Acceleration
  char  achDecel[4];            // Deceleration
  char  achVel[6];              // Velocity
  char  achVelM[6];             // Velocity into measurement region
  char  achPosXAF[7];           // Transverse X AF position
  char  achPosYAF[7];           // Transverse Y AF position
  char  achPosZAF[7];           // Axial Z AF position
  char  achPosLoad[7];          // Sample load position
  char  achPosBack[7];          // Background measurement position
  char  achPosMeas[7];          // Sample measurement position
  char  achRotCounts[6];        // Counts per one full revolution
  char  achRotAccel[4];         // Rotational Acceleration 
  char  achRotDecel[4];         // Rotational Deceleration 
  char  achRotVel[4];           // Rotational Velocity
  DWORD dwRight;                // Right limit switch
  } SH;

typedef struct _settings {      // Structure for Magnetometer Settings dialog box object
  DWORD dwXRange;               // X axis range
  DWORD dwXFlux;                // X axis flux counting
  DWORD dwXFilter;              // X axis filter
  DWORD dwXSlew;                // X axis fast slew
  DWORD dwXReset;               // X axis reset
  DWORD dwXPanel;               // X axis disable panel
  DWORD dwYRange;               // Y axis range
  DWORD dwYFlux;                // Y axis flux counting
  DWORD dwYFilter;              // Y axis filter
  DWORD dwYSlew;                // Y axis fast slew
  DWORD dwYReset;               // Y axis reset
  DWORD dwYPanel;               // Y axis disable panel
  DWORD dwZRange;               // Z axis range
  DWORD dwZFlux;                // Z axis flux counting
  DWORD dwZFilter;              // Z axis filter
  DWORD dwZSlew;                // Z axis fast slew
  DWORD dwZReset;               // Z axis reset
  DWORD dwZPanel;               // Z axis disable panel
  char  achDelay[16];            // Time delay for magnetometer settling
  } SETTINGS;

typedef struct _move {          // Structure for Sample Handler Move dialog box object
  DWORD dwTrans;                // Translation position
  DWORD dwRot;                  // Rotation position
  } MOVE;

typedef struct _single {        // Structure for Measure Single Step dialog box object
  DWORD dwAF;                   // AF demagnetization
  DWORD dwThermal;              // Thermal demagnetization
  DWORD dwNone;                 // No demagnetization
  char  achField[6];            // AF strength
  char  achTemp[5];             // Temperature
  } SINGLE;

typedef struct _sequence {      // Structure for Measure Sequence dialog box object
  char  achStart[6];            // Start AF strength
  char  achStop1[6];            // First stop AF strength
  char  achIncr1[6];            // First increment AF
  char  achStop2[6];            // Second stop AF strength
  char  achIncr2[6];            // Second increment AF
  } SEQUENCE;

typedef struct _holder {        // Structure for Measure Sample Holder dialog box object
  char  achX[11];               // X moment
  char  achY[11];               // Y moment
  char  achZ[11];               // Z moment
  char  achTotal[11];           // Total moment
  } HOLDER;

typedef struct _stnd {          // Structure for Measure Standard dialog box object
  char  achX[11];               // X moment
  char  achY[11];               // Y moment
  char  achZ[11];               // Z moment
  char  achTotal[11];           // Total moment
  char  achAngle[6];            // Angle between sample and magnetometer X axes
  } STND;

typedef struct _options {       // Structure for Measure Options dialog box object
  char  achXYZ[2];              // Analog readings for all axes
//char  achX[2];                // X axis readings
//char  achY[2];                // Y axis readings
//char  achZ[2];                // Z axis readings
  DWORD dwMin;                  // Minimum readings per sample
  DWORD dwSingle;               // Single rotation per sample
  DWORD dwMult;                 // Multiple rotations per sample
  char  achMult[2];             // Number of rotations
  DWORD dwZ;                    // Measure +Z and -Z
  char  achSN[5];               // Signal to noise threshold
  DWORD dwSNR;                  // Signal to noise remeasure
  DWORD dwSNN;                  // Signal to noise notify
//char  achDrift[8];            // Drift threshold
  char  achDrift[6];            // Signal to drift threshold
  DWORD dwDR;                   // Drift remeasure
  DWORD dwDN;                   // Drift notify
  } OPTIONS;
/*
typedef struct _project {       // Structure for Projection Options dialog box object
  DWORD dwCore;                 // Core coordinate system
  DWORD dwInSitu;               // In situ coordinate system
  DWORD dwRotated;              // Rotated coordinate system
  } PROJECT;
*/
typedef struct _specimendata {  // Structure for disk file specimen data
  char  achDemag[8];            // Demagnetization level
  char  achCoreDec[6];          // Magnetization declination in core coordinates
  char  achCoreInc[6];          // Magnetization inclination in core coordinates
  char  achInSituDec[6];        // Magnetization declination in geographic coordinates
  char  achInSituInc[6];        // Magnetization inclination in geographic coordinates
  char  achRotatedDec[6];       // Magnetization declination in stratigraphic coordinates
  char  achRotatedInc[6];       // Magnetization inclination in stratigraphic coordinates
  char  achTotalMoment[10];     // Total moment in emu
  char  achMagnetization[10];   // Magnetization in emu/cc or emu/gm
  char  achMomentX[11];         // Sample X moment
  char  achSigmaX[10];          // Standard deviation about the sample X component of magnetization
  char  achNX[3];               // Number of X magnetization measurements represented in mean
  char  achErrorX[8];           // Percent error for X
  char  achMomentY[11];         // Sample Y moment
  char  achSigmaY[10];          // Standard deviation about the sample Y component of magnetization
  char  achNY[3];               // Number of Y magnetization measurements represented in mean
  char  achErrorY[8];           // Percent error for Y
  char  achMomentZ[11];         // Sample Z moment
  char  achSigmaZ[10];          // Standard deviation about the sample Z component of magnetization
  char  achNZ[3];               // Number of Z magnetization measurements represented in mean
  char  achErrorZ[8];           // Percent error for Z
  char  achStoN[8];             // Signal to noise ratio (1.4e+01)
  char  achStoD[8];             // Signal to drift ratio
  char  achStoH[8];             // Signal to holder ratio
  char  achZAxis[3];            // Core orientation in magnetometer (+z)
  char  achTime[18];            // Time and date of measurement
  } SPECIMENDATA;

typedef struct _specimensummary{// Structure for disk file specimen data summary
  short  nCurrentStep;          // Current demagnetization/measurement step existing in disk file
  } SPECIMENSUMMARY;

#ifdef __cplusplus
};
#endif
#endif
