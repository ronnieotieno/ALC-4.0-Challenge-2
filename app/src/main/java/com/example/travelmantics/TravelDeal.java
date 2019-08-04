package com.example.travelmantics;

import android.view.View;
import android.widget.ImageView;

import androidx.databinding.BindingAdapter;

import com.google.firebase.firestore.Exclude;
import com.squareup.picasso.Picasso;

public class TravelDeal {


    private String title;
    private String description;
    private String price;
    private String imageUrl;

    @Exclude
    private String id;

    public TravelDeal() {
    }

    public TravelDeal(String title, String description, String price, String imageUrl) {

        this.title = title;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;

    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getPrice() {
        return price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    @BindingAdapter({"app:imageUrl"})
    public static void loadImage(View view, String image) {

        ImageView imageView = (ImageView) view;
        Picasso.get().load(image).fit().centerCrop().into(imageView);
    }

}
