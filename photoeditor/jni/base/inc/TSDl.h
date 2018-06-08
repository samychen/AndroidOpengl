/**
 * Copyright (C) 2011 Thundersoft Corporation
 * All rights Reserved
 */
#ifndef TSDL_H
#define TSDL_H

class TSDl{
public:
	TSDl();
	~TSDl();
	bool LoadDl(const char * dlPath);
	void *GetFuncPtr(const char *dlPath,const char *symbol);
	void *GetFuncPtr(const char *symbol);
private:
	void *pdl;
	void Release();
};

#endif //TSDL_H

