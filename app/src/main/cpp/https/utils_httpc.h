#ifndef UTILS_HTTPC_H__
#define  UTILS_HTTPC_H__

#include <stdbool.h>

#include "network_interface.h"

typedef enum {
    HTTP_GET,
    HTTP_POST,
    HTTP_PUT,
    HTTP_DELETE,
    HTTP_HEAD
} HttpMethod;

typedef struct {
    int remote_port;        // 端口号
    int response_code;      // 响应码
    char *header;            // 自定义头部
    char *auth_user;         // 身份验证的用户名
    char *auth_password;     // 身份验证的密码
    Network network_stack;
} HTTPClient;

typedef struct {
    bool    is_more;                // 是否需要检索更多的数据
    bool    is_chunked;             // 响应数据是否以分块进行编码
    int     retrieve_len;           // 要检索的内容长度
    int     response_content_len;   // 响应内容长度
    int     post_buf_len;           // post data length
    int     response_buf_len;       // 响应包缓冲区长度
    char    *post_content_type;     // post数据的内容类型
    char    *post_buf;              // post的数据
    char    *response_buf;          // 存储响应数据的缓冲区
} HTTPClientData;



#endif