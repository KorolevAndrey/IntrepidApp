package com.jenblight.intrepidapp.ui;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.jenblight.intrepidapp.R;
import com.jenblight.intrepidapp.data.PostData;

import java.util.ArrayList;
import java.util.List;


public class PostListFragment extends Fragment {

    private GridView postList;

    public static PostListFragment newInstance() {
        PostListFragment fragment = new PostListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.post_list_fragment, container, false);
        postList = (GridView)v.findViewById(R.id.post_grid);
        postList.setAdapter(new PostAdapter(getActivity()));
        return v;
    }

    @Override
    public void onResume(){
        super.onResume();
        ((PostAdapter) postList.getAdapter()).clear();
        new updatePosts().execute();
    }

    public class PostAdapter extends BaseAdapter {
        private Context context;
        private List<PostData> posts;
        private SquareImageLoader imageLoader;

        public PostAdapter(Context c) {
            context = c;
            posts = new ArrayList<PostData>();
            imageLoader = new SquareImageLoader(c);
        }

        public int getCount() {
            return posts.size();
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        public void addItem(PostData post){
            posts.add(post);
        }

        public void clear(){
            posts.clear();
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View gridItem;
            PostData post = posts.get(position);

            gridItem = inflater.inflate(R.layout.grid_item, null);
            ImageView imageView = (ImageView) gridItem
                    .findViewById(R.id.grid_item_image);

            imageLoader.displayImage("file://"+post.photos.get(0), imageView);

            TextView text = (TextView)gridItem.findViewById(R.id.grid_item_text);
            text.setText(post.name);

            return gridItem;
        }

    }

    class updatePosts extends AsyncTask<Void,PostData,Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
            for (PostData post : PostData.getAll(PostListFragment.this.getActivity())) {
                publishProgress(post);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate (PostData... post) {
            PostAdapter adapter = (PostAdapter)postList.getAdapter();
            adapter.addItem(post[0]);
            adapter.notifyDataSetChanged();
        }

    }
}
