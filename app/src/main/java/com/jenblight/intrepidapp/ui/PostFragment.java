package com.jenblight.intrepidapp.ui;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.jenblight.intrepidapp.R;
import com.jenblight.intrepidapp.data.PostData;
import com.jenblight.intrepidapp.net.PostQueue;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class PostFragment extends Fragment implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private final static int
            CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private GoogleApiClient client=null;
    private Location updatedLocation;

    private TripSelector tripSelector;
    private String currentPhotoPath;
    private List<String> allPhotos;

    private Button submit;
    private Button addPhoto;
    private EditText inputTitle;
    private EditText inputContent;
    private GridView photoGrid;

    private String title;
    private String content;

    public static PostFragment newInstance() {
        PostFragment fragment = new PostFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        allPhotos = new ArrayList<String>();

        SharedPreferences prefs = getActivity().getSharedPreferences("Intrepid", Context.MODE_PRIVATE);

        tripSelector = new TripSelector(getActivity());

        client=new GoogleApiClient.Builder(this.getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.post_fragment, container, false);
        inputTitle = (EditText)v.findViewById(R.id.title);
        inputContent = (EditText)v.findViewById(R.id.content);

        submit = (Button) v.findViewById(R.id.submit);
        submit.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        savePin();
                    }
                }
        );

        addPhoto = (Button)v.findViewById(R.id.add_photo);
        addPhoto.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addPhoto();

                    }
                }
        );


        photoGrid = (GridView)v.findViewById(R.id.photo_grid);
        photoGrid.setAdapter(new PhotoGridAdapter(getActivity(), new PhotoGridAdapter.ButtonListener() {
            @Override
            public void onButton(int position) {
                allPhotos.remove(position);
            }
        }));

        Spinner tripSpinner = (Spinner)v.findViewById(R.id.trip_spinner);
        tripSelector.createSpinner(tripSpinner);
        return v;
    }

    @Override
    public void onStart(){
        super.onStart();
        tripSelector.updateTrips();
    }

    @Override
    public void onResume() {
        super.onResume();
        client.connect();
    }

    @Override
    public void onPause() {
        client.disconnect();
        super.onPause();
    }

    public void savePin() {
        inputTitle.setError(null);

        title = inputTitle.getText().toString();
        content = inputContent.getText().toString();

        if (TextUtils.isEmpty(title)) {
            inputTitle.setError(getString(R.string.error_field_required));
        }
        else {
            if (updatedLocation == null){
                Toast.makeText(this.getActivity(),"Location not available",Toast.LENGTH_SHORT).show();
                return;
            }

            PostData post = new PostData(tripSelector.selectedTrip,title,Calendar.getInstance().getTime(),content,updatedLocation,allPhotos);
            try {
                PostQueue.add(post, this.getActivity());
                Toast.makeText(this.getActivity(),"Pin Created",Toast.LENGTH_SHORT).show();
                clearInputs();
            } catch (IOException e1) {
                Toast.makeText(this.getActivity(),"Could not save Pin",Toast.LENGTH_SHORT).show();
                e1.printStackTrace();
            }
        }

        new PostQueue.uploadQueueTask(this.getActivity()).execute();
    }

    private void clearInputs() {
        inputContent.setText("");
        inputTitle.setText("");
        allPhotos.clear();
        ((PhotoGridAdapter) photoGrid.getAdapter()).deleteItems();
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = File.separator + "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        Log.d("Intrepid", imageFileName);
        Log.d("Intrepid", storageDir.toString());
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public void addPhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
                Toast.makeText(this.getActivity(),"Cannot create image file",Toast.LENGTH_SHORT).show();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));

                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
            else {
                Toast.makeText(this.getActivity(),"Cannot create image file", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(this.getActivity(),"Cannot access camera",Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == getActivity().RESULT_OK) {
            Log.d("Intrepid","got result");
            //Bundle extras = data.getExtras();
            //Bitmap imageBitmap = (Bitmap) extras.get("data");
            // Add thumbnail?
            allPhotos.add(currentPhotoPath);
            PhotoGridAdapter gridAdapter = (PhotoGridAdapter)photoGrid.getAdapter();
            gridAdapter.addItem(currentPhotoPath);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        this.getActivity(),
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                * Thrown if Google Play services canceled the original
                * PendingIntent
                */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            Log.d("Intrepid",String.valueOf(connectionResult.getErrorCode()));
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        updatedLocation = LocationServices.FusedLocationApi.getLastLocation(client);
        LocationRequest request = LocationRequest.create();
        request.setNumUpdates(3);
        request.setExpirationDuration(300000);
        request.setInterval(30000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(client, request, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        //TODO: better UI for this
        Toast.makeText(this.getActivity(),"Location updated",Toast.LENGTH_SHORT).show();
        updatedLocation = location;
    }

}