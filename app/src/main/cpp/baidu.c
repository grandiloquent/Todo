#include <time.h>
#include "baidu.h"
#include "log.h"
#include "tmd5/tmd5.h"
#include "utils_url.h"
#include "utils_httpc.h"
#include "utils_string.h"
#include "cJSON/cJSON.h"

#define BAI_API_HOST "api.fanyi.baidu.com"
#define BAI_APP_ID "20190312000276185"

// http://api.fanyi.baidu.com/api/trans/vip/translate?q=word&from=auto&to=zh&appid=20190312000276185&salt=1570365910&sign=b302758513d7e2aa0e72b3e4e7548028
static inline unsigned char
int_to_hex(uint8_t i) {
    return (unsigned char) ((i > 0x09) ? ((i - 10) + 'A') : i + '0');
}

static void get_md5(uint8_t *buf, size_t len, char *output) {
    MD5_CTX md5_ctx;
    MD5Init(&md5_ctx);
    MD5Update(&md5_ctx, buf, len);
    MD5Final(&md5_ctx);
    //char md5[(16 << 1)];
    static char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                               'A', 'B', 'C', 'D', 'E', 'F'};
//    int i, j;
//
//
//    for (i = 0, j = 0; j < 16; i++, j += 4) {
//        output[j] = (unsigned char) (hexDigits[md5_ctx.digest[i] & 0xff]);
//        output[j + 1] = (unsigned char) (hexDigits[(md5_ctx.digest[i] >> 8) & 0xff]);
//        output[j + 2] = (unsigned char) (hexDigits[(md5_ctx.digest[i] >> 16) & 0xff]);
//        output[j + 3] = (unsigned char) (hexDigits[(md5_ctx.digest[i] >> 24) & 0xff]);
//    }
//    output[16] = 0;

    MD5_CTX context = md5_ctx;
    snprintf(output, 33, "%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x",
             context.digest[0],
             context.digest[1], context.digest[2], context.digest[3], context.digest[4],
             context.digest[5], context.digest[6],
             context.digest[7], context.digest[8], context.digest[9], context.digest[10],
             context.digest[11],
             context.digest[12], context.digest[13], context.digest[14], context.digest[15]);
}

static char *read_body(uintptr_t fd) {
    // !!!free
    char *buf = malloc(4096);
    memset(buf, 0, 4096);
    size_t size = 0, capacity = 4096;
    ssize_t rret;
    char *header = NULL;
    size_t length = 0;
    char size_buf[10];
    int found = 0;

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

        if (header == NULL && (header = strstr(buf, "Content-Length: ")) != NULL) {
            const char *tmp = header + strlen("Content-Length: ");

            int count = 0;
            while (*tmp && *tmp != '\r') {
                size_buf[count++] = *tmp++;
            }
            size_buf[count] = 0;
            length = (size_t) strtol(size_buf, NULL, 10);
            if (length == 0) {
                free(buf);
                return NULL;
            }
        }
        if (indexof(buf, "\r\n\r\n") != -1) {
            buf = strstr(buf, "\r\n\r\n") + 4;
            found = 1;
        }
//        LOGE(
//                "size_buf = %s\n"
//                "found = %d\n"
//                "  length = %d\n"
//                "  header = %s\n"
//                "  buf = %s\n"
//                "  strlen(buf) = %d\n"
//                " ", size_buf, found, length, header, buf, strlen(buf));
        if (found && length && strlen(buf) >= length) {

            break;
        }


        if (indexof(buf, "400 Bad Request") != -1) {
            free(buf);
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

static void make_header(char *buf, const char *path) {
    strcat(buf, "GET ");
    strcat(buf, path);
    strcat(buf, " HTTP/1.1\r\n");
    strcat(buf, "Accept: application/json, text/javascript, */*; q=0.01\r\n");
    strcat(buf,
           "User-Agent: Mozilla/4.0\r\n");
    strcat(buf, "Host: api.fanyi.baidu.com\r\n");
    strcat(buf, "\r\n");
}

int baidu_query_dictionary(const char *q, rapidstring *s,
                           const char *to) {
    int ret = 0;
    // ------------------
    uintptr_t fd = http_connect(BAI_API_HOST, 80);
    if (!fd) {
        LOGE("Failed at connect to (%s)\n", BAI_API_HOST);
        return 1;
    }
    // ------------------

    const char *secret = "sdK6QhtFE64Qm0ID_SjG";

    int salt = time(NULL);
    char salt_buf[11];
    snprintf(salt_buf, 11, "%d", salt);

    size_t md5_len = strlen(BAI_APP_ID) + strlen(q) + strlen(secret) + 12;
    char md5_buf[md5_len];
    memset(md5_buf, 0, md5_len);

    strcat(md5_buf, BAI_APP_ID);
    strcat(md5_buf, q);
    strcat(md5_buf, salt_buf);
    strcat(md5_buf, secret);

    md5_buf[md5_len] = 0;


    char md5[33];
    get_md5((uint8_t *) md5_buf, strlen(md5_buf), md5);


    const char *format = "/api/trans/vip/translate?q=%s&from=auto&to=%s&appid=%s&salt=%s&sign=%s";

    // !!!free
    char *eq = url_encode(q);

    size_t path_len =
            strlen(format) + strlen(eq) + strlen(to) + strlen(BAI_APP_ID) + strlen(salt_buf) +
            strlen(md5) + 10;


    char path_buf[path_len];
    memset(path_buf, 0, path_len);

    snprintf(path_buf, path_len, format, eq, to, BAI_APP_ID, salt_buf, md5);

    free(eq);


//    LOGE("md5_len = %d\n"
//         "  strlen(md5_buf) = %d\n"
//         "  md5_buf = %s\n"
//         "  md5 = %s\n"
//         "  salt_buf = %s\n"
//         "  path_len = %d\n"
//         "  strlen(path_buf) = %d\n"
//         "  path_buf = %s\n",
//         md5_len,
//         strlen(md5_buf),
//         md5_buf,
//         md5,
//         salt_buf,
//         path_len,
//         strlen(path_buf),
//         path_buf);

    // ------------------
    size_t header_buf_len = path_len + 128;
    char header_buf[header_buf_len];
    memset(header_buf, 0, header_buf_len);
    make_header(header_buf, path_buf);

//    LOGE("path_len = %d\n"
//         "  header_buf_len = %d\n"
//         "  strlen(header_buf) = %d\n"
//         "  header_buf = %s\n"
//         " ", path_len, header_buf_len, strlen(header_buf), header_buf);

    // ------------------
    size_t write_len = 0;
    ret = http_write(fd, header_buf, strlen(header_buf), TIMEOUT_MS, &write_len);
    // LOGE("http_write header_buf_len(%d),header_buf(%d) %d\n", header_buf_len, strlen(header_buf),ret);

    if (ret != ERR_SUCCESS) {
        goto end;
    }

    // ------------------
    // !!!free
    char *hb = read_body(fd);
    if (hb == NULL) {
        ret = 1;
        goto end;
    }
    // ------------------
    // !!!free
    cJSON *json = cJSON_Parse(hb);
    if (json == NULL) {
        free(hb);
        ret = 1;
        goto end;
    }
    cJSON *trans_result = cJSON_GetObjectItem(json, "trans_result");
    if (trans_result == NULL) {
        free(hb);
        free(json);
        ret = 1;
        goto end;
    }
    cJSON *t = NULL;

    cJSON_ArrayForEach(t, trans_result) {
         rs_cat(s, cJSON_GetObjectItem(t, "dst")->valuestring);
        rs_cat(s, "\n");
    }

    free(hb);

    cJSON_Delete(json);
    end:
    http_disconnect(fd);
    return ret;
}