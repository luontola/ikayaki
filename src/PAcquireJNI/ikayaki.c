
#include <jni.h>
#include <stdio.h>
#include "JNITest.h"

/*
 * Class:     JNITest
 * Method:    helloC
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_JNITest_helloC
(JNIEnv *env, jclass obj) {
	jclass cls;
	jmethodID mid;

	printf("Hello C!\n");
	flushall();

	cls = (*env)->FindClass(env, "ikayaki/core/JNITest");
//    if (cls == NULL) {
//        return;
//    }
    mid = (*env)->GetStaticMethodID(env, cls, "helloJava", "()V");
//    if (mid == NULL) {
//        return;
//    }
    (*env)->CallStaticVoidMethod(env, cls, mid);

	return;
}


/*
 * Class:     JNITest
 * Method:    getDouble
 * Signature: ()D
 */
JNIEXPORT jdouble JNICALL Java_JNITest_getDouble
(JNIEnv *env, jclass obj) {
	jdouble d = 1.23;
	return d;
}

/*
 * Class:     JNITest
 * Method:    getArray
 * Signature: ([D)V
 */
JNIEXPORT void JNICALL Java_JNITest_getArray
(JNIEnv *env, jclass obj, jdoubleArray arr) {
	jsize len = (*env)->GetArrayLength(env, arr);
	jdouble *arrBody = (*env)->GetDoubleArrayElements(env, arr, 0);
	int i;
	for (i = 0; i < len; i++) {
		arrBody[i] = i * i;
	}
	(*env)->ReleaseDoubleArrayElements(env, arr, arrBody, 0);
	return;
}



