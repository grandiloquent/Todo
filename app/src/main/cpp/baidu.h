#ifndef __BAIDU_H__
#define __BAIDU_H__

#include "rapidstring.h"

int baidu_query_dictionary(const char *q, rapidstring *s,
                           const char *to);


#endif