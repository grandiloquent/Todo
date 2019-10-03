#ifndef CONFIG_H__
#define  CONFIG_H__
#include <stdint.h>
#include <stdbool.h>
// ================>
typedef enum {
    LOG_DISABLE = 0,
    LOG_ERROR = 1,
    LOG_WARN = 2,
    LOG_INFO = 3,
    LOG_DEBUG = 4
} LOG_LEVEL;
void
Log_writter(const char *file, const char *func, const int line, const int level, const char *fmt,
            ...);
#define Log_d(fmt, ...) Log_writter(__FILE__, __FUNCTION__, __LINE__, LOG_DEBUG, fmt, ##__VA_ARGS__)
#define Log_i(fmt, ...) Log_writter(__FILE__, __FUNCTION__, __LINE__, LOG_INFO,  fmt, ##__VA_ARGS__)
#define Log_w(fmt, ...) Log_writter(__FILE__, __FUNCTION__, __LINE__, LOG_WARN,  fmt, ##__VA_ARGS__)
#define Log_e(fmt, ...) Log_writter(__FILE__, __FUNCTION__, __LINE__, LOG_ERROR, fmt, ##__VA_ARGS__)
#ifdef IOT_DEBUG
#define IOT_FUNC_ENTRY    \
        {\
        printf("FUNC_ENTRY:   %s L#%d \n", __FUNCTION__, __LINE__);  \
        }
#define IOT_FUNC_EXIT    \
        {\
        printf("FUNC_EXIT:   %s L#%d \n", __FUNCTION__, __LINE__);  \
        return;\
        }
#define IOT_FUNC_EXIT_RC(x)    \
        {\
        printf("FUNC_EXIT:   %s L#%d Return Code : %ld \n", __FUNCTION__, __LINE__, (long)(x));  \
        return x; \
        }
#else
#define IOT_FUNC_ENTRY
#define IOT_FUNC_EXIT            \
        {\
            return;\
        }
#define IOT_FUNC_EXIT_RC(x)     \
        {\
            return x; \
        }
#endif
#define LOG_UPLOAD_SERVER_PORT    80
#ifdef LOG_UPLOAD_DEBUG
#define UPLOAD_DBG(fmt, ...)   HAL_Printf(">>LOG-DBG>>%s(%d): " fmt "\n", __FUNCTION__, __LINE__, ##__VA_ARGS__)
#else
#define UPLOAD_DBG(...)
#endif
#define UPLOAD_ERR(fmt, ...)   HAL_Printf(">>LOG-ERR>>%s(%d): " fmt "\n", __FUNCTION__, __LINE__, ##__VA_ARGS__)
// ================>
typedef enum {
    QCLOUD_ERR_MQTT_ALREADY_CONNECTED = 4,       // 表示与MQTT服务器已经建立连接
    QCLOUD_ERR_MQTT_CONNACK_CONNECTION_ACCEPTED = 3,       // 表示服务器接受客户端MQTT连接
    QCLOUD_ERR_MQTT_MANUALLY_DISCONNECTED = 2,       // 表示与MQTT服务器已经手动断开
    QCLOUD_ERR_MQTT_RECONNECTED = 1,       // 表示与MQTT服务器重连成功
    QCLOUD_ERR_HTTP_CLOSED = -3,      // 远程主机关闭连接
    QCLOUD_ERR_HTTP = -4,      // HTTP未知错误
    QCLOUD_ERR_HTTP_PRTCL = -5,      // 协议错误
    QCLOUD_ERR_HTTP_UNRESOLVED_DNS = -6,      // 域名解析失败
    QCLOUD_ERR_HTTP_PARSE = -7,      // URL解析失败
    QCLOUD_ERR_HTTP_CONN = -8,      // HTTP连接失败
    QCLOUD_ERR_HTTP_AUTH = -9,      // HTTP鉴权问题
    QCLOUD_ERR_HTTP_NOT_FOUND = -10,     // HTTP 404
    QCLOUD_ERR_HTTP_TIMEOUT = -11,     // HTTP 超时
    QCLOUD_ERR_SUCCESS = 0,       // 表示成功返回
    QCLOUD_ERR_FAILURE = -1001,   // 表示失败返回
    QCLOUD_ERR_INVAL = -1002,   // 表示参数无效错误
    QCLOUD_ERR_DEV_INFO = -1003,   // 获取设备信息失败
    QCLOUD_ERR_MQTT_PUSH_TO_LIST_FAILED = -102,    // 表示往等待 ACK 列表中添加元素失败
    QCLOUD_ERR_MQTT_NO_CONN = -103,    // 表示未与MQTT服务器建立连接或已经断开连接
    QCLOUD_ERR_MQTT_UNKNOWN = -104,    // 表示MQTT相关的未知错误
    QCLOUD_ERR_MQTT_ATTEMPTING_RECONNECT = -105,    // 表示正在与MQTT服务重新建立连接
    QCLOUD_ERR_MQTT_RECONNECT_TIMEOUT = -106,    // 表示重连已经超时
    QCLOUD_ERR_MQTT_MAX_SUBSCRIPTIONS = -107,    // 表示超过可订阅的主题数
    QCLOUD_ERR_MQTT_SUB = -108,    // 表示订阅主题失败, 即服务器拒绝
    QCLOUD_ERR_MQTT_NOTHING_TO_READ = -109,    // 表示无MQTT相关报文可以读取
    QCLOUD_ERR_MQTT_PACKET_READ = -110,    // 表示读取的MQTT报文有问题
    QCLOUD_ERR_MQTT_REQUEST_TIMEOUT = -111,    // 表示MQTT相关操作请求超时
    QCLOUD_ERR_MQTT_CONNACK_UNKNOWN = -112,    // 表示客户端MQTT连接未知错误
    QCLOUD_ERR_MQTT_CONANCK_UNACCEPTABLE_PROTOCOL_VERSION = -113,    // 表示客户端MQTT版本错误
    QCLOUD_ERR_MQTT_CONNACK_IDENTIFIER_REJECTED = -114,    // 表示客户端标识符错误
    QCLOUD_ERR_MQTT_CONNACK_SERVER_UNAVAILABLE = -115,    // 表示服务器不可用
    QCLOUD_ERR_MQTT_CONNACK_BAD_USERDATA = -116,    // 表示客户端连接参数中的username或password错误
    QCLOUD_ERR_MQTT_CONNACK_NOT_AUTHORIZED = -117,    // 表示客户端连接认证失败
    QCLOUD_ERR_RX_MESSAGE_INVAL = -118,    // 表示收到的消息无效
    QCLOUD_ERR_BUF_TOO_SHORT = -119,    // 表示消息接收缓冲区的长度小于消息的长度
    QCLOUD_ERR_MQTT_QOS_NOT_SUPPORT = -120,    // 表示该QOS级别不支持
    QCLOUD_ERR_MQTT_UNSUB_FAIL = -121,    // 表示取消订阅主题失败,比如该主题不存在
    QCLOUD_ERR_JSON_PARSE = -132,    // 表示JSON解析错误
    QCLOUD_ERR_JSON_BUFFER_TRUNCATED = -133,    // 表示JSON文档会被截断
    QCLOUD_ERR_JSON_BUFFER_TOO_SMALL = -134,    // 表示存储JSON文档的缓冲区太小
    QCLOUD_ERR_JSON = -135,    // 表示JSON文档生成错误
    QCLOUD_ERR_MAX_JSON_TOKEN = -136,    // 表示超过JSON文档中的最大Token数
    QCLOUD_ERR_MAX_APPENDING_REQUEST = -137,    // 表示超过同时最大的文档请求
    QCLOUD_ERR_MAX_TOPIC_LENGTH = -138,    // 表示超过规定最大的topic长度
    QCLOUD_ERR_COAP_CONNRESET = -150,    // COAP参数错误
    QCLOUD_ERR_COAP_NULL = -151,    // 空指针
    QCLOUD_ERR_COAP_INVALID_LENGTH = -152,    // COAP参数长度错误
    QCLOUD_ERR_COAP_DATA_SIZE = -153,    // COAP数据大小超出限制
    QCLOUD_ERR_COAP_NOT_FOUND = -154,    // COAP查找失败
    QCLOUD_ERR_COAP_NET_INIT_FAILED = -155,    // COAP初始化失败
    QCLOUD_ERR_COAP_INTERNAL = -156,    // COAP内部错误
    QCLOUD_ERR_COAP_WRITE_FAILED = -157,    // COAP写数据失败
    QCLOUD_ERR_COAP_READ_FAILED = -158,    // COAP读数据失败
    QCLOUD_ERR_COAP_BADMSG = -159,    // COAPbad消息
    QCLOUD_ERR_COAP_TIMEOUT = -160,    // COAP超时错误
    QCLOUD_ERR_DTLS_PEER_CLOSE_NOTIFY = -161,    // 表示DTLS通道被关闭
    QCLOUD_ERR_SHADOW_PROPERTY_EXIST = -201,    // 表示注册的属性已经存在
    QCLOUD_ERR_SHADOW_NOT_PROPERTY_EXIST = -202,    // 表示注册的属性不存在
    QCLOUD_ERR_SHADOW_UPDATE_TIMEOUT = -203,    // 表示更新设备影子文档超时
    QCLOUD_ERR_SHADOW_UPDATE_REJECTED = -204,    // 表示更新设备影子文档被拒绝
    QCLOUD_ERR_SHADOW_GET_TIMEOUT = -205,    // 表示拉取设备影子文档超时
    QCLOUD_ERR_SHADOW_GET_REJECTED = -206,    // 表示拉取设备影子文档被拒绝
    QCLOUD_ERR_GATEWAY_CREATE_SESSION_FAIL = -221,    //创建子设备session失败
    QCLOUD_ERR_GATEWAY_SESSION_NO_EXIST = -222,    //子设备session不存在
    QCLOUD_ERR_GATEWAY_SESSION_TIMEOUT = -223,    //子设备session超时
    QCLOUD_ERR_GATEWAY_SUBDEV_ONLINE = -224,    //子设备已在线
    QCLOUD_ERR_GATEWAY_SUBDEV_OFFLINE = -225,    //子设备已不在线
    QCLOUD_ERR_TCP_SOCKET_FAILED = -601,    // 表示TCP连接建立套接字失败
    QCLOUD_ERR_TCP_UNKNOWN_HOST = -602,    // 表示无法通过主机名获取IP地址
    QCLOUD_ERR_TCP_CONNECT = -603,    // 表示建立TCP连接失败
    QCLOUD_ERR_TCP_READ_TIMEOUT = -604,    // 表示TCP读超时
    QCLOUD_ERR_TCP_WRITE_TIMEOUT = -605,    // 表示TCP写超时
    QCLOUD_ERR_TCP_READ_FAIL = -606,    // 表示TCP读错误
    QCLOUD_ERR_TCP_WRITE_FAIL = -607,    // 表示TCP写错误
    QCLOUD_ERR_TCP_PEER_SHUTDOWN = -608,    // 表示TCP对端关闭了连接
    QCLOUD_ERR_TCP_NOTHING_TO_READ = -609,    // 表示底层没有数据可以读取
    QCLOUD_ERR_SSL_INIT = -701,    // 表示SSL初始化失败
    QCLOUD_ERR_SSL_CERT = -702,    // 表示SSL证书相关问题
    QCLOUD_ERR_SSL_CONNECT = -703,    // 表示SSL连接失败
    QCLOUD_ERR_SSL_CONNECT_TIMEOUT = -704,    // 表示SSL连接超时
    QCLOUD_ERR_SSL_WRITE_TIMEOUT = -705,    // 表示SSL写超时
    QCLOUD_ERR_SSL_WRITE = -706,    // 表示SSL写错误
    QCLOUD_ERR_SSL_READ_TIMEOUT = -707,    // 表示SSL读超时
    QCLOUD_ERR_SSL_READ = -708,    // 表示SSL读错误
    QCLOUD_ERR_SSL_NOTHING_TO_READ = -709,    // 表示底层没有数据可以读取
} IoT_Error_Code;
// ================>
#define _IN_            /* 表明这是一个输入参数. */
#define _OU_            /* 表明这是一个输出参数. */
#define IOT_TRUE    (1)     /* indicate boolean value true */
#define IOT_FALSE   (0)     /* indicate boolean value false */
int HAL_Snprintf(_IN_ char *str, const int len, const char *fmt, ...);
void HAL_Printf(_IN_ const char *fmt, ...);
// ================>
struct Timer {
//#if defined(__linux__) && defined(__GLIBC__)
    struct timeval end_time;
//#else
    //  uintptr_t end_time;
//#endif
};
typedef struct Timer Timer;
bool HAL_Timer_expired(Timer *timer);
void HAL_Timer_countdown_ms(Timer *timer, unsigned int timeout_ms);
void HAL_Timer_countdown(Timer *timer, unsigned int timeout);
int HAL_Timer_remain(Timer *timer);
void HAL_Timer_init(Timer *timer);
// ================>
void *HAL_Malloc(_IN_ uint32_t size);
void HAL_Free(_IN_ void *ptr);

// ================>

#ifndef AUTH_WITH_NOTLS
/**
 * @brief TLS连接相关参数定义
 *
 * 在初始化时, 必须要将ca证书、客户端证书、客户端私钥文件及服务器域名带上来
 */
typedef struct {
    const char		 *ca_crt;
    uint16_t 		 ca_crt_len;

#ifdef AUTH_MODE_CERT
    /**
	 * 非对称加密
	 */
    const char       *cert_file;            // 客户端证书
    const char       *key_file;             // 客户端私钥
#else
    /**
     * 对称加密
     */
    const char       *psk;                  // 对称加密密钥
    const char       *psk_id;               // psk密钥ID
#endif

    size_t           psk_length;            // psk长度

    unsigned int     timeout_ms;            // SSL握手超时时间

} SSLConnectParams;

/********** TLS network **********/
typedef SSLConnectParams TLSConnectParams;

/**
 * @brief 为MQTT客户端建立TLS连接
 *
 * 主要步骤如下:
 *     1. 初始化工作, 例如mbedtls库初始化, 相关证书文件加载等
 *     2. 建立TCP socket连接
 *     3. 建立SSL连接, 包括握手, 服务器证书检查等
 *
 * @param   pConnectParams TLS连接初始化参数
 * @host    连接域名
 * @port    连接端口
 * @return  返回0 表示TLS连接成功
 */
uintptr_t HAL_TLS_Connect(TLSConnectParams *pConnectParams, const char *host, int port);

/**
 * @brief 断开TLS连接, 并释放相关对象资源
 *
 * @param pParams TLS连接参数
 */
void HAL_TLS_Disconnect(uintptr_t handle);

/**
 * @brief 通过TLS连接写数据
 *
 * @param handle        TLS连接相关数据结构
 * @param data          写入数据
 * @param totalLen      写入数据长度
 * @param timeout_ms    超时时间, 单位:ms
 * @param written_len   已写入数据长度
 * @return              若写数据成功, 则返回写入数据的长度
 */
int HAL_TLS_Write(uintptr_t handle, unsigned char *data, size_t totalLen, uint32_t timeout_ms,
                  size_t *written_len);

/**
 * @brief 通过TLS连接读数据
 *
 * @param handle        TLS连接相关数据结构
 * @param data          读取数据
 * @param totalLen      读取数据的长度
 * @param timeout_ms    超时时间, 单位:ms
 * @param read_len      已读取数据的长度
 * @return              若读数据成功, 则返回读取数据的长度
 */
int HAL_TLS_Read(uintptr_t handle, unsigned char *data, size_t totalLen, uint32_t timeout_ms,
                 size_t *read_len);

/********** TCP network **********/
/**
 * @brief 为MQTT客户端建立TCP连接
 *
 * @host	连接域名
 * @port	连接端口
 * @return	返回0 表示TCP连接失败；返回 > 0 表示TCP连接描述符FD值
 */
uintptr_t HAL_TCP_Connect(const char *host, uint16_t port);

/**
 * @brief 断开TCP连接
 *
 * @param fd TCP Socket描述符
 * @return	返回0 表示TCP断连成功
 */
int HAL_TCP_Disconnect(uintptr_t fd);

/**
 * @brief 通过TCP Socket写数据
 *
 * @param fd				TCP Socket描述符
 * @param buf				写入数据
 * @param len				写入数据长度
 * @param timeout_ms		超时时间
 * @param written_len		已写入数据长度
 * @return					若写数据成功, 则返回写入数据的长度
 */
int HAL_TCP_Write(uintptr_t fd, const unsigned char *buf, uint32_t len, uint32_t timeout_ms,
                  size_t *written_len);

/**
 * @brief 通过TCP Socket读数据
 *
 * @param fd				TCP Socket描述符
 * @param buf				读入数据
 * @param len				读入数据长度
 * @param timeout_ms		超时时间
 * @param written_len		已读入数据长度
 * @return					若读数据成功, 则返回读入数据的长度
 */
int HAL_TCP_Read(uintptr_t fd, unsigned char *buf, uint32_t len, uint32_t timeout_ms,
                 size_t *read_len);



/********** DTLS network **********/
#ifdef COAP_COMM_ENABLED
typedef SSLConnectParams DTLSConnectParams;

/**
 * @brief 为CoAP客户端建立DTLS连接
 *
 * 主要步骤如下:
 *     1. 初始化工作, 例如mbedtls库初始化, 相关证书文件加载等
 *     2. 建立UDP socket连接
 *     3. 建立SSL连接, 包括握手, 服务器证书检查等
 *
 * @param pConnectParams DTLS连接初始化参数
 * @host    连接域名
 * @port    连接端口
 * @return  返回0 表示DTLS连接成功
 */
uintptr_t HAL_DTLS_Connect(DTLSConnectParams *pConnectParams, const char *host, int port);

/**
 * @brief 断开DTLS连接
 *
 * @param handle DTLS连接相关数据结构
 * @return  返回0 表示DTLS断连
 */
void HAL_DTLS_Disconnect(uintptr_t handle);

/**
 * @brief 通过DTLS连接写数据
 *
 * @param pParams           DTLS连接相关数据结构
 * @param data              写入数据
 * @param datalen           写入数据长度
 * @param written_len       已写入数据长度
 * @return                  若写数据成功, 则返回写入数据的长度
 */
int HAL_DTLS_Write(uintptr_t handle, const unsigned char *data, size_t datalen, size_t *written_len);

/**
 * @brief 通过DTLS连接读数据
 *
 * @param handle            DTLS连接相关数据结构
 * @param data              读取数据
 * @param timeout_ms        超时时间, 单位:ms
 * @param datalen   	    读取数据的长度
 * @param read_len          已读取数据的长度
 * @return                  若读数据成功, 则返回读取数据的长度
 */
int HAL_DTLS_Read(uintptr_t handle, unsigned char *data, size_t datalen, uint32_t timeout_ms,
                  size_t *read_len);

#endif //CoAP Enabled

#else
/********** TCP network **********/
/**
 * @brief 为MQTT客户端建立TCP连接
 *
 * @host    连接域名
 * @port    连接端口
 * @return  返回0 表示TCP连接失败；返回 > 0 表示TCP连接描述符FD值
 */
uintptr_t HAL_TCP_Connect(const char *host, uint16_t port);

/**
 * @brief 断开TCP连接
 *
 * @param fd TCP Socket描述符
 * @return  返回0 表示TCP断连成功
 */
int HAL_TCP_Disconnect(uintptr_t fd);

/**
 * @brief 通过TCP Socket写数据
 *
 * @param fd           		TCP Socket描述符
 * @param buf              	写入数据
 * @param len           	写入数据长度
 * @param timeout_ms		超时时间
 * @param written_len       已写入数据长度
 * @return                  若写数据成功, 则返回写入数据的长度
 */
int HAL_TCP_Write(uintptr_t fd, const unsigned char *buf, uint32_t len, uint32_t timeout_ms,
                size_t *written_len);

/**
 * @brief 通过TCP Socket读数据
 *
 * @param fd           		TCP Socket描述符
 * @param buf              	读入数据
 * @param len           	读入数据长度
 * @param timeout_ms		超时时间
 * @param written_len       已读入数据长度
 * @return                  若读数据成功, 则返回读入数据的长度
 */
int HAL_TCP_Read(uintptr_t fd, unsigned char *buf, uint32_t len, uint32_t timeout_ms,
                size_t *read_len);

/********** UDP network **********/
#ifdef COAP_COMM_ENABLED
/**
 * @brief 建立UDP连接
 *
 * @host    连接域名
 * @port    连接端口
 * @return  返回0 表示UDP连接失败；返回 > 0 表示UDP连接描述符FD值
 */
uintptr_t HAL_UDP_Connect(const char *host, unsigned short port);

/**
 * @brief 断开UDP连接
 *
 * @param fd UDP Socket描述符
 * @return
 */
void HAL_UDP_Disconnect(uintptr_t fd);

/**
 * @brief 通过UDP Socket写数据
 *
 * @param fd           		UDP Socket描述符
 * @param buf              	写入数据
 * @param len           	写入数据长度
 * @return                  若写数据成功, 则返回写入数据的长度
 */
int HAL_UDP_Write(uintptr_t fd, const unsigned char *p_data, unsigned int datalen);

/**
 * @brief 通过TCP Socket读数据
 *
 * @param fd           		UDP Socket描述符
 * @param buf              	读入数据
 * @param len           	读入数据长度
 * @return                  若读数据成功, 则返回读入数据的长度
 */
int HAL_UDP_Read(uintptr_t fd, unsigned char *p_data, unsigned int datalen);

/**
 * @brief 通过TCP Socket读数据
 *
 * @param fd           		UDP Socket描述符
 * @param buf              	读入数据
 * @param len           	读入数据长度
 * @param timeout_ms		超时时间
 * @return                  若读数据成功, 则返回读入数据的长度
 */
int HAL_UDP_ReadTimeout(uintptr_t fd, unsigned char *p_data, unsigned int datalen, unsigned int timeout_ms);
#endif
#endif //NOTLS Enabled
#endif