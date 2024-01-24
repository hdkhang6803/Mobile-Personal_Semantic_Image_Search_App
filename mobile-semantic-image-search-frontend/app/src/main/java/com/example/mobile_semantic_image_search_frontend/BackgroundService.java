package com.example.mobile_semantic_image_search_frontend;

import android.app.Service;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import android.util.Log;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import android.net.Uri;

import com.google.firebase.auth.FirebaseAuth;

public  class BackgroundService extends Service {
    private static final String TAG = "BackgroundService";
    private static final String SERVER_IP = "http://164.92.122.168:5000/update_index"; // Replace with your server IP

    private static final int MAX_RETRY_COUNT = 3;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;

    private ImageUploadTask imageUploadTask;
    public static final String ACTION_UPDATE_PROGRESS = "com.example.mobile_semantic_image_search_frontend.ACTION_UPDATE_PROGRESS";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Your background task goes here
        // For example, displaying a Toast message
//        Toast.makeText(this, "Background Service is running", Toast.LENGTH_SHORT).show();


        mAuth = FirebaseAuth.getInstance();

        imageUploadTask = new ImageUploadTask();
        imageUploadTask.execute();

        // If you want the service to continue running until explicitly stopped
        return START_STICKY;
    }


    private class ImageUploadTask extends AsyncTask<Void, Void, Boolean> {



        @Override
        protected Boolean doInBackground(Void... voids) {
//            // Gather all images from a specific directory
//            ArrayList<File> imageFiles = getImagesFromDirectory("/path/to/your/images");

            // Gather all images from the media library
            ArrayList<File> imageFiles = getImagesFromMediaLibrary(BackgroundService.this.getApplicationContext());

            Log.d(TAG, " length: " + imageFiles.size());

            // Process images in batches of 5
            int batchSize = 3;
            int maxThreads = 5; // Set the maximum number of threads
            AtomicInteger failedCount = new AtomicInteger();

            // Use a CountDownLatch to wait for each batch of images to be processed
            CountDownLatch latch = new CountDownLatch(imageFiles.size() / batchSize);

            // Use a ThreadPoolExecutor with a fixed number of threads
            Executor executor = new ThreadPoolExecutor(maxThreads, maxThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());

            // Process images in batches of 5
            for (int i = 0; i < imageFiles.size(); i += batchSize) {
                Log.d(TAG, " i: " + i);
                int endIndex = Math.min(i + batchSize, imageFiles.size());
                ArrayList<File> batch = new ArrayList<>(imageFiles.subList(i, endIndex));

//                Executor executor = Executors.newFixedThreadPool(5);
                int finalI = i;
                executor.execute(() -> {
                    // Make a POST request with the gathered batch of images
                    if (batch != null && !batch.isEmpty()) {
                        try {
//                        postImagesBatchToServer(batch);
                            updateProgress(finalI * 100 / imageFiles.size());
                            Log.d(TAG, " sending batch i " + finalI);
                            boolean result = postImagesBatchToServerWithRetry(batch);
                            if (!result) {
                                failedCount.getAndIncrement();
                            }
                            latch.countDown();
                            Log.d(TAG, " processed " + (finalI + batchSize) );
                            Log.d(TAG, " failed count " + failedCount.get() * batchSize);

                        } catch (IOException e) {
                            Log.e(TAG, "Error making POST request with retry: " + e.getMessage());
//                            return false;
                        }
                    }
                });
            }

            // Wait for all batches to be processed before returning
            try {
                latch.await();
            } catch (InterruptedException e) {
                Log.e(TAG, "Error waiting for image batches to be processed: " + e.getMessage());
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            // Update UI or perform additional actions based on the result
            if (success) {
                Toast.makeText(BackgroundService.this, "Images uploaded successfully.", Toast.LENGTH_SHORT).show();
                progressFinishCallback.onFinish();

            } else {
                Toast.makeText(BackgroundService.this, "Failed to upload images.", Toast.LENGTH_SHORT).show();
                progressFinishCallback.onFinish();

            }
            imageUploadTask.cancel(true);
            stopSelf();

        }

        private ArrayList<File> getImagesFromDirectory(String directoryPath) {
            // Implement logic to gather all image files from the specified directory
            // For simplicity, you can assume that the images are stored in the app's internal storage

            // Replace this with your actual implementation
            // Here, we are assuming that the images are in the app's internal storage
            ArrayList<File> imageFiles = new ArrayList<>();
            File directory = new File(getFilesDir(), directoryPath);

            if (directory.exists() && directory.isDirectory()) {
                File[] files = directory.listFiles();

                if (files != null) {
                    for (File file : files) {
                        if (file.isFile() && file.getName().toLowerCase().endsWith(".jpg")) {
                            imageFiles.add(file);
                        }
                    }
                }
            }

            return imageFiles;
        }

        private void postImagesToServer(ArrayList<File> imageFiles) throws IOException {
            OkHttpClient client = new OkHttpClient();
            MediaType MEDIA_TYPE_JPG = MediaType.parse("image/jpeg");

            for (File imageFile : imageFiles) {
                Log.d(TAG, " one image " + imageFile.getPath());
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", imageFile.getPath(), RequestBody.create(MEDIA_TYPE_JPG, imageFile))
                        .build();

                Request request = new Request.Builder()
                        .url(SERVER_IP)
                        .post(requestBody)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    }
                    // You can handle the server response here if needed
                }
            }
        }

        private boolean postImagesBatchToServerWithRetry(ArrayList<File> imageFiles) throws IOException {
            int retryCount = 0;

            while (retryCount < MAX_RETRY_COUNT) {
                try {
                    postImagesBatchToServer(imageFiles);
                    return true; // Success, exit the loop
                } catch (IOException e) {
                    Log.e(TAG, "Error making POST request: " + e.getMessage());
                    retryCount++;
                    Log.d(TAG, "Retrying... Attempt " + retryCount);
                }
            }

            Log.e(TAG, "Maximum retry attempts reached. Failed to post images to the server.");
            return false;
        }

        private void postImagesBatchToServer(ArrayList<File> imageFiles) throws IOException {
            OkHttpClient client = new OkHttpClient();
            MediaType MEDIA_TYPE_JPG = MediaType.parse("image/jpeg");

            MultipartBody.Builder multipartBuilder = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM);

            String userId = mAuth.getCurrentUser().getUid();

            Log.d(TAG, " userId: " + userId);

            multipartBuilder.addFormDataPart("userId", userId, RequestBody.create(MediaType.parse("text/plain"), userId));
            for (File imageFile : imageFiles) {
                Log.d(TAG, " one image " + imageFile.getPath());
                RequestBody requestBody = RequestBody.create(MEDIA_TYPE_JPG, imageFile);
                multipartBuilder.addFormDataPart("files[]", imageFile.getPath(), requestBody);
            }

            RequestBody finalRequestBody = multipartBuilder.build();

            Request request = new Request.Builder()
                    .url(SERVER_IP)
                    .post(finalRequestBody)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }
                else {
                    Log.d(TAG, "Response: " + response.body().string());
                }
                // You can handle the server response here if needed
            }
        }

        private ArrayList<File> getImagesFromMediaLibrary(Context context) {
            ArrayList<File> imageFiles = new ArrayList<>();

//            String[] projection = {MediaStore.Images.Media.DATA};

            String[] projection = new String[] {
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.DURATION,
                    MediaStore.Images.Media.RELATIVE_PATH,
                    MediaStore.Images.Media.DATA,
            };

            Cursor cursor = context.getContentResolver().query(
                    MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL),
//                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    null,
                    null,
                    null
            );

            if (cursor != null) {
//                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                int nameColumn =
                        cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
                int durationColumn =
                        cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DURATION);
                int pathColumn = cursor.getColumnIndex(MediaStore.Images.Media.RELATIVE_PATH);
                int dataColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATA);

                while (cursor.moveToNext()) {
//                    String filePath = cursor.getString(columnIndex);

                    // Get values of columns for a given video.
                    long id = cursor.getLong(idColumn);
                    String name = cursor.getString(nameColumn);
                    int duration = cursor.getInt(durationColumn);
                    String filePath = cursor.getString(pathColumn);
                    String data = cursor.getString(dataColumn);

                    Uri contentUri = ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

                    Log.d(TAG, Objects.requireNonNull(contentUri.getPath()));

                    Log.d(TAG, data);

                    Log.d(TAG, " id: " + id + " name: " + name + " duration: " + duration + " path: " + filePath);

                    // Compress the image
//                    Bitmap originalBitmap = BitmapFactory.decodeFile(data);
//                    Bitmap compressedBitmap = compressBitmap(originalBitmap);

                    // Save the compressed bitmap to a new file
//                    File compressedImageFile = saveBitmapToFile(compressedBitmap, name);

                    imageFiles.add(new File(data));
//                    imageFiles.add(compressedImageFile);
                }
                cursor.close();
            }

            return imageFiles;
        }
    }

    private Bitmap compressBitmap(Bitmap originalBitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        originalBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        return BitmapFactory.decodeStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
    }

    private File saveBitmapToFile(Bitmap bitmap, String fileName) {
        File file = new File(getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName);
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }



    private ProgressCallback progressCallback;
    public interface ProgressCallback {
        void onProgressUpdate(int progress);
    }
    public void setProgressCallback(ProgressCallback callback) {
        this.progressCallback = callback;
    }

    private ProgressFinishCallback progressFinishCallback;
    public interface ProgressFinishCallback {
        void onFinish();
    }
    public void setProgressFinishCallback(ProgressFinishCallback callback) {
        this.progressFinishCallback = callback;
    }

    private final IBinder binder = new MyBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class MyBinder extends Binder {
        BackgroundService getService() {
            return BackgroundService.this;
        }
    }

    private void updateProgress(int progress) {
//        Intent intent = new Intent( "com.example.mobile_semantic_image_search_frontend.ACTION_UPDATE_PROGRESS");
//        intent.putExtra("progress", progress);

////        progressBar.setProgress(progress);
//        sendBroadcast(intent);
        if (progressCallback != null) {
            Log.e("Sendprogess", "progress: " + progress);
            progressCallback.onProgressUpdate(progress);
        }else {
            Log.e("Sendprogess", "progressCallback is null");
        }
    }
}