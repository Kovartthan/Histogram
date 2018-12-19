package com.ranjith.histogram.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtils {

    private static final int IO_BUFFER_SIZE = 4 * 1024;
    private static final String TEMP_DIR = "temp";
    private static final String TEMP_FILE_NAME = "shared_video.mp4";
    private static final String TEMP_IMAGE_FILE = "shared_image.png";
    private static final String TAG = "FileUtils";


    public static File createNewTempProfileFile(Context context, String type) {
        File dir = new File(context.getExternalCacheDir(), type);
        dir.mkdir();
        File f = new File(dir, "tmp_" + type + ".png");
        return f;
    }


    public static void copyStream(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
        output.flush();
        output.close();
        input.close();
    }

    public static File createImageProfile(Context context, String type) {
        File mapdir = null;
        File f = null;
        mapdir= new File(context.getExternalCacheDir(), "folder");
        mapdir.mkdir();
        f   = new File(mapdir + File.separator, type);
        return f;
    }



    public static Bitmap createBitMap(String path) {
        File file = new File(path);
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        return bitmap;
    }
    public void  deleteFile(File f) {
        if(f.exists()){
            f.delete();
        }
    }


    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        // The directory is now empty so delete it
        return dir.delete();
    }

    public static double getFileSizeinMb(File file) {
        if (file.exists()) {

            double bytes = file.length();
            double kilobytes = (bytes / 1024);
            double megabytes = (kilobytes / 1024);
            double gigabytes = (megabytes / 1024);
            double terabytes = (gigabytes / 1024);
            double petabytes = (terabytes / 1024);
            double exabytes = (petabytes / 1024);
            double zettabytes = (exabytes / 1024);
            double yottabytes = (zettabytes / 1024);

            return megabytes;
        } else {
        }
        return 0l;
    }



    public static String getExtension(String file) {
        int extensionStartIndex = file.lastIndexOf(".");
        int fileNameStartIndex = file.lastIndexOf(File.separator);
        if (extensionStartIndex != -1 && extensionStartIndex > fileNameStartIndex) {
            return file.substring(file.lastIndexOf("."));
        }
        return "";
    }


    public static void delete(File file) {
        if (file == null) {
            return;
        }

        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                delete(f);
            }
        }

        if (file.exists()) {
            file.delete();
        }
    }






    public static File saveBitmapToFile(Context context, Bitmap bm) {

        File dir = new File(context.getExternalCacheDir(), "default");
        dir.mkdir();
        File imageFile = new File(dir, "default" + ".png");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(imageFile);

            bm.compress(Bitmap.CompressFormat.PNG, 60, fos);

            fos.close();

            return imageFile;
        } catch (IOException e) {
            Log.e("app", e.getMessage());
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return null;
    }




}
