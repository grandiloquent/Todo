#include <jni.h>
#include <memory.h>
#include <malloc.h>
#include "utils_string.h"
#include "log.h"

JNIEXPORT jstring JNICALL
Java_euphoria_psycho_todo_NativeUtils_removeRedundancy(JNIEnv *env, jclass type, jstring text_) {
    const char *text = (*env)->GetStringUTFChars(env, text_, 0);

    char *ret = remove_redundancy(text);

    LOGE("%s\n", ret);
    (*env)->ReleaseStringUTFChars(env, text_, text);
    jstring ret_str = (*env)->NewStringUTF(env, ret);
    free(ret);
    return ret_str;
}