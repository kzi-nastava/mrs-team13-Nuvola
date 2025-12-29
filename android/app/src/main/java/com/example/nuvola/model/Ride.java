package com.example.nuvola.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Ride implements Parcelable {
    public Ride() {}
    public long id;
    public double price;
    public String dropoff;
    public String pickup;
    public LocalDateTime startingTime;
    public String driver;
    public boolean isFavouriteRoute;

    protected Ride(Parcel in) {
        id = in.readLong();
        price = in.readDouble();
        dropoff = in.readString();
        pickup = in.readString();
        driver = in.readString();
        isFavouriteRoute = in.readByte() != 0;

        String dateTimeString = in.readString();
        if (dateTimeString != null) {
            startingTime = LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm"));
        }
    }

    public static final Creator<Ride> CREATOR = new Creator<Ride>() {
        @Override
        public Ride createFromParcel(Parcel in) {
            return new Ride(in);
        }

        @Override
        public Ride[] newArray(int size) {
            return new Ride[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeDouble(price);
        dest.writeString(dropoff);
        dest.writeString(pickup);
        dest.writeString(driver);
        dest.writeByte((byte) (isFavouriteRoute ? 1 : 0));
        dest.writeString(startingTime != null ? startingTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")) : null);
    }
}
