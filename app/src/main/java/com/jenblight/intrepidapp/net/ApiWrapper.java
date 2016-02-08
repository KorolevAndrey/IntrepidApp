package com.jenblight.intrepidapp.net;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import com.jenblight.intrepidapp.data.PostData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by jgblight on 15-04-24.
 */

public class ApiWrapper {
    private String USERNAME = "__USERNAME__";
    private String PASSWORD = "__PASSWORD__";


    private static final String api_url = "http://intrepid.me/api/";

    private OkHttpClient client;

    public ApiWrapper() {
        client = new OkHttpClient();
    }

    private Login getLogin(Context context){
        SharedPreferences prefs = context.getSharedPreferences("Intrepid", Context.MODE_PRIVATE);
        String username = prefs.getString(USERNAME, null);
        String password = prefs.getString(PASSWORD, null);
        return new Login(username, password);
    }

    private void signRequest(Request.Builder request, Login login){
        signRequest(request, login.username, login.password);
    }

    private void signRequest(Request.Builder request, String mUsername, String mPassword){
        String credentials = mUsername + ":" + mPassword;
        String base64EncodedCredentials = Base64.encodeToString(
                credentials.getBytes(), Base64.NO_WRAP);
        request.addHeader("AUTH", "Basic " + base64EncodedCredentials);
    }

    public boolean checkLogin(Context context, String mUsername, String mPassword){
        Request.Builder get = new Request.Builder().url(api_url + mUsername + "/trips/active");
        signRequest(get, mUsername, mPassword);
        Response response;
        String response_body;
        try {
            Log.i("Intrepid", "sending request");
            response = client.newCall(get.build()).execute();
            response_body = response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        SharedPreferences prefs = context.getSharedPreferences("Intrepid",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(USERNAME, mUsername);
        editor.putString(PASSWORD, mPassword);

        try {
            editor.putString("trips", new JSONArray(response_body).toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        editor.commit();

        return true;
    }

    public JSONArray getTrips(Context context){
        Login login = getLogin(context);
        Request.Builder get = new Request.Builder().url(api_url + login.username + "/trips/active");
        signRequest(get, login);

        Response response;
        String response_body;
        try {
            response = client.newCall(get.build()).execute();
            response_body = response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        try {
            return new JSONArray(response_body);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean uploadPost(Context context, PostData post){
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("trip", post.trip.toString())
                .addFormDataPart("name", post.name)
                .addFormDataPart("date", new SimpleDateFormat(PostData.dateFormat).format(post.date))
                .addFormDataPart("text", post.text)
                .addFormDataPart("lat",post.latitude.toString())
                .addFormDataPart("lon", post.longitude.toString());


        for (int i=0; i < post.photos.size(); i++){
            try {
                Log.d("Intrepid", post.photos.get(i));
                String filename = "file" + String.valueOf(i);
                builder.addFormDataPart(filename, filename,RequestBody.create(MediaType.parse("image/jpg"), new File(post.photos.get(i))));
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }


        Login login = getLogin(context);
        Request.Builder request = new Request.Builder().url(api_url + "pin/new").post(builder.build());
        signRequest(request, login);

        Response response;
        String response_body;
        boolean success;
        try {
            Log.d("Intrepid","sending request");
            response = client.newCall(request.build()).execute();
            response_body = response.body().string();
            Log.d("Intrepid","response sent");
            JSONObject jsonObject = new JSONObject(response_body);
            if (jsonObject.optBoolean("success")){
                success = true;
            }
            else {
                success = false;
                Log.e("Intrepid",jsonObject.optString("error"));
            }
        } catch (IOException|JSONException e) {
            e.printStackTrace();
            success = false;
        }
        return success;
    }

    public void close() {

    }

    private class Login {
        public final String username;
        public final String password;

        public Login(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }

}
