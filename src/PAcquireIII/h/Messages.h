/************************************************************************
*                                                                       *
*       Messages.h:                                                     *
*                                                                       *
*       Contains The Text For The Messages.                             *
*                                                                       *
*       29.Aug.2002 Jamie Lisa Finch.                                   *
*                                                                       *
************************************************************************/

#ifndef __MESSAGES_INCLUDED__
#define __MESSAGES_INCLUDED__

#ifdef __cplusplus
extern "C" {
#endif

#define FirstChar 32
#define LastChar 127

void ErrorMessage( char *, char * );
void LastErrorToString( char ** );
void SystemError( char *, char * );
int  GetTextPixelLength( unsigned char *, int, INT * );
HFONT Courier_Font( int, BOOL );
void __cdecl Log( char *, ... );

#ifdef __cplusplus
};
#endif
#endif
