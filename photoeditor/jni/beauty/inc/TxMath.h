/*
** TxMath.h
*/

#ifndef _TX_MATH_H_
#define _TX_MATH_H_

#include "TxKitBase.h"

#ifdef __cplusplus
extern "C" {;
#endif

#ifndef TXC_ZERO
#define TXC_ZERO        1.0e-12
#endif // TXC_ZERO

#ifndef TXC_PI
#define TXC_PI          3.141592653589793238
#endif // TXC_PI

#ifndef TXM_MAX
#define TXM_MAX(a, b)   (((a) > (b)) ? (a) : (b))
#endif // TXM_MAX

#ifndef TXM_MIN
#define TXM_MIN(a, b)   (((a) < (b)) ? (a) : (b))
#endif // TXM_MIN

#ifndef TXM_ABS
#define TXM_ABS(x)      (((x) >= 0) ? (x) : (-(x)))
#endif // TXM_ABS

#ifndef TXM_FLOOR
#define TXM_FLOOR(x)    (((x) >= 0) ? (Sint32)(x) : ((Sint32)(x) - 1))
#endif // TXM_FLOOR

#ifndef TXM_UPPER
#define TXM_UPPER(x)    (((x) >= 0) ? (Sint32)(x + 1) : ((Sint32)(x)))
#endif // TXM_UPPER

#ifndef TXM_ROUND
#define TXM_ROUND(x)    (Sint32)((x) + ((x) >= 0 ? 0.5 : -0.5))
#endif // TXM_ROUND

#ifndef TXM_IN_RANGE
#define TXM_IN_RANGE(x, rmin, rmax)     (((x) >= (rmin)) && ((x) <= (rmax)))
#endif // TXM_IN_RANGE

/* unreference */
#ifndef TXM_UNREF
#define TXM_UNREF(x)         (Void)(x)
#endif // TXM_UNREF

#ifdef __cplusplus
};
#endif

#endif /* _TX_MATH_H_ */
