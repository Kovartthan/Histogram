package com.ranjith.histogram.ui;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;

import java.util.List;

@SuppressWarnings("deprecation")
public class CameraPreview extends SurfaceView
        implements SurfaceHolder.Callback {
    static final private String TAG = "CameraPreview";

    private HistogramView histogram;
    private android.hardware.Camera camera;
    private Context context;

    public CameraPreview(Context context) {
        super(context);
        this.context = context;
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
    }

    public void setCamera(android.hardware.Camera camera) {
        this.camera = camera;
    }

    public void setHistogramView(HistogramView histogram) {
        this.histogram = histogram;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            camera.setPreviewDisplay(holder);
            camera.setPreviewCallback(histogram);
            camera.startPreview();
        } catch (Exception e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {

        if (holder.getSurface() == null) {
            return;
        }
        try {
            camera.stopPreview();
        } catch (Exception e) {
        }


        Configuration config = getResources().getConfiguration();

        android.hardware.Camera.Parameters cameraParams =
                camera.getParameters();

        List<String> modes = cameraParams.getSupportedFocusModes();
        if (modes.contains(android.hardware.Camera.Parameters
                .FOCUS_MODE_CONTINUOUS_PICTURE))
            cameraParams
                    .setFocusMode(android.hardware.Camera.Parameters
                            .FOCUS_MODE_CONTINUOUS_PICTURE);

        for (Camera.Size size : cameraParams.getSupportedPictureSizes()) {
            if (1600 <= size.width & size.width <= 1920) {
                cameraParams.setPreviewSize(size.width, size.height);
                cameraParams.setPictureSize(size.width, size.height);
                break;
            }
        }
        camera.setParameters(cameraParams);

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int dWidth = displayMetrics.widthPixels;
        int dHeight = displayMetrics.heightPixels;

        android.hardware.Camera.Size size = cameraParams.getPreviewSize();
        FrameLayout.LayoutParams params =
                (FrameLayout.LayoutParams) getLayoutParams();

        params.width = FrameLayout.LayoutParams.MATCH_PARENT;// dm.widthPixels should also work
        params.height = FrameLayout.LayoutParams.MATCH_PARENT;

        setLayoutParams(params);

        histogram.setLayoutParams(params);

      /*  switch (config.orientation) {
            case Configuration.ORIENTATION_PORTRAIT:
                params.height = w * size.width / size.height;
                params.gravity = Gravity.TOP;
                setLayoutParams(params);

                int height = params.height;
                params = (FrameLayout.LayoutParams)
                        histogram.getLayoutParams();
                params.height = h - height;
                params.gravity = Gravity.BOTTOM;
                histogram.setLayoutParams(params);
                break;

            case Configuration.ORIENTATION_LANDSCAPE:
                params.width = w * size.height / size.width;
                params.gravity = Gravity.CENTER;
                setLayoutParams(params);
                break;
        }
*/
        try {
            camera.setPreviewDisplay(holder);
            camera.setPreviewCallback(histogram);
            camera.startPreview();
        } catch (Exception e) {
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }
}