package com.example.rest_app.utils;

import android.content.Context;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

public class ImageLoader {

    public static void loadBackground(Context context, ImageView imageView, String imageUrl) {
        Glide.with(context)
                .load(imageUrl)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imageView);
    }

    public static void loadBackground(Context context, ImageView imageView, int drawableResource) {
        Glide.with(context)
                .load(drawableResource)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imageView);
    }
}