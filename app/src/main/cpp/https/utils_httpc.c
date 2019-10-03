#include "utils_httpc.h"


#include <string.h>
#include <ctype.h>
#include <string.h>
#include "config.h"

#define HTTP_CLIENT_AUTHB_SIZE     128

#define HTTP_CLIENT_CHUNK_SIZE     1024
#define HTTP_CLIENT_SEND_BUF_SIZE  1024

#define HTTP_CLIENT_MAX_HOST_LEN   64
#define HTTP_CLIENT_MAX_URL_LEN    1024

static void _http_client_base64enc(char *out, const char *in) {
    const char code[] = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";
    int i = 0, x = 0, l = 0;

    for (; *in; in++) {
        x = x << 8 | *in;
        for (l += 8; l >= 6; l -= 6) {
            out[i++] = code[(x >> (l - 6)) & 0x3f];
        }
    }
    if (l > 0) {
        x <<= 6 - l;
        out[i++] = code[x & 0x3f];
    }
    for (; i % 4;) {
        out[i++] = '=';
    }
    out[i] = '\0';
}

static int
_http_client_parse_url(const char *url, char *scheme, uint32_t max_scheme_len, char *host,
                       uint32_t maxhost_len,
                       int *port, char *path, uint32_t max_path_len) {
    char *scheme_ptr = (char *) url;
    char *host_ptr = (char *) strstr(url, "://");
    uint32_t host_len = 0;
    uint32_t path_len;

    char *path_ptr;
    char *fragment_ptr;

    if (host_ptr == NULL) {
        Log_e("Could not find host");
        return QCLOUD_ERR_HTTP_PARSE;
    }

    if (max_scheme_len < host_ptr - scheme_ptr + 1) {
        Log_e("Scheme str is too small (%u >= %u)", max_scheme_len,
              (uint32_t) (host_ptr - scheme_ptr + 1));
        return QCLOUD_ERR_HTTP_PARSE;
    }
    memcpy(scheme, scheme_ptr, host_ptr - scheme_ptr);
    scheme[host_ptr - scheme_ptr] = '\0';

    host_ptr += 3;

    *port = 0;

    path_ptr = strchr(host_ptr, '/');
    if (NULL == path_ptr) {
        path_ptr = scheme_ptr + (int) strlen(url);
        host_len = path_ptr - host_ptr;
        memcpy(host, host_ptr, host_len);
        host[host_len] = '\0';

        memcpy(path, "/", 1);
        path[1] = '\0';

        return QCLOUD_ERR_SUCCESS;
    }

    if (host_len == 0) {
        host_len = path_ptr - host_ptr;
    }

    if (maxhost_len < host_len + 1) {
        Log_e("Host str is too long (host_len(%d) >= max_len(%d))", host_len + 1, maxhost_len);
        return QCLOUD_ERR_HTTP_PARSE;
    }
    memcpy(host, host_ptr, host_len);
    host[host_len] = '\0';

    fragment_ptr = strchr(host_ptr, '#');
    if (fragment_ptr != NULL) {
        path_len = fragment_ptr - path_ptr;
    } else {
        path_len = strlen(path_ptr);
    }

    if (max_path_len < path_len + 1) {
        Log_e("Path str is too small (%d >= %d)", max_path_len, path_len + 1);
        return QCLOUD_ERR_HTTP_PARSE;
    }

    memcpy(path, path_ptr, path_len);

    path[path_len] = '\0';

    return QCLOUD_ERR_SUCCESS;
}

static int _http_client_parse_host(const char *url, char *host, uint32_t host_max_len) {
    const char *host_ptr = (const char *) strstr(url, "://");
    uint32_t host_len = 0;
    char *path_ptr;

    if (host_ptr == NULL) {
        Log_e("Could not find host");
        return QCLOUD_ERR_HTTP_PARSE;
    }
    host_ptr += 3;

    uint32_t pro_len = 0;
    pro_len = host_ptr - url;

    path_ptr = strchr(host_ptr, '/');
    if (path_ptr != NULL)
        host_len = path_ptr - host_ptr;
    else
        host_len = strlen(url) - pro_len;

    if (host_max_len < host_len + 1) {
        Log_e("Host str is too small (%d >= %d)", host_max_len, host_len + 1);
        return QCLOUD_ERR_HTTP_PARSE;
    }
    memcpy(host, host_ptr, host_len);
    host[host_len] = '\0';

    return QCLOUD_ERR_SUCCESS;
}

static int _http_client_send_auth(HTTPClient *client, unsigned char *send_buf, int *send_idx) {
    char b_auth[(int) ((HTTP_CLIENT_AUTHB_SIZE + 3) * 4 / 3 + 1)];
    char base64buff[HTTP_CLIENT_AUTHB_SIZE + 3];

    _http_client_get_info(client, send_buf, send_idx, "Authorization: Basic ", 0);
    HAL_Snprintf(base64buff, sizeof(base64buff), "%s:%s", client->auth_user, client->auth_password);

    _http_client_base64enc(b_auth, base64buff);
    b_auth[strlen(b_auth) + 1] = '\0';
    b_auth[strlen(b_auth)] = '\n';

    _http_client_get_info(client, send_buf, send_idx, b_auth, 0);

    return QCLOUD_ERR_SUCCESS;
}

static int
_http_client_get_info(HTTPClient *client, unsigned char *send_buf, int *send_idx, char *buf,
                      uint32_t len) {
    int rc = QCLOUD_ERR_SUCCESS;
    int cp_len;
    int idx = *send_idx;

    if (len == 0) {
        len = strlen(buf);
    }

    do {
        if ((HTTP_CLIENT_SEND_BUF_SIZE - idx) >= len) {
            cp_len = len;
        } else {
            cp_len = HTTP_CLIENT_SEND_BUF_SIZE - idx;
        }

        memcpy(send_buf + idx, buf, cp_len);
        idx += cp_len;
        len -= cp_len;

        if (idx == HTTP_CLIENT_SEND_BUF_SIZE) {
            size_t byte_written_len = 0;
            rc = client->network_stack.write(&(client->network_stack), send_buf,
                                             HTTP_CLIENT_SEND_BUF_SIZE, 5000, &byte_written_len);
            if (byte_written_len) {
                return (byte_written_len);
            }
        }
    } while (len);

    *send_idx = idx;
    return rc;
}

static int _http_client_send_header(HTTPClient *client, const char *url, HttpMethod method,
                                    HTTPClientData *client_data) {
    char scheme[8] = {0};
    char host[HTTP_CLIENT_MAX_HOST_LEN] = {0};
    char path[HTTP_CLIENT_MAX_URL_LEN] = {0};
    int len;
    unsigned char send_buf[HTTP_CLIENT_SEND_BUF_SIZE] = {0};
    char buf[HTTP_CLIENT_SEND_BUF_SIZE] = {0};
    char *meth = (method == HTTP_GET) ? "GET" : (method == HTTP_POST) ? "POST" :
                                                (method == HTTP_PUT) ? "PUT" : (method ==
                                                                                HTTP_DELETE)
                                                                               ? "DELETE" :
                                                                               (method == HTTP_HEAD)
                                                                               ? "HEAD" : "";
    int rc;
    int port;

    int res = _http_client_parse_url(url, scheme, sizeof(scheme), host, sizeof(host), &port, path,
                                     sizeof(path));
    if (res != QCLOUD_ERR_SUCCESS) {
        Log_e("httpclient_parse_url returned %d", res);
        return res;
    }

    if (strcmp(scheme, "http") == 0) {

    } else if (strcmp(scheme, "https") == 0) {

    }

    memset(send_buf, 0, HTTP_CLIENT_SEND_BUF_SIZE);
    len = 0;

    HAL_Snprintf(buf, sizeof(buf), "%s %s HTTP/1.1\r\nHost: %s\r\n", meth, path, host);
    rc = _http_client_get_info(client, send_buf, &len, buf, strlen(buf));
    if (rc) {
        Log_e("Could not write request");
        return QCLOUD_ERR_HTTP_CONN;
    }

    if (client->auth_user) {
        _http_client_send_auth(client, send_buf, &len);
    }

    if (client->header) {
        _http_client_get_info(client, send_buf, &len, (char *) client->header,
                              strlen(client->header));
    }

    if (client_data->post_buf != NULL) {
        HAL_Snprintf(buf, sizeof(buf), "Content-Length: %d\r\n", client_data->post_buf_len);
        _http_client_get_info(client, send_buf, &len, buf, strlen(buf));

        if (client_data->post_content_type != NULL) {
            HAL_Snprintf(buf, sizeof(buf), "Content-Type: %s\r\n", client_data->post_content_type);
            _http_client_get_info(client, send_buf, &len, buf, strlen(buf));
        }
    }

    _http_client_get_info(client, send_buf, &len, "\r\n", 0);

    //Log_d("REQUEST:\n%s", send_buf);

    size_t written_len = 0;
    rc = client->network_stack.write(&client->network_stack, send_buf, len, 5000, &written_len);
    if (written_len > 0) {
        //Log_d("Written %lu bytes", written_len);
    } else if (written_len == 0) {
        Log_e("written_len == 0,Connection was closed by server");
        return QCLOUD_ERR_HTTP_CLOSED; /* Connection was closed by server */
    } else {
        Log_e("Connection error (send returned %d)", rc);
        return QCLOUD_ERR_HTTP_CONN;
    }

    return QCLOUD_ERR_SUCCESS;
}