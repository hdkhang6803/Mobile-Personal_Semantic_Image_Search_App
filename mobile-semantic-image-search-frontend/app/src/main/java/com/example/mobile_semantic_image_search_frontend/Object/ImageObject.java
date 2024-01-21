package com.example.mobile_semantic_image_search_frontend.Object;

import android.graphics.Bitmap;
import android.database.Cursor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageObject {
    private long id;
    private String photoUri;
    private String displayName;
    private String directory;
    private long dateTakenMillis;
    private long size;
    private int width;
    private int height;

    public ImageObject(
            long id,
            String photoUri,
            String displayName,
            String directory,
            long dateTakenMillis,
            long size,
            int width,
            int height
    ) {
        this.id = id;
        this.photoUri = photoUri;
        this.displayName = displayName;
        this.directory = directory;
        this.dateTakenMillis = dateTakenMillis;
        this.size = size;
        this.width = width;
        this.height = height;
    }

    public long getId() {
        return id;
    }

    public String getPhotoUri() {
        return photoUri;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDirectory() {
        return directory;
    }

    public long getDateTakenMillis() {
        return dateTakenMillis;
    }

    public long getSize() {
        return size;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getDateTaken() {
        return formatDate(dateTakenMillis);
    }

    private String formatDate(long dateTakenMillis) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return dateFormat.format(new Date(dateTakenMillis));
    }
}
