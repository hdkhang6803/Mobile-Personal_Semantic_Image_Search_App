package com.example.mobile_semantic_image_search_frontend.Object;

public class ImageModel {
    private String imageUri;
    private boolean isSelected;

    // Constructor and other methods
    public ImageModel(String imageUri) {
        this.imageUri = imageUri;
        this.isSelected = false;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getImageUri() {
        return imageUri;
    }
}
