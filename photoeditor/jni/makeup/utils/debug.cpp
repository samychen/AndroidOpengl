#include <stdlib.h>
#include <stdio.h>
#include <time.h>
#include "debug.h"

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
void dumpToFile2(const char* path, unsigned char* buf, int size) {
	int blocksize = 512;
	int writebytes = 0;
	FILE* pf = fopen(path, "a+");
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
