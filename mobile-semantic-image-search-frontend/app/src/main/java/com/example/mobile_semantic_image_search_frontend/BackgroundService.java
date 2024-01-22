package com.example.mobile_semantic_image_search_frontend;

import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.IBinder;
import android.provider.MediaStore;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import android.util.Log;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import android.net.Uri;

import com.google.firebase.auth.FirebaseAuth;

public class BackgroundService extends Service {
    private static final String TAG = "BackgroundService";
    private static final String SERVER_IP = "http://164.92.122.168:5000/update_index"; // Replace with your server IP

    private FirebaseAuth mAuth;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Your background task goes here
        // For example, displaying a Toast message
//        Toast.makeText(this, "Background Service is running", Toast.LENGTH_SHORT).show();

        mAuth = FirebaseAuth.getInstance();
        new ImageUploadTask().execute();

        // If you want the service to continue running until explicitly stopped
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Not used for this example
        return null;
    }

    private class ImageUploadTask extends AsyncTask<Void, Void, Boolean> {



        @Override
        protected Boolean doInBackground(Void... voids) {
//            // Gather all images from a specific directory
//            ArrayList<File> imageFiles = getImagesFromDirectory("/path/to/your/images");

            // Gather all images from the media library
            ArrayList<File> imageFiles = getImagesFromMediaLibrary(BackgroundService.this.getApplicationContext());
            // get only first 5 images
            imageFiles = new ArrayList<>(imageFiles.subList(0, 5));

            Log.d(TAG, " length: " + imageFiles.size());

            // Make a POST request with the gathered images
            if (imageFiles != null && !imageFiles.isEmpty()) {
                try {
//                    postImagesToServer(imageFiles);
                    postImagesBatchToServer(imageFiles);
                    return true;
                } catch (IOException e) {
                    Log.e(TAG, "Error making POST request: " + e.getMessage());
                    return false;
                }
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            // Update UI or perform additional actions based on the result
            if (success) {
                Toast.makeText(BackgroundService.this, "Images uploaded successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(BackgroundService.this, "Failed to upload images", Toast.LENGTH_SHORT).show();
            }
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

        private void postImagesBatchToServer(ArrayList<File> imageFiles) throws IOException {
            OkHttpClient client = new OkHttpClient();
            MediaType MEDIA_TYPE_JPG = MediaType.parse("image/jpeg");

            MultipartBody.Builder multipartBuilder = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM);

            String userId = mAuth.getCurrentUser().getUid();

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

                    imageFiles.add(new File(data));
                }
                cursor.close();
            }


            // new

//            ContentResolver contentResolver = context.getContentResolver();
//            Uri queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
//
//            String selection = MediaStore.Images.Media.MIME_TYPE + " LIKE 'image/%'";
//            String[] projection = {
//                    MediaStore.Images.Media._ID,
//                    MediaStore.Images.Media.DATA,
//                    MediaStore.Images.Media.DISPLAY_NAME,
//                    MediaStore.Images.Media.DATE_TAKEN,
//                    MediaStore.Images.Media.SIZE,
//                    MediaStore.Images.Media.WIDTH,
//                    MediaStore.Images.Media.HEIGHT
//            };
//            String sortOrder = MediaStore.Images.Media.DATE_TAKEN + " DESC";
//
//            Cursor cursor = null;
//            try {
//                cursor = contentResolver.query(queryUri, projection, selection, null, sortOrder);
//
//                if (cursor != null && cursor.moveToFirst()) {
//                    do {
//                        ImageObject imageObject = getImageObject(cursor, projection);
//                        imageList.add(imageObject);
//                        // imageUrls.add(imageObject.photoUri); // assuming imageUrls is declared somewhere
//                        // Log.d("Image No. " + imageList.size(), imageObject.id + " " + imageObject.photoUri);
//                        if (imageList.size() == 100) break;
//                    } while (cursor.moveToNext());
//                }
//            } finally {
//                // Close the Cursor to release system resources
//                if (cursor != null) {
//                    cursor.close();
//                }
//            }

            return imageFiles;
        }
    }

//    public ImageObject getImageObject(Cursor cursor, String[] projection) {
//        long id = cursor.getLong(cursor.getColumnIndex(projection[0]));
//        String photoUri = cursor.getString(cursor.getColumnIndex(projection[1]));
//        String displayName = cursor.getString(cursor.getColumnIndex(projection[2]));
//        String directory = getDirectoryFromPhotoUri(photoUri);
//        long dateTakenMillis = cursor.getLong(cursor.getColumnIndex(projection[3]));
//        long size = cursor.getLong(cursor.getColumnIndex(projection[4]));
//        int width = cursor.getInt(cursor.getColumnIndex(projection[5]));
//        int height = cursor.getInt(cursor.getColumnIndex(projection[6]));
//        // Bitmap bitmap = getBitmap(photoUri); // assuming you have a method to get the bitmap
//        Bitmap bitmap = null; // replace this with the actual method call
//
//        return new ImageObject(id, photoUri, displayName, directory, dateTakenMillis, size, width, height, bitmap);
//    }
//
//    public String getDirectoryFromPhotoUri(String photoUri, ContentResolver contentResolver) {
//        String[] projection = {MediaStore.Images.Media.DATA};
//        Cursor cursor = contentResolver.query(Uri.parse(photoUri), projection, null, null, null);
//
//        if (cursor != null) {
//            try {
//                if (cursor.moveToFirst()) {
//                    int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
//                    return cursor.getString(columnIndex);
//                }
//            } finally {
//                cursor.close();
//            }
//        }
//
//        return null;
//    }

    // Assuming you have a method to get the bitmap
    // private Bitmap getBitmap(String photoUri) { }
}