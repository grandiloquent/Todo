#include "utils_string.h"
#include "log.h"
#include <ctype.h>
#include <malloc.h>
#include <memory.h>
#define isnum(x) ((x) >= '0' && (x) <= '9')

int indexof(const char* s, const char* find)
{
    char c, sc;
    size_t len;
    int count = 0;

    if ((c = *find++) != 0)
    {
        len = strlen(find);
        do
        {
            do
            {
                if ((sc = *s++) == 0)
                    return -1;
                count++;
            } while (sc != c);
        } while (strncmp(s, find, len) != 0);
        count--;
    }
    return count;
}

char* toggle_list(const char* str)
{
    const char* prefix = "- ";

    char* out = malloc(strlen(str) * 2);
    char* tmp = out;
    char* buf = strdup(str);
    strcpy(buf, str);
    char* tok = strtok(buf, "\n");
    while (tok)
    {
        int add = strncmp(tok, prefix, strlen(prefix)) != 0;
        if (add)
        {
            *tmp++ = '-';
            *tmp++ = ' ';
        }
        else
        {
            tok += strlen(prefix);
        }

        // trim
        while (*tok && isspace(*tok))
            tok++;
        size_t tok_len = strlen(tok);
        while (tok_len > 0)
        {
            tok_len--;
            if (isspace(*(tok + tok_len)))
                *(tok + tok_len) = 0;
            else
                break;
        }
        // trim
        
        while (*tok)
        {

            *tmp++ = *tok;
            tok++;
        }
        *tmp++ = '\n';

        tok = strtok(0, "\n");
    }
    *tmp = 0;

    free(buf);
    return out;
}
bool iswhitespace(const char* s)
{
    if (!s)
        return true;
    while (*s)
        if (!isspace(*s++))
            return false;

    return true;
}

char* remove_redundancy(const char* s)
{
    size_t len = strlen(s);
    char* r = malloc(len + 1);
    memset(r, 0, len + 1);
    char* t = r;

    while (*s)
    {
        if (*s == '\n')
        {
            *t++ = '\n';
            char ch = 0;
            while (*++s && isspace(*s))
            {

                if (ch == 0 && *s == '\n')
                {

                    ch = '\n';
                }
            }

            if (ch != 0)
                *t++ = '\n';
            continue;
        }

        *t++ = *s++;
    }
    return r;
}
int is_number_list(const char* str)
{

    const char* tmp = str;
    int r = 0;

    while (*tmp)
    {
        if (!isnum(*tmp))
        {
            if (*tmp == '.' && *tmp != *str)
                r = 1;
            break;
        }
        tmp++;
    }
    if (r)
        return *tmp++ && *tmp == ' ';
    return r;
}

char* toggle_number_list(const char* str)
{
    const char* prefix = "- ";

    char* out = malloc(strlen(str) * 2);
    char* tmp = out;
    char* buf = strdup(str);
    strcpy(buf, str);
    char* tok = strtok(buf, "\n");
    size_t count = 0;
    while (tok)
    {
        if (iswhitespace(tok))
            continue;
        count++;

        int add = !is_number_list(tok);

        if (add)
        {
            char num_buf[10];
            memset(num_buf, 0, 10);
            snprintf(num_buf, 10, "%d. ", count);
            size_t i = 0;
            while (i < 10 && num_buf[i] != 0)
            {
                *tmp++ = num_buf[i++];
            }
        }
        else
        {
            while (*tok && *tok++ != ' ')
            {
            }
        }
        while (*tok && isspace(*tok))
            tok++;
        size_t tok_len = strlen(tok);
        while (tok_len > 0)
        {
            tok_len--;
            if (isspace(*(tok + tok_len)))
                *(tok + tok_len) = 0;
            else
                break;
        }
        while (*tok)
        {

            *tmp++ = *tok;
            tok++;
        }
        *tmp++ = '\n';

        tok = strtok(0, "\n");
    }
    *tmp = 0;
    printf("%s\n", out);

    free(buf);
    return out;
}
