/************************************************************************
*                                                                       *
*       FileRequestor:                                                  *
*                                                                       *
*       Contains The Loading And Saving File Requestors.                *
*                                                                       *
*       21.Aug.2002 Jamie Lisa Finch.                                   *
*                                                                       *
************************************************************************/

#include <windows.h>
#include <stdio.h>
#include "..\h\FileRequestor.h"


/************************************************************************
*                                                                       *
*       ParseFileName:                                                  *
*                                                                       *
*       Parses A File Name Into A Path, Name and Extension.             *
*                                                                       *
*       Input:                                                          *
*                                                                       *
*       Path        = Address Of String To Receive File Path.           *
*       Name        = Address Of String To Receive File Name.           *
*       Ext         = Address Of String To Receive File Extension.      *
*       PathNameExt = Address Of Source File Path Name and Extension.   *
*                                                                       *
*       Output:                                                         *
*                                                                       *
*       Returns -> TRUE  == Execution Without Error.                    *
*                  FALSE == Failed To Get File Name.                    *
*                                                                       *
*       6.Feb.2002 Jamie Lisa Finch.                                    *
*                                                                       *
************************************************************************/

int ParseFileName( char *Path, char *Name, char *Ext, char *PathNameExt ) {

  int   Count;
  char *Inc, *PathPos, *ExtPos;

  if( ( Path        != (char *) NULL ) &&
      ( Name        != (char *) NULL ) &&
      ( Ext         != (char *) NULL ) &&
      ( PathNameExt != (char *) NULL ) ) {

    Path[ 0 ] = 0;
    Name[ 0 ] = 0;
    Ext[  0 ] = 0;

    /* Search For Path. */

    Inc     = PathNameExt;
    PathPos = (char *) NULL;
    ExtPos  = (char *) NULL;

    while( *Inc != 0 ) {
      if( ( *Inc == ':'  ) ||
          ( *Inc == '\\' ) ||
          ( *Inc == '/'  ) ) {
        PathPos = Inc;
        }
      else if( *Inc == '.' ) {
        ExtPos  = Inc;
        }
      Inc++;
      }

    /* First Get The Path. */

    if( PathPos != (char *) NULL ) {
      Count = PathPos - PathNameExt;
      if( Count > 0 ) {
        memcpy( Path, PathNameExt, Count );
        Path[ Count ] = 0;
        PathNameExt = PathPos + 1;
        }
      else return FALSE;
      }

    /* Get The File Name & Extension. */

    if( *PathNameExt ) {
      if( ExtPos != (char *) NULL ) {
        Count = ExtPos - PathNameExt;
        if( Count > 0 ) {
          memcpy( Name, PathNameExt, Count );
          Name[ Count ] = 0;
          }
        PathNameExt = ExtPos + 1;
        strcpy( Ext, PathNameExt );
        }
      else strcpy( Name, PathNameExt );
      }
    return TRUE;
    }
  return FALSE;
  }


/************************************************************************
*                                                                       *
*       GetFileRequestorLoad:                                           *
*                                                                       *
*       Opens A File Requestor To Obtain A File Name.                   *
*                                                                       *
*       Input:                                                          *
*                                                                       *
*       FileName = Address Of String With Current File Name.            *
*       DefaultExtension = File Default Extension ( txt, dat, prj ).    *
*       Type     = Address Of File Type ( Text, Data, Project ).        *
*                                                                       *
*       Output:                                                         *
*                                                                       *
*       FileName = New File Name Is Copied Into This String.            *
*                                                                       *
*       Returns -> TRUE  == Execution Without Error.                    *
*                  FALSE == Failed To Obtain File Name.                 *
*                                                                       *
*       6.Feb.2002 Jamie Lisa Finch.                                    *
*                                                                       *
************************************************************************/

int GetFileRequestorLoad( char *FileName, char *DefaultExtension, char *Type ) {

  char Path[ 512 ];
  char Name[ 512 ];
  char Ext[  512 ];
  char FileTitle[ 512 ];
  char LoadFilter[512 ], *Inc;
  char LoadTitle[ 512 ];
  OPENFILENAME ofn;

  if( ParseFileName( Path, Name, Ext, FileName ) ) {
    strcpy( FileTitle, FileName );
    sprintf(LoadTitle, "Load %s From File", Type );

    Inc = LoadFilter;

    sprintf( Inc, "%s Files (*.%s)", Type, DefaultExtension ); Inc += strlen( Inc ) + 1;
    sprintf( Inc, "*.%s", DefaultExtension );                  Inc += strlen( Inc ) + 1;

    sprintf( Inc, " All Files (*.*)" );                        Inc += strlen( Inc ) + 1;
    sprintf( Inc, "*.*" );                                     Inc += strlen( Inc ) + 1;

    *Inc = 0;

    //***************** TEST **********************
    Path[0] = 0;
    //***************** TEST **********************



    /* Set Open File Name Structure. */

    memset( &ofn, 0, sizeof( ofn ) );
    ofn.lStructSize       = sizeof( ofn );
    ofn.hwndOwner         = (HWND) 0;
    ofn.hInstance         = (HINSTANCE) 0;
    ofn.lpstrFilter       = LoadFilter;
    ofn.lpstrCustomFilter = NULL;
    ofn.nMaxCustFilter    = 0;
    ofn.nFilterIndex      = 0;
    ofn.lpstrFile         = Name;
    ofn.nMaxFile          = sizeof( Name );
    ofn.lpstrFileTitle    = FileTitle;
    ofn.nMaxFileTitle     = sizeof( FileTitle );
    ofn.lpstrInitialDir   = Path;
    ofn.lpstrTitle        = LoadTitle;
    ofn.Flags             = OFN_LONGNAMES | OFN_EXPLORER | OFN_HIDEREADONLY;
    ofn.nFileOffset       = 0;
    ofn.nFileExtension    = 0;
    ofn.lpstrDefExt       = DefaultExtension;
    ofn.lCustData         = 0;
    ofn.lpfnHook          = NULL;
    ofn.lpTemplateName    = NULL;

    if( GetOpenFileName( &ofn ) ) {
      strcpy( FileName, Name );
      return TRUE;
      }
    }
  return FALSE;
  }


/************************************************************************
*                                                                       *
*       GetFileRequestorSave:                                           *
*                                                                       *
*       Opens A File Requestor To Obtain A File Name.                   *
*                                                                       *
*       Input:                                                          *
*                                                                       *
*       FileName = Address Of String With Current File Name.            *
*       DefaultExtension = File Default Extension ( txt, dat, prj ).    *
*       Type     = Address Of File Type ( Text, Data, Project ).        *
*                                                                       *
*       Output:                                                         *
*                                                                       *
*       FileName = New File Name Is Copied Into This String.            *
*                                                                       *
*       Returns -> TRUE  == Execution Without Error.                    *
*                  FALSE == Failed To Obtain File Name.                 *
*                                                                       *
*       6.Feb.2002 Jamie Lisa Finch.                                    *
*                                                                       *
************************************************************************/

int GetFileRequestorSave( char *FileName, char *DefaultExtension, char *Type ) {

  char Path[ 512 ];
  char Name[ 512 ];
  char Ext[  512 ];
  char FileTitle[ 512 ];
  char SaveFilter[512 ], *Inc;
  char SaveTitle[ 512 ];

  OPENFILENAME ofn;

  if( ParseFileName( Path, Name, Ext, FileName ) ) {
    strcpy( FileTitle, FileName );
    sprintf(SaveTitle, "Save %s To File", Type );

    Inc = SaveFilter;

    sprintf( Inc, "%s Files (*.%s)", Type, DefaultExtension ); Inc += strlen( Inc ) + 1;
    sprintf( Inc, "*.%s", DefaultExtension );                  Inc += strlen( Inc ) + 1;

    sprintf( Inc, " All Files (*.*)" );                        Inc += strlen( Inc ) + 1;
    sprintf( Inc, "*.*" );                                     Inc += strlen( Inc ) + 1;

    *Inc = 0;

    /* Set Open File Name Structure. */

    memset( &ofn, 0, sizeof( ofn ) );
    ofn.lStructSize       = sizeof( ofn );
    ofn.hwndOwner         = (HWND) 0;
    ofn.hInstance         = (HINSTANCE) 0;
    ofn.lpstrFilter       = SaveFilter;
    ofn.lpstrCustomFilter = NULL;
    ofn.nMaxCustFilter    = 0;
    ofn.nFilterIndex      = 0;
    ofn.lpstrFile         = Name;
    ofn.nMaxFile          = sizeof( Name );
    ofn.lpstrFileTitle    = FileTitle;
    ofn.nMaxFileTitle     = sizeof( FileTitle );
    ofn.lpstrInitialDir   = Path;
    ofn.lpstrTitle        = SaveTitle;
    ofn.Flags             = OFN_LONGNAMES | OFN_EXPLORER | OFN_HIDEREADONLY | OFN_OVERWRITEPROMPT;
    ofn.nFileOffset       = 0;
    ofn.nFileExtension    = 0;
    ofn.lpstrDefExt       = DefaultExtension;
    ofn.lCustData         = 0;
    ofn.lpfnHook          = NULL;
    ofn.lpTemplateName    = NULL;

    if( GetSaveFileName( &ofn ) ) {
      strcpy( FileName, Name );
      return TRUE;
      }
    }
  return FALSE;
  }
