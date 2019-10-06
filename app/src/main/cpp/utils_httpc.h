#ifndef __UTILS_HTTPC_H__
#define __UTILS_HTTPC_H__

#include <stdio.h>
#include <string.h>
#include <errno.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/time.h>
#include <unistd.h>
#include <fcntl.h>
#include <netinet/tcp.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netdb.h>
#include "log.h"

typedef enum {

    ERR_SUCCESS = 0,       // 表示成功返回

    ERR_TCP_SOCKET_FAILED = -601,    // 表示TCP连接建立套接字失败
    ERR_TCP_UNKNOWN_HOST = -602,    // 表示无法通过主机名获取IP地址
    ERR_TCP_CONNECT = -603,    // 表示建立TCP连接失败
    ERR_TCP_READ_TIMEOUT = -604,    // 表示TCP读超时
    ERR_TCP_WRITE_TIMEOUT = -605,    // 表示TCP写超时
    ERR_TCP_READ_FAIL = -606,    // 表示TCP读错误
    ERR_TCP_WRITE_FAIL = -607,    // 表示TCP写错误
    ERR_TCP_PEER_SHUTDOWN = -608,    // 表示TCP对端关闭了连接    
    ERR_TCP_NOTHING_TO_READ = -609,    // 表示底层没有数据可以读取

} ErrorCode;

int http_disconnect(uintptr_t fd);

int http_read(uintptr_t fd, unsigned char *buf, uint32_t len, uint32_t timeout_ms,
              size_t *read_len);

uintptr_t http_connect(const char *host, uint16_t port);

int http_write(uintptr_t fd, const unsigned char *buf, uint32_t len, uint32_t timeout_ms,
               size_t *written_len);

#endif