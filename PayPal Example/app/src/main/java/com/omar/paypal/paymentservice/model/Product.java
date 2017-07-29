package com.omar.paypal.paymentservice.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by Omar on 7/8/2017.
 */

public class Product implements Parcelable{
    String name,description,sku,image;
    double price;
    int quantity = 1;

    public Product(){}

    public Product(String name, String description, String sku, String image, double price) {
        this.name = name;
        this.description = description;
        this.sku = sku;
        this.image = image;
        this.price = price;
    }

    protected Product(Parcel in) {
        name = in.readString();
        description = in.readString();
        sku = in.readString();
        image = in.readString();
        price = in.readDouble();
        quantity = in.readInt();
    }

    public static final Creator<Product> CREATOR = new Creator<Product>() {
        @Override
        public Product createFromParcel(Parcel in) {
            return new Product(in);
        }

        @Override
        public Product[] newArray(int size) {
            return new Product[size];
        }
    };

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getSku() {
        return sku;
    }

    public String getImage() {
        return image;
    }

    public double getPrice() {
        return price;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public ArrayList<Product>getDummyProducts(){
        ArrayList<Product>DummyProducts = new ArrayList<>();
        DummyProducts.add( new Product("Google Nexus 6" , "Midnight Blue, with 32 GB" , "sku-2123wers100"
                , "http://api.androidhive.info/images/nexus5.jpeg" , 650.5));

        DummyProducts.add( new Product("Sandisk Cruzer Blade 16 GB Flash Pendrive" , "USB 2.0, 16 GB, Black & Red, Read 17.62 MB/sec, Write 4.42 MB/sec" , "sku-78955545w"
                , "http://api.androidhive.info/images/sandisk.jpeg" , 326));

        DummyProducts.add( new Product("Kanvas Katha Backpack" , "1 Zippered Pocket Outside at Front, Loop Handle, Dual Padded Straps at the Back, 1 Compartment" , "sku-8493948kk4"
                , "http://api.androidhive.info/images/backpack.jpeg" , 285));

        DummyProducts.add( new Product("Prestige Pressure Cooker" , "Prestige Induction Starter Pack Deluxe Plus Pressure Cooker 5 L" , "sku-90903034ll"
                , "http://api.androidhive.info/images/prestige.jpeg" , 50));

        return DummyProducts;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(description);
        parcel.writeString(sku);
        parcel.writeString(image);
        parcel.writeDouble(price);
        parcel.writeInt(quantity);
    }

    public int getQuantity() {
        return quantity;
    }

}
