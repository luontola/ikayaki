# Microsoft Developer Studio Project File - Name="PAcquire" - Package Owner=<4>
# Microsoft Developer Studio Generated Build File, Format Version 6.00
# ** DO NOT EDIT **

# TARGTYPE "Win32 (x86) Application" 0x0101

CFG=PAcquire - Win32 Debug
!MESSAGE This is not a valid makefile. To build this project using NMAKE,
!MESSAGE use the Export Makefile command and run
!MESSAGE 
!MESSAGE NMAKE /f "PAcquireIII.mak".
!MESSAGE 
!MESSAGE You can specify a configuration when running NMAKE
!MESSAGE by defining the macro CFG on the command line. For example:
!MESSAGE 
!MESSAGE NMAKE /f "PAcquireIII.mak" CFG="PAcquire - Win32 Debug"
!MESSAGE 
!MESSAGE Possible choices for configuration are:
!MESSAGE 
!MESSAGE "PAcquire - Win32 Release" (based on "Win32 (x86) Application")
!MESSAGE "PAcquire - Win32 Debug" (based on "Win32 (x86) Application")
!MESSAGE 

# Begin Project
# PROP AllowPerConfigDependencies 0
# PROP Scc_ProjName ""
# PROP Scc_LocalPath ""
CPP=cl.exe
MTL=midl.exe
RSC=rc.exe

!IF  "$(CFG)" == "PAcquire - Win32 Release"

# PROP BASE Use_MFC 0
# PROP BASE Use_Debug_Libraries 0
# PROP BASE Output_Dir "Release"
# PROP BASE Intermediate_Dir "Release"
# PROP BASE Target_Dir ""
# PROP Use_MFC 0
# PROP Use_Debug_Libraries 0
# PROP Output_Dir "Release"
# PROP Intermediate_Dir "Release"
# PROP Ignore_Export_Lib 0
# PROP Target_Dir ""
# ADD BASE CPP /nologo /W3 /GX /O2 /D "WIN32" /D "NDEBUG" /D "_WINDOWS" /YX /FD /c
# ADD CPP /nologo /Zp1 /W3 /GX /O2 /I "D:\Program Files\Java\jdk1.5.0_01\include" /D "WIN32" /D "NDEBUG" /D "_WINDOWS" /FR /YX /FD /c
# ADD BASE MTL /nologo /D "NDEBUG" /mktyplib203 /win32
# ADD MTL /nologo /D "NDEBUG" /mktyplib203 /win32
# ADD BASE RSC /l 0x409 /d "NDEBUG"
# ADD RSC /l 0x409 /d "NDEBUG"
BSC32=bscmake.exe
# ADD BASE BSC32 /nologo
# ADD BSC32 /nologo
LINK32=link.exe
# ADD BASE LINK32 kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib /nologo /subsystem:windows /machine:I386
# ADD LINK32 kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib comctl32.lib /nologo /subsystem:windows /machine:I386

!ELSEIF  "$(CFG)" == "PAcquire - Win32 Debug"

# PROP BASE Use_MFC 0
# PROP BASE Use_Debug_Libraries 1
# PROP BASE Output_Dir "Debug"
# PROP BASE Intermediate_Dir "Debug"
# PROP BASE Target_Dir ""
# PROP Use_MFC 0
# PROP Use_Debug_Libraries 1
# PROP Output_Dir "Debug"
# PROP Intermediate_Dir "Debug"
# PROP Ignore_Export_Lib 0
# PROP Target_Dir ""
# ADD BASE CPP /nologo /W3 /Gm /GX /ZI /Od /D "WIN32" /D "_DEBUG" /D "_WINDOWS" /YX /FD /GZ /c
# ADD CPP /nologo /Zp1 /W3 /Gm /GX /ZI /Od /I "D:\Program Files\Java\jdk1.5.0_01\include" /I "D:\Program Files\Java\jdk1.5.0_01\include\win32" /D "WIN32" /D "_DEBUG" /D "_WINDOWS" /FR /YX /FD /GZ /c
# ADD BASE MTL /nologo /D "_DEBUG" /mktyplib203 /win32
# ADD MTL /nologo /D "_DEBUG" /mktyplib203 /win32
# ADD BASE RSC /l 0x409 /d "_DEBUG"
# ADD RSC /l 0x409 /d "_DEBUG"
BSC32=bscmake.exe
# ADD BASE BSC32 /nologo
# ADD BSC32 /nologo
LINK32=link.exe
# ADD BASE LINK32 kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib /nologo /subsystem:windows /debug /machine:I386 /pdbtype:sept
# ADD LINK32 kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib comctl32.lib /nologo /subsystem:windows /debug /machine:I386 /pdbtype:sept

!ENDIF 

# Begin Target

# Name "PAcquire - Win32 Release"
# Name "PAcquire - Win32 Debug"
# Begin Group "c"

# PROP Default_Filter "cpp;c;cxx;rc;def;r;odl;idl;hpj;bat"
# Begin Source File

SOURCE=.\c\Configure.c
# End Source File
# Begin Source File

SOURCE=.\c\Fileobj.c
# End Source File
# Begin Source File

SOURCE=.\c\FileRequestor.c
# End Source File
# Begin Source File

SOURCE=.\c\Fnctns.c
# End Source File
# Begin Source File

SOURCE=.\c\Main.c

!IF  "$(CFG)" == "PAcquire - Win32 Release"

# SUBTRACT CPP /I "D:\Program Files\Java\jdk1.5.0_01\include"

!ELSEIF  "$(CFG)" == "PAcquire - Win32 Debug"

# SUBTRACT CPP /I "D:\Program Files\Java\jdk1.5.0_01\include" /I "D:\Program Files\Java\jdk1.5.0_01\include\win32"

!ENDIF 

# End Source File
# Begin Source File

SOURCE=.\c\Messages.c
# End Source File
# Begin Source File

SOURCE=.\c\MyChild.c
# End Source File
# Begin Source File

SOURCE=.\c\SerialIO.c
# End Source File
# Begin Source File

SOURCE=.\c\Text.c
# End Source File
# End Group
# Begin Group "h"

# PROP Default_Filter "h;hpp;hxx;hm;inl"
# Begin Source File

SOURCE=.\h\Configure.h
# End Source File
# Begin Source File

SOURCE=.\h\Dlgdata.h
# End Source File
# Begin Source File

SOURCE=.\h\Fileobj.h
# End Source File
# Begin Source File

SOURCE=.\h\FileRequestor.h
# End Source File
# Begin Source File

SOURCE=.\h\Fnctns.h
# End Source File
# Begin Source File

SOURCE=.\h\Messages.h
# End Source File
# Begin Source File

SOURCE=.\h\MyChild.h
# End Source File
# Begin Source File

SOURCE=.\h\PAcquire.h
# End Source File
# Begin Source File

SOURCE=.\res\resource.h
# End Source File
# Begin Source File

SOURCE=.\h\SerialIO.h
# End Source File
# Begin Source File

SOURCE=.\h\Text.h
# End Source File
# End Group
# Begin Group "res"

# PROP Default_Filter "ico;cur;bmp;dlg;rc2;rct;bin;rgs;gif;jpg;jpeg;jpe"
# Begin Source File

SOURCE=.\res\2GHalf.bmp
# End Source File
# Begin Source File

SOURCE=.\res\2GQuarter.bmp
# End Source File
# Begin Source File

SOURCE=.\res\2GTransparent.bmp
# End Source File
# Begin Source File

SOURCE=.\res\APSLogo.ico
# End Source File
# Begin Source File

SOURCE=.\res\icon1.ico
# End Source File
# Begin Source File

SOURCE=.\res\Pacquire.rc
# End Source File
# Begin Source File

SOURCE=.\res\Vector.ico
# End Source File
# End Group
# Begin Group "Microsoft"

# PROP Default_Filter "h;hpp;hxx;hm;inl"
# Begin Source File

SOURCE="..\..\..\sdk\Platform SDK\Include\BaseTsd.h"
# End Source File
# Begin Source File

SOURCE="..\..\..\sdk\Platform SDK\Include\Guiddef.h"
# End Source File
# Begin Source File

SOURCE="..\..\..\sdk\Platform SDK\Include\PropIdl.h"
# End Source File
# Begin Source File

SOURCE="..\..\..\sdk\Platform SDK\Include\Reason.h"
# End Source File
# Begin Source File

SOURCE="..\..\..\sdk\Platform SDK\Include\StrAlign.h"
# End Source File
# Begin Source File

SOURCE="..\..\..\sdk\Platform SDK\Include\Tvout.h"
# End Source File
# Begin Source File

SOURCE="..\..\..\sdk\Platform SDK\Include\WinEFS.h"
# End Source File
# End Group
# End Target
# End Project
