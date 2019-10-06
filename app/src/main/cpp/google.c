#include "google.h"
#include "utils_url.h"
#include "utils_httpc.h"
#include "utils_string.h"
#include "cJSON/cJSON.h"

// (?<!// )LOGE\([^\n]*?\n
// // $0

#define GOOLGE_API_HOST "translate.google.cn"

static void make_header(char *buf, const char *path) {
    strcat(buf, "GET ");
    strcat(buf, path);
    strcat(buf, " HTTP/1.1\r\n");
    strcat(buf, "Accept: application/json, text/javascript, */*; q=0.01\r\n");
    strcat(buf,
           "User-Agent: Mozilla/4.0\r\n");
    strcat(buf, "Host: translate.google.cn\r\n");
    strcat(buf, "\r\n");
}

static char *read_body(uintptr_t fd) {
    char *buf = malloc(4096);
    memset(buf, 0, 4096);
    size_t size = 0, capacity = 4096;
    ssize_t rret;

/* set consume_trailer to 1 to discard the trailing header, or the application
 * should call phr_parse_headers to parse the trailing header */

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
            // LOGE("%s\n", "IO Error");
            free(buf);
            return NULL;
        }
        // LOGE("%d:%d:%s", rret, strlen(buf), buf);

        if (indexof(buf, "0\r\n\r\n") != -1) {
            // LOGE("Find the end mark %d\n", indexof(buf, "0\r\n\r\n"));
            break;
        }
        if (indexof(buf, "400 Bad Request") != -1) {
            return NULL;
        }
        /* decode */
//        pret = phr_decode_chunked(&decoder, buf + size, &rsize);
//        if (pret == -1) {
//            // LOGE("%s\n", "Parse Error");
//            // LOGE("%s\n", buf);
//            return NULL;
//        }
        size += rret;
    } while (1);

    return buf;
}

int google_translate(const char *q, rapidstring *s, const char *to) {
    int ret = 0;
    // ------------------
    uintptr_t fd = http_connect(GOOLGE_API_HOST, 80);
    if (!fd) {
        return 1;
    }

    // ------------------
    char *eq = url_encode(q);
    const char *url_format = "/translate_a/single?client=gtx&sl=auto&tl=%s&dt=t&dt=bd&ie=UTF-8&oe=UTF-8&dj=1&source=icon&q=%s";
    size_t url_buf_len = strlen(eq) + strlen(url_format) + strlen(to);
    char url_buf[url_buf_len];
    memset(url_buf, 0, url_buf_len);
    snprintf(url_buf, url_buf_len, url_format, to, eq);
    free(eq);
    // LOGE("url_buf %s\n", url_buf);

    // ------------------
    size_t header_buf_len = url_buf_len + 256;
    char header_buf[header_buf_len];
    memset(header_buf, 0, header_buf_len);
    make_header(header_buf, url_buf);
    // LOGE("header_buf %s\n", header_buf);

    // ------------------
    size_t write_len = 0;
    ret = http_write(fd, header_buf, strlen(header_buf), TIMEOUT_MS, &write_len);
    // LOGE("http_write header_buf_len(%d),header_buf(%d) %d\n", header_buf_len, strlen(header_buf),ret);

    if (ret != ERR_SUCCESS) {
        goto end;
    }

    // ------------------
//    char *body = read_body(fd);
//    if (body == NULL) {
//        free(body);
//        ret = 1;
//        goto end;
//    }
//    // LOGE("%s\n", body);
//    body = strstr(body, "\r\n\r\n");
//    // LOGE("%s\n", body);
//    if (body == NULL) {
//        free(body);
//        ret = 1;
//        goto end;
//    }
//    body = body + 4;
//    body = strstr(body, "\r\n");
//    if (body == NULL) {
//        free(body);
//        return NULL;
//    }
    char *hb = read_body(fd);
    if (hb == NULL) {
        return 1;
    }
    char *mb = strstr(hb, "\r\n\r\n");
    if (mb == NULL) {
        free(hb);
        return 1;
    }
    mb = mb + 4;
    char *body = strstr(mb, "\r\n");
    if (body == NULL) {
        free(hb);
        return 1;
    };
    // LOGE("%s\n", body);

    // ------------------
    cJSON *json = cJSON_Parse(body);
    if (json == NULL) {
        free(body);
        ret = 1;
        goto end;
    }
    free(body);

    cJSON *sentences = cJSON_GetObjectItem(json, "sentences");
    if (sentences == NULL) {
        ret = 1;
        cJSON_Delete(json);
        goto end;
    }
    const cJSON *t = NULL;

    cJSON_ArrayForEach(t, sentences) {
        rs_cat(s, cJSON_GetObjectItem(t, "trans")->valuestring);
        rs_cat(s, "\n");
    }
    // ------------------
    end:
    http_disconnect(fd);
    return ret;
}