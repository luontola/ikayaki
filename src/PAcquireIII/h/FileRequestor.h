/************************************************************************
*                                                                       *
*       FileRequestor:                                                  *
*                                                                       *
*       Contains The Loading And Saving File Requestors.                *
*                                                                       *
*       21.Aug.2002 Jamie Lisa Finch.                                   *
*                                                                       *
************************************************************************/

#ifndef __FILEREQUESTOR_INCLUDED__
#define __FILEREQUESTOR_INCLUDED__

#ifdef __cplusplus
extern "C" {
#endif

int ParseFileName(char *, char *, char *, char * );
int GetFileRequestorLoad( char *, char *, char * );
int GetFileRequestorSave( char *, char *, char * );

#ifdef __cplusplus
};
#endif
#endif
