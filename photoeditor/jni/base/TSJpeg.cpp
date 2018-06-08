/**
 * Copyright (C) 2011 Thundersoft Corporation
 * All rights Reserved
 */

#include <unistd.h>
#include <string.h>
#include <stdio.h>
#include <stdarg.h>

#include"jnilogger.h"

#include "TSJpeg.h"
extern "C"{
#include "jconfig.h"
#include "jpeglib.h"
#include "jmorecfg.h"
#include "jerror.h"
}

#include "TSDl.h"
#include "CpuABI.h"

#ifndef ZeroMemory
#define ZeroMemory(dst,size) memset((dst),0,(size))
#endif

static void ErrorExit(j_common_ptr cinfo);
static void EmitMessage(j_common_ptr cinfo,int msgLevel);
static void OutputMessage(j_common_ptr cinfo);
static void FormatMessage(j_common_ptr cinfo,char*buffer);
static void ResetErrorMgr(j_common_ptr cinfo);

static void DstManagerInit(j_compress_ptr cinfo);
static int  DstEmptyOutputBuffer(j_compress_ptr cinfo);
static void DstTermination(j_compress_ptr cinfo);


static void SrcManagerInit(j_decompress_ptr cinfo);
static int SrcFillInputBuffer(j_decompress_ptr cinfo);
static void SrcSkipInputData(j_decompress_ptr cinfo,long num_bytes);
static int SrcResyncToRestart(j_decompress_ptr cinfo,int data);
static void SrcTermination(j_decompress_ptr cinfo);

#define COMPRESS_DEFAULT_BUFFER_SIZE (1<<22)
#define DECOMPRESS_DEFAULT_BUFFER_SIZE (1<<22)
#define COMPRESS_DEFAULT_BUFFER_SIZE_MIN (1<<18)

static void initErrMgr(jpeg_error_mgr & errmgr)
{
    ZeroMemory(&errmgr,sizeof(jpeg_error_mgr));
    jpeg_std_error(&errmgr);

    errmgr.error_exit= ErrorExit;
    errmgr.emit_message= EmitMessage;
    errmgr.output_message= OutputMessage;
    errmgr.format_message= FormatMessage;
    errmgr.reset_error_mgr= ResetErrorMgr;

    return;
}
static void initSrcMgr(jpeg_source_mgr & srcMgr)
{
    ZeroMemory(&srcMgr,sizeof(jpeg_source_mgr));
    srcMgr.init_source = SrcManagerInit;
    srcMgr.fill_input_buffer = SrcFillInputBuffer;
    srcMgr.skip_input_data = SrcSkipInputData;
    srcMgr.resync_to_restart = SrcResyncToRestart;
    srcMgr.term_source = SrcTermination;
}
static void initDstMgr(jpeg_destination_mgr & dstMgr)
{
    ZeroMemory(&dstMgr,sizeof(jpeg_destination_mgr));

    dstMgr.init_destination = DstManagerInit;
    dstMgr.empty_output_buffer = DstEmptyOutputBuffer;
    dstMgr.term_destination = DstTermination;

    return;

}


static void DstManagerInit(j_compress_ptr cinfo)
{
    jpeg_compress_struct *pCompressContext = cinfo;
    jpeg_destination_mgr *pDstMnager = pCompressContext->dest;

    int nBufferSize = (int)(CTSJpeg::m_nCompressDefaultSize * 2);
    if (nBufferSize == 0)
    {
        nBufferSize = COMPRESS_DEFAULT_BUFFER_SIZE;
    }else if(nBufferSize < COMPRESS_DEFAULT_BUFFER_SIZE_MIN){
        nBufferSize = COMPRESS_DEFAULT_BUFFER_SIZE_MIN;
    }

    pDstMnager->next_output_byte = (JOCTET*)new char[nBufferSize];
    ZeroMemory(pDstMnager->next_output_byte,nBufferSize);

    pDstMnager->free_in_buffer = nBufferSize;

    TSJpegParam* pJpegParam = (TSJpegParam*)pCompressContext->client_data;
    pJpegParam->dstBuffer = (char*)pDstMnager->next_output_byte;
    pJpegParam->dstBufferSize = nBufferSize;

    return;


}
static int  DstEmptyOutputBuffer(j_compress_ptr cinfo)
{
    jpeg_compress_struct *pCompressContext = cinfo;
    jpeg_destination_mgr *pDstMnager = pCompressContext->dest;

    //LOGE("DstEmptyOutputBuffer:This cannot happend because we have allocated enough buffer");
    return TRUE;

}
static void DstTermination(j_compress_ptr cinfo)
{
    jpeg_compress_struct *pCompressContext = cinfo;
    jpeg_destination_mgr *pDstMnager = pCompressContext->dest;
    TSJpegParam *pParam = (TSJpegParam *)pCompressContext->client_data;
    pParam->dstBufferSize = (char*)pDstMnager->next_output_byte - (char*)pParam->dstBuffer;
    LOGE("Compress complete, with buffer = %d\n",pParam->dstBufferSize);


}

static void SrcManagerInit(j_decompress_ptr cinfo)
{
    jpeg_decompress_struct *pDeCompressContext = cinfo;
    jpeg_source_mgr *pSrcMnager = pDeCompressContext->src;
    TSJpegParam *pParam = (TSJpegParam*)pDeCompressContext->client_data;
    pSrcMnager->next_input_byte = (const JOCTET*)pParam->srcBuffer;
    pSrcMnager->next_input_byte = (const JOCTET*)pParam->srcBuffer;
    pSrcMnager->bytes_in_buffer = pParam->srcBufferSize;

    return;

}
static int SrcFillInputBuffer(j_decompress_ptr cinfo)
{
    return TRUE;
}
static void SrcSkipInputData(j_decompress_ptr cinfo,long num_bytes)
{
    cinfo->src->bytes_in_buffer -= num_bytes;
     cinfo->src->next_input_byte += num_bytes;
    return ;
}
static int SrcResyncToRestart(j_decompress_ptr cinfo,int data)
{
    return jpeg_resync_to_restart(cinfo,data);
}
static void SrcTermination(j_decompress_ptr cinfo)
{
    jpeg_decompress_struct *pDeCompressContext = cinfo;
    jpeg_source_mgr *pSrcMnager = pDeCompressContext->src;
    TSJpegParam *pParam = (TSJpegParam*)pDeCompressContext->client_data;
    pParam->srcBuffer = (char*)pSrcMnager->next_input_byte;
}

void SetOutputScale(jpeg_decompress_struct *pDeCompressContext, const ScaleType &scale)
{
    switch(scale)
    {
        case HALF_SCALE:
            pDeCompressContext->scale_denom = 2;
            break;
        case QUARTER_SCALE:
            pDeCompressContext->scale_denom = 4;
            break;
        case EIGHTH_SCALE:
            pDeCompressContext->scale_denom = 8;
            break;
        default:
            pDeCompressContext->scale_denom = 1;
            break;
    }
    pDeCompressContext->scale_num = 1;
}

int CTSJpeg::m_nCompressDefaultSize = 0;
int CTSJpeg::CompressMemToMem(TSJpegParam & compressParam, const int &nDefaultSize)
{
	TSDl dl;
	bool loadSucc=false;
//	if(!CpuABI::FeatrueContainsNeon())
//	{
//		loadSucc=dl.LoadDl(SYS_LIBJPEG);
//	}

    m_nCompressDefaultSize = nDefaultSize;
    jpeg_compress_struct * pComressContext = new jpeg_compress_struct;
    ZeroMemory(pComressContext,sizeof(jpeg_compress_struct));

    void (*createCompress_fun_ptr)(j_compress_ptr,int, size_t);
    if(loadSucc)
    {
    	createCompress_fun_ptr=dl.GetFuncPtr("jpeg_CreateCompress");
    	if(NULL!=createCompress_fun_ptr)
    	{
    		(*createCompress_fun_ptr)(pComressContext,JPEG_LIB_VERSION,(size_t)sizeof(struct jpeg_compress_struct));
    	}
    	else
    	{
    		jpeg_create_compress(pComressContext);
    	}
    }
    else
    {
    	jpeg_create_compress(pComressContext);
    }

    pComressContext->client_data = &compressParam;

    jpeg_destination_mgr dstmanager;
    initDstMgr(dstmanager);
    pComressContext->dest = (jpeg_destination_mgr*)&dstmanager;

    jpeg_error_mgr errmgr;
    initErrMgr(errmgr);
    pComressContext->err = &errmgr;


    pComressContext->image_width = compressParam.img_width;
    pComressContext->image_height = compressParam.img_height;
    pComressContext->input_components = compressParam.img_components;
    pComressContext->in_color_space = (J_COLOR_SPACE)compressParam.color_space;

    void (*set_defaults_fun_ptr)(j_compress_ptr);
    if(loadSucc)
    {
    	set_defaults_fun_ptr=dl.GetFuncPtr("jpeg_set_defaults");
    	if(NULL!=set_defaults_fun_ptr)
    	{
    		(*set_defaults_fun_ptr)((jpeg_compress_struct*)pComressContext);
    	}
    	else
    	{
    		jpeg_set_defaults((jpeg_compress_struct*)pComressContext);
    	}
    }
    else
    {
    	jpeg_set_defaults((jpeg_compress_struct*)pComressContext);
    }

    void (*set_quality_fun_ptr)(j_compress_ptr, int,boolean);
    if(compressParam.quality > 0)
    {
    	if(loadSucc)
    	{
    		set_quality_fun_ptr=dl.GetFuncPtr("jpeg_set_quality");
    		if(NULL!=set_quality_fun_ptr)
    		{
    			(*set_quality_fun_ptr)((jpeg_compress_struct*)pComressContext,compressParam.quality,FALSE);
    		}
    		else
    		{
    			jpeg_set_quality((jpeg_compress_struct*)pComressContext,compressParam.quality,FALSE);
    		}
    	}
    	else
    	{
    		jpeg_set_quality((jpeg_compress_struct*)pComressContext,compressParam.quality,FALSE);
    	}
    }

    void (*start_compress_fun_ptr)(j_compress_ptr,boolean);
    if(loadSucc)
    {
    	start_compress_fun_ptr=dl.GetFuncPtr("jpeg_start_compress");
    	if(NULL!=start_compress_fun_ptr)
    	{
    		(*start_compress_fun_ptr)((jpeg_compress_struct*)pComressContext,TRUE);
    	}
    	else
    	{
    		jpeg_start_compress((jpeg_compress_struct*)pComressContext,TRUE);
    	}
    }
    else
    {
    	jpeg_start_compress((jpeg_compress_struct*)pComressContext,TRUE);
    }

    JSAMPROW rowpointer[1];
    JSAMPROW imageBuffer = (JSAMPROW)compressParam.srcBuffer;

    JDIMENSION (*write_scanlines_fun_ptr)(j_compress_ptr,JSAMPARRAY,JDIMENSION);
    if(loadSucc)
    {
    	write_scanlines_fun_ptr=dl.GetFuncPtr("jpeg_write_scanlines");
    }
    else
    {
    	write_scanlines_fun_ptr=NULL;
    }
    //int row_stride = pComressContext->image_width * 3;
    //int row_stride = ((pComressContext->image_width * 3+3)>>2)<<2;
    int row_stride = compressParam.img_widthStep;
    while (pComressContext->next_scanline < pComressContext->image_height && compressParam.nErrno == 0)
    {
        rowpointer[0] = (imageBuffer + pComressContext->next_scanline*row_stride);
        if(NULL!=write_scanlines_fun_ptr)
        {
        	(*write_scanlines_fun_ptr)((jpeg_compress_struct*)pComressContext,rowpointer,1);
        }
        else
        {
        	jpeg_write_scanlines((jpeg_compress_struct*)pComressContext,rowpointer,1);
        }
    }

    void (*finish_compress_fun_ptr)(j_compress_ptr);
    if(loadSucc)
    {
    	finish_compress_fun_ptr=dl.GetFuncPtr("jpeg_finish_compress");
    	if(NULL!=finish_compress_fun_ptr){
    		(*finish_compress_fun_ptr)((jpeg_compress_struct*)pComressContext);
    	}
    	else
    	{
    		jpeg_finish_compress((jpeg_compress_struct*)pComressContext);
    	}
    }
    else
    {
    	jpeg_finish_compress((jpeg_compress_struct*)pComressContext);
    }

    void (*destroy_compress_fun_ptr)(j_compress_ptr);
    if(loadSucc)
    {
    	destroy_compress_fun_ptr=dl.GetFuncPtr("jpeg_destroy_compress");
    	if(NULL!=destroy_compress_fun_ptr)
    	{
    		(*destroy_compress_fun_ptr)(pComressContext);
    	}
    	else
    	{
    		jpeg_destroy_compress(pComressContext);
    	}
    }
    else
    {
    	jpeg_destroy_compress(pComressContext);
    }

    delete(pComressContext);
    LOGE("compress buffer to buffer :%s\n",compressParam.nErrno == 0?"success":"fail");

    return 0;
}


int CTSJpeg::DeCompressMemToMem(TSJpegParam &deCompressParam)
{
	TSDl dl;
	bool loadSucc=false;
//	if(!CpuABI::FeatrueContainsNeon())
//	{
//		loadSucc=dl.LoadDl(SYS_LIBJPEG);
//	}

    jpeg_decompress_struct *pDeCompressContext = new jpeg_decompress_struct;
    ZeroMemory(pDeCompressContext,sizeof(jpeg_decompress_struct));
    void (*createDecomrpess_fun_ptr)(j_decompress_ptr,int , size_t);
    if(loadSucc)
    {
    	createDecomrpess_fun_ptr=dl.GetFuncPtr("jpeg_CreateDecompress");
    	if(NULL!=createDecomrpess_fun_ptr)
    	{
    		(*createDecomrpess_fun_ptr)(pDeCompressContext,JPEG_LIB_VERSION,(size_t) sizeof(struct jpeg_decompress_struct));
    	}
    	else
    	{
    		jpeg_create_decompress(pDeCompressContext);
    	}
    }
    else
    {
    	jpeg_create_decompress(pDeCompressContext);
    }
    pDeCompressContext->client_data = (void*)&deCompressParam;

    jpeg_error_mgr errmgr;
    initErrMgr(errmgr);
    pDeCompressContext->err = &errmgr;

    jpeg_source_mgr srcmgr;
    initSrcMgr(srcmgr);
    pDeCompressContext->src = &srcmgr;

    int(*read_header_fun_prt)(j_decompress_ptr,boolean);
    if(loadSucc)
    {
    	read_header_fun_prt=dl.GetFuncPtr("jpeg_read_header");
    	if(NULL!=read_header_fun_prt)
    	{
    		(*read_header_fun_prt)(pDeCompressContext,TRUE);
    	}
    	else
    	{
    		jpeg_read_header(pDeCompressContext,TRUE);
    	}
    }
    else
    {
    	jpeg_read_header(pDeCompressContext,TRUE);
    }
    //jpeg_calc_output_dimensions(pDeCompressContext);
    //LOGD("calc dimensions Width: %d Height: %d", pDeCompressContext->output_width, pDeCompressContext->output_height);

    SetOutputScale(pDeCompressContext, deCompressParam.scale);

    boolean (*start_decompress_func_ptr)(j_decompress_ptr);
    if(loadSucc)
    {
    	start_decompress_func_ptr=dl.GetFuncPtr("jpeg_start_decompress");
    	if(NULL!=start_decompress_func_ptr)
    	{
    		(*start_decompress_func_ptr)(pDeCompressContext);
    	}
    	else
    	{
    		jpeg_start_decompress(pDeCompressContext);
    	}
    }
    else
    {
    	jpeg_start_decompress(pDeCompressContext);
    }
    //LOGD("after start Width: %d Height: %d", pDeCompressContext->output_width, pDeCompressContext->output_height);

    pDeCompressContext->out_color_space = (J_COLOR_SPACE)deCompressParam.color_space;
    pDeCompressContext->output_components = 1;
    pDeCompressContext->out_color_components = 1;

    deCompressParam.color_space = (int)pDeCompressContext->out_color_space;
    deCompressParam.img_components = pDeCompressContext->output_components;
    deCompressParam.img_width = pDeCompressContext->output_width;
    deCompressParam.img_height = pDeCompressContext->output_height;

    deCompressParam.dstBufferSize = deCompressParam.img_width*deCompressParam.img_height*3;
    deCompressParam.dstBuffer = new char [deCompressParam.dstBufferSize];
    ZeroMemory(deCompressParam.dstBuffer,deCompressParam.dstBufferSize);

    int totalReadLines = 0;
    int maxLines = deCompressParam.img_height; //lines;
    int rowStride = deCompressParam.img_width * 3;
    JSAMPROW rowpointer[1];
    JSAMPROW imageBuffer = (JSAMPROW)deCompressParam.dstBuffer;
    int row_stride = deCompressParam.img_width * 3;
    int readLines=0;

    JSAMPARRAY (*read_scanlines_func_ptr)(j_decompress_ptr, JSAMPARRAY,JDIMENSION);
    if(loadSucc)
    {
    	read_scanlines_func_ptr=dl.GetFuncPtr("jpeg_read_scanlines");
    }
    else
    {
    	read_scanlines_func_ptr=NULL;
    }
    while(pDeCompressContext->output_scanline < pDeCompressContext->output_height && deCompressParam.nErrno == 0)
    {
        rowpointer[0] = (imageBuffer + pDeCompressContext->output_scanline*row_stride);
        if(NULL!=read_scanlines_func_ptr)
        {
        	readLines=(*read_scanlines_func_ptr)(pDeCompressContext,rowpointer,1);
        }
        else
        {
        	readLines = jpeg_read_scanlines(pDeCompressContext,rowpointer,1);
        }
        //int readLines = jpeg_read_scanlines(pDeCompressContext,rowpointer,1);
        //LOGE("decompress read %d lines",readLines);
    }

    boolean (*finish_decompress_fun_ptr)(j_decompress_ptr);
    if(loadSucc){
    	finish_decompress_fun_ptr=dl.GetFuncPtr("jpeg_finish_decompress");
    	if(NULL!=finish_decompress_fun_ptr)
    	{
    		(*finish_decompress_fun_ptr)(pDeCompressContext);
    	}
    	else
    	{
    		jpeg_finish_decompress(pDeCompressContext);
    	}
    }
    else
    {
    	jpeg_finish_decompress(pDeCompressContext);
    }
    void (*destroy_decompress_fun_ptr)(j_decompress_ptr);
    if(loadSucc)
    {
    	destroy_decompress_fun_ptr=dl.GetFuncPtr("jpeg_destroy_decompress");
    	if(NULL!=destroy_decompress_fun_ptr)
    	{
    		(*destroy_decompress_fun_ptr)(pDeCompressContext);
    	}
    	else
		{
			jpeg_destroy_decompress(pDeCompressContext);
		}
    }
    else
    {
    	jpeg_destroy_decompress(pDeCompressContext);
    }
    delete pDeCompressContext;
    return 0;

}

static int CTSJpeg::DecodeMemToMem(TSJpegParam &deCompressParam)
{
	TSDl dl;
	bool loadSucc=false;
//	if(!CpuABI::FeatrueContainsNeon())
//	{
//		loadSucc=dl.LoadDl(SYS_LIBJPEG);
//	}

	jpeg_decompress_struct *pcinfo = new jpeg_decompress_struct;
	ZeroMemory(pcinfo,sizeof(jpeg_decompress_struct));
	void (*createDecomrpess_fun_ptr)(j_decompress_ptr,int , size_t);
	if(loadSucc)
	{
		createDecomrpess_fun_ptr=dl.GetFuncPtr("jpeg_CreateDecompress");
		if(NULL!=createDecomrpess_fun_ptr)
		{
			(*createDecomrpess_fun_ptr)(pcinfo,JPEG_LIB_VERSION,(size_t) sizeof(struct jpeg_decompress_struct));
		}
		else
		{
			jpeg_create_decompress(pcinfo);
		}
	}
	else
	{
		jpeg_create_decompress(pcinfo);
	}

	jpeg_error_mgr errmgr;
	pcinfo->err = jpeg_std_error( &errmgr );
	pcinfo->client_data = (void*)&deCompressParam;
/*
	jpeg_error_mgr errmgr;
	initErrMgr(errmgr);
	pcinfo->err = &errmgr;
*/
	jpeg_source_mgr srcmgr;
	initSrcMgr(srcmgr);
	pcinfo->src = &srcmgr;

	//����Դ�ڴ� �� ��С
//	jpeg_mem_src( pcinfo, (unsigned char *) deCompressParam.srcBuffer, deCompressParam.srcBufferSize );

	int(*read_header_fun_prt)(j_decompress_ptr,boolean);
	if(loadSucc)
	{
		read_header_fun_prt=dl.GetFuncPtr("jpeg_read_header");
		if(NULL!=read_header_fun_prt)
		{
			(*read_header_fun_prt)(pcinfo,TRUE);
		}
		else
		{
			jpeg_read_header(pcinfo,TRUE);
		}
	}
	else
	{
		jpeg_read_header(pcinfo,TRUE);
	}
	//jpeg_calc_output_dimensions(pcinfo);
	//LOGD("calc dimensions Width: %d Height: %d", pcinfo->output_width, pcinfo->output_height);

	//SetOutputScale(pcinfo, deCompressParam.scale);

	boolean (*start_decompress_func_ptr)(j_decompress_ptr);
	if(loadSucc)
	{
		start_decompress_func_ptr=dl.GetFuncPtr("jpeg_start_decompress");
		if(NULL!=start_decompress_func_ptr)
		{
			(*start_decompress_func_ptr)(pcinfo);
		}
		else
		{
			jpeg_start_decompress(pcinfo);
		}
	}
	else
	{
		jpeg_start_decompress(pcinfo);
	}
	//LOGD("after start Width: %d Height: %d", pcinfo->output_width, pcinfo->output_height);

	//pcinfo->out_color_space = (J_COLOR_SPACE)deCompressParam.color_space;
	//pcinfo->output_components = 1;
	//pcinfo->out_color_components = 1;

	int rowStride = pcinfo->output_width * pcinfo->output_components;

	deCompressParam.color_space = (int)pcinfo->out_color_space;
	deCompressParam.img_components = pcinfo->output_components;
	deCompressParam.img_width = pcinfo->output_width;
	deCompressParam.img_height = pcinfo->output_height;
	deCompressParam.img_widthStep = rowStride;
	deCompressParam.dstBufferSize = deCompressParam.img_widthStep*deCompressParam.img_height;
	deCompressParam.dstBuffer = new char [deCompressParam.dstBufferSize];
	ZeroMemory(deCompressParam.dstBuffer,deCompressParam.dstBufferSize);

	int totalReadLines = 0;
	int maxLines = deCompressParam.img_height; //lines;
	
	JSAMPROW rowpointer[1];
	JSAMPROW imageBuffer = (JSAMPROW)deCompressParam.dstBuffer;
	int row_stride = rowStride;
	int readLines=0;

//	deCompressParam.img_widthStep =
	JSAMPARRAY (*read_scanlines_func_ptr)(j_decompress_ptr, JSAMPARRAY,JDIMENSION);
	if(loadSucc)
	{
		read_scanlines_func_ptr=dl.GetFuncPtr("jpeg_read_scanlines");
	}
	else
	{
		read_scanlines_func_ptr=NULL;
	}
	while(pcinfo->output_scanline < pcinfo->output_height && deCompressParam.nErrno == 0)
	{
		rowpointer[0] = (imageBuffer + pcinfo->output_scanline*row_stride);
		if(NULL!=read_scanlines_func_ptr)
		{
			readLines=(*read_scanlines_func_ptr)(pcinfo,rowpointer,1);
		}
		else
		{
			readLines = jpeg_read_scanlines(pcinfo,rowpointer,1);
		}
		//int readLines = jpeg_read_scanlines(pcinfo,rowpointer,1);
		//LOGE("decompress read %d lines",readLines);
	}

	boolean (*finish_decompress_fun_ptr)(j_decompress_ptr);
	if(loadSucc){
		finish_decompress_fun_ptr=dl.GetFuncPtr("jpeg_finish_decompress");
		if(NULL!=finish_decompress_fun_ptr)
		{
			(*finish_decompress_fun_ptr)(pcinfo);
		}
		else
		{
			jpeg_finish_decompress(pcinfo);
		}
	}
	else
	{
		jpeg_finish_decompress(pcinfo);
	}
	void (*destroy_decompress_fun_ptr)(j_decompress_ptr);
	if(loadSucc)
	{
		destroy_decompress_fun_ptr=dl.GetFuncPtr("jpeg_destroy_decompress");
		if(NULL!=destroy_decompress_fun_ptr)
		{
			(*destroy_decompress_fun_ptr)(pcinfo);
		}
		else
		{
			jpeg_destroy_decompress(pcinfo);
		}
	}
	else
	{
		jpeg_destroy_decompress(pcinfo);
	}
	delete pcinfo;
	return 0;

}


static void ErrorExit(j_common_ptr cinfo)
{
    int errorCode = cinfo->err->msg_code;
    const char* errMsg = cinfo->err->jpeg_message_table[errorCode];
    if(!cinfo->is_decompressor)
    {
        jpeg_compress_struct *pCompressContext = (jpeg_compress_struct*)cinfo;
        TSJpegParam *pParam = (TSJpegParam*)pCompressContext->client_data;

        if(errorCode != JERR_BAD_IN_COLORSPACE)
            pParam->nErrno = errorCode;

        LOGE("Compress err:%d[%s]\n",errorCode,errMsg);
    }
    else
    {
        jpeg_decompress_struct *pDeCompressContext = (jpeg_decompress_struct*)cinfo;
        TSJpegParam *pParam = (TSJpegParam*)pDeCompressContext->client_data;

        //if(errorCode != JERR_BAD_IN_COLORSPACE)
        //    pParam->nErrno = errorCode;

        LOGE("DeCompress err:%d[%s]\n",errorCode,errMsg);
    }

}
static void EmitMessage(j_common_ptr cinfo,int msgLevel)
{
    //LOGE(cinfo->err->jpeg_message_table[cinfo->err->msg_code]);
}
static void OutputMessage(j_common_ptr cinfo)
{
    //LOGE(cinfo->err->jpeg_message_table[cinfo->err->msg_code]);
    //LOGE("\n");
}
static void FormatMessage(j_common_ptr,char*buffer)
{

}
static void ResetErrorMgr(j_common_ptr cinfo)
{

}


