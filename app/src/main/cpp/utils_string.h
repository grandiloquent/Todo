#ifndef UTILS_STRING_H__
#define  UTILS_STRING_H__

#include <stdbool.h>

bool iswhitespace(const char *s);

char *remove_redundancy(const char *s);

int indexof(const char *s, const char *find);

char* toggle_list(const char* str);
char* toggle_number_list(const char* str);

#endif