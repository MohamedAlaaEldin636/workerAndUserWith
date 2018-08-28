package com.mohamed.mario.worker.utils;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Mohamed on 6/23/2018.
 *
 */
public class CommonIntentsUtils {

    public static final int REQUEST_CODE_GALLERY = 15;
    public static final int REQUEST_CODE_CAMERA = 16;
    public static  int IMAGE_ID = 0;
    public static  int Txt_ID = 0;
    public static View layout ;
    public static String mCurrentPhotoPath;

    /**
     * Official link for example on how to use it is in below link.
     * https://developer.android.com/guide/components/intents-common#ImageCapture
     * <p>
     * to handle Intent result in onActivityResult() method
     * Bitmap thumbnail = intent.getParcelable("data");
     * Uri fullPhotoUri = intent.getData();
     *
     * @param activity           instance of activity to call start activity for result.
     * @param intentChooserTitle if not null we will make intent chooser isa,
     *                           else we will launch immediately.
     * @param requestCode        request code for activity result.
     */
    public static void getImageFromGallery(Activity activity,
                                           String intentChooserTitle,
                                           int requestCode) {
        Context context = activity.getBaseContext();

        if (context != null) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                if (intentChooserTitle != null) {
                    activity.startActivityForResult(
                            Intent.createChooser(intent, intentChooserTitle), requestCode);
                } else {
                    activity.startActivityForResult(intent, requestCode);
                }
            }
        }
    }

    /**
     * Official link for example on how to use it is in below link.
     * https://developer.android.com/training/camera/photobasics
     * <p>
     * to handle Intent result in onActivityResult() method
     * 1- to get a thumbnail ( minimized image )
     * Bundle extras = data.getExtras();
     * Bitmap imageBitmap = (Bitmap) extras.get("data");
     * mImageView.setImageBitmap(imageBitmap);
     * ## Note -> This thumbnail image from "data" might be good for an icon, but not a lot more.
     * Dealing with a full-sized image takes a bit more work.
     * 2- to get the full-size photo
     * it will be isa in the provided extraOutput Uri param.
     *
     * @param activity           instance of activity to call start activity for result.
     * @param intentChooserTitle if not null we will make intent chooser isa,
     *                           else we will launch immediately.
     * @param extraOutput        uri to put full-sized photo in it.
     * @param requestCode        requestCode for activity result.
     */
    public static void getImageFromCamera(Activity activity,
                                          String intentChooserTitle,
                                          Uri extraOutput,
                                          int requestCode) {
        Context context = activity.getBaseContext();

        if (context != null) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, extraOutput);
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                if (intentChooserTitle != null) {
                    activity.startActivityForResult(
                            Intent.createChooser(intent, intentChooserTitle), requestCode);
                } else {
                    activity.startActivityForResult(intent, requestCode);
                }
            }
        }
    }

    public static File createImageFile(Activity activity) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

}
