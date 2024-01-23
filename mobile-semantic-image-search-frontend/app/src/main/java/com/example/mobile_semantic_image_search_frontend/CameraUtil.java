package com.example.mobile_semantic_image_search_frontend;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import kotlin.jvm.internal.Intrinsics;

public final class CameraUtil {
    public static final int REQUEST_IMAGE_CAPTURE = 123;
    @Nullable
    private static File photoFile;
    @Nullable
    private static String currentPhotoPath;

    @Nullable
    public static final File getPhotoFile() {
        return photoFile;
    }

    public static final void setPhotoFile(@Nullable File var0) {
        photoFile = var0;
    }

    @Nullable
    public static final String getCurrentPhotoPath() {
        return currentPhotoPath;
    }

    public static final void setCurrentPhotoPath(@Nullable String var0) {
        currentPhotoPath = var0;
    }

    public static final void dispatchTakePictureIntent(@NotNull Activity activity) {
        Intrinsics.checkNotNullParameter(activity, "activity");
        Intent takePictureIntent = new Intent("android.media.action.IMAGE_CAPTURE");
        if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
            try {
                File tempPhotoFile = createImageFile();
                photoFile = tempPhotoFile;
                Uri photoURI = ContentUriProvider.getUriForFile((Context)activity, "com.example.mobile_semantic_image_search_frontend.fileprovider", tempPhotoFile);
                takePictureIntent.putExtra("output", (Parcelable)photoURI);
                ActivityCompat.startActivityForResult(activity, takePictureIntent, 123, (Bundle)null);
            } catch (IOException var4) {
                Log.e("ERROR", var4.toString());
            }
        }

    }

    @NotNull
    public static final File createImageFile() throws IOException {
        String var10000 = (new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())).format(new Date());
        Intrinsics.checkNotNullExpressionValue(var10000, "SimpleDateFormat(\"yyyyMM…Default()).format(Date())");
        String timeStamp = var10000;
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File var2 = File.createTempFile("JPEG_" + timeStamp + '_', ".jpg", storageDir);
        boolean var4 = false;
        currentPhotoPath = var2.getAbsolutePath();
        Intrinsics.checkNotNullExpressionValue(var2, "File.createTempFile(\n   …Path = absolutePath\n    }");
        return var2;
    }
}