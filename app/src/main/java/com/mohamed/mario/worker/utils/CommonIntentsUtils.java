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

/**
 * Created by Mohamed on 6/23/2018.
 */
public class CommonIntentsUtils {
    public static final int REQUSER_CODE_Gallery = 15;
    public static final int REQUSER_CODE_CAMERA = 16;
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
     * @param fragment           instance of fragment to call start activity for result.
     * @param intentChooserTitle if not null we will make intent chooser isa,
     *                           else we will launch immediately.
     * @param requestCode        request code for activity result.
     */
    public static void getImageFromGallery(Activity activityCompat,
                                           String intentChooserTitle, int requestCode) {
        Context context = activityCompat.getBaseContext();

        if (context != null) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                if (intentChooserTitle != null) {
                    activityCompat.startActivityForResult(
                            Intent.createChooser(intent, intentChooserTitle), requestCode);
                } else {
                    activityCompat.startActivityForResult(intent, requestCode);
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
     * @param fragment           instance of fragment to call start activity for result.
     * @param intentChooserTitle if not null we will make intent chooser isa,
     *                           else we will launch immediately.
     * @param extraOutput        uri to put full-sized photo in it.
     * @param requestCode        requestCode for activity result.
     */
    public static void getImageFromCamera(Activity activityCompat, String intentChooserTitle,
                                          Uri extraOutput, int requestCode) {
        Context context = activityCompat.getBaseContext();

        if (context != null) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, extraOutput);
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                if (intentChooserTitle != null) {
                    activityCompat.startActivityForResult(
                            Intent.createChooser(intent, intentChooserTitle), requestCode);
                } else {
                    activityCompat.startActivityForResult(intent, requestCode);
                }
            }
        }
    }
    public static void openCameraIntent(Activity activity) {
        Context context=activity.getBaseContext();
        Intent pictureIntent = new Intent(
                MediaStore.ACTION_IMAGE_CAPTURE
        );
        if(pictureIntent.resolveActivity(context.getPackageManager()) != null) {
            activity.  startActivityForResult(pictureIntent,
                    REQUSER_CODE_CAMERA);
        }
    }


    public static File createImageFile(Activity activity) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
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
