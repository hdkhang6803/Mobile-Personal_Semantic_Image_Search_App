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
    private List<String> imageUriList;

    public ImageAdapter(Context context, List<String> imageUriList) {
        this.context = context;
        this.imageUriList = imageUriList;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imageUriString = imageUriList.get(position);
        File imageFile = new File(imageUriString);

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
                        holder.imageView.setVisibility(View.GONE);
                    }
                });

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
        }
    }

    public void setImageUriList(List<String> imageUriList){
        this.imageUriList = imageUriList;
    }
}
