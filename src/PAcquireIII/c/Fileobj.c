
// File object routines

#include <windows.h>
#include <stdio.h>
#include <io.h>
#include "..\h\fileobj.h"
#include <fcntl.h>
#include <sys/types.h>
#include <sys/stat.h>

// Save an object

void Save( FILEOBJECT *FileObject, char *Name, int RecSize ) {

  short Size;

  // Open the file if it is not already opened
  OutputDebugString( "Save\n" );

  FileObject->OpenIt( FileObject );

  // Look for the section

  if( FileObject->FindSection( FileObject, Name ) != RecSize) {
    _lseek( FileObject->FileHandle, 0L, 2 );                                   // If section not found, go to the end of the file
    Size = lstrlen( Name ) + 1;                                                 // Write the section name
    _write(FileObject->FileHandle, (const char *) &Size,    sizeof( short ) );
    _write(FileObject->FileHandle, Name, Size);
    _write(FileObject->FileHandle, (const char *) &RecSize, sizeof( short ) ); // Write the size of the section
    }
  }


// Load an object

int Load( FILEOBJECT *FileObject, char *Name ) {

  OutputDebugString( "Load\n" );
  FileObject->OpenIt( FileObject );                             // Open the file if it is not already opened
  return FileObject->FindSection( FileObject, Name );           // Look for the section
  }


int FindSection( FILEOBJECT *FileObject, char *Name ) {

  short Size;
  char  TmpStr[ 65536 ];
  long  Pos;

  // Get the current position
  OutputDebugString( "FindSection\n" );

  Pos = _lseek( FileObject->FileHandle, 0L, 1 );

  // While more data, keep looking

  while( _read( FileObject->FileHandle, &Size, sizeof( short ) ) == sizeof( short ) ) {
    _read( FileObject->FileHandle, TmpStr, Size );             // Get the section name
    Pos += (long) ( sizeof( short ) + Size );
    _read( FileObject->FileHandle, &Size, sizeof( short ) );   // Get the size of the section
    if( !lstrcmp( TmpStr, Name ) )                              // See if this is the right section
      return Size;
    Pos += (long) ( sizeof( short ) + Size );                   // Otherwise, skip this section
    _lseek( FileObject->FileHandle, Pos, 0 );
    }
  return 0;
  }


// Return true if the file exists

BOOL Exists( FILEOBJECT *FileObject ) {

  // If the file is opened, it must exist

  if( FileObject->Opened )
    return TRUE;

  // If it can be opened, it must exist

  if( ( FileObject->FileHandle = _open( FileObject->FileName, _O_BINARY | _O_RDWR )) != -1 ) {
    _close( FileObject->FileHandle );
    return TRUE;
    }
  return FALSE;
  }


// Open the file

void OpenIt( FILEOBJECT *FileObject ) {

  // Go to the beginning of the file
  OutputDebugString( "OpenIt\n" );

  if( FileObject->Opened ) {
    _lseek( FileObject->FileHandle, 0L, 0 );
    }
  // Open or create the file as required
  else {
    SetFileAttributes( FileObject->FileName, FILE_ATTRIBUTE_NORMAL );
    if( ( FileObject->FileHandle = _open( FileObject->FileName, _O_BINARY | _O_RDWR | _O_CREAT, _S_IREAD | _S_IWRITE  ) ) != -1 )
      FileObject->Opened = TRUE;
    }
  }


// Close the file

void CloseIt( FILEOBJECT *FileObject ) {

  // If the file is opened, close it
  OutputDebugString( "CloseIt\n" );


  // Always close the file EVEN IF FILEOBJECT->OPENED == FALSE !!!
  if( FileObject->Opened ) {
    _close( FileObject->FileHandle );
    FileObject->Opened = FALSE;
    }
  }


// Write data to the file at the current location

void CopyTo( FILEOBJECT *FileObject, char *Data, int Size ) {
  OutputDebugString( "CopyTo\n" );

  _write( FileObject->FileHandle, Data, Size );
  }


// Read data from the file

void CopyFrom( FILEOBJECT *FileObject, char *Data, int Size ) {
  OutputDebugString( "CopyFrom\n" );

  _read( FileObject->FileHandle, Data, Size );
  }


// Delete the file

void Delete( FILEOBJECT *FileObject ) {
  OutputDebugString( "Delete\n" );

  // If the file is opened, close it

  FileObject->CloseIt( FileObject );

  // Delete the file

  unlink( FileObject->FileName );
  }


// Change the name of the file

void ChangeName( FILEOBJECT *FileObject, char *Name ) {

  // If there is already a file, close it
  OutputDebugString( "ChangeName\n" );

  FileObject->CloseIt( FileObject );
  lstrcpy( FileObject->FileName, Name);
  }


// Get rid of the file object

void DeleteFileObject( FILEOBJECT *FileObject ) {

  OutputDebugString( "DeleteFileObject\n" );

  if( FileObject != (FILEOBJECT *) NULL ) {
    FileObject->CloseIt( FileObject );
    free( FileObject );
    }
  }


// HardFlush the file

void HardFlush( FILEOBJECT *FileObject ) {

  OutputDebugString( "HardFlush\n" );
  /* If the file is open then close it */
  if( FileObject->Opened ) {
    _close( FileObject->FileHandle );

    /* If we fail to reOpen the file then the program will know */
    FileObject->Opened = FALSE;
    }

  /* ReOpen the file */
  if( ( FileObject->FileHandle = _open( FileObject->FileName, _O_BINARY | _O_RDWR ) ) != -1 ) {

    /* If we fail to reOpen the file then the program will know */
    FileObject->Opened = TRUE;

    /* Append is implicit */
    //_llseek( FileObject->FileHandle, 0, FILE_END );
    
    }
  }

// Initialize a file object

FILEOBJECT *FileObject( char *Name ) {

  FILEOBJECT *FileObject;

  if( FileObject = malloc( sizeof( FILEOBJECT ) ) ) {
    memset( FileObject, 0, sizeof( FILEOBJECT ) );
    lstrcpy(FileObject->FileName, Name );
    FileObject->Opened           = FALSE;
    FileObject->FileHandle       = -1;
    FileObject->DeleteFileObject = DeleteFileObject;    // Destructor
    FileObject->ChangeName       = ChangeName;          // Select a new file
    FileObject->Save             = Save;                // Put a record header in the file
    FileObject->Load             = Load;                // Look for and read a record header
    FileObject->FindSection      = FindSection;         // Find a record header
    FileObject->Delete           = Delete;              // Delete the file
    FileObject->Exists           = Exists;              // Returns true if the file exists
    FileObject->CopyFrom         = CopyFrom;            // Read data from the file
    FileObject->CopyTo           = CopyTo;              // Write data to the file
    FileObject->OpenIt           = OpenIt;              // Open the file
    FileObject->CloseIt          = CloseIt;             // Close the file
    FileObject->HardFlush        = HardFlush;           // Close the file and open it again.
    }
  return FileObject;
  }
