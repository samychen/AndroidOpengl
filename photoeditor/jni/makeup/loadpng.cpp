#include "cosmetic.h"
#include "png.h"
#include "fmtconvert.h"
#include "utils/debug.h"

#define PNG_BYTES_TO_CHECK 4

void user_read_data(png_structp png_ptr, png_bytep data, png_size_t length)
{
    int readBytes = AAsset_read((AAsset*)png_ptr->io_ptr, data, length);
    if(readBytes!=length) {
    	png_error(png_ptr, "Read Error");
    }
}


int load_png_data_palette( const char *filepath, TSM_OFFSCREEN *psMultiChannel, AAssetManager* asset)//for png8
{
	AAsset *fp;
	png_structp png_ptr;
	png_infop info_ptr;
	//png_bytep* row_pointers;
	unsigned char * row_pointers;
	char buf[PNG_BYTES_TO_CHECK];
	int w, h, x, y, temp, color_type;

	if (!filepath || !psMultiChannel || !psMultiChannel->ppPlane[0])
	{
		return -1;
	}
	
	fp = AAssetManager_open(asset, filepath, AASSET_MODE_UNKNOWN);
	if( fp == NULL ) { 
		LOGI("open png file error\n");
		return -1; 
	}

	png_ptr  = png_create_read_struct( PNG_LIBPNG_VER_STRING, 0, 0, 0 );
	info_ptr = png_create_info_struct( png_ptr );

	setjmp( png_jmpbuf(png_ptr) ); 
	//检验PNG格式
	temp = AAsset_read(fp, buf, PNG_BYTES_TO_CHECK);
	if( temp < PNG_BYTES_TO_CHECK ) {
		AAsset_close(fp);
		png_destroy_read_struct( &png_ptr, &info_ptr, 0);
		LOGI("not a png file\n");
		return -2 ;
	}
	/* 检测数据是否为PNG的签名 */
	temp = png_sig_cmp( (png_bytep)buf, (png_size_t)0, PNG_BYTES_TO_CHECK );       
	if( temp != 0 )
	{
		AAsset_close(fp);
		png_destroy_read_struct( &png_ptr, &info_ptr, 0);
		LOGI("png signiture error\n");
		return -3 /* 你的返回值 */;
	}

	/* 复位文件指针 */
	AAsset_seek( fp, 0, SEEK_SET);
	png_init_io( png_ptr, (png_FILE_p)fp );
	png_set_read_fn(png_ptr, fp, (png_rw_ptr)user_read_data);
	png_read_info(png_ptr, info_ptr);
	color_type = png_get_color_type( png_ptr, info_ptr );

	if (color_type!=PNG_COLOR_TYPE_PALETTE)
	{
		AAsset_close(fp);
		png_destroy_read_struct( &png_ptr, &info_ptr, 0);
		LOGI("only PNG_COLOR_TYPE_PALETTE support");
		return -4 /* 你的返回值 */;
	}

	png_color_16 my_background={ 0, 192, 192, 192, 0 };
	png_color_16 *image_background;

	if (info_ptr->pixel_depth != 32){
		if (png_get_bKGD(png_ptr, info_ptr, &image_background))
			png_set_background(png_ptr, image_background,PNG_BACKGROUND_GAMMA_FILE, 1, 1.0);
		else
			png_set_background(png_ptr, &my_background,PNG_BACKGROUND_GAMMA_SCREEN, 0, 1.0);			
	}

	//GetPalette
	int  m=TMIN(256,info_ptr->num_palette);
	png_colorp pcolor = info_ptr->palette;
	TRGBA pPalette[256] = {0};
	for (int i=0; i<m;i++){
		int Y,U,V;
		_RGB2YUV(pcolor[i].red,pcolor[i].green,pcolor[i].blue,Y,U,V);
		pPalette[i].rgbRed      = Y;//pcolor[i].red;
		pPalette[i].rgbGreen    = U;//pcolor[i].green;
		pPalette[i].rgbBlue     = V;//pcolor[i].blue;
		if(i<info_ptr->num_trans)
			pPalette[i].rgbReserved = info_ptr->trans[i];
		else
			pPalette[i].rgbReserved = 255;
	}

	if (info_ptr->color_type & PNG_COLOR_MASK_COLOR) png_set_bgr(png_ptr);

	/* 获取图像的宽高 */
	w = png_get_image_width( png_ptr, info_ptr );
	h = png_get_image_height( png_ptr, info_ptr );	

	int row_stride = info_ptr->width * ((info_ptr->pixel_depth+7)>>3);
	row_pointers = new TUInt8[10+row_stride];

	switch( color_type ) {
		case PNG_COLOR_TYPE_RGB_ALPHA:		
			break;
		case PNG_COLOR_TYPE_RGB:
			break;
		case PNG_COLOR_TYPE_PALETTE :
			{		
				psMultiChannel->lHeight = h;
				psMultiChannel->lWidth  = w;
				psMultiChannel->plPitch[0] = w*4;
				psMultiChannel->lPixelArrayFormat = TSM_PAF_RGBA8888;
				for (y=0;y<h;y++)
				{
					unsigned char * pDst = (unsigned char *)psMultiChannel->ppPlane[0] + y*psMultiChannel->plPitch[0];
					png_read_row(png_ptr, row_pointers, NULL);
					for (x=0;x<w;x++)
					{
						int val     = row_pointers[x];
						pDst[x*4]   = pPalette[val].rgbRed;//Y
						pDst[x*4+1] = pPalette[val].rgbGreen;//U
						pDst[x*4+2] = pPalette[val].rgbBlue;//V
						pDst[x*4+3] = pPalette[val].rgbReserved;
					}				
				}
			}
			break;
		default:
			AAsset_close(fp);
			png_destroy_read_struct( &png_ptr, &info_ptr, 0);
			return 0;
	}
	png_destroy_read_struct( &png_ptr, &info_ptr, 0);
	delete [] row_pointers;
	return 0;
}

