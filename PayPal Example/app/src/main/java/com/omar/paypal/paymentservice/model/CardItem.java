package com.omar.paypal.paymentservice.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Omar on 7/10/2017.
 */

public class CardItem extends Product implements Parcelable{
    int quantity;

    protected CardItem(Parcel in) {
        quantity = in.readInt();
    }

    public static final Creator<CardItem> CREATOR = new Creator<CardItem>() {
        @Override
        public CardItem createFromParcel(Parcel in) {
            return new CardItem(in);
        }

        @Override
        public CardItem[] newArray(int size) {
            return new CardItem[size];
        }
    };

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(quantity);
    }
}
