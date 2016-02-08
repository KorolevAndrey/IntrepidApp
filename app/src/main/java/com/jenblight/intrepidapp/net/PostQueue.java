package com.jenblight.intrepidapp.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.jenblight.intrepidapp.data.PostData;

import java.io.IOException;

public class PostQueue {

    public PostQueue() {

    }

    public static void add(PostData post, Context context) throws IOException{
        post.save(context);
    }

    public static String uploadQueue(Context context) throws IOException{

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        int posts = 0;
        int successes = 0;

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (null != activeNetwork) {
            if(activeNetwork.isConnected()) {
                ApiWrapper wrapper = new ApiWrapper();
                boolean post_success;
                for (PostData post : PostData.getAll(context)) {
                    Log.d("Intrepid", post.name);
                    post_success = wrapper.uploadPost(context, post);
                    if (post_success) {
                        successes += 1;
                        post.delete(context);

                    }
                    posts += 1;
                }

                wrapper.close();

                return String.valueOf(successes) + " of " + String.valueOf(posts) + " Pins uploaded";
            }
        }
        return "WiFi not available";
    }

    public static class uploadQueueTask extends AsyncTask<Void,Void,String> {

        private Context context;

        public uploadQueueTask(Context context_in) {
            context = context_in;
        }

        @Override
        protected String doInBackground(Void... arg0) {
            try {
                return PostQueue.uploadQueue(context);
            } catch (IOException e) {
                e.printStackTrace();
                return "Could not get Pin cache";
            }
        }

    }

}

