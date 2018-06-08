#ifndef __TCOMDEF_H__
#define __TCOMDEF_H__
 

typedef long					TLong;
typedef float					TFloat;
typedef double					TDouble;
typedef unsigned char			TByte;
typedef unsigned short			TWord;
typedef unsigned long			TDWord;
typedef void*					THandle;
typedef signed char				TChar;
typedef long					TBool;
typedef void					TVoid;
typedef void*					TPVoid;
typedef char*					TPChar;
typedef short					TShort;
typedef const char*				TPCChar;
typedef	 TLong					TRESULT;
typedef TDWord					TCOLORREF; 

typedef	 signed		char		TInt8;
typedef	 unsigned	char		TUInt8;
typedef	 signed		short		TInt16;
typedef	 unsigned	short		TUInt16;
typedef signed		long		TInt32;
typedef unsigned	long		TUInt32;

#if defined(_MSC_VER)
typedef signed		__int64		TInt64;
typedef unsigned	__int64		TUInt64;
#else
typedef signed		long long	TInt64;
typedef unsigned	long long	TUInt64;
#endif

typedef struct __tag_rect
{
	TLong left;
	TLong top;
	TLong right;
	TLong bottom;
} TRECT, *PTRECT;

typedef struct __tag_point
{ 
	TLong x; 
	TLong y; 
} TPOINT, *PTPOINT;

#define TNull		0
#define TFalse		0
#define TTrue		1

#define TMAX(x,y) (((x)>=(y))?(x):(y))
#define TMIN(x,y) (((x)<=(y))?(x):(y))

#ifndef TABS
#define TABS(x) (((x)+((x)>>31))^((x)>>31))
#endif

#define TTChar TChar

#define TRIMBYTE(x)	(TUInt8)((x)&(~255)?((-(x))>>31):(x))

typedef TRESULT (*TFNPROGRESS)(TLong lPercent, TVoid* pUserData);

#endif
