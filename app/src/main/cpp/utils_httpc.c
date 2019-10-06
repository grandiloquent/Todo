#include "utils_httpc.h"

static uint64_t _linux_get_time_ms(void);

static uint64_t _linux_time_left(uint64_t t_end, uint64_t t_now);

// =============================================>
static uint64_t _linux_get_time_ms(void) {
    struct timeval tv = {0};
    uint64_t time_ms;

    gettimeofday(&tv, NULL);

    time_ms = tv.tv_sec * 1000 + tv.tv_usec / 1000;

    return time_ms;
}


static uint64_t _linux_time_left(uint64_t t_end, uint64_t t_now) {
    uint64_t t_left;

    if (t_end > t_now) {
        t_left = t_end - t_now;
    } else {
        t_left = 0;
    }

    return t_left;
}


uintptr_t http_connect(const char *host, uint16_t port) {
    int ret;
    struct addrinfo hints, *addr_list, *cur;
    int fd = 0;

    char port_str[6];
    snprintf(port_str, 6, "%d", port);

    memset(&hints, 0x00, sizeof(hints));
    hints.ai_family = AF_UNSPEC;
    hints.ai_socktype = SOCK_STREAM;
    hints.ai_protocol = IPPROTO_TCP;

    ret = getaddrinfo(host, port_str, &hints, &addr_list);
    if (ret) {
        if (ret == EAI_SYSTEM)
            LOGE("getaddrinfo(%s:%s) error: %s", host, port_str, strerror(errno));
        else
            LOGE("getaddrinfo(%s:%s) error: %s", host, port_str, gai_strerror(ret));
        return 0;
    }

    for (cur = addr_list; cur != NULL; cur = cur->ai_next) {
        fd = (int) socket(cur->ai_family, cur->ai_socktype, cur->ai_protocol);
        if (fd < 0) {
            ret = 0;
            continue;
        }

        if (connect(fd, cur->ai_addr, cur->ai_addrlen) == 0) {
            ret = fd;
            break;
        }

        close(fd);
        ret = 0;
    }

    if (0 == ret) {
        LOGE("fail to connect with TCP server: %s:%s", host, port_str);
    } else {

    }

    freeaddrinfo(addr_list);

    return (uintptr_t) ret;
}


int http_disconnect(uintptr_t fd) {
    int rc;

    /* Shutdown both send and receive operations. */
    rc = shutdown((int) fd, 2);
    if (0 != rc) {
        LOGE("shutdown error: %s", strerror(errno));
        return -1;
    }

    rc = close((int) fd);
    if (0 != rc) {
        LOGE("closesocket error: %s", strerror(errno));
        return -1;
    }

    return 0;
}


int http_read(uintptr_t fd, unsigned char *buf, uint32_t len, uint32_t timeout_ms,
              size_t *read_len) {
    int ret, err_code;
    uint32_t len_recv;
    uint64_t t_end, t_left;
    fd_set sets;
    struct timeval timeout;

    t_end = _linux_get_time_ms() + timeout_ms;
    len_recv = 0;
    err_code = 0;

    do {
        t_left = _linux_time_left(t_end, _linux_get_time_ms());
        if (0 == t_left) {
            err_code = ERR_TCP_READ_TIMEOUT;
            break;
        }

        FD_ZERO(&sets);
        FD_SET(fd, &sets);

        timeout.tv_sec = t_left / 1000;
        timeout.tv_usec = (t_left % 1000) * 1000;

        ret = select(fd + 1, &sets, NULL, NULL, &timeout);
        if (ret > 0) {
            ret = recv(fd, buf + len_recv, len - len_recv, 0);
            if (ret > 0) {
                len_recv += ret;
            } else if (0 == ret) {
                struct sockaddr_in peer;
                socklen_t sLen = sizeof(peer);
                int peer_port = 0;
                getpeername(fd, (struct sockaddr *) &peer, &sLen);
                peer_port = ntohs(peer.sin_port);

                /* reduce log print due to frequent log server connect/disconnect */
//                if (peer_port == LOG_UPLOAD_SERVER_PORT)
//                    UPLOAD_DBG("connection is closed by server: %s:%d", inet_ntoa(peer.sin_addr),
//                               peer_port);
//                else
//                    LOGE("connection is closed by server: %s:%d", inet_ntoa(peer.sin_addr),
//                         peer_port);

                err_code = ERR_TCP_PEER_SHUTDOWN;
                break;
            } else {
                if (EINTR == errno) {
                    LOGE("EINTR be caught");
                    continue;
                }
                LOGE("recv error: %s", strerror(errno));
                err_code = ERR_TCP_READ_FAIL;
                break;
            }
        } else if (0 == ret) {
            err_code = ERR_TCP_READ_TIMEOUT;
            break;
        } else {
            LOGE("select-recv error: %s", strerror(errno));
            err_code = ERR_TCP_READ_FAIL;
            break;
        }
    } while ((len_recv < len));

    *read_len = (size_t) len_recv;

    if (err_code == ERR_TCP_READ_TIMEOUT && len_recv == 0)
        err_code = ERR_TCP_NOTHING_TO_READ;

    return (len == len_recv) ? ERR_SUCCESS : err_code;
}


int http_write(uintptr_t fd, const unsigned char *buf, uint32_t len, uint32_t timeout_ms,
               size_t *written_len) {
    int ret;
    uint32_t len_sent;
    uint64_t t_end, t_left;
    fd_set sets;

    t_end = _linux_get_time_ms() + timeout_ms;
    len_sent = 0;
    ret = 1; /* send one time if timeout_ms is value 0 */

    do {
        t_left = _linux_time_left(t_end, _linux_get_time_ms());

        if (0 != t_left) {
            struct timeval timeout;

            FD_ZERO(&sets);
            FD_SET(fd, &sets);

            timeout.tv_sec = t_left / 1000;
            timeout.tv_usec = (t_left % 1000) * 1000;

            ret = select(fd + 1, NULL, &sets, NULL, &timeout);
            if (ret > 0) {
                if (0 == FD_ISSET(fd, &sets)) {
                    LOGE("Should NOT arrive");
                    /* If timeout in next loop, it will not sent any data */
                    ret = 0;
                    continue;
                }
            } else if (0 == ret) {
                ret = ERR_TCP_WRITE_TIMEOUT;
                LOGE("select-write timeout %d", (int) fd);
                break;
            } else {
                if (EINTR == errno) {
                    LOGE("EINTR be caught");
                    continue;
                }

                ret = ERR_TCP_WRITE_FAIL;
                LOGE("select-write fail: %s", strerror(errno));
                break;
            }
        } else {
            ret = ERR_TCP_WRITE_TIMEOUT;
        }

        if (ret > 0) {
            ret = send(fd, buf + len_sent, len - len_sent, 0);
            if (ret > 0) {
                len_sent += ret;
            } else if (0 == ret) {
                LOGE("No data be sent. Should NOT arrive");
            } else {
                if (EINTR == errno) {
                    LOGE("EINTR be caught");
                    continue;
                }

                ret = ERR_TCP_WRITE_FAIL;
                LOGE("send fail: %s", strerror(errno));
                break;
            }
        }
    } while ((len_sent < len) && (_linux_time_left(t_end, _linux_get_time_ms()) > 0));

    *written_len = (size_t) len_sent;

    return len_sent > 0 ? ERR_SUCCESS : ret;
}
