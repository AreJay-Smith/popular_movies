package com.arejaysmith.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Created by Urge_Smith on 6/28/16.
 */
public class Movie implements Parcelable {

    private int id;
    private String title;
    private String description;
    private double rating;
    private String posterPath;
    private String date;

    public void Movie() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public static final Parcelable.Creator<Movie> CREATOR = new Creator<Movie>() {
        public Movie createFromParcel(Parcel source) {
            Movie mMovie = new Movie();
            mMovie.id = source.readInt();
            mMovie.title = source.readString();
            mMovie.description = source.readString();
            mMovie.rating = source.readDouble();
            mMovie.posterPath = source.readString();
            mMovie.date = source.readString();
            return mMovie;
        }

        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    public int describeContents() {
        return 0;
    }
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(id);
        parcel.writeString(title);
        parcel.writeString(description);
        parcel.writeDouble(rating);
        parcel.writeString(posterPath);
        parcel.writeString(date);
    }
}
