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

typedef struct {
    mbedtls_net_context socket_fd;
    mbedtls_entropy_context entropy;
    mbedtls_ctr_drbg_context ctr_drbg;
    mbedtls_ssl_context ssl;
    mbedtls_ssl_config ssl_conf;
    mbedtls_x509_crt ca_cert;
    mbedtls_x509_crt client_cert;
    mbedtls_pk_context private_key;
} TLSDataParams;

static int _mbedtls_client_init(TLSDataParams *pDataParams) {
    int ret = 0;
    mbedtls_net_init(&(pDataParams)->socket_fd);
    mbedtls_ssl_init(&(pDataParams)->ssl);
    mbedtls_ssl_config_init(&(pDataParams)->ssl_conf);
    mbedtls_ctr_drbg_init(&(pDataParams)->ctr_drbg);
    mbedtls_x509_crt_init(&(pDataParams)->ca_cert);
    mbedtls_x509_crt_init(&(pDataParams)->client_cert);
    mbedtls_pk_init(&(pDataParams)->private_key);
    mbedtls_entropy_init(&(pDataParams->entropy));
    if ((ret = mbedtls_ctr_drbg_seed(&(pDataParams->ctr_drbg), mbedtls_entropy_func,
                                     &(pDataParams->entropy), NULL, 0)) != 0) {
        LOGE("mbedtls_ctr_drbg_seed failed returned %d\n", ret < 0 ? -ret : ret);
        return 1;
    }
    
    return ret;

}

JNIEXPORT jstring JNICALL
Java_euphoria_psycho_todo_NativeUtils_youdaoDictionary(JNIEnv *env, jclass type, jstring word_) {
    const char *word = (*env)->GetStringUTFChars(env, word_, 0);

    // TODO

    TLSDataParams *params = malloc(sizeof(TLSDataParams));
    _mbedtls_client_init(params);

    const char *ret = "123";
    (*env)->ReleaseStringUTFChars(env, word_, word);

    return (*env)->NewStringUTF(env, "123");
}