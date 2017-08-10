#include <jni.h>
#include <string>
#include "md5.h"
#include "dns.h"
#include <android/log.h>

#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, "Ismartv-Lib", __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG , "Ismartv-Lib", __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO  , "Ismartv-Lib", __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN  , "Ismartv-Lib", __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR  , "Ismartv-Lib", __VA_ARGS__)

extern "C"
JNIEXPORT jstring JNICALL
Java_tv_ismar_account_IsmartvActivator_nativeMacAddress(JNIEnv *env, jobject instance) {

    char *filename = "sys/class/net/eth0/address";
    char ch;
    char macaddress[64];
    FILE *fp;
    int i = 0;
    memset(macaddress, 0, 64);
    char buf[64];
    if ((fp = fopen(filename, "r")) == NULL) {
        memset(buf, 0, 64);
        memcpy(buf, "noaddress", strlen("noaddress"));
    } else {
        while ((ch = fgetc(fp)) != EOF) {
            if (ch >= 255)
                break;
            macaddress[i++] = ch;
        }
        memset(buf, 0, 64);
        memcpy(buf, macaddress, strlen(macaddress));
        fclose(fp);
    }
    return env->NewStringUTF(buf);
}

extern "C"
uint8_t *MD5(unsigned char *data, size_t len, uint8_t *out) {
    MD5_CTX ctx;
    static uint8_t digest[16];

    /* TODO(fork): remove this static buffer. */
    if (out == NULL) {
        out = digest;
    }

    MD5Init(&ctx);
    MD5Update(&ctx, data, len);
    MD5Final(out, &ctx);

    return out;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_tv_ismar_account_IsmartvActivator_nativeMD5(JNIEnv *env, jobject instance, jstring str_) {
    const char *source = env->GetStringUTFChars(str_, 0);
    unsigned char md[16];
    int i;
    char tmp[3] = {'\0'}, buf[33] = {'\0'};
    MD5((unsigned char *) source, strlen(source), md);
    for (i = 0; i < 16; i++) {
        sprintf(tmp, "%2.2x", md[i]);
        strcat(buf, tmp);
    }
    env->ReleaseStringUTFChars(str_, source);
    return env->NewStringUTF(buf);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_tv_ismar_account_IsmartvActivator_getHostByName(JNIEnv *env, jclass type, jstring hostName_) {
    const char *hostName = env->GetStringUTFChars(hostName_, 0);

    char hostNameTmp[256];
    strcpy(hostNameTmp, hostName);

    char *ip = getByName(hostNameTmp);

    env->ReleaseStringUTFChars(hostName_, hostName);
    return env->NewStringUTF(ip);
}


