#include "utils_string.h"
#include "log.h"
#include <ctype.h>
#include <malloc.h>
#include <memory.h>


bool iswhitespace(const char *s) {
    if (!s)
        return true;
    while (*s)
        if (!isspace(*s++))
            return false;

    return true;
}

char *remove_redundancy(const char *s) {
    const char *text = s;
    const char *p = s;
    char *ret = malloc(strlen(s));
    memset(ret, 0, strlen(s));
    while (*text) {
        if (*text == '\n') {
            size_t len = text - p;

            char tmp[len + 1];
            memset(tmp, 0, len + 1);
            strncpy(tmp, p, len);
            if (iswhitespace(tmp)) {
                strcat(ret, "\n");
                while (isspace(*++text))
                    p = text + 1;
                continue;
            } else {
                strcat(ret, tmp);
                strcat(ret, "\n");
            }

            p = text + 1;
        }
        text++;
    }
    if (text > p) {
        char tmp[text - p + 1];
        strncpy(tmp, p, text - p);
        if (!iswhitespace(tmp)) {
            strcat(ret, tmp);
        }
        // printf("%s, %d %d\n", tmp, text, p);
    }
    return ret;
}
