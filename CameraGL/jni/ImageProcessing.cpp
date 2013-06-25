/*
 *  ImageProcessing.cpp
 */
#include <jni.h>
#include <math.h>

using namespace std;

inline int convertYUVtoRGB(int y, int u, int v, float mR, float mG, float mB) {
	int r, g, b;
	float mU = u, mV = v;
	float rgbcolor[3] = { mR / 255.0, mG / 255.0, mB / 255.0 };

	float atanP, atanF;
	float filterU, filterV, delta, av = -0.25, bv = -0.2, cv = 0.2, dv = 0.25,
			alpha;
	float gray = y;

	//Check if pixel color needs to be filtered by setting u and v to 0

	filterU = 0.436 * mR - 0.33609 * mG - 0.09991 * mB;
	filterV = -0.05639 * mR - 0.55861 * mG + 0.615 * mB;

	atanP = atan2f(mV, mU);
	atanF = atan2f(filterV, filterU);

	delta = fabsf(atanF - atanP);

	if (delta < av || delta > dv)
		alpha = 0.0;
	else if (av <= delta && delta <= bv)
		alpha = (delta - av) / (b - av);
	else if (bv <= delta && delta <= cv)
		alpha = 1.0;
	else if (cv <= delta && delta <= dv)
		alpha = (dv - delta) / (dv - cv);

	r = y + (int) 1.402f * mV;
	g = y - (int) (0.344f * mU + 0.714f * mV);
	b = y + (int) 1.772f * mU;

	r = r > 255 ? 255 : r < 0 ? 0 : r;
	g = g > 255 ? 255 : g < 0 ? 0 : g;
	b = b > 255 ? 255 : b < 0 ? 0 : b;

	r *= alpha;
	g *= alpha;
	b *= alpha;

	r += gray * (1 - alpha);
	g += gray * (1 - alpha);
	b += gray * (1 - alpha);

	return 0xff000000 | (b << 16) | (g << 8) | r;
}

extern "C" jboolean Java_com_jxhembulla_cameragl_ndk_CameraPreview_ImageProcessing(
		JNIEnv* env, jobject thiz, jint width, jint height,
		jbyteArray NV21FrameData, jintArray outPixels, jint bitsPerPixel,
		jfloat mR, jfloat mG, jfloat mB) {
	jbyte * pNV21FrameData = env->GetByteArrayElements(NV21FrameData, 0);
	jint * poutPixels = env->GetIntArrayElements(outPixels, 0);

	int size = width * height;
	int offset = size;
	int u, v, y1, y2, y3, y4;

	//TEMP VARIABLES
	int r, g, b;
	float mU = u, mV = v;
	float rgbcolor[3] = { 0 };

	float atanP, atanF;
	float filterU, filterV, delta, av = -0.25, bv = -0.2, cv = 0.2, dv = 0.25,
			alpha;
	float gray;

	for (int i = 0, k = 0; i < size; i += 2, k += 2) {
		y1 = pNV21FrameData[i] & 0xff;
		y2 = pNV21FrameData[i + 1] & 0xff;
		y3 = pNV21FrameData[width + i] & 0xff;
		y4 = pNV21FrameData[width + i + 1] & 0xff;

		u = pNV21FrameData[offset + k] & 0xff;
		v = pNV21FrameData[offset + k + 1] & 0xff;
		u = u - 128;
		v = v - 128;

		mU = u;
		mV = v;

		rgbcolor[0] = mR / 255.0;
		rgbcolor[1] = mG / 255.0;
		rgbcolor[2] = mB / 255.0;

		filterU = 0.436 * mR - 0.33609 * mG - 0.09991 * mB;
		filterV = -0.05639 * mR - 0.55861 * mG + 0.615 * mB;

		atanP = atan2f(mV, mU);
		atanF = atan2f(filterV, filterU);

		delta = fabsf(atanF - atanP);

		if (delta < av || delta > dv)
			alpha = 0.0;
		else if (av <= delta && delta <= bv)
			alpha = (delta - av) / (b - av);
		else if (bv <= delta && delta <= cv)
			alpha = 1.0;
		else if (cv <= delta && delta <= dv)
			alpha = (dv - delta) / (dv - cv);

		//FIRST PIXEL OF THE REGION
		//poutPixels[i] = convertYUVtoRGB(y1, u, v, mR, mG, mB);

		{
			r = y1 + (int) 1.402f * mV;
			g = y1 - (int) (0.344f * mU + 0.714f * mV);
			b = y1 + (int) 1.772f * mU;

			r = r > 255 ? 255 : r < 0 ? 0 : r;
			g = g > 255 ? 255 : g < 0 ? 0 : g;
			b = b > 255 ? 255 : b < 0 ? 0 : b;

			r *= alpha;
			g *= alpha;
			b *= alpha;

			r += y1 * (1 - alpha);
			g += y1 * (1 - alpha);
			b += y1 * (1 - alpha);

			poutPixels[i] = 0xff000000 | (b << 16) | (g << 8) | r;
		}

		//SECOND PIXEL OF THE REGION
		//poutPixels[i + 1] = convertYUVtoRGB(y2, u, v, mR, mG, mB);

		{
			r = y2 + (int) 1.402f * mV;
			g = y2 - (int) (0.344f * mU + 0.714f * mV);
			b = y2 + (int) 1.772f * mU;

			r = r > 255 ? 255 : r < 0 ? 0 : r;
			g = g > 255 ? 255 : g < 0 ? 0 : g;
			b = b > 255 ? 255 : b < 0 ? 0 : b;

			r *= alpha;
			g *= alpha;
			b *= alpha;

			r += y2 * (1 - alpha);
			g += y2 * (1 - alpha);
			b += y2 * (1 - alpha);

			poutPixels[i + 1] = 0xff000000 | (b << 16) | (g << 8) | r;
		}
		//THIRD PIXEL OF THE REGION
		//poutPixels[width + i] = convertYUVtoRGB(y3, u, v, mR, mG, mB);
		{
			r = y3 + (int) 1.402f * mV;
			g = y3 - (int) (0.344f * mU + 0.714f * mV);
			b = y3 + (int) 1.772f * mU;

			r = r > 255 ? 255 : r < 0 ? 0 : r;
			g = g > 255 ? 255 : g < 0 ? 0 : g;
			b = b > 255 ? 255 : b < 0 ? 0 : b;

			r *= alpha;
			g *= alpha;
			b *= alpha;

			r += y3 * (1 - alpha);
			g += y3 * (1 - alpha);
			b += y3 * (1 - alpha);

			poutPixels[width + i] = 0xff000000 | (b << 16) | (g << 8) | r;
		}
		//FOURTH PIXEL OF THE REGION
		//poutPixels[width + i + 1] = convertYUVtoRGB(y4, u, v, mR, mG, mB);

		{
			r = y4 + (int) 1.402f * mV;
			g = y4 - (int) (0.344f * mU + 0.714f * mV);
			b = y4 + (int) 1.772f * mU;

			r = r > 255 ? 255 : r < 0 ? 0 : r;
			g = g > 255 ? 255 : g < 0 ? 0 : g;
			b = b > 255 ? 255 : b < 0 ? 0 : b;

			r *= alpha;
			g *= alpha;
			b *= alpha;

			r += y4 * (1 - alpha);
			g += y4 * (1 - alpha);
			b += y4 * (1 - alpha);

			poutPixels[width + i + 1] = 0xff000000 | (b << 16) | (g << 8) | r;
		}

		if (i != 0 && (i + 2) % width == 0)
			i += width;
	}
	return true;
}

