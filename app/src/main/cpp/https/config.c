#include "config.h"
#include <stdio.h>
#include <stdlib.h>
#include <stdarg.h>
#include <memory.h>
#include <pthread.h>
#include <semaphore.h>
#include <errno.h>
#include <time.h>
#include <sys/time.h>
#include <sys/types.h>
#include <unistd.h>
static char now_time_str[20] = {0};
char *HAL_Timer_current(void) {
    struct timeval tv;
    gettimeofday(&tv, NULL);
    time_t now_time = tv.tv_sec;
    struct tm tm_tmp = *localtime(&now_time);
    strftime(now_time_str, 20, "%F %T", &tm_tmp);
    return now_time_str;
}
void HAL_Printf(_IN_ const char *fmt, ...) {
    va_list args;
    va_start(args, fmt);
    vprintf(fmt, args);
    va_end(args);
    fflush(stdout);
}
int HAL_Snprintf(_IN_ char *str, const int len, const char *fmt, ...) {
    va_list args;
    int rc;
    va_start(args, fmt);
    rc = vsnprintf(str, len, fmt, args);
    va_end(args);
    return rc;
}
void HAL_Timer_countdown(Timer *timer, unsigned int timeout) {
    struct timeval now;
    gettimeofday(&now, NULL);
    struct timeval interval = {timeout, 0};
    timeradd(&now, &interval, &timer->end_time);
}
void HAL_Timer_countdown_ms(Timer *timer, unsigned int timeout_ms) {
    struct timeval now;
    gettimeofday(&now, NULL);
    struct timeval interval = {timeout_ms / 1000, (timeout_ms % 1000) * 1000};
    timeradd(&now, &interval, &timer->end_time);
}
long HAL_Timer_current_sec(void) {
    struct timeval tv;
    gettimeofday(&tv, NULL);
    return tv.tv_sec;
}
bool HAL_Timer_expired(Timer *timer) {
    struct timeval now, res;
    gettimeofday(&now, NULL);
    timersub(&timer->end_time, &now, &res);
    return res.tv_sec < 0 || (res.tv_sec == 0 && res.tv_usec <= 0);
}
void HAL_Timer_init(Timer *timer) {
    timer->end_time = (struct timeval) {0, 0};
}
int HAL_Timer_remain(Timer *timer) {
    struct timeval now, res;
    gettimeofday(&now, NULL);
    timersub(&timer->end_time, &now, &res);
    return (res.tv_sec < 0) ? 0 : res.tv_sec * 1000 + res.tv_usec / 1000;
}
void
Log_writter(const char *file, const char *func, const int line, const int level, const char *fmt,
            ...) {
}