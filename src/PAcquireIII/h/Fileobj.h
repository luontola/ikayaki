
#ifndef __FILEOBJ_INCLUDED__
#define __FILEOBJ_INCLUDED__

#ifdef __cplusplus
extern "C" {
#endif

// The file object definition

typedef struct _fileobject {
  char FileName[ 260 ];                                                 // The name of the file
  char Name[ 260 ];
  BOOL Opened;                                                          // True when the file is open
  int  FileHandle;                                                      // The MS-DOS file handle
  void (*DeleteFileObject)( struct _fileobject * );                     // Destructor
  void (*ChangeName      )( struct _fileobject *, char * );             // Select a new file
  void (*Save            )( struct _fileobject *, char *, int );        // Put a record header in the file
  int  (*Load            )( struct _fileobject *, char * );             // Look for and read a record header
  int  (*FindSection     )( struct _fileobject *, char * );             // Find a record header
  void (*Delete          )( struct _fileobject * );                     // Delete the file
  BOOL (*Exists          )( struct _fileobject * );                     // Returns true if the file exists
  void (*CopyFrom        )( struct _fileobject *, char *, int );        // Read data from the file
  void (*CopyTo          )( struct _fileobject *, char *, int );        // Write data to the file
  void (*OpenIt          )( struct _fileobject * );                     // Open the file
  void (*CloseIt         )( struct _fileobject * );                     // Close the file
  void (*HardFlush       )( struct _fileobject * );                     // Close the file then Open the file
  
  } FILEOBJECT;

FILEOBJECT *FileObject( char * );                                       // Constructor

#ifdef __cplusplus
};
#endif
#endif
