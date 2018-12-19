package com.ranjith.histogram.activities;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import com.ranjith.histogram.R;
import com.ranjith.histogram.ui.CameraPreview;
import com.ranjith.histogram.ui.HistogramView;

public class CameraActivity extends Activity {
    static final private String TAG = "CameraActivity";
    static final private String HIST = "hist";
    static final private String ID = "id";

    static final private int MARGIN = 56;
    static final private int ELEVATION = 24;

    private int id;
    private boolean hist = true;
    private android.hardware.Camera camera;
    private CameraPreview preview;
    private HistogramView histogram;
    private ImageButton button;
    private boolean isFirstTime = false;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Toast.makeText(this,"Double tap to disable the histogram",Toast.LENGTH_SHORT).show();

        preview = new CameraPreview(this);
        setContentView(preview);

        histogram = new HistogramView(this);
        ViewGroup parent = (ViewGroup) preview.getParent();
        parent.addView(histogram);
        preview.setHistogramView(histogram);


        button = new ImageButton(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            button.setElevation(ELEVATION);
        button.setImageResource(R.drawable.ic_action_switch_camera);
        button.setBackgroundResource(R.drawable.ic_button_background);
        parent.addView(button);

        FrameLayout.LayoutParams params =
                (FrameLayout.LayoutParams) button.getLayoutParams();
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.CENTER_VERTICAL | Gravity.END;
        params.leftMargin = MARGIN;
        params.rightMargin = MARGIN;
        button.setLayoutParams(params);


        if (savedInstanceState != null) {
            id = savedInstanceState.getInt(ID);
            hist = savedInstanceState.getBoolean(HIST);
            if (!hist)
                histogram.setVisibility(View.INVISIBLE);
        }

        preview.setOnClickListener(v -> {
            hist = !hist;

            if (!hist) {
                histogram.setVisibility(View.INVISIBLE);
                if(!isFirstTime)
                Toast.makeText(this,"Touch again to enable the histogram",Toast.LENGTH_SHORT).show();
            }
            else {
                histogram.setVisibility(View.VISIBLE);
            }
            isFirstTime = true;
        });


        histogram.setOnClickListener(v -> {
            hist = !hist;

            if (!hist) {
                histogram.setVisibility(View.INVISIBLE);
                if(!isFirstTime)
                    Toast.makeText(this,"Touch again to enable the histogram",Toast.LENGTH_SHORT).show();
            }else {
                histogram.setVisibility(View.VISIBLE);
            }
            isFirstTime = true;
        });

        button.setOnClickListener(v ->
        {
            int cameras = android.hardware.Camera.getNumberOfCameras();
            id = (id + 1) % cameras;
            recreate();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        camera = android.hardware.Camera.open(id);
        Configuration config = getResources().getConfiguration();
        switch (config.orientation) {
            case Configuration.ORIENTATION_PORTRAIT:
                camera.setDisplayOrientation(90);
                break;
        }
        preview.setCamera(camera);
        preview.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_LOW_PROFILE |
                        View.SYSTEM_UI_FLAG_FULLSCREEN);
        try {
            camera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (camera != null) {
            try {
                camera.setPreviewCallback(null);
                camera.stopPreview();
                camera.release();
                camera = null;
            } catch (Exception e) {
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(HIST, hist);
        outState.putInt(ID, id);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        id = savedInstanceState.getInt(ID);
        hist = savedInstanceState.getBoolean(HIST);
        if (!hist)
            histogram.setVisibility(View.INVISIBLE);
    }
}