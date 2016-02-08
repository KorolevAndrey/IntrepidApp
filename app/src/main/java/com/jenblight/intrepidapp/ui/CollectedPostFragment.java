package com.jenblight.intrepidapp.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.jenblight.intrepidapp.R;
import com.jenblight.intrepidapp.data.CollectedPhoto;
import com.jenblight.intrepidapp.data.SuggestedPost;

import java.util.ArrayList;
import java.util.List;


public class CollectedPostFragment extends Fragment {

    public static final int REQUEST_POST_EDIT = 0;

    private ListView uiPostList;
    private List<SuggestedPost> suggestedPosts;

    public static CollectedPostFragment newInstance() {
        CollectedPostFragment fragment = new CollectedPostFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public CollectedPostFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.collected_post_fragment, container, false);
        uiPostList = (ListView)v.findViewById(R.id.suggested_post_list);
        uiPostList.setAdapter(new PostAdapter(getActivity()));
        suggestedPosts = new ArrayList<SuggestedPost>();
        return v;
    }


    @Override
    public void onResume(){
        super.onResume();
        new groupPosts().execute();
    }


    private class PostAdapter extends BaseAdapter {
        private Context context;
        private List<SuggestedPost> posts;
        private SquareImageLoader imageLoader;

        public PostAdapter(Context c) {
            context = c;
            posts = new ArrayList<SuggestedPost>();
            imageLoader = new SquareImageLoader(c);
        }

        public int getCount() {
            return posts.size();
        }

        public SuggestedPost getItem(int position) {
            return posts.get(position);
        }

        public long getItemId(int position) {
            return 0;
        }

        public void addItem(SuggestedPost post) {
            posts.add(post);
        }

        public void deleteItem(int position) {
            posts.remove(position);
        }

        @Override
        public boolean areAllItemsEnabled()
        {
            return true;
        }

        @Override
        public boolean isEnabled(int arg0)
        {
            return true;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            //TODO: include images in ListItem
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View listItem;
            final SuggestedPost post = posts.get(position);
            final int adapter_postion = position;

            listItem = inflater.inflate(R.layout.suggested_post_item, null);


            listItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), EditPostActivity.class);
                    intent.putExtra("post", post);
                    intent.putExtra("position", adapter_postion);
                    startActivityForResult(intent, REQUEST_POST_EDIT);
                }
            });


            LinearLayout layout = (LinearLayout) listItem.findViewById(R.id.container);
            if (layout.getChildCount() == 0) {
                for (String photo : post.photos()) {
                    if (layout.getChildCount() < 4) {
                        ImageView imageView = new ImageView(context);
                        imageView.setPadding(5, 5, 5, 5);
                        imageLoader.displayImage("file://" + photo, imageView);
                        layout.addView(imageView, new LinearLayout.LayoutParams(250, 250));
                    }
                }
            }
            return listItem;
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if (resultCode == Activity.RESULT_OK){
            int position = data.getExtras().getInt("position");
            PostAdapter adapter = (PostAdapter) uiPostList.getAdapter();
            adapter.deleteItem(position);
            adapter.notifyDataSetChanged();
        }
    }

    class groupPosts extends AsyncTask<Void,SuggestedPost,Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
            Log.d("Intrepid", "Collecting Posts");
            if ( suggestedPosts.size() == 0 ) {
                SuggestedPost groupedPhotos = new SuggestedPost();

                for (CollectedPhoto photo : CollectedPhoto.getAll(CollectedPostFragment.this.getActivity())) {
                    if (groupedPhotos.isEmpty()) {
                        groupedPhotos.addPhoto(photo);
                    }
                    else {
                        Double latitudeDiff = photo.latitude - groupedPhotos.latitude();
                        Double longitudeDiff = photo.longitude - groupedPhotos.longitude();
                        Double approx_distance = Math.sqrt((latitudeDiff*latitudeDiff) + (longitudeDiff*longitudeDiff));
                        if (approx_distance <= 0.01) {
                            groupedPhotos.addPhoto(photo);
                        }
                        else {
                            suggestedPosts.add(groupedPhotos);
                            groupedPhotos = new SuggestedPost();
                            groupedPhotos.addPhoto(photo);
                            publishProgress(groupedPhotos);
                        }

                    }
                }
                if (!groupedPhotos.isEmpty()){
                    suggestedPosts.add(groupedPhotos);
                    publishProgress(groupedPhotos);

                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate (SuggestedPost... groupedPhotos) {
            PostAdapter adapter = (PostAdapter) uiPostList.getAdapter();
            adapter.addItem(groupedPhotos[0]);
            adapter.notifyDataSetChanged();
        }

    }

}
