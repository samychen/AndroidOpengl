#ifndef __GLMAKEUP_H__
#define __GLMAKEUP_H__
#include "program.h"
#include "texture_2d.h"
class GLMakeup
{
    public:
        GLMakeup();
        ~GLMakeup();

        void surfaceChanged(int surfaceWidth, int surfaceHeight);
        int skinWhiten(void *yBuf, void *uvBuf, int width, int height,float *matrix, int matsize, int level,
                void *outPixels=NULL);

    private:
        void release();
        int mLevel;
        program *mProgram;
        texture_2d *mWhitenTexture;
        texture_2d *mYTexture;
        texture_2d *mUVTexture;
};

#endif // end __GLMAKEUP_H__
