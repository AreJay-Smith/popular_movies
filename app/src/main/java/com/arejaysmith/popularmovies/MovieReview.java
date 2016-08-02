package com.arejaysmith.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Urge_Smith on 7/26/16.
 */
public class MovieReview implements Parcelable{

    private String author;
    private String content;

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public static final Parcelable.Creator<MovieReview> CREATOR = new Parcelable.Creator<MovieReview>() {
        public MovieReview createFromParcel(Parcel source) {
            MovieReview mMovieReview = new MovieReview();
            mMovieReview.author = source.readString();
            mMovieReview.content = source.readString();

            return mMovieReview;
        }

        public MovieReview[] newArray(int size) {
            return new MovieReview[size];
        }
    };

    public int describeContents() {
        return 0;
    }
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(author);
        parcel.writeString(content);
    }
}
