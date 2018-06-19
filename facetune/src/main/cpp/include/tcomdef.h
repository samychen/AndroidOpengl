#ifndef __TCOMDEF_H__
#define __TCOMDEF_H__

#include <stdint.h>
 
typedef long                    TLong;
typedef float                   TFloat;
typedef double                  TDouble;
typedef uint8_t                 TByte;
typedef uint16_t                TWord;
typedef uint32_t                TDWord;
typedef void*                   THandle;
typedef signed char             TChar;
typedef int32_t                 TBool;
typedef void                    TVoid;
typedef void*                   TPVoid;
typedef char*                   TPChar;
typedef short                   TShort;
typedef const char*             TPCChar;
typedef int32_t                 TRESULT;
typedef TDWord                  TCOLORREF; 

typedef int8_t     TInt8;
typedef uint8_t    TUInt8;
typedef int16_t    TInt16;
typedef uint16_t   TUInt16;
typedef int32_t    TInt32;
typedef uint32_t   TUInt32;
typedef int64_t    TInt64;
typedef uint64_t   TUInt64;

typedef struct
{
    TInt32 left;
    TInt32 top;
    TInt32 right;
    TInt32 bottom;
} TRECT, *PTRECT;

typedef struct
{ 
    TInt32 x; 
    TInt32 y; 
} TPOINT, *PTPOINT;

/// Euler Angle
typedef struct {
    float pitch;
    float yaw;
    float roll;
} TSEulerAngle;

#define TNull       0
#define TFalse      0
#define TTrue       1

#define TMAX(x,y) (((x)>=(y))?(x):(y))
#define TMIN(x,y) (((x)<=(y))?(x):(y))

#define TTChar TChar

#endif
