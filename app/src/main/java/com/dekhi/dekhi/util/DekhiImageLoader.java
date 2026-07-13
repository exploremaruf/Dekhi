package com.dekhi.dekhi.util;

import android.content.Context;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.dekhi.dekhi.R;

public class DekhiImageLoader {

    private static final RequestOptions BASE_OPTIONS = new RequestOptions()
            .placeholder(R.color.surface_elevated)
            .error(R.color.surface_elevated)
            .diskCacheStrategy(DiskCacheStrategy.ALL);

    public static void loadCategory(@NonNull Context context, @Nullable String url, @NonNull ImageView imageView) {
        if (url == null || url.isEmpty()) {
            imageView.setImageResource(R.color.surface_elevated);
            return;
        }

        Glide.with(context)
                .load(url)
                .apply(BASE_OPTIONS)
                .circleCrop()
                .into(imageView);
    }

    public static void loadThumbnail(@NonNull Context context, @Nullable String url, @NonNull ImageView imageView) {
        if (url == null || url.isEmpty()) {
            imageView.setImageResource(R.color.surface_elevated);
            return;
        }

        Glide.with(context)
                .load(url)
                .apply(BASE_OPTIONS)
                .centerCrop()
                .into(imageView);
    }

    public static void clear(@NonNull Context context, @NonNull ImageView imageView) {
        Glide.with(context).clear(imageView);
    }
}
