package com.ranjith.histogram.utils;

import android.content.Context;
import android.os.Build;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicHistogram;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;
import android.support.annotation.RequiresApi;

public class Converter {
    static final private String TAG = "Converter";

    private RenderScript rs;
    private ScriptIntrinsicYuvToRGB si;
    private ScriptIntrinsicHistogram hi;
    private Allocation yuvIn;
    private Allocation rgbIn;
    private Allocation rgbOut;
    private Allocation intOut;

    private byte[] pixels;
    private int[] hist;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public Converter(Context context) {
        rs = RenderScript.create(context);
        si = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs));
        hi = ScriptIntrinsicHistogram.create(rs, Element.U8_4(rs));
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public byte[] convertToRGB(byte[] yuv, int width, int height) {
        int size = (width * height * 4);

        if (yuvIn == null || pixels.length != size) {
            Type.Builder yuvType = new Type.Builder(rs, Element.U8(rs))
                    .setX(yuv.length);
            yuvIn = Allocation.createTyped(rs, yuvType.create());

            Type.Builder rgbaType = new Type.Builder(rs, Element.RGBA_8888(rs))
                    .setX(width)
                    .setY(height);
            rgbOut = Allocation.createTyped(rs, rgbaType.create());
            pixels = new byte[size];
        }

        yuvIn.copyFrom(yuv);
        si.setInput(yuvIn);
        si.forEach(rgbOut);
        rgbOut.copyTo(pixels);

        return pixels;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public int[] histogram(byte[] rgb, int width, int height) {
        int size = (width * height * 4);

        if (rgbIn == null || rgbIn.getBytesSize() != size) {
            Type.Builder rgbaType = new Type.Builder(rs, Element.RGBA_8888(rs))
                    .setX(width)
                    .setY(height);
            rgbIn = Allocation.createTyped(rs, rgbaType.create());

            Type.Builder uintType = new Type.Builder(rs, Element.U32_4(rs))
                    .setX(256);
            intOut = Allocation.createTyped(rs, uintType.create());
            hist = new int[256 * 4];
        }

        rgbIn.copyFrom(rgb);
        hi.setOutput(intOut);
        hi.forEach(rgbIn);
        intOut.copyTo(hist);

        return hist;
    }
}