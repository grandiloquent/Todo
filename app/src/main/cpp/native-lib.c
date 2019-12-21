#include <jni.h>
#include <memory.h>
#include <malloc.h>
#include <mbedtls/net_sockets.h>
#include <mbedtls/entropy.h>
#include <mbedtls/ctr_drbg.h>
#include <zconf.h>
#include <sys/socket.h>
#include <netdb.h>
#include <linux/in.h>
#include <ctype.h>
#include "utils_string.h"
#include "log.h"
#include "markdown/markdown.h"
#include "markdown/html.h"
#include "tinyexpr/tinyexpr.h"
#include "cJSON/cJSON.h"
#include "tmd5/tmd5.h"
#include "macros.h"
#include <unistd.h>
#include <fcntl.h>
#include <netinet/tcp.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <time.h>
#include <errno.h>
#include <sys/stat.h>
#include <netinet/in.h>

#define BAI_APP_ID "20190312000276185"
static const char HEX_ARRAY[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                                 'A', 'B', 'C', 'D', 'E', 'F'};

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

JNIEXPORT jstring JNICALL
Java_euphoria_psycho_todo_NativeUtils_toggleList(JNIEnv *env, jclass type, jstring text_) {
    const char *text = (*env)->GetStringUTFChars(env, text_, 0);

    char *ret = toggle_list(text);

    (*env)->ReleaseStringUTFChars(env, text_, text);
    jstring ret_str = (*env)->NewStringUTF(env, ret);
    free(ret);
    return ret_str;
}
JNIEXPORT jstring JNICALL
Java_euphoria_psycho_todo_NativeUtils_toggleNumberList(JNIEnv *env, jclass type, jstring text_) {
    const char *text = (*env)->GetStringUTFChars(env, text_, 0);

    char *ret = toggle_number_list(text);

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
    struct sd_markdown *markdown = sd_markdown_new(
            MKDEXT_TABLES | MKDEXT_AUTOLINK | MKDEXT_FENCED_CODE, 16, &callbacks,
            &options);
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
    //fflush(f);
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
    if (word_ == NULL)return NULL;
    const char *word = (*env)->GetStringUTFChars(env, word_, 0);
    const char *to = englishToChinese ? "zh" : "en";

    SOCKET_INIT("translate.google.cn", "80");
    URL_ENCODE(word);


    const char *url_format = "/translate_a/single?client=gtx&sl=auto&tl=%s&dt=t&dt=bd&ie=UTF-8&oe=UTF-8&dj=1&source=icon&q=%s";

    size_t buf_path_len = strlen(buf_encode) + strlen(url_format) + 10;
    char buf_path[buf_path_len];
    memset(buf_path, 0, buf_path_len);

    snprintf(buf_path, buf_path_len,
             url_format, to, buf_encode);

    size_t buf_header_len = strlen(buf_path) + 100;
    char buf_header[buf_header_len];
    memset(buf_header, 0, buf_header_len);
    strcat(buf_header, "GET ");
    strcat(buf_header, buf_path);
    strcat(buf_header, " HTTP/1.1\r\n");
    strcat(buf_header, "Accept: application/json, text/javascript, */*; q=0.01\r\n");
    strcat(buf_header, "Host: translate.google.cn\r\n");
    strcat(buf_header,
           "User-Agent: Mozilla/4.0\r\n");
    strcat(buf_header, "\r\n");


    SEND_HEADER();

    size_t buf_body_len = 1024 << 2, buf_body_read_len = 0, header_end = 0;
    char buf_body[buf_body_len];
    memset(buf_body, 0, buf_body_len);
    do {
        ENSURE_NOT_BIG();


        while ((ret = read(fd, buf_body + buf_body_read_len, buf_body_len - buf_body_read_len)) ==
               -1 && errno == EINTR);

        if (ret <= 0) {
            close(fd);
            (*env)->ReleaseStringUTFChars(env, word_, word);
            return NULL;
        }


        if (indexof(buf_body, "0\r\n\r\n") != -1) {
            break;
        }
//        LOGE("%d %d", ret, buf_body_read_len);
//
        //if (indexof(buf_body, "0\r\n\r\n") != -1) { break; }
        if (indexof(buf_body, "200 OK") == -1) {
            close(fd);
            (*env)->ReleaseStringUTFChars(env, word_, word);
            return NULL;
        }

        buf_body_read_len += ret;


    } while (1);


    char *y = strstr(buf_body, "\r\n\r\n");
    if (y == NULL || strlen(y) <= 4) {
        close(fd);
        (*env)->ReleaseStringUTFChars(env, word_, word);
        return NULL;
    }
    y = y + 4;
    char *body = strstr(y, "\r\n");
    if (body == NULL) {
        close(fd);
        (*env)->ReleaseStringUTFChars(env, word_, word);
        return NULL;
    };

    cJSON *json = cJSON_Parse(body);
    if (json == NULL) {
        close(fd);
        (*env)->ReleaseStringUTFChars(env, word_, word);
        return NULL;
    }


    cJSON *sentences = cJSON_GetObjectItem(json, "sentences");
    if (sentences == NULL) {
        cJSON_Delete(json);
        close(fd);
        (*env)->ReleaseStringUTFChars(env, word_, word);
        return NULL;
    }
    const cJSON *t = NULL;
    memset(buf_body, 0, buf_body_len);

    cJSON_ArrayForEach(t, sentences) {
        strcat(buf_body, cJSON_GetObjectItem(t, "trans")->valuestring);
    }
    (*env)->ReleaseStringUTFChars(env, word_, word);
    close(fd);
    return (*env)->NewStringUTF(env, buf_body);
}

JNIEXPORT jstring JNICALL
Java_euphoria_psycho_todo_NativeUtils_baiduTranslate(JNIEnv *env, jclass type, jstring word_,
                                                     jboolean englishToChinese) {
    if (word_ == NULL)return NULL;
    const char *word = (*env)->GetStringUTFChars(env, word_, 0);
    const char *to = englishToChinese ? "zh" : "en";
    //int ret = baidu_query_dictionary(word, &s, englishToChinese ? "zh" : "en");

    SOCKET_INIT("api.fanyi.baidu.com", "80");


    int salt = time(NULL);
    char salt_buf[11];
    snprintf(salt_buf, 11, "%d", salt);


    size_t md5_len = strlen(BAI_APP_ID) + strlen(word) + strlen(BAIDU_SECRET) + 12;
    char md5_buf[md5_len];
    memset(md5_buf, 0, md5_len);

    strcat(md5_buf, BAI_APP_ID);
    strcat(md5_buf, word);
    strcat(md5_buf, salt_buf);
    strcat(md5_buf, BAIDU_SECRET);
    md5_buf[md5_len] = 0;


    char md5[33];
    MD5_CTX md5_ctx;
    MD5Init(&md5_ctx);
    MD5Update(&md5_ctx, md5_buf, strlen(md5_buf));
    MD5Final(&md5_ctx);

    snprintf(md5, 33, "%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x",
             md5_ctx.digest[0],
             md5_ctx.digest[1], md5_ctx.digest[2], md5_ctx.digest[3], md5_ctx.digest[4],
             md5_ctx.digest[5], md5_ctx.digest[6],
             md5_ctx.digest[7], md5_ctx.digest[8], md5_ctx.digest[9], md5_ctx.digest[10],
             md5_ctx.digest[11],
             md5_ctx.digest[12], md5_ctx.digest[13], md5_ctx.digest[14], md5_ctx.digest[15]);

    const char *url_format = "/api/trans/vip/translate?q=%s&from=auto&to=%s&appid=%s&salt=%s&sign=%s";

    char buf_encode[strlen(word) * 3 + 1];
    {
        const char *path_str = word;
        size_t buf_encode_index = 0;
        while (*path_str) {
            if (isalnum(*path_str) || *path_str == '-' || *path_str == '_' || *path_str == '.' ||
                *path_str == '~') {
                buf_encode[buf_encode_index] = *path_str;
                buf_encode_index = buf_encode_index + 1;
            } else if (*path_str == ' ') {
                buf_encode[buf_encode_index] = '+';
                buf_encode_index = buf_encode_index + 1;
            } else {
                buf_encode[buf_encode_index] = '%';
                buf_encode_index = buf_encode_index + 1;
                buf_encode[buf_encode_index] = HEX_ARRAY[*path_str >> 4 & 15];
                buf_encode_index = buf_encode_index + 1;
                buf_encode[buf_encode_index] = HEX_ARRAY[*path_str & 15 & 15];
                buf_encode_index = buf_encode_index + 1;
            }
            path_str++;
        }
        buf_encode[buf_encode_index] = 0;
    }

    size_t buf_path_len =
            strlen(url_format) + strlen(buf_encode) +
            strlen(to) + strlen(BAI_APP_ID) + strlen(salt_buf) +
            strlen(md5) + 10;


    char buf_path[buf_path_len];
    memset(buf_path, 0, buf_path_len);

    snprintf(buf_path, buf_path_len, url_format, buf_encode, to, BAI_APP_ID, salt_buf, md5);

    size_t header_buf_len = buf_path_len + 128;
    char buf_header[header_buf_len];
    memset(buf_header, 0, header_buf_len);
    strcat(buf_header, "GET ");
    strcat(buf_header, buf_path);
    strcat(buf_header, " HTTP/1.1\r\n");
    strcat(buf_header, "Accept: application/json, text/javascript, */*; q=0.01\r\n");
    strcat(buf_header,
           "User-Agent: Mozilla/4.0\r\n");
    strcat(buf_header, "Host: api.fanyi.baidu.com\r\n");
    strcat(buf_header, "\r\n");

    SEND_HEADER();

    size_t buf_body_len = 1024 << 2, buf_body_read_len = 0, chunked = 0,
            content_length = 0;
    char buf_body[buf_body_len];
    memset(buf_body, 0, buf_body_len);


    do {


        ENSURE_NOT_BIG();

        while ((ret = read(fd, buf_body + buf_body_read_len, buf_body_len - buf_body_read_len)) ==
               -1 && errno == EINTR);
        //LOGE("%s", buf_body);
        if (!chunked && indexof(buf_body, "Transfer-Encoding: chunked") != -1)chunked = 1;
        if (!content_length &&
            indexof(buf_body, "Content-Length: ") != -1) {
            char tmp[10];
            memset(tmp, 0, 10);
            for (int i = indexof(buf_body, "Content-Length: ") + strlen("Content-Length: ")
                 , j = i + 10, c = 0;
                 i < j; ++i) {
                if (buf_body[i] == '\r') {
                    tmp[i] = 0;
                    break;
                }
                tmp[c++] = buf_body[i];
            }


            content_length = strtol(tmp, NULL, 10);

        }

        if (ret <= 0) {
            close(fd);
            (*env)->ReleaseStringUTFChars(env, word_, word);
            return NULL;
        }


        if (chunked && indexof(buf_body, "0\r\n\r\n") != -1) {
            break;
        }
//        if (content_length > 0) {
//            LOGE("strlen(strstr(buf_body = \n"
//                 "  \"\\r\\n\\r\\n\")) + 4 = %d\n"
//                 "  content_length = %d\n"
//                 " ", strlen(strstr(buf_body, "\r\n\r\n")) - 4, content_length);
//        }
//        if (content_length > 0) {
//            LOGE("content_length = %d %d", content_length,
//                 strlen(strstr(buf_body, "\r\n\r\n")) - 4);
//
//        }
        if (content_length > 0 && strlen(strstr(buf_body, "\r\n\r\n")) - 4 >= content_length)break;
//        LOGE("%d %d", ret, buf_body_read_len);
//
        //if (indexof(buf_body, "0\r\n\r\n") != -1) { break; }
        if (indexof(buf_body, "200 OK") == -1) {
            close(fd);
            (*env)->ReleaseStringUTFChars(env, word_, word);
            return NULL;
        }

        buf_body_read_len += ret;


    } while (1);

    char *body = strstr(buf_body, "\r\n\r\n");


    if (body == NULL) {
        close(fd);
        (*env)->ReleaseStringUTFChars(env, word_, word);
        return NULL;
    };
    body = body + 4;
    char *y = strstr(body, "\r\n");
    if (y != NULL) {
        body = y;
    }

//    body[indexof(body, "0\r\n")] = 0;
    cJSON *json = cJSON_Parse(body);
    if (json == NULL) {
        close(fd);
        (*env)->ReleaseStringUTFChars(env, word_, word);
        return NULL;
    }

    cJSON *trans_result = cJSON_GetObjectItem(json, "trans_result");

    if (trans_result == NULL) {
        close(fd);
        (*env)->ReleaseStringUTFChars(env, word_, word);
        cJSON_Delete(json);
        return NULL;
    }
    cJSON *t = NULL;
    memset(buf_body, 0, buf_body_len);

    cJSON_ArrayForEach(t, trans_result) {
        strcat(buf_body, cJSON_GetObjectItem(t, "dst")->valuestring);
        strcat(buf_body, "\n");
    }

    //LOGE("%s", buf_body);
//    LOGE("%s", buf_body);
    close(fd);
    (*env)->ReleaseStringUTFChars(env, word_, word);
    cJSON_Delete(json);
    return (*env)->NewStringUTF(env, buf_body);
}


JNIEXPORT jstring JNICALL
Java_euphoria_psycho_todo_NativeUtils_youdaoDictionary(JNIEnv *env, jclass type, jstring word_,
                                                       jboolean translate,
                                                       jboolean englishToChinese) {
    if (word_ == NULL)return NULL;
    const char *word = (*env)->GetStringUTFChars(env, word_, 0);
    const char *from = englishToChinese ? "EN" : "zh-CHS";
    const char *to = englishToChinese ? "zh-CHS" : "EN";


    int ret, fd;
    {
        struct addrinfo hints, *cur;
        memset(&hints, 0x00, sizeof(hints));
        hints.ai_family = AF_UNSPEC;
        hints.ai_socktype = SOCK_STREAM;
        hints.ai_protocol = IPPROTO_TCP;
        ret = getaddrinfo("openapi.youdao.com", "80", &hints, &cur);
        if (ret) {
            freeaddrinfo(cur);
            (*env)->ReleaseStringUTFChars(env, word_, word);
            return NULL;
        }
        fd = socket(cur->ai_family, cur->ai_socktype, cur->ai_protocol);
        if (fd < 0) {
            freeaddrinfo(cur);
            (*env)->ReleaseStringUTFChars(env, word_, word);
            return NULL;
        }
        if (connect(fd, cur->ai_addr, cur->ai_addrlen) != 0) {
            freeaddrinfo(cur);
            (*env)->ReleaseStringUTFChars(env, word_, word);
            return NULL;
        }
        freeaddrinfo(cur);
    };

    char buf_encode[strlen(word) * 3 + 1];
    const char *path_str = word;
    size_t buf_encode_index = 0;
    while (*path_str) {
        if (isalnum(*path_str) || *path_str == '-' || *path_str == '_' || *path_str == '.' ||
            *path_str == '~') {
            buf_encode[buf_encode_index] = *path_str;
            buf_encode_index = buf_encode_index + 1;
        } else if (*path_str == ' ') {
            buf_encode[buf_encode_index] = '+';
            buf_encode_index = buf_encode_index + 1;
        } else {
            buf_encode[buf_encode_index] = '%';
            buf_encode_index = buf_encode_index + 1;
            buf_encode[buf_encode_index] = HEX_ARRAY[*path_str >> 4 & 15];
            buf_encode_index = buf_encode_index + 1;
            buf_encode[buf_encode_index] = HEX_ARRAY[*path_str & 15 & 15];
            buf_encode_index = buf_encode_index + 1;
        }
        path_str++;
    }
    buf_encode[buf_encode_index] = 0;;
    size_t buf_path_len = strlen("1f5687b5a6b94361") + (strlen(word) << 2) +
                          strlen("2433z6GPFslGhUuQltdWP7CPlbk8NZC0") + 60;;
    char buf_path[buf_path_len];
    memset(buf_path, 0, buf_path_len);
    int salt = time(NULL);
    snprintf(buf_path, buf_path_len, "%s%s%d%s", "1f5687b5a6b94361", word, salt,
             "2433z6GPFslGhUuQltdWP7CPlbk8NZC0");
    char md5_buf[33];
    MD5_CTX md5_ctx;
    MD5Init(&md5_ctx);
    MD5Update(&md5_ctx, buf_path, strlen(buf_path));
    MD5Final(&md5_ctx);
    for (int i = 0, j = 0; i < 16; i++) {
        uint8_t t = md5_ctx.digest[i];
        md5_buf[j++] = HEX_ARRAY[t / 16];
        md5_buf[j++] = HEX_ARRAY[t % 16];
    }
    md5_buf[32] = 0;
    memset(buf_path, 0, buf_path_len);
    snprintf(buf_path, buf_path_len, "/api?q=%s&salt=%d&sign=%s&from=%s&appKey=%s&to=%s",
             buf_encode, salt, md5_buf, from, "1f5687b5a6b94361", to);;
    size_t buf_header_len = strlen(buf_path) + 50;
    char buf_header[buf_header_len];
    memset(buf_header, 0, buf_header_len);
    strcat(buf_header, "GET ");
    strcat(buf_header, buf_path);
    strcat(buf_header, " HTTP/1.1\r\n");
    strcat(buf_header, "Host: openapi.youdao.com\r\n");
    strcat(buf_header, "\r\n");;
    ret = send(fd, buf_header, strlen(buf_header), 0);
    if (ret <= 0) {
        close(fd);
        (*env)->ReleaseStringUTFChars(env, word_, word);
        return NULL;
    };

    size_t buf_body_len = 1024 << 2, buf_body_read_len = 0;
    char buf_body[buf_body_len];
    memset(buf_body, 0, buf_body_len);
    do {

        ENSURE_NOT_BIG();
        while ((ret = read(fd, buf_body + buf_body_read_len, buf_body_len - buf_body_read_len)) ==
               -1 && errno == EINTR);
        //LOGE("%s %s", buf_path, buf_body);
        if (ret <= 0) {
            close(fd);
            (*env)->ReleaseStringUTFChars(env, word_, word);
            return NULL;
        }
        if (indexof(buf_body, "0\r\n\r\n") != -1) { break; }
        if (indexof(buf_body, "400 Bad Request") != -1) {
            close(fd);
            (*env)->ReleaseStringUTFChars(env, word_, word);
            return NULL;
        }
        buf_body_read_len += ret;
    } while (1);


    char *y = strstr(buf_body, "\r\n\r\n");
    if (y == NULL || strlen(y) <= 4) {
        close(fd);
        (*env)->ReleaseStringUTFChars(env, word_, word);
        return NULL;
    }
    y = y + 4;
    char *body = strstr(y, "\r\n");
    if (body == NULL) {
        close(fd);
        (*env)->ReleaseStringUTFChars(env, word_, word);
        return NULL;
    };

    if (translate) {
        cJSON *json = cJSON_Parse(body);
        memset(buf_body, 0, buf_body_len);
        if (json == NULL) {
            const char *error_ptr = cJSON_GetErrorPtr();
            if (error_ptr != NULL) {
                cJSON_Delete(json);
                close(fd);
                (*env)->ReleaseStringUTFChars(env, word_, word);
                return NULL;
            }
        }
        const cJSON *translation = cJSON_GetObjectItem(json, "translation");
        if (translation == NULL) {
            cJSON_Delete(json);
            close(fd);
            (*env)->ReleaseStringUTFChars(env, word_, word);
            return NULL;
        }
        const cJSON *t = NULL;

        cJSON_ArrayForEach(t, translation) {
            strcat(buf_body, t->valuestring);
            strcat(buf_body, "\n");
        };
    } else {
        cJSON *json = cJSON_Parse(body);
        memset(buf_body, 0, buf_body_len);
        if (json == NULL) {
            const char *error_ptr = cJSON_GetErrorPtr();
            if (error_ptr != NULL) {
                cJSON_Delete(json);
                close(fd);
                (*env)->ReleaseStringUTFChars(env, word_, word);
                return NULL;
            }
        }
        const cJSON *basic = cJSON_GetObjectItem(json, "basic");
        const cJSON *explains = cJSON_GetObjectItem(basic, "explains");
        const cJSON *explain = NULL;
        cJSON_ArrayForEach(explain, explains) {
            strcat(buf_body, explain->valuestring);
            strcat(buf_body, "\n");
        };
        const cJSON *web = cJSON_GetObjectItem(json, "web");
        const cJSON *w = NULL;
        cJSON_ArrayForEach(w, web) {
            strcat(buf_body, cJSON_GetObjectItem(w, "key")->valuestring);
            const cJSON *values = cJSON_GetObjectItem(w, "value");
            const cJSON *value = NULL;
            strcat(buf_body, " ");
            cJSON_ArrayForEach(value, values) {
                strcat(buf_body, value->valuestring);
                strcat(buf_body, ",");
            }
            buf_body[strlen(buf_body) - 1] = '\n';
        }
        cJSON_Delete(json);
    }

    (*env)->ReleaseStringUTFChars(env, word_, word);
    close(fd);
    return (*env)->NewStringUTF(env, buf_body);
}
