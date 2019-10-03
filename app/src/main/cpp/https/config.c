
void
Log_writter(const char *file, const char *func, const int line, const int level, const char *fmt,
            ...);

#define Log_e(fmt, ...) Log_writter(__FILE__, __FUNCTION__, __LINE__, LOG_ERROR, fmt, ##__VA_ARGS__)
