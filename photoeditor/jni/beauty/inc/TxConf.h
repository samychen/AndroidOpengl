/*
** TxConf.h
*/

#ifndef _TX_CONF_H_
#define _TX_CONF_H_

#if defined (WIN32) || defined(_WINDOWS)
#include <SDKDDKVer.h>
#define WIN32_LEAN_AND_MEAN             // Exclude rarely-used stuff from Windows headers
// Windows Header Files:
#include <windows.h>
#endif /* WIN32 || _WINDOWS */

#if defined (TXK_IMPORTS) && defined (TXK_EXPORTS)
#  error "Import and export declarations are not valid"
#else
#  if defined (TXK_EXPORTS)
#    define TXK_API __declspec(dllexport)
#  elif defined (TXK_IMPORTS)
#    define TXK_API __declspec(dllimport)
#  else
#    define TXK_API
#  endif
#endif

//#ifndef inline
//#define inline _inline
//#endif /* inline */

#if defined (_WIN32) || defined(_WINDOWS)
    #include <stdio.h>
    #include <stdlib.h> 
    #define  LOGI  printf
    #define  LOGE  printf
#elif defined (__ANDROID__)
    //Android
    #include <android/log.h>
    #include <stdio.h>
    #include <stdlib.h> 
    #define LOG_TAG "faceclean"
    #define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
    #define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#endif

#define TXC_LOG_ENABLE      0

#endif /* _TX_CONF_H_ */
