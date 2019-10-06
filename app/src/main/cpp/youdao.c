#include <stdio.h>
#include <malloc.h>
#include <assert.h>
#include <memory.h>
#include <time.h>
#include "youdao.h"
#include "tmd5/tmd5.h"
#include "cJSON/cJSON.h"
#include "rapidstring.h"
#include "log.h"
#include "utils_httpc.h"
#include "picohttpparser.h"

#define YOUDAO_HOST "openapi.youdao.com"
#define YOUDAO_APPKEY "1f5687b5a6b94361"
#define YOUDAO_SECRET "2433z6GPFslGhUuQltdWP7CPlbk8NZC0"
#define TIMEOUT_MS 10000
#define PER_M_IN_BYTES 1048576
#define BUF_SIZE 8192

static int youdao_parse_dictionary(const char *json_string, rapidstring *s);

static void get_md5(uint8_t *buf, size_t len, char *md5);

static void make_url(const char *q, char *path, size_t path_len);

// =============================================>


static void get_md5(uint8_t *buf, size_t len, char *md5) {
    MD5_CTX md5_ctx;
    MD5Init(&md5_ctx);
    MD5Update(&md5_ctx, buf, len);
    MD5Final(&md5_ctx);
    //char md5[(16 << 1)];
    static char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                               'A', 'B', 'C', 'D', 'E', 'F'};
    for (int i = 0, j = 0; i < 16; i++) {
        uint8_t t = md5_ctx.digest[i];
        md5[j++] = hexDigits[t / 16];
        md5[j++] = hexDigits[t % 16];
    }
    md5[16 << 1] = 0;
}


static void make_url(const char *q, char *path, size_t path_len) {

    size_t tmp_len = strlen(YOUDAO_APPKEY) + strlen(q) + strlen(YOUDAO_SECRET) + 11;
    char tmp[tmp_len];
    memset(tmp, 0, tmp_len);
    int salt = time(NULL);
    int len = snprintf(tmp, tmp_len, "%s%s%d%s", YOUDAO_APPKEY, q, salt, YOUDAO_SECRET);
    tmp[len] = 0;

    char md5[(16 << 1) + 1];
    get_md5((uint8_t *) tmp, (size_t) len, md5);

    char *p
            = "/api?q=%s&salt=%d&sign=%s&from=%s&appKey=%s&to=%s";

    snprintf(path, path_len, p, q, salt, md5, "EN", YOUDAO_APPKEY, "zh-CHS");
}


static int youdao_parse_dictionary(const char *json_string, rapidstring *s) {
    int ret = 0;

    cJSON *json = cJSON_Parse(json_string);
    if (json == NULL) {
        const char *error_ptr = cJSON_GetErrorPtr();
        if (error_ptr != NULL) {
            fprintf(stderr, "Error before: %s\n", error_ptr);
            ret = 1;
            goto end;
        }
    }
    const cJSON *basic = cJSON_GetObjectItem(json, "basic");
    const cJSON *explains = cJSON_GetObjectItem(basic, "explains");
    const cJSON *explain = NULL;

    cJSON_ArrayForEach(explain, explains) {
        rs_cat(s, explain->valuestring);
        rs_cat(s, "\n");
    };
    const cJSON *web = cJSON_GetObjectItem(json, "web");
    const cJSON *w = NULL;
    cJSON_ArrayForEach(w, web) {
        rs_cat(s, cJSON_GetObjectItem(w, "key")->valuestring);
        const cJSON *values = cJSON_GetObjectItem(w, "value");
        const cJSON *value = NULL;
        cJSON_ArrayForEach(value, values) {
            rs_cat(s, value->valuestring);
            rs_cat(s, ",");
        }
        rs_cat(s, "\n");
    }
    end:
    cJSON_Delete(json);
    return ret;
}

static void make_header(char *buf, const char *path) {
    strcat(buf, "GET ");
    strcat(buf, path);
    strcat(buf, " HTTP/1.1\r\n");
    strcat(buf, "Host: openapi.youdao.com\r\n");
    strcat(buf, "\r\n");
}

static char *read_body(uintptr_t fd) {
    struct phr_chunked_decoder decoder = {}; /* zero-clear */
    char *buf = malloc(4096);
    size_t size = 0, capacity = 4096, rsize;
    ssize_t rret, pret;

/* set consume_trailer to 1 to discard the trailing header, or the application
 * should call phr_parse_headers to parse the trailing header */
    decoder.consume_trailer = 1;

    do {
        /* expand the buffer if necessary */
        if (size == capacity) {
            capacity *= 2;
            buf = realloc(buf, capacity);
            assert(buf != NULL);
        }
        /* read */
        while ((rret = read(fd, buf + size, capacity - size)) == -1 && errno == EINTR);
        if (rret <= 0) {
            LOGE("%s\n", "IO Error");
            return NULL;
        }
        /* decode */
        rsize = rret;
        pret = phr_decode_chunked(&decoder, buf + size, &rsize);
        if (pret == -1) {
            LOGE("%s\n", "Parse Error");
            return NULL;
        }
        size += rsize;
    } while (pret == -2);

/* successfully decoded the chunked data */
    assert(pret >= 0);
    printf("decoded data is at %p (%zu bytes)\n", buf, size);
    return buf;
}

char *youdao_query_dictionary(const char *q) {

    int ret = 0;

// 1.
    uintptr_t fd = http_connect(YOUDAO_HOST, 80);
    if (!fd) {
        // cannot jump from this goto statement to its label
        return NULL;
    }
// 2.
    size_t path_len = 256 + strlen(q);
    char path[path_len];
    make_url(q, path, path_len);

    // 3.
    size_t header_len = 256 + path_len;
    char buf[header_len];
    memset(buf, 0, header_len);
    make_header(buf, path);

    // 4.
    size_t write_len = 0;
    ret = http_write(fd, buf, strlen(buf), TIMEOUT_MS, &write_len);
    if (ret != ERR_SUCCESS) {
        goto end;
    }
    //5.

    char *body = read_body(fd);

    LOGE("%s\n", body);
    end:
    http_disconnect(fd);
    return NULL;

}
