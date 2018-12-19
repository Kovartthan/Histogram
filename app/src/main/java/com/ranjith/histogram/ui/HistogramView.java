package com.ranjith.histogram.ui;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.view.View;

import com.ranjith.histogram.utils.Converter;

@SuppressWarnings("deprecation")
public class HistogramView extends View implements android.hardware.Camera.PreviewCallback,
        Handler.Callback {
    static final private String TAG = "HistogramView";

    private static final int HISTOGRAM = 0;

    private Paint paint;
    private Handler handler;
    private Converter converter;
    private Configuration config;

    private RectF rect;

    private int width;
    private int height;
    private long max;

    private int[] histogram;

    private long count;

    public HistogramView(Context context) {
        super(context);

        paint = new Paint();
        handler = new Handler(this);
        converter = new Converter(getContext());
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        width = w;
        height = h;

        config = getResources().getConfiguration();
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (histogram == null)
            return;

        if (config.orientation == Configuration.ORIENTATION_PORTRAIT)
            canvas.drawColor(Color.BLACK);

        paint.setStrokeWidth(width / (histogram.length * 3 / 4));

        float xscale = (float) width / (histogram.length * 3 / 4);
        float yscale = (float) height / this.max;

        int i = 0, x = 0;
        int max = 0;
        for (int h : histogram) {
            if ((i < 4) || (i > (histogram.length - 5))) {
                i++;
                continue;
            }

            if (max < h)
                max = h;

            float xpos = x * xscale;
            float ypos = h * yscale;

            switch (i % 4) {
                case 0:
                    paint.setColor(Color.RED);
                    x++;
                    break;

                case 1:
                    paint.setColor(Color.GREEN);
                    x++;
                    break;

                case 2:
                    paint.setColor(Color.BLUE);
                    x++;
                    break;
            }

            canvas.drawLine(xpos, height,
                    xpos, height - ypos, paint);
            i++;
        }

        this.max = max;
    }

    @Override
    public void onPreviewFrame(byte[] data, android.hardware.Camera camera) {
        if (data != null) {
            if (count++ % 10 == 0) {
                android.hardware.Camera.Size size =
                        camera.getParameters().getPreviewSize();
                Message message = handler.obtainMessage(HISTOGRAM, size.width,
                        size.height, data);
                message.sendToTarget();
            }
        }
    }

    @Override
    public boolean handleMessage(Message message) {
        int width = message.arg1;
        int height = message.arg2;
        byte[] data = (byte[]) message.obj;
        byte[] pixels = converter.convertToRGB(data, width, height);
        histogram = converter.histogram(pixels, width, height);
        invalidate();
        return true;
    }
}