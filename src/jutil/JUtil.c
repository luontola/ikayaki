#include "JUtil.h"
#define STRICT
#include <windows.h>
#include <conio.h>


JNIEXPORT jchar JNICALL Java_jutil_JUtil_getConsoleChar(JNIEnv *env, jclass obj)
{
 return _getch();
}

JNIEXPORT jobjectArray JNICALL Java_jutil_JUtil_getLogicalDrives(JNIEnv *env, jclass obj)
{
 DWORD dStrLength;
 char szDrives[256];
 jobjectArray strArray = NULL;

 memset(szDrives,'\0',sizeof(szDrives));
 dStrLength = GetLogicalDriveStrings(sizeof(szDrives),szDrives);

 /*
 ** The length of the array is returned from the above function minus the
 ** terminating null character. The function will place the names of the
 ** logical drives in the passed character array and null separate the names.
 ** The names will be in the form of X:\ where X denotes the drive letter.
 ** Therefore the length returned can be divided by 4 (the total number of
 ** characters in each name plus the null separator) to determine the number 
 ** of drives returned from the function. This number is then used to allocate
 ** storage for the array of Strings that will passed back to the caller of this
 ** function.
 */
 if (dStrLength)
 {
  int i, nDriveCount, nOffset = 0;

  nDriveCount = dStrLength/4;

  /* Allocate storage for Java array of String objects */
  strArray = (*env)->NewObjectArray(env,nDriveCount,(*env)->FindClass(env,"java/lang/String"),(*env)->NewStringUTF(env, ""));

  /* Cycle through the string to get the names of the logical drives */
  for (i = 0; i < nDriveCount; i++)
  {
   /* Add the logical drive name as a Java String to the Java object array */
   (*env)->SetObjectArrayElement(env, strArray, i, (*env)->NewStringUTF(env, szDrives));

   /* Advance to the next logical drive name */
   szDrives[0] = szDrives[nOffset += 4];
  }
 }

 return strArray;
}

JNIEXPORT jlong JNICALL Java_jutil_JUtil_getFreeDiskSpace(JNIEnv *env, jclass obj, jstring drive)
{
 DWORD sectorsPerCluster = 0, bytesPerSector = 0;
 DWORD freeClusters = 0, totalClusters = 0, freeBytes = 0;
 BOOL fReturn;
 const char *str = NULL;

 str = (*env)->GetStringUTFChars(env, drive, 0);
 fReturn = GetDiskFreeSpace(str,&sectorsPerCluster,&bytesPerSector,&freeClusters,&totalClusters);
 (*env)->ReleaseStringUTFChars(env, drive, str);

 if (fReturn)
  freeBytes = freeClusters * sectorsPerCluster * bytesPerSector;

 return freeBytes;
}

JNIEXPORT jint JNICALL Java_jutil_JUtil_getDriveType(JNIEnv *env, jclass obj, jstring drive)
{
 const char *str = NULL;
 UINT uReturn;

 str = (*env)->GetStringUTFChars(env, drive, 0);
 uReturn = GetDriveType(str);
 (*env)->ReleaseStringUTFChars(env, drive, str);

 return uReturn;
}

JNIEXPORT jstring JNICALL Java_jutil_JUtil_getVolumeLabel(JNIEnv *env, jclass obj, jstring drive)
{
 char szVolumeName[256];
 const char *str = NULL;
 DWORD maxCompLen = 0, fileSystemFlags = 0;

 memset(szVolumeName,'\0',sizeof(szVolumeName));
 str = (*env)->GetStringUTFChars(env, drive, 0);
 GetVolumeInformation(str,szVolumeName,sizeof(szVolumeName),NULL,&maxCompLen,&fileSystemFlags,NULL,0);
 (*env)->ReleaseStringUTFChars(env, drive, str);

 return (*env)->NewStringUTF(env,szVolumeName);
}

JNIEXPORT jboolean JNICALL Java_jutil_JUtil_setVolumeLabel(JNIEnv *env, jclass obj, jstring drive, jstring label)
{
 BOOL fReturn;
 const char *str1 = NULL, *str2 = NULL;

 str1 = (*env)->GetStringUTFChars(env, drive, 0);
 str2 = (*env)->GetStringUTFChars(env, label, 0);
 fReturn = SetVolumeLabel(str1,str2);
 (*env)->ReleaseStringUTFChars(env, drive, str1);
 (*env)->ReleaseStringUTFChars(env, label, str2);

 return fReturn;
}

JNIEXPORT jstring JNICALL Java_jutil_JUtil_getCurrentDirectory(JNIEnv *env, jclass obj)
{
 char szCurrentDir[_MAX_PATH];

 memset(szCurrentDir,'\0',sizeof(szCurrentDir));
 GetCurrentDirectory(_MAX_PATH,szCurrentDir);

 return (*env)->NewStringUTF(env, szCurrentDir);
}

JNIEXPORT jboolean JNICALL Java_jutil_JUtil_setCurrentDirectory(JNIEnv *env, jclass obj, jstring directory)
{
 BOOL fReturn;
 const char *str = NULL;
 
 str = (*env)->GetStringUTFChars(env, directory, 0);
 fReturn = SetCurrentDirectory(str);
 (*env)->ReleaseStringUTFChars(env, directory, str);

 return fReturn;
}

JNIEXPORT jint JNICALL Java_jutil_JUtil_getHwnd(JNIEnv *env, jclass obj, jstring title)
{
 HWND hwnd = NULL;
 const char *str = NULL;

 str = (*env)->GetStringUTFChars(env, title, 0);
 hwnd = FindWindow(NULL,str);
 (*env)->ReleaseStringUTFChars(env, title, str);

 return (jint) hwnd;
}

JNIEXPORT void JNICALL Java_jutil_JUtil_setWindowMinimized(JNIEnv *env, jclass obj, jint hwnd)
{
 SendMessage((HWND) hwnd,WM_SYSCOMMAND,SC_MINIMIZE,0L);

 return;
}

JNIEXPORT void JNICALL Java_jutil_JUtil_setWindowMaximized(JNIEnv *env, jclass obj, jint hwnd)
{
 SendMessage((HWND) hwnd,WM_SYSCOMMAND,SC_MAXIMIZE,0L);

 return;
}

JNIEXPORT void JNICALL Java_jutil_JUtil_setWindowRestored(JNIEnv *env, jclass obj, jint hwnd)
{
 SendMessage((HWND) hwnd,WM_SYSCOMMAND,SC_RESTORE,0L);

 return;
}

JNIEXPORT void JNICALL Java_jutil_JUtil_setWindowRestoreEnabled(JNIEnv *env, jclass obj, jint hwnd, jboolean flag)
{
 HMENU hSysMenu = NULL;

 hSysMenu = GetSystemMenu((HWND) hwnd,FALSE);

 if (!flag)
  ModifyMenu(hSysMenu,0,MF_BYPOSITION|MF_GRAYED|MF_STRING,0,"&Restore");
 else
  ModifyMenu(hSysMenu,0,MF_BYPOSITION|MF_ENABLED|MF_STRING,0,"&Restore");

 return;
}

JNIEXPORT void JNICALL Java_jutil_JUtil_setWindowMoveEnabled(JNIEnv *env, jclass obj, jint hwnd, jboolean flag)
{
 HMENU hSysMenu = NULL;

 hSysMenu = GetSystemMenu((HWND) hwnd,FALSE);

 if (!flag)
  ModifyMenu(hSysMenu,1,MF_BYPOSITION|MF_GRAYED|MF_STRING,0,"&Move");
 else
  ModifyMenu(hSysMenu,1,MF_BYPOSITION|MF_ENABLED|MF_STRING,0,"&Move");

 return;
}

JNIEXPORT void JNICALL Java_jutil_JUtil_setWindowSizeEnabled(JNIEnv *env, jclass obj, jint hwnd, jboolean flag)
{
 HMENU hSysMenu = NULL;

 hSysMenu = GetSystemMenu((HWND) hwnd,FALSE);

 if (!flag)
  ModifyMenu(hSysMenu,2,MF_BYPOSITION|MF_GRAYED|MF_STRING,0,"&Size");
 else
  ModifyMenu(hSysMenu,2,MF_BYPOSITION|MF_ENABLED|MF_STRING,0,"&Size");

 return;
}

JNIEXPORT void JNICALL Java_jutil_JUtil_setWindowMinimizeEnabled(JNIEnv *env, jclass obj, jint hwnd, jboolean flag)
{
 HMENU hSysMenu = NULL;

 hSysMenu = GetSystemMenu((HWND) hwnd,FALSE);

 if (!flag)
  ModifyMenu(hSysMenu,3,MF_BYPOSITION|MF_GRAYED|MF_STRING,0,"Mi&nimize");
 else
  ModifyMenu(hSysMenu,3,MF_BYPOSITION|MF_ENABLED|MF_STRING,0,"Mi&nimize");

 return;
}

JNIEXPORT void JNICALL Java_jutil_JUtil_setWindowMaximizeEnabled(JNIEnv *env, jclass obj, jint hwnd, jboolean flag)
{
 HMENU hSysMenu = NULL;

 hSysMenu = GetSystemMenu((HWND) hwnd,FALSE);

 if (!flag)
  ModifyMenu(hSysMenu,4,MF_BYPOSITION|MF_GRAYED|MF_STRING,0,"Ma&ximize");
 else
  ModifyMenu(hSysMenu,4,MF_BYPOSITION|MF_ENABLED|MF_STRING,0,"Ma&ximize");

 return;
}

JNIEXPORT void JNICALL Java_jutil_JUtil_setWindowAlwaysOnTop(JNIEnv *env, jclass obj, jint hwnd, jboolean flag)
{
 if (flag)
  SetWindowPos((HWND) hwnd,HWND_TOPMOST,0,0,0,0,SWP_NOMOVE|SWP_NOSIZE);
 else
  SetWindowPos((HWND) hwnd,HWND_NOTOPMOST,0,0,0,0,SWP_NOMOVE|SWP_NOSIZE);

 return;
}
