; Ikayaki Installer
;

;--------------------------------

!include "Sections.nsh"

; The name of the installer
Name "Ikayaki"

; The file to write
OutFile "ikayaki-setup.exe"

; The default installation directory
InstallDir $PROGRAMFILES\Ikayaki

; Other installer settings
SetCompressor lzma

;--------------------------------
;Definitions

!define SHCNE_ASSOCCHANGED 0x8000000
!define SHCNF_IDLIST 0

;--------------------------------
; Pages

Page components
Page directory
Page instfiles

UninstPage uninstConfirm
UninstPage instfiles

;--------------------------------
; The stuff to install

Section "!Ikayaki (required)"

  SectionIn RO
  
  IfFileExists $INSTDIR\*.* 0 DoInstall
    MessageBox MB_YESNO "Folder $INSTDIR already exists. Do you want to overwrite it?" IDYES DoInstall IDNO AbortInstall
    AbortInstall:
    MessageBox MB_OK "Installation aborted."
    Quit
  DoInstall:
  
  SetOutPath $INSTDIR
  File "ikayaki.exe"
  File "ikayaki.jar"
  File "LICENCE"
  File /r "lib"
  File /r "jre"
  File /r "manual"
  
  ; File type associations
  WriteRegStr HKCR ".ika" "" "Ikayaki.Project"
  WriteRegStr HKCR "Ikayaki.Project" "" "Ikayaki Project File"
  WriteRegStr HKCR "Ikayaki.Project\DefaultIcon" "" "$INSTDIR\ikayaki.exe,0"
  ReadRegStr $R0 HKCR "Ikayaki.Project\shell\open\command" ""
  StrCmp $R0 "" 0 no_ikaopen
    WriteRegStr HKCR "Ikayaki.Project\shell" "" "open"
    WriteRegStr HKCR "Ikayaki.Project\shell\open\command" "" '$INSTDIR\ikayaki.exe "%1"'
  no_ikaopen:
  
  System::Call 'Shell32::SHChangeNotify(i ${SHCNE_ASSOCCHANGED}, i ${SHCNF_IDLIST}, i 0, i 0)'
  
  ; Write the uninstall keys for Windows
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Ikayaki" "DisplayName" "Ikayaki"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Ikayaki" "UninstallString" '"$INSTDIR\uninstall.exe"'
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Ikayaki" "NoModify" 1
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Ikayaki" "NoRepair" 1
  WriteUninstaller "uninstall.exe"
  
SectionEnd

; Optional section (can be disabled by the user)
Section "Start Menu Shortcuts"

  CreateDirectory "$SMPROGRAMS\Ikayaki"
  CreateShortCut "$SMPROGRAMS\Ikayaki\Uninstall.lnk" "$INSTDIR\uninstall.exe" "" "$INSTDIR\uninstall.exe" 0
  CreateShortCut "$SMPROGRAMS\Ikayaki\Ikayaki.lnk" "$INSTDIR\ikayaki.exe" "" "$INSTDIR\ikayaki.exe" 0
  
SectionEnd

Section "Desktop Shortcut"

  CreateShortCut "$DESKTOP\Ikayaki.lnk" "$INSTDIR\ikayaki.exe" "" "$INSTDIR\ikayaki.exe" 0
  
SectionEnd

;--------------------------------

; Functions

;--------------------------------

; Uninstaller

Section "Uninstall"
  
  ; Remove program files
  RMDir /r /REBOOTOK "$INSTDIR\manual"
  RMDir /r /REBOOTOK "$INSTDIR\jre"
  RMDir /r /REBOOTOK "$INSTDIR\lib"
  Delete /REBOOTOK "$INSTDIR\LICENCE"
  Delete /REBOOTOK "$INSTDIR\ikayaki.jar"
  Delete /REBOOTOK "$INSTDIR\ikayaki.exe"
  
  ; Remove file type associations
  ReadRegStr $R0 HKCR ".ika" ""
  StrCmp $R0 "Ikayaki.Project" 0 +2
    DeleteRegKey HKCR ".ika"
  
  DeleteRegKey HKCR "Ikayaki.Project"
  
  System::Call 'Shell32::SHChangeNotify(i ${SHCNE_ASSOCCHANGED}, i ${SHCNF_IDLIST}, i 0, i 0)'
  
  
  ; Remove registry keys
  DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Ikayaki"

  ; Remove uninstaller
  Delete $INSTDIR\uninstall.exe

  ; Remove shortcuts, if any
  Delete "$SMPROGRAMS\Ikayaki\*.*"
  Delete "$DESKTOP\Ikayaki.lnk"
  
  ; Remove directories used (if empty)
  RMDir "$SMPROGRAMS\Ikayaki"
  RMDir "$INSTDIR"

SectionEnd