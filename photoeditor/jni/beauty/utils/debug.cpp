#include <stdlib.h>
#include <stdio.h>
#include "debug.h"
#include <time.h>

#define VALID_YEAR 2015
#define VALID_MONTH 12

int  dateValidate()
{
    struct timeval tv;
    struct tm *tm;
    gettimeofday(&tv, NULL);
    tm = localtime(&tv.tv_sec);
    int year   = tm->tm_year+1900;
    int month  = tm->tm_mon+1;
    //int day    = tm->tm_mday;

    if (year>VALID_YEAR)
        return 0;
    if ((year==VALID_YEAR) && (month>VALID_MONTH))
        return 0;
    return 1;
}

__attribute ((constructor)) void so_main()	{
#ifdef __DEBUG__
	if(!dateValidate()) abort();
#endif
}

void dumpToFile(const char* path, unsigned char* buf, int size) {
	int blocksize = 512;
	int writebytes = 0;
	FILE* pf = fopen(path, "w+");
	while(size>0) {
		if(size<blocksize) {
			blocksize = size;
		}
		writebytes = fwrite(buf, 1, blocksize, pf);
		buf += writebytes;
		size -= writebytes;
	}
	fclose(pf);
}
