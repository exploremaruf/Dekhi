package com.dekhi.dekhi.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;
import android.widget.ImageView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NativeImageLoader {
    private static NativeImageLoader instance;
    private final LruCache<String, Bitmap> memoryCache;
    private final ExecutorService executorService;
    private final Handler mainHandler;

    private NativeImageLoader() {
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        int cacheSize = maxMemory / 8;
        memoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };
        executorService = Executors.newFixedThreadPool(4);
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public static synchronized NativeImageLoader getInstance() {
        if (instance == null) instance = new NativeImageLoader();
        return instance;
    }

    public void loadImage(String urlString, ImageView imageView, int placeholderRes) {
        if (urlString == null || urlString.isEmpty()) {
            imageView.setImageResource(placeholderRes);
            return;
        }

        Bitmap cachedBitmap = memoryCache.get(urlString);
        if (cachedBitmap != null) {
            imageView.setImageBitmap(cachedBitmap);
            return;
        }

        imageView.setImageResource(placeholderRes);
        imageView.setTag(urlString);

        executorService.execute(() -> {
            try {
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(input);
                
                if (bitmap != null) {
                    memoryCache.put(urlString, bitmap);
                    mainHandler.post(() -> {
                        if (urlString.equals(imageView.getTag())) {
                            imageView.setImageBitmap(bitmap);
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
