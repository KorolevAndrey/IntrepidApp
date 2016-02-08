package com.jenblight.intrepidapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.Toast;

import com.jenblight.intrepidapp.R;
import com.jenblight.intrepidapp.data.PostData;
import com.jenblight.intrepidapp.data.SuggestedPost;
import com.jenblight.intrepidapp.net.PostQueue;

import java.io.IOException;

public class EditPostActivity extends AppCompatActivity {

    private Toolbar toolbar;

    private SuggestedPost post;
    private int adapter_position;
    private TripSelector tripSelector;

    private Button save_button;
    private Button discard_button;
    private EditText inputTitle;
    private EditText inputContent;
    private GridView photoGrid;

    private String title;
    private String content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        post = extras.getParcelable("post");
        adapter_position = extras.getInt("position");

        setContentView(R.layout.activity_edit_post);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tripSelector = new TripSelector(this);

        Spinner tripSpinner = (Spinner)findViewById(R.id.trip_spinner);
        tripSelector.createSpinner(tripSpinner);

        inputTitle = (EditText)findViewById(R.id.title);
        inputContent = (EditText)findViewById(R.id.content);

        save_button = (Button) findViewById(R.id.submit);
        save_button.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        savePin();
                    }
                }
        );

        discard_button = (Button) findViewById(R.id.delete);
        discard_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                post.delete(EditPostActivity.this);
                setResult(RESULT_OK, new Intent().putExtra("position", adapter_position));
                finish();
            }
        });

        PhotoGridAdapter gridAdapter = new PhotoGridAdapter(this, new PhotoGridAdapter.ButtonListener() {
            @Override
            public void onButton(int position) {
                post.removePhoto(position, EditPostActivity.this);
            }
        });
        photoGrid = (GridView)findViewById(R.id.photo_grid);
        photoGrid.setAdapter(gridAdapter);

    }

    @Override
    protected void onStart() {
        super.onStart();
        tripSelector.updateTrips();
        PhotoGridAdapter gridAdapter = (PhotoGridAdapter)photoGrid.getAdapter();
        gridAdapter.addItems(post.photos());

    }

    public void savePin() {
        inputTitle.setError(null);

        title = inputTitle.getText().toString();
        content = inputContent.getText().toString();

        if (TextUtils.isEmpty(title)) {
            inputTitle.setError(getString(R.string.error_field_required));
        }
        else {

            PostData new_post = new PostData(
                    tripSelector.selectedTrip,
                    title,
                    post.date(),
                    content,
                    post.latitude(),
                    post.longitude(),
                    post.photos());
            try {
                PostQueue.add(new_post, this);
                post.delete(this);
            } catch (IOException e1) {
                Toast.makeText(this,"Could not save Pin",Toast.LENGTH_SHORT).show();
                e1.printStackTrace();
            }
            new PostQueue.uploadQueueTask(this).execute();
            setResult(RESULT_OK, new Intent().putExtra("position", adapter_position));
            finish();
        }
    }

}
