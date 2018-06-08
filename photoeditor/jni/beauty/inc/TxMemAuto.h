/*
** TxMemAuto.h
*/

#ifndef _TX_MEM_AUTO_H_
#define _TX_MEM_AUTO_H_

#include "TxKitBase.h"

#ifdef __cplusplus

class TxMemAuto
{
public:
    TxMemAuto();
    ~TxMemAuto();
private:
    TxMemAuto(const TxMemAuto&);
    TxMemAuto& operator = (const TxMemAuto&);
};

#endif // __cplusplus

#ifdef __cplusplus
extern "C" {;
#endif

Void* TxGlobalSmallAllocEx(const Uint32 size);

Bool TxGlobalSmallFreeEx(const Void* ptr);

#ifdef __cplusplus
};
#endif

#endif /* _TX_MEM_AUTO_H_ */
