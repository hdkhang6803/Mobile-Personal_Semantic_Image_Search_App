package com.example.mobile_semantic_image_search_frontend.Object;

import java.io.Serializable;

public class ImageModel implements Serializable {
    private String imageUri;
    private boolean isSelected;

    private boolean isShowingCheckbox;

    // Constructor and other methods
    public ImageModel(String imageUri) {
        this.imageUri = imageUri;
        this.isSelected = false;
        this.isShowingCheckbox = false;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public boolean isShowingCheckbox() {
        return isShowingCheckbox;
    }

    public void setShowingCheckbox(boolean showingCheckbox) {
        isShowingCheckbox = showingCheckbox;
    }

    public String getImageUri() {
        return imageUri;
    }
}
