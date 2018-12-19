package com.ranjith.histogram.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ranjith.histogram.BuildConfig;
import com.ranjith.histogram.R;
import com.ranjith.histogram.utils.FileUtils;
import com.ranjith.histogram.utils.ImageUtils;
import com.ranjith.histogram.utils.TextUtils;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Scalar;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class CompareActivity extends AppCompatActivity {
    private Button btnAddFirstImage, btnAddSecondImage, btnComapre;
    private String firstPath, secondPath;
    public static final int SELECT_IMAGE = 200;
    public static final int CAMERA_RESULT = 201;
    private String photoSaveName;
    private boolean isPermissionDefaults;
    private String currentlyCliked;
    private static String[] PERMISSIONS = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE};
    private String descriptorType = "FREAK";
    public static int min_dist = 10;
    public static int min_matches = 750;
    private static long startTime, endTime;
    private Bitmap bmpimg1,bmpimg2;
    private Bitmap bmp;
    private static String text;
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case SELECT_IMAGE:
                    String savedImageFile = FileUtils.createImageProfile(CompareActivity.this, photoSaveName).getAbsolutePath();
                    try {
                        InputStream inputStream = CompareActivity.this.getContentResolver().openInputStream(data.getData());
                        FileOutputStream fileOutputStream = new FileOutputStream(savedImageFile);
                        FileUtils.copyStream(inputStream, fileOutputStream);
                        fileOutputStream.close();
                        inputStream.close();
                        if(photoSaveName.equalsIgnoreCase("photoOne")){
                            firstPath = savedImageFile;
                            bmpimg1 = ImageUtils.getInstant().getCompressedBitmap(firstPath);
                            btnAddFirstImage.setText("Added");
                        }else{
                            secondPath = savedImageFile;
                            bmpimg2 = ImageUtils.getInstant().getCompressedBitmap(secondPath);
                            btnAddSecondImage.setText("Added");
                        }

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    break;

                case CAMERA_RESULT:
                    String savedCameraFile = FileUtils.createImageProfile(CompareActivity.this, photoSaveName).getAbsolutePath();
                    if(photoSaveName.equalsIgnoreCase("photoOne")){
                        firstPath = savedCameraFile;
                        bmpimg1 = ImageUtils.getInstant().getCompressedBitmap(firstPath);
                        btnAddFirstImage.setText("Added");
                    }else{
                        secondPath = savedCameraFile;
                        bmpimg2 = ImageUtils.getInstant().getCompressedBitmap(secondPath);
                        btnAddSecondImage.setText("Added");
                    }

                    break;

            }
        }
    }


    public  class asyncTask extends AsyncTask<Void, Void, Void> {
        private  Mat img1, img2, descriptors, dupDescriptors;
        private  FeatureDetector detector;
        private  DescriptorExtractor DescExtractor;
        private  DescriptorMatcher matcher;
        private  MatOfKeyPoint keypoints, dupKeypoints;
        private  MatOfDMatch matches, matches_final_mat;
        private  ProgressDialog pd;
        private  boolean isDuplicate = false;
        private CompareActivity asyncTaskContext = null;
        private  Scalar RED = new Scalar(255, 0, 0);
        private  Scalar GREEN = new Scalar(0, 255, 0);

        public asyncTask(CompareActivity context) {
            asyncTaskContext = context;
        }

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(asyncTaskContext);
            pd.setIndeterminate(true);
            pd.setCancelable(true);
            pd.setCanceledOnTouchOutside(false);
            pd.setMessage("Processing...");
            pd.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // TODO Auto-generated method stub
            compare();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            try {
                Mat img3 = new Mat();
                MatOfByte drawnMatches = new MatOfByte();
                Features2d.drawMatches(img1, keypoints, img2, dupKeypoints,
                        matches_final_mat, img3, GREEN, RED, drawnMatches, Features2d.NOT_DRAW_SINGLE_POINTS);
                bmp = Bitmap.createBitmap(img3.cols(), img3.rows(),
                        Bitmap.Config.ARGB_8888);
                Imgproc.cvtColor(img3, img3, Imgproc.COLOR_BGR2RGB);
                Utils.matToBitmap(img3, bmp);
                List<DMatch> finalMatchesList = matches_final_mat.toList();
                final int matchesFound = finalMatchesList.size();
                endTime = System.currentTimeMillis();
                if (finalMatchesList.size() > min_matches)// dev discretion for
                // number of matches to
                // be found for an image
                // to be judged as
                // duplicate
                {
                    text = finalMatchesList.size()
                            + " matches were found. Possible duplicate image.\nTime taken="
                            + (endTime - startTime) + "ms";
                    isDuplicate = true;
                } else {
                    text = finalMatchesList.size()
                            + " matches were found. Images aren't similar.\nTime taken="
                            + (endTime - startTime) + "ms";
                    isDuplicate = false;
                }
                pd.dismiss();
                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                        asyncTaskContext);
                alertDialog.setTitle("Result");
                alertDialog.setCancelable(false);
                LayoutInflater factory = LayoutInflater.from(asyncTaskContext);
                final View view = factory.inflate(R.layout.image_view, null);
                ImageView matchedImages = (ImageView) view
                        .findViewById(R.id.finalImage);
                matchedImages.setImageBitmap(bmp);
                matchedImages.invalidate();
                final CheckBox shouldBeDuplicate = (CheckBox) view
                        .findViewById(R.id.checkBox);
                TextView message = (TextView) view.findViewById(R.id.message);
                message.setText(text);
                alertDialog.setView(view);
                shouldBeDuplicate
                        .setText("These images are actually duplicates.");
                alertDialog.setPositiveButton("Add to logs",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                File logs = new File(Environment
                                        .getExternalStorageDirectory()
                                        .getAbsolutePath()
                                        + "/imageComparator/Data Logs.txt");
                                FileWriter fw;
                                BufferedWriter bw;
                                try {
                                    fw = new FileWriter(logs, true);
                                    bw = new BufferedWriter(fw);
                                    bw.write("Algorithm used: "
                                            + descriptorType
                                            + "\nHamming distance: "
                                            + min_dist + "\nMinimum good matches: " + min_matches
                                            + "\nMatches found: " + matchesFound + "\nTime elapsed: " + (endTime - startTime) + "seconds\n" + firstPath
                                            + " was compared to " + secondPath
                                            + "\n" + "Is actual duplicate: "
                                            + shouldBeDuplicate.isChecked()
                                            + "\nRecognized as duplicate: "
                                            + isDuplicate + "\n");
                                    bw.close();
                                    Toast.makeText(
                                            asyncTaskContext,
                                            "Logs updated.\nLog location: "
                                                    + Environment
                                                    .getExternalStorageDirectory()
                                                    .getAbsolutePath()
                                                    + "/imageComparator/Data Logs.txt",
                                            Toast.LENGTH_LONG).show();
                                } catch (IOException e) {
                                    // TODO Auto-generated catch block
                                    // e.printStackTrace();
                                    try {
                                        File dir = new File(Environment
                                                .getExternalStorageDirectory()
                                                .getAbsolutePath()
                                                + "/imageComparator/");
                                        dir.mkdirs();
                                        logs.createNewFile();
                                        logs = new File(
                                                Environment
                                                        .getExternalStorageDirectory()
                                                        .getAbsolutePath()
                                                        + "/imageComparator/Data Logs.txt");
                                        fw = new FileWriter(logs, true);
                                        bw = new BufferedWriter(fw);
                                        bw.write("Algorithm used: "
                                                + descriptorType
                                                + "\nMinimum distance between keypoints: "
                                                + min_dist + "\n" + firstPath
                                                + " was compared to " + secondPath
                                                + "\n"
                                                + "Is actual duplicate: "
                                                + shouldBeDuplicate.isChecked()
                                                + "\nRecognized as duplicate: "
                                                + isDuplicate + "\n");
                                        bw.close();
                                        Toast.makeText(
                                                asyncTaskContext,
                                                "Logs updated.\nLog location: "
                                                        + Environment
                                                        .getExternalStorageDirectory()
                                                        .getAbsolutePath()
                                                        + "/imageComparator/Data Logs.txt",
                                                Toast.LENGTH_LONG).show();
                                    } catch (IOException e1) {
                                        // TODO Auto-generated catch block
                                        e1.printStackTrace();
                                    }

                                }
                            }
                        });
                alertDialog.show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(asyncTaskContext, e.toString(),
                        Toast.LENGTH_LONG).show();
            }
        }

        private void compare() {
            try {
                bmpimg1 = bmpimg1.copy(Bitmap.Config.ARGB_8888, true);
                bmpimg2 = bmpimg2.copy(Bitmap.Config.ARGB_8888, true);
                img1 = new Mat();
                img2 = new Mat();
                Utils.bitmapToMat(bmpimg1, img1);
                Utils.bitmapToMat(bmpimg2, img2);
                Imgproc.cvtColor(img1, img1, Imgproc.COLOR_BGR2RGB);
                Imgproc.cvtColor(img2, img2, Imgproc.COLOR_BGR2RGB);
                detector = FeatureDetector.create(FeatureDetector.PYRAMID_FAST);
                int descriptor = DescriptorExtractor.FREAK;
                DescExtractor = DescriptorExtractor.create(descriptor);
                matcher = DescriptorMatcher
                        .create(DescriptorMatcher.BRUTEFORCE_HAMMING);

                keypoints = new MatOfKeyPoint();
                dupKeypoints = new MatOfKeyPoint();
                descriptors = new Mat();
                dupDescriptors = new Mat();
                matches = new MatOfDMatch();
                detector.detect(img1, keypoints);
                Log.d("LOG!", "number of query Keypoints= " + keypoints.size());
                detector.detect(img2, dupKeypoints);
                Log.d("LOG!", "number of dup Keypoints= " + dupKeypoints.size());
                // Descript keypoints
                DescExtractor.compute(img1, keypoints, descriptors);
                DescExtractor.compute(img2, dupKeypoints, dupDescriptors);
                Log.d("LOG!", "number of descriptors= " + descriptors.size());
                Log.d("LOG!",
                        "number of dupDescriptors= " + dupDescriptors.size());
                // matching descriptors
                matcher.match(descriptors, dupDescriptors, matches);
                Log.d("LOG!", "Matches Size " + matches.size());
                // New method of finding best matches
                List<DMatch> matchesList = matches.toList();
                List<DMatch> matches_final = new ArrayList<DMatch>();
                for (int i = 0; i < matchesList.size(); i++) {
                    if (matchesList.get(i).distance <= min_dist) {
                        matches_final.add(matches.toList().get(i));
                    }
                }

                matches_final_mat = new MatOfDMatch();
                matches_final_mat.fromList(matches_final);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compare);
        init();
        setupDefaults();
        setupEvents();
    }

    private void init() {
        btnAddFirstImage = findViewById(R.id.btn_add_first_image);
        btnAddSecondImage = findViewById(R.id.btn_add_second_image);
        btnComapre = findViewById(R.id.btn_compare);
    }

    private void setupDefaults() {

    }

    private void setupEvents() {
        btnAddFirstImage.setOnClickListener(view -> {
            photoSaveName = "photoOne";
            onCreateDialogSingleChoice();
        });

        btnAddSecondImage.setOnClickListener(view -> {
            photoSaveName = "photoSecond";
            onCreateDialogSingleChoice();
        });

        btnComapre.setOnClickListener(view ->{
            if(!TextUtils.isNullOrEmpty(firstPath) && !TextUtils.isNullOrEmpty(secondPath)){
                compareTwoImages();
            }else{
                Toast.makeText(CompareActivity.this,"Please add two images then compare",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void compareTwoImages() {
        if (bmpimg1 != null && bmpimg2 != null) {

            bmpimg1 = Bitmap.createScaledBitmap(bmpimg1, 100, 100, true);
            bmpimg2 = Bitmap.createScaledBitmap(bmpimg2, 100, 100, true);
            Mat img1 = new Mat();
            Utils.bitmapToMat(bmpimg1, img1);
            Mat img2 = new Mat();
            Utils.bitmapToMat(bmpimg2, img2);
            Imgproc.cvtColor(img1, img1, Imgproc.COLOR_RGBA2GRAY);
            Imgproc.cvtColor(img2, img2, Imgproc.COLOR_RGBA2GRAY);
            img1.convertTo(img1, CvType.CV_32F);
            img2.convertTo(img2, CvType.CV_32F);
            //Log.d("ImageComparator", "img1:"+img1.rows()+"x"+img1.cols()+" img2:"+img2.rows()+"x"+img2.cols());
            Mat hist1 = new Mat();
            Mat hist2 = new Mat();
            MatOfInt histSize = new MatOfInt(180);
            MatOfInt channels = new MatOfInt(0);
            ArrayList<Mat> bgr_planes1= new ArrayList<Mat>();
            ArrayList<Mat> bgr_planes2= new ArrayList<Mat>();
            Core.split(img1, bgr_planes1);
            Core.split(img2, bgr_planes2);
            MatOfFloat histRanges = new MatOfFloat (0f, 180f);
            boolean accumulate = false;
            Imgproc.calcHist(bgr_planes1, channels, new Mat(), hist1, histSize, histRanges, accumulate);
            Core.normalize(hist1, hist1, 0, hist1.rows(), Core.NORM_MINMAX, -1, new Mat());
            Imgproc.calcHist(bgr_planes2, channels, new Mat(), hist2, histSize, histRanges, accumulate);
            Core.normalize(hist2, hist2, 0, hist2.rows(), Core.NORM_MINMAX, -1, new Mat());
            img1.convertTo(img1, CvType.CV_32F);
            img2.convertTo(img2, CvType.CV_32F);
            hist1.convertTo(hist1, CvType.CV_32F);
            hist2.convertTo(hist2, CvType.CV_32F);

            double compare = Imgproc.compareHist(hist1, hist2, Imgproc.CV_COMP_CHISQR);

            if(compare>0 && compare<1500) {
                Toast.makeText(CompareActivity.this, "Images may be possible duplicates, verifying", Toast.LENGTH_LONG).show();
                new asyncTask(CompareActivity.this).execute();
            }
            else if(compare == 0)
                Toast.makeText(CompareActivity.this, "Images are exact duplicates", Toast.LENGTH_LONG).show();
            else
                Toast.makeText(CompareActivity.this, "Images are not duplicates", Toast.LENGTH_LONG).show();

            startTime = System.currentTimeMillis();
        } else
            Toast.makeText(CompareActivity.this,
                    "You haven't selected images.", Toast.LENGTH_LONG)
                    .show();
    }


    private void onCreateDialogSingleChoice() {
        AlertDialog.Builder builder = new AlertDialog.Builder(CompareActivity.this);
        String[] listItem = {"Camera", "Gallery"};
        builder.setTitle("Select One").setItems(listItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    isPermissionDefaults = true;
                    currentlyCliked = "camera";
                    String permission = Manifest.permission.CAMERA;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (ContextCompat.checkSelfPermission(CompareActivity.this, permission) == PackageManager.PERMISSION_GRANTED) {
                            loadImgFromCamera();
                        } else {
                            checkCameraPermision();
                        }
                    } else {
                        loadImgFromCamera();
                    }
                } else if (which == 1) {
                    isPermissionDefaults = true;
                    currentlyCliked = "gallery";
                    String permission = Manifest.permission.READ_EXTERNAL_STORAGE;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (ContextCompat.checkSelfPermission(CompareActivity.this, permission) == PackageManager.PERMISSION_GRANTED) {
                            launchGallery();
                        } else {
                            checkSDCardPermission();
                        }
                    } else {
                        launchGallery();
                    }
                }
                dialog.dismiss();
            }
        }).create().show();
    }


    private void launchGallery() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, SELECT_IMAGE);
    }


    private void loadImgFromCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri picUri = FileProvider.getUriForFile(CompareActivity.this, BuildConfig.APPLICATION_ID + ".provider", FileUtils.createImageProfile(CompareActivity.this, photoSaveName));
        intent.putExtra(MediaStore.EXTRA_OUTPUT, picUri);
        List<ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            grantUriPermission(packageName, picUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        startActivityForResult(intent, CAMERA_RESULT);
    }


    public void checkCameraPermision() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if ((checkSelfPermission(PERMISSIONS[0])) != 0) {
                if (isPermissionDefaults)
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, 2);
                else
                    requestPermissions(PERMISSIONS, 2);
            }
        }
    }

    private void checkSDCardPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(CompareActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (isPermissionDefaults) {
                        launchGallery();
                    }
                } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(CompareActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    } else {
                        if (isPermissionDefaults) {
                            if (!ActivityCompat.shouldShowRequestPermissionRationale(CompareActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                                promptSettings("sdCard");
                            } /*else {
                                    promptSettings("sdCard");
                                }*/
                        }
                    }
                }
                break;
            case 2:

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (isPermissionDefaults) {
                        loadImgFromCamera();
                    }
                } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(CompareActivity.this, Manifest.permission.CAMERA)) {

                    } else {
                        if (isPermissionDefaults) {
                            if (!ActivityCompat.shouldShowRequestPermissionRationale(CompareActivity.this, Manifest.permission.CAMERA)) {
                                promptSettings("Camera");
                            } /*else {
                                    promptSettings("Camera");
                                }*/
                        }
                    }
                }

                break;

        }
    }


    public void promptSettings(String type) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(CompareActivity.this);
        builder.setTitle(String.format(getResources().getString(R.string.denied_never_ask_title), type));
        builder.setMessage(String.format(getString(R.string.denied_never_ask_msg), type));
        builder.setPositiveButton("go to Settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                goToSettings();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }


    public void goToSettings() {
        Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + CompareActivity.this.getPackageName()));
        myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
        myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(myAppSettings);
    }

}









