#ifndef __UTILS_H__
#define __UTILS_H__


#define alloc_nr(x) (((x)+16)*3/2)

/*
 * Realloc the buffer pointed at by variable 'x' so that it can hold
 * at least 'nr' entries; the number of entries currently allocated
 * is 'alloc', using the standard growing factor alloc_nr() macro.
 *
 * DO NOT USE any expression with side-effect for 'x' or 'alloc'.
 */
#define ALLOC_GROW(x, nr, alloc) \
    do { \
        if ((nr) > alloc) { \
            if (alloc_nr(alloc) < (nr)) \
                alloc = (nr); \
            else \
                alloc = alloc_nr(alloc); \
            x = realloc((x), alloc * sizeof(*(x))); \
        } \
    } while(0)

#endif

