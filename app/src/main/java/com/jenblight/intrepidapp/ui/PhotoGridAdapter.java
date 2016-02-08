package com.jenblight.intrepidapp.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;

import com.jenblight.intrepidapp.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jgblight on 16-01-03.
 */
public class PhotoGridAdapter extends BaseAdapter {
    private Context context;
    public List<String> filenames;
    private ButtonListener buttonListener;
    private SquareImageLoader imageLoader;
    private DisplayImageOptions options;

    public PhotoGridAdapter(Context c, ButtonListener inListener) {
        context = c;
        filenames = new ArrayList<String>();
        buttonListener = inListener;
        imageLoader = new SquareImageLoader(c);
    }

    public int getCount() {
        return filenames.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }


    public void addItem(String imgFilename){
        filenames.add(imgFilename);
        this.notifyDataSetChanged();
    }

    public void addItems(List<String> imgFilenames) {
        for (String filename : imgFilenames) {
            filenames.add(filename);
        }
        this.notifyDataSetChanged();
    }

    public void deleteItem(int position){
        filenames.remove(position);
        this.notifyDataSetChanged();
    }

    public void deleteItems() {
        filenames.clear();
        this.notifyDataSetChanged();
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(final int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View gridItem;

        gridItem = inflater.inflate(R.layout.photo_grid_item, null);
        ImageView imageView = (ImageView) gridItem
                .findViewById(R.id.grid_item_image);

        imageLoader.displayImage("file://" + filenames.get(position), imageView);


        //TODO: change button styling (eg. small x)
        Button discard_button = (Button)gridItem.findViewById(R.id.discard_button);
        discard_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PhotoGridAdapter.this.deleteItem(position);
                buttonListener.onButton(position);
            }
        });

        return gridItem;
    }

    public interface ButtonListener {
        public void onButton(int position);
    }

}
