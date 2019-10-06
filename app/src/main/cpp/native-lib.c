#include <jni.h>
#include <memory.h>
#include <malloc.h>
#include <mbedtls/net_sockets.h>
#include <mbedtls/entropy.h>
#include <mbedtls/ctr_drbg.h>
#include "utils_string.h"
#include "log.h"
#include "markdown/markdown.h"
#include "markdown/html.h"
#include "youdao.h"
#include "tinyexpr/tinyexpr.h"
#include "baidu.h"
#include "google.h"

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


// |HTML_ESCAPE|HTML_SAFELINK
#define HTML_RENDER_FLAGS (HTML_USE_XHTML)

JNIEXPORT void JNICALL
Java_euphoria_psycho_todo_NativeUtils_renderMarkdown(JNIEnv *env, jclass type, jstring text_,
                                                     jstring outFile_) {
    const char *text = (*env)->GetStringUTFChars(env, text_, 0);
    const char *outFile = (*env)->GetStringUTFChars(env, outFile_, 0);
    struct sd_callbacks callbacks;
    struct html_renderopt options;
    sdhtml_renderer(&callbacks, &options, HTML_RENDER_FLAGS);
    struct sd_markdown *markdown = sd_markdown_new(MKDEXT_TABLES, 16, &callbacks, &options);
    struct buf *output_buf;
    output_buf = bufnew(128);
    //result = kno_make_string(NULL,output_buf->size,output_buf->data);
    sd_markdown_render(
            output_buf,
            (const uint8_t *) (text),
            strlen(text),
            markdown);
    sd_markdown_free(markdown);

    const char *p = "<!DOCTYPE html>\n"
                    "<html>\n"
                    "<head>\n"
                    "    <meta charset=utf-8>\n"
                    "    <meta http-equiv=X-UA-Compatible content=\"chrome=1\">\n"
                    "    <meta name=\"viewport\" content=\"width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no,shrink-to-fit=no\">\n"

                    "    <link rel=stylesheet href=app.css>\n"
                    "</head>\n"
                    "<body>\n"
                    "%s"
                    "</body>\n"
                    "</html>";
    char *buf = malloc(output_buf->size + strlen(p));

    sprintf(buf, p, output_buf->data);
    bufrelease(output_buf);
    FILE *f = fopen(outFile, "w");
    fputs(buf, f);
    fflush(f);
    fclose(f);
    free(buf);

    (*env)->ReleaseStringUTFChars(env, text_, text);
    (*env)->ReleaseStringUTFChars(env, outFile_, outFile);
}
//
//typedef struct {
//    mbedtls_net_context socket_fd;
//    mbedtls_entropy_context entropy;
//    mbedtls_ctr_drbg_context ctr_drbg;
//    mbedtls_ssl_context ssl;
//    mbedtls_ssl_config ssl_conf;
//    mbedtls_x509_crt ca_cert;
//    mbedtls_x509_crt client_cert;
//    mbedtls_pk_context private_key;
//} TLSDataParams;
//
//typedef struct {
//    const char *ca_crt;
//    uint16_t ca_crt_len;
//#ifdef AUTH_MODE_CERT
//    const char *cert_file;
//    const char *key_file;
//#else
//    const char *psk;
//    const char *psk_id;
//#endif
//    size_t psk_length;
//    unsigned int timeout_ms;
//} SSLConnectParams;
//
//typedef SSLConnectParams TLSConnectParams;
//typedef enum {
//    ERR_SUCCESS = -1,
//    ERR_SSL_INIT,
//    ERR_SSL_CERT,
//
//
//} ErrCodes;
//
//static int _mbedtls_client_init(TLSDataParams *pDataParams, TLSConnectParams *pConnectParams) {
//    int ret = ERR_SUCCESS;
//    mbedtls_net_init(&(pDataParams->socket_fd));
//    mbedtls_ssl_init(&(pDataParams->ssl));
//    mbedtls_ssl_config_init(&(pDataParams->ssl_conf));
//    mbedtls_ctr_drbg_init(&(pDataParams->ctr_drbg));
//    mbedtls_x509_crt_init(&(pDataParams->ca_cert));
//    mbedtls_x509_crt_init(&(pDataParams->client_cert));
//    mbedtls_pk_init(&(pDataParams->private_key));
//    mbedtls_entropy_init(&(pDataParams->entropy));
//    if ((ret = mbedtls_ctr_drbg_seed(&(pDataParams->ctr_drbg), mbedtls_entropy_func,
//                                     &(pDataParams->entropy), NULL, 0)) != 0) {
//        LOGE("mbedtls_ctr_drbg_seed failed returned 0x%04x\n", ret < 0 ? -ret : ret);
//        return ERR_SSL_INIT;
//    }
//    if (pConnectParams->ca_crt != NULL) {
//        if ((ret = mbedtls_x509_crt_parse(&(pDataParams->ca_cert),
//                                          (const unsigned char *) pConnectParams->ca_crt,
//                                          (pConnectParams->ca_crt_len + 1)))) {
//            LOGE("parse ca crt failed returned 0x%04x", ret < 0 ? -ret : ret);
//            return ERR_SSL_CERT;
//        }
//    }
//#ifdef AUTH_MODE_CERT
//    if (pConnectParams->cert_file != NULL && pConnectParams->key_file != NULL) {
//        if ((ret = mbedtls_x509_crt_parse_file(&(pDataParams->client_cert),
//                                               pConnectParams->cert_file)) != 0) {
//            LOGE("load client cert file failed returned 0x%x", ret < 0 ? -ret : ret);
//            return ERR_SSL_CERT;
//        }
//        if ((ret = mbedtls_pk_parse_keyfile(&(pDataParams->private_key), pConnectParams->key_file,
//                                            "")) != 0) {
//            LOGE("load client key file failed returned 0x%x", ret < 0 ? -ret : ret);
//            return ERR_SSL_CERT;
//        }
//    } else {
//        LOGE("cert_file/key_file is empty!|cert_file=%s|key_file=%s", pConnectParams->cert_file,
//             pConnectParams->key_file);
//    }
//#else
//if (pConnectParams->psk!=NULL && pConnectParams->psk_id!=NULL) {
//        const char *psk_id=pConnectParams->psk_id;
//        ret=mbedtls_ssl_conf_psk(&(pDataParams->ssl_conf),(
//    unsigned char*)pConnectParams->psk,pConnectParams->psk_length,(const unsigned char *)psk_id,
//                                 strlen(psk_id));
//
//} else{
//        LOGE("psk/pskid is empty!|psk=%s|psd_id=%s",pConnectParams->psk,pConnectParams->psk_id);
//    }
//    if(0!=ret){
//        LOGE("mbedtls_ssl_conf_psk fail: 0x%x",ret<0?-ret:ret);
//        return ret;
//    }
//#endif
//    return ERR_SUCCESS;
//
//}
// TODO
//
//TLSDataParams *params = malloc(sizeof(TLSDataParams));
//_mbedtls_client_init(params);


JNIEXPORT jdouble JNICALL
Java_euphoria_psycho_todo_NativeUtils_calculateExpr(JNIEnv *env, jclass type, jstring expr_) {
    const char *expr = (*env)->GetStringUTFChars(env, expr_, 0);

    double ret = te_interp(expr, 0);


    (*env)->ReleaseStringUTFChars(env, expr_, expr);

    return ret;
}

JNIEXPORT jstring JNICALL
Java_euphoria_psycho_todo_NativeUtils_googleTranslate(JNIEnv *env, jclass type, jstring word_,
                                                      jboolean englishToChinese) {
    const char *word = (*env)->GetStringUTFChars(env, word_, 0);

    // initialize the string
    rapidstring s;
    rs_init(&s);

    // do translate
    int ret = google_translate(word, &s, englishToChinese ? "zh" : "en");

    // release the word pass from java
    (*env)->ReleaseStringUTFChars(env, word_, word);

    // check the result
    if (ret != 0) {
        rs_free(&s);
        return NULL;
    }
    char *retStr = rs_data(&s);
    rs_free(&s);
    return (*env)->NewStringUTF(env, retStr);
}

JNIEXPORT jstring JNICALL
Java_euphoria_psycho_todo_NativeUtils_baiduTranslate(JNIEnv *env, jclass type, jstring word_,
                                                     jboolean englishToChinese) {
    const char *word = (*env)->GetStringUTFChars(env, word_, 0);

    rapidstring s;
    rs_init(&s);

    int ret = baidu_query_dictionary(word, &s, englishToChinese ? "zh" : "en");

    LOGE("----------------->");

    // release the word pass from java
    (*env)->ReleaseStringUTFChars(env, word_, word);
    LOGE("----------------->");

    // check the result
    if (ret != 0) {
        rs_free(&s);
        return NULL;
    }
    char *retStr = rs_data(&s);
    rs_free(&s);
    return (*env)->NewStringUTF(env, retStr);
}

JNIEXPORT jstring JNICALL
Java_euphoria_psycho_todo_NativeUtils_youdaoDictionary(JNIEnv *env, jclass type, jstring word_,
                                                       jboolean translate,
                                                       jboolean englishToChinese) {
    const char *word = (*env)->GetStringUTFChars(env, word_, 0);
    rapidstring s;
    rs_init(&s);

    int ret = youdao_query_dictionary(word, &s, translate ? 1 : 0,
                                      englishToChinese ? "EN" : "zh-CHS",
                                      englishToChinese ? "zh-CHS" : "EN"
    );
    (*env)->ReleaseStringUTFChars(env, word_, word);

    if (ret != 0) {
        rs_free(&s);
        return NULL;
    }

    char *retStr = rs_data(&s);

    rs_free(&s);

    if (retStr == NULL || !strlen(retStr))return NULL;
    return (*env)->NewStringUTF(env, retStr);
}