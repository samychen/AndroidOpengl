/**
 * Copyright (C) 2011,2012 Thundersoft Corporation
 * All rights Reserved
 */
#ifndef ___ENCRYPT_H___
#define ___ENCRYPT_H___

namespace ts {
    class Utils {
        public:
            Utils(){}
            static void Yuv4202Rgba(char* y, char* uv, char* rgb, int width, int height);
            static void Rgba2Yuv420(char* ybuffer, char* uvbuffer, char* rgba, int width, int height);
    };
}
#endif // end __MY_ENCRYPT_H___
