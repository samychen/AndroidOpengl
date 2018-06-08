/**
 * Copyright (C) 2011 Thundersoft Corporation
 * All rights Reserved
 *
 *
 * use linux libdl to implements
 */

#include <stdio.h>
#include <dlfcn.h>
#include "TSDl.h"

TSDl::TSDl():pdl(NULL){

}

/*
 * linux
 * dlclose
 */
TSDl::~TSDl(){
	Release();
}

void TSDl::Release(){
	if(NULL!=pdl){
		dlclose(pdl);
	}
	pdl=NULL;
}

bool TSDl::LoadDl(const char * dlPath){
	Release();
	if(NULL!=dlPath){
		pdl=dlopen(dlPath,RTLD_NOW);
		if(NULL!=pdl){
			return true;
		}else{
			return false;
		}
	}
	return false;
}

void *TSDl::GetFuncPtr(const char *symbol){
	void *r=NULL;
	if((NULL!=pdl)&&(NULL!=symbol)){
		r=dlsym(pdl,symbol);
	}
	return r;
}

/*
 * linux
 * dlop
 * dlsym
 */
void *TSDl::GetFuncPtr(const char *dlPath,const char *symbol){
	void *r=NULL;
	if(NULL!=dlPath){
		pdl=dlopen(dlPath,RTLD_NOW);
	}
	if((NULL!=pdl)&&(NULL!=symbol)){
		r=dlsym(pdl,symbol);
	}
	return r;
}
