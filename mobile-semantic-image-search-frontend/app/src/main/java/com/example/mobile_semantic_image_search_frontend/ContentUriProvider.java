package com.example.mobile_semantic_image_search_frontend;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

public class ContentUriProvider {

    private static final String HUAWEI_MANUFACTURER = "Xiaomi";

    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!sourceFile.exists()) {
            throw new IllegalArgumentException("Source file does not exist: " + sourceFile);
        }

        try (FileInputStream fis = new FileInputStream(sourceFile);
             FileOutputStream fos = new FileOutputStream(destFile);
             FileChannel sourceChannel = fis.getChannel();
             FileChannel destChannel = fos.getChannel()) {

            // Transfer data from source channel to destination channel
            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
        }
    }

    public static Uri getUriForFile(@NonNull Context context, @NonNull String authority, @NonNull File file) {
        if (HUAWEI_MANUFACTURER.equalsIgnoreCase(Build.MANUFACTURER)) {
            Log.w(ContentUriProvider.class.getSimpleName(), "Using a Huawei device Increased likelihood of failure...");
            try {
                return FileProvider.getUriForFile(context, authority, file);
            } catch (IllegalArgumentException e) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                    Log.w(ContentUriProvider.class.getSimpleName(), "Returning Uri.fromFile to avoid Huawei 'external-files-path' bug for pre-N devices", e);
                    return Uri.fromFile(file);
                } else {
                    Log.w(ContentUriProvider.class.getSimpleName(), "ANR Risk -- Copying the file the location cache to avoid Huawei 'external-files-path' bug for N+ devices", e);
                    // Note: Periodically clear this cache
                    final File cacheFolder = new File(context.getCacheDir(), HUAWEI_MANUFACTURER);
                    final File cacheLocation = new File(cacheFolder, file.getName());

                    if (!cacheFolder.exists()) {
                        cacheFolder.mkdirs();
                    }

                    // Create the file if it doesn't exist
                    if (!cacheLocation.exists()) {
                        try {
                            cacheLocation.createNewFile();
                        } catch (IOException e4) {
                            e.printStackTrace(); // Handle the exception according to your needs
                            Log.e("IOException", "Exception in creating file: " + e4.getMessage());
                        }
                    }


                    InputStream in = null;
                    OutputStream out = null;
                    try {
                        copyFile(file, cacheLocation);
//                        in = new FileInputStream(file);
//                        out = new FileOutputStream(cacheLocation); // appending output stream
////                        IOUtils.copy(in, out);
//
//                        byte[] buffer = new byte[1024];
//                        int length;
//
//                        while ((length = in.read(buffer)) > 0) {
//                            out.write(buffer, 0, length);
//                        }

                        Log.i(ContentUriProvider.class.getSimpleName(), "Completed Android N+ Huawei file copy. Attempting to return the cached file");
                        return FileProvider.getUriForFile(context, authority, cacheLocation);
                    } catch (IOException e1) {
                        Log.e(ContentUriProvider.class.getSimpleName(), "Failed to copy the Huawei file. Re-throwing exception", e1);
                        throw new IllegalArgumentException("Huawei devices are unsupported for Android N", e1);
                    } finally {
                        // Close streams in a finally block to ensure they are always closed
//                        if (in != null) {
//                            try {
//                                in.close();
//                            } catch (IOException e2) {
//                                e.printStackTrace();
//                            }
//                        }
//
//                        if (out != null) {
//                            try {
//                                out.close();
//                            } catch (IOException e2) {
//                                e.printStackTrace();
//                            }
//                        }
                    }
                }
            }
        } else {
            return FileProvider.getUriForFile(context, authority, file);
        }
    }

}
