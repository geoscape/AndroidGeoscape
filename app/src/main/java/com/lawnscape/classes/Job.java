package com.lawnscape.classes;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Mellis on 2/17/2017.
 *
 * By being a parcelable object, any Job object can be passed as an intent extra
 * See the bottom half of this class file to understand
 *
 * Must have a get method for any variable to be 'parcelable'
 */

public class Job implements Parcelable {
    private String title;
    private String location;
    private String description;
    private String category;
    private final String userid;
    private final String postid;
    private String date;
    private final String latitude;
    private final String longitude;

    public Job(String postDate, String t, String l, String c, String u, String lat, String lng) {
        date = postDate;
        title = t;
        location = l;
        category = c;
        userid = u;
        postid = "";
        latitude = lat;
        longitude = lng;
    }
    public Job(String postDate,String t, String l, String c, String d, String u, String p, String lat, String lng) {
        date = postDate;
        title = t;
        location = l;
        category = c;
        description = d;
        userid = u;
        postid = p;
        latitude = lat;
        longitude = lng;
    }

    public String getLatitude(){return latitude;}
    public String getLongitude(){return longitude;}
    public String getPostid(){ return postid; }


    public String getDescription() {
        return description;
    }
    public void setDescription(String d){ description = d;}

    public String getTitle() {
        return title;
    }
    public void setTitle(String t){
        title = t;
    }

    public String getUserid() {
        return userid;
    }

    public String getLocation() {
        return location;
    }
    public void setLocation(String l){
        location = l;
    }

    public String getDate() { return date;}
    public void setDate(String newDate){date = newDate;}

    public String getCategory() {return category; }
    public void setCategory(String c) { category = c; }


/***************** PARCEL PORTION *****************/
//The only reason these things are here is so a Job object
//Can be passed as an Intent Extra, google it
    public Job(Parcel in){
        String[] data= new String[9];

        in.readStringArray(data);
        this.title= data[0];
        this.location= data[1];
        this.category= data[2];
        this.description= data[3];
        this.userid= data[4];
        this.postid= data[5];
        this.date= data[6];
        this.latitude = data[7];
        this.longitude= data[8];

    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[]{
                this.title,
                this.location,
                this.category,
                this.description,
                this.userid,
                this.postid,
                this.date,
                this.latitude,
                this.longitude
        });
    }

    public static final Parcelable.Creator<Job> CREATOR= new Parcelable.Creator<Job>() {
        @Override
        public Job createFromParcel(Parcel in) {
            //using parcelable constructor
            return new Job(in);
        }
        @Override
        public Job[] newArray(int size) {
            return new Job[size];
        }
    };
}
