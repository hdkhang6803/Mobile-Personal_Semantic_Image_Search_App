package com.example.mobile_semantic_image_search_frontend;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class ServerResponse {

    // You can use @SerializedName annotation to map the JSON keys to Java variable names
    @SerializedName("status")
    private String status;
    @SerializedName("image_uris")
    private List<String> image_uris;

    private String message;

    public String getMessage() {
        return message;
    }

    public String getStatus() {
        return status;
    }
    public List<String> getImageUris(){
        return image_uris;
    }

//    public List<ImageObject> getImageList(Context context) {
//        List<ImageObject> imageList = new ArrayList<>();
//
//        ContentResolver contentResolver = context.getContentResolver();
//        Uri queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
//
//        String selection = MediaStore.Images.Media.MIME_TYPE + " LIKE 'image/%'";
//        String[] projection = {
//                MediaStore.Images.Media._ID,
//                MediaStore.Images.Media.DATA,
//                MediaStore.Images.Media.DISPLAY_NAME,
//                MediaStore.Images.Media.DATE_TAKEN,
//                MediaStore.Images.Media.SIZE,
//                MediaStore.Images.Media.WIDTH,
//                MediaStore.Images.Media.HEIGHT
//        };
//        String sortOrder = MediaStore.Images.Media.DATE_TAKEN + " DESC";
//
//        Cursor cursor = null;
//        try {
//            cursor = contentResolver.query(queryUri, projection, selection, null, sortOrder);
//
//            if (cursor != null && cursor.moveToFirst()) {
//                do {
//                    ImageObject imageObject = getImageObject(cursor, projection);
//                    imageList.add(imageObject);
//                    // imageUrls.add(imageObject.photoUri); // assuming imageUrls is declared somewhere
//                    // Log.d("Image No. " + imageList.size(), imageObject.id + " " + imageObject.photoUri);
//                    if (imageList.size() == 100) break;
//                } while (cursor.moveToNext());
//            }
//        } finally {
//            // Close the Cursor to release system resources
//            if (cursor != null) {
//                cursor.close();
//            }
//        }
//
//        return imageList;
//    }
//
//    public ImageObject getImageObject(Cursor cursor, String[] projection) {
//        long id = cursor.getLong(cursor.getColumnIndex(projection[0]));
//        String photoUri = cursor.getString(cursor.getColumnIndex(projection[1]));
//        String displayName = cursor.getString(cursor.getColumnIndex(projection[2]));
//        String directory = getDirectoryFromPhotoUri(photoUri);
//        long dateTakenMillis = cursor.getLong(cursor.getColumnIndex(projection[3]));
//        long size = cursor.getLong(cursor.getColumnIndex(projection[4]));
//        int width = cursor.getInt(cursor.getColumnIndex(projection[5]));
//        int height = cursor.getInt(cursor.getColumnIndex(projection[6]));
//
//        return new ImageObject(id, photoUri, displayName, directory, dateTakenMillis, size, width, height);
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
//        return null;
//    }
}

