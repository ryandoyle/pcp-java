#include <stdio.h>
#include <pcp/pmapi.h>

#include "net_ryandoyle_pcp_PMAPI.h"


typedef struct {
    int pm_error;
    const char *exception_class;
} jni_pmapi_exception_mapping;

jni_pmapi_exception_mapping jni_pmapi_exception_map[] = {
    {PM_ERR_GENERIC, "net/ryandoyle/pcp/pmapi/PmGenericError"},
    {PM_ERR_PMNS, "net/ryandoyle/pcp/pmapi/PmPMNSError"},
    {PM_ERR_NOPMNS, "net/ryandoyle/pcp/pmapi/PmNoPMNSError"},
};

static jclass getClassOrThrow(JNIEnv *env, const char *the_class) {
    jclass wanted = (*env)->FindClass(env, the_class);
    if(wanted == NULL) {
        (*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/NoClassDefFoundError"), the_class);
        return NULL;
    }
    return wanted;
}

static void throwPMAPIException(JNIEnv *env, int err) {
    jclass pcp_error = NULL;

    /* Find the exception class mapping to the error */
    for(int i = 0; i < sizeof(jni_pmapi_exception_map)/sizeof(jni_pmapi_exception_mapping); i++) {
        if(jni_pmapi_exception_map[i].pm_error == err) {
            if(!(pcp_error = getClassOrThrow(env, jni_pmapi_exception_map[i].exception_class))) {
                return;
            }
            char error_buf[PM_MAXERRMSGLEN];
            (*env)->ThrowNew(env, pcp_error, pmErrStr_r(err, error_buf, PM_MAXERRMSGLEN));
            return;
        }
    }

    /* Didn't find a suitable exception, throw the generic one. It could be an error from a system call */
    if(!(pcp_error = getClassOrThrow(env, "net/ryandoyle/pcp/pmapi/UncategorisedError"))) {
        return;
    }
    char error_buf[PM_MAXERRMSGLEN];
    (*env)->ThrowNew(env, pcp_error, pmErrStr_r(err, error_buf, PM_MAXERRMSGLEN));
}

JNIEXPORT jstring JNICALL Java_net_ryandoyle_pcp_PMAPI_sayHello(JNIEnv *env, jobject self) {
    return (*env)->NewStringUTF(env, "Hello from native, created a new context");
}

JNIEXPORT jint JNICALL Java_net_ryandoyle_pcp_PMAPI_pmNewContext(JNIEnv *env, jobject self, jint j_context_type, jstring j_host_or_archive) {
    const char *host_or_archive = (*env)->GetStringUTFChars(env, j_host_or_archive, JNI_FALSE);

    int status_or_context = pmNewContext((int)j_context_type, host_or_archive);
    (*env)->ReleaseStringUTFChars(env, j_host_or_archive, host_or_archive);

    if(status_or_context < 0) {
        throwPMAPIException(env, status_or_context);
        return 0;
    }

    return status_or_context;
}

JNIEXPORT void JNICALL Java_net_ryandoyle_pcp_PMAPI_pmDestroyContext(JNIEnv *env, jobject self, jint context_id) {
    int status = pmDestroyContext((int)context_id);
    if(status != 0) {
        throwPMAPIException(env, status);
    }
}

JNIEXPORT void JNICALL Java_net_ryandoyle_pcp_PMAPI_pmUseContext(JNIEnv *env, jobject self, jint context_id) {
    int status = pmUseContext((int)context_id);
    if(status != 0) {
        throwPMAPIException(env, status);
    }
}

JNIEXPORT jstring JNICALL Java_net_ryandoyle_pcp_PMAPI_pmGetContextHostName0(JNIEnv *env, jobject self, jint context_id) {
    char buf[MAXHOSTNAMELEN];

    pmGetContextHostName_r((int)context_id, buf, MAXHOSTNAMELEN);

    return (*env)->NewStringUTF(env, buf);
}

/* exposed to test exception classes are mapped to error codes */
JNIEXPORT void JNICALL Java_net_ryandoyle_pcp_PMAPI_raisePmapiException(JNIEnv *env, jclass self, jint err) {
    throwPMAPIException(env, (int)err);
}