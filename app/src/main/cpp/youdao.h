#ifndef __YOUDAO_H__
#define __YOUDAO_H__

#include "rapidstring.h"

int youdao_query_dictionary(const char *q, rapidstring *s, int translate, const char *from,
                            const char *to);


#endif