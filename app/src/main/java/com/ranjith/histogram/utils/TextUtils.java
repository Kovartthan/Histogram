package com.ranjith.histogram.utils;

import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;


public class TextUtils {

    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    public static int generateViewId() {
        for (;;) {
            final int result = sNextGeneratedId.get();
            // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
            int newValue = result + 1;
            if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to intro_icon1, not 0.
            if (sNextGeneratedId.compareAndSet(result, newValue)) {
                return result;
            }
        }
    }
    private static final String REGX_HASHTAG = "[`@!\\&×\\÷~#\\-\\+=\\[\\]{}\\^()<>/;:,.?'|\"\\*%$\\s+\\\\"
            + "•??£¢€°™®©¶¥??????????¿¡??¤??]"; // #$%^*()+=\-\[\]\';,.\/{}|":<>?~\\\\
    public static Pattern PATTERN_HASHTAG;

    static {
        PATTERN_HASHTAG = Pattern.compile(REGX_HASHTAG);
    }

    public static String fromHtml(String string) {
        Spanned result;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml(string,Html.FROM_HTML_MODE_LEGACY);
        } else {
            result = Html.fromHtml(string);
        }

        return String.valueOf(result);
    }

    public static String toHtml(String string) {
        String result;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.toHtml(new SpannableString(string));
        } else {
            result = Html.toHtml(new SpannableString(string));
        }

        return result;
    }
    public static final String encodeToBase64(CharSequence content) {
        if (content == null) {
            return null;
        }
        byte[] bytes = Base64.encode(content.toString().getBytes(), Base64.DEFAULT);
        return new String(bytes).trim();
    }

    public static final String encodeToBase64(byte[] data) {
        byte[] bytes = Base64.encode(data, Base64.DEFAULT);
        return new String(bytes).trim();
    }

    public static final String encodeToBase64(String content){
        if (content == null) {
            return null;
        }
        byte[] data = new byte[0];
        String base64 ="";

      //  base64 = Base64.encodeToString(content.getBytes(), Base64.DEFAULT);
        try {
            data = content.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        base64 = Base64.encodeToString(data, Base64.NO_WRAP);
        return base64.replaceAll("%0A","");
    }

    public static final String encodeToBase64URLSafe(String content){
        if (content == null) {
            return null;
        }
        byte[] data = new byte[0];
        String base64 ="";
        try {
            data = content.getBytes("UTF-8");
            base64 =  Base64.encodeToString(data, Base64.NO_WRAP);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return base64.replaceAll("%0A","");
    }

    public static final String decodeBase64(String base64String) {
        if (base64String == null) {
            return base64String;
        }

        try {
            return new String(Base64.decode(base64String, Base64.DEFAULT));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return base64String;
    }




    public static boolean isValidEmail(CharSequence target) {
        if (isNullOrEmpty(target)) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

    public static boolean isValidWebUrl(CharSequence target) {
        if (target == null) {
            return false;
        } else {
            return android.util.Patterns.WEB_URL.matcher(target).matches();
        }
    }



    public static boolean isNullOrEmpty(String value) {
        return value == null || value.trim().equals("")|| value.trim().equals("null");
    }

    public static boolean isNullOrEmpty(CharSequence value) {
        return value == null || value.toString().equals("");
    }

    public static String arrayToString(ArrayList<String> array, String delimiter) {
        StringBuilder builder = new StringBuilder();
        if (array.size() > 0) {
            builder.append(array.get(0));
            for (int i = 1; i < array.size(); i++) {
                builder.append(delimiter);
                builder.append(array.get(i));
            }
        }
        return builder.toString();
    }

    public static ArrayList<String> stringToArray(String string) {
        return new ArrayList<>(Arrays.asList(string.split(",")));
    }

    public static String integerArrayToString(ArrayList<Integer> array, String delimiter) {
        StringBuilder builder = new StringBuilder();
        if (array.size() > 0) {
            builder.append(array.get(0));
            for (int i = 1; i < array.size(); i++) {
                builder.append(delimiter);
                builder.append(array.get(i));
            }
        }
        return builder.toString();
    }

    public static String capitalizeFirstLetter(String original) {
        if (original.length() == 0)
            return original;
        return original.substring(0, 1).toUpperCase() + original.substring(1);
    }
    public static String capitalizeEachWord(String string) {
        char[] chars = string.toLowerCase().toCharArray();
        boolean found = false;
        for (int i = 0; i < chars.length; i++) {
            if (!found && Character.isLetter(chars[i])) {
                chars[i] = Character.toUpperCase(chars[i]);
                found = true;
            } else if (Character.isWhitespace(chars[i]) || chars[i]=='.' || chars[i]=='\'') {
                found = false;
            }
        }
        return String.valueOf(chars);
    }





}
