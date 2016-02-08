package com.jenblight.intrepidapp.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.process.BitmapProcessor;

/**
 * Created by jgblight on 16-02-04.
 */
public class SquareImageLoader {

    private Context context;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;

    public SquareImageLoader(Context c) {
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(c).build();
        imageLoader = ImageLoader.getInstance();
        imageLoader.init(config);
        options = new DisplayImageOptions.Builder().postProcessor(new BitmapProcessor() {
            @Override
            public Bitmap process(Bitmap bitmap) {
                int crop;
                crop = Math.min(bitmap.getHeight(), bitmap.getWidth());
                return Bitmap.createBitmap(bitmap, 0, 0, crop, crop);
            }
        }).build();
    }

    public void displayImage(String uri, ImageView imageView){
        imageLoader.displayImage(uri, imageView, options);
    }
}
