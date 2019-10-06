#include <ctype.h> /* for isalnum() */
#include <malloc.h>
#include <memory.h>

static char to_hex(char code)
{
    static const char hex[] = "0123456789abcdef";
    return hex[code & 15];
}
static char from_hex(char ch)
{
    return isdigit(ch) ? ch - '0' : tolower(ch) - 'a' + 10;
}
/* Note -- caller must free returned pointer! */
char* url_encode(const char* str)
{
    const char* pstr = str;
    char* buf = malloc(strlen(str) * 3 + 1);
    char* pbuf = buf;

    if (!buf)
    {
        return NULL;
    }

    while (*pstr)
    {
        if (isalnum(*pstr) || *pstr == '-' || *pstr == '_' || *pstr == '.' || *pstr == '~')
            *pbuf++ = *pstr;
        else if (*pstr == ' ')
            *pbuf++ = '+';
        else
            *pbuf++ = '%', *pbuf++ = to_hex(*pstr >> 4), *pbuf++ = to_hex(*pstr & 15);
        pstr++;
    }
    *pbuf = '\0';
    return buf;
}
char* url_decode(char* str)
{
    char *pstr = str, *buf = malloc(strlen(str) + 1), *pbuf = buf;

    if (!buf)
    {
        return NULL;
    }

    while (*pstr)
    {
        if (*pstr == '%')
        {
            if (pstr[1] && pstr[2])
            {
                *pbuf++ = from_hex(pstr[1]) << 4 | from_hex(pstr[2]);
                pstr += 2;
            }
        }
        else if (*pstr == '+')
        {
            *pbuf++ = ' ';
        }
        else
        {
            *pbuf++ = *pstr;
        }
        pstr++;
    }
    *pbuf = '\0';
    return buf;
}
