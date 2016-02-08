package com.jenblight.intrepidapp.data;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by jgblight on 16-01-03.
 */
public class SuggestedPost implements Parcelable{
    public static final String dateFormat = "yyyy-MM-dd'T'HH:mm:ssZ";

    public List<CollectedPhoto> collectedPhotos;

    public SuggestedPost(){
        collectedPhotos = new ArrayList<CollectedPhoto>();
    }

    public SuggestedPost(Parcel in) throws ParseException{
        collectedPhotos = in.readArrayList(CollectedPhoto.class.getClassLoader());
    }

    public boolean isEmpty() {
        return collectedPhotos.isEmpty();
    }

    public void addPhoto(CollectedPhoto collectedPhoto) {
        collectedPhotos.add(collectedPhoto);
    }

    public void removePhoto(int position, Context context) {
        CollectedPhoto photo = collectedPhotos.remove(position);
        photo.delete(context);
    }

    public Date date() {
        Date date = Calendar.getInstance().getTime();
        for (CollectedPhoto photo: collectedPhotos){
            if (photo.date.before(date))
                date = photo.date;
        }
        return date;
    }

    public List<String> photos(){
        List<String> photos = new ArrayList<String>();
        for (CollectedPhoto photo: collectedPhotos)
            photos.add(photo.photo);
        return photos;
    }

    public Double latitude() {
        Double latitudeSum = 0.0;
        for (CollectedPhoto photo : collectedPhotos)
            latitudeSum = latitudeSum + photo.latitude;

        return latitudeSum/collectedPhotos.size();
    }

    public Double longitude() {
        Double longitudeSum = 0.0;
        for (CollectedPhoto photo : collectedPhotos)
            longitudeSum = longitudeSum + photo.longitude;

        return longitudeSum/collectedPhotos.size();
    }

    public void delete(Context context) {
        for (CollectedPhoto photo: collectedPhotos)
            photo.delete(context);
    }

    public int describeContents() {
        return 0;
    }

    /** save object in parcel */
    public void writeToParcel(Parcel out, int flags) {
        out.writeList(collectedPhotos);
    }

    public static final Parcelable.Creator<SuggestedPost> CREATOR
            = new Parcelable.Creator<SuggestedPost>() {
        public SuggestedPost createFromParcel(Parcel in) {
            try {
                return new SuggestedPost(in);
            } catch (ParseException e) {
                return new SuggestedPost();
            }
        }

        public SuggestedPost[] newArray(int size) {
            return new SuggestedPost[size];
        }
    };

}
