/**
 * Copyright (C) 2011 Thundersoft Corporation
 * All rights Reserved
 */
#ifndef CPUABI_H
#define CPUABI_H

#define SYS_LIBJPEG "/system/lib/libjpeg.so"

class CpuABI{
public:
	static bool FeatrueContainsNeon();
private:
	CpuABI(){};
};

#endif //CPUABI_H

