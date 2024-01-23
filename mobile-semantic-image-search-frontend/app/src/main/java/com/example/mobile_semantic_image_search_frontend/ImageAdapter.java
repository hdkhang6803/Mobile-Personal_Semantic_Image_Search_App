package com.example.mobile_semantic_image_search_frontend;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobile_semantic_image_search_frontend.Object.ImageModel;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private Context context;

    public List<ImageModel> imageList;

    private OnImageClickListener onImageClickListener;

    private OnImageLongClickListener onImageLongClickListener;

    interface OnImageClickListener {
        void onImageClick(int position);
    }

    interface OnImageLongClickListener {
        void onImageLongClick(int position);
    }

    public ImageAdapter(Context context, List<String> imageUriList) {
        this.context = context;
        //Create a list of ImageModel objects from the list of image URIs
        this.imageList = new ArrayList<>(); // imageList
        for (String imageUri : imageUriList) {
            imageList.add(new ImageModel(imageUri));
        }
        this.onImageClickListener = (OnImageClickListener) context;
        this.onImageLongClickListener = (OnImageLongClickListener) context;
    }
    public void setImageUriList(List<String> imageUriList){
        this.imageList = new ArrayList<>(); // imageList
        for (String imageUri : imageUriList) {
            imageList.add(new ImageModel(imageUri));
        }
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {

        position = holder.getAdapterPosition();
        ImageModel imageModel = imageList.get(position);
        String imageUriString = imageModel.getImageUri();
        File imageFile = new File(imageUriString);

        int finalPosition = position;

        holder.bind(imageModel);

        holder.imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onImageLongClickListener.onImageLongClick(finalPosition);
                return true;
            }
        });

        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("onImageClickListener", "onImageClickListener");
                onImageClickListener.onImageClick(finalPosition);
            }
        });

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (finalPosition != RecyclerView.NO_POSITION && buttonView.isPressed()) {
                Log.e("onCheckedChanged", "onCheckedChanged");
                imageList.get(finalPosition).setSelected(isChecked);
                List<ImageModel> selectedImageList = getSelectedImages();
                Log.e("Selected ", "Selected  size: " + selectedImageList.size());
                // notifyItemChanged(finalPosition);
            }
        });

        Picasso.get()
                .load(Uri.fromFile(imageFile))                   // Convert the File to a Uri
                .resize(200, 200)          // Reduce image size to avoid lagging
                .centerInside()                                 // Crop the center of the image with given size while preserving dimension ratio
                .into(holder.imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        Log.d("load image success", imageUriString);
                        holder.imageView.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e("load image error", "Error loading image: " + e.getMessage());
                        // Remove the ImageView from the RecyclerView and notify the adapter
                        int adapterPosition = holder.getAdapterPosition();
                        if (adapterPosition != RecyclerView.NO_POSITION) {
                            imageList.remove(adapterPosition);
                            notifyItemRemoved(adapterPosition);
                            notifyItemRangeChanged(adapterPosition, imageList.size());
                        }
                    }
                });


    }


    public void showImageOptionsPopup(String imageUri) {
        // Inflate the popup layout
        View popupView = LayoutInflater.from(context).inflate(R.layout.popup_image_options, null);

        // Set up the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(popupView);
        AlertDialog dialog = builder.create();

        // Get views from the popup layout
        ImageView fullImageView = popupView.findViewById(R.id.fullImageView);
        ImageButton deleteButton = popupView.findViewById(R.id.deleteButton);
        ImageButton shareButton = popupView.findViewById(R.id.shareButton);
        ImageButton cancelButton = popupView.findViewById(R.id.cancelButton);

        // Load and display the full image
        Picasso.get().load(Uri.fromFile(new File(imageUri))).into(fullImageView);

        // Delete button click listener
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {


                    // Get the item position in imageList by image Uri
                    int position = 0;
                    for (int i = 0; i < imageList.size(); i++) {
                        if (imageList.get(i).getImageUri().equals(imageUri)) {
                            position = i;
                            break;
                        }
                    }

                    File imageFile = new File(imageUri);

                    // Query the MediaStore to find the file
                    String[] projection = {MediaStore.Files.FileColumns._ID};
                    String selection = MediaStore.Images.Media.DATA + "=?";
                    String[] selectionArgs = new String[]{imageFile.getAbsolutePath()};
                    Log.e("FileDeletionUtil", "File path: " + imageFile.getAbsolutePath());

                    ContentResolver contentResolver = context.getContentResolver();
                    Cursor cursor = contentResolver.query(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            projection,
                            selection,
                            selectionArgs,
                            null
                    );

                    if (cursor != null && cursor.moveToFirst()) {
                        // Get the file's ID
                        long fileId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID));

                        // Build the content URI for the file
                        Uri contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, fileId);

                        Log.e("FileDeletionUtil", "File ID: " + fileId + "\nContent URI: " + contentUri);

                        // Delete the file using the content resolver
                        int rowsDeleted = contentResolver.delete(contentUri, null, null);

                        if (rowsDeleted > 0) {
                            Log.e("FileDeletionUtil", "File deleted successfully");
                            imageList.remove(position);
                            notifyItemRemoved(position);
                        } else {
                            Log.e("FileDeletionUtil", "Failed to delete file");
                        }
                    } else {
                        Log.e("FileDeletionUtil", "File not found in MediaStore");
                    }

                    if (cursor != null) {
                        cursor.close();
                    }
                } catch (Exception e) {
                    Log.e("FileDeletionUtil", "Failed to delete file: " + e.getMessage());
                }

                // Dismiss the dialog
                dialog.dismiss();
            }
        });

        // Share button click listener
        shareButton.setOnClickListener(view -> {
            // Create an Intent with ACTION_SEND
            Intent shareIntent = new Intent(Intent.ACTION_SEND);

            // Set the MIME type for the intent (image type)
            shareIntent.setType("image/*");

            // Create a File object from the image URI
            File imageFile = new File(imageUri);

            // Check if the image file exists
            if (imageFile.exists()) {
                // Create a content URI from the image file
                Uri contentUri = FileProvider.getUriForFile(context, "com.example.android.fileprovider", imageFile);

                // Set the URI as the EXTRA_STREAM for the intent
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                // Optionally, add a subject for the shared content
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Shared Image");

                // Optionally, add text for the shared content
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out this image!");

                // Start the activity with the intent
                context.startActivity(Intent.createChooser(shareIntent, "Share Image"));
            } else {
                Toast.makeText(context, "Image not exist in device!", Toast.LENGTH_SHORT).show();
            }

            // Dismiss the dialog
            dialog.dismiss();
        });

        cancelButton.setOnClickListener(view -> {
            // Dismiss the dialog
            dialog.dismiss();
        });

        // Show the dialog
        dialog.show();
    }

    public void callBroadCast() {
        if (Build.VERSION.SDK_INT >= 14) {
            Log.e("-->", " >= 14");
            MediaScannerConnection.scanFile(context, new String[]{Environment.getExternalStorageDirectory().toString()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                public void onScanCompleted(String path, Uri uri) {
                    Log.e("ExternalStorage", "Scanned " + path + ":");
                    Log.e("ExternalStorage", "-> uri=" + uri);
                }
            });
        } else {
            Log.e("-->", " < 14");
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
                    Uri.parse("file://" + Environment.getExternalStorageDirectory())));
        }
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        CheckBox checkBox;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);

            checkBox = itemView.findViewById(R.id.selectionCheckBox);
            checkBox.setSelected(false);
            checkBox.setVisibility(View.GONE);

        }
        public void bind(ImageModel imageModel) {
            checkBox.setChecked(imageModel.isSelected());
            checkBox.setVisibility(imageModel.isShowingCheckbox() ? View.VISIBLE : View.GONE);
        }
    }



    public List<ImageModel> getSelectedImages() {
        Log.e("getSelectedImages", "getSelectedImages");
        List<ImageModel> selectedImages = new ArrayList<>();
        for (ImageModel imageModel : imageList) {
            if (imageModel.isSelected()) {
                selectedImages.add(imageModel);
            }
        }
        return selectedImages;
    }

    public void clearSelection() {
        for (ImageModel imageModel : imageList) {
            imageModel.setSelected(false);
            imageModel.setShowingCheckbox(false);
        }
        notifyDataSetChanged();

    }
}
