/**
 * Copyright (C) 2011 Thundersoft Corporation
 * All rights Reserved
 *
 * use to get cpu information etc features
 */
#include <cpu-features.h>
#include "CpuABI.h"

/*
 * ARM family Cpu features contains neon
 * @return contains return true ,or not false
 */
bool CpuABI::FeatrueContainsNeon(){
	if(android_getCpuFamily()==ANDROID_CPU_FAMILY_ARM&&
				(android_getCpuFeatures()&ANDROID_CPU_ARM_FEATURE_NEON)!=0){
		return true;
	}
	return false;
}
