/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class JNITest */

#ifndef _Included_JNITest
#define _Included_JNITest
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     JNITest
 * Method:    helloC
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_JNITest_helloC
  (JNIEnv *, jclass);

/*
 * Class:     JNITest
 * Method:    getDouble
 * Signature: ()D
 */
JNIEXPORT jdouble JNICALL Java_JNITest_getDouble
  (JNIEnv *, jclass);

/*
 * Class:     JNITest
 * Method:    getArray
 * Signature: ([D)V
 */
JNIEXPORT void JNICALL Java_JNITest_getArray
  (JNIEnv *, jclass, jdoubleArray);

#ifdef __cplusplus
}
#endif
#endif
