#ifndef BEAUTI_TUNE_H
#define BEAUTI_TUNE_H

#include "tcomdef.h"
#include "terror.h"
#include "tsoffscreen.h"

#if defined(__GNUC__) || defined(DARWIN) || defined(__MACH__) || defined(__APPLE__)
#  ifdef BT_DLL_EXPORT
#    define BEAUTI_TUNE_EXPORT __attribute__((visibility("default")))
#  else
#    define BEAUTI_TUNE_EXPORT
#  endif
#elif defined(_WINDOWS) || defined(WIN32)
#  ifdef BT_DLL_EXPORT
#    define BEAUTI_TUNE_EXPORT __declspec(dllexport)
#  else
#    define BEAUTI_TUNE_EXPORT
#  endif
#else
#  error "The compiler is not supported."
#endif

typedef enum BTType { TeethWhite, Smooth, Detail,Reshape,Tones } TuneType;
typedef enum BSWork {Paint,Erase} BrushWork;
typedef struct _tag_fpoint_bttune
{
	TFloat x;
	TFloat y;
} FPOINT;

typedef struct _process_para
{
	//for Smooth
	TInt32    IsMoreSmooth;
	BrushWork BsWork;
	//for Reshape
	FPOINT    ShapeStart, ShapeEnd;
	TFloat    ShapeScale;
	TInt32    IsFine;
	//for tones
	TFloat    fRGB[3];
}TypePara;
#ifdef _cplusplus
extern "C"{
#endif

/*  Initialize BeautiTune Engine
    bEngine  : Engine handle
	Width    : Image Width
	Height   : Image Height
	Type     : Effect type, one of "TuneType"
*/
BEAUTI_TUNE_EXPORT TRESULT BeautiTune_Init   ( THandle *bEngine, TInt32 Width   , TInt32 Height  ,TuneType Type );

/*  Image preprocess
    bEngine  : Engine handle
	SrcTexID : Source texture ID
*/
BEAUTI_TUNE_EXPORT TRESULT BeautiTune_PreProcess(THandle  bEngine, TInt32 SrcTexID);

/*  Effect process
    bEngine    :  Engine handle
	SrcTexID   :  Source texture ID
	DstTexID   :  Output texture ID
	center     :  Brush  point center
	Radius     :  Brush radius (>0.0f)
	Para       :  Brush or effects parameters
	DstBuf     :  Output buffer point for debug
*/
BEAUTI_TUNE_EXPORT TRESULT BeautiTune_Process( THandle  bEngine, TInt32 SrcTexID, TInt32 DstTexID, FPOINT * center, TFloat Radius, TypePara *Para,TByte* DstBuf=NULL);


/*  Release the engine handle
    bEngine   :  Engine handle
*/
BEAUTI_TUNE_EXPORT TVoid   BeautiTune_UnInit ( THandle  bEngine );

#ifdef _cplusplus
		  }
#endif

#endif