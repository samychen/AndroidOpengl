/**
 * Copyright (C) 2013 Thundersoft Corporation
 * All rights Reserved
 */

#include "TSAlgorithm.h"

namespace tslib
{

template <class T>
T min(T v1, T v2)
{
    return v1 > v2 ? v2 : v1;
}

template <class T>
T max(T v1, T v2)
{
    return v1 > v2 ? v1 : v2;
}

template <class T>
T* max_element(T* pFirst, T* pLast)
{
    if(pFirst == pLast) return pLast;
    T* biggest = pFirst;
    while( ++pFirst != pLast) {
        if(*biggest < *pFirst) {
            biggest = pFirst;
        }
    }
    return pFirst;
}


template int min<int>(int,int);
template int max<int>(int,int);
template unsigned char* max_element<unsigned char>(unsigned char*,unsigned char*);
}
