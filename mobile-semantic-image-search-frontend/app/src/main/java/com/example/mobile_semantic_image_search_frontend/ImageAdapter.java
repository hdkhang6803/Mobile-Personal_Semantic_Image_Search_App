package com.example.mobile_semantic_image_search_frontend;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private Context context;
<<<<<<< Updated upstream
    private List<String> imageUriList;
=======
    public List<ImageModel> imageList;

    private OnImageClickListener onImageClickListener;

    private static OnImageLongClickListener onImageLongClickListener;

    public interface OnImageClickListener {
        void onImageClick(int position);
    }

    public interface OnImageLongClickListener {
        void onImageLongClick(int position);
    }

    public void setOnImageClickListener(OnImageClickListener onImageClickListener) {
        this.onImageClickListener = onImageClickListener;
    }

    public void setOnImageLongClickListener(OnImageLongClickListener onImageLongClickListener) {
        this.onImageLongClickListener = onImageLongClickListener;
    }
>>>>>>> Stashed changes

    public ImageAdapter(Context context, List<String> imageUriList) {
        this.context = context;
        this.imageUriList = imageUriList;
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
<<<<<<< Updated upstream
        String imageUriString = imageUriList.get(position);
        File imageFile = new File(imageUriString);

=======
//        String imageUriString = imageUriList.get(position);
        position = holder.getAdapterPosition();
        ImageModel imageModel = imageList.get(position);
        String imageUriString = imageModel.getImageUri();
        File imageFile = new File(imageUriString);

        int finalPosition = position;
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onImageClickListener != null) {
                    onImageClickListener.onImageClick(finalPosition);
                }
            }
        });

        holder.bind(imageModel);

>>>>>>> Stashed changes
        Picasso.get()
                .load(Uri.fromFile(imageFile))  // Convert the File to a Uri
                .into(holder.imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        Log.d("load image success", "");
                        holder.imageView.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e("load image error", "Error loading image: " + e.getMessage());
                        // Remove the ImageView from the RecyclerView and notify the adapter
                        int adapterPosition = holder.getAdapterPosition();
                        if (adapterPosition != RecyclerView.NO_POSITION) {
                            imageUriList.remove(adapterPosition);
                            notifyItemRemoved(adapterPosition);
                            notifyItemRangeChanged(adapterPosition, imageList.size());
                        }
                    }
                });

<<<<<<< Updated upstream
=======
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
        deleteButton.setOnClickListener(view -> {
            // Handle delete action
            // Notify the adapter after deletion
            int position = imageList.indexOf(imageUri);
            if (position != -1) {
                File imageFile = new File(imageUri);

                // Set up the projection (we only need the ID)
                String[] projection = { MediaStore.Images.Media._ID };

                // Match on the file path
                String selection = MediaStore.Images.Media.DATA + " = ?";
                String[] selectionArgs = new String[] { imageFile.getAbsolutePath() };
                Log.e("selectionArgs", selectionArgs[0]);

                // Query for the ID of the media matching the file path
                Uri queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                ContentResolver contentResolver = context.getContentResolver();
                Cursor c = contentResolver.query(queryUri, projection, selection, selectionArgs, null);
                if (c.moveToFirst()) {
                    // We found the ID. Deleting the item via the content provider will also remove the file
                    long id = c.getLong(c.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                    Uri deleteUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                    contentResolver.delete(deleteUri, null, null);
                } else {
                    // File not found in media store DB
                    Toast.makeText(context, "Image not exist in device!", Toast.LENGTH_SHORT).show();
                }
                c.close();
            }

            // Dismiss the dialog
            dialog.dismiss();
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
                Uri contentUri = FileProvider.getUriForFile(context, "com.example.mobile_semantic_image_search_frontend.fileprovider", imageFile);

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
>>>>>>> Stashed changes
    }

    @Override
    public int getItemCount() {
        return imageUriList.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
<<<<<<< Updated upstream
        }
    }

    public void setImageUriList(List<String> imageUriList){
        this.imageUriList = imageUriList;
=======
            checkBox = itemView.findViewById(R.id.selectionCheckBox);
            checkBox.setSelected(false);
            checkBox.setVisibility(View.GONE);
        }
        public void bind(ImageModel imageModel) {
            checkBox.setChecked(imageModel.isSelected());
            // Update the checkbox visibility based on the model
            imageView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    onImageLongClickListener.onImageLongClick(getAdapterPosition());
                    return false;
                }
            });
        }
    }



    public List<ImageModel> getSelectedImages() {
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

        }
        notifyDataSetChanged();
>>>>>>> Stashed changes
    }
}
